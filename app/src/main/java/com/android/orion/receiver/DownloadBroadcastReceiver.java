package com.android.orion.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.orion.provider.StockDataProvider;
import com.android.orion.service.StockService;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Market;

import java.util.Calendar;

public class DownloadBroadcastReceiver extends BroadcastReceiver {

	Logger Log = Logger.getLogger();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("intent:" + intent);

		if (Market.isTradingHours(Calendar.getInstance())) {
			StockDataProvider.getInstance().download();
		}
	}
}
