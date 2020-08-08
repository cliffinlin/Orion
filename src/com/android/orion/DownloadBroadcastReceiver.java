package com.android.orion;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.orion.utility.Market;

public class DownloadBroadcastReceiver extends BroadcastReceiver {
	static final String TAG = Constants.TAG + " "
			+ DownloadBroadcastReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		int scheduleMinutes = 0;
		int executeType = Constants.EXECUTE_SCHEDULE;

		if (Market.isTradingHours(Calendar.getInstance())) {
			Log.d(TAG,
					"System.currentTimeMillis():" + System.currentTimeMillis());

			scheduleMinutes = Market.getScheduleMinutes();
			if ((scheduleMinutes % Constants.SCHEDULE_INTERVAL_MIN60) == 0) {
				executeType |= Constants.EXECUTE_SCHEDULE_MIN60;
			} else if ((scheduleMinutes % Constants.SCHEDULE_INTERVAL_MIN30) == 0) {
				executeType |= Constants.EXECUTE_SCHEDULE_MIN30;
			} else if ((scheduleMinutes % Constants.SCHEDULE_INTERVAL_MIN15) == 0) {
				executeType |= Constants.EXECUTE_SCHEDULE_MIN15;
			} else if ((scheduleMinutes % Constants.SCHEDULE_INTERVAL_MIN5) == 0) {
				executeType |= Constants.EXECUTE_SCHEDULE_MIN5;
			}

			Intent serviceIntent = new Intent(context, OrionService.class);
			serviceIntent.putExtra(Constants.EXTRA_SERVICE_TYPE,
					Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE);
			serviceIntent.putExtra(Constants.EXTRA_EXECUTE_TYPE, executeType);
			context.startService(serviceIntent);
		}
	}
}
