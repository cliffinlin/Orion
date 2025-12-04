package com.android.orion.data;

import android.database.Cursor;

import com.android.orion.database.DatabaseContract;
import com.android.orion.utility.Symbol;

public class Candle {
	private double mOpen;
	private double mTop;
	private double mHigh;
	private double mLow;
	private double mBottom;
	private double mClose;

	public Candle() {
		init();
	}

	public void init() {
		mOpen = 0;
		mTop = 0;
		mHigh = 0;
		mLow = 0;
		mBottom = 0;
		mClose = 0;
	}

	public void set(Candle candle) {
		if (candle == null) {
			return;
		}
		setOpen(candle.mOpen);
		setTop(candle.mTop);
		setHigh(candle.mHigh);
		setLow(candle.mLow);
		setBottom(candle.mBottom);
		setClose(candle.mClose);
	}

	public void set(Cursor cursor) {
		setOpen(cursor);
		setTop(cursor);
		setHigh(cursor);
		setLow(cursor);
		setBottom(cursor);
		setClose(cursor);
	}

	public double getOpen() {
		return mOpen;
	}

	public void setOpen(double open) {
		mOpen = open;
	}

	public void setOpen(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setOpen(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_OPEN)));
	}

	public double getTop() {
		return mTop;
	}

	public void setTop(double top) {
		mTop = top;
	}

	public void setTop(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setTop(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TOP)));
	}

	public double getHigh() {
		return mHigh;
	}

	public void setHigh(double high) {
		mHigh = high;
	}

	public void setHigh(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setHigh(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_HIGH)));
	}

	public double getLow() {
		return mLow;
	}

	public void setLow(double low) {
		mLow = low;
	}

	public void setLow(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setLow(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_LOW)));
	}

	public double getBottom() {
		return mBottom;
	}

	public void setBottom(double bottom) {
		mBottom = bottom;
	}

	public void setBottom(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setBottom(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_BOTTOM)));
	}
	public double getClose() {
		return mClose;
	}

	public void setClose(double close) {
		mClose = close;
	}

	public void setClose(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setClose(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_CLOSE)));
	}

	public void merge(Candle candle) {
		if (candle == null) {
			return;
		}
		setHigh(Math.max(getHigh(), candle.getHigh()));
		setLow(Math.min(getLow(), candle.getLow()));
		setClose(candle.getClose());
	}

	public String toString() {
		return mOpen + Symbol.TAB
				+ mTop + Symbol.TAB
				+ mHigh + Symbol.TAB
				+ mLow + Symbol.TAB
				+ mBottom + Symbol.TAB
				+ mClose + Symbol.TAB;
	}
}
