package be.buithg.etghaifgte.domain.model

import be.buithg.etghaifgte.data.local.entity.PredictionEntity

/**
 * Calculate daily statistics for the provided [items] and selected [section].
 */
fun updateCounters(items: List<PredictionEntity>, section: Section): DailyStats {
    val filtered = when (section) {
        Section.YESTERDAY -> items.filter { !it.upcomingFlag }
        Section.TOMORROW  -> items.filter { it.upcomingFlag }
        Section.TODAY     -> items
    }
    val predictedCount = filtered.size
    val upcomingCount  = filtered.count { it.upcomingFlag }
    val wonCount       = filtered.count { it.won }
    return DailyStats(predictedCount, upcomingCount, wonCount)
}
