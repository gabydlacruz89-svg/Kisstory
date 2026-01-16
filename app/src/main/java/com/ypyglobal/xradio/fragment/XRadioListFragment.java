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

package com.ypyglobal.xradio.fragment;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ypyglobal.xradio.R;
import com.ypyglobal.xradio.XMultiRadioMainActivity;
import com.ypyglobal.xradio.constants.IXRadioConstants;
import com.ypyglobal.xradio.databinding.FragmentRecyclerviewBinding;
import com.ypyglobal.xradio.model.ConfigureModel;
import com.ypyglobal.xradio.model.UIConfigModel;
import com.ypyglobal.xradio.ypylibs.adapter.YPYRecyclerViewAdapter;
import com.ypyglobal.xradio.ypylibs.executor.YPYExecutorSupplier;
import com.ypyglobal.xradio.ypylibs.fragment.YPYFragment;
import com.ypyglobal.xradio.ypylibs.model.AbstractModel;
import com.ypyglobal.xradio.ypylibs.model.ResultModel;
import com.ypyglobal.xradio.ypylibs.utils.ApplicationUtils;
import com.ypyglobal.xradio.ypylibs.utils.YPYLog;
import com.ypyglobal.xradio.ypylibs.view.YPYRecyclerView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 * Created by YPY Global on 10/25/17.
 */

