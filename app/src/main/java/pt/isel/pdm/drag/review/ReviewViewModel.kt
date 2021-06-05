package pt.isel.pdm.drag.review

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pt.isel.pdm.drag.DragApplication
import pt.isel.pdm.drag.database.entities.Round
import pt.isel.pdm.drag.database.entities.RoundResult
import pt.isel.pdm.drag.util.launchTask

data class ReviewData(
    val round: Round,
    val result: RoundResult
)

class ReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val dragApplication = getApplication<DragApplication>()
    private val gameRepository = dragApplication.gameRepository
    private val _error = MutableLiveData<String>()
    private val _review = MutableLiveData<List<List<ReviewData>>>()

    val error: LiveData<String> = _error
    val review: LiveData<List<List<ReviewData>>> = _review

    /**
     * Loads a previous game review data
     * @param gameId local game id to load
     */
    fun loadData(gameId: Long) {
        launchTask(_error::setValue) {
            val game = gameRepository.getGame(gameId)
            val rounds = gameRepository.getRounds(game)
            val list = mutableListOf<MutableList<ReviewData>>()

            rounds.forEach { round ->
                val results = gameRepository.getRoundResults(round)
                results.forEachIndexed { idx, result ->
                    var sequenceList = list.getOrNull(idx)
                    if (sequenceList == null) {
                        sequenceList = mutableListOf()
                        list.add(sequenceList)
                    }

                    sequenceList.add(ReviewData(round, result))
                }
            }

            _review.postValue(list)
        }
    }

}