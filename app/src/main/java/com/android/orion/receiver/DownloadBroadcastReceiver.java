package com.android.orion.receiver;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.orion.setting.Constants;
import com.android.orion.service.OrionService;
import com.android.orion.utility.Market;

public class DownloadBroadcastReceiver extends BroadcastReceiver {
	static final String TAG = Constants.TAG + " "
			+ DownloadBroadcastReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive intent:" + intent);

		if (Market.isTradingHours(Calendar.getInstance())) {
			OrionService.startService(context);
		}
	}
}
