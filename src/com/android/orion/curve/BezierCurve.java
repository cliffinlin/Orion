package com.android.orion.curve;

import com.android.orion.Constants;

public class BezierCurve {
	double[] mControlDataArray = null;
	YanghuiTriangle mYanghuiTriangle = null;

	public BezierCurve() {
	}

	public void init(int grade) {
		if (mYanghuiTriangle == null) {
			mYanghuiTriangle = new YanghuiTriangle();
		}

		if (mControlDataArray != null) {
			mControlDataArray = null;
		}

		if (grade > Constants.BENZIER_CURVE_GRADE_MAX) {
			grade = Constants.BENZIER_CURVE_GRADE_MAX;
		}

		mControlDataArray = new double[grade + 1];
	}

	public void addControlData(int index, double data) {

		if (mControlDataArray == null) {
			return;
		}

		if (index >= mControlDataArray.length) {
			return;
		}

		mControlDataArray[index] = data;
	}

	public double calculate(double t) {
		double result = 0;
		int grade = 0;

		if (mYanghuiTriangle == null) {
			return result;
		}

		if (mControlDataArray == null) {
			return result;
		}

		grade = mControlDataArray.length - 1;

		for (int i = 0; i <= grade; i++) {
			result += mYanghuiTriangle.mRow[grade][i]
					* Math.pow((1.0 - t), (grade - i)) * Math.pow(t, i)
					* mControlDataArray[i];
		}

		return result;
	}
}
