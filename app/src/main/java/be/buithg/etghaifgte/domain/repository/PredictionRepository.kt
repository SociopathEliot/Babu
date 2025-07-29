package be.buithg.etghaifgte.domain.repository

import be.buithg.etghaifgte.data.local.entity.PredictionEntity
import be.buithg.etghaifgte.domain.model.DailyStats
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface PredictionRepository {
    suspend fun addPrediction(prediction: PredictionEntity)
    suspend fun getPredictions(): List<PredictionEntity>

    suspend fun getPrediction(teamA: String, teamB: String, dateTime: String): PredictionEntity?

    fun getDailyStats(date: LocalDate): Flow<DailyStats>
}
