package com.android.orion;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

public class OrionAlarmManager {
	Context mContext = null;

	long mIntervalMillis = 0;
	AlarmManager mAlarmManager = null;
	PendingIntent mPendingIntent = null;

	public OrionAlarmManager() {
	}

	public OrionAlarmManager(Context context) {
		mContext = context;

		if (mAlarmManager == null) {
			mAlarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
		}
	}

	void setIntervalMillis(long intervalMillis) {
		if (intervalMillis <= 0) {
			mIntervalMillis = Constants.STOCK_DOWNLOAD_ALARM_INTERVAL_DEFAULT;
		} else {
			mIntervalMillis = intervalMillis;
		}
	}

	void setPendingIntent(PendingIntent operation) {
		mPendingIntent = operation;
	}

	void startAlarm() {
		stopAlarm();

		if ((mAlarmManager == null) || (mPendingIntent == null)
				|| (mIntervalMillis <= 0)) {
			return;
		}

		mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis(), mIntervalMillis, mPendingIntent);
	}

	void stopAlarm() {
		if ((mAlarmManager == null) || (mPendingIntent == null)) {
			return;
		}

		mAlarmManager.cancel(mPendingIntent);
	}
}
