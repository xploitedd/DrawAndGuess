package pt.isel.pdm.drag.game

import android.app.Application
import androidx.annotation.CallSuper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import pt.isel.pdm.drag.DragApplication
import pt.isel.pdm.drag.database.entities.Game
import pt.isel.pdm.drag.game.model.*
import pt.isel.pdm.drag.repo.WordLanguage
import pt.isel.pdm.drag.util.Scheduler
import pt.isel.pdm.drag.util.launchTask
import java.time.Duration
import java.time.Instant

const val GAME_MIN_PLAYERS = 5
const val GAME_STATE_KEY = "GAME_STATE"
const val GAME_ENTRY_KEY = "GAME_ENTRY"

abstract class AbstractGameViewModel(
    application: Application,
    protected val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    protected val dragApplication: DragApplication by lazy { getApplication() }
    private val wordRepository by lazy { dragApplication.wordRepository }
    protected val gameRepository by lazy { dragApplication.gameRepository }

    protected var gameEntry: Game? = null
    var localGameId: Long? = gameEntry?.gameId ?: savedStateHandle[GAME_ENTRY_KEY]
        private set(value) {
            field = value
            savedStateHandle[GAME_ENTRY_KEY] = value
        }


    abstract val pLiveBoard: MutableLiveData<DrawingBoard>
    protected val currentLiveBoard get() = pLiveBoard.value!!
    val liveBoard: LiveData<DrawingBoard> get() = pLiveBoard

    private val _timer = MutableLiveData<Long>()
    val timer: LiveData<Long> = _timer

    protected val pGame = MutableLiveData<GameState>()
    val game: LiveData<GameState> = pGame

    abstract val state: BlockState
    open val error: String? = null

    /**
     * Starts a new game with the specified configuration
     * @param config the game configuration
     * @param playerConfig the player configuration, used for online mode
     */
    abstract fun startGame(config: GameConfiguration, playerConfig: PlayerConfiguration? = null)

    /**
     * Updates the current board with the new line
     * @param newLine new line to draw to the board
     * @see GameState
     */
    abstract fun updateBoard(newLine: List<Vector2D>? = null)

    /**
     * Resets the current board, removing all existing lines
     * @see GameState
     */
    abstract fun resetBoard()

    /**
     * Updates the current word
     * @param newWord the current word
     * @see GameState
     */
    abstract fun updateWord(newWord: String)

    /**
     * Check whether this game has finished
     * @return true if finished, false otherwise
     */
    protected abstract fun hasFinished(): Boolean

    /**
     * Schedules the next round
     */
    protected abstract fun scheduleNextRound()

    /**
     * Switches the current game state to the new one
     */
    protected abstract suspend fun switchGameState()

    /**
     * Creates an entry in the local game database
     * @param players the number of players
     * @param roundTime the round time for this game
     */
    protected suspend fun createLocalGame(players: Int, roundTime: Long) {
        gameEntry = gameRepository.createNewGame(players, roundTime)
        localGameId = gameEntry!!.gameId
    }

    /**
     * Loads the existing local game
     */
    protected suspend fun loadLocalGame() {
        // throw exception if the localGameId isn't configured upon calling this method
        gameEntry = gameRepository.getGame(localGameId!!)
    }

    /**
     * Handles a error while doing an operation
     * The implementing class must call this implementation
     * @param error message representing the error
     */
    @CallSuper
    protected open fun handleError(error: String) {
        gameEntry?.let {
            launchTask({}, {
                gameRepository.removeGame(it)
            })
        }
    }

    /**
     * Gets a random word from the API with the specified language
     * @param lang the language of the word
     * @return a word in the specified language
     */
    protected suspend fun getRandomWord(lang: WordLanguage) =
        wordRepository.getRandomWord(lang).word

    /**
     * Schedules a timer for each round
     */
    protected fun scheduleTimer(roundTime: Long, start: () -> Long) {
        Scheduler.runAtFixedRate(Duration.ofMillis(500)) {
            var timer = roundTime - Instant.now().minusSeconds(start()).epochSecond
            if (timer < 0)
                timer = 0

            _timer.value = timer
        }
    }

}