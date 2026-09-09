package livefootball.footballstreamning.fifaworldcup.utilities



import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

object ReviewHelper {
    private const val PREFS_NAME = "review_prefs"
    private const val KEY_LAST_REVIEW_PROMPT = "last_review_prompt_time"
    private const val TAG = "ReviewHelper"

    fun maybeShowReview(activity: Activity) {
        if (shouldShowReview(activity)) {
            launchInAppReview(activity)
        }
    }

    private fun shouldShowReview(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastPromptTime = prefs.getLong(KEY_LAST_REVIEW_PROMPT, 0L)
        val currentTime = System.currentTimeMillis()

        // Show if it's the first time or if 3 days have passed
        val threeDaysMillis = 3 * 24 * 60 * 60 * 1000L
        return (currentTime - lastPromptTime) >= threeDaysMillis
    }

    private fun launchInAppReview(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()

        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                    updateLastPromptTime(activity)
                    Log.d(TAG, "In-app review flow complete")
                }
            } else {
                // There was some problem, log or handle error code.
                Log.e(TAG, "In-app review request failed", task.exception)
            }
        }
    }

    private fun updateLastPromptTime(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_REVIEW_PROMPT, System.currentTimeMillis()).apply()
    }
}