package livefootball.footballstreamning.fifaworldcup.ads;

import android.content.Context;
import android.content.SharedPreferences;


public class AppSPGetSet {


    public void setAddFirstTimeSP(Context context, boolean value) {
        SharedPreferences AddFirstTimeSP = context.getSharedPreferences("AddFirstTimeSP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = AddFirstTimeSP.edit();
        editor.putBoolean("AddFirstTimeSP", value);
        editor.apply();
    }

    public Boolean getAddFirstTimeSP(Context context) {
        SharedPreferences AddFirstTimeSP = context.getSharedPreferences("AddFirstTimeSP", Context.MODE_PRIVATE);
        return AddFirstTimeSP.getBoolean("AddFirstTimeSP", true);
    }


    public void setRewardAdShownSP(Context context, boolean value) {
        SharedPreferences RewardAdShownSP = context.getSharedPreferences("RewardAdShownSP", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = RewardAdShownSP.edit();
        editor.putBoolean("RewardAdShownSP", value);
        editor.apply();
    }

    public Boolean getRewardAdShownSP(Context context) {
        SharedPreferences RewardAdShownSP = context.getSharedPreferences("RewardAdShownSP", Context.MODE_PRIVATE);
        return RewardAdShownSP.getBoolean("RewardAdShownSP", false);
    }
}
