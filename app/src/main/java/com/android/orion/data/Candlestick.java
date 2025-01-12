package com.android.orion.data;

import android.database.Cursor;

import com.android.orion.database.DatabaseContract;
import com.android.orion.setting.Constant;

public class Candlestick {
	private double mOpen;
	private double mHigh;
	private double mLow;
	private double mClose;

	public void add(Candlestick candlestick, long weight) {
		if (candlestick == null) {
			return;
		}
		mOpen += candlestick.mOpen * weight;
		mHigh += candlestick.mHigh * weight;
		mLow += candlestick.mLow * weight;
		mClose += candlestick.mClose * weight;
	}

	public void set(Candlestick candlestick) {
		if (candlestick == null) {
			return;
		}
		setOpen(candlestick.mOpen);
		setHigh(candlestick.mHigh);
		setLow(candlestick.mLow);
		setClose(candlestick.mClose);
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

	public String toString() {
		return mOpen + Constant.TAB
				+ mHigh + Constant.TAB
				+ mLow + Constant.TAB
				+ mClose + Constant.TAB;
	}
}
