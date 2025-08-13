package com.android.orion.analyzer;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.android.orion.application.MainApplication;
import com.android.orion.data.Macd;
import com.android.orion.data.Period;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockRZRQ;
import com.android.orion.database.StockTrend;
import com.android.orion.manager.StockDatabaseManager;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Symbol;
import com.android.orion.utility.Logger;
import com.android.orion.utility.StopWatch;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.List;


public class StockAnalyzer {
	Stock mStock;
	ArrayList<StockData> mStockDataList;
	ArrayMap<String, StockRZRQ> mStockRZRQMap = new ArrayMap<>();
	StringBuffer mContentTitle = new StringBuffer();
	StringBuffer mContentText = new StringBuffer();

	Context mContext = MainApplication.getContext();
	StockDatabaseManager mStockDatabaseManager = StockDatabaseManager.getInstance();
	FinancialAnalyzer mFinancialAnalyzer = FinancialAnalyzer.getInstance();
	GridAnalyzer mGridAnalyzer = GridAnalyzer.getInstance();
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
			mStockDataList = mStock.getStockDataList(period);
			mStockDatabaseManager.loadStockDataList(mStock, period, mStockDataList);
			if (Period.getPeriodIndex(period) <= Period.getPeriodIndex(Period.MONTH)) {
				mFinancialAnalyzer.setNetProfileInYear(mStock, mStockDataList);
			}
			analyzeMacd(period);
			analyzeStockData(period);
			mStockDatabaseManager.updateStockData(mStock, period, mStockDataList);
			mStock.setModified(Utility.getCurrentDateTimeString());
			mStockDatabaseManager.updateStock(mStock, mStock.getContentValues());
		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d(mStock.getName() + " " + period + " "
				+ StopWatch.getInterval() + "s");
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
			mStock.setAdaptiveDate("");
			for (String period : Period.PERIODS) {
				if (Setting.getPeriod(period)) {
					analyze(period);
				}
			}
			mTrendAnalyzer.analyzeAdaptive();
			mFinancialAnalyzer.analyzeFinancial(mStock);
			mFinancialAnalyzer.setupFinancial(mStock);
			mFinancialAnalyzer.setupStockBonus(mStock);
			mGridAnalyzer.analyze(mStock);
			mStock.setModified(Utility.getCurrentDateTimeString());
			mStockDatabaseManager.updateStock(mStock, mStock.getContentValues());
		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d(stock.getName() + " " + StopWatch.getInterval() + "s");
	}

	private void analyzeMacd(String period) {
		if (mStockDataList == null || mStockDataList.size() < StockTrend.VERTEX_SIZE) {
			return;
		}

		try {
			MacdAnalyzer.calculate(period, mStockDataList);
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<Double> average5List = MacdAnalyzer.getEMAAverage5List();
		List<Double> average10List = MacdAnalyzer.getEMAAverage10List();
		List<Double> difList = MacdAnalyzer.getDIFList();
		List<Double> deaList = MacdAnalyzer.getDEAList();
		List<Double> histogramList = MacdAnalyzer.getHistogramList();
		List<Double> velocityList = MacdAnalyzer.getVelocityList();

		int size = mStockDataList.size();
		if (average5List.size() != size || average10List.size() != size || difList.size() != size || deaList.size() != size || histogramList.size() != size || velocityList.size() != size) {
			return;
		}

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
						velocityList.get(i)
				);
			}
		}
	}

	private void analyzeStockData(String period) {
		mTrendAnalyzer.setup(mStock, period, mStockDataList);

		mTrendAnalyzer.analyzeVertex(StockTrend.LEVEL_DRAW);
		mTrendAnalyzer.vertexListToDataList(mStock.getVertexList(period, StockTrend.LEVEL_DRAW), mStock.getDataList(period, StockTrend.LEVEL_DRAW));

		for (int i = StockTrend.LEVEL_STROKE; i < StockTrend.LEVELS.length; i++) {
			mTrendAnalyzer.analyzeLine(i);
		}

		mTrendAnalyzer.analyzeAdaptive(period);

		analyzeAction(period);
	}

	private void analyzeAction(String period) {
		if (mStock == null || mStockDataList == null || mStockDataList.isEmpty()) {
			return;
		}

		mStockDatabaseManager.getStockRZRQMap(mStock, mStockRZRQMap, DatabaseContract.ORDER_DATE_DESC);
		StockRZRQ prevStockRZRQ = null;
		for (StockData stockData : mStockDataList) {
			StockRZRQ stockRZRQ = mStockRZRQMap.get(stockData.getDate());
			if (stockRZRQ != null) {
				stockData.setRZValue(stockRZRQ.getRZValue());
				stockData.setRQValue(stockRZRQ.getRQValue());
				prevStockRZRQ = stockRZRQ;
			} else {
				if (prevStockRZRQ != null) {
					stockData.setRZValue(prevStockRZRQ.getRZValue());
					stockData.setRQValue(prevStockRZRQ.getRQValue());
				}
			}
		}

		StringBuilder actionBuilder = new StringBuilder();
		appendActionIfPresent(actionBuilder, getDirectionAction(period));
		appendActionIfPresent(actionBuilder, getVelocityAction());
		mStock.setAction(period, actionBuilder.toString());
	}

	private void appendActionIfPresent(StringBuilder builder, String action) {
		if (action != null && !action.isEmpty()) {
			builder.append(action).append(Symbol.NEW_LINE);
		}
	}

	String getDirectionAction(String period) {
		StringBuilder builder = new StringBuilder();
		StockData stockData = StockData.getLast(mStock.getVertexList(period, mStock.getLevel(period)), 1);
		if (stockData != null) {
			if (stockData.vertexOf(StockTrend.VERTEX_BOTTOM)) {
				if (mStock.getPrice() < stockData.getCandle().getLow()) {
					builder.append(Symbol.MINUS);
				} else {
					builder.append(Symbol.ADD);
				}
			} else if (stockData.vertexOf(StockTrend.VERTEX_TOP)) {
				if (mStock.getPrice() > stockData.getCandle().getHigh()) {
					builder.append(Symbol.ADD);
				} else {
					builder.append(Symbol.MINUS);
				}
			}
		}
		return builder.toString();
	}

	String getVelocityAction() {
		StringBuilder builder = new StringBuilder();
		StockData stockData = mStockDataList.get(mStockDataList.size() - 1);
		if (stockData != null) {
			Macd macd = stockData.getMacd();
			if (macd != null) {
				double velocity = macd.getVelocity();
				if (velocity > 0) {
					builder.append(Symbol.ADD);
				} else if (velocity < 0) {
					builder.append(Symbol.MINUS);
				}
			}
		}
		return builder.toString();
	}

	String getLevelAction(String period) {
		StringBuilder builder = new StringBuilder();

		double mean = 0;
		double sd = 0;
		String adaptive = "";
		for (int i = StockTrend.LEVEL_DRAW; i < StockTrend.LEVELS.length; i++) {
			ArrayList<StockData> dataList = mStock.getDataList(period, i);
			mean = Utility.Round2(calculateMean(dataList));
			sd = Utility.Round2(calculateStandardDeviation(dataList));
			if (mStock.getLevel(period) == i) {
				adaptive = Symbol.ASTERISK;
			} else {
				adaptive = "";
			}
			Log.d(mStock.getName() + " " + period + " " + Symbol.L + i + " mean=" + mean + " sd=" + sd + " size=" + dataList.size() + " " + adaptive);
		}

		builder.append(Symbol.L + mStock.getLevel(period));

		return builder.toString();
	}

	public double calculateMean(ArrayList<StockData> dataList) {
		double sum = 0.0;
		if (dataList == null || dataList.isEmpty()) {
			return sum;
		}

		for (StockData data : dataList) {
			sum += Math.abs(data.getNet());
		}
		return sum / dataList.size();
	}

	public double calculateStandardDeviation(ArrayList<StockData> dataList) {
		double sumOfSquaredDifferences = 0.0;
		if (dataList == null || dataList.isEmpty()) {
			return sumOfSquaredDifferences;
		}

		double mean = calculateMean(dataList);
		for (StockData data : dataList) {
			sumOfSquaredDifferences += Math.pow(Math.abs(data.getNet()) - mean, 2);
		}

		double variance = sumOfSquaredDifferences / dataList.size();
		return Math.sqrt(variance);
	}

	private static class SingletonHolder {
		private static final StockAnalyzer INSTANCE = new StockAnalyzer();
	}
}
