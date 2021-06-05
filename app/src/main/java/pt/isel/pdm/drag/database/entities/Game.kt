package pt.isel.pdm.drag.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "games")
data class Game(
    @PrimaryKey(autoGenerate = true) val gameId: Long = 0,
    val playerCount: Int,
    val roundTime: Long,
    val createdOn: Instant
)