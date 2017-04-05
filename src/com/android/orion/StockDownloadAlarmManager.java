package com.android.orion;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class StockDownloadAlarmManager extends OrionAlarmManager {
	private static StockDownloadAlarmManager mInstance = null;

	public static synchronized StockDownloadAlarmManager getInstance(
			Context context) {
		if (mInstance == null) {
			mInstance = new StockDownloadAlarmManager(context);
		}

		return mInstance;
	}

	private StockDownloadAlarmManager(Context context) {
		super(context);
	}

	@Override
	void startAlarm() {
		setIntervalMillis(Constants.STOCK_DOWNLOAD_ALARM_INTERVAL_DEFAULT);
		setPendingIntent(PendingIntent.getBroadcast(mContext, 0, new Intent(
				mContext, StockDownloadBroadcastReceiver.class), 0));

		super.startAlarm();
	}
}
