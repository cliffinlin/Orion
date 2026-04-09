package com.android.orion.activity;

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
import com.android.orion.data.Period;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockTrend;
import com.android.orion.constant.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Utility;
import com.android.orion.widget.TargetLevelPicker;

import java.util.Locale;

public class StockActivity extends StorageActivity implements OnClickListener {

	long mHoldA = 0;
	long mHoldB = 0;
	long mHoldC = 0;
	double mDividendA = 0.0;
	double mDividendB = 0.0;
	double mDividendC = 0.0;
	CheckBox mCheckBoxTarget;
	CheckBox mCheckBoxShort;
	CheckBox mCheckBoxLong;
	EditText mEditTextStockName;
	EditText mEditTextStockCode;
	EditText mEditTextStockLocked;
	EditText mEditTextStockQuota;
	EditText mEditTextStockTrading;
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
	TextView mTextViewMonthNet;
	TextView mTextViewWeekNet;
	TextView mTextViewDayNet;
	TextView mTextViewMin60Net;
	TextView mTextViewMin30Net;
	TextView mTextViewMin15Net;
	TextView mTextViewMin5Net;
	TargetLevelPicker mTargetLevelPickerMonth;
	TargetLevelPicker mTargetLevelPickerWeek;
	TargetLevelPicker mTargetLevelPickerDay;
	TargetLevelPicker mTargetLevelPickerMin60;
	TargetLevelPicker mTargetLevelPickerMin30;
	TargetLevelPicker mTargetLevelPickerMin15;
	TargetLevelPicker mTargetLevelPickerMin5;
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

	TextView mTextViewLockedHint;
	TextView mTextViewQuotaHint;
	TextView mTextViewTradingHint;
	TextView mTextViewHoldConsistencyHint;

