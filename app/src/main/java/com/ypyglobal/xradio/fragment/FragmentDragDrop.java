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

package com.ypyglobal.xradio.fragment;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;
import com.ypyglobal.xradio.R;
import com.ypyglobal.xradio.XMultiRadioMainActivity;
import com.ypyglobal.xradio.constants.IXRadioConstants;
import com.ypyglobal.xradio.databinding.FragmentDragDropDetailBinding;
import com.ypyglobal.xradio.model.RadioModel;
import com.ypyglobal.xradio.model.UIConfigModel;
import com.ypyglobal.xradio.stream.constant.IYPYStreamConstants;
import com.ypyglobal.xradio.stream.manager.YPYStreamManager;
import com.ypyglobal.xradio.stream.mediaplayer.YPYMediaPlayer;
import com.ypyglobal.xradio.ypylibs.fragment.YPYFragment;
import com.ypyglobal.xradio.ypylibs.imageloader.GlideImageLoader;
import com.ypyglobal.xradio.ypylibs.utils.ApplicationUtils;

import androidx.annotation.NonNull;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class FragmentDragDrop extends YPYFragment<FragmentDragDropDetailBinding> implements IXRadioConstants, IYPYStreamConstants, View.OnClickListener {

    private XMultiRadioMainActivity mContext;

    private AudioManager mAudioManager;
    private BlurTransformation mBlurTransform;
    private CropCircleTransformation mCropCircleTransform;
    private int mTypeUI = UI_PLAYER_NO_LAST_FM_SQUARE_DISK;
    private RotateAnimation rotate;

    @NonNull
    @Override
    protected FragmentDragDropDetailBinding getViewBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentDragDropDetailBinding.inflate(inflater, container, false);
    }

    @Override
    public void findView() {
        this.mContext = (XMultiRadioMainActivity) requireActivity();
        viewBinding.fbPlay.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
        viewBinding.fbPlay.setRippleColor(mContext.getResources().getColor(R.color.ripple_button_color));
        viewBinding.fbPlay.setSize(FloatingActionButton.SIZE_NORMAL);

        if (USE_BLUR_EFFECT) {
            mBlurTransform = new BlurTransformation();
        }
        else {
            viewBinding.imgOverlay.setVisibility(View.GONE);
        }

        mCropCircleTransform = new CropCircleTransformation();

        viewBinding.equalizer.setAnimationDuration(EQUALIZER_DURATION);
        viewBinding.equalizer.stopBars();

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        viewBinding.seekBar1.setOnSeekChangeListener(new OnSeekChangeListener() {
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
        updateBackground();
        updateVolume();

        updateInfo(true);
        viewBinding.btnFavorite.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                RadioModel model = YPYStreamManager.getInstance().getCurrentRadio();
                mContext.updateFavorite(model, TYPE_TAB_FAVORITE, true);
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                RadioModel model = YPYStreamManager.getInstance().getCurrentRadio();
                mContext.updateFavorite(model, TYPE_TAB_FAVORITE, false);
            }
        });

        UIConfigModel model = mContext.mTotalMng.getUiConfigModel();
        mTypeUI = model != null ? model.getUiPlayer() : UI_PLAYER_NO_LAST_FM_SQUARE_DISK;
        setUpClick();

        boolean b = ApplicationUtils.isSupportRTL();
        if (b) {
            onUpdateUIWhenSupportRTL();
        }

        boolean isLoading = YPYStreamManager.getInstance().isLoading();
        if (isLoading) {
            showLoading(true);
        }
        else {
            showLayoutControl();
            updateStatusPlayer(YPYStreamManager.getInstance().isPlaying());
            YPYMediaPlayer.StreamInfo mStrInfo = YPYStreamManager.getInstance().getStreamInfo();
            updateImage(mStrInfo != null ? mStrInfo.imgUrl : null);
        }

    }

    private void setUpClick() {
        viewBinding.btnClose.setOnClickListener(this);
        viewBinding.fbPlay.setOnClickListener(this);
        viewBinding.btnNext.setOnClickListener(this);
        viewBinding.btnPrev.setOnClickListener(this);
        viewBinding.btnFacebook.setOnClickListener(this);
        viewBinding.btnInstagram.setOnClickListener(this);
        viewBinding.btnWebsite.setOnClickListener(this);
        viewBinding.btnTwitter.setOnClickListener(this);
        viewBinding.btnShare.setOnClickListener(this);
        viewBinding.btnReport.setOnClickListener(this);
    }

    public void showLoading(boolean b) {
        try {
            if (mContext != null) {
                viewBinding.layoutContent.setVisibility(View.INVISIBLE);
                viewBinding.tvPercent.setVisibility(b ? View.INVISIBLE : View.VISIBLE);
                if (b) {
                    viewBinding.playProgressBar1.setVisibility(View.VISIBLE);
                    viewBinding.playProgressBar1.show();
                    if (viewBinding.equalizer.isAnimating()) {
                        viewBinding.equalizer.stopBars();
                    }
                }
                else {
                    if (viewBinding.playProgressBar1.getVisibility() == View.VISIBLE) {
                        viewBinding.playProgressBar1.hide();
                        viewBinding.playProgressBar1.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void updatePercent(long percent) {
        try {
            if (mContext != null) {
                viewBinding.tvPercent.setVisibility(View.VISIBLE);
                viewBinding.layoutContent.setVisibility(View.INVISIBLE);
                pauseRotateAnim();
                if (percent > 0) {
                    String msg = String.format(mContext.getString(R.string.format_buffering), percent + "%");
                    viewBinding.tvPercent.setText(msg);
                }
                if (viewBinding.equalizer.isAnimating()) {
                    viewBinding.equalizer.stopBars();
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showLayoutControl() {
        if (mContext != null) {
            viewBinding.layoutContent.setVisibility(View.VISIBLE);
            viewBinding.tvPercent.setVisibility(View.INVISIBLE);
        }
    }

    public void updateStatusPlayer(boolean isPlaying) {
        if (mContext != null) {
            showLayoutControl();
            viewBinding.fbPlay.setImageResource(isPlaying ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_arrow_white_36dp);
            if (isPlaying) {
                viewBinding.equalizer.animateBars();
                startRotateAnim();
            }
            else {
                viewBinding.equalizer.stopBars();
                pauseRotateAnim();
            }
        }
    }

    public void updateInfoWhenComplete() {
        try {
            if (mContext != null) {
                RadioModel mRadioModel = YPYStreamManager.getInstance().getCurrentRadio();
                if (mRadioModel != null) {
                    String nameRadio = mRadioModel.getName();
                    this.viewBinding.tvTitleDragDrop.setText(nameRadio);
                    this.viewBinding.tvBitrate.setText(String.format(mContext.getString(R.string.format_bitrate), mRadioModel.getBitRate()));
                    this.viewBinding.tvDragSong.setText(R.string.info_radio_ended_title);
                    this.viewBinding.tvDragSinger.setText(ApplicationUtils.isOnline(mContext) ? R.string.info_radio_ended_sub : R.string.info_connection_lost);
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateInfo(boolean isNeedUpdateSocial) {
        try {
            if (mContext != null) {
                RadioModel mRadioModel = YPYStreamManager.getInstance().getCurrentRadio();
                if (mRadioModel != null) {
                    String nameRadio = mRadioModel.getName();
                    this.viewBinding.tvTitleDragDrop.setText(nameRadio);
                    this.viewBinding.tvBitrate.setText(String.format(mContext.getString(R.string.format_bitrate), mRadioModel.getBitRate()));
                    YPYMediaPlayer.StreamInfo mStreamInfo = YPYStreamManager.getInstance().getStreamInfo();

                    String title = mStreamInfo != null && !TextUtils.isEmpty(mStreamInfo.title) ? mStreamInfo.title : mRadioModel.getName();
                    String artist = mStreamInfo != null && !TextUtils.isEmpty(mStreamInfo.artist) ? mStreamInfo.artist : mRadioModel.getTags();
                    this.viewBinding.tvDragSong.setText(title);
                    this.viewBinding.tvDragSinger.setText(artist);

                    if (isNeedUpdateSocial) {
                        String urlFB = mRadioModel.getUrlFacebook();
                        viewBinding.layoutFacebook.setVisibility(TextUtils.isEmpty(urlFB) ? View.GONE : View.VISIBLE);

                        String urlTW = mRadioModel.getUrlTwitter();
                        viewBinding.layoutTwitter.setVisibility(TextUtils.isEmpty(urlTW) ? View.GONE : View.VISIBLE);

                        String urlWeb = mRadioModel.getUrlWebsite();
                        viewBinding.layoutWebsite.setVisibility(TextUtils.isEmpty(urlWeb) ? View.GONE : View.VISIBLE);

                        String urlInsta = mRadioModel.getUrlInstagram();
                        viewBinding.layoutInstagram.setVisibility(TextUtils.isEmpty(urlInsta) ? View.GONE : View.VISIBLE);
                        viewBinding.btnFavorite.setLiked(mRadioModel.isFavorite());
                    }
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void updateVolume() {
        try {
            AudioManager mgr = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            if (mgr != null) {
                int values = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxVolume = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                viewBinding.seekBar1.setMax(maxVolume);
                viewBinding.seekBar1.setProgress(values);
            }
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
            viewBinding.seekBar1.setProgress(values);
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
            viewBinding.seekBar1.setProgress(values);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, values, 0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onDestroy() {
        pauseRotateAnim();
        if (viewBinding != null) {
            viewBinding.equalizer.stopBars();
            viewBinding.equalizer.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        RadioModel mRadioModel = YPYStreamManager.getInstance().getCurrentRadio();
        String nameRadio = mRadioModel != null ? mRadioModel.getName() : null;
        int id = view.getId();
        if (id == R.id.btn_close) {
            mContext.collapseListenMusic();
        }
        else if (id == R.id.btn_next) {
            if (mContext.isAllCheckNetWorkOff && !ApplicationUtils.isOnline(mContext)) {
                mContext.showToast(R.string.info_connect_to_play);
                return;
            }
            mContext.startMusicService(ACTION_NEXT);
        }
        else if (id == R.id.btn_prev) {
            if (mContext.isAllCheckNetWorkOff && !ApplicationUtils.isOnline(mContext)) {
                mContext.showToast(R.string.info_connect_to_play);
                return;
            }
            mContext.startMusicService(ACTION_PREVIOUS);
        }
        else if (id == R.id.fb_play) {
            if (mContext.isAllCheckNetWorkOff && !ApplicationUtils.isOnline(mContext)) {
                mContext.showToast(R.string.info_connect_to_play);
                return;
            }
            mContext.startMusicService(ACTION_TOGGLE_PLAYBACK);
        }
        else if (id == R.id.btn_facebook) {
            String urlFB = mRadioModel != null ? mRadioModel.getUrlFacebook() : null;
            if (!TextUtils.isEmpty(urlFB)) {
                mContext.goToUrl(nameRadio, urlFB);
            }
        }
        else if (id == R.id.btn_instagram) {
            String urlInsta = mRadioModel != null ? mRadioModel.getUrlInstagram() : null;
            if (!TextUtils.isEmpty(urlInsta)) {
                mContext.goToUrl(nameRadio, urlInsta);
            }
        }
        else if (id == R.id.btn_twitter) {
            String urlTW = mRadioModel != null ? mRadioModel.getUrlTwitter() : null;
            if (!TextUtils.isEmpty(urlTW)) {
                mContext.goToUrl(nameRadio, urlTW);
            }
        }
        else if (id == R.id.btn_website) {
            String urlWeb = mRadioModel != null ? mRadioModel.getUrlWebsite() : null;
            if (!TextUtils.isEmpty(urlWeb)) {
                mContext.goToUrl(nameRadio, urlWeb);
            }
        }
        else if (id == R.id.btn_share) {
            mContext.shareRadioModel(mRadioModel);
        }
        else if (id == R.id.btn_report) {
            mContext.reportRadioProblem(mRadioModel);
        }
    }

    public void updateImage(String url) {
        if (viewBinding != null) {
            if (!TextUtils.isEmpty(url)) {
                if (mTypeUI == UI_PLAYER_CIRCLE_DISK || mTypeUI == UI_PLAYER_ROTATE_DISK
                        || mTypeUI == UI_PLAYER_NO_LAST_FM_CIRCLE_DISK || mTypeUI == UI_PLAYER_NO_LAST_FM_ROTATE_DISK) {
                    GlideImageLoader.displayImage(mContext, viewBinding.imgPlaySong, url, mCropCircleTransform, R.drawable.ic_big_circle_img_default);
                }
                else {
                    GlideImageLoader.displayImage(mContext, viewBinding.imgPlaySong, url, R.drawable.ic_big_rect_img_default);
                }
                if (USE_BLUR_EFFECT) {
                    viewBinding.imgOverlay.setVisibility(View.VISIBLE);
                    GlideImageLoader.displayImage(mContext, viewBinding.imgBgDragDrop, url, mBlurTransform, R.drawable.background_transparent);
                }
            }
            else {
                resetDefaultImg();
            }
        }

    }

    private void resetDefaultImg() {
        if (mTypeUI == UI_PLAYER_CIRCLE_DISK || mTypeUI == UI_PLAYER_ROTATE_DISK
                || mTypeUI == UI_PLAYER_NO_LAST_FM_CIRCLE_DISK || mTypeUI == UI_PLAYER_NO_LAST_FM_ROTATE_DISK) {
            viewBinding.imgPlaySong.setImageResource(R.drawable.ic_big_circle_img_default);
        }
        else {
            viewBinding.imgPlaySong.setImageResource(R.drawable.ic_big_rect_img_default);
        }
        viewBinding.imgBgDragDrop.setImageResource(R.drawable.background_transparent);
        viewBinding.imgOverlay.setVisibility(View.GONE);
    }

    public void updateBackground() {
        try {
            if (viewBinding != null) {
                mContext.setUpBackground(viewBinding.layoutDragDropBg);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notifyFavorite(long trackId, boolean isFav) {
        try {
            if (mContext != null) {
                RadioModel mRadioModel = YPYStreamManager.getInstance().getCurrentRadio();
                if (mRadioModel != null) {
                    if (mRadioModel.getId() == trackId) {
                        mRadioModel.setFavorite(isFav);
                        viewBinding.btnFavorite.setLiked(isFav);
                    }
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void pauseRotateAnim() {
        try {
            if (viewBinding != null && rotate != null) {
                viewBinding.imgPlaySong.clearAnimation();
                rotate.cancel();
                rotate = null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRotateAnim() {
        try {
            if ((mTypeUI == UI_PLAYER_ROTATE_DISK || mTypeUI == UI_PLAYER_NO_LAST_FM_ROTATE_DISK) && viewBinding.imgPlaySong != null) {
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
                viewBinding.imgPlaySong.startAnimation(rotate);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void updateSleepMode(long value) {
        try {
            viewBinding.tvSleepTimer.setVisibility(value > 0 ? View.VISIBLE : View.INVISIBLE);
            viewBinding.tvSleepTimer.setText(value > 0 ? mContext.getStringTimer(value) : "00:00");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void onUpdateUIWhenSupportRTL() {
        try {
            viewBinding.btnNext.setImageResource(R.drawable.ic_skip_previous_white_36dp);
            viewBinding.btnPrev.setImageResource(R.drawable.ic_skip_next_white_36dp);
            viewBinding.seekBar1.setScaleX(-1f);
            viewBinding.imgVolumeMax.setScaleX(-1f);
            viewBinding.imgVolumeOff.setScaleX(-1f);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


}
