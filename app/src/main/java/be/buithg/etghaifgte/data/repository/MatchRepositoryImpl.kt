package be.buithg.etghaifgte.data.repository

import be.buithg.etghaifgte.data.remote.ApiInterface
import be.buithg.etghaifgte.domain.repository.MatchRepository
import javax.inject.Inject

class MatchRepositoryImpl @Inject constructor(
    private val api: ApiInterface
) : MatchRepository {
//    override suspend fun getCurrentMatches(apiKey: String): List<Data> {
//        return api.getLiveScore(apiKey).data ?: emptyList()
//    }

}

