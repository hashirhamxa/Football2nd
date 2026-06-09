package bicodes.fifa.footballapp.feature.standings.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import bicodes.fifa.footballapp.R;
import bicodes.fifa.footballapp.feature.standings.data.StandingRepository;
import bicodes.fifa.footballapp.feature.standings.ui.StandingAdapter;

public class StandingsActivity extends AppCompatActivity {

    // Offline-first: always shows cached data, API only refreshes.
    private String leagueId;
    private String leagueName;
    private StandingRepository repository;
    private StandingAdapter adapter;
    private TextView noStandingsText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standings);

        leagueId = getIntent().getStringExtra("league_id");
        leagueName = getIntent().getStringExtra("league_name");

        if (leagueId == null) {
            Toast.makeText(this, "League not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repository = new StandingRepository(this);
        initViews();
        setupRecyclerView();

        repository.getStandings(leagueId).observe(this, standings -> {
            if (standings != null && !standings.isEmpty()) {
                adapter.setStandings(standings);
                noStandingsText.setVisibility(View.GONE);
            } else {
                noStandingsText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.standings_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(leagueName + " Standings");
        }
        noStandingsText = findViewById(R.id.no_standings_text);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.standings_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StandingAdapter();
        recyclerView.setAdapter(adapter);
        
        // Add divider for better table visibility
        recyclerView.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(this, 
                androidx.recyclerview.widget.DividerItemDecoration.VERTICAL));
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

