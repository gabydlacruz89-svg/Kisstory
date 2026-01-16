package com.ypyglobal.xradio.ypylibs.ads;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.ypyglobal.xradio.ypylibs.task.IYPYCallback;
import com.ypyglobal.xradio.ypylibs.utils.ApplicationUtils;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://dndmix.com
 * Created by YPY Global on 2/22/18.
 */

public class FBAdvertisement extends YPYAdvertisement {

    public static final String FB_ADS = "facebook";

    private AdView fbAdView;
    private InterstitialAd mFBInterstitialAd;
    private InterstitialAd loopInterstitialAd;
    private AdView fbAdMediumView;
    private IYPYCallback loopCallback;
    private boolean isTimeOutAds;

    public FBAdvertisement(Activity mContext, String bannerId, String interstitialId, String testId) {
        super(mContext, bannerId, interstitialId, testId);
        AdSettings.addTestDevice(testId);
    }

    @Override
    public void setUpAdBanner(ViewGroup mLayoutAds, boolean isAllowShowAds) {
        if (isAllowShowAds && ApplicationUtils.isOnline(mContext)
                && mLayoutAds != null && mLayoutAds.getChildCount() == 0 && !TextUtils.isEmpty(bannerId)) {
            if (fbAdView != null) {
                fbAdView.destroy();
            }
            fbAdView = new AdView(mContext, bannerId, AdSize.BANNER_HEIGHT_50);
            mLayoutAds.addView(fbAdView);
            AdListener adListener = new AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {

                }

                @Override
                public void onAdLoaded(Ad ad) {
                    mLayoutAds.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdClicked(Ad ad) {
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                }
            };
            // Request an ad
            AdView.AdViewLoadConfig loadConfigure = fbAdView.buildLoadAdConfig().withAdListener(adListener).build();
            fbAdView.loadAd(loadConfigure);
            mLayoutAds.setVisibility(View.GONE);
            return;
        }
        if (mLayoutAds != null && mLayoutAds.getChildCount() == 0) {
            mLayoutAds.setVisibility(View.GONE);
        }
    }

    @Override
    public void setUpMediumBanner(ViewGroup mLayoutAds, boolean isAllowShowAds) {
        if (isAllowShowAds && ApplicationUtils.isOnline(mContext)
                && mLayoutAds != null && mLayoutAds.getChildCount() == 0 && !TextUtils.isEmpty(mediumId)) {
            if (fbAdMediumView != null) {
                fbAdMediumView.destroy();
            }
            fbAdMediumView = new AdView(mContext, mediumId, AdSize.RECTANGLE_HEIGHT_250);
            mLayoutAds.addView(fbAdMediumView);
            AdListener adListener = new AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {

                }

                @Override
                public void onAdLoaded(Ad ad) {
                    mLayoutAds.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdClicked(Ad ad) {
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                }
            };
            // Request an ad
            AdView.AdViewLoadConfig loadConfigure = fbAdMediumView.buildLoadAdConfig().withAdListener(adListener).build();
            fbAdMediumView.loadAd(loadConfigure);
            mLayoutAds.setVisibility(View.GONE);
            return;
        }
        if (mLayoutAds != null && mLayoutAds.getChildCount() == 0) {
            mLayoutAds.setVisibility(View.GONE);
        }
    }

    @Override
    public void showInterstitialAd(boolean isAllowShowAds, IYPYCallback mCallback) {
        if (ApplicationUtils.isOnline(mContext) && isAllowShowAds && !TextUtils.isEmpty(interstitialId)) {
            mFBInterstitialAd = new InterstitialAd(mContext, interstitialId);
            InterstitialAdListener adListener = new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {

                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    if (mCallback != null) {
                        mCallback.onAction();
                    }
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    Log.e("DCM", "=========>onError=" + adError);
                    mHandlerAds.removeCallbacksAndMessages(null);
                    if (mCallback != null && !isTimeOutAds) {
                        mCallback.onAction();
                    }
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    Log.e("DCM", "=========>onAdLoaded");
                    try {
                        mHandlerAds.removeCallbacksAndMessages(null);
                        if (mFBInterstitialAd != null && !isTimeOutAds) {
                            mFBInterstitialAd.show();
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {
                }
            };
            InterstitialAd.InterstitialLoadAdConfig adConfigure = mFBInterstitialAd.buildLoadAdConfig().withAdListener(adListener).build();
            mFBInterstitialAd.loadAd(adConfigure);
            mHandlerAds.postDelayed(() -> {
                isTimeOutAds = true;
                if (mCallback != null) {
                    mCallback.onAction();
                }
            }, timeOutLoadAds);
            return;
        }
        if (mCallback != null) {
            mCallback.onAction();
        }
    }

    @Override
    public void showLoopInterstitialAd(IYPYCallback mCallback) {
        if (this.loopInterstitialAd != null && this.loopInterstitialAd.isAdLoaded()) {
            this.loopCallback = mCallback;
            this.loopInterstitialAd.show();
            return;
        }
        if (mCallback != null) {
            mCallback.onAction();
        }
    }

    @Override
    public void setUpLoopInterstitial() {
        try {
            if (ApplicationUtils.isOnline(mContext) && !TextUtils.isEmpty(interstitialId)) {
                if (loopInterstitialAd == null) {
                    loopInterstitialAd = new InterstitialAd(mContext, interstitialId);
                }
                InterstitialAdListener adLoopListener = new InterstitialAdListener() {
                    @Override
                    public void onInterstitialDisplayed(Ad ad) {

                    }

                    @Override
                    public void onInterstitialDismissed(Ad ad) {
                        try {
                            if (!isDestroy && loopInterstitialAd != null) {
                                if (loopCallback != null) {
                                    loopCallback.onAction();
                                }
                                setUpLoopInterstitial();
                            }

                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Ad ad, AdError adError) {

                    }

                    @Override
                    public void onAdLoaded(Ad ad) {

                    }

                    @Override
                    public void onAdClicked(Ad ad) {

                    }

                    @Override
                    public void onLoggingImpression(Ad ad) {

                    }
                };
                InterstitialAd.InterstitialLoadAdConfig adConfigure = loopInterstitialAd.buildLoadAdConfig().withAdListener(adLoopListener).build();
                loopInterstitialAd.loadAd(adConfigure);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (loopInterstitialAd != null) {
                loopInterstitialAd.destroy();
            }
            if (mFBInterstitialAd != null) {
                mFBInterstitialAd.destroy();
            }
            if (fbAdView != null) {
                fbAdView.destroy();
            }
            if (fbAdMediumView != null) {
                fbAdMediumView.destroy();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
