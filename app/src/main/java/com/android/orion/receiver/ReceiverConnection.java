package com.android.orion.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;

import com.android.orion.manager.ConnectionManager;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Utility;

public class ReceiverConnection extends BroadcastReceiver {
	public static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

	private static ReceiverConnection mInstance;
	private static boolean mRegistered = false;

	Logger Log = Logger.getLogger();

	public static ReceiverConnection getInstance() {
		synchronized (ReceiverConnection.class) {
			if (mInstance == null) {
				mInstance = new ReceiverConnection();
			}
		}
		return mInstance;
	}

	public void registerReceiver(@NonNull Context context) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_CONNECTIVITY_CHANGE);
		context.registerReceiver(this, intentFilter);
		mRegistered = true;
	}

	public void unregisterReceiver(@NonNull Context context) {
		if (mRegistered) {
			context.unregisterReceiver(this);
			mRegistered = false;
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("onReceive intent=" + intent);

		if (intent.getAction().equals(ACTION_CONNECTIVITY_CHANGE)) {
			if (Utility.isNetworkConnected(context)) {
				ConnectionManager.getInstance().onConnected();
			} else {
				ConnectionManager.getInstance().onDisconnected();
			}
		}
	}
}