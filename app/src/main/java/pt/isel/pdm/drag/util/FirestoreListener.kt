package pt.isel.pdm.drag.util

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import pt.isel.pdm.drag.game.model.Dto
import pt.isel.pdm.drag.game.model.Model
import java.time.Duration
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass

typealias FirestoreEvent<T> = (T) -> Boolean
typealias EventTrigger<T> = (T) -> Unit

data class EventContinuation<T>(
    val onSuccess: EventTrigger<T>,
    val onError: EventTrigger<Exception>,
    val singleUse: Boolean = true,
    var isDone: Boolean = false
)

private val UNEXPECTED_EXCEPTION = Exception("An unexpected error has occurred!")
class FirestoreListener<T : Dto<V>, V : Model<T>>(private val docRef: DocumentReference, private val clazz: KClass<T>) {

    companion object {

        private val listeners = mutableMapOf<String, FirestoreListener<*, *>>()

        /**
         * Gets an existing or new listener for the specified key
         * @param key the document key to get the listener for
         * @param colRef the collection that contains the document with the specified key
         * @param clazz class of the document Dto for mapping purposes
         */
        // don't worry about the unchecked casting warning - we're checking :)
        @Suppress("UNCHECKED_CAST")
        fun <T : Dto<V>, V : Model<T>> getListener(key: String, colRef: CollectionReference, clazz: KClass<T>): FirestoreListener<T, V> {
            val listener = listeners[key]
            if (listener != null) {
                if (listener.clazz == clazz) {
                    return listener as FirestoreListener<T, V>
                } else {
                    throw Exception("Invalid requested type!")
                }
            }

            val docRef = colRef.document(key)
            val newListener = FirestoreListener(docRef, clazz)
            listeners[key] = newListener
            return newListener
        }

        /**
         * Removes the listener associated with the document key
         * If the key has no listener then it does nothing
         * @param key document key to remove the listener for
         */
        fun removeListener(key: String) {
            val listener = listeners[key]
            if (listener != null) {
                listener.detach()
                listeners.remove(key)
            }
        }

    }

    private val eventMap = mutableMapOf<EventContinuation<V>, FirestoreEvent<V>>()
    private var listener = createListener()
    private var isListening = true

    /**
     * Listen for all events that occur in the document
     * @param event event to listen for
     * @param onSuccess when the event listener is successful
     * @param onError when an error has occurred, preventing this listener from continuing
     */
    fun listenForEvents(event: FirestoreEvent<V>, onSuccess: EventTrigger<V>, onError: EventTrigger<Exception>) {
        val continuation = EventContinuation(onSuccess, onError, false)
        eventMap[continuation] = event
        doChecks()
    }

    /**
     * Listen once for a specified event
     * @param timeout timeout for this event
     * @param event event to listen for
     * @return the mapped model object if the event was successful
     * @throws Exception an exception if a timeout or some other error has occurred
     */
    suspend fun listenForEvent(timeout: Duration? = null, event: FirestoreEvent<V>): V {
        return suspendCoroutine {
            val continuation = EventContinuation<V>(
                { obj -> it.resume(obj) },
                { ex -> it.resumeWithException(ex) }
            )

            timeout?.let {
                Scheduler.runDelayed(it) {
                    if (!continuation.isDone) {
                        continuation.isDone = true
                        continuation.onError(TimeoutException("The event has timed out"))
                    }
                }
            }

            eventMap[continuation] = event
            doChecks()
        }
    }

    /**
     * Do event checks, like checking the listener status
     * or checking if the event has already happened but the listener didn't catch it
     */
    private fun doChecks() {
        if (!isListening) {
            // try to create another listener
            listener = createListener()
            isListening = true
        }

        // check if the event has already happened
        docRef.get()
            .addOnSuccessListener { snap -> handleSnapshot(snap) }
            .addOnFailureListener { ex -> failAll(ex) }
    }

    /**
     * Create a new listener for this document
     * @return the listener registration
     */
    private fun createListener(): ListenerRegistration {
        return docRef.addSnapshotListener { snap, ex ->
            if (isListening) {
                if (ex != null) {
                    failAll(ex)
                } else if (snap != null && snap.exists()) {
                    handleSnapshot(snap)
                }
            }
        }
    }

    /**
     * Handles the snapshot of the document listener
     * @param snap document snapshot
     */
    private fun handleSnapshot(snap: DocumentSnapshot) {
        val obj = snap.toObject(clazz.java)
        if (obj == null) {
            failAll(UNEXPECTED_EXCEPTION)
        } else {
            satisfyAll(obj.mapToModel())
        }
    }

    /**
     * Satisfy all events that can be satisfied by the specified object
     * @param obj the document Dto
     */
    private fun satisfyAll(obj: V) {
        val satisfied = mutableListOf<EventContinuation<V>>()
        eventMap.forEach { (c, e) ->
            if (c.isDone && c.singleUse) {
                satisfied.add(c)
            } else if (e(obj)) {
                if (c.singleUse) {
                    c.isDone = true
                    satisfied.add(c)
                }

                c.onSuccess(obj)
            }
        }

        satisfied.forEach { eventMap.remove(it) }
    }

    /**
     * Fail all events that depend on this listener
     * Removes the events and detaches the current listener
     * @param ex the exception that occurred
     */
    private fun failAll(ex: Exception) {
        eventMap.forEach { (c, _) ->
            if (!c.isDone) {
                if (c.singleUse)
                    c.isDone = true

                c.onError(ex)
            }
        }

        eventMap.clear()
        detach()
    }

    /**
     * Detach the current listener
     */
    private fun detach() {
        isListening = false
        listener.remove()
    }

}
