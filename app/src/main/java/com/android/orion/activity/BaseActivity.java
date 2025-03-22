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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.orion.R;
import com.android.orion.database.Stock;
import com.android.orion.handler.BackgroundHandler;
import com.android.orion.interfaces.AnalyzeListener;
import com.android.orion.interfaces.DownloadListener;
import com.android.orion.interfaces.IBackgroundHandler;
import com.android.orion.interfaces.IStockDataProvider;
import com.android.orion.manager.DatabaseManager;
import com.android.orion.manager.StockManager;
import com.android.orion.provider.StockDataProvider;
import com.android.orion.utility.Logger;

import java.util.ArrayList;

public class BaseActivity extends Activity implements IBackgroundHandler, AnalyzeListener, DownloadListener {

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
	BackgroundHandler mBackgroundHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBackgroundHandler = BackgroundHandler.create(this);
		mBackgroundHandler.onCreate(savedInstanceState);
	}

	public void handleOnCreate(Bundle savedInstanceState) {
		mContext = this;
		mStockDataProvider.registerAnalyzeListener(this);
		mStockDataProvider.registerDownloadListener(this);
		mIntent = getIntent();
		if (mIntent != null) {
			mAction = mIntent.getAction();
			mBundle = mIntent.getExtras();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mBackgroundHandler.onResume();
	}

	public void handleOnResume() {
		checkPermission();
		mResumed = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		mBackgroundHandler.onPause();
	}

	public void handleOnPause() {
		mResumed = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBackgroundHandler.onDestroy();
	}

	public void handleOnDestroy() {
		mStockDataProvider.unRegisterAnalyzeListener(this);
		mStockDataProvider.unRegisterDownloadListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mBackgroundHandler.onStart();
	}

	@Override
	public void handleOnStart() {
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		mBackgroundHandler.onRestart();
	}

	public void handleOnRestart() {
	}

	@Override
	protected void onStop() {
		super.onStop();
		mBackgroundHandler.onStop();
	}

	public void handleOnStop() {
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		mBackgroundHandler.onNewIntent(intent);
	}

	public void handleOnNewIntent(Intent intent) {
		setIntent(intent);
		mIntent = getIntent();
		if (mIntent != null) {
			mAction = mIntent.getAction();
			mBundle = mIntent.getExtras();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mBackgroundHandler.onCreateOptionsMenu(menu);
		return super.onCreateOptionsMenu(menu);
	}

	public void handleOnCreateOptionsMenu(Menu menu) {
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		mBackgroundHandler.onOptionsItemSelected(item);
		return super.onOptionsItemSelected(item);
	}

	public void handleOnOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
			case R.id.action_search:
				startActivity(new Intent(this, StockSearchActivity.class));
				break;
			case R.id.action_setting:
				startActivity(new Intent(this, SettingActivity.class));
				break;
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
