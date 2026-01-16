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

package com.ypyglobal.xradio.stream.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.ypyglobal.xradio.R;
import com.ypyglobal.xradio.XMultiRadioMainActivity;
import com.ypyglobal.xradio.XSingleRadioMainActivity;
import com.ypyglobal.xradio.dataMng.TotalDataManager;
import com.ypyglobal.xradio.dataMng.XRadioNetUtils;
import com.ypyglobal.xradio.model.RadioModel;
import com.ypyglobal.xradio.setting.XRadioSettingManager;
import com.ypyglobal.xradio.stream.audiofocus.AudioFocusHelper;
import com.ypyglobal.xradio.stream.audiofocus.IStreamFocusableListener;
import com.ypyglobal.xradio.stream.constant.IYPYStreamConstants;
import com.ypyglobal.xradio.stream.manager.YPYStreamManager;
import com.ypyglobal.xradio.stream.mediaplayer.YPYMediaPlayer;
import com.ypyglobal.xradio.stream.ssl.XRadioHttpsTrustManager;
import com.ypyglobal.xradio.ypylibs.executor.YPYExecutorSupplier;
import com.ypyglobal.xradio.ypylibs.utils.ApplicationUtils;
import com.ypyglobal.xradio.ypylibs.utils.IOUtils;
import com.ypyglobal.xradio.ypylibs.utils.YPYLog;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;


import static com.ypyglobal.xradio.constants.IXRadioConstants.AUTO_NEXT_WHEN_COMPLETE;
import static com.ypyglobal.xradio.constants.IXRadioConstants.ENABLE_CUSTOMIZE_SSL;
import static com.ypyglobal.xradio.constants.IXRadioConstants.IS_MUSIC_PLAYER;
import static com.ypyglobal.xradio.constants.IXRadioConstants.TAG;


public class YPYStreamService extends Service implements IYPYStreamConstants {

    public static final String ANDROID8_CHANNEL_ONE_NAME = "XRadioChannel";
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_PAUSE = 3;
    public static final int STATE_STOP = 4;
    public static final int STATE_ERROR = 5;
    public static final int STATE_COMPLETE = 6;
    public static final int STATE_CONNECTION_LOST = 7;

    private int mCurrentState = STATE_STOP;

    private static final float MAX_VOLUME = 1f;
    public static final float DUCK_VOLUME = 0.2f;

    private AudioFocusHelper mAudioFocusHelper;
    private RadioModel mCurrentTrack;
    private boolean isStartLoading;

    private boolean isPlayOnAudioFocus;
    private boolean isPauseFromUser;

    private final Handler mHandlerSleep = new Handler();
    private int mMinuteCount;
    private Bitmap mBitmap;


    private static final IntentFilter AUDIO_NOISY_INTENT_FILTER =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private boolean isAudioNoisyReceiverRegistered;

