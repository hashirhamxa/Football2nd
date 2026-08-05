package livefootball.footballstreamning.fifaworldcup.utilities

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object TimeUtils {

    private val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Converts a UTC timestamp string to a local Date object.
     */
    fun parseUtcToLocal(utcString: String?): Date? {
        if (utcString.isNullOrBlank()) return null
        return try {
            utcFormat.parse(utcString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Formats the remaining time until a date as a countdown string (e.g., "02:15:30").
     */
    fun getCountdownString(targetDate: Date?): String {
        if (targetDate == null) return ""
        val diff = targetDate.time - System.currentTimeMillis()
        if (diff <= 0) return ""

        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * Checks if the current time has passed the target date.
     */
    fun isEventLive(targetDate: Date?): Boolean {
        if (targetDate == null) return true // Default to live if no time provided
        return System.currentTimeMillis() >= targetDate.time
    }
}
