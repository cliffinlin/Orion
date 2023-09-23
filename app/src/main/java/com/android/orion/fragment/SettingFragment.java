package com.android.orion.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.android.orion.R;
import com.android.orion.activity.SettingLoopbackActivity;
import com.android.orion.setting.Setting;
import com.android.orion.service.OrionService;

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

		if (key.equals(Setting.KEY_NOTIFICATION_OPERATE)
				|| key.equals(Setting.KEY_PERIOD_MIN1)
				|| key.equals(Setting.KEY_PERIOD_MIN5)
				|| key.equals(Setting.KEY_PERIOD_MIN15)
				|| key.equals(Setting.KEY_PERIOD_MIN30)
				|| key.equals(Setting.KEY_PERIOD_MIN60)
				|| key.equals(Setting.KEY_PERIOD_DAY)
				|| key.equals(Setting.KEY_PERIOD_WEEK)
				|| key.equals(Setting.KEY_PERIOD_MONTH)
				|| key.equals(Setting.KEY_PERIOD_QUARTER)
				|| key.equals(Setting.KEY_PERIOD_YEAR)) {

			if (checked) {
				OrionService.startService(mContext);
			}
		}

		if (key.equals(Setting.KEY_LOOPBACK)) {
			Intent intent = new Intent(mContext, SettingLoopbackActivity.class);

			if (checked) {
				mContext.startActivity(intent);
			}
		}
	}
}
