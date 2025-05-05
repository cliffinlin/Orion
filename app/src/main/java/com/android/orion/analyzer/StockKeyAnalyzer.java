package com.android.orion.analyzer;

import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockTrend;

import java.util.ArrayList;

public class StockKeyAnalyzer {

	static final boolean LOG = false;

	private static StockKeyAnalyzer mInstance;

	int mThresholdType;
	double mNaturalRally;
	double mUpwardTrend;
	double mDownwardTrend;
	double mNaturalReaction;
	double mPrevHigh;
	double mPrevLow;

	private StockKeyAnalyzer() {
	}

	public static synchronized StockKeyAnalyzer getInstance() {
		if (mInstance == null) {
			mInstance = new StockKeyAnalyzer();
		}
		return mInstance;
	}

	void init() {
		mThresholdType = StockData.THRESHOLD_NONE;
		mNaturalRally = 0;
		mUpwardTrend = 0;
		mDownwardTrend = 0;
		mNaturalReaction = 0;
		mPrevHigh = 0;
		mPrevLow = 0;
	}

	void analyze(Stock stock, ArrayList<StockData> dataList) {
		init();

		int i = 0;
		int size = 0;
		double threshold = 0;

		StockData prev = null;
		StockData current = null;

		if (stock == null || dataList == null) {
			return;
		}

		size = dataList.size();
		if (size < StockTrend.VERTEX_SIZE) {
			return;
		}

		if (stock.getThreshold() == 0) {
			return;
		}

		threshold = stock.getThreshold() / 100.0;

		i = 0;
		current = dataList.get(i);
		resetThreshold(current);

		for (i = 1; i < size; i++) {
			prev = dataList.get(i - 1);
			current = dataList.get(i);
			resetThreshold(current);

			switch (mThresholdType) {
				case StockData.THRESHOLD_NATURAL_RALLY:
					if (current.getCandle().getHigh() > mNaturalRally) {
						setNaturalRally(current);
						if (current.getCandle().getHigh() > mPrevLow * (1.0 + threshold)) {
							mThresholdType = StockData.THRESHOLD_UPWARD_TREND;
							setUpwardTrend(current);
							current.setNaturalRally(0);
						}
					} else if (current.getCandle().getLow() < mNaturalRally * (1.0 - threshold)) {
						mPrevHigh = mNaturalRally;
						mThresholdType = StockData.THRESHOLD_NATURAL_REACTION;
						setNaturalReaction(current);
					}
					break;

				case StockData.THRESHOLD_UPWARD_TREND:
					if (current.getCandle().getHigh() > mUpwardTrend) {
						setUpwardTrend(current);
					} else if (current.getCandle().getLow() < mUpwardTrend * (1.0 - threshold)) {
						mPrevHigh = mUpwardTrend;
						mThresholdType = StockData.THRESHOLD_NATURAL_REACTION;
						setNaturalReaction(current);
					}
					break;

				case StockData.THRESHOLD_DOWNWARD_TREND:
					if (current.getCandle().getLow() < mDownwardTrend) {
						setDownwardTrend(current);
					} else if (current.getCandle().getHigh() > mDownwardTrend * (1.0 + threshold)) {
						mPrevLow = mDownwardTrend;
						mThresholdType = StockData.THRESHOLD_NATURAL_RALLY;
						setNaturalRally(current);
					}
					break;

				case StockData.THRESHOLD_NATURAL_REACTION:
					if (current.getCandle().getLow() < mNaturalReaction) {
						setNaturalReaction(current);
						if (current.getCandle().getLow() < mPrevHigh * (1.0 - threshold)) {
							mThresholdType = StockData.THRESHOLD_DOWNWARD_TREND;
							setDownwardTrend(current);
							current.setNaturalReaction(0);
						}
					} else if (current.getCandle().getHigh() > mNaturalReaction * (1.0 + threshold)) {
						mPrevLow = mNaturalReaction;
						mThresholdType = StockData.THRESHOLD_NATURAL_RALLY;
						setNaturalRally(current);
					}
					break;

				default:
					if (current.getCandle().getHigh() > prev.getCandle().getHigh()) {
						mThresholdType = StockData.THRESHOLD_UPWARD_TREND;
						mUpwardTrend = current.getCandle().getHigh();
						mDownwardTrend = current.getCandle().getHigh();
						mPrevHigh = current.getCandle().getHigh();
						mPrevLow = current.getCandle().getHigh();
					} else if (current.getCandle().getLow() < prev.getCandle().getLow()) {
						mThresholdType = StockData.THRESHOLD_DOWNWARD_TREND;
						mUpwardTrend = current.getCandle().getLow();
						mDownwardTrend = current.getCandle().getLow();
						mPrevHigh = current.getCandle().getLow();
						mPrevLow = current.getCandle().getLow();
					}
					break;
			}
		}
	}

	void resetThreshold(StockData stockData) {
		if (stockData == null) {
			return;
		}

		stockData.setNaturalRally(0);
		stockData.setUpwardTrend(0);
		stockData.setDownwardTrend(0);
		stockData.setNaturalReaction(0);
	}

	void setNaturalRally(StockData stockData) {
		if (stockData == null) {
			return;
		}

		mNaturalRally = stockData.getCandle().getHigh();
		stockData.setNaturalRally(mNaturalRally);
	}


	void setUpwardTrend(StockData stockData) {
		if (stockData == null) {
			return;
		}

		mUpwardTrend = stockData.getCandle().getHigh();
		stockData.setUpwardTrend(mUpwardTrend);
	}

	void setDownwardTrend(StockData stockData) {
		if (stockData == null) {
			return;
		}

		mDownwardTrend = stockData.getCandle().getLow();
		stockData.setDownwardTrend(mDownwardTrend);
	}


	void setNaturalReaction(StockData stockData) {
		if (stockData == null) {
			return;
		}

		mNaturalReaction = stockData.getCandle().getLow();
		stockData.setNaturalReaction(mNaturalReaction);
	}
}
