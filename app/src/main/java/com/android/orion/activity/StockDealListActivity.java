package com.android.orion.activity;

import android.os.Bundle;
import android.text.TextUtils;

import com.android.orion.setting.Constants;
import com.android.orion.database.DatabaseContract;

public class StockDealListActivity extends DealListActivity {
	static final String TAG = Constants.TAG + " "
			+ StockDealListActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mFilterType = FILTER_TYPE_ALL;
	}

	void setupSelection() {
		super.setupSelection();

		String superSelection = mSelection;

		if (mBundle != null) {
			String se = mBundle.getString(Constants.EXTRA_STOCK_SE);
			String code = mBundle.getString(Constants.EXTRA_STOCK_CODE);

			mSelection = DatabaseContract.COLUMN_SE + " = " + "\'" + se + "\'"
					+ " AND " + DatabaseContract.COLUMN_CODE + " = " + "\'"
					+ code + "\'";

			if (!TextUtils.isEmpty(superSelection)) {
				mSelection += " AND " + superSelection;
			}

			mStock.setSE(se);
			mStock.setCode(code);
		}
	}
}
