package com.android.orion.chart;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

public class TradeLevelPicker extends NumberPicker {

    private static final String TAG = "TradeLevelPicker";
    private static final int MSG_PLAY_SOUND = 1;
    private static final long MIN_PLAY_INTERVAL = 80; // 最小播放间隔80ms

    private MediaPlayer mediaPlayer;
    private Handler soundHandler;
    private long lastPlayTime = 0;

    // 声音处理器
    private class SoundHandler extends Handler {
        SoundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_PLAY_SOUND) {
                playSound();
            }
        }
    }

    public TradeLevelPicker(Context context) {
        super(context);
        init();
    }

    public TradeLevelPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TradeLevelPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化媒体播放器
        initMediaPlayer();

        // 初始化声音处理器（使用主线程）
        soundHandler = new SoundHandler(Looper.getMainLooper());

        // 设置值变化监听器
        setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // 只要值变化就发送播放消息
                if (oldVal != newVal) {
                    sendPlayMessage();
                }
            }
        });
    }

    private void initMediaPlayer() {
        try {
            // 获取资源ID
            int soundResourceId = getResources().getIdentifier("sound_picker", "raw", getContext().getPackageName());
            if (soundResourceId != 0) {
                mediaPlayer = MediaPlayer.create(getContext(), soundResourceId);
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(0.5f, 0.5f);
                } else {
                    Log.e(TAG, "无法加载声音文件 sound_picker.ogg");
                }
            } else {
                Log.e(TAG, "未找到声音资源: sound_picker.ogg");
            }
        } catch (Exception e) {
            Log.e(TAG, "初始化媒体播放器失败", e);
        }
    }

    private void sendPlayMessage() {
        if (soundHandler == null || mediaPlayer == null) return;

        long currentTime = System.currentTimeMillis();
        long timeSinceLastPlay = currentTime - lastPlayTime;

        if (timeSinceLastPlay >= MIN_PLAY_INTERVAL) {
            // 可以直接播放
            soundHandler.removeMessages(MSG_PLAY_SOUND); // 移除之前的延迟消息
            soundHandler.sendEmptyMessage(MSG_PLAY_SOUND);
        } else {
            // 需要延迟播放，但只保留最新的延迟消息
            soundHandler.removeMessages(MSG_PLAY_SOUND);
            long delay = MIN_PLAY_INTERVAL - timeSinceLastPlay;
            soundHandler.sendEmptyMessageDelayed(MSG_PLAY_SOUND, delay);
        }
    }

    private void playSound() {
        if (mediaPlayer == null) {
            return;
        }

        try {
            // 重置播放器到开始位置
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
            lastPlayTime = System.currentTimeMillis();

            Log.d(TAG, "播放声音");

        } catch (Exception e) {
            Log.e(TAG, "播放声音时发生错误", e);
            // 尝试重新初始化媒体播放器
            releaseMediaPlayer();
            initMediaPlayer();
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "释放媒体播放器时发生错误", e);
            }
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 清理资源
        releaseMediaPlayer();
        if (soundHandler != null) {
            soundHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        updateView(child);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateView(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        updateView(child);
    }

    private void updateView(View view) {
        if (view instanceof EditText) {
            EditText editText = (EditText) view;
            editText.setTextColor(Color.BLACK);
            editText.setTextSize(16);
        }
    }
}