public abstract class XRadioListFragment<T> extends YPYFragment<FragmentRecyclerviewBinding> implements IXRadioConstants
        , YPYRecyclerView.OnLoadMoreListener {

    protected XMultiRadioMainActivity mContext;
    private ArrayList<T> mListModels;

    int mType = -1;
    private boolean isDestroy;
    YPYRecyclerViewAdapter<T> mAdapter;

    private boolean isAllowLoadMore;
    private boolean isShowWhenNoData;
    private boolean isAllowRefresh = true;
    private boolean isTab;

    int mNumberItemPerPage = NUMBER_ITEM_PER_PAGE;
    private int mMaxPage = MAX_PAGE;
    private boolean isAllowReadCache;
    private boolean isOfflineData;
    private boolean isGetFromCacheWhenNoData;

    UIConfigModel mUIConfigureModel;
    ConfigureModel mConfigureMode;
    public String mUrlHost;
    String mApiKey;
    int mSizeH;

    @NonNull
    @Override
    protected FragmentRecyclerviewBinding getViewBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentRecyclerviewBinding.inflate(inflater, container, false);
    }


    @Override
    public void findView() {
        mContext = (XMultiRadioMainActivity) requireActivity();
        viewBinding.swiperefresh.setOnRefreshListener(this::onRefreshData);
        viewBinding.swiperefresh.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        viewBinding.swiperefresh.setEnabled(isAllowRefresh);

        mUIConfigureModel = mContext.mTotalMng.getUiConfigModel();
        mConfigureMode = mContext.mTotalMng.getConfigureModel();

        mUrlHost = mConfigureMode != null ? mConfigureMode.getUrlEndPoint() : null;
        mApiKey = mConfigureMode != null ? mConfigureMode.getApiKey() : null;

        setUpUI();

        if (isAllowLoadMore) {
            viewBinding.recyclerView.setOnDBListViewListener(this);
        }

        if (!isTab || isFirstInTab()) {
            startLoadData();
        }

    }

    void onRefreshData() {
        if (mContext != null) {
            if (viewBinding.progressBar.getVisibility() == View.VISIBLE) {
                viewBinding.swiperefresh.setRefreshing(false);
                return;
            }
            if (isAllowLoadMore && viewBinding.footerView.getRoot().getVisibility() == View.VISIBLE) {
                viewBinding.swiperefresh.setRefreshing(false);
                return;
            }
            onReceiveData(true, false);
        }

    }

    @Override
    public void hideFooterView() {
        viewBinding.footerView.getRoot().setVisibility(View.GONE);
    }

    @Override
    public void showFooterView() {
        viewBinding.footerView.getRoot().setVisibility(View.VISIBLE);
    }


    @Override
    public void startLoadData() {
        super.startLoadData();
        if (mContext != null && !isLoadingData()) {
            setLoadingData(true);
            onReceiveData(false, true);
        }
    }

    private void onReceiveData(boolean isNeedRefresh, boolean isNeedHideRecycler) {
        if (isNeedRefresh) {
            viewBinding.recyclerView.onResetData(false);
        }
        if (isNeedHideRecycler) {
            viewBinding.recyclerView.setVisibility(View.GONE);
            showLoading(true);
        }
        YPYExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            ArrayList<T> mListModels = null;
            ResultModel<T> resultModel = null;
            boolean isNeedCheckOnline = false;
            if (isOfflineData || (!isNeedRefresh && isAllowReadCache && mType > 0 && !ApplicationUtils.isOnline(mContext))) {
                mListModels = getDataFromCacheDisk();
            }
            if (!isOfflineData && (mListModels == null || isNeedRefresh)) {
                isNeedCheckOnline = true;
                resultModel = getListModelFromServer(0, mNumberItemPerPage);
                if (resultModel != null && resultModel.isResultOk()) {
                    if (isAllowReadCache && mType > 0) {
                        mContext.mTotalMng.setListCacheData(mType, resultModel.getListModels());
                        mListModels = (ArrayList<T>) mContext.mTotalMng.getListData(mType);
                    }
                    if (mListModels == null || mListModels.size() == 0) {
                        mListModels = resultModel.getListModels();
                    }
                }
                else {
                    if (isGetFromCacheWhenNoData) {
                        mListModels = getDataFromCacheDisk();
                    }
                }
            }
            ResultModel<T> finalResultModel = resultModel;
            boolean finalIsNeedCheckOnline = isNeedCheckOnline;
            ArrayList<T> finalMListModels = mListModels;
            mContext.runOnUiThread(() -> {
                try {
                    if (isDestroy) return;
                    showLoading(false);
                    viewBinding.swiperefresh.setRefreshing(false);
                    if (finalIsNeedCheckOnline && (finalResultModel == null || !finalResultModel.isResultOk())) {
                        if (isGetFromCacheWhenNoData) {
                            setUpInfo(finalMListModels);
                            return;
                        }
                        String msg = finalResultModel != null ? finalResultModel.getMsg() : null;
                        if (!TextUtils.isEmpty(msg)) {
                            showResult(msg);
                            return;
                        }
                        int msgId = !ApplicationUtils.isOnline(mContext) ? R.string.info_lose_internet : R.string.info_server_error;
                        showResult(msgId);
                        return;
                    }
                    if (isNeedRefresh) {
                        onDoWhenRefreshList();
                    }
                    setUpInfo(finalMListModels);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            });
        });
    }

    public void onDoWhenRefreshList() {

    }

    @Override
    public void onLoadNextItem() {
        if (!ApplicationUtils.isOnline(mContext)) {
            hideFooterView();
            viewBinding.swiperefresh.setRefreshing(false);
            mContext.showToast(R.string.info_lose_internet);
            viewBinding.recyclerView.setStartAddingPage(false);
            return;
        }
        YPYExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            final int originalSize = mListModels != null ? mListModels.size() : 0;
            ResultModel<T> resultModel = getListModelFromServer(originalSize, mNumberItemPerPage);
            final ArrayList<T> listLoadMores = (resultModel != null && resultModel.isResultOk()) ? resultModel.getListModels() : null;
            final int sizeLoaded = listLoadMores != null ? listLoadMores.size() : 0;
            final boolean isLoadOkNumberItem = sizeLoaded >= mNumberItemPerPage;

            mContext.runOnUiThread(() -> {
                try {
                    if (isDestroy) return;
                    hideFooterView();
                    boolean isAllowLoadPage = isLoadOkNumberItem && viewBinding.recyclerView.getCurrentPage() < mMaxPage;
                    YPYLog.e(TAG, "=========>isLoadOkNumberItem=" + isLoadOkNumberItem + "==>isAllowLoadPage=" + isAllowLoadPage);
                    viewBinding.recyclerView.setAllowAddPage(isAllowLoadPage);
                    if (isAllowLoadPage) {
                        int page = viewBinding.recyclerView.getCurrentPage() + 1;
                        viewBinding.recyclerView.setCurrentPage(page);
                    }

                    if (sizeLoaded > 0) {
                        mListModels.addAll(listLoadMores);
                        if (mAdapter != null) {
                            mAdapter.notifyItemRangeChanged(originalSize, sizeLoaded);
                        }
                        mContext.mTotalMng.saveListCacheModelInThread(mType);
                    }
                    viewBinding.recyclerView.setStartAddingPage(false);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            });
        });
    }

    private void setUpInfo(ArrayList<T> listObjects) {
        if (isDestroy) return;
        this.viewBinding.recyclerView.setAdapter(null);
        if (!isOfflineData) {
            if (this.mListModels != null) {
                this.mListModels.clear();
            }
        }
        this.mListModels = listObjects;
        int size = mListModels != null ? mListModels.size() : 0;
        if (size > 0 || (isShowWhenNoData && mListModels != null)) {
            viewBinding.recyclerView.setVisibility(View.VISIBLE);
            mAdapter = createAdapter(listObjects);
            if (mAdapter != null) {
                viewBinding.recyclerView.setAdapter(mAdapter);
            }
            if (isAllowLoadMore) {
                boolean b = checkAllowLoadMore(size);
                viewBinding.recyclerView.setAllowAddPage(b);
                if (b) {
                    int page = viewBinding.recyclerView.getCurrentPage() + 1;
                    viewBinding.recyclerView.setCurrentPage(page);
                }
            }

        }
        if (!isShowWhenNoData) {
            updateInfo();
        }
    }

    public abstract YPYRecyclerViewAdapter<T> createAdapter(ArrayList<T> listObjects);

    public ResultModel<T> getListModelFromServer(int offset, int limit) {
        return null;
    }

    public abstract void setUpUI();

    private ArrayList<T> getDataFromCacheDisk() {
        return getDataFromCacheDisk(mType);
    }

    private ArrayList<T> getDataFromCacheDisk(int type) {
        ArrayList<T> mListModels = (ArrayList<T>) mContext.mTotalMng.getListData(type);
        if (mListModels == null) {
            mContext.mTotalMng.readTypeData(mContext, type);
            mListModels = (ArrayList<T>) mContext.mTotalMng.getListData(type);
        }
        return mListModels;
    }

    private boolean checkAllowLoadMore(int sizeLoaded) {
        int page = (int) Math.floor((float) sizeLoaded / (float) mNumberItemPerPage);
        return page < mMaxPage && sizeLoaded >= mNumberItemPerPage;
    }

    @Override
    public void onDestroy() {
        isDestroy = true;
        try {
            if (!isOfflineData && viewBinding != null) {
                viewBinding.swiperefresh.setRefreshing(false);
                viewBinding.swiperefresh.setEnabled(false);
                viewBinding.recyclerView.setAdapter(null);
                if (mListModels != null) {
                    mListModels.clear();
                    mListModels = null;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    protected void updateInfo() {
        boolean b = mListModels != null && mListModels.size() > 0;
        viewBinding.tvNoResult.setVisibility(b ? View.GONE : View.VISIBLE);
        if (!b) {
            viewBinding.tvNoResult.setText(R.string.title_no_data);
        }
    }

    private void showLoading(boolean b) {
        viewBinding.progressBar.setVisibility(b ? View.VISIBLE : View.GONE);
        if (b) {
            viewBinding.recyclerView.setVisibility(View.GONE);
            viewBinding.tvNoResult.setVisibility(View.GONE);
        }
    }

    private void showResult(int resId) {
        if (mContext != null) {
            showResult(mContext.getString(resId));
        }
    }

    private void showResult(String msg) {
        viewBinding.tvNoResult.setText(msg);
        viewBinding.tvNoResult.setVisibility(mAdapter == null ? View.VISIBLE : View.GONE);
        if (mAdapter != null) {
            mContext.showToast(msg);
        }
    }

    @Override
    public void onExtractData(Bundle args) {
        super.onExtractData(args);
        if (args != null) {
            mType = args.getInt(KEY_TYPE_FRAGMENT, -1);
            isAllowLoadMore = args.getBoolean(KEY_ALLOW_MORE, false);
            isAllowReadCache = args.getBoolean(KEY_ALLOW_READ_CACHE, false);
            isTab = args.getBoolean(KEY_IS_TAB, false);
            isAllowRefresh = args.getBoolean(KEY_ALLOW_REFRESH, true);
            isShowWhenNoData = args.getBoolean(KEY_ALLOW_SHOW_NO_DATA, false);
            mNumberItemPerPage = args.getInt(KEY_NUMBER_ITEM_PER_PAGE, NUMBER_ITEM_PER_PAGE);
            mMaxPage = args.getInt(KEY_MAX_PAGE, MAX_PAGE);
            isOfflineData = args.getBoolean(KEY_OFFLINE_DATA, false);
            isGetFromCacheWhenNoData = args.getBoolean(KEY_READ_CACHE_WHEN_NO_DATA, false);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void notifyData() {
        super.notifyData();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            if (!isShowWhenNoData) {
                updateInfo();
            }
        }
    }


    @Override
    public void notifyData(int pos) {
        super.notifyData(pos);
        if (mAdapter != null) {
            mAdapter.notifyItemChanged(pos);
        }
    }

    public void notifyFavorite(long trackId, boolean isFav) {
        if (mContext != null && mListModels != null && mListModels.size() > 0) {
            int index = mContext.mTotalMng.updateFavoriteForId((ArrayList<? extends AbstractModel>) mListModels, trackId, isFav);
            if (index >= 0) {
                mContext.runOnUiThread(() -> notifyData(index));
            }
        }
    }


    void setUpUIRecyclerView(int mTypeUI) {
        try {
            int dialogMargin = getResources().getDimensionPixelOffset(R.dimen.dialog_margin);
            int smallMargin = getResources().getDimensionPixelOffset(R.dimen.small_margin);
            int numColumn = NUMBER_GRID_COLUMN;

            mSizeH = (int) ((mContext.getScreenWidth() - ((float) numColumn + 1) * dialogMargin) / numColumn);

            if (mTypeUI == UI_FLAT_LIST || mTypeUI == UI_CARD_LIST) {
                mContext.setUpRecyclerViewAsListView(viewBinding.recyclerView, mTypeUI == UI_CARD_LIST ? mContext.getSupportDrawable(R.drawable.alpha_divider_small_verti) : null);
            }
            else if (mTypeUI == UI_CARD_GRID || mTypeUI == UI_FLAT_GRID) {
                Drawable mDrawableVer = null;
                try {
                    if (mTypeUI == UI_FLAT_GRID) {
                        mDrawableVer = mContext.getSupportDrawable(R.drawable.alpha_divider_large_verti);
                    }
                    else {
                        mDrawableVer = mContext.getSupportDrawable(R.drawable.alpha_divider_small_verti);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                mContext.setUpRecyclerViewAsGridView(viewBinding.recyclerView, numColumn, mDrawableVer, null);
                GridLayoutManager layoutManager = (GridLayoutManager) viewBinding.recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                        @Override
                        public int getSpanSize(int position) {
                            RecyclerView.Adapter<?> mAdapter = viewBinding.recyclerView.getAdapter();
                            if (mAdapter != null && mAdapter.getItemViewType(position) == YPYRecyclerViewAdapter.TYPE_HEADER_VIEW) {
                                return numColumn;
                            }
                            return 1;
                        }
                    });
                }
            }
            else if (mTypeUI == UI_MAGIC_GRID) {
                mContext.setUpRecyclerViewAsStaggered(viewBinding.recyclerView, mContext.getSupportDrawable(R.drawable.alpha_divider_small_verti), null);
            }
            if (mTypeUI != UI_FLAT_LIST) {
                viewBinding.recyclerView.setPadding(smallMargin, dialogMargin, smallMargin, 0);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean isCheckBack() {
        //fix case when activity is destroyed
        if (viewBinding == null) return false;
        if (!isOfflineData && viewBinding.footerView.getRoot().getVisibility() == View.VISIBLE) {
            return true;
        }
        if (viewBinding.progressBar.getVisibility() == View.VISIBLE) {
            return true;
        }
        if (isRecyclerScrolling()) {
            return true;
        }
        return super.isCheckBack();
    }

    private boolean isRecyclerScrolling() {
        try {
            return viewBinding.recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_TYPE_FRAGMENT, mType);
        outState.putBoolean(KEY_ALLOW_MORE, isAllowLoadMore);
        outState.putBoolean(KEY_ALLOW_READ_CACHE, isAllowReadCache);
        outState.putBoolean(KEY_IS_TAB, isTab);
        outState.putBoolean(KEY_ALLOW_REFRESH, isAllowRefresh);
        outState.putBoolean(KEY_ALLOW_SHOW_NO_DATA, isShowWhenNoData);
        outState.putInt(KEY_NUMBER_ITEM_PER_PAGE, NUMBER_ITEM_PER_PAGE);
        outState.putInt(KEY_MAX_PAGE, MAX_PAGE);
        outState.putBoolean(KEY_OFFLINE_DATA, isOfflineData);
        outState.putBoolean(KEY_READ_CACHE_WHEN_NO_DATA, isGetFromCacheWhenNoData);

    }

}
