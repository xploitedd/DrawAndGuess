package pt.isel.pdm.drag.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import pt.isel.pdm.drag.database.entities.RoundResult

@Dao
interface RoundResultDao {

    @Insert
    suspend fun createRoundResult(result: RoundResult): Long

    @Query("select * from results where roundId = :roundId")
    suspend fun getResultsByRound(roundId: Long): List<RoundResult>

}