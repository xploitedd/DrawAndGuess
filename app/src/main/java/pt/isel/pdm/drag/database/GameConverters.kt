package pt.isel.pdm.drag.database

import androidx.room.TypeConverter
import pt.isel.pdm.drag.game.model.DrawingBoard
import pt.isel.pdm.drag.game.model.GameState
import java.time.Instant

class GameConverters {

    @TypeConverter
    fun boardToString(board: DrawingBoard) = board.boardString

    @TypeConverter
    fun stringToBoard(linesStr: String) = DrawingBoard.boardFromString(linesStr)

    @TypeConverter
    fun stateToString(state: GameState) = state.toString()

    @TypeConverter
    fun stringToState(stateStr: String) = GameState.valueOf(stateStr)

    @TypeConverter
    fun instantToTimestamp(instant: Instant) = instant.toEpochMilli()

    @TypeConverter
    fun timestampToInstant(timestamp: Long): Instant = Instant.ofEpochMilli(timestamp)

}