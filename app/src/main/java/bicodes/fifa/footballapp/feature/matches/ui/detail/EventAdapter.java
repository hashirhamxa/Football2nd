package bicodes.fifa.footballapp.feature.matches.ui.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import bicodes.fifa.footballapp.R;
import bicodes.fifa.footballapp.feature.matches.model.MatchEvent;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<MatchEvent> events = new ArrayList<>();

    public void setEvents(List<MatchEvent> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_row, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        MatchEvent event = events.get(position);
        holder.time.setText(event.time + "'");
        holder.player.setText(event.playerName);
        holder.score.setText(event.score != null ? event.score : "");

        switch (event.type) {
            case GOAL:
                holder.icon.setImageResource(android.R.drawable.ic_menu_myplaces); // Placeholder for Goal icon
                break;
            case YELLOW_CARD:
                holder.icon.setImageResource(android.R.drawable.ic_menu_info_details); // Placeholder
                break;
            case RED_CARD:
                holder.icon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel); // Placeholder
                break;
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView time, player, score;
        ImageView icon;

        public EventViewHolder(@NonNull View view) {
            super(view);
            time = view.findViewById(R.id.event_time);
            player = view.findViewById(R.id.event_player);
            score = view.findViewById(R.id.event_score);
            icon = view.findViewById(R.id.event_icon);
        }
    }
}

