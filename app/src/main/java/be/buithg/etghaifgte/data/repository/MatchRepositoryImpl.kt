package be.buithg.etghaifgte.data.repository

import be.buithg.etghaifgte.data.remote.ApiInterface
import be.buithg.etghaifgte.data.remote.model.toMatch
import be.buithg.etghaifgte.domain.model.Match
import be.buithg.etghaifgte.domain.repository.MatchRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class MatchRepositoryImpl @Inject constructor(
    private val api: ApiInterface
) : MatchRepository {

    private val leagues = listOf("eng.1")
    private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    override suspend fun getMatches(dates: List<LocalDate>): List<Match> {
        val result = mutableListOf<Match>()
        for (date in dates) {
            val dateStr = date.format(formatter)
            for (league in leagues) {
                val response = api.getScoreboard(league, dateStr)
                val matches = response.events?.mapNotNull { it.toMatch(league) } ?: emptyList()
                result += matches
            }
        }
        return result
    }
}

