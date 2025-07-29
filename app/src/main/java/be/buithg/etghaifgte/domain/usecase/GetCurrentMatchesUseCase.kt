package be.buithg.etghaifgte.domain.usecase

import be.buithg.etghaifgte.domain.model.Match
import be.buithg.etghaifgte.domain.repository.MatchRepository
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.inject.Inject

class GetCurrentMatchesUseCase @Inject constructor(
    private val repository: MatchRepository
) {
    suspend operator fun invoke(): List<Match> {
        val utcDate = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate()
        val dates = listOf(
            utcDate.minusDays(1),
            utcDate,
            utcDate.plusDays(1)
        )

        return repository.getMatches(dates)
    }
}

