package com.android.orion;

import org.apache.commons.math.stat.regression.SimpleRegression;

public class StockMatch {

	void test() {
		SimpleRegression regression = new SimpleRegression();
		
		double slope = regression.getSlope();
		double intercept = regression.getIntercept();
	}
}
