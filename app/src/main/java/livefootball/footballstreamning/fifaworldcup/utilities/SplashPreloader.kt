package livefootball.footballstreamning.fifaworldcup.utilities

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class SplashPreloader(private val context: Context) {

    private val sharedPrefs = context.getSharedPreferences("splash_prefs", Context.MODE_PRIVATE)
    private val splashFile = File(context.filesDir, "splash_image.png")

    companion object {
        private const val PREF_KEY_URL = "current_splash_url"
        private const val TAG = "SplashPreloader"
    }

    /**
     * Downloads and caches the splash image if the URL has changed.
     * If the URL is null or blank, it deletes the cached image.
     * This should be called from a background scope (e.g., in MainActivity after fetching config).
     */
    suspend fun updateSplashImage(imageUrl: String?) {
        if (imageUrl.isNullOrBlank()) {
            clearCachedSplash()
            return
        }

        val currentUrl = sharedPrefs.getString(PREF_KEY_URL, null)
        if (imageUrl == currentUrl && splashFile.exists()) {
            Log.d(TAG, "Splash image already cached for URL: $imageUrl")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting splash image download: $imageUrl")
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()

                val inputStream = connection.inputStream
                val tempFile = File(context.cacheDir, "temp_splash.png")
                
                FileOutputStream(tempFile).use { output ->
                    inputStream.copyTo(output)
                }

                // Verification: Check if the downloaded file is a valid image
                if (isValidImage(tempFile)) {
                    if (tempFile.renameTo(splashFile)) {
                        sharedPrefs.edit().putString(PREF_KEY_URL, imageUrl).apply()
                        Log.d(TAG, "Splash image updated successfully")
                    } else {
                        // Fallback if rename fails (e.g., across partitions)
                        tempFile.copyTo(splashFile, overwrite = true)
                        tempFile.delete()
                        sharedPrefs.edit().putString(PREF_KEY_URL, imageUrl).apply()
                        Log.d(TAG, "Splash image updated successfully (copied)")
                    }
                } else {
                    Log.e(TAG, "Downloaded file is not a valid image")
                    tempFile.delete()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error downloading splash image", e)
            }
        }
    }

    private fun clearCachedSplash() {
        if (splashFile.exists()) {
            splashFile.delete()
            Log.d(TAG, "Cached splash image deleted (URL was null/empty)")
        }
        sharedPrefs.edit().remove(PREF_KEY_URL).apply()
    }

    private fun isValidImage(file: File): Boolean {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
            options.outWidth > 0 && options.outHeight > 0
        } catch (e: Exception) {
            false
        }
    }

    fun getCachedSplashFile(): File? {
        return if (splashFile.exists()) splashFile else null
    }
}
