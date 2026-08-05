package livefootball.footballstreamning.fifaworldcup.activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.adapters.EventAdapter
import livefootball.footballstreamning.fifaworldcup.ads.AdsHelper
import livefootball.footballstreamning.fifaworldcup.network.AppRepository
import livefootball.footballstreamning.fifaworldcup.viewmodels.EventViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EventActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: AppRepository

    private val viewModel: EventViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament) // Reusing the same list layout

        val tournamentId = intent.getIntExtra("TOURNAMENT_ID", -1)
        val tournamentName = intent.getStringExtra("TOURNAMENT_NAME") ?: "Tournament"
        val tournamentThumbUrl = intent.getStringExtra("TOURNAMENT_THUMB_URL")
        val isHighlightsMode = intent.getBooleanExtra("IS_HIGHLIGHTS_MODE", false)

        findViewById<TextView>(R.id.text_category_title).text = tournamentName
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { 
            AdsHelper.getInstance(this@EventActivity).showAd_Mob_X_Inter_With_Time(this@EventActivity)
            finish() 
        }

        val rvEvents = findViewById<RecyclerView>(R.id.rv_tournaments)

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_tournaments)
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh(tournamentId, isHighlightsMode)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    if (isHighlightsMode) {
                        viewModel.highlightEvents.collectLatest { events ->
                            rvEvents.adapter = EventAdapter(events, tournamentName, true, tournamentThumbUrl)
                        }
                    } else {
                        viewModel.events.collectLatest { events ->
                            rvEvents.adapter =
                                EventAdapter(events, tournamentName, false, tournamentThumbUrl)
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

        if (tournamentId != -1) {
            if (isHighlightsMode) {
                viewModel.loadHighlightEvents(tournamentId)
            } else {
                viewModel.loadEvents(tournamentId)
            }
        }
        loadAds()
    }

    private fun loadAds() {
        lifecycleScope.launch {
            val ads = repository.getAllAds()

            // 1. Banner Ad
            ads.find { it.adPlacement.equals("Banner", ignoreCase = true) }?.let { ad ->
                if (ad.isActive == true && !ad.adUnitId.isNullOrEmpty()) {
                    val adContainer = findViewById<RelativeLayout>(R.id.ad_container_tournament)
                    AdsHelper.getInstance(this@EventActivity).loadAdaptiveADMOB_X_Banner(this@EventActivity, adContainer, ad.adUnitId)
                }
            }

            // 2. Preload Interstitial
            ads.find { it.adPlacement.equals("Interstitial", ignoreCase = true) }?.let { ad ->
                if (ad.isActive == true && !ad.adUnitId.isNullOrEmpty()) {
                    AdsHelper.getInstance(this@EventActivity).preloadAdADMOB_X_Inter(this@EventActivity, ad.adUnitId)
                }
            }

            // 3. Preload Rewarded
            ads.find { it.adPlacement.equals("Rewarded", ignoreCase = true) }?.let { ad ->
                if (ad.isActive == true && !ad.adUnitId.isNullOrEmpty()) {
                    AdsHelper.getInstance(this@EventActivity).preloadRewardedAd(this@EventActivity, ad.adUnitId)
                }
            }
        }
    }

    override fun onBackPressed() {
        AdsHelper.getInstance(this@EventActivity).showAd_Mob_X_Inter_With_Time(this@EventActivity)
        super.onBackPressed()
    }
}
