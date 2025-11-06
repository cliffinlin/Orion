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
import com.android.orion.setting.Constant;
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

	private void analyzeStockData(String period) {
		mTrendAnalyzer.setup(mStock, period, mStockDataList);

		mTrendAnalyzer.analyzeVertex(StockTrend.LEVEL_DRAW);
		mTrendAnalyzer.vertexListToDataList(mStock.getVertexList(period, StockTrend.LEVEL_DRAW), mStock.getStockDataList(period, StockTrend.LEVEL_DRAW));

		for (int i = StockTrend.LEVEL_STROKE; i < StockTrend.LEVELS.length; i++) {
			mTrendAnalyzer.analyzeLine(i);
		}

		mTrendAnalyzer.analyzeAdaptive(period);
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

			setupPulseList(period);
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
/*
	void setupPulseList(String period) {
		int level = mStock.getLevel(period);
		int vertexTopIndex = 0;
		int vertexBottomIndex = 0;
		double vertexTopValue = 0;
		double vertexBottomValue = 0;
		int startIndex;
		int endIndex;
		double startValue;
		double endValue;
		boolean foundVertex = false;
		StockData vertexData = null;

		mPulseList.clear();
		int size = mStockDataList.size();

		for (int i = 0; i < size; i++) {
			StockData stockData = mStockDataList.get(i);
			if (stockData.vertexOf(StockTrend.getVertexTOP(level))) {
				foundVertex = true;
				vertexData = stockData;
				vertexTopIndex = i;
				vertexTopValue = stockData.getCandle().getHigh();//Constant.PULSE_HIGH;
			} else if (stockData.vertexOf(StockTrend.getVertexBottom(level))) {
				foundVertex = true;
				vertexData = stockData;
				vertexBottomIndex = i;
				vertexBottomValue = stockData.getCandle().getLow();//Constant.PULSE_LOW;
			}

			if (i == size -1) {
				if (vertexData == null) {
					continue;
				}
				if (vertexData.vertexOf(StockTrend.getVertexTOP(level))) {
					foundVertex = true;
					vertexBottomIndex = i;
					vertexBottomValue = stockData.getCandle().getLow();//Constant.PULSE_LOW;
				} else if (vertexData.vertexOf(StockTrend.getVertexBottom(level))) {
					foundVertex = true;
					vertexTopIndex = i;
					vertexTopValue = stockData.getCandle().getHigh();//Constant.PULSE_HIGH;
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
					if (startIndex == 0) {
//						mPulseList.add((double) Constant.PULSE_MIDDLE);
						mPulseList.add((stockData.getCandle().getHigh() + stockData.getCandle().getLow())/2);
					} else {
						mPulseList.add(startValue + (endValue - startValue) * (double) (j - startIndex) / (double) (endIndex - startIndex));
					}
				}

				if (i == size -1) {
					mPulseList.add(endValue);
				}

				foundVertex = false;
			}
		}
	}
	*/
	void setupPulseList(String period) {
		int level = mStock.getLevel(period);
		int vertexTopIndex = 0;
		int vertexBottomIndex = 0;
		double vertexTopValue = 0;
		double vertexBottomValue = 0;
		int startIndex;
		int endIndex;
		double startValue;
		double endValue;
		boolean foundVertex = false;
		StockData vertexData = null;

		mPulseList.clear();
		int size = mStockDataList.size();

		// 第一步：收集所有顶点的价格值，用于归一化
		List<Double> allVertexPrices = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			StockData stockData = mStockDataList.get(i);
			if (stockData.vertexOf(StockTrend.getVertexTOP(level))) {
				allVertexPrices.add(stockData.getCandle().getHigh());
			} else if (stockData.vertexOf(StockTrend.getVertexBottom(level))) {
				allVertexPrices.add(stockData.getCandle().getLow());
			}
		}

		// 计算价格范围用于归一化
		double minPrice = Double.MAX_VALUE;
		double maxPrice = Double.MIN_VALUE;
		for (Double price : allVertexPrices) {
			if (price < minPrice) minPrice = price;
			if (price > maxPrice) maxPrice = price;
		}

		// 如果只有一个顶点或者价格范围太小，使用默认范围
		if (allVertexPrices.size() <= 1 || Math.abs(maxPrice - minPrice) < 0.001) {
			minPrice = 0.0;
			maxPrice = 1.0;
		}

		double priceRange = maxPrice - minPrice;
		double midPrice = (maxPrice + minPrice) / 2;

		if (true) {
			Log.d("顶点价格归一化 - 最小值: " + minPrice + ", 最大值: " + maxPrice +
					", 中间值: " + midPrice + ", 范围: " + priceRange);
		}

		for (int i = 0; i < size; i++) {
			StockData stockData = mStockDataList.get(i);
			if (stockData.vertexOf(StockTrend.getVertexTOP(level))) {
				foundVertex = true;
				vertexData = stockData;
				vertexTopIndex = i;
				vertexTopValue = normalizePrice(stockData.getCandle().getHigh(), minPrice, maxPrice, midPrice);
			} else if (stockData.vertexOf(StockTrend.getVertexBottom(level))) {
				foundVertex = true;
				vertexData = stockData;
				vertexBottomIndex = i;
				vertexBottomValue = normalizePrice(stockData.getCandle().getLow(), minPrice, maxPrice, midPrice);
			}

			if (i == size - 1) {
				if (vertexData == null) {
					continue;
				}
				if (vertexData.vertexOf(StockTrend.getVertexTOP(level))) {
					foundVertex = true;
					vertexBottomIndex = i;
					vertexBottomValue = normalizePrice(stockData.getCandle().getLow(), minPrice, maxPrice, midPrice);
				} else if (vertexData.vertexOf(StockTrend.getVertexBottom(level))) {
					foundVertex = true;
					vertexTopIndex = i;
					vertexTopValue = normalizePrice(stockData.getCandle().getHigh(), minPrice, maxPrice, midPrice);
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
					if (startIndex == 0) {
						// 使用中间值作为起始点
						double midNormalized = normalizePrice((stockData.getCandle().getHigh() + stockData.getCandle().getLow()) / 2,
								minPrice, maxPrice, midPrice);
						mPulseList.add(midNormalized);
					} else {
						// 线性插值
						double interpolatedPrice = startValue + (endValue - startValue) * (double) (j - startIndex) / (double) (endIndex - startIndex);
						mPulseList.add(interpolatedPrice);
					}
				}

				if (i == size - 1) {
					mPulseList.add(endValue);
				}

				foundVertex = false;
			}
		}

		// 验证归一化结果
		if (!mPulseList.isEmpty()) {
			double minNorm = Double.MAX_VALUE;
			double maxNorm = Double.MIN_VALUE;
			for (Double value : mPulseList) {
				if (value < minNorm) minNorm = value;
				if (value > maxNorm) maxNorm = value;
			}
			Log.d("归一化验证 - 最小值: " + String.format("%.3f", minNorm) +
					", 最大值: " + String.format("%.3f", maxNorm) +
					", 数据点数: " + mPulseList.size());
		}
	}

	/**
	 * 将价格归一化到 [-1, 1] 范围
	 * 中间价格映射到 0，最高价格映射到 1，最低价格映射到 -1
	 */
	private double normalizePrice(double price, double minPrice, double maxPrice, double midPrice) {
		if (Math.abs(maxPrice - minPrice) < 0.001) {
			return 0.0; // 避免除零
		}

		// 线性归一化到 [-1, 1]
		double normalized = 2 * (price - minPrice) / (maxPrice - minPrice) - 1;

		// 确保在 [-1, 1] 范围内
		if (normalized < -1.0) normalized = -1.0;
		if (normalized > 1.0) normalized = 1.0;

		return normalized;
	}

	/**
	 * 替代方案：使用Z-score归一化（基于均值和标准差）
	 */
	private double normalizePriceZScore(double price, double meanPrice, double stdDevPrice) {
		if (stdDevPrice < 0.001) {
			return 0.0; // 避免除零
		}

		double zScore = (price - meanPrice) / stdDevPrice;

		// 将Z-score缩放到 [-1, 1] 范围（假设大部分数据在3个标准差内）
		double normalized = zScore / 3.0;

		if (normalized < -1.0) normalized = -1.0;
		if (normalized > 1.0) normalized = 1.0;

		return normalized;
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
