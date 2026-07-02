package livefootball.footballstreamning.fifaworldcup.feature.matches.ui.past;

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

import android.content.Intent;
import livefootball.footballstreamning.fifaworldcup.R;
import livefootball.footballstreamning.fifaworldcup.feature.matches.data.MatchRepository;
import livefootball.footballstreamning.fifaworldcup.feature.matches.ui.past.PastMatchAdapter;
import livefootball.footballstreamning.fifaworldcup.feature.matches.ui.detail.MatchDetailBottomSheet;
import livefootball.footballstreamning.fifaworldcup.core.util.DateGroupUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PastFragment extends Fragment {

    private MatchRepository matchRepository;
    private PastMatchAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView noMatchesText;
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private static final long REFRESH_TIMEOUT = 2 * 60 * 1000;

    // Offline-first: always shows cached data, API only refreshes.
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        matchRepository = new MatchRepository(requireContext());
        adapter = new PastMatchAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_past, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_past);
        noMatchesText = view.findViewById(R.id.no_past_matches_text);
        RecyclerView recyclerView = view.findViewById(R.id.past_matches_recycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnMatchClickListener(match -> {
            MatchDetailBottomSheet bottomSheet = MatchDetailBottomSheet.newInstance(match.matchId);
            bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
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
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        matchRepository.getPastMatches(today).observe(getViewLifecycleOwner(), matches -> {
            if (matches != null && !matches.isEmpty()) {
                adapter.setData(DateGroupUtils.groupMatchesByDate(matches));
                noMatchesText.setVisibility(View.GONE);
            } else {
                noMatchesText.setVisibility(View.VISIBLE);
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
        
        // Refresh last 7 days for the "Recent" view
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        String to = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, -7);
        String from = sdf.format(cal.getTime());

        matchRepository.refreshPastMatches(from, to);
        
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

