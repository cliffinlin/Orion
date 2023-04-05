package com.android.orion.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.android.orion.R;
import com.android.orion.setting.Settings;
import com.android.orion.utility.Preferences;

import java.util.Calendar;

public class SettingBacktestActivity extends BaseActivity {

	static final int MESSAGE_BACK_TEST_ON = 0;
	static final int MESSAGE_BACK_TEST_OFF = 1;

	public static final String EXTRA_BACK_TEST = "back_test";
	public static final int EXTRA_BACK_TEST_ON = 0;
	public static final int EXTRA_BACK_TEST_OFF = 1;

	String DEFAULT_TIME_STRING = "00:00:00";
	int DEFAULT_HOUR_OF_DAY = 15;
	int DEFAULT_MINUTE = 0;

	boolean is24HourView = true;

	int mYear;
	int mMonthOfYear;
	int mDayOfMonth;
	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
				case MESSAGE_BACK_TEST_ON:
					Calendar calendar = Calendar.getInstance();

					mYear = calendar.get(Calendar.YEAR);
					mMonthOfYear = calendar.get(Calendar.MONTH);
					mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

					DatePickerDialog datePicker = new DatePickerDialog(SettingBacktestActivity.this, new DatePickerDialog.OnDateSetListener() {

						@Override
						public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
							mYear = year;
							mMonthOfYear = month + 1;
							mDayOfMonth = dayOfMonth;
							String dateTime = mYear + "-" + String.format("%02d", mMonthOfYear) + "-" + String.format("%02d", mDayOfMonth) + " " + DEFAULT_TIME_STRING;
							Preferences.putString(SettingBacktestActivity.this, Settings.KEY_BACKTEST_DATE_TIME, dateTime);

							TimePickerDialog time = new TimePickerDialog(SettingBacktestActivity.this, new TimePickerDialog.OnTimeSetListener() {

								@Override
								public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
									String dateTime = mYear + "-" + String.format("%02d", mMonthOfYear) + "-" + String.format("%02d", mDayOfMonth) + " " + String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + ":00";
									Preferences.putString(SettingBacktestActivity.this, Settings.KEY_BACKTEST_DATE_TIME, dateTime);
									finish();
								}
							}, DEFAULT_HOUR_OF_DAY, DEFAULT_MINUTE, is24HourView);
							time.show();
						}
					}, mYear, mMonthOfYear, mDayOfMonth);
					datePicker.show();
					break;

				case MESSAGE_BACK_TEST_OFF:
					Preferences.putString(SettingBacktestActivity.this, Settings.KEY_BACKTEST_DATE_TIME, "");
					finish();
					break;

				default:
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_backtest);

		int pickDateTime = mIntent.getIntExtra(EXTRA_BACK_TEST, EXTRA_BACK_TEST_OFF);

		if (pickDateTime == EXTRA_BACK_TEST_ON) {
			mHandler.sendEmptyMessage(MESSAGE_BACK_TEST_ON);
		} else {
			mHandler.sendEmptyMessage(MESSAGE_BACK_TEST_OFF);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.service_setting, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(RESULT_OK, mIntent);
			finish();
			return true;

		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}
}
