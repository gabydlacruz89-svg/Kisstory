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

package com.ypyglobal.xradio.dataMng;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ypyglobal.xradio.constants.IXRadioConstants;
import com.ypyglobal.xradio.ypylibs.model.ResultModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: www.ypyglobal.com
 * @Date:Oct 20, 2017
 */

class JsonParsingUtils implements IXRadioConstants {

    static <T> ResultModel<T> getResultModel(Reader in, Type mDatas) {
        if (in == null) {
            return null;
        }
        try {
            Gson mGson = new GsonBuilder().create();
            return mGson.fromJson(in, mDatas);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                in.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static String parsingImageSong(String data) {
        if (!TextUtils.isEmpty(data)) {
            try {
                JSONObject mJsonObject = new JSONObject(data);
                if (mJsonObject.opt("track") != null) {
                    JSONObject mJsTracks = mJsonObject.getJSONObject("track");
                    if (mJsTracks.opt("album") != null) {
                        JSONObject mJsAlbum = mJsTracks.getJSONObject("album");
                        if (mJsAlbum.opt("image") != null) {
                            JSONArray mJsImgs = mJsAlbum.getJSONArray("image");
                            if (mJsImgs.length() > 0) {
                                for (int j = 0; j < mJsImgs.length(); j++) {
                                    JSONObject mJsImg = mJsImgs.getJSONObject(j);
                                    String sizeImg = mJsImg.getString("size");
                                    if (sizeImg.equalsIgnoreCase("extralarge")) {
                                        return mJsImg.getString("#text");
                                    }
                                }

                            }

                        }
                    }

                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}
