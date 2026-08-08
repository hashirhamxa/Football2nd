package livefootball.footballstreamning.fifaworldcup.adapters

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.utilities.TimeUtils
import livefootball.footballstreamning.fifaworldcup.utilities.Utils
import livefootball.footballstreamning.fifaworldcup.viewmodels.HomeDisplayItem

class MatchListAdapter(
    private val items: List<HomeDisplayItem>,
    private val isHighlightsMode: Boolean = false,
    private val onItemClick: (HomeDisplayItem) -> Unit
) : RecyclerView.Adapter<MatchListAdapter.ViewHolder>() {

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            // Using a payload to update ONLY the countdown text
            // This prevents the entire view from being rebound/reset every second,
            // which was likely causing the "double click" issue.
            notifyItemRangeChanged(0, itemCount, "PAYLOAD_TICK")
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
        val startingInText: TextView = view.findViewById(R.id.text_starting_in)
        val countdownText: TextView = view.findViewById(R.id.text_countdown)

        // Teams UI
        val layoutTeams: View = view.findViewById(R.id.layout_teams)
        val team1Name: TextView = view.findViewById(R.id.text_team1_name)
        val team1Image: ImageView = view.findViewById(R.id.img_team1)
        val team1Code: TextView = view.findViewById(R.id.text_team1_code)
        val team2Name: TextView = view.findViewById(R.id.text_team2_name)
        val team2Image: ImageView = view.findViewById(R.id.img_team2)
        val team2Code: TextView = view.findViewById(R.id.text_team2_code)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tournament_large, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.contains("PAYLOAD_TICK")) {
            val item = items[position]
            updateCountdownOnly(holder, item)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.bg_section_indicator)
            .into(holder.banner)

        holder.tournamentName.text = item.subtitle ?: ""
        
        // Handle Teams vs Match Title
        val hasTeams = !item.team1Name.isNullOrEmpty() || !item.team2Name.isNullOrEmpty()
        if (hasTeams) {
            holder.layoutTeams.visibility = View.VISIBLE
            holder.matchTitle.visibility = View.GONE
            
            holder.team1Name.text = item.team1Name ?: ""
            holder.team2Name.text = item.team2Name ?: ""
            holder.team1Code.text = item.team1Name?.take(3)?.uppercase() ?: ""
            holder.team2Code.text = item.team2Name?.take(3)?.uppercase() ?: ""

            loadLogo(item.team1Image, holder.team1Image, holder.team1Code)
            loadLogo(item.team2Image, holder.team2Image, holder.team2Code)
        } else {
            holder.layoutTeams.visibility = View.GONE
            holder.matchTitle.visibility = View.VISIBLE
            holder.matchTitle.text = item.title
        }
        
        if (!isHighlightsMode) {
            val startDate = TimeUtils.parseUtcToLocal(item.startTime)
            if (startDate != null && !TimeUtils.isEventLive(startDate)) {
                // Event is Upcoming
                holder.statusMain.visibility = View.GONE
                holder.liveBadge.visibility = View.GONE
                
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

                holder.startingInText.visibility = View.GONE
                holder.countdownText.visibility = View.GONE
            }
        } else {
            // Highlights mode
            holder.statusMain.text = "HIGHLIGHT"
            holder.liveBadge.visibility = View.GONE
            holder.countdownText.visibility = View.GONE
            holder.startingInText.visibility = View.GONE
        }

        holder.statusSub.text = item.status ?: ""

        holder.container.setOnClickListener {
            onItemClick(item)
        }
    }

    private fun updateCountdownOnly(holder: ViewHolder, item: HomeDisplayItem) {
        if (!isHighlightsMode) {
            val startDate = TimeUtils.parseUtcToLocal(item.startTime)
            if (startDate != null && !TimeUtils.isEventLive(startDate)) {
                holder.countdownText.text = TimeUtils.getCountdownString(startDate)
            }
        }
    }

    private fun loadLogo(url: String?, imageView: ImageView, codeView: TextView) {
        if (url.isNullOrEmpty() || url.contains("placeholder") || url.contains("default")) {
            imageView.visibility = View.GONE
            codeView.visibility = View.VISIBLE
        } else {
            imageView.visibility = View.VISIBLE
            codeView.visibility = View.GONE
            Glide.with(imageView.context)
                .load(url)
                .centerInside()
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        imageView.post {
                            imageView.visibility = View.GONE
                            codeView.visibility = View.VISIBLE
                        }
                        return false
                    }
                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        return false
                    }
                })
                .into(imageView)
        }
    }

    override fun getItemCount() = items.size
}
