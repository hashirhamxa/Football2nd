package livefootball.footballstreamning.fifaworldcup.feature.standings.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StandingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StandingEntity> standings);

    @Query("SELECT * FROM standings WHERE leagueId = :leagueId ORDER BY position ASC")
    LiveData<List<StandingEntity>> getStandingsByLeague(String leagueId);

    @Query("DELETE FROM standings WHERE leagueId = :leagueId")
    void deleteByLeague(String leagueId);
}

