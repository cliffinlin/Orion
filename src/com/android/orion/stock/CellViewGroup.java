package com.android.orion.stock;

import com.android.orion.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CellViewGroup extends FrameLayout {
	private Context mContext;
	public TextView mTextView;
	public View mView;

	public CellViewGroup(Context context) {
		super(context);
		mContext = context;
	}

	public CellViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	private void init(int layoutId) {
		LayoutInflater.from(mContext).inflate(layoutId, this,
				true);
		mView = findViewById(R.id.vg_root);
		mTextView = (TextView) findViewById(R.id.tv_text);
	}
}
