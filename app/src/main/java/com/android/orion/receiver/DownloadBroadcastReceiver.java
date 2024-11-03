package com.android.orion.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.orion.manager.StockAlarmManager;
import com.android.orion.provider.StockDataProvider;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Market;

public class DownloadBroadcastReceiver extends BroadcastReceiver {

	Logger Log = Logger.getLogger();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("intent:" + intent);
		try {
			StockAlarmManager.getInstance().stopAlarm();
			if (Market.isTradingHours()) {
				StockDataProvider.getInstance().download();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			StockAlarmManager.getInstance().startAlarm();
		}
	}
}
