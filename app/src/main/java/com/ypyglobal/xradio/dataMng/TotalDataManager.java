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

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.ypyglobal.xradio.constants.IXRadioConstants;
import com.ypyglobal.xradio.model.ConfigureModel;
import com.ypyglobal.xradio.model.GenreModel;
import com.ypyglobal.xradio.model.RadioModel;
import com.ypyglobal.xradio.model.ThemeModel;
import com.ypyglobal.xradio.model.UIConfigModel;
import com.ypyglobal.xradio.setting.IXRadioSettingConstants;
import com.ypyglobal.xradio.setting.XRadioSettingManager;
import com.ypyglobal.xradio.ypylibs.cache.YPYCacheDataModel;
import com.ypyglobal.xradio.ypylibs.executor.YPYExecutorSupplier;
import com.ypyglobal.xradio.ypylibs.model.AbstractModel;
import com.ypyglobal.xradio.ypylibs.model.ResultModel;
import com.ypyglobal.xradio.ypylibs.utils.ApplicationUtils;
import com.ypyglobal.xradio.ypylibs.utils.IOUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: www.ypyglobal.com
 * @Date:Oct 20, 2017
 */

public class TotalDataManager implements IXRadioConstants, IXRadioSettingConstants {

    private static TotalDataManager totalDataManager;
    private YPYCacheDataModel mYPYCacheModel;
    private ConfigureModel configureModel;
    private UIConfigModel uiConfigModel;
    private RadioModel singRadioModel;

    public static TotalDataManager getInstance(Context mContext) {
        if (totalDataManager == null) {
            totalDataManager = new TotalDataManager(mContext);
        }
        return totalDataManager;
    }

    private TotalDataManager(Context mContext) {
        mYPYCacheModel = new YPYCacheDataModel(() -> getDirectoryTemp(mContext));
        mYPYCacheModel.addSaveMode(TYPE_TAB_FEATURED, new TypeToken<ArrayList<RadioModel>>() {
        }.getType());
        mYPYCacheModel.addSaveMode(TYPE_TAB_GENRE, new TypeToken<ArrayList<GenreModel>>() {
        }.getType());
        mYPYCacheModel.addSaveMode(TYPE_TAB_FAVORITE, new TypeToken<ArrayList<RadioModel>>() {
        }.getType());
        mYPYCacheModel.addSaveMode(TYPE_TAB_THEMES, new TypeToken<ArrayList<ThemeModel>>() {
        }.getType());
        mYPYCacheModel.addSaveMode(TYPE_UI_CONFIG, new TypeToken<ArrayList<UIConfigModel>>() {
        }.getType());
        mYPYCacheModel.addSaveMode(TYPE_SINGLE_RADIO);
    }

    public ArrayList<?> getListData(int id) {
        if (mYPYCacheModel != null) {
            if (id == TYPE_TAB_FAVORITE) {
                ArrayList<RadioModel> mListFav = (ArrayList<RadioModel>) mYPYCacheModel.getListCacheData(TYPE_TAB_FAVORITE);
                if (mListFav != null && mListFav.size() > 0) {
                    for (RadioModel model : mListFav) {
                        model.setFavorite(true);
                    }
                }
            }
            return mYPYCacheModel.getListCacheData(id);
        }
        return null;
    }

    private boolean isCacheExisted(int id) {
        if (mYPYCacheModel != null) {
            return mYPYCacheModel.isCacheExisted(id);
        }
        return false;
    }

    public void readAllCache() {
        if (mYPYCacheModel != null) {
            mYPYCacheModel.readAllCache();
            ArrayList<RadioModel> mListFav = (ArrayList<RadioModel>) getListData(TYPE_TAB_FAVORITE);
            if (mListFav != null && mListFav.size() > 0) {
                for (RadioModel model : mListFav) {
                    model.setFavorite(true);
                }
            }
        }
    }

    public void onDestroy() {
        if (mYPYCacheModel != null) {
            mYPYCacheModel.onDestroy();
            mYPYCacheModel = null;
        }
        totalDataManager = null;
    }

