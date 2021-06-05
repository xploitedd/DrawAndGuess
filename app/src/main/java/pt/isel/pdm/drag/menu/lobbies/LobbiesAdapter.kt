package pt.isel.pdm.drag.menu.lobbies

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class LobbiesAdapter(
    private val lobbyData: List<LobbyData>,
    private val onSelect: (String) -> Unit
) : RecyclerView.Adapter<LobbiesAdapter.ViewHolder>() {

    class ViewHolder(val view: LobbyItemView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LobbyItemView(parent.context)
        view.layoutParams = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        return ViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lobby = lobbyData[position]
        holder.view.setOnTouchListener { _, _ ->
            onSelect(lobby.id)
            true
        }

        holder.view.setLobby(lobby)
    }

    override fun getItemCount() = lobbyData.size

}