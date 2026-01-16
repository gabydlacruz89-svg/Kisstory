package com.ypyglobal.xradio.ypylibs.ads;

import android.app.Activity;
import android.os.Handler;
import android.view.ViewGroup;

import com.ypyglobal.xradio.ypylibs.task.IYPYCallback;


/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 * Created by YPY Global on 2/22/18.
 */

public abstract class YPYAdvertisement {

    public static final long DEFAULT_TIME_OUT_LOAD_ADS = 15000;

    public Activity mContext;

    String testId;
    String bannerId;
    String mediumId;
    String interstitialId;

    Handler mHandlerAds = new Handler();
    long timeOutLoadAds;
    boolean isDestroy;


    YPYAdvertisement(Activity mContext,
                     String bannerId, String interstitialId, String testId) {
        this(mContext,bannerId,interstitialId,testId,DEFAULT_TIME_OUT_LOAD_ADS);
    }

    YPYAdvertisement(Activity mContext,String bannerId,
                     String interstitialId, String testId, long timeOutLoadAds) {
        this.mContext = mContext;
        this.timeOutLoadAds = timeOutLoadAds;
        this.bannerId = bannerId;
        this.interstitialId = interstitialId;
        this.testId = testId;
    }

    public abstract void setUpAdBanner(ViewGroup mLayoutAds, boolean isAllowShowAds);
    public abstract void setUpMediumBanner(ViewGroup mLayoutAds, boolean isAllowShowAds);
    public abstract void showInterstitialAd(boolean isAllowShowAds,IYPYCallback mCallback);
    public abstract void showLoopInterstitialAd(IYPYCallback mCallback);
    public abstract void setUpLoopInterstitial();

    public void onDestroy() {
        isDestroy = true;
        mHandlerAds.removeCallbacksAndMessages(null);
    }

}
