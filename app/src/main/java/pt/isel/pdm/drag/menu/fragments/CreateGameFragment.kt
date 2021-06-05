package pt.isel.pdm.drag.menu.fragments

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import pt.isel.pdm.drag.R
import pt.isel.pdm.drag.databinding.CreateGameFragmentBinding
import pt.isel.pdm.drag.game.GAME_CONFIGURATION
import pt.isel.pdm.drag.game.GameActivity
import pt.isel.pdm.drag.game.PLAYER_CONFIGURATION
import pt.isel.pdm.drag.game.model.GameConfiguration
import pt.isel.pdm.drag.game.model.PlayerConfiguration
import pt.isel.pdm.drag.repo.WordLanguage
import java.util.*

class CreateGameFragment : DragFragment() {

    private lateinit var binding: CreateGameFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.create_game_fragment, container, false)
        binding = CreateGameFragmentBinding.bind(view)

        ArrayAdapter.createFromResource(
            view.context,
            R.array.round_time,
            R.layout.spinner_item_selected
        ).also { arrayAdapter ->
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.roundTime.adapter = arrayAdapter
        }

        ArrayAdapter.createFromResource(
            view.context,
            R.array.number_of_players,
            R.layout.spinner_item_selected
        ).also { arrayAdapter ->
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.playerCount.adapter = arrayAdapter
        }

        binding.hostName.filters = arrayOf(InputFilter.LengthFilter(PLAYER_NAME_MAX_CHARS))
        binding.onlineMode.setOnCheckedChangeListener { _, checked -> onOnlineModeChange(checked) }
        binding.startGame.setOnClickListener { onGameStart() }

        return view
    }

    /**
     * Callback when the player changes the online mode setting
     * @param checked true if online mode is enabled, false otherwise
     */
    private fun onOnlineModeChange(checked: Boolean) {
        if (checked) {
            checkPermissions()
            binding.playerNameLayout.visibility = View.VISIBLE
        } else {
            binding.playerNameLayout.visibility = View.GONE
        }
    }

    /**
     * Callback used to start a new online game
     */
    private fun onGameStart() {
        val gameConfig = GameConfiguration(
            WordLanguage.languageFromLocale(Locale.getDefault()),
            binding.roundTime.selectedItem.toString().toLong(),
            binding.playerCount.selectedItem.toString().toInt()
        )

        val onlineMode = binding.onlineMode.isChecked
        val intent = Intent(requireContext(), GameActivity::class.java)
        intent.putExtra(GAME_CONFIGURATION, gameConfig)
        if (onlineMode) {
            intent.putExtra(PLAYER_CONFIGURATION, PlayerConfiguration(
                binding.hostName.text.toString()
            ))
        }

        requestActivity(intent)
    }

    override fun onPermissionDenied() {
        binding.onlineMode.isChecked = false
    }

}