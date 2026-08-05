package livefootball.footballstreamning.fifaworldcup.newplayer;

import android.os.Bundle;
import android.view.WindowManager;

import com.brouken.player.PlayerActivity;

import livefootball.footballstreamning.fifaworldcup.ads.AdsHelper;


public class NewPlayerActivity extends PlayerActivity {
    AdsHelper adsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        adsHelper = AdsHelper.getInstance(NewPlayerActivity.this);

        String interID = getIntent().getStringExtra("interstitialAdKey");
        String rewardedID = getIntent().getStringExtra("rewardedAdKey");

        if (interID != null && !interID.isEmpty()) {
            adsHelper.preloadAdADMOB_X_Inter(NewPlayerActivity.this, interID);
        }
        if (rewardedID != null && !rewardedID.isEmpty()) {
            adsHelper.preloadRewardedAd(NewPlayerActivity.this, rewardedID);
        }
    }

    @Override
    public void onBackPressed() {
        adsHelper.showAd_Mob_X_Inter_With_Time(NewPlayerActivity.this);
        NewPlayerActivity.this.finish();

    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
    }
}