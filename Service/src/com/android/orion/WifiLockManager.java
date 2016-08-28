package com.android.orion;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

public class WifiLockManager {

	WifiManager mWifiManager = null;
	WifiLock mWifiLock = null;

	public WifiLockManager(Context context) {
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
		}

		if (mWifiManager != null) {
			if (mWifiLock == null) {
				mWifiLock = mWifiManager.createWifiLock(
						WifiManager.WIFI_MODE_FULL, Constants.TAG);
			}
		}
	}

	public void acquire() {
		if (mWifiLock != null) {
			mWifiLock.acquire();
		}
	}

	public void release() {
		if (mWifiLock != null) {
			mWifiLock.release();
		}
	}
}
