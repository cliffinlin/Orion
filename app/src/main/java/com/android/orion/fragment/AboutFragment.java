package com.android.orion.fragment;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.android.orion.R;

public class AboutFragment extends PreferenceFragment {
	Context mContext = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = getActivity();

		addPreferencesFromResource(R.xml.preference_about);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		String versionInfo = "";
		String versionName = "";
		int versionCode = 0;

		try {
			versionName = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), 0).versionName;
			versionCode = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), 0).versionCode;

			Preference preference = findPreference("version");

			if (preference != null) {
				versionInfo = versionName + " build "
						+ versionCode;

				preference.setSummary(versionInfo);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
