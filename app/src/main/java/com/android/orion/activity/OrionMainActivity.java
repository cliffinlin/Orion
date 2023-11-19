package com.android.orion.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.orion.R;
import com.android.orion.manager.DownloadAlarmManager;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Preferences;

import java.util.List;

public class OrionMainActivity extends PreferenceActivity {
	DownloadAlarmManager mStockDownloadAlarmManager = null;
	private long mExitTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initSharepreference();

		mStockDownloadAlarmManager = DownloadAlarmManager.getInstance(this);

		if (mStockDownloadAlarmManager != null) {
			mStockDownloadAlarmManager.startAlarm();
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
			case R.id.action_exit: {
				onActionExit();
				return true;
			}
			case android.R.id.home:
				finish();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(this,
						getResources().getString(R.string.press_again_to_exit),
						Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				onActionExit();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	void onActionExit() {
		if (mStockDownloadAlarmManager != null) {
			mStockDownloadAlarmManager.stopAlarm();
		}

		finish();
	}

	void initSharepreference() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);

		if (!Preferences.getBoolean(this, Setting.KEY_PREFERENCES_INIT, false)) {
			Preferences.putBoolean(this, Setting.KEY_PREFERENCES_INIT, true);

			Preferences.putBoolean(this, Setting.KEY_NOTIFICATION, true);

			Preferences.putBoolean(this, Setting.KEY_PERIOD_DAY, true);
			Preferences.putBoolean(this, Setting.KEY_PERIOD_MIN60, true);
			Preferences.putBoolean(this, Setting.KEY_PERIOD_MIN30, true);
			Preferences.putBoolean(this, Setting.KEY_PERIOD_MIN15, true);
			Preferences.putBoolean(this, Setting.KEY_PERIOD_MIN5, true);

			Preferences.putBoolean(this, Setting.KEY_DISPLAY_NET, true);
			Preferences.putBoolean(this, Setting.KEY_DISPLAY_THRESHOLD, true);
			Preferences.putBoolean(this, Setting.KEY_DISPLAY_DRAW, true);
			Preferences.putBoolean(this, Setting.KEY_DISPLAY_STROKE, true);
			Preferences.putBoolean(this, Setting.KEY_DISPLAY_SEGMENT, true);
			Preferences.putBoolean(this, Setting.KEY_DISPLAY_LINE, true);
			Preferences.putBoolean(this, Setting.KEY_DISPLAY_LATEST, true);
			Preferences.putBoolean(this, Setting.KEY_DISPLAY_COST, true);

			Preferences.putBoolean(this, Setting.KEY_INDEXES_WEIGHT, true);

			Preferences.putBoolean(this, Setting.KEY_LOOPBACK, false);
		}
	}
}