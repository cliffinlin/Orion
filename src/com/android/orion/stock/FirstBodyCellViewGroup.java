package com.android.orion.stock;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.android.orion.R;
import com.miguelbcr.tablefixheaders.TableFixHeaderAdapter;

public class FirstBodyCellViewGroup extends CellViewGroup implements
		TableFixHeaderAdapter.FirstBodyBinder<List<String>> {

	public FirstBodyCellViewGroup(Context context) {
		super(context);
		init();
	}

	public FirstBodyCellViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	void init() {
		init(R.layout.text_view_group);
	}

	@Override
	public void bindFirstBody(List<String> items, int row) {
		mTextView.setText(items.get(0));
		mTextView.setTypeface(null, Typeface.NORMAL);
		mView.setBackgroundResource(R.drawable.cell_lightgray_border_bottom_right_gray);
	}
}
