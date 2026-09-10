package livefootball.footballstreamning.fifaworldcup.activities

import android.content.Intent
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.adapters.DashboardPagerAdapter
import livefootball.footballstreamning.fifaworldcup.ads.AdsHelper
import livefootball.footballstreamning.fifaworldcup.network.AppRepository
import livefootball.footballstreamning.fifaworldcup.utilities.DialogManager
import livefootball.footballstreamning.fifaworldcup.utilities.ReviewHelper
import livefootball.footballstreamning.fifaworldcup.utilities.Utils
import livefootball.footballstreamning.fifaworldcup.viewmodels.HomeDashboardViewModel
import java.lang.Void
import javax.inject.Inject
import kotlin.String

@AndroidEntryPoint
class HomeDashboardActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: AppRepository

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var btnWhatsapp: View

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
        val topPanel = findViewById<View>(R.id.top_panel)
        val bottomNavCard = findViewById<View>(R.id.card_bottom_navigation)

        // Ensure there's a professional gap from the status bar
        ViewCompat.setOnApplyWindowInsetsListener(topPanel) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply status bar height + 12dp extra padding for a professional look
            val extraPadding = (12 * resources.displayMetrics.density).toInt()
            v.updatePadding(top = systemBars.top + extraPadding)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(bottomNavCard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Reduce the bottom inset impact to prevent too much empty space,
            // especially on 3-button navigation.
            v.updatePadding(bottom = systemBars.bottom)
            insets
        }
        viewPager = findViewById(R.id.view_pager)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        btnWhatsapp = findViewById(R.id.btn_whatsapp)

        setupViewPager()
        setupBottomNavigation()
        observeViewModel()
        loadAds()
        settingFirebaseMessage()
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
                                    // Trigger In-App Review check after UI reveal
                                    lifecycleScope.launch {
                                        delay(5000) // Wait 5 seconds after reveal
                                        ReviewHelper.maybeShowReview(this@HomeDashboardActivity)
                                    }
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
                    viewModel.whatsappLink.collect { link ->
                        if (!link.isNullOrEmpty()) {
                            btnWhatsapp.visibility = View.VISIBLE
                            Utils.animateSocialIcon(btnWhatsapp)
                            btnWhatsapp.setOnClickListener {
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = android.net.Uri.parse(link)
                                startActivity(intent)
                            }
                        } else {
                            btnWhatsapp.visibility = View.GONE
                            btnWhatsapp.clearAnimation()
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

    private fun settingFirebaseMessage() {
        try {
            // Initialize Firebase App
            if (FirebaseApp.getApps(this@HomeDashboardActivity).isEmpty()) {
                FirebaseApp.initializeApp(this@HomeDashboardActivity)
            }
            // Subscribe to topic "all"
            val firebaseMessaging = FirebaseMessaging.getInstance()
            firebaseMessaging.subscribeToTopic("all")
                .addOnCompleteListener(OnCompleteListener { task: Task<Void?>? -> })

            // Get FCM registration token
            firebaseMessaging.getToken()
                .addOnCompleteListener(OnCompleteListener { task: Task<String?>? ->
                    if (task!!.isSuccessful()) {
                        // Get new FCM registration token
                        val token = task.getResult()
                        if (token != null) {
                            // Log or use the token
                            // Log.d("FCM", "Token: " + token);
                        }
                    }
                })
        } catch (e: NullPointerException) {
            // Log the exception or handle it
            e.printStackTrace()
            // You can also show a toast or error message here if needed
            // Toast.makeText(MainActivity.this, "Error in Firebase setup: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (e: Exception) {
            // Handle any other exceptions
            e.printStackTrace()
        }
    }

}