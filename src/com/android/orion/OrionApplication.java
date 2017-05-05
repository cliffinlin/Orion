package com.android.orion;

import com.android.orion.leancloud.LeanCloudContract;
import com.avos.avoscloud.AVOSCloud;

import android.app.Application;

public class OrionApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		AVOSCloud.initialize(this, LeanCloudContract.APPLICATION_ID,
				LeanCloudContract.CLIENT_KEY);
	}
}
