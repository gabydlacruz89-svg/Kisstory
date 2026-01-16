package com.ypyglobal.xradio.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 * Created by YPY Global on 1/4/18.
 */

public class ConfigureModel {

    @SerializedName("api_key")
    private String apiKey;

    @SerializedName("lastfm_api_key")
    private String lastFmApiKey;

    @SerializedName("cache_expiration")
    private int cacheExpiration;

    @SerializedName("url_endpoint")
    private String urlEndPoint;

    public String getApiKey() {
        return apiKey;
    }

    public String getUrlEndPoint() {
        return urlEndPoint;
    }

    public boolean isOnlineApp(){
        return !TextUtils.isEmpty(urlEndPoint);
    }

    public String getLastFmApiKey() {
        return lastFmApiKey;
    }

    public int getCacheExpiration() {
        return cacheExpiration;
    }

}
