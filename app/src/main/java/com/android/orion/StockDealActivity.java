package com.android.orion;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.android.orion.utility.Utility;

public class StockDealActivity extends DatabaseActivity implements
		OnClickListener {

	public static final String ACTION_DEAL_INSERT = "orion.intent.action.ACTION_DEAL_INSERT";
	public static final String ACTION_DEAL_EDIT = "orion.intent.action.ACTION_DEAL_EDIT";

	public static final String EXTRA_DEAL_ID = "deal_id";

	static final int MESSAGE_LOAD_DEAL = 0;
	static final int MESSAGE_SAVE_DEAL = 1;
	static final int MESSAGE_LOAD_STOCK_BY_ID = 2;
	static final int MESSAGE_LOAD_STOCK_BY_SE_CODE = 3;

	static final int REQUEST_CODE_STOCK_ID = 0;

	EditText mEditTextStockName;
	EditText mEditTextStockCode;
	EditText mEditTextDealProfit;
	EditText mEditTextDealPrice;
	EditText mEditTextDealVolume;

	Button mButtonAdd;
	Button mButtonSubtract;
	Button mButtonOk;
	Button mButtonCancel;

	ArrayAdapter<String> mArrayAdapter;
	List<String> mListStockAction;
	Spinner mSpinnerStockAcion;

	StockDeal mDeal = null;

	int mOrder = 0;

	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MESSAGE_LOAD_DEAL:
				mStockDatabaseManager.getStockDealById(mDeal);
				mStock.setSE(mDeal.getSE());
				mStock.setCode(mDeal.getCode());
				mStockDatabaseManager.getStock(mStock);
				updateView();
				break;

			case MESSAGE_SAVE_DEAL:
				if (ACTION_DEAL_INSERT.equals(mAction)) {
					mDeal.setCreated(Utility.getCurrentDateTimeString());
					mStockDatabaseManager.insertStockDeal(mDeal);
				} else if (ACTION_DEAL_EDIT.equals(mAction)) {
					mDeal.setModified(Utility.getCurrentDateTimeString());
					mStockDatabaseManager.updateStockDealByID(mDeal);
				}

				mStockDatabaseManager.updateStockDeal(mStock);
				mStockDatabaseManager.updateStock(mStock,
						mStock.getContentValues());
				break;

			case MESSAGE_LOAD_STOCK_BY_ID:
				mStockDatabaseManager.getStockById(mStock);
				mDeal.setSE(mStock.getSE());
				mDeal.setCode(mStock.getCode());
				mDeal.setName(mStock.getName());
				mDeal.setPrice(mStock.getPrice());
				mDeal.setDeal(mStock.getPrice());
				updateView();
				break;

			case MESSAGE_LOAD_STOCK_BY_SE_CODE:
				mStockDatabaseManager.getStock(mStock);
				mDeal.setSE(mStock.getSE());
				mDeal.setCode(mStock.getCode());
				mDeal.setName(mStock.getName());
				mDeal.setPrice(mStock.getPrice());
				mDeal.setDeal(mStock.getPrice());
				updateView();
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_deal);

		if (mDeal == null) {
			mDeal = new StockDeal();
		}

		if (mStock == null) {
			mStock = new Stock();
		}

		initView();

		if (ACTION_DEAL_INSERT.equals(mAction)) {
			if (mBundle != null) {
				mStock.setSE(mBundle.getString(Constants.EXTRA_STOCK_SE));
				mStock.setCode(mBundle.getString(Constants.EXTRA_STOCK_CODE));
				mHandler.sendEmptyMessage(MESSAGE_LOAD_STOCK_BY_SE_CODE);
			}
		} else if (ACTION_DEAL_EDIT.equals(mAction)) {
			mDeal.setId(mIntent.getLongExtra(EXTRA_DEAL_ID, 0));
			mHandler.sendEmptyMessage(MESSAGE_LOAD_DEAL);
		}
	}

	void initView() {
		mEditTextStockName = (EditText) findViewById(R.id.edittext_stock_name);
		mEditTextStockCode = (EditText) findViewById(R.id.edittext_stock_code);
		mEditTextDealProfit = (EditText) findViewById(R.id.edittext_deal_profit);
		mEditTextDealPrice = (EditText) findViewById(R.id.edittext_deal_price);
		mEditTextDealVolume = (EditText) findViewById(R.id.edittext_deal_volume);
		mButtonAdd = (Button) findViewById(R.id.button_add);
		mButtonSubtract = (Button) findViewById(R.id.button_subtract);
		mSpinnerStockAcion = (Spinner) findViewById(R.id.spinner_stock_action);
		mButtonOk = (Button) findViewById(R.id.button_ok);
		mButtonCancel = (Button) findViewById(R.id.button_cancel);

		mEditTextStockName.setOnClickListener(this);
		mEditTextStockCode.setOnClickListener(this);
		mEditTextDealProfit.setOnClickListener(this);
		mEditTextDealPrice.setOnClickListener(this);
		mEditTextDealVolume.setOnClickListener(this);
		mButtonAdd.setOnClickListener(this);
		mButtonSubtract.setOnClickListener(this);
		mButtonOk.setOnClickListener(this);
		mButtonCancel.setOnClickListener(this);

		mEditTextStockName.setInputType(InputType.TYPE_NULL);
		mEditTextStockName.setFocusable(false);
		mEditTextStockCode.setInputType(InputType.TYPE_NULL);
		mEditTextStockCode.setFocusable(false);
		mEditTextDealProfit.setInputType(InputType.TYPE_NULL);
		mEditTextDealProfit.setFocusable(false);

		mListStockAction = new ArrayList<String>();
		mListStockAction.add("");
		mListStockAction.add(Constants.PERIOD_MONTH);
		mListStockAction.add(Constants.PERIOD_WEEK);
		mListStockAction.add(Constants.PERIOD_DAY);
		mListStockAction.add(Constants.PERIOD_MIN60);
		mListStockAction.add(Constants.PERIOD_MIN30);
		mListStockAction.add(Constants.PERIOD_MIN15);
		mListStockAction.add(Constants.PERIOD_MIN5);

		mArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, mListStockAction);
		mArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerStockAcion.setAdapter(mArrayAdapter);

		if (ACTION_DEAL_INSERT.equals(mAction)) {
			setTitle(R.string.deal_insert);
		} else if (ACTION_DEAL_EDIT.equals(mAction)) {
			setTitle(R.string.deal_edit);
		}

        mEditTextDealVolume.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
				String volumeString = s.toString();
				long volume = 0;
				if (!TextUtils.isEmpty(volumeString)) {
					if (volumeString.equals("+") || volumeString.equals("-")) {
						return;
					}

					volume = Long.valueOf(volumeString);
				}

				if (volume == 0) {
					return;
				}

				mDeal.setVolume(volume);

				mDeal.setupProfit(mStock.getRDate(), mStock.getDividend());
				mEditTextDealProfit.setText(String.valueOf(mDeal.getProfit()));
            }
        });
	}

	void updateView() {
		mEditTextStockName.setText(mDeal.getName());
		mEditTextStockCode.setText(mDeal.getCode());
		mEditTextDealProfit.setText(String.valueOf(mDeal.getProfit()));
		mEditTextDealPrice.setText(String.valueOf(mDeal.getDeal()));
		mEditTextDealVolume.setText(String.valueOf(mDeal.getVolume()));
		String dealAction = mDeal.getAction();
		for (int i = 0; i < mListStockAction.size(); i++) {
			if (mListStockAction.get(i).equals(dealAction)) {
				mSpinnerStockAcion.setSelection(i);
				break;
			}
		}
	}

	void setupDealPrice() {
		double dealPrice = 0;
		dealPrice = mStockDatabaseManager.getStockDealTargetPrice(mStock, mOrder);
		dealPrice = Utility.Round(dealPrice, Constants.DOUBLE_FIXED_DECIMAL);

		if (dealPrice > 0) {
			mDeal.setDeal(dealPrice);
			mEditTextDealPrice.setText(String.valueOf(mDeal.getDeal()));

			mDeal.setupProfit(mStock.getRDate(), mStock.getDividend());
			mEditTextDealProfit.setText(String.valueOf(mDeal.getProfit()));
		}
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
			if (ACTION_DEAL_INSERT.equals(mAction)) {
				Intent intent = new Intent(this, StockListActivity.class);
				intent.setAction(StockListActivity.ACTION_STOCK_ID);
				startActivityForResult(intent, REQUEST_CODE_STOCK_ID);
			}
			break;
		case R.id.button_add:
			mOrder--;
			setupDealPrice();
			break;
		case R.id.button_subtract:
			mOrder++;
			setupDealPrice();
			break;
		case R.id.button_ok:
			String dealString = "";
			String volumeString = "";

			String dealAction = mSpinnerStockAcion.getSelectedItem().toString();
			mDeal.setAction(dealAction);

			dealString = mEditTextDealPrice.getText().toString();
			if (!TextUtils.isEmpty(dealString)) {
				mDeal.setDeal(Double.valueOf(dealString));
			} else {
				mDeal.setDeal(0);
			}

			volumeString = mEditTextDealVolume.getText().toString();
			if (!TextUtils.isEmpty(volumeString)) {
				mDeal.setVolume(Long.valueOf(volumeString));
			} else {
				mDeal.setVolume(0);
			}
			mDeal.setupNet();
			mDeal.setupProfit(mStock.getRDate(), mStock.getDividend());
			mDeal.setupValue();
			mHandler.sendEmptyMessage(MESSAGE_SAVE_DEAL);
			setResult(RESULT_OK);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_STOCK_ID:
				if (mStock != null) {
					mStock.setId(data.getLongExtra(Constants.EXTRA_STOCK_ID,
							Constants.STOCK_ID_INVALID));
					mHandler.sendEmptyMessage(MESSAGE_LOAD_STOCK_BY_ID);
				}
				break;

			default:
				break;
			}
		}
	}
}
