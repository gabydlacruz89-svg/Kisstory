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

package com.ypyglobal.xradio.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ypyglobal.xradio.R;
import com.ypyglobal.xradio.model.ThemeModel;
import com.ypyglobal.xradio.setting.XRadioSettingManager;
import com.ypyglobal.xradio.ypylibs.activity.YPYFragmentActivity;
import com.ypyglobal.xradio.ypylibs.adapter.YPYRecyclerViewAdapter;
import com.ypyglobal.xradio.ypylibs.imageloader.GlideImageLoader;
import com.ypyglobal.xradio.ypylibs.view.MaterialIconView;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import static com.ypyglobal.xradio.constants.IXRadioConstants.UI_CARD_GRID;
import static com.ypyglobal.xradio.constants.IXRadioConstants.UI_CARD_LIST;
import static com.ypyglobal.xradio.constants.IXRadioConstants.UI_FLAT_GRID;
import static com.ypyglobal.xradio.constants.IXRadioConstants.UI_FLAT_LIST;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 * Created by YPY Global on 4/20/18.
 */
public class ThemeAdapter extends YPYRecyclerViewAdapter<ThemeModel> {

    private int mResId;
    private final int mSizeH;
    private final int mTypeUI;
    private final String mUrlHost;

    public ThemeAdapter(Context mContext, ArrayList<ThemeModel> listObjects,String mUrlHost, int sizeH,int typeUI) {
        super(mContext, listObjects);
        this.mSizeH=sizeH;
        this.mTypeUI=typeUI;
        this.mUrlHost=mUrlHost;
        this.mResId =R.layout.item_flat_list_theme;
        if(mTypeUI==UI_FLAT_GRID){
            this.mResId =R.layout.item_flat_grid_theme;
        }
        else if(mTypeUI== UI_CARD_GRID){
            this.mResId =R.layout.item_card_grid_theme;
        }
        else if(mTypeUI==UI_CARD_LIST){
            this.mResId =R.layout.item_card_list_theme;
        }
    }

    @Override
    public void onBindNormalViewHolder(RecyclerView.ViewHolder holder, int position) {
        BackgroundHolder mHolder = (BackgroundHolder) holder;
        final ThemeModel themeModel = mListObjects.get(position);

        boolean b= XRadioSettingManager.getThemId(mContext)==themeModel.getId();
        mHolder.mImgCheck.setVisibility(b ?View.VISIBLE:View.GONE);
        mHolder.mTvName.setText(themeModel.getName());

        String artwork=themeModel.getArtWork(mUrlHost);
        if(!TextUtils.isEmpty(artwork)){
            GlideImageLoader.displayImage(mContext,mHolder.mImgTheme,artwork,R.drawable.default_image);
        }
        else{
            GradientDrawable gradientDrawable = themeModel.getGradientDrawable();
            if (gradientDrawable == null) {
                int startColor=((YPYFragmentActivity)mContext).parseColor(themeModel.getGradStartColor());
                int endColor=((YPYFragmentActivity)mContext).parseColor(themeModel.getGradEndColor());
                gradientDrawable = ((YPYFragmentActivity) mContext).getGradientDrawable(startColor,0,endColor,themeModel.getOrientation());
                themeModel.setGradientDrawable(gradientDrawable);
            }
            mHolder.mImgTheme.setImageDrawable(gradientDrawable);
        }

        if(mHolder.mCardView!=null){
            mHolder.mCardView.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onViewDetail(themeModel);
                }
            });
        }
        else{
            mHolder.mLayoutRoot.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onViewDetail(themeModel);
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateNormalViewHolder(ViewGroup v, int viewType) {
        View mView = mInflater.inflate(mResId, v, false);
        return new BackgroundHolder(mView);
    }

    public class BackgroundHolder extends ViewNormalHolder {

        public MaterialIconView mImgCheck;
        public ImageView mImgTheme;
        public TextView mTvName;
        public RelativeLayout mLayoutRoot;

        @Nullable
        public CardView mCardView;

        BackgroundHolder(View convertView) {
            super(convertView);
        }

        @Override
        public void onFindView(View convertView) {
            mImgCheck = convertView.findViewById(R.id.icon_check);
            mImgTheme = convertView.findViewById(R.id.img_theme);
            mTvName = convertView.findViewById(R.id.tv_name);
            mLayoutRoot = convertView.findViewById(R.id.layout_root);
            mCardView = convertView.findViewById(R.id.card_view);

            if(mSizeH>0 && (mTypeUI== UI_CARD_GRID || mTypeUI==UI_FLAT_GRID)){
                if(mTypeUI==UI_CARD_GRID){
                    CardView.LayoutParams mLayoutParams = (CardView.LayoutParams) mLayoutRoot.getLayoutParams();
                    mLayoutParams.height=mSizeH;
                    mLayoutRoot.setLayoutParams(mLayoutParams);
                }
                else{
                    FrameLayout.LayoutParams mLayoutParams = (FrameLayout.LayoutParams) mLayoutRoot.getLayoutParams();
                    mLayoutParams.height=mSizeH;
                    mLayoutRoot.setLayoutParams(mLayoutParams);
                }
            }

        }

        @Override
        public void onUpdateUIWhenSupportRTL() {
            super.onUpdateUIWhenSupportRTL();
            if (mTypeUI == UI_FLAT_LIST || mTypeUI == UI_CARD_LIST) {
                mTvName.setGravity(Gravity.END);
            }
        }
    }
}
