package livefootball.footballstreamning.fifaworldcup.feature.tournaments.model;

import com.google.gson.annotations.SerializedName;

public class League {
    @SerializedName("league_id")
    public String leagueId;

    @SerializedName("league_name")
    public String leagueName;

    @SerializedName("country_name")
    public String countryName;

    @SerializedName("league_logo")
    public String leagueLogo;

    @SerializedName("league_season")
    public String leagueSeason;
}

