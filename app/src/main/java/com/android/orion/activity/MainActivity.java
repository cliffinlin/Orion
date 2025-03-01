package com.android.orion.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.orion.R;
import com.android.orion.application.MainApplication;
import com.android.orion.data.Period;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Market;
import com.android.orion.utility.Utility;

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
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
		if (!Utility.isNetworkConnected(this)) {
			Toast.makeText(this,
					getResources().getString(R.string.network_unavailable),
					Toast.LENGTH_SHORT).show();
		}
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
		PreferenceManager.getDefaultSharedPreferences(this);
		if (!Setting.getPreferenceInit()) {
			Setting.setPreferenceInit(true);

			Setting.setPeriod(Period.MONTH, Setting.SETTING_PERIOD_MONTH_DEFAULT);
			Setting.setPeriod(Period.WEEK, Setting.SETTING_PERIOD_WEEK_DEFAULT);
			Setting.setPeriod(Period.DAY, Setting.SETTING_PERIOD_DAY_DEFAULT);
			Setting.setPeriod(Period.MIN60, Setting.SETTING_PERIOD_MIN60_DEFAULT);
			Setting.setPeriod(Period.MIN30, Setting.SETTING_PERIOD_MIN30_DEFAULT);
			Setting.setPeriod(Period.MIN15, Setting.SETTING_PERIOD_MIN15_DEFAULT);
			Setting.setPeriod(Period.MIN5, Setting.SETTING_PERIOD_MIN5_DEFAULT);

			Setting.setDisplayNet(Setting.SETTING_DISPLAY_NET_DEFAULT);
			Setting.setDisplayDraw(Setting.SETTING_DISPLAY_DRAW_DEFAULT);
			Setting.setDisplayStroke(Setting.SETTING_DISPLAY_STROKE_DEFAULT);
			Setting.setDisplaySegment(Setting.SETTING_DISPLAY_SEGMENT_DEFAULT);
			Setting.setDisplayLine(Setting.SETTING_DISPLAY_LINE_DEFAULT);
			Setting.setDisplayOutline(Setting.SETTING_DISPLAY_OUTLINE_DEFAULT);

			Setting.setNotifyDraw(Setting.SETTING_NOTIFY_DRAW_DEFAULT);
			Setting.setNotifyStroke(Setting.SETTING_NOTIFY_STROKE_DEFAULT);
			Setting.setNotifySegment(Setting.SETTING_NOTIFY_SEGMENT_DEFAULT);
			Setting.setNotifyLine(Setting.SETTING_NOTIFY_LINE_DEFAULT);

			Setting.setDebugLog(Setting.SETTING_DEBUG_LOG_DEFAULT);
			Setting.setDebugDataFile(Setting.SETTING_DEBUG_DATAFILE_DEFAULT);
		}
	}
}