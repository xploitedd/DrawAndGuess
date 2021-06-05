package pt.isel.pdm.drag.review.view

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import pt.isel.pdm.drag.review.ReviewData

class ReviewRoundResultAdapter(
    private val dataList: List<ReviewData>,
    private val hideDownArrow: Boolean = false
) : RecyclerView.Adapter<ReviewRoundResultAdapter.ViewHolder>() {

    class ViewHolder(val view: ReviewItemView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ReviewItemView(parent.context)
        view.layoutParams = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        if (hideDownArrow)
            view.hideDownArrow()

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        if (position + 1 == itemCount)
            holder.view.hideLeftArrow()

        holder.view.setRoundResult(data.round, data.result, position)
    }

    override fun getItemCount() = dataList.size

}