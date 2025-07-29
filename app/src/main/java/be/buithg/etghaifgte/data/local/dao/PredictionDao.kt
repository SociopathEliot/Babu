package be.buithg.etghaifgte.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import be.buithg.etghaifgte.data.local.entity.PredictionEntity
import be.buithg.etghaifgte.domain.model.DailyStats
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prediction: PredictionEntity)

    @Query("SELECT * FROM predictions ORDER BY id DESC")
    suspend fun getAll(): List<PredictionEntity>

    @Query("SELECT * FROM predictions WHERE teamA = :teamA AND teamB = :teamB AND dateTime = :dateTime LIMIT 1")
    suspend fun getByMatch(teamA: String, teamB: String, dateTime: String): PredictionEntity?

    @Query(
        """
        SELECT
          (SELECT COUNT(*) FROM predictions WHERE matchTime BETWEEN :start AND :end) AS predicted,
          (SELECT COUNT(*) FROM predictions WHERE matchTime BETWEEN :start AND :end AND upcomingFlag = 1) AS upcoming,
          (SELECT COUNT(*) FROM predictions WHERE matchTime BETWEEN :start AND :end AND won = 1) AS won
        """
    )
    fun getDailyStats(start: Long, end: Long): Flow<DailyStats>

}
