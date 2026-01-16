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

package com.ypyglobal.xradio.stream.manager;


import com.ypyglobal.xradio.model.RadioModel;
import com.ypyglobal.xradio.stream.mediaplayer.YPYMediaPlayer;

import java.util.ArrayList;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 * Created by YPY Global on 10/19/17.
 */

public class YPYStreamManager {

    private static YPYStreamManager instance;
    private ArrayList<RadioModel> listModels;

    private int currentIndex=-1;
    private RadioModel currentData;
    private boolean isLoading;
    private YPYMediaPlayer radioMediaPlayer;
    private YPYMediaPlayer.StreamInfo streamInfo;

    public static YPYStreamManager getInstance() {
        if (instance == null) {
            instance = new YPYStreamManager();
        }
        return instance;
    }

    private YPYStreamManager() {

    }

    public void onDestroy() {
        if (listModels != null) {
            listModels.clear();
            listModels = null;
        }
        currentIndex = -1;
        currentData = null;
        instance = null;
    }

    public ArrayList<RadioModel> getListModels() {
        return listModels;
    }

    public boolean setCurrentData(RadioModel RadioModel){
        if(listModels !=null && listModels.size()>0){
            for(RadioModel mStreamRadioObject1: listModels){
                if(mStreamRadioObject1.getId()==RadioModel.getId()){
                    currentData=mStreamRadioObject1;
                    currentIndex= listModels.indexOf(mStreamRadioObject1);
                    return true;
                }
            }
        }
        return false;
    }


    public void setListModels(ArrayList<RadioModel> listModels) {
        if(this.listModels !=null){
            this.listModels.clear();
            this.listModels =null;
        }
        this.currentIndex=-1;
        this.currentData=null;
        this.listModels = listModels;
        int size = listModels !=null ? listModels.size():0;
        if(size>0){
            currentIndex=0;
            currentData= listModels.get(currentIndex);
        }
    }

    public RadioModel getCurrentRadio() {
        return currentData;
    }

    public RadioModel nextPlay(){
        int size = listModels !=null ? listModels.size():0;
        if(size>0){
            currentIndex++;
            if(currentIndex>=size){
                currentIndex=0;
            }
            currentData= listModels.get(currentIndex);
            return currentData;
        }
        return null;
    }
    public RadioModel prevPlay(){
        int size = listModels !=null ? listModels.size():0;
        if(size>0){
            currentIndex--;
            if(currentIndex<0){
                currentIndex=size-1;
            }
            currentData= listModels.get(currentIndex);
            return currentData;
        }
        return null;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public boolean isPlaying(){
        try{
            if(radioMediaPlayer !=null){
                return radioMediaPlayer.isPlaying();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public boolean isPrepareDone(){
        return radioMediaPlayer != null;
    }

    public void onResetMedia(){
        radioMediaPlayer =null;
        streamInfo=null;
    }

    public void setRadioMediaPlayer(YPYMediaPlayer radioMediaPlayer) {
        this.radioMediaPlayer = radioMediaPlayer;
    }

    public YPYMediaPlayer.StreamInfo getStreamInfo() {
        return streamInfo;
    }

    public void setStreamInfo(YPYMediaPlayer.StreamInfo streamInfo) {
        this.streamInfo = streamInfo;
    }

    public boolean isHavingList(){
        try{
            return listModels != null && listModels.size() > 0;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;

    }

}
