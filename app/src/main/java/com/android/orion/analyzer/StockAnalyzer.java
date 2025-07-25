package com.android.orion.analyzer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.ArrayMap;
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
import com.android.orion.database.StockBonus;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDeal;
import com.android.orion.database.StockGrid;
import com.android.orion.database.StockRZRQ;
import com.android.orion.database.StockTrend;
import com.android.orion.manager.DatabaseManager;
import com.android.orion.provider.StockPerceptronProvider;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Market;
import com.android.orion.utility.StopWatch;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.List;


public class StockAnalyzer {
	Stock mStock;
	ArrayList<StockData> mStockDataList;
	ArrayMap<String, StockRZRQ> mStockRZRQMap = new ArrayMap<>();
	ArrayList<StockBonus> mStockBonusList = new ArrayList<>();
	StringBuffer mContentTitle = new StringBuffer();
	StringBuffer mContentText = new StringBuffer();

	Context mContext = MainApplication.getContext();
	NotificationManager mNotificationManager;
	DatabaseManager mDatabaseManager = DatabaseManager.getInstance();
	FinancialAnalyzer mFinancialAnalyzer = FinancialAnalyzer.getInstance();
	GridAnalyzer mGridAnalyzer = GridAnalyzer.getInstance();
	TrendAnalyzer mTrendAnalyzer = TrendAnalyzer.getInstance();
	StockPerceptronProvider mStockPerceptronProvider = StockPerceptronProvider.getInstance();
	Logger Log = Logger.getLogger();

	private StockAnalyzer() {
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
			if (Period.getPeriodIndex(period) <= Period.getPeriodIndex(Period.MONTH)) {
				mFinancialAnalyzer.setNetProfileInYear(mStock, mStockDataList);
			}
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
			mFinancialAnalyzer.analyzeFinancial(mStock);
			mFinancialAnalyzer.setupFinancial(mStock);
			mFinancialAnalyzer.setupStockBonus(mStock);
			for (String period : Period.PERIODS) {
				if (Setting.getPeriod(period)) {
					analyze(period);
				}
			}
			mFinancialAnalyzer.analyzeFinancial(mStock);
			mFinancialAnalyzer.setupFinancial(mStock);
			mFinancialAnalyzer.setupStockBonus(mStock);
			mGridAnalyzer.analyze(mStock);
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

		String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";
		mDatabaseManager.getStockRZRQMap(mStock, mStockRZRQMap, sortOrder);
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

		StringBuilder actionBuilder = new StringBuilder();
		appendActionIfPresent(actionBuilder, getDirectionAction(period));
		appendActionIfPresent(actionBuilder, getVelocityAction());
		mStock.setAction(period, actionBuilder.toString());
	}

	private void appendActionIfPresent(StringBuilder builder, String action) {
		if (action != null && !action.isEmpty()) {
			builder.append(action).append(Constant.NEW_LINE);
		}
	}

	String getDirectionAction(String period) {
		StringBuilder builder = new StringBuilder();
		StockData stockData = StockData.getLast(mStock.getVertexList(period, mStock.getLevel(period)), 1);
		if (stockData != null) {
			if (stockData.vertexOf(StockTrend.VERTEX_BOTTOM)) {
				builder.append(Constant.MARK_ADD);
			} else if (stockData.vertexOf(StockTrend.VERTEX_TOP)) {
				builder.append(Constant.MARK_MINUS);
			}
		}
		return builder.toString();
	}

	String getVelocityAction() {
		StringBuilder builder = new StringBuilder();
		StockData stockData = mStockDataList.get(mStockDataList.size() - 1);
		if (stockData != null) {
			Macd macd = stockData.getMacd();
			if (macd != null) {
				double velocity = macd.getVelocity();
				if (velocity > 0) {
					builder.append(Constant.MARK_ADD);
				} else if (velocity < 0) {
					builder.append(Constant.MARK_MINUS);
				}
			}
		}
		return builder.toString();
	}

