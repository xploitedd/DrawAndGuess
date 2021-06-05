package pt.isel.pdm.drag.menu.games

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pt.isel.pdm.drag.DragApplication
import pt.isel.pdm.drag.database.entities.Game
import pt.isel.pdm.drag.repo.GameRepository
import pt.isel.pdm.drag.util.launchTask

class PreviousGamesViewModel(application: Application) : AndroidViewModel(application) {

    private val drag: DragApplication by lazy { getApplication() }
    private val gameRepository: GameRepository by lazy { drag.gameRepository }
    private val _error: MutableLiveData<String> = MutableLiveData()
    private val _games: MutableLiveData<List<Game>> = MutableLiveData()

    val error: LiveData<String> = _error
    val games: LiveData<List<Game>> = _games

    /**
     * Load previous games from the local database
     */
    fun loadGames() {
        launchTask(this::onError) {
            _games.value = gameRepository.getGames()
        }
    }

    /**
     * Remove the specified game from the local database
     * @param game game to be removed
     */
    fun removeGame(game: Game) {
        launchTask(this::onError) {
            gameRepository.removeGame(game)
            _games.value = gameRepository.getGames()
        }
    }

    /**
     * Notify the fragment about an error
     * @param error message representing the error
     */
    private fun onError(error: String) {
        _error.value = error
    }

}