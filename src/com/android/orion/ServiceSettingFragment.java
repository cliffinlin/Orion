package com.android.orion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.android.orion.utility.Utility;

public class ServiceSettingFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	DownloadAlarmManager mStockDownloadAlarmManager = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

		mStockDownloadAlarmManager = DownloadAlarmManager
				.getInstance(getActivity());

		addPreferencesFromResource(R.xml.preference_service_setting);
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

		remindNetworkConnection();

		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		boolean bChecked;

		bChecked = sharedPreferences.getBoolean(key, true);
		if (key.equals(Constants.SETTING_KEY_ALARM)) {
			if (bChecked) {
				remindNetworkConnection();

				if (mStockDownloadAlarmManager != null) {
					mStockDownloadAlarmManager.startAlarm();
				}
			} else {
				if (mStockDownloadAlarmManager != null) {
					mStockDownloadAlarmManager.stopAlarm();
				}
			}
		} else if (key.equals(Constants.PERIOD_MIN1)
				|| key.equals(Constants.PERIOD_MIN5)
				|| key.equals(Constants.PERIOD_MIN15)
				|| key.equals(Constants.PERIOD_MIN30)
				|| key.equals(Constants.PERIOD_MIN60)
				|| key.equals(Constants.PERIOD_DAY)
				|| key.equals(Constants.PERIOD_WEEK)
				|| key.equals(Constants.PERIOD_MONTH)
				|| key.equals(Constants.PERIOD_QUARTER)
				|| key.equals(Constants.PERIOD_YEAR)) {
			if (bChecked) {
				Intent intent = new Intent(getActivity(), OrionService.class);
				intent.putExtra(Constants.EXTRA_SERVICE_TYPE,
						Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE);
				intent.putExtra(Constants.EXTRA_EXECUTE_TYPE,
						Constants.EXECUTE_IMMEDIATE);
				getActivity().startService(intent);
			}
		}
	}

	void remindNetworkConnection() {
		if (!Utility.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(),
					getResources().getString(R.string.network_unavailable),
					Toast.LENGTH_SHORT).show();
		}
	}
}
