package com.android.orion.manager;

import android.app.PendingIntent;
import android.content.Intent;

import com.android.orion.config.Config;
import com.android.orion.receiver.DownloadBroadcastReceiver;

public class DownloadAlarmManager extends OrionAlarmManager {
	private static DownloadAlarmManager mInstance;

	private DownloadAlarmManager() {
		super();
	}

	public static synchronized DownloadAlarmManager getInstance() {
		if (mInstance == null) {
			mInstance = new DownloadAlarmManager();
		}
		return mInstance;
	}

	@Override
	public void startAlarm() {
		setIntervalMillis(Config.alarmInterval);
		setPendingIntent(PendingIntent.getBroadcast(mContext, 0, new Intent(
				mContext, DownloadBroadcastReceiver.class), 0));

		super.startAlarm();
	}
}
