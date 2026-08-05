package livefootball.footballstreamning.fifaworldcup.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.viewmodels.HomeDisplayItem

import android.os.Handler
import android.os.Looper
import com.google.android.material.button.MaterialButton
import livefootball.footballstreamning.fifaworldcup.utilities.TimeUtils
import livefootball.footballstreamning.fifaworldcup.utilities.Utils

class CategoryAdapter(
    private val items: List<HomeDisplayItem>,
    private val isVertical: Boolean = false,
    private val onItemClick: (HomeDisplayItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            notifyDataSetChanged()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        handler.post(updateRunnable)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        handler.removeCallbacks(updateRunnable)
    }

    companion object {
        private const val TYPE_MATCH = 1
        private const val TYPE_TRENDING = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].isTrending) TYPE_TRENDING else TYPE_MATCH
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_TRENDING) {
            val view = inflater.inflate(R.layout.item_home_trending, parent, false)
            if (isVertical) {
                val params = view.layoutParams as RecyclerView.LayoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.bottomMargin = view.context.resources.getDimensionPixelSize(R.dimen.spacing_large)
                view.layoutParams = params
            }
            TrendingViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_home_match, parent, false)
            // Adjust width for vertical layout
            if (isVertical) {
                val params = view.layoutParams as RecyclerView.LayoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.bottomMargin = view.context.resources.getDimensionPixelSize(R.dimen.spacing_large)
                view.layoutParams = params
            }
            MatchViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is MatchViewHolder) {
            holder.bind(item)
        } else if (holder is TrendingViewHolder) {
            holder.bind(item)
        }
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size

    inner class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val banner: ImageView = view.findViewById(R.id.img_match_banner)
        private val title: TextView = view.findViewById(R.id.text_match_title)
        private val tournament: TextView = view.findViewById(R.id.badge_tournament)
        private val status: TextView = view.findViewById(R.id.text_match_status)
        private val liveBadge: View = view.findViewById(R.id.badge_live)
        private val liveDot: View? = view.findViewById(R.id.dot_live)
        private val badgeText: TextView = view.findViewById(R.id.badge_live_text)
        private val startingInText: TextView = view.findViewById(R.id.text_starting_in)
        private val countdownText: TextView = view.findViewById(R.id.text_countdown)

        fun bind(item: HomeDisplayItem) {
            title.text = item.title
            tournament.text = item.subtitle
            
            val startDate = TimeUtils.parseUtcToLocal(item.startTime)
            if (startDate != null && !TimeUtils.isEventLive(startDate)) {
                // Event is Upcoming
                liveBadge.visibility = View.GONE

                status.visibility = View.VISIBLE
                status.text = item.status

                startingInText.visibility = View.VISIBLE
                countdownText.text = TimeUtils.getCountdownString(startDate)
                countdownText.visibility = View.VISIBLE
            } else {
                // Event is Live
                liveBadge.visibility = if (item.isLive) View.VISIBLE else View.GONE
                status.visibility = View.VISIBLE
                status.text = item.status
                badgeText.text = "LIVE"
                badgeText.setBackgroundResource(android.R.color.transparent)
                liveBadge.setBackgroundResource(R.drawable.bg_badge_live)
                
                liveDot?.let { Utils.animateLiveDot(it) }
                
                startingInText.visibility = View.GONE
                countdownText.visibility = View.GONE
            }
            
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.bg_section_indicator)
                .into(banner)
        }
    }

    inner class TrendingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val banner: ImageView = view.findViewById(R.id.img_trending_banner)
        private val title: TextView = view.findViewById(R.id.text_trending_title)
        private val category: TextView = view.findViewById(R.id.text_category)
        private val description: TextView = view.findViewById(R.id.text_trending_desc)
        private val liveBadge: View? = view.findViewById(R.id.badge_live_trending)
        private val liveDot: View? = view.findViewById(R.id.dot_live_trending)
        private val btnWatch: MaterialButton? = view.findViewById(R.id.btn_watch_now)
        private val btnDetails: View? = view.findViewById(R.id.btn_details)

        fun bind(item: HomeDisplayItem) {
            title.text = item.title
            category.text = item.subtitle
            description.text = item.status
            
            if (item.isLive) {
                liveBadge?.visibility = View.VISIBLE
                liveDot?.let { Utils.animateLiveDot(it) }
                btnWatch?.text = "WATCH NOW"
            } else {
                liveBadge?.visibility = View.GONE
                liveDot?.clearAnimation()
                btnWatch?.text = "WATCH"
            }
            
            btnWatch?.setOnClickListener { onItemClick(item) }
            btnDetails?.setOnClickListener { onItemClick(item) }
            
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.bg_section_indicator)
                .into(banner)
        }
    }
}
