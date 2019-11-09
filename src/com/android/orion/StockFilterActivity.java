package com.android.orion;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.android.orion.database.Setting;

public class StockFilterActivity extends DatabaseActivity implements
		OnClickListener {

	public static final int EXECUTE_STOCK_FILTER_LOAD = 0;
	public static final int EXECUTE_STOCK_FILTER_SAVE = 1;

	CheckBox mCheckBox;

	EditText mEditTextPE;
	EditText mEditTextPB;
	EditText mEditTextDividend;
	EditText mEditTextYield;
	EditText mEditTextDelta;
	Button mButtonOk, mButtonCancel;

	boolean mChecked = false;

	String mPE = "";
	String mPB = "";
	String mDividend = "";
	String mYield = "";
	String mDelta = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_filter);

		initView();

		startLoadTask(EXECUTE_STOCK_FILTER_LOAD);
	}

	void initView() {
		mCheckBox = (CheckBox) findViewById(R.id.checkbox);

		mEditTextPE = (EditText) findViewById(R.id.edittext_pe);
		mEditTextPB = (EditText) findViewById(R.id.edittext_pb);
		mEditTextDividend = (EditText) findViewById(R.id.edittext_dividend);
		mEditTextYield = (EditText) findViewById(R.id.edittext_yield);
		mEditTextDelta = (EditText) findViewById(R.id.edittext_delta);

		mButtonOk = (Button) findViewById(R.id.button_ok);
		mButtonCancel = (Button) findViewById(R.id.button_cancel);

		mCheckBox.setOnClickListener(this);

		mEditTextPE.setOnClickListener(this);
		mEditTextPB.setOnClickListener(this);
		mEditTextDividend.setOnClickListener(this);
		mEditTextYield.setOnClickListener(this);
		mEditTextDelta.setOnClickListener(this);

		mButtonOk.setOnClickListener(this);
		mButtonCancel.setOnClickListener(this);
	}

	void updateEditText() {
		mEditTextPE.setText(mPE);
		mEditTextPB.setText(mPB);
		mEditTextDividend.setText(mDividend);
		mEditTextYield.setText(mYield);
		mEditTextDelta.setText(mDelta);

		mEditTextPE.setEnabled(mChecked);
		mEditTextPB.setEnabled(mChecked);
		mEditTextDividend.setEnabled(mChecked);
		mEditTextYield.setEnabled(mChecked);
		mEditTextDelta.setEnabled(mChecked);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_deal_edit, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();

		switch (id) {
		case R.id.checkbox:
			mChecked = mCheckBox.isChecked();

			updateEditText();
			break;

		case R.id.button_ok:
			mChecked = mCheckBox.isChecked();

			mPE = mEditTextPE.getText().toString();
			mPB = mEditTextPB.getText().toString();
			mDividend = mEditTextDividend.getText().toString();
			mYield = mEditTextYield.getText().toString();
			mDelta = mEditTextDelta.getText().toString();

			startSaveTask(EXECUTE_STOCK_FILTER_SAVE);
			setResult(RESULT_OK, getIntent());
			finish();
			break;

		case R.id.button_cancel:
			finish();
			break;

		default:
			break;
		}
	}

	@Override
	Long doInBackgroundLoad(Object... params) {
		super.doInBackgroundLoad(params);
		int execute = (Integer) params[0];

		switch (execute) {
		case EXECUTE_STOCK_FILTER_LOAD:
			mChecked = mStockDatabaseManager.getSettingBoolean(
					Setting.KEY_STOCK_FILTER, false);

			mPE = mStockDatabaseManager
					.getSettingString(Setting.KEY_STOCK_FILTER_PE);
			mPB = mStockDatabaseManager
					.getSettingString(Setting.KEY_STOCK_FILTER_PB);
			mDividend = mStockDatabaseManager
					.getSettingString(Setting.KEY_STOCK_FILTER_DIVIDEND);
			mYield = mStockDatabaseManager
					.getSettingString(Setting.KEY_STOCK_FILTER_YIELD);
			mDelta = mStockDatabaseManager
					.getSettingString(Setting.KEY_STOCK_FILTER_DELTA);
			break;

		default:
			break;
		}

		return RESULT_SUCCESS;
	}

	@Override
	void onPostExecuteLoad(Long result) {
		super.onPostExecuteLoad(result);
		mCheckBox.setChecked(mChecked);
		updateEditText();
	}

	@Override
	Long doInBackgroundSave(Object... params) {
		super.doInBackgroundSave(params);
		int execute = (Integer) params[0];

		switch (execute) {
		case EXECUTE_STOCK_FILTER_SAVE:
			mStockDatabaseManager.saveSetting(Setting.KEY_STOCK_FILTER,
					mChecked);

			mStockDatabaseManager.saveSetting(Setting.KEY_STOCK_FILTER_PE, mPE);
			mStockDatabaseManager.saveSetting(Setting.KEY_STOCK_FILTER_PB, mPB);
			mStockDatabaseManager.saveSetting(
					Setting.KEY_STOCK_FILTER_DIVIDEND, mDividend);
			mStockDatabaseManager.saveSetting(Setting.KEY_STOCK_FILTER_YIELD,
					mYield);
			mStockDatabaseManager.saveSetting(Setting.KEY_STOCK_FILTER_DELTA,
					mDelta);
			break;

		default:
			break;
		}

		return RESULT_SUCCESS;
	}

	@Override
	void onPostExecuteSave(Long result) {
		super.onPostExecuteSave(result);

		// Toast.makeText(mContext, R.string.stock_exist, Toast.LENGTH_LONG)
		// .show();
	}
}
