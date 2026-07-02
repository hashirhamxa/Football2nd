package livefootball.footballstreamning.fifaworldcup.feature.standings.data;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import livefootball.footballstreamning.fifaworldcup.core.network.FootballApiService;
import livefootball.footballstreamning.fifaworldcup.core.network.RetrofitClient;
import livefootball.footballstreamning.fifaworldcup.core.database.AppDatabase;
import livefootball.footballstreamning.fifaworldcup.feature.tournaments.data.LeagueDao;
import livefootball.footballstreamning.fifaworldcup.feature.tournaments.data.LeagueEntity;
import livefootball.footballstreamning.fifaworldcup.feature.standings.data.StandingDao;
import livefootball.footballstreamning.fifaworldcup.feature.standings.data.StandingEntity;
import livefootball.footballstreamning.fifaworldcup.feature.tournaments.model.League;
import livefootball.footballstreamning.fifaworldcup.feature.standings.model.Standing;
import livefootball.footballstreamning.fifaworldcup.core.util.Config;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StandingRepository {
    private final StandingDao standingDao;
    private final LeagueDao leagueDao;
    private final FootballApiService apiService;
    private final ExecutorService executorService;

    public StandingRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        standingDao = db.standingDao();
        leagueDao = db.leagueDao();
        apiService = RetrofitClient.getApiService();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<LeagueEntity>> getLeagues() {
        refreshLeagues();
        return leagueDao.getAllLeagues();
    }

    public void refreshLeagues() {
        apiService.getLeagues(Config.ACTION_GET_LEAGUES, Config.API_KEY)
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JsonElement body = response.body();
                            if (body.isJsonArray()) {
                                List<League> leagues = new Gson().fromJson(body, new TypeToken<List<League>>(){}.getType());
                                executorService.execute(() -> {
                                    List<LeagueEntity> entities = new ArrayList<>();
                                    long currentTime = System.currentTimeMillis();
                                    for (League l : leagues) {
                                        LeagueEntity entity = new LeagueEntity();
                                        entity.leagueId = l.leagueId;
                                        entity.leagueName = l.leagueName;
                                        entity.countryName = l.countryName;
                                        entity.leagueLogo = l.leagueLogo;
                                        entity.leagueSeason = l.leagueSeason;
                                        entity.timestamp = currentTime;
                                        entities.add(entity);
                                    }
                                    leagueDao.deleteAll();
                                    leagueDao.insertAll(entities);
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {
                        Log.e("StandingRepository", "Failed to fetch leagues", t);
                    }
                });
    }

    public LiveData<List<StandingEntity>> getStandings(String leagueId) {
        refreshStandings(leagueId);
        return standingDao.getStandingsByLeague(leagueId);
    }

    public void refreshStandings(String leagueId) {
        apiService.getStandings(Config.ACTION_GET_STANDINGS, leagueId, Config.API_KEY)
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JsonElement body = response.body();
                            if (body.isJsonArray()) {
                                List<Standing> standings = new Gson().fromJson(body, new TypeToken<List<Standing>>(){}.getType());
                                executorService.execute(() -> {
                                    standingDao.deleteByLeague(leagueId);
                                    standingDao.insertAll(convertToEntities(standings));
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {
                        Log.e("StandingRepository", "Failed to fetch standings for league: " + leagueId, t);
                    }
                });
    }

    private List<StandingEntity> convertToEntities(List<Standing> standings) {
        List<StandingEntity> entities = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        for (Standing s : standings) {
            StandingEntity entity = new StandingEntity();
            entity.leagueId = s.leagueId;
            entity.teamId = s.teamId;
            entity.teamName = s.teamName;
            entity.position = s.position;
            entity.played = s.played;
            entity.won = s.won;
            entity.draw = s.draw;
            entity.lost = s.lost;
            entity.goalsFor = s.goalsFor;
            entity.goalsAgainst = s.goalsAgainst;
            entity.points = s.points;
            entity.teamBadge = s.teamBadge;
            entity.timestamp = currentTime;
            entities.add(entity);
        }
        return entities;
    }
}

