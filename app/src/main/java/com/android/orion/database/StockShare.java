package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

public class StockShare extends DatabaseTable {

	protected String mSE;
	protected String mCode;
	protected String mName;
	private String mDate;
	private double mShare;

	public StockShare() {
		init();
	}

	public StockShare(StockShare stockShare) {
		set(stockShare);
	}

	public StockShare(Cursor cursor) {
		set(cursor);
	}

	void init() {
		super.init();

		setTableName(DatabaseContract.StockShare.TABLE_NAME);

		mSE = "";
		mCode = "";
		mName = "";
		mDate = "";
		mShare = 0;
	}

	@Override
	public ContentValues getContentValues() {
		ContentValues contentValues = super.getContentValues();

		contentValues.put(DatabaseContract.COLUMN_SE, mSE);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		contentValues.put(DatabaseContract.COLUMN_DATE, mDate);
		contentValues.put(DatabaseContract.COLUMN_SHARE, mShare);
		return contentValues;
	}

	public void set(StockShare stockShare) {
		if (stockShare == null) {
			return;
		}

		init();

		super.set(stockShare);

		setSE(stockShare.mSE);
		setCode(stockShare.mCode);
		setName(stockShare.mName);
		setDate(stockShare.mDate);
		setStockShare(stockShare.mShare);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		init();

		super.set(cursor);

		setSE(cursor);
		setCode(cursor);
		setName(cursor);
		setDate(cursor);
		setStockShare(cursor);
	}

	public String getSE() {
		return mSE;
	}

	public void setSE(String se) {
		mSE = se;
	}

	void setSE(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setSE(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_SE)));
	}

	public String getCode() {
		return mCode;
	}

	public void setCode(String code) {
		mCode = code;
	}

	void setCode(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setCode(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_CODE)));
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	void setName(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setName(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NAME)));
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(String date) {
		mDate = date;
	}

	void setDate(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setDate(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DATE)));
	}

	public double getStockShare() {
		return mShare;
	}

	public void setStockShare(double stockShare) {
		mShare = stockShare;
	}

	void setStockShare(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setStockShare(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_SHARE)));
	}
}
