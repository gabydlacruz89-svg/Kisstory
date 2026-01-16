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

import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.ypyglobal.xradio.dataMng.XRadioNetUtils;
import com.ypyglobal.xradio.ypylibs.model.AbstractModel;
import com.ypyglobal.xradio.ypylibs.utils.ApplicationUtils;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 * Created by YPY Global on 4/20/18.
 */
public class ThemeModel extends AbstractModel {

    @SerializedName("grad_start_color")
    private String gradStartColor;

    @SerializedName("grad_end_color")
    private String gradEndColor;

    @SerializedName("grad_orientation")
    private int gradOrientation;

    private transient GradientDrawable.Orientation orientation;

    private transient GradientDrawable gradientDrawable;

    public ThemeModel(long id, String name, String image) {
        super(id, name, image);
    }

    public String getGradStartColor() {
        return gradStartColor;
    }


    public String getGradEndColor() {
        return gradEndColor;
    }


    public int getGradOrientation() {
        return gradOrientation;
    }


    public GradientDrawable.Orientation getOrientation() {
        if (orientation == null) {
            orientation = ApplicationUtils.getOrientation(gradOrientation);
        }
        return orientation;
    }

    public void setOrientation(GradientDrawable.Orientation orientation) {
        this.orientation = orientation;
    }

    @Override
    public String getArtWork(String urlHost) {
        if (!TextUtils.isEmpty(image) && !image.startsWith("http") && !TextUtils.isEmpty(urlHost)) {
            image = urlHost + XRadioNetUtils.FOLDER_THEMES + image;
        }
        return super.getImage();
    }


    public GradientDrawable getGradientDrawable() {
        return gradientDrawable;
    }

    public void setGradientDrawable(GradientDrawable gradientDrawable) {
        this.gradientDrawable = gradientDrawable;
    }

}
