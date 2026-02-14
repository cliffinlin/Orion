package com.android.orion.analyzer;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.android.orion.application.MainApplication;
import com.android.orion.data.Macd;
import com.android.orion.data.Period;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockRadar;
import com.android.orion.database.StockTrend;
import com.android.orion.manager.StockDatabaseManager;
import com.android.orion.constant.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.android.orion.utility.StopWatch;
import com.android.orion.utility.Symbol;
import com.android.orion.utility.Utility;

import java.util.ArrayList;


public class StockAnalyzer {
	Stock mStock;
	ArrayMap<String, StockRadar> mStockRadarMap;
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
		if (mStock == null) {
			return;
		}

		StopWatch.start();

		try {
			loadStockRadarMap(period);
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
		Log.d(mStock.toLogString() + Symbol.TAB + period + Symbol.TAB + StopWatch.getIntervalString());
	}

	public void analyze(Stock stock) {
		if (stock == null) {
			return;
		}

		StopWatch.start();
		try {
			mStock = stock;
			mStockDatabaseManager.getStock(mStock);
			mFinancialAnalyzer.analyzeFinancial(mStock);
			mFinancialAnalyzer.setupFinancial(mStock);
			mFinancialAnalyzer.setupStockBonus(mStock);
			for (String period : Period.PERIODS) {
				if (Setting.getPeriod(period)) {
					analyze(period);
				}
			}
			mTrendAnalyzer.setupThumbnail(mStock);
			mTradeAnalyzer.analyzeProfit(mStock);
			mFinancialAnalyzer.analyzeFinancial(mStock);
			mFinancialAnalyzer.setupFinancial(mStock);
			mFinancialAnalyzer.setupStockBonus(mStock);
			mStock.setModified(Utility.getCurrentDateTimeString());
			mStockDatabaseManager.updateStock(mStock, mStock.getContentValues());
		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d(mStock.toLogString() + Symbol.TAB + "total" + Symbol.TAB + StopWatch.getIntervalString());
	}

	private void analyzeStockData(String period) {
		mTrendAnalyzer.setup(mStock, period, mStockDataList);

		mTrendAnalyzer.analyzeVertex(StockTrend.LEVEL_DRAW);
		mTrendAnalyzer.vertexListToDataList(mStock.getVertexList(period, StockTrend.LEVEL_DRAW), mStock.getStockDataList(period, StockTrend.LEVEL_DRAW));

		for (int i = StockTrend.LEVEL_STROKE; i < StockTrend.LEVELS.length; i++) {
			mTrendAnalyzer.analyzeLine(i);
//__TEST_CASE__
//			mTrendAnalyzer.analyzeVertexExt(i);
//			mTrendAnalyzer.vertexListToDataList(mStock.getVertexList(period, i), mStock.getStockDataList(period, i));
//__TEST_CASE__
		}
	}

	private void analyzeMACD(String period) {
		if (mStockDataList == null || mStockDataList.size() < StockTrend.VERTEX_SIZE) {
			return;
		}

		try {
			MACDAnalyzer.calculateMACD(period, mStockDataList);
			ArrayList<Double> difList = MACDAnalyzer.getDIFList();
			ArrayList<Double> deaList = MACDAnalyzer.getDEAList();
			ArrayList<Double> histogramList = MACDAnalyzer.getHistogramList();

			int size = mStockDataList.size();
			if (difList.size() != size || deaList.size() != size || histogramList.size() != size) {
				return;
			}

			setupPulseList(mStock.getTarget(period));
			FourierAnalyzer.analyze(period, mPulseList);
			ArrayList<Double> targetList = FourierAnalyzer.getRadarList();
			mStock.setTargetRadar(period, FourierAnalyzer.getRadar());

			if (targetList.size() != size) {
				Log.d("return, size=" + size + " targetList.size()=" + targetList.size());
				return;
			}

			for (int i = 0; i < size; i++) {
				StockData stockData = mStockDataList.get(i);
				if (stockData == null) {
					continue;
				}
				Macd macd = stockData.getMacd();
				if (macd == null) {
					continue;
				}
				macd.set(difList.get(i), deaList.get(i), histogramList.get(i), targetList.get(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void setupPulseList(int level) {
		int startIndex = 0;
		int endIndex = 0;
		double startValue;
		double endValue;
		ArrayList<StockData> vertexDataList = new ArrayList<>();
		ArrayList<Double> vertexValueList = new ArrayList<>();

		mPulseList.clear();
		setupVertexList(level, vertexDataList, vertexValueList);
		if (vertexDataList.size() < StockTrend.VERTEX_SIZE || vertexDataList.size() != vertexValueList.size()) {
			return;
		}

		for (int i = 1; i < vertexValueList.size(); i++) {
			startIndex = vertexDataList.get(i - 1).getIndex();
			startValue = vertexValueList.get(i - 1);
			endIndex = vertexDataList.get(i).getIndex();
			endValue = vertexValueList.get(i);
			for (int j = startIndex; j < endIndex; j++) {
				double interpolated = Utility.interpolate(startIndex, startValue, endIndex, endValue, j);
				mPulseList.add(interpolated);
			}
			if (i == vertexValueList.size() - 1) {
				mPulseList.add(vertexValueList.get(i));
			}
		}

		FourierAnalyzer.setComponentCount(vertexValueList.size());
	}

	void setupVertexList(int level, ArrayList<StockData> vertexDataList, ArrayList<Double> vertexValueList) {
		StockData extendData = null;
		StockData vertexData = null;
		int size = mStockDataList.size();
		boolean inserted = false;

		if (vertexDataList == null) {
			vertexDataList = new ArrayList<>();
		}

		if (vertexValueList == null) {
			vertexValueList = new ArrayList<>();
		}

		for (int i = 0; i < size; i++) {
			StockData stockData = mStockDataList.get(i);
			if (stockData.vertexOf(StockTrend.getVertexTOP(level)) || stockData.vertexOf(StockTrend.getVertexBottom(level))) {
				vertexData = new StockData(stockData);
				vertexDataList.add(vertexData);
				vertexValueList.add(getVertexValue(level, vertexData));

				if (!inserted) {
					inserted = true;
					extendData = new StockData(mStockDataList.get(0));
					extendVertexData(level, vertexData, extendData);
					vertexDataList.add(0, extendData);
					vertexValueList.add(0, getVertexValue(level, extendData));
				}
			}

			if (i == size - 1) {
				if (vertexData != null) {
					extendData = new StockData(stockData);
					extendVertexData(level, vertexData, extendData);
					vertexDataList.add(extendData);
					vertexValueList.add(getVertexValue(level, extendData));
				}
			}
		}
	}

	void extendVertexData(int level, StockData vertexData, StockData extendData) {
		if (vertexData == null || extendData == null) {
			return;
		}
		if (vertexData.vertexOf(StockTrend.getVertexTOP(level))) {
			extendData.addVertex(StockTrend.getVertexBottom(level));
		} else if (vertexData.vertexOf(StockTrend.getVertexBottom(level))) {
			extendData.addVertex(StockTrend.getVertexTOP(level));
		}
	}

	double getVertexValue(int level, StockData vertexData) {
		double result = 0;
		if (vertexData == null) {
			return result;
		}
		if (vertexData.vertexOf(StockTrend.getVertexTOP(level))) {
			result  = Constant.PULSE_HIGH;
		} else if (vertexData.vertexOf(StockTrend.getVertexBottom(level))) {
			result  = Constant.PULSE_LOW;
		}
		return result;
	}

	void loadStockRadarMap(String period) {
		mStockRadarMap = mStock.getStockRadarMap(period);
		mStockDatabaseManager.loadStockRadarMap(mStock, period, mStockRadarMap);
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
