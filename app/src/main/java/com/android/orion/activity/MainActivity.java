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
import com.android.orion.application.MainApplication;
import com.android.orion.database.DatabaseContract;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Market;
import com.android.orion.utility.Preferences;

import java.util.List;

public class MainActivity extends PreferenceActivity {

	private long mExitTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initSharedPreferences();
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
			case R.id.action_setting: {
				return true;
			}
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
				if (!Market.isTradingHours()) {
					Toast.makeText(this,
							getResources().getString(R.string.press_again_to_exit),
							Toast.LENGTH_SHORT).show();
				}
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
		finish();

		if (!Market.isTradingHours()) {
			MainApplication.getInstance().onTerminate();
		}
	}

	void initSharedPreferences() {
		SharedPreferences setting = PreferenceManager
				.getDefaultSharedPreferences(this);

		if (!Preferences.getBoolean(Setting.SETTING_PREFERENCES_INIT, false)) {
			Preferences.putBoolean(Setting.SETTING_PREFERENCES_INIT, true);

			Preferences.putBoolean(DatabaseContract.COLUMN_MONTH, true);
			Preferences.putBoolean(DatabaseContract.COLUMN_WEEK, true);
			Preferences.putBoolean(DatabaseContract.COLUMN_DAY, true);
			Preferences.putBoolean(DatabaseContract.COLUMN_MIN60, true);
			Preferences.putBoolean(DatabaseContract.COLUMN_MIN30, true);
			Preferences.putBoolean(DatabaseContract.COLUMN_MIN15, true);
			Preferences.putBoolean(DatabaseContract.COLUMN_MIN5, true);

			Setting.setDisplayNet(true);

			Setting.setDisplayDraw(true);
			Setting.setDisplayStroke(true);
			Setting.setDisplaySegment(true);
			Setting.setDisplayLine(true);

			Setting.setDebugLoopback(false);
		}
	}
}