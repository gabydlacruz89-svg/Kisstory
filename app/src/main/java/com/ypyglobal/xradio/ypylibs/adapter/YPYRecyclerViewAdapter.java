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

package com.ypyglobal.xradio.ypylibs.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ypyglobal.xradio.ypylibs.utils.ApplicationUtils;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: www.ypyglobal.com
 * @Date:Oct 20, 2017
 */

public abstract class YPYRecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_HEADER_VIEW = -1;
    protected LayoutInflater mInflater;
    private final View mHeaderView;

    public Context mContext;
    protected ArrayList<T> mListObjects;
    private final boolean isHasHeader;
    private final boolean isSupportRTL;

    public OnItemClickListener<T> listener;

    public YPYRecyclerViewAdapter(Context mContext, ArrayList<T> listObjects) {
        this(mContext, listObjects, null);
    }

    public YPYRecyclerViewAdapter(Context mContext, ArrayList<T> listObjects, View mHeaderView) {
        this.mContext = mContext;
        this.mListObjects = listObjects;
        this.isHasHeader = mHeaderView != null;
        this.mHeaderView = mHeaderView;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.isSupportRTL = ApplicationUtils.isSupportRTL();
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (isHasHeader) {
            int viewType = getItemViewType(position);
            if (viewType != TYPE_HEADER_VIEW) {
                onBindNormalViewHolder(holder, position - 1);
            }
        }
        else {
            onBindNormalViewHolder(holder, position);
        }

    }

    public abstract void onBindNormalViewHolder(RecyclerView.ViewHolder holder, int position);

    public abstract RecyclerView.ViewHolder onCreateNormalViewHolder(ViewGroup v, int viewType);


    @Override
    public int getItemCount() {
        int size = mListObjects != null ? mListObjects.size() : 0;
        if (isHasHeader) {
            return size + 1;
        }
        else {
            return size;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isHasHeader && position == 0) {
            return TYPE_HEADER_VIEW;
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup v, int viewType) {
        RecyclerView.ViewHolder mHolder;
        if (viewType == TYPE_HEADER_VIEW) {
            mHolder = new ViewHeaderHolder(mHeaderView);
        }
        else {
            mHolder = onCreateNormalViewHolder(v, viewType);
        }
        return mHolder;
    }


    public static class ViewHeaderHolder extends RecyclerView.ViewHolder {
        ViewHeaderHolder(View convertView) {
            super(convertView);
        }
    }

    public interface OnItemClickListener<T> {
        void onViewDetail(T mObject);
    }

    public void setListener(OnItemClickListener<T> listener) {
        this.listener = listener;
    }


    public abstract class ViewNormalHolder extends RecyclerView.ViewHolder {

        public ViewNormalHolder(View convertView) {
            super(convertView);
            onFindView(convertView);
            if (isSupportRTL) {
                onUpdateUIWhenSupportRTL();
            }
        }

        public abstract void onFindView(View convertView);

        public void onUpdateUIWhenSupportRTL() {

        }
    }
}
