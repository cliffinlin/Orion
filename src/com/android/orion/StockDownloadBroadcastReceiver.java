package com.android.orion;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.orion.utility.Utility;

public class StockDownloadBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		int scheduleMinutes = 0;
		int executeType = Constants.EXECUTE_SCHEDULE_1MIN;

		if (Utility.isTradingHours(Calendar.getInstance())) {
			Utility.Log("System.currentTimeMillis():"
					+ System.currentTimeMillis());

			scheduleMinutes = Utility.getScheduleMinutes();
			if ((scheduleMinutes % Constants.SCHEDULE_INTERVAL_60MIN) == 0) {
				executeType |= Constants.EXECUTE_SCHEDULE_60MIN;
			} else if ((scheduleMinutes % Constants.SCHEDULE_INTERVAL_30MIN) == 0) {
				executeType |= Constants.EXECUTE_SCHEDULE_30MIN;
			} else if ((scheduleMinutes % Constants.SCHEDULE_INTERVAL_15MIN) == 0) {
				executeType |= Constants.EXECUTE_SCHEDULE_15MIN;
			} else if ((scheduleMinutes % Constants.SCHEDULE_INTERVAL_5MIN) == 0) {
				executeType |= Constants.EXECUTE_SCHEDULE_5MIN;
			}

			Intent serviceIntent = new Intent(context, OrionService.class);
			serviceIntent.putExtra(Constants.EXTRA_SERVICE_TYPE,
					Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE);
			serviceIntent.putExtra(Constants.EXTRA_EXECUTE_TYPE, executeType);
			context.startService(serviceIntent);
		}
	}
}