	LinearLayout mLayoutTargetLevelPickers;
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
	private TextWatcher mLockedTextWatcher;
	private TextWatcher mQuotaTextWatcher;
	private TextWatcher mTradingTextWatcher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock);
		if (mStock == null) {
			mStock = new Stock();
		}
		initView();
		setupTargetLevelPickers();
		updateView();
	}

	void initView() {
		mCheckBoxTarget = findViewById(R.id.checkbox_target);
		mCheckBoxShort = findViewById(R.id.checkbox_short);
		mCheckBoxLong = findViewById(R.id.checkbox_long);
		mEditTextStockName = findViewById(R.id.edittext_stock_name);
		mEditTextStockCode = findViewById(R.id.edittext_stock_code);
		mEditTextStockLocked = findViewById(R.id.edittext_stock_locked);
		mEditTextStockQuota = findViewById(R.id.edittext_stock_quota);
		mEditTextStockTrading = findViewById(R.id.edittext_stock_trading);
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
		mTextViewMonthNet = findViewById(R.id.textview_month_net);
		mTextViewWeekNet = findViewById(R.id.textview_week_net);
		mTextViewDayNet = findViewById(R.id.textview_day_net);
		mTextViewMin60Net = findViewById(R.id.textview_min60_net);
		mTextViewMin30Net = findViewById(R.id.textview_min30_net);
		mTextViewMin15Net = findViewById(R.id.textview_min15_net);
		mTextViewMin5Net = findViewById(R.id.textview_min5_net);
		mTargetLevelPickerMonth = findViewById(R.id.target_level_picker_month);
		mTargetLevelPickerWeek = findViewById(R.id.target_level_picker_week);
		mTargetLevelPickerDay = findViewById(R.id.target_level_picker_day);
		mTargetLevelPickerMin60 = findViewById(R.id.target_level_picker_min60);
		mTargetLevelPickerMin30 = findViewById(R.id.target_level_picker_min30);
		mTargetLevelPickerMin15 = findViewById(R.id.target_level_picker_min15);
		mTargetLevelPickerMin5 = findViewById(R.id.target_level_picker_min5);
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

		mTextViewLockedHint = findViewById(R.id.textview_locked_hint);
		mTextViewQuotaHint = findViewById(R.id.textview_quota_hint);
		mTextViewTradingHint = findViewById(R.id.textview_trading_hint);

		mTextViewHoldConsistencyHint = findViewById(R.id.textview_hold_consistency_hint);

		mLayoutTargetLevelPickers = findViewById(R.id.layout_target_level_pickers);
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

		mCheckBoxTarget.setOnClickListener(this);
		mCheckBoxShort.setOnClickListener(this);
		mCheckBoxLong.setOnClickListener(this);
		mEditTextStockName.setOnClickListener(this);
		mEditTextStockCode.setOnClickListener(this);
		mEditTextStockLocked.setOnClickListener(this);
		mEditTextStockQuota.setOnClickListener(this);
		mEditTextStockTrading.setOnClickListener(this);
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

		mCheckBoxTarget.setOnCheckedChangeListener((buttonView, isChecked) -> {
			updateQuotaEditState(isChecked);
		});

		mCheckBoxShort.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (isChecked) {
				mStock.addFlag(Stock.FLAG_SHORT);
			} else {
				mStock.removeFlag(Stock.FLAG_SHORT);
			}
		});

		mCheckBoxLong.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (isChecked) {
				mStock.addFlag(Stock.FLAG_LONG);
			} else {
				mStock.removeFlag(Stock.FLAG_LONG);
			}
		});

		// 创建并添加Locked EditText的文本变化监听器
		mLockedTextWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				// 检查输入是否为100的整数倍
				validateLockedInput(s);
			}
		};

		mEditTextStockLocked.addTextChangedListener(mLockedTextWatcher);

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

		// 创建并添加Trading EditText的文本变化监听器
		mTradingTextWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				// 检查输入是否为100的整数倍
				validateTradingInput(s);
			}
		};

		mEditTextStockTrading.addTextChangedListener(mTradingTextWatcher);

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
	 * 检查Locked输入是否为100的整数倍
	 */
	private void validateLockedInput(Editable s) {
		if (TextUtils.isEmpty(s)) {
			// 隐藏提示
			mTextViewLockedHint.setVisibility(View.GONE);
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

				mTextViewLockedHint.setVisibility(View.VISIBLE);
				mTextViewLockedHint.setText(String.format(Locale.getDefault(),
						"Locked必须是100的整数倍 (建议: %d)", nearestMultiple));
			} else {
				// 隐藏提示文字
				mTextViewLockedHint.setVisibility(View.GONE);
			}
		} catch (NumberFormatException e) {
			// 如果不是数字，显示错误提示
			mTextViewLockedHint.setVisibility(View.VISIBLE);
			mTextViewLockedHint.setText("请输入有效的数字");
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
	 * 检查Trading输入是否为100的整数倍
	 */
	private void validateTradingInput(Editable s) {
		if (TextUtils.isEmpty(s)) {
			// 隐藏提示
			mTextViewTradingHint.setVisibility(View.GONE);
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

				mTextViewTradingHint.setVisibility(View.VISIBLE);
				mTextViewTradingHint.setText(String.format(Locale.getDefault(),
						"Trading必须是100的整数倍 (建议: %d)", nearestMultiple));
			} else {
				// 隐藏提示文字
				mTextViewTradingHint.setVisibility(View.GONE);
			}
		} catch (NumberFormatException e) {
			// 如果不是数字，显示错误提示
			mTextViewTradingHint.setVisibility(View.VISIBLE);
			mTextViewTradingHint.setText("请输入有效的数字");
		}
	}

	/**
	 * 根据Target复选框状态更新Quota的可编辑状态
	 */
	private void updateQuotaEditState(boolean isTargetChecked) {
		mIsQuotaEditable = isTargetChecked;

		mEditTextStockLocked.setEnabled(isTargetChecked);
		mEditTextStockQuota.setEnabled(isTargetChecked);
		mEditTextStockTrading.setEnabled(isTargetChecked);

		// 更新显示样式
		if (isTargetChecked) {
			// Target勾选时，显示正常颜色和背景
			mEditTextStockLocked.setTextColor(Color.BLACK);
			mEditTextStockLocked.setBackgroundResource(android.R.drawable.editbox_background_normal);

			mEditTextStockQuota.setTextColor(Color.BLACK);
			mEditTextStockQuota.setBackgroundResource(android.R.drawable.editbox_background_normal);

			mEditTextStockTrading.setTextColor(Color.BLACK);
			mEditTextStockTrading.setBackgroundResource(android.R.drawable.editbox_background_normal);

			// 检查当前Locked值是否符合要求
			try {
				String lockedText = mEditTextStockLocked.getText().toString();
				if (!TextUtils.isEmpty(lockedText)) {
					long locked = Long.parseLong(lockedText);
					if (locked % 100 != 0) {
						mTextViewLockedHint.setVisibility(View.VISIBLE);
						mTextViewLockedHint.setText(String.format(Locale.getDefault(),
								"Locked必须是100的整数倍 (建议: %d)", Math.round(locked / 100.0) * 100));
					} else {
						mTextViewLockedHint.setVisibility(View.GONE);
					}
				}
			} catch (Exception e) {
				mTextViewLockedHint.setVisibility(View.GONE);
			}

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

				// 检查当前Trading值是否符合要求
				String tradingText = mEditTextStockTrading.getText().toString();
				if (!TextUtils.isEmpty(tradingText)) {
					long trading = Long.parseLong(tradingText);
					if (trading % 100 != 0) {
						mTextViewTradingHint.setVisibility(View.VISIBLE);
						mTextViewTradingHint.setText(String.format(Locale.getDefault(),
								"Trading必须是100的整数倍 (建议: %d)", Math.round(trading / 100.0) * 100));
					} else {
						mTextViewTradingHint.setVisibility(View.GONE);
					}
				}
			} catch (Exception e) {
				mTextViewQuotaHint.setVisibility(View.GONE);
				mTextViewTradingHint.setVisibility(View.GONE);
			}
		} else {
			// Target未勾选时，显示灰色表示不可编辑
			mEditTextStockLocked.setTextColor(Color.GRAY);
			mEditTextStockLocked.setBackgroundResource(android.R.drawable.editbox_background);

			mEditTextStockQuota.setTextColor(Color.GRAY);
			mEditTextStockQuota.setBackgroundResource(android.R.drawable.editbox_background);

			mEditTextStockTrading.setTextColor(Color.GRAY);
			mEditTextStockTrading.setBackgroundResource(android.R.drawable.editbox_background);

			// 隐藏提示文字
			mTextViewLockedHint.setVisibility(View.GONE);
			mTextViewQuotaHint.setVisibility(View.GONE);
			mTextViewTradingHint.setVisibility(View.GONE);

			mEditTextStockLocked.removeTextChangedListener(mLockedTextWatcher);
			mEditTextStockLocked.addTextChangedListener(mLockedTextWatcher);

			mEditTextStockQuota.removeTextChangedListener(mQuotaTextWatcher);
			mEditTextStockQuota.addTextChangedListener(mQuotaTextWatcher);

			mEditTextStockTrading.removeTextChangedListener(mTradingTextWatcher);
			mEditTextStockTrading.addTextChangedListener(mTradingTextWatcher);
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
	 * 确保Locked值是100的整数倍
	 */
	private long ensureLockedMultipleOf100(long value) {
		// 如果已经是100的整数倍，直接返回
		if (value % 100 == 0) {
			return value;
		}

		// 如果不是100的整数倍，调整为最接近的100的整数倍
		return Math.round(value / 100.0) * 100;
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

	/**
	 * 确保Trading值是100的整数倍
	 */
	private long ensureTradingMultipleOf100(long value) {
		// 如果已经是100的整数倍，直接返回
		if (value % 100 == 0) {
			return value;
		}

		// 如果不是100的整数倍，调整为最接近的100的整数倍
		return Math.round(value / 100.0) * 100;
	}

	private void setupTargetLevelPickers() {
		mTargetLevelPickerMonth.setMinValue(StockTrend.LEVEL_NONE);
		mTargetLevelPickerMonth.setMaxValue(StockTrend.LEVEL_TREND_LINE);
		mTargetLevelPickerMonth.setTargetValue(mStock.getTargetLevel(Period.MONTH));

		mTargetLevelPickerWeek.setMinValue(StockTrend.LEVEL_NONE);
		mTargetLevelPickerWeek.setMaxValue(StockTrend.LEVEL_TREND_LINE);
		mTargetLevelPickerWeek.setTargetValue(mStock.getTargetLevel(Period.WEEK));

		mTargetLevelPickerDay.setMinValue(StockTrend.LEVEL_NONE);
		mTargetLevelPickerDay.setMaxValue(StockTrend.LEVEL_TREND_LINE);
		mTargetLevelPickerDay.setTargetValue(mStock.getTargetLevel(Period.DAY));

		mTargetLevelPickerMin60.setMinValue(StockTrend.LEVEL_NONE);
		mTargetLevelPickerMin60.setMaxValue(StockTrend.LEVEL_TREND_LINE);
		mTargetLevelPickerMin60.setTargetValue(mStock.getTargetLevel(Period.MIN60));

		mTargetLevelPickerMin30.setMinValue(StockTrend.LEVEL_NONE);
		mTargetLevelPickerMin30.setMaxValue(StockTrend.LEVEL_TREND_LINE);
		mTargetLevelPickerMin30.setTargetValue(mStock.getTargetLevel(Period.MIN30));

		mTargetLevelPickerMin15.setMinValue(StockTrend.LEVEL_NONE);
		mTargetLevelPickerMin15.setMaxValue(StockTrend.LEVEL_TREND_LINE);
		mTargetLevelPickerMin15.setTargetValue(mStock.getTargetLevel(Period.MIN15));

		mTargetLevelPickerMin5.setMinValue(StockTrend.LEVEL_NONE);
		mTargetLevelPickerMin5.setMaxValue(StockTrend.LEVEL_TREND_LINE);
		mTargetLevelPickerMin5.setTargetValue(mStock.getTargetLevel(Period.MIN5));

		setupTargetLevelPickerListener(mTargetLevelPickerMonth, Period.MONTH, mTextViewMonthNet, mImageViewMonthTarget);
		setupTargetLevelPickerListener(mTargetLevelPickerWeek, Period.WEEK, mTextViewWeekNet, mImageViewWeekTarget);
		setupTargetLevelPickerListener(mTargetLevelPickerDay, Period.DAY, mTextViewDayNet, mImageViewDayTarget);
		setupTargetLevelPickerListener(mTargetLevelPickerMin60, Period.MIN60, mTextViewMin60Net, mImageViewMin60Target);
		setupTargetLevelPickerListener(mTargetLevelPickerMin30, Period.MIN30, mTextViewMin30Net, mImageViewMin30Target);
		setupTargetLevelPickerListener(mTargetLevelPickerMin15, Period.MIN15, mTextViewMin15Net, mImageViewMin15Target);
		setupTargetLevelPickerListener(mTargetLevelPickerMin5, Period.MIN5, mTextViewMin5Net, mImageViewMin5Target);
	}

	private void setupTargetLevelPickerListener(TargetLevelPicker picker, final String period,
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
		int target = mStock.getTargetLevel(period);
		if (target > StockTrend.LEVEL_NONE && target == currentLevel) {
			targetImageView.setImageResource(R.drawable.ic_crosshair_checked);
		} else {
			targetImageView.setImageResource(R.drawable.ic_crosshair_unchecked);
		}
	}

	void updateView() {
		boolean isTargetChecked = mStock.hasFlag(Stock.FLAG_TARGET);
		mCheckBoxTarget.setChecked(isTargetChecked);

		mCheckBoxShort.setChecked(mStock.hasFlag(Stock.FLAG_SHORT));
		mCheckBoxLong.setChecked(mStock.hasFlag(Stock.FLAG_LONG));

		// 根据Target状态更新Quota的显示状态
		updateQuotaEditState(isTargetChecked);

		mEditTextStockName.setText(mStock.getName());
		mEditTextStockCode.setText(mStock.getCode());

		// 设置 Locked 的值（确保是100的整数倍）
		long lockedValue = mStock.getLocked();
		lockedValue = ensureLockedMultipleOf100(lockedValue);
		mEditTextStockLocked.removeTextChangedListener(mLockedTextWatcher);
		mEditTextStockLocked.setText(String.valueOf(lockedValue));
		mEditTextStockLocked.addTextChangedListener(mLockedTextWatcher);

		// 设置配额的值（确保是100的整数倍）
		long quotaValue = mStock.getQuota();
		quotaValue = ensureQuotaMultipleOf100(quotaValue);
		mEditTextStockQuota.removeTextChangedListener(mQuotaTextWatcher);
		mEditTextStockQuota.setText(String.valueOf(quotaValue));
		mEditTextStockQuota.addTextChangedListener(mQuotaTextWatcher);

		// 设置Trading的值（确保是100的整数倍）
		long tradingValue = mStock.getTrading();
		tradingValue = ensureTradingMultipleOf100(tradingValue);
		mEditTextStockTrading.removeTextChangedListener(mTradingTextWatcher);
		mEditTextStockTrading.setText(String.valueOf(tradingValue));
		mEditTextStockTrading.addTextChangedListener(mTradingTextWatcher);

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

		mTargetLevelPickerMonth.setTargetValue(mStock.getTargetLevel(Period.MONTH));
		mTargetLevelPickerWeek.setTargetValue(mStock.getTargetLevel(Period.WEEK));
		mTargetLevelPickerDay.setTargetValue(mStock.getTargetLevel(Period.DAY));
		mTargetLevelPickerMin60.setTargetValue(mStock.getTargetLevel(Period.MIN60));
		mTargetLevelPickerMin30.setTargetValue(mStock.getTargetLevel(Period.MIN30));
		mTargetLevelPickerMin15.setTargetValue(mStock.getTargetLevel(Period.MIN15));
		mTargetLevelPickerMin5.setTargetValue(mStock.getTargetLevel(Period.MIN5));

		mTargetLevelPickerMonth.setValue(mStock.getTargetLevel(Period.MONTH));
		mTargetLevelPickerWeek.setValue(mStock.getTargetLevel(Period.WEEK));
		mTargetLevelPickerDay.setValue(mStock.getTargetLevel(Period.DAY));
		mTargetLevelPickerMin60.setValue(mStock.getTargetLevel(Period.MIN60));
		mTargetLevelPickerMin30.setValue(mStock.getTargetLevel(Period.MIN30));
		mTargetLevelPickerMin15.setValue(mStock.getTargetLevel(Period.MIN15));
		mTargetLevelPickerMin5.setValue(mStock.getTargetLevel(Period.MIN5));

		mTargetLevelPickerMonth.invalidate();
		mTargetLevelPickerWeek.invalidate();
		mTargetLevelPickerDay.invalidate();
		mTargetLevelPickerMin60.invalidate();
		mTargetLevelPickerMin30.invalidate();
		mTargetLevelPickerMin15.invalidate();
		mTargetLevelPickerMin5.invalidate();

		updateNetTextView(Period.MONTH, mStock.getTargetLevel(Period.MONTH), mTextViewMonthNet);
		updateNetTextView(Period.WEEK, mStock.getTargetLevel(Period.WEEK), mTextViewWeekNet);
		updateNetTextView(Period.DAY, mStock.getTargetLevel(Period.DAY), mTextViewDayNet);
		updateNetTextView(Period.MIN60, mStock.getTargetLevel(Period.MIN60), mTextViewMin60Net);
		updateNetTextView(Period.MIN30, mStock.getTargetLevel(Period.MIN30), mTextViewMin30Net);
		updateNetTextView(Period.MIN15, mStock.getTargetLevel(Period.MIN15), mTextViewMin15Net);
		updateNetTextView(Period.MIN5, mStock.getTargetLevel(Period.MIN5), mTextViewMin5Net);

		updateTargetImageView(Period.MONTH, mStock.getTargetLevel(Period.MONTH), mImageViewMonthTarget);
		updateTargetImageView(Period.WEEK, mStock.getTargetLevel(Period.WEEK), mImageViewWeekTarget);
		updateTargetImageView(Period.DAY, mStock.getTargetLevel(Period.DAY), mImageViewDayTarget);
		updateTargetImageView(Period.MIN60, mStock.getTargetLevel(Period.MIN60), mImageViewMin60Target);
		updateTargetImageView(Period.MIN30, mStock.getTargetLevel(Period.MIN30), mImageViewMin30Target);
		updateTargetImageView(Period.MIN15, mStock.getTargetLevel(Period.MIN15), mImageViewMin15Target);
		updateTargetImageView(Period.MIN5, mStock.getTargetLevel(Period.MIN5), mImageViewMin5Target);

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
		mLayoutTargetLevelPickers.setVisibility(hasVisiblePeriod ? View.VISIBLE : View.GONE);
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

			case R.id.checkbox_target:
				break;

			case R.id.checkbox_short:
				break;

			case R.id.checkbox_long:
				break;

			case R.id.imageview_restore_target:
				restoreToTargetValues();
				break;

			case R.id.imageview_month_target:
				toggleTargetImageView(Period.MONTH, mTargetLevelPickerMonth.getValue(), mImageViewMonthTarget);
				break;

			case R.id.imageview_week_target:
				toggleTargetImageView(Period.WEEK, mTargetLevelPickerWeek.getValue(), mImageViewWeekTarget);
				break;

			case R.id.imageview_day_target:
				toggleTargetImageView(Period.DAY, mTargetLevelPickerDay.getValue(), mImageViewDayTarget);
				break;

			case R.id.imageview_min60_target:
				toggleTargetImageView(Period.MIN60, mTargetLevelPickerMin60.getValue(), mImageViewMin60Target);
				break;

			case R.id.imageview_min30_target:
				toggleTargetImageView(Period.MIN30, mTargetLevelPickerMin30.getValue(), mImageViewMin30Target);
				break;

			case R.id.imageview_min15_target:
				toggleTargetImageView(Period.MIN15, mTargetLevelPickerMin15.getValue(), mImageViewMin15Target);
				break;

			case R.id.imageview_min5_target:
				toggleTargetImageView(Period.MIN5, mTargetLevelPickerMin5.getValue(), mImageViewMin5Target);
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

				if (mCheckBoxTarget.isChecked()) {
					mStock.addFlag(Stock.FLAG_TARGET);

					// 保存Locked值（确保是100的整数倍）
					try {
						String lockedText = mEditTextStockLocked.getText().toString();
						if (!TextUtils.isEmpty(lockedText)) {
							long locked = Long.parseLong(lockedText);

							// 检查是否为100的整数倍
							if (locked % 100 != 0) {
								// 显示提示并返回，不保存
								mTextViewLockedHint.setVisibility(View.VISIBLE);
								mTextViewLockedHint.setText(String.format(Locale.getDefault(),
										"Locked必须是100的整数倍 (建议: %d)", Math.round(locked / 100.0) * 100));
								Toast.makeText(this, "请修正Locked值", Toast.LENGTH_SHORT).show();
								return; // 不保存，返回
							}

							locked = ensureLockedMultipleOf100(locked);
							mStock.setLocked(locked);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

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
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					// 保存Trading值（确保是100的整数倍）
					try {
						String tradingText = mEditTextStockTrading.getText().toString();
						if (!TextUtils.isEmpty(tradingText)) {
							long trading = Long.parseLong(tradingText);

							// 检查是否为100的整数倍
							if (trading % 100 != 0) {
								// 显示提示并返回，不保存
								mTextViewTradingHint.setVisibility(View.VISIBLE);
								mTextViewTradingHint.setText(String.format(Locale.getDefault(),
										"Trading必须是100的整数倍 (建议: %d)", Math.round(trading / 100.0) * 100));
								Toast.makeText(this, "请修正Trading值", Toast.LENGTH_SHORT).show();
								return; // 不保存，返回
							}

							trading = ensureTradingMultipleOf100(trading);
							mStock.setTrading(trading);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					mStock.removeFlag(Stock.FLAG_TARGET);
					mStock.setBuyProfit(0);
					mStock.setSellProfit(0);
				}

				if (mCheckBoxShort.isChecked()) {
					mStock.addFlag(Stock.FLAG_SHORT);
				} else {
					mStock.removeFlag(Stock.FLAG_SHORT);
				}

				if (mCheckBoxLong.isChecked()) {
					mStock.addFlag(Stock.FLAG_LONG);
				} else {
					mStock.removeFlag(Stock.FLAG_LONG);
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
				mBackgroundHandler.downloadStockData(mStock);
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
		TargetLevelPicker picker = getTargetLevelPickerByPeriod(period);

		if (isCrosshairIconShow(imageView)) {
			imageView.setImageResource(R.drawable.ic_crosshair_unchecked);
			mStock.setTargetLevel(period, StockTrend.LEVEL_NONE);
			if (picker != null) {
				picker.setTargetValue(StockTrend.LEVEL_NONE);
			}
		} else {
			imageView.setImageResource(R.drawable.ic_crosshair_checked);
			mStock.setTargetLevel(period, target);
			if (picker != null) {
				picker.setTargetValue(target);
			}
		}
	}

	private TargetLevelPicker getTargetLevelPickerByPeriod(String period) {
		switch (period) {
			case Period.MONTH:
				return mTargetLevelPickerMonth;
			case Period.WEEK:
				return mTargetLevelPickerWeek;
			case Period.DAY:
				return mTargetLevelPickerDay;
			case Period.MIN60:
				return mTargetLevelPickerMin60;
			case Period.MIN30:
				return mTargetLevelPickerMin30;
			case Period.MIN15:
				return mTargetLevelPickerMin15;
			case Period.MIN5:
				return mTargetLevelPickerMin5;
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

	private void restoreToTargetValues() {
		restorePeriodToTarget(Period.MONTH, mTargetLevelPickerMonth, mTextViewMonthNet, mImageViewMonthTarget);
		restorePeriodToTarget(Period.WEEK, mTargetLevelPickerWeek, mTextViewWeekNet, mImageViewWeekTarget);
		restorePeriodToTarget(Period.DAY, mTargetLevelPickerDay, mTextViewDayNet, mImageViewDayTarget);
		restorePeriodToTarget(Period.MIN60, mTargetLevelPickerMin60, mTextViewMin60Net, mImageViewMin60Target);
		restorePeriodToTarget(Period.MIN30, mTargetLevelPickerMin30, mTextViewMin30Net, mImageViewMin30Target);
		restorePeriodToTarget(Period.MIN15, mTargetLevelPickerMin15, mTextViewMin15Net, mImageViewMin15Target);
		restorePeriodToTarget(Period.MIN5, mTargetLevelPickerMin5, mTextViewMin5Net, mImageViewMin5Target);
	}

	private void restorePeriodToTarget(String period, TargetLevelPicker picker, TextView netTextView, ImageView targetImageView) {
		int target = mStock.getTargetLevel(period);
		if (target > StockTrend.LEVEL_NONE) {
			picker.setValue(target);
			updateNetTextView(period, target, netTextView);
			updateTargetImageView(period, target, targetImageView);
		}
	}
}