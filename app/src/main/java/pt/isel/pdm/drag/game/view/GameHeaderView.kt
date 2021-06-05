package pt.isel.pdm.drag.game.view

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import pt.isel.pdm.drag.R
import pt.isel.pdm.drag.databinding.GameHeaderLayoutBinding
import pt.isel.pdm.drag.game.model.GameState

class GameHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), GameStateDependent {

    private val view = inflate(context, R.layout.game_header_layout, this)
    private val binding: GameHeaderLayoutBinding by lazy {
        GameHeaderLayoutBinding.bind(view)
    }

    /**
     * Sets the current round word
     * Used for DRAWING rounds
     * @param word the word to be set
     * @see GameState
     */
    fun setWord(word: String) {
        binding.word.text = if (word == "") context.getString(R.string.no_word) else word
    }

    override fun setMode(state: GameState) {
        when (state) {
            GameState.WAITING -> {
                binding.word.visibility = GONE
                binding.myGuess.visibility = GONE
                binding.waiting.visibility = VISIBLE
            }
            GameState.DRAWING -> {
                binding.myGuess.visibility = GONE
                binding.waiting.visibility = GONE
                binding.word.visibility = VISIBLE
            }
            GameState.PICK_WORD -> {
                binding.waiting.visibility = GONE
                binding.word.visibility = GONE
                binding.word.text = ""

                binding.myGuess.text = context.getString(R.string.pick_word)
                binding.myGuess.visibility = VISIBLE
            }
            GameState.GUESSING -> {
                binding.waiting.visibility = GONE
                binding.word.visibility = GONE
                binding.word.text = ""

                binding.myGuess.text = context.getString(R.string.guess_word)
                binding.myGuess.visibility = VISIBLE
            }
            else -> {}
        }
    }

}