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

package com.ypyglobal.xradio.ypylibs.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ypyglobal.xradio.ypylibs.utils.IOUtils;
import com.ypyglobal.xradio.ypylibs.utils.YPYLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 * Created by YPY Global on 10/19/17.
 */

public class YPYCacheDataModel {

    private static final String PREFIX_CACHE = "cache_%1$s.ypy";

    private final IYPYCacheListener cacheListener;
    private final ArrayList<YPYSaveModel> listSaveModels;

    public YPYCacheDataModel(IYPYCacheListener cacheListener) {
        this.cacheListener = cacheListener;
        this.listSaveModels = new ArrayList<>();
    }

    public void addSaveMode(int id, Type type) {
        addSaveMode(id, type, 0);

    }

    private void addSaveMode(int id, Type type, int maxCache) {
        try {
            if (getSaveMode(id) == null) {
                YPYSaveModel saveModel = new YPYSaveModel(id, type, String.format(PREFIX_CACHE, String.valueOf(id)));
                saveModel.setMaximumObject(maxCache);
                listSaveModels.add(saveModel);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addSaveMode(int id) {
        try {
            if (getSaveMode(id) == null) {
                YPYSaveModel saveModel = new YPYSaveModel(id);
                listSaveModels.add(saveModel);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private YPYSaveModel getSaveMode(int id) {
        if (listSaveModels != null && listSaveModels.size() > 0 && id != 0) {
            for (YPYSaveModel model : listSaveModels) {
                if (model.getId() == id) {
                    return model;
                }
            }
        }
        return null;
    }

    public void readAllCache() {
        if (listSaveModels != null && listSaveModels.size() > 0) {
            for (YPYSaveModel model : listSaveModels) {
                readCacheData(model);
            }
        }
    }

    public void readCacheData(int id) {
        YPYSaveModel model = getSaveMode(id);
        readCacheData(model);
    }

    public synchronized void addModelInCache(int id, Object model) {
        addModelInCache(id, -1, model);
    }

    public synchronized void addModelInCache(int id, int pos, Object model) {
        try {
            if (model != null) {
                ArrayList<Object> mListDatas = (ArrayList<Object>) getListCacheData(id);
                YPYSaveModel saveMode = getSaveMode(id);
                int maxSize = saveMode != null ? saveMode.getMaximumObject() : 0;
                if (mListDatas != null) {
                    Object mOldObject = getObjectExistedInCache(id, model);
                    boolean isNeedSave = false;
                    if (pos >= 0) {
                        if (mOldObject != null) {
                            mListDatas.remove(mOldObject);
                            mListDatas.add(pos, mOldObject);
                        }
                        else {
                            mListDatas.add(pos, model);
                        }
                        isNeedSave = true;
                    }
                    else {
                        if (mOldObject == null) {
                            mListDatas.add(model);
                            isNeedSave = true;
                        }
                    }
                    if (isNeedSave) {
                        int currentSize = mListDatas.size();
                        if (maxSize > 0 && currentSize > maxSize && pos == 0) {
                            mListDatas.remove(currentSize - 1);
                        }
                        saveCacheData(id);
                    }
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Object getObjectExistedInCache(int id, Object model) {
        if (model != null) {
            ArrayList<Object> mListDatas = (ArrayList<Object>) getListCacheData(id);
            if (mListDatas != null && mListDatas.size() > 0) {
                for (Object mObject1 : mListDatas) {
                    if (mObject1.equals(model)) {
                        return mObject1;
                    }
                }
            }
        }
        return null;
    }


    public boolean removeModelInCache(int id, Object model) {
        boolean b = removeModelInCache(getListCacheData(id), model);
        if (b) {
            saveCacheData(id);
        }
        return b;
    }

    private boolean removeModelInCache(ArrayList<?> mListDatas, Object model) {
        try {
            if (mListDatas != null && mListDatas.size() > 0 && model != null) {
                Iterator<?> mIterator = mListDatas.iterator();
                while (mIterator.hasNext()) {
                    Object mObject = mIterator.next();
                    if (mObject.equals(model)) {
                        mIterator.remove();
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

    private void readCacheData(YPYSaveModel model) {
        try {
            if (model != null && model.getSaveType() != null) {
                ArrayList<?> mListSavedData = model.getListSavedData();
                if (mListSavedData != null && mListSavedData.size() > 0) {
                    return;
                }
                File mFileCache = cacheListener != null ? cacheListener.getSavePath() : null;
                if (mFileCache != null) {
                    File mFileData = new File(mFileCache, model.getFileName());
                    if (mFileData.exists() && mFileData.isFile()) {
                        try {
                            FileInputStream mFileInputStream = new FileInputStream(mFileData);
                            Gson mGson = new GsonBuilder().create();
                            Type listType = model.getSaveType();
                            ArrayList<?> listSavedData = mGson.fromJson(new InputStreamReader(mFileInputStream), listType);
                            int size = listSavedData != null ? listSavedData.size() : 0;
                            int maxSize = model.getMaximumObject();
                            if (maxSize > 0 && size > maxSize) {
                                List<?> mSublist = listSavedData.subList(0, maxSize);
                                model.setListSavedData(new ArrayList<>(mSublist));
                            }
                            else {
                                model.setListSavedData(listSavedData);
                            }
                            return;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                model.setListSavedData(new ArrayList<>());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


    public boolean isCacheExisted(int id) {
        try {
            File mFile = cacheListener != null ? cacheListener.getSavePath() : null;
            if (mFile != null) {
                YPYSaveModel model = getSaveMode(id);
                if (model != null && model.getSaveType() != null) {
                    File mCacheFile = new File(mFile, model.getFileName());
                    if (mCacheFile.exists() && mCacheFile.isFile()) {
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

    public synchronized void saveCacheData(int id) {
        try {
            File mFile = cacheListener != null ? cacheListener.getSavePath() : null;
            if (mFile != null) {
                YPYSaveModel model = getSaveMode(id);
                if (model != null && model.getSaveType() != null) {
                    String data = "[]";
                    ArrayList<?> mListSave = model.getListSavedData();
                    if (mListSave != null && mListSave.size() > 0) {
                        Gson mGson = new GsonBuilder().create();
                        Type listType = model.getSaveType();
                        data = mGson.toJson(mListSave, listType);
                    }
                    YPYLog.e("DCM", "===>saveCacheData=" + (mListSave != null ? mListSave.size() : 0) + "=>id=" + id);
                    IOUtils.writeString(mFile.getAbsolutePath(), model.getFileName(), data);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }

    public ArrayList<?> getListCacheData(int id) {
        try {
            YPYSaveModel model = getSaveMode(id);
            if (model != null) {
                return model.getListSavedData();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setListCacheData(int id, ArrayList<?> listData) {
        try {
            YPYSaveModel model = getSaveMode(id);
            if (model != null) {
                model.setListSavedData(listData);
                saveCacheData(id);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        try {
            if (listSaveModels != null) {
                if (listSaveModels.size() > 0) {
                    for (YPYSaveModel model : listSaveModels) {
                        model.onDestroy();
                    }
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


}
