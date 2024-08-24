package com.android.orion.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.android.orion.application.MainApplication;
import com.android.orion.config.Config;
import com.android.orion.receiver.DownloadBroadcastReceiver;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Market;
import com.android.orion.utility.Utility;

import java.util.Calendar;

public class StockAlarmManager {
	Logger Log = Logger.getLogger();
	Context mContext;
	private long mIntervalMillis = Config.alarmInterval;
	private AlarmManager mAlarmManager = null;
	private PendingIntent mPendingIntent;
	private static StockAlarmManager mInstance;

	public static synchronized StockAlarmManager getInstance() {
		if (mInstance == null) {
			mInstance = new StockAlarmManager();
		}
		return mInstance;
	}

	StockAlarmManager() {
		mContext = MainApplication.getContext();

		if (mAlarmManager == null) {
			mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		}

		mPendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(
				mContext, DownloadBroadcastReceiver.class), 0);
	}

	public void startAlarm() {
		stopAlarm();

		if ((mAlarmManager == null) || (mPendingIntent == null)
				|| (mIntervalMillis <= 0)) {
			Log.d("return, mAlarmManager = "
					+ mAlarmManager + " mPendingIntent = " + mPendingIntent
					+ " mIntervalMillis = " + mIntervalMillis);
			return;
		}

		Calendar calendar = Calendar.getInstance();
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

		if (Market.isWeekday()) {
			if (Market.beforeOpen()) {
				calendar = Market.getFirstHalfStartCalendar();
			} else if (Market.isFirstHalf()) {
				calendar = Calendar.getInstance();
			} else if (Market.isLunchTime()) {
				calendar = Market.getSecondHalfStartCalendar();
			} else if (Market.isSecondHalf()) {
				calendar = Calendar.getInstance();
			} else if (Market.afterClosed()) {
				if (dayOfWeek < Calendar.FRIDAY) {
					calendar.add(Calendar.DAY_OF_WEEK, 1);
				} else {
					calendar.add(Calendar.DAY_OF_WEEK, 3);
				}
				calendar = Market.getFirstHalfStartCalendar();
			}
		} else {
			if (dayOfWeek == Calendar.SATURDAY) {
				calendar.add(Calendar.DAY_OF_WEEK, 2);
			} else if (dayOfWeek == Calendar.SUNDAY) {
				calendar.add(Calendar.DAY_OF_WEEK, 1);
			}
			calendar = Market.getFirstHalfStartCalendar();
		}

		long triggerMillis = calendar.getTimeInMillis();

		Log.d("will arrive at "
				+ Utility.getCalendarDateTimeString(calendar)
				+ " triggerMillis=" + triggerMillis
				+ " intervalMillis=" + mIntervalMillis);

		mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				triggerMillis, mIntervalMillis, mPendingIntent);
	}

	public void stopAlarm() {
		if ((mAlarmManager == null) || (mPendingIntent == null)) {
			Log.d("return, mAlarmManager = " + mAlarmManager
					+ " mPendingIntent = " + mPendingIntent);
			return;
		}

		mAlarmManager.cancel(mPendingIntent);
	}
}
