package livefootball.footballstreamning.fifaworldcup.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import livefootball.footballstreamning.fifaworldcup.database.EventEntity
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.activities.LinksActivity
import livefootball.footballstreamning.fifaworldcup.ads.AdsHelper
import livefootball.footballstreamning.fifaworldcup.utilities.Utils

import android.os.Handler
import android.os.Looper
import livefootball.footballstreamning.fifaworldcup.utilities.TimeUtils

class EventAdapter(
    private val items: List<EventEntity>,
    private val tournamentName: String,
    private val isHighlightsMode: Boolean = false,
    private val tournamentThumbUrl: String? = null
) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            notifyDataSetChanged()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (!isHighlightsMode) handler.post(updateRunnable)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        handler.removeCallbacks(updateRunnable)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: View = view
        val banner: ImageView = view.findViewById(R.id.img_banner)
        val tournamentName: TextView = view.findViewById(R.id.text_tournament_name)
        val liveBadge: View = view.findViewById(R.id.text_live_badge)
        val liveDot: View? = view.findViewById(R.id.dot_live_tournament)
        val badgeText: TextView = view.findViewById(R.id.text_live_badge_text)
        val matchTitle: TextView = view.findViewById(R.id.text_match_title)
        val statusMain: TextView = view.findViewById(R.id.text_status_main)
        val statusSub: TextView = view.findViewById(R.id.text_status_sub)
        val btnAction: AppCompatButton = view.findViewById(R.id.btn_action)
        val startingInText: TextView = view.findViewById(R.id.text_starting_in)
        val countdownText: TextView = view.findViewById(R.id.text_countdown)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tournament_large, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        val imageUrl = item.eventThumbUrl ?: tournamentThumbUrl
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.bg_section_indicator)
            .into(holder.banner)

        holder.tournamentName.text = tournamentName
        holder.matchTitle.text = item.eventName
        
        if (!isHighlightsMode) {
            val startDate = TimeUtils.parseUtcToLocal(item.startTime)
            if (startDate != null && !TimeUtils.isEventLive(startDate)) {
                // Event is Upcoming
                holder.statusMain.visibility = View.GONE
                holder.liveBadge.visibility = View.GONE
                
                holder.btnAction.text = "DETAILS"
                holder.btnAction.setBackgroundResource(R.drawable.bg_button_details)
                holder.btnAction.setTextColor(Color.WHITE)
                
                holder.startingInText.visibility = View.VISIBLE
                holder.countdownText.text = TimeUtils.getCountdownString(startDate)
                holder.countdownText.visibility = View.VISIBLE
            } else {
                // Event is Live
                holder.statusMain.visibility = View.VISIBLE
                holder.statusMain.text = "LIVE NOW"
                holder.liveBadge.visibility = View.VISIBLE
                
                holder.badgeText.text = "LIVE"
                holder.liveBadge.setBackgroundResource(R.drawable.bg_badge_live_red)
                holder.liveDot?.let { Utils.animateLiveDot(it) }

                holder.btnAction.text = "WATCH NOW"
                holder.btnAction.setBackgroundResource(R.drawable.bg_button_watch_now)
                holder.btnAction.setTextColor(Color.BLACK)
                
                holder.startingInText.visibility = View.GONE
                holder.countdownText.visibility = View.GONE
            }
        } else {
            // Highlights mode: Simple static display
            holder.statusMain.text = "HIGHLIGHT"
            holder.liveBadge.visibility = View.GONE
            holder.btnAction.text = "WATCH"
            holder.btnAction.setBackgroundResource(R.drawable.bg_button_watch_now)
            holder.btnAction.setTextColor(Color.BLACK)
            holder.countdownText.visibility = View.GONE
        }

        holder.statusSub.text = item.description ?: ""

        holder.container.setOnClickListener {
            AdsHelper.getInstance(it.context).showAd_Mob_X_Inter_With_Time((it.context as Activity))
            val intent = Intent(it.context, LinksActivity::class.java).apply {
                putExtra("MATCH_TITLE", item.eventName)
                putExtra("TOURNAMENT", tournamentName)
                putExtra("EVENT_ID", item.id)
                putExtra("EVENT_THUMB_URL", item.eventThumbUrl ?: tournamentThumbUrl)
                putExtra("IS_HIGHLIGHTS_MODE", isHighlightsMode)
            }
            it.context.startActivity(intent)
        }
        
        holder.btnAction.setOnClickListener {
            holder.container.performClick()
        }
    }

    override fun getItemCount() = items.size
}
