package com.android.orion.stock;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;

import com.android.orion.R;
import com.miguelbcr.tablefixheaders.TableFixHeaderAdapter;

public class StockCellViewGroup extends CellViewGroup implements
		TableFixHeaderAdapter.FirstHeaderBinder<String>,
		TableFixHeaderAdapter.HeaderBinder<String>,
		TableFixHeaderAdapter.FirstBodyBinder<Nexus>,
		TableFixHeaderAdapter.BodyBinder<Nexus>,
		TableFixHeaderAdapter.SectionBinder<Nexus> {

	public StockCellViewGroup(Context context) {
		super(context);

		init();
	}

	public StockCellViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	private void init() {
		init(R.layout.text_view_group);
	}

	@Override
	public void bindFirstHeader(String headerName) {
		mTextView.setText(headerName.toUpperCase());
		mTextView.setTypeface(null, Typeface.BOLD);
		mTextView.setGravity(Gravity.CENTER);

		mView.setBackgroundResource(R.drawable.cell_header_border_bottom_right_gray);
	}

	@Override
	public void bindHeader(String headerName, int column) {
		mTextView.setText(headerName.toUpperCase());
		mTextView.setTypeface(null, Typeface.BOLD);
		mTextView.setGravity(Gravity.CENTER);

		mView.setBackgroundResource(R.drawable.cell_header_border_bottom_right_gray);
	}

	@Override
	public void bindFirstBody(Nexus item, int row) {
		mTextView.setText(item.data[0]);
		mTextView.setTypeface(null, Typeface.NORMAL);

		mView.setBackgroundResource(row % 2 == 0 ? R.drawable.cell_lightgray_border_bottom_right_gray
				: R.drawable.cell_white_border_bottom_right_gray);
	}

	@Override
	public void bindBody(Nexus item, int row, int column) {
		mTextView.setText(item.data[column + 1]);
		mTextView.setTypeface(null, Typeface.NORMAL);
		mTextView.setGravity(Gravity.CENTER);

		mView.setBackgroundResource(row % 2 == 0 ? R.drawable.cell_lightgray_border_bottom_right_gray
				: R.drawable.cell_white_border_bottom_right_gray);
	}

	@Override
	public void bindSection(Nexus item, int row, int column) {
		mTextView.setText(column == 0 ? item.type.toUpperCase() : "");
		mTextView.setTypeface(null, Typeface.BOLD);

		mView.setBackgroundResource(R.drawable.cell_blue_border_bottom_right_gray);
	}
}
