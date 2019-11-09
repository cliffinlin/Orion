package com.android.orion;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.orion.database.Stock;
import com.android.orion.utility.Utility;

public class StockActivity extends DatabaseActivity implements OnClickListener {

	public static final String ACTION_STOCK_INSERT = "orion.intent.action.ACTION_STOCKINSERT";
	public static final String ACTION_STOCK_EDIT = "orion.intent.action.ACTION_STOCK_EDIT";

	public static final int EXECUTE_STOCK_LOAD = 1;
	public static final int EXECUTE_STOCK_SAVE = 2;

	public static final long RESULT_STOCK_EXIST = -2;

	String mSE = "";

	RadioGroup mRadioGroup;
	EditText mEditTextStockName;
	EditText mEditTextStockCode;
	EditText mEditTextStockDividend;
	Button mButtonOk, mButtonCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock);

		if (mStock == null) {
			mStock = Stock.obtain();
		}

		initView();

		if (ACTION_STOCK_EDIT.equals(mAction)) {
			mStock.setId(mIntent.getLongExtra(Constants.EXTRA_STOCK_ID, 0));
			startLoadTask(EXECUTE_STOCK_LOAD);
		}
	}

	void initView() {
		mRadioGroup = (RadioGroup) findViewById(R.id.radioGroupSE);
		mEditTextStockName = (EditText) findViewById(R.id.edittext_stock_name);
		mEditTextStockCode = (EditText) findViewById(R.id.edittext_stock_code);
		mEditTextStockDividend = (EditText) findViewById(R.id.edittext_stock_dividend);
		mButtonOk = (Button) findViewById(R.id.button_ok);
		mButtonCancel = (Button) findViewById(R.id.button_cancel);

		mRadioGroup.setOnClickListener(this);
		mEditTextStockName.setOnClickListener(this);
		mEditTextStockCode.setOnClickListener(this);
		mEditTextStockDividend.setOnClickListener(this);
		mButtonOk.setOnClickListener(this);
		mButtonCancel.setOnClickListener(this);

		mEditTextStockCode.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				if ((arg0 != null) && (arg0.length() > 0)) {
					if (arg0.charAt(0) == '6') {
						mRadioGroup.check(R.id.radio_sh);
					} else {
						mRadioGroup.check(R.id.radio_sz);
					}
				}
			}
		});

		if (ACTION_STOCK_INSERT.equals(mAction)) {
			setTitle(R.string.stock_insert);
		} else if (ACTION_STOCK_EDIT.equals(mAction)) {
			setTitle(R.string.stock_edit);
		}
	}

	String getSE() {
		String result = "";

		int id = mRadioGroup.getCheckedRadioButtonId();

		if (id == R.id.radio_sh) {
			result = "sh";
		} else {
			result = "sz";
		}

		return result;
	}

	void updateView() {
		mEditTextStockName.setText(mStock.getName());
		mEditTextStockCode.setText(mStock.getCode());
		mEditTextStockDividend.setText(String.valueOf(mStock.getDividend()));
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
			mSE = getSE();
			String name = mEditTextStockName.getText().toString();
			String code = mEditTextStockCode.getText().toString();
			String dividend = mEditTextStockDividend.getText().toString();

			mStock.setClasses(Constants.STOCK_FLAG_CLASS_HSA);

			if (Constants.STOCK_SE_SH.equals(mSE)
					|| Constants.STOCK_SE_SZ.equals(mSE)) {
				mStock.setSE(mSE);
			} else {
				Toast.makeText(mContext, R.string.stock_se_not_found,
						Toast.LENGTH_LONG).show();
			}

			if (!TextUtils.isEmpty(name)) {
				mStock.setName(name);
			}

			if (!TextUtils.isEmpty(code)) {
				mStock.setCode(code);
			} else {
				Toast.makeText(mContext, R.string.stock_code_empty,
						Toast.LENGTH_LONG).show();
			}

			if (!TextUtils.isEmpty(dividend)) {
				mStock.setDividend(Double.valueOf(dividend));
			} else {
				mStock.setDividend(0);
			}

			mStock.setMark(Constants.STOCK_FLAG_MARK_FAVORITE);

			if (ACTION_STOCK_INSERT.equals(mAction)) {
				mStock.setCreated(Utility.getCurrentDateTimeString());
			} else if (ACTION_STOCK_EDIT.equals(mAction)) {
				mStock.setModified(Utility.getCurrentDateTimeString());
			}
			startSaveTask(EXECUTE_STOCK_SAVE);
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
		case EXECUTE_STOCK_LOAD:
			mStockDatabaseManager.getStockById(mStock);
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
		case EXECUTE_STOCK_SAVE:
			if (ACTION_STOCK_INSERT.equals(mAction)) {
				if (!mStockDatabaseManager.isStockExist(mStock)) {
					mStockDatabaseManager.insertStock(mStock);
				} else {
					return RESULT_STOCK_EXIST;
				}
			} else if (ACTION_STOCK_EDIT.equals(mAction)) {
				mStockDatabaseManager.updateStock(mStock,
						mStock.getContentValues());
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

		if (result == RESULT_STOCK_EXIST) {
			Toast.makeText(mContext, R.string.stock_exist, Toast.LENGTH_LONG)
					.show();
		}
	}
}
