package livefootball.footballstreamning.fifaworldcup.activities

import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.adapters.DashboardPagerAdapter
import livefootball.footballstreamning.fifaworldcup.ads.AdsHelper
import livefootball.footballstreamning.fifaworldcup.network.AppRepository
import livefootball.footballstreamning.fifaworldcup.utilities.DialogManager
import livefootball.footballstreamning.fifaworldcup.viewmodels.HomeDashboardViewModel
import javax.inject.Inject

@AndroidEntryPoint
class HomeDashboardActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: AppRepository

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView
    private var isUiRevealed = false

    private val viewModel: HomeDashboardViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle result if necessary
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_main)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        viewPager = findViewById(R.id.view_pager)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        setupViewPager()
        setupBottomNavigation()
        observeViewModel()
        loadAds()
    }

    private fun loadAds() {
        lifecycleScope.launch {
            val ads = repository.getAllAds()

            // 1. Preload Interstitial
            ads.find { it.adPlacement.equals("Interstitial", ignoreCase = true) }?.let { ad ->
                if (ad.isActive == true && !ad.adUnitId.isNullOrEmpty()) {
                    AdsHelper.getInstance(this@HomeDashboardActivity).preloadAdADMOB_X_Inter(this@HomeDashboardActivity, ad.adUnitId)
                }
            }

            // 2. Preload Rewarded
            ads.find { it.adPlacement.equals("Rewarded", ignoreCase = true) }?.let { ad ->
                if (ad.isActive == true && !ad.adUnitId.isNullOrEmpty()) {
                    AdsHelper.getInstance(this@HomeDashboardActivity).preloadRewardedAd(this@HomeDashboardActivity, ad.adUnitId)
                }
            }
        }
    }

    private fun observeViewModel() {
        val progressBar = findViewById<ProgressBar>(R.id.main_progress_bar)
        val bottomNavCard = findViewById<View>(R.id.card_bottom_navigation)
        val shimmerContainer = findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Global loading state: Only reveal UI when EVERYTHING is ready
                launch {
                    viewModel.isConfigReady.collect { ready ->
                        if (ready) {
                            if (isUiRevealed) {
                                shimmerContainer.visibility = View.GONE
                                viewPager.visibility = View.VISIBLE
                                bottomNavCard.visibility = View.VISIBLE
                                progressBar.visibility = View.GONE
                                return@collect
                            }

                            isUiRevealed = true
                            // Add a 2-second delay to ensure premium shimmer feel
                            delay(2000)

                            // Stop Shimmer
                            shimmerContainer.stopShimmer()

                            // Animate transition
                            shimmerContainer.animate()
                                .alpha(0f)
                                .setDuration(300)
                                .withEndAction {
                                    shimmerContainer.visibility = View.GONE
                                    checkNotificationPermission()
                                }

                            viewPager.alpha = 0f
                            bottomNavCard.alpha = 0f
                            viewPager.visibility = View.VISIBLE
                            bottomNavCard.visibility = View.VISIBLE

                            viewPager.animate()
                                .alpha(1f)
                                .translationYBy(-16f)
                                .setDuration(400)
                                .start()

                            bottomNavCard.animate()
                                .alpha(1f)
                                .setDuration(400)
                                .start()

                            progressBar.visibility = View.GONE

                            // Initial selection when app first opens and config is ready
                            resetToFirstTab()
                        } else {
                            if (!isUiRevealed) {
                                shimmerContainer.startShimmer()
                                shimmerContainer.visibility = View.VISIBLE
                                viewPager.visibility = View.GONE
                                bottomNavCard.visibility = View.GONE
                            }
                        }
                    }
                }

                launch {
                    combine(viewModel.appConfig, viewModel.streamingConfig) { app, streaming ->
                        Pair(app, streaming)
                    }.collect { (app, streaming) ->
                        if (app != null) {
                            DialogManager.checkAndShowDialog(this@HomeDashboardActivity, app, streaming, false)
                        }
                    }
                }

                launch {
                    viewModel.showHighlights.collect { show ->
                        val currentMenu = bottomNavigationView.menu.findItem(R.id.navigation_highlights)
                        if (currentMenu.isVisible != show) {
                            currentMenu.isVisible = show
                            val adapter = viewPager.adapter as? DashboardPagerAdapter
                            adapter?.updateHighlightsVisibility(show)
                            if (isUiRevealed) resetToFirstTab()
                        }
                    }
                }

                launch {
                    viewModel.showHome.collect { show ->
                        val currentMenu = bottomNavigationView.menu.findItem(R.id.navigation_home)
                        if (currentMenu.isVisible != show) {
                            currentMenu.isVisible = show
                            val adapter = viewPager.adapter as? DashboardPagerAdapter
                            adapter?.updateHomeVisibility(show)
                            if (isUiRevealed) resetToFirstTab()
                        }
                    }
                }

                launch {
                    viewModel.showScore.collect { show ->
                        val currentMenu = bottomNavigationView.menu.findItem(R.id.navigation_score)
                        if (currentMenu.isVisible != show) {
                            currentMenu.isVisible = show
                            val adapter = viewPager.adapter as? DashboardPagerAdapter
                            adapter?.updateScoreVisibility(show)
                            if (isUiRevealed) resetToFirstTab()
                        }
                    }
                }
            }
        }
    }

    /**
     * Handles logic when server configuration changes (tabs added/removed).
     * Ensures we don't stay on a removed fragment.
     */
    private fun handleConfigChange() {
        if (!isUiRevealed) return

        val adapter = viewPager.adapter as? DashboardPagerAdapter ?: return
        val currentItemId = bottomNavigationView.selectedItemId

        // Check if current item still exists in the active set
        val ids = mutableListOf<Int>()
        if (viewModel.showScore.value) ids.add(R.id.navigation_score)
        if (viewModel.showHome.value) ids.add(R.id.navigation_home)
        if (viewModel.showHighlights.value) ids.add(R.id.navigation_highlights)
        ids.add(R.id.navigation_settings)

        if (!ids.contains(currentItemId)) {
            // Current fragment was removed, reset to first available
            resetToFirstTab()
        } else {
            // Sync positions in case indices shifted but item still exists
            syncSelection()
        }
    }

    private fun resetToFirstTab() {
        val adapter = viewPager.adapter as? DashboardPagerAdapter ?: return
        val firstId = adapter.getIdForPosition(0)
        bottomNavigationView.selectedItemId = firstId
        viewPager.setCurrentItem(0, false)
    }

    private fun syncSelection() {
        val adapter = viewPager.adapter as? DashboardPagerAdapter ?: return
        val currentItemId = bottomNavigationView.selectedItemId
        val targetPos = adapter.getPositionForId(currentItemId)
        if (viewPager.currentItem != targetPos) {
            viewPager.currentItem = targetPos
        }
    }

    private fun setupViewPager() {
        val adapter = DashboardPagerAdapter(this)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val itemId = (viewPager.adapter as? DashboardPagerAdapter)?.getIdForPosition(position)
                if (itemId != null && bottomNavigationView.selectedItemId != itemId) {
                    bottomNavigationView.selectedItemId = itemId
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            val targetPos = (viewPager.adapter as? DashboardPagerAdapter)?.getPositionForId(item.itemId)
            if (targetPos != null) {
                if (viewPager.currentItem != targetPos) {
                    viewPager.currentItem = targetPos
                }
                true
            } else false
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}