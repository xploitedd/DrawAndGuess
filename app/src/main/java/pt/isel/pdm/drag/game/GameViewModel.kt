package pt.isel.pdm.drag.game

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import pt.isel.pdm.drag.game.model.*
import pt.isel.pdm.drag.util.Scheduler
import pt.isel.pdm.drag.util.launchTask
import java.time.Duration

class GameViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AbstractGameViewModel(application, savedStateHandle) {

    private var pState = savedStateHandle.get(GAME_STATE_KEY) ?: GameViewState()
        set(value) {
            field = value
            savedStateHandle[GAME_STATE_KEY] = value
            pLiveBoard.value = value.block.board
        }

    override val pLiveBoard: MutableLiveData<DrawingBoard> = MutableLiveData(pState.block.board)
    override val state: BlockState get() = pState.block
    override val error: String? get() = pState.error

    override fun startGame(config: GameConfiguration, playerConfig: PlayerConfiguration?) {
        // if a game is underway only push the state to the live data
        if (pState.player == -1) {
            val players = config.players
            val roundTime = config.roundTime

            if (players < GAME_MIN_PLAYERS) {
                handleError("There should be at least 5 players!")
                return
            }

            launchTask(this::handleError) {
                var startState: GameState = GameState.PICK_WORD
                var word = ""

                if (players % 2 == 0) {
                    startState = GameState.DRAWING
                    word = getRandomWord(config.language)
                }

                createLocalGame(players, roundTime)
                pState = GameViewState(players, startState, BlockState(word))

                scheduleNextRound()
                scheduleTimer(roundTime) { pState.roundStart.epochSecond }
                pGame.value = pState.gameState
            }
        } else {
            launchTask(this::handleError) {
                loadLocalGame()
                pGame.value = pState.gameState
            }
        }
    }

    override fun updateBoard(newLine: List<Vector2D>?) {
        if (pState.gameState != GameState.DRAWING)
            return

        val board = if (newLine != null)
            currentLiveBoard + newLine
        else
            currentLiveBoard

        pState = GameViewState(
            pState.player,
            GameState.DRAWING,
            BlockState(pState.block.word, board),
            pState.roundStart
        )
    }

    override fun resetBoard() {
        if (pState.gameState != GameState.DRAWING)
            return

        pState = GameViewState(
            pState.player,
            GameState.DRAWING,
            BlockState(pState.block.word),
            pState.roundStart
        )
    }

    override fun updateWord(newWord: String) {
        if (pState.gameState == GameState.DRAWING)
            return

        pState = GameViewState(
            pState.player,
            GameState.GUESSING,
            BlockState(newWord, pState.block.board),
            pState.roundStart
        )
    }

    override fun hasFinished() = pState.player - 1 == 0

    override fun scheduleNextRound() {
        Scheduler.runDelayed(Duration.ofSeconds(gameEntry!!.roundTime)) {
            launchTask(this::handleError) {
                val round = gameRepository.addRoundToGame(gameEntry!!, pState.gameState)
                gameRepository.addRoundResult(round, pState.block, 0)

                if (hasFinished()) {
                    pGame.value = GameState.FINISHED
                } else {
                    switchGameState()
                    pGame.value = pState.gameState
                }
            }
        }
    }

    override fun handleError(error: String) {
        super.handleError(error)
        pState = GameViewState(
            pState.player,
            GameState.ERROR,
            pState.block,
            pState.roundStart,
            error
        )

        pGame.value = pState.gameState
    }

    override suspend fun switchGameState() {
        val newPlayer = pState.player - 1
        pState = if (pState.gameState == GameState.DRAWING) {
            GameViewState(
                newPlayer,
                GameState.GUESSING,
                BlockState(board = pState.block.board)
            )
        } else {
            GameViewState(
                newPlayer,
                GameState.DRAWING,
                BlockState(pState.block.word)
            )
        }

        scheduleNextRound()
    }

}