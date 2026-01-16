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

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 * Created by YPY Global on 10/19/17.
 */

public class YPYSaveModel {
    private int id;
    private final Type saveType;
    private final String fileName;
    private ArrayList<?> listSavedData;
    private int maximumObject;

    YPYSaveModel(int id, Type saveType, String fileName) {
        this.id = id;
        this.saveType = saveType;
        this.fileName=fileName;
    }
    YPYSaveModel(int id) {
        this(id,null,null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    Type getSaveType() {
        return saveType;
    }


    ArrayList<?> getListSavedData() {
        return listSavedData;
    }

    void setListSavedData(ArrayList<?> listSavedData) {
        this.listSavedData = listSavedData;
    }

    String getFileName() {
        return fileName;
    }


    public void onDestroy(){
        try{
            if(listSavedData!=null){
                listSavedData.clear();
                listSavedData=null;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    int getMaximumObject() {
        return maximumObject;
    }

    void setMaximumObject(int maximumObject) {
        this.maximumObject = maximumObject;
    }
}
