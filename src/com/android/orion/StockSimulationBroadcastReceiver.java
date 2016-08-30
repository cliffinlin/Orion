package com.android.orion;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.orion.utility.Utility;

public class StockSimulationBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!Utility.isTradingHours(Calendar.getInstance())) {
			int executeType = Constants.EXECUTE_SCHEDULE_SIMULATION;

			Utility.Log("System.currentTimeMillis():"
					+ System.currentTimeMillis());

			Intent serviceIntent = new Intent(context, OrionService.class);
			serviceIntent.putExtra(Constants.EXTRA_KEY_SERVICE_TYPE,
					Constants.SERVICE_SIMULATE_STOCK_FAVORITE);
			serviceIntent.putExtra(Constants.EXTRA_KEY_EXECUTE_TYPE,
					executeType);
			context.startService(serviceIntent);
		}
	}
}
