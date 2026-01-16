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

package com.ypyglobal.xradio.ypylibs.utils;

import android.os.Build;

import java.io.File;

import okio.BufferedSink;
import okio.Okio;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: www.ypyglobal.com
 */
public class IOUtils {
    private final static String TAG = IOUtils.class.getSimpleName();

    public static void writeString(String mDirectory, String mNameFile, String mStrData) {
        if (mDirectory == null || mNameFile == null || mStrData == null) {
            new Exception(TAG + ": Some content can not null").printStackTrace();
            return;
        }
        File mFile = new File(mDirectory);
        if ((!mFile.exists())) {
            mFile.mkdirs();
        }
        try {
            File newTextFile = new File(mDirectory, mNameFile);
            BufferedSink sink = Okio.buffer(Okio.sink(newTextFile));
            sink.writeUtf8(mStrData);
            sink.close();
        }
        catch (Exception iox) {
            iox.printStackTrace();
        }
    }

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean hasAndroid80() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean hasAndroid10() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }
    public static boolean isAndroid14() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
    }
}
