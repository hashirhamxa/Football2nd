package bicodes.fifa.footballapp.feature.tournaments.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import bicodes.fifa.footballapp.R;
import bicodes.fifa.footballapp.feature.tournaments.data.LeagueEntity;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class LeagueAdapter extends RecyclerView.Adapter<LeagueAdapter.LeagueViewHolder> {

    private List<LeagueEntity> leagues = new ArrayList<>();
    private OnLeagueClickListener listener;

    public interface OnLeagueClickListener {
        void onLeagueClick(LeagueEntity league);
    }

    public void setLeagues(List<LeagueEntity> leagues) {
        this.leagues = leagues;
        notifyDataSetChanged();
    }

    public void setOnLeagueClickListener(OnLeagueClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LeagueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_league_card, parent, false);
        return new LeagueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeagueViewHolder holder, int position) {
        LeagueEntity league = leagues.get(position);

        holder.leagueName.setText(league.leagueName);
        holder.leagueCountry.setText(league.countryName);
        holder.leagueStatus.setText(league.leagueSeason);

        Glide.with(holder.itemView.getContext())
                .load(league.leagueLogo)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.leagueBadge);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLeagueClick(league);
            }
        });
    }

    @Override
    public int getItemCount() {
        return leagues.size();
    }

    static class LeagueViewHolder extends RecyclerView.ViewHolder {
        ImageView leagueBadge;
        TextView leagueName, leagueCountry, leagueStatus;

        public LeagueViewHolder(@NonNull View itemView) {
            super(itemView);
            leagueBadge = itemView.findViewById(R.id.league_badge);
            leagueName = itemView.findViewById(R.id.league_name_text);
            leagueCountry = itemView.findViewById(R.id.league_country);
            leagueStatus = itemView.findViewById(R.id.league_status);
        }
    }
}

