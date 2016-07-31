package com.android.orion;

import java.util.List;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.android.orion.utility.Utility;
import com.avos.avoscloud.AVUser;

public class OrionMainActivity extends PreferenceActivity {
	StockDownloadAlarmManager mStockDownloadAlarmManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (mStockDownloadAlarmManager == null) {
			mStockDownloadAlarmManager = StockDownloadAlarmManager
					.getInstance(this);
			boolean bChecked = Utility.getSettingBoolean(this,
					Constants.SETTING_KEY_ALARM);

			if (mStockDownloadAlarmManager != null) {
				if (bChecked) {
					mStockDownloadAlarmManager.startAlarm();
				} else {
					mStockDownloadAlarmManager.stopAlarm();
				}
			}
		}
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		return true;
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		super.onBuildHeaders(target);
		loadHeadersFromResource(R.xml.preference_headers, target);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		case R.id.action_logout:
			AVUser.logOut();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}