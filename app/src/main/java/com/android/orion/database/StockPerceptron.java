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
	public static final double DEFAULT_BIAS = 0.0;
	public static final double DEFAULT_DELTA = 0.00001;
	public static final double MAX_VALUE = 10000.0;
	public static final int DESCRIPTION_ROUND_N = 5;

	private ArrayList<Double> mXArray;
	private ArrayList<Double> mYArray;

	private String mPeriod;
	private int mLevel;
	private String mTrend;
	private double mWeight;
	private double mBias;
	private double mError;
	private double mDelta;
	private double mXMin, mXMax, mYMin, mYMax;
	private int mTimes;

	private double mLastError;

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

	void init() {
		super.init();

		setTableName(DatabaseContract.StockPerceptron.TABLE_NAME);

		mPeriod = "";
		mLevel = Trend.LEVEL_NONE;
		mTrend = Trend.TREND_NONE;
		mWeight = DEFAULT_WEIGHT;
		mBias = DEFAULT_BIAS;
		mError = 0.0;
		mDelta = 0.0;
		mXMin = 0.0;
		mXMax = 0.0;
		mYMin = 0.0;
		mYMax = 0.0;
		mTimes = 0;
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
		contentValues.put(DatabaseContract.COLUMN_DELTA, mDelta);
		contentValues.put(DatabaseContract.COLUMN_X_MIN, mXMin);
		contentValues.put(DatabaseContract.COLUMN_X_MAX, mXMax);
		contentValues.put(DatabaseContract.COLUMN_Y_MIN, mYMin);
		contentValues.put(DatabaseContract.COLUMN_Y_MAX, mYMax);
		contentValues.put(DatabaseContract.COLUMN_TIMES, mTimes);
		return contentValues;
	}

	public ContentValues getContentValuesPerceptron() {
		ContentValues contentValues = getContentValues();
		contentValues.put(DatabaseContract.COLUMN_WEIGHT, mWeight);
		contentValues.put(DatabaseContract.COLUMN_BIAS, mBias);
		contentValues.put(DatabaseContract.COLUMN_ERROR, mError);
		contentValues.put(DatabaseContract.COLUMN_DELTA, mDelta);
		contentValues.put(DatabaseContract.COLUMN_X_MIN, mXMin);
		contentValues.put(DatabaseContract.COLUMN_X_MAX, mXMax);
		contentValues.put(DatabaseContract.COLUMN_Y_MIN, mYMin);
		contentValues.put(DatabaseContract.COLUMN_Y_MAX, mYMax);
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
		setTrend(stockPerceptron.mTrend);
		setWeight(stockPerceptron.mWeight);
		setBias(stockPerceptron.mBias);
		setError(stockPerceptron.mError);
		setDelta(stockPerceptron.mDelta);
		setXMin(stockPerceptron.mXMin);
		setXMax(stockPerceptron.mXMax);
		setYMin(stockPerceptron.mYMin);
		setYMax(stockPerceptron.mYMax);
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
		setTrend(cursor);
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

	// 获取反归一化后的斜率
	public double getWeight() {
		return denormalizeSlope(mWeight);
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

	// 获取反归一化后的偏置
	public double getBias() {
		return denormalizeBias(mBias);
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
		return mXMin;
	}

	public void setXMin(double xMin) {
		mXMin = xMin;
	}

	void setXMin(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setXMin(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_X_MIN)));
	}

	public double getXMax() {
		return mXMax;
	}

	public void setXMax(double xMax) {
		mXMax = xMax;
	}

	void setXMax(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setXMax(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_X_MAX)));
	}

	public double getYMin() {
		return mYMin;
	}

	public void setYMin(double yMin) {
		mYMin = yMin;
	}

	void setYMin(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setYMin(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_Y_MIN)));
	}

	public double getYMax() {
		return mYMax;
	}

	public void setYMax(double yMax) {
		mYMax = yMax;
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

	public String toDescriptionString() {
		return  mPeriod + Constant.TAB
				+ mLevel + Constant.TAB
				+ mTrend + Constant.TAB
				+ Utility.Round(mWeight, DESCRIPTION_ROUND_N) + Constant.TAB
				+ Utility.Round(mBias, DESCRIPTION_ROUND_N) + Constant.TAB
				+ Utility.Round(mError, DESCRIPTION_ROUND_N) + Constant.TAB
				+ mTimes + Constant.TAB;
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

	// 归一化数据到 [0, 1] 范围
	private ArrayList<Double> normalize(ArrayList<Double> data, boolean isX) {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;

		// 找到最小值和最大值
		for (double value : data) {
			if (value < min) min = value;
			if (value > max) max = value;
		}

		// 记录最小值和最大值
		if (isX) {
			this.mXMin = min;
			this.mXMax = max;
		} else {
			this.mYMin = min;
			this.mYMax = max;
		}

		// 归一化数据
		ArrayList<Double> normalizedData = new ArrayList<>();
		for (double value : data) {
			normalizedData.add((value - min) / (max - min));
		}
		return normalizedData;
	}

	// 反归一化斜率
	private double denormalizeSlope(double slope) {
		return slope * (mYMax - mYMin) / (mXMax - mXMin);
	}

	// 反归一化偏置
	private double denormalizeBias(double bias) {
		return bias * (mYMax - mYMin) + mYMin - denormalizeSlope(mWeight) * mXMin;
	}

	void resetIfNeed() {
		if (Math.abs(mWeight) > MAX_VALUE || Math.abs(mBias) > MAX_VALUE || Math.abs(mError) > MAX_VALUE) {
			mWeight = DEFAULT_WEIGHT;
			mBias = DEFAULT_BIAS;
			mError = 0.0;
			mLastError = 0.0;
			mDelta = 0.0;
			mTimes = 0;
		}
	}

	// 使用梯度下降法训练模型
	public void train(ArrayList<Double> xArray, ArrayList<Double> yArray, int iterations) {
		if (xArray == null || yArray == null || xArray.size() < 2 || yArray.size() < 2 || xArray.size() != yArray.size() || iterations <= 0) {
			return;
		}

		if (xArray.size() != yArray.size()) {
			return;
		}

		mXArray = normalize(xArray, true);
		mYArray = normalize(yArray, false);
		mTimes = iterations;
		resetIfNeed();

		try {
			for (int i = 0; i < iterations; i++) {
				calculateGradients();
				mError = calculateError();
				mDelta = Math.abs(mLastError - mError);
				mLastError = mError;
				if (mDelta < DEFAULT_DELTA) {
					mTimes = i;
					break;
				}
			}
			Log.d("......" + toLogString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 计算斜率和偏置的梯度
	private void calculateGradients() {
		double slopeGradient = 0;
		double biasGradient = 0;
		int n = mXArray.size();

		for (int i = 0; i < n; i++) {
			double prediction = mWeight * mXArray.get(i) + mBias; // 预测值
			slopeGradient += (-2.0 / n) * mXArray.get(i) * (mYArray.get(i) - prediction); // 斜率梯度
			biasGradient += (-2.0 / n) * (mYArray.get(i) - prediction); // 偏置梯度
		}

		mWeight -= DEFAULT_LEARNING_RATE * slopeGradient; // 更新斜率
		mBias -= DEFAULT_LEARNING_RATE * biasGradient; // 更新偏置
	}

	public static double predict(double weight, double x, double bias) {
		return weight * x + bias;
	}

	// 预测新数据点的值
	public double predict(double x) {
		// 归一化输入
		double normalizedX = (x - mXMin) / (mXMax - mXMin);
		// 计算预测值
		double normalizedY = mWeight * normalizedX + mBias;
		// 反归一化输出
		return normalizedY * (mYMax - mYMin) + mYMin;
	}

	// 计算均方误差 (MSE)
	public double calculateError() {
		double error = 0;
		int n = mXArray.size();

		for (int i = 0; i < n; i++) {
			double prediction = mWeight * mXArray.get(i) + mBias; // 预测值
			double actual = mYArray.get(i); // 实际值
			error += Math.pow(prediction - actual, 2); // 平方误差
		}

		return error / n; // 返回均方误差
	}

	public static void test() {

				// 初始化数据
				ArrayList<Double> xList = new ArrayList<>();
				ArrayList<Double> yList = new ArrayList<>();
				xList.add(32.0);
				xList.add(53.0);
				xList.add(6011.0);
				xList.add(47.0);
				xList.add(59.0);
				xList.add(55.0);
				xList.add(52.0);
				xList.add(39.0);
				xList.add(48.0);
				xList.add(52.0);
				xList.add(45.0);
				xList.add(54.0);
				xList.add(44.0);
				xList.add(58.0);
				xList.add(56.0);
				xList.add(48.0);
				xList.add(44.0);
				xList.add(60.0);

				yList.add(31.0);
				yList.add(68.0);
				yList.add(62.0);
				yList.add(71.0);
				yList.add(87.0);
				yList.add(78.0);
				yList.add(79.0);
				yList.add(59.0);
				yList.add(75.0);
				yList.add(71.0);
				yList.add(55.0);
				yList.add(82.0);
				yList.add(62.0);
				yList.add(75.0);
				yList.add(81.0);
				yList.add(60.0);
				yList.add(82.0);
				yList.add(97.0);

				// 创建线性回归对象
				StockPerceptron linearRegression = new StockPerceptron();

				// 训练模型
				linearRegression.train(xList, yList, 1000);

				// 获取斜率和偏置
				double slope = linearRegression.getWeight();
				double bias = linearRegression.getBias();
				System.out.println("Slope: " + slope);
				System.out.println("Bias: " + bias);

				// 计算均方误差
				double error = linearRegression.calculateError();
				System.out.println("Mean Squared Error: " + error);

				// 预测新数据点
				double x = 50.0;
				double predictedY = linearRegression.predict(x);
				System.out.println("Predicted value for x = " + x + ": " + predictedY);
	}
}