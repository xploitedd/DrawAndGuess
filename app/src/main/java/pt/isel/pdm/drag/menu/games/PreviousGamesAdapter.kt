package pt.isel.pdm.drag.menu.games

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import pt.isel.pdm.drag.database.entities.Game

class PreviousGamesAdapter(
    private val gameList: List<Game>,
    private val onGameSelected: (Game) -> Unit,
    private val onGameRemove: (Game) -> Unit
) : RecyclerView.Adapter<PreviousGamesAdapter.ViewHolder>() {

    class ViewHolder(val view: PreviousGameView): RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = PreviousGameView(parent.context)
        view.layoutParams = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val game = gameList[position]
        holder.view.setOnClickListener { onGameSelected(game) }
        holder.view.setRemoveHandler { onGameRemove(game) }
        holder.view.setGame(game)
    }

    override fun getItemCount() = gameList.size

}