package com.android.orion.indicator;

import java.util.ArrayList;
import java.util.List;

public class MACD {
	static final int N_AVERAGE5 = 5;
	static final int N_AVERAGE10 = 10;

	static final int N_FAST = 12;
	static final int N_SLOW = 26;
	static final int N_SIGNAL = 9;

	public List<Double> mPriceList = null;

	public List<Double> mEMAAverage5List = null;
	public List<Double> mEMAAverage10List = null;
	public List<Double> mEMAFastList = null;
	public List<Double> mEMASlowList = null;
	public List<Double> mDEAList = null;

	public List<Double> mDIFList = null;
	public List<Double> mHistogramList = null;

	public MACD() {
		if (mPriceList == null) {
			mPriceList = new ArrayList<Double>();
		}

		if (mEMAAverage5List == null) {
			mEMAAverage5List = new ArrayList<Double>();
		}

		if (mEMAAverage10List == null) {
			mEMAAverage10List = new ArrayList<Double>();
		}

		if (mEMAFastList == null) {
			mEMAFastList = new ArrayList<Double>();
		}

		if (mEMASlowList == null) {
			mEMASlowList = new ArrayList<Double>();
		}

		if (mDEAList == null) {
			mDEAList = new ArrayList<Double>();
		}

		if (mDIFList == null) {
			mDIFList = new ArrayList<Double>();
		}

		if (mHistogramList == null) {
			mHistogramList = new ArrayList<Double>();
		}
	}

	double getAlpha(int n) {
		double result = 0;

		if (n > 0) {
			result = 2.0 / (n + 1.0);
		}

		return result;
	}

	double EMA(int N, List<Double> dataList, List<Double> emaList) {
		double result = 0;
		double alpha = getAlpha(N);

		if ((dataList == null) || (dataList.size() == 0) || (emaList == null)) {
			return result;
		}

		for (int i = 0; i < dataList.size(); i++) {
			if (i == 0) {
				result = dataList.get(0);
			} else {
				result = alpha * dataList.get(i) + (1.0 - alpha)
						* emaList.get(i - 1);
			}
			emaList.add(result);
		}

		return result;
	}

	public void calculate() {
		int i;
		int size = 0;

		double dif = 0;
		double histogram = 0;

		if ((mPriceList == null) || (mEMAAverage5List == null)
				|| (mEMAAverage10List == null) || (mEMAFastList == null)
				|| (mEMASlowList == null) || (mDEAList == null)
				|| (mDIFList == null) || (mHistogramList == null)) {
			return;
		}

		size = mPriceList.size();

		if (size < 1) {
			return;
		}

		mEMAAverage5List.clear();
		mEMAAverage10List.clear();
		mEMAFastList.clear();
		mEMASlowList.clear();
		mDEAList.clear();
		mDIFList.clear();
		mHistogramList.clear();

		EMA(N_AVERAGE5, mPriceList, mEMAAverage5List);
		EMA(N_AVERAGE10, mPriceList, mEMAAverage10List);
		EMA(N_FAST, mPriceList, mEMAFastList);
		EMA(N_SLOW, mPriceList, mEMASlowList);

		for (i = 0; i < size; i++) {
			dif = mEMAFastList.get(i) - mEMASlowList.get(i);
			mDIFList.add(dif);
		}

		EMA(N_SIGNAL, mDIFList, mDEAList);

		for (i = 0; i < size; i++) {
			histogram = mDIFList.get(i) - mDEAList.get(i);
			mHistogramList.add(histogram);
		}
	}
}
