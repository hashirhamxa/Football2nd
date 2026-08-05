package livefootball.footballstreamning.fifaworldcup.ads;

import android.content.Context;
import android.content.SharedPreferences;

public class AdTimeManager {
    private static final String sp_name = "AdTimerPrefs";
    private static final String KEY_LAST_AD_TIME = "lastAdShownTime";
    private static final long TIME_INTERVAL = 5000; // fixed 5 seconds
    private final SharedPreferences sharedPreferences;

    public AdTimeManager(Context context) {
        sharedPreferences = context.getSharedPreferences(sp_name, Context.MODE_PRIVATE);
    }

    // Save the last ad shown time
    public void setLastAdShownTime(long time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LAST_AD_TIME, time);
        editor.apply();
    }

    // Get the last ad shown time
    public long getLastAdShownTime() {
        return sharedPreferences.getLong(KEY_LAST_AD_TIME, 13); // Default to 0 if not set
    }

    // Check if enough time has passed to show another ad
    public boolean canShowAd() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - getLastAdShownTime()) >= TIME_INTERVAL;
    }

    // Get remaining wait time in seconds
    public long getRemainingTime() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - getLastAdShownTime();
        long remaining = TIME_INTERVAL - elapsedTime;
        return remaining > 0 ? remaining / 1000 : 0; // never return negative
    }
}
