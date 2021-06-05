package pt.isel.pdm.drag.menu.games

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import pt.isel.pdm.drag.R
import pt.isel.pdm.drag.database.entities.Game
import pt.isel.pdm.drag.databinding.PreviousGameItemLayoutBinding

class PreviousGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttrStyle: Int = 0
) : ConstraintLayout(context, attrs, defAttrStyle) {

    private val view = inflate(context, R.layout.previous_game_item_layout, this)
    private val binding: PreviousGameItemLayoutBinding by lazy {
        PreviousGameItemLayoutBinding.bind(view)
    }

    /**
     * Sets the game that this view represents
     * @param game previous game properties
     */
    fun setGame(game: Game) {
        binding.previousGameDate.text = context.getString(R.string.previousGameDate, game.createdOn.toString())
        binding.previousGamePlayers.text = context.getString(R.string.previousGamePlayers, game.playerCount)
        binding.previousGameRoundTime.text = context.getString(R.string.previousGameRoundTime, game.roundTime)
    }

    /**
     * Sets the handler that is called when a user removes this game
     * @param onRemove the remove handler
     */
    fun setRemoveHandler(onRemove: () -> Unit) {
        binding.removeButton.setOnClickListener { onRemove() }
    }

}