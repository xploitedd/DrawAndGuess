package pt.isel.pdm.drag.menu.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import pt.isel.pdm.drag.R
import pt.isel.pdm.drag.databinding.PreviousGamesFragmentBinding
import pt.isel.pdm.drag.menu.games.PreviousGamesAdapter
import pt.isel.pdm.drag.menu.games.PreviousGamesViewModel
import pt.isel.pdm.drag.review.GAME_REVIEW_ID
import pt.isel.pdm.drag.review.ReviewActivity
import pt.isel.pdm.drag.util.MarginItemDecoration

class PreviousGamesFragment : DragFragment() {

    private val viewModel: PreviousGamesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.previous_games_fragment, container, false)
        val binding = PreviousGamesFragmentBinding.bind(view)

        binding.previousGamesView.layoutManager = LinearLayoutManager(requireContext())
        binding.previousGamesView.addItemDecoration(
            MarginItemDecoration(
                requireContext()
                    .resources.
                    getDimension(R.dimen.margin_item)
                    .toInt()
            )
        )

        viewModel.error.observe(viewLifecycleOwner, this::showError)
        viewModel.games.observe(viewLifecycleOwner) {
            binding.noGames.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            binding.previousGamesView.adapter = PreviousGamesAdapter(
                it,
                { g -> showGame(g.gameId) },
                viewModel::removeGame
            )
        }

        viewModel.loadGames()
        return view
    }

    /**
     * Show the specified previous game
     * @param gameId previous game local id
     */
    private fun showGame(gameId: Long) {
        val intent = Intent(requireContext(), ReviewActivity::class.java)
        intent.putExtra(GAME_REVIEW_ID, gameId)
        startActivity(intent)
    }

    override fun onPermissionDenied() {}

}