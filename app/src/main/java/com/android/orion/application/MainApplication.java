package com.android.orion.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import com.android.orion.config.Config;
import com.android.orion.manager.StockAlarmManager;
import com.android.orion.service.StockService;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Utility;

public class MainApplication extends Application {

	public static Context mContext;
	private static MainApplication mInstance;
	Logger Log;
	int mActivityStartedCounter = 0;
	long mBackgroundTimeMillis = 0;

	public static Context getContext() {
		return mContext;
	}

	public static MainApplication getInstance() {
		return mInstance;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mContext = getApplicationContext();
		mInstance = this;
		Log = Logger.getLogger();

		Utility.createDirectory(Environment.getExternalStorageDirectory() + "/" + Config.APP_NAME);

		StockAlarmManager.getInstance().startAlarm();

		startService();

		registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
			@Override
			public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

			}

			@Override
			public void onActivityStarted(Activity activity) {
				mActivityStartedCounter++;
				Log.d("mActivityStartedCounter=" + mActivityStartedCounter);
				if (mActivityStartedCounter > 0) {
					mBackgroundTimeMillis = 0;
				}
			}

			@Override
			public void onActivityResumed(Activity activity) {

			}

			@Override
			public void onActivityPaused(Activity activity) {

			}

			@Override
			public void onActivityStopped(Activity activity) {
				mActivityStartedCounter--;
				Log.d("mActivityStartedCounter=" + mActivityStartedCounter);
				if (mActivityStartedCounter == 0) {
					mBackgroundTimeMillis = System.currentTimeMillis();
				}
			}

			@Override
			public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

			}

			@Override
			public void onActivityDestroyed(Activity activity) {

			}
		});
	}

	@Override
	public void onTerminate() {
		Log.d("onTerminate");
		StockAlarmManager.getInstance().stopAlarm();
		stopService();

		super.onTerminate();
	}

	public void startService() {
		Log.d("startService");
		Intent serviceIntent = new Intent(mContext, StockService.class);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForegroundService(serviceIntent);
		} else {
			startService(serviceIntent);
		}
	}

	public void stopService() {
		Log.d("stopService");
		Intent serviceIntent = new Intent(mContext, StockService.class);
		stopService(serviceIntent);
	}
}
