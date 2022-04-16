package com.android.orion;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.orion.database.IndexComponent;
import com.android.orion.database.Stock;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.List;

public class StockEditActivity extends DatabaseActivity implements OnClickListener {

	public static final String ACTION_INDEX_COMPONENT_INSERT = "orion.intent.action.ACTION_INDEX_COMPONENT_INSERT";
	public static final String ACTION_FAVORITE_STOCK_INSERT = "orion.intent.action.ACTION_FAVORITE_STOCK_INSERT";
	public static final String ACTION_STOCK_EDIT = "orion.intent.action.ACTION_STOCK_EDIT";

	RadioGroup mRadioGroupClass;
	RadioGroup mRadioGroupSE;
	EditText mEditTextStockName;
	EditText mEditTextStockCode;
	EditText mEditTextStockCost;
	EditText mEditTextStockHold;
	EditText mEditTextStockValuation;

	List<String> mListStockOperate;
	ArrayAdapter<String> mArrayAdapter;
	Spinner mSpinnerStockAcion;

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

		if (ACTION_STOCK_EDIT.equals(mAction)) {
			mRadioGroupClass.setEnabled(false);
			mRadioGroupSE.setEnabled(false);
			mEditTextStockCode.setEnabled(false);

			mStock.setId(mIntent.getLongExtra(Constants.EXTRA_STOCK_ID,
					Stock.INVALID_ID));
			mStockDatabaseManager.getStockById(mStock);
			updateView();
		}
	}

	void initView() {
		mRadioGroupClass = (RadioGroup) findViewById(R.id.radioGroupClass);
		mRadioGroupSE = (RadioGroup) findViewById(R.id.radioGroupSE);
		mEditTextStockName = (EditText) findViewById(R.id.edittext_stock_name);
		mEditTextStockCode = (EditText) findViewById(R.id.edittext_stock_code);
		mEditTextStockCost = (EditText) findViewById(R.id.edittext_stock_cost);
		mEditTextStockHold = (EditText) findViewById(R.id.edittext_stock_hold);
		mEditTextStockValuation = (EditText) findViewById(R.id.edittext_stock_valuation);
		mSpinnerStockAcion = (Spinner) findViewById(R.id.spinner_stock_operate);
		mButtonOk = (Button) findViewById(R.id.button_ok);
		mButtonCancel = (Button) findViewById(R.id.button_cancel);

		mRadioGroupClass.setOnClickListener(this);
		mRadioGroupSE.setOnClickListener(this);
		mEditTextStockName.setOnClickListener(this);
		mEditTextStockCode.setOnClickListener(this);
		mEditTextStockCost.setOnClickListener(this);
		mEditTextStockCost.setEnabled(false);
		mEditTextStockHold.setOnClickListener(this);
		mEditTextStockHold.setEnabled(false);
		mEditTextStockValuation.setOnClickListener(this);
		mEditTextStockValuation.setEnabled(false);
		mButtonOk.setOnClickListener(this);
		mButtonCancel.setOnClickListener(this);

		mListStockOperate = new ArrayList<String>();
		mListStockOperate.add("");
		mListStockOperate.add(Settings.KEY_PERIOD_MONTH);
		mListStockOperate.add(Settings.KEY_PERIOD_WEEK);
		mListStockOperate.add(Settings.KEY_PERIOD_DAY);
		mListStockOperate.add(Settings.KEY_PERIOD_MIN60);
		mListStockOperate.add(Settings.KEY_PERIOD_MIN30);
		mListStockOperate.add(Settings.KEY_PERIOD_MIN15);
		mListStockOperate.add(Settings.KEY_PERIOD_MIN5);

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
		if (mStock.getClases().equals(Stock.CLASS_A)) {
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
		mEditTextStockCost.setText(String.valueOf(mStock.getCost()));
		mEditTextStockHold.setText(String.valueOf(mStock.getHold()));
		mEditTextStockValuation.setText(String.valueOf(mStock.getValuation()));

		String operate = mStock.getOperate();
		for (int i = 0; i < mListStockOperate.size(); i++) {
			if (mListStockOperate.get(i).equals(operate)) {
				mSpinnerStockAcion.setSelection(i);
				break;
			}
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
		int viewId = view.getId();

		switch (viewId) {
		case R.id.button_ok:
			int id = 0;

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
			String cost = mEditTextStockCost.getText().toString();

			if (!TextUtils.isEmpty(name)) {
				mStock.setName(name);
			}

			if (!TextUtils.isEmpty(code)) {
				mStock.setCode(code);
			} else {
				Toast.makeText(mContext, R.string.stock_code_empty,
						Toast.LENGTH_LONG).show();
			}

			if (!TextUtils.isEmpty(cost)) {
				mStock.setCost(Double.valueOf(cost));
			} else {
				mStock.setCost(0);
			}

			String operate = mSpinnerStockAcion.getSelectedItem().toString();
			mStock.setOperate(operate);

			mStock.setFlag(Stock.FLAG_FAVORITE);

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
						mStock.getContentValues());
			}

			getIntent().putExtra(Constants.EXTRA_STOCK_ID, mStock.getId());

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
}
