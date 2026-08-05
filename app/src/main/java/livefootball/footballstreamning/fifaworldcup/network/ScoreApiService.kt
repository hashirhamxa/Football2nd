package livefootball.footballstreamning.fifaworldcup.network

import livefootball.footballstreamning.fifaworldcup.models.CurrentMatchesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ScoreApiService {
    @GET("currentMatches")
    suspend fun getCurrentMatches(
        @Query("apikey") apiKey: String,
        @Query("offset") offset: Int = 0
    ): CurrentMatchesResponse
}
