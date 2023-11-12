package com.android.orion.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.orion.service.OrionService;
import com.android.orion.utility.Market;

import java.util.Calendar;

public class DownloadBroadcastReceiver extends BroadcastReceiver {
	public static final String TAG = DownloadBroadcastReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive intent:" + intent);

		if (Market.isTradingHours(Calendar.getInstance())) {
			OrionService.getInstance().download();
		}
	}
}
