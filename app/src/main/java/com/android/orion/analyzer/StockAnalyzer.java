package com.android.orion.analyzer;

import android.content.Context;
import android.text.TextUtils;

import com.android.orion.application.MainApplication;
import com.android.orion.data.Macd;
import com.android.orion.data.Period;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockTrend;
import com.android.orion.manager.StockDatabaseManager;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.android.orion.utility.StopWatch;
import com.android.orion.utility.Symbol;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.List;


public class StockAnalyzer {
	Stock mStock;
	ArrayList<StockData> mStockDataList;
	ArrayList<Double> mPulseList = new ArrayList<>();

	Context mContext = MainApplication.getContext();
	StockDatabaseManager mStockDatabaseManager = StockDatabaseManager.getInstance();
	FinancialAnalyzer mFinancialAnalyzer = FinancialAnalyzer.getInstance();
	TradeAnalyzer mTradeAnalyzer = TradeAnalyzer.getInstance();
	TrendAnalyzer mTrendAnalyzer = TrendAnalyzer.getInstance();
	Logger Log = Logger.getLogger();

	private StockAnalyzer() {
	}

	public static StockAnalyzer getInstance() {
		return SingletonHolder.INSTANCE;
	}

	void analyze(String period) {
		StopWatch.start();

		if (mStock == null) {
			return;
		}

		try {
			loadStockDataList(period);
			if (Period.indexOf(period) <= Period.indexOf(Period.MONTH)) {
				mFinancialAnalyzer.setNetProfileInYear(mStock, mStockDataList);
			}
			analyzeStockData(period);
			analyzeMACD(period);
			mStockDatabaseManager.updateStockData(mStock, period, mStockDataList);
			mStock.setModified(Utility.getCurrentDateTimeString());
			mStockDatabaseManager.updateStock(mStock, mStock.getContentValues());
		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d(mStock.toLogString() + " " + period + Symbol.TAB + StopWatch.getIntervalString());
	}

	public void analyze(Stock stock) {
		StopWatch.start();

		mStock = stock;
		if (mStock == null) {
			return;
		}

		try {
			mStockDatabaseManager.getStock(stock);
			mFinancialAnalyzer.analyzeFinancial(mStock);
			mFinancialAnalyzer.setupFinancial(mStock);
			mFinancialAnalyzer.setupStockBonus(mStock);
			for (String period : Period.PERIODS) {
				if (Setting.getPeriod(period)) {
					analyze(period);
				}
			}
			mTrendAnalyzer.analyzeAdaptive(mStock);
			mFinancialAnalyzer.analyzeFinancial(mStock);
			mFinancialAnalyzer.setupFinancial(mStock);
			mFinancialAnalyzer.setupStockBonus(mStock);
			mTradeAnalyzer.analyze(mStock);
			mStock.setModified(Utility.getCurrentDateTimeString());
			mStockDatabaseManager.updateStock(mStock, mStock.getContentValues());
		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d(stock.toLogString() + Symbol.TAB + stock.getPriceNetString(Symbol.TAB) + Symbol.TAB + stock.getTrendStringBySetting() + Symbol.TAB + StopWatch.getIntervalString());
	}

	private void analyzeMACD(String period) {
		if (mStockDataList == null || mStockDataList.size() < StockTrend.VERTEX_SIZE) {
			return;
		}

		try {
			MACDAnalyzer.calculateMACD(period, mStockDataList);
			ArrayList<Double> average5List = MACDAnalyzer.getEMAAverage5List();
			ArrayList<Double> average10List = MACDAnalyzer.getEMAAverage10List();
			ArrayList<Double> difList = MACDAnalyzer.getDIFList();
			ArrayList<Double> deaList = MACDAnalyzer.getDEAList();
			ArrayList<Double> histogramList = MACDAnalyzer.getHistogramList();

			int size = mStockDataList.size();
			if (average5List.size() != size || average10List.size() != size || difList.size() != size || deaList.size() != size || histogramList.size() != size) {
				return;
			}

			int level = mStock.getLevel(period);
			int vertexTopIndex = 0;
			int vertexBottomIndex = 0;
			double vertexTopValue = 0;
			double vertexBottomValue = 0;
			double value;
			int startIndex = 0;
			int endIndex = 0;
			double startValue;
			double endValue;
			boolean foundVertex = false;
			StockData vertexData = null;

			mPulseList.clear();

			for (int i = 0; i < size; i++) {
				StockData stockData = mStockDataList.get(i);
				if (i == 0) {
					value = (stockData.getCandle().getHigh() + stockData.getCandle().getLow()) / 2.0;
					vertexTopValue = value;
					vertexBottomValue = value;
				}

				if (stockData.vertexOf(StockTrend.getVertexTOP(level))) {
					foundVertex = true;
					vertexData = stockData;
					vertexTopIndex = i;
					vertexTopValue = stockData.getCandle().getHigh();
				} else if (stockData.vertexOf(StockTrend.getVertexBottom(level))) {
					foundVertex = true;
					vertexData = stockData;
					vertexBottomIndex = i;
					vertexBottomValue = stockData.getCandle().getLow();
				}

				if (i == size -1) {
					if (vertexData == null) {
						continue;
					}
					if (vertexData.vertexOf(StockTrend.getVertexTOP(level))) {
						foundVertex = true;
						vertexBottomIndex = i;
						vertexBottomValue = stockData.getCandle().getLow();
					} else if (vertexData.vertexOf(StockTrend.getVertexBottom(level))) {
						foundVertex = true;
						vertexTopIndex = i;
						vertexTopValue = stockData.getCandle().getHigh();
					}
				}

				if (foundVertex && vertexTopIndex != vertexBottomIndex) {
					if (vertexBottomIndex < vertexTopIndex) {
						startIndex = vertexBottomIndex;
						startValue = vertexBottomValue;
						endIndex = vertexTopIndex;
						endValue = vertexTopValue;
					} else {
						endIndex = vertexBottomIndex;
						endValue = vertexBottomValue;
						startIndex = vertexTopIndex;
						startValue = vertexTopValue;
					}

					for (int j = startIndex; j < endIndex; j++) {
						double temp = (double) (j - startIndex) / (double) (endIndex - startIndex);
						value = startValue + (endValue - startValue) * temp;
						mPulseList.add(value);
					}

					if (i == size -1) {
						mPulseList.add(endValue);
					}

					foundVertex = false;
				}
			}

			FourierAnalyzer.analyze(period, mPulseList);
			ArrayList<Double> radarList = FourierAnalyzer.getRadarList();
			mStock.setRadar(period, FourierAnalyzer.getRadar());

			for (int i = 0; i < size; i++) {
				StockData stockData = mStockDataList.get(i);
				Macd macd = stockData.getMacd();
				if (macd != null) {
					macd.set(
							average5List.get(i),
							average10List.get(i),
							difList.get(i),
							deaList.get(i),
							histogramList.get(i),
							radarList.get(i)
					);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void analyzeStockData(String period) {
		mTrendAnalyzer.setup(mStock, period, mStockDataList);

		mTrendAnalyzer.analyzeVertex(StockTrend.LEVEL_DRAW);
		mTrendAnalyzer.vertexListToDataList(mStock.getVertexList(period, StockTrend.LEVEL_DRAW), mStock.getStockDataList(period, StockTrend.LEVEL_DRAW));

		for (int i = StockTrend.LEVEL_STROKE; i < StockTrend.LEVELS.length; i++) {
			mTrendAnalyzer.analyzeLine(i);
		}

		mTrendAnalyzer.analyzeAdaptive(period);
	}

	void loadStockDataList(String period) {
		mStockDataList = mStock.getStockDataList(period, StockTrend.LEVEL_NONE);
		mStockDatabaseManager.loadStockDataList(mStock, period, mStockDataList);

		boolean foundRepeated = false;
		if (period.equals(Period.MONTH) || period.equals(Period.WEEK)) {
			if (mStockDataList == null || mStockDataList.isEmpty()) {
				return;
			}

			for (int i = mStockDataList.size() - 1; i > 0; i--) {
				StockData current = mStockDataList.get(i);
				StockData prev = mStockDataList.get(i - 1);
				if (TextUtils.equals(prev.getMonth(), current.getMonth()) || TextUtils.equals(prev.getWeek(), current.getWeek())) {
					if (!TextUtils.equals(prev.getDay(), current.getDay())) {
						mStockDatabaseManager.deleteStockData(prev.getId());
						foundRepeated = true;
					}
				} else {
					break;
				}
			}
		}

		if (foundRepeated) {
			mStockDatabaseManager.loadStockDataList(mStock, period, mStockDataList);
		}
	}

	private static class SingletonHolder {
		private static final StockAnalyzer INSTANCE = new StockAnalyzer();
	}
}
