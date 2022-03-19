package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

public class StockDatabaseTable extends DatabaseTable {
	double mOverlap;
	double mOverlapLow;
	double mOverlapHigh;

	public StockDatabaseTable() {
		init();
	}

	void init() {
		super.init();

		mOverlap = 0;
		mOverlapLow = 0;
		mOverlapHigh = 0;
	}

	@Override
	ContentValues getContentValues(ContentValues contentValues) {
		super.getContentValues(contentValues);

		contentValues.put(DatabaseContract.COLUMN_OVERLAP, mOverlap);
		contentValues.put(DatabaseContract.COLUMN_OVERLAP_LOW, mOverlapLow);
		contentValues.put(DatabaseContract.COLUMN_OVERLAP_HIGH, mOverlapHigh);

		return contentValues;
	}

	void set(StockDatabaseTable stockDatabaseTable) {
		if (stockDatabaseTable == null) {
			return;
		}

		init();

		super.set(stockDatabaseTable);

		setOverlap(stockDatabaseTable.mOverlap);
		setOverlapLow(stockDatabaseTable.mOverlapLow);
		setOverlapHigh(stockDatabaseTable.mOverlapHigh);
	}

	void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		super.set(cursor);

		setOverlap(cursor);
		setOverlapLow(cursor);
		setOverlapHigh(cursor);
	}

	public double getOverlap() {
		return mOverlap;
	}

	public void setOverlap(double overlap) {
		mOverlap = overlap;
	}

	void setOverlap(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setOverlap(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_OVERLAP)));
	}

	public double getOverlapLow() {
		return mOverlapLow;
	}

	public void setOverlapLow(double overlapLow) {
		mOverlapLow = overlapLow;
	}

	void setOverlapLow(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setOverlapLow(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_OVERLAP_LOW)));
	}

	public double getOverlapHigh() {
		return mOverlapHigh;
	}

	public void setOverlapHigh(double overlapHigh) {
		mOverlapHigh = overlapHigh;
	}

	void setOverlapHigh(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setOverlapHigh(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_OVERLAP_HIGH)));
	}

}
