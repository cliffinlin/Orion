package com.android.orion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.android.orion.utility.Utility;

public class SettingFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	DownloadAlarmManager mStockDownloadAlarmManager = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mStockDownloadAlarmManager = DownloadAlarmManager
				.getInstance(getActivity());

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
				|| key.equals(Settings.KEY_NOTIFICATION_OPERATE)
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
			if (checked) {
				if (mStockDownloadAlarmManager != null) {
					mStockDownloadAlarmManager.startAlarm();
				}
			}

			Intent intent = new Intent(getActivity(), OrionService.class);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				getActivity().startForegroundService(intent);
			} else {
				getActivity().startService(intent);
			}
		}
	}
}
