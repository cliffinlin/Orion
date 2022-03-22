package com.android.orion;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = getActivity();

		addPreferencesFromResource(R.xml.preference_setting);
	}

	@Override
	public void onPause() {
		super.onPause();

		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		boolean checked;

		checked = sharedPreferences.getBoolean(key, true);

		if (key.equals(Settings.KEY_NOTIFICATION_MESSAGE)
				|| key.equals(Settings.KEY_PERIOD_MIN1)
				|| key.equals(Settings.KEY_PERIOD_MIN5)
				|| key.equals(Settings.KEY_PERIOD_MIN15)
				|| key.equals(Settings.KEY_PERIOD_MIN30)
				|| key.equals(Settings.KEY_PERIOD_MIN60)
				|| key.equals(Settings.KEY_PERIOD_DAY)
				|| key.equals(Settings.KEY_PERIOD_WEEK)
				|| key.equals(Settings.KEY_PERIOD_MONTH)
				|| key.equals(Settings.KEY_PERIOD_QUARTER)
				|| key.equals(Settings.KEY_PERIOD_YEAR)) {

			Intent serviceIntent = new Intent(mContext, OrionService.class);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				mContext.startForegroundService(serviceIntent);
			} else {
				mContext.startService(serviceIntent);
			}
		}

		if (key.equals(Settings.KEY_BACKTEST)) {
			Intent intent = new Intent(mContext, SettingBacktestActivity.class);

			if (checked) {
				intent.putExtra(SettingBacktestActivity.EXTRA_BACK_TEST, SettingBacktestActivity.EXTRA_BACK_TEST_ON);
				mContext.startActivity(intent);
			} else {
				intent.putExtra(SettingBacktestActivity.EXTRA_BACK_TEST, SettingBacktestActivity.EXTRA_BACK_TEST_OFF);
				mContext.startActivity(intent);
			}
		}
	}
}
