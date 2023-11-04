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

import com.android.orion.setting.Constant;
import com.android.orion.R;
import com.android.orion.setting.Setting;
import com.android.orion.database.IndexComponent;
import com.android.orion.database.Stock;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.List;

public class StockEditActivity extends DatabaseActivity implements OnClickListener, AdapterView.OnItemSelectedListener {

	public static final String ACTION_INDEX_COMPONENT_INSERT = "orion.intent.action.ACTION_INDEX_COMPONENT_INSERT";
	public static final String ACTION_FAVORITE_STOCK_INSERT = "orion.intent.action.ACTION_FAVORITE_STOCK_INSERT";
	public static final String ACTION_STOCK_EDIT = "orion.intent.action.ACTION_STOCK_EDIT";

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

	double mStockThreshold;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_edit);

		if (mStock == null) {
			mStock = new Stock();
		}

		initView();

		if (ACTION_STOCK_EDIT.equals(mAction)) {
			mRadioGroupClass.setEnabled(false);
			mRadioGroupSE.setEnabled(false);
			mEditTextStockCode.setEnabled(false);

			mStock.setId(mIntent.getLongExtra(Constant.EXTRA_STOCK_ID,
					Stock.INVALID_ID));
			mStockDatabaseManager.getStockById(mStock);
			mStockThreshold = mStock.getThreshold();
			updateView();
		}
	}

	void initView() {
		mCheckBoxFavorite = (CheckBox) findViewById(R.id.checkbox_favorite);
		mRadioGroupClass = (RadioGroup) findViewById(R.id.radio_group_class);
		mRadioGroupSE = (RadioGroup) findViewById(R.id.radio_group_se);
		mEditTextStockName = (EditText) findViewById(R.id.edittext_stock_name);
		mEditTextStockCode = (EditText) findViewById(R.id.edittext_stock_code);
		mEditTextStockQuantVolume = (EditText) findViewById(R.id.edittext_stock_quant_volume);
		mEditTextStockThreshold = (EditText) findViewById(R.id.edittext_threshold);
		mSpinnerStockAcion = (Spinner) findViewById(R.id.spinner_stock_operate);
		mButtonOk = (Button) findViewById(R.id.button_ok);
		mButtonCancel = (Button) findViewById(R.id.button_cancel);

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
		mListStockOperate.add(Setting.KEY_PERIOD_MONTH);
		mListStockOperate.add(Setting.KEY_PERIOD_WEEK);
		mListStockOperate.add(Setting.KEY_PERIOD_DAY);
		mListStockOperate.add(Setting.KEY_PERIOD_MIN60);
		mListStockOperate.add(Setting.KEY_PERIOD_MIN30);
		mListStockOperate.add(Setting.KEY_PERIOD_MIN15);
		mListStockOperate.add(Setting.KEY_PERIOD_MIN5);

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
				if (ACTION_STOCK_EDIT.equals(mAction)) {
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

		if (ACTION_INDEX_COMPONENT_INSERT.equals(mAction) || ACTION_FAVORITE_STOCK_INSERT.equals(mAction)) {
			setTitle(R.string.stock_insert);
		} else if (ACTION_STOCK_EDIT.equals(mAction)) {
			setTitle(R.string.stock_edit);
		}
	}

	void updateView() {
		mCheckBoxFavorite.setChecked(mStock.hasFlag(Stock.FLAG_FAVORITE));

		if (mStock.getClasses().equals(Stock.CLASS_A)) {
			mRadioGroupClass.check(R.id.radio_class_hsa);
		} else {
			mRadioGroupClass.check(R.id.radio_class_index);
		}

		if (mStock.getSE().equals(Stock.SE_SH)) {
			mRadioGroupSE.check(R.id.radio_se_sh);
		} else {
			mRadioGroupSE.check(R.id.radio_se_sz);
		}

		mEditTextStockName.setText(mStock.getName());
		mEditTextStockCode.setText(mStock.getCode());
		if (mStock.getThreshold() == 0) {
            mStock.setThreshold(Constant.STOCK_THRESHOLD);
        }
		mEditTextStockQuantVolume.setText(String.valueOf(mStock.getQuantVolume()));
		mEditTextStockThreshold.setText(String.valueOf(mStock.getThreshold()));
		if (mStock.getQuantVolume() == 0) {
			mStock.setQuantVolume(Constant.STOCK_QUANT_VOLUME);
		}

		String operate = mStock.getOperate();
		for (int i = 0; i < mListStockOperate.size(); i++) {
			if (mListStockOperate.get(i).equals(operate)) {
				mSpinnerStockAcion.setSelection(i);
				break;
			}
		}
	}

	void onCheckBoxFavoriteChanged() {
        if (mCheckBoxFavorite.isChecked()) {
            mStock.addFlag(Stock.FLAG_FAVORITE);
        } else {
            mStock.removeFlag(Stock.FLAG_FAVORITE);
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

            onCheckBoxFavoriteChanged();

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
			mStock.setQuantVolume(quantVolumeValue);

			String threshold = mEditTextStockThreshold.getText().toString();
			double thresholdValue = TextUtils.isEmpty(threshold) ? 0 : Double.valueOf(threshold);
			if (thresholdValue != mStockThreshold) {
				mStockThreshold = thresholdValue;
				mStock.setThreshold(mStockThreshold);
				mStockDatabaseManager.deleteStockQuant(mStock);
			}

			operate = mSpinnerStockAcion.getSelectedItem().toString();
			mStock.setOperate(operate);

			if (ACTION_FAVORITE_STOCK_INSERT.equals(mAction) || ACTION_INDEX_COMPONENT_INSERT.equals(mAction)) {
				if (!mStockDatabaseManager.isStockExist(mStock)) {
					mStock.setCreated(Utility.getCurrentDateTimeString());
					Uri uri = mStockDatabaseManager.insertStock(mStock);

                    mStockDatabaseManager.getStock(uri, mStock);

					if (ACTION_INDEX_COMPONENT_INSERT.equals(mAction)) {
						IndexComponent indexComponent = new IndexComponent();

						indexComponent.setSE(mStock.getSE());
						indexComponent.setCode(mStock.getCode());
						indexComponent.setName(mStock.getName());

						if (!mStockDatabaseManager.isIndexComponentExist(indexComponent)) {
							indexComponent.setCreated(Utility.getCurrentDateTimeString());
							mStockDatabaseManager.insertIndexComponent(indexComponent);
						} else {
							Toast.makeText(mContext, R.string.stock_exist,
									Toast.LENGTH_LONG).show();
						}
					}
				} else {
					Toast.makeText(mContext, R.string.stock_exist,
							Toast.LENGTH_LONG).show();
				}
			} else if (ACTION_STOCK_EDIT.equals(mAction)) {
				mStock.setModified(Utility.getCurrentDateTimeString());
				mStockDatabaseManager.updateStock(mStock,
						mStock.getContentValuesForEdit());
			}

			getIntent().putExtra(Constant.EXTRA_STOCK_ID, mStock.getId());

			setResult(RESULT_OK, getIntent());

			mOrionService.download(mStock.getSE(), mStock.getCode());

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
			if (!operate.equals(mStock.getOperate())) {
				mStock.setOperate(operate);
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}
}
