package com.android.orion;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class DownloadAlarmManager extends OrionAlarmManager {
	private static DownloadAlarmManager mInstance = null;

	public static synchronized DownloadAlarmManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DownloadAlarmManager(context);
		}

		return mInstance;
	}

	private DownloadAlarmManager(Context context) {
		super(context);
	}

	@Override
	void startAlarm() {
		setIntervalMillis(Constants.DEFAULT_ALARM_INTERVAL);
		setPendingIntent(PendingIntent.getBroadcast(mContext, 0, new Intent(
				mContext, DownloadBroadcastReceiver.class), 0));

		super.startAlarm();
	}
}
