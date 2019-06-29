package com.android.orion;

import java.util.List;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.orion.utility.Preferences;

public class OrionMainActivity extends PreferenceActivity {
	StockDownloadAlarmManager mStockDownloadAlarmManager = null;
	private long mExitTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initSharepreference();

		if (mStockDownloadAlarmManager == null) {
			mStockDownloadAlarmManager = StockDownloadAlarmManager
					.getInstance(this);
		}

		if (mStockDownloadAlarmManager != null) {
			boolean bChecked = Preferences.readBoolean(this,
					Constants.SETTING_KEY_ALARM, false);

			if (bChecked) {
				mStockDownloadAlarmManager.startAlarm();
			} else {
				mStockDownloadAlarmManager.stopAlarm();
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
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
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
		SharedPreferences.Editor editor = settings.edit();

		if (!settings.contains(Constants.SETTING_KEY_ALARM)) {
			editor.putBoolean(Constants.SETTING_KEY_ALARM, true);
			editor.putBoolean(Constants.PERIOD_MONTH, true);
			editor.putBoolean(Constants.PERIOD_WEEK, true);
			editor.putBoolean(Constants.PERIOD_DAY, true);
			editor.commit();
		}
	}
}