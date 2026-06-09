package bicodes.fifa.footballapp.feature.matches.ui.live;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import bicodes.fifa.footballapp.R;
import bicodes.fifa.footballapp.app.ui.OnMatchClickListener;
import bicodes.fifa.footballapp.feature.matches.data.MatchEntity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class LiveMatchAdapter extends RecyclerView.Adapter<LiveMatchAdapter.MatchViewHolder> {

    private List<MatchEntity> matches = new ArrayList<>();
    private OnMatchClickListener listener;

    public void setMatches(List<MatchEntity> matches) {
        this.matches = matches;
        notifyDataSetChanged();
    }

    public void setOnMatchClickListener(OnMatchClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_live_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        MatchEntity match = matches.get(position);

        holder.leagueName.setText(match.leagueName);
        holder.homeTeamName.setText(match.homeTeamName);
        holder.awayTeamName.setText(match.awayTeamName);
        
        String score = (match.homeTeamScore == null ? "0" : match.homeTeamScore) + " - " + 
                       (match.awayTeamScore == null ? "0" : match.awayTeamScore);
        holder.matchScore.setText(score);
        
        holder.matchTime.setText(match.matchTime);
        holder.matchStatus.setText(match.matchStatus);

        // Load logos
        Glide.with(holder.itemView.getContext())
                .load(match.leagueLogo)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.leagueLogo);

        Glide.with(holder.itemView.getContext())
                .load(match.homeTeamBadge)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.homeTeamBadge);

        Glide.with(holder.itemView.getContext())
                .load(match.awayTeamBadge)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.awayTeamBadge);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMatchClick(match);
            }
        });
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        ImageView leagueLogo, homeTeamBadge, awayTeamBadge;
        TextView leagueName, homeTeamName, awayTeamName, matchScore, matchTime, matchStatus;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            leagueLogo = itemView.findViewById(R.id.league_logo);
            homeTeamBadge = itemView.findViewById(R.id.home_team_badge);
            awayTeamBadge = itemView.findViewById(R.id.away_team_badge);
            leagueName = itemView.findViewById(R.id.league_name);
            homeTeamName = itemView.findViewById(R.id.home_team_name);
            awayTeamName = itemView.findViewById(R.id.away_team_name);
            matchScore = itemView.findViewById(R.id.match_score);
            matchTime = itemView.findViewById(R.id.match_time);
            matchStatus = itemView.findViewById(R.id.match_status);
        }
    }
}

