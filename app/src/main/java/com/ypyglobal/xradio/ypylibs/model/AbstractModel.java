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

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 * Created by YPY Global on 10/27/17.
 */

public class AbstractModel {

    @SerializedName("id")
    protected long id;

    @SerializedName("name")
    protected String name;

    @SerializedName("img")
    protected String image;

    protected transient boolean isFavorite;


    public AbstractModel(long id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public String getArtWork(String urlHost){
        return image;
    }

    public String getShareStr(){
        return null;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public AbstractModel cloneObject(){
        return null;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractModel) {
            AbstractModel model = (AbstractModel) obj;
            return id != 0 && id == model.getId();
        }
        return false;
    }
}
