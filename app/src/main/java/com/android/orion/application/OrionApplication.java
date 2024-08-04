package com.android.orion.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.android.orion.manager.StockAlarmManager;
import com.android.orion.service.OrionService;
import com.android.orion.utility.Logger;

public class OrionApplication extends Application {

	public static Context mContext;
	private static OrionApplication mInstance;
	Logger Log;
	int mActivityStartedCounter = 0;
	long mBackgroundTimeMillis = 0;

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

		Log = Logger.getLogger();

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
		Intent serviceIntent = new Intent(mContext, OrionService.class);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForegroundService(serviceIntent);
		} else {
			startService(serviceIntent);
		}
	}

	public void stopService() {
		Log.d("stopService");
		Intent serviceIntent = new Intent(mContext, OrionService.class);
		stopService(serviceIntent);
	}
}
