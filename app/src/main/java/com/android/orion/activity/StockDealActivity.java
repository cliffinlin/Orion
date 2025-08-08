package com.android.orion.activity;

import android.app.DatePickerDialog;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.orion.R;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.android.orion.setting.Constant;
import com.android.orion.utility.Symbol;
import com.android.orion.utility.RecordFile;
import com.android.orion.utility.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StockDealActivity extends DatabaseActivity implements
		OnClickListener, RadioGroup.OnCheckedChangeListener {

	static final int MESSAGE_LOAD_DEAL = 0;
	static final int MESSAGE_SAVE_DEAL = 1;
	static final int MESSAGE_LOAD_STOCK_BY_ID = 2;
	static final int MESSAGE_LOAD_STOCK_BY_SE_CODE = 3;

	static final int REQUEST_CODE_STOCK_ID = 0;

	List<String> mListStockAccount;
	ArrayAdapter<String> mArrayAdapterStockAccount;
	Spinner mSpinnerStockAccount;
	RadioGroup mRadioGroupDealType;

	EditText mEditTextStockName;
	EditText mEditTextStockCode;
	EditText mEditTextDealProfit;
	EditText mEditTextBuyPrice;
	EditText mEditTextSellPrice;
	EditText mEditTextDealVolume;
	EditText mEditTextDealDate;

	Button mButtonOk;
	Button mButtonCancel;

	StockDeal mDeal = null;

	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
				case MESSAGE_LOAD_DEAL:
					mStockDatabaseManager.getStockDeal(mDeal);
					mStock.setSE(mDeal.getSE());
					mStock.setCode(mDeal.getCode());
					mStockDatabaseManager.getStock(mStock);
					updateView();
					RecordFile.writeDealFile(mStock, mDeal, Constant.DEAL_EDIT);
					break;

				case MESSAGE_SAVE_DEAL:
					if (TextUtils.equals(mAction, Constant.ACTION_DEAL_INSERT)) {
						mDeal.setCreated(Utility.getCurrentDateTimeString());
						RecordFile.writeDealFile(mStock, mDeal, Constant.DEAL_INSERT);
						mStockDatabaseManager.insertStockDeal(mDeal);
					} else if (TextUtils.equals(mAction, Constant.ACTION_DEAL_EDIT)) {
						mDeal.setModified(Utility.getCurrentDateTimeString());
						RecordFile.writeDealFile(mStock, mDeal, Constant.DEAL_EDIT);
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
					mDeal.setDate(Utility.getCurrentDateString());
					updateView();
					break;

				case MESSAGE_LOAD_STOCK_BY_SE_CODE:
					mStockDatabaseManager.getStock(mStock);
					mDeal.setSE(mStock.getSE());
					mDeal.setCode(mStock.getCode());
					mDeal.setName(mStock.getName());
					mDeal.setPrice(mStock.getPrice());
					mDeal.setBuy(mStock.getPrice());
					mDeal.setDate(Utility.getCurrentDateString());
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
		mRadioGroupDealType = findViewById(R.id.radiogroup_deal_type);
		mEditTextStockName = findViewById(R.id.edittext_stock_name);
		mEditTextStockCode = findViewById(R.id.edittext_stock_code);
		mEditTextDealProfit = findViewById(R.id.edittext_deal_profit);
		mEditTextBuyPrice = findViewById(R.id.edittext_buy_price);
		mEditTextSellPrice = findViewById(R.id.edittext_sell_price);
		mEditTextDealVolume = findViewById(R.id.edittext_deal_volume);
		mEditTextDealDate = findViewById(R.id.edittext_deal_date);
		mButtonOk = findViewById(R.id.button_ok);
		mButtonCancel = findViewById(R.id.button_cancel);

		mRadioGroupDealType.setOnCheckedChangeListener(this);
		mEditTextStockName.setOnClickListener(this);
		mEditTextStockCode.setOnClickListener(this);
		mEditTextDealProfit.setOnClickListener(this);
		mEditTextBuyPrice.setOnClickListener(this);
		mEditTextSellPrice.setOnClickListener(this);
		mEditTextDealVolume.setOnClickListener(this);
		mEditTextDealDate.setOnClickListener(this);
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

		mListStockAccount = new ArrayList<>();
		mListStockAccount.add("");
		mListStockAccount.add(StockDeal.ACCOUNT_A);
		mListStockAccount.add(StockDeal.ACCOUNT_B);

		mArrayAdapterStockAccount = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, mListStockAccount);
		mArrayAdapterStockAccount
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerStockAccount.setAdapter(mArrayAdapterStockAccount);

		if (TextUtils.equals(mAction, Constant.ACTION_DEAL_INSERT)) {
			setTitle(R.string.deal_insert);
		} else if (TextUtils.equals(mAction, Constant.ACTION_DEAL_EDIT)) {
			setTitle(R.string.deal_edit);
		}

		mEditTextBuyPrice.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String buyPriceString = s.toString();
				double buy = 0;
				if (!TextUtils.isEmpty(buyPriceString)) {
					if (TextUtils.equals(buyPriceString, Symbol.ADD) || TextUtils.equals(buyPriceString, Symbol.MINUS)) {
						return;
					}
					buy = Double.parseDouble(buyPriceString);
				}
				if (buy == 0) {
					return;
				}
				mDeal.setBuy(buy);
				setupDeal();
				mEditTextDealProfit.setText(String.valueOf(mDeal.getProfit()));
			}
		});

		mEditTextSellPrice.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String sellPriceString = s.toString();
				double sell = 0;
				if (!TextUtils.isEmpty(sellPriceString)) {
					if (TextUtils.equals(sellPriceString, Symbol.ADD) || TextUtils.equals(sellPriceString, Symbol.MINUS)) {
						return;
					}
					sell = Double.parseDouble(sellPriceString);
				}
				if (sell == 0) {
					return;
				}
				mDeal.setSell(sell);
				setupDeal();
				mEditTextDealProfit.setText(String.valueOf(mDeal.getProfit()));
			}
		});

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
					if (TextUtils.equals(volumeString, Symbol.ADD) || TextUtils.equals(volumeString, Symbol.MINUS)) {
						return;
					}
					volume = Long.parseLong(volumeString);
				}
				if (volume == 0) {
					return;
				}
				mDeal.setVolume(volume);
				setupDeal();
				mEditTextDealProfit.setText(String.valueOf(mDeal.getProfit()));
			}
		});
	}

	void setupDeal() {
		mDeal.setupFee(mStock.getRDate(), mStock.getDividend());
		mDeal.setupNet();
		mDeal.setupValue();
		mDeal.setupProfit();
		mDeal.setupBonus(mStock.getDividend());
		mDeal.setupYield(mStock.getDividend());
	}

	void updateView() {
		String stockAccount = mDeal.getAccount();
		for (int i = 0; i < mListStockAccount.size(); i++) {
			if (TextUtils.equals(mListStockAccount.get(i), stockAccount)) {
				mSpinnerStockAccount.setSelection(i);
				break;
			}
		}
		if (TextUtils.equals(mDeal.getType(), StockDeal.TYPE_BUY)) {
			mRadioGroupDealType.check(R.id.radio_deal_buy);
			mEditTextBuyPrice.setEnabled(true);
			mEditTextSellPrice.setEnabled(false);
		} else {
			mRadioGroupDealType.check(R.id.radio_deal_sell);
			mEditTextBuyPrice.setEnabled(false);
			mEditTextSellPrice.setEnabled(true);
		}
		mEditTextStockName.setText(mDeal.getName());
		mEditTextStockCode.setText(mDeal.getCode());
		mEditTextDealProfit.setText(String.valueOf(mDeal.getProfit()));
		mEditTextBuyPrice.setText(String.valueOf(mDeal.getBuy()));
		mEditTextSellPrice.setText(String.valueOf(mDeal.getSell()));
		mEditTextDealVolume.setText(String.valueOf(mDeal.getVolume()));
		mEditTextDealDate.setText(mDeal.getDate());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_deal_edit, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (group == mRadioGroupDealType) {
			switch (checkedId) {
				case R.id.radio_deal_buy:
					mEditTextBuyPrice.setEnabled(true);
					mEditTextSellPrice.setEnabled(false);
					break;
				case R.id.radio_deal_sell:
					mEditTextBuyPrice.setEnabled(false);
					mEditTextSellPrice.setEnabled(true);
					break;
			}
		}
	}

	@Override
	public void onClick(View view) {
		int viewId = view.getId();

		switch (viewId) {
			case R.id.edittext_stock_name:
			case R.id.edittext_stock_code:
				if (TextUtils.equals(Constant.ACTION_DEAL_INSERT, mAction)) {
					Intent intent = new Intent(this, StockFavoriteListActivity.class);
					intent.setAction(Constant.ACTION_STOCK_ID);
					startActivityForResult(intent, REQUEST_CODE_STOCK_ID);
				}
				break;
			case R.id.edittext_deal_date:
				showDatePicker();
				break;
			case R.id.button_ok:
				String buyString = "";
				String sellString = "";
				String volumeString = "";

				String stockAccount = mSpinnerStockAccount.getSelectedItem().toString();
				mDeal.setAccount(stockAccount);

				int id = mRadioGroupDealType.getCheckedRadioButtonId();
				if (id == R.id.radio_deal_buy) {
					mDeal.setType(StockDeal.TYPE_BUY);
				} else if (id == R.id.radio_deal_sell) {
					mDeal.setType(StockDeal.TYPE_SELL);
				}

				String dealDate = mEditTextDealDate.getText().toString();
				mDeal.setDate(dealDate);
				if (TextUtils.isEmpty(dealDate)) {
					Toast.makeText(mContext, R.string.stock_deal_date_empty, Toast.LENGTH_LONG).show();
				}

				buyString = mEditTextBuyPrice.getText().toString();
				if (TextUtils.isEmpty(buyString)) {
					mDeal.setBuy(0);
				} else {
					mDeal.setBuy(Double.parseDouble(buyString));
				}

				sellString = mEditTextSellPrice.getText().toString();
				if (TextUtils.isEmpty(sellString)) {
					mDeal.setSell(0);
				} else {
					mDeal.setSell(Double.parseDouble(sellString));
				}

				volumeString = mEditTextDealVolume.getText().toString();
				long volumeValue = TextUtils.isEmpty(volumeString) ? 0 : Long.parseLong(volumeString);
				if (volumeValue == 0) {
					Toast.makeText(mContext, R.string.stock_deal_volume_empty, Toast.LENGTH_LONG).show();
					return;
				}
				mDeal.setVolume(volumeValue);

				setupDeal();
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
								DatabaseContract.INVALID_ID));
						mHandler.sendEmptyMessage(MESSAGE_LOAD_STOCK_BY_ID);
					}
					break;

				default:
					break;
			}
		}
	}

	private void showDatePicker() {
		Calendar calendar = Calendar.getInstance();
		DatePickerDialog datePickerDialog = new DatePickerDialog(
				this,
				(view, year, month, dayOfMonth) -> {
					Calendar selectedDate = Calendar.getInstance();
					selectedDate.set(year, month, dayOfMonth);
					SimpleDateFormat sdf = new SimpleDateFormat(Utility.CALENDAR_DATE_FORMAT, Locale.getDefault());
					mEditTextDealDate.setText(sdf.format(selectedDate.getTime()));
				},
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH));

		datePickerDialog.show();
	}

}
