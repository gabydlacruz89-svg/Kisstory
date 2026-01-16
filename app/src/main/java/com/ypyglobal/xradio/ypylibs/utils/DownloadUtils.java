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


import java.io.Reader;

import okhttp3.Response;


/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: www.ypyglobal.com
 */

public class DownloadUtils {

    public static String downloadString(String url) {
        try {
            Response mResponse = YPYOKHttpClient.getResponse(url);
            if (mResponse != null && mResponse.body() != null) {
                return mResponse.body().string();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Reader downloadReader(String url) {
        Response mResponse = YPYOKHttpClient.getResponse(url);
        if (mResponse != null && mResponse.body() != null) {
            return mResponse.body().charStream();
        }
        return null;
    }

}
