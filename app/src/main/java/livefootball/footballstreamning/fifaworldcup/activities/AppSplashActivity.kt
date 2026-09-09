package livefootball.footballstreamning.fifaworldcup.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.ads.AdsHelper
import livefootball.footballstreamning.fifaworldcup.ads.AppOpenManager
import livefootball.footballstreamning.fifaworldcup.database.AdEntity
import livefootball.footballstreamning.fifaworldcup.network.AppRepository
import livefootball.footballstreamning.fifaworldcup.utilities.CricketApp
import livefootball.footballstreamning.fifaworldcup.utilities.DialogManager
import livefootball.footballstreamning.fifaworldcup.utilities.SplashPreloader
import livefootball.footballstreamning.fifaworldcup.utilities.Utils
import javax.inject.Inject
import kotlin.coroutines.resume

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class AppSplashActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: AppRepository

    private var configRetryCount = 0
    private var isFlowStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        setContentView(R.layout.activity_splash)

        loadSplashBackground()
    }

    override fun onResume() {
        super.onResume()
        if (!isFlowStarted) {
            if (!AppOpenManager.checkSniffer(this)) {
                isFlowStarted = true
                lifecycleScope.launch {
                    handleSplashFlow()
                }
            }
        }
    }

    private fun loadSplashBackground() {
        val imgBackground = findViewById<ImageView>(R.id.img_background)
        val preloader = SplashPreloader(this)
        val cachedFile = preloader.getCachedSplashFile()

        if (cachedFile != null) {
            try {
                val bitmap = BitmapFactory.decodeFile(cachedFile.absolutePath)
                if (bitmap != null) {
                    imgBackground.setImageBitmap(bitmap)
                } else {
                    imgBackground.setImageResource(R.drawable.splash_background)
                }
            } catch (e: Exception) {
                imgBackground.setImageResource(R.drawable.splash_background)
            }
        } else {
            imgBackground.setImageResource(R.drawable.splash_background)
        }
    }

    private suspend fun handleSplashFlow() {
        if (!Utils.isInternetAvailable(this)) {
            showNoInternetDialog()
            return
        }

        if (AppOpenManager.checkSniffer(this)) {
            isFlowStarted = false
            return
        }
        
        fetchConfigAndProceed()
    }

    private suspend fun fetchConfigAndProceed() {
        // Linear call to suspending function
        val isSuccess = repository.fetchAndSaveConfig { success, error ->
            if (!success) {
                Log.e("AppSplashActivity", "fetchConfigAndProceed: API failed with error: $error")
            } else {
                Log.d("AppSplashActivity", "fetchConfigAndProceed: API success")
            }
        }

        if (isSuccess) {
            val app = repository.getApp()
            if (app != null) {
                AdsHelper.getInstance(this).setAdInterval(app.adsTimeCountDown)
                val streaming = repository.getStreamingData(app.id).firstOrNull()?.streaming
                val dialogShown = DialogManager.checkAndShowDialog(this, app, streaming, true)
                if (dialogShown) return
            }

            val allAds = repository.getAllAds()
            val appOpenId = allAds.find { it.adPlacement.equals("AppOpen", ignoreCase = true) }?.adUnitId
            (application as CricketApp).appOpenManager?.setAppOpenAdId(appOpenId)

            preloadAds(allAds)
            loadAndShowAppOpenAd(allAds)
        } else {
            if (configRetryCount < 1) {
                configRetryCount++
                fetchConfigAndProceed()
            } else {
                showErrorDialog()
            }
        }
    }

    private fun preloadAds(ads: List<AdEntity>) {
        val adsHelper = AdsHelper.getInstance(this)
        ads.find { it.adPlacement.equals("Interstitial", ignoreCase = true) }?.adUnitId?.let {
            adsHelper.preloadAdADMOB_X_Inter(this, it)
        }
        ads.find { it.adPlacement.equals("Rewarded", ignoreCase = true) }?.adUnitId?.let {
            adsHelper.preloadRewardedAd(this, it)
        }
    }

    private suspend fun loadAndShowAppOpenAd(ads: List<AdEntity>) {
        val appOpenId = ads.find { it.adPlacement.equals("AppOpen", ignoreCase = true) }?.adUnitId
        val appOpenManager = (application as CricketApp).appOpenManager

        if (appOpenId != null && appOpenManager != null) {
            suspendCancellableCoroutine { continuation ->
                var isDone = false
                var isShowing = false

                val timeoutHandler = Handler(Looper.getMainLooper())
                val timeoutRunnable = Runnable {
                    if (!isDone && !isShowing) {
                        isDone = true
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                }

                // Start 4-second timeout
                timeoutHandler.postDelayed(timeoutRunnable, 4000)

                appOpenManager.fetchAndShowAd(appOpenId, object : AppOpenManager.OnAppOpenAdListener {
                    override fun onAdResult() {
                        timeoutHandler.removeCallbacks(timeoutRunnable)
                        if (!isDone) {
                            isDone = true
                            if (continuation.isActive) continuation.resume(Unit)
                        }
                    }

                    override fun onAdShowed() {
                        isShowing = true
                        // Ad is visible, cancel the timeout and wait for onAdResult (dismissed)
                        timeoutHandler.removeCallbacks(timeoutRunnable)
                    }
                })

                continuation.invokeOnCancellation {
                    timeoutHandler.removeCallbacks(timeoutRunnable)
                }
            }
        }
        navigateToMain()
    }

    private fun showNoInternetDialog() {
        Utils.showCustomDialog(
            this,
            "No Internet Available!",
            "Please check your internet connection and try again",
            "OK",
            "Cancel",
            false,
            false,
            null,
            { finishAffinity() },
            { }
        )
    }

    private fun showErrorDialog() {
        Utils.showCustomDialog(
            this,
            "Something went wrong!",
            "An error occurred while fetching data from the server. Please try again later.",
            "OK",
            "",
            false,
            false,
            null,
            { finishAffinity() },
            null
        )
    }

    private fun navigateToMain() {
        startActivity(Intent(this, HomeDashboardActivity::class.java))
        finish()
    }
}