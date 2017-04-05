package com.android.orion;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import com.android.orion.utility.Utility;

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

		Utility.Log("setIntervalMillis: " + "mIntervalMillis = "
				+ mIntervalMillis);
	}

	void setPendingIntent(PendingIntent operation) {
		mPendingIntent = operation;
	}

	void startAlarm() {
		stopAlarm();

		if ((mAlarmManager == null) || (mPendingIntent == null)
				|| (mIntervalMillis <= 0)) {
			Utility.Log("startAlarm return " + "mAlarmManager = "
					+ mAlarmManager + " mPendingIntent = " + mPendingIntent
					+ " mIntervalMillis = " + mIntervalMillis);
			return;
		}

		mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis(), mIntervalMillis, mPendingIntent);
	}

	void stopAlarm() {
		if ((mAlarmManager == null) || (mPendingIntent == null)) {
			Utility.Log("stopAlarm return " + "mAlarmManager = "
					+ mAlarmManager + " mPendingIntent = " + mPendingIntent);
			return;
		}

		mAlarmManager.cancel(mPendingIntent);
	}
}
