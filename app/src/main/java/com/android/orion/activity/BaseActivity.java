package com.android.orion.activity;

import android.Manifest;
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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.orion.R;
import com.android.orion.config.Config;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDeal;
import com.android.orion.database.StockFinancial;
import com.android.orion.database.StockQuant;
import com.android.orion.database.TotalShare;
import com.android.orion.manager.DatabaseManager;
import com.android.orion.manager.StockManager;
import com.android.orion.service.StockService;
import com.android.orion.service.StockService.StockServiceBinder;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Utility;

import java.util.ArrayList;

public class BaseActivity extends Activity {

	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static final String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE",
			"android.permission.WRITE_EXTERNAL_STORAGE"};
	Logger Log = Logger.getLogger();
	boolean mResumed = false;
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
	ArrayList<Stock> mStockList = new ArrayList<Stock>();
	ArrayList<StockData> mStockDataList = null;
	ArrayList<StockDeal> mStockDealList = null;
	ArrayList<StockQuant> mStockQuantList = null;
	ArrayList<StockFinancial> mStockFinancialList = null;
	ArrayList<ShareBonus> mShareBonusList = null;
	ArrayList<TotalShare> mTotalShareList = null;
	ArrayMap<String, Stock> mStockDealArrayMap = null;
	StockManager mStockManager = StockManager.getInstance();
	DatabaseManager mDatabaseManager;
	StockService mStockService = null;
	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			if (binder == null) {
				return;
			}

			StockServiceBinder mStockServiceBinder = (StockServiceBinder) binder;

			mStockService = mStockServiceBinder.getService();

			BaseActivity.this.onServiceConnected();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mStockService = null;
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

				if (TextUtils.equals(action, Constant.ACTION_RESTART_LOADER)) {
					restartLoader(intent);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		bindService(new Intent(this, StockService.class), mServiceConnection,
				Context.BIND_AUTO_CREATE);

		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				Config.TAG + ":" + BaseActivity.class.getSimpleName());

		LocalBroadcastManager.getInstance(this).registerReceiver(
				mBroadcastReceiver,
				new IntentFilter(Constant.ACTION_RESTART_LOADER));

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

		if (mStockDataList == null) {
			mStockDataList = new ArrayList<StockData>();
		}

		if (mStockDealList == null) {
			mStockDealList = new ArrayList<StockDeal>();
		}

		if (mStockQuantList == null) {
			mStockQuantList = new ArrayList<StockQuant>();
		}

		if (mStockFinancialList == null) {
			mStockFinancialList = new ArrayList<StockFinancial>();
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

		mDatabaseManager = DatabaseManager.getInstance();

		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(mContext,
					ProgressDialog.THEME_HOLO_LIGHT);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mResumed = false;
	}

	@Override
	protected void onResume() {
		super.onResume();

		checkPermission();
		mResumed = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mBroadcastReceiver);

		unbindService(mServiceConnection);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getActionBar().setDisplayHomeAsUpEnabled(true);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;

			default:
				return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		setIntent(intent);

		mIntent = getIntent();
		if (mIntent != null) {
			mAction = mIntent.getAction();
			mBundle = mIntent.getExtras();
		}
	}

	void onServiceConnected() {
		if (Utility.isNetworkConnected(this)) {
			if (mStockService != null) {
				mStockService.download();
			}
		} else {
			Toast.makeText(this,
					getResources().getString(R.string.network_unavailable),
					Toast.LENGTH_SHORT).show();
		}
	}

	void restartLoader(Intent intent) {
	}

	void onMessageRefresh(Stock stock) {
		if (stock == null) {
			return;
		}

		Setting.setDownloadTimemillis(stock.getSE(), stock.getCode(), 0);

		stock.reset();
		mDatabaseManager.updateStock(stock, stock.getContentValues());

		mDatabaseManager.deleteStockData(stock.getId());
		mDatabaseManager.deleteStockFinancial(stock.getId());
		mDatabaseManager.deleteShareBonus(stock.getId());
		mDatabaseManager.deleteStockQuant(stock);

		if (mStockService != null) {
			mStockService.download(stock);
		}
	}

	private void checkPermission() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
					.WRITE_EXTERNAL_STORAGE)) {
				Toast.makeText(this, "请开通相关权限！", Toast.LENGTH_SHORT).show();
			}

			ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case REQUEST_EXTERNAL_STORAGE: {
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(this, "授权被拒绝！", Toast.LENGTH_SHORT).show();
				}
				return;
			}
		}
	}
}
