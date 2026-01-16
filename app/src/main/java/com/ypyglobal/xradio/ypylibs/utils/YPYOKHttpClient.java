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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class YPYOKHttpClient {

    private static final int CONNECT_TIME_OUT =10;
    private static final int WRITE_TIME_OUT =10;
    private static final int READ_TIME_OUT =30;

    public static OkHttpClient build(){
        return new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
                .build();
    }

    static Response getResponse(String url){
        try {
            OkHttpClient mOkHttpClient = build();
            Request request = new Request.Builder().url(url).build();
            return mOkHttpClient.newCall(request).execute();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}
