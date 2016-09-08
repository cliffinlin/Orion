package com.android.orion.stock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.orion.R;

public class CellViewGroup extends FrameLayout {
	Context mContext;
	TextView mTextView;
	View mView;

	public CellViewGroup(Context context) {
		super(context);
		mContext = context;
	}

	public CellViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	void init(int resource) {
		LayoutInflater.from(mContext).inflate(resource, this, true);
		mView = findViewById(R.id.vg_root);
		mTextView = (TextView) findViewById(R.id.tv_text);
	}
}
