package pt.isel.pdm.drag.game

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.work.*
import pt.isel.pdm.drag.DragApplication
import pt.isel.pdm.drag.game.model.*
import pt.isel.pdm.drag.util.LiveUtils
import pt.isel.pdm.drag.util.Scheduler
import pt.isel.pdm.drag.util.launchTask
import java.time.Duration

private const val GAME_PLAYER_KEY = "GAME_PLAYER"
private const val GAME_ID = "GAME_ID"
private const val PLAYER_ID = "PLAYER_ID"

class LiveGameViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AbstractGameViewModel(application, savedStateHandle) {

    private val liveGameRepository = dragApplication.liveGameRepository

    private var cleanupCalled = false
    private var pState: LiveGameState = savedStateHandle[GAME_STATE_KEY] ?: LiveGameState("", -1, -1, mutableMapOf(), mapOf())
        set(value) {
            field = value
            savedStateHandle[GAME_STATE_KEY] = value
            currentBlockState?.let {
                pLiveBoard.value = it.block.board
            }
        }

    private var player = savedStateHandle[GAME_PLAYER_KEY] ?: LocalPlayerState("", -1, -1)
        set(value) {
            field = value
            savedStateHandle[GAME_PLAYER_KEY] = value
        }

    private var currentBlockState: LiveGameBlockState? = null
        get() = pState.blockStates[player.blockId.toString()]
        set(value) {
            field = value
            if (value != null) {
                val blockId = player.blockId.toString()
                pState.blockStates[blockId] = value
                pLiveBoard.value = value.block.board
            }
        }

    private val nextState: GameState
        get() {
            return if (pState.gameState == GameState.DRAWING)
                GameState.GUESSING
            else
                GameState.DRAWING
        }

    override val pLiveBoard: MutableLiveData<DrawingBoard> = MutableLiveData()
    override val state: BlockState get() = currentBlockState!!.block
    override val error: String? get() = pState.error
    private val isHost: Boolean get() = player.playerId == 0
    private val nextBlockId get() = (player.blockId + 1) % pState.maxPlayers
    val gameId get() = pState.id

    override fun startGame(config: GameConfiguration, playerConfig: PlayerConfiguration?) {
        if (gameId == "") {
            val players = config.players
            val roundTime = config.roundTime

            if (players < GAME_MIN_PLAYERS) {
                handleLocalError("The minimum number of players is 5!")
                return
            }

            if (playerConfig == null) {
                handleLocalError("Invalid player configuration")
                return
            }

            if (playerConfig.playerName == "") {
                handleLocalError("Please choose a player name")
                return
            }

            player = LocalPlayerState(playerConfig.playerName, 0, 0)
            pState = LiveGameState(
                LiveUtils.generateRandomId(),
                roundTime,
                players,
                mutableMapOf(),
                mapOf("0" to RemotePlayerState(player.playerName, 0)),
                GameState.WAITING,
                config.language
            )

            launchAction {
                liveGameRepository.createNewGame(pState)
                configureGame()
            }
        } else {
            resumeGame()
        }
    }

    /**
     * Join an existing online game
     * @param playerConfig the player configuration containing the online game id
     * @see PlayerConfiguration
     */
    fun joinGame(playerConfig: PlayerConfiguration) {
        if (gameId == "") {
            if (playerConfig.playerName == "") {
                handleLocalError("Please choose a player name")
                return
            }

            if (playerConfig.gameId == null) {
                handleLocalError("Invalid game id")
                return
            }

            launchAction {
                val info = liveGameRepository.joinGame(playerConfig)
                val remote = info.second
                pState = info.first
                player = LocalPlayerState(playerConfig.playerName, remote.blockId, remote.blockId)
                configureGame()
            }
        } else {
            resumeGame()
        }
    }

    /**
     * Resumes the current game
     * Useful when the current activity needs to be re-created
     */
    private fun resumeGame() {
        launchAction {
            loadLocalGame()
            pGame.value = pState.gameState
        }
    }

    /**
     * Configure the online game for this device
     */
    private suspend fun configureGame() {
        pGame.value = pState.gameState
        liveGameRepository.listenForErrorState(pState) { handleError(it) }
        pState = liveGameRepository.waitForPlayers(pState)

        val startingState = if (pState.maxPlayers % 2 == 0)
            GameState.DRAWING
        else
            GameState.PICK_WORD

        val playerBlock = if (startingState == GameState.DRAWING) {
            val word = getRandomWord(pState.lang)
            LiveGameBlockState(player.playerId, BlockState(word))
        } else {
            LiveGameBlockState(player.playerId, BlockState())
        }

        // publish player state
        liveGameRepository.publishBlockState(pState.id, player.blockId, playerBlock)
        if (isHost) {
            // if this player is the host then change the game state when every player is
            // accounted for (they published their block state)
            pState = liveGameRepository.waitForBlockStates(pState)
            pState = liveGameRepository.changeGameState(pState, startingState)
        } else {
            pState = liveGameRepository.waitForGameStart(pState)
        }

        createLocalGame(pState.maxPlayers, pState.roundTime)
        pGame.value = pState.gameState

        // set the roundStart to current player time
        player = LocalPlayerState(player.playerName, player.playerId, player.blockId)
        scheduleNextRound()
        scheduleTimer(pState.roundTime) { player.roundStart.epochSecond }
    }

    override fun updateBoard(newLine: List<Vector2D>?) {
        if (pState.gameState != GameState.DRAWING)
            return

        val board = if (newLine != null)
            currentLiveBoard + newLine
        else
            currentLiveBoard

        if (!currentBlockState!!.completed) {
            currentBlockState = LiveGameBlockState(
                currentBlockState!!.curPlayer,
                BlockState(currentBlockState!!.block.word, board)
            )
        }
    }

