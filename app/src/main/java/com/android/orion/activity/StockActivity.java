package com.android.orion.activity;

import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.orion.R;
import com.android.orion.chart.TradeLevelPicker;
import com.android.orion.data.Period;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockTrend;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Utility;

public class StockActivity extends DatabaseActivity implements OnClickListener {

	long mHoldA = 0;
	long mHoldB = 0;
	CheckBox mCheckBoxTrade;
	CheckBox mCheckBoxCustom;
	EditText mEditTextStockName;
	EditText mEditTextStockCode;
	EditText mEditTextStockHold;
	EditText mEditTextStockYield;
	EditText mEditTextStockHoldA;
	EditText mEditTextStockHoldB;
	TextView mTextViewSeUrl;
	TradeLevelPicker mTradeLevelPickerDay;
	TradeLevelPicker mTradeLevelPickerMin60;
	TradeLevelPicker mTradeLevelPickerMin30;
	TradeLevelPicker mTradeLevelPickerMin15;
	TradeLevelPicker mTradeLevelPickerMin5;
	Button mButtonOk;
	Button mButtonCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock);
		if (mStock == null) {
			mStock = new Stock();
		}
		initView();
		setupTradeLevelPickers();
		updateView();
	}

	void initView() {
		mCheckBoxTrade = findViewById(R.id.checkbox_trade);
		mCheckBoxCustom = findViewById(R.id.checkbox_custom);
		mEditTextStockName = findViewById(R.id.edittext_stock_name);
		mEditTextStockCode = findViewById(R.id.edittext_stock_code);
		mEditTextStockHold = findViewById(R.id.edittext_stock_hold);
		mEditTextStockYield = findViewById(R.id.edittext_stock_yield);
		mEditTextStockHoldA = findViewById(R.id.edittext_stock_hold_a);
		mEditTextStockHoldB = findViewById(R.id.edittext_stock_hold_b);
		mTextViewSeUrl = findViewById(R.id.textview_se_url);
		mTradeLevelPickerDay = findViewById(R.id.trade_level_picker_day);
		mTradeLevelPickerMin60 = findViewById(R.id.trade_level_picker_min60);
		mTradeLevelPickerMin30 = findViewById(R.id.trade_level_picker_min30);
		mTradeLevelPickerMin15 = findViewById(R.id.trade_level_picker_min15);
		mTradeLevelPickerMin5 = findViewById(R.id.trade_level_picker_min5);
		mButtonOk = findViewById(R.id.button_ok);
		mButtonCancel = findViewById(R.id.button_cancel);

		mCheckBoxTrade.setOnClickListener(this);
		mCheckBoxCustom.setOnClickListener(this);
		mEditTextStockName.setOnClickListener(this);
		mEditTextStockCode.setOnClickListener(this);
		mEditTextStockHold.setOnClickListener(this);
		mEditTextStockYield.setOnClickListener(this);
		mTextViewSeUrl.setOnClickListener(this);
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
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {
				if (TextUtils.equals(mAction, Constant.ACTION_STOCK_EDIT)) {
					return;
				}

				if ((s != null) && (s.length() > 0)) {
					if (s.toString().startsWith(Stock.CODE_PREFIX_0) || s.toString().startsWith(Stock.CODE_PREFIX_3)) {
						mStock.setClasses(Stock.CLASS_A);
						mStock.setSE(Stock.SE_SZ);
					} else if (s.toString().startsWith(Stock.CODE_PREFIX_5) || s.toString().startsWith(Stock.CODE_PREFIX_6)) {
						mStock.setClasses(Stock.CLASS_A);
						mStock.setSE(Stock.SE_SH);
					} else {
						mStock.setClasses(Stock.CLASS_INDEX);
						mStock.setSE(Stock.SE_SH);
					}
					mStock.setCode(s.toString());
				}
			}
		});

		if (TextUtils.equals(mAction, Constant.ACTION_FAVORITE_STOCK_INSERT)) {
			setTitle(R.string.stock_insert);
			mStock.addFlag(Stock.FLAG_FAVORITE);
		} else if (TextUtils.equals(mAction, Constant.ACTION_STOCK_EDIT)) {
			setTitle(R.string.stock_edit);
			mEditTextStockCode.setEnabled(false);
			mEditTextStockHold.setEnabled(false);
			mEditTextStockYield.setEnabled(false);
			mEditTextStockHoldA.setEnabled(false);
			mEditTextStockHoldB.setEnabled(false);
			mStock.setId(mIntent.getLongExtra(Constant.EXTRA_STOCK_ID,
					DatabaseContract.INVALID_ID));
			mStockDatabaseManager.getStockById(mStock);
			mHoldB = mStockDatabaseManager.getStockDealBuy(mStock, Stock.ACCOUNT_B);
			if (mHoldB > 0) {
				findViewById(R.id.layout_stock_hold_account).setVisibility(View.VISIBLE);
				mHoldA = mStock.getHold() - mHoldB;
				mEditTextStockHoldA.setText(String.valueOf(mHoldA));
				mEditTextStockHoldB.setText(String.valueOf(mHoldB));
			}
		}
	}

	private void setupTradeLevelPickers() {
		mTradeLevelPickerDay.setMinValue(StockTrend.LEVEL_NONE);
		mTradeLevelPickerDay.setMaxValue(StockTrend.LEVEL_TREND_LINE);

		mTradeLevelPickerMin60.setMinValue(StockTrend.LEVEL_NONE);
		mTradeLevelPickerMin60.setMaxValue(StockTrend.LEVEL_TREND_LINE);

		mTradeLevelPickerMin30.setMinValue(StockTrend.LEVEL_NONE);
		mTradeLevelPickerMin30.setMaxValue(StockTrend.LEVEL_TREND_LINE);

		mTradeLevelPickerMin15.setMinValue(StockTrend.LEVEL_NONE);
		mTradeLevelPickerMin15.setMaxValue(StockTrend.LEVEL_TREND_LINE);

		mTradeLevelPickerMin5.setMinValue(StockTrend.LEVEL_NONE);
		mTradeLevelPickerMin5.setMaxValue(StockTrend.LEVEL_TREND_LINE);
	}

	void updateView() {
		mCheckBoxTrade.setChecked(mStock.hasFlag(Stock.FLAG_TRADE));
		mCheckBoxCustom.setChecked(mStock.hasFlag(Stock.FLAG_CUSTOM));

		mEditTextStockName.setText(mStock.getName());
		mEditTextStockCode.setText(mStock.getCode());
		mEditTextStockHold.setText(String.valueOf(mStock.getHold()));
		mEditTextStockYield.setText(String.valueOf(mStock.getYield()));
		mTextViewSeUrl.setText(mStock.getSeUrl());
		mTextViewSeUrl.setPaintFlags(mTextViewSeUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

		boolean custom = mStock.hasFlag(Stock.FLAG_CUSTOM);
		mTradeLevelPickerDay.setEnabled(custom);
		mTradeLevelPickerMin60.setEnabled(custom);
		mTradeLevelPickerMin30.setEnabled(custom);
		mTradeLevelPickerMin15.setEnabled(custom);
		mTradeLevelPickerMin5.setEnabled(custom);

		mTradeLevelPickerDay.setValue(mStock.getLevel(Period.DAY));
		mTradeLevelPickerMin60.setValue(mStock.getLevel(Period.MIN60));
		mTradeLevelPickerMin30.setValue(mStock.getLevel(Period.MIN30));
		mTradeLevelPickerMin15.setValue(mStock.getLevel(Period.MIN15));
		mTradeLevelPickerMin5.setValue(mStock.getLevel(Period.MIN5));

		mTradeLevelPickerDay.invalidate();
		mTradeLevelPickerMin60.invalidate();
		mTradeLevelPickerMin30.invalidate();
		mTradeLevelPickerMin15.invalidate();
		mTradeLevelPickerMin5.invalidate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_deal_edit, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onClick(@NonNull View view) {
		int viewId = view.getId();

		switch (viewId) {
			case R.id.checkbox_trade:
				if (mCheckBoxTrade.isChecked()) {
					mStock.addFlag(Stock.FLAG_TRADE);
				} else {
					mStock.removeFlag(Stock.FLAG_TRADE);
					mStock.setBuyProfit(0);
					mStock.setSellProfit(0);
				}
				break;

			case R.id.checkbox_custom:
				if (mCheckBoxCustom.isChecked()) {
					mStock.addFlag(Stock.FLAG_CUSTOM);
				} else {
					mStock.removeFlag(Stock.FLAG_CUSTOM);
				}
				updateView();
				break;

			case R.id.button_ok:
				if (mCheckBoxTrade.isChecked()) {
					mStock.addFlag(Stock.FLAG_TRADE);
				} else {
					mStock.removeFlag(Stock.FLAG_TRADE);
					mStock.setBuyProfit(0);
					mStock.setSellProfit(0);
				}

				if (mCheckBoxCustom.isChecked()) {
					mStock.addFlag(Stock.FLAG_CUSTOM);
				} else {
					mStock.removeFlag(Stock.FLAG_CUSTOM);
				}

				String name = mEditTextStockName.getText().toString();
				String code = mEditTextStockCode.getText().toString();

				if (!TextUtils.isEmpty(name)) {
					mStock.setName(name);
				}

				if (!TextUtils.isEmpty(code) && code.length() == Stock.CODE_LENGTH) {
					mStock.setCode(code);
				} else {
					Toast.makeText(mContext, R.string.stock_code_empty,
							Toast.LENGTH_LONG).show();
					return;
				}

				if (mStock.hasFlag(Stock.FLAG_CUSTOM)) {
					mStock.setLevel(Period.DAY, mTradeLevelPickerDay.getValue());
					mStock.setLevel(Period.MIN60, mTradeLevelPickerMin60.getValue());
					mStock.setLevel(Period.MIN30, mTradeLevelPickerMin30.getValue());
					mStock.setLevel(Period.MIN15, mTradeLevelPickerMin15.getValue());
					mStock.setLevel(Period.MIN5, mTradeLevelPickerMin5.getValue());
				}

				if (TextUtils.equals(mAction, Constant.ACTION_FAVORITE_STOCK_INSERT)) {
					if (!mStockDatabaseManager.isStockExist(mStock)) {
						mStock.setCreated(Utility.getCurrentDateTimeString());
						Uri uri = mStockDatabaseManager.insertStock(mStock);
						mStockDatabaseManager.getStock(uri, mStock);
						mStockDatabaseManager.updateStock(mStock, mStock.getContentValuesEdit());
					} else {
						mStockDatabaseManager.getStock(mStock);
						updateView();
						Toast.makeText(mContext, R.string.stock_exist,
								Toast.LENGTH_LONG).show();
						return;
					}
				} else if (TextUtils.equals(mAction, Constant.ACTION_STOCK_EDIT)) {
					mStock.setModified(Utility.getCurrentDateTimeString());
					mStockDatabaseManager.updateStock(mStock,
							mStock.getContentValuesEdit());
				}

				getIntent().putExtra(Constant.EXTRA_STOCK_ID, mStock.getId());
				setResult(RESULT_OK, getIntent());
				Setting.setDownloadStockTimeMillis(mStock, 0);
				Setting.setDownloadStockDataTimeMillis(mStock, 0);
				mStockDataProvider.download(mStock);
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
