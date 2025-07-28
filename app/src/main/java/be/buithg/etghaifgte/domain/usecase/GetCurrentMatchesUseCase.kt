package be.buithg.etghaifgte.domain.usecase

import be.buithg.etghaifgte.domain.repository.MatchRepository
import javax.inject.Inject

class GetCurrentMatchesUseCase @Inject constructor(
    private val repository: MatchRepository
) {
    suspend operator fun invoke(apiKey: String): List<Data> {
        return repository.getCurrentMatches(apiKey)
    }
}

