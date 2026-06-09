package bicodes.fifa.footballapp.core.network;

import bicodes.fifa.footballapp.feature.matches.model.Match;
import bicodes.fifa.footballapp.feature.standings.model.Standing;
import com.google.gson.JsonElement;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FootballApiService {

    // Fetch matches - using JsonElement to handle both List and Error Object
    @GET(".")
    Call<JsonElement> getMatches(
            @Query("action") String action,
            @Query("from") String from,
            @Query("to") String to,
            @Query("league_id") String leagueId,
            @Query("match_id") String matchId,
            @Query("match_live") String isLive,
            @Query("APIkey") String apiKey
    );

    // Fetch standings
    @GET(".")
    Call<JsonElement> getStandings(
            @Query("action") String action,
            @Query("league_id") String leagueId,
            @Query("APIkey") String apiKey
    );

    // Fetch active leagues
    @GET(".")
    Call<JsonElement> getLeagues(
            @Query("action") String action,
            @Query("APIkey") String apiKey
    );
}

