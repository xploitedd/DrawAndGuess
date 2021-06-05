package pt.isel.pdm.drag.review

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.android.material.snackbar.Snackbar
import pt.isel.pdm.drag.databinding.ReviewActivityBinding
import pt.isel.pdm.drag.review.view.ReviewRoundAdapter

const val GAME_REVIEW_ID = "GAME_REVIEW_ID"

class ReviewActivity : AppCompatActivity() {

    private val binding: ReviewActivityBinding by lazy { ReviewActivityBinding.inflate(layoutInflater) }
    private val viewModel: ReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val gameId = intent.getLongExtra(GAME_REVIEW_ID, -1)
        if (gameId == -1L) {
            Log.wtf(application::class.simpleName, "A gameId was not passed to ReviewActivity!")
            finish()
            return
        }

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView)

        viewModel.error.observe(this) {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                .show()
        }

        viewModel.review.observe(this) {
            binding.recyclerView.adapter = ReviewRoundAdapter(it)
        }

        viewModel.loadData(gameId)
    }

}