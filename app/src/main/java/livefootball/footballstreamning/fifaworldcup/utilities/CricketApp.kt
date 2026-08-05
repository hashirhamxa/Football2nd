package livefootball.footballstreamning.fifaworldcup.utilities

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import livefootball.footballstreamning.fifaworldcup.network.AppRepository
import livefootball.footballstreamning.fifaworldcup.ads.AppOpenManager
import javax.inject.Inject

@HiltAndroidApp
class CricketApp : Application() {

    @Inject
    lateinit var repository: AppRepository

    var appOpenManager: AppOpenManager? = null
        private set
    //Test Unity Id
    //    private final String unityGameID = "14851";
    private val unityGameID = "5607296"
    private val testMode = false
    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) { }
        appOpenManager = AppOpenManager(this, repository)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {
                if (activityReferences == 0 && !isActivityChangingConfigurations) {
                    // App has come to the foreground
                    onAppForegrounded(activity)
                }
                activityReferences++
            }

            override fun onActivityResumed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {
                activityReferences--
                isActivityChangingConfigurations = activity.isChangingConfigurations
                if (activityReferences == 0 && !isActivityChangingConfigurations) {
                    // App has gone to the background
                    onAppBackgrounded()
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    private fun onAppForegrounded(activity: Activity) {
        // Called when app comes to the foreground
        AppOpenManager.checkSniffer(activity)
    }

    private fun onAppBackgrounded() {
        // Called when app goes to the background
    }
}