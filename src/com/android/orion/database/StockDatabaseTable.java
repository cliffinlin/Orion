package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

public class StockDatabaseTable extends DatabaseTable {
	double mOverlap;
	double mVelocity;
	double mAcceleration;
	double mAccelerationVelocity;

	public StockDatabaseTable() {
		init();
	}

	void init() {
		super.init();

		mOverlap = 0;
		mVelocity = 0;
		mAcceleration = 0;
		mAccelerationVelocity = 0;
	}

	ContentValues getContentValues(ContentValues contentValues) {
		super.getContentValues(contentValues);

		contentValues.put(DatabaseContract.COLUMN_OVERLAP, mOverlap);
		contentValues.put(DatabaseContract.COLUMN_VELOCITY, mVelocity);
		contentValues.put(DatabaseContract.COLUMN_ACCELERATION, mAcceleration);
		contentValues.put(DatabaseContract.COLUMN_ACCELERATION_VELOCITY, mAccelerationVelocity);

		return contentValues;
	}

	void set(StockDatabaseTable stockDatabaseTable) {
		if (stockDatabaseTable == null) {
			return;
		}

		init();

		super.set(stockDatabaseTable);

		setOverlap(stockDatabaseTable.mOverlap);
		setVelocity(stockDatabaseTable.mVelocity);
		setAcceleration(stockDatabaseTable.mAcceleration);
		setAccelerationVelocity(stockDatabaseTable.mAccelerationVelocity);
	}

	void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		super.set(cursor);

		setOverlap(cursor);
		setVelocity(cursor);
		setAcceleration(cursor);
		setAccelerationVelocity(cursor);
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

	public double getVelocity() {
		return mVelocity;
	}

	public void setVelocity(double velocity) {
		mVelocity = velocity;
	}

	void setVelocity(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setVelocity(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VELOCITY)));
	}

	public double getAcceleration() {
		return mAcceleration;
	}

	public void setAcceleration(double acceleration) {
		mAcceleration = acceleration;
	}

	void setAcceleration(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setAcceleration(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ACCELERATION)));
	}

	public double getAccelerationVelocity() {
		return mAccelerationVelocity;
	}

	public void setAccelerationVelocity(double accelerationVelocity) {
		mAccelerationVelocity = accelerationVelocity;
	}

	void setAccelerationVelocity(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setAccelerationVelocity(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ACCELERATION_VELOCITY)));
	}
}
