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

package com.behavior.model;

import android.content.Context;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author:YPY Global

 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 * Created by YPY Global on 10/19/17.
 */

public class YPYBottomSheetBehavior<V extends View> extends BottomSheetBehavior<V> {

    private boolean mAllowUserDragging = true;

    public YPYBottomSheetBehavior() {

    }

    public YPYBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAllowUserDragging(boolean allowUserDragging) {
        mAllowUserDragging = allowUserDragging;
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
        try{
            if (!mAllowUserDragging) {
                return false;
            }
            return super.onInterceptTouchEvent(parent, child, event);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

}
