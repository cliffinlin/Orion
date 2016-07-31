package com.android.orion;

import android.os.Bundle;

import com.android.orion.database.DatabaseContract;

public class StockFavoriteEditActivity extends StockEditActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	String getSelection() {
		return DatabaseContract.Stock.COLUMN_MARK + " = '"
				+ Constants.STOCK_FLAG_MARK_FAVORITE + "'";
	}
}
