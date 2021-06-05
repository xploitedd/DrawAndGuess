package pt.isel.pdm.drag.menu.fragments

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import pt.isel.pdm.drag.R
import pt.isel.pdm.drag.databinding.JoinGameFragmentBinding
import pt.isel.pdm.drag.game.GameActivity
import pt.isel.pdm.drag.game.PLAYER_CONFIGURATION
import pt.isel.pdm.drag.game.model.PlayerConfiguration
import pt.isel.pdm.drag.menu.lobbies.LobbiesAdapter
import pt.isel.pdm.drag.menu.lobbies.LobbiesViewModel
import pt.isel.pdm.drag.util.LIVE_ID_CHARS
import pt.isel.pdm.drag.util.MarginItemDecoration

class JoinGameFragment : DragFragment() {

    private lateinit var binding: JoinGameFragmentBinding
    private val viewModel: LobbiesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.join_game_fragment, container, false)
        binding = JoinGameFragmentBinding.bind(view)

        // configure recycler view
        binding.lobbyList.layoutManager = LinearLayoutManager(requireContext())
        binding.lobbyList.addItemDecoration(
            MarginItemDecoration(
                requireContext()
                    .resources.
                    getDimension(R.dimen.margin_item)
                    .toInt()
            )
        )

        viewModel.lobbyRefreshError.observe(viewLifecycleOwner, this::showError)
        viewModel.lobbies.observe(viewLifecycleOwner) {
            binding.noGames.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            binding.lobbyList.adapter = LobbiesAdapter(it) { id -> joinLobby(id) }
            binding.refreshLayout.isRefreshing = false
        }

        binding.lobbyId.filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(LIVE_ID_CHARS))
        binding.playerName.filters = arrayOf(InputFilter.LengthFilter(PLAYER_NAME_MAX_CHARS))

        binding.refreshLayout.setOnRefreshListener(this::refreshLobbyList)
        binding.refreshButton.setOnClickListener {
            binding.refreshLayout.isRefreshing = true
            refreshLobbyList()
        }


        binding.startGame.setOnClickListener {
            val lobbyId = binding.lobbyId.text.toString()
            joinLobby(lobbyId)
        }

        checkPermissions()
        return view
    }

    override fun onStart() {
        super.onStart()
        refreshLobbyList()
    }

    /**
     * Join the specified online lobby
     * @param lobbyId the id of the lobby to join
     */
    private fun joinLobby(lobbyId: String) {
        val playerName = binding.playerName.text.toString()
        val intent = Intent(requireContext(), GameActivity::class.java)

        val config = PlayerConfiguration(
            playerName,
            lobbyId
        )

        intent.putExtra(PLAYER_CONFIGURATION, config)
        requestActivity(intent)
    }

    /**
     * Refresh the available game lobby list
     */
    private fun refreshLobbyList() {
        viewModel.getAvailableLobbies()
    }

    override fun onError(error: String) {
        super.onError(error)
        refreshLobbyList()
    }

    override fun onPermissionDenied() {
        Navigation.findNavController(requireView()).navigate(R.id.action_joinGameFragment_to_mainMenuFragment)
    }

}