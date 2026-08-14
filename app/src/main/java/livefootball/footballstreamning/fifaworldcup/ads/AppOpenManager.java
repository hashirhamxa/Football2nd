package livefootball.footballstreamning.fifaworldcup.ads;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.io.File;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import livefootball.footballstreamning.fifaworldcup.BuildConfig;
import livefootball.footballstreamning.fifaworldcup.network.AppRepository;
import livefootball.footballstreamning.fifaworldcup.activities.AppSplashActivity;
import livefootball.footballstreamning.fifaworldcup.newplayer.NewPlayerActivity;
import livefootball.footballstreamning.fifaworldcup.utilities.Utils;


public class AppOpenManager implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    private static final String LOG_TAG = "AppOpenManager";
    private static boolean isShowingAd = false;
    private final Application myApplication;
    private final AppRepository repository;
    private AppOpenAd appOpenAd = null;
    private Activity currentActivity;
    private int retryCount = 0;
    private final int maxRetries = 3;
    private String appOpenAdId = null;

    public AppOpenManager(Application myApplication, AppRepository repository) {
        this.myApplication = myApplication;
        this.repository = repository;
        this.myApplication.registerActivityLifecycleCallbacks(this);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this); // Register as a lifecycle observer
    }

    public void setAppOpenAdId(String adId) {
        this.appOpenAdId = adId;
        if (adId != null && !adId.isEmpty()) {
            fetchAd(adId);
        }
    }

    /**
     * Request an ad
     */
    public void fetchAd(String adKey) {
        if (adKey == null || adKey.isEmpty()) return;
        if (BuildConfig.DEBUG) {
            return; // Skip loading ads in debug mode
        }
        try {
            if (isAdAvailable() || retryCount >= maxRetries) {
                return;
            }
            AppOpenAd.AppOpenAdLoadCallback loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                    Log.e(LOG_TAG, "AppOpen fetchAd onAdLoaded");
                    AppOpenManager.this.appOpenAd = appOpenAd;
                    retryCount = 0; // Reset retry count
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.e(LOG_TAG, "AppOpen fetchAd onAdFailedToLoad: " + loadAdError.getMessage());
                    retryCount++;
                    if (retryCount < maxRetries) {
                        fetchAd(adKey);
                    }
                }
            };

            AdRequest request = getAdRequest();
            AppOpenAd.load(myApplication, adKey, request, loadCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnAppOpenAdListener {
        void onAdResult();
        void onAdShowed();
    }

    /**
     * Request and show an ad once loaded.
     */
    public void fetchAndShowAd(String adKey, OnAppOpenAdListener listener) {
        if (BuildConfig.DEBUG) {
            return; // Skip loading ads in debug mode
        }
        try {
            AppOpenAd.AppOpenAdLoadCallback loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                    Log.e(LOG_TAG, "AppOpen fetchAndShowAd onAdLoaded");
                    AppOpenManager.this.appOpenAd = appOpenAd;
                    showAdWithCallback(listener);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.e(LOG_TAG, "AppOpen fetchAndShowAd onAdFailedToLoad: " + loadAdError.getMessage());
                    if (listener != null) {
                        listener.onAdResult();
                    }
                }
            };

            AdRequest request = getAdRequest();
            AppOpenAd.load(myApplication, adKey, request, loadCallback);
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onAdResult();
            }
        }
    }

    private void showAdWithCallback(OnAppOpenAdListener listener) {
        if (!isShowingAd && isAdAvailable()) {
            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    appOpenAd = null;
                    isShowingAd = false;
                    if (listener != null) {
                        listener.onAdResult();
                    }
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                    Log.e(LOG_TAG, "Ad failed to show: " + adError.getMessage());
                    isShowingAd = false;
                    appOpenAd = null;
                    if (listener != null) {
                        listener.onAdResult();
                    }
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    isShowingAd = true;
                    if (listener != null) {
                        listener.onAdShowed();
                    }
                }
            };

            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            appOpenAd.show(currentActivity);
        } else {
            if (listener != null) {
                listener.onAdResult();
            }
        }
    }

    /**
     * Shows the ad if one isn't already showing.
     */
    public void showAdIfAvailable() {
        if (currentActivity instanceof AppSplashActivity || currentActivity instanceof NewPlayerActivity)
            return;

        if (!isShowingAd && isAdAvailable()) {
            Log.d(LOG_TAG, "Will show ad.");
            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    appOpenAd = null;
                    isShowingAd = false;
                    fetchAd(appOpenAdId);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                    Log.e(LOG_TAG, "Ad failed to show: " + adError.getMessage());
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    isShowingAd = true;
                }
            };

            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            appOpenAd.show(currentActivity);
        } else {
            Log.d(LOG_TAG, "Cannot show ad. Fetching...");
            fetchAd(appOpenAdId);
        }
    }

    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    public boolean isAdAvailable() {
        return appOpenAd != null;
    }

    public boolean isShowingAd() {
        return isShowingAd;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        Log.e(LOG_TAG, "onActivityCreated");
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        Log.e(LOG_TAG, "onActivityStarted");
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        Log.e(LOG_TAG, "onActivityResumed");
        currentActivity = activity;
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        Log.e(LOG_TAG, "onActivityStopped");
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
        Log.e(LOG_TAG, "onActivitySaveInstanceState");
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        Log.e(LOG_TAG, "onActivityDestroyed");
        currentActivity = null;
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        if (!checkSniffer(currentActivity)) {
            showAdIfAvailable();
        }
        Log.d(LOG_TAG, "onStart");
    }


    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        Log.e("leoog", "AppOPenManager onActivityPaused");
        Log.e(LOG_TAG, "onActivityPaused");
    }

    public static boolean checkSniffer(Activity currentActivity) {
        if (BuildConfig.DEBUG) return false;
        Log.e("leolog", "AppOPenManager checkSniffer");
        boolean isThreatDetected = false;
        String threatMessage = "";
        String detailMessage = "";

        if (checkVPN(currentActivity)) {
            Log.e("leolog", "AppOPenManager checkSniffer checkVPN");

            if (currentActivity instanceof NewPlayerActivity) {
                Toast.makeText(currentActivity, "VPN Detected", Toast.LENGTH_SHORT).show();
                currentActivity.finishAffinity();
            }
            threatMessage = "VPN Detected";
            detailMessage = "Our app has detected that you're are using some kind of sniffer app in your device. if you want to continue to our app then uninstall it.";
            isThreatDetected = true;
        } else if (isUsingProxy(currentActivity)) {
            Log.e("leolog", "AppOPenManager checkSniffer isUsingProxy");

            if (currentActivity instanceof NewPlayerActivity) {
                Toast.makeText(currentActivity, "Proxy Detected", Toast.LENGTH_SHORT).show();
                currentActivity.finishAffinity();
            }
            threatMessage = "Proxy Detected";
            detailMessage = "Our app has detected that you're are using some kind of sniffer app in your device. if you want to continue to our app then uninstall it.";
            isThreatDetected = true;
        } else if (isPacketCaptureAppInstalled(currentActivity)) {
            Log.e("leolog", "AppOPenManager checkSniffer isPacketCaptureAppInstalled");

            if (currentActivity instanceof NewPlayerActivity) {
                Toast.makeText(currentActivity, "Unauthorized Activities Detected", Toast.LENGTH_SHORT).show();
                currentActivity.finishAffinity();
            }
            threatMessage = "Packet Capture App Detected";
            detailMessage = "Our app has detected that you're are using some kind of sniffer app in your device. if you want to continue to our app then uninstall it.";
            isThreatDetected = true;
        } else if (isRooted()) {
            Log.e("leolog", "AppOPenManager checkSniffer isRooted");

            if (currentActivity instanceof NewPlayerActivity) {
                Toast.makeText(currentActivity, "Rooted Device Detected", Toast.LENGTH_SHORT).show();
                currentActivity.finishAffinity();
            }
            threatMessage = "Rooted Device Detected";
            detailMessage = "Our app has detected that your app is rooted and we don't allow rooted devices to use our app.";
            isThreatDetected = true;
        } else if (hasTunnelingActive(currentActivity)) {
            Log.e("leolog", "AppOPenManager checkSniffer hasTunnelingActive");
            if (currentActivity instanceof NewPlayerActivity) {
                Toast.makeText(currentActivity, "Unauthorized Activities Detected", Toast.LENGTH_SHORT).show();
                currentActivity.finishAffinity();
            }
            threatMessage = "Network Tunneling Detected";
            detailMessage = "Our app has detected that you're are using some kind of sniffer app in your device. if you want to continue to our app then uninstall it.";
            isThreatDetected = true;
        }
        else if (checkDeveloperOP(currentActivity) == 1) {
            if (currentActivity instanceof NewPlayerActivity) {
                Toast.makeText(currentActivity, "Developer Options Enabled", Toast.LENGTH_SHORT).show();
                currentActivity.finishAffinity();
            }
            threatMessage = "Developer Options Enabled";
            detailMessage = "Please disable USB or Wireless debugging from your phone to use the app.";
            isThreatDetected = true;
        } else if (checkWirelessDebugOP(currentActivity) == 1) {
            if (currentActivity instanceof NewPlayerActivity) {
                Toast.makeText(currentActivity, "Wireless Debugging Enabled", Toast.LENGTH_SHORT).show();
                currentActivity.finishAffinity();
            }
            threatMessage = "Wireless Debugging Enabled";
            detailMessage = "Please disable Wireless debugging from your phone to use the app.";
            isThreatDetected = true;
        }

        if (isThreatDetected) {
            String finalThreatMessage = threatMessage;
            String positiveBtn = "Exit";
            if (finalThreatMessage.contains("Developer Options Enabled")) {
                positiveBtn = "Disable";
            }
            Utils.showCustomDialog(currentActivity, threatMessage, detailMessage, positiveBtn, "Cancel", false, false, null,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (finalThreatMessage.contains("Developer Options Enabled")) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                currentActivity.startActivity(intent);
                                System.exit(0);
                            } else {
                                currentActivity.finishAffinity();
                            }
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            currentActivity.finishAffinity();
                        }
                    });
            return true;
        }

        return false;
    }

    private static boolean isUsingProxy(Activity activity) {
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        return proxyHost != null || proxyPort != null;
    }

    private static boolean isPacketCaptureAppInstalled(Context context) {
        // Known Packet Capture / Sniffer Apps
        String[] sniffingApps = {
                "com.guoshi.httpcanary",             // HttpCanary
                "app.greyshirts.sslcapture",         // SSL Capture
                "com.minhui.networkcapture",         // Network Capture
                "com.egorovandreyrm.pcapremote",     // PCAP Remote
                "com.evbadroid.wicapdemo",           // Wicap Demo (NEW)
                "com.evbadroid.wicap",               // Wicap Pro (NEW)
                "com.packagesniffer",                // Packet Sniffer (NEW)
                "jp.co.taosoftware.android.packetcapture", // Packet Capture (NEW)
                "com.emanuelef.remote_capture",      // Remote Capture (NEW)
                "com.pcap.packetcapture",            // Packet Capture App (NEW)
                "com.kpnh.pcap",                     // PCAP (NEW)
                "com.reqable.android",               // Reqable (NEW)
                "com.northghost.touchvpn"            // TouchVPN (Known VPN, used for bypass/sniff) (NEW)
        };

        PackageManager pm = context.getPackageManager();
        for (String packageName : sniffingApps) {
            try {
                pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                return true;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return false;
    }

    private static boolean isRooted() {
        // Check common root binaries
        String[] paths = {
                "/sbin/su", "/system/bin/su", "/system/xbin/su",
                "/data/local/xbin/su", "/data/local/bin/su",
                "/system/sd/xbin/su", "/system/bin/failsafe/su",
                "/data/local/su"
        };

        for (String path : paths) {
            if (new File(path).exists()) return true;
        }

        // Check for Superuser.apk
        String[] superuserPaths = {
                "/system/app/Superuser.apk",
                "/system/app/SuperSU.apk",
                "/system/app/superuser.apk"
        };

        for (String path : superuserPaths) {
            if (new File(path).exists()) return true;
        }

        return false;
    }

    private static boolean hasTunnelingActive(Context context) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (intf.isUp() && intf.getInterfaceAddresses().size() > 0) {
                    // Check for tun/tap interfaces
                    if (intf.getName().startsWith("tun") ||
                            intf.getName().startsWith("ppp") ||
                            intf.getName().startsWith("tap")) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

//    private static boolean checkVPN(Activity currentActivity) {
//        ConnectivityManager cm = (ConnectivityManager) currentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (cm != null) {
//            for (Network network : cm.getAllNetworks()) {
//                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
//                if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
//                    return true; // VPN is active
//                }
//            }
//        }
//        return false; // VPN is not active
//    }

    public static boolean checkVPN(Activity currentActivity) {
        ConnectivityManager cm = (ConnectivityManager) currentActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getNetworkInfo(ConnectivityManager.TYPE_VPN).isConnectedOrConnecting();
    }

    private static int checkDeveloperOP(Activity currentActivity) {
        return Settings.Global.getInt(currentActivity.getContentResolver(), Settings.Global.ADB_ENABLED, 0);
    }

    private static int checkWirelessDebugOP(Activity currentActivity) {
        return Settings.Global.getInt(currentActivity.getContentResolver(), "adb_wifi_enabled", 0);
    }
}