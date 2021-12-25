package com.android.orion;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

public class OrionAlarmManager {
	static final String TAG = Constants.TAG + " "
			+ OrionAlarmManager.class.getSimpleName();

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
			mIntervalMillis = Constants.DEFAULT_ALARM_INTERVAL;
		} else {
			mIntervalMillis = intervalMillis;
		}

		Log.d(TAG, "setIntervalMillis: " + "mIntervalMillis = "
				+ mIntervalMillis);
	}

	void setPendingIntent(PendingIntent pendingIntent) {
		mPendingIntent = pendingIntent;
	}

	void startAlarm() {
		stopAlarm();

		if ((mAlarmManager == null) || (mPendingIntent == null)
				|| (mIntervalMillis <= 0)) {
			Log.d(TAG, "startAlarm return " + "mAlarmManager = "
					+ mAlarmManager + " mPendingIntent = " + mPendingIntent
					+ " mIntervalMillis = " + mIntervalMillis);
			return;
		}

		mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis(), mIntervalMillis, mPendingIntent);
	}

	void stopAlarm() {
		if ((mAlarmManager == null) || (mPendingIntent == null)) {
			Log.d(TAG, "stopAlarm return " + "mAlarmManager = " + mAlarmManager
					+ " mPendingIntent = " + mPendingIntent);
			return;
		}

		mAlarmManager.cancel(mPendingIntent);
	}
}
