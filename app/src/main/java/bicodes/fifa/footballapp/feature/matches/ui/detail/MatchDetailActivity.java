package bicodes.fifa.footballapp.feature.matches.ui.detail;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import bicodes.fifa.footballapp.R;
import bicodes.fifa.footballapp.feature.matches.data.MatchEntity;
import bicodes.fifa.footballapp.feature.matches.model.Match;
import bicodes.fifa.footballapp.feature.matches.model.MatchEvent;
import bicodes.fifa.footballapp.feature.matches.data.MatchRepository;
import bicodes.fifa.footballapp.feature.matches.ui.detail.EventAdapter;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MatchDetailActivity extends AppCompatActivity {

    // Offline-first: always shows cached data, API only refreshes.
    private String matchId;
    private MatchRepository repository;
    private EventAdapter eventAdapter;

    private ImageView homeBadge, awayBadge;
    private TextView leagueName, homeName, awayName, score, status, matchInfo;
    private TextView refereeText, venueText, countryText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_detail);

        matchId = getIntent().getStringExtra("match_id");
        if (matchId == null) {
            Toast.makeText(this, "Match not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repository = new MatchRepository(this);
        initViews();
        setupRecyclerView();

        repository.getMatchById(matchId).observe(this, match -> {
            if (match != null) {
                populateUI(match);
            } else {
                Toast.makeText(this, R.string.error_match_not_found, Toast.LENGTH_SHORT).show();
            }
        });
        repository.refreshMatchDetail(matchId);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Match Detail");
        }

        leagueName = findViewById(R.id.league_name_header);
        homeName = findViewById(R.id.home_name_detail);
        awayName = findViewById(R.id.away_name_detail);
        score = findViewById(R.id.score_detail);
        status = findViewById(R.id.status_detail);
        matchInfo = findViewById(R.id.match_info_detail);
        homeBadge = findViewById(R.id.home_badge_detail);
        awayBadge = findViewById(R.id.away_badge_detail);

        refereeText = findViewById(R.id.referee_text);
        venueText = findViewById(R.id.venue_text);
        countryText = findViewById(R.id.country_text);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.events_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter();
        recyclerView.setAdapter(eventAdapter);
    }

    private void populateUI(MatchEntity match) {
        if (match == null) return;

        leagueName.setText(match.leagueName);
        homeName.setText(match.homeTeamName);
        awayName.setText(match.awayTeamName);
        score.setText(getString(R.string.score_format, match.homeTeamScore, match.awayTeamScore));
        status.setText(match.matchStatus);
        matchInfo.setText(match.matchDate + " - " + match.matchTime);

        refereeText.setText(getString(R.string.referee_label, "N/A"));
        venueText.setText(getString(R.string.venue_label, "N/A"));
        
        String country = (match.countryName == null || match.countryName.isEmpty()) ? "N/A" : match.countryName;
        countryText.setText(getString(R.string.country_label, country));

        Glide.with(this).load(match.homeTeamBadge).placeholder(android.R.drawable.ic_menu_gallery).into(homeBadge);
        Glide.with(this).load(match.awayTeamBadge).placeholder(android.R.drawable.ic_menu_gallery).into(awayBadge);

        if (match.eventsJson != null) {
            parseEvents(match.eventsJson);
        }
    }

    private void parseEvents(String json) {
        Gson gson = new Gson();
        JsonObject root = gson.fromJson(json, JsonObject.class);
        List<MatchEvent> matchEvents = new ArrayList<>();

        if (root.has("goalscorer")) {
            JsonArray goals = root.getAsJsonArray("goalscorer");
            for (JsonElement e : goals) {
                Match.Goalscorer g = gson.fromJson(e, Match.Goalscorer.class);
                String player = g.homeScorer.isEmpty() ? g.awayScorer : g.homeScorer;
                matchEvents.add(new MatchEvent(g.time, player, g.score, MatchEvent.Type.GOAL));
            }
        }

        if (root.has("cards")) {
            JsonArray cards = root.getAsJsonArray("cards");
            for (JsonElement e : cards) {
                Match.Card c = gson.fromJson(e, Match.Card.class);
                String player = c.homeFault.isEmpty() ? c.awayFault : c.homeFault;
                MatchEvent.Type type = c.cardType.toLowerCase().contains("yellow") ? 
                        MatchEvent.Type.YELLOW_CARD : MatchEvent.Type.RED_CARD;
                matchEvents.add(new MatchEvent(c.time, player, null, type));
            }
        }

        // Sort events by time
        Collections.sort(matchEvents, new Comparator<MatchEvent>() {
            @Override
            public int compare(MatchEvent e1, MatchEvent e2) {
                try {
                    int t1 = Integer.parseInt(e1.time.replaceAll("[^0-9]", ""));
                    int t2 = Integer.parseInt(e2.time.replaceAll("[^0-9]", ""));
                    return Integer.compare(t1, t2);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        });

        eventAdapter.setEvents(matchEvents);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

