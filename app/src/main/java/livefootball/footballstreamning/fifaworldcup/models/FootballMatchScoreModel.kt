package livefootball.footballstreamning.fifaworldcup.models

import com.google.gson.annotations.SerializedName

data class FootballMatchScoreModel(
    @SerializedName("match_id")
    val matchId: String,
    
    @SerializedName("country_name")
    val countryName: String?,
    
    @SerializedName("league_name")
    val leagueName: String?,
    
    @SerializedName("league_id")
    val leagueId: String?,
    
    @SerializedName("match_date")
    val matchDate: String?,
    
    @SerializedName("match_status")
    val matchStatus: String?,
    
    @SerializedName("match_time")
    val matchTime: String?,
    
    @SerializedName("match_hometeam_name")
    val homeTeamName: String?,
    
    @SerializedName("match_hometeam_score")
    val homeTeamScore: String?,
    
    @SerializedName("match_awayteam_name")
    val awayTeamName: String?,
    
    @SerializedName("match_awayteam_score")
    val awayTeamScore: String?,
    
    @SerializedName("league_logo")
    val leagueLogo: String?,
    
    @SerializedName("team_home_badge")
    val homeTeamBadge: String?,
    
    @SerializedName("team_away_badge")
    val awayTeamBadge: String?,
    
    @SerializedName("match_live")
    val isLive: String?
)
