package com.android.orion.application;

import android.app.Application;
import android.content.Context;

public class OrionApplication extends Application {

	public static Context mContext;
	public static OrionApplication mInstance;

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
	}
}
