package com.android.orion;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.StockFinancial;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDatabaseManager;
import com.android.orion.database.StockDeal;
import com.android.orion.database.StockTrends;
import com.android.orion.database.TotalShare;
import com.android.orion.indicator.MACD;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.RecordFile;
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
	}

	void acquireWakeLock() {
		Log.d(TAG, "acquireWakeLock");

		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire(10*60*1000L /*10 minutes*/);
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
		boolean backTest = false;
		int index = 0;

		Calendar calendar = Calendar.getInstance();
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

		backTest = Preferences.getBoolean(mContext, Settings.KEY_BACKTEST,
				false);
		if (backTest) {
			String dateTime = Preferences.getString(mContext, Settings.KEY_BACKTEST_DATE_TIME, "");
			if (!TextUtils.isEmpty(dateTime)) {
				calendar = Utility.getCalendar(dateTime, Utility.CALENDAR_DATE_TIME_FORMAT);
			} else {
				calendar = Calendar.getInstance();
			}
		}

		try {
			stockDataList.clear();

			selection = mStockDatabaseManager.getStockDataSelection(
					stock.getId(), period, StockData.LEVEL_NONE);
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
					stockData.setAction(StockData.ACTION_NONE);

					if (backTest) {
						if (stockData.getCalendar().after(calendar)) {
							stock.setPrice(stockData.getClose());
							break;
						}
					}

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
		ArrayList<StockData> drawVertexList =  null;
		ArrayList<StockData> drawDataList =  null;
		ArrayList<StockData> strokeVertexList =  null;
		ArrayList<StockData> strokeDataList =  null;
		ArrayList<StockData> segmentVertexList =  null;
		ArrayList<StockData> segmentDataList =  null;

		if (stock == null) {
			return;
		}

		stockDataList = stock.getStockDataList(period);
		if (stockDataList == null) {
			return;
		}

		drawVertexList = stock.getDrawVertexList(period);
		if (drawVertexList == null) {
			return;
		}

		drawDataList = stock.getDrawDataList(period);
		if (drawDataList == null) {
			return;
		}

		strokeVertexList = stock.getStrokeVertexList(period);
		if (strokeVertexList == null) {
			return;
		}

		strokeDataList = stock.getStrokeDataList(period);
		if (strokeDataList == null) {
			return;
		}

		segmentVertexList = stock.getSegmentVertexList(period);
		if (segmentVertexList == null) {
			return;
		}

		segmentDataList = stock.getSegmentDataList(period);
		if (segmentDataList == null) {
			return;
		}

		try {
			setupStockShareBonus(stock);
			setupStockFinancial(stock);

			loadStockDataList(stock, period, stockDataList);
			analyzeStockData(stock, period, stockDataList,
					drawVertexList, drawDataList,
					strokeVertexList, strokeDataList,
					segmentVertexList, segmentDataList);
			updateDatabase(stock, period, stockDataList,
					drawDataList, strokeDataList, segmentDataList);
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
            if (!Stock.CLASS_INDEX.equals(stock.getClases())) {
                analyzeStockFinancial(stock);
                setupStockFinancial(stock);
                setupStockShareBonus(stock);
            }

			setupStockOperate(stock);

			updateDatabase(stock);

			updateNotification(stock);
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(TAG, "analyze:" + stock.getName() + " " + stopWatch.getInterval()
				+ "s");
	}

	private void analyzeStockFinancial(Stock stock) {
		ArrayList<StockFinancial> stockFinancialList = new ArrayList<StockFinancial>();
		ArrayList<TotalShare> totalShareList = new ArrayList<TotalShare>();
		ArrayList<ShareBonus> shareBonusList = new ArrayList<ShareBonus>();
		ArrayList<StockData> stockDataList = new ArrayList<StockData>();
		String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";

		if (Stock.CLASS_INDEX.equals(stock.getClases())) {
		    return;
        }

		mStockDatabaseManager.getStockFinancialList(stock, stockFinancialList,
				sortOrder);
		mStockDatabaseManager.getTotalShareList(stock, totalShareList,
				sortOrder);
		mStockDatabaseManager.getShareBonusList(stock, shareBonusList,
				sortOrder);
		mStockDatabaseManager.getStockDataList(stock, Settings.KEY_PERIOD_MONTH,
				stockDataList, sortOrder);

		setupTotalShare(stockFinancialList, totalShareList);
		setupNetProfitPerShareInYear(stockFinancialList);
		setupNetProfitPerShare(stockFinancialList);
		setupRate(stockFinancialList);
		setupRoe(stockFinancialList);
        setupRoi(stockDataList, stockFinancialList);

		for (StockFinancial stockFinancial : stockFinancialList) {
			stockFinancial.setModified(Utility.getCurrentDateTimeString());
			mStockDatabaseManager.updateStockFinancial(stockFinancial,
					stockFinancial.getContentValues());
		}

		for (StockData stockData : stockDataList) {
			stockData.setModified(Utility.getCurrentDateTimeString());
			mStockDatabaseManager.updateStockData(stockData,
					stockData.getContentValues());
		}
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
			stockFinancial.setupDebtToNetAssetsRatio();
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

		if (stockFinancialList.size() < Constants.SEASONS_IN_A_YEAR + 1) {
			return;
		}

		for (int i = 0; i < stockFinancialList.size()
				- Constants.SEASONS_IN_A_YEAR; i++) {
			mainBusinessIncomeInYear = 0;
			netProfitInYear = 0;
			netProfitPerShareInYear = 0;
			for (int j = 0; j < Constants.SEASONS_IN_A_YEAR; j++) {
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

		if (stockFinancialList.size() < Constants.SEASONS_IN_A_YEAR + 1) {
			return;
		}

		for (int i = 0; i < stockFinancialList.size()
				- Constants.SEASONS_IN_A_YEAR; i++) {
			StockFinancial stockFinancial = stockFinancialList.get(i);
			StockFinancial prev = stockFinancialList.get(i
					+ Constants.SEASONS_IN_A_YEAR);

			if (prev == null || prev.getNetProfitPerShareInYear() == 0) {
				continue;
			}

			rate = Utility.Round(stockFinancial.getNetProfitPerShareInYear()
					/ prev.getNetProfitPerShareInYear(),
					Constants.DOUBLE_FIXED_DECIMAL);

			stockFinancial.setRate(rate);
		}
	}

	private void setupRoe(ArrayList<StockFinancial> stockFinancialList) {
		double roe = 0;

		if (stockFinancialList == null) {
			return;
		}

		if (stockFinancialList.size() < Constants.SEASONS_IN_A_YEAR + 1) {
			return;
		}

		for (int i = 0; i < stockFinancialList.size()
				- Constants.SEASONS_IN_A_YEAR; i++) {
			StockFinancial stockFinancial = stockFinancialList.get(i);
			StockFinancial prev = stockFinancialList.get(i
					+ Constants.SEASONS_IN_A_YEAR);

			if (prev == null || prev.getBookValuePerShare() == 0) {
				continue;
			}

			roe = Utility.Round(
					100.0 * stockFinancial.getNetProfitPerShareInYear()
							/ prev.getBookValuePerShare(),
					Constants.DOUBLE_FIXED_DECIMAL);
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
			price = stockData.getClose();
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
									/ price, Constants.DOUBLE_FIXED_DECIMAL);

					if (stockFinancial.getBookValuePerShare() != 0) {
						pb = Utility.Round(
								price / stockFinancial.getBookValuePerShare(),
								Constants.DOUBLE_FIXED_DECIMAL);
					}

					roi = Utility.Round(stockFinancial.getRoe() * pe
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

	private void setupStockFinancial(Stock stock) {
		String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";
		StockFinancial stockFinancial = new StockFinancial();
		ArrayList<StockFinancial> stockFinancialList = new ArrayList<StockFinancial>();

        if (Stock.CLASS_INDEX.equals(stock.getClases())) {
            return;
        }

		stockFinancial.setStockId(stock.getId());

		mStockDatabaseManager.getStockFinancial(stock, stockFinancial);
		mStockDatabaseManager.getStockFinancialList(stock, stockFinancialList,
				sortOrder);
		mStockDatabaseManager.updateStockDeal(stock);

		stock.setBookValuePerShare(stockFinancial.getBookValuePerShare());
		stock.setTotalAssets(stockFinancial.getTotalAssets());
		stock.setTotalLongTermLiabilities(stockFinancial
				.getTotalLongTermLiabilities());
		stock.setMainBusinessIncome(stockFinancial.getMainBusinessIncome());
		stock.setNetProfit(stockFinancial.getNetProfit());
		stock.setCashFlowPerShare(stockFinancial.getCashFlowPerShare());

		stock.setupMarketValue();
		stock.setupNetProfitPerShare();
		stock.setupNetProfitPerShareInYear(stockFinancialList);
		stock.setupNetProfitMargin();
		stock.setupRate(stockFinancialList);
		stock.setupDebtToNetAssetsRatio();
		stock.setupRoe(stockFinancialList);
		stock.setupPE();
		stock.setupPB();
		stock.setupRoi();
	}

	private void setupStockShareBonus(Stock stock) {
		double totalDivident = 0;

		String yearString = "";
		String prevYearString = "";
		String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";
		ArrayList<ShareBonus> shareBonusList = new ArrayList<ShareBonus>();

        if (Stock.CLASS_INDEX.equals(stock.getClases())) {
            return;
        }

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

	private void setupStockOperate(Stock stock) {
		StringBuilder result = new StringBuilder();
		ArrayMap<Integer, Integer> divergenceArrayMap = new ArrayMap<Integer, Integer>();

		if (stock == null) {
			return;
		}


		if (TextUtils.isEmpty(stock.getStatus())) {
			stock.setOperate("");
		} else {
			if (stock.getStatus().equals(Stock.STATUS_SUSPENSION)) {
				stock.setOperate(Stock.STATUS_SUSPENSION);
//				return;
			}
		}

		for (String period : Settings.KEY_PERIODS) {
			if (Preferences.getBoolean(mContext, period, false)) {
				updateDivergenceArrayMap(divergenceArrayMap, stock.getSegmentDataList(period));
				updateDivergenceArrayMap(divergenceArrayMap, stock.getStrokeDataList(period));
				updateDivergenceArrayMap(divergenceArrayMap, stock.getDrawDataList(period));
			}
		}

		if (divergenceArrayMap.size() > 0) {
			for (int i = 0; i < divergenceArrayMap.size(); i++) {
				result.append(divergenceArrayMap.valueAt(i));
				if (divergenceArrayMap.keyAt(i) == StockData.DIVERGENCE_UP) {
					result.append(StockData.ACTION_HIGH);
				} else if (divergenceArrayMap.keyAt(i) == StockData.DIVERGENCE_DOWN) {
					result.append(StockData.ACTION_LOW);
				}

				if (i < divergenceArrayMap.size() - 1) {
					result.append(" ");
				}
			}
		}

		stock.setOperate(result.toString());
	}

	private void setMACD(ArrayList<StockData> stockDataList) {
		int size = 0;

		double average5 = 0;
		double average10 = 0;
		double dif = 0;
		double dea = 0;
		double histogram = 0;
		MACD macd = new MACD();

		if (stockDataList == null) {
		    return;
        }

		size = stockDataList.size();
		if (size < StockData.VERTEX_TYPING_SIZE) {
			return;
		}

		macd.mPriceList.clear();

		for (int i = 0; i < size; i++) {
			macd.mPriceList
					.add(stockDataList.get(i).getClose());
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

	private void analyzeStockData(Stock stock, String period, ArrayList<StockData> stockDataList,
						  ArrayList<StockData> drawVertexList, ArrayList<StockData> drawDataList,
						  ArrayList<StockData> strokeVertexList, ArrayList<StockData> strokeDataList,
						  ArrayList<StockData> segmentVertexList, ArrayList<StockData> segmentDataList) {
		StockVertexAnalyzer stockVertexAnalyzer = new StockVertexAnalyzer();
		ArrayList<StockData> overlapList = new ArrayList<StockData>();

		setMACD(stockDataList);

		stockVertexAnalyzer.analyzeVertex(stockDataList, drawVertexList);
		stockVertexAnalyzer.vertexListToDataList(stockDataList, drawVertexList,
				drawDataList, StockData.LEVEL_DRAW);

		stockVertexAnalyzer.analyzeLine(stockDataList, drawDataList,
				strokeVertexList, StockData.VERTEX_TOP_STROKE,
				StockData.VERTEX_BOTTOM_STROKE);
		stockVertexAnalyzer.vertexListToDataList(stockDataList, strokeVertexList,
				strokeDataList, StockData.LEVEL_STROKE);

		stockVertexAnalyzer.analyzeLine(stockDataList, strokeDataList,
				segmentVertexList, StockData.VERTEX_TOP_SEGMENT,
				StockData.VERTEX_BOTTOM_SEGMENT);
		stockVertexAnalyzer.vertexListToDataList(stockDataList, segmentVertexList,
				segmentDataList, StockData.LEVEL_SEGMENT);

		if (segmentDataList.size() > Constants.OVERLAP_ANALYZE_THRESHOLD) {
			stockVertexAnalyzer.analyzeOverlap(stockDataList, segmentDataList,
					overlapList);
		} else if (strokeDataList.size() > Constants.OVERLAP_ANALYZE_THRESHOLD) {
			stockVertexAnalyzer.analyzeOverlap(stockDataList, strokeDataList,
					overlapList);
		} else {
			stockVertexAnalyzer.analyzeOverlap(stockDataList, drawDataList,
					overlapList);
		}

		//stockVertexAnalyzer.analyzeOverlap(stockDataList, segmentDataList, overlapList);
		//stockVertexAnalyzer.testShowVertextNumber(stockDataList, stockDataList);

		if (Preferences.getBoolean(mContext, Settings.KEY_DISPLAY_DIRECT,false)) {
			stockVertexAnalyzer.debugShow(stockDataList, stockDataList);
		}

		if (Preferences.getBoolean(mContext, Settings.KEY_DISPLAY_DRAW,false)) {
			stockVertexAnalyzer.debugShow(stockDataList, drawDataList);
		}

		if (Preferences.getBoolean(mContext, Settings.KEY_DISPLAY_STROKE,false)) {
			stockVertexAnalyzer.debugShow(stockDataList, strokeDataList);
		}

		if (Preferences.getBoolean(mContext, Settings.KEY_DISPLAY_SEGMENT,false)) {
			stockVertexAnalyzer.debugShow(stockDataList, segmentDataList);
		}

		stockVertexAnalyzer.analyzeDivergence(stock, stockDataList, segmentDataList);
		stockVertexAnalyzer.analyzeDivergence(stock, stockDataList, strokeDataList);
		stockVertexAnalyzer.analyzeDivergence(stock, stockDataList, drawDataList);

		stockVertexAnalyzer.analyzeDirection(stockDataList);

		analyzeAction(stock, period, stockDataList, drawVertexList, overlapList, drawDataList, strokeDataList, segmentDataList);
	}

	private String getFirstBottomAction(Stock stock, ArrayList<StockData> stockDataList, ArrayList<StockData> vertexList) {
		String result = "";
		StockData prev = null;
		StockData start = null;
		StockData end = null;
		int numerator = 0;
		int denominator = 0;

		if ((stockDataList == null)
				|| (stockDataList.size() < StockData.VERTEX_TYPING_SIZE)) {
			return result;
		}

		prev = stockDataList.get(stockDataList.size() - 2);
		if (prev == null) {
			return result;
		}

		if (!prev.vertexOf(StockData.VERTEX_BOTTOM)) {
			return result;
		}

		if ((vertexList == null)
				|| (vertexList.size() < StockData.VERTEX_TYPING_SIZE + 2)) {
			return result;
		}

		end = vertexList.get(vertexList.size() - 2);
		if (end == null) {
			return result;
		}

		if (end.vertexOf(StockData.VERTEX_BOTTOM)) {
			for (int i = vertexList.size() - 3; i >= 0; i--) {
				start = vertexList.get(i);
				if ((start != null) && (start.vertexOf(StockData.VERTEX_TOP))) {
					if ((stock.getPrice() > 0) && (start.getVertexHigh() > 0)) {
						numerator = (int)(100 * (stock.getPrice() - start.getVertexHigh())/start.getVertexHigh());
						denominator = (int)(100 * (end.getVertexLow() - start.getVertexHigh())/start.getVertexHigh());
					}
					break;
				}
			}

			if (Math.abs(denominator) >= Constants.FIRST_ACTION_THRESHOLD) {
				result += StockData.ACTION_BUY1;
				result += " " + numerator;
				result += "/" + denominator;
			}
		}

		return result;
	}

	private String getFirstTopAction(Stock stock, ArrayList<StockData> stockDataList, ArrayList<StockData> vertexList) {
		String result = "";
		StockData prev = null;
		StockData start = null;
		StockData end = null;
		int numerator = 0;
		int denominator = 0;

		if ((stockDataList == null)
				|| (stockDataList.size() < StockData.VERTEX_TYPING_SIZE)) {
			return result;
		}

		prev = stockDataList.get(stockDataList.size() - 2);
		if (prev == null) {
			return result;
		}

		if (!prev.vertexOf(StockData.VERTEX_TOP)) {
			return result;
		}

		if ((vertexList == null)
				|| (vertexList.size() < StockData.VERTEX_TYPING_SIZE + 2)) {
			return result;
		}

		end = vertexList.get(vertexList.size() - 2);
		if (end == null) {
			return result;
		}

		if (end.vertexOf(StockData.VERTEX_TOP)) {
			for (int i = vertexList.size() - 3; i >= 0; i--) {
				start = vertexList.get(i);
				if ((start != null) && (start.vertexOf(StockData.VERTEX_BOTTOM))) {
					if ((stock.getPrice() > 0) && (start.getVertexLow() > 0)) {
						numerator = (int)(100 * (stock.getPrice() - start.getVertexLow())/start.getVertexLow());
						denominator = (int)(100 * (end.getVertexHigh() - start.getVertexLow())/start.getVertexLow());
					}
					break;
				}
			}

			if (Math.abs(denominator) >= Constants.FIRST_ACTION_THRESHOLD) {
				result += StockData.ACTION_SELL1;
				result += " " + numerator;
				result += "/" + denominator;
			}
		}

		return result;
	}

    private String getSecondBottomAction(Stock stock, ArrayList<StockData> vertexList,
                                         ArrayList<StockData> strokeDataList,
                                         ArrayList<StockData> segmentDataList) {
        String result = "";
        StockData firstBottomVertex = null;
        StockData secondBottomVertex = null;
        StockData secondTopVertex = null;

        StockData baseStockData = null;
        StockData brokenStockData = null;
        int divergence = 0;
        int numerator = 0;
        int denominator = 0;

        if ((vertexList == null)
                || (vertexList.size() < 2 * StockData.VERTEX_TYPING_SIZE + 1)) {
            return result;
        }

        if ((strokeDataList == null) || (strokeDataList.size() < 2 * StockData.VERTEX_TYPING_SIZE)) {
            return result;
        }

        if ((segmentDataList == null) || (segmentDataList.size() < 2 * StockData.VERTEX_TYPING_SIZE)) {
            return result;
        }

        firstBottomVertex = vertexList.get(vertexList.size() - 4);
        if (firstBottomVertex == null) {
            return result;
        }

        secondTopVertex = vertexList.get(vertexList.size() - 3);
        if (secondTopVertex == null) {
            return result;
        }

        secondBottomVertex = vertexList.get(vertexList.size() - 2);
        if (secondBottomVertex == null) {
            return result;
        }

        if (firstBottomVertex.vertexOf(StockData.VERTEX_BOTTOM_SEGMENT)) {
            baseStockData = segmentDataList.get(segmentDataList.size() - 4);
            brokenStockData = segmentDataList.get(segmentDataList.size() - 2);
//		} else if (firstBottomVertex.vertexOf(StockData.VERTEX_BOTTOM_STROKE)) {
//			baseStockData = strokeDataList.get(strokeDataList.size() - 4);
//			brokenStockData = strokeDataList.get(strokeDataList.size() - 2);
        } else {
            return result;
        }

        if ((baseStockData == null) || (brokenStockData == null)) {
            return result;
        }

        if ((firstBottomVertex.getVertexLow() < secondBottomVertex.getVertexLow())
                && (secondBottomVertex.getVertexLow() < stock.getPrice())
                && (stock.getPrice() < secondTopVertex.getVertexHigh())
        ) {
            if ((stock.getPrice() > 0) && (brokenStockData.getVertexHigh() > 0)) {
                numerator = (int)(100 * (stock.getPrice() - brokenStockData.getVertexHigh())/brokenStockData.getVertexHigh());
                denominator = (int)(brokenStockData.getNet());
            }

			if (Math.abs(denominator) < Constants.SECEND_ACTION_THRESHOLD) {
				return result;
			}

			divergence = brokenStockData.divergenceTo(baseStockData);
			if (divergence == StockData.DIVERGENCE_UP) {
				result += StockData.ACTION_HIGH;
			} else if (divergence == StockData.DIVERGENCE_DOWN) {
				result += StockData.ACTION_LOW;
			}

            result += StockData.ACTION_BUY2;
            result += StockData.ACTION_BUY2;
            result += " " + numerator;
            result += "/" + denominator;
        }

        return result;
    }

    private String getSecondTopAction(Stock stock, ArrayList<StockData> vertexList,
                                      ArrayList<StockData> strokeDataList,
                                      ArrayList<StockData> segmentDataList) {
        String result = "";
        StockData firstTopVertex = null;
        StockData secondBottomVertex = null;
        StockData secondTopVertex = null;

        StockData baseStockData = null;
        StockData brokenStockData = null;
        int divergence = 0;
        int numerator = 0;
        int denominator = 0;

        if ((vertexList == null)
                || (vertexList.size() < 2 * StockData.VERTEX_TYPING_SIZE + 1)) {
            return result;
        }

        if ((strokeDataList == null) || (strokeDataList.size() < 2 * StockData.VERTEX_TYPING_SIZE)) {
            return result;
        }

        if ((segmentDataList == null) || (segmentDataList.size() < 2 * StockData.VERTEX_TYPING_SIZE)) {
            return result;
        }

        firstTopVertex = vertexList.get(vertexList.size() - 4);
        if (firstTopVertex == null) {
            return result;
        }

        secondBottomVertex = vertexList.get(vertexList.size() - 3);
        if (secondBottomVertex == null) {
            return result;
        }

        secondTopVertex = vertexList.get(vertexList.size() - 2);
        if (secondTopVertex == null) {
            return result;
        }

        if (firstTopVertex.vertexOf(StockData.VERTEX_TOP_SEGMENT)) {
            baseStockData = segmentDataList.get(segmentDataList.size() - 4);
            brokenStockData = segmentDataList.get(segmentDataList.size() - 2);
//		} else if (firstTopVertex.vertexOf(StockData.VERTEX_TOP_STROKE)) {
//			baseStockData = strokeDataList.get(strokeDataList.size() - 4);
//			brokenStockData = strokeDataList.get(strokeDataList.size() - 2);
        } else {
            return result;
        }

        if ((baseStockData == null) || (brokenStockData == null)) {
            return result;
        }

        if ((firstTopVertex.getVertexHigh() > secondTopVertex.getVertexHigh())
                && (secondTopVertex.getVertexHigh() > stock.getPrice())
                && (stock.getPrice() > secondBottomVertex.getVertexLow())
        ) {
            if ((stock.getPrice() > 0) && (brokenStockData.getVertexLow() > 0)) {
                numerator = (int)(100 * (stock.getPrice() - brokenStockData.getVertexLow())/brokenStockData.getVertexLow());
                denominator = (int)(brokenStockData.getNet());
            }

			if (Math.abs(denominator) < Constants.SECEND_ACTION_THRESHOLD) {
				return result;
			}

			divergence = brokenStockData.divergenceTo(baseStockData);
			if (divergence == StockData.DIVERGENCE_UP) {
				result += StockData.ACTION_HIGH;
			} else if (divergence == StockData.DIVERGENCE_DOWN) {
				result += StockData.ACTION_LOW;
			}

            result += StockData.ACTION_SELL2;
            result += StockData.ACTION_SELL2;
            result += " " + numerator;
            result += "/" + denominator;
        }

        return result;
    }

    private int getLastNet(ArrayList<StockData> stockDataList) {
		int result = 0;
		StockData stockData;

		if (stockDataList == null || stockDataList.size() == 0) {
			return result;
		}

		stockData = stockDataList.get(stockDataList.size() - 1);

		result = (int) stockData.getNet();

		return result;
	}

	private int getLastDivergence(ArrayList<StockData> stockDataList) {
		int result = 0;
		StockData stockData;

		if (stockDataList == null || stockDataList.size() == 0) {
			return result;
		}

		stockData = stockDataList.get(stockDataList.size() - 1);

		result = (int) stockData.getDivergence();

		return result;
	}

	private void analyzeAction(Stock stock, String period,
			ArrayList<StockData> stockDataList,
			ArrayList<StockData> drawVertexList,
			ArrayList<StockData> overlapList,
			ArrayList<StockData> drawDataList,
			ArrayList<StockData> strokeDataList,
			ArrayList<StockData> segmentDataList) {
		String action = StockData.ACTION_NONE;
		StockData prev = null;
		StockData stockData = null;
		int drawNet = 0;
		int strokeNet = 0;
		int segmentNet = 0;
		int drawDivergence = 0;
		int strokeDivergence = 0;
		int segmentDivergence = 0;

		if (stockDataList == null) {
			Log.d(TAG, "analyzeAction return" + " stockDataList = " + stockDataList);
			return;
		}

		if (stockDataList.size() < StockData.VERTEX_TYPING_SIZE) {
			return;
		}

		drawNet = getLastNet(drawDataList);
		strokeNet = getLastNet(strokeDataList);
		segmentNet = getLastNet(segmentDataList);

		drawDivergence = getLastDivergence(drawDataList);
		strokeDivergence = getLastDivergence(strokeDataList);
		segmentDivergence = getLastDivergence(segmentDataList);

		prev = stockDataList.get(stockDataList.size() - 2);
		stockData = stockDataList.get(stockDataList.size() - 1);

		if (stockData.directionOf(StockData.DIRECTION_UP_SEGMENT)) {
			if (prev.vertexOf(StockData.VERTEX_BOTTOM_SEGMENT)) {
				action += StockData.ACTION_D;
			} else {
				action += StockData.ACTION_ADD;
				action += Math.abs(segmentNet);
			}

			if (segmentDivergence == StockData.DIVERGENCE_UP) {
				action += StockData.ACTION_HIGH;
			}
		} else if (stockData
				.directionOf(StockData.DIRECTION_DOWN_SEGMENT)) {
			if (prev.vertexOf(StockData.VERTEX_TOP_SEGMENT)) {
				action += StockData.ACTION_G;
			} else {
				action += StockData.ACTION_MINUS;
				action += Math.abs(segmentNet);
			}

			if (segmentDivergence == StockData.DIVERGENCE_DOWN) {
				action += StockData.ACTION_LOW;
			}
		}

		if (stockData.directionOf(StockData.DIRECTION_UP_STROKE)) {
			if (prev.vertexOf(StockData.VERTEX_BOTTOM_STROKE)) {
				action += StockData.ACTION_D;
			} else {
				action += StockData.ACTION_ADD;
				action += Math.abs(strokeNet);
			}

			if (strokeDivergence == StockData.DIVERGENCE_UP) {
				action += StockData.ACTION_HIGH;
			}
		} else if (stockData.directionOf(StockData.DIRECTION_DOWN_STROKE)) {
			if (prev.vertexOf(StockData.VERTEX_TOP_STROKE)) {
				action += StockData.ACTION_G;
			} else {
				action += StockData.ACTION_MINUS;
				action += Math.abs(strokeNet);
			}

			if (strokeDivergence == StockData.DIVERGENCE_DOWN) {
				action += StockData.ACTION_LOW;
			}
		}

		if (stockData.directionOf(StockData.DIRECTION_UP)) {
			if (prev.vertexOf(StockData.VERTEX_BOTTOM)) {
				action += StockData.ACTION_D;
//				if (period.equals(Settings.KEY_PERIOD_DAY)) {
//					String result1 = getFirstBottomAction(stock, stockDataList, drawVertexList);
//					if (!TextUtils.isEmpty(result1)) {
//						action += result1;
//					}
//				}

				String result2 = getSecondBottomAction(stock, drawVertexList, strokeDataList, segmentDataList);
				if (!TextUtils.isEmpty(result2)) {
					action = result2;
				}
			} else {
				action += StockData.ACTION_ADD;
				action += Math.abs(drawNet);
			}

			if (drawDivergence == StockData.DIVERGENCE_UP) {
				action += StockData.ACTION_HIGH;
			}
		} else if (stockData.directionOf(StockData.DIRECTION_DOWN)) {
			if (prev.vertexOf(StockData.VERTEX_TOP)) {
				action += StockData.ACTION_G;
//				if (period.equals(Settings.KEY_PERIOD_DAY)) {
//					String result1 = getFirstTopAction(stock, stockDataList, drawVertexList);
//					if (!TextUtils.isEmpty(result1)) {
//						action += result1;
//					}
//				}

				String result2 = getSecondTopAction(stock, drawVertexList, strokeDataList, segmentDataList);
				if (!TextUtils.isEmpty(result2)) {
					action = result2;
				}
			} else {
				action += StockData.ACTION_MINUS;
				action += Math.abs(drawNet);
			}

			if (drawDivergence == StockData.DIVERGENCE_DOWN) {
				action += StockData.ACTION_LOW;
			}
		}

		stock.setDateTime(stockData.getDate(), stockData.getTime());
		stock.setAction(period, action + stockData.getAction());
	}

	private void updateDatabase(Stock stock) {
		StockTrends stockTrends = new StockTrends();

		if (mStockDatabaseManager == null) {
			Log.d(TAG, "updateDatabase return " + " mStockDatabaseManager = "
					+ mStockDatabaseManager);
			return;
		}

		try {
			mStockDatabaseManager.updateStock(stock,
					stock.getContentValues());

			stockTrends.set(stock);

			if (mStockDatabaseManager.isStockTrendsExist(stockTrends)) {
				StockTrends temp = new StockTrends();

				temp.setStockId(stock.getId());
				temp.setDate(stock.getDate());
				temp.setTime(stock.getTime());

				mStockDatabaseManager.getStockTrends(temp);

				if (!stockTrends.getTrends().equals(temp.getTrends())) {
					mStockDatabaseManager.insertStockTrends(stockTrends);
				}
            } else {
                mStockDatabaseManager.insertStockTrends(stockTrends);
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateDatabase(ArrayList<StockData> stockDataList) {
		if ((stockDataList == null) || (stockDataList.size() == 0)) {
			return;
		}

		ContentValues contentValues[] = new ContentValues[stockDataList.size()];

		for (int i = 0; i < stockDataList.size(); i++) {
			StockData stockData = stockDataList.get(i);
			contentValues[i] = stockData.getContentValues();
		}

		mStockDatabaseManager.bulkInsertStockData(contentValues);
	}

	void updateDatabase(Stock stock, String period, ArrayList<StockData> stockDataList) {
		if (mStockDatabaseManager == null) {
			Log.d(TAG, "updateDatabase return " + " mStockDatabaseManager = "
					+ mStockDatabaseManager);
			return;
		}

		try {
			mStockDatabaseManager.deleteStockData(stock.getId(), period);

			updateDatabase(stockDataList);

			stock.setModified(Utility.getCurrentDateTimeString());
			mStockDatabaseManager.updateStock(stock,
					stock.getContentValues());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateDatabase(Stock stock, String period, ArrayList<StockData> stockDataList,
						ArrayList<StockData> drawDataList,
						ArrayList<StockData> strokeDataList,
						ArrayList<StockData> segmentDataList) {
		if (mStockDatabaseManager == null) {
			Log.d(TAG, "updateDatabase return " + " mStockDatabaseManager = "
					+ mStockDatabaseManager);
			return;
		}

		try {
			mStockDatabaseManager.deleteStockData(stock.getId(), period);

			updateDatabase(stockDataList);

//			updateDatabase(drawDataList);
//			updateDatabase(strokeDataList);
//			updateDatabase(segmentDataList);

			stock.setModified(Utility.getCurrentDateTimeString());
			mStockDatabaseManager.updateStock(stock,
					stock.getContentValues());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateNetArrayMap(ArrayMap<Integer, Integer> netArrayMap, ArrayList<StockData> stockDataList) {
		int net = 0;

		if ((netArrayMap == null) || (stockDataList == null)) {
			return;
		}

		net = getLastNet(stockDataList);

		if (netArrayMap.containsKey(net)) {
			int value = netArrayMap.get(net);
			netArrayMap.put(net, ++value);
		} else {
			netArrayMap.put(net, 1);
		}
	}

	private void updateDivergenceArrayMap(ArrayMap<Integer, Integer> divergenceArrayMap, ArrayList<StockData> stockDataList) {
		int divergence = 0;

		if ((divergenceArrayMap == null) || (stockDataList == null)) {
			return;
		}

		divergence = getLastDivergence(stockDataList);

		if (divergence == StockData.DIVERGENCE_NONE) {
			return;
		}

		if (divergenceArrayMap.containsKey(divergence)) {
			int value = divergenceArrayMap.get(divergence);
			divergenceArrayMap.put(divergence, ++value);
		} else {
			divergenceArrayMap.put(divergence, 1);
		}
	}

	int getDenominator(String action) {
		String strings[] = null;
		int result = 0;

		try {
			if (TextUtils.isEmpty(action)) {
				return result;
			}

			strings = action.split("/");

			if ((strings == null) | (strings.length < 2)) {
				return result;
			}

			result = Integer.valueOf(strings[1]);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return result;
		}
	}

	private void updateNotification(Stock stock) {
        ArrayList<StockDeal> stockDealList = new ArrayList<StockDeal>();
		StringBuilder actionString = new StringBuilder();
		StringBuilder contentTitle = new StringBuilder();

		if (stock == null) {
			return;
		}

		if (stock.getPrice() == 0) {
			return;
		}

		for (String period : Settings.KEY_PERIODS) {
			if (Preferences.getBoolean(mContext, period, false)) {
				String action = stock.getAction(period);
				if (action.contains(StockData.ACTION_BUY2 + StockData.ACTION_BUY2)) {
					actionString.append(period + " " + action + " ");
				}

				if (action.contains(StockData.ACTION_SELL2 + StockData.ACTION_SELL2)) {
					if (Stock.CLASS_INDEX.equals(stock.getClases())) {
						actionString.append(period + " " + action + " ");
					} else {
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
		}

		if (TextUtils.isEmpty(actionString)) {
			return;
		}

		contentTitle.append(stock.getName() + " " + stock.getPrice() + " "
				+ stock.getNet() + " " + stock.getOperate() + " " + actionString);

		RecordFile.writeNotificationFile(contentTitle.toString());

		if (!Preferences.getBoolean(mContext, Settings.KEY_NOTIFICATION_MESSAGE,
				true)) {
			return;
		}

		if (Preferences.getBoolean(mContext, Settings.KEY_NOTIFICATION_OPERATE,
				false)) {
			if (TextUtils.isEmpty(stock.getOperate())) {
				return;
			}

			if (stock.getOperate().contains(StockData.ACTION_HIGH) && actionString.toString().contains(StockData.ACTION_BUY)) {
				return;
			}

			if (stock.getOperate().contains(StockData.ACTION_LOW) && actionString.toString().contains(StockData.ACTION_SELL)) {
				return;
			}
		}

		notify((int) stock.getId(), Constants.MESSAGE_CHANNEL_ID, Constants.MESSAGE_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH,
				contentTitle.toString(), "");
	}

	void notify(int id, String channelID, String channelName, int importance, String contentTitle, String contentText) {
		Notification.Builder notification;

		if (mNotificationManager == null) {
			return;
		}

		mNotificationManager.cancel(id);

		Intent intent = new Intent(mContext, StockListActivity.class);
		intent.setType("vnd.android-dir/mms-sms");
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
				intent, 0);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel notificationChannel = new NotificationChannel(channelID,
					channelName, importance);
			notificationChannel.enableVibration(true);
			notificationChannel.setVibrationPattern(new long[]{500, 500, 500, 500, 500});
			notificationChannel.enableLights(true);
			notificationChannel.setLightColor(0xFF0000FF);
			mNotificationManager.createNotificationChannel(notificationChannel);

			notification = new Notification.Builder(
					mContext, channelID).setContentTitle(contentTitle)
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
