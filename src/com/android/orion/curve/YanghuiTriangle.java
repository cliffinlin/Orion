package com.android.orion.curve;

import com.android.orion.Constants;

public class YanghuiTriangle {
	public Long[][] mRow = new Long[Constants.BENZIER_CURVE_GRADE_MAX + 1][];

	public YanghuiTriangle() {
		for (int i = 0; i < Constants.BENZIER_CURVE_GRADE_MAX + 1; i++) {
			mRow[i] = calcRow(i);
		}
	}

	Long[] calcRow(int row) {
		if (row == 0) {
			return new Long[] { (long) 1 };
		}

		if (row == 1) {
			return new Long[] { (long) 1, (long) 1 };
		}

		Long[] result = new Long[row + 1];

		for (int i = 0; i < result.length; i++) {
			if (i == 0 || i == result.length - 1) {
				result[i] = (long) 1;
			} else {
				result[i] = mRow[row - 1][i - 1] + mRow[row - 1][i];
			}
		}

		return result;
	}
}
