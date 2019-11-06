package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

public class StockFilter extends DatabaseTable {
	private String mPE;
	private String mPB;
	private String mDividend;
	private String mYield;
	private String mDelta;

	private StockFilter next;
	private static final Object sPoolSync = new Object();
	private static StockFilter sPool;

	public static StockFilter obtain() {
		synchronized (sPoolSync) {
			if (sPool != null) {
				StockFilter m = sPool;
				sPool = m.next;
				m.next = null;
				return m;
			}
		}

		return new StockFilter();
	}

	public StockFilter() {
		init();
	}

	public StockFilter(StockFilter stockFilter) {
		set(stockFilter);
	}

	public StockFilter(Cursor cursor) {
		set(cursor);
	}

	void init() {
		super.init();

		setTableName(DatabaseContract.StockFilter.TABLE_NAME);

		mPE = "";
		mPB = "";
		mDividend = "";
		mYield = "";
		mDelta = "";
	}

	public ContentValues getContentValues() {
		ContentValues contentValues = new ContentValues();
		super.getContentValues(contentValues);
		contentValues = getContentValues(contentValues);

		return contentValues;
	}

	ContentValues getContentValues(ContentValues contentValues) {
		contentValues.put(DatabaseContract.COLUMN_PE, mPE);
		contentValues.put(DatabaseContract.COLUMN_PB, mPB);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND, mDividend);
		contentValues.put(DatabaseContract.COLUMN_YIELD, mYield);
		contentValues.put(DatabaseContract.COLUMN_DELTA, mDelta);

		return contentValues;
	}

	public void set(StockFilter stockFilter) {
		if (stockFilter == null) {
			return;
		}

		init();

		super.set(stockFilter);

		setPE(stockFilter.mPE);
		setPB(stockFilter.mPB);
		setDividend(stockFilter.mDividend);
		setYield(stockFilter.mYield);
		setDelta(stockFilter.mDelta);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		super.set(cursor);

		setPE(cursor);
		setPB(cursor);
		setDividend(cursor);
		setYield(cursor);
		setDelta(cursor);
	}

	public String getPE() {
		return mPE;
	}

	public void setPE(String pe) {
		mPE = pe;
	}

	public void setPE(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setPE(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PE)));
	}

	public String getPB() {
		return mPB;
	}

	public void setPB(String pb) {
		mPB = pb;
	}

	public void setPB(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setPB(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PB)));
	}

	public String getDividend() {
		return mDividend;
	}

	public void setDividend(String dividend) {
		mDividend = dividend;
	}

	void setDividend(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDividend(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIVIDEND)));
	}

	public String getYield() {
		return mYield;
	}

	public void setYield(String yield) {
		mYield = yield;
	}

	void setYield(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setYield(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_YIELD)));
	}

	public String getDelta() {
		return mDelta;
	}

	public void setDelta(String delta) {
		mDelta = delta;
	}

	void setDelta(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDelta(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DELTA)));
	}
}
