package com.android.orion.manager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.android.orion.receiver.DownloadBroadcastReceiver;
import com.android.orion.setting.Constant;

public class DownloadAlarmManager extends OrionAlarmManager {
	private static DownloadAlarmManager mInstance = null;

	private DownloadAlarmManager(Context context) {
		super(context);
	}

	public static synchronized DownloadAlarmManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DownloadAlarmManager(context);
		}

		return mInstance;
	}

	@Override
	public void startAlarm() {
		setIntervalMillis(Constant.DEFAULT_ALARM_INTERVAL);
		setPendingIntent(PendingIntent.getBroadcast(mContext, 0, new Intent(
				mContext, DownloadBroadcastReceiver.class), 0));

		super.startAlarm();
	}
}
