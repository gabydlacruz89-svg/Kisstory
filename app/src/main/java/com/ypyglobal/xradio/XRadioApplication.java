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

import android.util.Log;

import com.facebook.ads.AudienceNetworkAds;
import com.ypyglobal.xradio.constants.IXRadioConstants;

import androidx.multidex.MultiDexApplication;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 * Created by YPY Global on 10/19/17.
 */
public class XRadioApplication extends MultiDexApplication implements IXRadioConstants {

    @Override
    public void onCreate() {
        super.onCreate();
        if (!AudienceNetworkAds.isInitialized(this) && ALLOW_FB_ADS) {
            AudienceNetworkAds
                    .buildInitSettings(this)
                    .withInitListener(initResult -> {
                        Log.e(TAG, "=======>isInitialized=" + initResult);
                    })
                    .initialize();
        }
    }

}
