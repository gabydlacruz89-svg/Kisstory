package com.ypyglobal.xradio.ypylibs.ads;

import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.ypyglobal.xradio.ypylibs.activity.YPYFragmentActivity;
import com.ypyglobal.xradio.ypylibs.task.IYPYCallback;
import com.ypyglobal.xradio.ypylibs.utils.YPYLog;

import androidx.annotation.NonNull;

public class AppOpenAdsManager {

    public static final String OPEN_ADS_TAG = "open_ads";

    private static final long SLASH_OPEN_ADS_TIMEOUT_IN_SECONDS = 10000L; //15 seconds
    private static final long IN_APP_OPEN_ADS_TIMEOUT_IN_HOURS = 4L; //4 hours

    private final YPYFragmentActivity activity;
    private final String openAdsId;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private AppOpenAd appOpenAd = null;
    private boolean isTimeOutAds = false;
    private long loadTime = 0L;

    public AppOpenAdsManager(@NonNull YPYFragmentActivity activity, @NonNull String openAdsId) {
        this.activity = activity;
        this.openAdsId = openAdsId;
    }

    public void showOpenAdsInSplash(IYPYCallback callback) {
        if (appOpenAd != null || isTimeOutAds) {
            if (callback != null) {
                callback.onAction();
            }
            return;
        }
        AppOpenAd.AppOpenAdLoadCallback loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                super.onAdLoaded(appOpenAd);
                YPYLog.e(OPEN_ADS_TAG, "=======>onAppOpenAdLoaded");
                try {
                    handler.removeCallbacksAndMessages(null);
                    AppOpenAdsManager.this.loadTime = System.currentTimeMillis();
                    AppOpenAdsManager.this.appOpenAd = appOpenAd;
                    if (!isTimeOutAds) {
                        showAdIfAvailable(callback);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                YPYLog.e(OPEN_ADS_TAG, "=======>Open Ads AdError=" + loadAdError.getMessage());
                handler.removeCallbacksAndMessages(null);
                if (!isTimeOutAds && callback != null) {
                    callback.onAction();
                }
            }
        };
        int openAdsType = getOpenAdsType();
        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(activity, openAdsId, request, openAdsType, loadCallback);
        handler.postDelayed(() -> {
            isTimeOutAds = true;
            if (callback != null) {
                callback.onAction();
            }
        }, SLASH_OPEN_ADS_TIMEOUT_IN_SECONDS);
    }

    public void showAdIfAvailable(IYPYCallback callback) {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isAdAvailable()) return;
        if (appOpenAd != null) {
            appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    // Set the reference to null so isAdAvailable() returns false.
                    YPYLog.e(OPEN_ADS_TAG, "=======>onAdDismissedFullScreenContent");
                    handler.removeCallbacksAndMessages(null);
                    AppOpenAdsManager.this.appOpenAd = null;
                    if (callback != null) {
                        callback.onAction();
                    }
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    YPYLog.e(OPEN_ADS_TAG, "=======>Open Ads AdError =" + adError.getMessage());
                    handler.removeCallbacksAndMessages(null);
                    AppOpenAdsManager.this.appOpenAd = null;
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    YPYLog.e(OPEN_ADS_TAG, "=======>onAdShowedFullScreenContent");
                }
            });
            appOpenAd.show(this.activity);
            return;
        }
        if (callback != null) {
            callback.onAction();
        }
    }

    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        isTimeOutAds = true;
        appOpenAd = null;
    }

    private int getOpenAdsType() {
        try {
            // Step 2 - Determine the screen width (less decorations) to use for the ad width.
            Display display = activity.getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);
            int widthPixels = outMetrics.widthPixels;
            int heightPixels = outMetrics.heightPixels;
            boolean isLandscape = widthPixels > heightPixels;
            if (isLandscape) {
                return AppOpenAd.APP_OPEN_AD_ORIENTATION_LANDSCAPE;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT;
    }

    /**
     * Utility method to check if ad was loaded more than n hours ago.
     */
    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = System.currentTimeMillis() - this.loadTime;
        long numMilliSecondsPerHour = 3600000L;
        return dateDifference < numMilliSecondsPerHour * numHours;
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    private boolean isAdAvailable() {
        return !isTimeOutAds && appOpenAd != null && wasLoadTimeLessThanNHoursAgo(IN_APP_OPEN_ADS_TIMEOUT_IN_HOURS);
    }

}
