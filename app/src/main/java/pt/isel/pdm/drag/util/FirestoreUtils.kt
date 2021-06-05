package pt.isel.pdm.drag.util

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import pt.isel.pdm.drag.game.model.Dto
import java.time.Duration
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass

private typealias Resume<T> = (T) -> Unit

/**
 * Run a task in this collection
 * @param timeout task timeout
 * @param taskSupplier supplier of the task
 */
suspend fun <T> CollectionReference.runTask(timeout: Duration? = null, taskSupplier: (CollectionReference) -> Task<T>): T {
    return runTaskUtil(timeout, taskSupplier)
}

/**
 * Run a task in this document
 * @param timeout task timeout
 * @param taskSupplier supplier of the task
 */
suspend fun <T> DocumentReference.runTask(timeout: Duration? = null, taskSupplier: (DocumentReference) -> Task<T>): T {
    return runTaskUtil(timeout, taskSupplier)
}

/**
 * Run a task in the generic <V>
 * @param timeout timeout of the task
 * @param taskSupplier supplier of the task
 */
private suspend fun <T, V> V.runTaskUtil(timeout: Duration? = null, taskSupplier: (V) -> Task<T>): T {
    return suspendWithTimeout(timeout) { onSuccess, onError ->
        taskSupplier(this)
            .addOnSuccessListener { obj -> onSuccess(obj) }
            .addOnFailureListener { ex -> onError(ex) }
    }
}

/**
 * Start a new Firestore transaction
 * @param timeout timeout of the transaction
 * @param transaction transaction to be executed
 */
suspend fun <T> FirebaseFirestore.startTransaction(timeout: Duration? = null, transaction: (Transaction) -> T): T {
    return suspendWithTimeout(timeout) { onSuccess, onError ->
        runTransaction {
            transaction(it)
        }.addOnSuccessListener { obj -> onSuccess(obj) }
            .addOnFailureListener { ex -> onError(ex) }
    }
}

/**
 * Suspends the coroutine until it has reached the timeout or the task has been completed
 * @param timeout timeout to complete the task
 * @param task task to be completed
 * @return the value of the task, if completed with success
 * @throws Exception if there was an error while completing the task
 */
private suspend fun <T> suspendWithTimeout(timeout: Duration? = null, task: (Resume<T>, Resume<Exception>) -> Unit): T {
    return suspendCoroutine {
        // these tasks are supposed to be executed on the same thread
        var resumed = false
        val onSuccess: Resume<T> = { obj ->
            if (!resumed) {
                resumed = true
                it.resume(obj)
            }
        }

        val onError: Resume<Exception> = { ex ->
            if (!resumed) {
                resumed = true
                it.resumeWithException(ex)
            }
        }

        task(onSuccess, onError)
        timeout?.let { d ->
            Scheduler.runDelayed(d) {
                if (!resumed) {
                    resumed = true
                    it.resumeWithException(TimeoutException("The timeout has expired"))
                }
            }
        }
    }
}

/**
 * Maps a DocumentSnapshot to a Model object
 * @param dtoClass class of the dto associated with the model
 * @return the model object, or null if failed
 */
fun <T, V : Dto<T>> DocumentSnapshot.mapToObject(dtoClass: KClass<V>): T? {
    val dto = toObject(dtoClass.java)
    return dto?.mapToModel()
}