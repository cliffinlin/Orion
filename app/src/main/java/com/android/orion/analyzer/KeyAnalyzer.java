package com.android.orion.analyzer;

import com.android.orion.data.Trend;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;

import java.util.ArrayList;

public class KeyAnalyzer {

	static final boolean LOG = false;

	int mThresholdType;
	double mNaturalRally;
	double mUpwardTrend;
	double mDownwardTrend;
	double mNaturalReaction;
	double mPrevHigh;
	double mPrevLow;

	private KeyAnalyzer() {
	}

	public static KeyAnalyzer getInstance() {
		return SingletonHelper.INSTANCE;
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
		if (size < Trend.VERTEX_SIZE) {
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
					if (current.getCandlestick().getHigh() > mNaturalRally) {
						setNaturalRally(current);
						if (current.getCandlestick().getHigh() > mPrevLow * (1.0 + threshold)) {
							mThresholdType = StockData.THRESHOLD_UPWARD_TREND;
							setUpwardTrend(current);
							current.setNaturalRally(0);
						}
					} else if (current.getCandlestick().getLow() < mNaturalRally * (1.0 - threshold)) {
						mPrevHigh = mNaturalRally;
						mThresholdType = StockData.THRESHOLD_NATURAL_REACTION;
						setNaturalReaction(current);
					}
					break;

				case StockData.THRESHOLD_UPWARD_TREND:
					if (current.getCandlestick().getHigh() > mUpwardTrend) {
						setUpwardTrend(current);
					} else if (current.getCandlestick().getLow() < mUpwardTrend * (1.0 - threshold)) {
						mPrevHigh = mUpwardTrend;
						mThresholdType = StockData.THRESHOLD_NATURAL_REACTION;
						setNaturalReaction(current);
					}
					break;

				case StockData.THRESHOLD_DOWNWARD_TREND:
					if (current.getCandlestick().getLow() < mDownwardTrend) {
						setDownwardTrend(current);
					} else if (current.getCandlestick().getHigh() > mDownwardTrend * (1.0 + threshold)) {
						mPrevLow = mDownwardTrend;
						mThresholdType = StockData.THRESHOLD_NATURAL_RALLY;
						setNaturalRally(current);
					}
					break;

				case StockData.THRESHOLD_NATURAL_REACTION:
					if (current.getCandlestick().getLow() < mNaturalReaction) {
						setNaturalReaction(current);
						if (current.getCandlestick().getLow() < mPrevHigh * (1.0 - threshold)) {
							mThresholdType = StockData.THRESHOLD_DOWNWARD_TREND;
							setDownwardTrend(current);
							current.setNaturalReaction(0);
						}
					} else if (current.getCandlestick().getHigh() > mNaturalReaction * (1.0 + threshold)) {
						mPrevLow = mNaturalReaction;
						mThresholdType = StockData.THRESHOLD_NATURAL_RALLY;
						setNaturalRally(current);
					}
					break;

				default:
					if (current.getCandlestick().getHigh() > prev.getCandlestick().getHigh()) {
						mThresholdType = StockData.THRESHOLD_UPWARD_TREND;
						mUpwardTrend = current.getCandlestick().getHigh();
						mDownwardTrend = current.getCandlestick().getHigh();
						mPrevHigh = current.getCandlestick().getHigh();
						mPrevLow = current.getCandlestick().getHigh();
					} else if (current.getCandlestick().getLow() < prev.getCandlestick().getLow()) {
						mThresholdType = StockData.THRESHOLD_DOWNWARD_TREND;
						mUpwardTrend = current.getCandlestick().getLow();
						mDownwardTrend = current.getCandlestick().getLow();
						mPrevHigh = current.getCandlestick().getLow();
						mPrevLow = current.getCandlestick().getLow();
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

		mNaturalRally = stockData.getCandlestick().getHigh();
		stockData.setNaturalRally(mNaturalRally);
	}

	void setUpwardTrend(StockData stockData) {
		if (stockData == null) {
			return;
		}

		mUpwardTrend = stockData.getCandlestick().getHigh();
		stockData.setUpwardTrend(mUpwardTrend);
	}

	void setDownwardTrend(StockData stockData) {
		if (stockData == null) {
			return;
		}

		mDownwardTrend = stockData.getCandlestick().getLow();
		stockData.setDownwardTrend(mDownwardTrend);
	}

	void setNaturalReaction(StockData stockData) {
		if (stockData == null) {
			return;
		}

		mNaturalReaction = stockData.getCandlestick().getLow();
		stockData.setNaturalReaction(mNaturalReaction);
	}

	private static class SingletonHelper {
		private static final KeyAnalyzer INSTANCE = new KeyAnalyzer();
	}
}
