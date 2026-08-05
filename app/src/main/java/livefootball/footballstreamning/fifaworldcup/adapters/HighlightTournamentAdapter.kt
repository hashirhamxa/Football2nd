package livecricket.livecrickettv.cricketstreaming.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.utilities.TournamentHighlight

class HighlightTournamentAdapter(
    private val items: List<TournamentHighlight>,
    private val onItemClick: (TournamentHighlight) -> Unit
) : RecyclerView.Adapter<HighlightTournamentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.text_tournament_name)
        val subtitle: TextView = view.findViewById(R.id.text_subtitle)
        val eventCount: TextView = view.findViewById(R.id.text_event_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_highlight_tournament, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.subtitle.text = item.subtitle
        holder.eventCount.text = "${item.eventCount} EVENTS"
        
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}
