package com.android.orion.stock;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.android.orion.R;
import com.miguelbcr.tablefixheaders.TableFixHeaderAdapter;

public class BodyCellViewGroup extends CellViewGroup implements
		TableFixHeaderAdapter.BodyBinder<List<String>> {

	public BodyCellViewGroup(Context context) {
		super(context);
		init();
	}

	public BodyCellViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	void init() {
		init(R.layout.text_view_group);
	}

	@Override
	public void bindBody(List<String> itemList, int row, int column) {
		mTextView.setText(itemList.get(column + 1));
		mTextView.setTypeface(null, Typeface.NORMAL);
		mView.setBackgroundResource(R.drawable.cell_lightgray_border_bottom_right_gray);
	}
}
