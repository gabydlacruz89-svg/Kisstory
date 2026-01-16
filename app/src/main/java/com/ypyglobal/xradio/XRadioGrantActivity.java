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
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;

import com.ypyglobal.xradio.constants.IXRadioConstants;
import com.ypyglobal.xradio.dataMng.TotalDataManager;
import com.ypyglobal.xradio.databinding.ActivityGrantPermissionBinding;
import com.ypyglobal.xradio.setting.XRadioSettingManager;
import com.ypyglobal.xradio.ypylibs.activity.YPYSplashActivity;
import com.ypyglobal.xradio.ypylibs.executor.YPYExecutorSupplier;
import com.ypyglobal.xradio.ypylibs.utils.IOUtils;
import com.ypyglobal.xradio.ypylibs.utils.ShareActionUtils;

import java.io.File;


/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: www.ypyglobal.com
 * @Date:Oct 20, 2017
 */

public class XRadioGrantActivity extends YPYSplashActivity<ActivityGrantPermissionBinding> implements IXRadioConstants, View.OnClickListener {

    private TotalDataManager mTotalMng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isNeedCheckGoogleService = false;
        super.onCreate(savedInstanceState);
        setUpOverlayBackground(true);

        mTotalMng = TotalDataManager.getInstance(getApplicationContext());

        int resId = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? R.string.format_request_permission_13 : R.string.format_request_permission;
        String data = String.format(getString(resId), getString(R.string.app_name));
        this.viewBinding.tvInfo.setText(Html.fromHtml(data));
        this.viewBinding.tvPolicy.setOnClickListener(this);
        this.viewBinding.tvTos.setOnClickListener(this);
        this.viewBinding.btnAllow.setOnClickListener(this);
        this.viewBinding.btnSkip.setOnClickListener(this);

    }

    @Override
    public void onInitData() {
        startCheckData();
    }

    @Override
    public File getDirectoryCached() {
        return mTotalMng.getDirectoryCached(getApplicationContext());
    }

    @Override
    public String[] getListPermissionNeedGrant() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return LIST_PERMISSIONS_13;
        }
        if (SAVE_FAVORITE_SDCARD && !IOUtils.hasAndroid10()) {
            return LIST_PERMISSIONS;
        }
        return null;
    }

    @Override
    protected ActivityGrantPermissionBinding getViewBinding() {
        return ActivityGrantPermissionBinding.inflate(getLayoutInflater());
    }


    private void startCheckData() {
        YPYExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            mTotalMng.readConfigure(this);
            mTotalMng.readAllCache();
            runOnUiThread(this::goToMainActivity);
        });
    }


    public void goToMainActivity() {
        boolean isSingleRadio = mTotalMng.isSingleRadio();
        Intent mIntent = new Intent(this, isSingleRadio ? XSingleRadioMainActivity.class : XMultiRadioMainActivity.class);
        startActivity(mIntent);
        finish();

    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tv_policy) {
            ShareActionUtils.goToUrl(this, URL_PRIVACY_POLICY);
        }
        else if (id == R.id.tv_tos) {
            ShareActionUtils.goToUrl(this, URL_TERM_OF_USE);
        }
        else if (id == R.id.btn_allow) {
            startGrantPermission();
        }
        else if (id == R.id.btn_skip) {
            XRadioSettingManager.setSkipPermission(this,true);
            startCheckData();
        }
    }

    @Override
    public void onPermissionDenied() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backToHome();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onUpdateUIWhenSupportRTL() {
        super.onUpdateUIWhenSupportRTL();
        this.viewBinding.tvInfo.setGravity(Gravity.END);
    }
}
