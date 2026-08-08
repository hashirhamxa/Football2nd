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
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import livefootball.footballstreamning.fifaworldcup.utilities.Utils
import livefootball.footballstreamning.fifaworldcup.utilities.TimeUtils

class SportCategoryAdapter(
    private val items: List<HomeDisplayItem>,
    private val isVertical: Boolean = false,
    private val isHighlightsMode: Boolean = false,
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
        if (!isHighlightsMode) handler.post(updateRunnable)
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
            // Use item_match for normal matches (Live Cricket/Football)
            val view = inflater.inflate(R.layout.item_home_cricket, parent, false)
            // Adjust width for vertical layout or horizontal carousels
            val params = view.layoutParams ?: RecyclerView.LayoutParams(
                if (isVertical) ViewGroup.LayoutParams.MATCH_PARENT else (parent.width * 0.85).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            
            if (isVertical) {
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                if (params is ViewGroup.MarginLayoutParams) {
                    params.bottomMargin = view.context.resources.getDimensionPixelSize(R.dimen.spacing_medium)
                }
            } else {
                // For horizontal, give it a fixed-ish width so multiple items are visible
                params.width = view.context.resources.getDimensionPixelSize(R.dimen.nav_indicator_width) * 4 // approx 256dp
                if (params is ViewGroup.MarginLayoutParams) {
                    params.marginEnd = view.context.resources.getDimensionPixelSize(R.dimen.spacing_medium)
                }
            }
            view.layoutParams = params
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
        // Shared
        private val matchBg: ImageView? = view.findViewById(R.id.img_match_bg)
        private val layoutTeams: View = view.findViewById(R.id.layout_teams)
        private val matchStatus: TextView = view.findViewById(R.id.text_match_status)
        private val layoutStatusBadge: View? = view.findViewById(R.id.layout_status_badge)
        private val statusBadgeText: TextView? = view.findViewById(R.id.text_status_badge_text)
        private val startingInText: TextView? = view.findViewById(R.id.text_starting_in)
        private val countdownText: TextView? = view.findViewById(R.id.text_countdown)

        // Cricket specific
        private val seriesName: TextView? = view.findViewById(R.id.text_series_name)
        private val team1Name: TextView? = view.findViewById(R.id.text_team1_name)
        private val team1Image: ImageView? = view.findViewById(R.id.img_team1)
        private val team1Code: TextView? = view.findViewById(R.id.text_team1_code)
        private val team2Name: TextView? = view.findViewById(R.id.text_team2_name)
        private val team2Image: ImageView? = view.findViewById(R.id.img_team2)
        private val team2Code: TextView? = view.findViewById(R.id.text_team2_code)

        // Football specific
        private val leagueName: TextView? = view.findViewById(R.id.text_league_name)
        private val homeName: TextView? = view.findViewById(R.id.text_home_name)
        private val homeLogo: ImageView? = view.findViewById(R.id.img_home_logo)
        private val homeCode: TextView? = view.findViewById(R.id.text_home_code)
        private val awayName: TextView? = view.findViewById(R.id.text_away_name)
        private val awayLogo: ImageView? = view.findViewById(R.id.img_away_logo)
        private val awayCode: TextView? = view.findViewById(R.id.text_away_code)

        fun bind(item: HomeDisplayItem) {
            seriesName?.text = item.subtitle ?: item.title
            leagueName?.text = item.subtitle ?: item.title
            matchStatus.text = item.status

            val hasTeams = !item.team1Name.isNullOrEmpty() || !item.team2Name.isNullOrEmpty()

            if (hasTeams) {
                layoutTeams.visibility = View.VISIBLE
                matchStatus.visibility = View.GONE

                // Cricket mapping
                team1Name?.text = item.team1Name ?: ""
                team2Name?.text = item.team2Name ?: ""
                team1Code?.text = item.team1Name?.take(3)?.uppercase()
                team2Code?.text = item.team2Name?.take(3)?.uppercase()

                // Football mapping
                homeName?.text = item.team1Name ?: ""
                awayName?.text = item.team2Name ?: ""
                homeCode?.text = item.team1Name?.take(3)?.uppercase()
                awayCode?.text = item.team2Name?.take(3)?.uppercase()

                val placeholder = R.drawable.bg_match_team_logo

                fun loadLogo(url: String?, imageView: ImageView?, codeView: TextView?) {
                    if (imageView == null) return
                    if (url.isNullOrEmpty() || url.contains("placeholder") || url.contains("default")) {
                        imageView.visibility = View.GONE
                        codeView?.visibility = View.VISIBLE
                    } else {
                        imageView.visibility = View.VISIBLE
                        codeView?.visibility = View.GONE
                        Glide.with(itemView.context)
                            .load(url)
                            .centerInside()
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                                    imageView.post {
                                        imageView.visibility = View.GONE
                                        codeView?.visibility = View.VISIBLE
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

                loadLogo(item.team1Image, team1Image, team1Code)
                loadLogo(item.team2Image, team2Image, team2Code)
                loadLogo(item.team1Image, homeLogo, homeCode)
                loadLogo(item.team2Image, awayLogo, awayCode)

            } else {
                layoutTeams.visibility = View.GONE
                matchStatus.visibility = View.VISIBLE
                matchStatus.text = item.title
            }

            // Background image
            matchBg?.let {
                Glide.with(itemView.context)
                    .load(item.imageUrl)
                    .placeholder(R.color.surface)
                    .into(it)
            }

            // Handle status badge and countdown based on mode
            if (isHighlightsMode) {
                // Highlights mode: hide all live/upcoming indicators
                layoutStatusBadge?.visibility = View.GONE
                statusBadgeText?.visibility = View.GONE
                startingInText?.visibility = View.GONE
                countdownText?.visibility = View.GONE

                val dotLive = itemView.findViewById<View>(R.id.dot_live_score) ?: itemView.findViewById<View>(R.id.dot_live)
                dotLive?.visibility = View.GONE
                dotLive?.clearAnimation()
            } else {
                // Live mode: determine live/upcoming based on start time
                val startDate = TimeUtils.parseUtcToLocal(item.startTime)
                val isEventLive = startDate == null || TimeUtils.isEventLive(startDate)

                val dotLive = itemView.findViewById<View>(R.id.dot_live_score) ?: itemView.findViewById<View>(R.id.dot_live)

                if (isEventLive) {
                    // Event is live
                    layoutStatusBadge?.visibility = View.VISIBLE
                    statusBadgeText?.text = "LIVE"
                    statusBadgeText?.visibility = View.VISIBLE
                    startingInText?.visibility = View.GONE
                    countdownText?.visibility = View.GONE

                    dotLive?.let {
                        it.visibility = View.VISIBLE
                        Utils.animateLiveDot(it)
                    }
                } else {
                    // Event is upcoming
                    layoutStatusBadge?.visibility = View.GONE
                    statusBadgeText?.visibility = View.GONE
                    startingInText?.visibility = View.VISIBLE
                    countdownText?.let {
                        it.text = TimeUtils.getCountdownString(startDate)
                        it.visibility = View.VISIBLE
                    }

                    dotLive?.let {
                        it.visibility = View.GONE
                        it.clearAnimation()
                    }
                }
            }
        }
    }

    inner class TrendingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val banner: ImageView = view.findViewById(R.id.img_trending_banner)
        private val title: TextView = view.findViewById(R.id.text_trending_title)
        private val category: TextView = view.findViewById(R.id.text_category)
        private val description: TextView = view.findViewById(R.id.text_trending_desc)
        private val liveIndicatorLayout: View = view.findViewById(R.id.layout_status_badge)
        private val liveBadge: TextView = view.findViewById(R.id.badge_live_trending)
        private val liveDot: View = view.findViewById(R.id.dot_live_trending)
        private val startingInText: TextView? = view.findViewById(R.id.text_starting_in)
        private val countdownText: TextView? = view.findViewById(R.id.text_countdown)
        private val btnWatch: MaterialButton? = view.findViewById(R.id.btn_watch_now)
        private val btnDetails: View? = view.findViewById(R.id.btn_details)

        fun bind(item: HomeDisplayItem) {
            title.text = item.title
            category.text = item.subtitle
            description.text = item.status

            if (isHighlightsMode) {
                // Highlights mode: hide all live/upcoming indicators
                liveIndicatorLayout.visibility = View.GONE
                liveDot.clearAnimation()
                startingInText?.visibility = View.GONE
                countdownText?.visibility = View.GONE
                btnWatch?.text = "WATCH"
            } else {
                // Live mode: determine live based on start time
                val startDate = TimeUtils.parseUtcToLocal(item.startTime)
                val isEventLive = startDate == null || TimeUtils.isEventLive(startDate)

                if (isEventLive) {
                    // Event is live
                    liveIndicatorLayout.visibility = View.VISIBLE
                    liveDot.let { Utils.animateLiveDot(it) }
                    startingInText?.visibility = View.GONE
                    countdownText?.visibility = View.GONE
                    btnWatch?.text = "WATCH NOW"
                } else {
                    // Event is upcoming
                    liveIndicatorLayout.visibility = View.GONE
                    liveDot.clearAnimation()
                    startingInText?.visibility = View.VISIBLE
                    countdownText?.let {
                        it.text = TimeUtils.getCountdownString(startDate)
                        it.visibility = View.VISIBLE
                    }
                    btnWatch?.text = "WATCH"
                }
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
