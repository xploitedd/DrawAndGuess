package pt.isel.pdm.drag.review.view

import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import pt.isel.pdm.drag.review.ReviewData

class ReviewRoundAdapter(
    private val rounds: List<List<ReviewData>>
) : RecyclerView.Adapter<ReviewRoundAdapter.ViewHolder>() {

    class ViewHolder(val view: RecyclerView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a horizontal recycler view for each round of the game
        val view = RecyclerView(parent.context)
        view.layoutManager = LinearLayoutManager(
            parent.context,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        view.layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(view)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val round = rounds[position]
        holder.view.adapter = ReviewRoundResultAdapter(round, position + 1 == itemCount)
    }

    override fun getItemCount(): Int = rounds.size

}
