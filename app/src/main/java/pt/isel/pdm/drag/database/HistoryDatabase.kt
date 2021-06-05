package pt.isel.pdm.drag.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pt.isel.pdm.drag.database.dao.GameDao
import pt.isel.pdm.drag.database.dao.RoundDao
import pt.isel.pdm.drag.database.dao.RoundResultDao
import pt.isel.pdm.drag.database.entities.Game
import pt.isel.pdm.drag.database.entities.Round
import pt.isel.pdm.drag.database.entities.RoundResult

@Database(entities = [Game::class, Round::class, RoundResult::class], version = 4)
@TypeConverters(GameConverters::class)
abstract class HistoryDatabase : RoomDatabase() {

    abstract fun getGameDao(): GameDao
    abstract fun getRoundDao(): RoundDao
    abstract fun getRoundResultDao(): RoundResultDao

}