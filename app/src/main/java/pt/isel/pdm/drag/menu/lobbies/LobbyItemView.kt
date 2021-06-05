package pt.isel.pdm.drag.menu.lobbies

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import pt.isel.pdm.drag.R
import pt.isel.pdm.drag.databinding.LobbyItemLayoutBinding

class LobbyItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val view = inflate(context, R.layout.lobby_item_layout, this)
    private val binding: LobbyItemLayoutBinding by lazy {
        LobbyItemLayoutBinding.bind(view)
    }

    /**
     * Sets the lobby that this item represents
     * @param lobby data with game lobby parameters
     */
    fun setLobby(lobby: LobbyData) {
        binding.lobbyOwner.text = context.getString(R.string.lobbyIdFormat, lobby.id)
        binding.lobbyPlayerCount.text = context.getString(R.string.lobbyPlayerCount, lobby.joinedPlayers, lobby.maxPlayers)
        binding.lobbyLanguage.text = context.getString(R.string.lobbyLanguage, lobby.lang.langId)
    }

}