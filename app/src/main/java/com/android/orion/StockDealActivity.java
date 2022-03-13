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
import com.android.orion.utility.RecordFile;
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
	EditText mEditTextBuyPrice;
	EditText mEditTextSellPrice;
	EditText mEditTextDealVolume;

	Button mButtonOk;
	Button mButtonCancel;

	ArrayAdapter<String> mArrayAdapter;
	List<String> mListStockAction;
	Spinner mSpinnerStockAcion;

	StockDeal mDeal = null;

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
				RecordFile.writeDealFile(mStock, mDeal, Constants.DEAL_OPERATE_EDIT);
				break;

			case MESSAGE_SAVE_DEAL:
				if (ACTION_DEAL_INSERT.equals(mAction)) {
					mDeal.setCreated(Utility.getCurrentDateTimeString());
					RecordFile.writeDealFile(mStock, mDeal, Constants.DEAL_OPERATE_INSERT);
					mStockDatabaseManager.insertStockDeal(mDeal);
				} else if (ACTION_DEAL_EDIT.equals(mAction)) {
					mDeal.setModified(Utility.getCurrentDateTimeString());
					RecordFile.writeDealFile(mStock, mDeal, Constants.DEAL_OPERATE_EDIT);
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
				mDeal.setBuy(mStock.getPrice());
				updateView();
				break;

			case MESSAGE_LOAD_STOCK_BY_SE_CODE:
				mStockDatabaseManager.getStock(mStock);
				mDeal.setSE(mStock.getSE());
				mDeal.setCode(mStock.getCode());
				mDeal.setName(mStock.getName());
				mDeal.setPrice(mStock.getPrice());
				mDeal.setBuy(mStock.getPrice());
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
		mEditTextBuyPrice = (EditText) findViewById(R.id.edittext_buy_price);
		mEditTextSellPrice = (EditText) findViewById(R.id.edittext_sell_price);
		mEditTextDealVolume = (EditText) findViewById(R.id.edittext_deal_volume);
		mSpinnerStockAcion = (Spinner) findViewById(R.id.spinner_stock_action);
		mButtonOk = (Button) findViewById(R.id.button_ok);
		mButtonCancel = (Button) findViewById(R.id.button_cancel);

		mEditTextStockName.setOnClickListener(this);
		mEditTextStockCode.setOnClickListener(this);
		mEditTextDealProfit.setOnClickListener(this);
		mEditTextBuyPrice.setOnClickListener(this);
		mEditTextSellPrice.setOnClickListener(this);
		mEditTextDealVolume.setOnClickListener(this);
		mButtonOk.setOnClickListener(this);
		mButtonCancel.setOnClickListener(this);

		mEditTextStockName.setInputType(InputType.TYPE_NULL);
		mEditTextStockName.setFocusable(false);
		mEditTextStockCode.setInputType(InputType.TYPE_NULL);
		mEditTextStockCode.setFocusable(false);
		mEditTextDealProfit.setInputType(InputType.TYPE_NULL);
		mEditTextDealProfit.setFocusable(false);

		mEditTextStockName.setEnabled(false);
		mEditTextStockCode.setEnabled(false);
		mEditTextDealProfit.setEnabled(false);

		mListStockAction = new ArrayList<String>();
		mListStockAction.add("");
		mListStockAction.add(Settings.KEY_PERIOD_MONTH);
		mListStockAction.add(Settings.KEY_PERIOD_WEEK);
		mListStockAction.add(Settings.KEY_PERIOD_DAY);
		mListStockAction.add(Settings.KEY_PERIOD_MIN60);
		mListStockAction.add(Settings.KEY_PERIOD_MIN30);
		mListStockAction.add(Settings.KEY_PERIOD_MIN15);
		mListStockAction.add(Settings.KEY_PERIOD_MIN5);

		mArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, mListStockAction);
		mArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerStockAcion.setAdapter(mArrayAdapter);

		if (ACTION_DEAL_INSERT.equals(mAction)) {
			setTitle(R.string.deal_insert);
			mDeal.setAction(Settings.KEY_PERIOD_DAY);
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

				mDeal.setupFee(mStock.getRDate(), mStock.getDividend());
				mDeal.setupNet();
				mDeal.setupValue();
				mDeal.setupProfit();
				mEditTextDealProfit.setText(String.valueOf(mDeal.getProfit()));
            }
        });
	}

	void updateView() {
		mEditTextStockName.setText(mDeal.getName());
		mEditTextStockCode.setText(mDeal.getCode());
		mEditTextDealProfit.setText(String.valueOf(mDeal.getProfit()));
		mEditTextBuyPrice.setText(String.valueOf(mDeal.getBuy()));
		mEditTextSellPrice.setText(String.valueOf(mDeal.getSell()));
		mEditTextDealVolume.setText(String.valueOf(mDeal.getVolume()));
		String dealAction = mDeal.getAction();
		for (int i = 0; i < mListStockAction.size(); i++) {
			if (mListStockAction.get(i).equals(dealAction)) {
				mSpinnerStockAcion.setSelection(i);
				break;
			}
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
		case R.id.button_ok:
			String buyString = "";
			String sellString = "";
			String volumeString = "";

			String dealAction = mSpinnerStockAcion.getSelectedItem().toString();
			mDeal.setAction(dealAction);

			buyString = mEditTextBuyPrice.getText().toString();
			if (!TextUtils.isEmpty(buyString)) {
				mDeal.setBuy(Double.valueOf(buyString));
			} else {
				mDeal.setBuy(0);
			}

			sellString = mEditTextSellPrice.getText().toString();
			if (!TextUtils.isEmpty(sellString)) {
				mDeal.setSell(Double.valueOf(sellString));
			} else {
				mDeal.setSell(0);
			}

			volumeString = mEditTextDealVolume.getText().toString();
			if (!TextUtils.isEmpty(volumeString)) {
				mDeal.setVolume(Long.valueOf(volumeString));
			} else {
				mDeal.setVolume(0);
			}

			mDeal.setupFee(mStock.getRDate(), mStock.getDividend());
			mDeal.setupNet();
			mDeal.setupValue();
			mDeal.setupProfit();
			mDeal.setupBonus(mStock.getDividend());
			mDeal.setupYield(mStock.getDividend());
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
							Stock.INVALID_ID));
					mHandler.sendEmptyMessage(MESSAGE_LOAD_STOCK_BY_ID);
				}
				break;

			default:
				break;
			}
		}
	}
}
