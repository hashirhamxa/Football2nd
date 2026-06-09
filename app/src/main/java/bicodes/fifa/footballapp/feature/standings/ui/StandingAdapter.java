package bicodes.fifa.footballapp.feature.standings.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import bicodes.fifa.footballapp.R;
import bicodes.fifa.footballapp.feature.standings.data.StandingEntity;

import java.util.ArrayList;
import java.util.List;

public class StandingAdapter extends RecyclerView.Adapter<StandingAdapter.StandingViewHolder> {

    private List<StandingEntity> standings = new ArrayList<>();

    public void setStandings(List<StandingEntity> standings) {
        this.standings = standings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StandingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_standing_row, parent, false);
        return new StandingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StandingViewHolder holder, int position) {
        StandingEntity team = standings.get(position);

        holder.pos.setText(team.position);
        holder.teamName.setText(team.teamName);
        holder.played.setText(team.played);
        holder.won.setText(team.won);
        holder.draw.setText(team.draw);
        holder.lost.setText(team.lost);
        
        // Calculate Goal Difference if not available directly
        int gf = 0, ga = 0;
        try {
            gf = Integer.parseInt(team.goalsFor != null ? team.goalsFor : "0");
            ga = Integer.parseInt(team.goalsAgainst != null ? team.goalsAgainst : "0");
        } catch (NumberFormatException ignored) {}
        
        int gd = gf - ga;
        holder.gd.setText(holder.itemView.getContext().getString(R.string.gd_format, (gd > 0 ? "+" : ""), gd));
        
        holder.pts.setText(team.points);
    }

    @Override
    public int getItemCount() {
        return standings.size();
    }

    static class StandingViewHolder extends RecyclerView.ViewHolder {
        TextView pos, teamName, played, won, draw, lost, gd, pts;

        public StandingViewHolder(@NonNull View itemView) {
            super(itemView);
            pos = itemView.findViewById(R.id.standing_pos);
            teamName = itemView.findViewById(R.id.standing_team);
            played = itemView.findViewById(R.id.standing_p);
            won = itemView.findViewById(R.id.standing_w);
            draw = itemView.findViewById(R.id.standing_d);
            lost = itemView.findViewById(R.id.standing_l);
            gd = itemView.findViewById(R.id.standing_gd);
            pts = itemView.findViewById(R.id.standing_pts);
        }
    }
}

