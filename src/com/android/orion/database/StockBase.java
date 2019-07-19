package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

public class StockBase extends DatabaseTable {
	double mDividend;
	double mYield;

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
		mYield = 0;
	}

	@Override
	ContentValues getContentValues(ContentValues contentValues) {
		super.getContentValues(contentValues);

		return contentValues;
	}

	public void set(StockBase stock) {
		if (stock == null) {
			return;
		}

		init();

		super.set(stock);

		setDividend(stock.mDividend);
		setYield(stock.mYield);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		super.set(cursor);

		setDividend(cursor);
		setYield(cursor);
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

	public double getYield() {
		return mYield;
	}

	public void setYield(double yield) {
		mYield = yield;
	}

	void setYield(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setYield(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_YIELD)));
	}
}
