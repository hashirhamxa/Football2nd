package livefootball.footballstreamning.fifaworldcup.ads;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

import livefootball.footballstreamning.fifaworldcup.R;
import livefootball.footballstreamning.fifaworldcup.BuildConfig;


public class AdsHelper {
    private static AdsHelper instance;
    AdTimeManager adTimeManager;
    AppSPGetSet appSPGetSet;

    // Private constructor
    private AdsHelper(Context context) {
        adTimeManager = new AdTimeManager(context);
        appSPGetSet = new AppSPGetSet();
    }

    // Singleton getInstance method
    public static synchronized AdsHelper getInstance(Context context) {
        if (instance == null) {
            instance = new AdsHelper(context.getApplicationContext());
        }
        return instance;
    }




    public void initializeAdMob(Activity activity, String appId) {
        if (BuildConfig.DEBUG) {
            return; // Skip loading ads in debug mode
        }
        MobileAds.initialize(activity, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                Log.e("AdsHelper", "AdMob initialized with App ID: " + appId);
            }
        });
    }

    public void loadAdaptiveADMOB_X_Banner(Activity activity, RelativeLayout adContainerView, String banner_id) {
        if (BuildConfig.DEBUG) {
            return; // Skip loading ads in debug mode
        }

        Log.e("AdMob", "loadAdaptiveADMOB_X_Banner banner_id " + banner_id);
        // Create an AdView and set the ad unit ID
        AdView adView = new AdView(activity);
        adView.setAdUnitId(banner_id);
        adContainerView.removeAllViews(); // Ensure only one ad view is added
        adContainerView.addView(adView);
        // Determine the adaptive ad size
        AdSize adSize = getBannerAdSize(activity);
        adView.setAdSize(adSize);
        // Set an AdListener for logging and handling ad events
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                super.onAdFailedToLoad(adError);
                // Log the error or handle fallback ads
                // Example: loadFBBannerAd(activity);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                // Ad successfully loaded, you can log or handle this event
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                // Handle ad clicks if needed
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                // Log impressions if required
            }
        });
        // Load the ad
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private AdSize getBannerAdSize(Activity activity) {
        // Get the display metrics to calculate the screen width in pixels
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // Calculate the width of the screen in density-independent pixels (dp)
        float density = displayMetrics.density;
        int adWidth = (int) (displayMetrics.widthPixels / density);

        // Return the adaptive AdSize
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }


    public void loadNativeBannerAd(Context context, NativeAdView adView, String nativeId) {
        Log.e("AdMob", "loadNativeBannerAd nativeId " + nativeId);
        if (BuildConfig.DEBUG) {
            return; // Skip loading ads in debug mode
        }
        AdLoader adLoader = new AdLoader.Builder(context, nativeId).forNativeAd(nativeAd -> populateNativeAdView(nativeAd, adView))  // On Ad Loaded, populate it
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        Log.e("AdMob", "Ad failed to load: " + adError.getMessage());
                        adView.setVisibility(View.GONE);  // Hide the adView if ad fails to load
                    }
                }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        if (adView == null || nativeAd == null) return;

        // Set the headline
        TextView headlineView = adView.findViewById(R.id.ad_headline);
        if (headlineView != null) {
            if (nativeAd.getHeadline() != null) {
                headlineView.setText(nativeAd.getHeadline());
                adView.setHeadlineView(headlineView);
            } else {
                headlineView.setVisibility(View.GONE);
            }
        }

        // Set the app icon
        ImageView iconView = adView.findViewById(R.id.ad_app_icon);
        if (iconView != null) {
            if (nativeAd.getIcon() != null) {
                iconView.setImageDrawable(nativeAd.getIcon().getDrawable());
                iconView.setVisibility(View.VISIBLE);
                adView.setIconView(iconView);
            } else {
                iconView.setVisibility(View.GONE);
            }
        }

        // Set the call-to-action button
        View callToActionView = adView.findViewById(R.id.ad_call_to_action);
        if (callToActionView != null) {
            if (nativeAd.getCallToAction() != null) {
                if (callToActionView instanceof TextView) {
                    ((TextView) callToActionView).setText(nativeAd.getCallToAction());
                }
                callToActionView.setVisibility(View.VISIBLE);
                adView.setCallToActionView(callToActionView);
            } else {
                callToActionView.setVisibility(View.GONE);
            }
        }

        // Assign the NativeAd to the NativeAdView
        adView.setNativeAd(nativeAd);

        // Make the adView visible
        adView.setVisibility(View.VISIBLE);
    }


    private InterstitialAd interstitialAd;
    private boolean isAdLoading = false; // To prevent multiple loading attempts

    public static boolean interAdShowing = false;


    /**
     * Preload an interstitial ad.
     *
     * @param activity The current activity context.
     */
    public void preloadAdADMOB_X_Inter(Activity activity, String adUnitId1) {
        if (BuildConfig.DEBUG) {
            return; // Skip loading ads in debug mode
        }
        if (isAdLoading || interstitialAd != null) {
            return; // Prevent multiple loading attempts
        }

        interAdShowing = false;
        isAdLoading = true; // Mark as loading
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, adUnitId1, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(InterstitialAd ad) {
                isAdLoading = false;
                interstitialAd = ad;
                Log.e("AdMob", "Interstitial Ad Loaded");

                // Set a callback to handle ad lifecycle events
                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.e("AdMob", "Ad Dismissed");
                        adTimeManager.setLastAdShownTime(System.currentTimeMillis());
                        interAdShowing = false;
                        interstitialAd = null; // Ad object is no longer valid
                        preloadAdADMOB_X_Inter(activity, adUnitId1); // Preload next ad

                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                        Log.e("AdMob", "Ad Failed to Show: " + adError.getMessage());
                        interAdShowing = false;
                        interstitialAd = null; // Reset ad state
                        preloadAdADMOB_X_Inter(activity, adUnitId1); // Retry loading
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.e("AdMob", "Ad Shown");
                        interAdShowing = true;
                        interstitialAd = null; // Prevent reuse
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                isAdLoading = false; // Reset loading state
                Log.e("AdMob", "Failed to Load Interstitial Ad: " + adError.getMessage());
            }
        });
    }

    /**
     * Show the interstitial ad if it's loaded.
     *
     * @param activity The current activity context.
     */


    public void showAd_Mob_X_Inter_With_Time(Activity activity) {
        if (BuildConfig.DEBUG) {
            return; // Skip loading ads in debug mode
        }
        Log.e("AdMob", "showAd_Mob_X_Inter_With_Time");
        Log.e("AdMob", "showAd_Mob_X_Inter_With_Time adTimeManager.canShowAd() " + adTimeManager.canShowAd());

       if (rewardedAd != null && !appSPGetSet.getRewardAdShownSP(activity)) {
            Log.e("AdMob", "showAd_Mob_X_Inter_With_Time Reward");
            if (appSPGetSet.getAddFirstTimeSP(activity) || adTimeManager.canShowAd()) {
                showRewardedAd(activity);
            }
        } else if (appSPGetSet.getAddFirstTimeSP(activity) || adTimeManager.canShowAd()) {
            Log.e("AdMob", "showAd_Mob_X_Inter_With_Time Inter");
            Log.e("AdMob", "adTimeManager.canShowAd() " + adTimeManager.canShowAd() + " isFirstAd " + appSPGetSet.getAddFirstTimeSP(activity));
            if (interstitialAd != null) {
                interstitialAd.show(activity);
                appSPGetSet.setAddFirstTimeSP(activity, false);
            } else {
                Log.e("AdMob", "Interstitial Ad Not Ready");
            }
        }

    }


    private RewardedAd rewardedAd;
    private boolean isRewardedAdLoading = false;

    public void preloadRewardedAd(Activity activity, String adUnitId1) {
        if (BuildConfig.DEBUG) {
            return; // Skip loading ads in debug mode
        }
        Log.e("AdMob", "preloadRewardedAd");
        Log.e("AdMob", "preloadRewardedAd adUnitId " + adUnitId1);

        if (isRewardedAdLoading || rewardedAd != null || appSPGetSet.getRewardAdShownSP(activity)) {
            Log.e("AdMob", "preloadRewardedAd return");
            return; // Already loading,loaded or shown
        }

        isRewardedAdLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(activity, adUnitId1, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                Log.e("AdMob", "Rewarded Ad Loaded");
                rewardedAd = ad;
                isRewardedAdLoading = false;

                rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        adTimeManager.setLastAdShownTime(System.currentTimeMillis());
                        Log.e("AdMob", "Rewarded Ad Dismissed");
                        rewardedAd = null;
                        appSPGetSet.setRewardAdShownSP(activity, true);
                        appSPGetSet.setAddFirstTimeSP(activity, false);
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                        Log.e("AdMob", "Rewarded Ad Failed to Show: " + adError.getMessage());
                        rewardedAd = null;
                        preloadRewardedAd(activity, adUnitId1);
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.e("AdMob", "Rewarded Ad Shown");

                        rewardedAd = null; // Prevent reuse
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.e("AdMob", "Failed to Load Rewarded Ad: " + loadAdError.getMessage());
                rewardedAd = null;
                isRewardedAdLoading = false;
            }
        });
    }

    public void showRewardedAd(Activity activity) {
        if (rewardedAd != null) {
            rewardedAd.show(activity, rewardItem -> {
                Log.e("AdMob", "User watched rewarded ad and earned reward.");
                // Optional: Internal reward flag logic can be placed here
                appSPGetSet.setAddFirstTimeSP(activity, false);
            });
        } else {
            Log.e("AdMob", "Rewarded Ad Not Ready");
        }
    }


    public void initUnityAds(Activity activity, String appID) {
        boolean TEST_MODE = false;
        UnityAds.initialize(activity, appID, TEST_MODE, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                // Ads ready to use
                Log.e("admob unity", "initUnityAds onInitializationComplete");
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                // Handle initialization failure
                Log.e("admob unity", "initUnityAds onInitializationFailed " + error + " " + message);

            }
        });
    }

    public void loadUnityInterstitialAd(Activity activity, String INTERSTITIAL_AD_ID) {
        UnityAds.load(INTERSTITIAL_AD_ID, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                // The ad has been loaded successfully
                Log.e("admob unity", "loadUnityInterstitialAd Interstitial ad loaded successfully.");
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                // Handle ad loading failure
                Log.e("admob unity", "loadUnityInterstitialAd Failed to load ad: " + message);
            }
        });
    }

    public void showUnityInterstitialAd(Activity activity, String INTERSTITIAL_AD_ID) {
        if (adTimeManager.canShowAd()) {

            try {
                // Save the original orientation
                int originalOrientation = activity.getRequestedOrientation();
                // Force Portrait Mode
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } catch (Exception e) {
                Log.e("admob unity", "showUnityInterstitialAd Exception Orientation " + e.getMessage());
            }


            UnityAds.show(activity, INTERSTITIAL_AD_ID, new IUnityAdsShowListener() {
                @Override
                public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                    // Handle the completion of the ad
                    Log.e("admob unity", "showUnityInterstitialAd onUnityAdsShowComplete");
                    loadUnityInterstitialAd(activity, INTERSTITIAL_AD_ID);
                    adTimeManager.setLastAdShownTime(System.currentTimeMillis());
                }


                @Override
                public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                    Log.e("admob unity", "showUnityInterstitialAd onUnityAdsShowFailure " + error + " " + message);

                }

                @Override
                public void onUnityAdsShowStart(String placementId) {
                    // Handle when the ad starts showing
                    Log.e("admob unity", "showUnityInterstitialAd onUnityAdsShowStart");

                }

                @Override
                public void onUnityAdsShowClick(String placementId) {
                    // Handle when the ad is clicked
                    Log.e("admob unity", "showUnityInterstitialAd onUnityAdsShowClick");

                }
            });
        }

    }
}
