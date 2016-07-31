package com.android.orion;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TimePicker;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.StockData;
import com.android.orion.utility.Utility;

public class StockSimulationActivity extends OrionBaseActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	static final int LOADER_ID_STOCK_DATA_LIST = 0;

	String mPeriod = "";
	ArrayList<StockData> mStockDataArrayList = new ArrayList<StockData>();

	Switch mSwitch = null;
	EditText mFromDate = null;
	EditText mFromTime = null;
	EditText mToDate = null;
	EditText mToTime = null;
	EditText mInterval = null;

	StockSimulationAlarmManager mStockSimulationAlarmManager = null;

	public StockSimulationActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_simulation);

		if (mSwitch == null) {
			mSwitch = (Switch) findViewById(R.id.switchSimulation);
		}

		if (mFromDate == null) {
			mFromDate = (EditText) findViewById(R.id.editTextFromDate);
		}

		if (mFromTime == null) {
			mFromTime = (EditText) findViewById(R.id.editTextFromTime);
		}

		if (mToDate == null) {
			mToDate = (EditText) findViewById(R.id.editTextToDate);
		}

		if (mToTime == null) {
			mToTime = (EditText) findViewById(R.id.editTextToTime);
		}

		if (mInterval == null) {
			mInterval = (EditText) findViewById(R.id.editTextInterval);
		}

		if (mStockSimulationAlarmManager == null) {
			mStockSimulationAlarmManager = StockSimulationAlarmManager
					.getInstance(this);
		}

		mStock.setId(getIntent().getLongExtra(Constants.EXTRA_KEY_STOCK_ID, 0));
		mStock.setSE(getIntent().getStringExtra(Constants.EXTRA_KEY_STOCK_SE));
		mStock.setCode(getIntent().getStringExtra(
				Constants.EXTRA_KEY_STOCK_CODE));

		initLoader();

		mSwitch.setChecked(Utility.getSettingBoolean(this,
				Constants.SETTING_KEY_SIMULATION));

		mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton compoundButton,
					boolean isChecked) {
				Context context = StockSimulationActivity.this;
				Utility.setSettingBoolean(StockSimulationActivity.this,
						Constants.SETTING_KEY_SIMULATION, isChecked);

				if (isChecked) {
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_STOCK_ID,
							String.valueOf(mStock.getId()));
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_STOCK_SE,
							mStock.getSE());
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_STOCK_CODE,
							mStock.getCode());
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_PERIOD, mPeriod);
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_FROM_DATE,
							mFromDate.getText().toString());
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_FROM_TIME,
							mFromTime.getText().toString());
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_DATE, mFromDate
									.getText().toString());
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_TIME, mFromTime
									.getText().toString());
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_TO_DATE, mToDate
									.getText().toString());
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_TO_TIME, mToTime
									.getText().toString());
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_INTERVAL,
							mInterval.getText().toString());
					mStockSimulationAlarmManager.startAlarm();
				} else {
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_STOCK_ID, "");
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_STOCK_SE, "");
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_STOCK_CODE, "");
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_PERIOD, "");
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_FROM_DATE, "");
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_FROM_TIME, "");
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_DATE, "");
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_TIME, "");
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_TO_DATE, "");
					Utility.setSettingString(context,
							Constants.SETTING_KEY_SIMULATION_TO_TIME, "");
					mStockSimulationAlarmManager.stopAlarm();
				}
			}
		});

		mFromDate.setEnabled(false);
		mFromDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// DialogFragment datePickerFragment = new DatePickerFragment();
				// ((DatePickerFragment) datePickerFragment)
				// .setEditText(mFromDate);
				// ((DatePickerFragment) datePickerFragment)
				// .setKey(Constants.SETTING_KEY_SIMULATION_FROM_DATE);
				// datePickerFragment.show(getFragmentManager(), "datePicker");
			}
		});

		mFromTime.setEnabled(false);
		mFromTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// DialogFragment timePickerFragment = new TimePickerFragment();
				// ((TimePickerFragment) timePickerFragment)
				// .setEditText(mFromTime);
				// ((TimePickerFragment) timePickerFragment)
				// .setKey(Constants.SETTING_KEY_SIMULATION_FROM_TIME);
				// timePickerFragment.show(getFragmentManager(), "timePicker");
			}
		});

		mToDate.setEnabled(false);
		mToDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// DialogFragment datePickerFragment = new DatePickerFragment();
				// ((DatePickerFragment)
				// datePickerFragment).setEditText(mToDate);
				// ((DatePickerFragment) datePickerFragment)
				// .setKey(Constants.SETTING_KEY_SIMULATION_TO_DATE);
				// datePickerFragment.show(getFragmentManager(), "datePicker");
			}
		});

		mToTime.setEnabled(false);
		mToTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// DialogFragment timePickerFragment = new TimePickerFragment();
				// ((TimePickerFragment)
				// timePickerFragment).setEditText(mToTime);
				// ((TimePickerFragment) timePickerFragment)
				// .setKey(Constants.SETTING_KEY_SIMULATION_TO_TIME);
				// timePickerFragment.show(getFragmentManager(), "timePicker");
			}
		});

		String intervarString = Utility.getSettingString(
				StockSimulationActivity.this,
				Constants.SETTING_KEY_SIMULATION_INTERVAL);
		if (TextUtils.isEmpty(intervarString)) {
			intervarString = String
					.valueOf(Constants.STOCK_SIMULATION_ALARM_INTERVAL_DEFAULT);
		}
		mInterval.setText(intervarString);

		mInterval.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				Utility.setSettingString(StockSimulationActivity.this,
						Constants.SETTING_KEY_SIMULATION_INTERVAL, s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.simulation_setting, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	static String to2DigitalString(int value) {
		String result = "";

		if (value < 10) {
			result = "0" + String.valueOf(value);
		} else {
			result = String.valueOf(value);
		}

		return result;
	}

	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		EditText mEditText = null;
		String mKey = "";

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void setEditText(EditText editText) {
			mEditText = editText;
		}

		public void setKey(String key) {
			mKey = key;
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			String result = to2DigitalString(year) + "_"
					+ to2DigitalString(month + 1) + "_" + to2DigitalString(day);
			Utility.setSettingString(getActivity(), mKey, result);
			if (mEditText != null) {
				mEditText.setText(result);
			}
		}
	}

	public static class TimePickerFragment extends DialogFragment implements
			TimePickerDialog.OnTimeSetListener {

		EditText mEditText = null;
		String mKey = "";

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);

			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute, true);
		}

		public void setEditText(EditText editText) {
			mEditText = editText;
		}

		public void setKey(String key) {
			mKey = key;
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			String result = to2DigitalString(hourOfDay) + "_"
					+ to2DigitalString(minute);
			Utility.setSettingString(getActivity(), mKey, result);
			if (mEditText != null) {
				mEditText.setText(result);
			}
		}
	}

	void initLoader() {
		for (String period : Constants.PERIODS) {
			if (Utility.getSettingBoolean(this, period)) {
				mPeriod = period;
				mLoaderManager
						.initLoader(LOADER_ID_STOCK_DATA_LIST, null, this);
				break;
			}
		}
	}

	CursorLoader getStockDataCursorLoader(String period) {
		String selection = "";
		String sortOrder = "";
		CursorLoader loader = null;

		selection = mStockDatabaseManager.getStockDataSelection(mStock.getId(),
				period, Constants.STOCK_DATA_FLAG_NONE);
		sortOrder = mStockDatabaseManager.getStockDataOrder();

		loader = new CursorLoader(this, DatabaseContract.StockData.CONTENT_URI,
				DatabaseContract.StockData.PROJECTION_ALL, selection, null,
				sortOrder);

		return loader;
	}

	public void swapStockDataCursor(Cursor cursor) {
		mFromDate.setText("");
		mFromTime.setText("");
		mToDate.setText("");
		mToTime.setText("");

		mStockDataArrayList.clear();

		try {
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockData stockData = StockData.obtain();
					stockData.set(cursor);
					mStockDataArrayList.add(stockData);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				if (!cursor.isClosed()) {
					cursor.close();
				}
			}
		}

		if (mStockDataArrayList.size() > 0) {
			StockData stockData = mStockDataArrayList.get(0);
			mFromDate.setText(stockData.getDate());
			mFromTime.setText(stockData.getTime());

			stockData = mStockDataArrayList.get(mStockDataArrayList.size() - 1);
			mToDate.setText(stockData.getDate());
			mToTime.setText(stockData.getTime());
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader loader = null;

		loader = getStockDataCursorLoader(mPeriod);

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader == null) {
			return;
		}

		swapStockDataCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader == null) {
			return;
		}

		swapStockDataCursor(null);
	}
}
