package livefootball.footballstreamning.fifaworldcup.network

import android.util.Log
import com.google.gson.JsonObject
import livefootball.footballstreamning.fifaworldcup.models.FootballMatchScoreModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FootballScoreRepository @Inject constructor(
    private val footballScoreApiService: FootballScoreApiService
) {
    suspend fun getLiveMatches(apiKey: String): List<FootballMatchScoreModel> {
        val sanitizedKey = apiKey.trim()
        Log.d("FootballScoreRepo", "getLiveMatches: Using API Key: [$sanitizedKey]")

        return try {
            val response = footballScoreApiService.getMatches(
                apiKey = sanitizedKey,
                rapidApiKey = sanitizedKey,
                live = "all"
            )

            Log.d("FootballScoreRepo", "getLiveMatches: Response received: $response")

            if (response.isJsonObject) {
                val jsonObject = response.asJsonObject
                
                if (jsonObject.has("errors") && !jsonObject.get("errors").isJsonArray) {
                    val errors = jsonObject.get("errors")
                    if (errors.isJsonObject && errors.asJsonObject.size() > 0) {
                        Log.e("FootballScoreRepo", "API Errors detected: $errors")
                    }
                }

                val responseArray = jsonObject.getAsJsonArray("response")
                val matches = mutableListOf<FootballMatchScoreModel>()

                if (responseArray != null && responseArray.size() > 0) {
                    Log.d("FootballScoreRepo", "Processing ${responseArray.size()} matches")
                    responseArray.forEach { element ->
                        try {
                            val item = element.asJsonObject
                            val fixture = item.getAsJsonObject("fixture")
                            val league = item.getAsJsonObject("league")
                            val teams = item.getAsJsonObject("teams")
                            val goals = item.getAsJsonObject("goals")
                            val status = fixture?.getAsJsonObject("status")

                            val homeGoal = if (goals != null && goals.has("home") && !goals.get("home").isJsonNull) goals.get("home").asString else "0"
                            val awayGoal = if (goals != null && goals.has("away") && !goals.get("away").isJsonNull) goals.get("away").asString else "0"

                            matches.add(
                                FootballMatchScoreModel(
                                    matchId = fixture?.get("id")?.asString ?: "0",
                                    countryName = league?.get("country")?.asString,
                                    leagueName = league?.get("name")?.asString,
                                    leagueId = league?.get("id")?.asString,
                                    matchDate = fixture?.get("date")?.asString?.split("T")?.get(0),
                                    matchStatus = status?.get("short")?.asString,
                                    matchTime = fixture?.get("date")?.asString?.split("T")?.get(1)?.substring(0, 5),
                                    homeTeamName = teams?.getAsJsonObject("home")?.get("name")?.asString,
                                    homeTeamScore = homeGoal,
                                    awayTeamName = teams?.getAsJsonObject("away")?.get("name")?.asString,
                                    awayTeamScore = awayGoal,
                                    leagueLogo = league?.get("logo")?.asString,
                                    homeTeamBadge = teams?.getAsJsonObject("home")?.get("logo")?.asString,
                                    awayTeamBadge = teams?.getAsJsonObject("away")?.get("logo")?.asString,
                                    isLive = if (listOf("1H", "HT", "2H", "ET", "P", "BT", "SUSP", "INT").contains(status?.get("short")?.asString)) "1" else "0"
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("FootballScoreRepo", "Error parsing match item: ${e.message}")
                        }
                    }
                } else {
                    Log.d("FootballScoreRepo", "Response array is empty or null")
                }
                
                // Sorting logic: Live matches first, then by date/time (newest first)
                matches.sortWith(compareByDescending<FootballMatchScoreModel> { it.isLive == "1" }
                    .thenByDescending { it.matchDate }
                    .thenByDescending { it.matchTime })

                matches
            } else {
                Log.e("FootballScoreRepo", "Response is not a JSON object")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("FootballScoreRepo", "Exception in getLiveMatches: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getMatchDetail(apiKey: String, fixtureId: String): FootballMatchScoreModel? {
        val sanitizedKey = apiKey.trim()
        return try {
            val response = footballScoreApiService.getMatches(
                apiKey = sanitizedKey,
                rapidApiKey = sanitizedKey,
                fixtureId = fixtureId
            )

            if (response.isJsonObject) {
                val jsonObject = response.asJsonObject
                val responseArray = jsonObject.getAsJsonArray("response")

                if (responseArray != null && responseArray.size() > 0) {
                    val item = responseArray.get(0).asJsonObject
                    val fixture = item.getAsJsonObject("fixture")
                    val league = item.getAsJsonObject("league")
                    val teams = item.getAsJsonObject("teams")
                    val goals = item.getAsJsonObject("goals")
                    val status = fixture?.getAsJsonObject("status")

                    val homeGoal = if (goals != null && goals.has("home") && !goals.get("home").isJsonNull) goals.get("home").asString else "0"
                    val awayGoal = if (goals != null && goals.has("away") && !goals.get("away").isJsonNull) goals.get("away").asString else "0"

                    FootballMatchScoreModel(
                        matchId = fixture?.get("id")?.asString ?: "0",
                        countryName = league?.get("country")?.asString,
                        leagueName = league?.get("name")?.asString,
                        leagueId = league?.get("id")?.asString,
                        matchDate = fixture?.get("date")?.asString?.split("T")?.get(0),
                        matchStatus = status?.get("short")?.asString,
                        matchTime = fixture?.get("date")?.asString?.split("T")?.get(1)?.substring(0, 5),
                        homeTeamName = teams?.getAsJsonObject("home")?.get("name")?.asString,
                        homeTeamScore = homeGoal,
                        awayTeamName = teams?.getAsJsonObject("away")?.get("name")?.asString,
                        awayTeamScore = awayGoal,
                        leagueLogo = league?.get("logo")?.asString,
                        homeTeamBadge = teams?.getAsJsonObject("home")?.get("logo")?.asString,
                        awayTeamBadge = teams?.getAsJsonObject("away")?.get("logo")?.asString,
                        isLive = if (listOf("1H", "HT", "2H", "ET", "P", "BT", "SUSP", "INT").contains(status?.get("short")?.asString)) "1" else "0"
                    )
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
