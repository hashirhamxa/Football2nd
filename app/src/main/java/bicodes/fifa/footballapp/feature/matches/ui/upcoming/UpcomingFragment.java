package bicodes.fifa.footballapp.feature.matches.ui.upcoming;

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
import bicodes.fifa.footballapp.feature.matches.data.MatchRepository;
import bicodes.fifa.footballapp.feature.matches.ui.upcoming.UpcomingMatchAdapter;
import bicodes.fifa.footballapp.feature.matches.ui.detail.MatchDetailBottomSheet;
import bicodes.fifa.footballapp.core.util.DateGroupUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UpcomingFragment extends Fragment {

    private MatchRepository matchRepository;
    private UpcomingMatchAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView noMatchesText;
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private static final long REFRESH_TIMEOUT = 2 * 60 * 1000;

    // Offline-first: always shows cached data, API only refreshes.
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        matchRepository = new MatchRepository(requireContext());
        adapter = new UpcomingMatchAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_upcoming);
        noMatchesText = view.findViewById(R.id.no_upcoming_matches_text);
        RecyclerView recyclerView = view.findViewById(R.id.upcoming_matches_recycler);

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

    private boolean isFirstLoad = true;

    private void observeData() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        matchRepository.getUpcomingMatches(today).observe(getViewLifecycleOwner(), matches -> {
            if (matches != null && !matches.isEmpty()) {
                adapter.setData(DateGroupUtils.groupUpcomingByDate(matches));
                noMatchesText.setVisibility(View.GONE);
                isFirstLoad = false;
            } else {
                noMatchesText.setVisibility(View.VISIBLE);
                // Trigger refresh if empty (First load)
                if (isFirstLoad) {
                    isFirstLoad = false;
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
        
        // Refresh next 7 days
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        String from = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, 7);
        String to = sdf.format(cal.getTime());

        matchRepository.refreshUpcomingMatches(from, to);
        
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
