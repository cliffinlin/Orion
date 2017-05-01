package com.android.orion;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class OrionManager {

	private static final OrionManager mInstance = new OrionManager();

	IOrionService mOrionService = null;

	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mOrionService = IOrionService.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mOrionService = null;
		}
	};

	private OrionManager() {
	}

	public OrionManager getInstance() {
		return mInstance;
	}

	public IOrionService getService() {
		return mOrionService;
	}
}
