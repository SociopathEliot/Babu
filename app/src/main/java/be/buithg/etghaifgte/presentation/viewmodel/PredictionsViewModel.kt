package be.buithg.etghaifgte.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.buithg.etghaifgte.data.local.entity.PredictionEntity
import be.buithg.etghaifgte.domain.model.DailyStats
import be.buithg.etghaifgte.domain.usecase.AddPredictionUseCase
import be.buithg.etghaifgte.domain.usecase.GetCurrentMatchesUseCase
import be.buithg.etghaifgte.domain.usecase.GetDailyStatsUseCase
import be.buithg.etghaifgte.domain.usecase.GetPredictionsUseCase
import be.buithg.etghaifgte.utils.parseUtcToLocal
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

@HiltViewModel
class PredictionsViewModel @Inject constructor(
    private val addPredictionUseCase: AddPredictionUseCase,
    private val getPredictionsUseCase: GetPredictionsUseCase,
    private val getCurrentMatchesUseCase: GetCurrentMatchesUseCase,
    private val getDailyStatsUseCase: GetDailyStatsUseCase
) : ViewModel() {

    private val _predictions    = MutableLiveData<List<PredictionEntity>>(emptyList())
    val predictions: LiveData<List<PredictionEntity>> = _predictions

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    val predYesterday = MutableStateFlow(0)
    val predToday     = MutableStateFlow(0)
    val predTomorrow  = MutableStateFlow(0)

    val dailyStats: StateFlow<DailyStats> = _selectedDate
        .flatMapLatest { getDailyStatsUseCase(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DailyStats(0, 0, 0))

    init {
        loadPredictions()
    }

    fun loadPredictions() = viewModelScope.launch {
        val raw = getPredictionsUseCase()
        refreshUpcomingMatches(raw)
        _predictions.value = getPredictionsUseCase()
    }

    fun addPrediction(entity: PredictionEntity) = viewModelScope.launch {
        addPredictionUseCase(entity)
        _predictions.value = listOf(entity) + (_predictions.value.orEmpty())
        when (_selectedDate.value) {
            LocalDate.now().minusDays(1) -> predYesterday.value++
            LocalDate.now()             -> predToday.value++
            LocalDate.now().plusDays(1) -> predTomorrow.value++
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun getFilterDate(): LocalDate = _selectedDate.value

    private fun isUpcoming(item: PredictionEntity): Boolean {
        if (item.upcomingFlag) return true
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
                    val wonFlag = when (win) {
                        1 -> p.pick == p.teamA
                        2 -> p.pick == p.teamB
                        else -> false
                    }
                    addPredictionUseCase(
                        p.copy(
                            upcoming = 0,
                            wonMatches = win,
                            upcomingFlag = false,
                            won = wonFlag
                        )
                    )
                }
            }
        }
    }
}
