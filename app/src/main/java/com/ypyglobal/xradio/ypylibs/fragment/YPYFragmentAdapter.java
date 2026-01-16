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

package com.ypyglobal.xradio.ypylibs.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;


/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: www.ypyglobal.com
 */
public class YPYFragmentAdapter extends FragmentStatePagerAdapter {

	public static final String TAG = YPYFragmentAdapter.class.getSimpleName();
	private static final String TAG_VIEWS = "tagViews";
	private final ViewPager mViewPager;
	private SparseArray<Parcelable> mViewStates = new SparseArray<Parcelable>();

	private final ArrayList<Fragment> listFragments;

	public YPYFragmentAdapter(FragmentManager fm, ArrayList<Fragment> listFragments,ViewPager mViewPager) {
		super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
		this.listFragments = listFragments;
		this.mViewPager=mViewPager;
	}

	@NonNull
	@Override
	public Fragment getItem(int position) {
		return listFragments.get(position);
	}

	@Override
	public int getCount() {
		return listFragments.size();
	}

	@Override
	public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object pObject) {
		try {
			container.removeView((View) pObject);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Parcelable saveState() {
		try{
			final int count = mViewPager.getChildCount();
			for (int i = 0; i < count; i++) {
				View c = mViewPager.getChildAt(i);
				if (c.isSaveFromParentEnabled()) {
					c.saveHierarchyState(mViewStates);
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		Bundle bundle = new Bundle();
		bundle.putSparseParcelableArray(TAG_VIEWS, mViewStates);
		return bundle;
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
		try{
			Bundle bundle = (Bundle) state;
			bundle.setClassLoader(loader);
			mViewStates = bundle.getSparseParcelableArray(TAG_VIEWS);
		}
		catch (Exception e){
			e.printStackTrace();
		}

	}
}
