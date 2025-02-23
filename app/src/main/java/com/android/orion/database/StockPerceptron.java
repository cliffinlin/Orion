package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.data.LinearRegression;
import com.android.orion.data.Trend;
import com.android.orion.setting.Constant;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Utility;

import java.util.ArrayList;

public class StockPerceptron extends DatabaseTable {
	Logger Log = Logger.getLogger();

	public static final double DEFAULT_DELTA = 0.00001;
	public static final int DESCRIPTION_ROUND_N = 3;

	LinearRegression mLinearRegression = new LinearRegression();

	private String mPeriod;
	private int mLevel;
	private String mType;

	private double mDelta;
	private int mTimes;

	private double mLastError;

	public StockPerceptron() {
		init();
	}

	public StockPerceptron(String period, int level, String type) {
		init();
		setPeriod(period);
		setLevel(level);
		setType(type);
	}

	public StockPerceptron(StockPerceptron stockPerceptron) {
		set(stockPerceptron);
	}

	public StockPerceptron(Cursor cursor) {
		set(cursor);
	}

	public boolean isEmpty() {
		return (mLevel == Trend.LEVEL_NONE) && TextUtils.isEmpty(mPeriod)
				&& TextUtils.isEmpty(mType);
	}

	void init() {
		super.init();

		setTableName(DatabaseContract.StockPerceptron.TABLE_NAME);

		mPeriod = "";
		mLevel = Trend.LEVEL_NONE;
		mType = Trend.TYPE_NONE;
		mDelta = 0.0;
		mTimes = 0;
	}

	@Override
	public ContentValues getContentValues() {
		ContentValues contentValues = super.getContentValues();

		contentValues.put(DatabaseContract.COLUMN_PERIOD, mPeriod);
		contentValues.put(DatabaseContract.COLUMN_LEVEL, mLevel);
		contentValues.put(DatabaseContract.COLUMN_TYPE, mType);
		contentValues.put(DatabaseContract.COLUMN_WEIGHT, mLinearRegression.slope);
		contentValues.put(DatabaseContract.COLUMN_BIAS, mLinearRegression.bias);
		contentValues.put(DatabaseContract.COLUMN_ERROR, mLinearRegression.mse);
		contentValues.put(DatabaseContract.COLUMN_DELTA, mDelta);
		contentValues.put(DatabaseContract.COLUMN_X_MIN, mLinearRegression.xMin);
		contentValues.put(DatabaseContract.COLUMN_X_MAX, mLinearRegression.xMax);
		contentValues.put(DatabaseContract.COLUMN_Y_MIN, mLinearRegression.yMin);
		contentValues.put(DatabaseContract.COLUMN_Y_MAX, mLinearRegression.yMax);
		contentValues.put(DatabaseContract.COLUMN_TIMES, mTimes);
		return contentValues;
	}

	public ContentValues getContentValuesPerceptron() {
		ContentValues contentValues = super.getContentValues();

		contentValues.put(DatabaseContract.COLUMN_WEIGHT, mLinearRegression.slope);
		contentValues.put(DatabaseContract.COLUMN_BIAS, mLinearRegression.bias);
		contentValues.put(DatabaseContract.COLUMN_ERROR, mLinearRegression.mse);
		contentValues.put(DatabaseContract.COLUMN_DELTA, mDelta);
		contentValues.put(DatabaseContract.COLUMN_X_MIN, mLinearRegression.xMin);
		contentValues.put(DatabaseContract.COLUMN_X_MAX, mLinearRegression.xMax);
		contentValues.put(DatabaseContract.COLUMN_Y_MIN, mLinearRegression.yMin);
		contentValues.put(DatabaseContract.COLUMN_Y_MAX, mLinearRegression.yMax);
		contentValues.put(DatabaseContract.COLUMN_TIMES, mTimes);
		return contentValues;
	}

