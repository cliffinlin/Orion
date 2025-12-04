package com.android.orion.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.orion.R;
import com.android.orion.widget.TradeLevelPicker;
import com.android.orion.data.Period;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockTrend;
import com.android.orion.constant.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StockActivity extends StorageActivity implements OnClickListener {

	long mHoldA = 0;
	long mHoldB = 0;
	String mOrginStockWindow = "";
	CheckBox mCheckBoxTrade;
	EditText mEditTextStockName;
	EditText mEditTextStockCode;
	EditText mEditTextStockWindow;
	EditText mEditTextStockQuota;
	EditText mEditTextStockQuotaEst;
	EditText mEditTextStockTrading;
	EditText mEditTextStockTradingCost;
	EditText mEditTextStockHold;
	EditText mEditTextStockYield;
	EditText mEditTextStockHoldA;
	EditText mEditTextStockHoldB;
	TextView mTextviewMonthNet;
	TextView mTextviewWeekNet;
	TextView mTextviewDayNet;
	TextView mTextviewMin60Net;
	TextView mTextviewMin30Net;
	TextView mTextviewMin15Net;
	TextView mTextviewMin5Net;
	TradeLevelPicker mTradeLevelPickerMonth;
	TradeLevelPicker mTradeLevelPickerWeek;
	TradeLevelPicker mTradeLevelPickerDay;
	TradeLevelPicker mTradeLevelPickerMin60;
	TradeLevelPicker mTradeLevelPickerMin30;
	TradeLevelPicker mTradeLevelPickerMin15;
	TradeLevelPicker mTradeLevelPickerMin5;
	ImageView mImageViewRestoreTarget;
	ImageView mImageViewMonthTarget;
	ImageView mImageViewWeekTarget;
	ImageView mImageViewDayTarget;
	ImageView mImageViewMin60Target;
	ImageView mImageViewMin30Target;
	ImageView mImageViewMin15Target;
	ImageView mImageViewMin5Target;
	Button mButtonOk;
	Button mButtonCancel;

	LinearLayout mLayoutTradeLevelPickers;
	LinearLayout mLayoutMonthPicker;
	LinearLayout mLayoutWeekPicker;
	LinearLayout mLayoutDayPicker;
	LinearLayout mLayoutMin60Picker;
	LinearLayout mLayoutMin30Picker;
	LinearLayout mLayoutMin15Picker;
	LinearLayout mLayoutMin5Picker;

	LinearLayout mLayoutStockNameCodeDisplay;
	LinearLayout mLayoutStockNameCodeEdit;
	TextView mTextViewDisplayStockName;
	TextView mTextViewDisplayStockCode;

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
		mEditTextStockName = findViewById(R.id.edittext_stock_name);
		mEditTextStockCode = findViewById(R.id.edittext_stock_code);
		mEditTextStockWindow = findViewById(R.id.edittext_stock_window);
		mEditTextStockQuota = findViewById(R.id.edittext_stock_quota);
		mEditTextStockQuotaEst = findViewById(R.id.edittext_stock_quota_est);
		mEditTextStockTrading = findViewById(R.id.edittext_stock_trading);
		mEditTextStockTradingCost = findViewById(R.id.edittext_stock_trading_cost);
		mEditTextStockHold = findViewById(R.id.edittext_stock_hold);
		mEditTextStockYield = findViewById(R.id.edittext_stock_yield);
		mEditTextStockHoldA = findViewById(R.id.edittext_stock_hold_a);
		mEditTextStockHoldB = findViewById(R.id.edittext_stock_hold_b);
		mTextviewMonthNet = findViewById(R.id.textview_month_net);
		mTextviewWeekNet = findViewById(R.id.textview_week_net);
		mTextviewDayNet = findViewById(R.id.textview_day_net);
		mTextviewMin60Net = findViewById(R.id.textview_min60_net);
		mTextviewMin30Net = findViewById(R.id.textview_min30_net);
		mTextviewMin15Net = findViewById(R.id.textview_min15_net);
		mTextviewMin5Net = findViewById(R.id.textview_min5_net);
		mTradeLevelPickerMonth = findViewById(R.id.trade_level_picker_month);
		mTradeLevelPickerWeek = findViewById(R.id.trade_level_picker_week);
		mTradeLevelPickerDay = findViewById(R.id.trade_level_picker_day);
		mTradeLevelPickerMin60 = findViewById(R.id.trade_level_picker_min60);
		mTradeLevelPickerMin30 = findViewById(R.id.trade_level_picker_min30);
		mTradeLevelPickerMin15 = findViewById(R.id.trade_level_picker_min15);
		mTradeLevelPickerMin5 = findViewById(R.id.trade_level_picker_min5);
		mImageViewRestoreTarget = findViewById(R.id.imageview_restore_target);
		mImageViewMonthTarget = findViewById(R.id.imageview_month_target);
		mImageViewWeekTarget = findViewById(R.id.imageview_week_target);
		mImageViewDayTarget = findViewById(R.id.imageview_day_target);
		mImageViewMin60Target = findViewById(R.id.imageview_min60_target);
		mImageViewMin30Target = findViewById(R.id.imageview_min30_target);
		mImageViewMin15Target = findViewById(R.id.imageview_min15_target);
		mImageViewMin5Target = findViewById(R.id.imageview_min5_target);
		mButtonOk = findViewById(R.id.button_ok);
		mButtonCancel = findViewById(R.id.button_cancel);

		mLayoutTradeLevelPickers = findViewById(R.id.layout_trade_level_pickers);
		mLayoutMonthPicker = findViewById(R.id.layout_month_picker);
		mLayoutWeekPicker = findViewById(R.id.layout_week_picker);
		mLayoutDayPicker = findViewById(R.id.layout_day_picker);
		mLayoutMin60Picker = findViewById(R.id.layout_min60_picker);
		mLayoutMin30Picker = findViewById(R.id.layout_min30_picker);
		mLayoutMin15Picker = findViewById(R.id.layout_min15_picker);
		mLayoutMin5Picker = findViewById(R.id.layout_min5_picker);

		mLayoutStockNameCodeDisplay = findViewById(R.id.layout_stock_name_code_display);
		mLayoutStockNameCodeEdit = findViewById(R.id.layout_stock_name_code_edit);
		mTextViewDisplayStockName = findViewById(R.id.textview_display_stock_name);
		mTextViewDisplayStockCode = findViewById(R.id.textview_display_stock_code);

		mCheckBoxTrade.setOnClickListener(this);
		mEditTextStockName.setOnClickListener(this);
		mEditTextStockCode.setOnClickListener(this);
		mEditTextStockWindow.setOnClickListener(this);
		mEditTextStockQuota.setOnClickListener(this);
		mEditTextStockQuotaEst.setOnClickListener(this);
		mEditTextStockTrading.setOnClickListener(this);
		mEditTextStockTradingCost.setOnClickListener(this);
		mEditTextStockHold.setOnClickListener(this);
		mEditTextStockYield.setOnClickListener(this);
		mImageViewRestoreTarget.setOnClickListener(this);
		mImageViewMonthTarget.setOnClickListener(this);
		mImageViewWeekTarget.setOnClickListener(this);
		mImageViewDayTarget.setOnClickListener(this);
		mImageViewMin60Target.setOnClickListener(this);
		mImageViewMin30Target.setOnClickListener(this);
		mImageViewMin15Target.setOnClickListener(this);
		mImageViewMin5Target.setOnClickListener(this);
		mButtonOk.setOnClickListener(this);
		mButtonCancel.setOnClickListener(this);

		if (TextUtils.equals(mAction, Constant.ACTION_STOCK_EDIT)) {
			mLayoutStockNameCodeDisplay.setOnClickListener(this);
		}

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

		mEditTextStockQuota.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				updateQuotaEstimation();
			}
		});

		if (TextUtils.equals(mAction, Constant.ACTION_STOCK_NEW)) {
			setTitle(R.string.stock_insert);
			mStock.addFlag(Stock.FLAG_FAVORITE);
			mLayoutStockNameCodeDisplay.setVisibility(View.GONE);
			mLayoutStockNameCodeEdit.setVisibility(View.VISIBLE);
		} else if (TextUtils.equals(mAction, Constant.ACTION_STOCK_EDIT)) {
			setTitle(R.string.stock_edit);
			mEditTextStockCode.setEnabled(false);
			mStock.setId(mIntent.getLongExtra(Constant.EXTRA_STOCK_ID,
					DatabaseContract.INVALID_ID));
			mStockDatabaseManager.getStockById(mStock);
			mOrginStockWindow = mStock.getWindow();
			mHoldB = mStockDatabaseManager.getStockDealBuy(mStock, Stock.ACCOUNT_B);
			if (mHoldB > 0) {
				findViewById(R.id.layout_stock_hold_account).setVisibility(View.VISIBLE);
				mHoldA = mStock.getHold() - mHoldB;
				mEditTextStockHoldA.setText(String.valueOf(mHoldA));
				mEditTextStockHoldB.setText(String.valueOf(mHoldB));
			}
			mStockDatabaseManager.getStockTrendMap(mStock, mStock.getStockTrendMap());
			mLayoutStockNameCodeDisplay.setVisibility(View.VISIBLE);
			mLayoutStockNameCodeEdit.setVisibility(View.GONE);
			mTextViewDisplayStockName.setText(mStock.getName());
			mTextViewDisplayStockCode.setText(mStock.getCode());
		}
	}

	private void updateQuotaEstimation() {
		try {
			String quotaText = mEditTextStockQuota.getText().toString();
			if (!TextUtils.isEmpty(quotaText)) {
				long quota = Long.parseLong(quotaText);
				double price = mStock.getPrice();
				double estimatedValue = quota * price;

				mEditTextStockQuotaEst.setText(String.format(Locale.getDefault(), "%.2f", estimatedValue));
			} else {
				mEditTextStockQuotaEst.setText("");
			}
		} catch (Exception e) {
			mEditTextStockQuotaEst.setText("");
		}
	}

	private void setupTradeLevelPickers() {
		mTradeLevelPickerMonth.setMinValue(StockTrend.LEVEL_NONE);
		mTradeLevelPickerMonth.setMaxValue(StockTrend.LEVEL_TREND_LINE);
		mTradeLevelPickerMonth.setTargetValue(mStock.getTarget(Period.MONTH));

		mTradeLevelPickerWeek.setMinValue(StockTrend.LEVEL_NONE);
		mTradeLevelPickerWeek.setMaxValue(StockTrend.LEVEL_TREND_LINE);
		mTradeLevelPickerWeek.setTargetValue(mStock.getTarget(Period.WEEK));

		mTradeLevelPickerDay.setMinValue(StockTrend.LEVEL_NONE);
		mTradeLevelPickerDay.setMaxValue(StockTrend.LEVEL_TREND_LINE);
		mTradeLevelPickerDay.setTargetValue(mStock.getTarget(Period.DAY));

		mTradeLevelPickerMin60.setMinValue(StockTrend.LEVEL_NONE);
		mTradeLevelPickerMin60.setMaxValue(StockTrend.LEVEL_TREND_LINE);
		mTradeLevelPickerMin60.setTargetValue(mStock.getTarget(Period.MIN60));

		mTradeLevelPickerMin30.setMinValue(StockTrend.LEVEL_NONE);
		mTradeLevelPickerMin30.setMaxValue(StockTrend.LEVEL_TREND_LINE);
		mTradeLevelPickerMin30.setTargetValue(mStock.getTarget(Period.MIN30));

		mTradeLevelPickerMin15.setMinValue(StockTrend.LEVEL_NONE);
		mTradeLevelPickerMin15.setMaxValue(StockTrend.LEVEL_TREND_LINE);
		mTradeLevelPickerMin15.setTargetValue(mStock.getTarget(Period.MIN15));

		mTradeLevelPickerMin5.setMinValue(StockTrend.LEVEL_NONE);
		mTradeLevelPickerMin5.setMaxValue(StockTrend.LEVEL_TREND_LINE);
		mTradeLevelPickerMin5.setTargetValue(mStock.getTarget(Period.MIN5));

		setupTradeLevelPickerListener(mTradeLevelPickerMonth, Period.MONTH, mTextviewMonthNet, mImageViewMonthTarget);
		setupTradeLevelPickerListener(mTradeLevelPickerWeek, Period.WEEK, mTextviewWeekNet, mImageViewWeekTarget);
		setupTradeLevelPickerListener(mTradeLevelPickerDay, Period.DAY, mTextviewDayNet, mImageViewDayTarget);
		setupTradeLevelPickerListener(mTradeLevelPickerMin60, Period.MIN60, mTextviewMin60Net, mImageViewMin60Target);
		setupTradeLevelPickerListener(mTradeLevelPickerMin30, Period.MIN30, mTextviewMin30Net, mImageViewMin30Target);
		setupTradeLevelPickerListener(mTradeLevelPickerMin15, Period.MIN15, mTextviewMin15Net, mImageViewMin15Target);
		setupTradeLevelPickerListener(mTradeLevelPickerMin5, Period.MIN5, mTextviewMin5Net, mImageViewMin5Target);
	}

	private void setupTradeLevelPickerListener(TradeLevelPicker picker, final String period,
											   final TextView netTextView, final ImageView targetImageView) {
		if (picker == null) {
			return;
		}
		picker.setOnValueChangedListener(new android.widget.NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(android.widget.NumberPicker numberPicker, int oldVal, int newVal) {
				updateNetTextView(period, newVal, netTextView);
				updateTargetImageView(period, newVal, targetImageView);
			}
		});
	}

	private void updateNetTextView(String period, int level, TextView netTextView) {
		StockTrend stockTrend = mStock.getStockTrend(period, level);
		if (stockTrend != null) {
			setNetTextView(netTextView, stockTrend.getNextNet());
		} else {
			setNetTextView(netTextView, 0);
		}
	}

	private void updateTargetImageView(String period, int currentLevel, ImageView targetImageView) {
		int target = mStock.getTarget(period);
		if (target > StockTrend.LEVEL_NONE && target == currentLevel) {
			targetImageView.setImageResource(R.drawable.ic_crosshair_checked);
		} else {
			targetImageView.setImageResource(R.drawable.ic_crosshair_unchecked);
		}
	}

	void updateView() {
		mCheckBoxTrade.setChecked(mStock.hasFlag(Stock.FLAG_TRADE));

		mEditTextStockName.setText(mStock.getName());
		mEditTextStockCode.setText(mStock.getCode());
		mEditTextStockWindow.setText(mStock.getWindow());
		mEditTextStockQuota.setText(String.valueOf(mStock.getQuota()));
		mEditTextStockTrading.setText(String.valueOf(mStock.getTrading()));
		mEditTextStockTradingCost.setText(String.valueOf(mStock.getTradingCost()));
		mEditTextStockHold.setText(String.valueOf(mStock.getHold()));
		mEditTextStockYield.setText(String.valueOf(mStock.getYield()));

		updateQuotaEstimation();

		mTradeLevelPickerMonth.setTargetValue(mStock.getTarget(Period.MONTH));
		mTradeLevelPickerWeek.setTargetValue(mStock.getTarget(Period.WEEK));
		mTradeLevelPickerDay.setTargetValue(mStock.getTarget(Period.DAY));
		mTradeLevelPickerMin60.setTargetValue(mStock.getTarget(Period.MIN60));
		mTradeLevelPickerMin30.setTargetValue(mStock.getTarget(Period.MIN30));
		mTradeLevelPickerMin15.setTargetValue(mStock.getTarget(Period.MIN15));
		mTradeLevelPickerMin5.setTargetValue(mStock.getTarget(Period.MIN5));

		mTradeLevelPickerMonth.setValue(mStock.getAdaptive(Period.MONTH));
		mTradeLevelPickerWeek.setValue(mStock.getAdaptive(Period.WEEK));
		mTradeLevelPickerDay.setValue(mStock.getAdaptive(Period.DAY));
		mTradeLevelPickerMin60.setValue(mStock.getAdaptive(Period.MIN60));
		mTradeLevelPickerMin30.setValue(mStock.getAdaptive(Period.MIN30));
		mTradeLevelPickerMin15.setValue(mStock.getAdaptive(Period.MIN15));
		mTradeLevelPickerMin5.setValue(mStock.getAdaptive(Period.MIN5));

		mTradeLevelPickerMonth.invalidate();
		mTradeLevelPickerWeek.invalidate();
		mTradeLevelPickerDay.invalidate();
		mTradeLevelPickerMin60.invalidate();
		mTradeLevelPickerMin30.invalidate();
		mTradeLevelPickerMin15.invalidate();
		mTradeLevelPickerMin5.invalidate();

		updateNetTextView(Period.MONTH, mStock.getAdaptive(Period.MONTH), mTextviewMonthNet);
		updateNetTextView(Period.WEEK, mStock.getAdaptive(Period.WEEK), mTextviewWeekNet);
		updateNetTextView(Period.DAY, mStock.getAdaptive(Period.DAY), mTextviewDayNet);
		updateNetTextView(Period.MIN60, mStock.getAdaptive(Period.MIN60), mTextviewMin60Net);
		updateNetTextView(Period.MIN30, mStock.getAdaptive(Period.MIN30), mTextviewMin30Net);
		updateNetTextView(Period.MIN15, mStock.getAdaptive(Period.MIN15), mTextviewMin15Net);
		updateNetTextView(Period.MIN5, mStock.getAdaptive(Period.MIN5), mTextviewMin5Net);

		updateTargetImageView(Period.MONTH, mImageViewMonthTarget);
		updateTargetImageView(Period.WEEK, mImageViewWeekTarget);
		updateTargetImageView(Period.DAY, mImageViewDayTarget);
		updateTargetImageView(Period.MIN60, mImageViewMin60Target);
		updateTargetImageView(Period.MIN30, mImageViewMin30Target);
		updateTargetImageView(Period.MIN15, mImageViewMin15Target);
		updateTargetImageView(Period.MIN5, mImageViewMin5Target);

		updateLevelPickerVisibility();
		if (TextUtils.equals(mAction, Constant.ACTION_STOCK_EDIT)) {
			mTextViewDisplayStockName.setText(mStock.getName());
			mTextViewDisplayStockCode.setText(mStock.getCode());
		}
	}

	private void updateLevelPickerVisibility() {
		mLayoutMonthPicker.setVisibility(Setting.getPeriod(Period.MONTH) ? View.VISIBLE : View.GONE);
		mLayoutWeekPicker.setVisibility(Setting.getPeriod(Period.WEEK) ? View.VISIBLE : View.GONE);
		mLayoutDayPicker.setVisibility(Setting.getPeriod(Period.DAY) ? View.VISIBLE : View.GONE);
		mLayoutMin60Picker.setVisibility(Setting.getPeriod(Period.MIN60) ? View.VISIBLE : View.GONE);
		mLayoutMin30Picker.setVisibility(Setting.getPeriod(Period.MIN30) ? View.VISIBLE : View.GONE);
		mLayoutMin15Picker.setVisibility(Setting.getPeriod(Period.MIN15) ? View.VISIBLE : View.GONE);
		mLayoutMin5Picker.setVisibility(Setting.getPeriod(Period.MIN5) ? View.VISIBLE : View.GONE);

		boolean hasVisiblePeriod = Setting.getPeriod(Period.MONTH) ||
				Setting.getPeriod(Period.WEEK) ||
				Setting.getPeriod(Period.DAY) ||
				Setting.getPeriod(Period.MIN60) ||
				Setting.getPeriod(Period.MIN30) ||
				Setting.getPeriod(Period.MIN15) ||
				Setting.getPeriod(Period.MIN5);
		mLayoutTradeLevelPickers.setVisibility(hasVisiblePeriod ? View.VISIBLE : View.GONE);
	}

	void setNetTextView(TextView textView, double nextNet) {
		if (textView == null) {
			return;
		}
		textView.setText(String.valueOf(nextNet));
		textView.setTextColor(nextNet > 0 ? Color.RED : Color.BLACK);
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
			case R.id.layout_stock_name_code_display:
				if (TextUtils.equals(mAction, Constant.ACTION_STOCK_EDIT)) {
					openStockUrl();
				}
				break;

			case R.id.checkbox_trade:
				if (mCheckBoxTrade.isChecked()) {
					mStock.addFlag(Stock.FLAG_TRADE);
				} else {
					mStock.removeFlag(Stock.FLAG_TRADE);
					mStock.setBuyProfit(0);
					mStock.setSellProfit(0);
				}
				break;

			case R.id.imageview_restore_target:
				restoreToTargetValues();
				break;

			case R.id.imageview_month_target:
				toggleTargetImageView(Period.MONTH, mTradeLevelPickerMonth.getValue(), mImageViewMonthTarget);
				break;

			case R.id.imageview_week_target:
				toggleTargetImageView(Period.WEEK, mTradeLevelPickerWeek.getValue(), mImageViewWeekTarget);
				break;

			case R.id.imageview_day_target:
				toggleTargetImageView(Period.DAY, mTradeLevelPickerDay.getValue(), mImageViewDayTarget);
				break;

			case R.id.imageview_min60_target:
				toggleTargetImageView(Period.MIN60, mTradeLevelPickerMin60.getValue(), mImageViewMin60Target);
				break;

			case R.id.imageview_min30_target:
				toggleTargetImageView(Period.MIN30, mTradeLevelPickerMin30.getValue(), mImageViewMin30Target);
				break;

			case R.id.imageview_min15_target:
				toggleTargetImageView(Period.MIN15, mTradeLevelPickerMin15.getValue(), mImageViewMin15Target);
				break;

			case R.id.imageview_min5_target:
				toggleTargetImageView(Period.MIN5, mTradeLevelPickerMin5.getValue(), mImageViewMin5Target);
				break;

			case R.id.edittext_stock_window:
				showDatePicker();
				break;

			case R.id.button_ok:
				if (mCheckBoxTrade.isChecked()) {
					mStock.addFlag(Stock.FLAG_TRADE);
				} else {
					mStock.removeFlag(Stock.FLAG_TRADE);
					mStock.setBuyProfit(0);
					mStock.setSellProfit(0);
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

				mStock.setAdaptive(Period.MONTH, mTradeLevelPickerMonth.getValue());
				mStock.setAdaptive(Period.WEEK, mTradeLevelPickerWeek.getValue());
				mStock.setAdaptive(Period.DAY, mTradeLevelPickerDay.getValue());
				mStock.setAdaptive(Period.MIN60, mTradeLevelPickerMin60.getValue());
				mStock.setAdaptive(Period.MIN30, mTradeLevelPickerMin30.getValue());
				mStock.setAdaptive(Period.MIN15, mTradeLevelPickerMin15.getValue());
				mStock.setAdaptive(Period.MIN5, mTradeLevelPickerMin5.getValue());

				String stockWindow = mEditTextStockWindow.getText().toString();
				mStock.setWindow(stockWindow);
				if (!TextUtils.equals(mOrginStockWindow, stockWindow)) {
					importTDXDataFile(mStock);
				}

				String stockQuota = mEditTextStockQuota.getText().toString();
				if (!TextUtils.isEmpty(stockQuota)) {
					mStock.setQuota(Long.parseLong(stockQuota));
				}

				if (TextUtils.equals(mAction, Constant.ACTION_STOCK_NEW)) {
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
				mStockDataProvider.analyze(mStock);
				finish();
				break;

			case R.id.button_cancel:
				finish();
				break;

			default:
				break;
		}
	}

	private void openStockUrl() {
		String code = mEditTextStockCode.getText().toString();
		if (!TextUtils.isEmpty(code) && code.length() == Stock.CODE_LENGTH) {
			String url = mStock.getSeUrl();
			if (!TextUtils.isEmpty(url)) {
				try {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(url));
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
//					Toast.makeText(mContext, R.string.url_open_failed, Toast.LENGTH_SHORT).show();
				}
			} else {
//				Toast.makeText(mContext, R.string.url_empty, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void toggleTargetImageView(String period, int target, ImageView imageView) {
		TradeLevelPicker picker = getTradeLevelPickerByPeriod(period);

		if (isCrosshairIconShow(imageView)) {
			imageView.setImageResource(R.drawable.ic_crosshair_unchecked);
			mStock.setTarget(period, StockTrend.LEVEL_NONE);
			if (picker != null) {
				picker.setTargetValue(StockTrend.LEVEL_NONE);
			}
		} else {
			imageView.setImageResource(R.drawable.ic_crosshair_checked);
			mStock.setTarget(period, target);
			if (picker != null) {
				picker.setTargetValue(target);
			}
		}
	}

	private TradeLevelPicker getTradeLevelPickerByPeriod(String period) {
		switch (period) {
			case Period.MONTH:
				return mTradeLevelPickerMonth;
			case Period.WEEK:
				return mTradeLevelPickerWeek;
			case Period.DAY:
				return mTradeLevelPickerDay;
			case Period.MIN60:
				return mTradeLevelPickerMin60;
			case Period.MIN30:
				return mTradeLevelPickerMin30;
			case Period.MIN15:
				return mTradeLevelPickerMin15;
			case Period.MIN5:
				return mTradeLevelPickerMin5;
			default:
				return null;
		}
	}

	private boolean isCrosshairIconShow(ImageView imageView) {
		try {
			return imageView.getDrawable().getConstantState().equals(
					getResources().getDrawable(R.drawable.ic_crosshair_checked).getConstantState());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void updateTargetImageView(String period, ImageView imageView) {
		int adaptive = mStock.getAdaptive(period);
		int target = mStock.getTarget(period);
		if (target > StockTrend.LEVEL_NONE && target == adaptive) {
			imageView.setImageResource(R.drawable.ic_crosshair_checked);
		} else {
			imageView.setImageResource(R.drawable.ic_crosshair_unchecked);
		}
	}

	private void restoreToTargetValues() {
		restorePeriodToTarget(Period.MONTH, mTradeLevelPickerMonth, mTextviewMonthNet, mImageViewMonthTarget);
		restorePeriodToTarget(Period.WEEK, mTradeLevelPickerWeek, mTextviewWeekNet, mImageViewWeekTarget);
		restorePeriodToTarget(Period.DAY, mTradeLevelPickerDay, mTextviewDayNet, mImageViewDayTarget);
		restorePeriodToTarget(Period.MIN60, mTradeLevelPickerMin60, mTextviewMin60Net, mImageViewMin60Target);
		restorePeriodToTarget(Period.MIN30, mTradeLevelPickerMin30, mTextviewMin30Net, mImageViewMin30Target);
		restorePeriodToTarget(Period.MIN15, mTradeLevelPickerMin15, mTextviewMin15Net, mImageViewMin15Target);
		restorePeriodToTarget(Period.MIN5, mTradeLevelPickerMin5, mTextviewMin5Net, mImageViewMin5Target);
	}

	private void restorePeriodToTarget(String period, TradeLevelPicker picker, TextView netTextView, ImageView targetImageView) {
		int target = mStock.getTarget(period);
		if (target > StockTrend.LEVEL_NONE) {
			picker.setValue(target);
			updateNetTextView(period, target, netTextView);
			updateTargetImageView(period, target, targetImageView);
		}
	}

	private void showDatePicker() {
		Calendar calendar = Calendar.getInstance();

		String currentDateStr = mEditTextStockWindow.getText().toString();
		if (!TextUtils.isEmpty(currentDateStr)) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(Utility.CALENDAR_DATE_FORMAT, Locale.getDefault());
				Date currentDate = sdf.parse(currentDateStr);
				if (currentDate != null) {
					calendar.setTime(currentDate);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		DatePickerDialog datePickerDialog = new DatePickerDialog(
				this,
				(view, year, month, dayOfMonth) -> {
					Calendar selectedDate = Calendar.getInstance();
					selectedDate.set(year, month, dayOfMonth);
					SimpleDateFormat sdf = new SimpleDateFormat(Utility.CALENDAR_DATE_FORMAT, Locale.getDefault());
					mEditTextStockWindow.setText(sdf.format(selectedDate.getTime()));
				},
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH));

		datePickerDialog.show();
	}
}