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
	long mHoldC = 0;
	double mDividendA = 0.0;
	double mDividendB = 0.0;
	double mDividendC = 0.0;
	CheckBox mCheckBoxTrade;
	EditText mEditTextStockName;
	EditText mEditTextStockCode;
	EditText mEditTextStockQuota;
	EditText mEditTextStockTee;
	TextView mTextViewStockHoldLabel;
	TextView mTextViewStockHoldValue;
	TextView mTextViewStockYieldLabel;
	TextView mTextViewStockYieldValue;
	TextView mTextViewStockBonusLabel;
	TextView mTextViewStockBonusValue;
	TextView mTextViewStockAccountALabel;
	TextView mTextViewStockAccountAValue;
	TextView mTextViewStockDividendALabel;
	TextView mTextViewStockDividendAValue;
	TextView mTextViewStockAccountBLabel;
	TextView mTextViewStockAccountBValue;
	TextView mTextViewStockDividendBLabel;
	TextView mTextViewStockDividendBValue;
	TextView mTextViewStockAccountCLabel;
	TextView mTextViewStockAccountCValue;
	TextView mTextViewStockDividendCLabel;
	TextView mTextViewStockDividendCValue;
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

	// 配额加减按钮
	Button mButtonQuotaMinus;
	Button mButtonQuotaPlus;

	// 配额提示文字
	TextView mTextViewQuotaHint;

	// 持仓一致性检查提示文字
	TextView mTextViewHoldConsistencyHint;

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

	// 用于临时存储配额的变量
	private boolean mIsQuotaEditable = false;

	// TextWatcher引用
	private TextWatcher mQuotaTextWatcher;

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
		mEditTextStockQuota = findViewById(R.id.edittext_stock_quota);
		mEditTextStockTee = findViewById(R.id.edittext_stock_tee);
		mTextViewStockHoldLabel = findViewById(R.id.textview_stock_hold_label);
		mTextViewStockHoldValue = findViewById(R.id.textview_stock_hold_value);
		mTextViewStockYieldLabel = findViewById(R.id.textview_stock_yield_label);
		mTextViewStockYieldValue = findViewById(R.id.textview_stock_yield_value);
		mTextViewStockBonusLabel = findViewById(R.id.textview_stock_bonus_label);
		mTextViewStockBonusValue = findViewById(R.id.textview_stock_bonus_value);
		mTextViewStockAccountALabel = findViewById(R.id.textview_stock_account_a_label);
		mTextViewStockAccountAValue = findViewById(R.id.textview_stock_account_a_value);
		mTextViewStockDividendALabel = findViewById(R.id.textview_stock_dividend_a_label);
		mTextViewStockDividendAValue = findViewById(R.id.textview_stock_dividend_a_value);
		mTextViewStockAccountBLabel = findViewById(R.id.textview_stock_account_b_label);
		mTextViewStockAccountBValue = findViewById(R.id.textview_stock_account_b_value);
		mTextViewStockDividendBLabel = findViewById(R.id.textview_stock_dividend_b_label);
		mTextViewStockDividendBValue = findViewById(R.id.textview_stock_dividend_b_value);
		mTextViewStockAccountCLabel = findViewById(R.id.textview_stock_account_c_label);
		mTextViewStockAccountCValue = findViewById(R.id.textview_stock_account_c_value);
		mTextViewStockDividendCLabel = findViewById(R.id.textview_stock_dividend_c_label);
		mTextViewStockDividendCValue = findViewById(R.id.textview_stock_dividend_c_value);
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

		// 初始化配额加减按钮
		mButtonQuotaMinus = findViewById(R.id.button_quota_minus);
		mButtonQuotaPlus = findViewById(R.id.button_quota_plus);

		// 初始化配额提示TextView
		mTextViewQuotaHint = findViewById(R.id.textview_quota_hint);

		// 初始化持仓一致性检查提示TextView
		mTextViewHoldConsistencyHint = findViewById(R.id.textview_hold_consistency_hint);

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
		mEditTextStockQuota.setOnClickListener(this);
		mEditTextStockTee.setOnClickListener(this);
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

		// 设置配额加减按钮的点击监听
		mButtonQuotaMinus.setOnClickListener(this);
		mButtonQuotaPlus.setOnClickListener(this);

		// 创建并添加配额EditText的文本变化监听器
		mQuotaTextWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				// 检查输入是否为100的整数倍
				validateQuotaInput(s);
			}
		};

		mEditTextStockQuota.addTextChangedListener(mQuotaTextWatcher);

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

		// 添加Trade复选框的监听器来控制Quota的编辑状态
		mCheckBoxTrade.setOnCheckedChangeListener((buttonView, isChecked) -> {
			updateQuotaEditState(isChecked);
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
			mHoldA = mStockDatabaseManager.getStockDealBuy(mStock, Stock.ACCOUNT_A);
			mHoldB = mStockDatabaseManager.getStockDealBuy(mStock, Stock.ACCOUNT_B);
			mHoldC = mStockDatabaseManager.getStockDealBuy(mStock, Stock.ACCOUNT_C);

			// 检查持仓一致性
			checkHoldConsistency();

			if (mHoldB > 0 || mHoldC > 0) {
				findViewById(R.id.layout_stock_hold_account).setVisibility(View.VISIBLE);
			}
			calculateDividends();
			updateAccountViews();

			mStockDatabaseManager.getStockTrendMap(mStock, mStock.getStockTrendMap());
			mLayoutStockNameCodeDisplay.setVisibility(View.VISIBLE);
			mLayoutStockNameCodeEdit.setVisibility(View.GONE);
			mTextViewDisplayStockName.setText(mStock.getName());
			mTextViewDisplayStockCode.setText(mStock.getCode());
		}
	}

	/**
	 * 检查持仓数量与分账户总和是否一致
	 */
	private void checkHoldConsistency() {
		long totalAccountHold = mHoldA + mHoldB + mHoldC;
		long mainHold = mStock.getHold();

		// 计算差值
		long diff = Math.abs(mainHold - totalAccountHold);

		if (diff != 0) {
			// 显示提示
			mTextViewHoldConsistencyHint.setVisibility(View.VISIBLE);

			// 计算差值百分比
			double diffPercentage = 0;
			if (mainHold > 0) {
				diffPercentage = (double) diff / mainHold * 100;
			}

			// 设置提示文字
			if (mainHold > totalAccountHold) {
				// 主持仓大于分账户总和
				mTextViewHoldConsistencyHint.setText(String.format(Locale.getDefault(),
						"持仓不一致: 主持仓(%d) > 分账户总和(%d) 差值: %d (%.2f%%)",
						mainHold, totalAccountHold, diff, diffPercentage));
			} else if (mainHold < totalAccountHold) {
				// 主持仓小于分账户总和
				mTextViewHoldConsistencyHint.setText(String.format(Locale.getDefault(),
						"持仓不一致: 主持仓(%d) < 分账户总和(%d) 差值: %d (%.2f%%)",
						mainHold, totalAccountHold, diff, diffPercentage));
			}
		} else {
			// 隐藏提示
			mTextViewHoldConsistencyHint.setVisibility(View.GONE);
		}
	}

	/**
	 * 检查配额输入是否为100的整数倍
	 */
	private void validateQuotaInput(Editable s) {
		if (TextUtils.isEmpty(s)) {
			// 隐藏提示
			mTextViewQuotaHint.setVisibility(View.GONE);
			return;
		}

		try {
			String input = s.toString();
			long value = Long.parseLong(input);

			// 检查是否为100的整数倍
			if (value % 100 != 0) {
				// 显示提示文字，并提示最接近的100的整数倍
				long nearestMultiple = Math.round(value / 100.0) * 100;
				if (nearestMultiple < 0) nearestMultiple = 0;

				mTextViewQuotaHint.setVisibility(View.VISIBLE);
				mTextViewQuotaHint.setText(String.format(Locale.getDefault(),
						"配额必须是100的整数倍 (建议: %d)", nearestMultiple));
			} else {
				// 隐藏提示文字
				mTextViewQuotaHint.setVisibility(View.GONE);
			}
		} catch (NumberFormatException e) {
			// 如果不是数字，显示错误提示
			mTextViewQuotaHint.setVisibility(View.VISIBLE);
			mTextViewQuotaHint.setText("请输入有效的数字");
		}
	}

	/**
	 * 根据Trade复选框状态更新Quota的可编辑状态
	 */
	private void updateQuotaEditState(boolean isTradeChecked) {
		mIsQuotaEditable = isTradeChecked;

		// 更新配额EditText和加减按钮的状态
		mEditTextStockQuota.setEnabled(isTradeChecked);
		mButtonQuotaMinus.setEnabled(isTradeChecked);
		mButtonQuotaPlus.setEnabled(isTradeChecked);

		// 更新Quota的显示样式
		if (isTradeChecked) {
			// Trade勾选时，显示正常颜色和背景
			mEditTextStockQuota.setTextColor(Color.BLACK);
			mEditTextStockQuota.setBackgroundResource(android.R.drawable.editbox_background_normal);

			// 检查当前配额值是否符合要求
			try {
				String quotaText = mEditTextStockQuota.getText().toString();
				if (!TextUtils.isEmpty(quotaText)) {
					long quota = Long.parseLong(quotaText);
					if (quota % 100 != 0) {
						mTextViewQuotaHint.setVisibility(View.VISIBLE);
						mTextViewQuotaHint.setText(String.format(Locale.getDefault(),
								"配额必须是100的整数倍 (建议: %d)", Math.round(quota / 100.0) * 100));
					} else {
						mTextViewQuotaHint.setVisibility(View.GONE);
					}
				}
			} catch (Exception e) {
				mTextViewQuotaHint.setVisibility(View.GONE);
			}
		} else {
			// Trade未勾选时，显示灰色表示不可编辑
			mEditTextStockQuota.setTextColor(Color.GRAY);
			mEditTextStockQuota.setBackgroundResource(android.R.drawable.editbox_background);

			// 隐藏提示文字
			mTextViewQuotaHint.setVisibility(View.GONE);

			// 如果Trade未勾选，确保配额为0
			mEditTextStockQuota.removeTextChangedListener(mQuotaTextWatcher);
			mEditTextStockQuota.setText("0");
			mEditTextStockQuota.addTextChangedListener(mQuotaTextWatcher);
		}
	}

	private void calculateDividends() {
		double dividendInYear = mStock.getDividendInYear() / 10.0;

		mDividendA = mHoldA * dividendInYear;
		mDividendB = mHoldB * dividendInYear;
		mDividendC = mHoldC * dividendInYear;

		// 更新分红显示
		updateDividendViews();
	}

	/**
	 * 更新分红显示
	 */
	private void updateDividendViews() {
		String dividendFormat = "%.2f";
		mTextViewStockDividendAValue.setText(String.format(Locale.getDefault(), dividendFormat, mDividendA));
		mTextViewStockDividendBValue.setText(String.format(Locale.getDefault(), dividendFormat, mDividendB));
		mTextViewStockDividendCValue.setText(String.format(Locale.getDefault(), dividendFormat, mDividendC));

		if (mDividendA > 0) {
			mTextViewStockDividendAValue.setTextColor(Color.RED);
		} else {
			mTextViewStockDividendAValue.setTextColor(Color.BLACK);
		}

		if (mDividendB > 0) {
			mTextViewStockDividendBValue.setTextColor(Color.RED);
		} else {
			mTextViewStockDividendBValue.setTextColor(Color.BLACK);
		}

		if (mDividendC > 0) {
			mTextViewStockDividendCValue.setTextColor(Color.RED);
		} else {
			mTextViewStockDividendCValue.setTextColor(Color.BLACK);
		}
	}

	private void updateAccountViews() {
		mTextViewStockAccountAValue.setText(String.valueOf(mHoldA));
		mTextViewStockAccountBValue.setText(String.valueOf(mHoldB));
		mTextViewStockAccountCValue.setText(String.valueOf(mHoldC));

		// 更新分红显示
		updateDividendViews();
	}

	/**
	 * 增加配额值（每次增加100）
	 */
	private void increaseQuota() {
		try {
			String currentText = mEditTextStockQuota.getText().toString();
			long currentValue = TextUtils.isEmpty(currentText) ? 0 : Long.parseLong(currentText);
			long newValue = currentValue + 100;

			// 确保不会出现负数（虽然增加不会，但为了安全）
			if (newValue < 0) {
				newValue = 0;
			}

			mEditTextStockQuota.removeTextChangedListener(mQuotaTextWatcher);
			mEditTextStockQuota.setText(String.valueOf(newValue));
			mEditTextStockQuota.addTextChangedListener(mQuotaTextWatcher);

			// 由于是增加100，肯定是100的整数倍，隐藏提示
			mTextViewQuotaHint.setVisibility(View.GONE);
		} catch (Exception e) {
			e.printStackTrace();
			// 如果解析出错，重置为0
			mEditTextStockQuota.removeTextChangedListener(mQuotaTextWatcher);
			mEditTextStockQuota.setText("0");
			mEditTextStockQuota.addTextChangedListener(mQuotaTextWatcher);
			mTextViewQuotaHint.setVisibility(View.GONE);
		}
	}

	/**
	 * 减少配额值（每次减少100）
	 */
	private void decreaseQuota() {
		try {
			String currentText = mEditTextStockQuota.getText().toString();
			long currentValue = TextUtils.isEmpty(currentText) ? 0 : Long.parseLong(currentText);
			long newValue = currentValue - 100;

			// 确保不会出现负数
			if (newValue < 0) {
				newValue = 0;
			}

			mEditTextStockQuota.removeTextChangedListener(mQuotaTextWatcher);
			mEditTextStockQuota.setText(String.valueOf(newValue));
			mEditTextStockQuota.addTextChangedListener(mQuotaTextWatcher);

			// 由于是减少100，如果原始值是100的整数倍，结果也是100的整数倍
			// 检查新的值是否是100的整数倍
			if (newValue % 100 != 0) {
				mTextViewQuotaHint.setVisibility(View.VISIBLE);
				mTextViewQuotaHint.setText(String.format(Locale.getDefault(),
						"配额必须是100的整数倍 (建议: %d)", Math.round(newValue / 100.0) * 100));
			} else {
				mTextViewQuotaHint.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 如果解析出错，重置为0
			mEditTextStockQuota.removeTextChangedListener(mQuotaTextWatcher);
			mEditTextStockQuota.setText("0");
			mEditTextStockQuota.addTextChangedListener(mQuotaTextWatcher);
			mTextViewQuotaHint.setVisibility(View.GONE);
		}
	}

	/**
	 * 确保配额值是100的整数倍
	 */
	private long ensureQuotaMultipleOf100(long value) {
		// 如果已经是100的整数倍，直接返回
		if (value % 100 == 0) {
			return value;
		}

		// 如果不是100的整数倍，调整为最接近的100的整数倍
		return Math.round(value / 100.0) * 100;
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
		boolean isTradeChecked = mStock.hasFlag(Stock.FLAG_TRADE);
		mCheckBoxTrade.setChecked(isTradeChecked);

		// 根据Trade状态更新Quota的显示状态
		updateQuotaEditState(isTradeChecked);

		mEditTextStockName.setText(mStock.getName());
		mEditTextStockCode.setText(mStock.getCode());

		// 设置配额的值（确保是100的整数倍）
		long quotaValue = mStock.getQuota();
		quotaValue = ensureQuotaMultipleOf100(quotaValue);
		mEditTextStockQuota.removeTextChangedListener(mQuotaTextWatcher);
		mEditTextStockQuota.setText(String.valueOf(quotaValue));
		mEditTextStockQuota.addTextChangedListener(mQuotaTextWatcher);

		mEditTextStockTee.setText(String.format(Locale.getDefault(), "%.2f", mStock.getTee()));

		mTextViewStockHoldValue.setText(String.valueOf(mStock.getHold()));

		String yieldValue = String.format(Locale.getDefault(), "%.2f%%", mStock.getYield());
		mTextViewStockYieldValue.setText(yieldValue);
		if (mStock.getYield() > 0) {
			mTextViewStockYieldValue.setTextColor(Color.RED);
		} else {
			mTextViewStockYieldValue.setTextColor(Color.BLACK);
		}

		String bonusValue = String.format(Locale.getDefault(), "%.2f", mStock.getBonus());
		mTextViewStockBonusValue.setText(bonusValue);
		if (mStock.getBonus() > 0) {
			mTextViewStockBonusValue.setTextColor(Color.RED);
		} else {
			mTextViewStockBonusValue.setTextColor(Color.BLACK);
		}

		// 检查持仓一致性（在编辑模式下）
		if (TextUtils.equals(mAction, Constant.ACTION_STOCK_EDIT)) {
			checkHoldConsistency();
		}

		// 更新账户和分红显示
		if (mHoldA > 0 || mHoldB > 0 || mHoldC > 0) {
			calculateDividends();
			updateAccountViews();
		}

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
				// Trade复选框状态改变会通过OnCheckedChangeListener处理
				break;

			case R.id.button_quota_minus:
				// 点击减号按钮，减少配额100
				if (mIsQuotaEditable) {
					decreaseQuota();
				}
				break;

			case R.id.button_quota_plus:
				// 点击加号按钮，增加配额100
				if (mIsQuotaEditable) {
					increaseQuota();
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

			case R.id.button_ok:
				// 如果是编辑模式，检查持仓一致性
				if (TextUtils.equals(mAction, Constant.ACTION_STOCK_EDIT)) {
					long totalAccountHold = mHoldA + mHoldB + mHoldC;
					long mainHold = mStock.getHold();

					if (totalAccountHold != mainHold) {
						// 显示提示但不阻止保存
						mTextViewHoldConsistencyHint.setVisibility(View.VISIBLE);
						long diff = Math.abs(mainHold - totalAccountHold);
						double diffPercentage = mainHold > 0 ? (double) diff / mainHold * 100 : 0;

						mTextViewHoldConsistencyHint.setText(String.format(Locale.getDefault(),
								"持仓不一致: 主持仓(%d) ≠ 分账户总和(%d) 差值: %d (%.2f%%)",
								mainHold, totalAccountHold, diff, diffPercentage));

						// 可以添加一个Toast提示用户检查
						Toast.makeText(this, "请注意：持仓数量与分账户总和不一致", Toast.LENGTH_SHORT).show();
					}
				}

				if (mCheckBoxTrade.isChecked()) {
					mStock.addFlag(Stock.FLAG_TRADE);

					// 保存配额值（确保是100的整数倍）
					try {
						String quotaText = mEditTextStockQuota.getText().toString();
						if (!TextUtils.isEmpty(quotaText)) {
							long quota = Long.parseLong(quotaText);

							// 检查是否为100的整数倍
							if (quota % 100 != 0) {
								// 显示提示并返回，不保存
								mTextViewQuotaHint.setVisibility(View.VISIBLE);
								mTextViewQuotaHint.setText(String.format(Locale.getDefault(),
										"配额必须是100的整数倍 (建议: %d)", Math.round(quota / 100.0) * 100));
								Toast.makeText(this, "请修正配额值", Toast.LENGTH_SHORT).show();
								return; // 不保存，返回
							}

							quota = ensureQuotaMultipleOf100(quota);
							mStock.setQuota(quota);
						} else {
							mStock.setQuota(0);
						}
					} catch (Exception e) {
						mStock.setQuota(0);
					}
				} else {
					mStock.removeFlag(Stock.FLAG_TRADE);
					mStock.setBuyProfit(0);
					mStock.setSellProfit(0);
					// Trade未勾选时，配额设置为0
					mStock.setQuota(0);
				}

				try {
					String teeText = mEditTextStockTee.getText().toString();
					if (!TextUtils.isEmpty(teeText)) {
						double tee = Double.parseDouble(teeText);
						mStock.setTee(tee);
					} else {
						mStock.setTee(0.0);
					}
				} catch (Exception e) {
					mStock.setTee(0.0);
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

				setAdaptiveIfChanged(Period.MONTH, mTradeLevelPickerMonth.getValue());
				setAdaptiveIfChanged(Period.WEEK, mTradeLevelPickerWeek.getValue());
				setAdaptiveIfChanged(Period.DAY, mTradeLevelPickerDay.getValue());
				setAdaptiveIfChanged(Period.MIN60, mTradeLevelPickerMin60.getValue());
				setAdaptiveIfChanged(Period.MIN30, mTradeLevelPickerMin30.getValue());
				setAdaptiveIfChanged(Period.MIN15, mTradeLevelPickerMin15.getValue());
				setAdaptiveIfChanged(Period.MIN5, mTradeLevelPickerMin5.getValue());

				if (TextUtils.equals(mAction, Constant.ACTION_STOCK_NEW)) {
					if (!mStockDatabaseManager.isStockExist(mStock)) {
						mStock.setCreated(Utility.getCurrentDateTimeString());
						Uri uri = mStockDatabaseManager.insertStock(mStock);
						mStockDatabaseManager.getStock(uri, mStock);
						mStockDatabaseManager.updateStock(mStock, mStock.getContentValuesEdit());
						Setting.setDownloadStockTimeMillis(mStock, 0);
						Setting.setDownloadStockDataTimeMillis(mStock, 0);
						mStockDataProvider.download(mStock);
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
					mStockDataProvider.analyze(mStock);
				}
				getIntent().putExtra(Constant.EXTRA_STOCK_ID, mStock.getId());
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
				}
			}
		}
	}

	private void toggleTargetImageView(String period, int target, ImageView imageView) {
		TradeLevelPicker picker = getTradeLevelPickerByPeriod(period);

		if (isCrosshairIconShow(imageView)) {
			imageView.setImageResource(R.drawable.ic_crosshair_unchecked);
			mStock.setTarget(period, StockTrend.LEVEL_NONE);
			mStock.setAdaptive(period, StockTrend.LEVEL_NONE);
			if (picker != null) {
				picker.setTargetValue(StockTrend.LEVEL_NONE);
			}
		} else {
			imageView.setImageResource(R.drawable.ic_crosshair_checked);
			mStock.setTarget(period, target);
			int adaptive = target - 1;
			if (adaptive < StockTrend.LEVEL_NONE) {
				adaptive = StockTrend.LEVEL_NONE;
			}
			mStock.setAdaptive(period, adaptive);
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
			mStock.setAdaptive(period, target - 1);
			picker.setValue(target);
			updateNetTextView(period, target, netTextView);
			updateTargetImageView(period, target, targetImageView);
		}
	}

	void setAdaptiveIfChanged(String period, int value) {
		if (value != mStock.getTarget(period) && value != mStock.getAdaptive(period)) {
			mStock.setAdaptive(period, value);
		}
	}
}