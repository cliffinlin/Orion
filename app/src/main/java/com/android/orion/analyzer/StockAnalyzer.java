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
			mFinancialAnalyzer.analyzeFinancial(stock);
			mFinancialAnalyzer.setupFinancial(stock);
			mFinancialAnalyzer.setupShareBonus(stock);
			stock.setModified(Utility.getCurrentDateTimeString());
			mDatabaseManager.updateStock(stock, stock.getContentValues());
			updateNotification(stock);
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
		if (stock == null || mStockDataList == null || mStockDataList.isEmpty()) {
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

		stock.setDateTime(stockData.getDate(), stockData.getTime());
		stock.setAction(period, actionBuilder.toString());
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
		if (drawTrend == null) {
			return "";
		}

		Trend strokeTrend = StockData.getLastTrend(mStrokeVertexList, 1);
		if (strokeTrend == null) {
			return "";
		}

		Trend segmentTrend = StockData.getLastTrend(mSegmentVertexList, 1);
		if (segmentTrend == null) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		if (drawTrend.vertexOf(Trend.VERTEX_BOTTOM)) {
			if (strokeTrend.vertexOf(Trend.VERTEX_BOTTOM_STROKE)) {
				if (segmentTrend.vertexOf(Trend.VERTEX_BOTTOM_SEGMENT)) {
					if (isOperateType1()) {
						builder.append(Trend.MARK_BUY1);
						builder.append(Trend.MARK_BUY1);
					} else if (isOperateType2()) {
						builder.append(Trend.MARK_BUY2);
						builder.append(Trend.MARK_BUY2);
					}
				}
			}
		} else if (drawTrend.vertexOf(Trend.VERTEX_TOP)) {
			if (strokeTrend.vertexOf(Trend.VERTEX_TOP_STROKE)) {
				if (segmentTrend.vertexOf(Trend.VERTEX_TOP_SEGMENT)) {
					if (isOperateType1()) {
						builder.append(Trend.MARK_SELL1);
						builder.append(Trend.MARK_SELL1);
					} else if (isOperateType2()) {
						builder.append(Trend.MARK_SELL2);
						builder.append(Trend.MARK_SELL2);
					}
				}
			}
		}
		return builder.toString();
	}

	boolean isOperateType1() {
		boolean result = false;

		Trend drawTrend = StockData.getLastTrend(mDrawVertexList, 1);
		if (drawTrend == null) {
			return result;
		}

		Trend strokeTrend = StockData.getLastTrend(mStrokeVertexList, 1);
		if (strokeTrend == null) {
			return result;
		}

		Trend segmentTrend = StockData.getLastTrend(mSegmentVertexList, 1);
		if (segmentTrend == null) {
			return result;
		}

		if (strokeTrend.getIndexStart() == segmentTrend.getIndexStart()) {
			result = true;
		}

		return result;
	}

	boolean isOperateType2() {
		boolean result = false;

		Trend drawTrend1 = StockData.getLastTrend(mDrawVertexList, 1);
		if (drawTrend1 == null) {
			return result;
		}

		Trend drawTrend3 = StockData.getLastTrend(mDrawVertexList, 3);
		if (drawTrend3 == null) {
			return result;
		}

		Trend strokeTrend1 = StockData.getLastTrend(mStrokeVertexList, 1);
		if (strokeTrend1 == null) {
			return result;
		}

		Trend strokeTrend3 = StockData.getLastTrend(mStrokeVertexList, 3);
		if (strokeTrend3 == null) {
			return result;
		}

		Trend segmentTrend = StockData.getLastTrend(mSegmentVertexList, 1);
		if (segmentTrend == null) {
			return result;
		}

		if (drawTrend1.getIndexStart() == strokeTrend1.getIndexStart() || drawTrend3.getIndexStart() == strokeTrend1.getIndexStart()) {
			result = true;
		}

		return result;
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

	private static class SingletonHolder {
		private static final StockAnalyzer INSTANCE = new StockAnalyzer();
	}
}
