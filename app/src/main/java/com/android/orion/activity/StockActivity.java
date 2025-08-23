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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.orion.R;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Utility;

public class StockActivity extends DatabaseActivity implements OnClickListener {

	CheckBox mCheckBoxFavorite;
	CheckBox mCheckBoxNotify;
	CheckBox mCheckBoxGrid;
	RadioGroup mRadioGroupClass;
	RadioGroup mRadioGroupSE;
	EditText mEditTextStockName;
	EditText mEditTextStockCode;
	EditText mEditTextStockHold;
	EditText mEditTextStockYield;
	TextView mTextViewSeUrl;
	Button mButtonOk;
	Button mButtonCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_edit);

		if (mStock == null) {
			mStock = new Stock();
		}

		initView();

		updateView();
	}

	void initView() {
		mCheckBoxFavorite = findViewById(R.id.checkbox_favorite);
		mCheckBoxNotify = findViewById(R.id.checkbox_notify);
		mCheckBoxGrid = findViewById(R.id.checkbox_grid);
		mRadioGroupClass = findViewById(R.id.radiogroup_class);
		mRadioGroupSE = findViewById(R.id.radiogroup_se);
		mEditTextStockName = findViewById(R.id.edittext_stock_name);
		mEditTextStockCode = findViewById(R.id.edittext_stock_code);
		mEditTextStockHold = findViewById(R.id.edittext_stock_hold);
		mEditTextStockYield = findViewById(R.id.edittext_stock_yield);
		mTextViewSeUrl = findViewById(R.id.textview_se_url);
		mButtonOk = findViewById(R.id.button_ok);
		mButtonCancel = findViewById(R.id.button_cancel);

		mCheckBoxFavorite.setOnClickListener(this);
		mCheckBoxNotify.setOnClickListener(this);
		mCheckBoxGrid.setOnClickListener(this);
		mRadioGroupClass.setOnClickListener(this);
		mRadioGroupSE.setOnClickListener(this);
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
						mRadioGroupClass.check(R.id.radio_class_hsa);
						mRadioGroupSE.check(R.id.radio_se_sz);
					} else if (s.toString().startsWith(Stock.CODE_PREFIX_5) || s.toString().startsWith(Stock.CODE_PREFIX_6)) {
						mRadioGroupClass.check(R.id.radio_class_hsa);
						mRadioGroupSE.check(R.id.radio_se_sh);
					} else {
						mRadioGroupClass.check(R.id.radio_class_index);
						mRadioGroupSE.check(R.id.radio_se_sh);
					}
					mStock.setCode(s.toString());
				}
			}
		});

		if (TextUtils.equals(mAction, Constant.ACTION_FAVORITE_STOCK_INSERT)) {
			setTitle(R.string.stock_insert);
			mStock.addFlag(Stock.FLAG_FAVORITE);
			mStock.addFlag(Stock.FLAG_NOTIFY);
		} else if (TextUtils.equals(mAction, Constant.ACTION_STOCK_EDIT)) {
			setTitle(R.string.stock_edit);
			mRadioGroupClass.setEnabled(false);
			mRadioGroupSE.setEnabled(false);
			mEditTextStockCode.setEnabled(false);
			mEditTextStockHold.setEnabled(false);
			mEditTextStockYield.setEnabled(false);
			mStock.setId(mIntent.getLongExtra(Constant.EXTRA_STOCK_ID,
					DatabaseContract.INVALID_ID));
			mStockDatabaseManager.getStockById(mStock);
		}
	}

	void updateView() {
		mCheckBoxFavorite.setChecked(mStock.hasFlag(Stock.FLAG_FAVORITE));
		mCheckBoxNotify.setChecked(mStock.hasFlag(Stock.FLAG_NOTIFY));
		mCheckBoxGrid.setChecked(mStock.hasFlag(Stock.FLAG_GRID));

		if (TextUtils.equals(mStock.getClasses(), Stock.CLASS_A)) {
			mRadioGroupClass.check(R.id.radio_class_hsa);
		} else {
			mRadioGroupClass.check(R.id.radio_class_index);
		}

		if (TextUtils.equals(mStock.getSE(), Stock.SE_SH)) {
			mRadioGroupSE.check(R.id.radio_se_sh);
		} else {
			mRadioGroupSE.check(R.id.radio_se_sz);
		}

		mEditTextStockName.setText(mStock.getName());
		mEditTextStockCode.setText(mStock.getCode());
		mEditTextStockHold.setText(String.valueOf(mStock.getHold()));
		mEditTextStockYield.setText(String.valueOf(mStock.getYield()));
		mTextViewSeUrl.setText(mStock.getSeUrl());
		mTextViewSeUrl.setPaintFlags(mTextViewSeUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
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
			case R.id.checkbox_favorite:
				if (mCheckBoxFavorite.isChecked()) {
					mStock.addFlag(Stock.FLAG_FAVORITE);
					mStockManager.onAddFavorite(mStock);
				} else {
					mStock.removeFlag(Stock.FLAG_FAVORITE);
					mStock.removeFlag(Stock.FLAG_NOTIFY);
					mCheckBoxNotify.setChecked(mStock.hasFlag(Stock.FLAG_NOTIFY));
					mStockManager.onRemoveFavorite(mStock);
				}
				break;

			case R.id.checkbox_notify:
				if (mCheckBoxNotify.isChecked()) {
					mStock.addFlag(Stock.FLAG_NOTIFY);
				} else {
					mStock.removeFlag(Stock.FLAG_NOTIFY);
				}
				break;

			case R.id.checkbox_grid:
				if (mCheckBoxGrid.isChecked()) {
					mStock.addFlag(Stock.FLAG_GRID);
				} else {
					mStock.removeFlag(Stock.FLAG_GRID);
					mStock.setGridProfit(0);
				}
				break;

			case R.id.button_ok:
				if (mCheckBoxFavorite.isChecked()) {
					mStock.addFlag(Stock.FLAG_FAVORITE);
				} else {
					mStock.removeFlag(Stock.FLAG_FAVORITE);
				}

				if (mCheckBoxNotify.isChecked()) {
					mStock.addFlag(Stock.FLAG_NOTIFY);
				} else {
					mStock.removeFlag(Stock.FLAG_NOTIFY);
				}

				if (mCheckBoxGrid.isChecked()) {
					mStock.addFlag(Stock.FLAG_GRID);
				} else {
					mStock.removeFlag(Stock.FLAG_GRID);
					mStock.setGridProfit(0);
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

				int id = mRadioGroupClass.getCheckedRadioButtonId();
				if (id == R.id.radio_class_hsa) {
					mStock.setClasses(Stock.CLASS_A);
				} else if (id == R.id.radio_class_index) {
					mStock.setClasses(Stock.CLASS_INDEX);
				}

				id = mRadioGroupSE.getCheckedRadioButtonId();
				if (id == R.id.radio_se_sh) {
					mStock.setSE(Stock.SE_SH);
				} else if (id == R.id.radio_se_sz) {
					mStock.setSE(Stock.SE_SZ);
				}

				if (TextUtils.equals(mAction, Constant.ACTION_FAVORITE_STOCK_INSERT)) {
					if (!mStockDatabaseManager.isStockExist(mStock)) {
						mStock.setCreated(Utility.getCurrentDateTimeString());
						Uri uri = mStockDatabaseManager.insert(mStock);
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
