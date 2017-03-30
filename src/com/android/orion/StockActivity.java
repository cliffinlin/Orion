package com.android.orion;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.orion.database.Stock;
import com.android.orion.utility.Utility;

public class StockActivity extends DatabaseActivity implements OnClickListener {

	public static final String ACTION_STOCK_INSERT = "orion.intent.action.ACTION_STOCKINSERT";
	public static final String ACTION_STOCK_EDIT = "orion.intent.action.ACTION_STOCK_EDIT";

	public static final String EXTRA_STOCK_ID = "stock_id";

	public static final int EXECUTE_STOCK_LOAD = 1;
	public static final int EXECUTE_STOCK_SAVE = 2;

	public static final long RESULT_STOCK_EXIST = -2;

	EditText mEditTextStockSE, mEditTextStockName, mEditTextStockCode;
	Button mButtonOk, mButtonCancel;

	Stock mStock = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock);

		if (mStock == null) {
			mStock = Stock.obtain();
		}

		initView();

		if (ACTION_STOCK_EDIT.equals(mAction)) {
			mStock.setId(mIntent.getLongExtra(EXTRA_STOCK_ID, 0));
			startLoadTask(EXECUTE_STOCK_LOAD);
		}
	}

	void initView() {
		mEditTextStockSE = (EditText) findViewById(R.id.edittext_stock_se);
		mEditTextStockName = (EditText) findViewById(R.id.edittext_stock_name);
		mEditTextStockCode = (EditText) findViewById(R.id.edittext_stock_code);
		mButtonOk = (Button) findViewById(R.id.button_ok);
		mButtonCancel = (Button) findViewById(R.id.button_cancel);

		mEditTextStockSE.setOnClickListener(this);
		mEditTextStockName.setOnClickListener(this);
		mEditTextStockCode.setOnClickListener(this);
		mButtonOk.setOnClickListener(this);
		mButtonCancel.setOnClickListener(this);

		if (ACTION_STOCK_INSERT.equals(mAction)) {
			setTitle(R.string.stock_insert);
		} else if (ACTION_STOCK_EDIT.equals(mAction)) {
			setTitle(R.string.stock_edit);
		}
	}

	void updateView() {
		mEditTextStockSE.setText(mStock.getSE());
		mEditTextStockSE.setText(mStock.getSE());
		mEditTextStockName.setText(mStock.getName());
		mEditTextStockCode.setText(mStock.getCode());
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
		int id = view.getId();

		switch (id) {
		case R.id.button_ok:
			String se = mEditTextStockSE.getText().toString();
			String name = mEditTextStockName.getText().toString();
			String code = mEditTextStockCode.getText().toString();

			mStock.setClasses(Constants.STOCK_FLAG_CLASS_HSA);

			if (Constants.STOCK_SE_SH.equals(se)
					|| Constants.STOCK_SE_SZ.equals(se)) {
				mStock.setSE(se);
			} else {
				Toast.makeText(mContext, R.string.stock_se_not_found,
						Toast.LENGTH_LONG).show();
			}

			if (!TextUtils.isEmpty(name)) {
				mStock.setName(name);
			}

			if (!TextUtils.isEmpty(code)) {
				mStock.setCode(code);
			} else {
				Toast.makeText(mContext, R.string.stock_code_empty,
						Toast.LENGTH_LONG).show();
			}
			mStock.setMark(Constants.STOCK_FLAG_MARK_FAVORITE);
			if (ACTION_STOCK_INSERT.equals(mAction)) {
				mStock.setCreated(Utility.getCurrentDateTimeString());
			} else if (ACTION_STOCK_EDIT.equals(mAction)) {
				mStock.setModified(Utility.getCurrentDateTimeString());
			}
			startSaveTask(EXECUTE_STOCK_SAVE);
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
	Long doInBackgroundLoad(Object... params) {
		super.doInBackgroundLoad(params);
		int execute = (Integer) params[0];

		switch (execute) {
		case EXECUTE_STOCK_LOAD:
			mStockDatabaseManager.getStockById(mStock);
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
		case EXECUTE_STOCK_SAVE:
			if (ACTION_STOCK_INSERT.equals(mAction)) {
				if (!mStockDatabaseManager.isStockExist(mStock)) {
					mStockDatabaseManager.insertStock(mStock);
				} else {
					return RESULT_STOCK_EXIST;
				}
			} else if (ACTION_STOCK_EDIT.equals(mAction)) {
				mStockDatabaseManager.updateStock(mStock,
						mStock.getContentValues());
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

		if (result == RESULT_STOCK_EXIST) {
			Toast.makeText(mContext, R.string.stock_exist, Toast.LENGTH_LONG)
					.show();
		}
	}
}
