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

package com.ypyglobal.xradio.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.ypyglobal.xradio.model.ThemeModel;


import static com.ypyglobal.xradio.constants.IXRadioConstants.DEFAULT_END_COLOR;
import static com.ypyglobal.xradio.constants.IXRadioConstants.DEFAULT_START_COLOR;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: www.ypyglobal.com
 * @Date:Oct 20, 2017
 */

public class XRadioSettingManager implements IXRadioSettingConstants {
	

	private static final String NAME_SHARPREFS = "app_prefs";

	private static void saveSetting(Context mContext, String mKey, String mValue){
		try{
			if(mContext!=null){
				SharedPreferences mSharedPreferences =mContext.getSharedPreferences(NAME_SHARPREFS, Context.MODE_PRIVATE);
				if(mSharedPreferences!=null){
					Editor editor = mSharedPreferences.edit();
					editor.putString(mKey, mValue);
					editor.apply();
				}
			}


		}
		catch (Exception e){
			e.printStackTrace();
		}

	}
	
	private static String getSetting(Context mContext, String mKey, String mDefValue){
		try{
			if(mContext!=null){
				SharedPreferences mSharedPreferences =mContext.getSharedPreferences(NAME_SHARPREFS, Context.MODE_PRIVATE);
				if(mSharedPreferences!=null){
					return mSharedPreferences.getString(mKey, mDefValue);
				}
			}

		}
		catch (Exception e){
			e.printStackTrace();
		}
		return mDefValue;

	}

	public static void setOnline(Context mContext, boolean mValue){
		saveSetting(mContext, KEY_ONLINE, String.valueOf(mValue));
	}
	public static boolean getOnline(Context mContext){
		return Boolean.parseBoolean(getSetting(mContext, KEY_ONLINE, "false"));
	}


	public static void setRateApp(Context mContext, boolean mValue){
		saveSetting(mContext, KEY_RATE_APP, String.valueOf(mValue));
	}
	public static boolean getRateApp(Context mContext){
		return Boolean.parseBoolean(getSetting(mContext, KEY_RATE_APP, "false"));
	}

	public static void setPivotTime(Context mContext, long mValue){
		saveSetting(mContext, KEY_PIVOT_TIME, String.valueOf(mValue));
	}
	public static long getPivotTime(Context mContext){
		return Long.parseLong(getSetting(mContext, KEY_PIVOT_TIME, "0"));
	}

	private static void setBackgroundUrl(Context mContext, String mValue){
		saveSetting(mContext, KEY_BACKGROUND,mValue);
	}

	public static String getBackgroundUrl(Context mContext){
		return getSetting(mContext, KEY_BACKGROUND, "");
	}


	private static void setStartColor(Context mContext, String mValue){
		saveSetting(mContext, KEY_START_COLOR,mValue);
	}

	public static String getStartColor(Context mContext){
		return getSetting(mContext, KEY_START_COLOR, DEFAULT_START_COLOR);
	}

	private static void setOrientation(Context mContext, int mValue){
		saveSetting(mContext, KEY_ORIENTATION, String.valueOf(mValue));
	}

	public static int getOrientation(Context mContext){
		return Integer.parseInt(getSetting(mContext, KEY_ORIENTATION, "315"));
	}

	private static void setThemId(Context mContext, long mValue){
		saveSetting(mContext, KEY_THEMES_ID, String.valueOf(mValue));
	}

	public static int getThemId(Context mContext){
		return Integer.parseInt(getSetting(mContext, KEY_THEMES_ID, "0"));
	}

	private static void setEndColor(Context mContext, String mValue){
		saveSetting(mContext, KEY_END_COLOR, mValue);
	}

	public static String getEndColor(Context mContext){
		return getSetting(mContext, KEY_END_COLOR, DEFAULT_END_COLOR);
	}

	public static void saveThemes(Context mContext,ThemeModel model,String urlHost){
		try{
			if(model!=null){
				if(model.getId()>0){
					setThemId(mContext,model.getId());
				}
				if(!TextUtils.isEmpty(model.getGradStartColor())){
					setStartColor(mContext,model.getGradStartColor());
				}
				if(!TextUtils.isEmpty(model.getGradEndColor())){
					setEndColor(mContext,model.getGradEndColor());
				}
				String artWork=model.getArtWork(urlHost);
				if(!TextUtils.isEmpty(artWork)){
					setBackgroundUrl(mContext,artWork);
				}
				else{
					setBackgroundUrl(mContext,"");
				}
				if(model.getGradOrientation()>0){
					setOrientation(mContext,model.getGradOrientation());
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}

	}

	public static int getSleepMode(Context mContext){
		return Integer.parseInt(getSetting(mContext, KEY_TIME_SLEEP, "0"));
	}

	public static void setSleepMode(Context mContext, int mValue){
		saveSetting(mContext, KEY_TIME_SLEEP, String.valueOf(mValue));
	}

	public static void setAgreeTerm(Context mContext, boolean mValue){
		saveSetting(mContext, KEY_AGREE_TERM, String.valueOf(mValue));
	}
	public static boolean getAgreeTerm(Context mContext){
		return Boolean.parseBoolean(getSetting(mContext, KEY_AGREE_TERM, "false"));
	}


	public static void setSkipPermission(Context mContext, boolean mValue){
		saveSetting(mContext, KEY_SKIP_PERMISSION, String.valueOf(mValue));
	}
	public static boolean getSkipPermission(Context mContext){
		return Boolean.parseBoolean(getSetting(mContext, KEY_SKIP_PERMISSION, "false"));
	}


}
