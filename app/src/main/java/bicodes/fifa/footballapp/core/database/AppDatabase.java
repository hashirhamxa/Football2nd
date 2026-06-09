package bicodes.fifa.footballapp.core.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import bicodes.fifa.footballapp.feature.matches.data.MatchDao;
import bicodes.fifa.footballapp.feature.matches.data.MatchEntity;
import bicodes.fifa.footballapp.feature.standings.data.StandingDao;
import bicodes.fifa.footballapp.feature.standings.data.StandingEntity;
import bicodes.fifa.footballapp.feature.tournaments.data.LeagueDao;
import bicodes.fifa.footballapp.feature.tournaments.data.LeagueEntity;

@Database(entities = {MatchEntity.class, StandingEntity.class, LeagueEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract MatchDao matchDao();
    public abstract StandingDao standingDao();
    public abstract LeagueDao leagueDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "footballapp_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}

