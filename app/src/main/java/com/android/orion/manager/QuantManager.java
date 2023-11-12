package com.android.orion.manager;

public class QuantManager {
	public static final String TAG = QuantManager.class.getSimpleName();

	private static QuantManager mInstance;

	public static QuantManager getInstance() {
		synchronized (QuantManager.class) {
			if (mInstance == null) {
				mInstance = new QuantManager();
			}
		}
		return mInstance;
	}
}
