package livefootball.footballstreamning.fifaworldcup.activities

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import livefootball.footballstreamning.fifaworldcup.viewmodels.ScoreDetailViewModel
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.adapters.CricketInningScoreAdapter
import livefootball.footballstreamning.fifaworldcup.ads.AdsHelper
import livefootball.footballstreamning.fifaworldcup.database.MatchEntity
import livefootball.footballstreamning.fifaworldcup.models.FootballMatchScoreModel
import livefootball.footballstreamning.fifaworldcup.models.Inning
import livefootball.footballstreamning.fifaworldcup.network.AppRepository
import livefootball.footballstreamning.fifaworldcup.viewmodels.FootballScoresViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MatchScoreDetailActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: AppRepository

    private val viewModel: ScoreDetailViewModel by viewModels()

    private val footballViewModel: FootballScoresViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_detail)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBars.bottom)
            insets
        }
        val matchId = intent.getStringExtra("MATCH_ID") ?: ""
        val isFootball = intent.getBooleanExtra("IS_FOOTBALL", false)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            AdsHelper.getInstance(this@MatchScoreDetailActivity).showAd_Mob_X_Inter_With_Time(this@MatchScoreDetailActivity)
            finish() 
        }

        val rvScores = findViewById<RecyclerView>(R.id.recycler_scores)
        rvScores.layoutManager = LinearLayoutManager(this)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        if (isFootball) {
            lifecycleScope.launch {
                footballViewModel.selectedMatch.collect { match ->
                    match?.let { bindFootballMatchData(it) }
                }
            }
            lifecycleScope.launch {
                footballViewModel.isRefreshing.collect { isRefreshing ->
                    progressBar.visibility = if (isRefreshing) View.VISIBLE else View.GONE
                }
            }
            if (matchId.isNotEmpty()) {
                footballViewModel.loadMatchDetail(matchId)
            }
        } else {
            lifecycleScope.launch {
                viewModel.match.collect { match ->
                    match?.let { bindMatchData(it, rvScores) }
                }
            }
            if (matchId.isNotEmpty()) {
                viewModel.loadMatch(matchId)
            }
        }
        
        loadAds()
    }

    private fun loadAds() {
        lifecycleScope.launch {
            val ads = repository.getAllAds()

            // Preload Interstitial
            ads.find { it.adPlacement.equals("Interstitial", ignoreCase = true) }?.let { ad ->
                if (ad.isActive == true && !ad.adUnitId.isNullOrEmpty()) {
                    AdsHelper.getInstance(this@MatchScoreDetailActivity).preloadAdADMOB_X_Inter(this@MatchScoreDetailActivity, ad.adUnitId)
                }
            }

            // Preload Rewarded
            ads.find { it.adPlacement.equals("Rewarded", ignoreCase = true) }?.let { ad ->
                if (ad.isActive == true && !ad.adUnitId.isNullOrEmpty()) {
                    AdsHelper.getInstance(this@MatchScoreDetailActivity).preloadRewardedAd(this@MatchScoreDetailActivity, ad.adUnitId)
                }
            }
        }
    }

    override fun onBackPressed() {
        AdsHelper.getInstance(this@MatchScoreDetailActivity).showAd_Mob_X_Inter_With_Time(this@MatchScoreDetailActivity)
        super.onBackPressed()
    }

    private fun bindFootballMatchData(match: FootballMatchScoreModel) {
        findViewById<TextView>(R.id.text_match_name).text = "${match.homeTeamName} vs ${match.awayTeamName}"
        findViewById<TextView>(R.id.text_match_status).text = match.matchStatus
        
        val homeLogo = findViewById<ImageView>(R.id.img_home_logo)
        val awayLogo = findViewById<ImageView>(R.id.img_away_logo)

        Glide.with(this)
            .load(match.homeTeamBadge)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(homeLogo)

        Glide.with(this)
            .load(match.awayTeamBadge)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(awayLogo)

        val statusBadge = findViewById<TextView>(R.id.text_status_badge)
        if (match.isLive == "1") {
            statusBadge.visibility = View.VISIBLE
            statusBadge.text = "LIVE"
        } else {
            statusBadge.visibility = View.GONE
        }

        findViewById<TextView>(R.id.text_venue).text = "League: ${match.leagueName}"
        findViewById<TextView>(R.id.text_date).text = "Date: ${match.matchDate}"
        findViewById<TextView>(R.id.text_match_type).text = "Country: ${match.countryName}"

        // Custom display for football score in the details
        val scoreText = "Score: ${match.homeTeamScore} - ${match.awayTeamScore}"
        findViewById<TextView>(R.id.text_match_status).append("\n$scoreText")
    }

    private fun bindMatchData(match: MatchEntity, rv: RecyclerView) {
        findViewById<TextView>(R.id.text_match_name).text = "${match.team1} vs ${match.team2}"
        findViewById<TextView>(R.id.text_match_status).text = match.status
        
        val homeLogo = findViewById<ImageView>(R.id.img_home_logo)
        val awayLogo = findViewById<ImageView>(R.id.img_away_logo)

        Glide.with(this)
            .load(match.team1Img)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(homeLogo)

        Glide.with(this)
            .load(match.team2Img)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(awayLogo)

        val statusBadge = findViewById<TextView>(R.id.text_status_badge)
        if (match.status?.contains("Live", ignoreCase = true) == true) {
            statusBadge.visibility = View.VISIBLE
        } else {
            statusBadge.visibility = View.GONE
        }

        findViewById<TextView>(R.id.text_venue).text = "Venue: ${match.venue}"
        findViewById<TextView>(R.id.text_date).text = "Date: ${match.date}"
        findViewById<TextView>(R.id.text_match_type).text = "Type: ${match.matchType}"

        if (!match.scoreJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<Inning>>() {}.type
            val scores: List<Inning> = Gson().fromJson(match.scoreJson, type)
            rv.adapter = CricketInningScoreAdapter(scores)
        }
    }
}
