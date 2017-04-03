package com.android.orion;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.ArrayMap;

import com.android.orion.OrionService.OrionServiceBinder;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDatabaseManager;

public class OrionBaseActivity extends Activity {
	boolean mBound = false;
	boolean mResumed = false;

	Context mContext = null;

	String mAction = null;
	Intent mIntent = null;

	ProgressDialog mProgressDialog = null;

	ContentResolver mContentResolver = null;
	LoaderManager mLoaderManager = null;

	Stock mStock = null;
	StockData mStockData = null;

	ArrayList<Stock> mStockList = null;

	ArrayMap<String, Stock> mStockDealArrayMap = null;

	SharedPreferences mSharedPreferences = null;
	StockDatabaseManager mStockDatabaseManager = null;

	OrionService mOrionService = null;

	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder iBinder) {
			OrionServiceBinder orionServiceBinder = (OrionServiceBinder) iBinder;
			if (orionServiceBinder != null) {
				mOrionService = orionServiceBinder.getService();
				mBound = true;
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBound = false;
			mOrionService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		mIntent = getIntent();
		if (mIntent != null) {
			mAction = mIntent.getAction();
		}

		if (mContentResolver == null) {
			mContentResolver = getContentResolver();
		}

		if (mLoaderManager == null) {
			mLoaderManager = getLoaderManager();
		}

		if (mStock == null) {
			mStock = Stock.obtain();
		}

		if (mStockList == null) {
			mStockList = new ArrayList<Stock>();
		}

		if (mStockDealArrayMap == null) {
			mStockDealArrayMap = new ArrayMap<String, Stock>();
		}

		if (mStockData == null) {
			mStockData = StockData.obtain();
		}

		if (mSharedPreferences == null) {
			mSharedPreferences = getSharedPreferences(
					Constants.SETTING_SHARED_PREFERENCE, MODE_PRIVATE);
		}

		if (mStockDatabaseManager == null) {
			mStockDatabaseManager = StockDatabaseManager.getInstance(this);
		}

		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(mContext,
					ProgressDialog.THEME_HOLO_LIGHT);
		}

		bindService();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unbindService();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mResumed = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		mResumed = true;
	}

	public void showProgressDialog(String content) {
		if (mProgressDialog != null) {
			mProgressDialog.setMessage(content);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
		}
	}

	public void hideProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}

	void bindService() {
		Intent intent = new Intent(this, OrionService.class);
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	void unbindService() {
		if (mBound) {
			unbindService(mServiceConnection);
			mBound = false;
			mOrionService = null;
		}
	}

	void startService(int serviceType, int executeType) {
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.EXTRA_KEY_SERVICE_TYPE, serviceType);
		bundle.putInt(Constants.EXTRA_KEY_EXECUTE_TYPE, executeType);
		startService(bundle);
	}

	void startService(int serviceType, int executeType, String se, String code) {
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.EXTRA_KEY_SERVICE_TYPE, serviceType);
		bundle.putInt(Constants.EXTRA_KEY_EXECUTE_TYPE, executeType);
		bundle.putString(Constants.EXTRA_KEY_STOCK_SE, se);
		bundle.putString(Constants.EXTRA_KEY_STOCK_CODE, code);
		startService(bundle);
	}

	void startService(int serviceType, int executeType, String se, String code,
			double stockDealPrice, long stockDealVolume) {
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.EXTRA_KEY_SERVICE_TYPE, serviceType);
		bundle.putInt(Constants.EXTRA_KEY_EXECUTE_TYPE, executeType);
		bundle.putString(Constants.EXTRA_KEY_STOCK_SE, se);
		bundle.putString(Constants.EXTRA_KEY_STOCK_CODE, code);
		bundle.putDouble(Constants.EXTRA_KEY_STOCK_DEAL_PRICE, stockDealPrice);
		bundle.putLong(Constants.EXTRA_KEY_STOCK_DEAL_VOLUME, stockDealVolume);
		startService(bundle);
	}

	void startService(Bundle bundle) {
		Intent intent = new Intent(this, OrionService.class);
		intent.putExtras(bundle);
		startService(intent);
	}

	public String getSetting(String key, String defaultValue) {
		String value = "";

		if (mSharedPreferences != null) {
			value = mSharedPreferences.getString(key, defaultValue);
		}
		return value;
	}

	public void saveSetting(String key, String value) {
		if (mSharedPreferences != null) {
			Editor editor = mSharedPreferences.edit();
			editor.putString(key, value);
			editor.commit();
		}
	}

	//
	// String getSettingFromDatabase(String key, String defaultValue) {
	// String value = "";
	//
	// if (mSettingDatabaseManager != null) {
	// try {
	// value = mSettingDatabaseManager.query(key, defaultValue);
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// }
	// }
	//
	// return value;
	// }
	//
	// void saveSettingToDatabase(String key, String value) {
	// if (mSettingDatabaseManager != null) {
	// try {
	// mSettingDatabaseManager.save(key, value);
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// }
	// }
	// }

	void updateStockMark(long stockId, String mark) {
		Uri uri = null;

		uri = ContentUris.withAppendedId(DatabaseContract.Stock.CONTENT_URI,
				stockId);

		try {
			ContentValues contentValues = new ContentValues();
			contentValues.put(DatabaseContract.Stock.COLUMN_MARK, mark);
			mContentResolver.update(uri, contentValues, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void deleteStockData(long stockId) {
		Uri uri = DatabaseContract.StockData.CONTENT_URI;
		String where = null;

		if (stockId > 0) {
			where = DatabaseContract.COLUMN_STOCK_ID + "=" + stockId;
		}

		try {
			mContentResolver.delete(uri, where, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void showSaveSDAlertDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.title_save_to_sd_card)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						onSaveSD();
					}
				}).setNegativeButton(R.string.cancel, new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				}).create().show();
	}

	void onSaveSD() {
	}

	void loadStockDealArrayMap() {
		loadStockDealArrayMap("1) GROUP BY (" + DatabaseContract.COLUMN_NAME,
				null, null, mStockDealArrayMap);
	}

	void loadStockDealArrayMap(String selection, String[] selectionArgs,
			String sortOrder, ArrayMap<String, Stock> stockArrayMap) {
		Cursor cursor = null;

		if ((mStockDatabaseManager == null) || (stockArrayMap == null)) {
			return;
		}

		try {
			stockArrayMap.clear();
			cursor = mStockDatabaseManager.queryStockDeal(selection,
					selectionArgs, sortOrder);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					Stock stock = Stock.obtain();
					stock.setSE(cursor);
					stock.setCode(cursor);
					stock.setName(cursor);
					stockArrayMap.put(stock.getSE() + stock.getCode(), stock);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}
	}
}