    public ConfigureModel getConfigureModel() {
        return configureModel;
    }

    public String getLastFmKey() {
        int uiPlayer = uiConfigModel != null ? uiConfigModel.getUiPlayer() : UI_PLAYER_NO_LAST_FM_ROTATE_DISK;
        if (uiPlayer >= UI_PLAYER_NO_LAST_FM_SQUARE_DISK) {
            return null;
        }
        return configureModel != null ? configureModel.getLastFmApiKey() : null;
    }

    public void readConfigure(Context mContext) {
        try {
            Type mTypeToken = new TypeToken<ResultModel<ConfigureModel>>() {
            }.getType();
            ResultModel<ConfigureModel> mResultModel = XRadioNetUtils.getListDataFromAssets(mContext, FILE_CONFIGURE, mTypeToken);
            if (mResultModel != null && mResultModel.isResultOk()) {
                ArrayList<ConfigureModel> mListConfigs = mResultModel.getListModels();
                configureModel = mListConfigs != null && mListConfigs.size() > 0 ? mListConfigs.get(0) : null;
                int cacheExpiration = configureModel != null ? configureModel.getCacheExpiration() : 0;

                long deltaTime = System.currentTimeMillis() - XRadioSettingManager.getPivotTime(mContext);
                boolean isCacheExpired = (cacheExpiration > 0 && deltaTime >= cacheExpiration * ONE_HOUR) || cacheExpiration == 0;

                readUIConfig(mContext, isCacheExpired);
                readDefaultThemes(mContext, isCacheExpired);
                readCurrentRadioInSingleMode(mContext, isCacheExpired);

                if (isCacheExpired && cacheExpiration > 0 && configureModel != null && configureModel.isOnlineApp() && ApplicationUtils.isOnline(mContext)) {
                    XRadioSettingManager.setPivotTime(mContext, System.currentTimeMillis());
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readCurrentRadioInSingleMode(Context mContext, boolean isCacheExpired) {
        try {
            boolean isOnlineApp = configureModel != null && configureModel.isOnlineApp();
            boolean isSingleRadio = isSingleRadio();
            ArrayList<RadioModel> mListRadioModels = null;
            if (isSingleRadio) {
                Type mTypeToken = new TypeToken<ResultModel<RadioModel>>() {
                }.getType();
                if (!isOnlineApp) {
                    ResultModel<RadioModel> mResultModel = XRadioNetUtils.getListDataFromAssets(mContext, FILE_RADIOS, mTypeToken);
                    if (mResultModel != null && mResultModel.isResultOk()) {
                        mListRadioModels = mResultModel.getListModels();
                        setListCacheData(TYPE_SINGLE_RADIO, mListRadioModels);
                        singRadioModel = mListRadioModels != null && mListRadioModels.size() > 0 ? mListRadioModels.get(0) : null;
                    }
                }
                else {
                    String urlHost = configureModel.getUrlEndPoint();
                    String apiKey = configureModel.getApiKey();
                    boolean isCacheExisted = isCacheExisted(TYPE_SINGLE_RADIO);
                    if (DEBUG || isCacheExpired || !isCacheExisted) {
                        if (ApplicationUtils.isOnline(mContext)) {
                            ResultModel<RadioModel> mResultModel = XRadioNetUtils.getListRadioModel(urlHost, apiKey, 0, 1);
                            if (mResultModel != null && mResultModel.isResultOk()) {
                                mListRadioModels = mResultModel.getListModels();
                                singRadioModel = mListRadioModels != null && mListRadioModels.size() > 0 ? mListRadioModels.get(0) : null;
                                if (singRadioModel != null) {
                                    setListCacheData(TYPE_SINGLE_RADIO, mListRadioModels);
                                    return;
                                }
                            }
                        }
                    }
                    //read from cache if it has existed
                    if (isCacheExisted && (mListRadioModels == null || mListRadioModels.size() == 0)) {
                        readTypeData(mContext, TYPE_SINGLE_RADIO);
                        mListRadioModels = (ArrayList<RadioModel>) getListData(TYPE_SINGLE_RADIO);
                        singRadioModel = mListRadioModels != null && mListRadioModels.size() > 0 ? mListRadioModels.get(0) : null;
                    }
                }

            }


        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readUIConfig(Context mContext, boolean isCacheExpired) {
        try {
            if (configureModel != null) {
                if (!configureModel.isOnlineApp() || OFFLINE_UI_CONFIG) {
                    Type mTypeToken = new TypeToken<ResultModel<UIConfigModel>>() {
                    }.getType();
                    ResultModel<UIConfigModel> mResultModel = XRadioNetUtils.getListDataFromAssets(mContext, FILE_UI, mTypeToken);
                    if (mResultModel != null && mResultModel.isResultOk()) {
                        ArrayList<UIConfigModel> mListUIConfig = mResultModel.getListModels();
                        uiConfigModel = mListUIConfig != null && mListUIConfig.size() > 0 ? mListUIConfig.get(0) : null;
                        setListCacheData(TYPE_UI_CONFIG, mListUIConfig);

                    }

                }
                else {
                    //read UI config morel
                    String urlHost = configureModel.getUrlEndPoint();
                    String apiKey = configureModel.getApiKey();
                    boolean isCacheExisted = isCacheExisted(TYPE_UI_CONFIG);
                    ArrayList<UIConfigModel> mListConfigUI = null;
                    if (DEBUG || isCacheExpired || !isCacheExisted) {
                        if (ApplicationUtils.isOnline(mContext)) {
                            ResultModel<UIConfigModel> mUIConfigResult = XRadioNetUtils.getUIConfigModel(urlHost, apiKey);
                            if (mUIConfigResult != null && mUIConfigResult.isResultOk()) {
                                mListConfigUI = mUIConfigResult.getListModels();
                                setListCacheData(TYPE_UI_CONFIG, mListConfigUI);
                            }
                        }
                    }
                    //read from cache if it has existed
                    if (isCacheExisted && (mListConfigUI == null || mListConfigUI.size() == 0)) {
                        readTypeData(mContext, TYPE_UI_CONFIG);
                        mListConfigUI = (ArrayList<UIConfigModel>) getListData(TYPE_UI_CONFIG);
                    }
                    //read again from assets if network is timeout
                    if (mListConfigUI == null || mListConfigUI.size() == 0) {
                        Type mTypeToken = new TypeToken<ResultModel<UIConfigModel>>() {
                        }.getType();
                        ResultModel<UIConfigModel> mResultModel = XRadioNetUtils.getListDataFromAssets(mContext, FILE_UI, mTypeToken);
                        if (mResultModel != null && mResultModel.isResultOk()) {
                            mListConfigUI = mResultModel.getListModels();
                            setListCacheData(TYPE_UI_CONFIG, mListConfigUI);
                        }
                    }
                    if (mListConfigUI != null && mListConfigUI.size() > 0) {
                        uiConfigModel = mListConfigUI.get(0);
                    }
                }

            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void readDefaultThemes(Context mContext, boolean isCacheExpired) {
        try {
            int savedThemeId = XRadioSettingManager.getThemId(mContext);
            if (configureModel != null) {
                if (!configureModel.isOnlineApp()) {
                    Type mTypeToken = new TypeToken<ResultModel<ThemeModel>>() {
                    }.getType();
                    ResultModel<ThemeModel> mResultModel = XRadioNetUtils.getListDataFromAssets(mContext, FILE_THEMES, mTypeToken);
                    if (mResultModel != null && mResultModel.isResultOk()) {
                        ArrayList<ThemeModel> mListThemes = mResultModel.getListModels();
                        if (mListThemes != null && mListThemes.size() > 0 && savedThemeId == 0) {
                            XRadioSettingManager.saveThemes(mContext, mListThemes.get(0), configureModel.getUrlEndPoint());
                        }
                    }

                }
                else {
                    //get theme for single radio or first time open app
                    boolean isSingleRadio = isSingleRadio();
                    String urlHost = configureModel.getUrlEndPoint();
                    String apiKey = configureModel.getApiKey();
                    boolean isCacheExisted = isCacheExisted(TYPE_TAB_THEMES);
                    if (savedThemeId == 0 || (isSingleRadio && (isCacheExpired || DEBUG)) || !isCacheExisted) {
                        ArrayList<ThemeModel> mListThemes = null;
                        if (ApplicationUtils.isOnline(mContext)) {
                            ResultModel<ThemeModel> mResultModel = XRadioNetUtils.getDefaultThemes(urlHost, apiKey);
                            if (mResultModel != null && mResultModel.isResultOk()) {
                                mListThemes = mResultModel.getListModels();
                                setListCacheData(TYPE_TAB_THEMES, mListThemes);
                            }
                        }
                        //read from cache if it has existed
                        if (isCacheExisted && (mListThemes == null || mListThemes.size() == 0)) {
                            readTypeData(mContext, TYPE_TAB_THEMES);
                            mListThemes = (ArrayList<ThemeModel>) getListData(TYPE_TAB_THEMES);
                        }
                        //save themes to xml
                        if (mListThemes != null && mListThemes.size() > 0) {
                            XRadioSettingManager.saveThemes(mContext, mListThemes.get(0), urlHost);
                        }

                    }
                }

            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public File getDirectoryCached(Context mContext) {
        try {
            if (!IOUtils.hasAndroid10() && ApplicationUtils.hasSDcard() && SAVE_FAVORITE_SDCARD) {
                boolean isGranted = ApplicationUtils.isGrantAllPermission(mContext, LIST_PERMISSIONS);
                if (isGranted) {
                    File mRoot = new File(Environment.getExternalStorageDirectory(), DIR_CACHE);
                    if (!mRoot.exists()) {
                        mRoot.mkdirs();
                    }
                    return mRoot;
                }
            }
            if (USE_EXTERNAL_CACHE) {
                File mFile = mContext.getExternalCacheDir();
                if (mFile == null) {
                    mFile = mContext.getCacheDir();
                }
                return mFile;
            }
            return mContext.getCacheDir();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private File getDirectoryTemp(Context mContext) {
        File mRoot = getDirectoryCached(mContext);
        if (mRoot != null) {
            final File mFile = new File(mRoot, DIR_TEMP);
            if (!mFile.exists()) {
                mFile.mkdirs();
            }
            return mFile;
        }
        return null;

    }

    public void readTypeData(Context mContext, int id) {
        if (mYPYCacheModel != null) {
            mYPYCacheModel.readCacheData(id);
            if (id == TYPE_TAB_FEATURED) {
                updateFavoriteForList((ArrayList<RadioModel>) getListData(TYPE_TAB_FEATURED), TYPE_TAB_FAVORITE);
            }
        }
    }

    public void setListCacheData(int id, ArrayList<?> mListDatas) {
        if (mYPYCacheModel != null) {
            mYPYCacheModel.setListCacheData(id, mListDatas);
        }
    }

    private void saveListCacheModel(int id) {
        if (mYPYCacheModel != null) {
            mYPYCacheModel.saveCacheData(id);
        }
    }

    public void saveListCacheModelInThread(int id) {
        YPYExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            saveListCacheModel(id);
        });

    }

    public void addModelToCache(int type, Object object) {
        if (mYPYCacheModel != null) {
            mYPYCacheModel.addModelInCache(type, 0, object);
        }
    }

    public boolean removeModelToCache(int type, Object object) {
        if (mYPYCacheModel != null) {
            return mYPYCacheModel.removeModelInCache(type, object);
        }
        return false;
    }

    public int updateFavoriteForId(ArrayList<? extends AbstractModel> listObject, long id, boolean isFavorite) {
        if (listObject != null && listObject.size() > 0) {
            for (AbstractModel abstractModel : listObject) {
                if (abstractModel.getId() == id) {
                    abstractModel.setFavorite(isFavorite);
                    return listObject.indexOf(abstractModel);
                }
            }
        }
        return -1;
    }


    public void updateFavoriteForList(ArrayList<? extends AbstractModel> listObject, int type) {
        ArrayList<? extends AbstractModel> listFavoriteObject = (ArrayList<? extends AbstractModel>) getListData(type);
        if (listObject != null && listObject.size() > 0) {
            for (AbstractModel abstractModel : listObject) {
                abstractModel.setFavorite(isInFavoriteList(listFavoriteObject, abstractModel.getId()));
            }
        }
    }

    private boolean isInFavoriteList(ArrayList<? extends AbstractModel> listFavoriteObject, long id) {
        try {
            if (listFavoriteObject != null && listFavoriteObject.size() > 0) {
                for (AbstractModel trackModel : listFavoriteObject) {
                    if (trackModel.getId() == id) {
                        return true;
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isListEqual(ArrayList<? extends AbstractModel> mList1, ArrayList<? extends AbstractModel> mList2) {
        try {
            int size1 = mList1 != null ? mList1.size() : 0;
            int size2 = mList2 != null ? mList2.size() : 0;
            if (size2 > 0 && size1 == size2) {
                for (int i = 0; i < size1; i++) {
                    AbstractModel model1 = mList1.get(i);
                    AbstractModel model2 = mList2.get(i);
                    if (!model1.equals(model2)) {
                        return false;
                    }
                }
                return true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public UIConfigModel getUiConfigModel() {
        return uiConfigModel;
    }

    public ResultModel<RadioModel> searchRadioModel(Context mContext, String keyword) {
        ResultModel<RadioModel> mResult = new ResultModel<>(200, "");
        if (!TextUtils.isEmpty(keyword)) {
            final String finalKey = keyword.toLowerCase();
            Type mTypeToken = new TypeToken<ResultModel<RadioModel>>() {
            }.getType();
            ResultModel<RadioModel> mResultModel = XRadioNetUtils.getListDataFromAssets(mContext, FILE_RADIOS, mTypeToken);

            ArrayList<RadioModel> mListRadios = mResultModel != null ? mResultModel.getListModels() : null;
            if (mListRadios != null && mListRadios.size() > 0) {
                ArrayList<RadioModel> mListSearch = new ArrayList<>();
                mResult.setListModels(mListSearch);
                for (RadioModel model : mListRadios) {
                    String name = model.getName();
                    String tag = model.getTags();
                    if ((name != null && name.toLowerCase().contains(finalKey)) || (tag != null && tag.toLowerCase().contains(finalKey))) {
                        mListSearch.add(model);
                    }
                }
            }
        }
        return mResult;

    }

    public ResultModel<RadioModel> getRadioModelOfGenreId(Context mContext, long genreId) {
        ResultModel<RadioModel> mResult = new ResultModel<>(200, "");
        if (genreId > 0) {
            Type mTypeToken = new TypeToken<ResultModel<RadioModel>>() {
            }.getType();
            ResultModel<RadioModel> mResultModel = XRadioNetUtils.getListDataFromAssets(mContext, FILE_RADIOS, mTypeToken);

            ArrayList<RadioModel> mListRadios = mResultModel != null ? mResultModel.getListModels() : null;
            if (mListRadios != null && mListRadios.size() > 0) {
                ArrayList<RadioModel> mListSearch = new ArrayList<>();
                mResult.setListModels(mListSearch);
                for (RadioModel model : mListRadios) {
                    ArrayList<Integer> mListGenresId = model.getListGenreIds();
                    if (mListGenresId != null && mListGenresId.size() > 0) {
                        for (Integer mId : mListGenresId) {
                            if (genreId == mId) {
                                mListSearch.add(model.cloneObject());
                                break;
                            }
                        }
                    }
                }
            }
        }
        return mResult;

    }

    public boolean isSingleRadio() {
        return uiConfigModel != null && !uiConfigModel.isMultiApp();
    }

    public RadioModel getSingRadioModel() {
        return singRadioModel;
    }

}