	public void set(StockPerceptron stockPerceptron) {
		if (stockPerceptron == null) {
			return;
		}

		init();

		super.set(stockPerceptron);

		setPeriod(stockPerceptron.mPeriod);
		setLevel(stockPerceptron.mLevel);
		setType(stockPerceptron.mType);
		setWeight(stockPerceptron.mLinearRegression.slope);
		setBias(stockPerceptron.mLinearRegression.bias);
		setError(stockPerceptron.mLinearRegression.mse);
		setDelta(stockPerceptron.mDelta);
		setXMin(stockPerceptron.mLinearRegression.xMin);
		setXMax(stockPerceptron.mLinearRegression.xMax);
		setYMin(stockPerceptron.mLinearRegression.yMin);
		setYMax(stockPerceptron.mLinearRegression.yMax);
		setTimes(stockPerceptron.mTimes);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		init();

		super.set(cursor);

		setPeriod(cursor);
		setLevel(cursor);
		setType(cursor);
		setWeight(cursor);
		setBias(cursor);
		setError(cursor);
		setDelta(cursor);
		setXMin(cursor);
		setXMax(cursor);
		setYMin(cursor);
		setYMax(cursor);
		setTimes(cursor);
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

	public void setWeight(double weight) {
		mLinearRegression.slope = weight;
	}

	void setWeight(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setWeight(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_WEIGHT)));
	}

	public void setBias(double bias) {
		mLinearRegression.bias = bias;
	}

	void setBias(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setBias(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_BIAS)));
	}

	public double getError() {
		return mLinearRegression.mse;
	}

	public void setError(double error) {
		mLinearRegression.mse = error;
	}

	void setError(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setError(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ERROR)));
	}

	public double getDelta() {
		return mDelta;
	}

	public void setDelta(double delta) {
		mDelta = delta;
	}

	void setDelta(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setDelta(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DELTA)));
	}

	public double getXMin() {
		return mLinearRegression.xMin;
	}

	public void setXMin(double xMin) {
		mLinearRegression.xMin = xMin;
	}

	void setXMin(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setXMin(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_X_MIN)));
	}

	public double getXMax() {
		return mLinearRegression.xMax;
	}

	public void setXMax(double xMax) {
		mLinearRegression.xMax = xMax;
	}

	void setXMax(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setXMax(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_X_MAX)));
	}

	public double getYMin() {
		return mLinearRegression.yMin;
	}

	public void setYMin(double yMin) {
		mLinearRegression.yMin = yMin;
	}

	void setYMin(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setYMin(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_Y_MIN)));
	}

	public double getYMax() {
		return mLinearRegression.yMax;
	}

	public void setYMax(double yMax) {
		mLinearRegression.yMax = yMax;
	}

	void setYMax(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setYMax(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_Y_MAX)));
	}

	public int getTimes() {
		return mTimes;
	}

	public void setTimes(int times) {
		mTimes = times;
	}

	void setTimes(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setTimes(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TIMES)));
	}

	public void train(ArrayList<Double> xArray, ArrayList<Double> yArray, int iterations) {
		if (xArray == null || yArray == null || xArray.size() < 2 || yArray.size() < 2 || xArray.size() != yArray.size()) {
			return;
		}

		mTimes += iterations;
		mLinearRegression.train(xArray, yArray, iterations);

		double error = mLinearRegression.calculateError();
		mDelta = Math.abs(error - mLastError);
		mLastError = error;

		if (mDelta > 0 && mDelta < DEFAULT_DELTA) {
			Log.d(toLogString());
		}
	}

	public double predict(double x) {
		return mLinearRegression.predict(x);
	}

	public String toDescriptionString() {
		return  mPeriod + Constant.TAB
				+ mLevel + Constant.TAB
				+ mType + Constant.TAB
				+ Utility.Round(mLinearRegression.slope, DESCRIPTION_ROUND_N) + Constant.TAB
				+ Utility.Round(mLinearRegression.bias, DESCRIPTION_ROUND_N) + Constant.TAB
				+ Utility.Round(mLinearRegression.mse, DESCRIPTION_ROUND_N) + Constant.TAB
				+ mTimes + Constant.TAB;
	}

	public String toLogString() {
		return  "mPeriod=" + mPeriod + Constant.TAB
				+ "mLevel=" + mLevel + Constant.TAB
				+ "mType=" + mType + Constant.TAB
				+ "mWeight=" + mLinearRegression.slope + Constant.TAB
				+ "mBias=" + mLinearRegression.bias + Constant.TAB
				+ "mError=" + mLinearRegression.mse + Constant.TAB
				+ "mDelta=" + mDelta + Constant.TAB
				+ "mTimes=" + mTimes + Constant.TAB;
	}
}