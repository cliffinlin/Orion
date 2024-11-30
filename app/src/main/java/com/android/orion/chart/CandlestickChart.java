package com.android.orion.chart;

import android.database.Cursor;

import com.android.orion.database.DatabaseContract;
import com.android.orion.setting.Constant;

public class CandlestickChart {
	private double mOpen;
	private double mHigh;
	private double mLow;
	private double mClose;

	public void add(CandlestickChart candlestickChart, long weight) {
		if (candlestickChart == null) {
			return;
		}
		mOpen += candlestickChart.mOpen * weight;
		mHigh += candlestickChart.mHigh * weight;
		mLow += candlestickChart.mLow * weight;
		mClose += candlestickChart.mClose * weight;
	}

	public void set(CandlestickChart candlestickChart) {
		if (candlestickChart == null) {
			return;
		}
		setOpen(candlestickChart.mOpen);
		setHigh(candlestickChart.mHigh);
		setLow(candlestickChart.mLow);
		setClose(candlestickChart.mClose);
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
		if (cursor == null) {
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
		if (cursor == null) {
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
		if (cursor == null) {
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
		if (cursor == null) {
			return;
		}

		setClose(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_CLOSE)));
	}

	public String toString() {
		return  mOpen + Constant.TAB
				+ mHigh + Constant.TAB
				+ mLow + Constant.TAB
				+ mClose + Constant.TAB;
	}
}