    override fun resetBoard() {
        if (pState.gameState != GameState.DRAWING)
            return

        if (!currentBlockState!!.completed) {
            currentBlockState = LiveGameBlockState(
                currentBlockState!!.curPlayer,
                BlockState(currentBlockState!!.block.word)
            )
        }
    }

    override fun updateWord(newWord: String) {
        if (pState.gameState != GameState.GUESSING && pState.gameState != GameState.PICK_WORD)
            return

        if (!currentBlockState!!.completed) {
            currentBlockState = LiveGameBlockState(
                currentBlockState!!.curPlayer,
                BlockState(newWord, currentBlockState!!.block.board)
            )
        }
    }

    override fun hasFinished() = nextBlockId == player.playerId

    override fun scheduleNextRound() {
        Scheduler.runDelayed(Duration.ofSeconds(pState.roundTime)) {
            launchAction {
                val blockState = currentBlockState!!.block
                if ((pState.gameState == GameState.GUESSING || pState.gameState == GameState.PICK_WORD) && blockState.word == "")
                    throw Exception("The game was terminated due to a player being away")

                val liveBlockState = LiveGameBlockState(player.playerId, blockState, true)
                liveGameRepository.publishBlockState(gameId, player.blockId, liveBlockState)
                // wait for every round to be completed
                pState = liveGameRepository.waitForCompleted(pState)
                // add round results to local db
                addResultsToLocalDb()

                if (hasFinished()) {
                    pState = if (isHost) {
                        liveGameRepository.changeGameState(pState, GameState.FINISHED)
                    } else {
                        liveGameRepository.waitForNextState(pState, GameState.FINISHED)
                    }

                    cleanup()
                } else {
                    if (isHost) switchGameState()
                    else switchLocalState()
                    scheduleNextRound()
                }

                pGame.value = pState.gameState
            }
        }
    }

    /**
     * Adds the latest round to the local database
     */
    private suspend fun addResultsToLocalDb() {
        val round = gameRepository.addRoundToGame(gameEntry!!, pState.gameState)
        pState.blockStates.forEach { (_, bs) ->
            val playerId = bs.curPlayer
            val remotePlayer = pState.joinedPlayers[playerId.toString()]
                ?: throw Exception("An unexpected error occurred!")

            gameRepository.addRoundResult(round, bs.block, playerId, remotePlayer.playerName)
        }
    }

    override suspend fun switchGameState() {
        publishNextBlockState()
        pState = liveGameRepository.waitForCompleted(pState, false)
        switchPlayerState()
        pState = liveGameRepository.changeGameState(pState, nextState)
    }

    /**
     * Publishes the new local state and waits for every player
     */
    private suspend fun switchLocalState() {
        publishNextBlockState()
        switchPlayerState()
        pState = liveGameRepository.waitForNextState(pState, nextState)
    }

    /**
     * Switches the local player state to the next round state
     */
    private fun switchPlayerState() {
        player = LocalPlayerState(
            player.playerName,
            player.playerId,
            nextBlockId
        )
    }

    /**
     * Publishes the next state to the online services
     */
    private suspend fun publishNextBlockState() {
        val nextBlockIdStr = nextBlockId.toString()
        val curNext = pState.blockStates[nextBlockIdStr]!!
        val nextBlock = if (nextState == GameState.DRAWING) {
            LiveGameBlockState(
                player.playerId,
                BlockState(curNext.block.word)
            )
        } else {
            LiveGameBlockState(
                player.playerId,
                BlockState("", curNext.block.board)
            )
        }

        liveGameRepository.publishBlockState(pState.id, nextBlockId, nextBlock)
    }

    /**
     * Useful method to launch an action that may throw an error
     * This method also tries to publish the error to the online services in
     * an effort to alert other players
     * @param action action that may throw an error
     */
    private fun launchAction(action: suspend () -> Unit) {
        launchTask({ error ->
            handleError(error)
            liveGameRepository.setError(pState, error)
        }, action)
    }

    /**
     * Cleans up the local view model, launching a job
     * to disconnect the player from the current game
     */
    private fun cleanup() {
        if (cleanupCalled)
            return

        cleanupCalled = true
        liveGameRepository.unregisterListener(pState)
        // launch a job so the player can quit the current game
        val inputData = Data.Builder()
            .putString(GAME_ID, gameId)
            .putInt(PLAYER_ID, player.playerId)
            .build()

        val cleanupJob = OneTimeWorkRequestBuilder<CleanupWorker>()
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()

        WorkManager.getInstance(dragApplication).enqueue(cleanupJob)
    }

    override fun handleError(error: String) {
        super.handleError(error)
        cleanup()
        handleLocalError(error)
    }

    /**
     * Handles an error that has occurred while setting up
     * It's also used by the handleError method to notify the activity of the error
     */
    private fun handleLocalError(error: String) {
        pState = LiveGameState(
            pState.id,
            pState.roundTime,
            pState.maxPlayers,
            pState.blockStates,
            pState.joinedPlayers,
            GameState.ERROR,
            pState.lang,
            error
        )

        pGame.value = pState.gameState
    }

    override fun onCleared() {
        super.onCleared()
        if (gameId != "") {
            cleanup()
        }
    }

}

class CleanupWorker(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {

    private val app = applicationContext as DragApplication
    private val liveRepo = app.liveGameRepository

    override suspend fun doWork(): Result {
        return try {
            val gameId = inputData.getString(GAME_ID)!!
            val playerId = inputData.getInt(PLAYER_ID, -1)
            if (playerId == -1)
                throw IllegalStateException("player id was not passed to cleanup worker")

            liveRepo.quitGame(gameId, playerId)
            Result.success()
        } catch (ex: Exception) {
            Result.retry()
        }
    }

}