package be.buithg.etghaifgte.data.repository

import be.buithg.etghaifgte.data.remote.ApiInterface
import be.buithg.etghaifgte.data.remote.model.toMatch
import be.buithg.etghaifgte.domain.model.Match
import be.buithg.etghaifgte.domain.repository.MatchRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class MatchRepositoryImpl @Inject constructor(
    private val api: ApiInterface
) : MatchRepository {

    // Список пар sport/league, которые мы хотим захватить
    private val leaguePaths = listOf(
        "football" to "nfl",
        "basketball" to "nba",
        "baseball"   to "mlb",
        "hockey"     to "nhl",
        "soccer"     to "eng.1",
        "soccer"     to "fra.1"
    )

    private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    override suspend fun getMatches(dates: List<LocalDate>): List<Match> {
        val result = mutableListOf<Match>()

        for (date in dates) {
            val dateStr = date.format(formatter)

            for ((sport, league) in leaguePaths) {
                // 1) Стягиваем события на конкретную дату
                val resp = api.getScoreboard(sport, league, dates = dateStr)
                val matches = resp.events
                    ?.mapNotNull { it.toMatch(league) }
                    .orEmpty()

                if (matches.isNotEmpty()) {
                    result += matches
                } else {
                    // 2) Фолбэк для этой лиги: без даты
                    val fb = api.getScoreboard(sport, league, dates = null)
                    result += fb.events
                        ?.mapNotNull { it.toMatch(league) }
                        .orEmpty()
                }
            }
        }

        // 3) Сортируем всё по реальному времени старта
        return result
            .sortedBy { runCatching { LocalDateTime.parse(it.dateTimeGMT) }.getOrNull() }
    }
}
