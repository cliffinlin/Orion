package com.android.orion.activity;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.orion.R;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.IndexComponent;
import com.android.orion.database.Stock;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.List;

public class StockActivity extends DatabaseActivity implements OnClickListener, AdapterView.OnItemSelectedListener {

	CheckBox mCheckBoxFavorite;
	CheckBox mCheckBoxNotify;
	RadioGroup mRadioGroupClass;
	RadioGroup mRadioGroupSE;
	EditText mEditTextStockName;
	EditText mEditTextStockCode;
	EditText mEditTextStockHold;
	EditText mEditTextStockYield;

	List<String> mListStockOperate;
	ArrayAdapter<String> mArrayAdapter;
	Spinner mSpinnerStockAcion;

	Button mButtonOk;
	Button mButtonCancel;

	String mStockOperate;

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
		mRadioGroupClass = findViewById(R.id.radio_group_class);
		mRadioGroupSE = findViewById(R.id.radio_group_se);
		mEditTextStockName = findViewById(R.id.edittext_stock_name);
		mEditTextStockCode = findViewById(R.id.edittext_stock_code);
		mEditTextStockHold = findViewById(R.id.edittext_stock_hold);
		mEditTextStockYield = findViewById(R.id.edittext_stock_yield);
		mSpinnerStockAcion = findViewById(R.id.spinner_stock_operate);
		mButtonOk = findViewById(R.id.button_ok);
		mButtonCancel = findViewById(R.id.button_cancel);

		mCheckBoxFavorite.setOnClickListener(this);
		mCheckBoxNotify.setOnClickListener(this);
		mRadioGroupClass.setOnClickListener(this);
		mRadioGroupSE.setOnClickListener(this);
		mEditTextStockName.setOnClickListener(this);
		mEditTextStockCode.setOnClickListener(this);
		mEditTextStockHold.setOnClickListener(this);
		mEditTextStockYield.setOnClickListener(this);
		mSpinnerStockAcion.setOnItemSelectedListener(this);
		mButtonOk.setOnClickListener(this);
		mButtonCancel.setOnClickListener(this);

		mListStockOperate = new ArrayList<String>();
		mListStockOperate.add("");
		mListStockOperate.add(DatabaseContract.COLUMN_MONTH);
		mListStockOperate.add(DatabaseContract.COLUMN_WEEK);
		mListStockOperate.add(DatabaseContract.COLUMN_DAY);
		mListStockOperate.add(DatabaseContract.COLUMN_MIN60);
		mListStockOperate.add(DatabaseContract.COLUMN_MIN30);
		mListStockOperate.add(DatabaseContract.COLUMN_MIN15);
		mListStockOperate.add(DatabaseContract.COLUMN_MIN5);

		mArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, mListStockOperate);
		mArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerStockAcion.setAdapter(mArrayAdapter);

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
					if (s.charAt(0) == '6') {
						mRadioGroupClass.check(R.id.radio_class_hsa);
						mRadioGroupSE.check(R.id.radio_se_sh);
					} else if (s.charAt(0) == '0' || s.charAt(0) == '3') {
						mRadioGroupClass.check(R.id.radio_class_hsa);
						mRadioGroupSE.check(R.id.radio_se_sz);
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
		} else if (TextUtils.equals(mAction, Constant.ACTION_INDEX_COMPONENT_INSERT)) {
			setTitle(R.string.stock_insert);
		} else if (TextUtils.equals(mAction, Constant.ACTION_STOCK_EDIT)) {
			setTitle(R.string.stock_edit);
			mRadioGroupClass.setEnabled(false);
			mRadioGroupSE.setEnabled(false);
			mEditTextStockCode.setEnabled(false);
			mEditTextStockHold.setEnabled(false);
			mEditTextStockYield.setEnabled(false);
			mStock.setId(mIntent.getLongExtra(Constant.EXTRA_STOCK_ID,
					DatabaseContract.INVALID_ID));
			mDatabaseManager.getStockById(mStock);
		}
	}

	void updateView() {
		mCheckBoxFavorite.setChecked(mStock.hasFlag(Stock.FLAG_FAVORITE));
		mCheckBoxNotify.setChecked(mStock.hasFlag(Stock.FLAG_NOTIFY));

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

		String operate = mStock.getOperate();
		for (int i = 0; i < mListStockOperate.size(); i++) {
			if (TextUtils.equals(mListStockOperate.get(i), operate)) {
				mSpinnerStockAcion.setSelection(i);
				break;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_deal_edit, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onClick(@NonNull View view) {
		String operate;
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
					mSpinnerStockAcion.setSelection(0);
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

				operate = mSpinnerStockAcion.getSelectedItem().toString();
				if (!TextUtils.equals(operate, mStockOperate)) {
					mStockOperate = operate;
					mStock.setOperate(mStockOperate);
				}

				String name = mEditTextStockName.getText().toString();
				String code = mEditTextStockCode.getText().toString();

				if (!TextUtils.isEmpty(name)) {
					mStock.setName(name);
				}

				if (!TextUtils.isEmpty(code)) {
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

				if (TextUtils.equals(mAction, Constant.ACTION_FAVORITE_STOCK_INSERT) || TextUtils.equals(mAction, Constant.ACTION_INDEX_COMPONENT_INSERT)) {
					if (!mDatabaseManager.isStockExist(mStock)) {
						mStock.setCreated(Utility.getCurrentDateTimeString());
						Uri uri = mDatabaseManager.insertStock(mStock);
						mDatabaseManager.getStock(uri, mStock);
						mDatabaseManager.updateStock(mStock, mStock.getContentValuesEdit());

						if (TextUtils.equals(mAction, Constant.ACTION_INDEX_COMPONENT_INSERT)) {
							IndexComponent indexComponent = new IndexComponent();

							indexComponent.setSE(mStock.getSE());
							indexComponent.setCode(mStock.getCode());
							indexComponent.setName(mStock.getName());

							if (!mDatabaseManager.isIndexComponentExist(indexComponent)) {
								indexComponent.setCreated(Utility.getCurrentDateTimeString());
								mDatabaseManager.insertIndexComponent(indexComponent);
							} else {
								Toast.makeText(mContext, R.string.stock_exist,
										Toast.LENGTH_LONG).show();
								return;
							}
						}
					} else {
						mDatabaseManager.getStock(mStock);
						updateView();
						Toast.makeText(mContext, R.string.stock_exist,
								Toast.LENGTH_LONG).show();
						return;
					}
				} else if (TextUtils.equals(mAction, Constant.ACTION_STOCK_EDIT)) {
					mStock.setModified(Utility.getCurrentDateTimeString());
					mDatabaseManager.updateStock(mStock,
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

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		String operate;

		operate = mSpinnerStockAcion.getSelectedItem().toString();
		if (!TextUtils.isEmpty(operate)) {
			if (!TextUtils.equals(operate, mStock.getOperate())) {
				mStock.setOperate(operate);
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}
}
