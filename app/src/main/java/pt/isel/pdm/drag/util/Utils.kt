package pt.isel.pdm.drag.util

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.time.Duration

/**
 * Launches a task and catches any exception that it may throw
 * @param onError callback to be called when an error occurs
 * @param task the suspending task to be executed on the view model scope
 */
fun AndroidViewModel.launchTask(onError: (String) -> Unit, task: suspend () -> Unit) {
    viewModelScope.launch {
        try {
            task()
        } catch (ex: Exception) {
            val message = ex.message ?: "Unexpected Error"
            onError(message)
        }
    }
}

object Scheduler {

    private val handler = Handler(Looper.getMainLooper())

    /**
     * Run an action delayed on the main thread
     * @param delay delay to run this action
     * @param action the action to run on the main thread after the delay has passed
     */
    fun runDelayed(delay: Duration, action: () -> Unit) {
        handler.postDelayed(action, delay.toMillis())
    }

    /**
     * Run an action at a fixed rate on the main thread
     * The specified action is going to be run for its first time once this method is called
     * @param rate the rate to run the action at
     * @param action the action to run
     */
    fun runAtFixedRate(rate: Duration, action: () -> Unit) {
        action()
        runAtFixedRateR(rate, action)
    }

    private fun runAtFixedRateR(rate: Duration, action: () -> Unit) {
        runDelayed(rate) {
            action()
            runAtFixedRateR(rate, action)
        }
    }

}

const val LIVE_ID_CHARS = 6
object LiveUtils {

    private val secureRandom = SecureRandom()

    /**
     * Generates a random online lobby id
     * @return online lobby id
     */
    fun generateRandomId(): String {
        val strBuilder = StringBuilder()
        repeat(LIVE_ID_CHARS) {
            val randomChar = 'A' + secureRandom.nextInt('Z' - 'A' + 1)
            strBuilder.append(randomChar)
        }

        return strBuilder.toString()
    }

}