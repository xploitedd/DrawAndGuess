package pt.isel.pdm.drag.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import pt.isel.pdm.drag.database.entities.Round

@Dao
interface RoundDao {

    @Insert
    suspend fun createRound(round: Round): Long

    @Query("select * from rounds where roundId = :roundId")
    suspend fun getRoundById(roundId: Long): Round

    @Query("select * from rounds where gameId = :gameId")
    suspend fun getGameRounds(gameId: Long): List<Round>

}