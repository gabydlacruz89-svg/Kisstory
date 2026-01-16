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

package com.ypyglobal.xradio.stream.mediaplayer;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.icy.IcyHeaders;
import com.google.android.exoplayer2.metadata.icy.IcyInfo;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.DefaultHlsExtractorFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.ypyglobal.xradio.ypylibs.executor.YPYExecutorSupplier;
import com.ypyglobal.xradio.ypylibs.utils.YPYLog;

import androidx.annotation.NonNull;


public class YPYMediaPlayer {

    private final Context mContext;

    private OnStreamListener onStreamListener;

    private boolean isPrepared;
    private ExoPlayer audioPlayer;
    private final String userAgent;
    private Player.Listener mPlayerListener;

    public YPYMediaPlayer(Context mContext, String mUserAgent) {
        this.mContext = mContext;
        this.userAgent = mUserAgent;
    }

    public void release() {
        isPrepared = false;
        if (audioPlayer != null) {
            if (mPlayerListener != null) {
                audioPlayer.removeListener(mPlayerListener);
                mPlayerListener = null;
            }
            audioPlayer.release();
            audioPlayer = null;
        }

    }

    public void setVolume(float volume) {
        try {
            if (audioPlayer != null) {
                audioPlayer.setVolume(volume);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnStreamListener(OnStreamListener onStreamListener) {
        this.onStreamListener = onStreamListener;
    }


    public void setDataSource(String url) {
        if (!TextUtils.isEmpty(url)) {
            String mUrlStream = url;

            AdaptiveTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(mContext, trackSelectionFactory);
            ExoPlayer.Builder mAudioBuilder = new ExoPlayer.Builder(mContext);
            mAudioBuilder.setTrackSelector(trackSelector);

            audioPlayer = mAudioBuilder.build();
            setUpListenerPlayer();

            MediaSource mediaSource;
            if (mUrlStream.endsWith("_Other")) {
                mUrlStream = mUrlStream.replace("_Other", "");
            }

            DefaultHttpDataSource.Factory mHttpFact = new DefaultHttpDataSource.Factory();
            mHttpFact.setUserAgent(getUserAgent(mContext));
            mHttpFact.setAllowCrossProtocolRedirects(true);
            mHttpFact.setTransferListener(null);

            DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(mContext, mHttpFact);
            MediaItem mMediaItem = MediaItem.fromUri(Uri.parse(mUrlStream));
            YPYLog.e("DCM", "======>start stream url stream=" + mUrlStream);
            if (mUrlStream.endsWith(".m3u8") || mUrlStream.endsWith(".M3U8")) {
                mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                        .setAllowChunklessPreparation(false)
                        .setExtractorFactory(
                                new DefaultHlsExtractorFactory(
                                        DefaultTsPayloadReaderFactory.FLAG_IGNORE_H264_STREAM, false))
                        .createMediaSource(mMediaItem);
            }
            else {
                mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory, new DefaultExtractorsFactory())
                        .createMediaSource(mMediaItem);
            }
            audioPlayer.setMediaSource(mediaSource);
            audioPlayer.prepare();
            start();
            return;

        }
        if (onStreamListener != null) {
            onStreamListener.onError();
        }
    }

    private void setUpListenerPlayer() {
        mPlayerListener = new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                    if (onStreamListener != null) {
                        onStreamListener.onComplete();
                    }
                }
                else if (playbackState == Player.STATE_READY) {
                    if (onStreamListener != null && !isPrepared) {
                        isPrepared = true;
                        onStreamListener.onPrepare();
                    }
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                if (onStreamListener != null) {
                    onStreamListener.onError();
                }
            }

            @Override
            public void onMetadata(@NonNull Metadata metadata) {
                if (metadata.length() > 0) {
                    try {
                        int size = metadata.length();
                        for (int i = 0; i < size; i++) {
                            Metadata.Entry mEntry = metadata.get(i);
                            if (mEntry instanceof IcyInfo) {
                                processMetadata((((IcyInfo) mEntry).title));
                                break;
                            }
                            else if (mEntry instanceof IcyHeaders) {
                                processMetadata(((IcyHeaders) mEntry).name);
                                break;
                            }

                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        audioPlayer.addListener(mPlayerListener);
    }


    private void processMetadata(String title) {
        try {
            StreamInfo mStreamInfo = new StreamInfo();
            if (!TextUtils.isEmpty(title)) {
                String[] metadata = title.split(" - ");
                if (metadata.length > 0) {
                    if (metadata.length == 3) {
                        mStreamInfo.artist = metadata[1];
                        mStreamInfo.title = metadata[2];
                    }
                    else if (metadata.length == 2) {
                        mStreamInfo.artist = metadata[0];
                        mStreamInfo.title = metadata[1];
                    }
                    else {
                        mStreamInfo.title = metadata[0];
                    }
                }
            }
            YPYExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                if (onStreamListener != null) {
                    onStreamListener.onUpdateMetaData(mStreamInfo);
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void start() {
        try {
            if (audioPlayer != null) {
                audioPlayer.setPlayWhenReady(true);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stop() {
        try {
            if (audioPlayer != null) {
                audioPlayer.stop();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void pause() {
        try {
            if (audioPlayer != null) {
                audioPlayer.setPlayWhenReady(false);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isPlaying() {
        try {
            return audioPlayer != null && audioPlayer.getPlayWhenReady();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private String getUserAgent(Context mContext) {
        if (!TextUtils.isEmpty(userAgent)) {
            return userAgent;
        }
        return Util.getUserAgent(mContext, getClass().getSimpleName());
    }


    public interface OnStreamListener {
        void onPrepare();

        void onError();

        void onComplete();

        void onUpdateMetaData(StreamInfo info);
    }

    public static class StreamInfo {
        public String title;
        public String artist;
        public String imgUrl;
    }


}
