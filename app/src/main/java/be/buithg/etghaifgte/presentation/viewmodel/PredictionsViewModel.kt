package be.buithg.etghaifgte.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.buithg.etghaifgte.data.local.entity.PredictionEntity
import be.buithg.etghaifgte.domain.usecase.AddPredictionUseCase
import be.buithg.etghaifgte.domain.usecase.GetPredictionsUseCase
import be.buithg.etghaifgte.domain.usecase.GetCurrentMatchesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class PredictionsViewModel @Inject constructor(
    private val addPredictionUseCase: AddPredictionUseCase,
    private val getPredictionsUseCase: GetPredictionsUseCase,
    private val getCurrentMatchesUseCase: GetCurrentMatchesUseCase
) : ViewModel() {

    private val _predictions = MutableLiveData<List<PredictionEntity>>()
    val predictions: LiveData<List<PredictionEntity>> = _predictions

    private val _predictedCount = MutableLiveData<Int>()
    val predictedCount: LiveData<Int> = _predictedCount

    private val _upcomingCount = MutableLiveData<Int>()
    val upcomingCount: LiveData<Int> = _upcomingCount

    private val _wonCount = MutableLiveData<Int>()
    val wonCount: LiveData<Int> = _wonCount

    private var filterDate: LocalDate? = null

    private val apiKey = "1c5944c7-5c88-4b8c-80f3-c88f198ed725"

    private fun winnerTeam(match: Data): Int {
        val team1 = match.teamInfo?.getOrNull(0)?.shortname ?: match.teams?.getOrNull(0) ?: ""
        val team2 = match.teamInfo?.getOrNull(1)?.shortname ?: match.teams?.getOrNull(1) ?: ""

        val scores = match.score ?: emptyList()
        if (scores.size >= 2) {
            val score1 = scores[0].r
            val score2 = scores[1].r
            if (score1 > score2) return 1
            if (score2 > score1) return 2
        }

        val status = match.status?.lowercase() ?: ""
        return when {
            status.contains(team1.lowercase()) -> 1
            status.contains(team2.lowercase()) -> 2
            status.contains("draw") -> 0
            else -> 0
        }
    }

    private fun isUpcoming(item: PredictionEntity): Boolean {
        if (item.upcoming == 1) return true
        val dt = runCatching { LocalDateTime.parse(item.dateTime) }.getOrNull()
        return dt?.isAfter(LocalDateTime.now()) ?: false
    }

    fun loadPredictions() {
        viewModelScope.launch {
            val list = getPredictionsUseCase()
            refreshUpcomingMatches(list)
            _predictions.value = getPredictionsUseCase()
            updateCountsWithFilter()
        }
    }

    fun addPrediction(entity: PredictionEntity) {
        viewModelScope.launch {
            addPredictionUseCase(entity)
            loadPredictions()
        }
    }

    fun setFilterDate(date: LocalDate) {
        filterDate = date
        updateCountsWithFilter()
    }

    private fun updateCountsWithFilter() {
        val list = _predictions.value ?: emptyList()
        val filtered = filterDate?.let { date ->
            list.filter {
                runCatching { LocalDateTime.parse(it.dateTime).toLocalDate() }.getOrNull() == date
            }
        } ?: list

        _predictedCount.value = filtered.size
        _upcomingCount.value = filtered.count { isUpcoming(it) }
        _wonCount.value = filtered.count { prediction ->
            if (isUpcoming(prediction)) return@count false
            when (prediction.wonMatches) {
                1 -> prediction.pick == prediction.teamA
                2 -> prediction.pick == prediction.teamB
                else -> false
            }
        }
    }

    private suspend fun refreshUpcomingMatches(list: List<PredictionEntity>) {
        val upcomingList = list.filter { isUpcoming(it) }
        if (upcomingList.isEmpty()) return

        val matches = runCatching { getCurrentMatchesUseCase(apiKey) }.getOrNull() ?: return

        upcomingList.forEach { prediction ->
            val match = matches.find { it.dateTimeGMT == prediction.dateTime }
            if (match != null && match.matchEnded) {
                val winner = winnerTeam(match)
                val updated = prediction.copy(upcoming = 0, wonMatches = winner)
                addPredictionUseCase(updated)
            }
        }
    }
}
