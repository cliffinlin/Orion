package com.android.orion.stock;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.android.orion.R;
import com.miguelbcr.tablefixheaders.TableFixHeaderAdapter;

public class FirstHeaderCellViewGroup extends CellViewGroup implements
		TableFixHeaderAdapter.FirstHeaderBinder<String> {

	public FirstHeaderCellViewGroup(Context context) {
		super(context);
		init();
	}

	public FirstHeaderCellViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	void init() {
		init(R.layout.text_view_group);
	}

	@Override
	public void bindFirstHeader(String item) {
		mTextView.setText(item);
		mTextView.setTypeface(null, Typeface.BOLD);
		mView.setBackgroundResource(R.drawable.cell_lightgray_border_bottom_right_gray);
	}
}
