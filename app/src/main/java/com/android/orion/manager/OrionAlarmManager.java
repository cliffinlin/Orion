package com.android.orion.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import com.android.orion.application.OrionApplication;
import com.android.orion.config.Config;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Market;
import com.android.orion.utility.Utility;

import java.util.Calendar;

public class OrionAlarmManager {

	Logger Log = Logger.getLogger();
	Context mContext;
	private long mIntervalMillis = 0;
	private AlarmManager mAlarmManager = null;
	private PendingIntent mPendingIntent = null;

	OrionAlarmManager() {
		mContext = OrionApplication.getContext();

		if (mAlarmManager == null) {
			mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		}
	}

	void setIntervalMillis(long intervalMillis) {
		if (intervalMillis <= 0) {
			mIntervalMillis = Config.alarmInterval;
		} else {
			mIntervalMillis = intervalMillis;
		}

		Log.d("mIntervalMillis = "
				+ mIntervalMillis);
	}

	void setPendingIntent(PendingIntent pendingIntent) {
		mPendingIntent = pendingIntent;
	}

	void startAlarm() {
		int dayOfWeek = Calendar.SUNDAY;
		long triggerMillis = 0;

		stopAlarm();

		if ((mAlarmManager == null) || (mPendingIntent == null)
				|| (mIntervalMillis <= 0)) {
			Log.d("return, mAlarmManager = "
					+ mAlarmManager + " mPendingIntent = " + mPendingIntent
					+ " mIntervalMillis = " + mIntervalMillis);
			return;
		}

		Calendar calendar = Calendar.getInstance();

		dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

		if (Market.isWeekday(calendar)) {
			if (Market.beforeOpen(calendar)) {
				calendar = Market.getMarketOpenCalendar(calendar);
			} else if (Market.inFirstHalf(calendar)) {
				calendar = Calendar.getInstance();
			} else if (Market.isLunchTime(calendar)) {
				calendar = Market.getMarketLunchEndCalendar(calendar);
			} else if (Market.inSecondHalf(calendar)) {
				calendar = Calendar.getInstance();
			} else if (Market.afterClosed(calendar)) {
				if (dayOfWeek < Calendar.FRIDAY) {
					calendar.add(Calendar.DAY_OF_WEEK, 1);
				} else {
					calendar.add(Calendar.DAY_OF_WEEK, 3);
				}
				calendar = Market.getMarketOpenCalendar(calendar);
			}
		} else {
			if (dayOfWeek == Calendar.SATURDAY) {
				calendar.add(Calendar.DAY_OF_WEEK, 2);
			} else if (dayOfWeek == Calendar.SUNDAY) {
				calendar.add(Calendar.DAY_OF_WEEK, 1);
			}
			calendar = Market.getMarketOpenCalendar(calendar);
		}

		triggerMillis = calendar.getTimeInMillis();

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
