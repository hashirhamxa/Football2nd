package bicodes.fifa.footballapp.feature.matches.ui.past;

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
import bicodes.fifa.footballapp.core.util.DateGroupUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class PastMatchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DateGroupUtils.DisplayItem> items = new ArrayList<>();
    private OnMatchClickListener listener;

    public void setData(List<DateGroupUtils.DisplayItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setOnMatchClickListener(OnMatchClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == DateGroupUtils.DisplayItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_past_date_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_past_match, parent, false);
            return new MatchViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DateGroupUtils.DisplayItem item = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).headerText.setText(item.headerText);
        } else if (holder instanceof MatchViewHolder) {
            MatchViewHolder matchHolder = (MatchViewHolder) holder;
            MatchEntity match = item.match;

            matchHolder.leagueName.setText(match.leagueName);
            matchHolder.homeTeamName.setText(match.homeTeamName);
            matchHolder.awayTeamName.setText(match.awayTeamName);
            
            String score = (match.homeTeamScore == null ? "0" : match.homeTeamScore) + " - " + 
                           (match.awayTeamScore == null ? "0" : match.awayTeamScore);
            matchHolder.matchScore.setText(score);
            matchHolder.matchDateText.setText(match.matchDate);

            Glide.with(matchHolder.itemView.getContext())
                    .load(match.leagueLogo)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(matchHolder.leagueLogo);

            Glide.with(matchHolder.itemView.getContext())
                    .load(match.homeTeamBadge)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(matchHolder.homeTeamBadge);

            Glide.with(matchHolder.itemView.getContext())
                    .load(match.awayTeamBadge)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(matchHolder.awayTeamBadge);

            matchHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMatchClick(match);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.date_header_text);
        }
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        ImageView leagueLogo, homeTeamBadge, awayTeamBadge;
        TextView leagueName, homeTeamName, awayTeamName, matchScore, matchDateText;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            leagueLogo = itemView.findViewById(R.id.league_logo);
            homeTeamBadge = itemView.findViewById(R.id.home_team_badge);
            awayTeamBadge = itemView.findViewById(R.id.away_team_badge);
            leagueName = itemView.findViewById(R.id.league_name);
            homeTeamName = itemView.findViewById(R.id.home_team_name);
            awayTeamName = itemView.findViewById(R.id.away_team_name);
            matchScore = itemView.findViewById(R.id.match_score);
            matchDateText = itemView.findViewById(R.id.match_date_text);
        }
    }
}

