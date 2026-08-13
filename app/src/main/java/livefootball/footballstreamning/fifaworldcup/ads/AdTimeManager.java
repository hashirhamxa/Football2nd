package livefootball.footballstreamning.fifaworldcup.ads;

import android.content.Context;
import android.content.SharedPreferences;

public class AdTimeManager {
    private final String sp_name = "AdTimerPrefs";
    private final String KEY_LAST_AD_TIME = "lastAdShownTime";
    private final String KEY_AD_INTERVAL = "adInterval";
    private final SharedPreferences sharedPreferences;

    public AdTimeManager(Context context) {
        sharedPreferences = context.getSharedPreferences(sp_name, Context.MODE_PRIVATE);
    }

    // Save the ad interval in seconds
    public void setAdInterval(Integer seconds) {
        long intervalMs;
        if (seconds == null || seconds <= 0) {
            intervalMs = 5000; // Default 5 seconds
        } else {
            intervalMs = seconds * 1000L;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_AD_INTERVAL, intervalMs);
        editor.apply();
    }

    // Get the ad interval in milliseconds
    public long getAdInterval() {
        return sharedPreferences.getLong(KEY_AD_INTERVAL, 5000); // Default 5 seconds
    }

    // Save the last ad shown time
    public void setLastAdShownTime(long time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LAST_AD_TIME, time);
        editor.apply();
    }

    // Get the last ad shown time
    public long getLastAdShownTime() {
        return sharedPreferences.getLong(KEY_LAST_AD_TIME, 0); // Default to 0 if not set
    }

    // Check if enough time has passed to show another ad
    public boolean canShowAd() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - getLastAdShownTime()) >= getAdInterval();
    }

    // Get remaining wait time in seconds
    public long getRemainingTime() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - getLastAdShownTime();
        long remaining = getAdInterval() - elapsedTime;
        return remaining > 0 ? remaining / 1000 : 0; // never return negative
    }
}
