package com.android.orion.data;

import android.database.Cursor;

import com.android.orion.database.DatabaseContract;
import com.android.orion.utility.Symbol;

public class Candle {
	private double mOpen;
	private double mHigh;
	private double mLow;
	private double mClose;

	public Candle() {
		init();
	}

	public void init() {
		mOpen = 0;
		mHigh = 0;
		mLow = 0;
		mClose = 0;
	}

	public void set(Candle candle) {
		if (candle == null) {
			return;
		}
		setOpen(candle.mOpen);
		setHigh(candle.mHigh);
		setLow(candle.mLow);
		setClose(candle.mClose);
	}

	public void set(Cursor cursor) {
		setOpen(cursor);
		setHigh(cursor);
		setLow(cursor);
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

	public void add(Candle candle, long weight) {
		if (candle == null) {
			return;
		}
		mOpen += candle.mOpen * weight;
		mHigh += candle.mHigh * weight;
		mLow += candle.mLow * weight;
		mClose += candle.mClose * weight;
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
				+ mHigh + Symbol.TAB
				+ mLow + Symbol.TAB
				+ mClose + Symbol.TAB;
	}
}
