package com.android.orion.chart;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

public class TradeLevelPicker extends NumberPicker {

    public TradeLevelPicker(Context context) {
        super(context);
    }

    public TradeLevelPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TradeLevelPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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

    public void setPickerHeight(int height) {
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params != null) {
            params.height = height;
            setLayoutParams(params);
        }
    }

    public void setPickerWidth(int width) {
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params != null) {
            params.width = width;
            setLayoutParams(params);
        }
    }
}