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

package com.ypyglobal.xradio.ypylibs.executor;

import android.os.Process;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: www.ypyglobal.com
 */
public class YPYExecutorSupplier {
    public static final String TAG =YPYExecutorSupplier.class.getSimpleName();
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private ThreadPoolExecutor mForBackgroundTasks;
    private ThreadPoolExecutor mForLightWeightBackgroundTasks;
    private MainThreadExecutor mMainThreadExecutor;
    private static YPYExecutorSupplier sInstance;
    private PriorityThreadFactory mBgThreadPiority;

    public static YPYExecutorSupplier getInstance() {
        if (sInstance == null) {
            synchronized (YPYExecutorSupplier.class) {
                sInstance = new YPYExecutorSupplier();
            }
        }
        return sInstance;
    }

    private YPYExecutorSupplier() {
        // setting the thread factory
        mBgThreadPiority = new PriorityThreadFactory(Process.THREAD_PRIORITY_BACKGROUND);
        mForBackgroundTasks = new ThreadPoolExecutor(
                NUMBER_OF_CORES * 2,
                NUMBER_OF_CORES * 2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                mBgThreadPiority
        );

        mForLightWeightBackgroundTasks = new ThreadPoolExecutor(
                NUMBER_OF_CORES * 2,
                NUMBER_OF_CORES * 2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                mBgThreadPiority
        );

        mMainThreadExecutor = new MainThreadExecutor();
    }


    public ThreadPoolExecutor forBackgroundTasks() {
        return mForBackgroundTasks;
    }

    public ThreadPoolExecutor forLightWeightBackgroundTasks() {
        return mForLightWeightBackgroundTasks;
    }

    public Executor forMainThreadTasks() {
        return mMainThreadExecutor;
    }

    public void onDestroy() {
        try {
            if (mBgThreadPiority != null) {
                mBgThreadPiority.onDestroy();
                mBgThreadPiority=null;
            }
            if (mMainThreadExecutor != null) {
                mMainThreadExecutor.onDestroy();
                mMainThreadExecutor=null;
            }
            mForLightWeightBackgroundTasks=null;
            mForBackgroundTasks=null;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        sInstance=null;

    }
}
