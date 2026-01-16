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

package com.ypyglobal.xradio.ypylibs.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: www.ypyglobal.com
 * @Date:Oct 20, 2017
 */
public class YPYRecyclerView extends RecyclerView {

    public static final String TAG = YPYRecyclerView.class.getSimpleName();

    private int currentPage;
    private boolean isAllowAddPage;
    private boolean isStartAddingPage;

    private OnLoadMoreListener mOnLoadMoreListener;

    public YPYRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public YPYRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public YPYRecyclerView(Context context) {
        super(context);
        init();
    }

    private void init() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1) && dy != 0) {
                    onCheckLastItem();
                }

            }
        });
    }

    public void setOnDBListViewListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    /**
     * Simple Listener that allows you to be notified when the user has scrolled
     * to the end of the AdapterView. See (
     *
     * @author DOBAO
     */
    public interface OnLoadMoreListener {
        void onLoadNextItem();

        void hideFooterView();

        void showFooterView();
    }

    public void onCheckLastItem() {
        if (mOnLoadMoreListener != null) {
            Adapter<?> mAdapter = getAdapter();
            if (mAdapter != null) {
                if (isAllowAddPage) {
                    if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener.showFooterView();
                    }
                    if (!isStartAddingPage) {
                        isStartAddingPage = true;
                        if (mOnLoadMoreListener != null) {
                            mOnLoadMoreListener.onLoadNextItem();
                        }
                    }
                }
                else {
                    if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener.hideFooterView();
                    }
                }
            }
        }

    }

    public boolean isAllowAddPage() {
        return isAllowAddPage;
    }

    public void setAllowAddPage(boolean isAllowAddPage) {
        this.isAllowAddPage = isAllowAddPage;
    }

    public boolean isStartAddingPage() {
        return isStartAddingPage;
    }

    public void setStartAddingPage(boolean isStartAddingPage) {
        this.isStartAddingPage = isStartAddingPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void onResetData(boolean isResetAdapter) {
        currentPage = 0;
        isAllowAddPage = false;
        isStartAddingPage = false;
        if (isResetAdapter) {
            setAdapter(null);
        }
    }


}
