package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

public class StockBase extends DatabaseTable {
	double mDividend;
	double mDividendYield;

	public StockBase() {
		init();
	}

	public StockBase(StockBase stockBase) {
		set(stockBase);
	}

	public StockBase(Cursor cursor) {
		set(cursor);
	}

	void init() {
		super.init();

		mDividend = 0;
		mDividendYield = 0;
	}

	@Override
	ContentValues getContentValues(ContentValues contentValues) {
		super.getContentValues(contentValues);

		contentValues.put(DatabaseContract.COLUMN_DIVIDEND, mDividend);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND_YIELD,
				mDividendYield);

		return contentValues;
	}

	public void set(StockBase stock) {
		if (stock == null) {
			return;
		}

		init();

		super.set(stock);

		setDividend(stock.mDividend);
		setDividendYield(stock.mDividendYield);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		super.set(cursor);

		setDividend(cursor);
		setDividendYield(cursor);
	}

	public double getDividend() {
		return mDividend;
	}

	public void setDividend(double dividend) {
		mDividend = dividend;
	}

	void setDividend(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDividend(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIVIDEND)));
	}

	public double getDividendYield() {
		return mDividendYield;
	}

	public void setDividendYield(double dividendYield) {
		mDividendYield = dividendYield;
	}

	void setDividendYield(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDividendYield(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIVIDEND_YIELD)));
	}
}
