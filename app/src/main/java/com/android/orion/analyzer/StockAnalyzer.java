package com.android.orion.analyzer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.android.orion.R;
import com.android.orion.activity.StockFavoriteChartListActivity;
import com.android.orion.activity.StockFavoriteListActivity;
import com.android.orion.application.MainApplication;
import com.android.orion.config.Config;
import com.android.orion.data.Macd;
import com.android.orion.data.Period;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDeal;
import com.android.orion.database.StockTrend;
import com.android.orion.manager.DatabaseManager;
import com.android.orion.provider.StockPerceptronProvider;
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

	StringBuffer mContentTitle = new StringBuffer();
	StringBuffer mContentText = new StringBuffer();

	Context mContext;
	NotificationManager mNotificationManager;
	DatabaseManager mDatabaseManager = DatabaseManager.getInstance();
	FinancialAnalyzer mFinancialAnalyzer = FinancialAnalyzer.getInstance();
	TrendAnalyzer mTrendAnalyzer = TrendAnalyzer.getInstance();
	StockPerceptronProvider mStockPerceptronProvider = StockPerceptronProvider.getInstance();
	Logger Log = Logger.getLogger();

	private StockAnalyzer() {
		mContext = MainApplication.getContext();
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
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
			mStockPerceptronProvider.train(mTrendAnalyzer.getStockTrendNetMap());
			mFinancialAnalyzer.analyzeFinancial(mStock);
			mFinancialAnalyzer.setupFinancial(mStock);
			mFinancialAnalyzer.setupStockBonus(mStock);
			stock.setModified(Utility.getCurrentDateTimeString());
			mDatabaseManager.updateStock(mStock, mStock.getContentValues());
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

		int size = mStockDataList.size();
		if (average5List.size() != size || average10List.size() != size || difList.size() != size || deaList.size() != size || histogramList.size() != size) {
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
						histogramList.get(i)
				);
			}
		}
	}

	private void analyzeStockData(String period) {
		mTrendAnalyzer.setup(mStock, period, mStockDataList);

		mTrendAnalyzer.analyzeVertex();
		mTrendAnalyzer.vertexListToDataList(mStock.getVertexList(period, StockTrend.LEVEL_DRAW), mStock.getDataList(period, StockTrend.LEVEL_DRAW));

		for (int i = 2; i < StockTrend.LEVELS.length; i++) {
			ArrayList<StockData> prevDataList = mStock.getDataList(period, i - 1);
			ArrayList<StockData> vertexList = mStock.getVertexList(period, i);
			ArrayList<StockData> dataList = mStock.getDataList(period, i);
			mTrendAnalyzer.analyzeLine(i - 1, prevDataList, vertexList);
			mTrendAnalyzer.vertexListToDataList(vertexList, dataList);
		}

		mTrendAnalyzer.analyzeLine(StockTrend.LEVEL_TREND_LINE, mStock.getDataList(period, StockTrend.LEVEL_TREND_LINE), mStock.getVertexList(period, StockTrend.LEVEL_TREND_LINE));
		mTrendAnalyzer.vertexListToDataList(mStock.getVertexList(period, StockTrend.LEVEL_TREND_LINE), mStock.getDataList(period, StockTrend.LEVEL_TREND_LINE));
		mStock.setLevel(period, determineAdaptiveLevel(period));

		analyzeAction(period);
	}

	private int determineAdaptiveLevel(String period) {
		for (int i = StockTrend.LEVELS.length - 1; i > 0; i--) {
			if (mStock.getDataList(period, i).size() >= StockTrend.ADAPTIVE_SIZE) {
				return i;
			}
		}
		return StockTrend.LEVEL_DRAW;
	}

	private void analyzeAction(String period) {
		if (mStock == null || mStockDataList == null || mStockDataList.isEmpty()) {
			return;
		}

		StringBuilder actionBuilder = new StringBuilder();
		appendActionIfPresent(actionBuilder, getDirectionAction(period));
		appendActionIfPresent(actionBuilder, getTrendAction());
		mStock.setAction(period, actionBuilder.toString());
	}

	private void appendActionIfPresent(StringBuilder builder, String action) {
		if (action != null && !action.isEmpty()) {
			builder.append(action).append(Constant.NEW_LINE);
		}
	}

	String getDirectionAction(String period) {
		StockData draw = StockData.getLast(mStock.getVertexList(period, StockTrend.LEVEL_DRAW), 1);
		StockData stroke = StockData.getLast(mStock.getVertexList(period, StockTrend.LEVEL_STROKE), 1);
		StockData segment = StockData.getLast(mStock.getVertexList(period, StockTrend.LEVEL_SEGMENT), 1);

		StringBuilder builder = new StringBuilder();
		appendDirection(builder, segment);
		appendDirection(builder, stroke);
		appendDirection(builder, draw);
		return builder.toString();
	}

	private void appendDirection(StringBuilder builder, StockData stockData) {
		if (builder == null || stockData == null) {
			return;
		}

		if (stockData.vertexOf(StockTrend.VERTEX_BOTTOM)) {
			builder.append(Constant.MARK_ADD);
		} else if (stockData.vertexOf(StockTrend.VERTEX_TOP)) {
			builder.append(Constant.MARK_MINUS);
		}
	}

	String getTrendAction() {
		if (mStockDataList == null || mStockDataList.isEmpty()) {
			return "";
		}

		StockData stockData = StockData.getLast(mStockDataList, 0);
		return stockData != null ? stockData.getAction() : "";
	}

	public void notifyStockTrend(StockTrend stockTrend) {
		if (mStock == null || mContext == null || stockTrend == null) {
			return;
		}

		if (!Market.isTradingHours()) {
			Toast.makeText(mContext,
					mContext.getResources().getString(R.string.out_of_trading_hours),
					Toast.LENGTH_SHORT).show();
			return;
		}

		if (!mStock.hasFlag(Stock.FLAG_NOTIFY)) {
			return;
		}

		mContentTitle.setLength(0);
		mContentText.setLength(0);

		StockDeal stockDeal = new StockDeal();
		mDatabaseManager.getStockDeal(mStock, stockDeal);
		double stockDealProfit = stockDeal.getProfit();
		if (stockDealProfit > 0) {
			mContentTitle.append(Constant.MARK_DOLLAR);
		}
		mContentTitle.append(mStock.getName() + " " + mStock.getPrice() + " " + mStock.getNet() + " " + stockTrend.toTrendString() + " ? " + mStockPerceptronProvider.getStockPerceptron().predict(stockTrend.getNet1()));

		RecordFile.writeNotificationFile(mContentTitle.toString());
		try {
			int code = (int) stockTrend.getId();
			long stockId = mStock.getId();
			notify(code, stockId, Config.MESSAGE_CHANNEL_ID, Config.MESSAGE_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH,
					mContentTitle.toString(), mContentText.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void notify(int id, String channelID, String channelName, int importance, String contentTitle, String contentText) {
		notify(id, DatabaseContract.INVALID_ID, channelID, channelName, importance, contentTitle, contentText);
	}

	public void notify(int id, long stockID, String channelID, String channelName, int importance, String contentTitle, String contentText) {
		if (mNotificationManager == null || mContext == null) {
			return;
		}

		mNotificationManager.cancel(id);

		Intent intent = new Intent();
		if (stockID == DatabaseContract.INVALID_ID) {
			intent.setClass(mContext, StockFavoriteListActivity.class);
		} else {
			intent.setClass(mContext, StockFavoriteChartListActivity.class);
			intent.putExtra(Constant.EXTRA_STOCK_ID, stockID);
		}
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
