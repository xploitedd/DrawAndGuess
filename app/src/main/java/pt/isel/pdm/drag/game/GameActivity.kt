package pt.isel.pdm.drag.game

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import pt.isel.pdm.drag.databinding.GameActivityBinding
import pt.isel.pdm.drag.game.model.GameConfiguration
import pt.isel.pdm.drag.game.model.GameState
import pt.isel.pdm.drag.game.model.PlayerConfiguration
import pt.isel.pdm.drag.game.model.Vector2D
import pt.isel.pdm.drag.menu.fragments.GAME_ERROR_MESSAGE
import pt.isel.pdm.drag.review.GAME_REVIEW_ID
import pt.isel.pdm.drag.review.ReviewActivity

const val GAME_CONFIGURATION = "gameConfig"
const val PLAYER_CONFIGURATION = "playerConfig"

class GameActivity : AppCompatActivity() {

    private val binding: GameActivityBinding by lazy { GameActivityBinding.inflate(layoutInflater) }
    private val viewModel: AbstractGameViewModel by lazy { getGameViewModel() }

    /**
     * The game configuration passed to this activity
     */
    private val gameConfiguration: GameConfiguration? by lazy {
        intent.getParcelableExtra(GAME_CONFIGURATION)
    }

    /**
     * The player configuration passed to this activity
     */
    private val playerConfiguration: PlayerConfiguration? by lazy {
        intent.getParcelableExtra(PLAYER_CONFIGURATION)
    }

    /**
     * Used to store the current drawing line
     */
    private lateinit var currentLine: MutableList<Vector2D>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // prevent phone screen from turning off while in game
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding.gameFooterView.setTextInputListener(action = viewModel::updateWord)
        binding.gameFooterView.setClearListener(viewModel::resetBoard)


        viewModel.game.observe(this) {
            binding.gameHeaderView.setMode(it)
            binding.gameFooterView.setMode(it)

            when (it!!) {
                GameState.WAITING -> onWaitingStart()
                GameState.DRAWING -> onDrawingStart()
                GameState.PICK_WORD -> onPickWordStart()
                GameState.GUESSING -> onGuessingStart()
                GameState.FINISHED -> onGameFinish()
                GameState.ERROR -> onError()
            }
        }

        viewModel.liveBoard.observe(this) {
            binding.gameView.board = it
        }

        viewModel.timer.observe(this, binding.gameFooterView::updateTimer)

        if (gameConfiguration == null) {
            // join an existing game
            (viewModel as LiveGameViewModel).joinGame(playerConfiguration!!)
        } else {
            // create new game
            viewModel.startGame(gameConfiguration!!, playerConfiguration)
        }
    }

    /**
     * Called on WAITING game state
     * @see GameState
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun onWaitingStart() {
        binding.gameView.setOnTouchListener(null)
        // since this state is only available through LiveGameViewModel
        // we are going to cast directly
        binding.gameFooterView.setGameId((viewModel as LiveGameViewModel).gameId)
    }

    /**
     * Called on DRAWING game state
     * @see GameState
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun onDrawingStart() {
        binding.gameHeaderView.setWord(viewModel.state.word)

        currentLine = mutableListOf()
        binding.gameView.setOnTouchListener(this::onTouch)
    }

    /**
     * Called on GUESSING game state
     * @see GameState
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun onGuessingStart() {
        binding.gameView.setOnTouchListener(null)
    }

    /**
     * Called on PICK_WORD game state
     * @see GameState
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun onPickWordStart() {
        binding.gameView.setOnTouchListener(null)
    }

    /**
     * Called on ERROR game state
     * @see GameState
     */
    private fun onError() {
        val intent = Intent()
        intent.putExtra(GAME_ERROR_MESSAGE, viewModel.error!!)
        setResult(RESULT_FIRST_USER, intent)
        finish()
    }

    /**
     * Called on FINISHED game state
     * @see GameState
     */
    private fun onGameFinish() {
        val intent = Intent(this, ReviewActivity::class.java)
        // here we want the local game id, in opposition to the live id
        intent.putExtra(GAME_REVIEW_ID, viewModel.localGameId)
        startActivity(intent)
        finish()
    }

    /**
     * Handles touch events to the drawing board
     * @param view DrawingBoard view
     * @param event the motion event
     * @return always true, since this listener consumes every touch event
     */
    private fun onTouch(view: View, event: MotionEvent): Boolean {
        val width = view.width
        val height = view.height
        var x = event.x
        var y = event.y

        if (width > height) {
            x = (height - event.y) / height
            y = event.x / width
        } else {
            x /= width
            y /= height
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> addPoint(x, y, true)
            MotionEvent.ACTION_MOVE -> addPoint(x, y)
            MotionEvent.ACTION_UP -> {
                addPoint(x, y)
                currentLine = mutableListOf()
            }
        }

        return true
    }

    /**
     * Adds a point to a new or existing line
     * @param x x position of the point
     * @param y y position of the point
     * @param newLine whether this point belongs to a new line
     */
    private fun addPoint(x: Float, y: Float, newLine: Boolean = false) {
        currentLine.add(Vector2D(x, y))
        // if this isn't a new line then just update the board, since the current line
        // is a mutable list
        if (newLine)
            viewModel.updateBoard(currentLine)
        else
            viewModel.updateBoard()
    }

    /**
     * Gets the view model for the current game mode
     * @return the view model
     * @see GameViewModel
     * @see LiveGameViewModel
     * @see AbstractGameViewModel
     */
    private fun getGameViewModel(): AbstractGameViewModel {
        return if (playerConfiguration != null) {
            val vm: LiveGameViewModel by viewModels()
            vm
        } else {
            val vm: GameViewModel by viewModels()
            vm
        }
    }

}