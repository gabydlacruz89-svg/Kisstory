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

package com.ypyglobal.xradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;

import com.google.android.ump.ConsentInformation;
import com.google.android.ump.UserMessagingPlatform;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;
import com.ypyglobal.xradio.ads.RewardedAdsHelper;
import com.ypyglobal.xradio.databinding.ActivitySingleRadioBinding;
import com.ypyglobal.xradio.gdpr.GDPRManager;
import com.ypyglobal.xradio.model.ConfigureModel;
import com.ypyglobal.xradio.model.RadioModel;
import com.ypyglobal.xradio.model.UIConfigModel;
import com.ypyglobal.xradio.setting.XRadioSettingManager;
import com.ypyglobal.xradio.stream.constant.IYPYStreamConstants;
import com.ypyglobal.xradio.stream.manager.YPYStreamManager;
import com.ypyglobal.xradio.stream.mediaplayer.YPYMediaPlayer;
import com.ypyglobal.xradio.ypylibs.imageloader.GlideImageLoader;
import com.ypyglobal.xradio.ypylibs.utils.ApplicationUtils;
import com.ypyglobal.xradio.ypylibs.utils.IOUtils;
import com.ypyglobal.xradio.ypylibs.utils.ShareActionUtils;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;




/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://ypyglobal.com
 * Created by YPY Global on 10/19/17.
 */
public class XSingleRadioMainActivity extends XRadioFragmentActivity<ActivitySingleRadioBinding> implements IYPYStreamConstants, View.OnClickListener {
    private RewardedAdsHelper rewardedHelper;
    private eu.gsottbauer.equalizerview.EqualizerView equalizerView;

    private CropCircleTransformation mCropCircleTransform;
    private int mTypeUI = UI_PLAYER_NO_LAST_FM_ROTATE_DISK;

    private RotateAnimation rotate;

    public String mUrlHost;
    public String mApiKey;
    private ApplicationBroadcast mApplicationBroadcast;
    private AudioManager mAudioManager;
    private BlurTransformation mBlurTransform;

    private int mBgMode;

