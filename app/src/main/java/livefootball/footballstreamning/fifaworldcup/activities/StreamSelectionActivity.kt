package livefootball.footballstreamning.fifaworldcup.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.adapters.Channel
import livefootball.footballstreamning.fifaworldcup.adapters.StreamChannelAdapter
import livefootball.footballstreamning.fifaworldcup.ads.AdsHelper
import livefootball.footballstreamning.fifaworldcup.database.LinkEntity
import livefootball.footballstreamning.fifaworldcup.network.AppRepository
import livefootball.footballstreamning.fifaworldcup.newplayer.NewPlayerActivity
import livefootball.footballstreamning.fifaworldcup.utilities.TimeUtils
import livefootball.footballstreamning.fifaworldcup.utilities.Utils
import livefootball.footballstreamning.fifaworldcup.viewmodels.StreamLinksViewModel
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class StreamSelectionActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: AppRepository

    private val viewModel: StreamLinksViewModel by viewModels()

    private var isHighlightsMode = false
    private var showAdInExo = false
    private var bannerAdKey = ""
    private var interstitialAdKey = ""
    private var rewardedAdKey = ""

    private val handler = Handler(Looper.getMainLooper())
    private var startTime: String? = null
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateHeroTime()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_links)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ad_container_links)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBars.bottom)
            insets
        }

        val eventId = intent.getIntExtra("EVENT_ID", -1)
        val matchTitle = intent.getStringExtra("MATCH_TITLE") ?: "Match Details"
        val tournament = intent.getStringExtra("TOURNAMENT") ?: "Tournament"
        isHighlightsMode = intent.getBooleanExtra("IS_HIGHLIGHTS_MODE", false)
        val eventThumbUrl = intent.getStringExtra("EVENT_THUMB_URL")
        startTime = intent.getStringExtra("START_TIME")

        Log.e("leolog eventThumbUrl", "eventThumbUrl " + eventThumbUrl)

        findViewById<TextView>(R.id.text_match_title_top)?.text = matchTitle
        findViewById<TextView>(R.id.text_tournament_top)?.text = tournament
        findViewById<TextView>(R.id.text_match_title_hero)?.text = matchTitle
        findViewById<TextView>(R.id.badge_tournament_hero)?.text = tournament

        // Set hero image
        val imgHero = findViewById<ImageView>(R.id.img_hero)
        val liveDotHero = findViewById<View>(R.id.dot_live_hero)
        if (!isHighlightsMode) {
            liveDotHero?.let { Utils.animateLiveDot(it) }

            // Set random watching count only for live matches
            val randomWatching = Random.nextInt(1000, 10001)
            findViewById<TextView>(R.id.text_watching)?.text = "$randomWatching WATCHING"
        } else {
            findViewById<TextView>(R.id.text_watching)?.visibility = View.GONE
            findViewById<View>(R.id.badge_live_hero)?.visibility = View.GONE
            findViewById<View>(R.id.text_live_toolbar)?.visibility = View.GONE
        }

        Glide.with(this).load(eventThumbUrl)
            .placeholder(R.drawable.bg_section_indicator)
            .into(imgHero)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            AdsHelper.getInstance(this@StreamSelectionActivity)
                .showAd_Mob_X_Inter_With_Time(this@StreamSelectionActivity)
            finish()
        }

        val rvChannels = findViewById<RecyclerView>(R.id.rv_channels)

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_links)
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.surface)
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary)
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh(eventId, isHighlightsMode)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    if (isHighlightsMode) {
                        combine(
                            viewModel.highlights,
                            viewModel.streaming
                        ) { highlights, streaming ->
                            Pair(highlights, streaming)
                        }.collectLatest { (highlights, streaming) ->

                            val channels = highlights.map { highlight ->
                                Channel(
                                    name = highlight.linkName ?: "Highlight",
                                    quality = "HD",
                                    isHighlight = true,
                                    thumbnailLink = highlight.linkImage,
                                    link = LinkEntity(
                                        id = highlight.id,
                                        linkName = highlight.linkName,
                                        linkUrl = highlight.linkUrl,
                                        linkType = "Highlight",
                                        mpdLink = null,
                                        mpdKey = null,
                                        linkImage = highlight.linkImage,
                                        isVisible = highlight.isVisible,
                                        priority = 0,
                                        excludedAppPackageNames = null,
                                        refererHeader = null,
                                        originHeader = null,
                                        userAgentHeader = null,
                                        eventId = eventId
                                    )
                                )
                            }.toMutableList()

                            if (streaming != null && !streaming.newAppOutsideUrl.isNullOrEmpty()) {
                                val promoChannel = Channel(
                                    name = streaming.outsideUrlTitle ?: "New App Available",
                                    quality = "",
                                    isPromotion = true,
                                    promotionUrl = streaming.newAppOutsideUrl,
                                    promotionTitle = streaming.outsideUrlTitle,
                                    promotionDescription = streaming.outsideUrlDescription,
                                    promotionImageUrl = streaming.outsideUrlImageUrl
                                )
                                val middleIndex =
                                    if (channels.isEmpty()) 0 else maxOf(1, channels.size / 2)
                                channels.add(middleIndex, promoChannel)
                            }
                            updateAdapter(rvChannels, channels, matchTitle)
                        }
                    } else {
                        combine(viewModel.links, viewModel.streaming) { links, streaming ->
                            Pair(links, streaming)
                        }.collectLatest { (rawLinks, streaming) ->
                            val hasValidPriority =
                                rawLinks.any { it.priority != null && it.priority > 0 }
                            val sortedLinks = if (hasValidPriority) {
                                rawLinks.sortedWith { a, b ->
                                    val p1 = a.priority
                                    val p2 = b.priority
                                    val v1 = if (p1 != null && p1 > 0) p1 else Int.MAX_VALUE
                                    val v2 = if (p2 != null && p2 > 0) p2 else Int.MAX_VALUE
                                    v1.compareTo(v2)
                                }
                            } else {
                                rawLinks
                            }

                            val channels = sortedLinks.map { link ->
                                Channel(
                                    name = link.linkName ?: "Link",
                                    quality = link.linkType ?: "HD",
                                    link = link,
                                    isHighlight = false,
                                    thumbnailLink = null
                                )
                            }.toMutableList()

                            if (streaming != null && !streaming.newAppOutsideUrl.isNullOrEmpty()) {
                                val promoChannel = Channel(
                                    name = streaming.outsideUrlTitle ?: "New App Available",
                                    quality = "",
                                    isPromotion = true,
                                    promotionUrl = streaming.newAppOutsideUrl,
                                    promotionTitle = streaming.outsideUrlTitle,
                                    promotionDescription = streaming.outsideUrlDescription,
                                    promotionImageUrl = streaming.outsideUrlImageUrl
                                )
                                val middleIndex =
                                    if (channels.isEmpty()) 0 else maxOf(1, channels.size / 2)
                                channels.add(middleIndex, promoChannel)
                            }
                            updateAdapter(rvChannels, channels, matchTitle)
                        }
                    }
                }

                launch {
                    viewModel.isRefreshing.collectLatest { isRefreshing ->
                        swipeRefresh.isRefreshing = isRefreshing
                    }
                }
            }
        }

        if (eventId != -1) {
            if (isHighlightsMode) {
                viewModel.loadHighlights(eventId)
            } else {
                viewModel.loadLinks(eventId)
                handler.post(updateRunnable)
            }
        }
        updateHeroTime()
        loadBannerAd()
        loadAdSettings()
    }

    private fun loadAdSettings() {
        lifecycleScope.launch {
            repository.getApp()?.let { app ->
                repository.getStreamingData(app.id).firstOrNull()?.let { data ->
                    showAdInExo = data.streaming.bannerAds == true
                }
            }
            val ads = repository.getAllAds()
            ads.find { it.adPlacement.equals("Banner", ignoreCase = true) }?.let { ad ->
                bannerAdKey = ad.adUnitId ?: ""
            }
            ads.find { it.adPlacement.equals("Interstitial", ignoreCase = true) }?.let { ad ->
                interstitialAdKey = ad.adUnitId ?: ""
                if (ad.isActive == true && !interstitialAdKey.isEmpty()) {
                    AdsHelper.getInstance(this@StreamSelectionActivity)
                        .preloadAdADMOB_X_Inter(this@StreamSelectionActivity, interstitialAdKey)
                }
            }
            ads.find { it.adPlacement.equals("Rewarded", ignoreCase = true) }?.let { ad ->
                rewardedAdKey = ad.adUnitId ?: ""
                if (ad.isActive == true && !rewardedAdKey.isEmpty()) {
                    AdsHelper.getInstance(this@StreamSelectionActivity)
                        .preloadRewardedAd(this@StreamSelectionActivity, rewardedAdKey)
                }
            }
        }
    }

    private fun loadBannerAd() {
        lifecycleScope.launch {
            repository.getAllAds().find { it.adPlacement.equals("Banner", ignoreCase = true) }
                ?.let { ad ->
                    if (ad.isActive == true && !ad.adUnitId.isNullOrEmpty()) {
                        val adContainer = findViewById<RelativeLayout>(R.id.ad_container_links)
                        AdsHelper.getInstance(this@StreamSelectionActivity)
                            .loadAdaptiveADMOB_X_Banner(
                                this@StreamSelectionActivity,
                                adContainer,
                                ad.adUnitId
                            )
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }

    private fun updateHeroTime() {
        val countdownText = findViewById<TextView>(R.id.text_countdown_hero)
        val startingInText = findViewById<TextView>(R.id.text_starting_in_hero)
        val watchingText = findViewById<TextView>(R.id.text_watching)
        val liveBadgeHero = findViewById<View>(R.id.badge_live_hero)
        val liveToolbar = findViewById<View>(R.id.text_live_toolbar)

        if (isHighlightsMode) {
            watchingText?.visibility = View.GONE
            liveBadgeHero?.visibility = View.GONE
            liveToolbar?.visibility = View.GONE
            startingInText?.visibility = View.GONE
            countdownText?.visibility = View.GONE
            return
        }

        val startDate = TimeUtils.parseUtcToLocal(startTime)

        if (startDate != null && !TimeUtils.isEventLive(startDate)) {
            // Upcoming
            watchingText?.visibility = View.GONE
            liveBadgeHero?.visibility = View.GONE

            startingInText?.visibility = View.VISIBLE
            countdownText?.let {
                it.text = TimeUtils.getCountdownString(startDate)
                it.visibility = View.VISIBLE
            }
        } else {
            // Live
            watchingText?.visibility = View.VISIBLE
            liveBadgeHero?.visibility = View.VISIBLE

            startingInText?.visibility = View.GONE
            countdownText?.visibility = View.GONE
        }
    }

    private fun updateAdapter(rv: RecyclerView, channels: List<Channel>, matchTitle: String) {
        rv.adapter = StreamChannelAdapter(channels) { channel ->
            AdsHelper.getInstance(this@StreamSelectionActivity)
                .showAd_Mob_X_Inter_With_Time(this@StreamSelectionActivity)
            channel.link?.let { link ->
                val intent =
                    Intent(this@StreamSelectionActivity, NewPlayerActivity::class.java).apply {
                        putExtra("isVideoLoop", false)
                        putExtra("videoTittle", matchTitle)
                        putExtra(
                            "videoLink",
                            if (!link.mpdLink.isNullOrEmpty()) null else link.linkUrl
                        )
                        putExtra("mpdLink", link.mpdLink)
                        putExtra("mpdKey", link.mpdKey)
                        putExtra("refererHeader", link.refererHeader)
                        putExtra("originHeader", link.originHeader)
                        putExtra("userAgentHeader", link.userAgentHeader)



                        putExtra("unityAds", false)
                        putExtra("showAdInExo", showAdInExo)
                        putExtra("bannerAdKey", bannerAdKey)
                        putExtra("interstitialAdKey", interstitialAdKey)
                        putExtra("rewardedAdKey", rewardedAdKey)

                        setAction(Intent.ACTION_SEND)
                        setType("text/plain")
                        putExtra(Intent.EXTRA_TEXT, link.linkUrl)
                    }
                startActivity(intent)
            }
        }
    }
}
