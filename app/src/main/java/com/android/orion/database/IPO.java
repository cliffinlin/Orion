package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

public class IPO extends DatabaseTable {
	private long mStockId;
	private String mCode;
	private String mName;
	private double mPrice;
	private String mDate;
	private String mTimeToMarket;
	private double mPe;

	public IPO() {
		init();
	}

	public IPO(IPO ipo) {
		set(ipo);
	}

	public IPO(Cursor cursor) {
		set(cursor);
	}

	void init() {
		super.init();

		setTableName(DatabaseContract.IPO.TABLE_NAME);

		mStockId = 0;
		mCode = "";
		mName = "";
		mPrice = 0;
		mDate = "";
		mTimeToMarket = "";
		mPe = 0;
	}

	@Override
	ContentValues getContentValues(ContentValues contentValues) {
		super.getContentValues(contentValues);

		contentValues.put(DatabaseContract.COLUMN_STOCK_ID, mStockId);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		contentValues.put(DatabaseContract.COLUMN_PRICE, mPrice);
		contentValues.put(DatabaseContract.COLUMN_DATE, mDate);
		contentValues
				.put(DatabaseContract.COLUMN_TIME_TO_MARKET, mTimeToMarket);
		contentValues.put(DatabaseContract.COLUMN_PE, mPe);

		return contentValues;
	}

	public void set(IPO ipo) {
		if (ipo == null) {
			return;
		}

		init();

		super.set(ipo);

		setStockId(ipo.mStockId);
		setCode(ipo.mCode);
		setName(ipo.mName);
		setPrice(ipo.mPrice);
		setDate(ipo.mDate);
		setDate(ipo.mTimeToMarket);
		setPe(ipo.mPe);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		super.set(cursor);

		setStockID(cursor);
		setCode(cursor);
		setName(cursor);
		setPrice(cursor);
		setDate(cursor);
		setTimeToMarket(cursor);
		setPe(cursor);
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

	public String getCode() {
		return mCode;
	}

	public void setCode(String code) {
		mCode = code;
	}

	public void setCode(Cursor cursor) {
		if (cursor == null) {
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

	public void setName(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setName(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NAME)));
	}

	public double getPrice() {
		return mPrice;
	}

	public void setPrice(double price) {
		mPrice = price;
	}

	void setPrice(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setPrice(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PRICE)));
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

	public String getTimeToMarket() {
		return mTimeToMarket;
	}

	public void setTimeToMarket(String timeToMarket) {
		mTimeToMarket = timeToMarket;
	}

	void setTimeToMarket(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setTimeToMarket(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TIME_TO_MARKET)));
	}

	public double getPe() {
		return mPe;
	}

	public void setPe(double pe) {
		mPe = pe;
	}

	void setPe(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setPe(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PE)));
	}
}
