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
import java.util.List;


public class StockAnalyzer {
	static ArrayList<StockFinancial> mStockFinancialList = new ArrayList<>();
	static ArrayList<TotalShare> mTotalShareList = new ArrayList<>();
	static ArrayList<ShareBonus> mShareBonusList = new ArrayList<>();
	static ArrayList<StockData> mStockDataList;
	static ArrayList<StockData> mDrawVertexList;
	static ArrayList<StockData> mDrawDataList;
	static ArrayList<StockData> mStrokeVertexList;
	static ArrayList<StockData> mStrokeDataList;
	static ArrayList<StockData> mSegmentVertexList;
	static ArrayList<StockData> mSegmentDataList;
	static ArrayList<StockData> mLineVertexList;
	static ArrayList<StockData> mLineDataList;
	static ArrayList<StockData> mOutlineVertexList;
	static ArrayList<StockData> mOutlineDataList;
	static StringBuffer mContentTitle = new StringBuffer();
	static StringBuffer mContentText = new StringBuffer();

	Context mContext;
	NotificationManager mNotificationManager;
	DatabaseManager mDatabaseManager;
	Logger Log = Logger.getLogger();

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

		mStockDataList = stock.getArrayList(period, Period.TYPE_STOCK_DATA);
		mDrawVertexList = stock.getArrayList(period, Period.TYPE_DRAW_VERTEX);
		mDrawDataList = stock.getArrayList(period, Period.TYPE_DRAW_DATA);
		mStrokeVertexList = stock.getArrayList(period, Period.TYPE_STROKE_VERTEX);
		mStrokeDataList = stock.getArrayList(period, Period.TYPE_STROKE_DATA);
		mSegmentVertexList = stock.getArrayList(period, Period.TYPE_SEGMENT_VERTEX);
		mSegmentDataList = stock.getArrayList(period, Period.TYPE_SEGMENT_DATA);
		mLineVertexList = stock.getArrayList(period, Period.TYPE_LINE_VERTEX);
		mLineDataList = stock.getArrayList(period, Period.TYPE_LINE_DATA);
		mOutlineVertexList = stock.getArrayList(period, Period.TYPE_OUTLINE_VERTEX);
		mOutlineDataList = stock.getArrayList(period, Period.TYPE_OUTLINE_DATA);

		try {
			mDatabaseManager.loadStockDataList(stock, period, mStockDataList);
			analyzeMacd(period);
			analyzeStockData(stock, period);
			mDatabaseManager.updateStockData(stock, period, mStockDataList);
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

		mStockDataList = stock.getArrayList(Period.MONTH, Period.TYPE_STOCK_DATA);
		mDatabaseManager.getStockFinancialList(stock, mStockFinancialList,
				sortOrder);
		mDatabaseManager.getTotalShareList(stock, mTotalShareList,
				sortOrder);
		mDatabaseManager.getShareBonusList(stock, mShareBonusList,
				sortOrder);
		mDatabaseManager.getStockDataList(stock, DatabaseContract.COLUMN_MONTH,
				mStockDataList, sortOrder);

		setupTotalShare(mTotalShareList);
		setupNetProfitPerShareInYear();
		setupNetProfitPerShare();
		setupRate();
		setupRoe();
		setupRoi();

		mDatabaseManager.updateStockFinancial(stock, mStockFinancialList);
		mDatabaseManager.updateStockData(stock, DatabaseContract.COLUMN_MONTH, mStockDataList);
	}

