package com.android.orion.activity;

import android.os.Bundle;
import android.text.TextUtils;

import com.android.orion.setting.Constant;
import com.android.orion.database.DatabaseContract;

public class StockDealListActivity extends DealListActivity {
	static final String TAG = Constant.TAG + " "
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
			String se = mBundle.getString(Constant.EXTRA_STOCK_SE);
			String code = mBundle.getString(Constant.EXTRA_STOCK_CODE);

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
