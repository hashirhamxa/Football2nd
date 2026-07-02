package livefootball.footballstreamning.fifaworldcup.feature.tournaments.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "leagues")
public class LeagueEntity {
    @PrimaryKey
    @NonNull
    public String leagueId;

    public String leagueName;
    public String countryName;
    public String leagueLogo;
    public String leagueSeason;
    
    public long timestamp;
}

