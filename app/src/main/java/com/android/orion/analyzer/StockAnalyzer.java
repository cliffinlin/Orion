package com.android.orion.analyzer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.android.orion.application.MainApplication;
import com.android.orion.chart.CurveThumbnail;
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
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Arrays;
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
			for (String period : Period.PERIODS) {
				if (Setting.getPeriod(period)) {
					setupThumbnail(period);
				}
			}
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
	}

	public void setupThumbnail(String  period) {
		int vertexTop = StockTrend.VERTEX_NONE;
		int vertexBottom = StockTrend.VERTEX_NONE;
		switch (mStock.getLevel(period)) {
			case StockTrend.LEVEL_DRAW:
				vertexTop = StockTrend.VERTEX_TOP;
				vertexBottom = StockTrend.VERTEX_BOTTOM;
				break;
			case StockTrend.LEVEL_STROKE:
				vertexTop = StockTrend.VERTEX_TOP_STROKE;
				vertexBottom = StockTrend.VERTEX_BOTTOM_STROKE;
				break;
			case StockTrend.LEVEL_SEGMENT:
				vertexTop = StockTrend.VERTEX_TOP_SEGMENT;
				vertexBottom = StockTrend.VERTEX_BOTTOM_SEGMENT;
				break;
			case StockTrend.LEVEL_LINE:
				vertexTop = StockTrend.VERTEX_TOP_LINE;
				vertexBottom = StockTrend.VERTEX_BOTTOM_LINE;
				break;
			case StockTrend.LEVEL_OUT_LINE:
				vertexTop = StockTrend.VERTEX_TOP_OUTLINE;
				vertexBottom = StockTrend.VERTEX_BOTTOM_OUTLINE;
				break;
			case StockTrend.LEVEL_SUPER_LINE:
				vertexTop = StockTrend.VERTEX_TOP_SUPERLINE;
				vertexBottom = StockTrend.VERTEX_BOTTOM_SUPERLINE;
				break;
			case StockTrend.LEVEL_TREND_LINE:
				vertexTop = StockTrend.VERTEX_TOP_TREND_LINE;
				vertexBottom = StockTrend.VERTEX_BOTTOM_TREND_LINE;
				break;
			default:
				break;
		}

		mStockDataList = mStock.getStockDataList(period);
		if (mStockDataList.isEmpty()) {
			return;
		}

		List<Float> xValues = new ArrayList<>();
		List<Float> yValues = new ArrayList<>();
		for (int i = 0; i < mStockDataList.size(); i++) {
			StockData stockData = mStockDataList.get(i);
			if (stockData.vertexOf(vertexTop)) {
				xValues.add((float) i);
				yValues.add((float) stockData.getCandle().getHigh());
			} else if (stockData.vertexOf(vertexBottom)) {
				xValues.add((float) i);
				yValues.add((float) stockData.getCandle().getLow());
			}
		}

		List<CurveThumbnail.LineConfig> lines = Arrays.asList(
				new CurveThumbnail.LineConfig(xValues, yValues,	StockTrend.COLORS[mStock.getLevel(period)], 4f));
		CurveThumbnail.CrossMarkerConfig markerConfig =
				new CurveThumbnail.CrossMarkerConfig(mStockDataList.size() - 1, (float) mStock.getPrice(), Color.RED,4f, 20f);
		mStock.setThumbnail(period, Utility.thumbnailToBytes(new CurveThumbnail(160,	Color.TRANSPARENT, lines, markerConfig)));
	}

	private static class SingletonHolder {
		private static final StockAnalyzer INSTANCE = new StockAnalyzer();
	}
}
