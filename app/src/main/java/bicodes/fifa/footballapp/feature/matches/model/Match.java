package bicodes.fifa.footballapp.feature.matches.model;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Match {
    @SerializedName("match_id")
    public String matchId;
    
    @SerializedName("country_name")
    public String countryName;
    
    @SerializedName("league_name")
    public String leagueName;
    
    @SerializedName("league_id")
    public String leagueId;
    
    @SerializedName("match_date")
    public String matchDate;
    
    @SerializedName("match_status")
    public String matchStatus;
    
    @SerializedName("match_time")
    public String matchTime;
    
    @SerializedName("match_hometeam_name")
    public String homeTeamName;
    
    @SerializedName("match_hometeam_score")
    public String homeTeamScore;
    
    @SerializedName("match_awayteam_name")
    public String awayTeamName;
    
    @SerializedName("match_awayteam_score")
    public String awayTeamScore;
    
    @SerializedName("league_logo")
    public String leagueLogo;
    
    @SerializedName("team_home_badge")
    public String homeTeamBadge;
    
    @SerializedName("team_away_badge")
    public String awayTeamBadge;
    
    @SerializedName("match_live")
    public String isLive;

    // Use JsonElement to handle cases where API returns "" instead of []
    @SerializedName("goalscorer")
    public JsonElement goalscorers;

    @SerializedName("cards")
    public JsonElement cards;

    public static class Goalscorer {
        @SerializedName("time") public String time;
        @SerializedName("home_scorer") public String homeScorer;
        @SerializedName("away_scorer") public String awayScorer;
        @SerializedName("score") public String score;
    }

    public static class Card {
        @SerializedName("time") public String time;
        @SerializedName("home_fault") public String homeFault;
        @SerializedName("away_fault") public String awayFault;
        @SerializedName("card") public String cardType;
    }
}

