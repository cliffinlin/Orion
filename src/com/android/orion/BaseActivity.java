package com.android.orion;

import java.util.ArrayList;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.ArrayMap;
import android.util.Log;

import com.android.orion.OrionService.OrionBinder;
import com.android.orion.database.FinancialData;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDatabaseManager;
import com.android.orion.database.StockDeal;
import com.android.orion.database.TotalShare;

public class BaseActivity extends Activity {
	static final String TAG = Constants.TAG + " "
			+ BaseActivity.class.getSimpleName();

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
	TotalShare mTotalShare = null;

	ArrayList<Stock> mStockList = null;
	ArrayList<StockData> mStockDataList = null;
	ArrayList<StockDeal> mStockDealList = null;
	ArrayList<FinancialData> mFinancialDataList = null;
	ArrayList<ShareBonus> mShareBonusList = null;
	ArrayList<TotalShare> mTotalShareList = null;

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

			BaseActivity.this.onServiceConnected();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mOrionService = null;
		}
	};

	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null) {
				return;
			}

			if (mResumed) {
				String action = intent.getAction();

				if (Constants.ACTION_RESTART_LOADER.equals(action)) {
					if (System.currentTimeMillis() - mLastRestartLoader > Constants.DEFAULT_RESTART_LOADER_INTERVAL) {
						mLastRestartLoader = System.currentTimeMillis();
						restartLoader(intent);
					}
				}
			}
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
				Constants.TAG + ":" + BaseActivity.class.getSimpleName());

		LocalBroadcastManager.getInstance(this).registerReceiver(
				mBroadcastReceiver,
				new IntentFilter(Constants.ACTION_RESTART_LOADER));

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

		if (mTotalShare == null) {
			mTotalShare = new TotalShare();
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

		if (mTotalShareList == null) {
			mTotalShareList = new ArrayList<TotalShare>();
		}

		if (mStockDealArrayMap == null) {
			mStockDealArrayMap = new ArrayMap<String, Stock>();
		}

		if (mSharedPreferences == null) {
			mSharedPreferences = getSharedPreferences(
					Settings.SHARED_PREFERENCE, MODE_PRIVATE);
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

		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mBroadcastReceiver);

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

	void restartLoader(Intent intent) {
	}
}
