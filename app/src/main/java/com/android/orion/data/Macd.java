package com.android.orion.data;

import android.content.ContentValues;
import android.database.Cursor;

import com.android.orion.database.DatabaseContract;

public class Macd {
	private double mDIF;
	private double mDEA;
	private double mHistogram;
	private double mEcho;

	public Macd() {
		init();
	}

	public void init() {
		mDIF = 0;
		mDEA = 0;
		mHistogram = 0;
		mEcho = 0;
	}

	public ContentValues getContentValues(ContentValues contentValues) {
		if (contentValues != null) {
			contentValues.put(DatabaseContract.COLUMN_DIF, getDIF());
			contentValues.put(DatabaseContract.COLUMN_DEA, getDEA());
			contentValues.put(DatabaseContract.COLUMN_HISTOGRAM, getHistogram());
			contentValues.put(DatabaseContract.COLUMN_ECHO, getEcho());
		}
		return contentValues;
	}

	public void set(Macd macd) {
		if (macd == null) {
			return;
		}
		setDIF(macd.mDIF);
		setDEA(macd.mDEA);
		setHistogram(macd.mHistogram);
		setEcho(macd.mEcho);
	}

	public void set(Cursor cursor) {
		setDIF(cursor);
		setDEA(cursor);
		setHistogram(cursor);
		setEcho(cursor);
	}

	public void set(double dif, double dea, double histogram, double echo) {
		setDIF(dif);
		setDEA(dea);
		setHistogram(histogram);
		setEcho(echo);
	}

	public double getDIF() {
		return mDIF;
	}

	public void setDIF(double dif) {
		mDIF = dif;
	}

	void setDIF(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setDIF(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIF)));
	}

	public double getDEA() {
		return mDEA;
	}

	public void setDEA(double dea) {
		mDEA = dea;
	}

	void setDEA(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setDEA(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DEA)));
	}

	public double getHistogram() {
		return mHistogram;
	}

	public void setHistogram(double histogram) {
		mHistogram = histogram;
	}

	void setHistogram(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setHistogram(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_HISTOGRAM)));
	}

	public double getEcho() {
		return mEcho;
	}

	public void setEcho(double echo) {
		mEcho = echo;
	}

	void setEcho(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setEcho(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ECHO)));
	}
}
