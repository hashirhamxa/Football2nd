package livefootball.footballstreamning.fifaworldcup.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.database.MatchEntity
import livefootball.footballstreamning.fifaworldcup.models.Inning
import livefootball.footballstreamning.fifaworldcup.utilities.Utils

class CricketScoreAdapter(
    private val items: List<MatchEntity>,
    private val onMatchClick: (MatchEntity) -> Unit
) : RecyclerView.Adapter<CricketScoreAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val statusBadge: View = view.findViewById(R.id.text_match_status)
        val statusBadgeText: TextView = view.findViewById(R.id.text_status_badge_text)
        val team1: TextView = view.findViewById(R.id.text_home_name)
        val team1Score: TextView = view.findViewById(R.id.text_home_score)
        val team2: TextView = view.findViewById(R.id.text_away_name)
        val team2Score: TextView = view.findViewById(R.id.text_away_score)
        val frameTeam1: View = view.findViewById(R.id.frame_home)
        val frameTeam2: View = view.findViewById(R.id.frame_away)
        val status: TextView = view.findViewById(R.id.text_match_status)
        val venue: TextView = view.findViewById(R.id.text_venue)
        val lastUpdated: TextView = view.findViewById(R.id.text_last_updated)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_score, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.statusBadgeText.text = item.status
        holder.team1.text = item.team1
        holder.team2.text = item.team2
        holder.status.text = item.status

        // Hide team images for cricket as data is not available
        holder.frameTeam1.visibility = View.GONE
        holder.frameTeam2.visibility = View.GONE

        holder.venue.text = "📍 ${item.venue ?: "Unknown Venue"}"
        
        if (item.status?.contains("Live", ignoreCase = true) == true) {
            holder.statusBadge.findViewById<View>(R.id.dot_live_score)?.let {
                it.visibility = View.VISIBLE
                Utils.animateLiveDot(it)
            }
        } else {
            holder.statusBadge.findViewById<View>(R.id.dot_live_score)?.let {
                it.visibility = View.GONE
                it.clearAnimation()
            }
        }

        val timeAgo = DateUtils.getRelativeTimeSpanString(item.lastUpdated, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
        holder.lastUpdated.text = "Updated: $timeAgo"

        // Handle Score Parsing
        if (!item.scoreJson.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<Inning>>() {}.type
                val scores: List<Inning> = Gson().fromJson(item.scoreJson, type)
                
                // Intelligently map scores to teams
                val t1Score = scores.find { it.inning?.contains(item.team1 ?: "", ignoreCase = true) == true }
                val t2Score = scores.find { it.inning?.contains(item.team2 ?: "", ignoreCase = true) == true }

                holder.team1Score.text = formatScore(t1Score)
                holder.team2Score.text = formatScore(t2Score)
            } catch (e: Exception) {
                holder.team1Score.text = "N/A"
                holder.team2Score.text = "N/A"
            }
        } else {
            holder.team1Score.text = "N/A"
            holder.team2Score.text = "N/A"
        }

        holder.itemView.setOnClickListener { onMatchClick(item) }
    }

    private fun formatScore(inning: Inning?): String {
        if (inning == null) return "Yet to bat"
        val runs = inning.runs ?: 0
        val wickets = inning.wickets ?: 0
        val overs = inning.overs ?: 0.0
        return "$runs/$wickets ($overs)"
    }

    override fun getItemCount() = items.size
}
