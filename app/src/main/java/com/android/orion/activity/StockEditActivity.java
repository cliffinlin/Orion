package com.android.orion.activity;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
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

import com.android.orion.R;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.IndexComponent;
import com.android.orion.database.Stock;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.List;

public class StockEditActivity extends DatabaseActivity implements OnClickListener, AdapterView.OnItemSelectedListener {

	CheckBox mCheckBoxFavorite;
	RadioGroup mRadioGroupClass;
	RadioGroup mRadioGroupSE;
	EditText mEditTextStockName;
	EditText mEditTextStockCode;
	EditText mEditTextStockQuantVolume;
	EditText mEditTextStockThreshold;

	List<String> mListStockOperate;
	ArrayAdapter<String> mArrayAdapter;
	Spinner mSpinnerStockAcion;

	Button mButtonOk;
	Button mButtonCancel;

	String mStockOperate;
	long mStockQuantVolume;
	double mStockThreshold;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_edit);

		if (mStock == null) {
			mStock = new Stock();
		}

		initView();

		if (TextUtils.equals(mAction, Constant.ACTION_STOCK_EDIT)) {
			mRadioGroupClass.setEnabled(false);
			mRadioGroupSE.setEnabled(false);
			mEditTextStockCode.setEnabled(false);

			mStock.setId(mIntent.getLongExtra(Constant.EXTRA_STOCK_ID,
					Stock.INVALID_ID));
			mDatabaseManager.getStockById(mStock);
			mStockOperate = mStock.getOperate();
			mStockQuantVolume = mStock.getQuantVolume();
			mStockThreshold = mStock.getThreshold();
			updateView();
		}
	}

	void initView() {
		mCheckBoxFavorite = findViewById(R.id.checkbox_favorite);
		mRadioGroupClass = findViewById(R.id.radio_group_class);
		mRadioGroupSE = findViewById(R.id.radio_group_se);
		mEditTextStockName = findViewById(R.id.edittext_stock_name);
		mEditTextStockCode = findViewById(R.id.edittext_stock_code);
		mEditTextStockQuantVolume = findViewById(R.id.edittext_stock_quant_volume);
		mEditTextStockThreshold = findViewById(R.id.edittext_threshold);
		mSpinnerStockAcion = findViewById(R.id.spinner_stock_operate);
		mButtonOk = findViewById(R.id.button_ok);
		mButtonCancel = findViewById(R.id.button_cancel);

		mCheckBoxFavorite.setOnClickListener(this);
		mRadioGroupClass.setOnClickListener(this);
		mRadioGroupSE.setOnClickListener(this);
		mEditTextStockName.setOnClickListener(this);
		mEditTextStockCode.setOnClickListener(this);
		mEditTextStockQuantVolume.setOnClickListener(this);
		mEditTextStockThreshold.setOnClickListener(this);
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
				}
			}
		});

		if (TextUtils.equals(mAction, Constant.ACTION_INDEX_COMPONENT_INSERT) || TextUtils.equals(mAction, Constant.ACTION_FAVORITE_STOCK_INSERT)) {
			setTitle(R.string.stock_insert);
		} else if (TextUtils.equals(mAction, Constant.ACTION_STOCK_EDIT)) {
			setTitle(R.string.stock_edit);
		}
	}

	void updateView() {
		mCheckBoxFavorite.setChecked(mStock.hasFlag(Stock.FLAG_FAVORITE));

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
		mEditTextStockQuantVolume.setText(String.valueOf(mStock.getQuantVolume()));
		mEditTextStockThreshold.setText(String.valueOf(mStock.getThreshold()));

		String operate = mStock.getOperate();
		for (int i = 0; i < mListStockOperate.size(); i++) {
			if (TextUtils.equals(mListStockOperate.get(i), operate)) {
				mSpinnerStockAcion.setSelection(i);
				break;
			}
		}
	}

	void onCheckBoxFavoriteChanged() {
		if (mCheckBoxFavorite.isChecked()) {
			mStock.addFlag(Stock.FLAG_FAVORITE);
			mStockManager.onStockAddFavorite(mStock);
		} else {
			mStock.removeFlag(Stock.FLAG_FAVORITE);
			mStockManager.onStockRemoveFavorite(mStock);
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
		String operate;
		int viewId = view.getId();

		switch (viewId) {
			case R.id.checkbox_favorite:
				onCheckBoxFavoriteChanged();
				break;

			case R.id.button_ok:
				int id = 0;
				boolean operateChanged = false;
				boolean quantVolumeChanged = false;
				boolean thresholdChanged = false;

				onCheckBoxFavoriteChanged();

				operate = mSpinnerStockAcion.getSelectedItem().toString();
				if (!TextUtils.equals(operate, mStockOperate)) {
					operateChanged = true;
					mStockOperate = operate;
					mStock.setOperate(mStockOperate);
					mDatabaseManager.deleteStockQuant(mStock);
				}

				id = mRadioGroupClass.getCheckedRadioButtonId();
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
				}

				String quantVolume = mEditTextStockQuantVolume.getText().toString();
				long quantVolumeValue = TextUtils.isEmpty(quantVolume) ? 0 : Long.valueOf(quantVolume);
				if (quantVolumeValue != mStockQuantVolume) {
					quantVolumeChanged = true;
					mStockQuantVolume = quantVolumeValue;
					mStock.setQuantVolume(mStockQuantVolume);
					mDatabaseManager.deleteStockQuant(mStock);
				}

				String threshold = mEditTextStockThreshold.getText().toString();
				double thresholdValue = TextUtils.isEmpty(threshold) ? 0 : Double.valueOf(threshold);
				if (thresholdValue != mStockThreshold) {
					thresholdChanged = true;
					mStockThreshold = thresholdValue;
					mStock.setThreshold(mStockThreshold);

				}

				if (TextUtils.isEmpty(operate) || TextUtils.isEmpty(quantVolume) || TextUtils.isEmpty(threshold)
						|| operateChanged || quantVolumeChanged || thresholdChanged) {
					mDatabaseManager.deleteStockQuant(mStock);
				}

				if (TextUtils.equals(mAction, Constant.ACTION_FAVORITE_STOCK_INSERT) || TextUtils.equals(mAction, Constant.ACTION_INDEX_COMPONENT_INSERT)) {
					if (!mDatabaseManager.isStockExist(mStock)) {
						mStock.setCreated(Utility.getCurrentDateTimeString());
						Uri uri = mDatabaseManager.insertStock(mStock);

						mDatabaseManager.getStock(uri, mStock);

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
							}
						}
					} else {
						Toast.makeText(mContext, R.string.stock_exist,
								Toast.LENGTH_LONG).show();
					}
				} else if (TextUtils.equals(mAction, Constant.ACTION_STOCK_EDIT)) {
					mStock.setModified(Utility.getCurrentDateTimeString());
					mDatabaseManager.updateStock(mStock,
							mStock.getContentValuesForEdit());
				}

				getIntent().putExtra(Constant.EXTRA_STOCK_ID, mStock.getId());

				setResult(RESULT_OK, getIntent());

				Setting.setStockDataChanged(mStock.getSE(), mStock.getCode(), true);
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
