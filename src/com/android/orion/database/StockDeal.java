package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.Constants;
import com.android.orion.utility.Utility;

public class StockDeal extends StockBase {
	private String mSE;
	private String mCode;
	private String mName;
	private double mPrice;
	private double mDeal;
	private double mNet;
	private long mVolume;
	private double mProfit;

	public StockDeal() {
		init();
	}

	public StockDeal(StockDeal stockDeal) {
		set(stockDeal);
	}

	public StockDeal(Cursor cursor) {
		set(cursor);
	}

	boolean isEmpty() {
		boolean result = false;

		if (TextUtils.isEmpty(mSE) && TextUtils.isEmpty(mCode)
				&& TextUtils.isEmpty(mName)) {
			result = true;
		}

		return result;
	}

	public void init() {
		super.init();

		setTableName(DatabaseContract.StockDeal.TABLE_NAME);

		mSE = "";
		mCode = "";
		mName = "";
		mPrice = 0;
		mDeal = 0;
		mNet = 0;
		mVolume = 0;
		mProfit = 0;
	}

	public ContentValues getContentValues() {
		ContentValues contentValues = new ContentValues();
		super.getContentValues(contentValues);
		contentValues = getContentValues(contentValues);
		return contentValues;
	}

	ContentValues getContentValues(ContentValues contentValues) {
		contentValues.put(DatabaseContract.COLUMN_SE, mSE);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		contentValues.put(DatabaseContract.COLUMN_PRICE, mPrice);
		contentValues.put(DatabaseContract.COLUMN_DEAL, mDeal);
		contentValues.put(DatabaseContract.COLUMN_NET, mNet);
		contentValues.put(DatabaseContract.COLUMN_VOLUME, mVolume);
		contentValues.put(DatabaseContract.COLUMN_PROFIT, mProfit);

		return contentValues;
	}

	void set(StockDeal stockDeal) {
		if (stockDeal == null) {
			return;
		}

		init();

		super.set(stockDeal);

		setSE(stockDeal.mSE);
		setCode(stockDeal.mCode);
		setName(stockDeal.mName);
		setPrice(stockDeal.mPrice);
		setDeal(stockDeal.mDeal);
		setNet(stockDeal.mNet);
		setVolume(stockDeal.mVolume);
		setProfit(stockDeal.mProfit);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		super.set(cursor);

		setSE(cursor);
		setCode(cursor);
		setName(cursor);
		setPrice(cursor);
		setDeal(cursor);
		setNet(cursor);
		setVolume(cursor);
		setProfit(cursor);
	}

	public String getSE() {
		return mSE;
	}

	public void setSE(String se) {
		mSE = se;
	}

	void setSE(Cursor cursor) {
		if (cursor == null) {
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

	void setName(Cursor cursor) {
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

	public double getDeal() {
		return mDeal;
	}

	public void setDeal(double deal) {
		mDeal = deal;
	}

	void setDeal(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDeal(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DEAL)));
	}

	public double getNet() {
		return mNet;
	}

	public void setNet(double net) {
		mNet = net;
	}

	void setNet(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setNet(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NET)));
	}

	public long getVolume() {
		return mVolume;
	}

	public void setVolume(long volume) {
		mVolume = volume;
	}

	void setVolume(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setVolume(cursor.getLong(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VOLUME)));
	}

	public double getProfit() {
		return mProfit;
	}

	public void setProfit(double profit) {
		mProfit = profit;
	}

	void setProfit(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setProfit(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PROFIT)));
	}

	public void setupDeal() {
		double change = 0;
		if ((mDeal == 0) || (mVolume == 0)) {
			mNet = 0;
			mProfit = 0;
			return;
		}

		change = mPrice - mDeal;
		mNet = 100 * change / mDeal;
		mProfit = change * mVolume;

		mNet = Utility.Round(mNet, Constants.DOUBLE_FIXED_DECIMAL);
		mProfit = Utility.Round(mProfit, Constants.DOUBLE_FIXED_DECIMAL);
	}

	public void setupProfit() {
		mProfit = Utility.Round((mPrice - mDeal) * mVolume,
				Constants.DOUBLE_FIXED_DECIMAL);
	}

	public void setupYield() {
		if (mDeal == 0) {
			mYield = 0;
			return;
		}

		mYield = Utility.Round(100.0 * mDividend / 10.0 / mDeal,
				Constants.DOUBLE_FIXED_DECIMAL);
	}

	public void setupNet() {
		if (mDeal == 0) {
			mNet = 0;
			return;
		}

		mNet = Utility.Round(100 * (mPrice - mDeal) / mDeal,
				Constants.DOUBLE_FIXED_DECIMAL);
	}
}
