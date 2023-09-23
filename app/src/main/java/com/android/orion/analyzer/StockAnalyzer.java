package com.android.orion.analyzer;

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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.orion.setting.Constant;
import com.android.orion.R;
import com.android.orion.setting.Setting;
import com.android.orion.activity.StockListActivity;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDatabaseManager;
import com.android.orion.database.StockDeal;
import com.android.orion.database.StockFinancial;
import com.android.orion.database.TotalShare;
import com.android.orion.indicator.MACD;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.RecordFile;
import com.android.orion.utility.StopWatch;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.Calendar;


public class StockAnalyzer {
	static final String TAG = Constant.TAG + " "
			+ StockAnalyzer.class.getSimpleName();

	public Context mContext;

	PowerManager mPowerManager;
	WakeLock mWakeLock;

	public LocalBroadcastManager mLocalBroadcastManager;
	NotificationManager mNotificationManager;
	public StockDatabaseManager mStockDatabaseManager;

	public StockAnalyzer(@NonNull Context context) {
		mContext = context;

		mPowerManager = (PowerManager) mContext
				.getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				Constant.TAG + ":" + StockAnalyzer.class.getSimpleName());

		mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mStockDatabaseManager = StockDatabaseManager.getInstance(mContext);
	}

	public void acquireWakeLock() {
		Log.d(TAG, "acquireWakeLock");

		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire(10*60*1000L /*10 minutes*/);
			Log.d(TAG, "acquireWakeLock, mWakeLock acquired.");
		}
	}

	public void releaseWakeLock() {
		Log.d(TAG, "releaseWakeLock");
		if (mWakeLock.isHeld()) {
			mWakeLock.release();
			Log.d(TAG, "releaseWakeLock, mWakeLock released.");
		}
	}

	public void loadStockDataList(Stock stock, String period,
			ArrayList<StockData> stockDataList) {
		boolean loopback = false;
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

		loopback = Preferences.getBoolean(mContext, Setting.KEY_LOOPBACK,
				false);
		if (loopback) {
			String dateTime = Preferences.getString(mContext, Setting.KEY_LOOPBACK_DATE_TIME, "");
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

					if (loopback) {
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

	public void analyze(Stock stock, String period) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		ArrayList<StockData> stockDataList = null;
		ArrayList<StockData> drawVertexList =  null;
		ArrayList<StockData> drawDataList =  null;
		ArrayList<StockData> strokeVertexList =  null;
		ArrayList<StockData> strokeDataList =  null;
		ArrayList<StockData> segmentVertexList =  null;
		ArrayList<StockData> segmentDataList =  null;
		ArrayList<StockData> lineVertexList =  null;
		ArrayList<StockData> lineDataList =  null;
		ArrayList<StockData> outlineVertexList =  null;
		ArrayList<StockData> outlineDataList =  null;

		if (stock == null) {
			return;
		}

		stockDataList = stock.getStockDataList(period);
		drawVertexList = stock.getDrawVertexList(period);
		drawDataList = stock.getDrawDataList(period);
		strokeVertexList = stock.getStrokeVertexList(period);
		strokeDataList = stock.getStrokeDataList(period);
		segmentVertexList = stock.getSegmentVertexList(period);
		segmentDataList = stock.getSegmentDataList(period);
		lineVertexList = stock.getLineVertexList(period);
		lineDataList = stock.getLineDataList(period);
		outlineVertexList = stock.getOutlineVertexList(period);
		outlineDataList = stock.getOutlineDataList(period);

		try {
			setupStockShareBonus(stock);
			setupStockFinancial(stock);

			loadStockDataList(stock, period, stockDataList);
			analyzeStockData(stock, period, stockDataList,
					drawVertexList, drawDataList,
					strokeVertexList, strokeDataList,
					segmentVertexList, segmentDataList,
					lineVertexList, lineDataList,
					outlineVertexList, outlineDataList);
			updateDatabase(stock, period, stockDataList,
					drawDataList, strokeDataList, segmentDataList);
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(TAG, "analyze:" + stock.getName() + " " + period + " "
				+ stopWatch.getInterval() + "s");
	}

	public void analyze(Stock stock) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		if (stock == null) {
			return;
		}

		try {
            if (!Stock.CLASS_INDEX.equals(stock.getClasses())) {
                analyzeStockFinancial(stock);
                setupStockFinancial(stock);
                setupStockShareBonus(stock);
            }

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

		if (Stock.CLASS_INDEX.equals(stock.getClasses())) {
		    return;
        }

		mStockDatabaseManager.getStockFinancialList(stock, stockFinancialList,
				sortOrder);
		mStockDatabaseManager.getTotalShareList(stock, totalShareList,
				sortOrder);
		mStockDatabaseManager.getShareBonusList(stock, shareBonusList,
				sortOrder);
		mStockDatabaseManager.getStockDataList(stock, Setting.KEY_PERIOD_MONTH,
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
					/ prev.getNetProfitPerShareInYear(),
					Constant.DOUBLE_FIXED_DECIMAL);

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
							/ prev.getBookValuePerShare(),
					Constant.DOUBLE_FIXED_DECIMAL);
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
									/ price, Constant.DOUBLE_FIXED_DECIMAL);

					if (stockFinancial.getBookValuePerShare() != 0) {
						pb = Utility.Round(
								price / stockFinancial.getBookValuePerShare(),
								Constant.DOUBLE_FIXED_DECIMAL);
					}

					roi = Utility.Round(stockFinancial.getRoe() * pe
							* Constant.ROI_COEFFICIENT,
							Constant.DOUBLE_FIXED_DECIMAL);
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

        if (Stock.CLASS_INDEX.equals(stock.getClasses())) {
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
		stock.setupDebtToNetAssetsRatio(stockFinancialList);
		stock.setupRoe(stockFinancialList);
		stock.setupPe();
		stock.setupPb();
		stock.setupRoi();
	}

	private void setupStockShareBonus(Stock stock) {
		double totalDivident = 0;

		String yearString = "";
		String prevYearString = "";
		String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";
		ArrayList<ShareBonus> shareBonusList = new ArrayList<ShareBonus>();

        if (Stock.CLASS_INDEX.equals(stock.getClasses())) {
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
					Constant.DOUBLE_FIXED_DECIMAL));
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
						  ArrayList<StockData> segmentVertexList, ArrayList<StockData> segmentDataList,
						  ArrayList<StockData> lineVertexList, ArrayList<StockData> lineDataList,
						  ArrayList<StockData> outlineVertexList, ArrayList<StockData> outlineDataList) {
        StockKeyAnalyzer stockKeyAnalyzer = new StockKeyAnalyzer();
		StockVertexAnalyzer stockVertexAnalyzer = new StockVertexAnalyzer();
		StockQuantAnalyzer stockQuantAnalyzer = new StockQuantAnalyzer();
		ArrayList<StockData> overlapList = new ArrayList<StockData>();
		ArrayList<ShareBonus> shareBonusList = new ArrayList<ShareBonus>();

		stockKeyAnalyzer.analyze(stock, period, stockDataList);

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

		stockVertexAnalyzer.analyzeLine(stockDataList, segmentDataList,
				lineVertexList, StockData.VERTEX_TOP_LINE,
				StockData.VERTEX_BOTTOM_LINE);
		stockVertexAnalyzer.vertexListToDataList(stockDataList, lineVertexList,
				lineDataList, StockData.LEVEL_LINE);

		stockVertexAnalyzer.analyzeLine(stockDataList, lineDataList,
				outlineVertexList, StockData.VERTEX_TOP_OUTLINE,
				StockData.VERTEX_BOTTOM_OUTLINE);
		stockVertexAnalyzer.vertexListToDataList(stockDataList, outlineVertexList,
				outlineDataList, StockData.LEVEL_OUTLINE);

		if (segmentDataList.size() > StockData.OVERLAP_TYPING_SIZE) {
			stockVertexAnalyzer.analyzeOverlap(stockDataList, segmentDataList,
					overlapList);
		} else if (strokeDataList.size() > StockData.OVERLAP_TYPING_SIZE) {
			stockVertexAnalyzer.analyzeOverlap(stockDataList, strokeDataList,
					overlapList);
		} else {
			stockVertexAnalyzer.analyzeOverlap(stockDataList, drawDataList,
					overlapList);
		}

		//stockVertexAnalyzer.analyzeOverlap(stockDataList, segmentDataList, overlapList);
		//stockVertexAnalyzer.testShowVertextNumber(stockDataList, stockDataList);

		if (Preferences.getBoolean(mContext, Setting.KEY_LOOPBACK,false)) {
			if (Preferences.getBoolean(mContext, Setting.KEY_DISPLAY_DIRECT,false)) {
				stockVertexAnalyzer.debugShow(stockDataList, stockDataList);
			}

			if (Preferences.getBoolean(mContext, Setting.KEY_DISPLAY_DRAW,false)) {
				stockVertexAnalyzer.debugShow(stockDataList, drawDataList);
			}

			if (Preferences.getBoolean(mContext, Setting.KEY_DISPLAY_STROKE,false)) {
				stockVertexAnalyzer.debugShow(stockDataList, strokeDataList);
			}

			if (Preferences.getBoolean(mContext, Setting.KEY_DISPLAY_SEGMENT,false)) {
				stockVertexAnalyzer.debugShow(stockDataList, segmentDataList);
			}

            if (Preferences.getBoolean(mContext, Setting.KEY_DISPLAY_LINE,false)) {
                stockVertexAnalyzer.debugShow(stockDataList, lineDataList);
            }
		}

		stockVertexAnalyzer.analyzeDivergence(stock, stockDataList, segmentDataList);
		stockVertexAnalyzer.analyzeDivergence(stock, stockDataList, strokeDataList);
		stockVertexAnalyzer.analyzeDivergence(stock, stockDataList, drawDataList);

		stockVertexAnalyzer.analyzeDirection(stockDataList);

		analyzeAction(stock, period, stockDataList, drawVertexList, overlapList, drawDataList, strokeDataList, segmentDataList);

		mStockDatabaseManager.getShareBonusList(stock, shareBonusList,
				DatabaseContract.COLUMN_DATE + " DESC ");

		if (period.equals(Setting.KEY_PERIOD_MIN60)) {
			stockQuantAnalyzer.analyze(mContext, stock, stockDataList, shareBonusList);
		}
	}

	private String getSecondBottomAction(Stock stock, ArrayList<StockData> vertexList,
										 ArrayList<StockData> strokeDataList,
										 ArrayList<StockData> segmentDataList) {
		String result = "";
		StockData firstBottomVertex = null;
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

		firstBottomVertex = vertexList.get(vertexList.size() - 4);
		if (firstBottomVertex == null) {
			return result;
		}

		firstTopVertex = vertexList.get(vertexList.size() - 3);
		if (firstTopVertex == null) {
			return result;
		}

		secondBottomVertex = vertexList.get(vertexList.size() - 2);
		if (secondBottomVertex == null) {
			return result;
		}

		secondTopVertex = vertexList.get(vertexList.size() - 1);
		if (secondTopVertex == null) {
			return result;
		}

		if (firstBottomVertex.vertexOf(StockData.VERTEX_BOTTOM_SEGMENT)) {
			baseStockData = segmentDataList.get(segmentDataList.size() - 4);
			brokenStockData = segmentDataList.get(segmentDataList.size() - 2);
		} else {
			return result;
		}

		if ((baseStockData == null) || (brokenStockData == null)) {
			return result;
		}

		if ((firstBottomVertex.getVertexLow() < secondBottomVertex.getVertexLow())
				&& (secondBottomVertex.getVertexLow() < stock.getPrice())
				&& (stock.getPrice() < firstTopVertex.getVertexHigh())
		) {
			if ((stock.getPrice() > 0) && (brokenStockData.getVertexHigh() > 0)) {
				numerator = (int)(100 * (stock.getPrice() - brokenStockData.getVertexHigh())/brokenStockData.getVertexHigh());
				denominator = (int)(brokenStockData.getNet());
			}

			divergence = brokenStockData.divergenceTo(baseStockData);
			if (divergence == StockData.DIVERGENCE_UP) {
//				result += StockData.ACTION_HIGH;
			} else if (divergence == StockData.DIVERGENCE_DOWN) {
//				result += StockData.ACTION_LOW;
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
		StockData firstBottomVertex = null;
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

		firstBottomVertex = vertexList.get(vertexList.size() - 3);
		if (firstBottomVertex == null) {
			return result;
		}

		secondTopVertex = vertexList.get(vertexList.size() - 2);
		if (secondTopVertex == null) {
			return result;
		}

		secondBottomVertex = vertexList.get(vertexList.size() - 1);
		if (secondBottomVertex == null) {
			return result;
		}

		if (firstTopVertex.vertexOf(StockData.VERTEX_TOP_SEGMENT)) {
			baseStockData = segmentDataList.get(segmentDataList.size() - 4);
			brokenStockData = segmentDataList.get(segmentDataList.size() - 2);
		} else {
			return result;
		}

		if ((baseStockData == null) || (brokenStockData == null)) {
			return result;
		}

		if ((firstTopVertex.getVertexHigh() > secondTopVertex.getVertexHigh())
				&& (secondTopVertex.getVertexHigh() > stock.getPrice())
				&& (stock.getPrice() > firstBottomVertex.getVertexLow())
		) {
			if ((stock.getPrice() > 0) && (brokenStockData.getVertexLow() > 0)) {
				numerator = (int)(100 * (stock.getPrice() - brokenStockData.getVertexLow())/brokenStockData.getVertexLow());
				denominator = (int)(brokenStockData.getNet());
			}

			divergence = brokenStockData.divergenceTo(baseStockData);
			if (divergence == StockData.DIVERGENCE_UP) {
//				result += StockData.ACTION_HIGH;
			} else if (divergence == StockData.DIVERGENCE_DOWN) {
//				result += StockData.ACTION_LOW;
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
//				action += StockData.ACTION_D;
			} else {
				if (segmentNet >= 0) {
					action += StockData.ACTION_ADD;
				}
				action += segmentNet;
			}

			if (segmentDivergence == StockData.DIVERGENCE_UP) {
//				action += StockData.ACTION_HIGH;
			}
		} else if (stockData
				.directionOf(StockData.DIRECTION_DOWN_SEGMENT)) {
			if (prev.vertexOf(StockData.VERTEX_TOP_SEGMENT)) {
//				action += StockData.ACTION_G;
			} else {
				if (segmentNet >= 0) {
					action += StockData.ACTION_ADD;
				}
				action += segmentNet;
			}

			if (segmentDivergence == StockData.DIVERGENCE_DOWN) {
//				action += StockData.ACTION_LOW;
			}
		}

		if (stockData.directionOf(StockData.DIRECTION_UP_STROKE)) {
			if (prev.vertexOf(StockData.VERTEX_BOTTOM_STROKE)) {
//				action += StockData.ACTION_D;
			} else {
//				if (strokeNet >= 0) {
//					action += StockData.ACTION_ADD;
//				}
//				action += strokeNet;
			}

			if (strokeDivergence == StockData.DIVERGENCE_UP) {
//				action += StockData.ACTION_HIGH;
			}
		} else if (stockData.directionOf(StockData.DIRECTION_DOWN_STROKE)) {
			if (prev.vertexOf(StockData.VERTEX_TOP_STROKE)) {
//				action += StockData.ACTION_G;
			} else {
//				if (strokeNet >= 0) {
//					action += StockData.ACTION_ADD;
//				}
//				action += strokeNet;
			}

			if (strokeDivergence == StockData.DIVERGENCE_DOWN) {
//				action += StockData.ACTION_LOW;
			}
		}

		if (stockData.directionOf(StockData.DIRECTION_UP)) {
			if (prev.vertexOf(StockData.VERTEX_BOTTOM)) {
				String result2 = getSecondBottomAction(stock, drawVertexList, strokeDataList, segmentDataList);
				if (!TextUtils.isEmpty(result2)) {
					action = result2;
				}
			} else {
//				if (drawNet >= 0) {
//					action += StockData.ACTION_ADD;
//				}
//				action += drawNet;
			}

			if (drawDivergence == StockData.DIVERGENCE_UP) {
//				action += StockData.ACTION_HIGH;
			}
		} else if (stockData.directionOf(StockData.DIRECTION_DOWN)) {
			if (prev.vertexOf(StockData.VERTEX_TOP)) {
				String result2 = getSecondTopAction(stock, drawVertexList, strokeDataList, segmentDataList);
				if (!TextUtils.isEmpty(result2)) {
					action = result2;
				}
			} else {
//				if (drawNet >= 0) {
//					action += StockData.ACTION_ADD;
//				}
//				action += drawNet;
			}

			if (drawDivergence == StockData.DIVERGENCE_DOWN) {
//				action += StockData.ACTION_LOW;
			}
		}

		if (!period.equals(Setting.KEY_PERIOD_MONTH) && !period.equals(Setting.KEY_PERIOD_WEEK)) {
			if (stockData.getNaturalRally() > 0) {
				action += StockData.ACTION_NATURAL_RALLY;
			}

			if (stockData.getUpwardTrend() > 0) {
				action += StockData.ACTION_UPWARD_TREND;
			}

			if (stockData.getDownwardTrend() > 0) {
				action += StockData.ACTION_DOWNWARD_TREND;
			}

			if (stockData.getNaturalReaction() > 0) {
				action += StockData.ACTION_NATURAL_REACTION;
			}
		}

		stock.setDateTime(stockData.getDate(), stockData.getTime());
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
					stock.getContentValues());
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

	public void updateDatabase(Stock stock, String period, ArrayList<StockData> stockDataList) {
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

	double getToBuyProfit(Stock stock) {
		ArrayList<StockDeal> stockDealList = new ArrayList<StockDeal>();
		double result = 0;

		if (stock == null) {
			return result;
		}

		mStockDatabaseManager.getStockDealListToBuy(stock, stockDealList);
		for (StockDeal stockDeal : stockDealList) {
			result += stockDeal.getProfit();
		}

		return result;
	}

	double getToSellProfit(Stock stock) {
		ArrayList<StockDeal> stockDealList = new ArrayList<StockDeal>();
		double result = 0;

		if (stock == null) {
			return result;
		}

		mStockDatabaseManager.getStockDealListToSell(stock, stockDealList);
		for (StockDeal stockDeal : stockDealList) {
			result += stockDeal.getProfit();
		}

		return result;
	}

	private void updateNotification(Stock stock) {
		boolean notifyToBuy1;
		boolean notifyToSell1;
		boolean notifyToBuy2;
		boolean notifyToSell2;
	    double toBuyProfit = 0;
		double toSellProfit = 0;
		StringBuilder actionString = new StringBuilder();
		StringBuilder contentTitle = new StringBuilder();

		if (stock == null) {
			return;
		}

		if (stock.getPrice() == 0) {
			return;
		}

		if (TextUtils.isEmpty(stock.getOperate())) {
			return;
		}

		toBuyProfit = getToBuyProfit(stock);
		toSellProfit = getToSellProfit(stock);

		notifyToBuy1 = true;
		notifyToSell1 = true;

		for (String period : Setting.KEY_PERIODS) {
			if (Preferences.getBoolean(mContext, period, false)) {
				String action = stock.getAction(period);

				notifyToBuy2 = false;
				notifyToSell2 = false;

				if (Preferences.getBoolean(mContext, Setting.KEY_NOTIFICATION_OPERATE,
						true)) {
					if (action.contains(StockData.ACTION_BUY2 + StockData.ACTION_BUY2)) {
						notifyToBuy2 = true;
					} else if (!action.contains(StockData.ACTION_D)) {
						notifyToBuy1 &= false;
					}

					if (action.contains(StockData.ACTION_SELL2 + StockData.ACTION_SELL2)) {
						notifyToSell2 = true;
					} else if (!action.contains(StockData.ACTION_G)) {
						notifyToSell1 &= false;
					}

					if (notifyToBuy2 || notifyToSell2) {
						actionString.append(period + " " + action + " ");
					}
				}
			}
		}

		if (notifyToBuy1) {
			if (toBuyProfit > 0) {
				actionString.append(StockData.ACTION_D + " " + (int)toBuyProfit + " ");
			} else {
				actionString.append(StockData.ACTION_D + " ");
			}
		}

		if (notifyToSell1) {
			if (toSellProfit > 0) {
				actionString.append(StockData.ACTION_G + " " + (int)toSellProfit + " ");
			} else {
				actionString.append(StockData.ACTION_G + " ");
			}
		}

		if (TextUtils.isEmpty(actionString)) {
			return;
		}

		contentTitle.append(stock.getName() + " " + stock.getPrice() + " "
				+ stock.getNet() + " " + actionString);

		RecordFile.writeNotificationFile(contentTitle.toString());

		notify((int) stock.getId(), Constant.MESSAGE_CHANNEL_ID, Constant.MESSAGE_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH,
				contentTitle.toString(), "");
	}

	public void notify(int id, String channelID, String channelName, int importance, String contentTitle, String contentText) {
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
