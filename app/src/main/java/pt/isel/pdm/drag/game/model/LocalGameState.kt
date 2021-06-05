package pt.isel.pdm.drag.game.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant

@Parcelize
data class GameViewState(
    val player: Int = -1,
    val gameState: GameState = GameState.DRAWING,
    val block: BlockState = BlockState(),
    val roundStart: Instant = Instant.now(),
    val error: String? = null
): Parcelable