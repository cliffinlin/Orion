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
		if (Market.isTradingHours(Calendar.getInstance())) {
			Log.d(TAG, "onReceive intent:" + intent);

			Intent serviceIntent = new Intent(context, OrionService.class);
			context.startService(serviceIntent);
		}
	}
}
