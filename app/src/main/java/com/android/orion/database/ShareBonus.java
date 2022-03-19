package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

public class ShareBonus extends DatabaseTable {
	private long mStockId;
	private String mDate;
	private double mDividend;
	private String mRDate;

	public ShareBonus() {
		init();
	}

	public ShareBonus(ShareBonus shareBonus) {
		set(shareBonus);
	}

	public ShareBonus(Cursor cursor) {
		set(cursor);
	}

	void init() {
		super.init();

		setTableName(DatabaseContract.ShareBonus.TABLE_NAME);

		mStockId = 0;
		mDate = "";
		mDividend = 0;
		mRDate = "";
	}

	@Override
	ContentValues getContentValues(ContentValues contentValues) {
		super.getContentValues(contentValues);

		contentValues.put(DatabaseContract.COLUMN_STOCK_ID, mStockId);
		contentValues.put(DatabaseContract.COLUMN_DATE, mDate);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND, mDividend);
		contentValues.put(DatabaseContract.COLUMN_R_DATE, mRDate);

		return contentValues;
	}

	public void set(ShareBonus shareBonus) {
		if (shareBonus == null) {
			return;
		}

		init();

		super.set(shareBonus);

		setStockId(shareBonus.mStockId);
		setDate(shareBonus.mDate);
		setDividend(shareBonus.mDividend);
		setRDate(shareBonus.mRDate);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		super.set(cursor);

		setStockID(cursor);
		setDate(cursor);
		setDividend(cursor);
		setRDate(cursor);
	}

	public long getStockId() {
		return mStockId;
	}

	public void setStockId(long stockId) {
		mStockId = stockId;
	}

	void setStockID(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setStockId(cursor.getLong(cursor
				.getColumnIndex(DatabaseContract.COLUMN_STOCK_ID)));
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(String date) {
		mDate = date;
	}

	void setDate(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDate(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DATE)));
	}

	public double getDividend() {
		return mDividend;
	}

	public void setDividend(double dividend) {
		mDividend = dividend;
	}

	void setDividend(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDividend(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIVIDEND)));
	}

	public String getRDate() {
		return mRDate;
	}

	public void setRDate(String rDate) {
		mRDate = rDate;
	}

	void setRDate(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setRDate(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_R_DATE)));
	}
}
