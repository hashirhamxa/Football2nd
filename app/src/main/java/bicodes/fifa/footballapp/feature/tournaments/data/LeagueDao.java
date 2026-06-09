package bicodes.fifa.footballapp.feature.tournaments.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LeagueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LeagueEntity> leagues);

    @Query("SELECT * FROM leagues ORDER BY leagueName ASC")
    LiveData<List<LeagueEntity>> getAllLeagues();

    @Query("DELETE FROM leagues")
    void deleteAll();
}

