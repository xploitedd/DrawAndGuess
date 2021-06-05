package pt.isel.pdm.drag.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import pt.isel.pdm.drag.database.entities.Game

@Dao
interface GameDao {

    @Insert
    suspend fun createGame(game: Game): Long

    @Query("select * from games where gameId = :id")
    suspend fun getGameById(id: Long): Game

    @Query("select * from games")
    suspend fun getGames(): List<Game>

    @Delete
    suspend fun deleteGame(game: Game)

}