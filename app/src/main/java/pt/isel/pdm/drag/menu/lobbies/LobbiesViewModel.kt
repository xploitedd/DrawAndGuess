package pt.isel.pdm.drag.menu.lobbies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pt.isel.pdm.drag.DragApplication
import pt.isel.pdm.drag.repo.WordLanguage
import pt.isel.pdm.drag.util.launchTask

data class LobbyData(
    val id: String,
    val joinedPlayers: Int,
    val maxPlayers: Int,
    val lang: WordLanguage
)

class LobbiesViewModel(application: Application): AndroidViewModel(application) {

    private val drag: DragApplication by lazy { getApplication() }
    private val liveGameRepository by lazy { drag.liveGameRepository }
    private val _lobbyRefreshError: MutableLiveData<String> = MutableLiveData()
    private val _lobbies: MutableLiveData<List<LobbyData>> = MutableLiveData()

    val lobbyRefreshError: LiveData<String> = _lobbyRefreshError
    val lobbies: LiveData<List<LobbyData>> = _lobbies

    /**
     * Get available online lobbies
     */
    fun getAvailableLobbies() {
        launchTask(this::onError) {
            val availableLobbies = liveGameRepository.getAvailableLobbies()
            _lobbies.value = availableLobbies.map {
                LobbyData(
                    it.id,
                    it.joinedPlayers.filterValues { v -> v != null }.size,
                    it.maxPlayers,
                    it.lang
                )
            }
        }
    }

    /**
     * Notify the fragment about an error
     * @param error message representing the error
     */
    private fun onError(error: String) {
        _lobbyRefreshError.value = error
    }

}