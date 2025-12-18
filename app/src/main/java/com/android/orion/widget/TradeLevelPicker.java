package com.android.orion.widget;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
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

    private MediaPlayer mediaPlayer;
    private Handler soundHandler;

    // 关键：记录上一次播放声音时的值
    private int lastPlayedValue = -1;

    // 用于存储多个监听器
    private List<OnValueChangeListener> valueChangeListeners = new ArrayList<>();
    private boolean isSoundEnabled = true;

    // 新增：目标值属性
    private int targetValue = 0;
    private OnValueChangeListener externalValueChangeListener;

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
        // 设置长按更新间隔为0，禁用长按连续滚动
        setOnLongPressUpdateInterval(0);

        // 初始化媒体播放器
        initMediaPlayer();

        // 初始化声音处理器
        soundHandler = new SoundHandler(Looper.getMainLooper());

        // 设置内部监听器（代理模式）
        super.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.d(TAG, "onValueChange: " + oldVal + " -> " + newVal);

                // 更新文本颜色
                updateTextColor(newVal);

                // 关键：只有值真正变化时才播放声音
                if (oldVal != newVal && isSoundEnabled) {
                    // 检查是否是连续滚动产生的重复值变化
                    if (newVal != lastPlayedValue) {
                        lastPlayedValue = newVal;
                        sendPlayMessage();
                        Log.d(TAG, "播放声音，值变化: " + oldVal + " -> " + newVal);
                    } else {
                        Log.d(TAG, "跳过声音播放，值与上次播放时相同: " + newVal);
                    }
                }

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
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 手指抬起时，重置lastPlayedValue，确保下一次触摸可以从头开始
                lastPlayedValue = -1;
                Log.d(TAG, "触摸结束，重置lastPlayedValue");
                break;
        }

        // 阻止触摸事件传递给内部EditText
        try {
            Field field = NumberPicker.class.getDeclaredField("mInputText");
            field.setAccessible(true);
            EditText inputText = (EditText) field.get(this);

            if (inputText != null && inputText.isFocused()) {
                inputText.clearFocus();
            }
        } catch (Exception e) {
            // 忽略异常
        }

        return super.onTouchEvent(event);
    }

    /**
     * 禁用内部EditText的点击和聚焦，防止弹出键盘
     */
    private void disableInputEditText() {
        try {
            // 通过反射获取内部EditText
            Field field = NumberPicker.class.getDeclaredField("mInputText");
            field.setAccessible(true);
            EditText inputText = (EditText) field.get(this);

            if (inputText != null) {
                // 禁用所有交互
                inputText.setClickable(false);
                inputText.setFocusable(false);
                inputText.setFocusableInTouchMode(false);
                inputText.setLongClickable(false);
                inputText.setCursorVisible(false);
                inputText.setSoundEffectsEnabled(false);

                // 设置背景透明，看起来像普通文本
                inputText.setBackgroundColor(Color.TRANSPARENT);

                // 对于Android 4.4及以上版本，设置inputType
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    inputText.setShowSoftInputOnFocus(false);
                }

                // 添加触摸监听器，阻止所有触摸事件
                inputText.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true; // 消费所有触摸事件，不传递给父控件
                    }
                });

                Log.d(TAG, "已禁用内部EditText的点击和聚焦");
            }
        } catch (Exception e) {
            Log.e(TAG, "禁用内部EditText失败: " + e.getMessage());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // 阻止内部EditText获取焦点
        clearFocus();
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        updateView(child);
        disableChildView(child);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateView(child);
        disableChildView(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        updateView(child);
        disableChildView(child);
    }

    /**
     * 禁用子视图的点击和聚焦
     */
    private void disableChildView(View child) {
        if (child instanceof EditText) {
            EditText editText = (EditText) child;
            editText.setClickable(false);
            editText.setFocusable(false);
            editText.setFocusableInTouchMode(false);
            editText.setLongClickable(false);
            editText.setCursorVisible(false);

            // 设置触摸监听器，阻止所有触摸事件
            editText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // 消费所有触摸事件
                    return true;
                }
            });

            // 对于Android 4.4及以上版本
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                editText.setShowSoftInputOnFocus(false);
            }
        }
    }

    @Override
    public void setValue(int value) {
        int oldValue = getValue();
        super.setValue(value);

        // 手动触发颜色更新
        updateTextColor(value);

        // 手动触发监听器（如果需要）
        if (oldValue != value) {
            for (OnValueChangeListener listener : valueChangeListeners) {
                if (listener != null) {
                    listener.onValueChange(this, oldValue, value);
                }
            }

            if (externalValueChangeListener != null) {
                externalValueChangeListener.onValueChange(this, oldValue, value);
            }
        }
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
        updateTextColor(getValue());
    }

    /**
     * 获取目标值
     */
    public int getTargetValue() {
        return targetValue;
    }

    /**
     * 更新文本颜色
     */
    private void updateTextColor(int currentValue) {
        // 移除版本检查，确保在所有版本上都工作
        if (currentValue > 0 && currentValue == targetValue) {
            setTextColor(Color.RED);
        } else {
            setTextColor(Color.BLACK);
        }

        // 同时更新所有子视图的颜色
        updateAllChildViews();
    }

    /**
     * 更新所有子视图的文本颜色
     */
    private void updateAllChildViews() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof EditText) {
                EditText editText = (EditText) child;
                if (getValue() > 0 && getValue() == targetValue) {
                    editText.setTextColor(Color.RED);
                } else {
                    editText.setTextColor(Color.BLACK);
                }
            }
        }
    }

    // 启用/禁用声音
    public void setSoundEnabled(boolean enabled) {
        this.isSoundEnabled = enabled;
        Log.d(TAG, "声音 " + (enabled ? "启用" : "禁用"));
    }

    // 检查声音是否启用
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

        // 移除之前的播放消息
        soundHandler.removeMessages(MSG_PLAY_SOUND);
        // 发送新的播放消息
        soundHandler.sendEmptyMessage(MSG_PLAY_SOUND);
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
        // 确保内部EditText被禁用
        disableInputEditText();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "从窗口分离");
        releaseMediaPlayer();
        if (soundHandler != null) {
            soundHandler.removeCallbacksAndMessages(null);
        }
        valueChangeListeners.clear();
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
}