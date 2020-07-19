package com.android.orion;

import java.util.ArrayList;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.ArrayMap;
import android.util.Log;

import com.android.orion.OrionService.OrionBinder;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.FinancialData;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDatabaseManager;
import com.android.orion.database.StockDeal;
import com.android.orion.database.StockFilter;

public class OrionBaseActivity extends Activity {
	static final String TAG = Constants.TAG + " "
			+ OrionBaseActivity.class.getSimpleName();

	boolean mBound = false;
	boolean mResumed = false;

	long mLastRestartLoader = 0;

	String mPathName = null;
	String mFileName = null;

	Context mContext = null;

	Bundle mBundle = null;
	String mAction = null;
	Intent mIntent = null;

	StockFilter mStockFilter;

	PowerManager mPowerManager;
	WakeLock mWakeLock;

	ProgressDialog mProgressDialog = null;

	ContentResolver mContentResolver = null;
	LoaderManager mLoaderManager = null;

	Stock mStock = null;
	StockData mStockData = null;
	StockDeal mStockDeal = null;
	FinancialData mFinancialData = null;
	ShareBonus mShareBonus = null;

	ArrayList<Stock> mStockList = null;
	ArrayList<StockData> mStockDataList = null;
	ArrayList<StockDeal> mStockDealList = null;
	ArrayList<FinancialData> mFinancialDataList = null;
	ArrayList<ShareBonus> mShareBonusList = null;

	ArrayMap<String, Stock> mStockDealArrayMap = null;

	SharedPreferences mSharedPreferences = null;
	StockDatabaseManager mStockDatabaseManager = null;

	OrionBinder mOrionBinder = null;

	OrionService mOrionService = null;

	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			if (binder == null) {
				return;
			}

			OrionBinder mOrionBinder = (OrionBinder) binder;

			mOrionService = mOrionBinder.getService();

			OrionBaseActivity.this.onServiceConnected();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mOrionService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		bindService(new Intent(this, OrionService.class), mServiceConnection,
				Context.BIND_AUTO_CREATE);

		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				Constants.TAG);

		mStockFilter = new StockFilter(mContext);

		mIntent = getIntent();
		if (mIntent != null) {
			mAction = mIntent.getAction();
			mBundle = mIntent.getExtras();
		}

		if (mContentResolver == null) {
			mContentResolver = getContentResolver();
		}

		if (mLoaderManager == null) {
			mLoaderManager = getLoaderManager();
		}

		if (mStock == null) {
			mStock = new Stock();
		}

		if (mStockData == null) {
			mStockData = new StockData();
		}

		if (mStockDeal == null) {
			mStockDeal = new StockDeal();
		}

		if (mFinancialData == null) {
			mFinancialData = new FinancialData();
		}

		if (mShareBonus == null) {
			mShareBonus = new ShareBonus();
		}

		if (mStockList == null) {
			mStockList = new ArrayList<Stock>();
		}

		if (mStockDataList == null) {
			mStockDataList = new ArrayList<StockData>();
		}

		if (mStockDealList == null) {
			mStockDealList = new ArrayList<StockDeal>();
		}

		if (mFinancialDataList == null) {
			mFinancialDataList = new ArrayList<FinancialData>();
		}

		if (mShareBonusList == null) {
			mShareBonusList = new ArrayList<ShareBonus>();
		}

		if (mStockDealArrayMap == null) {
			mStockDealArrayMap = new ArrayMap<String, Stock>();
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
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unbindService(mServiceConnection);
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

	void onServiceConnected() {
	}

	void acquireWakeLock() {
		Log.d(TAG, "acquireWakeLock");
		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire();
			Log.d(TAG, "acquireWakeLock, mWakeLock acquired.");
		}
	}

	void releaseWakeLock() {
		Log.d(TAG, "releaseWakeLock");
		if (mWakeLock.isHeld()) {
			mWakeLock.release();
			Log.d(TAG, "releaseWakeLock, mWakeLock released.");
		}
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
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	}

	void startService(int serviceType, int executeType) {
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.EXTRA_SERVICE_TYPE, serviceType);
		bundle.putInt(Constants.EXTRA_EXECUTE_TYPE, executeType);
		startService(bundle);
	}

	void startService(int serviceType, int executeType, String se, String code) {
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.EXTRA_SERVICE_TYPE, serviceType);
		bundle.putInt(Constants.EXTRA_EXECUTE_TYPE, executeType);
		bundle.putString(Constants.EXTRA_STOCK_SE, se);
		bundle.putString(Constants.EXTRA_STOCK_CODE, code);
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
					Stock stock = new Stock();
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
