package com.android.orion;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.FinancialData;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDatabaseManager;
import com.android.orion.database.StockDeal;
import com.android.orion.database.TotalShare;
import com.android.orion.indicator.MACD;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.StopWatch;
import com.android.orion.utility.Utility;


public class StockAnalyzer {
	static final String TAG = Constants.TAG + " "
			+ StockAnalyzer.class.getSimpleName();

	Context mContext;

	PowerManager mPowerManager;
	WakeLock mWakeLock;

	LocalBroadcastManager mLocalBroadcastManager;
	NotificationManager mNotificationManager;
	StockDatabaseManager mStockDatabaseManager;

	StockFilter mStockFilter;

	public StockAnalyzer(Context context) {
		mContext = context;

		mPowerManager = (PowerManager) mContext
				.getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				Constants.TAG + ":" + StockAnalyzer.class.getSimpleName());

		if (mLocalBroadcastManager == null) {
			mLocalBroadcastManager = LocalBroadcastManager
					.getInstance(mContext);
		}

		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		}

		if (mStockDatabaseManager == null) {
			mStockDatabaseManager = StockDatabaseManager.getInstance(mContext);
		}

		mStockFilter = new StockFilter(mContext);
	}

	void acquireWakeLock() {
		Log.d(TAG, "acquireWakeLock");

		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire();
			Log.d(TAG, "acquireWakeLock, mWakeLock acquired.");
		}
	}

	void releaseWakeLock() {
		Log.d(TAG, "releaseWakeLock");
		if (mWakeLock.isHeld()) {
			mWakeLock.release();
			Log.d(TAG, "releaseWakeLock, mWakeLock released.");
		}
	}

	void loadStockDataList(Stock stock, String period,
			ArrayList<StockData> stockDataList) {
		int index = 0;

		Cursor cursor = null;
		String selection = null;
		String sortOrder = null;

		if ((stock == null) || TextUtils.isEmpty(period)
				|| (stockDataList == null)) {
			return;
		}

		if (mStockDatabaseManager == null) {
			return;
		}

		try {
			stockDataList.clear();

			selection = mStockDatabaseManager.getStockDataSelection(
					stock.getId(), period);
			sortOrder = mStockDatabaseManager.getStockDataOrder();
			cursor = mStockDatabaseManager.queryStockData(selection, null,
					sortOrder);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockData stockData = new StockData(period);
					stockData.set(cursor);
					index = stockDataList.size();
					stockData.setIndex(index);
					stockData.setIndexStart(index);
					stockData.setIndexEnd(index);
					stockData.setAction(Constants.STOCK_ACTION_NONE);

					stockDataList.add(stockData);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}
	}

	void analyze(Stock stock, String period) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		ArrayList<StockData> stockDataList = null;

		if (stock == null) {
			return;
		}

		stockDataList = stock.getStockDataList(period);
		if (stockDataList == null) {
			return;
		}

		try {
			setupStockShareBonus(stock);
			setupStockFinancialData(stock);

			loadStockDataList(stock, period, stockDataList);
			analyzeStockData(stock, period, stockDataList);
			updateDatabase(stock, period, stockDataList);
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(TAG, "analyze:" + stock.getName() + " " + period + " "
				+ stopWatch.getInterval() + "s");
	}

	void analyze(Stock stock) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		if (stock == null) {
			return;
		}

		try {
			analyzeFinancial(stock);

			setupStockFinancialData(stock);
			setupStockShareBonus(stock);

			updateDatabase(stock);

			updateActionFile(stock);
			updateNotification(stock);
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(TAG, "analyze:" + stock.getName() + " " + stopWatch.getInterval()
				+ "s");
	}

	boolean isFinancialAnalyzed(Stock stock) {
		boolean result = false;

		FinancialData financialData = new FinancialData();
		financialData.setStockId(stock.getId());
		mStockDatabaseManager.getFinancialData(stock, financialData);

		if (financialData.getCreated().contains(Utility.getCurrentDateString())
				|| financialData.getModified().contains(
						Utility.getCurrentDateString())) {
		    result = true;
		}

		return result;
	}

	void analyzeFinancial(Stock stock) {
		ArrayList<FinancialData> financialDataList = new ArrayList<FinancialData>();
		ArrayList<TotalShare> totalShareList = new ArrayList<TotalShare>();
		ArrayList<ShareBonus> shareBonusList = new ArrayList<ShareBonus>();
		ArrayList<StockData> stockDataList = new ArrayList<StockData>();
		String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";

		mStockDatabaseManager.getFinancialDataList(stock, financialDataList,
				sortOrder);
		mStockDatabaseManager.getTotalShareList(stock, totalShareList,
				sortOrder);
		mStockDatabaseManager.getShareBonusList(stock, shareBonusList,
				sortOrder);
		mStockDatabaseManager.getStockDataList(stock, Constants.PERIOD_MONTH,
				stockDataList, sortOrder);

		setupTotalShare(financialDataList, totalShareList);
		setupNetProfitPerShareInYear(financialDataList);
		setupNetProfitPerShare(financialDataList);
		setupRate(financialDataList);
		setupRoe(financialDataList);
        setupRoi(stockDataList, financialDataList);

		for (FinancialData financialData : financialDataList) {
			financialData.setModified(Utility.getCurrentDateTimeString());
			mStockDatabaseManager.updateFinancialData(financialData,
					financialData.getContentValues());
		}

		for (StockData stockData : stockDataList) {
			stockData.setModified(Utility.getCurrentDateTimeString());
			mStockDatabaseManager.updateStockData(stockData,
					stockData.getContentValues());
		}
	}

	void setupTotalShare(ArrayList<FinancialData> financialDataList,
			ArrayList<TotalShare> totalShareList) {
		if (financialDataList == null || totalShareList == null) {
			return;
		}

		int j = 0;
		for (FinancialData financialData : financialDataList) {
			while (j < totalShareList.size()) {
				TotalShare totalShare = totalShareList.get(j);
				if (Utility.stringToCalendar(financialData.getDate(),
						Utility.CALENDAR_DATE_FORMAT).after(
						Utility.stringToCalendar(totalShare.getDate(),
								Utility.CALENDAR_DATE_FORMAT))) {
					financialData.setTotalShare(totalShare.getTotalShare());
					break;
				} else {
					j++;
				}
			}
		}
	}

	void setupNetProfitPerShare(ArrayList<FinancialData> financialDataList) {
		if (financialDataList == null) {
			return;
		}

		for (FinancialData financialData : financialDataList) {
			financialData.setupDebtToNetAssetsRatio();
			financialData.setupNetProfitMargin();
			financialData.setupNetProfitPerShare();
		}
	}

	void setupNetProfitPerShareInYear(ArrayList<FinancialData> financialDataList) {
		double mainBusinessIncome = 0;
		double mainBusinessIncomeInYear = 0;
		double netProfit = 0;
		double netProfitInYear = 0;
		double netProfitPerShareInYear = 0;
		double netProfitPerShare = 0;

		if (financialDataList == null) {
			return;
		}

		if (financialDataList.size() < Constants.SEASONS_IN_A_YEAR + 1) {
			return;
		}

		for (int i = 0; i < financialDataList.size()
				- Constants.SEASONS_IN_A_YEAR; i++) {
			mainBusinessIncomeInYear = 0;
			netProfitInYear = 0;
			netProfitPerShareInYear = 0;
			for (int j = 0; j < Constants.SEASONS_IN_A_YEAR; j++) {
				FinancialData current = financialDataList.get(i + j);
				FinancialData prev = financialDataList.get(i + j + 1);

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

			FinancialData financialData = financialDataList.get(i);
			financialData.setMainBusinessIncomeInYear(mainBusinessIncomeInYear);
			financialData.setNetProfitInYear(netProfitInYear);
			financialData.setNetProfitPerShareInYear(netProfitPerShareInYear);
		}
	}

	void setupRate(ArrayList<FinancialData> financialDataList) {
		double rate = 0;

		if (financialDataList == null) {
			return;
		}

		if (financialDataList.size() < Constants.SEASONS_IN_A_YEAR + 1) {
			return;
		}

		for (int i = 0; i < financialDataList.size()
				- Constants.SEASONS_IN_A_YEAR; i++) {
			FinancialData financialData = financialDataList.get(i);
			FinancialData prev = financialDataList.get(i
					+ Constants.SEASONS_IN_A_YEAR);

			if (prev == null || prev.getNetProfitPerShareInYear() == 0) {
				continue;
			}

			rate = Utility.Round(financialData.getNetProfitPerShareInYear()
					/ prev.getNetProfitPerShareInYear(),
					Constants.DOUBLE_FIXED_DECIMAL);

			financialData.setRate(rate);
		}
	}

	void setupRoe(ArrayList<FinancialData> financialDataList) {
		double roe = 0;

		if (financialDataList == null) {
			return;
		}

		if (financialDataList.size() < Constants.SEASONS_IN_A_YEAR + 1) {
			return;
		}

		for (int i = 0; i < financialDataList.size()
				- Constants.SEASONS_IN_A_YEAR; i++) {
			FinancialData financialData = financialDataList.get(i);
			FinancialData prev = financialDataList.get(i
					+ Constants.SEASONS_IN_A_YEAR);

			if (prev == null || prev.getBookValuePerShare() == 0) {
				continue;
			}

			roe = Utility.Round(
					100.0 * financialData.getNetProfitPerShareInYear()
							/ prev.getBookValuePerShare(),
					Constants.DOUBLE_FIXED_DECIMAL);
			if (roe < 0) {
				roe = 0;
			}

			financialData.setRoe(roe);
		}
	}

	void setupRoi(ArrayList<StockData> stockDataList,
			ArrayList<FinancialData> financialDataList) {
		double price = 0;
		double pe = 0;
		double pb = 0;
		double roi = 0;

		if (stockDataList == null || financialDataList == null) {
			return;
		}

		int j = 0;
		for (StockData stockData : stockDataList) {
			price = stockData.getClose();
			if (price == 0) {
				continue;
			}

			while (j < financialDataList.size()) {
				FinancialData financialData = financialDataList.get(j);
				if (Utility.stringToCalendar(stockData.getDate(),
						Utility.CALENDAR_DATE_FORMAT).after(
						Utility.stringToCalendar(financialData.getDate(),
								Utility.CALENDAR_DATE_FORMAT))) {
					pe = Utility.Round(
							100.0 * financialData.getNetProfitPerShareInYear()
									/ price, Constants.DOUBLE_FIXED_DECIMAL);

					if (financialData.getBookValuePerShare() != 0) {
						pb = Utility.Round(
								price / financialData.getBookValuePerShare(),
								Constants.DOUBLE_FIXED_DECIMAL);
					}

					roi = Utility.Round(financialData.getRoe() * pe
							* Constants.ROI_COEFFICIENT,
							Constants.DOUBLE_FIXED_DECIMAL);
					if (roi < 0) {
						roi = 0;
					}

					stockData.setPe(pe);
					stockData.setPb(pb);
					stockData.setRoi(roi);
					break;
				} else {
					j++;
				}
			}
		}
	}

	void setupStockFinancialData(Stock stock) {
		String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";
		FinancialData financialData = new FinancialData();
		ArrayList<FinancialData> financialDataList = new ArrayList<FinancialData>();

		financialData.setStockId(stock.getId());

		mStockDatabaseManager.getFinancialData(stock, financialData);
		mStockDatabaseManager.getFinancialDataList(stock, financialDataList,
				sortOrder);
		mStockDatabaseManager.updateStockDeal(stock);

		stock.setBookValuePerShare(financialData.getBookValuePerShare());
		stock.setTotalAssets(financialData.getTotalAssets());
		stock.setTotalLongTermLiabilities(financialData
				.getTotalLongTermLiabilities());
		stock.setMainBusinessIncome(financialData.getMainBusinessIncome());
		stock.setNetProfit(financialData.getNetProfit());
		stock.setCashFlowPerShare(financialData.getCashFlowPerShare());

		stock.setupMarketValue();
		stock.setupNetProfitPerShare();
		stock.setupNetProfitPerShareInYear(financialDataList);
		stock.setupNetProfitMargin();
		stock.setupRate(financialDataList);
		stock.setupDebtToNetAssetsRatio();
		stock.setupRoe(financialDataList);
		stock.setupPE();
		stock.setupPB();
		stock.setupRoi();
	}

	void setupStockShareBonus(Stock stock) {
		double totalDivident = 0;

		String yearString = "";
		String prevYearString = "";
		String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";

		ArrayList<ShareBonus> shareBonusList = new ArrayList<ShareBonus>();

		mStockDatabaseManager.getShareBonusList(stock, shareBonusList,
				sortOrder);

		int i = 0;
		for (ShareBonus shareBonus : shareBonusList) {
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
				stock.setDate(shareBonus.getDate());
				stock.setRDate(shareBonus.getRDate());
			}
			stock.setDividend(Utility.Round(totalDivident,
					Constants.DOUBLE_FIXED_DECIMAL));
			stock.setupBonus();
			stock.setupYield();
			stock.setupDividendRatio();

			prevYearString = yearString;
			i++;
		}
	}

	private void setMACD(ArrayList<StockData> stockDataList) {
		int size = 0;

		double average5 = 0;
		double average10 = 0;
		double dif = 0;
		double dea = 0;
		double histogram = 0;

		MACD macd = new MACD();

		size = stockDataList.size();

		if (size < Constants.STOCK_VERTEX_TYPING_SIZE) {
			return;
		}

		macd.mPriceList.clear();

		for (int i = 0; i < size; i++) {
			macd.mPriceList
					.add((stockDataList.get(i).getVertexHigh() + stockDataList
							.get(i).getVertexLow()) / 2.0);
		}

		macd.calculate();

		for (int i = 0; i < size; i++) {
			average5 = macd.mEMAAverage5List.get(i);
			average10 = macd.mEMAAverage10List.get(i);
			dif = macd.mDIFList.get(i);
			dea = macd.mDEAList.get(i);
			histogram = macd.mHistogramList.get(i);

			stockDataList.get(i).setAverage5(average5);
			stockDataList.get(i).setAverage10(average10);
			stockDataList.get(i).setDIF(dif);
			stockDataList.get(i).setDEA(dea);
			stockDataList.get(i).setHistogram(histogram);
		}
	}

	void analyzeStockData(Stock stock, String period,
			ArrayList<StockData> stockDataList) {
		VertexAnalyzer vertexAnalyzer = new VertexAnalyzer();

		ArrayList<StockData> drawVertexList = new ArrayList<StockData>();
		ArrayList<StockData> drawDataList = new ArrayList<StockData>();
		ArrayList<StockData> strokeVertexList = new ArrayList<StockData>();
		ArrayList<StockData> strokeDataList = new ArrayList<StockData>();
		ArrayList<StockData> segmentVertexList = new ArrayList<StockData>();
		ArrayList<StockData> segmentDataList = new ArrayList<StockData>();
		ArrayList<StockData> overlapList = new ArrayList<StockData>();

		vertexAnalyzer.analyzeVertex(stockDataList, drawVertexList);
		vertexAnalyzer.vertexListToDataList(stockDataList, drawVertexList,
				drawDataList);

		setMACD(stockDataList);

		vertexAnalyzer.analyzeLine(stockDataList, drawDataList,
				strokeVertexList, Constants.STOCK_VERTEX_TOP_STROKE,
				Constants.STOCK_VERTEX_BOTTOM_STROKE);
		vertexAnalyzer.vertexListToDataList(stockDataList, strokeVertexList,
				strokeDataList);

		vertexAnalyzer.analyzeLine(stockDataList, strokeDataList,
				segmentVertexList, Constants.STOCK_VERTEX_TOP_SEGMENT,
				Constants.STOCK_VERTEX_BOTTOM_SEGMENT);
		vertexAnalyzer.vertexListToDataList(stockDataList, segmentVertexList,
				segmentDataList);

		vertexAnalyzer.analyzeOverlap(stockDataList, strokeDataList,
				overlapList);

//        vertexAnalyzer.analyzeOverlap(stockDataList, segmentDataList,
//                overlapList);

		// vertexAnalyzer.testShowVertextNumber(stockDataList, stockDataList);

		vertexAnalyzer.analyzeAction(stockDataList, segmentDataList,
				Constants.STOCK_DIVERGENCE_TYPE_SEGMENT);
		vertexAnalyzer.analyzeAction(stockDataList, strokeDataList,
				Constants.STOCK_DIVERGENCE_TYPE_STROKE);
		vertexAnalyzer.analyzeAction(stockDataList, drawDataList,
				Constants.STOCK_DIVERGENCE_TYPE_DRAW);

		vertexAnalyzer.analyzeDirection(stockDataList);

		analyzeAction(stock, period, stockDataList, drawVertexList, overlapList);
	}

	private String getFirstBottomAction(Stock stock, ArrayList<StockData> vertexList, ArrayList<StockData> overlapList) {
		String result = "";
		StockData start = null;
		StockData end = null;
		StockData overlap = null;

		if ((vertexList == null)
				|| (vertexList.size() < Constants.STOCK_VERTEX_TYPING_SIZE + 2)) {
			return result;
		}

		end = vertexList.get(vertexList.size() - 2);
		if (end == null) {
			return result;
		}

		if ((overlapList == null) || (overlapList.size() < 1)) {
			return result;
		}

		if (end.getVertexLow() > overlapList.get(overlapList.size() - 1).getOverlapLow()) {
			return result;
		}

		if (end.vertexOf(Constants.STOCK_VERTEX_BOTTOM)) {
			result += Constants.STOCK_ACTION_BUY1;
			result += Constants.STOCK_ACTION_BUY1;

			for (int i = vertexList.size() - 3; i >= 0; i--) {
				start = vertexList.get(i);
				if ((start != null) && (start.vertexOf(Constants.STOCK_VERTEX_TOP))) {
					if ((stock.getPrice() > 0) && (start.getVertexHigh() > 0)) {
						result += " " + (int)(100 * (stock.getPrice() - start.getVertexHigh())/start.getVertexHigh());
						result += "/" + (int)(100 * (end.getVertexLow() - start.getVertexHigh())/start.getVertexHigh());
					}
					break;
				}
			}
		}

		return result;
	}

	private String getFirstTopAction(Stock stock, ArrayList<StockData> vertexList, ArrayList<StockData> overlapList) {
		String result = "";
		StockData start = null;
        StockData end = null;

		if ((vertexList == null)
				|| (vertexList.size() < Constants.STOCK_VERTEX_TYPING_SIZE + 2)) {
			return result;
		}

		end = vertexList.get(vertexList.size() - 2);
		if (end == null) {
			return result;
		}

		if ((overlapList == null) || (overlapList.size() < 1)) {
			return result;
		}

		if (end.getVertexHigh() < overlapList.get(overlapList.size() - 1).getOverlapHigh()) {
			return result;
		}

		if (end.vertexOf(Constants.STOCK_VERTEX_TOP)) {
			result += Constants.STOCK_ACTION_SELL1;
			result += Constants.STOCK_ACTION_SELL1;

			for (int i = vertexList.size() - 3; i >= 0; i--) {
				start = vertexList.get(i);
				if ((start != null) && (start.vertexOf(Constants.STOCK_VERTEX_BOTTOM))) {
					if ((stock.getPrice() > 0) && (start.getVertexLow() > 0)) {
						result += " " + (int)(100 * (stock.getPrice() - start.getVertexLow())/start.getVertexLow());
						result += "/" + (int)(100 * (end.getVertexHigh() - start.getVertexLow())/start.getVertexLow());
					}
					break;
				}
			}
		}

		return result;
	}

	private String getSecondBottomAction(Stock stock, ArrayList<StockData> vertexList,
			ArrayList<StockData> overlapList) {
		String result = "";
		StockData prev = null;
		StockData stockData = null;
		StockData start = null;

		if ((vertexList == null)
				|| (vertexList.size() < Constants.STOCK_VERTEX_TYPING_SIZE + 2)) {
			return result;
		}

//		if ((overlapList == null) || (overlapList.size() < 1)) {
//			return result;
//		}

		prev = vertexList.get(vertexList.size() - 2);
		if (prev == null) {
			return result;
		}

		stockData = vertexList.get(vertexList.size() - 4);
		if (stockData == null) {
			return result;
		}

		if (prev.getVertexLow() < stockData.getVertexLow()) {
			return result;
		}

		if (stock.getPrice() > prev.getVertexHigh()) {
			return result;
		}

		if (stockData.vertexOf(Constants.STOCK_VERTEX_BOTTOM_SEGMENT)) {
			result += Constants.STOCK_ACTION_BUY2;
			result += Constants.STOCK_ACTION_BUY2;

			for (int i = vertexList.size() - 5; i >= 0; i--) {
				start = vertexList.get(i);
				if ((start != null) && (start.vertexOf(Constants.STOCK_VERTEX_TOP_SEGMENT))) {
					if ((stock.getPrice() > 0) && (start.getHigh() > 0)) {
						result += " " + (int)(100 * (stock.getPrice() - start.getHigh())/start.getHigh());
						result += "/" + (int)(100 * (stockData.getLow() - start.getHigh())/start.getHigh());
					}
					break;
				}
			}
		}

		return result;
	}

	private String getSecondTopAction(Stock stock, ArrayList<StockData> vertexList,
			ArrayList<StockData> overlapList) {
		String result = "";
		StockData prev = null;
		StockData stockData = null;
		StockData start = null;

		if ((vertexList == null)
				|| (vertexList.size() < Constants.STOCK_VERTEX_TYPING_SIZE + 2)) {
			return result;
		}

//		if ((overlapList == null) || (overlapList.size() < 1)) {
//			return result;
//		}

		prev = vertexList.get(vertexList.size() - 2);
		if (prev == null) {
			return result;
		}

		stockData = vertexList.get(vertexList.size() - 4);
		if (stockData == null) {
			return result;
		}

		if (prev.getVertexHigh() > stockData.getVertexHigh()) {
			return result;
		}

		if (stock.getPrice() < prev.getVertexLow()) {
			return result;
		}

		if (stockData.vertexOf(Constants.STOCK_VERTEX_TOP_SEGMENT)) {
			result += Constants.STOCK_ACTION_SELL2;
			result += Constants.STOCK_ACTION_SELL2;

			for (int i = vertexList.size() - 5; i >= 0; i--) {
				start = vertexList.get(i);
				if ((start != null) && (start.vertexOf(Constants.STOCK_VERTEX_BOTTOM_SEGMENT))) {
					if ((stock.getPrice() > 0) && (start.getLow() > 0)) {
						result += " " + (int)(100 * (stock.getPrice() - start.getLow())/start.getLow());
						result += "/" + (int)(100 * (stockData.getHigh() - start.getLow())/start.getLow());
					}
					break;
				}
			}
		}

		return result;
	}

	private void analyzeAction(Stock stock, String period,
			ArrayList<StockData> stockDataList,
			ArrayList<StockData> drawVertexList,
			ArrayList<StockData> overlapList) {
		String action = Constants.STOCK_ACTION_NONE;
		StockData prev = null;
		StockData stockData = null;

		if (stockDataList == null) {
			Log.d(TAG, "analyzeAction return" + " stockDataList = " + stockDataList);
			return;
		}

		if (stockDataList.size() < Constants.STOCK_VERTEX_TYPING_SIZE) {
			return;
		}

		prev = stockDataList.get(stockDataList.size() - 2);
		stockData = stockDataList.get(stockDataList.size() - 1);

		if (stockData.directionOf(Constants.STOCK_DIRECTION_UP_SEGMENT)) {
			if (prev.vertexOf(Constants.STOCK_VERTEX_BOTTOM_SEGMENT)) {
				action += Constants.STOCK_ACTION_D;
			} else {
				action += Constants.STOCK_ACTION_ADD;
			}
		} else if (stockData
				.directionOf(Constants.STOCK_DIRECTION_DOWN_SEGMENT)) {
			if (prev.vertexOf(Constants.STOCK_VERTEX_TOP_SEGMENT)) {
				action += Constants.STOCK_ACTION_G;
			} else {
				action += Constants.STOCK_ACTION_MINUS;
			}
		}

		if (stockData.directionOf(Constants.STOCK_DIRECTION_UP_STROKE)) {
			if (prev.vertexOf(Constants.STOCK_VERTEX_BOTTOM_STROKE)) {
				action += Constants.STOCK_ACTION_D;
			} else {
				action += Constants.STOCK_ACTION_ADD;
			}
		} else if (stockData.directionOf(Constants.STOCK_DIRECTION_DOWN_STROKE)) {
			if (prev.vertexOf(Constants.STOCK_VERTEX_TOP_STROKE)) {
				action += Constants.STOCK_ACTION_G;
			} else {
				action += Constants.STOCK_ACTION_MINUS;
			}
		}

		if (stockData.directionOf(Constants.STOCK_DIRECTION_UP)) {
			if (prev.vertexOf(Constants.STOCK_VERTEX_BOTTOM)) {
				action += Constants.STOCK_ACTION_D;
				if (period.equals(Constants.PERIOD_DAY)) {
					String result1 = getFirstBottomAction(stock, drawVertexList, overlapList);
					if (!TextUtils.isEmpty(result1)) {
						action = result1;
					}
				}

				String result2 = getSecondBottomAction(stock, drawVertexList,
						overlapList);
				if (!TextUtils.isEmpty(result2)) {
					action = result2;
				}
			} else {
				action += Constants.STOCK_ACTION_ADD;
			}
		} else if (stockData.directionOf(Constants.STOCK_DIRECTION_DOWN)) {
			if (prev.vertexOf(Constants.STOCK_VERTEX_TOP)) {
				action += Constants.STOCK_ACTION_G;
				if (period.equals(Constants.PERIOD_DAY)) {
					String result1 = getFirstTopAction(stock, drawVertexList, overlapList);
					if (!TextUtils.isEmpty(result1)) {
						action = result1;
					}
				}

				String result2 = getSecondTopAction(stock, drawVertexList, overlapList);
				if (!TextUtils.isEmpty(result2)) {
					action = result2;
				}
			} else {
				action += Constants.STOCK_ACTION_MINUS;
			}
		}

		stock.setAction(period, action + stockData.getAction());
	}

	private void updateDatabase(Stock stock) {
		if (mStockDatabaseManager == null) {
			Log.d(TAG, "updateDatabase return " + " mStockDatabaseManager = "
					+ mStockDatabaseManager);
			return;
		}

		try {
			mStockDatabaseManager.updateStock(stock,
					stock.getContentValuesAnalyze(""));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void updateDatabase(Stock stock, String period,
			ArrayList<StockData> stockDataList) {
		ContentValues contentValues[] = new ContentValues[stockDataList.size()];

		if (mStockDatabaseManager == null) {
			Log.d(TAG, "updateDatabase return " + " mStockDatabaseManager = "
					+ mStockDatabaseManager);
			return;
		}

		try {
			mStockDatabaseManager.deleteStockData(stock.getId(), period);

			for (int i = 0; i < stockDataList.size(); i++) {
				StockData stockData = stockDataList.get(i);
				contentValues[i] = stockData.getContentValues();
			}

			mStockDatabaseManager.bulkInsertStockData(contentValues);
			stock.setModified(Utility.getCurrentDateTimeString());
			mStockDatabaseManager.updateStock(stock,
					stock.getContentValuesAnalyze(period));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int getNetFromAction(String action) {
		int index = 0;
		int result = 0;
		String remainingString;
		String netString;

		try {
			remainingString = action.substring(4);
			if (TextUtils.isEmpty(remainingString)) {
				return result;
			}

			index = remainingString.indexOf("/");
			if (index < 0) {
				return result;
			}

			netString = remainingString.substring(0, index).trim();
			if (TextUtils.isEmpty(netString)) {
				return result;
			}

			result = Integer.valueOf(netString);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return result;
		}
	}

	private void updateActionFile(Stock stock) {
		String fileName;
		StringBuilder logString = new StringBuilder();

		logString.append(stock.getName() + " " + stock.getPrice() + " "
				+ stock.getNet() + " ");

		for (String period : Constants.PERIODS) {
			if (Preferences.getBoolean(mContext, period, false)) {
				logString.append(period + " " + stock.getAction(period) + " ");
			}
		}
		logString.append(stock.getModified() + "\n");

		try {
			fileName = Environment.getExternalStorageDirectory().getCanonicalPath() + "/Android/" + stock.getSE() + stock.getCode() + stock.getName() + ".txt";
			Utility.writeFile(fileName, logString.toString(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateNotification(Stock stock) {
		int id = 0;
		String contentText = "";
		Notification.Builder notification;
        ArrayList<StockDeal> stockDealList = new ArrayList<StockDeal>();

		if (mNotificationManager == null) {
			return;
		}

		id = (int) stock.getId();
		mNotificationManager.cancel(id);

		if (!Preferences.getBoolean(mContext, Settings.KEY_NOTIFICATION_MESSAGE,
				true)) {
			return;
		}

		if (TextUtils.isEmpty(stock.getOperation())) {
			return;
		}

		if (stock.getPrice() == 0) {
			return;
		}

		StringBuilder actionString = new StringBuilder();
		for (String period : Constants.PERIODS) {
			if (Preferences.getBoolean(mContext, period, false)) {
				String action = stock.getAction(period);
				if (action.contains("B1B1") || action.contains("B2B2")) {
					StockDeal stockDeal = new StockDeal();
					mStockDatabaseManager.getStockDealToBuy(stock, stockDeal);
					if (!TextUtils.isEmpty(stockDeal.getCode()) && (stockDeal.getVolume() <= 0)) {
						actionString.append(period + " " + action + " ");
						actionString.append(" " + stockDeal.getProfit() + " ");
					}
				} else if (action.contains("S1S1") || action.contains("S2S2")) {
					mStockDatabaseManager.getStockDealListToSell(stock, stockDealList);
					double totalProfit = 0;
					for (StockDeal stockDeal : stockDealList) {
						totalProfit += stockDeal.getProfit();
					}

					if (totalProfit > 0) {
						actionString.append(period + " " + action + " ");
						actionString.append(" " + (int)totalProfit + " ");
					}
				}
			}
		}

		if (TextUtils.isEmpty(actionString)) {
			return;
		}

		if (actionString.toString().contains("B") && actionString.toString().contains("S")) {
			return;
		}

		StringBuilder contentTitle = new StringBuilder();
		contentTitle.append(stock.getName() + " " + stock.getPrice() + " "
				+ stock.getNet());
		contentTitle.append(" " + actionString);

		Intent intent = new Intent(mContext, StockListActivity.class);
		intent.setType("vnd.android-dir/mms-sms");
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
				intent, 0);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel notificationChannel = new NotificationChannel(Constants.MESSAGE_CHANNEL_ID,
					Constants.MESSAGE_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
			notificationChannel.enableVibration(true);
			notificationChannel.setVibrationPattern(new long[]{500, 500, 500, 500, 500});
			notificationChannel.enableLights(true);
			notificationChannel.setLightColor(0xFF0000FF);
			mNotificationManager.createNotificationChannel(notificationChannel);

			notification = new Notification.Builder(
					mContext, Constants.MESSAGE_CHANNEL_ID).setContentTitle(contentTitle)
					.setContentText(contentText)
					.setSmallIcon(R.drawable.ic_dialog_email)
					.setAutoCancel(true)
					.setContentIntent(pendingIntent);
		} else {
			notification = new Notification.Builder(
					mContext).setContentTitle(contentTitle)
					.setContentText(contentText)
					.setSmallIcon(R.drawable.ic_dialog_email).setAutoCancel(true)
					.setLights(0xFF0000FF, 100, 300)
					.setContentIntent(pendingIntent);
		}

		mNotificationManager.notify(id, notification.build());
	}
}
