package be.buithg.etghaifgte.domain.repository

import be.buithg.etghaifgte.domain.model.Match
import java.time.LocalDate

interface MatchRepository {
    suspend fun getMatches(dates: List<LocalDate>): List<Match>
}

