package be.buithg.etghaifgte.data.repository

import be.buithg.etghaifgte.data.remote.ApiInterface
import be.buithg.etghaifgte.data.remote.model.toMatch
import be.buithg.etghaifgte.domain.model.Match
import be.buithg.etghaifgte.domain.repository.MatchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class MatchRepositoryImpl @Inject constructor(
    private val api: ApiInterface
) : MatchRepository {

    // Перечень «sport» + «league», которые хотим подтянуть
    private val leaguePaths = listOf(
        "football"   to "nfl",
        "basketball" to "nba",
        "baseball"   to "mlb",
        "hockey"     to "nhl",
        "soccer"     to "eng.1",
        "soccer"     to "fra.1"
    )

    private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    override suspend fun getMatches(dates: List<LocalDate>): List<Match> = coroutineScope {
        // Собираем все асинхронные «корзинки» запросов
        val deferreds = dates.flatMap { date ->
            leaguePaths.map { (sport, league) ->
                async(Dispatchers.IO) {
                    // 1) Пытаемся получить матчи на конкретную дату
                    val dateStr = date.format(formatter)
                    val resp = runCatching {
                        api.getScoreboard(sport, league, dates = dateStr)
                    }.getOrNull()

                    val evs = resp?.events
                        ?.mapNotNull { it.toMatch(league) }
                        .orEmpty()

                    if (evs.isNotEmpty()) {
                        evs
                    } else {
                        // 2) Если пусто — делаем один фолбэк‑запрос без даты
                        api.getScoreboard(sport, league, dates = null)
                            .events
                            ?.mapNotNull { it.toMatch(league) }
                            .orEmpty()
                    }
                }
            }
        }

        // Ждём все запросы и склеиваем результаты
        val allMatches = deferreds
            .map { it.await() }
            .flatten()

        // Сортируем по моменту старта (Instant.parse умеет «2025-08-15T19:00Z»)
        allMatches.sortedBy { runCatching { Instant.parse(it.dateTimeGMT) }.getOrNull() }
    }
}
