package com.android.orion.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.android.orion.R;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.Utility;

import java.util.Calendar;

public class SettingLoopbackActivity extends BaseActivity {

	static final int MESSAGE_BACK_TEST_ON = 1;

	boolean is24HourView = true;

	int mYear;
	int mMonth;
	int mDayOfMonth;
	int mHourOfDay;
	int mMinute;

	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
				case MESSAGE_BACK_TEST_ON:
					Calendar calendar;
					String dateTime = Preferences.getString(mContext, Setting.KEY_LOOPBACK_DATE_TIME, "");
					if (!TextUtils.isEmpty(dateTime)) {
						calendar = Utility.getCalendar(dateTime, Utility.CALENDAR_DATE_TIME_FORMAT);
					} else {
						calendar = Calendar.getInstance();
					}

					mYear = calendar.get(Calendar.YEAR);
					mMonth = calendar.get(Calendar.MONTH);
					mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
					mHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
					mMinute = calendar.get(Calendar.MINUTE);

					DatePickerDialog datePicker = new DatePickerDialog(SettingLoopbackActivity.this, new DatePickerDialog.OnDateSetListener() {

						@Override
						public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
							mYear = year;
							mMonth = month;
							mDayOfMonth = dayOfMonth;

							TimePickerDialog timePicker = new TimePickerDialog(SettingLoopbackActivity.this, new TimePickerDialog.OnTimeSetListener() {

								@Override
								public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
									mHourOfDay = hourOfDay;
									mMinute = minute;
									String dateTime = mYear + "-" + String.format("%02d", mMonth + 1) + "-" + String.format("%02d", mDayOfMonth) + " " + String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + ":00";
									Preferences.putString(SettingLoopbackActivity.this, Setting.KEY_LOOPBACK_DATE_TIME, dateTime);
									setResult(RESULT_OK, mIntent);
									finish();
								}
							}, mHourOfDay, mMinute, is24HourView);
							timePicker.show();
						}
					}, mYear, mMonth, mDayOfMonth);
					datePicker.show();
					break;

				default:
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_setting_loopback);

		mHandler.sendEmptyMessage(MESSAGE_BACK_TEST_ON);
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
