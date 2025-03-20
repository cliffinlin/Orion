package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.data.Trend;
import com.android.orion.setting.Constant;
import com.android.orion.utility.Utility;

public class StockTrend extends DatabaseTable {

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
	private int mGrouped;
	private int mDirection;
	private double mVertexLow;
	private double mVertexHigh;
	private double mVertexNet;
	private double mTurning;
	private double mTurningNet;
	private double mTurningRate;
	private double mProfit;

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
		return TextUtils.isEmpty(mDate)
				&& TextUtils.isEmpty(mTime);
	}

	public void init() {
		super.init();

		setTableName(DatabaseContract.StockTrend.TABLE_NAME);

		mSE = "";
		mCode = "";
		mName = "";
		mPrice = 0;
		mNet = 0;
		mPeriod = "";
		mDate = "";
		mTime = "";
		mLevel = Trend.LEVEL_NONE;
		mType = Trend.TYPE_NONE;
		mFlag = Trend.FLAG_NONE;
		mGrouped = Trend.GROUPED_NONE;
		mDirection = Trend.DIRECTION_NONE;
		mVertexLow = 0;
		mVertexHigh = 0;
		mVertexNet = 0;
		mTurning = 0;
		mTurningNet = 0;
		mTurningRate = 0;
		mProfit = 0;
	}

	@Override
	public ContentValues getContentValues() {
		ContentValues contentValues = super.getContentValues();

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
		contentValues.put(DatabaseContract.COLUMN_GROUPED, mGrouped);
		contentValues.put(DatabaseContract.COLUMN_DIRECTION, mDirection);
		contentValues.put(DatabaseContract.COLUMN_VERTEX_LOW, mVertexLow);
		contentValues.put(DatabaseContract.COLUMN_VERTEX_HIGH, mVertexHigh);
		contentValues.put(DatabaseContract.COLUMN_VERTEX_NET, mVertexNet);
		contentValues.put(DatabaseContract.COLUMN_TURNING, mTurning);
		contentValues.put(DatabaseContract.COLUMN_TURNING_NET, mTurningNet);
		contentValues.put(DatabaseContract.COLUMN_TURNING_RATE, mTurningRate);
		contentValues.put(DatabaseContract.COLUMN_PROFIT, mProfit);
		return contentValues;
	}

	public ContentValues getContentValuesGrouped() {
		ContentValues contentValues = super.getContentValues();

		contentValues.put(DatabaseContract.COLUMN_GROUPED, mGrouped);
		return contentValues;
	}

	public ContentValues getContentValuesFlag() {
		ContentValues contentValues = super.getContentValues();

		contentValues.put(DatabaseContract.COLUMN_FLAG, mFlag);
		return contentValues;
	}

	public void set(StockTrend stockTrend) {
		if (stockTrend == null) {
			return;
		}

		init();

		super.set(stockTrend);

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
		setGrouped(stockTrend.mGrouped);
		setDirection(stockTrend.mDirection);
		setVertexLow(stockTrend.mVertexLow);
		setVertexHigh(stockTrend.mVertexHigh);
		setVertexNet(stockTrend.mVertexNet);
		setTurning(stockTrend.mTurning);
		setTurningNet(stockTrend.mTurningNet);
		setTurningRate(stockTrend.mTurningRate);
		setProfit(stockTrend.mProfit);
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
		setPrice(cursor);
		setNet(cursor);
		setPeriod(cursor);
		setDate(cursor);
		setTime(cursor);
		setLevel(cursor);
		setType(cursor);
		setFlag(cursor);
		setGrouped(cursor);
		setDirection(cursor);
		setVertexLow(cursor);
		setVertexHigh(cursor);
		setVertexNet(cursor);
		setTurning(cursor);
		setTurningNet(cursor);
		setTurningRate(cursor);
		setProfit(cursor);
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

	public void setDateTime(StockData stockData) {
		if (stockData == null) {
			return;
		}
		setDate(stockData.getDate());
		setTime(stockData.getTime());
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

	public int getGrouped() {
		return mGrouped;
	}

	public void setGrouped(int grouped) {
		mGrouped = grouped;
	}

	void setGrouped(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setGrouped(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_GROUPED)));
	}

	public int getDirection() {
		return mDirection;
	}

	public void setDirection(int direction) {
		mDirection = direction;
	}

	void setDirection(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setDirection(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIRECTION)));
	}

	public double getVertexLow() {
		return mVertexLow;
	}

	public void setVertexLow(double vertexLow) {
		mVertexLow = vertexLow;
	}

	void setVertexLow(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setVertexLow(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VERTEX_LOW)));
	}

	public double getVertexHigh() {
		return mVertexHigh;
	}

	public void setVertexHigh(double vertexHigh) {
		mVertexHigh = vertexHigh;
	}

	void setVertexHigh(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setVertexHigh(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VERTEX_HIGH)));
	}

	public double getVertexNet() {
		return mVertexNet;
	}

	public void setVertexNet(double vertexNet) {
		mVertexNet = vertexNet;
	}

	void setVertexNet(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setVertexNet(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VERTEX_NET)));
	}

	public double getTurning() {
		return mTurning;
	}

	public void setTurning(double turning) {
		mTurning = turning;
	}

	void setTurning(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setTurning(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TURNING)));
	}

	public double getTurningNet() {
		return mTurningNet;
	}

	public void setTurningNet(double turningNet) {
		mTurningNet = turningNet;
	}

	void setTurningNet(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setTurningNet(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TURNING_NET)));
	}

	public double getTurningRate() {
		return mTurningRate;
	}

	public void setTurningRate(double turningRate) {
		mTurningRate = turningRate;
	}

	void setTurningRate(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setTurningRate(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TURNING_RATE)));
	}

	public double getProfit() {
		return mProfit;
	}

	public void setProfit(double profit) {
		mProfit = profit;
	}

	void setProfit(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setProfit(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PROFIT)));
	}

	public void addFlag(int flag) {
		mFlag |= flag;
	}

	public void removeFlag(int flag) {
		if (hasFlag(flag)) {
			mFlag &= ~flag;
		}
	}

	public boolean hasFlag(int flag) {
		return (mFlag & flag) == flag;
	}

	public void setupVertexNet() {
		if (mDirection == Trend.DIRECTION_NONE || mVertexLow == 0 || mVertexHigh == 0) {
			mVertexNet = 0;
			return;
		}

		if (mDirection == Trend.DIRECTION_UP) {
			mVertexNet = Utility.Round2(100 * (mVertexHigh - mVertexLow) / mVertexLow);
		} else if (mDirection == Trend.DIRECTION_DOWN) {
			mVertexNet = Utility.Round2(100 * (mVertexLow - mVertexHigh) / mVertexHigh);
		}
	}

	public void setupTurningNet() {
		if (mPrice == 0 || mDirection == Trend.DIRECTION_NONE || mVertexLow == 0 || mVertexHigh == 0) {
			mTurningNet = 0;
			return;
		}

		if (mDirection == Trend.DIRECTION_UP) {
			mTurningNet = Utility.Round2(100 * (mPrice - mVertexHigh) / mVertexHigh);
		} else if (mDirection == Trend.DIRECTION_DOWN) {
			mTurningNet = Utility.Round2(100 * (mPrice - mVertexLow) / mVertexLow);
		}
	}

	public void setupTurningRate() {
		if (mPrice == 0 || mDirection == Trend.DIRECTION_NONE || mVertexLow == 0 || mVertexHigh == 0) {
			mTurningRate = 0;
			return;
		}

		if (mDirection == Trend.DIRECTION_UP) {
			mTurningRate = Utility.Round2(100 * Math.abs(mPrice - mVertexHigh) / (mVertexHigh - mVertexLow));
		} else if (mDirection == Trend.DIRECTION_DOWN) {
			mTurningRate = Utility.Round2(100 * Math.abs(mPrice - mVertexLow) / (mVertexHigh - mVertexLow));
		}
	}

	public void setupProfit() {
		if (mPrice == 0 || mTurning == 0) {
			mProfit = 0;
			return;
		}

		mProfit = Utility.Round2(100 * (mPrice - mTurning) / mTurning);
	}

	public void updateStock(Stock stock) {
		if (stock == null) {
			return;
		}
		setName(stock.getName());
		setPrice(stock.getPrice());
		setNet(stock.getNet());
	}

	public void updateTrend(Trend trend) {
		if (trend == null) {
			return;
		}
		setDirection(trend.getDirection());
		setVertexLow(trend.getVertexLow());
		setVertexHigh(trend.getVertexHigh());
		setupVertexNet();
	}

	public void updateTurningProfit() {
		setupTurningNet();
		setupTurningRate();
		setupProfit();
	}

	public String toString() {
		return mSE + Constant.TAB
				+ mCode + Constant.TAB
				+ mName + Constant.TAB
				+ mPrice + Constant.TAB
				+ mNet + Constant.TAB
				+ mPeriod + Constant.TAB
				+ mDate + Constant.TAB
				+ mTime + Constant.TAB
				+ mLevel + Constant.TAB
				+ mType + Constant.TAB
				+ mFlag + Constant.TAB
				+ mDirection + Constant.TAB
				+ mVertexLow + Constant.TAB
				+ mVertexHigh + Constant.TAB
				+ mVertexNet + Constant.TAB
				+ mTurning + Constant.TAB
				+ mTurningNet + Constant.TAB
				+ mTurningRate + Constant.TAB
				+ mProfit + Constant.TAB;
	}

	public String toTrendString() {
		return getPeriod() + " " + Trend.MARK_LEVEL + getLevel() + " " + getType() + " " + (int)getVertexNet() + "/" + (int)getTurningNet();
	}
}