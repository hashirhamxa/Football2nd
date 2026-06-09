package bicodes.fifa.footballapp.feature.matches.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "matches")
public class MatchEntity {
    @PrimaryKey
    @NonNull
    public String matchId;
    
    public String countryName;
    public String leagueName;
    public String leagueId;
    public String matchDate;
    public String matchStatus;
    public String matchTime;
    public String homeTeamName;
    public String homeTeamScore;
    public String awayTeamName;
    public String awayTeamScore;
    public String leagueLogo;
    public String homeTeamBadge;
    public String awayTeamBadge;
    public String isLive; // "1" for live, "0" for not
    
    // Store events as JSON string
    public String eventsJson;
    
    // For cache management
    public long timestamp;
}

