package com.android.orion.stock;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.android.orion.R;
import com.miguelbcr.tablefixheaders.TableFixHeaderAdapter;

public class HeaderCellViewGroup extends CellViewGroup implements
		TableFixHeaderAdapter.HeaderBinder<String> {

	public HeaderCellViewGroup(Context context) {
		super(context);
		init();
	}

	public HeaderCellViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	void init() {
		init(R.layout.text_view_group);
	}

	@Override
	public void bindHeader(String item, int column) {
		mTextView.setText(item);
		mTextView.setTypeface(null, Typeface.BOLD);
		mView.setBackgroundResource(R.drawable.cell_lightgray_border_bottom_right_gray);
	}
}
