package livefootball.footballstreamning.fifaworldcup.feature.matches.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MatchEntity> matches);

    @Query("SELECT * FROM matches WHERE isLive = '1'")
    LiveData<List<MatchEntity>> getLiveMatches();

    @Query("SELECT * FROM matches WHERE matchDate = :date")
    LiveData<List<MatchEntity>> getMatchesByDate(String date);

    @Query("SELECT * FROM matches WHERE matchDate < :today AND isLive = '0' ORDER BY matchDate DESC, matchTime DESC")
    LiveData<List<MatchEntity>> getPastMatches(String today);

    @Query("SELECT * FROM matches WHERE matchDate >= :today AND isLive = '0' ORDER BY matchDate ASC, matchTime ASC")
    LiveData<List<MatchEntity>> getUpcomingMatches(String today);

    @Query("SELECT * FROM matches WHERE matchId = :id")
    LiveData<MatchEntity> getMatchById(String id);

    @Query("DELETE FROM matches WHERE isLive = '1'")
    void clearLiveMatches();
    
    @Query("DELETE FROM matches WHERE matchDate = :date")
    void deleteByDate(String date);
}

