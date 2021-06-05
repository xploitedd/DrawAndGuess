package pt.isel.pdm.drag.game.view

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import pt.isel.pdm.drag.R
import pt.isel.pdm.drag.databinding.GameFooterLayoutBinding
import pt.isel.pdm.drag.game.model.GameState

private const val GUESS_MAX_CHARS = 25

class GameFooterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), GameStateDependent {

    private val view = inflate(context, R.layout.game_footer_layout, this)
    private val binding: GameFooterLayoutBinding by lazy {
        GameFooterLayoutBinding.bind(view)
    }

    init {
        binding.guess.filters = arrayOf(InputFilter.LengthFilter(GUESS_MAX_CHARS))
    }

    /**
     * Updates the round timer
     * @param remaining the time left to the round end
     */
    fun updateTimer(remaining: Long) {
        binding.roundTimer.text = String.format("%02d", remaining)
    }

    /**
     * Updates the game id, used to display the live game lobby code
     * @param gameId game id to be displayed
     */
    fun setGameId(gameId: String) {
        binding.gameId.text = gameId
    }

    /**
     * Sets the action to be performed once the clear button is pressed
     * @param action action to be performed
     */
    fun setClearListener(action: () -> Unit) {
        binding.clearButton.setOnClickListener { action() }
    }

    /**
     * Sets the action to be performed once the player inputs a new word character
     * @param minLength minimum length of the world that will trigger the action (default: 3)
     * @param action action to be triggered on each character of a valid word
     */
    fun setTextInputListener(minLength: Int = 3, action: (String) -> Unit) {
        binding.guess.doOnTextChanged { text, _, _, _ ->
            text?.let {
                if (it.length >= minLength)
                    action(it.toString())
            }
        }
    }

    override fun setMode(state: GameState) {
        when (state) {
            GameState.WAITING -> {
                binding.roundTimer.visibility = GONE
                binding.gameId.visibility = VISIBLE
            }
            GameState.DRAWING -> {
                binding.gameId.visibility = GONE
                binding.roundTimer.visibility = VISIBLE
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                hideKeyboard()

                binding.guess.visibility = GONE
                binding.guess.setText("", TextView.BufferType.EDITABLE)

                binding.clearButton.visibility = VISIBLE
            }
            GameState.PICK_WORD -> {
                binding.gameId.visibility = GONE
                binding.roundTimer.visibility = VISIBLE
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                binding.clearButton.visibility = GONE
                binding.guess.visibility = VISIBLE
            }
            GameState.GUESSING -> {
                binding.gameId.visibility = GONE
                binding.roundTimer.visibility = VISIBLE
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                binding.clearButton.visibility = GONE
                binding.guess.visibility = VISIBLE
            }
            else -> {
                binding.gameId.visibility = GONE
                binding.roundTimer.visibility = GONE
            }
        }
    }

    /**
     * Hides the keyboard so the user can no longer use it
     * Useful when a PICK_WORD or GUESSING round ends
     * @see GameState
     */
    private fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.guess.windowToken, 0)
    }

}