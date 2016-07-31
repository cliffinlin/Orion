package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.Constants;
import com.android.orion.utility.Utility;

public class Deal extends DatabaseTable {
	private String mSE;
	private String mCode;
	private String mName;
	private double mPrice;
	private double mDeal;
	private double mNet;
	private long mVolume;
	private double mProfit;

	private Deal next;
	private static final Object sPoolSync = new Object();
	private static Deal sPool;

	public static Deal obtain() {
		synchronized (sPoolSync) {
			if (sPool != null) {
				Deal m = sPool;
				sPool = m.next;
				m.next = null;
				return m;
			}
		}
		return new Deal();
	}

	public Deal() {
		init();
	}

	public Deal(Deal deal) {
		set(deal);
	}

	public Deal(Cursor cursor) {
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

	void init() {
		super.init();

		setTableName(DatabaseContract.Deal.TABLE_NAME);

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

	void set(Deal deal) {
		if (deal == null) {
			return;
		}

		init();

		super.set(deal);

		setSE(deal.mSE);
		setCode(deal.mCode);
		setName(deal.mName);
		setPrice(deal.mPrice);
		setDeal(deal.mDeal);
		setNet(deal.mNet);
		setVolume(deal.mVolume);
		setProfit(deal.mProfit);
	}

	@Override
	public
	void set(Cursor cursor) {
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
}
