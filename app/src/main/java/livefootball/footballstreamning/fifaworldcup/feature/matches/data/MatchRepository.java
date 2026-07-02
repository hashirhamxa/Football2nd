package livefootball.footballstreamning.fifaworldcup.feature.matches.data;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import livefootball.footballstreamning.fifaworldcup.core.network.FootballApiService;
import livefootball.footballstreamning.fifaworldcup.core.network.RetrofitClient;
import livefootball.footballstreamning.fifaworldcup.core.database.AppDatabase;
import livefootball.footballstreamning.fifaworldcup.feature.matches.data.MatchDao;
import livefootball.footballstreamning.fifaworldcup.feature.matches.data.MatchEntity;
import livefootball.footballstreamning.fifaworldcup.feature.matches.model.Match;
import livefootball.footballstreamning.fifaworldcup.core.util.Config;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchRepository {
    private final MatchDao matchDao;
    private final FootballApiService apiService;
    private final ExecutorService executorService;

    public MatchRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        matchDao = db.matchDao();
        apiService = RetrofitClient.getApiService();
        executorService = Executors.newSingleThreadExecutor();
    }

    // --- Live Matches ---
    public LiveData<List<MatchEntity>> getLiveMatches() {
        return matchDao.getLiveMatches();
    }

    public void refreshLiveMatches() {
        apiService.getMatches(Config.ACTION_GET_EVENTS, null, null, null, null, "1", Config.API_KEY)
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JsonElement body = response.body();
                            if (body.isJsonArray()) {
                                List<Match> matches = new Gson().fromJson(body, new TypeToken<List<Match>>(){}.getType());
                                executorService.execute(() -> {
                                    matchDao.clearLiveMatches();
                                    matchDao.insertAll(convertToEntities(matches, true));
                                });
                            } else {
                                Log.e("MatchRepository", "API returned Object instead of Array (possible error or no data)");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {
                        Log.e("MatchRepository", "Failed to fetch live matches", t);
                    }
                });
    }

    // --- Matches by Date (Past/Upcoming) ---
    public LiveData<List<MatchEntity>> getMatchesByDate(String date) {
        return matchDao.getMatchesByDate(date);
    }

    public LiveData<List<MatchEntity>> getPastMatches(String today) {
        return matchDao.getPastMatches(today);
    }

    public LiveData<List<MatchEntity>> getUpcomingMatches(String today) {
        return matchDao.getUpcomingMatches(today);
    }

    public void refreshUpcomingMatches(String from, String to) {
        apiService.getMatches(Config.ACTION_GET_EVENTS, from, to, null, null, null, Config.API_KEY)
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JsonElement body = response.body();
                            if (body.isJsonArray()) {
                                List<Match> matches = new Gson().fromJson(body, new TypeToken<List<Match>>(){}.getType());
                                executorService.execute(() -> {
                                    matchDao.insertAll(convertToEntities(matches, false));
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {
                        Log.e("MatchRepository", "Failed to refresh upcoming matches", t);
                    }
                });
    }

    public LiveData<MatchEntity> getMatchById(String id) {
        return matchDao.getMatchById(id);
    }

    public void refreshMatchDetail(String matchId) {
        apiService.getMatches(Config.ACTION_GET_EVENTS, null, null, null, matchId, null, Config.API_KEY)
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JsonElement body = response.body();
                            if (body.isJsonArray()) {
                                List<Match> matches = new Gson().fromJson(body, new TypeToken<List<Match>>(){}.getType());
                                executorService.execute(() -> {
                                    matchDao.insertAll(convertToEntities(matches, false));
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {
                        Log.e("MatchRepository", "Failed to refresh match detail", t);
                    }
                });
    }

    public void refreshPastMatches(String from, String to) {
        apiService.getMatches(Config.ACTION_GET_EVENTS, from, to, null, null, null, Config.API_KEY)
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JsonElement body = response.body();
                            if (body.isJsonArray()) {
                                List<Match> matches = new Gson().fromJson(body, new TypeToken<List<Match>>(){}.getType());
                                executorService.execute(() -> {
                                    // For simplicity, we just insert all. 
                                    // In a real app, you might want to delete old ones in that range first.
                                    matchDao.insertAll(convertToEntities(matches, false));
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {
                        Log.e("MatchRepository", "Failed to refresh past matches", t);
                    }
                });
    }

    public void refreshMatchesByDate(String date) {
        apiService.getMatches(Config.ACTION_GET_EVENTS, date, date, null, null, null, Config.API_KEY)
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            JsonElement body = response.body();
                            if (body.isJsonArray()) {
                                List<Match> matches = new Gson().fromJson(body, new TypeToken<List<Match>>(){}.getType());
                                executorService.execute(() -> {
                                    matchDao.deleteByDate(date);
                                    matchDao.insertAll(convertToEntities(matches, false));
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {
                        Log.e("MatchRepository", "Failed to fetch matches for date: " + date, t);
                    }
                });
    }

    // Helper to convert API models to Room entities
    private List<MatchEntity> convertToEntities(List<Match> matches, boolean isLive) {
        List<MatchEntity> entities = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        Gson gson = new Gson();
        for (Match m : matches) {
            MatchEntity entity = new MatchEntity();
            entity.matchId = m.matchId;
            entity.countryName = m.countryName;
            entity.leagueName = m.leagueName;
            entity.leagueId = m.leagueId;
            entity.matchDate = m.matchDate;
            entity.matchStatus = m.matchStatus;
            entity.matchTime = m.matchTime;
            entity.homeTeamName = m.homeTeamName;
            entity.homeTeamScore = m.homeTeamScore;
            entity.awayTeamName = m.awayTeamName;
            entity.awayTeamScore = m.awayTeamScore;
            entity.leagueLogo = m.leagueLogo;
            entity.homeTeamBadge = m.homeTeamBadge;
            entity.awayTeamBadge = m.awayTeamBadge;
            entity.isLive = isLive ? "1" : "0";
            
            // Serialize events (goalscorers and cards)
            JsonObject eventsObj = new JsonObject();
            if (m.goalscorers != null && m.goalscorers.isJsonArray()) {
                eventsObj.add("goalscorer", m.goalscorers);
            }
            if (m.cards != null && m.cards.isJsonArray()) {
                eventsObj.add("cards", m.cards);
            }
            entity.eventsJson = gson.toJson(eventsObj);

            entity.timestamp = currentTime;
            entities.add(entity);
        }
        return entities;
    }
}

