package com.android.orion;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.android.orion.database.Stock;
import com.android.orion.database.StockMatch;
import com.android.orion.utility.Utility;

public class StockMatchActivity extends DatabaseActivity implements
		OnClickListener {

	public static final String ACTION_MATCH_INSERT = "orion.intent.action.ACTION_MATCH_INSERT";
	public static final String ACTION_MATCH_EDIT = "orion.intent.action.ACTION_MATCH_EDIT";

	public static final String EXTRA_MATCH_ID = "match_id";

	static final int EXECUTE_MATCH_LOAD = 1;
	static final int EXECUTE_MATCH_SAVE = 2;
	static final int EXECUTE_STOCK_X_LOAD = 3;
	static final int EXECUTE_STOCK_Y_LOAD = 4;

	static final int REQUEST_CODE_STOCK_X_ID = 0;
	static final int REQUEST_CODE_STOCK_Y_ID = 1;

	EditText mEditTextStockName_X, mEditTextStockCode_X;
	EditText mEditTextStockName_Y, mEditTextStockCode_Y;

	Button mButtonOk, mButtonCancel;

	StockMatch mMatch = null;
	Stock mStock_X = null;
	Stock mStock_Y = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_match);

		if (mMatch == null) {
			mMatch = StockMatch.obtain();
		}

		if (mStock_X == null) {
			mStock_X = Stock.obtain();
		}

		if (mStock_Y == null) {
			mStock_Y = Stock.obtain();
		}

		initView();

		if (ACTION_MATCH_EDIT.equals(mAction)) {
			mMatch.setId(mIntent.getLongExtra(EXTRA_MATCH_ID, 0));
			startLoadTask(EXECUTE_MATCH_LOAD);
		}
	}

	void initView() {
		mEditTextStockName_X = (EditText) findViewById(R.id.edittext_stock_name_x);
		mEditTextStockCode_X = (EditText) findViewById(R.id.edittext_stock_code_x);
		mEditTextStockName_Y = (EditText) findViewById(R.id.edittext_stock_name_y);
		mEditTextStockCode_Y = (EditText) findViewById(R.id.edittext_stock_code_y);
		mButtonOk = (Button) findViewById(R.id.button_ok);
		mButtonCancel = (Button) findViewById(R.id.button_cancel);

		mEditTextStockName_X.setOnClickListener(this);
		mEditTextStockCode_X.setOnClickListener(this);
		mEditTextStockName_Y.setOnClickListener(this);
		mEditTextStockCode_Y.setOnClickListener(this);
		mButtonOk.setOnClickListener(this);
		mButtonCancel.setOnClickListener(this);

		mEditTextStockName_X.setInputType(InputType.TYPE_NULL);
		mEditTextStockName_X.setFocusable(false);
		mEditTextStockCode_X.setInputType(InputType.TYPE_NULL);
		mEditTextStockCode_X.setFocusable(false);
		mEditTextStockName_Y.setInputType(InputType.TYPE_NULL);
		mEditTextStockName_Y.setFocusable(false);
		mEditTextStockCode_Y.setInputType(InputType.TYPE_NULL);
		mEditTextStockCode_Y.setFocusable(false);

		if (ACTION_MATCH_INSERT.equals(mAction)) {
			setTitle(R.string.match_insert);
		} else if (ACTION_MATCH_EDIT.equals(mAction)) {
			setTitle(R.string.match_edit);
		}
	}

	void updateView() {
		mEditTextStockName_X.setText(mMatch.getName_X());
		mEditTextStockCode_X.setText(mMatch.getCode_X());
		mEditTextStockName_Y.setText(mMatch.getName_Y());
		mEditTextStockCode_Y.setText(mMatch.getCode_Y());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_match_edit, menu);
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
		case R.id.edittext_stock_name_x:
		case R.id.edittext_stock_code_x:
			if (ACTION_MATCH_INSERT.equals(mAction)) {
				Intent intent = new Intent(this,
						StockFavoriteListActivity.class);
				intent.setAction(StockFavoriteListActivity.ACTION_STOCK_ID);
				startActivityForResult(intent, REQUEST_CODE_STOCK_X_ID);
			}
			break;
		case R.id.edittext_stock_name_y:
		case R.id.edittext_stock_code_y:
			if (ACTION_MATCH_INSERT.equals(mAction)) {
				Intent intent = new Intent(this,
						StockFavoriteListActivity.class);
				intent.setAction(StockFavoriteListActivity.ACTION_STOCK_ID);
				startActivityForResult(intent, REQUEST_CODE_STOCK_Y_ID);
			}
			break;
		case R.id.button_ok:
			startSaveTask(EXECUTE_MATCH_SAVE);
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
			case REQUEST_CODE_STOCK_X_ID:
				if (mStock_X != null) {
					mStock_X.setId(data.getLongExtra(
							StockFavoriteListActivity.EXTRA_STOCK_ID, 0));
					startLoadTask(EXECUTE_STOCK_X_LOAD);
				}
				break;
			case REQUEST_CODE_STOCK_Y_ID:
				if (mStock_Y != null) {
					mStock_Y.setId(data.getLongExtra(
							StockFavoriteListActivity.EXTRA_STOCK_ID, 0));
					startLoadTask(EXECUTE_STOCK_Y_LOAD);
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
		case EXECUTE_STOCK_X_LOAD:
			mStockDatabaseManager.getStockById(mStock_X);

			mMatch.setSE_X(mStock_X.getSE());
			mMatch.setCode_X(mStock_X.getCode());
			mMatch.setName_X(mStock_X.getName());
			break;

		case EXECUTE_STOCK_Y_LOAD:
			mStockDatabaseManager.getStockById(mStock_Y);

			mMatch.setSE_X(mStock_Y.getSE());
			mMatch.setCode_X(mStock_Y.getCode());
			mMatch.setName_X(mStock_Y.getName());
			break;

		case EXECUTE_MATCH_LOAD:
			mStockDatabaseManager.getStockMatchById(mMatch);
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
		case EXECUTE_MATCH_SAVE:
			if (ACTION_MATCH_INSERT.equals(mAction)) {
				mMatch.setCreated(Utility.getCurrentDateTimeString());
				mStockDatabaseManager.insertStockMatch(mMatch);
			} else if (ACTION_MATCH_EDIT.equals(mAction)) {
				mMatch.setModified(Utility.getCurrentDateTimeString());
				mStockDatabaseManager.updateStockMatchByID(mMatch);
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
