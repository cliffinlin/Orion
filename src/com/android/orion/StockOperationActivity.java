package com.android.orion;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.orion.utility.Utility;

public class StockOperationActivity extends DatabaseActivity implements
		OnClickListener, OnItemSelectedListener {

	public static final String ACTION_DEAL_INSERT = "orion.intent.action.ACTION_DEAL_INSERT";
	public static final String ACTION_DEAL_EDIT = "orion.intent.action.ACTION_DEAL_EDIT";

	public static final String EXTRA_DEAL_ID = "deal_id";

	static final int MESSAGE_LOAD_STOCK = 0;
	static final int MESSAGE_LOAD_OVERLAP = 1;
	static final int MESSAGE_SAVE = 2;

	static final int REQUEST_CODE_STOCK_ID = 0;

	EditText mEditTextStockName, mEditTextStockCode, mEditTextStockQuota;
	EditText mEditTextStockOverlap, mEditTextDealVolume;

	Spinner mSpinnerStockOperation;

	Button mButtonOk, mButtonCancel;

	String[] mStockOperationList = null;

	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MESSAGE_LOAD_STOCK:
				mStockDatabaseManager.getStock(mStock);
				mStockData.setStockId(mStock.getId());
				updateView();
				break;

			case MESSAGE_LOAD_OVERLAP:
				mStockData.setOverlap(0);
				mStockData.setOverlapLow(0);
				mStockData.setOverlapHigh(0);
				mStockDatabaseManager.getStockData(mStockData);
				mStock.setOverlap(mStockData.getOverlap());
				mStock.setOverlapLow(mStockData.getOverlapLow());
				mStock.setOverlapHigh(mStockData.getOverlapHigh());
				mStock.setDealVolume(Utility.getDealVolumeMin(
						mStock.getPrice(), mStock.getOverlapLow(),
						mStock.getOverlapHigh(), mStock.getQuota()));
				updateView();
				break;

			case MESSAGE_SAVE:
				mStockDatabaseManager.updateStock(mStock,
						mStock.getContentValues());
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_operation);

		initView();

		if (mBundle != null) {
			mStock.setSE(mBundle.getString(Constants.EXTRA_STOCK_SE));
			mStock.setCode(mBundle.getString(Constants.EXTRA_STOCK_CODE));
			mHandler.sendEmptyMessage(MESSAGE_LOAD_STOCK);
		}
	}

	void initView() {
		mStockOperationList = getResources().getStringArray(
				R.array.stock_operation);

		mEditTextStockName = (EditText) findViewById(R.id.edittext_stock_name);
		mEditTextStockCode = (EditText) findViewById(R.id.edittext_stock_code);
		mEditTextStockQuota = (EditText) findViewById(R.id.edittext_stock_quota);
		mSpinnerStockOperation = (Spinner) findViewById(R.id.spinner_stock_operation);
		mEditTextStockOverlap = (EditText) findViewById(R.id.edittext_stock_overlap);
		mEditTextDealVolume = (EditText) findViewById(R.id.edittext_deal_volume);
		mButtonOk = (Button) findViewById(R.id.button_ok);
		mButtonCancel = (Button) findViewById(R.id.button_cancel);

		mEditTextStockName.setOnClickListener(this);
		mEditTextStockCode.setOnClickListener(this);
		mEditTextStockQuota.setOnClickListener(this);
		mSpinnerStockOperation.setOnItemSelectedListener(this);
		mEditTextStockOverlap.setOnClickListener(this);
		mEditTextDealVolume.setOnClickListener(this);
		mButtonOk.setOnClickListener(this);
		mButtonCancel.setOnClickListener(this);

		mEditTextStockName.setInputType(InputType.TYPE_NULL);
		mEditTextStockName.setFocusable(false);
		mEditTextStockCode.setInputType(InputType.TYPE_NULL);
		mEditTextStockCode.setFocusable(false);
		mEditTextStockOverlap.setInputType(InputType.TYPE_NULL);
		mEditTextStockOverlap.setFocusable(false);

		mEditTextStockQuota.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mStock.setQuota(Long.valueOf(s.toString()));
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
	}

	void updateView() {
		mEditTextStockName.setText(mStock.getName());
		mEditTextStockCode.setText(mStock.getCode());
		mEditTextStockQuota.setText(String.valueOf(mStock.getQuota()));
		mSpinnerStockOperation.setSelection(stockOperationToPosition());
		mEditTextStockOverlap.setText(String.valueOf(mStock.getOverlap()));
		mEditTextDealVolume.setText(String.valueOf(mStock.getDealVolume()));
	}

	int stockOperationToPosition() {
		int result = 0;
		String operation = mStock.getOperation();

		if (TextUtils.isEmpty(operation)) {
			return result;
		}

		if (mStockOperationList != null) {
			for (int i = 0; i < mStockOperationList.length; i++) {
				if (operation.equals(mStockOperationList[i])) {
					result = i;
					break;
				}
			}
		}

		return result;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		mStock.setOperation(mStockOperationList[mSpinnerStockOperation
				.getSelectedItemPosition()]);
		mStockData.setPeriod(mStock.getOperation());
		mHandler.sendEmptyMessage(MESSAGE_LOAD_OVERLAP);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
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
		case R.id.edittext_stock_name:
		case R.id.edittext_stock_code:
			break;
		case R.id.button_ok:
			String quotaString = "";
			String operationString = "";
			String overlapString = "";
			String volumeString = "";

			quotaString = mEditTextStockQuota.getText().toString();
			if (!TextUtils.isEmpty(quotaString)) {
				mStock.setQuota(Long.valueOf(quotaString));
			} else {
				mStock.setQuota(0);
			}

			operationString = mStockOperationList[mSpinnerStockOperation
					.getSelectedItemPosition()];
			if (!TextUtils.isEmpty(operationString)) {
				mStock.setOperation(operationString);
			} else {
				mStock.setOperation("");
			}

			overlapString = mEditTextStockOverlap.getText().toString();
			if (!TextUtils.isEmpty(overlapString)) {
				mStock.setOverlap(Double.valueOf(overlapString));
				mStock.setOverlapHigh(mStockData.getOverlapHigh());
				mStock.setOverlapLow(mStockData.getOverlapLow());
			} else {
				mStock.setOverlap(0);
			}

			volumeString = mEditTextDealVolume.getText().toString();
			if (!TextUtils.isEmpty(volumeString)) {
				mStock.setDealVolume(Long.valueOf(volumeString));
			} else {
				mStock.setDealVolume(0);
			}
			mHandler.sendEmptyMessage(MESSAGE_SAVE);
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
