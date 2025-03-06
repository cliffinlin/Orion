package com.android.orion.activity;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.android.orion.R;
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

	public static final int MESSAGE_ON_CREATE = 1000;
	public static final int MESSAGE_ON_RESUME = 1001;
	public static final int MESSAGE_ON_PAUSE = 1002;
	public static final int MESSAGE_ON_DESTROY = 1003;
	public static final int MESSAGE_ON_STOP = 1004;
	public static final int MESSAGE_ON_START = 1005;
	public static final int MESSAGE_ON_RESTART = 1006;
	public static final int MESSAGE_ON_NEW_INTENT = 1007;

	public static final int MESSAGE_ON_CREATE_OPTIONS_MENU = 1100;

	public static final int MESSAGE_ON_MENU_ITEM_SELECTED = 1200;
	public static final int MESSAGE_ON_MENU_ITEM_SELECTED_HOME = 1201;
	public static final int MESSAGE_ON_MENU_ITEM_SELECTED_SEARCH = 1202;
	public static final int MESSAGE_ON_MENU_ITEM_SELECTED_NEW = 1203;
	public static final int MESSAGE_ON_MENU_ITEM_SELECTED_REFRESH = 1204;
	public static final int MESSAGE_ON_MENU_ITEM_SELECTED_SETTINGS = 1205;

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

	HandlerThread mHandlerThread;
	BackgroundHandler mBackgroundHandler;
	String TAG = getClass().getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initBackgroundHandler();
		mBackgroundHandler.sendEmptyMessage(MESSAGE_ON_CREATE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mBackgroundHandler.sendEmptyMessage(MESSAGE_ON_RESUME);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mBackgroundHandler.sendEmptyMessage(MESSAGE_ON_PAUSE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBackgroundHandler.sendEmptyMessage(MESSAGE_ON_DESTROY);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		mBackgroundHandler.sendEmptyMessage(MESSAGE_ON_NEW_INTENT);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mBackgroundHandler.sendEmptyMessage(MESSAGE_ON_CREATE_OPTIONS_MENU);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				mBackgroundHandler.sendEmptyMessage(MESSAGE_ON_MENU_ITEM_SELECTED_HOME);
				return true;
			case R.id.action_search:
				mBackgroundHandler.sendEmptyMessage(MESSAGE_ON_MENU_ITEM_SELECTED_SEARCH);
				return true;
			case R.id.action_new:
				mBackgroundHandler.sendEmptyMessage(MESSAGE_ON_MENU_ITEM_SELECTED_NEW);
				return true;
			case R.id.action_refresh:
				mBackgroundHandler.sendEmptyMessage(MESSAGE_ON_MENU_ITEM_SELECTED_REFRESH);
				return true;
			case R.id.action_setting:
				mBackgroundHandler.sendEmptyMessage(MESSAGE_ON_MENU_ITEM_SELECTED_SETTINGS);
				return true;
			default:
				return super.onMenuItemSelected(featureId, item);
		}
	}

	void initBackgroundHandler() {
		mHandlerThread = new HandlerThread(TAG);
		mHandlerThread.start();
		mBackgroundHandler = new BackgroundHandler(mHandlerThread.getLooper());
	}

	void onCreateHandler() {
		mContext = this;
		onNewIntentHandler();
		mStockDataProvider.registerAnalyzeListener(this);
		mStockDataProvider.registerDownloadListener(this);
	}

	void onResumeHandler() {
		checkPermission();
		mResumed = true;
	}

	void onPauseHandler() {
		mResumed = false;
	}

	void onDestroyHandler() {
		mStockDataProvider.unRegisterAnalyzeListener(this);
		mStockDataProvider.unRegisterDownloadListener(this);

		mHandlerThread.quitSafely();
	}

	void onNewIntentHandler() {
		mIntent = getIntent();
		if (mIntent != null) {
			mAction = mIntent.getAction();
			mBundle = mIntent.getExtras();
		}
	}

	void onCreateOptionsMenuHandler() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	void onMenuItemSelectedHandler() {
	}

	void onMenuItemSelectedHomeHandler() {
		finish();
	}

	void onMenuItemSelectedSearchHandler() {
		startActivity(new Intent(this, StockSearchActivity.class));
	}

	void onMenuItemSelectedNewHandler() {
	}

	void onMenuItemSelectedRefreshHandler() {
	}

	void onMenuItemSelectedSettingsHandler() {
		startActivity(new Intent(this, SettingActivity.class));
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

	class BackgroundHandler extends Handler {

		public BackgroundHandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {
			if (msg == null) {
				return;
			}

			switch (msg.what) {
				case MESSAGE_ON_CREATE:
					onCreateHandler();
					break;
				case MESSAGE_ON_RESUME:
					onResumeHandler();
					break;
				case MESSAGE_ON_PAUSE:
					onPauseHandler();
					break;
				case MESSAGE_ON_DESTROY:
					onDestroyHandler();
					break;
				case MESSAGE_ON_STOP:
					break;
				case MESSAGE_ON_START:
					break;
				case MESSAGE_ON_RESTART:
					break;
				case MESSAGE_ON_NEW_INTENT:
					onNewIntentHandler();
					break;
				case MESSAGE_ON_CREATE_OPTIONS_MENU:
					onCreateOptionsMenuHandler();
					break;
				case MESSAGE_ON_MENU_ITEM_SELECTED:
					onMenuItemSelectedHandler();
					break;
				case MESSAGE_ON_MENU_ITEM_SELECTED_HOME:
					onMenuItemSelectedHomeHandler();
					break;
				case MESSAGE_ON_MENU_ITEM_SELECTED_SEARCH:
					onMenuItemSelectedSearchHandler();
					break;
				case MESSAGE_ON_MENU_ITEM_SELECTED_NEW:
					onMenuItemSelectedNewHandler();
					break;
				case MESSAGE_ON_MENU_ITEM_SELECTED_REFRESH:
					onMenuItemSelectedRefreshHandler();
					break;
				case MESSAGE_ON_MENU_ITEM_SELECTED_SETTINGS:
					onMenuItemSelectedSettingsHandler();
					break;
			}
		}
	}
}
