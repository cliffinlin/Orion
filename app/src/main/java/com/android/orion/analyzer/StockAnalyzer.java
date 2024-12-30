package com.android.orion.analyzer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.orion.R;
import com.android.orion.activity.StockFavoriteListActivity;
import com.android.orion.application.MainApplication;
import com.android.orion.config.Config;
import com.android.orion.data.Macd;
import com.android.orion.data.Period;
import com.android.orion.data.Trend;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockFinancial;
import com.android.orion.database.TotalShare;
import com.android.orion.manager.DatabaseManager;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Market;
import com.android.orion.utility.RecordFile;
import com.android.orion.utility.StopWatch;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class StockAnalyzer {
	static ArrayList<StockFinancial> mStockFinancialList = new ArrayList<>();
	static ArrayList<TotalShare> mTotalShareList = new ArrayList<>();
	static ArrayList<ShareBonus> mShareBonusList = new ArrayList<>();
	static ArrayList<StockData> mStockDataList = new ArrayList<>();
	static StringBuffer mContentTitle = new StringBuffer();
	static StringBuffer mContentText = new StringBuffer();
	Context mContext;
	NotificationManager mNotificationManager;
	DatabaseManager mDatabaseManager;
	Logger Log = Logger.getLogger();
	static Set<String> mNotifyActions = new HashSet<>(Arrays.asList(
			StockData.MARK_BUY, StockData.MARK_BUY1, StockData.MARK_BUY2,
			StockData.MARK_SELL, StockData.MARK_SELL1, StockData.MARK_SELL2));

	private StockAnalyzer() {
		mContext = MainApplication.getContext();

		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mDatabaseManager = DatabaseManager.getInstance();
	}

	private static class SingletonHolder {
		private static final StockAnalyzer INSTANCE = new StockAnalyzer();
	}

	public static StockAnalyzer getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void analyze(Stock stock, String period) {
		StopWatch.start();

		if (stock == null) {
			return;
		}

		ArrayList<StockData> stockDataList = stock.getArrayList(period, Period.TYPE_STOCK_DATA);
		ArrayList<StockData> drawVertexList = stock.getArrayList(period, Period.TYPE_DRAW_VERTEX);
		ArrayList<StockData> drawDataList = stock.getArrayList(period, Period.TYPE_DRAW_DATA);
		ArrayList<StockData> strokeVertexList = stock.getArrayList(period, Period.TYPE_STROKE_VERTEX);
		ArrayList<StockData> strokeDataList = stock.getArrayList(period, Period.TYPE_STROKE_DATA);
		ArrayList<StockData> segmentVertexList = stock.getArrayList(period, Period.TYPE_SEGMENT_VERTEX);
		ArrayList<StockData> segmentDataList = stock.getArrayList(period, Period.TYPE_SEGMENT_DATA);
		ArrayList<StockData> lineVertexList = stock.getArrayList(period, Period.TYPE_LINE_VERTEX);
		ArrayList<StockData> lineDataList = stock.getArrayList(period, Period.TYPE_LINE_DATA);
		ArrayList<StockData> outlineVertexList = stock.getArrayList(period, Period.TYPE_OUTLINE_VERTEX);
		ArrayList<StockData> outlineDataList = stock.getArrayList(period, Period.TYPE_OUTLINE_DATA);

		try {
			mDatabaseManager.loadStockDataList(stock, period, stockDataList);
			analyzeMacd(period, stockDataList);
			analyzeStockData(stock, period, stockDataList,
					drawVertexList, drawDataList,
					strokeVertexList, strokeDataList,
					segmentVertexList, segmentDataList,
					lineVertexList, lineDataList,
					outlineVertexList, outlineDataList);
			mDatabaseManager.updateStockData(stock, period, stockDataList);
			stock.setModified(Utility.getCurrentDateTimeString());
			mDatabaseManager.updateStock(stock, stock.getContentValues());
		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d(stock.getName() + " " + period + " "
				+ StopWatch.getInterval() + "s");
	}

	public void analyze(Stock stock) {
		StopWatch.start();

		if (stock == null) {
			return;
		}

		try {
			analyzeStockFinancial(stock);
			setupStockFinancial(stock);
			setupStockShareBonus(stock);
			stock.setModified(Utility.getCurrentDateTimeString());
			mDatabaseManager.updateStock(stock, stock.getContentValues());
			updateNotification(stock);
		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d(stock.getName() + " " + StopWatch.getInterval() + "s");
	}

	private void analyzeStockFinancial(Stock stock) {
		if (stock == null) {
			return;
		}

		String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";

		if (TextUtils.equals(stock.getClasses(), Stock.CLASS_INDEX)) {
			return;
		}

		mDatabaseManager.getStockFinancialList(stock, mStockFinancialList,
				sortOrder);
		mDatabaseManager.getTotalShareList(stock, mTotalShareList,
				sortOrder);
		mDatabaseManager.getShareBonusList(stock, mShareBonusList,
				sortOrder);
		mDatabaseManager.getStockDataList(stock, DatabaseContract.COLUMN_MONTH,
				mStockDataList, sortOrder);

		setupTotalShare(mStockFinancialList, mTotalShareList);
		setupNetProfitPerShareInYear(mStockFinancialList);
		setupNetProfitPerShare(mStockFinancialList);
		setupRate(mStockFinancialList);
		setupRoe(mStockFinancialList);
		setupRoi(mStockDataList, mStockFinancialList);

		mDatabaseManager.updateStockFinancial(stock, mStockFinancialList);
		mDatabaseManager.updateStockData(stock, DatabaseContract.COLUMN_MONTH, mStockDataList);
	}

	private void setupTotalShare(ArrayList<StockFinancial> stockFinancialList,
	                             ArrayList<TotalShare> totalShareList) {
		if (stockFinancialList == null || totalShareList == null) {
			return;
		}

		int j = 0;
		for (StockFinancial stockFinancial : stockFinancialList) {
			while (j < totalShareList.size()) {
				TotalShare totalShare = totalShareList.get(j);
				if (Utility.getCalendar(stockFinancial.getDate(),
						Utility.CALENDAR_DATE_FORMAT).after(
						Utility.getCalendar(totalShare.getDate(),
								Utility.CALENDAR_DATE_FORMAT))) {
					stockFinancial.setTotalShare(totalShare.getTotalShare());
					break;
				} else {
					j++;
				}
			}
		}
	}

	private void setupNetProfitPerShare(ArrayList<StockFinancial> stockFinancialList) {
		if (stockFinancialList == null) {
			return;
		}

		for (StockFinancial stockFinancial : stockFinancialList) {
			stockFinancial.setupNetProfitMargin();
			stockFinancial.setupNetProfitPerShare();
		}
	}

	private void setupNetProfitPerShareInYear(ArrayList<StockFinancial> stockFinancialList) {
		double mainBusinessIncome = 0;
		double mainBusinessIncomeInYear = 0;
		double netProfit = 0;
		double netProfitInYear = 0;
		double netProfitPerShareInYear = 0;
		double netProfitPerShare = 0;

		if (stockFinancialList == null) {
			return;
		}

		if (stockFinancialList.size() < Constant.SEASONS_IN_A_YEAR + 1) {
			return;
		}

		for (int i = 0; i < stockFinancialList.size()
				- Constant.SEASONS_IN_A_YEAR; i++) {
			mainBusinessIncomeInYear = 0;
			netProfitInYear = 0;
			netProfitPerShareInYear = 0;
			for (int j = 0; j < Constant.SEASONS_IN_A_YEAR; j++) {
				StockFinancial current = stockFinancialList.get(i + j);
				StockFinancial prev = stockFinancialList.get(i + j + 1);

				if (current == null || prev == null) {
					continue;
				}

				if (current.getTotalShare() == 0) {
					continue;
				}

				if (current.getDate().contains("03-31")) {
					mainBusinessIncome = current.getMainBusinessIncome();
					netProfit = current.getNetProfit();
					netProfitPerShare = current.getNetProfit()
							/ current.getTotalShare();
				} else {
					mainBusinessIncome = current.getMainBusinessIncome() - prev.getMainBusinessIncome();
					netProfit = current.getNetProfit() - prev.getNetProfit();
					netProfitPerShare = (current.getNetProfit() - prev
							.getNetProfit()) / current.getTotalShare();
				}

				mainBusinessIncomeInYear += mainBusinessIncome;
				netProfitInYear += netProfit;
				netProfitPerShareInYear += netProfitPerShare;
			}

			StockFinancial stockFinancial = stockFinancialList.get(i);
			stockFinancial.setMainBusinessIncomeInYear(mainBusinessIncomeInYear);
			stockFinancial.setNetProfitInYear(netProfitInYear);
			stockFinancial.setNetProfitPerShareInYear(netProfitPerShareInYear);
		}
	}

	private void setupRate(ArrayList<StockFinancial> stockFinancialList) {
		double rate = 0;

		if (stockFinancialList == null) {
			return;
		}

		if (stockFinancialList.size() < Constant.SEASONS_IN_A_YEAR + 1) {
			return;
		}

		for (int i = 0; i < stockFinancialList.size()
				- Constant.SEASONS_IN_A_YEAR; i++) {
			StockFinancial stockFinancial = stockFinancialList.get(i);
			StockFinancial prev = stockFinancialList.get(i
					+ Constant.SEASONS_IN_A_YEAR);

			if (prev == null || prev.getNetProfitPerShareInYear() == 0) {
				continue;
			}

			rate = Utility.Round(stockFinancial.getNetProfitPerShareInYear()
					/ prev.getNetProfitPerShareInYear());

			stockFinancial.setRate(rate);
		}
	}

	private void setupRoe(ArrayList<StockFinancial> stockFinancialList) {
		double roe = 0;

		if (stockFinancialList == null) {
			return;
		}

		if (stockFinancialList.size() < Constant.SEASONS_IN_A_YEAR + 1) {
			return;
		}

		for (int i = 0; i < stockFinancialList.size()
				- Constant.SEASONS_IN_A_YEAR; i++) {
			StockFinancial stockFinancial = stockFinancialList.get(i);
			StockFinancial prev = stockFinancialList.get(i
					+ Constant.SEASONS_IN_A_YEAR);

			if (prev == null || prev.getBookValuePerShare() == 0) {
				continue;
			}

			roe = Utility.Round(
					100.0 * stockFinancial.getNetProfitPerShareInYear()
							/ prev.getBookValuePerShare());
			if (roe < 0) {
				roe = 0;
			}

			stockFinancial.setRoe(roe);
		}
	}

	private void setupRoi(ArrayList<StockData> stockDataList,
	                      ArrayList<StockFinancial> stockFinancialList) {
		double price = 0;
		double pe = 0;
		double pb = 0;
		double roi = 0;

		if (stockDataList == null || stockFinancialList == null) {
			return;
		}

		int j = 0;
		for (StockData stockData : stockDataList) {
			price = stockData.getCandlestick().getClose();
			if (price == 0) {
				continue;
			}

			while (j < stockFinancialList.size()) {
				StockFinancial stockFinancial = stockFinancialList.get(j);
				if (Utility.getCalendar(stockData.getDate(),
						Utility.CALENDAR_DATE_FORMAT).after(
						Utility.getCalendar(stockFinancial.getDate(),
								Utility.CALENDAR_DATE_FORMAT))) {
					pe = Utility.Round(
							100.0 * stockFinancial.getNetProfitPerShareInYear()
									/ price);

					if (stockFinancial.getBookValuePerShare() != 0) {
						pb = Utility.Round(
								price / stockFinancial.getBookValuePerShare());
					}

					//TODO
					roi = Utility.Round(stockFinancial.getRoe() * pe
							* Stock.ROI_COEFFICIENT);
					if (roi < 0) {
						roi = 0;
					}
					break;
				} else {
					j++;
				}
			}
		}
	}

	private void setupStockFinancial(Stock stock) {
		if (stock == null) {
			return;
		}

		String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";
		StockFinancial stockFinancial = new StockFinancial();

		if (TextUtils.equals(stock.getClasses(), Stock.CLASS_INDEX)) {
			return;
		}

		stockFinancial.setStockId(stock.getId());

		mDatabaseManager.getStockFinancial(stock, stockFinancial);
		mDatabaseManager.getStockFinancialList(stock, mStockFinancialList,
				sortOrder);
		mDatabaseManager.updateStockDeal(stock);

		stock.setBookValuePerShare(stockFinancial.getBookValuePerShare());
		stock.setTotalAssets(stockFinancial.getTotalAssets());
		stock.setTotalLongTermLiabilities(stockFinancial
				.getTotalLongTermLiabilities());
		stock.setMainBusinessIncome(stockFinancial.getMainBusinessIncome());
		stock.setNetProfit(stockFinancial.getNetProfit());
		stock.setCashFlowPerShare(stockFinancial.getCashFlowPerShare());

		stock.setupMarketValue();
		stock.setupNetProfitPerShare();
		stock.setupNetProfitPerShareInYear(mStockFinancialList);
		stock.setupNetProfitMargin();
		stock.setupRate(mStockFinancialList);
		stock.setupDebtToNetAssetsRatio(mStockFinancialList);
		stock.setupRoe(mStockFinancialList);
		stock.setupPe();
		stock.setupPb();
		stock.setupRoi();
	}

	private void setupStockShareBonus(Stock stock) {
		if (stock == null) {
			return;
		}

		double totalDivident = 0;

		String yearString = "";
		String prevYearString = "";
		String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";

		if (TextUtils.equals(stock.getClasses(), Stock.CLASS_INDEX)) {
			return;
		}

		mDatabaseManager.getShareBonusList(stock, mShareBonusList,
				sortOrder);

		int i = 0;
		for (ShareBonus shareBonus : mShareBonusList) {
			String dateString = shareBonus.getDate();
			if (!TextUtils.isEmpty(dateString)) {
				String[] strings = dateString.split("-");
				if (strings != null && strings.length > 0) {
					yearString = strings[0];
				}

				if (!TextUtils.isEmpty(prevYearString)) {
					if (!prevYearString.equals(yearString)) {
						break;
					}
				}
			}

			totalDivident += shareBonus.getDividend();

			if (i == 0) {
				stock.setRDate(shareBonus.getRDate());
			}
			stock.setDividend(Utility.Round(totalDivident));
			stock.setupBonus();
			stock.setupYield();
			stock.setupDividendRatio();

			prevYearString = yearString;
			i++;
		}
	}

	private void analyzeMacd(String period, ArrayList<StockData> stockDataList) {
		if (stockDataList == null || stockDataList.size() < Trend.VERTEX_SIZE) {
			return;
		}

		try {
			MacdAnalyzer.calculate(period, stockDataList);
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<Double> average5List = MacdAnalyzer.getEMAAverage5List();
		List<Double> average10List = MacdAnalyzer.getEMAAverage10List();
		List<Double> difList = MacdAnalyzer.getDIFList();
		List<Double> deaList = MacdAnalyzer.getDEAList();
		List<Double> histogramList = MacdAnalyzer.getHistogramList();
		List<Double> velocityList = MacdAnalyzer.getVelocityList();

		int size = stockDataList.size();
		if (average5List.size() != size || average10List.size() != size || difList.size() != size || deaList.size() != size || histogramList.size() != size || velocityList.size() != size) {
			return;
		}

		for (int i = 0; i < size; i++) {
			StockData stockData = stockDataList.get(i);
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

	private void analyzeStockData(Stock stock, String period, ArrayList<StockData> stockDataList,
	                              ArrayList<StockData> drawVertexList, ArrayList<StockData> drawDataList,
	                              ArrayList<StockData> strokeVertexList, ArrayList<StockData> strokeDataList,
	                              ArrayList<StockData> segmentVertexList, ArrayList<StockData> segmentDataList,
	                              ArrayList<StockData> lineVertexList, ArrayList<StockData> lineDataList,
	                              ArrayList<StockData> outlineVertexList, ArrayList<StockData> outlineDataList) {
		TrendAnalyzer trendAnalyzer = TrendAnalyzer.getInstance();

		ArrayList<StockData> dataList = new ArrayList<>();
		for (StockData stockData : stockDataList) {
			dataList.add(new StockData(stockData));
		}

		trendAnalyzer.analyzeVertex(stockDataList, dataList, drawVertexList);
		trendAnalyzer.vertexListToDataList(stockDataList, drawVertexList, drawDataList);

		trendAnalyzer.analyzeLine(stockDataList, drawDataList, strokeVertexList, Trend.VERTEX_TOP_STROKE, Trend.VERTEX_BOTTOM_STROKE);
		trendAnalyzer.vertexListToDataList(stockDataList, strokeVertexList, strokeDataList);

		trendAnalyzer.analyzeLine(stockDataList, strokeDataList, segmentVertexList, Trend.VERTEX_TOP_SEGMENT, Trend.VERTEX_BOTTOM_SEGMENT);
		trendAnalyzer.vertexListToDataList(stockDataList, segmentVertexList, segmentDataList);

		trendAnalyzer.analyzeLine(stockDataList, segmentDataList, lineVertexList, Trend.VERTEX_TOP_LINE, Trend.VERTEX_BOTTOM_LINE);
		trendAnalyzer.vertexListToDataList(stockDataList, lineVertexList, lineDataList);

		trendAnalyzer.analyzeLine(stockDataList, lineDataList, outlineVertexList, Trend.VERTEX_TOP_OUTLINE, Trend.VERTEX_BOTTOM_OUTLINE);
		trendAnalyzer.vertexListToDataList(stockDataList, outlineVertexList, outlineDataList);

		//trendAnalyzer.testShowVertextNumber(stockDataList, stockDataList);

		if (Setting.getDebugLoopback()) {
			if (Setting.getDebugDirect()) {
				trendAnalyzer.debugShow(stockDataList, stockDataList);
			}

			if (Setting.getDisplayDraw()) {
				trendAnalyzer.debugShow(stockDataList, drawDataList);
			}

			if (Setting.getDisplayStroke()) {
				trendAnalyzer.debugShow(stockDataList, strokeDataList);
			}

			if (Setting.getDisplaySegment()) {
				trendAnalyzer.debugShow(stockDataList, segmentDataList);
			}

			if (Setting.getDisplayLine()) {
				trendAnalyzer.debugShow(stockDataList, lineDataList);
			}
		}

		analyzeAction(stock, period, drawVertexList, stockDataList, drawDataList, strokeDataList, segmentDataList);
	}

	private void analyzeAction(Stock stock, String period,
	                           ArrayList<StockData> drawVertexList,
	                           ArrayList<StockData> stockDataList,
	                           ArrayList<StockData> drawDataList,
	                           ArrayList<StockData> strokeDataList,
	                           ArrayList<StockData> segmentDataList) {
		String action = StockData.MARK_NONE;
		String trend = StockData.MARK_NONE;

		if (stock == null || stockDataList == null || drawDataList == null || strokeDataList == null ||
				segmentDataList == null || drawVertexList == null) {
			return;
		}

		if (stockDataList.size() < Trend.VERTEX_SIZE
				|| drawDataList.size() < Trend.VERTEX_SIZE
				|| strokeDataList.size() < Trend.VERTEX_SIZE
				|| segmentDataList.size() < Trend.VERTEX_SIZE
				|| drawVertexList.size() < Trend.VERTEX_SIZE) {
			return;
		}

		action += getDirectionAction(segmentDataList.get(segmentDataList.size() - 1), strokeDataList.get(strokeDataList.size() - 1), drawDataList.get(drawDataList.size() - 1));
		action += Constant.NEW_LINE;

//		{
//			String result = getSecondAction(stock, stockDataList, drawDataList);
//			action += result;
//		}

		StockData stockData = stockDataList.get(stockDataList.size() - 1);
		StockData prev = stockDataList.get(stockDataList.size() - 2);
		if (stockData.getTrend().directionOf(Trend.DIRECTION_UP)) {
			if (prev.getTrend().vertexOf(Trend.VERTEX_BOTTOM)) {
				if (!Period.isMinutePeriod(period)) {
					action += StockData.MARK_D;
				}
				String result = getSecondBottomAction(stock, drawVertexList, strokeDataList, segmentDataList);
				action += result;
			}
		} else if (stockData.getTrend().directionOf(Trend.DIRECTION_DOWN)) {
			if (prev.getTrend().vertexOf(Trend.VERTEX_TOP)) {
				if (!Period.isMinutePeriod(period)) {
					action += StockData.MARK_G;
				}
				String result = getSecondTopAction(stock, drawVertexList, strokeDataList, segmentDataList);
				action += result;
			}
		}

		if (stockData.getMacd().getVelocity() > 0) {
			action += StockData.MARK_ADD;
		} else if (stockData.getMacd().getVelocity() < 0) {
			action += StockData.MARK_MINUS;
		}

		stock.setDateTime(stockData.getDate(), stockData.getTime());
		stock.setAction(period, action + stockData.getAction());
	}

	String getDirectionAction(StockData segmentData, StockData strokeData, StockData drawData) {
		if (segmentData == null || strokeData == null || drawData == null) {
			return "";
		}

		StringBuilder result = new StringBuilder();

		appendDirection(result, segmentData);
		appendDirection(result, strokeData);
		appendDirection(result, drawData);

		return result.toString();
	}

	private void appendDirection(StringBuilder builder, StockData data) {
		if (builder == null || data == null) {
			return;
		}

		Trend trend = data.getTrend();
		if (trend == null) {
			return;
		}

		if (trend.directionOf(Trend.DIRECTION_UP)) {
			builder.append(StockData.MARK_ADD);
		} else if (trend.directionOf(Trend.DIRECTION_DOWN)) {
			builder.append(StockData.MARK_MINUS);
		}
	}

	private String getSecondAction(Stock stock, ArrayList<StockData> stockDataList, ArrayList<StockData> drawDataList) {
		String result = "";
		StockData stockData;
		StockData drawData0;
		StockData drawData1;
		StockData drawData2;

		if ((stockDataList == null) || (stockDataList.size() < 2 * Trend.VERTEX_SIZE)) {
			return result;
		}

		if ((drawDataList == null) || (drawDataList.size() < 2 * Trend.VERTEX_SIZE)) {
			return result;
		}

		int index = drawDataList.size() - 1;
		drawData0 = drawDataList.get(index);
		drawData1 = drawDataList.get(index - 1);
		drawData2 = drawDataList.get(index - 2);

		if (drawData0.getTrend().include(drawData2.getTrend()) || drawData0.getTrend().includedBy(drawData2.getTrend())) {
			return result;
		}

		stockData = stockDataList.get(drawData1.getTrend().getIndexStart());
		if (stockData == null) {
			return result;
		}
		if (!(stockData.getTrend().getVertex() == Trend.VERTEX_BOTTOM || stockData.getTrend().getVertex() == Trend.VERTEX_TOP)) {
			return result;
		}

		stockData = stockDataList.get(drawData1.getTrend().getIndexEnd());
		if (stockData == null) {
			return result;
		}
		if (!(stockData.getTrend().getVertex() == Trend.VERTEX_BOTTOM || stockData.getTrend().getVertex() == Trend.VERTEX_TOP)) {
			return result;
		}

//		if (stock.getPrice() < drawData1.getTrend().getVertexLow() || stock.getPrice() > drawData1.getTrend().getVertexHigh()) {
//			return result;
//		}

		stockData = stockDataList.get(drawData2.getTrend().getIndexStart());
		if (stockData == null) {
			return result;
		}

		if (stockData.getTrend().vertexOf(Trend.VERTEX_BOTTOM_STROKE)) {
			result += StockData.MARK_BUY2;
			if (stockData.getTrend().vertexOf(Trend.VERTEX_BOTTOM_SEGMENT)) {
				result += StockData.MARK_BUY2;
			}
		} else if (stockData.getTrend().vertexOf(Trend.VERTEX_TOP_STROKE)) {
			result += StockData.MARK_SELL2;
			if (stockData.getTrend().vertexOf(Trend.VERTEX_TOP_SEGMENT)) {
				result += StockData.MARK_SELL2;
			}
		}

		return result;
	}

	private static final int MIN_VERTEX_LIST_SIZE = 2 * Trend.VERTEX_SIZE + 1;
	private static final int MIN_STROKE_DATA_LIST_SIZE = 2 * Trend.VERTEX_SIZE;
	private static final int MIN_SEGMENT_DATA_LIST_SIZE = 2 * Trend.VERTEX_SIZE;

	private String getSecondBottomAction(Stock stock, ArrayList<StockData> vertexList,
	                                     ArrayList<StockData> strokeDataList,
	                                     ArrayList<StockData> segmentDataList) {
		return getAction(stock, vertexList, strokeDataList, segmentDataList, true);
	}

	private String getSecondTopAction(Stock stock, ArrayList<StockData> vertexList,
	                                  ArrayList<StockData> strokeDataList,
	                                  ArrayList<StockData> segmentDataList) {
		return getAction(stock, vertexList, strokeDataList, segmentDataList, false);
	}

	private String getAction(Stock stock, ArrayList<StockData> vertexList,
	                         ArrayList<StockData> strokeDataList,
	                         ArrayList<StockData> segmentDataList, boolean isBottom) {
		String result = "";

		if (vertexList == null || vertexList.size() < MIN_VERTEX_LIST_SIZE ||
				strokeDataList == null || strokeDataList.size() < MIN_STROKE_DATA_LIST_SIZE ||
				segmentDataList == null || segmentDataList.size() < MIN_SEGMENT_DATA_LIST_SIZE) {
			return result;
		}

		try {
			StockData firstVertex = vertexList.get(vertexList.size() - 4);
			StockData secondVertex = vertexList.get(vertexList.size() - 3);
			StockData thirdVertex = vertexList.get(vertexList.size() - 2);
			StockData fourthVertex = vertexList.get(vertexList.size() - 1);

			if (firstVertex == null || secondVertex == null || thirdVertex == null || fourthVertex == null) {
				return result;
			}

			StockData baseStockData = segmentDataList.get(segmentDataList.size() - 4);
			StockData brokenStockData = segmentDataList.get(segmentDataList.size() - 2);

			if (baseStockData == null || brokenStockData == null) {
				return result;
			}

			if (isBottom) {
				if (!firstVertex.getTrend().vertexOf(Trend.VERTEX_BOTTOM_SEGMENT)) {
					return result;
				}

				if (firstVertex.getTrend().getVertexLow() < thirdVertex.getTrend().getVertexLow() &&
						thirdVertex.getTrend().getVertexLow() < stock.getPrice() &&
						stock.getPrice() < secondVertex.getTrend().getVertexHigh()) {
					result += StockData.MARK_BUY2;
					result += StockData.MARK_BUY2;
				}
			} else {
				if (!firstVertex.getTrend().vertexOf(Trend.VERTEX_TOP_SEGMENT)) {
					return result;
				}

				if (firstVertex.getTrend().getVertexHigh() > thirdVertex.getTrend().getVertexHigh() &&
						thirdVertex.getTrend().getVertexHigh() > stock.getPrice() &&
						stock.getPrice() > secondVertex.getTrend().getVertexLow()) {
					result += StockData.MARK_SELL2;
					result += StockData.MARK_SELL2;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	protected void updateNotification(Stock stock) {
		if (stock == null || mContext == null || mContentTitle == null || mContentText == null) {
			return;
		}

		if (stock.getPrice() == 0 || !stock.hasFlag(Stock.FLAG_NOTIFY)) {
			return;
		}

		mContentTitle.setLength(0);
		mContentText.setLength(0);

		for (String period : Period.PERIODS) {
			if (!Setting.getPeriod(period)) {
				continue;
			}

			String action = stock.getAction(period);
			setContentTitle(period, action);
		}

		if (TextUtils.isEmpty(mContentTitle)) {
			return;
		}

		if (!Market.isTradingHours()) {
			Toast.makeText(mContext,
					mContext.getResources().getString(R.string.out_of_trading_hours),
					Toast.LENGTH_SHORT).show();
			return;
		}

		mContentTitle.insert(0, stock.getName() + " " + stock.getPrice() + " " + stock.getNet() + " ");
		RecordFile.writeNotificationFile(mContentTitle.toString());
		try {
			int code = Integer.parseInt(stock.getCode());
			notify(code, Config.MESSAGE_CHANNEL_ID, Config.MESSAGE_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH,
					mContentTitle.toString(), mContentText.toString());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	void setContentTitle(String period, String action) {
		if (mNotifyActions == null || mContentTitle == null) {
			return;
		}

		if (period == null || action == null || period.isEmpty() || action.isEmpty()) {
			return;
		}

		boolean containsAction = false;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			containsAction = mNotifyActions.stream().anyMatch(action::contains);
		} else {
			for (String notifyAction : mNotifyActions) {
				if (action.contains(notifyAction)) {
					containsAction = true;
					break;
				}
			}
		}

		if (containsAction) {
			appendContentTitle(period, action);
		}
	}

	private void appendContentTitle(String period, String action) {
		mContentTitle.append(period).append(" ").append(action).append(" ");
	}

	public void notify(int id, String channelID, String channelName, int importance, String contentTitle, String contentText) {
		if (mNotificationManager == null || mContext == null) {
			return;
		}

		mNotificationManager.cancel(id);

		Intent intent = new Intent(mContext, StockFavoriteListActivity.class);
		intent.setType("vnd.android-dir/mms-sms");
		PendingIntent pendingIntent = PendingIntent.getActivity(
				mContext,
				id,
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);

		Notification.Builder notificationBuilder = new Notification.Builder(mContext)
				.setContentTitle(contentTitle)
				.setContentText(contentText)
				.setSmallIcon(R.drawable.ic_dialog_email)
				.setAutoCancel(true)
				.setContentIntent(pendingIntent);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			if (mNotificationManager.getNotificationChannel(channelID) == null) {
				NotificationChannel notificationChannel = new NotificationChannel(channelID, channelName, importance);
				notificationChannel.enableVibration(true);
				notificationChannel.setVibrationPattern(new long[]{500, 500, 500, 500, 500});
				notificationChannel.enableLights(true);
				notificationChannel.setLightColor(0xFF0000FF);
				mNotificationManager.createNotificationChannel(notificationChannel);
			}
			notificationBuilder.setChannelId(channelID);
		} else {
			notificationBuilder.setLights(0xFF0000FF, 100, 300);
		}

		mNotificationManager.notify(id, notificationBuilder.build());
	}
}
