package livefootball.footballstreamning.fifaworldcup.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.models.FootballMatchScoreModel

class FootballScoreListAdapter(
    private val matches: List<FootballMatchScoreModel>,
    private val onItemClick: (FootballMatchScoreModel) -> Unit
) : RecyclerView.Adapter<FootballScoreListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val status: TextView = view.findViewById(R.id.text_match_status)
        val homeName: TextView = view.findViewById(R.id.text_home_name)
        val awayName: TextView = view.findViewById(R.id.text_away_name)
        val homeScore: TextView = view.findViewById(R.id.text_home_score)
        val awayScore: TextView = view.findViewById(R.id.text_away_score)
        val homeLogo: ImageView = view.findViewById(R.id.img_home_logo)
        val awayLogo: ImageView = view.findViewById(R.id.img_away_logo)
        val matchTime: TextView = view.findViewById(R.id.text_match_time)
        val liveIndicator: View = view.findViewById(R.id.layout_live_indicator)
        val liveDot: View = view.findViewById(R.id.dot_live_score)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_score, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val match = matches[position]
        
        holder.status.text = match.matchStatus
        holder.homeName.text = match.homeTeamName
        holder.awayName.text = match.awayTeamName
        holder.homeScore.text = match.homeTeamScore ?: "0"
        holder.awayScore.text = match.awayTeamScore ?: "0"
        holder.matchTime.text = "${match.matchDate} ${match.matchTime}"

        if (match.isLive == "1") {
            holder.liveIndicator.visibility = View.VISIBLE
            val blinkAnim = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.blink)
            holder.liveDot.startAnimation(blinkAnim)
        } else {
            holder.liveIndicator.visibility = View.GONE
            holder.liveDot.clearAnimation()
        }

        // Show logos for football
        holder.homeLogo.visibility = View.VISIBLE
        holder.awayLogo.visibility = View.VISIBLE

        Glide.with(holder.itemView.context)
            .load(match.homeTeamBadge)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.homeLogo)

        Glide.with(holder.itemView.context)
            .load(match.awayTeamBadge)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.awayLogo)

        holder.itemView.setOnClickListener { onItemClick(match) }
    }

    override fun getItemCount() = matches.size
}
