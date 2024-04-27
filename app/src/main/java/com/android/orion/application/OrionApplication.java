package com.android.orion.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.orion.manager.DownloadAlarmManager;
import com.android.orion.service.OrionService;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Market;

import java.util.Calendar;

public class OrionApplication extends Application {

	public static Context mContext;
	private static OrionApplication mInstance;
	DownloadAlarmManager mStockDownloadAlarmManager = null;
	int mActivityStartedCounter = 0;

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
				if (mActivityStartedCounter == 0) {
					if (!Market.isTradingHours(Calendar.getInstance())) {
						Toast.makeText(mContext, "后台退出", Toast.LENGTH_LONG).show();
						onTerminate();
					}
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
		super.onTerminate();

		Logger.getLogger().d("onTerminate");
		if (mStockDownloadAlarmManager != null) {
			mStockDownloadAlarmManager.stopAlarm();
		}
		stopService();
	}

	public void startService() {
		Logger.getLogger().d("startService");
		Intent serviceIntent = new Intent(mContext, OrionService.class);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForegroundService(serviceIntent);
		} else {
			startService(serviceIntent);
		}
	}

	public void stopService() {
		Logger.getLogger().d("stopService");
		Intent serviceIntent = new Intent(mContext, OrionService.class);
		stopService(serviceIntent);
	}
}
