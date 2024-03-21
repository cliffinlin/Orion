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
import android.util.ArrayMap;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.orion.R;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDeal;
import com.android.orion.database.StockFinancial;
import com.android.orion.database.StockQuant;
import com.android.orion.database.TotalShare;
import com.android.orion.manager.StockDatabaseManager;
import com.android.orion.service.OrionService;
import com.android.orion.service.OrionService.OrionBinder;
import com.android.orion.setting.Constant;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Utility;

import java.util.ArrayList;

public class BaseActivity extends Activity {

	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE",
			"android.permission.WRITE_EXTERNAL_STORAGE"};
	public Logger Log = Logger.getLogger();
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
	ArrayList<Stock> mStockList = null;
	ArrayList<StockData> mStockDataList = null;
	ArrayList<StockDeal> mStockDealList = null;
	ArrayList<StockQuant> mStockQuantList = null;
	ArrayList<StockFinancial> mStockFinancialList = null;
	ArrayList<ShareBonus> mShareBonusList = null;
	ArrayList<TotalShare> mTotalShareList = null;
	ArrayMap<String, Stock> mStockDealArrayMap = null;
	StockDatabaseManager mStockDatabaseManager;
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

				if (Constant.ACTION_RESTART_LOADER.equals(action)) {
					restartLoader(intent);
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
				Constant.TAG + ":" + BaseActivity.class.getSimpleName());

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


		if (mStockList == null) {
			mStockList = new ArrayList<Stock>();
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

		mStockDatabaseManager = StockDatabaseManager.getInstance(this);

		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(mContext,
					ProgressDialog.THEME_HOLO_LIGHT);
		}

		checkPermission();
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
		if (!Utility.isNetworkConnected(this)) {
			Toast.makeText(this,
					getResources().getString(R.string.network_unavailable),
					Toast.LENGTH_SHORT).show();
		}
	}

	void restartLoader(Intent intent) {
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
