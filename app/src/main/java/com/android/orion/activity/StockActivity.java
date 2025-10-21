package com.android.orion.activity;

import android.graphics.Color;
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
import android.widget.ImageView;
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
	TextView mTextviewDayNet;
	TextView mTextviewMin60Net;
	TextView mTextviewMin30Net;
	TextView mTextviewMin15Net;
	TextView mTextviewMin5Net;
	TextView mTextViewSeUrl;
	TradeLevelPicker mTradeLevelPickerDay;
	TradeLevelPicker mTradeLevelPickerMin60;
	TradeLevelPicker mTradeLevelPickerMin30;
	TradeLevelPicker mTradeLevelPickerMin15;
	TradeLevelPicker mTradeLevelPickerMin5;
	ImageView mImageViewDayCrosshair;
	ImageView mImageViewMin60Crosshair;
	ImageView mImageViewMin30Crosshair;
	ImageView mImageViewMin15Crosshair;
	ImageView mImageViewMin5Crosshair;
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
		mTextviewDayNet = findViewById(R.id.textview_day_net);
		mTextviewMin60Net = findViewById(R.id.textview_min60_net);
		mTextviewMin30Net = findViewById(R.id.textview_min30_net);
		mTextviewMin15Net = findViewById(R.id.textview_min15_net);
		mTextviewMin5Net = findViewById(R.id.textview_min5_net);
		mTextViewSeUrl = findViewById(R.id.textview_se_url);
		mTradeLevelPickerDay = findViewById(R.id.trade_level_picker_day);
		mTradeLevelPickerMin60 = findViewById(R.id.trade_level_picker_min60);
		mTradeLevelPickerMin30 = findViewById(R.id.trade_level_picker_min30);
		mTradeLevelPickerMin15 = findViewById(R.id.trade_level_picker_min15);
		mTradeLevelPickerMin5 = findViewById(R.id.trade_level_picker_min5);
		mImageViewDayCrosshair = findViewById(R.id.imageview_day_crosshair);
		mImageViewMin60Crosshair = findViewById(R.id.imageview_min60_crosshair);
		mImageViewMin30Crosshair = findViewById(R.id.imageview_min30_crosshair);
		mImageViewMin15Crosshair = findViewById(R.id.imageview_min15_crosshair);
		mImageViewMin5Crosshair = findViewById(R.id.imageview_min5_crosshair);
		mButtonOk = findViewById(R.id.button_ok);
		mButtonCancel = findViewById(R.id.button_cancel);

		mCheckBoxTrade.setOnClickListener(this);
		mCheckBoxCustom.setOnClickListener(this);
		mEditTextStockName.setOnClickListener(this);
		mEditTextStockCode.setOnClickListener(this);
		mEditTextStockHold.setOnClickListener(this);
		mEditTextStockYield.setOnClickListener(this);
		mTextViewSeUrl.setOnClickListener(this);
		mImageViewDayCrosshair.setOnClickListener(this);
		mImageViewMin60Crosshair.setOnClickListener(this);
		mImageViewMin30Crosshair.setOnClickListener(this);
		mImageViewMin15Crosshair.setOnClickListener(this);
		mImageViewMin5Crosshair.setOnClickListener(this);
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
			mStockDatabaseManager.getStockTrendMap(mStock);
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

		setupTradeLevelPickerListener(mTradeLevelPickerDay, Period.DAY, mTextviewDayNet);
		setupTradeLevelPickerListener(mTradeLevelPickerMin60, Period.MIN60, mTextviewMin60Net);
		setupTradeLevelPickerListener(mTradeLevelPickerMin30, Period.MIN30, mTextviewMin30Net);
		setupTradeLevelPickerListener(mTradeLevelPickerMin15, Period.MIN15, mTextviewMin15Net);
		setupTradeLevelPickerListener(mTradeLevelPickerMin5, Period.MIN5, mTextviewMin5Net);
	}

	private void setupTradeLevelPickerListener(TradeLevelPicker picker, final String period, final TextView netTextView) {
		picker.setOnValueChangedListener(new android.widget.NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(android.widget.NumberPicker numberPicker, int oldVal, int newVal) {
				updateNetForPeriod(period, newVal, netTextView);
			}
		});
	}

	private void updateNetForPeriod(String period, int level, TextView netTextView) {
		StockTrend stockTrend = mStock.getStockTrend(period, level);
		if (stockTrend != null) {
			setNetTextView(netTextView, stockTrend.getNextNet());
		} else {
			setNetTextView(netTextView, 0);
		}
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

		updateNetForPeriod(Period.DAY, mStock.getLevel(Period.DAY), mTextviewDayNet);
		updateNetForPeriod(Period.MIN60, mStock.getLevel(Period.MIN60), mTextviewMin60Net);
		updateNetForPeriod(Period.MIN30, mStock.getLevel(Period.MIN30), mTextviewMin30Net);
		updateNetForPeriod(Period.MIN15, mStock.getLevel(Period.MIN15), mTextviewMin15Net);
		updateNetForPeriod(Period.MIN5, mStock.getLevel(Period.MIN5), mTextviewMin5Net);

		restoreCrosshairState(mImageViewDayCrosshair, Period.DAY);
		restoreCrosshairState(mImageViewMin60Crosshair, Period.MIN60);
		restoreCrosshairState(mImageViewMin30Crosshair, Period.MIN30);
		restoreCrosshairState(mImageViewMin15Crosshair, Period.MIN15);
		restoreCrosshairState(mImageViewMin5Crosshair, Period.MIN5);
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

			case R.id.imageview_day_crosshair:
				toggleCrosshairIcon(mImageViewDayCrosshair, Period.DAY);
				break;

			case R.id.imageview_min60_crosshair:
				toggleCrosshairIcon(mImageViewMin60Crosshair, Period.MIN60);
				break;

			case R.id.imageview_min30_crosshair:
				toggleCrosshairIcon(mImageViewMin30Crosshair, Period.MIN30);
				break;

			case R.id.imageview_min15_crosshair:
				toggleCrosshairIcon(mImageViewMin15Crosshair, Period.MIN15);
				break;

			case R.id.imageview_min5_crosshair:
				toggleCrosshairIcon(mImageViewMin5Crosshair, Period.MIN5);
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

				saveCrosshairStates();

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

	/**
	 * 切换crosshair图标状态
	 */
	private void toggleCrosshairIcon(ImageView imageView, String period) {
		// 检查当前图标状态并切换
		if (isCrosshairEnabled(imageView)) {
			// 当前是crosshair状态，切换为unchecked
			imageView.setImageResource(R.drawable.ic_unchecked);
			saveCrosshairState(period, false);
		} else {
			// 当前是unchecked状态，切换为crosshair
			imageView.setImageResource(R.drawable.ic_crosshair);
			saveCrosshairState(period, true);
		}
	}

	/**
	 * 检查当前图标是否是crosshair状态
	 */
	private boolean isCrosshairEnabled(ImageView imageView) {
		try {
			// 通过检查当前设置的图片资源来判断状态
			return imageView.getDrawable().getConstantState().equals(
					getResources().getDrawable(R.drawable.ic_crosshair).getConstantState());
		} catch (Exception e) {
			// 如果比较失败，默认返回false
			return false;
		}
	}

	/**
	 * 保存crosshair状态到Stock对象
	 */
	private void saveCrosshairState(String period, boolean enabled) {
		// 将状态保存到Stock对象中
//		mStock.setCrosshairEnabled(period, enabled);
	}

	/**
	 * 保存所有crosshair状态
	 */
	private void saveCrosshairStates() {
		// 所有状态已经在点击时保存到Stock对象中
		// 这里可以添加额外的保存逻辑，如果需要的话
	}

	/**
	 * 从Stock对象恢复crosshair状态
	 */
	private void restoreCrosshairState(ImageView imageView, String period) {
		// 从Stock对象加载状态并设置图标
		boolean isEnabled = false;//mStock.isCrosshairEnabled(period);
		imageView.setImageResource(isEnabled ? R.drawable.ic_crosshair : R.drawable.ic_unchecked);
	}
}
