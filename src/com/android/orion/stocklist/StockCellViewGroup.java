package com.android.orion.stocklist;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;

import com.android.orion.Constants;
import com.android.orion.R;
import com.android.orion.database.Stock;
import com.miguelbcr.tablefixheaders.TableFixHeaderAdapter;

public class StockCellViewGroup extends CellViewGroup implements
		TableFixHeaderAdapter.FirstHeaderBinder<String>,
		TableFixHeaderAdapter.HeaderBinder<String>,
		TableFixHeaderAdapter.FirstBodyBinder<Stock>,
		TableFixHeaderAdapter.BodyBinder<Stock>,
		TableFixHeaderAdapter.SectionBinder<Stock> {

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
		mTextView.setText(headerName);
		mTextView.setTypeface(null, Typeface.BOLD);
		mTextView.setGravity(Gravity.CENTER);

		mView.setBackgroundResource(R.drawable.cell_header_border_bottom_right_gray);
	}

	@Override
	public void bindHeader(String headerName, int column) {
		mTextView.setText(headerName);
		mTextView.setTypeface(null, Typeface.BOLD);
		mTextView.setGravity(Gravity.CENTER);

		mView.setBackgroundResource(R.drawable.cell_header_border_bottom_right_gray);
	}

	@Override
	public void bindFirstBody(Stock stock, int row) {
		mTextView.setText(stock.getName() + "\n" + stock.getCode());
		mTextView.setTypeface(null, Typeface.NORMAL);

		mView.setBackgroundResource(row % 2 == 0 ? R.drawable.cell_lightgray_border_bottom_right_gray
				: R.drawable.cell_white_border_bottom_right_gray);
	}

	@Override
	public void bindBody(Stock stock, int row, int column) {
		switch (column) {
		case 0:
			mTextView.setText(String.valueOf(stock.getPrice()));
			break;
		case 1:
			mTextView.setText(String.valueOf(stock.getNet()));
			break;
		case 2:
			mTextView.setText(stock.getAction(Constants.PERIOD_5MIN));
			break;
		case 3:
			mTextView.setText(stock.getAction(Constants.PERIOD_15MIN));
			break;
		case 4:
			mTextView.setText(stock.getAction(Constants.PERIOD_30MIN));
			break;
		case 5:
			mTextView.setText(stock.getAction(Constants.PERIOD_60MIN));
			break;
		case 6:
			mTextView.setText(stock.getAction(Constants.PERIOD_DAY));
			break;
		case 7:
			mTextView.setText(stock.getAction(Constants.PERIOD_WEEK));
			break;
		case 8:
			mTextView.setText(stock.getAction(Constants.PERIOD_MONTH));
			break;
		default:
			break;
		}

		mTextView.setTypeface(null, Typeface.NORMAL);
		mTextView.setGravity(Gravity.CENTER);

		mView.setBackgroundResource(row % 2 == 0 ? R.drawable.cell_lightgray_border_bottom_right_gray
				: R.drawable.cell_white_border_bottom_right_gray);
	}

	@Override
	public void bindSection(Stock stock, int row, int column) {
		mTextView.setText("");
		mTextView.setTypeface(null, Typeface.BOLD);

		mView.setBackgroundResource(R.drawable.cell_blue_border_bottom_right_gray);
	}
}
