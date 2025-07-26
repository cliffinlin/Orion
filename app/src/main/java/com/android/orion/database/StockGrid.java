package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.utility.Utility;

public class StockGrid extends DatabaseTable {

	public static final String TYPE_BUY = "BUY";
	public static final String TYPE_NONE = "";
	public static final String TYPE_SELL = "SELL";

	private String mSE;
	private String mCode;
	private String mName;
	private String mType;
	private double mHigh;
	private double mLow;
	private double mGridGap;
	private double mPrice;
	private long mVolume;
	private double mValue;

	public StockGrid() {
		init();
	}

	public StockGrid(StockGrid stockGrid) {
		set(stockGrid);
	}

	public StockGrid(Cursor cursor) {
		set(cursor);
	}

	public void init() {
		super.init();

		setTableName(DatabaseContract.StockGrid.TABLE_NAME);

		mSE = "";
		mCode = "";
		mName = "";
		mType = TYPE_NONE;
		mHigh = 0;
		mLow = 0;
		mGridGap = 0;
		mPrice = 0;
		mVolume = 0;
		mValue = 0;
	}

	@Override
	public ContentValues getContentValues() {
		ContentValues contentValues = super.getContentValues();

		contentValues.put(DatabaseContract.COLUMN_SE, mSE);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		contentValues.put(DatabaseContract.COLUMN_TYPE, mType);
		contentValues.put(DatabaseContract.COLUMN_HIGH, mHigh);
		contentValues.put(DatabaseContract.COLUMN_LOW, mLow);
		contentValues.put(DatabaseContract.COLUMN_GRID_GAP, mGridGap);
		contentValues.put(DatabaseContract.COLUMN_PRICE, mPrice);
		contentValues.put(DatabaseContract.COLUMN_VOLUME, mVolume);
		contentValues.put(DatabaseContract.COLUMN_VALUE, mValue);
		return contentValues;
	}

	void set(StockGrid stockGrid) {
		if (stockGrid == null) {
			return;
		}

		init();

		super.set(stockGrid);

		setSE(stockGrid.mSE);
		setCode(stockGrid.mCode);
		setName(stockGrid.mName);
		setType(stockGrid.mType);
		setHigh(stockGrid.mHigh);
		setLow(stockGrid.mLow);
		setGridGap(stockGrid.mGridGap);
		setPrice(stockGrid.mPrice);
		setVolume(stockGrid.mVolume);
		setValue(stockGrid.mValue);
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
		setType(cursor);
		setHigh(cursor);
		setLow(cursor);
		setGridGap(cursor);
		setPrice(cursor);
		setVolume(cursor);
		setValue(cursor);
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

	public String getType() {
		return mType;
	}

	public void setType(String type) {
		mType = type;
	}

	void setType(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setType(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TYPE)));
	}

	public double getHigh() {
		return mHigh;
	}

	public void setHigh(double high) {
		mHigh = high;
	}

	void setHigh(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setHigh(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_HIGH)));
	}

	public double getLow() {
		return mLow;
	}

	public void setLow(double low) {
		mLow = low;
	}

	void setLow(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setLow(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_LOW)));
	}

	public double getGridGap() {
		return mGridGap;
	}

	public void setGridGap(double gridGap) {
		mGridGap = gridGap;
	}

	void setGridGap(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setGridGap(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_GRID_GAP)));
	}

	public double getPrice() {
		return mPrice;
	}

	public void setPrice(double price) {
		mPrice = price;
	}

	void setPrice(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setPrice(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PRICE)));
	}

	public long getVolume() {
		return mVolume;
	}

	public void setVolume(long volume) {
		mVolume = volume;
	}

	void setVolume(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setVolume(cursor.getLong(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VOLUME)));
	}

	public double getValue() {
		return mValue;
	}

	public void setValue(double value) {
		mValue = value;
	}

	void setValue(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setValue(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VALUE)));
	}

	public void setupPrice() {
		mPrice = 0;
		if (mHigh == 0 || mLow == 0 || mVolume == 0 || mGridGap == 0) {
			return;
		}

		switch (mType) {
		case TYPE_BUY:
			int n = (int) (100.0 * (mHigh - mLow) / mHigh / mGridGap) + 1;
			mPrice = Utility.Round2(mHigh * (1.0 - n * mGridGap / 100.0));
			break;
		case TYPE_SELL:
			mPrice = Utility.Round2(mLow * (1.0 + mGridGap / 100.0));
			break;
		default:
			break;
		}
	}

	public void setupValue() {
		mValue = Utility.Round2(mPrice * Math.abs(mVolume));
	}

	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mName + " ");
		stringBuffer.append(mType + " ");
		stringBuffer.append("mPrice=" + mPrice + ", ");
		stringBuffer.append("mVolume=" + mVolume + ",  ");
		stringBuffer.append("mValue=" + mValue + ",  ");

		return stringBuffer.toString();
	}

	public String toNotifyString() {
		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mType + " ");
		stringBuffer.append("mPrice=" + mPrice + ", ");
		stringBuffer.append("mVolume=" + mVolume + ",  ");
		stringBuffer.append("mValue=" + mValue + ",  ");

		return stringBuffer.toString();
	}
}
