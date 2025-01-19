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
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
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
	Stock mStock;
	ArrayList<StockData> mStockDataList;
	ArrayList<StockData> mDrawVertexList;
	ArrayList<StockData> mDrawDataList;
	ArrayList<StockData> mStrokeVertexList;
	ArrayList<StockData> mStrokeDataList;
	ArrayList<StockData> mSegmentVertexList;
	ArrayList<StockData> mSegmentDataList;
	ArrayList<StockData> mLineVertexList;
	ArrayList<StockData> mLineDataList;
	ArrayList<StockData> mOutlineVertexList;
	ArrayList<StockData> mOutlineDataList;

	StringBuffer mContentTitle = new StringBuffer();
	StringBuffer mContentText = new StringBuffer();

	Context mContext;
	NotificationManager mNotificationManager;
	DatabaseManager mDatabaseManager;
	FinancialAnalyzer mFinancialAnalyzer;
	Logger Log = Logger.getLogger();

	private StockAnalyzer() {
		mContext = MainApplication.getContext();

		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mDatabaseManager = DatabaseManager.getInstance();
		mFinancialAnalyzer = FinancialAnalyzer.getInstance();
	}

	public static StockAnalyzer getInstance() {
		return SingletonHolder.INSTANCE;
	}

	void analyze(String period) {
		StopWatch.start();

		if (mStock == null) {
			return;
		}

		mStockDataList = mStock.getArrayList(period, Period.TYPE_STOCK_DATA);
		mDrawVertexList = mStock.getArrayList(period, Period.TYPE_DRAW_VERTEX);
		mDrawDataList = mStock.getArrayList(period, Period.TYPE_DRAW_DATA);
		mStrokeVertexList = mStock.getArrayList(period, Period.TYPE_STROKE_VERTEX);
		mStrokeDataList = mStock.getArrayList(period, Period.TYPE_STROKE_DATA);
		mSegmentVertexList = mStock.getArrayList(period, Period.TYPE_SEGMENT_VERTEX);
		mSegmentDataList = mStock.getArrayList(period, Period.TYPE_SEGMENT_DATA);
		mLineVertexList = mStock.getArrayList(period, Period.TYPE_LINE_VERTEX);
		mLineDataList = mStock.getArrayList(period, Period.TYPE_LINE_DATA);
		mOutlineVertexList = mStock.getArrayList(period, Period.TYPE_OUTLINE_VERTEX);
		mOutlineDataList = mStock.getArrayList(period, Period.TYPE_OUTLINE_DATA);

		try {
			mDatabaseManager.loadStockDataList(mStock, period, mStockDataList);
			analyzeMacd(period);
			analyzeStockData(period);
			mDatabaseManager.updateStockData(mStock, period, mStockDataList);
			mStock.setModified(Utility.getCurrentDateTimeString());
			mDatabaseManager.updateStock(mStock, mStock.getContentValues());
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
			for (String period : Period.PERIODS) {
				if (Setting.getPeriod(period)) {
					analyze(period);
				}
			}
			mFinancialAnalyzer.analyzeFinancial(mStock);
			mFinancialAnalyzer.setupFinancial(mStock);
			mFinancialAnalyzer.setupShareBonus(mStock);
			stock.setModified(Utility.getCurrentDateTimeString());
			mDatabaseManager.updateStock(mStock, mStock.getContentValues());
			updateNotification();
		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d(stock.getName() + " " + StopWatch.getInterval() + "s");
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

	private void analyzeStockData(String period) {
		TrendAnalyzer trendAnalyzer = TrendAnalyzer.getInstance();
		trendAnalyzer.setup(mStock, mStockDataList);

		trendAnalyzer.analyzeVertex(mDrawVertexList);
		trendAnalyzer.vertexListToDataList(mDrawVertexList, mDrawDataList);

		trendAnalyzer.analyzeLine(Trend.LEVEL_DRAW, mDrawDataList, mStrokeVertexList);
		trendAnalyzer.vertexListToDataList(mStrokeVertexList, mStrokeDataList);

		trendAnalyzer.analyzeLine(Trend.LEVEL_STROKE, mStrokeDataList, mSegmentVertexList);
		trendAnalyzer.vertexListToDataList(mSegmentVertexList, mSegmentDataList);

		trendAnalyzer.analyzeLine(Trend.LEVEL_SEGMENT, mSegmentDataList, mLineVertexList);
		trendAnalyzer.vertexListToDataList(mLineVertexList, mLineDataList);

		trendAnalyzer.analyzeLine(Trend.LEVEL_LINE, mLineDataList, mOutlineVertexList);
		trendAnalyzer.vertexListToDataList(mOutlineVertexList, mOutlineDataList);

		analyzeAction(period);
	}

	private void analyzeAction(String period) {
		if (mStock == null || mStockDataList == null || mStockDataList.isEmpty()) {
			return;
		}

		StringBuilder actionBuilder = new StringBuilder();
		appendActionIfPresent(actionBuilder, getDirectionAction());
		appendActionIfPresent(actionBuilder, getTrendAction());
		appendActionIfPresent(actionBuilder, getOperateAction());

		StockData stockData = mStockDataList.get(mStockDataList.size() - 1);
		Macd macd = stockData.getMacd();
		if (macd != null) {
			double velocity = macd.getVelocity();
			if (velocity > 0) {
				actionBuilder.append(Trend.MARK_ADD);
			} else if (velocity < 0) {
				actionBuilder.append(Trend.MARK_MINUS);
			}
		}

		mStock.setDateTime(stockData.getDate(), stockData.getTime());
		mStock.setAction(period, actionBuilder.toString());
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
		if (mStockDataList == null || mStockDataList.isEmpty()) {
			return "";
		}

		StockData stockData = StockData.getLast(mStockDataList, 0);
		return stockData != null ? stockData.getAction() : "";
	}

	String getOperateAction() {
		Trend drawTrend = StockData.getLastTrend(mDrawVertexList, 1);
		Trend strokeTrend = StockData.getLastTrend(mStrokeVertexList, 1);
		Trend segmentTrend = StockData.getLastTrend(mSegmentVertexList, 1);

		if (drawTrend == null || strokeTrend == null || segmentTrend == null) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		if (drawTrend.vertexOf(Trend.VERTEX_BOTTOM)
				&& strokeTrend.vertexOf(Trend.VERTEX_BOTTOM_STROKE)
				&& segmentTrend.vertexOf(Trend.VERTEX_BOTTOM_SEGMENT)) {
			if (isOperateType1()) {
				builder.append(Trend.MARK_BUY1);
				builder.append(Trend.MARK_BUY1);
			} else if (isOperateType2()) {
				builder.append(Trend.MARK_BUY2);
				builder.append(Trend.MARK_BUY2);
			}
		} else if (drawTrend.vertexOf(Trend.VERTEX_TOP)
				&& strokeTrend.vertexOf(Trend.VERTEX_TOP_STROKE)
				&& segmentTrend.vertexOf(Trend.VERTEX_TOP_SEGMENT)) {
			if (isOperateType1()) {
				builder.append(Trend.MARK_SELL1);
				builder.append(Trend.MARK_SELL1);
			} else if (isOperateType2()) {
				builder.append(Trend.MARK_SELL2);
				builder.append(Trend.MARK_SELL2);
			}
		}
		return builder.toString();
	}

	boolean isOperateType1() {
		boolean result = false;

		StockData stockData = StockData.getLast(mStockDataList, 0);
		Trend drawTrend1 = StockData.getLastTrend(mDrawVertexList, 1);
		Trend drawTrend3 = StockData.getLastTrend(mDrawVertexList, 3);
		Trend strokeTrend = StockData.getLastTrend(mStrokeVertexList, 1);
		Trend segmentTrend = StockData.getLastTrend(mSegmentVertexList, 1);

		if (stockData == null || drawTrend1 == null || drawTrend3 == null || strokeTrend == null || segmentTrend == null) {
			return result;
		}

		if (strokeTrend.getIndexStart() == segmentTrend.getIndexStart()) {
			if (drawTrend1.getIndexStart() == strokeTrend.getIndexStart()) {
				result = true;
			}
			if (drawTrend3.getIndexStart() == strokeTrend.getIndexStart()) {
				result = true;
			}
		}

		return result;
	}

	boolean isOperateType2() {
		boolean result = false;

		StockData stockData = StockData.getLast(mStockDataList, 0);
		Trend drawTrend1 = StockData.getLastTrend(mDrawVertexList, 1);
		Trend drawTrend3 = StockData.getLastTrend(mDrawVertexList, 3);
		Trend strokeTrend1 = StockData.getLastTrend(mStrokeVertexList, 1);
		Trend strokeTrend3 = StockData.getLastTrend(mStrokeVertexList, 3);
		Trend segmentTrend = StockData.getLastTrend(mSegmentVertexList, 1);

		if (stockData == null || drawTrend1 == null || drawTrend3 == null || strokeTrend1 == null || strokeTrend3 == null || segmentTrend == null) {
			return result;
		}

		if (strokeTrend3.getIndexStart() == segmentTrend.getIndexStart()) {
			if (drawTrend1.getIndexStart() == strokeTrend1.getIndexStart()) {
				result = true;
			}
			if (drawTrend3.getIndexStart() == strokeTrend1.getIndexStart()) {
				result = true;
			}
		}

		return result;
	}

	protected void updateNotification() {
		if (mStock == null || mContext == null) {
			return;
		}

		if (mStock.getPrice() == 0 || !mStock.hasFlag(Stock.FLAG_NOTIFY)) {
			return;
		}

		mContentTitle.setLength(0);
		mContentText.setLength(0);

		for (String period : Period.PERIODS) {
			if (!Setting.getPeriod(period)) {
				continue;
			}

			String action = mStock.getAction(period);
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

		mContentTitle.insert(0, mStock.getName() + " " + mStock.getPrice() + " " + mStock.getNet() + " ");
		RecordFile.writeNotificationFile(mContentTitle.toString());
		try {
			int code = Integer.parseInt(mStock.getCode());
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

	private static class SingletonHolder {
		private static final StockAnalyzer INSTANCE = new StockAnalyzer();
	}
}
