package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.data.Trend;
import com.android.orion.setting.Constant;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Utility;

import java.util.ArrayList;

public class StockPerceptron extends DatabaseTable {
	Logger Log = Logger.getLogger();

	public static final double DEFAULT_LEARNING_RATE = 0.00001;
	public static final double DEFAULT_WEIGHT = 0.0;
	public static final double DEFAULT_BIAS = 1.0;
	public static final double DEFAULT_ERROR = 0.0;
	public static final double DEFAULT_DELTA = 0.00001;
	public static final int DESCRIPTION_ROUND_N = 5;

	private ArrayList<Double> mXArray;
	private ArrayList<Double> mYArray;

	private String mPeriod;
	private int mLevel;
	private String mTrend;
	private double mWeight;
	private double mBias;
	private double mError;

	private int mPoints;
	private int mTimes;
	private double mLastError;
	private double mDelta;

	public StockPerceptron() {
		init();
	}

	public StockPerceptron(String period, int level, String trend) {
		init();
		setPeriod(period);
		setLevel(level);
		setTrend(trend);
	}

	public StockPerceptron(StockPerceptron stockPerceptron) {
		set(stockPerceptron);
	}

	public StockPerceptron(Cursor cursor) {
		set(cursor);
	}

	public boolean isEmpty() {
		return (mLevel == 0) && TextUtils.isEmpty(mPeriod)
				&& TextUtils.isEmpty(mTrend);
	}

	public boolean isLearning() {
		return (mWeight != DEFAULT_WEIGHT) && (mBias != DEFAULT_BIAS) && (mError != DEFAULT_ERROR);
	}

	void init() {
		super.init();

		setTableName(DatabaseContract.StockPerceptron.TABLE_NAME);

		mPeriod = "";
		mLevel = Trend.LEVEL_NONE;
		mTrend = Trend.TREND_NONE;
		mWeight = DEFAULT_WEIGHT;
		mBias = DEFAULT_BIAS;
		mError = DEFAULT_ERROR;
	}

	@Override
	ContentValues getContentValues(ContentValues contentValues) {
		super.getContentValues(contentValues);
		contentValues.put(DatabaseContract.COLUMN_PERIOD, mPeriod);
		contentValues.put(DatabaseContract.COLUMN_LEVEL, mLevel);
		contentValues.put(DatabaseContract.COLUMN_TREND, mTrend);
		contentValues.put(DatabaseContract.COLUMN_WEIGHT, mWeight);
		contentValues.put(DatabaseContract.COLUMN_BIAS, mBias);
		contentValues.put(DatabaseContract.COLUMN_ERROR, mError);
		return contentValues;
	}

	public ContentValues getContentValuesPerceptron() {
		ContentValues contentValues = getContentValues();
		contentValues.put(DatabaseContract.COLUMN_WEIGHT, mWeight);
		contentValues.put(DatabaseContract.COLUMN_BIAS, mBias);
		contentValues.put(DatabaseContract.COLUMN_ERROR, mError);
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
		setTrend(stockPerceptron.mTrend);
		setWeight(stockPerceptron.mWeight);
		setBias(stockPerceptron.mBias);
		setError(stockPerceptron.mError);
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
		setTrend(cursor);
		setWeight(cursor);
		setBias(cursor);
		setError(cursor);
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

	public String getTrend() {
		return mTrend;
	}

	public void setTrend(String trend) {
		mTrend = trend;
	}

	void setTrend(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setTrend(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TREND)));
	}

	public double getWeight() {
		return mWeight;
	}

	public void setWeight(double weight) {
		mWeight = weight;
	}

	void setWeight(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setWeight(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_WEIGHT)));
	}

	public double getBias() {
		return mBias;
	}

	public void setBias(double bias) {
		mBias = bias;
	}

	void setBias(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setBias(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_BIAS)));
	}

	public double getError() {
		return mError;
	}

	public void setError(double error) {
		mError = error;
	}

	void setError(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setError(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ERROR)));
	}

	void resetIfNeed() {
//		if (Math.abs(mWeight) > MAX_VALUE || Math.abs(mBias) > MAX_VALUE || Math.abs(mError) > MAX_VALUE) {
			mWeight = DEFAULT_WEIGHT;
			mBias = DEFAULT_BIAS;
			mError = DEFAULT_ERROR;
			mLastError = DEFAULT_ERROR;
//		}
	}

	public String toDescriptionString() {
		return  mPeriod + Constant.TAB
				+ mLevel + Constant.TAB
				+ mTrend + Constant.TAB
				+ Utility.Round(mWeight, DESCRIPTION_ROUND_N) + Constant.TAB
				+ Utility.Round(mBias, DESCRIPTION_ROUND_N) + Constant.TAB
				+ Utility.Round(mError, DESCRIPTION_ROUND_N) + Constant.TAB;
	}

	public String toLogString() {
		return  "mPeriod=" + mPeriod + Constant.TAB
				+ "mLevel=" + mLevel + Constant.TAB
				+ "mTrend=" + mTrend + Constant.TAB
				+ "mWeight=" + mWeight + Constant.TAB
				+ "mBias=" + mBias + Constant.TAB
				+ "mError=" + mError + Constant.TAB
				+ "mDelta=" + mDelta + Constant.TAB
				+ "mTimes=" + mTimes + Constant.TAB;
	}

	public double predict(double x) {
		return predict(mWeight, x, mBias);
	}

	public static double predict(double weight, double x, double bias) {
		return weight * x + bias;
	}

	double costError() {
		double total = 0;
		for (int i = 0; i < mPoints; i++) {
			total += Math.pow((mYArray.get(i) - predict(mXArray.get(i))), 2);
		}
		return total / mPoints;
	}

	void updateWeights() {
		double wx;
		double w_deriv = 0;
		double b_deriv = 0;
		for (int i = 0; i < mPoints; i++) {
			wx = mYArray.get(i) - predict(mXArray.get(i));
			w_deriv += -2 * wx * mXArray.get(i);
			b_deriv += -2 * wx;
		}
		mWeight -= (w_deriv / mPoints) * DEFAULT_LEARNING_RATE;
		mBias -= (b_deriv / mPoints) * DEFAULT_LEARNING_RATE;
	}

	public void train(ArrayList<Double> xArray, ArrayList<Double> yArray, int times) {
		if (xArray == null || yArray == null || xArray.size() < 2 || yArray.size() < 2 || xArray.size() != yArray.size() || times <= 0) {
			return;
		}

		mXArray = new ArrayList<>(xArray);
		mYArray = new ArrayList<>(yArray);
		mPoints = mXArray.size();
		mTimes = times;
//		resetIfNeed();

		try {
			for (int i = 0; i < mTimes; i++) {
				updateWeights();
				mError = costError();
				mDelta = Math.abs(mLastError - mError);
				Log.d("=====> i=" + i + " " + toLogString());
				if (mDelta < DEFAULT_DELTA) {
					mTimes = i;
					break;
				}
				mLastError = mError;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}