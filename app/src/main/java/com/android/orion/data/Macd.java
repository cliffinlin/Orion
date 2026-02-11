package com.android.orion.data;

import android.content.ContentValues;
import android.database.Cursor;

import com.android.orion.database.DatabaseContract;

public class Macd {
	private double mDIF;
	private double mDEA;
	private double mHistogram;
	private double mAdaptive;
	private double mTarget;

	public Macd() {
		init();
	}

	public void init() {
		mDIF = 0;
		mDEA = 0;
		mHistogram = 0;
		mAdaptive = 0;
		mTarget = 0;
	}

	public ContentValues getContentValues(ContentValues contentValues) {
		if (contentValues != null) {
			contentValues.put(DatabaseContract.COLUMN_DIF, getDIF());
			contentValues.put(DatabaseContract.COLUMN_DEA, getDEA());
			contentValues.put(DatabaseContract.COLUMN_HISTOGRAM, getHistogram());
			contentValues.put(DatabaseContract.COLUMN_ADAPTIVE, getAdaptive());
			contentValues.put(DatabaseContract.COLUMN_TARGET, getTarget());
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
		setAdaptive(macd.mAdaptive);
		setTarget(macd.mTarget);
	}

	public void set(Cursor cursor) {
		setDIF(cursor);
		setDEA(cursor);
		setHistogram(cursor);
		setAdaptive(cursor);
		setTarget(cursor);
	}

	public void set(double dif, double dea, double histogram, double adaptive, double target) {
		setDIF(dif);
		setDEA(dea);
		setHistogram(histogram);
		setAdaptive(adaptive);
		setTarget(target);
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

	public double getAdaptive() {
		return mAdaptive;
	}

	public void setAdaptive(double adaptive) {
		mAdaptive = adaptive;
	}

	void setAdaptive(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setAdaptive(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ADAPTIVE)));
	}

	public double getTarget() {
		return mTarget;
	}

	public void setTarget(double target) {
		mTarget = target;
	}

	void setTarget(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setTarget(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TARGET)));
	}
}
