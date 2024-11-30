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
	private static StockAlarmManager mInstance;
	Logger Log = Logger.getLogger();
	Context mContext;
	private final long mIntervalMillis = Config.alarmInterval;
	private AlarmManager mAlarmManager = null;
	private final PendingIntent mPendingIntent;

	StockAlarmManager() {
		mContext = MainApplication.getContext();

		if (mAlarmManager == null) {
			mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		}

		mPendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(
				mContext, DownloadBroadcastReceiver.class), 0);
	}

	public static synchronized StockAlarmManager getInstance() {
		if (mInstance == null) {
			mInstance = new StockAlarmManager();
		}
		return mInstance;
	}

	public void startAlarm() {

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
				calendar.add(Calendar.MILLISECOND, (int) mIntervalMillis);
			} else if (Market.isLunchTime()) {
				calendar = Market.getSecondHalfStartCalendar();
			} else if (Market.isSecondHalf()) {
				calendar = Calendar.getInstance();
				calendar.add(Calendar.MILLISECOND, (int) mIntervalMillis);
			} else if (Market.afterClosed()) {
				if (dayOfWeek == Calendar.FRIDAY) {
					//TODO
					return;
				} else {
					//TODO
					return;
				}
			}
		} else {
			//TODO
			return;
		}

		long triggerMillis = calendar.getTimeInMillis();

		Log.d("Alarm will arrive at "
				+ Utility.getCalendarDateTimeString(calendar)
				+ " triggerMillis=" + triggerMillis
				+ " intervalMillis=" + mIntervalMillis);

		mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				triggerMillis, mIntervalMillis, mPendingIntent);
	}

	public void stopAlarm() {
		Log.d("cancel");

		if ((mAlarmManager == null) || (mPendingIntent == null)) {
			Log.d("return, mAlarmManager = " + mAlarmManager
					+ " mPendingIntent = " + mPendingIntent);
			return;
		}

		mAlarmManager.cancel(mPendingIntent);
	}
}
