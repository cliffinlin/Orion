package com.android.orion.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.android.orion.R;
import com.android.orion.activity.SettingLoopbackActivity;
import com.android.orion.service.OrionService;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;

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
		if (key.equals(Setting.KEY_DEBUG_LOG)) {
			Logger.setDebug(sharedPreferences.getBoolean(Setting.KEY_DEBUG_LOG, false));
		} else if (key.equals(Setting.KEY_LOOPBACK)) {
			Intent intent = new Intent(mContext, SettingLoopbackActivity.class);
			mContext.startActivity(intent);
		} else {
			OrionService.getInstance().download();
		}
	}
}
