package com.android.orion;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.android.orion.database.Deal;
import com.android.orion.database.Stock;
import com.android.orion.utility.Utility;

public class DealActivity extends DatabaseActivity implements OnClickListener {

	public static final String ACTION_DEAL_INSERT = "orion.intent.action.ACTION_DEAL_INSERT";
	public static final String ACTION_DEAL_EDIT = "orion.intent.action.ACTION_DEAL_EDIT";

	public static final String EXTRA_DEAL_ID = "deal_id";

	static final int EXECUTE_STOCK_LOAD = 1;
	static final int EXECUTE_DEAL_LOAD = 2;
	static final int EXECUTE_DEAL_SAVE = 3;

	static final int REQUESTCODE_STOCK_ID = 0;

	EditText mEditTextStockName, mEditTextStockCode;
	EditText mEditTextDealPrice, mEditTextDealVolume;

	Button mButtonOk, mButtonCancel;

	Deal mDeal = null;
	Stock mStock = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deal);

		if (mDeal == null) {
			mDeal = Deal.obtain();
		}

		if (mStock == null) {
			mStock = Stock.obtain();
		}

		initView();

		if (ACTION_DEAL_EDIT.equals(mAction)) {
			mDeal.setId(mIntent.getLongExtra(EXTRA_DEAL_ID, 0));
			startLoadTask(EXECUTE_DEAL_LOAD);
		}
	}

	void initView() {
		mEditTextStockName = (EditText) findViewById(R.id.edittext_stock_name);
		mEditTextStockCode = (EditText) findViewById(R.id.edittext_stock_code);
		mEditTextDealPrice = (EditText) findViewById(R.id.edittext_deal_price);
		mEditTextDealVolume = (EditText) findViewById(R.id.edittext_deal_volume);
		mButtonOk = (Button) findViewById(R.id.button_ok);
		mButtonCancel = (Button) findViewById(R.id.button_cancel);

		mEditTextStockName.setOnClickListener(this);
		mEditTextStockCode.setOnClickListener(this);
		mEditTextDealPrice.setOnClickListener(this);
		mEditTextDealVolume.setOnClickListener(this);
		mButtonOk.setOnClickListener(this);
		mButtonCancel.setOnClickListener(this);

		mEditTextStockName.setInputType(InputType.TYPE_NULL);
		mEditTextStockName.setFocusable(false);
		mEditTextStockCode.setInputType(InputType.TYPE_NULL);
		mEditTextStockCode.setFocusable(false);

		if (ACTION_DEAL_INSERT.equals(mAction)) {
			setTitle(R.string.deal_insert);
		} else if (ACTION_DEAL_EDIT.equals(mAction)) {
			setTitle(R.string.deal_edit);
		}
	}

	void updateView() {
		mEditTextStockName.setText(mDeal.getName());
		mEditTextStockCode.setText(mDeal.getCode());
		mEditTextDealPrice.setText(String.valueOf(mDeal.getDeal()));
		mEditTextDealVolume.setText(String.valueOf(mDeal.getVolume()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.deal_edit, menu);
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
		int id = view.getId();

		switch (id) {
		case R.id.edittext_stock_name:
		case R.id.edittext_stock_code:
			if (ACTION_DEAL_INSERT.equals(mAction)) {
				Intent intent = new Intent(this,
						StockFavoriteListActivity.class);
				intent.setAction(StockFavoriteListActivity.ACTION_STOCK_ID);
				startActivityForResult(intent, REQUESTCODE_STOCK_ID);
			}
			break;
		case R.id.button_ok:
			String dealString = "";
			String volumeString = "";

			dealString = mEditTextDealPrice.getText().toString();
			if (!TextUtils.isEmpty(dealString)) {
				mDeal.setDeal(Double.valueOf(dealString));
			} else {
				mDeal.setDeal(0);
			}

			volumeString = mEditTextDealVolume.getText().toString();
			if (!TextUtils.isEmpty(volumeString)) {
				mDeal.setVolume(Long.valueOf(volumeString));
			} else {
				mDeal.setVolume(0);
			}
			mDeal.setupDeal();
			startSaveTask(EXECUTE_DEAL_SAVE);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUESTCODE_STOCK_ID:
				if (mStock != null) {
					mStock.setId(data.getLongExtra(
							StockFavoriteListActivity.EXTRA_STOCK_ID, 0));
					startLoadTask(EXECUTE_STOCK_LOAD);
				}
				break;

			default:
				break;
			}
		}
	}

	@Override
	Long doInBackgroundLoad(Object... params) {
		super.doInBackgroundLoad(params);
		int execute = (Integer) params[0];

		switch (execute) {
		case EXECUTE_STOCK_LOAD:
			mStockDatabaseManager.getStockById(mStock);

			mDeal.setSE(mStock.getSE());
			mDeal.setCode(mStock.getCode());
			mDeal.setName(mStock.getName());
			mDeal.setPrice(mStock.getPrice());
			mDeal.setDeal(mStock.getPrice());
			break;

		case EXECUTE_DEAL_LOAD:
			mStockDatabaseManager.getDealById(mDeal);
			break;

		default:
			break;
		}

		return RESULT_SUCCESS;
	}

	@Override
	void onPostExecuteLoad(Long result) {
		super.onPostExecuteLoad(result);
		updateView();
	}

	@Override
	Long doInBackgroundSave(Object... params) {
		super.doInBackgroundSave(params);
		int execute = (Integer) params[0];

		switch (execute) {
		case EXECUTE_DEAL_SAVE:
			if (ACTION_DEAL_INSERT.equals(mAction)) {
				mDeal.setCreated(Utility.getCurrentDateTimeString());
				mStockDatabaseManager.insertDeal(mDeal);
			} else if (ACTION_DEAL_EDIT.equals(mAction)) {
				mDeal.setModified(Utility.getCurrentDateTimeString());
				mStockDatabaseManager.updateDealByID(mDeal);
			}
			break;

		default:
			break;
		}

		return RESULT_SUCCESS;
	}

	@Override
	void onPostExecuteSave(Long result) {
		super.onPostExecuteSave(result);
	}
}
