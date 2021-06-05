package pt.isel.pdm.drag.menu.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

const val GAME_ERROR_MESSAGE = "error"
const val PLAYER_NAME_MAX_CHARS = 10

private val requiredPermissions = arrayOf(
    Manifest.permission.ACCESS_NETWORK_STATE,
    Manifest.permission.INTERNET
)

abstract class DragFragment : Fragment() {

    private lateinit var permLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var gameLauncher: ActivityResultLauncher<Intent>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // creates activity result listeners
        permLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantedMap ->
            val anyDeny = grantedMap.any { (_, v) -> v == false }
            if (anyDeny) {
                onPermissionDenied()
            }
        }

        gameLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val error = it.data?.getStringExtra(GAME_ERROR_MESSAGE)
            if (error != null)
                onError(error)
        }
    }

    /**
     * Called when a game error occurs
     * Any classes that override this implementation must call the super method
     * @param error the error that occurred
     */
    @CallSuper
    protected open fun onError(error: String) {
        showError(error)
    }

    protected abstract fun onPermissionDenied()

    protected fun requestActivity(intent: Intent) = gameLauncher.launch(intent)

    protected fun showError(error: String) {
        Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG)
            .setAction("X") {}
            .show()
    }

    protected fun checkPermissions() {
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty())
            permLauncher.launch(missing.toTypedArray())
    }

}