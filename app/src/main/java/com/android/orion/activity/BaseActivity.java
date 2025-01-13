package com.android.orion.activity;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.android.orion.database.Stock;
import com.android.orion.interfaces.AnalyzeListener;
import com.android.orion.interfaces.DownloadListener;
import com.android.orion.interfaces.IStockDataProvider;
import com.android.orion.manager.DatabaseManager;
import com.android.orion.manager.StockManager;
import com.android.orion.provider.StockDataProvider;
import com.android.orion.utility.Logger;

import java.util.ArrayList;

public class BaseActivity extends Activity implements AnalyzeListener, DownloadListener {

	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static final String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE",
			"android.permission.WRITE_EXTERNAL_STORAGE"};
	static ArrayMap<String, Stock> mStockArrayMap = new ArrayMap<>();
	Logger Log = Logger.getLogger();
	boolean mResumed = false;
	Context mContext = null;
	Bundle mBundle = null;
	String mAction = null;
	Intent mIntent = null;
	Stock mStock = new Stock();
	ArrayList<Stock> mStockList = new ArrayList<>();
	LoaderManager mLoaderManager = getLoaderManager();
	StockManager mStockManager = StockManager.getInstance();
	DatabaseManager mDatabaseManager = DatabaseManager.getInstance();
	IStockDataProvider mStockDataProvider = StockDataProvider.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		mIntent = getIntent();
		if (mIntent != null) {
			mAction = mIntent.getAction();
			mBundle = mIntent.getExtras();
		}

		mStockDataProvider.registerAnalyzeListener(this);
		mStockDataProvider.registerDownloadListener(this);
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
		mStockDataProvider.unRegisterAnalyzeListener(this);
		mStockDataProvider.unRegisterDownloadListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getActionBar().setDisplayHomeAsUpEnabled(true);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
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

	void restartLoader() {
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

	@Override
	public void onAnalyzeStart(String stockCode) {
	}

	@Override
	public void onAnalyzeFinish(String stockCode) {
		if (mStock.getCode().equals(stockCode)) {
			restartLoader();
		}
	}

	@Override
	public void onDownloadStart(String stockCode) {
	}

	@Override
	public void onDownloadComplete(String stockCode) {
		if (mStock.getCode().equals(stockCode)) {
			restartLoader();
		}
	}
}
