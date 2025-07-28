package be.buithg.etghaifgte.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import be.buithg.etghaifgte.data.remote.model.ScoreboardResponse

interface ApiInterface {
    @GET("apis/site/v2/sports/soccer/{league}/scoreboard")
    suspend fun getScoreboard(
        @Path("league") league: String,
        @Query("dates") date: String,
        @Query("limit") limit: Int = 100
    ): ScoreboardResponse
}