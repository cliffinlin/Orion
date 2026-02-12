package com.android.orion.manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.android.orion.R;
import com.android.orion.activity.StockFavoriteChartListActivity;
import com.android.orion.activity.StockFavoriteListActivity;
import com.android.orion.analyzer.TradeAnalyzer;
import com.android.orion.application.MainApplication;
import com.android.orion.config.Config;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockTrend;
import com.android.orion.constant.Constant;

public class StockNotificationManager {

	public static final int SERVICE_NOTIFICATION_ID = -10000;
	public static final int TREND_NOTIFICATION_ID = 0;

	StringBuffer mContentTitle = new StringBuffer();
	StringBuffer mContentText = new StringBuffer();

	Context mContext = MainApplication.getContext();
	NotificationManager mNotificationManager;
	TradeAnalyzer mTradeAnalyzer = TradeAnalyzer.getInstance();

	private StockNotificationManager() {
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public static StockNotificationManager getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public void createNotificationChannel(NotificationChannel channel) {
		if (mNotificationManager == null) {
			return;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			mNotificationManager.createNotificationChannel(channel);
		}
	}

	public void cancel(int id) {
		if (mNotificationManager == null) {
			return;
		}

		try {
			mNotificationManager.cancel(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void notify(Stock stock, StockTrend stockTrend) {
		if (stock == null || mContext == null || stockTrend == null) {
			return;
		}

		if (stockTrend.getLevel() != stock.getTarget(stockTrend.getPeriod())) {
			return;
		}

		String notifyString = "";
		if (stock.hasFlag(Stock.FLAG_TRADE)) {
			mTradeAnalyzer.analyzeProfit(stock);
			if (stockTrend.getNet() > 0) {
				if (stock.getBuyProfit() > 0) {
					notifyString = mTradeAnalyzer.getBuyDealString();
				}
			} else {
				if (stock.getSellProfit() < 0) {
					notifyString = mTradeAnalyzer.getSellDealString();
				}
			}
		}

		mContentTitle.setLength(0);
		mContentText.setLength(0);

		mContentTitle.append(stock.getNamePriceNetString(" ") + " " + stockTrend.toNotifyString());
		mContentText.append(notifyString);
		try {
			int id = TREND_NOTIFICATION_ID + (int) stockTrend.getId();
			long stockId = stock.getId();
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

	private static class SingletonHelper {
		private static final StockNotificationManager INSTANCE = new StockNotificationManager();
	}
}
