package livefootball.footballstreamning.fifaworldcup.utilities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import livefootball.footballstreamning.fifaworldcup.BuildConfig
import livefootball.footballstreamning.fifaworldcup.database.AppEntity
import livefootball.footballstreamning.fifaworldcup.database.StreamingEntity

object DialogManager {

    /**
     * Evaluates and shows a dialog based on the server configuration.
     * returns true if a dialog was shown, false otherwise.
     */
    fun checkAndShowDialog(
        activity: Activity,
        app: AppEntity,
        streaming: StreamingEntity?,
        isSplash: Boolean
    ): Boolean {
        val currentVersion = app.currentVersion ?: ""
        val appVersion = BuildConfig.VERSION_NAME
        val isNewVersionAvailable = compareVersions(currentVersion, appVersion) > 0

        // 1. App Migration (Play Store) - Required
        if (!app.newPackageName.isNullOrEmpty()) {
            Utils.showCustomDialog(
                activity,
                "Update Required",
                "A new version of the app is available and is required to continue. Please update to the latest version to keep using the app.",
                "Update Now",
                "Exit",
                true,
                false,
                null,
                { openPlayStore(activity, app.newPackageName!!) },
                { activity.finishAffinity() }
            )
            return true
        }

        // 2. Force Update (Same app, version increase)
        if (isNewVersionAvailable && app.updateRequired == true) {
            Utils.showCustomDialog(
                activity,
                "Update Required",
                "A new version of the app is available and is required to continue. Please update to the latest version to keep using the app.",
                "Update Now",
                "",
                false,
                false,
                null,
                { openPlayStore(activity, activity.packageName) },
                null
            )
            return true
        }

        // 3. App Migration (External URL) - Required
        if (streaming != null && !streaming.newAppOutsideUrl.isNullOrEmpty() && streaming.forceNewAppOutsideUrl == true) {
            val title = streaming.outsideUrlTitle ?: "A New Version is Available"
            val message = streaming.outsideUrlDescription ?: "This app is no longer supported and will no longer receive updates. Please download our new app to continue enjoying the latest features, improved performance, and ongoing support."
            
            Utils.showCustomDialog(
                activity,
                title,
                message,
                "Download New App",
                "Exit",
                true,
                false,
                streaming.outsideUrlImageUrl,
                { openExternalUrl(activity, streaming.newAppOutsideUrl!!) },
                { activity.finishAffinity() }
            )
            return true
        }

        // If we are in Splash, we only check the above conditions.
        if (isSplash) return false

        // Step 2 - Promotional Dialogs (Only for MainActivity)

        // 1. Promote New Play Store App - Optional
        // (This condition might need adjustment since we now force if package name exists above)
        // But following logic: if it reached here, force didn't trigger. 
        // However, if newPackageName is not empty, it ALWAYS triggers above.
        // So this optional Play Store promotion is effectively skipped.

        // 2. Optional Update
        if (isNewVersionAvailable && app.updateRequired == false) {
            Utils.showCustomDialog(
                activity,
                "Update Is Available",
                "A new version of the app is available. Please update to the latest version to enjoy the newest features, improvements, and performance enhancements.",
                "Update Now",
                "Cancel",
                true,
                true,
                null,
                { openPlayStore(activity, activity.packageName) },
                { }
            )
            return true
        }

        // 3. Promote New App (External URL) - Optional
        if (streaming != null && !streaming.newAppOutsideUrl.isNullOrEmpty() && streaming.forceNewAppOutsideUrl == false) {
            val title = streaming.outsideUrlTitle ?: "Try Our New App"
            val message = streaming.outsideUrlDescription ?: "We've launched a brand-new app with an improved design, better performance, and exciting new features. Download it today and experience the latest version."

            Utils.showCustomDialog(
                activity,
                title,
                message,
                "Download New App",
                "Cancel",
                true,
                true,
                streaming.outsideUrlImageUrl,
                { openExternalUrl(activity, streaming.newAppOutsideUrl!!) },
                { }
            )
            return true
        }

        return false
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val levels1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val levels2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        val length = maxOf(levels1.size, levels2.size)
        for (i in 0 until length) {
            val v1Level = levels1.getOrElse(i) { 0 }
            val v2Level = levels2.getOrElse(i) { 0 }
            if (v1Level < v2Level) return -1
            if (v1Level > v2Level) return 1
        }
        return 0
    }

    private fun openPlayStore(activity: Activity, packageName: String) {
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun openExternalUrl(activity: Activity, url: String) {
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
