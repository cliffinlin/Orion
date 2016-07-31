package com.android.orion;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.android.orion.utility.Utility;

public class StockSimulationAlarmManager extends OrionAlarmManager {
	private static StockSimulationAlarmManager mInstance = null;

	public static synchronized StockSimulationAlarmManager getInstance(
			Context context) {
		if (mInstance == null) {
			mInstance = new StockSimulationAlarmManager(context);
		}
		return mInstance;
	}

	private StockSimulationAlarmManager(Context context) {
		super(context);
	}

	@Override
	void startAlarm() {
		String intervalString = "";

		intervalString = Utility.getSettingString(mContext,
				Constants.SETTING_KEY_SIMULATION_INTERVAL);

		if (TextUtils.isEmpty(intervalString)) {
			intervalString = String
					.valueOf(Constants.STOCK_SIMULATION_ALARM_INTERVAL_DEFAULT);
		}

		setIntervalMillis(Long.valueOf(intervalString));

		setPendingIntent(PendingIntent.getBroadcast(mContext, 0, new Intent(
				mContext, StockSimulationBroadcastReceiver.class), 0));

		super.startAlarm();
	}
}
