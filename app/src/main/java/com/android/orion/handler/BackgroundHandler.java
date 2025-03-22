package com.android.orion.handler;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.view.Menu;
import android.view.MenuItem;

import com.android.orion.database.Stock;
import com.android.orion.interfaces.IBackgroundHandler;
import com.android.orion.interfaces.IStockDataProvider;
import com.android.orion.provider.StockDataProvider;
import com.android.orion.setting.Setting;

import java.util.ArrayList;


public class BackgroundHandler extends Handler {

	public static final int MESSAGE_ON_CREATE = 1000;
	public static final int MESSAGE_ON_RESUME = 1001;
	public static final int MESSAGE_ON_PAUSE = 1002;
	public static final int MESSAGE_ON_DESTROY = 1003;
	public static final int MESSAGE_ON_START = 1004;
	public static final int MESSAGE_ON_RESTART = 1005;
	public static final int MESSAGE_ON_STOP = 1006;
	public static final int MESSAGE_ON_NEW_INTENT = 1007;

	public static final int MESSAGE_ON_CREATE_OPTIONS_MENU = 1100;
	public static final int MESSAGE_ON_OPTIONS_ITEM_SELECTED = 1101;

	public static final int MESSAGE_DOWNLOAD = 1200;
	public static final int MESSAGE_DOWNLOAD_STOCK = 1201;
	public static final int MESSAGE_DOWNLOAD_STOCK_DATA = 1202;
	public static final int MESSAGE_DOWNLOAD_STOCK_FINANCIAL = 1203;

	public static final int MESSAGE_IMPORT_TDX_DATA_FILE = 999999 + 1;
	private final IBackgroundHandler mHandler;
	private final IStockDataProvider mStockDataProvider = StockDataProvider.getInstance();
	private HandlerThread mHandlerThread;

	private BackgroundHandler(IBackgroundHandler handler, HandlerThread handlerThread) {
		super(handlerThread.getLooper());
		mHandler = handler;
		mHandlerThread = handlerThread;
	}

	public static BackgroundHandler create(IBackgroundHandler handler) {
		HandlerThread handlerThread = new HandlerThread(handler.getClass().getSimpleName(), Process.THREAD_PRIORITY_BACKGROUND);
		handlerThread.start();
		return new BackgroundHandler(handler, handlerThread);
	}

	public void release() {
		if (mHandlerThread != null) {
			mHandlerThread.quitSafely();
			mHandlerThread = null;
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		sendMessage(obtainMessage(MESSAGE_ON_CREATE, savedInstanceState));
	}

	public void onResume() {
		sendEmptyMessage(MESSAGE_ON_RESUME);
	}

	public void onPause() {
		sendEmptyMessage(MESSAGE_ON_PAUSE);
	}

	public void onDestroy() {
		sendEmptyMessage(MESSAGE_ON_DESTROY);
	}

	public void onStart() {
		sendEmptyMessage(MESSAGE_ON_START);
	}

	public void onRestart() {
		sendEmptyMessage(MESSAGE_ON_RESTART);
	}

	public void onStop() {
		sendEmptyMessage(MESSAGE_ON_STOP);
	}

	public void onNewIntent(Intent intent) {
		sendMessage(obtainMessage(MESSAGE_ON_NEW_INTENT, intent));
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		sendMessage(obtainMessage(MESSAGE_ON_CREATE_OPTIONS_MENU, menu));
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		sendMessage(obtainMessage(MESSAGE_ON_OPTIONS_ITEM_SELECTED, item));
		return true;
	}

	public void download() {
		sendEmptyMessage(MESSAGE_DOWNLOAD);
	}

	public void download(Stock stock) {
		sendMessage(obtainMessage(MESSAGE_DOWNLOAD_STOCK, stock));
	}

	public void downloadStockData(Stock stock) {
		sendMessage(obtainMessage(MESSAGE_DOWNLOAD_STOCK_DATA, stock));
	}

	public void downloadStockFinancial(Stock stock) {
		sendMessage(obtainMessage(MESSAGE_DOWNLOAD_STOCK_FINANCIAL, stock));
	}

	public void importTDXDataFile(ArrayList<Uri> uriList) {
		sendMessage(obtainMessage(MESSAGE_IMPORT_TDX_DATA_FILE, uriList));
	}

	public void handleMessage(Message msg) {
		if (mHandler == null || msg == null) {
			return;
		}

		switch (msg.what) {
			case MESSAGE_ON_CREATE:
				Bundle savedInstanceState = (Bundle) msg.obj;
				mHandler.handleOnCreate(savedInstanceState);
				break;
			case MESSAGE_ON_RESUME:
				mHandler.handleOnResume();
				break;
			case MESSAGE_ON_PAUSE:
				mHandler.handleOnPause();
				break;
			case MESSAGE_ON_DESTROY:
				mHandler.handleOnDestroy();
				release();
				break;
			case MESSAGE_ON_START:
				mHandler.handleOnStart();
				break;
			case MESSAGE_ON_RESTART:
				mHandler.handleOnRestart();
				break;
			case MESSAGE_ON_STOP:
				mHandler.handleOnStop();
				break;
			case MESSAGE_ON_NEW_INTENT:
				Intent intent = (Intent) msg.obj;
				mHandler.handleOnNewIntent(intent);
				break;
			case MESSAGE_ON_CREATE_OPTIONS_MENU:
				Menu menu = (Menu) msg.obj;
				mHandler.handleOnCreateOptionsMenu(menu);
				break;
			case MESSAGE_ON_OPTIONS_ITEM_SELECTED:
				MenuItem item = (MenuItem) msg.obj;
				mHandler.handleOnOptionsItemSelected(item);
				break;
			case MESSAGE_DOWNLOAD:
				mStockDataProvider.download();
				break;
			case MESSAGE_DOWNLOAD_STOCK: {
				Stock stock = (Stock) msg.obj;
				Setting.setDownloadStockTimeMillis(stock, 0);
				Setting.setDownloadStockDataTimeMillis(stock, 0);
				mStockDataProvider.download(stock);
				break;
			}
			case MESSAGE_DOWNLOAD_STOCK_DATA: {
				Stock stock = (Stock) msg.obj;
				Setting.setDownloadStockDataTimeMillis(stock, 0);
				mStockDataProvider.download(stock);
				break;
			}
			case MESSAGE_DOWNLOAD_STOCK_FINANCIAL: {
				Stock stock = (Stock) msg.obj;
				Setting.setDownloadStockTimeMillis(stock, 0);
				mStockDataProvider.download(stock);
				break;
			}
			case MESSAGE_IMPORT_TDX_DATA_FILE:
				ArrayList<Uri> uriList = (ArrayList<Uri>) msg.obj;
				mStockDataProvider.importTDXDataFile(uriList);
				break;
		}
	}
}