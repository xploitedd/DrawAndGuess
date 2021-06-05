package pt.isel.pdm.drag.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import pt.isel.pdm.drag.game.model.DrawingBoard

@Entity(
    tableName = "results",
    foreignKeys = [
        ForeignKey(entity = Round::class, parentColumns = [ "roundId" ], childColumns = [ "roundId" ], onDelete = ForeignKey.CASCADE)
    ]
)
data class RoundResult(
    @PrimaryKey(autoGenerate = true) val resultId: Long = 0,
    val roundId: Long,
    val playerId: Int,
    val playerName: String?,
    val word: String,
    val board: DrawingBoard,
)