    private final BroadcastReceiver mAudioNoisyReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(@NonNull Context context, @NonNull Intent intent) {
                    if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                        if (isPlaying()) {
                            pause();
                        }
                    }
                }
            };

    private YPYMediaPlayer mRadioMediaPlayer;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createAudioFocus();
    }

    private void createAudioFocus() {
        mAudioFocusHelper = new AudioFocusHelper(this.getApplicationContext(), new IStreamFocusableListener() {
            @Override
            public void onGrantAudioFocus() {
                if (isPlayOnAudioFocus && !isPlaying()) {
                    play();
                }
                else if (isPlaying()) {
                    setVolume(MAX_VOLUME);
                }
                isPlayOnAudioFocus = false;
            }

            @Override
            public void onLostAudioFocus(boolean canDuck) {
                if (canDuck) {
                    setVolume(DUCK_VOLUME);
                    return;
                }
                if (isPlaying()) {
                    isPlayOnAudioFocus = true;
                    pause();
                }
            }

            @Override
            public void onGiveUpAudioFocus() {
                isPlayOnAudioFocus = false;
                pause();
            }
        });
    }

    private void registerAudioNoisyReceiver() {
        if (!isAudioNoisyReceiverRegistered) {
            if(IOUtils.isAndroid14()){
                this.registerReceiver(mAudioNoisyReceiver, AUDIO_NOISY_INTENT_FILTER,RECEIVER_EXPORTED);
            }
            else{
                this.registerReceiver(mAudioNoisyReceiver, AUDIO_NOISY_INTENT_FILTER);
            }
            isAudioNoisyReceiverRegistered = true;
        }
    }

    private void unregisterAudioNoisyReceiver() {
        if (isAudioNoisyReceiverRegistered) {
            this.unregisterReceiver(mAudioNoisyReceiver);
            isAudioNoisyReceiverRegistered = false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                String packageName = getPackageName();
                if (action.equalsIgnoreCase(packageName + ACTION_TOGGLE_PLAYBACK)) {
                    if (mCurrentState == STATE_COMPLETE || mCurrentState == STATE_CONNECTION_LOST) {
                        startSleepMode();
                        onActionPlay();
                    }
                    else {
                        isPauseFromUser = mCurrentState == STATE_PLAYING;
                        onActionTogglePlay();
                    }
                }
                else if (action.equalsIgnoreCase(packageName + ACTION_PLAY)) {
                    setUpNotification();
                    startSleepMode();
                    onActionPlay();
                }
                else if (action.equalsIgnoreCase(packageName + ACTION_NEXT)) {
                    setUpNotification();
                    onActionNext();
                }
                else if (action.equalsIgnoreCase(packageName + ACTION_PREVIOUS)) {
                    setUpNotification();
                    onActionPrevious();
                }
                else if (action.equalsIgnoreCase(packageName + ACTION_STOP)) {
                    setUpNotification();
                    onActionStop();
                }
                else if (action.equals(packageName + ACTION_UPDATE_SLEEP_MODE)) {
                    startSleepMode();
                }
                else if (action.equals(packageName + ACTION_CONNECTION_LOST)) {
                    mCurrentState = STATE_CONNECTION_LOST;
                    onActionComplete();
                    sendMusicBroadcast(ACTION_CONNECTION_LOST);
                }
            }
        }
        return START_NOT_STICKY;
    }

    private void startSleepMode() {
        try {
            int minute = XRadioSettingManager.getSleepMode(this);
            mHandlerSleep.removeCallbacksAndMessages(null);
            if (minute > 0) {
                this.mMinuteCount = minute * ONE_MINUTE;
                startCountSleep();
            }
            else {
                sendMusicBroadcast(ACTION_UPDATE_SLEEP_MODE, 0);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCountSleep() {
        try {
            if (mMinuteCount > 0) {
                mHandlerSleep.postDelayed(() -> {
                    mMinuteCount = mMinuteCount - 1000;
                    sendMusicBroadcast(ACTION_UPDATE_SLEEP_MODE, mMinuteCount);
                    if (mMinuteCount <= 0) {
                        onActionStop();
                    }
                    else {
                        startCountSleep();
                    }
                }, 1000);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void onActionStop() {
        isStartLoading = false;
        isPauseFromUser = false;
        destroyMedia();
        boolean isError = mCurrentState == STATE_ERROR;
        sendMusicBroadcast(isError ? ACTION_ERROR : ACTION_STOP);
    }

    private void onActionPrevious() {
        try {
            isPauseFromUser = false;
            mCurrentTrack = YPYStreamManager.getInstance().prevPlay();
            if (mCurrentTrack != null) {
                startPlayNewSong();
            }
            else {
                mCurrentState = STATE_ERROR;
                onActionStop();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onActionNext() {
        try {
            isPauseFromUser = false;
            mCurrentTrack = YPYStreamManager.getInstance().nextPlay();
            if (mCurrentTrack != null) {
                startPlayNewSong();
            }
            else {
                mCurrentState = STATE_ERROR;
                onActionStop();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onActionComplete() {
        isStartLoading = false;
        try {
            if (mRadioMediaPlayer != null) {
                releasePlayer(false);
            }
            setUpNotification();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onActionPlay() {
        isPauseFromUser = false;
        processPlayRequest(true);
    }

    private void onActionTogglePlay() {
        try {
            if (mCurrentState == STATE_PAUSE || mCurrentState == STATE_STOP) {
                processPlayRequest(false);
            }
            else {
                processPauseRequest();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processPauseRequest() {
        if (mCurrentTrack == null || mRadioMediaPlayer == null) {
            mCurrentState = STATE_ERROR;
            onActionStop();
            return;
        }
        try {
            if (mCurrentState == STATE_PLAYING) {
                if (IS_MUSIC_PLAYER) {
                    mCurrentState = STATE_PAUSE;
                    mRadioMediaPlayer.pause();
                }
                else {
                    mCurrentState = STATE_COMPLETE;
                    mRadioMediaPlayer.stop();
                }
                setUpNotification();
                sendMusicBroadcast(ACTION_PAUSE);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            onActionNext();
        }
    }

    private void processPlayRequest(boolean isForces) {
        mCurrentTrack = YPYStreamManager.getInstance().getCurrentRadio();
        if (mCurrentTrack == null) {
            mCurrentState = STATE_ERROR;
            onActionStop();
            return;
        }
        if (mCurrentState == STATE_STOP || mCurrentState == STATE_PLAYING || isForces) {
            startPlayNewSong();
            sendMusicBroadcast(ACTION_NEXT);
            return;
        }
        if (mCurrentState == STATE_PAUSE && mAudioFocusHelper != null && mAudioFocusHelper.requestFocus()) {
            mCurrentState = STATE_PLAYING;
            registerAudioNoisyReceiver();
            mRadioMediaPlayer.start();
            setUpNotification();
            sendMusicBroadcast(ACTION_PLAY);
        }

    }

    private synchronized void startPlayNewSong() {
        if (!isStartLoading) {
            mCurrentState = STATE_STOP;
            isStartLoading = true;
            if (mCurrentTrack == null) {
                mCurrentState = STATE_ERROR;
                onActionStop();
                return;
            }
            startStreamMusic();
        }

    }

    private synchronized void startStreamMusic() {
        releasePlayer(true);
        sendMusicBroadcast(ACTION_LOADING);
        setUpNotification();
        YPYStreamManager.getInstance().setLoading(true);
        YPYExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
            final String uriStream = mCurrentTrack.getLinkRadio(this);
            if (ENABLE_CUSTOMIZE_SSL && uriStream != null &&
                    !TextUtils.isEmpty(uriStream) && uriStream.startsWith("https")) {
                XRadioHttpsTrustManager.trustAllHttps();
            }
            YPYExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                setUpMediaForStream(uriStream);
                isStartLoading = false;
            });

        });
    }

    private void setUpMediaForStream(final String path) {
        createRadioMediaPlayer();
        try {
            if (mRadioMediaPlayer != null) {
                mCurrentState = STATE_PREPARING;
                mRadioMediaPlayer.setDataSource(path);

            }
        }
        catch (Exception ex) {
            Log.d(TAG, "IOException playing next song: " + ex.getMessage());
            ex.printStackTrace();
            onActionStop();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyMedia();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendMusicBroadcast(String action) {
        sendMusicBroadcast(action, -1);
    }

    private void sendMusicBroadcast(String action, long value) {
        try {
            Intent mIntent = new Intent(getPackageName() + ACTION_BROADCAST_PLAYER);
            mIntent.putExtra(KEY_ACTION, action);
            if (value != -1) {
                mIntent.putExtra(KEY_VALUE, value);
            }
            sendBroadcast(mIntent);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMusicBroadcast(String action, String value) {
        try {
            Intent mIntent = new Intent(getPackageName() + ACTION_BROADCAST_PLAYER);
            mIntent.putExtra(KEY_ACTION, action);
            mIntent.putExtra(KEY_VALUE, value);
            sendBroadcast(mIntent);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isSupportRTL() {
        try {
            return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    private void setUpNotification() {
        try {
            boolean isSingle = TotalDataManager.getInstance(getApplicationContext()).isSingleRadio();
            String packageName = getPackageName();

            Intent mIntent = new Intent(this.getApplicationContext(), isSingle ? XSingleRadioMainActivity.class : XMultiRadioMainActivity.class);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            int flag = isAndroid12() ? (PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT) : PendingIntent.FLAG_UPDATE_CURRENT;
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), NOTIFICATION_ID, mIntent, flag);

            String CHANNEL_ONE_ID = getPackageName() + ".N2";
            String CHANNEL_ONE_NAME = getPackageName() + ANDROID8_CHANNEL_ONE_NAME;
            if (IOUtils.hasAndroid80()) {
                try {
                    NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                            CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_LOW);
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.setShowBadge(true);
                    notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                    mNotificationManager.createNotificationChannel(notificationChannel);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            int flagPendingIntent = isAndroid12() ? PendingIntent.FLAG_IMMUTABLE : 0;

            Intent stopIntent = new Intent(this, YPYIntentReceiver.class);
            stopIntent.setAction(packageName + ACTION_STOP);
            PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 100, stopIntent, flagPendingIntent);

            Intent toggleIntent = new Intent(this, YPYIntentReceiver.class);
            toggleIntent.setAction(packageName + ACTION_TOGGLE_PLAYBACK);
            PendingIntent pendingToggleIntent = PendingIntent.getBroadcast(this, 100, toggleIntent, flagPendingIntent);

            boolean isPlay = YPYStreamManager.getInstance().isPlaying();
            int resDrawable = isPlay ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_arrow_white_36dp;
            NotificationCompat.Action mPlayAction = new NotificationCompat.Action(resDrawable, "Toggle", pendingToggleIntent);
            NotificationCompat.Action mStopAction = new NotificationCompat.Action(R.drawable.ic_close_white_36dp, "Stop", stopPendingIntent);

            androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle();
            if (isSingle) {
                mediaStyle.setShowActionsInCompactView(0, 1);
            }
            else {
                mediaStyle.setShowActionsInCompactView(0, 1, 2);
            }
            mediaStyle.setCancelButtonIntent(stopPendingIntent);
            mediaStyle.setShowCancelButton(true);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ONE_ID);
            mBuilder.setStyle(mediaStyle);
            mBuilder.setSmallIcon(R.drawable.ic_notification_24dp);
            mBuilder.setLargeIcon(getBitmap());
            mBuilder.setColorized(true);
            mBuilder.setShowWhen(true);
            if (isSingle) {
                mBuilder.addAction(mPlayAction);
                mBuilder.addAction(mStopAction);
            }
            else {
                Intent nextIntent = new Intent(this, YPYIntentReceiver.class);
                nextIntent.setAction(packageName + ACTION_NEXT);
                PendingIntent pendingNextIntent = PendingIntent.getBroadcast(this, 100, nextIntent, flagPendingIntent);
                int nextDrawable = isSupportRTL() ? R.drawable.ic_skip_previous_white_36dp : R.drawable.ic_skip_next_white_36dp;
                NotificationCompat.Action mNextAction = new NotificationCompat.Action(nextDrawable, "Next", pendingNextIntent);

                mBuilder.addAction(mPlayAction);
                mBuilder.addAction(mNextAction);
                mBuilder.addAction(mStopAction);
            }
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            mBuilder.setOngoing(isPlay);
            mBuilder.setAutoCancel(false);
            mBuilder.setDeleteIntent(stopPendingIntent);

            String contentTitle = mCurrentTrack != null ? mCurrentTrack.getName() : getString(R.string.app_name);
            String contentInfo = mCurrentTrack != null ? mCurrentTrack.getTags() : getString(R.string.title_unknown);
            if (mCurrentTrack != null && !TextUtils.isEmpty(mCurrentTrack.getSong())) {
                contentInfo = mCurrentTrack.getMetaData();
            }
            mBuilder.setContentTitle(contentTitle);
            mBuilder.setContentText(contentInfo);
            mBuilder.setColor(ContextCompat.getColor(this, R.color.color_noti_background));

            Notification mNotification = mBuilder.build();
            mNotification.contentIntent = pi;
            mNotification.flags |= Notification.FLAG_NO_CLEAR;
            if (IOUtils.isAndroid14()) {
                startForeground(NOTIFICATION_ID, mNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            }
            else {
                startForeground(NOTIFICATION_ID, mNotification);
            }


        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Bitmap getBitmap() {
        try {
            if (mBitmap == null || mBitmap.isRecycled()) {
                mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_rect_img_default);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return mBitmap;
    }

    private boolean isAndroid12() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }

    private void createRadioMediaPlayer() {
        try {
            String userAgent = mCurrentTrack != null ? mCurrentTrack.getUserAgentRadio() : null;
            mRadioMediaPlayer = new YPYMediaPlayer(this, userAgent);
            mRadioMediaPlayer.setOnStreamListener(new YPYMediaPlayer.OnStreamListener() {
                @Override
                public void onPrepare() {
                    sendMusicBroadcast(ACTION_DIMINISH_LOADING);
                    YPYStreamManager.getInstance().setLoading(false);
                    if (mAudioFocusHelper != null && mAudioFocusHelper.requestFocus()) {
                        mCurrentState = STATE_PLAYING;
                        registerAudioNoisyReceiver();
                        mRadioMediaPlayer.start();
                        setUpNotification();
                        sendMusicBroadcast(ACTION_PLAY);
                    }
                }

                @Override
                public void onError() {
                    try {
                        YPYStreamManager.getInstance().setLoading(false);
                        sendMusicBroadcast(ACTION_DIMINISH_LOADING);
                        mCurrentState = STATE_ERROR;
                        onActionStop();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onComplete() {
                    if (AUTO_NEXT_WHEN_COMPLETE) {
                        mCurrentState = STATE_STOP;
                        onActionNext();
                        sendMusicBroadcast(ACTION_NEXT);
                    }
                    else {
                        mCurrentState = STATE_COMPLETE;
                        onActionComplete();
                        sendMusicBroadcast(ACTION_COMPLETE);
                    }

                }

                @Override
                public void onUpdateMetaData(YPYMediaPlayer.StreamInfo info) {
                    if (!IS_MUSIC_PLAYER) {
                        YPYStreamManager.getInstance().setStreamInfo(info);
                        if (info != null) {
                            String title = info.title;
                            String artist = info.artist;
                            if (mCurrentTrack != null) {
                                mCurrentTrack.setSong(title);
                                mCurrentTrack.setArtist(artist);
                            }
                            sendMusicBroadcast(ACTION_UPDATE_INFO);
                            setUpNotification();
                            startGetImageOfSong(title, artist, info);
                        }
                        else {
                            if (mCurrentTrack != null) {
                                mCurrentTrack.setSong(null);
                                mCurrentTrack.setArtist(null);
                            }
                            setUpNotification();
                            sendMusicBroadcast(ACTION_RESET_INFO);
                        }
                    }

                }
            });
            YPYStreamManager.getInstance().setRadioMediaPlayer(mRadioMediaPlayer);
        }
        catch (Exception e) {
            e.printStackTrace();
            mCurrentState = STATE_ERROR;
            onActionStop();
        }

    }


    private void startGetImageOfSong(String title, String artist, YPYMediaPlayer.StreamInfo mStreamInfo) {
        if (ApplicationUtils.isOnline(this) && mStreamInfo != null && (!TextUtils.isEmpty(title) || !TextUtils.isEmpty(artist))) {
            YPYExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
                String lastFmKey = TotalDataManager.getInstance(getApplicationContext()).getLastFmKey();
                String url = XRadioNetUtils.getImageOfSong(title, artist, lastFmKey);
                YPYLog.e(TAG, "=====>startGetImageOfSong=" + url);
                mStreamInfo.imgUrl = !TextUtils.isEmpty(url) ? url : "";
                sendMusicBroadcast(ACTION_UPDATE_COVER_ART, url);
            });
        }

    }

    private void releasePlayer(boolean isNeedResetState) {
        try {
            onDestroyBitmap();
            unregisterAudioNoisyReceiver();
            if (mAudioFocusHelper != null && isNeedResetState) {
                mAudioFocusHelper.abandonFocus();
            }
            if (mRadioMediaPlayer != null) {
                mRadioMediaPlayer.release();
                YPYStreamManager.getInstance().onResetMedia();
                mRadioMediaPlayer = null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (isNeedResetState) {
            mCurrentState = STATE_STOP;
        }

    }

    private void destroyMedia() {
        mHandlerSleep.removeCallbacksAndMessages(null);
        releasePlayer(true);
        try {
            stopForeground(true);
            stopSelf();
            YPYStreamManager.getInstance().onDestroy();
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void onDestroyBitmap() {
        try {
            if (mBitmap != null) {
                mBitmap.recycle();
                mBitmap = null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isPlaying() {
        return mRadioMediaPlayer != null && mRadioMediaPlayer.isPlaying();
    }

    private void setVolume(float volume) {
        if (mRadioMediaPlayer != null) {
            mRadioMediaPlayer.setVolume(volume);
        }
    }

    private void play() {
        if (mAudioFocusHelper != null && mAudioFocusHelper.requestFocus()) {
            registerAudioNoisyReceiver();
            onActionPlay();
        }
    }

    private void pause() {
        if (mAudioFocusHelper != null) {
            if (!isPlayOnAudioFocus) {
                Log.e("DCM", "=========>pause abandonFocus");
                mAudioFocusHelper.abandonFocus();
            }
        }
        unregisterAudioNoisyReceiver();
        processPauseRequest();

    }
}
