package com.android.orion.analyzer;

import com.android.orion.database.Stock;
import com.android.orion.database.StockData;

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
		if (size < StockData.VERTEX_TYPING_SIZE) {
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
					if (current.getHigh() > mNaturalRally) {
						setNaturalRally(current);
						if (current.getHigh() > mPrevLow * (1.0 + threshold)) {
							mThresholdType = StockData.THRESHOLD_UPWARD_TREND;
							setUpwardTrend(current);
							current.setNaturalRally(0);
						}
					} else if (current.getLow() < mNaturalRally * (1.0 - threshold)) {
						mPrevHigh = mNaturalRally;
						mThresholdType = StockData.THRESHOLD_NATURAL_REACTION;
						setNaturalReaction(current);
					}
					break;

				case StockData.THRESHOLD_UPWARD_TREND:
					if (current.getHigh() > mUpwardTrend) {
						setUpwardTrend(current);
					} else if (current.getLow() < mUpwardTrend * (1.0 - threshold)) {
						mPrevHigh = mUpwardTrend;
						mThresholdType = StockData.THRESHOLD_NATURAL_REACTION;
						setNaturalReaction(current);
					}
					break;

				case StockData.THRESHOLD_DOWNWARD_TREND:
					if (current.getLow() < mDownwardTrend) {
						setDownwardTrend(current);
					} else if (current.getHigh() > mDownwardTrend * (1.0 + threshold)) {
						mPrevLow = mDownwardTrend;
						mThresholdType = StockData.THRESHOLD_NATURAL_RALLY;
						setNaturalRally(current);
					}
					break;

				case StockData.THRESHOLD_NATURAL_REACTION:
					if (current.getLow() < mNaturalReaction) {
						setNaturalReaction(current);
						if (current.getLow() < mPrevHigh * (1.0 - threshold)) {
							mThresholdType = StockData.THRESHOLD_DOWNWARD_TREND;
							setDownwardTrend(current);
							current.setNaturalReaction(0);
						}
					} else if (current.getHigh() > mNaturalReaction * (1.0 + threshold)) {
						mPrevLow = mNaturalReaction;
						mThresholdType = StockData.THRESHOLD_NATURAL_RALLY;
						setNaturalRally(current);
					}
					break;

				default:
					if (current.getHigh() > prev.getHigh()) {
						mThresholdType = StockData.THRESHOLD_UPWARD_TREND;
						mUpwardTrend = current.getHigh();
						mDownwardTrend = current.getHigh();
						mPrevHigh = current.getHigh();
						mPrevLow = current.getHigh();
					} else if (current.getLow() < prev.getLow()) {
						mThresholdType = StockData.THRESHOLD_DOWNWARD_TREND;
						mUpwardTrend = current.getLow();
						mDownwardTrend = current.getLow();
						mPrevHigh = current.getLow();
						mPrevLow = current.getLow();
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

		mNaturalRally = stockData.getHigh();
		stockData.setNaturalRally(mNaturalRally);
	}


	void setUpwardTrend(StockData stockData) {
		if (stockData == null) {
			return;
		}

		mUpwardTrend = stockData.getHigh();
		stockData.setUpwardTrend(mUpwardTrend);
	}

	void setDownwardTrend(StockData stockData) {
		if (stockData == null) {
			return;
		}

		mDownwardTrend = stockData.getLow();
		stockData.setDownwardTrend(mDownwardTrend);
	}


	void setNaturalReaction(StockData stockData) {
		if (stockData == null) {
			return;
		}

		mNaturalReaction = stockData.getLow();
		stockData.setNaturalReaction(mNaturalReaction);
	}
}
