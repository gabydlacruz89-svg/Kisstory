package com.ypyglobal.xradio.ads;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.ypyglobal.xradio.R;

/**
 * Policy-safe helper for Rewarded Ads.
 * Purpose: "Support the radio" (optional, user-initiated).
 * Effect: Temporarily hide banner ads (cooldown-based).
 */
public class RewardedAdsHelper {

    // ====== CONFIG ======
    // Cooldown duration after a successful reward (30 minutes)
    private static final long COOLDOWN_MS = 30 * 60 * 1000L;

    // SharedPreferences
    private static final String PREF_ADS = "ads_prefs";
    private static final String KEY_REWARDED_COOLDOWN_UNTIL = "rewarded_cooldown_until";

    private final Activity activity;
    private final SharedPreferences prefs;

    private RewardedAd rewardedAd;
    private boolean isLoading = false;

    public RewardedAdsHelper(@NonNull Activity activity) {
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(PREF_ADS, Context.MODE_PRIVATE);
    }

    /**
     * Call early (e.g., onDoWhenDone / onCreate) to preload.
     */
    public void loadRewardedAd() {
        if (isLoading || rewardedAd != null) return;

        isLoading = true;

        AdRequest adRequest = new AdRequest.Builder().build();
        String adUnitId = activity.getString(R.string.rewarded_id);

        RewardedAd.load(
                activity,
                adUnitId,
                adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        isLoading = false;

                        rewardedAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        rewardedAd = null;
                                        // Preload next
                                        loadRewardedAd();
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                        rewardedAd = null;
                                        loadRewardedAd();
                                    }
                                }
                        );
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        rewardedAd = null;
                        isLoading = false;
                    }
                }
        );
    }

    /**
     * True if reward cooldown is active (i.e., banners should be hidden).
     */
    public boolean isRewardActive() {
        long until = prefs.getLong(KEY_REWARDED_COOLDOWN_UNTIL, 0L);
        return System.currentTimeMillis() < until;
    }

    /**
     * Show rewarded ad (user-initiated).
     *
     * @param bannerContainer View that holds banner ads (will be hidden on reward).
     * @param onRewardGranted Runnable executed after reward is earned.
     */
    public void showRewardedAd(@NonNull View bannerContainer, @NonNull Runnable onRewardGranted) {
        // If already supporting, do nothing
        if (isRewardActive()) return;

        if (rewardedAd == null) {
            // Try to load and exit gracefully
            loadRewardedAd();
            return;
        }

        rewardedAd.show(activity, rewardItem -> {
            // Save cooldown
            long until = System.currentTimeMillis() + COOLDOWN_MS;
            prefs.edit().putLong(KEY_REWARDED_COOLDOWN_UNTIL, until).apply();

            // Hide banners during cooldown
            bannerContainer.setVisibility(View.GONE);

            // Callback to Activity (toast, UI feedback)
            onRewardGranted.run();
        });
    }
}
