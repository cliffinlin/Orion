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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TradeLevelPicker extends NumberPicker {

    private static final String TAG = "TradeLevelPicker";
    private static final int MSG_PLAY_SOUND = 1;
    private static final long MIN_PLAY_INTERVAL = 80;

    private MediaPlayer mediaPlayer;
    private Handler soundHandler;
    private long lastPlayTime = 0;

    // 用于存储多个监听器
    private List<OnValueChangeListener> valueChangeListeners = new ArrayList<>();
    private boolean isSoundEnabled = true;

    // 新增：目标值属性
    private int targetValue = 0;
    private OnValueChangeListener externalValueChangeListener;

    // 颜色更新处理器
    private Handler mColorUpdateHandler = new Handler(Looper.getMainLooper());
    private Runnable mColorUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateTextColorFinal(getValue());
        }
    };

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

        // 初始化声音处理器
        soundHandler = new SoundHandler(Looper.getMainLooper());

        // 设置内部监听器（代理模式）
        super.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // 播放声音
                if (oldVal != newVal && isSoundEnabled) {
                    sendPlayMessage();
                }

                // 延迟更新文本颜色（避免循环调用）
                updateTextColor(newVal);

                // 通知所有注册的监听器
                for (OnValueChangeListener listener : valueChangeListeners) {
                    if (listener != null) {
                        listener.onValueChange(picker, oldVal, newVal);
                    }
                }

                // 通知外部监听器（用于更新图标）
                if (externalValueChangeListener != null) {
                    externalValueChangeListener.onValueChange(picker, oldVal, newVal);
                }
            }
        });
    }

    @Override
    public void setOnValueChangedListener(OnValueChangeListener onValueChangedListener) {
        // 不直接设置，而是添加到监听器列表
        if (onValueChangedListener != null && !valueChangeListeners.contains(onValueChangedListener)) {
            valueChangeListeners.add(onValueChangedListener);
        }
        // 保存外部监听器用于特殊处理
        this.externalValueChangeListener = onValueChangedListener;
    }

    /**
     * 添加值变化监听器
     */
    public void addOnValueChangedListener(OnValueChangeListener listener) {
        if (listener != null && !valueChangeListeners.contains(listener)) {
            valueChangeListeners.add(listener);
        }
    }

    /**
     * 移除值变化监听器
     */
    public void removeOnValueChangedListener(OnValueChangeListener listener) {
        valueChangeListeners.remove(listener);
    }

    /**
     * 设置目标值并更新显示颜色
     */
    public void setTargetValue(int target) {
        this.targetValue = target;
        // 延迟更新当前值的颜色
        updateTextColor(getValue());
    }

    /**
     * 获取目标值
     */
    public int getTargetValue() {
        return targetValue;
    }

    /**
     * 更新文本颜色（延迟执行，避免循环调用）
     */
    public void updateTextColor(int currentValue) {
        // 移除之前的任务
        mColorUpdateHandler.removeCallbacks(mColorUpdateRunnable);
        // 延迟更新，避免在滚动过程中频繁更新
        mColorUpdateHandler.postDelayed(mColorUpdateRunnable, 50);
    }

    /**
     * 最终的颜色更新方法（避免循环调用）
     */
    private void updateTextColorFinal(int currentValue) {
        Log.d(TAG, "updateTextColorFinal: currentValue=" + currentValue + ", targetValue=" + targetValue);

        try {
            // 方法1：使用反射获取 mInputText（中心显示的EditText）
            Field field = NumberPicker.class.getDeclaredField("mInputText");
            field.setAccessible(true);
            EditText editText = (EditText) field.get(this);
            if (editText != null) {
                int targetColor = (currentValue > 0 && currentValue == targetValue) ? Color.RED : Color.BLACK;
                if (editText.getCurrentTextColor() != targetColor) {
                    editText.setTextColor(targetColor);
                    Log.d(TAG, "最终设置中心文本颜色: " + (targetColor == Color.RED ? "红色" : "黑色"));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "反射获取 mInputText 失败: " + e.getMessage());

            // 方法2：遍历子视图查找EditText
            findAndUpdateEditTextColor(currentValue);
        }
    }

    /**
     * 遍历子视图查找并更新EditText颜色
     */
    private void findAndUpdateEditTextColor(int currentValue) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof EditText) {
                EditText editText = (EditText) child;
                int targetColor = (currentValue > 0 && currentValue == targetValue) ? Color.RED : Color.BLACK;
                if (editText.getCurrentTextColor() != targetColor) {
                    editText.setTextColor(targetColor);
                    Log.d(TAG, "遍历设置EditText颜色: " + (targetColor == Color.RED ? "红色" : "黑色"));
                }
            }
        }
    }

    /**
     * 启用/禁用声音
     */
    public void setSoundEnabled(boolean enabled) {
        this.isSoundEnabled = enabled;
        Log.d(TAG, "声音 " + (enabled ? "启用" : "禁用"));
    }

    /**
     * 检查声音是否启用
     */
    public boolean isSoundEnabled() {
        return isSoundEnabled;
    }

    private void initMediaPlayer() {
        try {
            int soundResourceId = getResources().getIdentifier("sound_picker", "raw", getContext().getPackageName());
            if (soundResourceId == 0) {
                Log.e(TAG, "未找到声音资源: sound_picker.ogg");
                isSoundEnabled = false;
                return;
            }

            mediaPlayer = MediaPlayer.create(getContext(), soundResourceId);
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(0.3f, 0.3f); // 降低音量避免刺耳
                Log.d(TAG, "媒体播放器初始化成功");
            } else {
                Log.e(TAG, "无法创建媒体播放器");
                isSoundEnabled = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "初始化媒体播放器失败: " + e.getMessage());
            isSoundEnabled = false;
        }
    }

    private void sendPlayMessage() {
        if (soundHandler == null || mediaPlayer == null || !isSoundEnabled) return;

        long currentTime = System.currentTimeMillis();
        long timeSinceLastPlay = currentTime - lastPlayTime;

        if (timeSinceLastPlay >= MIN_PLAY_INTERVAL) {
            soundHandler.removeMessages(MSG_PLAY_SOUND);
            soundHandler.sendEmptyMessage(MSG_PLAY_SOUND);
        } else {
            soundHandler.removeMessages(MSG_PLAY_SOUND);
            long delay = MIN_PLAY_INTERVAL - timeSinceLastPlay;
            soundHandler.sendEmptyMessageDelayed(MSG_PLAY_SOUND, delay);
        }
    }

    private void playSound() {
        if (mediaPlayer == null || !isSoundEnabled) {
            return;
        }

        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(0);
            } else {
                mediaPlayer.start();
            }
            lastPlayTime = System.currentTimeMillis();
            Log.d(TAG, "播放拨轮声音");

        } catch (IllegalStateException e) {
            Log.e(TAG, "媒体播放器状态异常: " + e.getMessage());
            // 重新初始化媒体播放器
            reinitMediaPlayer();
        } catch (Exception e) {
            Log.e(TAG, "播放声音时发生错误: " + e.getMessage());
        }
    }

    /**
     * 重新初始化媒体播放器
     */
    private void reinitMediaPlayer() {
        releaseMediaPlayer();
        initMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                Log.d(TAG, "媒体播放器已释放");
            } catch (Exception e) {
                Log.e(TAG, "释放媒体播放器时发生错误: " + e.getMessage());
            }
            mediaPlayer = null;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "附加到窗口");
        // 初始颜色更新
        updateTextColor(getValue());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "从窗口分离");
        releaseMediaPlayer();
        if (soundHandler != null) {
            soundHandler.removeCallbacksAndMessages(null);
        }
        if (mColorUpdateHandler != null) {
            mColorUpdateHandler.removeCallbacksAndMessages(null);
        }
        valueChangeListeners.clear();
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
            // 根据当前值设置颜色
            if (getValue() > 0 && getValue() == targetValue) {
                editText.setTextColor(Color.RED);
            } else {
                editText.setTextColor(Color.BLACK);
            }
            editText.setTextSize(16);
        }
    }

    /**
     * 手动测试声音播放
     */
    public void testSound() {
        Log.d(TAG, "手动测试声音播放");
        if (soundHandler != null) {
            soundHandler.post(new Runnable() {
                @Override
                public void run() {
                    playSound();
                }
            });
        }
    }

    /**
     * 设置音量 (0.0f - 1.0f)
     */
    public void setSoundVolume(float volume) {
        if (mediaPlayer != null && volume >= 0.0f && volume <= 1.0f) {
            mediaPlayer.setVolume(volume, volume);
        }
    }

    /**
     * 强制立即更新颜色（用于特殊情况）
     */
    public void forceUpdateTextColor() {
        mColorUpdateHandler.removeCallbacks(mColorUpdateRunnable);
        updateTextColorFinal(getValue());
    }
}