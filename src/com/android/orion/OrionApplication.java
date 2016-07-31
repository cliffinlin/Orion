package com.android.orion;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;

public class OrionApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		AVOSCloud.initialize(this, LeanCloudContract.APPLICATION_ID,
				LeanCloudContract.CLIENT_KEY);
	}
}
