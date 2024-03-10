package com.android.orion.analyzer;

import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.utility.Logger;

import java.util.ArrayList;

public class StockKeyAnalyzer {

	static final boolean LOG = false;
	public Logger Log = Logger.getLogger();
	int mThresholdType = StockData.THRESHOLD_NONE;
	double mNaturalRally;
	double mUpwardTrend;
	double mDownwardTrend;
	double mNaturalReaction;
	double mPrevHigh;
	double mPrevLow;

	public StockKeyAnalyzer() {
		init();
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

	void analyze(Stock stock, String period, ArrayList<StockData> dataList) {
		int i = 0;
		int size = 0;
		double threshold = 0;

		StockData prev = null;
		StockData current = null;

		if (stock == null) {
			return;
		}

		if (dataList == null) {
			return;
		}

		size = dataList.size();
		if (size < StockData.VERTEX_TYPING_SIZE) {
			return;
		}

		threshold = stock.getThreshold();
		if (threshold == 0) {
			return;
		}

		threshold = threshold / 100.0;

		i = 0;
		current = dataList.get(i);
		resetThreshold(current);

		for (i = 1; i < size; i++) {
			prev = dataList.get(i - 1);
			current = dataList.get(i);
			resetThreshold(current);

			DEBUG("i=" + i + " current.getDate()=" + current.getDate() + " current.getLow()=" + current.getLow() + " current.getHigh()=" + current.getHigh());

			switch (mThresholdType) {
				case StockData.THRESHOLD_NATURAL_RALLY:
					DEBUG("case StockData.THRESHOLD_NATURAL_RALLY: mNaturalRally=" + mNaturalRally);
					if (current.getHigh() > mNaturalRally) {
						DEBUG("case StockData.THRESHOLD_NATURAL_RALLY: current.getHigh()=" + current.getHigh() + " > mNaturalRally=" + mNaturalRally);
						setNaturalRally(current);
						if (current.getHigh() > mPrevLow * (1.0 + threshold / 1.0)) {
							DEBUG("case StockData.THRESHOLD_NATURAL_RALLY: current.getHigh()=" + current.getHigh() + " > mPrevLow * (1.0 + threshold / 1.0)=" + mPrevLow * (1.0 + threshold / 1.0));
							mThresholdType = StockData.THRESHOLD_UPWARD_TREND;
							DEBUG("case StockData.THRESHOLD_NATURAL_RALLY: mThresholdType = StockData.THRESHOLD_UPWARD_TREND");
							setUpwardTrend(current);
							current.setNaturalRally(0);
							DEBUG("case StockData.THRESHOLD_NATURAL_RALLY: current.setNaturalRally(0)");
						}
					} else if (current.getLow() < mNaturalRally * (1.0 - threshold)) {
						DEBUG("case StockData.THRESHOLD_NATURAL_RALLY: current.getLow()=" + current.getLow() + " < mNaturalRally * (1.0 - threshold)=" + mNaturalRally * (1.0 - threshold));
						mPrevHigh = mNaturalRally;
						DEBUG("case StockData.THRESHOLD_NATURAL_RALLY: mPrevHigh = mNaturalRally=" + mPrevHigh);
						mThresholdType = StockData.THRESHOLD_NATURAL_REACTION;
						DEBUG("case StockData.THRESHOLD_NATURAL_RALLY: mThresholdType = StockData.THRESHOLD_NATURAL_REACTION");
						setNaturalReaction(current);
					}
					break;

				case StockData.THRESHOLD_UPWARD_TREND:
					DEBUG("case StockData.THRESHOLD_UPWARD_TREND: mUpwardTrend=" + mUpwardTrend);
					if (current.getHigh() > mUpwardTrend) {
						DEBUG("case StockData.THRESHOLD_UPWARD_TREND: current.getHigh()=" + current.getHigh() + " > mUpwardTrend=" + mUpwardTrend);
						setUpwardTrend(current);
					} else if (current.getLow() < mUpwardTrend * (1.0 - threshold)) {
						DEBUG("case StockData.THRESHOLD_UPWARD_TREND: current.getLow()=" + current.getLow() + " < mUpwardTrend * (1.0 - threshold)=" + mUpwardTrend * (1.0 - threshold));
						mPrevHigh = mUpwardTrend;
						DEBUG("case StockData.THRESHOLD_UPWARD_TREND: mPrevHigh = mUpwardTrend=" + mPrevHigh);
						mThresholdType = StockData.THRESHOLD_NATURAL_REACTION;
						DEBUG("case StockData.THRESHOLD_UPWARD_TREND: mThresholdType = StockData.THRESHOLD_NATURAL_REACTION");
						setNaturalReaction(current);
					}
					break;

				case StockData.THRESHOLD_DOWNWARD_TREND:
					DEBUG("case StockData.THRESHOLD_DOWNWARD_TREND: mDownwardTrend=" + mDownwardTrend);
					if (current.getLow() < mDownwardTrend) {
						DEBUG("case StockData.THRESHOLD_DOWNWARD_TREND: current.getLow()=" + current.getLow() + " < mDownwardTrend=" + mDownwardTrend);
						setDownwardTrend(current);
					} else if (current.getHigh() > mDownwardTrend * (1.0 + threshold)) {
						DEBUG("case StockData.THRESHOLD_DOWNWARD_TREND: current.getHigh()=" + current.getHigh() + " > mDownwardTrend * (1.0 + threshold)=" + mDownwardTrend * (1.0 + threshold));
						mPrevLow = mDownwardTrend;
						DEBUG("case StockData.THRESHOLD_DOWNWARD_TREND: mPrevLow = mDownwardTrend=" + mPrevLow);
						mThresholdType = StockData.THRESHOLD_NATURAL_RALLY;
						DEBUG("case StockData.THRESHOLD_DOWNWARD_TREND: mThresholdType = StockData.THRESHOLD_NATURAL_RALLY");
						setNaturalRally(current);
					}
					break;

				case StockData.THRESHOLD_NATURAL_REACTION:
					DEBUG("case StockData.THRESHOLD_NATURAL_REACTION: mNaturalReaction=" + mNaturalReaction);
					if (current.getLow() < mNaturalReaction) {
						DEBUG("case StockData.THRESHOLD_NATURAL_REACTION: current.getLow()=" + current.getLow() + " < mNaturalReaction=" + mNaturalReaction);
						setNaturalReaction(current);
						if (current.getLow() < mPrevHigh * (1.0 - threshold / 1.0)) {
							DEBUG("case StockData.THRESHOLD_NATURAL_REACTION: current.getLow()=" + current.getLow() + "  < mPrevHigh * (1.0 - threshold / 1.0)=" + mPrevHigh * (1.0 - threshold / 1.0));
							mThresholdType = StockData.THRESHOLD_DOWNWARD_TREND;
							DEBUG("case StockData.THRESHOLD_NATURAL_REACTION: mThresholdType = StockData.THRESHOLD_DOWNWARD_TREND");
							setDownwardTrend(current);
							current.setNaturalReaction(0);
							DEBUG("case StockData.THRESHOLD_NATURAL_REACTION: current.setNaturalReaction(0)");
						}
					} else if (current.getHigh() > mNaturalReaction * (1.0 + threshold)) {
						DEBUG("case StockData.THRESHOLD_NATURAL_REACTION: current.getHigh()=" + current.getHigh() + " > mNaturalReaction * (1.0 + threshold)=" + mNaturalReaction * (1.0 + threshold));
						mPrevLow = mNaturalReaction;
						DEBUG("case StockData.THRESHOLD_NATURAL_REACTION: mPrevLow = mNaturalReaction=" + mPrevLow);
						mThresholdType = StockData.THRESHOLD_NATURAL_RALLY;
						DEBUG("case StockData.THRESHOLD_NATURAL_REACTION: mThresholdType = StockData.THRESHOLD_NATURAL_RALLY");
						setNaturalRally(current);
					}
					break;

				default:
					DEBUG("default:");
					if (current.getHigh() > prev.getHigh()) {
						DEBUG("current.getHigh() > prev.getHigh()");
						mThresholdType = StockData.THRESHOLD_UPWARD_TREND;
						DEBUG("mThresholdType = StockData.THRESHOLD_UPWARD_TREND");
						mUpwardTrend = current.getHigh();
						mDownwardTrend = current.getHigh();
						mPrevHigh = current.getHigh();
						mPrevLow = current.getHigh();
						DEBUG("mUpwardTrend = mDownwardTrend = mPrevHigh = mPrevLow = " + current.getHigh());
					} else if (current.getLow() < prev.getLow()) {
						DEBUG("current.getLow() < prev.getLow()");
						mThresholdType = StockData.THRESHOLD_DOWNWARD_TREND;
						DEBUG("mThresholdType = StockData.THRESHOLD_DOWNWARD_TREND");
						mUpwardTrend = current.getLow();
						mDownwardTrend = current.getLow();
						mPrevHigh = current.getLow();
						mPrevLow = current.getLow();
						DEBUG("mUpwardTrend = mDownwardTrend = mPrevHigh = mPrevLow = " + current.getLow());
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
		DEBUG("mNaturalRally=" + mNaturalRally);
	}


	void setUpwardTrend(StockData stockData) {
		if (stockData == null) {
			return;
		}

		mUpwardTrend = stockData.getHigh();
		stockData.setUpwardTrend(mUpwardTrend);
		DEBUG("mUpwardTrend=" + mUpwardTrend);
	}

	void setDownwardTrend(StockData stockData) {
		if (stockData == null) {
			return;
		}

		mDownwardTrend = stockData.getLow();
		stockData.setDownwardTrend(mDownwardTrend);
		DEBUG("mDownwardTrend=" + mDownwardTrend);
	}


	void setNaturalReaction(StockData stockData) {
		if (stockData == null) {
			return;
		}

		mNaturalReaction = stockData.getLow();
		stockData.setNaturalReaction(mNaturalReaction);
		DEBUG("mNaturalReaction=" + mNaturalReaction);
	}

	void DEBUG(String msg) {
		if (LOG) {
			Log.d(msg);
		}
	}
}
