package com.android.orion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OrionBroadcastReceiver extends BroadcastReceiver {
	static final String TAG = Constants.TAG + " "
			+ OrionBroadcastReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "OrionBroadcastReceiver onReceive");

//		Intent i = new Intent(Intent.ACTION_MAIN);
//		i.setClass(context, StockFavoriteListActivity.class);
//		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//		context.startActivity(i);
	}
}
