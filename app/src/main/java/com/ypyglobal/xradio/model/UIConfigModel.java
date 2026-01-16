/*
 * Copyright (c) 2018. YPY Global - All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *         http://ypyglobal.com/sourcecode/policy
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ypyglobal.xradio.model;

import com.google.gson.annotations.SerializedName;
import com.ypyglobal.xradio.dataMng.XRadioNetUtils;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 * Created by YPY Global on 4/27/18.
 */
public class UIConfigModel {

    @SerializedName("is_full_bg")
    private int isFullBg;

    @SerializedName("ui_top_chart")
    private int uiTopChart;

    @SerializedName("ui_genre")
    private int uiGenre;

    @SerializedName("ui_favorite")
    private int uiFavorite;

    @SerializedName("ui_themes")
    private int uiThemes;

    @SerializedName("ui_detail_genre")
    private int uiDetailGenre;

    @SerializedName("ui_player")
    private int uiPlayer;

    @SerializedName("ui_search")
    private int uiSearch;

    @SerializedName("app_type")
    private int appType;

    public int getIsFullBg() {
        return isFullBg;
    }


    public int getUiTopChart() {
        return uiTopChart;
    }


    public int getUiGenre() {
        return uiGenre;
    }


    public int getUiFavorite() {
        return uiFavorite;
    }


    public int getUiThemes() {
        return uiThemes;
    }


    public int getUiDetailGenre() {
        return uiDetailGenre;
    }


    public int getUiPlayer() {
        return uiPlayer;
    }


    public int getUiSearch() {
        return uiSearch;
    }


    public boolean isMultiApp(){
        return appType == XRadioNetUtils.TYPE_APP_MULTI;
    }
}
