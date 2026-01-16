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

package com.ypyglobal.xradio.ypylibs.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

import java.io.File;


import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;


/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: www.ypyglobal.com
 */

public class GlideImageLoader {

    private static final int DEFAULT_ANIM_TIME = 200;
    private static final String HTTP_PREFIX = "http";
    private static final String PREFIX_ASSETS = "assets://";
    private static final String PREFIX_NEW_ASSETS = "file:///android_asset/";

    public static void displayImage(Context mContext, ImageView mImageView, String artwork, int resId) {
        displayImage(mContext, mImageView, artwork, null, resId);
    }

    public static void displayImage(Context mContext, ImageView mImageView, String artwork,
                                    Transformation<Bitmap> mTransform, int resId) {
        if (!TextUtils.isEmpty(artwork)) {
            if (artwork.startsWith(PREFIX_ASSETS)) {
                artwork = artwork.replace(PREFIX_ASSETS, PREFIX_NEW_ASSETS);
            }
            Uri mUri;
            if (artwork.startsWith(HTTP_PREFIX)) {
                mUri = Uri.parse(artwork);
            }
            else {
                File mFile = new File(artwork);
                if (mFile.exists() && mFile.isFile()) {
                    mUri = Uri.fromFile(mFile);
                }
                else {
                    mUri = Uri.parse(artwork);
                }
            }
            if (mUri != null) {
                DrawableCrossFadeFactory factory =
                        new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
                RequestOptions options = new RequestOptions()
                        .centerCrop()
                        .placeholder(resId)
                        .priority(Priority.HIGH);
                if (mTransform != null) {
                    options.transform(mTransform);
                }
                Glide.with(mContext).load(mUri).apply(options).transition(withCrossFade(factory)).into(mImageView);
            }

        }
    }

    public static void displayImage(Context mContext, ImageView mImageView, int resId) {
        displayImage(mContext, mImageView, resId, null);
    }

    public static void displayImage(Context mContext, ImageView mImageView, int resId,
                                    Transformation<Bitmap> mTransform) {
        if (resId != 0) {
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .priority(Priority.HIGH);
            if (mTransform != null) {
                options.transform(mTransform);
            }
            Glide.with(mContext).load(resId).apply(options).transition(withCrossFade(DEFAULT_ANIM_TIME)).into(mImageView);
        }
    }


}
