package com.android.orion;

import android.os.Bundle;
import android.text.TextUtils;

import com.android.orion.database.DatabaseContract;

public class StockDealListActivity extends DealListActivity {
	static final String TAG = Constants.TAG + " "
			+ StockDealListActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mFilterType = FILTER_TYPE_NONE;
	}

	String getSelection() {
		String selection = null;

		String superSelection = super.getSelection();

		if (mBundle != null) {
			String se = mBundle.getString(Constants.EXTRA_STOCK_SE);
			String code = mBundle.getString(Constants.EXTRA_STOCK_CODE);

			selection = DatabaseContract.COLUMN_SE + " = " + "\'" + se + "\'"
					+ " AND " + DatabaseContract.COLUMN_CODE + " = " + "\'"
					+ code + "\'";

			if (!TextUtils.isEmpty(superSelection)) {
				selection += " AND " + superSelection;
			}
		}

		return selection;
	}
}
