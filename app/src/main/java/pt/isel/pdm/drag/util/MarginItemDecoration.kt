package pt.isel.pdm.drag.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(private val margin: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.apply {
            if (parent.getChildAdapterPosition(view) == 0)
                top = margin

            left = margin
            right = margin
            bottom = margin
        }
    }

}