    @Override
    protected ActivitySingleRadioBinding getViewBinding() {
        return ActivitySingleRadioBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void updateBackground() {
        setUpBackground(viewBinding.layoutBg);
    }

    @Override
    public void onDoWhenDone() {
        super.onDoWhenDone();

        rewardedHelper = new RewardedAdsHelper(this);
        rewardedHelper.loadRewardedAd();

        XRadioSettingManager.setOnline(this, true);
        setIsAllowPressMoreToExit(true);

        resetTimer();
        setUpActionBar();
        showAppRate();
        setUpColorWidget();

        mCropCircleTransform = new CropCircleTransformation();

        if (USE_BLUR_EFFECT) {
            mBlurTransform = new BlurTransformation();
        }
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        updateVolume();
        setUpOnClick();
        UIConfigModel model = mTotalMng.getUiConfigModel();
        mTypeUI = model != null ? model.getUiPlayer() : UI_PLAYER_NO_LAST_FM_ROTATE_DISK;

        updateInfoOfPlayingTrack();
        initEqualizerSafe();


        viewBinding.itemSinglePlay.seekBar1.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seekBar.getProgress(), 0);
            }
        });

        registerApplicationBroadcastReceiver();

        if (mSavedInstance != null) {
            if (isHavingListStream()) {
                updateStatePlayer(YPYStreamManager.getInstance().isPlaying());
                YPYMediaPlayer.StreamInfo mStrInfo = YPYStreamManager.getInstance().getStreamInfo();
                processUpdateImage(mStrInfo != null ? mStrInfo.imgUrl : null);
            }
        }
        else {
            if (AUTO_PLAY_IN_SINGLE_MODE) {
                onActionPlay();
            }
        }


    }

    private void setUpOnClick() {
        viewBinding.itemSinglePlay.fbPlay.setOnClickListener(this);
        viewBinding.itemSinglePlay.btnFacebook.setOnClickListener(this);
        viewBinding.itemSinglePlay.btnInstagram.setOnClickListener(this);
        viewBinding.itemSinglePlay.btnWebsite.setOnClickListener(this);
        viewBinding.itemSinglePlay.btnTwitter.setOnClickListener(this);
        viewBinding.itemSinglePlay.btnShare.setOnClickListener(this);
        viewBinding.itemSinglePlay.btnReport.setOnClickListener(this);
    }


    @Override
    public void onDoWhenResume() {
        super.onDoWhenResume();
        updateVolume();
    }

    private void setUpActionBar() {
        ConfigureModel mConfigureModel = mTotalMng.getConfigureModel();
        removeElevationActionBar();
        setUpCustomizeActionBar(Color.TRANSPARENT);
        setActionBarTitle(R.string.title_home_screen);

        mUrlHost = mConfigureModel != null ? mConfigureModel.getUrlEndPoint() : null;
        mApiKey = mConfigureModel != null ? mConfigureModel.getApiKey() : null;
    }

    private void setUpColorWidget() {
        try {
            UIConfigModel mUIConfigModel = mTotalMng.getUiConfigModel();
            mBgMode = mUIConfigModel != null ? mUIConfigModel.getIsFullBg() : UI_BG_JUST_ACTIONBAR;
            if (mBgMode == UI_BG_FULL) {
                viewBinding.itemSinglePlay.layoutDragDropBg.setBackgroundColor(Color.TRANSPARENT);
                if (mLayoutAds != null) {
                    mLayoutAds.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void initEqualizerSafe() {
        try {
            View root = viewBinding.itemSinglePlay.getRoot();
            equalizerView = root.findViewById(R.id.equalizer);

            if (equalizerView != null) {
                equalizerView.setAnimationDuration(EQUALIZER_DURATION);
                equalizerView.stopBars();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyData() {
        XRadioSettingManager.setOnline(this, false);
        resetTimer();
        if (isHavingListStream()) {
            startMusicService(ACTION_STOP);
        }
        else {
            YPYStreamManager.getInstance().onDestroy();
        }
        super.onDestroyData();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            menu.findItem(R.id.action_search).setVisible(false);


            ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(this);
            boolean isAvailable = consentInformation.isConsentFormAvailable();
            menu.findItem(R.id.action_setting_ads).setVisible(isAvailable);

            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_sleep_mode) {
            showDialogSleepMode();
            return true;
        }
        else if (itemId == R.id.action_rate_me) {
            String urlApp = String.format(URL_FORMAT_LINK_APP, getPackageName());
            ShareActionUtils.goToUrl(this, urlApp);
            XRadioSettingManager.setRateApp(this, true);
            return true;
        }
        else if (itemId == R.id.action_share) {
            String urlApp1 = String.format(URL_FORMAT_LINK_APP, getPackageName());
            String msg = String.format(
                    getString(R.string.info_share_app),
                    getString(R.string.app_name),
                    urlApp1
            );
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/*");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, msg);
            startActivity(Intent.createChooser(
                    sharingIntent,
                    getString(R.string.title_menu_share)
            ));
            return true;
        }
        else if (itemId == R.id.action_contact_us) {
            ShareActionUtils.shareViaEmail(this, YOUR_CONTACT_EMAIL, "", "");
            return true;
        }
        else if (itemId == R.id.action_term_of_use) {
            goToUrl(getString(R.string.title_term_of_use), URL_TERM_OF_USE);
            return true;
        }
        else if (itemId == R.id.action_privacy_policy) {
            goToUrl(getString(R.string.title_privacy_policy), URL_PRIVACY_POLICY);
            return true;
        }
        else if (itemId == R.id.action_setting_ads) {
            GDPRManager.getInstance().loadConsentForm(this, null);
            return true;
        }
        else if (itemId == R.id.action_support_radio) {

            if (rewardedHelper.isRewardActive()) {
                showToast("Thanks ❤️ You are already supporting the radio");
                return true;
            }

            rewardedHelper.showRewardedAd(
                    viewBinding.layoutAds,
                    () -> showToast("Thanks for supporting the radio ❤️")
            );
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            increaseVolume();
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            downVolume();
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (ApplicationUtils.isOnline(this) && isHavingListStream()) {
                if (YPYStreamManager.getInstance().isPlaying()) {
                    startMusicService(ACTION_TOGGLE_PLAYBACK);
                    return true;
                }
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (ApplicationUtils.isOnline(this) && isHavingListStream()) {
                if (YPYStreamManager.getInstance().isPrepareDone() &&
                        !YPYStreamManager.getInstance().isPlaying()) {
                    startMusicService(ACTION_TOGGLE_PLAYBACK);
                    return true;
                }
            }

        }
        else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            if (ApplicationUtils.isOnline(this) && isHavingListStream()) {
                startMusicService(ACTION_TOGGLE_PLAYBACK);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void startPlayingRadio() {
        if (!ApplicationUtils.isOnline(this)) {
            if (YPYStreamManager.getInstance().isPrepareDone()) {
                startMusicService(ACTION_STOP);
            }
            showToast(R.string.info_connect_to_play);
            return;
        }
        if (YPYStreamManager.getInstance().isPrepareDone()) {
            return;
        }
        ArrayList<RadioModel> mListPlaying = (ArrayList<RadioModel>) mTotalMng.getListData(TYPE_SINGLE_RADIO);
        if (mListPlaying != null && mListPlaying.size() > 0) {
            ArrayList<RadioModel> mListDatas = (ArrayList<RadioModel>) mListPlaying.clone();
            YPYStreamManager.getInstance().setListModels(mListDatas);
            startPlayRadio(mListPlaying.get(0));
            updateInfo();
        }
    }

    public void startPlayRadio(RadioModel trackModel) {
        try {
            boolean b = YPYStreamManager.getInstance().setCurrentData(trackModel);
            if (b) {
                startMusicService(ACTION_PLAY);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            startMusicService(ACTION_STOP);
        }

    }

    public void updateInfo() {
        try {
            RadioModel mRadioModel = mTotalMng.getSingRadioModel();
            this.viewBinding.itemSinglePlay.tvDragSong.setSelected(true);
            if (mRadioModel != null) {
                YPYMediaPlayer.StreamInfo mStreamInfo = YPYStreamManager.getInstance().getStreamInfo();
                String title = mStreamInfo != null && !TextUtils.isEmpty(mStreamInfo.title) ? mStreamInfo.title : mRadioModel.getName();
                String singer = mStreamInfo != null && !TextUtils.isEmpty(mStreamInfo.artist) ? mStreamInfo.artist : mRadioModel.getTags();
                this.viewBinding.itemSinglePlay.tvDragSong.setText(title);
                this.viewBinding.itemSinglePlay.tvDragSinger.setText(singer);
            }
            else {
                this.viewBinding.itemSinglePlay.tvDragSong.setText(R.string.title_unknown);
                this.viewBinding.itemSinglePlay.tvDragSinger.setText(R.string.title_unknown);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateInfoOfPlayingTrack() {
        try {
            RadioModel mRadioModel = mTotalMng.getSingRadioModel();
            if (mRadioModel != null) {
                updateInfo();

                String imgSong = mRadioModel.getArtWork(mUrlHost);
                processUpdateImage(imgSong);
                String urlFB = mRadioModel.getUrlFacebook();
                this.viewBinding.itemSinglePlay.layoutFacebook.setVisibility(TextUtils.isEmpty(urlFB) ? View.GONE : View.VISIBLE);

                String urlTW = mRadioModel.getUrlTwitter();
                this.viewBinding.itemSinglePlay.layoutTwitter.setVisibility(TextUtils.isEmpty(urlTW) ? View.GONE : View.VISIBLE);

                String urlWeb = mRadioModel.getUrlWebsite();
                this.viewBinding.itemSinglePlay.layoutWebsite.setVisibility(TextUtils.isEmpty(urlWeb) ? View.GONE : View.VISIBLE);

                String urlInsta = mRadioModel.getUrlInstagram();
                this.viewBinding.itemSinglePlay.layoutInstagram.setVisibility(TextUtils.isEmpty(urlInsta) ? View.GONE : View.VISIBLE);

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void registerApplicationBroadcastReceiver() {
        if (mApplicationBroadcast != null) {
            return;
        }
        mApplicationBroadcast = new ApplicationBroadcast();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(getPackageName() + ACTION_BROADCAST_PLAYER);
        if (IOUtils.isAndroid14()) {
            registerReceiver(mApplicationBroadcast, mIntentFilter, RECEIVER_EXPORTED);
        }
        else {
            registerReceiver(mApplicationBroadcast, mIntentFilter);
        }

    }

    private class ApplicationBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();
                    if (!TextUtils.isEmpty(action)) {
                        String packageName = getPackageName();
                        if (action.equals(packageName + ACTION_BROADCAST_PLAYER)) {
                            String actionPlay = intent.getStringExtra(KEY_ACTION);

                            if (!TextUtils.isEmpty(actionPlay)) {
                                if (actionPlay.equalsIgnoreCase(ACTION_UPDATE_COVER_ART)) {
                                    String value = intent.getStringExtra(KEY_VALUE);
                                    processUpdateImage(value);
                                }
                                else {
                                    long value = intent.getLongExtra(KEY_VALUE, -1);
                                    processBroadcast(actionPlay, value);
                                }

                            }
                        }

                    }
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void processBroadcast(String actionPlay, long value) {
        if (actionPlay.equalsIgnoreCase(ACTION_LOADING)) {
            showLoading(true);
        }
        if (actionPlay.equalsIgnoreCase(ACTION_DIMINISH_LOADING)) {
            showLoading(false);
        }
        if (actionPlay.equalsIgnoreCase(ACTION_RESET_INFO)) {
            updateInfo();
            processUpdateImage(null);
        }
        else if (actionPlay.equalsIgnoreCase(ACTION_BUFFERING)) {
            showLoading(false);
            updatePercent(value);
        }
        if (actionPlay.equalsIgnoreCase(ACTION_COMPLETE)) {
            updateInfoWhenComplete();
            processUpdateImage(null);
        }
        else if (actionPlay.equalsIgnoreCase(ACTION_PAUSE)) {
            updateStatePlayer(false);
        }
        else if (actionPlay.equalsIgnoreCase(ACTION_PLAY)) {
            updateStatePlayer(true);
        }
        else if (actionPlay.equalsIgnoreCase(ACTION_STOP) || actionPlay.equalsIgnoreCase(ACTION_ERROR)) {
            updateStatePlayer(false);
            this.viewBinding.itemSinglePlay.tvSleepTimer.setVisibility(View.INVISIBLE);
            if (actionPlay.equalsIgnoreCase(ACTION_ERROR)) {
                int resId = ApplicationUtils.isOnline(this) ? R.string.info_play_error : R.string.info_connect_to_play;
                showToast(resId);
            }
        }
        else if (actionPlay.equalsIgnoreCase(ACTION_UPDATE_INFO)) {
            updateInfo();
        }
        else if (actionPlay.equalsIgnoreCase(ACTION_UPDATE_SLEEP_MODE)) {
            updateSleepMode(value);
        }

    }

    private void updateSleepMode(long value) {
        this.viewBinding.itemSinglePlay.tvSleepTimer.setVisibility(value > 0 ? View.VISIBLE : View.INVISIBLE);
        this.viewBinding.itemSinglePlay.tvSleepTimer.setText(value > 0 ? getStringTimer(value) : "00:00");
    }

    public void processUpdateImage(String imgSong) {
        if (TextUtils.isEmpty(imgSong)) {
            RadioModel ringtoneModel = YPYStreamManager.getInstance().getCurrentRadio();
            imgSong = ringtoneModel != null ? ringtoneModel.getArtWork(mUrlHost) : null;
        }
        if (!TextUtils.isEmpty(imgSong)) {
            if (mTypeUI == UI_PLAYER_CIRCLE_DISK || mTypeUI == UI_PLAYER_ROTATE_DISK
                    || mTypeUI == UI_PLAYER_NO_LAST_FM_CIRCLE_DISK || mTypeUI == UI_PLAYER_NO_LAST_FM_ROTATE_DISK) {
                GlideImageLoader.displayImage(this, viewBinding.itemSinglePlay.imgPlaySong, imgSong, mCropCircleTransform, R.drawable.ic_big_circle_img_default);
            }
            else {
                GlideImageLoader.displayImage(this, viewBinding.itemSinglePlay.imgPlaySong, imgSong, R.drawable.ic_big_rect_img_default);
            }
            if (USE_BLUR_EFFECT && mBgMode == UI_BG_FULL) {
                GlideImageLoader.displayImage(this, viewBinding.imgBg, imgSong, mBlurTransform, R.drawable.background_transparent);
            }
        }
        else {
            resetDefaultImg();
        }
    }

    private void resetDefaultImg() {
        if (mTypeUI == UI_PLAYER_CIRCLE_DISK || mTypeUI == UI_PLAYER_ROTATE_DISK
                || mTypeUI == UI_PLAYER_NO_LAST_FM_CIRCLE_DISK || mTypeUI == UI_PLAYER_NO_LAST_FM_ROTATE_DISK) {
            viewBinding.itemSinglePlay.imgPlaySong.setImageResource(R.drawable.ic_big_circle_img_default);
        }
        else {
            viewBinding.itemSinglePlay.imgPlaySong.setImageResource(R.drawable.ic_big_rect_img_default);
        }
        viewBinding.imgBg.setImageResource(R.drawable.background_transparent);
    }


    public void showLoading(boolean b) {
        viewBinding.itemSinglePlay.layoutContent.setVisibility(View.INVISIBLE);
        viewBinding.itemSinglePlay.tvPercent.setVisibility(b ? View.INVISIBLE : View.VISIBLE);
        if (b) {
            viewBinding.itemSinglePlay.playProgressBar1.setVisibility(View.VISIBLE);
            viewBinding.itemSinglePlay.playProgressBar1.show();
        }
        else {
            if (viewBinding.itemSinglePlay.playProgressBar1.getVisibility() == View.VISIBLE) {
                viewBinding.itemSinglePlay.playProgressBar1.hide();
                viewBinding.itemSinglePlay.playProgressBar1.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void updatePercent(long percent) {
        viewBinding.itemSinglePlay.tvPercent.setVisibility(View.VISIBLE);
        viewBinding.itemSinglePlay.layoutContent.setVisibility(View.INVISIBLE);
        pauseRotateAnim();
        if (percent > 0) {
            String msg = String.format(getString(R.string.format_buffering), percent + "%");
            viewBinding.itemSinglePlay.tvPercent.setText(msg);
        }

    }

    public void updateStatePlayer(boolean isPlaying) {
        viewBinding.itemSinglePlay.layoutContent.setVisibility(View.VISIBLE);
        viewBinding.itemSinglePlay.tvPercent.setVisibility(View.INVISIBLE);
        int playId = isPlaying ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_arrow_white_36dp;
        viewBinding.itemSinglePlay.fbPlay.setImageResource(playId);
        if (equalizerView != null) {
            if (isPlaying) {
                equalizerView.animateBars();
            }
            else {
                equalizerView.stopBars();
            }
        }
        if (mTypeUI == UI_PLAYER_ROTATE_DISK || mTypeUI == UI_PLAYER_NO_LAST_FM_ROTATE_DISK) {
            if (isPlaying) {
                startRotateAnim();
            }
            else {
                pauseRotateAnim();
            }
        }

    }

    @Override
    protected void onDestroy() {
        if (isHavingListStream()) {
            startMusicService(ACTION_STOP);
        }
        pauseRotateAnim();
        if (mApplicationBroadcast != null) {
            unregisterReceiver(mApplicationBroadcast);
            mApplicationBroadcast = null;
        }
        super.onDestroy();
    }

    private void onActionPlay() {
        if (YPYStreamManager.getInstance().isPrepareDone()) {
            startMusicService(ACTION_TOGGLE_PLAYBACK);
        }
        else {
            startPlayingRadio();
        }
    }

    @Override
    public void onClick(View view) {
        RadioModel mRadioModel = YPYStreamManager.getInstance().getCurrentRadio();
        if (mRadioModel == null) {
            mRadioModel = mTotalMng.getSingRadioModel();
        }
        String nameRadio = mRadioModel != null ? mRadioModel.getName() : null;
        int id = view.getId();
        if (id == R.id.fb_play) {
            onActionPlay();
        }
        else if (id == R.id.btn_facebook) {
            String urlFB = mRadioModel != null ? mRadioModel.getUrlFacebook() : null;
            if (!TextUtils.isEmpty(urlFB)) {
                goToUrl(nameRadio, urlFB);
            }
        }
        else if (id == R.id.btn_instagram) {
            String urlInsta = mRadioModel != null ? mRadioModel.getUrlInstagram() : null;
            if (!TextUtils.isEmpty(urlInsta)) {
                goToUrl(nameRadio, urlInsta);
            }
        }
        else if (id == R.id.btn_twitter) {
            String urlTW = mRadioModel != null ? mRadioModel.getUrlTwitter() : null;
            if (!TextUtils.isEmpty(urlTW)) {
                goToUrl(nameRadio, urlTW);
            }
        }
        else if (id == R.id.btn_website) {
            String urlWeb = mRadioModel != null ? mRadioModel.getUrlWebsite() : null;
            if (!TextUtils.isEmpty(urlWeb)) {
                goToUrl(nameRadio, urlWeb);
            }
        }
        else if (id == R.id.btn_share) {
            shareRadioModel(mRadioModel);
        }
        else if (id == R.id.btn_report) {
            reportRadioProblem(mRadioModel);
        }
    }

    public void updateVolume() {
        try {
            int values = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            viewBinding.itemSinglePlay.seekBar1.setMax(maxVolume);
            viewBinding.itemSinglePlay.seekBar1.setProgress(values);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void increaseVolume() {
        try {
            int values = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            values++;
            if (values >= maxVolume) {
                values = maxVolume;
            }
            viewBinding.itemSinglePlay.seekBar1.setProgress(values);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, values, 0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void downVolume() {
        try {
            int values = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            values--;
            if (values < 0) {
                values = 0;
            }
            viewBinding.itemSinglePlay.seekBar1.setProgress(values);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, values, 0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void pauseRotateAnim() {
        if (rotate != null) {
            viewBinding.itemSinglePlay.imgPlaySong.clearAnimation();
            rotate.cancel();
            rotate = null;
        }
    }

    private void startRotateAnim() {
        try {
            if ((mTypeUI == UI_PLAYER_ROTATE_DISK || mTypeUI == UI_PLAYER_NO_LAST_FM_ROTATE_DISK)) {
                pauseRotateAnim();
                final float toDegrees = DEGREE * 360;
                rotate = new RotateAnimation(0, toDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f) {
                    private boolean isFirstTime;

                    @Override
                    public boolean getTransformation(long currentTime, Transformation outTransformation) {
                        if (!isFirstTime) {
                            isFirstTime = true;
                            setStartTime(currentTime);
                        }
                        return super.getTransformation(currentTime, outTransformation);
                    }
                };
                rotate.setDuration(DEGREE * DELTA_TIME);
                rotate.setRepeatCount(1000);
                viewBinding.itemSinglePlay.imgPlaySong.startAnimation(rotate);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateInfoWhenComplete() {
        this.viewBinding.itemSinglePlay.tvDragSong.setText(R.string.info_radio_ended_title);
        this.viewBinding.itemSinglePlay.tvDragSinger.setText("");
        startMusicService(ACTION_STOP);

    }

    @Override
    public void onUpdateUIWhenSupportRTL() {
        super.onUpdateUIWhenSupportRTL();
        this.viewBinding.itemSinglePlay.seekBar1.setScaleX(-1f);
        this.viewBinding.itemSinglePlay.imgVolumeMax.setScaleX(-1f);
        this.viewBinding.itemSinglePlay.imgVolumeOff.setScaleX(-1f);

    }
}
