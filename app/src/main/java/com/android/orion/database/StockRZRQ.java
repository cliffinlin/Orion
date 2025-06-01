package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

public class StockRZRQ extends DatabaseTable {

	protected String mSE;
	protected String mCode;
	protected String mName;
	private String mDate;
	private double mRZRemaining;
	private double mRZBuy;
	private double mRZRepay;
	private double mRQRemaining;
	private double mRQSell;
	private double mRQRepay;

	public StockRZRQ() {
		init();
	}

	public StockRZRQ(StockRZRQ stockRZRQ) {
		set(stockRZRQ);
	}

	public StockRZRQ(Cursor cursor) {
		set(cursor);
	}

	void init() {
		super.init();

		setTableName(DatabaseContract.StockRZRQ.TABLE_NAME);

		mSE = "";
		mCode = "";
		mName = "";
		mDate = "";
		mRZRemaining = 0;
		mRZBuy = 0;
		mRZRepay = 0;
		mRQRemaining = 0;
		mRQSell = 0;
		mRQRepay = 0;
	}

	@Override
	public ContentValues getContentValues() {
		ContentValues contentValues = super.getContentValues();

		contentValues.put(DatabaseContract.COLUMN_SE, mSE);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		contentValues.put(DatabaseContract.COLUMN_DATE, mDate);
		contentValues.put(DatabaseContract.COLUMN_RZ_REMAINING, mRZRemaining);
		contentValues.put(DatabaseContract.COLUMN_RZ_BUY, mRZBuy);
		contentValues.put(DatabaseContract.COLUMN_RZ_REPAY, mRZRepay);
		contentValues.put(DatabaseContract.COLUMN_RQ_REMAINING, mRQRemaining);
		contentValues.put(DatabaseContract.COLUMN_RQ_SELL, mRQSell);
		contentValues.put(DatabaseContract.COLUMN_RQ_REPAY, mRQRepay);
		return contentValues;
	}

	public void set(StockRZRQ stockRZRQ) {
		if (stockRZRQ == null) {
			return;
		}

		init();

		super.set(stockRZRQ);

		setSE(stockRZRQ.mSE);
		setCode(stockRZRQ.mCode);
		setName(stockRZRQ.mName);
		setDate(stockRZRQ.mDate);
		setRZRemaining(stockRZRQ.mRZRemaining);
		setRZBuy(stockRZRQ.mRZBuy);
		setRZRepay(stockRZRQ.mRZRepay);
		setRQRemaining(stockRZRQ.mRQRemaining);
		setRQSell(stockRZRQ.mRQSell);
		setRQRepay(stockRZRQ.mRQRepay);
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
		setRZRemaining(cursor);
		setRZBuy(cursor);
		setRZRepay(cursor);
		setRQRemaining(cursor);
		setRQSell(cursor);
		setRQRepay(cursor);
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

	public double getRZRemaining() {
		return mRZRemaining;
	}

	public void setRZRemaining(double rzRemaining) {
		mRZRemaining = rzRemaining;
	}

	void setRZRemaining(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setRZRemaining(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_RZ_REMAINING)));
	}
	public double getRZBuy() {
		return mRZBuy;
	}

	public void setRZBuy(double rzBuy) {
		mRZBuy = rzBuy;
	}

	void setRZBuy(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setRZBuy(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_RZ_BUY)));
	}
	public double getRZRepay() {
		return mRZRepay;
	}

	public void setRZRepay(double rzRepay) {
		mRZRepay = rzRepay;
	}

	void setRZRepay(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setRZRepay(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_RZ_REPAY)));
	}

	public double getRQRemaining() {
		return mRQRemaining;
	}

	public void setRQRemaining(double rqRemaining) {
		mRQRemaining = rqRemaining;
	}

	void setRQRemaining(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setRQRemaining(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_RQ_REMAINING)));
	}

	public double getRQSell() {
		return mRQSell;
	}

	public void setRQSell(double rqSell) {
		mRQSell = rqSell;
	}

	void setRQSell(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setRQSell(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_RQ_SELL)));
	}

	public double getRQRepay() {
		return mRQRepay;
	}

	public void setRQRepay(double rqRepay) {
		mRQRepay = rqRepay;
	}

	void setRQRepay(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setRQRepay(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_RQ_REPAY)));
	}
}
