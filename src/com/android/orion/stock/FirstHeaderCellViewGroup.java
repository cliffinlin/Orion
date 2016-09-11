package com.android.orion.stock;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.android.orion.R;
import com.android.orion.database.Stock;
import com.miguelbcr.tablefixheaders.TableFixHeaderAdapter;

public class FirstHeaderCellViewGroup extends CellViewGroup implements
		TableFixHeaderAdapter.FirstHeaderBinder<Stock> {

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
	public void bindFirstHeader(Stock stock) {
		mTextView.setText(stock.getName());
		mTextView.setTypeface(null, Typeface.BOLD);
		mView.setBackgroundResource(R.drawable.cell_lightgray_border_bottom_right_gray);
	}
}
