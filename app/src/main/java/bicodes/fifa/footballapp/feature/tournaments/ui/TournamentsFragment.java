package bicodes.fifa.footballapp.feature.tournaments.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import bicodes.fifa.footballapp.R;
import bicodes.fifa.footballapp.feature.standings.data.StandingRepository;
import bicodes.fifa.footballapp.feature.tournaments.ui.LeagueAdapter;
import bicodes.fifa.footballapp.feature.standings.ui.StandingsActivity;

public class TournamentsFragment extends Fragment {

    private StandingRepository repository;
    private LeagueAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView noTournamentsText;
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private static final long REFRESH_TIMEOUT = 2 * 60 * 1000;

    // Offline-first: always shows cached data, API only refreshes.
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new StandingRepository(requireContext());
        adapter = new LeagueAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tournaments, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_tournaments);
        noTournamentsText = view.findViewById(R.id.no_tournaments_text);
        RecyclerView recyclerView = view.findViewById(R.id.tournaments_recycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnLeagueClickListener(league -> {
            Intent intent = new Intent(requireContext(), StandingsActivity.class);
            intent.putExtra("league_id", league.leagueId);
            intent.putExtra("league_name", league.leagueName);
            startActivity(intent);
        });

        // Swipe to Refresh - No API call, just show spinner for 40s
        swipeRefreshLayout.setOnRefreshListener(this::handleManualSwipe);

        observeData();

        return view;
    }

    private void handleManualSwipe() {
        swipeRefreshLayout.setRefreshing(true);
        // Do NOT call API. Just wait 40 seconds then hide.
        timeoutHandler.postDelayed(this::stopRefreshing, 40 * 1000);
    }

    private void observeData() {
        repository.getLeagues().observe(getViewLifecycleOwner(), leagues -> {
            if (leagues != null && !leagues.isEmpty()) {
                adapter.setLeagues(leagues);
                noTournamentsText.setVisibility(View.GONE);
            } else {
                noTournamentsText.setVisibility(View.VISIBLE);
                // Trigger refresh if empty (First load)
                if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
                    refreshData();
                }
            }
            stopRefreshing();
        });
    }

    private void refreshData() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        repository.refreshLeagues();
        timeoutHandler.postDelayed(() -> {
            if (isAdded() && swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                stopRefreshing();
                Toast.makeText(getContext(), R.string.error_loading_data, Toast.LENGTH_SHORT).show();
            }
        }, REFRESH_TIMEOUT);
    }

    private void stopRefreshing() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        timeoutHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timeoutHandler.removeCallbacksAndMessages(null);
    }
}

