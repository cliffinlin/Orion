package com.android.orion;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

import java.util.Calendar;

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

//	public void startAlarm1() {
//		int value = 0;
//		long triggerMillis = 0;
//		long intervalMillis = 0;
//
//		Calendar calendar;
//
//		if ((mAlarmManager == null) || (mPendingIntent == null)) {
//			Utility.Log("startAlarm return:" + "mAlarmManager=" + mAlarmManager
//					+ " mPendingIntent=" + mPendingIntent);
//			return;
//		}
//
//		calendar = Calendar.getInstance();
//		value = EPHEMERIS_VALID_IN_MINUTE - calendar.get(Calendar.MINUTE)
//				% EPHEMERIS_VALID_IN_MINUTE;
//		calendar.add(Calendar.MINUTE, value);
//		calendar.add(Calendar.SECOND, -1 * calendar.get(Calendar.SECOND));
//
//		triggerMillis = calendar.getTimeInMillis();
//		intervalMillis = EPHEMERIS_VALID_IN_MINUTE * MINUTE_TO_MILLIS;
//
//		Utility.Log("startAlarm will arrive at "
//				+ calendar.get(Calendar.HOUR_OF_DAY) + ":"
//				+ calendar.get(Calendar.MINUTE) + ":"
//				+ calendar.get(Calendar.SECOND) + " triggerMillis="
//				+ triggerMillis + " intervalMillis=" + intervalMillis);
//
//		mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
//				triggerMillis, intervalMillis, mPendingIntent);
//	}

	void stopAlarm() {
		if ((mAlarmManager == null) || (mPendingIntent == null)) {
			Log.d(TAG, "stopAlarm return " + "mAlarmManager = " + mAlarmManager
					+ " mPendingIntent = " + mPendingIntent);
			return;
		}

		mAlarmManager.cancel(mPendingIntent);
	}
}
