package be.buithg.etghaifgte.domain.usecase

import be.buithg.etghaifgte.domain.model.Match
import be.buithg.etghaifgte.domain.repository.MatchRepository
import java.time.LocalDate
import javax.inject.Inject

class GetCurrentMatchesUseCase @Inject constructor(
    private val repository: MatchRepository
) {
    suspend operator fun invoke(): List<Match> {
        val dates = listOf(
            LocalDate.now().minusDays(1),
            LocalDate.now(),
            LocalDate.now().plusDays(1)
        )
        return repository.getMatches(dates)
    }
}

