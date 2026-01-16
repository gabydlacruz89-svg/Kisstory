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
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: www.ypyglobal.com
 * @Date:Oct 20, 2017
 */
public class ShareActionUtils {

	public static final String TAG = ShareActionUtils.class.getSimpleName();

	public static void shareViaEmail(Activity mActivity, String destEmail, String subject, String body) {
		try {
			Intent sharingIntent = new Intent(Intent.ACTION_SEND);
			if (!TextUtils.isEmpty(destEmail) && EmailUtils.isEmailAddressValid(destEmail)) {
				sharingIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { destEmail });
			}
			sharingIntent.setType("message/rfc822");
			if (!TextUtils.isEmpty(subject)) {
				sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
			}
			if (!TextUtils.isEmpty(body)) {
				sharingIntent.putExtra(Intent.EXTRA_TEXT, body);
			}
			mActivity.startActivity(sharingIntent);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(mActivity, "Can not share via email!Please try again", Toast.LENGTH_LONG).show();
		}
	}

	public static void goToUrl(Activity mActivity, String mUrl) {
		try {
			Intent mIt = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
			mIt.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			mIt.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			mIt.addFlags(Intent.FLAG_FROM_BACKGROUND);
			mActivity.startActivity(mIt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void shareInfo(Activity mActivity, String content) {
		try {
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.setType("text/plain");
			sendIntent.putExtra(Intent.EXTRA_TEXT, content);
			mActivity.startActivity(Intent.createChooser(sendIntent ,"Share"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
