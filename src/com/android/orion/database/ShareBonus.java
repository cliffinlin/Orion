package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

public class ShareBonus extends DatabaseTable {
	private long mStockId;
	private String mDate;// 公告日期
	private double mDividend;// 派息(税前)(元)(每10股)
	private String mDividendDate;// 除权除息日

	private ShareBonus next;
	private static final Object sPoolSync = new Object();
	private static ShareBonus sPool;

	public static ShareBonus obtain() {
		synchronized (sPoolSync) {
			if (sPool != null) {
				ShareBonus m = sPool;
				sPool = m.next;
				m.next = null;
				return m;
			}
		}
		return new ShareBonus();
	}

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
		mDividendDate = "";
	}

	public ContentValues getContentValues() {
		ContentValues contentValues = new ContentValues();
		super.getContentValues(contentValues);
		contentValues = getContentValues(contentValues);
		return contentValues;
	}

	ContentValues getContentValues(ContentValues contentValues) {
		contentValues.put(DatabaseContract.COLUMN_STOCK_ID, mStockId);
		contentValues.put(DatabaseContract.COLUMN_DATE, mDate);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND, mDividend);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND_DATE, mDividendDate);

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
		setDividendDate(shareBonus.mDividendDate);
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
		setDividendDate(cursor);
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

	public String getDividendDate() {
		return mDividendDate;
	}

	public void setDividendDate(String dividendDate) {
		mDividendDate = dividendDate;
	}

	void setDividendDate(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDividendDate(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIVIDEND_DATE)));
	}
}
