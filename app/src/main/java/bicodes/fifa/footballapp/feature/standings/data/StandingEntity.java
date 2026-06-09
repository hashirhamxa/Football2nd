package bicodes.fifa.footballapp.feature.standings.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "standings", primaryKeys = {"leagueId", "teamId"})
public class StandingEntity {
    @NonNull
    public String leagueId;
    @NonNull
    public String teamId;
    
    public String teamName;
    public String position;
    public String played;
    public String won;
    public String draw;
    public String lost;
    public String goalsFor;
    public String goalsAgainst;
    public String points;
    public String teamBadge;
    
    public long timestamp;
}

