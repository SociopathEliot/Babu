package be.buithg.etghaifgte.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.buithg.etghaifgte.data.local.entity.PredictionEntity
import be.buithg.etghaifgte.domain.usecase.AddPredictionUseCase
import be.buithg.etghaifgte.domain.usecase.GetCurrentMatchesUseCase
import be.buithg.etghaifgte.domain.usecase.GetPredictionsUseCase
import be.buithg.etghaifgte.utils.parseUtcToLocal
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

@HiltViewModel
class PredictionsViewModel @Inject constructor(
    private val addPredictionUseCase: AddPredictionUseCase,
    private val getPredictionsUseCase: GetPredictionsUseCase,
    private val getCurrentMatchesUseCase: GetCurrentMatchesUseCase
) : ViewModel() {

    private val _predictions    = MutableLiveData<List<PredictionEntity>>(emptyList())
    val predictions: LiveData<List<PredictionEntity>> = _predictions

    private val _predictedCount = MutableLiveData(0)
    val predictedCount: LiveData<Int> = _predictedCount

    private val _upcomingCount  = MutableLiveData(0)
    val upcomingCount: LiveData<Int> = _upcomingCount

    private val _wonCount       = MutableLiveData(0)
    val wonCount: LiveData<Int> = _wonCount

    private var filterDate: LocalDate = LocalDate.now()
    private val predictedCounts = mutableMapOf<LocalDate, Int>()

    init {
        loadPredictions()
    }

    fun loadPredictions() = viewModelScope.launch {
        val raw = getPredictionsUseCase()
        refreshUpcomingMatches(raw)
        val updated = getPredictionsUseCase()
        computePredictedCounts(updated)
        _predictions.value = updated
        updateCountsForDate()
    }

    fun addPrediction(entity: PredictionEntity) = viewModelScope.launch {
        addPredictionUseCase(entity)
        _predictions.value = listOf(entity) + (_predictions.value.orEmpty())
        entity.dateTime.parseUtcToLocal()
            ?.toLocalDate()
            ?.let { d -> predictedCounts[d] = (predictedCounts[d] ?: 0) + 1 }
        updateCountsForDate()
    }

    fun setFilterDate(date: LocalDate) {
        filterDate = date
        updateCountsForDate()
    }

    private fun computePredictedCounts(list: List<PredictionEntity>) {
        predictedCounts.clear()
        list.forEach { e ->
            e.dateTime.parseUtcToLocal()
                ?.toLocalDate()
                ?.let { d -> predictedCounts[d] = (predictedCounts[d] ?: 0) + 1 }
        }
    }

    private fun updateCountsForDate() {
        _predictedCount.value = predictedCounts[filterDate] ?: 0
        val listForDate = _predictions.value.orEmpty().filter {
            it.dateTime.parseUtcToLocal()?.toLocalDate() == filterDate
        }
        _upcomingCount.value = listForDate.count { isUpcoming(it) }
        _wonCount.value      = listForDate.count { it.upcoming == 0 && it.wonMatches > 0 }
    }

    private fun isUpcoming(item: PredictionEntity): Boolean {
        if (item.upcoming == 1) return true
        return item.dateTime.parseUtcToLocal()?.isAfter(LocalDateTime.now()) ?: false
    }

    private suspend fun refreshUpcomingMatches(list: List<PredictionEntity>) {
        val future = list.filter { isUpcoming(it) }
        if (future.isEmpty()) return
        val matches = runCatching { getCurrentMatchesUseCase() }.getOrNull() ?: return
        future.forEach { p ->
            matches.find { it.dateTimeGMT == p.dateTime }?.let { m ->
                if (m.matchEnded) {
                    val win = when {
                        m.scoreA != null && m.scoreB != null ->
                            if (m.scoreA > m.scoreB) 1 else if (m.scoreB > m.scoreA) 2 else 0
                        else -> 0
                    }
                    addPredictionUseCase(p.copy(upcoming = 0, wonMatches = win))
                }
            }
        }
    }
}
