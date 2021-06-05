package pt.isel.pdm.drag.repo

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import pt.isel.pdm.drag.game.model.*
import pt.isel.pdm.drag.util.FirestoreListener
import pt.isel.pdm.drag.util.mapToObject
import pt.isel.pdm.drag.util.runTask
import pt.isel.pdm.drag.util.startTransaction
import java.time.Duration

private const val GAME_REF = "games"

class LiveGameRepository(private val db: FirebaseFirestore) {

    private val defaultTimeout = Duration.ofSeconds(3)
    private val gameCol = db.collection(GAME_REF)

    /**
     * Creates a new online game
     * @param state the initial game state
     * @see LiveGameState
     */
    suspend fun createNewGame(state: LiveGameState) {
        val ref = gameCol.document(state.id)
        db.startTransaction(defaultTimeout) { tran ->
            if (!tran.get(ref).exists()) {
                tran.set(ref, state.mapToDto())
            } else {
                throw FirebaseFirestoreException("Game ID collision", FirebaseFirestoreException.Code.ALREADY_EXISTS)
            }
        }
    }

    /**
     * Joins an existing online game
     * @param playerConfig PlayerConfiguration containing the player and game lobby information
     * @return a pair that contains the joined game state and the remote player state containing the player id
     * @see PlayerConfiguration
     * @see LiveGameState
     * @see RemotePlayerState
     */
    suspend fun joinGame(playerConfig: PlayerConfiguration): Pair<LiveGameState, RemotePlayerState> {
        val ref = gameCol.document(playerConfig.gameId!!)
        return db.startTransaction(defaultTimeout) { tran ->
            val state = tran.get(ref).mapToObject(LiveGameStateDto::class)
                ?: throw FirebaseFirestoreException("Invalid game id", FirebaseFirestoreException.Code.NOT_FOUND)

            when {
                state.gameState == GameState.ERROR || state.gameState == GameState.FINISHED -> {
                    throw FirebaseFirestoreException("Game unavailable!", FirebaseFirestoreException.Code.ABORTED)
                }
                state.gameState != GameState.WAITING -> {
                    throw FirebaseFirestoreException("Game already started!", FirebaseFirestoreException.Code.ABORTED)
                }
                state.joinedPlayers.size < state.maxPlayers -> {
                    val remote = findAvailableSlot(playerConfig, state.joinedPlayers)
                    val newMap = mutableMapOf<String, RemotePlayerState?>()
                    newMap.putAll(state.joinedPlayers)
                    newMap[remote.blockId.toString()] = remote
                    val newLiveGameState = LiveGameState(
                        state.id,
                        state.roundTime,
                        state.maxPlayers,
                        state.blockStates,
                        newMap,
                        state.gameState,
                        state.lang,
                        state.error
                    )

                    tran.update(ref, "joinedPlayers.${remote.blockId}", remote)
                    Pair(newLiveGameState, remote)
                }
                else -> {
                    throw FirebaseFirestoreException("Game is already full", FirebaseFirestoreException.Code.ABORTED)
                }
            }
        }
    }

    /**
     * Finds an available slot in an online game for this player
     * @param playerConfig player to find the slot for
     * @param joinedPlayers map of the players that have already joined
     * @return remote player information
     */
    private fun findAvailableSlot(playerConfig: PlayerConfiguration, joinedPlayers: Map<String, RemotePlayerState?>): RemotePlayerState {
        var blockId: Int = joinedPlayers.size
        (joinedPlayers.keys.indices).forEach { i ->
            // need this to check if any player has abandoned the game
            // and if there are name collisions
            val otherPlayer = joinedPlayers[i.toString()]
            if (otherPlayer == null) {
                blockId = i
            } else if (otherPlayer.playerName == playerConfig.playerName) {
                throw FirebaseFirestoreException("A player with the same name already exists", FirebaseFirestoreException.Code.ALREADY_EXISTS)
            }
        }

        return RemotePlayerState(
            playerConfig.playerName,
            blockId
        )
    }

    /**
     * Quit the specified game
     * @param gameId id of the game to quit
     * @param playerId id of the player that wants to quit
     */
    suspend fun quitGame(gameId: String, playerId: Int) {
        val ref = gameCol.document(gameId)
        return db.startTransaction(defaultTimeout) { tran ->
            val state = tran.get(ref).mapToObject(LiveGameStateDto::class)
                ?: throw FirebaseFirestoreException("Invalid game id", FirebaseFirestoreException.Code.NOT_FOUND)

            val gameState = state.gameState
            val joinedPlayers = state.joinedPlayers.filter { (_, v) -> v != null }
            if (gameState == GameState.PICK_WORD || gameState == GameState.DRAWING || gameState == GameState.GUESSING) {
                // if a player quits a running game then send an error for all players
                tran.update(ref, "gameState", GameState.ERROR)
                tran.update(ref, "error", "A player has quit the game!")
                tran.update(ref, "joinedPlayers.${playerId}", null)
            } else if (joinedPlayers.size == 1) {
                // the last remaining
                tran.delete(ref)
            } else {
                tran.update(ref, "joinedPlayers.${playerId}", null)
            }
        }
    }

