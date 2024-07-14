package com.android.orion.indicator;

import com.android.orion.database.StockData;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;

import java.util.ArrayList;
import java.util.List;

public class MACD {
	static final int AVERAGE5 = 5;
	static final int AVERAGE10 = 10;
	static final int FAST = 10;//12;
	static final int SLOW = 20;//26;
	static final int SIGNAL = 8;//9;

	static String mPeriod;
	static int mAVERAGE5;
	static int mVERAGE10;
	static int mFAST;
	static int mSLOW;
	static int mSIGNAL;

	static List<Double> mPriceList = new ArrayList<>();
	static List<Double> mEMAAverage5List = new ArrayList<>();
	static List<Double> mEMAAverage10List = new ArrayList<>();
	static List<Double> mEMAFastList = new ArrayList<>();
	static List<Double> mEMASlowList = new ArrayList<>();
	static List<Double> mDEAList = new ArrayList<>();
	static List<Double> mDIFList = new ArrayList<>();
	static List<Double> mHistogramList = new ArrayList<>();

	private static MACD mInstance;

	private MACD() {
	}

	public static synchronized MACD getInstance() {
		if (mInstance == null) {
			mInstance = new MACD();
		}
		return mInstance;
	}

	public static List<Double> getEMAAverage5List() {
		return mEMAAverage5List;
	}

	public static List<Double> getEMAAverage10List() {
		return mEMAAverage10List;
	}

	public static List<Double> getDEAList() {
		return mDEAList;
	}

	public static List<Double> getDIFList() {
		return mDIFList;
	}

	public static List<Double> getHistogramList() {
		return mHistogramList;
	}

	static void init(String period, ArrayList<StockData> stockDataList) {
		mPeriod = period;

		if (mPeriod.equals(Setting.SETTING_PERIOD_MONTH)
				|| mPeriod.equals(Setting.SETTING_PERIOD_WEEK)
				|| mPeriod.equals(Setting.SETTING_PERIOD_DAY)) {
			mAVERAGE5 = AVERAGE5;
			mVERAGE10 = AVERAGE10;
			mFAST = FAST;
			mSLOW = SLOW;
			mSIGNAL = SIGNAL;
		} else if (mPeriod.equals(Setting.SETTING_PERIOD_MIN60)) {
			mAVERAGE5 = Constant.MIN60_PER_TRADE_DAY * AVERAGE5;
			mVERAGE10 = Constant.MIN60_PER_TRADE_DAY * AVERAGE10;
			mFAST = Constant.MIN60_PER_TRADE_DAY * FAST;
			mSLOW = Constant.MIN60_PER_TRADE_DAY * SLOW;
			mSIGNAL = Constant.MIN60_PER_TRADE_DAY * SIGNAL;
		} else if (mPeriod.equals(Setting.SETTING_PERIOD_MIN30)) {
			mAVERAGE5 = Constant.MIN30_PER_TRADE_DAY * AVERAGE5;
			mVERAGE10 = Constant.MIN30_PER_TRADE_DAY * AVERAGE10;
			mFAST = Constant.MIN30_PER_TRADE_DAY * FAST;
			mSLOW = Constant.MIN30_PER_TRADE_DAY * SLOW;
			mSIGNAL = Constant.MIN30_PER_TRADE_DAY * SIGNAL;
		} else if (mPeriod.equals(Setting.SETTING_PERIOD_MIN15)) {
			mAVERAGE5 = Constant.MIN15_PER_TRADE_DAY * AVERAGE5;
			mVERAGE10 = Constant.MIN15_PER_TRADE_DAY * AVERAGE10;
			mFAST = Constant.MIN15_PER_TRADE_DAY * FAST;
			mSLOW = Constant.MIN15_PER_TRADE_DAY * SLOW;
			mSIGNAL = Constant.MIN15_PER_TRADE_DAY * SIGNAL;
		} else if (mPeriod.equals(Setting.SETTING_PERIOD_MIN5)) {
			mAVERAGE5 = Constant.MIN5_PER_TRADE_DAY * AVERAGE5;
			mVERAGE10 = Constant.MIN5_PER_TRADE_DAY * AVERAGE10;
			mFAST = Constant.MIN5_PER_TRADE_DAY * FAST;
			mSLOW = Constant.MIN5_PER_TRADE_DAY * SLOW;
			mSIGNAL = Constant.MIN5_PER_TRADE_DAY * SIGNAL;
		}

		mPriceList.clear();
		mEMAAverage5List.clear();
		mEMAAverage10List.clear();
		mEMAFastList.clear();
		mEMASlowList.clear();
		mDEAList.clear();
		mDIFList.clear();
		mHistogramList.clear();

		if (stockDataList == null) {
			return;
		}

		for (int i = 0; i < stockDataList.size(); i++) {
			mPriceList.add(stockDataList.get(i).getClose());
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

	public void calculate(String period, ArrayList<StockData> stockDataList) {
		int i;
		int size = 0;

		init(period, stockDataList);

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

		EMA(mAVERAGE5, mPriceList, mEMAAverage5List);
		EMA(mVERAGE10, mPriceList, mEMAAverage10List);
		EMA(mFAST, mPriceList, mEMAFastList);
		EMA(mSLOW, mPriceList, mEMASlowList);

		for (i = 0; i < size; i++) {
			dif = mEMAFastList.get(i) - mEMASlowList.get(i);
			mDIFList.add(dif);
		}

		EMA(mSIGNAL, mDIFList, mDEAList);

		for (i = 0; i < size; i++) {
			histogram = mDIFList.get(i) - mDEAList.get(i);
			mHistogramList.add(histogram);
		}
	}
}
