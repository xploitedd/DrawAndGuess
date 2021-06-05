package pt.isel.pdm.drag.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import pt.isel.pdm.drag.game.model.GameState

@Entity(
    tableName = "rounds",
    foreignKeys = [
        ForeignKey(entity = Game::class, parentColumns = [ "gameId" ], childColumns = [ "gameId" ], onDelete = ForeignKey.CASCADE)
    ]
)
class Round(
    @PrimaryKey(autoGenerate = true) val roundId: Long = 0,
    val gameId: Long,
    val gameState: GameState
)