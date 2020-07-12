package com.android.orion;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class StockFilterActivity extends DatabaseActivity implements
		OnClickListener {

	public static final int EXECUTE_STOCK_FILTER_LOAD = 0;
	public static final int EXECUTE_STOCK_FILTER_SAVE = 1;

	CheckBox mCheckBox;

	EditText mEditTextHold;
	EditText mEditTextRoi;
	EditText mEditTextRate;
	EditText mEditTextRoe;
	EditText mEditTextPE;
	EditText mEditTextPB;
	EditText mEditTextDividend;
	EditText mEditTextYield;
	EditText mEditTextDelta;

	Button mButtonOk;
	Button mButtonCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_filter);

		mStockFilter.setDefaultEnable(true);
		mStockFilter.read();

		initView();
	}

	void initView() {
		mCheckBox = (CheckBox) findViewById(R.id.checkbox);

		mEditTextHold = (EditText) findViewById(R.id.edittext_hold);
		mEditTextRoi = (EditText) findViewById(R.id.edittext_roi);
		mEditTextRate = (EditText) findViewById(R.id.edittext_rate);
		mEditTextRoe = (EditText) findViewById(R.id.edittext_roe);
		mEditTextPE = (EditText) findViewById(R.id.edittext_pe);
		mEditTextPB = (EditText) findViewById(R.id.edittext_pb);
		mEditTextDividend = (EditText) findViewById(R.id.edittext_dividend);
		mEditTextYield = (EditText) findViewById(R.id.edittext_yield);
		mEditTextDelta = (EditText) findViewById(R.id.edittext_delta);

		mButtonOk = (Button) findViewById(R.id.button_ok);
		mButtonCancel = (Button) findViewById(R.id.button_cancel);

		mCheckBox.setOnClickListener(this);

		mEditTextHold.setOnClickListener(this);
		mEditTextRoi.setOnClickListener(this);
		mEditTextRate.setOnClickListener(this);
		mEditTextRoe.setOnClickListener(this);
		mEditTextPE.setOnClickListener(this);
		mEditTextPB.setOnClickListener(this);
		mEditTextDividend.setOnClickListener(this);
		mEditTextYield.setOnClickListener(this);
		mEditTextDelta.setOnClickListener(this);

		mButtonOk.setOnClickListener(this);
		mButtonCancel.setOnClickListener(this);

		mCheckBox.setChecked(mStockFilter.getEnable());
		updateEditText();
	}

	void updateEditText() {
		mEditTextHold.setText(mStockFilter.getHold());
		mEditTextRoi.setText(mStockFilter.getRoi());
		mEditTextRate.setText(mStockFilter.getRate());
		mEditTextRoe.setText(mStockFilter.getRoe());
		mEditTextPE.setText(mStockFilter.getPE());
		mEditTextPB.setText(mStockFilter.getPB());
		mEditTextDividend.setText(mStockFilter.getDividend());
		mEditTextYield.setText(mStockFilter.getYield());
		mEditTextDelta.setText(mStockFilter.getDelta());

		mEditTextHold.setEnabled(mStockFilter.getEnable());
		mEditTextRoi.setEnabled(mStockFilter.getEnable());
		mEditTextRate.setEnabled(mStockFilter.getEnable());
		mEditTextRoe.setEnabled(mStockFilter.getEnable());
		mEditTextPE.setEnabled(mStockFilter.getEnable());
		mEditTextPB.setEnabled(mStockFilter.getEnable());
		mEditTextDividend.setEnabled(mStockFilter.getEnable());
		mEditTextYield.setEnabled(mStockFilter.getEnable());
		mEditTextDelta.setEnabled(mStockFilter.getEnable());
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
			mStockFilter.setEnable(mCheckBox.isChecked());
			updateEditText();
			break;

		case R.id.button_ok:
			mStockFilter.setEnable(mCheckBox.isChecked());

			mStockFilter.setHold(mEditTextHold.getText().toString());
			mStockFilter.setRoi(mEditTextRoi.getText().toString());
			mStockFilter.setRate(mEditTextRate.getText().toString());
			mStockFilter.setRoe(mEditTextRoe.getText().toString());
			mStockFilter.setPE(mEditTextPE.getText().toString());
			mStockFilter.setPB(mEditTextPB.getText().toString());
			mStockFilter.setDividend(mEditTextDividend.getText().toString());
			mStockFilter.setYield(mEditTextYield.getText().toString());
			mStockFilter.setDelta(mEditTextDelta.getText().toString());

			mStockFilter.write();

			Bundle bundle = new Bundle();
			mStockFilter.put(bundle);
			mIntent.putExtras(bundle);
			setResult(RESULT_OK, mIntent);
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
			mStockFilter.read();
			break;

		default:
			break;
		}

		return RESULT_SUCCESS;
	}

	@Override
	void onPostExecuteLoad(Long result) {
		super.onPostExecuteLoad(result);
		mCheckBox.setChecked(mStockFilter.getEnable());
		updateEditText();
	}

	@Override
	Long doInBackgroundSave(Object... params) {
		super.doInBackgroundSave(params);
		int execute = (Integer) params[0];

		switch (execute) {
		case EXECUTE_STOCK_FILTER_SAVE:
			mStockFilter.write();
			break;

		default:
			break;
		}

		return RESULT_SUCCESS;
	}

	@Override
	void onPostExecuteSave(Long result) {
		super.onPostExecuteSave(result);

		Toast.makeText(mContext, R.string.saved, Toast.LENGTH_LONG).show();
	}
}
