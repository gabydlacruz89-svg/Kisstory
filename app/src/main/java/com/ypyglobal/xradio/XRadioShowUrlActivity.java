/*
 * Copyright (c) 2017. YPY Global - All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *         http://ypyglobal.com/sourcecode/policy
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ypyglobal.xradio;

import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ypyglobal.xradio.databinding.ActivityShowUrlBinding;
import com.ypyglobal.xradio.ypylibs.utils.ApplicationUtils;
import com.ypyglobal.xradio.ypylibs.utils.YPYLog;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 * Created by YPY Global on 10/19/17.
 */
public class XRadioShowUrlActivity extends XRadioFragmentActivity<ActivityShowUrlBinding> {

    public static final String KEY_HEADER = "KEY_HEADER";
    public static final String KEY_SHOW_URL = "KEY_SHOW_URL";
    public static final String KEY_SHOW_ADS = "KEY_SHOW_ADS";

    private String mUrl;
    private String mNameHeader;

    private boolean isShowAds;

    @Override
    protected void onDoBeforeSetView() {
        super.onDoBeforeSetView();
    }

    @Override
    protected ActivityShowUrlBinding getViewBinding() {
        return ActivityShowUrlBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void updateBackground() {
        setUpBackground(viewBinding.layoutBg);
    }

    @Override
    public void onDoWhenDone() {
        Intent args = getIntent();
        if (args != null) {
            mUrl = args.getStringExtra(KEY_SHOW_URL);
            mNameHeader = args.getStringExtra(KEY_HEADER);
            isShowAds = args.getBooleanExtra(KEY_SHOW_ADS,true);
            YPYLog.d("DCM", "===========>url=" + mUrl);
        }
        if(TextUtils.isEmpty(mUrl)){
            backToHome();
            return;
        }
        super.onDoWhenDone();
        setUpCustomizeActionBar(Color.TRANSPARENT,true);
        if (!TextUtils.isEmpty(mNameHeader)) {
            setActionBarTitle(mNameHeader);
        }
        viewBinding.webview.getSettings().setJavaScriptEnabled(true);
        viewBinding.webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                viewBinding.progressBar.setVisibility(View.GONE);
            }
        });

        if(ApplicationUtils.isOnline(this)){
            if(!mUrl.startsWith("http")){
                mUrl = "http://"+mUrl;
            }
            viewBinding.webview.loadUrl(mUrl);
        }
    }
    @Override
    public void setUpLayoutBanner() {
        if(isShowAds){
            super.setUpLayoutBanner();
        }
        else{
            mLayoutAds=findViewById(R.id.layout_ads);
            if(mLayoutAds!=null){
                mLayoutAds.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDoWhenNetworkOn() {
        super.onDoWhenNetworkOn();
        viewBinding.webview.loadUrl(mUrl);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        viewBinding.webview.destroy();
    }

    @Override
    public boolean backToHome() {
        finish();
        return true;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (viewBinding.webview.canGoBack()) {
                viewBinding.webview.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
