package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.data.Trend;
import com.android.orion.setting.Constant;
import com.android.orion.utility.Utility;

public class StockTrend extends DatabaseTable {

	private long mStockId;
	private String mSE;
	private String mCode;
	private String mName;
	private double mPrice;
	private double mNet;
	private String mPeriod;
	private String mDate;
	private String mTime;
	private int mLevel;
	private String mType;
	private int mFlag;

	public StockTrend() {
		init();
	}

	public StockTrend(String period) {
		init();
		setPeriod(period);
	}

	public StockTrend(StockTrend stockTrend) {
		set(stockTrend);
	}

	public StockTrend(Cursor cursor) {
		set(cursor);
	}

	public boolean isEmpty() {
		return (mStockId == 0) && TextUtils.isEmpty(mDate)
				&& TextUtils.isEmpty(mTime);
	}

	public void init() {
		super.init();

		setTableName(DatabaseContract.StockTrend.TABLE_NAME);

		mStockId = 0;
		mSE = "";
		mCode = "";
		mName = "";
		mPrice = 0;
		mNet = 0;
		mPeriod = "";
		mDate = "";
		mTime = "";
		mLevel = Trend.LEVEL_NONE;
		mType = Trend.TREND_NONE;
		mFlag = Trend.FLAG_NONE;
	}

	@Override
	ContentValues getContentValues(ContentValues contentValues) {
		super.getContentValues(contentValues);
		contentValues.put(DatabaseContract.COLUMN_STOCK_ID, mStockId);
		contentValues.put(DatabaseContract.COLUMN_SE, mSE);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		contentValues.put(DatabaseContract.COLUMN_PRICE, mPrice);
		contentValues.put(DatabaseContract.COLUMN_NET, mNet);
		contentValues.put(DatabaseContract.COLUMN_PERIOD, mPeriod);
		contentValues.put(DatabaseContract.COLUMN_DATE, mDate);
		contentValues.put(DatabaseContract.COLUMN_TIME, mTime);
		contentValues.put(DatabaseContract.COLUMN_LEVEL, mLevel);
		contentValues.put(DatabaseContract.COLUMN_TYPE, mType);
		contentValues.put(DatabaseContract.COLUMN_FLAG, mFlag);
		return contentValues;
	}

	public ContentValues getContentValuesNet() {
		ContentValues contentValues = getContentValues();
		contentValues.put(DatabaseContract.COLUMN_NET, mNet);
		return contentValues;
	}

	public ContentValues getContentValuesFlag() {
		ContentValues contentValues = getContentValues();
		contentValues.put(DatabaseContract.COLUMN_FLAG, mFlag);
		return contentValues;
	}

	public void set(StockTrend stockTrend) {
		if (stockTrend == null) {
			return;
		}

		init();

		super.set(stockTrend);

		setStockId(stockTrend.mStockId);
		setSE(stockTrend.mSE);
		setCode(stockTrend.mCode);
		setName(stockTrend.mName);
		setPrice(stockTrend.mPrice);
		setNet(stockTrend.mNet);
		setPeriod(stockTrend.mPeriod);
		setDate(stockTrend.mDate);
		setTime(stockTrend.mTime);
		setLevel(stockTrend.mLevel);
		setType(stockTrend.mType);
		setFlag(stockTrend.mFlag);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		init();

		super.set(cursor);

		setStockID(cursor);
		setSE(cursor);
		setCode(cursor);
		setName(cursor);
		setPrice(cursor);
		setNet(cursor);
		setPeriod(cursor);
		setDate(cursor);
		setTime(cursor);
		setLevel(cursor);
		setType(cursor);
		setFlag(cursor);
	}

	public long getStockId() {
		return mStockId;
	}

	public void setStockId(long stockId) {
		mStockId = stockId;
	}

	void setStockID(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setStockId(cursor.getLong(cursor
				.getColumnIndex(DatabaseContract.COLUMN_STOCK_ID)));
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

	public double getNet() {
		return mNet;
	}

	public void setNet(double net) {
		mNet = net;
	}

	void setNet(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setNet(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NET)));
	}

	public String getPeriod() {
		return mPeriod;
	}

	public void setPeriod(String period) {
		mPeriod = period;
	}

	void setPeriod(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setPeriod(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PERIOD)));
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(String date) {
		mDate = date;
	}

	public void setDate(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setDate(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DATE)));
	}

	public String getTime() {
		return mTime;
	}

	public void setTime(String time) {
		mTime = time;
	}

	void setTime(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setTime(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TIME)));
	}

	public String getDateTime() {
		if (!TextUtils.isEmpty(mTime)) {
			return mDate + " " + mTime;
		} else {
			return mDate + " " + "00:00:00";
		}
	}

	public int getLevel() {
		return mLevel;
	}

	public void setLevel(int level) {
		mLevel = level;
	}

	void setLevel(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setLevel(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_LEVEL)));
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

	public int getFlag() {
		return mFlag;
	}

	public void setFlag(int flag) {
		mFlag = flag;
	}

	void setFlag(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setFlag(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_FLAG)));
	}

	public void setupNet(double current) {
		if (mPrice == 0 || current == 0) {
			mNet = 0;
			return;
		}
		mNet = Utility.Round2(100 * (current - mPrice) / mPrice);
	}

	public String toString() {
		return  mStockId + Constant.TAB
				+ mSE + Constant.TAB
				+ mCode + Constant.TAB
				+ mName + Constant.TAB
				+ mPrice + Constant.TAB
				+ mNet + Constant.TAB
				+ mPeriod + Constant.TAB
				+ mDate + Constant.TAB
				+ mTime + Constant.TAB
				+ mLevel + Constant.TAB
				+ mType + Constant.TAB
				+ mFlag + Constant.TAB;
	}
}