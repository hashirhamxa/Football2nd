package livefootball.footballstreamning.fifaworldcup.feature.matches.ui.detail;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import livefootball.footballstreamning.fifaworldcup.R;
import livefootball.footballstreamning.fifaworldcup.feature.matches.data.MatchEntity;
import livefootball.footballstreamning.fifaworldcup.feature.matches.model.Match;
import livefootball.footballstreamning.fifaworldcup.feature.matches.model.MatchEvent;
import livefootball.footballstreamning.fifaworldcup.feature.matches.data.MatchRepository;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MatchDetailBottomSheet extends BottomSheetDialogFragment {

    private String matchId;
    private MatchRepository repository;
    private EventAdapter eventAdapter;

    private ImageView homeBadge, awayBadge;
    private TextView leagueName, homeName, awayName, score, status, matchInfo;
    private TextView refereeText, venueText, countryText;

    public static MatchDetailBottomSheet newInstance(String matchId) {
        MatchDetailBottomSheet fragment = new MatchDetailBottomSheet();
        Bundle args = new Bundle();
        args.putString("match_id", matchId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getTheme() {
        return R.style.Theme_FootballApp_BottomSheetDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            matchId = getArguments().getString("match_id");
        }
        repository = new MatchRepository(requireContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog instanceof BottomSheetDialog) {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(Color.TRANSPARENT);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_match_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView(view);

        if (matchId == null) {
            Toast.makeText(getContext(), "Match not found", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        repository.getMatchById(matchId).observe(getViewLifecycleOwner(), match -> {
            if (match != null) {
                populateUI(match);
            } else {
                Toast.makeText(getContext(), R.string.error_match_not_found, Toast.LENGTH_SHORT).show();
            }
        });
        repository.refreshMatchDetail(matchId);
    }

    private void initViews(View view) {
        leagueName = view.findViewById(R.id.league_name_header);
        homeName = view.findViewById(R.id.home_name_detail);
        awayName = view.findViewById(R.id.away_name_detail);
        score = view.findViewById(R.id.score_detail);
        status = view.findViewById(R.id.status_detail);
        matchInfo = view.findViewById(R.id.match_info_detail);
        homeBadge = view.findViewById(R.id.home_badge_detail);
        awayBadge = view.findViewById(R.id.away_badge_detail);

        refereeText = view.findViewById(R.id.referee_text);
        venueText = view.findViewById(R.id.venue_text);
        countryText = view.findViewById(R.id.country_text);
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.events_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter();
        recyclerView.setAdapter(eventAdapter);
    }

    private void populateUI(MatchEntity match) {
        if (match == null || !isAdded()) return;

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
}
