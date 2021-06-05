package pt.isel.pdm.drag.game.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import pt.isel.pdm.drag.repo.WordLanguage

enum class GameState { WAITING, PICK_WORD, DRAWING, GUESSING, FINISHED, ERROR }

@Parcelize
data class BlockState(
    val word: String = "",
    val board: DrawingBoard = DrawingBoard.getBlankBoard()
): Parcelable

@Parcelize
data class GameConfiguration(
    val language: WordLanguage,
    val roundTime: Long,
    val players: Int
): Parcelable