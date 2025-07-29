package be.buithg.etghaifgte.domain.usecase

import be.buithg.etghaifgte.domain.model.DailyStats
import be.buithg.etghaifgte.domain.repository.PredictionRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetDailyStatsUseCase @Inject constructor(
    private val repository: PredictionRepository
) {
    operator fun invoke(date: LocalDate): Flow<DailyStats> =
        repository.getDailyStats(date)
}
