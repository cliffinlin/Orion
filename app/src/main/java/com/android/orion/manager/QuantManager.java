package com.android.orion.manager;

import com.android.orion.setting.Constant;


public class QuantManager {
	static final String TAG = Constant.TAG + " "
			+ QuantManager.class.getSimpleName();

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
