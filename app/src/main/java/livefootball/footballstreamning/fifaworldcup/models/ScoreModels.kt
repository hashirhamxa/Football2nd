package livefootball.footballstreamning.fifaworldcup.models

import com.google.gson.annotations.SerializedName

data class CurrentMatchesResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<MatchScore>?,
    @SerializedName("info") val info: ResponseInfo?
)

data class MatchScore(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("matchType") val matchType: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("venue") val venue: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("teams") val teams: List<String>?,
    @SerializedName("teamInfo") val teamInfo: List<TeamInfo>?,
    @SerializedName("score") val score: List<Inning>?,
    @SerializedName("series_id") val seriesId: String?,
    @SerializedName("fantasyEnabled") val fantasyEnabled: Boolean?,
    @SerializedName("bbbEnabled") val bbbEnabled: Boolean?,
    @SerializedName("hasSquad") val hasSquad: Boolean?,
    @SerializedName("matchStarted") val matchStarted: Boolean?,
    @SerializedName("matchEnded") val matchEnded: Boolean?
)

data class TeamInfo(
    @SerializedName("name") val name: String?,
    @SerializedName("shortname") val shortName: String?,
    @SerializedName("img") val img: String?
)

data class Inning(
    @SerializedName("r") val runs: Int?,
    @SerializedName("w") val wickets: Int?,
    @SerializedName("o") val overs: Double?,
    @SerializedName("inning") val inning: String?
)

data class ResponseInfo(
    @SerializedName("hitsToday") val hitsToday: Int?,
    @SerializedName("hitsLimit") val hitsLimit: Int?,
    @SerializedName("credits") val credits: Int?,
    @SerializedName("server") val server: Int?,
    @SerializedName("queryTime") val queryTime: Double?,
    @SerializedName("s") val s: Int?
)
