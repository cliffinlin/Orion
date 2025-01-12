package com.android.orion.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.orion.R;
import com.android.orion.setting.StockFilter;

public class StockFilterActivity extends DatabaseActivity implements
		OnClickListener {

	public static final int EXECUTE_STOCK_FILTER_LOAD = 0;
	public static final int EXECUTE_STOCK_FILTER_SAVE = 1;

	CheckBox mCheckBoxEnabled;
	CheckBox mCheckBoxFavorite;

	EditText mEditTextHold;
	EditText mEditTextRoi;
	EditText mEditTextRate;
	EditText mEditTextRoe;
	EditText mEditTextPE;
	EditText mEditTextPB;
	EditText mEditTextDividend;
	EditText mEditTextYield;
	EditText mEditTextDividendRatio;

	Button mButtonOk;
	Button mButtonCancel;

	StockFilter mStockFilter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_filter);

		mStockFilter = new StockFilter(this);
		mStockFilter.read();

		initView();
	}

	void initView() {
		mCheckBoxEnabled = findViewById(R.id.checkbox_enabled);
		mCheckBoxFavorite = findViewById(R.id.checkbox_favorite);

		mEditTextHold = findViewById(R.id.edittext_hold);
		mEditTextRoi = findViewById(R.id.edittext_roi);
		mEditTextRate = findViewById(R.id.edittext_rate);
		mEditTextRoe = findViewById(R.id.edittext_roe);
		mEditTextPE = findViewById(R.id.edittext_pe);
		mEditTextPB = findViewById(R.id.edittext_pb);
		mEditTextDividend = findViewById(R.id.edittext_dividend);
		mEditTextYield = findViewById(R.id.edittext_yield);
		mEditTextDividendRatio = findViewById(R.id.edittext_dividend_ratio);

		mButtonOk = findViewById(R.id.button_ok);
		mButtonCancel = findViewById(R.id.button_cancel);

		mCheckBoxEnabled.setOnClickListener(this);
		mCheckBoxFavorite.setOnClickListener(this);

		mEditTextHold.setOnClickListener(this);
		mEditTextRoi.setOnClickListener(this);
		mEditTextRate.setOnClickListener(this);
		mEditTextRoe.setOnClickListener(this);
		mEditTextPE.setOnClickListener(this);
		mEditTextPB.setOnClickListener(this);
		mEditTextDividend.setOnClickListener(this);
		mEditTextYield.setOnClickListener(this);
		mEditTextDividendRatio.setOnClickListener(this);

		mButtonOk.setOnClickListener(this);
		mButtonCancel.setOnClickListener(this);

		update();
	}

	void update() {
		mCheckBoxEnabled.setChecked(mStockFilter.getEnabled());

		mCheckBoxFavorite.setChecked(mStockFilter.getFavorite());
		mCheckBoxFavorite.setEnabled(mStockFilter.getEnabled());

		mEditTextHold.setText(mStockFilter.getHold());
		mEditTextRoi.setText(mStockFilter.getRoi());
		mEditTextRate.setText(mStockFilter.getRate());
		mEditTextRoe.setText(mStockFilter.getRoe());
		mEditTextPE.setText(mStockFilter.getPe());
		mEditTextPB.setText(mStockFilter.getPb());
		mEditTextDividend.setText(mStockFilter.getDividend());
		mEditTextYield.setText(mStockFilter.getYield());
		mEditTextDividendRatio.setText(mStockFilter.getDividendRatio());

		mEditTextHold.setEnabled(mStockFilter.getEnabled());
		mEditTextRoi.setEnabled(mStockFilter.getEnabled());
		mEditTextRate.setEnabled(mStockFilter.getEnabled());
		mEditTextRoe.setEnabled(mStockFilter.getEnabled());
		mEditTextPE.setEnabled(mStockFilter.getEnabled());
		mEditTextPB.setEnabled(mStockFilter.getEnabled());
		mEditTextDividend.setEnabled(mStockFilter.getEnabled());
		mEditTextYield.setEnabled(mStockFilter.getEnabled());
		mEditTextDividendRatio.setEnabled(mStockFilter.getEnabled());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_deal_edit, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(@NonNull View view) {
		int id = view.getId();

		switch (id) {
			case R.id.checkbox_enabled:
				mStockFilter.setEnabled(mCheckBoxEnabled.isChecked());
				update();
				break;

			case R.id.checkbox_favorite:
				mStockFilter.setFavorite(mCheckBoxFavorite.isChecked());
				update();
				break;

			case R.id.button_ok:
				mStockFilter.setEnabled(mCheckBoxEnabled.isChecked());
				mStockFilter.setFavorite(mCheckBoxFavorite.isChecked());

				mStockFilter.setHold(mEditTextHold.getText().toString());
				mStockFilter.setRoi(mEditTextRoi.getText().toString());
				mStockFilter.setRate(mEditTextRate.getText().toString());
				mStockFilter.setRoe(mEditTextRoe.getText().toString());
				mStockFilter.setPe(mEditTextPE.getText().toString());
				mStockFilter.setPb(mEditTextPB.getText().toString());
				mStockFilter.setDividend(mEditTextDividend.getText().toString());
				mStockFilter.setYield(mEditTextYield.getText().toString());
				mStockFilter.setDividendRatio(mEditTextDividendRatio.getText()
						.toString());

				mStockFilter.write();

				Bundle bundle = new Bundle();
				mStockFilter.put(bundle);
				mIntent.putExtras(bundle);
				setResult(RESULT_OK, mIntent);
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
