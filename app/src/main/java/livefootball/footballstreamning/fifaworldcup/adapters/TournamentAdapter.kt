package livefootball.footballstreamning.fifaworldcup.adapters

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import livecricket.livecrickettv.cricketstreaming.activities.TournamentActivity
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.activities.EventActivity
import livefootball.footballstreamning.fifaworldcup.activities.LinksActivity
import livefootball.footballstreamning.fifaworldcup.utilities.HomeMatch

class TournamentAdapter(private val items: List<HomeMatch>, private val isHighlightsMode: Boolean = false) :
    RecyclerView.Adapter<TournamentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: View = view
        val tournamentName: TextView = view.findViewById(R.id.text_tournament_name)
        val liveBadge: TextView = view.findViewById(R.id.text_live_badge)
        val matchTitle: TextView = view.findViewById(R.id.text_match_title)
        val statusMain: TextView = view.findViewById(R.id.text_status_main)
        val statusSub: TextView = view.findViewById(R.id.text_status_sub)
        val btnAction: AppCompatButton = view.findViewById(R.id.btn_action)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tournament_large, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tournamentName.text = item.tournament
        holder.matchTitle.text = item.title
        holder.statusMain.text = if (item.isLive) "LIVE NOW" else "STARTS IN 45 MIN"
        holder.statusSub.text = if (item.isLive) "1.2M WATCHING" else "19:30 GMT"

        if (item.isLive) {
            holder.liveBadge.text = "LIVE"
            holder.liveBadge.setBackgroundResource(R.drawable.bg_badge_live_red)
            holder.btnAction.text = "WATCH NOW"
            holder.btnAction.setBackgroundResource(R.drawable.bg_button_watch_now)
            holder.btnAction.setTextColor(Color.BLACK)
        } else {
            holder.liveBadge.text = "UPCOMING"
            holder.liveBadge.setBackgroundResource(R.drawable.bg_badge_upcoming)
            holder.btnAction.text = "SET REMINDER"
            holder.btnAction.setBackgroundResource(R.drawable.bg_button_details)
            holder.btnAction.setTextColor(Color.WHITE)
        }

        holder.container.setOnClickListener {
            val context = it.context
            when (context) {
                is TournamentActivity -> {
                    val intent = Intent(context, EventActivity::class.java).apply {
                        putExtra("TOURNAMENT_ID", item.eventId) // This might be tournamentId in HomeMatch
                        putExtra("TOURNAMENT_NAME", item.tournament)
                        putExtra("TOURNAMENT_THUMB_URL", item.imageUrl)
                        putExtra("IS_HIGHLIGHTS_MODE", isHighlightsMode)
                    }
                    context.startActivity(intent)
                }
                is EventActivity -> {
                    val intent = Intent(context, LinksActivity::class.java).apply {
                        putExtra("MATCH_TITLE", item.title)
                        putExtra("TOURNAMENT", item.tournament)
                        putExtra("EVENT_ID", item.eventId ?: -1)
                        putExtra("EVENT_THUMB_URL", item.eventThumbUrl)
                        putExtra("IS_HIGHLIGHTS_MODE", isHighlightsMode)
                    }
                    context.startActivity(intent)
                }
            }
        }
        
        holder.btnAction.setOnClickListener {
            holder.container.performClick()
        }
    }

    override fun getItemCount() = items.size
}
