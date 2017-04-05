package com.android.orion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.orion.utility.Utility;

public class OrionBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Utility.Log("OrionBroadcastReceiver onReceive");

		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setClass(context, StockFavoriteListActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		context.startActivity(i);
	}
}
