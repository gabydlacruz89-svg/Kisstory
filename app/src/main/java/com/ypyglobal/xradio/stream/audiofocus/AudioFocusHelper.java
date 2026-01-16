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

package com.ypyglobal.xradio.stream.audiofocus;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

public class AudioFocusHelper implements AudioManager.OnAudioFocusChangeListener {

    private final AudioManager mAudioManager;
    private final IStreamFocusableListener listener;

    public AudioFocusHelper(Context ctx, IStreamFocusableListener listener) {
        this.mAudioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        this.listener = listener;
    }

    /**
     * Requests audio focus. Returns whether request was successful or not.
     */
    public boolean requestFocus() {
        if (mAudioManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(this).build();
                final int result = mAudioManager.requestAudioFocus(audioFocusRequest);
                return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
            }
            else {
                final int result = mAudioManager.requestAudioFocus(this,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);
                return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
            }
        }
        return false;
    }

    /**
     * Abandons audio focus. Returns whether request was successful or not.
     */
    public void abandonFocus() {
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(this);
        }
    }

    /**
     * Called by AudioManager on audio focus changes. We implement this by
     * calling our MusicFocusable appropriately to relay the message.
     */
    @Override
    public void onAudioFocusChange(int focusChange) {
        try {
            if (listener == null) return;
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    listener.onGrantAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    listener.onLostAudioFocus(false);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    listener.onLostAudioFocus(true);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    listener.onGiveUpAudioFocus();
                    break;
                default:
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
