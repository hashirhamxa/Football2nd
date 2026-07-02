package livefootball.footballstreamning.fifaworldcup.feature.standings.model;

import com.google.gson.annotations.SerializedName;

public class Standing {
    @SerializedName("league_id")
    public String leagueId;

    @SerializedName("team_id")
    public String teamId;

    @SerializedName("team_name")
    public String teamName;

    @SerializedName("overall_league_position")
    public String position;

    @SerializedName("overall_league_payed")
    public String played;

    @SerializedName("overall_league_W")
    public String won;

    @SerializedName("overall_league_D")
    public String draw;

    @SerializedName("overall_league_L")
    public String lost;

    @SerializedName("overall_league_GF")
    public String goalsFor;

    @SerializedName("overall_league_GA")
    public String goalsAgainst;

    @SerializedName("overall_league_PTS")
    public String points;

    @SerializedName("team_badge")
    public String teamBadge;
}

