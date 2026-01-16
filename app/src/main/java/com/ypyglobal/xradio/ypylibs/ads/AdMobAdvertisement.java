package com.ypyglobal.xradio.ypylibs.ads;

import android.app.Activity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.ypyglobal.xradio.ypylibs.task.IYPYCallback;
import com.ypyglobal.xradio.ypylibs.utils.ApplicationUtils;
import com.ypyglobal.xradio.ypylibs.utils.YPYLog;

import java.util.Collections;

import androidx.annotation.NonNull;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 * Created by YPY Global on 2/22/18.
 */

public class AdMobAdvertisement extends YPYAdvertisement {

    public static final String ADMOB_ADS = "admob";

    private AdView adView;
    private InterstitialAd loopInterstitialAd;
    private AdView adMediumView;
    private boolean isTimeOutAds;


    public AdMobAdvertisement(Activity mContext, String bannerId, String interstitialId, String testId) {
        super(mContext, bannerId, interstitialId, testId);
    }

    public void initAds() {
        MobileAds.initialize(mContext, initializationStatus -> {
            YPYLog.e("DCM", "======>initializationStatus admob=" + initializationStatus);
        });
        if (!TextUtils.isEmpty(testId)) {
            RequestConfiguration.Builder mRequestBuilder = new RequestConfiguration.Builder();
            if (testId != null) {
                mRequestBuilder.setTestDeviceIds(Collections.singletonList(testId));
            }
            MobileAds.setRequestConfiguration(mRequestBuilder.build());
        }
    }

    @Override
    public void setUpAdBanner(ViewGroup mLayoutAds, boolean isAllowShowAds) {
        if (isAllowShowAds && ApplicationUtils.isOnline(mContext) && !TextUtils.isEmpty(bannerId) && mLayoutAds != null && mLayoutAds.getChildCount() == 0) {
            if (adView != null) {
                adView.destroy();
            }
            adView = new AdView(mContext);
            adView.setAdUnitId(bannerId);

            AdSize mAdSize = getAdSize();
            adView.setAdSize((mAdSize != null && mAdSize != AdSize.INVALID) ? mAdSize : AdSize.BANNER);

            mLayoutAds.addView(adView);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mLayoutAds.setVisibility(View.VISIBLE);

                }
            });
            AdRequest adRequest = buildAdRequest();
            if (adRequest != null) {
                adView.loadAd(adRequest);
            }
            mLayoutAds.setVisibility(View.GONE);
            return;

        }
        if (mLayoutAds != null && mLayoutAds.getChildCount() == 0) {
            mLayoutAds.setVisibility(View.GONE);
        }
    }

    @Override
    public void setUpMediumBanner(ViewGroup mLayoutAds, boolean isAllowShowAds) {
        if (isAllowShowAds && !TextUtils.isEmpty(mediumId) && ApplicationUtils.isOnline(mContext) && mLayoutAds != null && mLayoutAds.getChildCount() == 0) {
            if (adMediumView != null) {
                adMediumView.destroy();
            }
            adMediumView = new AdView(mContext);
            adMediumView.setAdUnitId(mediumId);
            adMediumView.setAdSize(AdSize.MEDIUM_RECTANGLE);
            mLayoutAds.addView(adMediumView);
            adMediumView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mLayoutAds.setVisibility(View.VISIBLE);

                }
            });
            AdRequest adRequest = buildAdRequest();
            if (adRequest != null) {
                adMediumView.loadAd(adRequest);
            }
            mLayoutAds.setVisibility(View.GONE);
            return;
        }
        if (mLayoutAds != null && mLayoutAds.getChildCount() == 0) {
            mLayoutAds.setVisibility(View.GONE);
        }
    }

    @Override
    public void showInterstitialAd(boolean isAllowShowAds, IYPYCallback mCallback) {
        if (ApplicationUtils.isOnline(mContext) && isAllowShowAds) {
            AdRequest adRequest = buildAdRequest();
            if (adRequest != null) {
                InterstitialAd.load(mContext, interstitialId, adRequest, new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        super.onAdLoaded(interstitialAd);
                        mHandlerAds.removeCallbacksAndMessages(null);
                        try {
                            if (!isTimeOutAds) {
                                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent();
                                        if (mCallback != null) {
                                            mCallback.onAction();
                                        }
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                        super.onAdFailedToShowFullScreenContent(adError);
                                        YPYLog.e("DCM", "========>onAdFailedToShowFullScreenContent=" + adError);
                                        if (mCallback != null) {
                                            mCallback.onAction();
                                        }
                                    }
                                });
                                interstitialAd.show(mContext);
                            }

                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        YPYLog.e("DCM", "========>onAdFailedToLoad=" + loadAdError);
                        mHandlerAds.removeCallbacksAndMessages(null);
                        if (!isTimeOutAds) {
                            if (mCallback != null) {
                                mCallback.onAction();
                            }
                        }

                    }
                });
                mHandlerAds.postDelayed(() -> {
                    isTimeOutAds = true;
                    if (mCallback != null) {
                        mCallback.onAction();
                    }
                }, timeOutLoadAds);
                return;
            }
        }
        if (mCallback != null) {
            mCallback.onAction();
        }
    }

    @Override
    public void showLoopInterstitialAd(IYPYCallback mCallback) {
        if (this.loopInterstitialAd != null) {
            this.loopInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    AdMobAdvertisement.this.loopInterstitialAd = null;
                    if (mCallback != null) {
                        mCallback.onAction();
                    }
                    if (!isDestroy) {
                        setUpLoopInterstitial();
                    }
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    YPYLog.e("DCM", "========>showLoopInterstitialAd onAdFailedToShowFullScreenContent=" + adError);
                    AdMobAdvertisement.this.loopInterstitialAd = null;
                    if (mCallback != null) {
                        mCallback.onAction();
                    }
                }
            });
            this.loopInterstitialAd.show(mContext);
            return;
        }
        if (mCallback != null) {
            mCallback.onAction();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (adView != null) {
                adView.destroy();
            }
            if (adMediumView != null) {
                adMediumView.destroy();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setUpLoopInterstitial() {
        try {
            if (ApplicationUtils.isOnline(mContext) && loopInterstitialAd == null && !TextUtils.isEmpty(interstitialId)) {
                AdRequest adRequest = buildAdRequest();
                if (adRequest != null) {
                    InterstitialAd.load(mContext, interstitialId, adRequest, new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            super.onAdLoaded(interstitialAd);
                            AdMobAdvertisement.this.loopInterstitialAd = interstitialAd;
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            YPYLog.e("DCM", "========>setUpLoopInterstitial onAdFailedToLoad=" + loadAdError);
                        }
                    });
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AdSize getAdSize() {
        try {
            // Step 2 - Determine the screen width (less decorations) to use for the ad width.
            Display display = mContext.getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);

            float widthPixels = outMetrics.widthPixels;
            float density = outMetrics.density;

            int adWidth = (int) (widthPixels / density);
            // Step 3 - Get adaptive ad size and return for setting on the ad view.
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mContext, adWidth);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public AdRequest buildAdRequest() {
        try {
            AdRequest.Builder mBuilder = new AdRequest.Builder();
            return mBuilder.build();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
