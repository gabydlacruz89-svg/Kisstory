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

package com.ypyglobal.xradio.ypylibs.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 * Created by YPY Global on 10/25/17.
 */

public class ResultModel<T>{

    @SerializedName("status")
    private int status;

    @SerializedName("msg")
    private String msg;

    @SerializedName("datas")
    private ArrayList<T> listModels;

    public ResultModel(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public boolean isResultOk() {
        return status == 200;
    }


    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ArrayList<T> getListModels() {
        return listModels;
    }

    public void setListModels(ArrayList<T> listModels) {
        this.listModels = listModels;
    }
}
