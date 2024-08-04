package com.android.orion.activity;

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

import com.android.orion.R;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.android.orion.setting.Constant;
import com.android.orion.utility.RecordFile;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.List;

public class StockDealActivity extends DatabaseActivity implements
		OnClickListener {

	static final int MESSAGE_LOAD_DEAL = 0;
	static final int MESSAGE_SAVE_DEAL = 1;
	static final int MESSAGE_LOAD_STOCK_BY_ID = 2;
	static final int MESSAGE_LOAD_STOCK_BY_SE_CODE = 3;

	static final int REQUEST_CODE_STOCK_ID = 0;

	List<String> mListStockAccount;
	ArrayAdapter<String> mArrayAdapterStockAccount;
	Spinner mSpinnerStockAccount;

	EditText mEditTextStockName;
	EditText mEditTextStockCode;
	EditText mEditTextDealProfit;
	EditText mEditTextBuyPrice;
	EditText mEditTextSellPrice;
	EditText mEditTextDealVolume;

	List<String> mListStockOperate;
	ArrayAdapter<String> mArrayAdapterStockOperate;
	Spinner mSpinnerStockOperate;

	Button mButtonOk;
	Button mButtonCancel;

	StockDeal mDeal = null;

	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
				case MESSAGE_LOAD_DEAL:
					mDatabaseManager.getStockDealById(mDeal);
					mStock.setSE(mDeal.getSE());
					mStock.setCode(mDeal.getCode());
					mDatabaseManager.getStock(mStock);
					updateView();
					RecordFile.writeDealFile(mStock, mDeal, Constant.DEAL_OPERATE_EDIT);
					break;

				case MESSAGE_SAVE_DEAL:
					if (TextUtils.equals(mAction, Constant.ACTION_DEAL_INSERT)) {
						mDeal.setCreated(Utility.getCurrentDateTimeString());
						RecordFile.writeDealFile(mStock, mDeal, Constant.DEAL_OPERATE_INSERT);
						mDatabaseManager.insertStockDeal(mDeal);
					} else if (TextUtils.equals(mAction, Constant.ACTION_DEAL_EDIT)) {
						mDeal.setModified(Utility.getCurrentDateTimeString());
						RecordFile.writeDealFile(mStock, mDeal, Constant.DEAL_OPERATE_EDIT);
						mDatabaseManager.updateStockDealByID(mDeal);
					}
					mDatabaseManager.updateStockDeal(mStock);
					mDatabaseManager.updateStock(mStock,
							mStock.getContentValues());
					break;

				case MESSAGE_LOAD_STOCK_BY_ID:
					mDatabaseManager.getStockById(mStock);
					mDeal.setSE(mStock.getSE());
					mDeal.setCode(mStock.getCode());
					mDeal.setName(mStock.getName());
					mDeal.setPrice(mStock.getPrice());
					mDeal.setBuy(mStock.getPrice());
					updateView();
					break;

				case MESSAGE_LOAD_STOCK_BY_SE_CODE:
					mDatabaseManager.getStock(mStock);
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

		if (TextUtils.equals(mAction, Constant.ACTION_DEAL_INSERT)) {
			if (mBundle != null) {
				mStock.setSE(mBundle.getString(Constant.EXTRA_STOCK_SE));
				mStock.setCode(mBundle.getString(Constant.EXTRA_STOCK_CODE));
				mHandler.sendEmptyMessage(MESSAGE_LOAD_STOCK_BY_SE_CODE);
			}
		} else if (TextUtils.equals(mAction, Constant.ACTION_DEAL_EDIT)) {
			mDeal.setId(mIntent.getLongExtra(Constant.EXTRA_DEAL_ID, 0));
			mHandler.sendEmptyMessage(MESSAGE_LOAD_DEAL);
		}
	}

	void initView() {
		mSpinnerStockAccount = findViewById(R.id.spinner_stock_account);
		mEditTextStockName = findViewById(R.id.edittext_stock_name);
		mEditTextStockCode = findViewById(R.id.edittext_stock_code);
		mEditTextDealProfit = findViewById(R.id.edittext_deal_profit);
		mEditTextBuyPrice = findViewById(R.id.edittext_buy_price);
		mEditTextSellPrice = findViewById(R.id.edittext_sell_price);
		mEditTextDealVolume = findViewById(R.id.edittext_deal_volume);
		mSpinnerStockOperate = findViewById(R.id.spinner_stock_operate);
		mButtonOk = findViewById(R.id.button_ok);
		mButtonCancel = findViewById(R.id.button_cancel);

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

		mListStockAccount = new ArrayList<String>();
		mListStockAccount.add("");
		mListStockAccount.add(StockDeal.ACCOUNT_A);
		mListStockAccount.add(StockDeal.ACCOUNT_B);

		mArrayAdapterStockAccount = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, mListStockAccount);
		mArrayAdapterStockAccount
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerStockAccount.setAdapter(mArrayAdapterStockAccount);

		mListStockOperate = new ArrayList<String>();
		mListStockOperate.add("");
		mListStockOperate.add(DatabaseContract.COLUMN_MONTH);
		mListStockOperate.add(DatabaseContract.COLUMN_WEEK);
		mListStockOperate.add(DatabaseContract.COLUMN_DAY);
		mListStockOperate.add(DatabaseContract.COLUMN_MIN60);
		mListStockOperate.add(DatabaseContract.COLUMN_MIN30);
		mListStockOperate.add(DatabaseContract.COLUMN_MIN15);
		mListStockOperate.add(DatabaseContract.COLUMN_MIN5);

		mArrayAdapterStockOperate = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, mListStockOperate);
		mArrayAdapterStockOperate
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerStockOperate.setAdapter(mArrayAdapterStockOperate);

		if (TextUtils.equals(mAction, Constant.ACTION_DEAL_INSERT)) {
			setTitle(R.string.deal_insert);
			mDeal.setAction(DatabaseContract.COLUMN_DAY);
		} else if (TextUtils.equals(mAction, Constant.ACTION_DEAL_EDIT)) {
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
					if (TextUtils.equals(volumeString, "+") || TextUtils.equals(volumeString, "-")) {
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
		String stockAccount = mDeal.getAccount();
		for (int i = 0; i < mListStockAccount.size(); i++) {
			if (TextUtils.equals(mListStockAccount.get(i), stockAccount)) {
				mSpinnerStockAccount.setSelection(i);
				break;
			}
		}
		mEditTextStockName.setText(mDeal.getName());
		mEditTextStockCode.setText(mDeal.getCode());
		mEditTextDealProfit.setText(String.valueOf(mDeal.getProfit()));
		mEditTextBuyPrice.setText(String.valueOf(mDeal.getBuy()));
		mEditTextSellPrice.setText(String.valueOf(mDeal.getSell()));
		mEditTextDealVolume.setText(String.valueOf(mDeal.getVolume()));
		String dealAction = mDeal.getAction();
		for (int i = 0; i < mListStockOperate.size(); i++) {
			if (TextUtils.equals(mListStockOperate.get(i), dealAction)) {
				mSpinnerStockOperate.setSelection(i);
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
				if (TextUtils.equals(Constant.ACTION_DEAL_INSERT, mAction)) {
					Intent intent = new Intent(this, StockFavoriteListActivity.class);
					intent.setAction(Constant.ACTION_STOCK_ID);
					startActivityForResult(intent, REQUEST_CODE_STOCK_ID);
				}
				break;
			case R.id.button_ok:
				String buyString = "";
				String sellString = "";
				String volumeString = "";

				String stockAccount = mSpinnerStockAccount.getSelectedItem().toString();
				mDeal.setAccount(stockAccount);

				String dealAction = mSpinnerStockOperate.getSelectedItem().toString();
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
						mStock.setId(data.getLongExtra(Constant.EXTRA_STOCK_ID,
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