	String getLevelAction(String period) {
		StringBuilder builder = new StringBuilder();

		double mean = 0;
		double sd = 0;
		String adaptive = "";
		for (int i = StockTrend.LEVEL_DRAW; i < StockTrend.LEVELS.length; i++) {
			ArrayList<StockData> dataList = mStock.getDataList(period, i);
			mean = Utility.Round2(calculateMean(dataList));
			sd = Utility.Round2(calculateStandardDeviation(dataList));
			if (mStock.getLevel(period) == i) {
				adaptive = Constant.MARK_ASTERISK;
			} else {
				adaptive = "";
			}
			Log.d(mStock.getName() + " " + period + " " + Constant.MARK_LEVEL + i + " mean=" + mean + " sd=" + sd + " size=" + dataList.size() + " " + adaptive);
		}

		builder.append(Constant.MARK_LEVEL + mStock.getLevel(period));

		return builder.toString();
	}

	public double calculateMean(ArrayList<StockData> dataList) {
		double sum = 0.0;
		if (dataList == null || dataList.isEmpty()) {
			return sum;
		}

		for (StockData data : dataList) {
			sum += Math.abs(data.getNet());
		}
		return sum / dataList.size();
	}

	public double calculateStandardDeviation(ArrayList<StockData> dataList) {
		double sumOfSquaredDifferences = 0.0;
		if (dataList == null || dataList.isEmpty()) {
			return sumOfSquaredDifferences;
		}

		double mean = calculateMean(dataList);
		for (StockData data : dataList) {
			sumOfSquaredDifferences += Math.pow(Math.abs(data.getNet()) - mean, 2);
		}

		double variance = sumOfSquaredDifferences / dataList.size();
		return Math.sqrt(variance);
	}

	public void notifyStockGrid(StockGrid stockGrid) {
		if (mStock == null || mContext == null || stockGrid == null) {
			return;
		}

		if (!mStock.hasFlag(Stock.FLAG_GRID)) {
			return;
		}

		if (!Market.isTradingHours()) {
			Toast.makeText(mContext,
					mContext.getResources().getString(R.string.out_of_trading_hours),
					Toast.LENGTH_SHORT).show();
			return;
		}

		mContentTitle.setLength(0);
		mContentText.setLength(0);

		mContentTitle.append(mStock.getName() + " " + mStock.getPrice() + " " + mStock.getNet() + " " + stockGrid.toNotifyString());
		try {
			int id = Config.SERVICE_NOTIFICATION_ID + (int) stockGrid.getId();
			long stockId = mStock.getId();
			notify(id, stockId, Config.MESSAGE_CHANNEL_ID, Config.MESSAGE_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH,
					mContentTitle.toString(), mContentText.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void cancelNotifyStockTrend(StockTrend stockTrend) {
		if (mNotificationManager == null || stockTrend == null) {
			return;
		}

		try {
			int id = (int) stockTrend.getId();
			mNotificationManager.cancel(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void notifyStockTrend(StockTrend stockTrend) {
		if (mStock == null || mContext == null || stockTrend == null) {
			return;
		}

		if (!mStock.hasFlag(Stock.FLAG_NOTIFY)) {
			return;
		}

		if (stockTrend.getLevel() < mStock.getLevel(stockTrend.getPeriod())) {
			return;
		}

		if (!Market.isTradingHours()) {
			Toast.makeText(mContext,
					mContext.getResources().getString(R.string.out_of_trading_hours),
					Toast.LENGTH_SHORT).show();
			return;
		}

		mContentTitle.setLength(0);
		mContentText.setLength(0);

		mContentTitle.append(mStock.getName() + " " + mStock.getPrice() + " " + mStock.getNet() + " " + stockTrend.toNotifyString());
		try {
			int id = (int) stockTrend.getId();
			long stockId = mStock.getId();
			notify(id, stockId, Config.MESSAGE_CHANNEL_ID, Config.MESSAGE_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH,
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
