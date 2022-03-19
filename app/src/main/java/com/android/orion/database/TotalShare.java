package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

public class TotalShare extends DatabaseTable {
	private long mStockId;
	private String mDate;
	private double mTotalShare;

	public TotalShare() {
		init();
	}

	public TotalShare(TotalShare totalShare) {
		set(totalShare);
	}

	public TotalShare(Cursor cursor) {
		set(cursor);
	}

	void init() {
		super.init();

		setTableName(DatabaseContract.TotalShare.TABLE_NAME);

		mStockId = 0;
		mDate = "";
		mTotalShare = 0;
	}

	@Override
	ContentValues getContentValues(ContentValues contentValues) {
		super.getContentValues(contentValues);

		contentValues.put(DatabaseContract.COLUMN_STOCK_ID, mStockId);
		contentValues.put(DatabaseContract.COLUMN_DATE, mDate);
		contentValues.put(DatabaseContract.COLUMN_TOTAL_SHARE, mTotalShare);

		return contentValues;
	}

	public void set(TotalShare totalShare) {
		if (totalShare == null) {
			return;
		}

		init();

		super.set(totalShare);

		setStockId(totalShare.mStockId);
		setDate(totalShare.mDate);
		setTotalShare(totalShare.mTotalShare);
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
		setTotalShare(cursor);
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

	public double getTotalShare() {
		return mTotalShare;
	}

	public void setTotalShare(double totalShare) {
		mTotalShare = totalShare;
	}

	void setTotalShare(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setTotalShare(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TOTAL_SHARE)));
	}
}
