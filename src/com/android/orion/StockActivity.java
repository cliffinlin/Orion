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

	RadioGroup mRadioGroupClass;
	RadioGroup mRadioGroupSE;
	EditText mEditTextStockName;
	EditText mEditTextStockCode;
	EditText mEditTextStockValuation;
	Button mButtonOk, mButtonCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock);

		if (mStock == null) {
			mStock = new Stock();
		}

		initView();

		if (ACTION_STOCK_EDIT.equals(mAction)) {
			mRadioGroupClass.setEnabled(false);
			mRadioGroupSE.setEnabled(false);
			mEditTextStockCode.setEnabled(false);

			mStock.setId(mIntent.getLongExtra(Constants.EXTRA_STOCK_ID, 0));
			mStockDatabaseManager.getStockById(mStock);
			updateView();
		}
	}

	void initView() {
		mRadioGroupClass = (RadioGroup) findViewById(R.id.radioGroupClass);
		mRadioGroupSE = (RadioGroup) findViewById(R.id.radioGroupSE);
		mEditTextStockName = (EditText) findViewById(R.id.edittext_stock_name);
		mEditTextStockCode = (EditText) findViewById(R.id.edittext_stock_code);
		mEditTextStockValuation = (EditText) findViewById(R.id.edittext_stock_valuation);
		mButtonOk = (Button) findViewById(R.id.button_ok);
		mButtonCancel = (Button) findViewById(R.id.button_cancel);

		mRadioGroupClass.setOnClickListener(this);
		mRadioGroupSE.setOnClickListener(this);
		mEditTextStockName.setOnClickListener(this);
		mEditTextStockCode.setOnClickListener(this);
		mEditTextStockValuation.setOnClickListener(this);
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
						mRadioGroupClass.check(R.id.radio_class_hsa);
						mRadioGroupSE.check(R.id.radio_se_sh);
					} else if (arg0.charAt(0) == '0' || arg0.charAt(0) == '3') {
						mRadioGroupClass.check(R.id.radio_class_hsa);
						mRadioGroupSE.check(R.id.radio_se_sz);
					} else if (arg0.charAt(0) == '8' || arg0.charAt(0) == '9') {
						mRadioGroupSE.check(R.id.radio_se_sh);
						mRadioGroupClass.check(R.id.radio_class_index);
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

	void updateView() {
		mEditTextStockName.setText(mStock.getName());
		mEditTextStockCode.setText(mStock.getCode());
		mEditTextStockValuation.setText(String.valueOf(mStock.getValuation()));
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
		int viewId = view.getId();

		switch (viewId) {
		case R.id.button_ok:
			int id = 0;

			id = mRadioGroupSE.getCheckedRadioButtonId();
			if (id == R.id.radio_class_hsa) {
				mStock.setClasses(Constants.STOCK_FLAG_CLASS_HSA);
			} else if (id == R.id.radio_se_sz) {
				mStock.setClasses(Constants.STOCK_FLAG_CLASS_INDEXES);
			}

			id = mRadioGroupSE.getCheckedRadioButtonId();
			if (id == R.id.radio_se_sh) {
				mStock.setSE(Constants.STOCK_SE_SH);
			} else if (id == R.id.radio_se_sz) {
				mStock.setSE(Constants.STOCK_SE_SZ);
			}

			String name = mEditTextStockName.getText().toString();
			String code = mEditTextStockCode.getText().toString();
			String valuation = mEditTextStockValuation.getText().toString();

			if (!TextUtils.isEmpty(name)) {
				mStock.setName(name);
			}

			if (!TextUtils.isEmpty(code)) {
				mStock.setCode(code);
			} else {
				Toast.makeText(mContext, R.string.stock_code_empty,
						Toast.LENGTH_LONG).show();
			}

			if (!TextUtils.isEmpty(valuation)) {
				mStock.setValuation(Double.valueOf(valuation));
			} else {
				mStock.setValuation(0);
			}

			mStock.setMark(Constants.STOCK_FLAG_MARK_FAVORITE);

			if (ACTION_STOCK_INSERT.equals(mAction)) {
				if (!mStockDatabaseManager.isStockExist(mStock)) {
					mStock.setCreated(Utility.getCurrentDateTimeString());
					mStockDatabaseManager.insertStock(mStock);
				} else {
					Toast.makeText(mContext, R.string.stock_exist,
							Toast.LENGTH_LONG).show();
				}
			} else if (ACTION_STOCK_EDIT.equals(mAction)) {
				mStock.setModified(Utility.getCurrentDateTimeString());
				mStockDatabaseManager.updateStock(mStock,
						mStock.getContentValues());
			}

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
}
