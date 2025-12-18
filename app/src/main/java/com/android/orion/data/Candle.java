package com.android.orion.data;

import android.content.ContentValues;
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

	public boolean isEmpty() {
		return mOpen == 0 || mHigh == 0 || mLow == 0 || mClose == 0;
	}

	public void init() {
		mOpen = 0;
		mTop = 0;
		mHigh = 0;
		mLow = 0;
		mBottom = 0;
		mClose = 0;
	}

	public ContentValues getContentValues(ContentValues contentValues) {
		if (contentValues != null) {
			contentValues.put(DatabaseContract.COLUMN_OPEN, getOpen());
			contentValues.put(DatabaseContract.COLUMN_TOP, getTop());
			contentValues.put(DatabaseContract.COLUMN_HIGH, getHigh());
			contentValues.put(DatabaseContract.COLUMN_LOW, getLow());
			contentValues.put(DatabaseContract.COLUMN_BOTTOM, getBottom());
			contentValues.put(DatabaseContract.COLUMN_CLOSE, getClose());
		}
		return contentValues;
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
		setHigh(Math.max(mHigh, candle.getHigh()));
		setLow(Math.min(mLow, candle.getLow()));
		setClose(candle.getClose());
	}

	public String toTDXContent() {
		return mOpen + Symbol.TAB
				+ mHigh + Symbol.TAB
				+ mLow + Symbol.TAB
				+ mClose + Symbol.TAB;
	}
}
