package com.android.orion.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.orion.config.Config;
import com.android.orion.manager.DownloadAlarmManager;
import com.android.orion.service.OrionService;
import com.android.orion.utility.Logger;

public class OrionApplication extends Application {

	public static Context mContext;
	private static OrionApplication mInstance;
	Logger Log;
	DownloadAlarmManager mStockDownloadAlarmManager = null;
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

		mStockDownloadAlarmManager = DownloadAlarmManager.getInstance();
		mStockDownloadAlarmManager.startAlarm();

		startService();

		registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
			@Override
			public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

			}

			@Override
			public void onActivityStarted(@NonNull Activity activity) {
				mActivityStartedCounter++;
				Log.d("mActivityStartedCounter=" + mActivityStartedCounter);
				if (mActivityStartedCounter > 0) {
					mBackgroundTimeMillis = 0;
					Log.d("mBackgroundTimeMillis=" + mBackgroundTimeMillis);
				}
			}

			@Override
			public void onActivityResumed(@NonNull Activity activity) {

			}

			@Override
			public void onActivityPaused(@NonNull Activity activity) {

			}

			@Override
			public void onActivityStopped(@NonNull Activity activity) {
				mActivityStartedCounter--;
				Log.d("mActivityStartedCounter=" + mActivityStartedCounter);
				if (mActivityStartedCounter == 0) {
					mBackgroundTimeMillis = System.currentTimeMillis();
					Log.d("mBackgroundTimeMillis=" + mBackgroundTimeMillis);
				}
			}

			@Override
			public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

			}

			@Override
			public void onActivityDestroyed(@NonNull Activity activity) {

			}
		});
	}

	@Override
	public void onTerminate() {
		Log.d("onTerminate");
		if (mStockDownloadAlarmManager != null) {
			mStockDownloadAlarmManager.stopAlarm();
		}
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

	public void onBackground() {
		if (mActivityStartedCounter == 0) {
			Log.d("mActivityStartedCounter=" + mActivityStartedCounter);
			if (System.currentTimeMillis() - mBackgroundTimeMillis > Config.backgroundTerminate) {
				Log.d("System.currentTimeMillis() - mBackgroundTimeMillis > " + Config.backgroundTerminate);
				onTerminate();
			}
		}
	}
}
