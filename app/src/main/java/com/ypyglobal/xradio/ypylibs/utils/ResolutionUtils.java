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

package com.ypyglobal.xradio.ypylibs.utils;
import android.app.Activity;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: www.ypyglobal.com
 */
public class ResolutionUtils {
	
	public static int[] getDeviceResolution(Activity mContext){
		int[] res = null;
		DisplayMetrics metrics = new DisplayMetrics();
		mContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;

		int i = mContext.getResources().getConfiguration().orientation;
		if (i == Configuration.ORIENTATION_PORTRAIT){
			res = new int[2];
			int finalWidth = Math.min(height, width);
			int finalHeight = Math.max(height, width);
			res[0]= finalWidth;
			res[1]= finalHeight;
		}
		else if (i == Configuration.ORIENTATION_LANDSCAPE){
			res = new int[2];
			int finalWidth = Math.max(height, width);
			int finalHeight = Math.min(height, width);
			res[0]= finalWidth;
			res[1]= finalHeight;
		}
		return res;
	}
}
