package livefootball.footballstreamning.fifaworldcup.activities

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageButton
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import livefootball.footballstreamning.fifaworldcup.viewmodels.TournamentViewModel
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.adapters.MatchListAdapter
import livefootball.footballstreamning.fifaworldcup.ads.AdsHelper
import livefootball.footballstreamning.fifaworldcup.database.EventEntity
import livefootball.footballstreamning.fifaworldcup.database.TournamentEntity
import livefootball.footballstreamning.fifaworldcup.network.AppRepository
import livefootball.footballstreamning.fifaworldcup.viewmodels.HomeDisplayItem
import javax.inject.Inject

@AndroidEntryPoint
class TournamentListActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: AppRepository

    private val viewModel: TournamentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ad_container_tournament)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBars.bottom)
            insets
        }

        val category = intent.getStringExtra("CATEGORY") ?: "CRICKET"
        val isHighlights = intent.getBooleanExtra("IS_HIGHLIGHTS_MODE", false)

        findViewById<TextView>(R.id.text_category_title).text = category
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { 
            AdsHelper.getInstance(this@TournamentListActivity).showAd_Mob_X_Inter_With_Time(this@TournamentListActivity)
            finish() 
        }

        val rvTournaments = findViewById<RecyclerView>(R.id.rv_tournaments)
        rvTournaments.layoutManager = LinearLayoutManager(this)

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_tournaments)
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.surface)
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary)
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh(category, isHighlights)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.items.collectLatest { items ->
                        rvTournaments.adapter = MatchListAdapter(items, isHighlights) { handleItemClick(it) }
                    }
                }

                launch {
                    viewModel.isRefreshing.collectLatest { isRefreshing ->
                        swipeRefresh.isRefreshing = isRefreshing
                    }
                }
            }
        }

        viewModel.loadTournamentsBySportType(category, isHighlights)
        loadAds()
    }

    private fun loadAds() {
        lifecycleScope.launch {
            val ads = repository.getAllAds()

            // 1. Banner Ad
            ads.find { it.adPlacement.equals("Banner", ignoreCase = true) }?.let { ad ->
                if (ad.isActive == true && !ad.adUnitId.isNullOrEmpty()) {
                    val adContainer = findViewById<RelativeLayout>(R.id.ad_container_tournament)
                    AdsHelper.getInstance(this@TournamentListActivity).loadAdaptiveADMOB_X_Banner(this@TournamentListActivity, adContainer, ad.adUnitId)
                }
            }

            // 2. Preload Interstitial
            ads.find { it.adPlacement.equals("Interstitial", ignoreCase = true) }?.let { ad ->
                if (ad.isActive == true && !ad.adUnitId.isNullOrEmpty()) {
                    AdsHelper.getInstance(this@TournamentListActivity).preloadAdADMOB_X_Inter(this@TournamentListActivity, ad.adUnitId)
                }
            }

            // 3. Preload Rewarded
            ads.find { it.adPlacement.equals("Rewarded", ignoreCase = true) }?.let { ad ->
                if (ad.isActive == true && !ad.adUnitId.isNullOrEmpty()) {
                    AdsHelper.getInstance(this@TournamentListActivity).preloadRewardedAd(this@TournamentListActivity, ad.adUnitId)
                }
            }
        }
    }

    override fun onBackPressed() {
        AdsHelper.getInstance(this@TournamentListActivity).showAd_Mob_X_Inter_With_Time(this@TournamentListActivity)
        super.onBackPressed()
    }

    private fun handleItemClick(item: HomeDisplayItem) {
        AdsHelper.getInstance(this@TournamentListActivity).showAd_Mob_X_Inter_With_Time(this@TournamentListActivity)
        val isHighlights = intent.getBooleanExtra("IS_HIGHLIGHTS_MODE", false)
        when (val original = item.originalObject) {
            is EventEntity -> {
                val intent = Intent(this, StreamSelectionActivity::class.java).apply {
                    putExtra("MATCH_TITLE", original.eventName)
                    putExtra("TOURNAMENT", item.subtitle)
                    putExtra("EVENT_ID", original.id)
                    putExtra("EVENT_THUMB_URL", original.eventThumbUrl)
                    putExtra("START_TIME", original.startTime)
                    putExtra("IS_HIGHLIGHTS_MODE", isHighlights)
                }
                startActivity(intent)
            }
            is TournamentEntity -> {
                val intent = Intent(this, EventsListActivity::class.java).apply {
                    putExtra("TOURNAMENT_ID", original.id)
                    putExtra("TOURNAMENT_NAME", original.name)
                    putExtra("TOURNAMENT_THUMB_URL", original.thumbUrl)
                    putExtra("IS_HIGHLIGHTS_MODE", isHighlights)
                }
                startActivity(intent)
            }
        }
    }
}
