package com.android.orion;

import java.util.ArrayList;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.ArrayMap;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.FinancialData;
import com.android.orion.database.Setting;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDatabaseManager;
import com.android.orion.database.StockDeal;
import com.android.orion.utility.Preferences;

public class OrionBaseActivity extends Activity {

	boolean mStockFilter = false;

	String mStockFilterPE = "";
	String mStockFilterPB = "";
	String mStockFilterDividend = "";
	String mStockFilterYield = "";
	String mStockFilterDelta = "";
	
	boolean mBound = false;
	boolean mResumed = false;

	String mPathName = null;
	String mFileName = null;

	Context mContext = null;

	Bundle mBundle = null;
	String mAction = null;
	Intent mIntent = null;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				Constants.TAG);

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
			mStock = Stock.obtain();
		}

		if (mStockData == null) {
			mStockData = StockData.obtain();
		}

		if (mStockDeal == null) {
			mStockDeal = StockDeal.obtain();
		}

		if (mFinancialData == null) {
			mFinancialData = FinancialData.obtain();
		}

		if (mShareBonus == null) {
			mShareBonus = ShareBonus.obtain();
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

	void loadSetting() {
		mStockFilter = Preferences.readBoolean(this, Setting.KEY_STOCK_FILTER,
				false);

		mStockFilterPE = Preferences.readString(this,
				Setting.KEY_STOCK_FILTER_PE, "");
		mStockFilterPB = Preferences.readString(this,
				Setting.KEY_STOCK_FILTER_PB, "");
		mStockFilterDividend = Preferences.readString(this,
				Setting.KEY_STOCK_FILTER_DIVIDEND, "");
		mStockFilterYield = Preferences.readString(this,
				Setting.KEY_STOCK_FILTER_YIELD, "");
		mStockFilterDelta = Preferences.readString(this,
				Setting.KEY_STOCK_FILTER_DELTA, "");
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

	void startService(int serviceType, int executeType, String se, String code,
			double stockDealPrice, long stockDealVolume) {
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.EXTRA_SERVICE_TYPE, serviceType);
		bundle.putInt(Constants.EXTRA_EXECUTE_TYPE, executeType);
		bundle.putString(Constants.EXTRA_STOCK_SE, se);
		bundle.putString(Constants.EXTRA_STOCK_CODE, code);
		bundle.putDouble(Constants.EXTRA_STOCK_DEAL_PRICE, stockDealPrice);
		bundle.putLong(Constants.EXTRA_STOCK_DEAL_VOLUME, stockDealVolume);
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

	void updateStockAction(long stockId, String action) {
		Uri uri = null;

		uri = ContentUris.withAppendedId(DatabaseContract.Stock.CONTENT_URI,
				stockId);

		try {
			ContentValues contentValues = new ContentValues();
			for (String period : Constants.PERIODS) {
				contentValues.put(period, action);
			}
			mContentResolver.update(uri, contentValues, null, null);
		} catch (Exception e) {
			e.printStackTrace();
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
