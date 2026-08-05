package livefootball.footballstreamning.fifaworldcup.network

import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Query

interface FootballScoreApiService {
    @GET(".")
    suspend fun getMatches(
        @Query("action") action: String = "get_events",
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("match_live") isLive: String = "1",
        @Query("APIkey") apiKey: String
    ): JsonElement
}
