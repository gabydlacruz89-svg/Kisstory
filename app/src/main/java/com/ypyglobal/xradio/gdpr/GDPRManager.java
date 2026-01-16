package com.ypyglobal.xradio.gdpr;

import android.app.Activity;
import android.os.Handler;
import android.text.TextUtils;

import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.ypyglobal.xradio.R;
import com.ypyglobal.xradio.ypylibs.task.IYPYCallback;
import com.ypyglobal.xradio.ypylibs.utils.ApplicationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GDPRManager {

    private static GDPRManager mInstance;
    private GDPRModel gdprModel;
    private boolean isGoToFirstTime;
    private boolean showConsentDialog;

    private ConsentInformation consentInformation;

    public static GDPRManager getInstance() {
        if (mInstance == null) {
            mInstance = new GDPRManager();
        }
        return mInstance;
    }

    private GDPRManager() {

    }

    public void init(@NonNull String appId, @NonNull String testId) {
        if (gdprModel == null && !TextUtils.isEmpty(appId)) {
            gdprModel = new GDPRModel(appId, testId);
        }
    }

    public void onDestroy() {
        isGoToFirstTime = false;
        gdprModel = null;
        mInstance = null;
    }

    public void startCheck(@NonNull Activity mContext, @Nullable Handler mHandler, IYPYCallback mCallback) {
        try {
            if (!ApplicationUtils.isOnline(mContext) || gdprModel == null) {
                showConsentDialog = false;
                if (mCallback != null) {
                    mCallback.onAction();
                }
                return;
            }
            showConsentDialog = false;
            // Set tag for underage of consent. false means users are not underage.
            ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(mContext)
                    .setDebugGeography(ConsentDebugSettings
                            .DebugGeography
                            .DEBUG_GEOGRAPHY_EEA)
                    .addTestDeviceHashedId(gdprModel.getTestId())
                    .build();

            ConsentRequestParameters params = new ConsentRequestParameters
                    .Builder()
                    .setAdMobAppId(mContext.getString(R.string.admob_app_id))
                    .setConsentDebugSettings(debugSettings)
                    .setTagForUnderAgeOfConsent(false)
                    .build();
            consentInformation = UserMessagingPlatform.getConsentInformation(mContext);
            consentInformation.requestConsentInfoUpdate(
                    mContext,
                    params,
                    () -> {
                        // The consent information state was updated.
                        // You are now ready to check if a form is available.
                        if (mHandler != null) {
                            mHandler.removeCallbacksAndMessages(null);
                        }
                        if (!isGoToFirstTime && consentInformation != null && consentInformation.isConsentFormAvailable()) {
                            showConsentDialog = true;
                            loadConsentForm(mContext, mCallback);
                            return;
                        }
                        if (mCallback != null && !isGoToFirstTime) {
                            mCallback.onAction();
                        }
                    },
                    formError -> {
                        if (mHandler != null) {
                            mHandler.removeCallbacksAndMessages(null);
                        }
                        if (mCallback != null && !isGoToFirstTime) {
                            mCallback.onAction();
                        }
                    });
            int consentStatus = consentInformation != null ? consentInformation.getConsentStatus() : ConsentInformation.ConsentStatus.UNKNOWN;
            if (consentStatus != ConsentInformation.ConsentStatus.UNKNOWN) {
                isGoToFirstTime = true;
                if (mCallback != null) {
                    mCallback.onAction();
                }
                return;
            }
            if (mHandler != null) {
                mHandler.postDelayed(() -> {
                    if (!showConsentDialog) {
                        mHandler.removeCallbacksAndMessages(null);
                        if (mCallback != null) {
                            mCallback.onAction();
                        }
                    }
                }, 10000);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void loadConsentForm(@NonNull Activity mContext, IYPYCallback mCallback) {
        boolean isConsentFormEnable = consentInformation != null && consentInformation.isConsentFormAvailable();
        if (isConsentFormEnable) {
            UserMessagingPlatform.loadConsentForm(
                    mContext,
                    consentForm -> showConsentDialog(mContext, consentForm, mCallback),
                    formError -> {
                        // Handle the error
                        if (mCallback != null) {
                            mCallback.onAction();
                        }
                    }
            );
        }

    }

    private void showConsentDialog(@NonNull Activity mContext, @Nullable ConsentForm consentForm, IYPYCallback mCallback) {
        try {
            if (consentForm != null && consentInformation != null) {
                consentForm.show(
                        mContext,
                        formError -> {
                            // Handle dismissal by reloading form.
                            if (mCallback != null) {
                                mCallback.onAction();
                            }
                        });
                return;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (mCallback != null) {
            mCallback.onAction();
        }
    }


}