    /**
     * Change the current game state
     * @param liveState the current live game state
     * @param state the next game state
     * @return the live game state with the new game state
     */
    suspend fun changeGameState(liveState: LiveGameState, state: GameState): LiveGameState {
        if (liveState.gameState == state)
            return liveState

        val gameRef = gameCol.document(liveState.id)
        gameRef.runTask(defaultTimeout) { it.update("gameState", state) }
        return gameRef.runTask(defaultTimeout) {
            it.get()
        }.mapToObject(LiveGameStateDto::class)!!
    }

    /**
     * Publishes a player block state
     * @param gameId id of the game
     * @param blockId id of the block to be published
     * @param blockState the live block state to be published
     */
    suspend fun publishBlockState(gameId: String, blockId: Int, blockState: LiveGameBlockState) {
        gameCol.document(gameId)
            .runTask(defaultTimeout) { it.update("blockStates.${blockId}", blockState.mapToDto()) }
    }

    /**
     * Get all available game lobbies, that is, lobbies that are in a WAITING state
     * and that are not full
     * @return a list of available lobbies
     */
    suspend fun getAvailableLobbies(): List<LiveGameState> {
        return gameCol.runTask(defaultTimeout) { it.get() }
            .documents
            .map{ it.mapToObject(LiveGameStateDto::class)!! }
            .filter {
                it.gameState == GameState.WAITING &&
                        it.joinedPlayers.filterValues { v -> v != null }.size < it.maxPlayers
            }
    }

    /**
     * Sets an error for the current game
     * Ignores all exceptions that are a result of this operation
     * @param state live game state of the game to set the error for
     * @param error message representing the error
     */
    fun setError(state: LiveGameState, error: String) {
        try {
            gameCol.document(state.id)
                .update(
                    "gameState", GameState.ERROR,
                    "error", error
                )
        } catch (ex: Exception) {
            // ignore exceptions thrown here
        }
    }

    /**
     * Wait for all players to join (i.e to publish their player information)
     * Has a maximum timeout of 2 minutes after which the game will encounter an error state
     * and be removed
     * @param state the current live game state
     * @return the live game state after all players have joined
     */
    suspend fun waitForPlayers(state: LiveGameState): LiveGameState {
        if (state.joinedPlayers.size == state.maxPlayers)
            return state

        val listener = FirestoreListener.getListener(state.id, gameCol, LiveGameStateDto::class)
        return listener.listenForEvent(Duration.ofMinutes(2)) { obj ->
            obj.joinedPlayers.size == obj.maxPlayers
        }
    }

    /**
     * Waits for all players to publish their block states
     * @param state the live game state
     * @return the live game state after all block states have been published
     */
    suspend fun waitForBlockStates(state: LiveGameState): LiveGameState {
        if (state.gameState != GameState.WAITING)
            return state

        val listener = FirestoreListener.getListener(state.id, gameCol, LiveGameStateDto::class)
        return listener.listenForEvent(defaultTimeout) { obj ->
            val required = obj.maxPlayers
            val blockStatesSize = obj.blockStates.keys.size
            required == blockStatesSize
        }
    }

    /**
     * Waits for the host to start the current game
     * i.e the host has changed the game state to PICK_WORD or DRAWING
     * @see GameState
     * @param state the live game state
     * @return the live game state after the host has started the game
     */
    suspend fun waitForGameStart(state: LiveGameState): LiveGameState {
        if (state.gameState == GameState.PICK_WORD || state.gameState == GameState.DRAWING)
            return state

        val listener = FirestoreListener.getListener(state.id, gameCol, LiveGameStateDto::class)
        return listener.listenForEvent(defaultTimeout) { obj ->
            obj.gameState == GameState.PICK_WORD || obj.gameState == GameState.DRAWING
        }
    }

    /**
     * Wait for the block state completion status to be the specified
     * @param state the live game state
     * @param isCompleted expected completion status
     * @return the live game state when all block states reach the specified status
     */
    suspend fun waitForCompleted(state: LiveGameState, isCompleted: Boolean = true): LiveGameState {
        val listener = FirestoreListener.getListener(state.id, gameCol, LiveGameStateDto::class)
        return listener.listenForEvent(defaultTimeout) { obj ->
            obj.blockStates.values.all { lgs -> lgs.completed == isCompleted }
        }
    }

    /**
     * Waits for the host to publish the next game state
     * @param state the live game state
     * @param nextState the next state to wait for
     * @return the live game state when the next state has been published
     */
    suspend fun waitForNextState(state: LiveGameState, nextState: GameState): LiveGameState {
        val listener = FirestoreListener.getListener(state.id, gameCol, LiveGameStateDto::class)
        return listener.listenForEvent(defaultTimeout) { obj ->
            obj.gameState == nextState
        }
    }

    /**
     * Listen for any error that occurs, calling the callback with the error message
     * @param state the live game state that is going to be listened
     * @param onError the callback that will be called when an error occurs
     */
    fun listenForErrorState(state: LiveGameState, onError: (String) -> Unit) {
        val listener = FirestoreListener.getListener(state.id, gameCol, LiveGameStateDto::class)
        listener.listenForEvents(
            { it.gameState == GameState.ERROR },
            { onError(it.error!!) },
            { onError(it.message!!) }
        )
    }

    /**
     * Unregisters the listener associated with a live game state
     * @param state the live game state associated with the listener
     */
    fun unregisterListener(state: LiveGameState) {
        FirestoreListener.removeListener(state.id)
    }

}