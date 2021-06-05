package pt.isel.pdm.drag.menu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import pt.isel.pdm.drag.R
import pt.isel.pdm.drag.databinding.MainMenuFragmentBinding

class MainMenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.main_menu_fragment, container, false)
        val binding = MainMenuFragmentBinding.bind(view)

        binding.joinGame.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_mainMenuFragment_to_joinGameFragment)
        }

        binding.createGame.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_mainMenuFragment_to_createGameFragment)
        }

        binding.previousGamesButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_mainMenuFragment_to_previousGamesFragment)
        }

        return view
    }

}