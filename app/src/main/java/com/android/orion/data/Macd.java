package com.android.orion.data;

import android.content.ContentValues;
import android.database.Cursor;

import com.android.orion.database.DatabaseContract;

public class Macd {
	private double mAverage5;
	private double mAverage10;
	private double mDIF;
	private double mDEA;
	private double mHistogram;
	private double mTarget;

	public Macd() {
		init();
	}

	public void init() {
		mAverage5 = 0;
		mAverage10 = 0;
		mDIF = 0;
		mDEA = 0;
		mHistogram = 0;
		mTarget = 0;
	}

	public ContentValues getContentValues(ContentValues contentValues) {
		if (contentValues != null) {
			contentValues.put(DatabaseContract.COLUMN_AVERAGE5, getAverage5());
			contentValues.put(DatabaseContract.COLUMN_AVERAGE10, getAverage10());
			contentValues.put(DatabaseContract.COLUMN_DIF, getDIF());
			contentValues.put(DatabaseContract.COLUMN_DEA, getDEA());
			contentValues.put(DatabaseContract.COLUMN_HISTOGRAM, getHistogram());
			contentValues.put(DatabaseContract.COLUMN_TARGET, getTarget());
		}
		return contentValues;
	}

	public void set(Macd macd) {
		if (macd == null) {
			return;
		}
		setAverage5(macd.mAverage5);
		setAverage10(macd.mAverage10);
		setDIF(macd.mDIF);
		setDEA(macd.mDEA);
		setHistogram(macd.mHistogram);
		setTarget(macd.mTarget);
	}

	public void set(Cursor cursor) {
		setAverage5(cursor);
		setAverage10(cursor);
		setDIF(cursor);
		setDEA(cursor);
		setHistogram(cursor);
		setTarget(cursor);
	}

	public void set(double average5, double average10, double dif, double dea, double histogram, double target) {
		setAverage5(average5);
		setAverage10(average10);
		setDIF(dif);
		setDEA(dea);
		setHistogram(histogram);
		setTarget(target);
	}

	public double getAverage5() {
		return mAverage5;
	}

	public void setAverage5(double average) {
		mAverage5 = average;
	}

	void setAverage5(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setAverage5(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_AVERAGE5)));
	}

	public double getAverage10() {
		return mAverage10;
	}

	public void setAverage10(double average) {
		mAverage10 = average;
	}

	void setAverage10(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setAverage10(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_AVERAGE10)));
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