	private void setupTotalShare(ArrayList<TotalShare> totalShareList) {
		int j = 0;
		for (StockFinancial stockFinancial : mStockFinancialList) {
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

	private void setupNetProfitPerShare() {
		for (StockFinancial stockFinancial : mStockFinancialList) {
			stockFinancial.setupNetProfitMargin();
			stockFinancial.setupNetProfitPerShare();
		}
	}

	private void setupNetProfitPerShareInYear() {
		double mainBusinessIncome = 0;
		double mainBusinessIncomeInYear = 0;
		double netProfit = 0;
		double netProfitInYear = 0;
		double netProfitPerShareInYear = 0;
		double netProfitPerShare = 0;

		if (mStockFinancialList.size() < Constant.SEASONS_IN_A_YEAR + 1) {
			return;
		}

		for (int i = 0; i < mStockFinancialList.size()
				- Constant.SEASONS_IN_A_YEAR; i++) {
			mainBusinessIncomeInYear = 0;
			netProfitInYear = 0;
			netProfitPerShareInYear = 0;
			for (int j = 0; j < Constant.SEASONS_IN_A_YEAR; j++) {
				StockFinancial current = mStockFinancialList.get(i + j);
				StockFinancial prev = mStockFinancialList.get(i + j + 1);

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

			StockFinancial stockFinancial = mStockFinancialList.get(i);
			stockFinancial.setMainBusinessIncomeInYear(mainBusinessIncomeInYear);
			stockFinancial.setNetProfitInYear(netProfitInYear);
			stockFinancial.setNetProfitPerShareInYear(netProfitPerShareInYear);
		}
	}

	private void setupRate() {
		double rate = 0;

		if (mStockFinancialList.size() < Constant.SEASONS_IN_A_YEAR + 1) {
			return;
		}

		for (int i = 0; i < mStockFinancialList.size()
				- Constant.SEASONS_IN_A_YEAR; i++) {
			StockFinancial stockFinancial = mStockFinancialList.get(i);
			StockFinancial prev = mStockFinancialList.get(i
					+ Constant.SEASONS_IN_A_YEAR);

			if (prev == null || prev.getNetProfitPerShareInYear() == 0) {
				continue;
			}

			rate = Utility.Round(stockFinancial.getNetProfitPerShareInYear()
					/ prev.getNetProfitPerShareInYear());

			stockFinancial.setRate(rate);
		}
	}

	private void setupRoe() {
		double roe = 0;

		if (mStockFinancialList.size() < Constant.SEASONS_IN_A_YEAR + 1) {
			return;
		}

		for (int i = 0; i < mStockFinancialList.size()
				- Constant.SEASONS_IN_A_YEAR; i++) {
			StockFinancial stockFinancial = mStockFinancialList.get(i);
			StockFinancial prev = mStockFinancialList.get(i
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

	private void setupRoi() {
		double price = 0;
		double pe = 0;
		double pb = 0;
		double roi = 0;

		int j = 0;
		for (StockData stockData : mStockDataList) {
			price = stockData.getCandlestick().getClose();
			if (price == 0) {
				continue;
			}

			while (j < mStockFinancialList.size()) {
				StockFinancial stockFinancial = mStockFinancialList.get(j);
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

	private void analyzeMacd(String period) {
		if (mStockDataList == null || mStockDataList.size() < Trend.VERTEX_SIZE) {
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

	private void analyzeStockData(Stock stock, String period) {
		TrendAnalyzer trendAnalyzer = TrendAnalyzer.getInstance();

		trendAnalyzer.analyzeVertex(mStockDataList, mDrawVertexList);
		trendAnalyzer.vertexListToDataList(mStockDataList, mDrawVertexList, mDrawDataList);

		trendAnalyzer.analyzeLine(mStockDataList, mDrawDataList, mStrokeVertexList, Trend.VERTEX_TOP_STROKE, Trend.VERTEX_BOTTOM_STROKE);
		trendAnalyzer.vertexListToDataList(mStockDataList, mStrokeVertexList, mStrokeDataList);

		trendAnalyzer.analyzeLine(mStockDataList, mStrokeDataList, mSegmentVertexList, Trend.VERTEX_TOP_SEGMENT, Trend.VERTEX_BOTTOM_SEGMENT);
		trendAnalyzer.vertexListToDataList(mStockDataList, mSegmentVertexList, mSegmentDataList);

		trendAnalyzer.analyzeLine(mStockDataList, mSegmentDataList, mLineVertexList, Trend.VERTEX_TOP_LINE, Trend.VERTEX_BOTTOM_LINE);
		trendAnalyzer.vertexListToDataList(mStockDataList, mLineVertexList, mLineDataList);

		trendAnalyzer.analyzeLine(mStockDataList, mLineDataList, mOutlineVertexList, Trend.VERTEX_TOP_OUTLINE, Trend.VERTEX_BOTTOM_OUTLINE);
		trendAnalyzer.vertexListToDataList(mStockDataList, mOutlineVertexList, mOutlineDataList);

		analyzeAction(stock, period);
	}

	private void analyzeAction(Stock stock, String period) {
		if (stock == null) {
			return;
		}

		StringBuilder actionBuilder = new StringBuilder();
		appendActionIfPresent(actionBuilder, getDirectionAction());
		if (stock.hasFlag(Stock.FLAG_NOTIFY)) {
			String trendAction = getTrendAction();
			appendActionIfPresent(actionBuilder, trendAction);
			String operateAction = getOperateAction(trendAction);
			appendActionIfPresent(actionBuilder, operateAction);
		}

		StockData stockData = mStockDataList.get(mStockDataList.size() - 1);
		double velocity = stockData.getMacd().getVelocity();
		if (velocity > 0) {
			actionBuilder.append(Trend.MARK_ADD);
		} else if (velocity < 0) {
			actionBuilder.append(Trend.MARK_MINUS);
		}

		stock.setDateTime(stockData.getDate(), stockData.getTime());
		stock.setAction(period, actionBuilder.toString() + stockData.getAction());
	}

	private void appendActionIfPresent(StringBuilder builder, String action) {
		if (action != null && !action.isEmpty()) {
			builder.append(action).append(Constant.NEW_LINE);
		}
	}

	String getDirectionAction() {
		Trend drawVertexTrend = StockData.getLastTrend(mDrawVertexList, 1);
		Trend strokeVertexTrend = StockData.getLastTrend(mStrokeVertexList, 1);
		Trend segmentVertexTrend = StockData.getLastTrend(mSegmentVertexList, 1);

		StringBuilder builder = new StringBuilder();
		appendDirection(builder, segmentVertexTrend);
		appendDirection(builder, strokeVertexTrend);
		appendDirection(builder, drawVertexTrend);
		return builder.toString();
	}

	private void appendDirection(StringBuilder builder, Trend trend) {
		if (builder == null || trend == null) {
			return;
		}

		if (trend.vertexOf(Trend.VERTEX_BOTTOM)) {
			builder.append(Trend.MARK_ADD);
		} else if (trend.vertexOf(Trend.VERTEX_TOP)) {
			builder.append(Trend.MARK_MINUS);
		}
	}

	String getTrendAction() {
		String action = StockData.getLastAction(mDrawDataList, 2, mStockDataList);
		if (!TextUtils.isEmpty(action)) {
			return action;
		}
		return StockData.getLastAction(mStrokeDataList, 0, mStockDataList);
	}

	String getOperateAction(String trendAction) {
		if (TextUtils.isEmpty(trendAction)) {
			return "";
		}

		Trend prevTrend = StockData.getLastTrend(mStockDataList, 1);
		if (prevTrend != null && prevTrend.getVertex() == Trend.VERTEX_NONE) {
			return "";
		}

		StockData stockData = StockData.getLast(mDrawDataList, 2, mStockDataList);
		if (stockData == null || TextUtils.isEmpty(stockData.getAction())) {
			return "";
		}

		Trend strokeTrend = stockData.getTrend();
		if (strokeTrend == null) {
			return "";
		}

		Trend segmentTrend = StockData.getLastTrend(mSegmentVertexList, 1);
		if (segmentTrend == null) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		if (TextUtils.equals(trendAction, Trend.TREND_TYPE_DOWN_NONE_UP)) {
			if (strokeTrend.vertexOf(Trend.VERTEX_BOTTOM_STROKE)) {
				builder.append(Trend.MARK_BUY2);
				if (segmentTrend.vertexOf(Trend.VERTEX_BOTTOM)) {
					builder.append(Trend.MARK_BUY2);
				}
			}
		} else if (TextUtils.equals(trendAction, Trend.TREND_TYPE_UP_NONE_DOWN)) {
			if (strokeTrend.vertexOf(Trend.VERTEX_TOP_STROKE)) {
				builder.append(Trend.MARK_SELL2);
				if (segmentTrend.vertexOf(Trend.VERTEX_TOP)) {
					builder.append(Trend.MARK_SELL2);
				}
			}
		}
		return builder.toString();
	}

	protected void updateNotification(Stock stock) {
		if (stock == null || mContext == null) {
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
		if (period == null || action == null || period.isEmpty() || action.isEmpty()) {
			return;
		}

		boolean containsAction = false;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			containsAction = Trend.NOTIFYACTIONS.stream().anyMatch(action::contains);
		} else {
			for (String notifyAction : Trend.NOTIFYACTIONS) {
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
