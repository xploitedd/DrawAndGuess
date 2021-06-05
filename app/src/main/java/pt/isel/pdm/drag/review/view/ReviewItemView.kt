package pt.isel.pdm.drag.review.view

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import pt.isel.pdm.drag.R
import pt.isel.pdm.drag.database.entities.Round
import pt.isel.pdm.drag.database.entities.RoundResult
import pt.isel.pdm.drag.databinding.ReviewItemLayoutBinding
import pt.isel.pdm.drag.game.model.GameState
import pt.isel.pdm.drag.game.view.GameStateDependent

class ReviewItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), GameStateDependent {

    private val view = inflate(context, R.layout.review_item_layout, this)
    private val binding: ReviewItemLayoutBinding by lazy {
        ReviewItemLayoutBinding.bind(view)
    }

    private lateinit var round: Round
    private lateinit var result: RoundResult
    private var resultIdx: Int = -1

    fun hideDownArrow() {
        binding.arrowDown.visibility = GONE
    }

    fun hideLeftArrow() {
        binding.arrowLeft.visibility = GONE
    }

    /**
     * Sets the round result that this view represents
     * @param round round associated with the result
     * @param result the round result
     * @param resultIdx index of this round result in the round
     */
    fun setRoundResult(round: Round, result: RoundResult, resultIdx: Int) {
        binding.playerReviewText.text = result.playerName
            ?: context.getString(R.string.playerFormat, resultIdx)

        // we have this to avoid using two when's
        this.round = round
        this.result = result
        this.resultIdx = resultIdx

        setMode(round.gameState)
    }

    override fun setMode(state: GameState) {
        when (state) {
            GameState.DRAWING -> {
                binding.previewView.board = result.board
                binding.drawingWordText.text = context.getString(R.string.drawingWord, result.word)

                binding.previewWordGuess.visibility = GONE
                binding.drawingWord.visibility = VISIBLE
                binding.previewView.visibility = VISIBLE
            }
            else -> {
                // PICK_WORD or GUESSING
                val actualWord = if (result.word == "") context.getString(R.string.no_word) else result.word
                binding.wordGuess.text = actualWord

                binding.drawingWord.visibility = GONE
                binding.previewView.visibility = GONE
                binding.previewWordGuess.visibility = VISIBLE
            }
        }
    }

}