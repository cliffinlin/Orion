package com.android.orion.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.android.orion.service.OrionService;

public class OrionApplication extends Application {

	public static Context mContext;
	private static OrionApplication mInstance;

	public static Context getContext() {
		return mContext;
	}

	public static OrionApplication getInstance() {
		return mInstance;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mContext = getApplicationContext();
		mInstance = this;

		startService();
	}

	public void startService() {
		Intent serviceIntent = new Intent(mContext, OrionService.class);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForegroundService(serviceIntent);
		} else {
			startService(serviceIntent);
		}
	}
}
