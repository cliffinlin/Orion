package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

public class StockMatch extends DatabaseTable {
	private String mSE_X;
	private String mCode_X;
	private String mName_X;
	private String mSE_Y;
	private String mCode_Y;
	private String mName_Y;
	private double mSlope;
	private double mIntercept;
	private double mMean;
	private double mSTD;
	private double mDelta;

	private StockMatch next;
	private static final Object sPoolSync = new Object();
	private static StockMatch sPool;

	public static StockMatch obtain() {
		synchronized (sPoolSync) {
			if (sPool != null) {
				StockMatch m = sPool;
				sPool = m.next;
				m.next = null;
				return m;
			}
		}
		return new StockMatch();
	}

	public StockMatch() {
		init();
	}

	public StockMatch(StockMatch stockMatch) {
		set(stockMatch);
	}

	public StockMatch(Cursor cursor) {
		set(cursor);
	}

	boolean isEmpty() {
		boolean result = false;

		if (TextUtils.isEmpty(mSE_X) && TextUtils.isEmpty(mCode_X)
				&& TextUtils.isEmpty(mName_X) && TextUtils.isEmpty(mSE_Y)
				&& TextUtils.isEmpty(mCode_Y) && TextUtils.isEmpty(mName_Y)) {
			result = true;
		}

		return result;
	}

	void init() {
		super.init();

		setTableName(DatabaseContract.StockMatch.TABLE_NAME);

		mSE_X = "";
		mCode_X = "";
		mName_X = "";
		mSE_Y = "";
		mCode_Y = "";
		mName_Y = "";
		mSlope = 0;
		mIntercept = 0;
		mMean = 0;
		mSTD = 0;
		mDelta = 0;
	}

	public ContentValues getContentValues() {
		ContentValues contentValues = new ContentValues();
		super.getContentValues(contentValues);
		contentValues = getContentValues(contentValues);
		return contentValues;
	}

	ContentValues getContentValues(ContentValues contentValues) {
		contentValues.put(DatabaseContract.StockMatch.COLUMN_SE_X, mSE_X);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_CODE_X, mCode_X);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_NAME_X, mName_X);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_SE_Y, mSE_Y);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_CODE_Y, mCode_Y);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_NAME_Y, mName_Y);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_SLOPE, mSlope);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_INTERCEPT,
				mIntercept);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_MEAN, mMean);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_STD, mSTD);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_DELTA, mDelta);

		return contentValues;
	}

	void set(StockMatch stockMatch) {
		if (stockMatch == null) {
			return;
		}

		init();

		super.set(stockMatch);

		setSE_X(stockMatch.mSE_X);
		setCode_X(stockMatch.mCode_X);
		setName_X(stockMatch.mName_X);
		setSE_Y(stockMatch.mSE_Y);
		setCode_Y(stockMatch.mCode_Y);
		setName_Y(stockMatch.mName_Y);
		setSlope(stockMatch.mSlope);
		setIntercept(stockMatch.mIntercept);
		setMean(stockMatch.mMean);
		setSTD(stockMatch.mSTD);
		setDelta(stockMatch.mDelta);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		super.set(cursor);

		setSE_X(cursor);
		setCode_X(cursor);
		setName_X(cursor);
		setSE_Y(cursor);
		setCode_Y(cursor);
		setName_Y(cursor);
		setSlope(cursor);
		setIntercept(cursor);
		setMean(cursor);
		setSTD(cursor);
		setDelta(cursor);
	}

	public String getSE_X() {
		return mSE_X;
	}

	public void setSE_X(String se_x) {
		mSE_X = se_x;
	}

	void setSE_X(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setSE_X(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_SE_X)));
	}

	public String getCode_X() {
		return mCode_X;
	}

	public void setCode_X(String code_x) {
		mCode_X = code_x;
	}

	void setCode_X(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setCode_X(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_CODE_X)));
	}

	public String getName_X() {
		return mName_X;
	}

	public void setName_X(String name_x) {
		mName_X = name_x;
	}

	void setName_X(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setName_X(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_NAME_X)));
	}

	public String getSE_Y() {
		return mSE_Y;
	}

	public void setSE_Y(String se_y) {
		mSE_Y = se_y;
	}

	void setSE_Y(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setSE_Y(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_SE_Y)));
	}

	public String getCode_Y() {
		return mCode_Y;
	}

	public void setCode_Y(String code_y) {
		mCode_Y = code_y;
	}

	void setCode_Y(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setCode_Y(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_CODE_Y)));
	}

	public String getName_Y() {
		return mName_Y;
	}

	public void setName_Y(String name_y) {
		mName_Y = name_y;
	}

	void setName_Y(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setName_Y(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_NAME_Y)));
	}

	public double getSlope() {
		return mSlope;
	}

	public void setSlope(double slope) {
		mSlope = slope;
	}

	void setSlope(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setSlope(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_SLOPE)));
	}

	public double getIntercept() {
		return mIntercept;
	}

	public void setIntercept(double intercept) {
		mIntercept = intercept;
	}

	void setIntercept(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setIntercept(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_INTERCEPT)));
	}

	public double getMean() {
		return mMean;
	}

	public void setMean(double mean) {
		mMean = mean;
	}

	void setMean(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setMean(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_MEAN)));
	}
	
	public double getSTD() {
		return mSTD;
	}

	public void setSTD(double std) {
		mSTD = std;
	}

	void setSTD(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setSTD(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_STD)));
	}

	public double getDelta() {
		return mDelta;
	}

	public void setDelta(double delta) {
		mDelta = delta;
	}

	void setDelta(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDelta(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_DELTA)));
	}

	public void setupMatch() {
	}
}
