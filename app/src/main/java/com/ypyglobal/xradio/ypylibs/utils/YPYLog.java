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

import android.util.Log;

/**
 * @author:YPY Global

 * @Email: bl911vn@gmail.com
 * @Website: www.ypyglobal.com
 * @Date:Oct 20, 2017
 */
public class YPYLog {
	
	private static boolean LOG = false;

    public static void i(String tag, String string) {
        if (LOG) {
        	Log.i(tag, string);
        }
    }
    public static void e(String tag, String string) {
        if (LOG) {
        	Log.e(tag, string);
        }
    }
    public static void d(String tag, String string) {
        if (LOG) {
        	Log.d(tag, string);
        }
    }
    public static void v(String tag, String string) {
        if (LOG) {
        	Log.v(tag, string);
        }
    }
    public static void w(String tag, String string) {
        if (LOG) {
        	Log.w(tag, string);
        }
    }
    public static void setDebug(boolean b){
    	LOG=b;
    }
}
