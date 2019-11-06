package com.android.orion;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.orion.database.StockFilter;
import com.android.orion.utility.Utility;

public class StockFilterActivity extends DatabaseActivity implements
		OnClickListener {

	public static final int EXECUTE_STOCK_FILTER_LOAD = 0;
	public static final int EXECUTE_STOCK_FILTER_SAVE = 1;

	EditText mEditTextPE;
	EditText mEditTextPB;
	EditText mEditTextDividend;
	EditText mEditTextYield;
	EditText mEditTextDelta;
	Button mButtonOk, mButtonCancel;

	StockFilter mStockFilter = StockFilter.obtain();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_filter);

		initView();

		startLoadTask(EXECUTE_STOCK_FILTER_LOAD);
	}

	void initView() {
		mEditTextPE = (EditText) findViewById(R.id.edittext_pe);
		mEditTextPB = (EditText) findViewById(R.id.edittext_pb);
		mEditTextDividend = (EditText) findViewById(R.id.edittext_dividend);
		mEditTextYield = (EditText) findViewById(R.id.edittext_yield);
		mEditTextDelta = (EditText) findViewById(R.id.edittext_delta);

		mButtonOk = (Button) findViewById(R.id.button_ok);
		mButtonCancel = (Button) findViewById(R.id.button_cancel);

		mEditTextPE.setOnClickListener(this);
		mEditTextPB.setOnClickListener(this);
		mEditTextDividend.setOnClickListener(this);
		mEditTextYield.setOnClickListener(this);
		mEditTextDelta.setOnClickListener(this);

		mButtonOk.setOnClickListener(this);
		mButtonCancel.setOnClickListener(this);
	}

	void updateView() {
		mEditTextPE.setText(mStockFilter.getPE());
		mEditTextPB.setText(mStockFilter.getPB());
		mEditTextDividend.setText(mStockFilter.getDividend());
		mEditTextYield.setText(mStockFilter.getYield());
		mEditTextDelta.setText(mStockFilter.getDelta());
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
		case R.id.button_ok:
			String pe = mEditTextPE.getText().toString();
			String pb = mEditTextPB.getText().toString();
			String dividend = mEditTextDividend.getText().toString();
			String yield = mEditTextYield.getText().toString();
			String delta = mEditTextDelta.getText().toString();

			mStockFilter.setPE(pe);
			mStockFilter.setPB(pb);
			mStockFilter.setDividend(dividend);
			mStockFilter.setYield(yield);
			mStockFilter.setDelta(delta);

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
			mStockDatabaseManager.getStockFilter(mStockFilter);
			break;

		default:
			break;
		}

		return RESULT_SUCCESS;
	}

	@Override
	void onPostExecuteLoad(Long result) {
		super.onPostExecuteLoad(result);
		updateView();
	}

	@Override
	Long doInBackgroundSave(Object... params) {
		super.doInBackgroundSave(params);
		int execute = (Integer) params[0];

		switch (execute) {
		case EXECUTE_STOCK_FILTER_SAVE:
			if (!mStockDatabaseManager.isStockFilterExist(mStockFilter)) {
				mStockFilter.setCreated(Utility.getCurrentDateTimeString());
				mStockDatabaseManager.insertStockFilter(mStockFilter);
			} else {
				mStockFilter.setModified(Utility.getCurrentDateTimeString());
				mStockDatabaseManager.updateStockFilter(mStockFilter,
						mStockFilter.getContentValues());
			}
			break;

		default:
			break;
		}

		return RESULT_SUCCESS;
	}

	@Override
	void onPostExecuteSave(Long result) {
		super.onPostExecuteSave(result);

		Toast.makeText(mContext, R.string.stock_exist, Toast.LENGTH_LONG)
				.show();
	}
}
