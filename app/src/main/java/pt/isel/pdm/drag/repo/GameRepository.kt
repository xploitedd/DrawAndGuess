package pt.isel.pdm.drag.repo

import pt.isel.pdm.drag.database.HistoryDatabase
import pt.isel.pdm.drag.database.entities.Game
import pt.isel.pdm.drag.database.entities.Round
import pt.isel.pdm.drag.database.entities.RoundResult
import pt.isel.pdm.drag.game.model.BlockState
import pt.isel.pdm.drag.game.model.GameState
import java.time.Instant

class GameRepository(private val db: HistoryDatabase) {

    /**
     * Creates a new local game
     * @param players number of players
     * @param roundTime time in seconds of each round
     * @return the created game object
     */
    suspend fun createNewGame(players: Int, roundTime: Long): Game {
        val dao = db.getGameDao()
        val id = dao.createGame(Game(
            playerCount = players,
            roundTime = roundTime,
            createdOn = Instant.now()
        ))

        return dao.getGameById(id)
    }

    /**
     * Adds a new round to an existing game
     * @param game game where the round will be added
     * @param state the round state
     * @return the created round
     */
    suspend fun addRoundToGame(game: Game, state: GameState): Round {
        val dao = db.getRoundDao()
        val id = dao.createRound(Round(
            gameId = game.gameId,
            gameState = state
        ))

        return dao.getRoundById(id)
    }

    /**
     * Adds a result to the specified round
     * @param round round that is associated with the result
     * @param blockState block state of this result
     * @param playerId id of the player that is the owner of this result
     * @param playerName name of the player
     */
    suspend fun addRoundResult(round: Round, blockState: BlockState, playerId: Int, playerName: String? = null) =
        db.getRoundResultDao().createRoundResult(RoundResult(
            roundId = round.roundId,
            playerId = playerId,
            playerName = playerName,
            word = blockState.word,
            board = blockState.board
        ))

    /**
     * Get all games that exist in the database
     * @return list of Game objects
     */
    suspend fun getGames() =
        db.getGameDao().getGames()

    /**
     * Gets the game object for the specified game id
     * @param gameId id of the game to get the object for
     * @return the game object associated with the id
     */
    suspend fun getGame(gameId: Long) =
        db.getGameDao().getGameById(gameId)

    /**
     * Get all rounds for the specified game
     * @param game game to get rounds for
     * @return list of rounds in the game
     */
    suspend fun getRounds(game: Game) =
        db.getRoundDao().getGameRounds(game.gameId)

    /**
     * Get all round results for the specified round
     * @param round the round to get results for
     * @return list of round results in a round
     */
    suspend fun getRoundResults(round: Round) =
        db.getRoundResultDao().getResultsByRound(round.roundId)

    /**
     * Removes the specified game and its associated objects from the database
     * @param game game to be removed
     */
    suspend fun removeGame(game: Game) =
        db.getGameDao().deleteGame(game)

}