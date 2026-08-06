package livefootball.footballstreamning.fifaworldcup.network

import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface FootballScoreApiService {
    @GET("fixtures")
    suspend fun getMatches(
        @Header("x-apisports-key") apiKey: String? = null,
        @Header("x-rapidapi-key") rapidApiKey: String? = null,
        @Header("x-rapidapi-host") host: String? = null,
        @Query("key") queryApiKey: String? = null, // Fallback query param
        @Query("id") fixtureId: String? = null, // Added for match detail
        @Query("live") live: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("date") date: String? = null,
        @Query("league") leagueId: Int? = null,
        @Query("season") season: Int? = null
    ): JsonElement
}
