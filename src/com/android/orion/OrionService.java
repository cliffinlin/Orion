package com.android.orion;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.android.orion.database.DatabaseContract;

public class OrionService extends Service {
	private volatile Looper mServiceLooper;
	private volatile ServiceHandler mServiceHandler;

	ContentResolver mContentResolver = null;
	private DatabaseContentOberserver mDatabaseContentOberserver = null;

	private String mName = "OrionService";
	private boolean mRedelivery = true;

	private final IBinder mBinder = new OrionServiceBinder();

	SinaFinance mSinaFinance = null;
	LeanCloudManager mLeanCloudManager = null;

	public class OrionServiceBinder extends Binder {

		OrionService getService() {
			return OrionService.this;
		}
	}

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			onHandleIntent((Intent) msg.obj);
			// stopSelf(msg.arg1);
		}
	}

	public OrionService() {
	}

	public OrionService(String name) {
		super();
		mName = name;
	}

	public void setIntentRedelivery(boolean enabled) {
		mRedelivery = enabled;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		HandlerThread handlerThread = new HandlerThread(mName);
		handlerThread.start();
		mServiceLooper = handlerThread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);

		if (mDatabaseContentOberserver == null) {
			mDatabaseContentOberserver = new DatabaseContentOberserver(null);
		}

		if (mDatabaseContentOberserver != null) {
			if (mContentResolver == null) {
				mContentResolver = getContentResolver();
			}

			mContentResolver.registerContentObserver(
					DatabaseContract.Stock.CONTENT_URI, true,
					mDatabaseContentOberserver);
		}

		if (mSinaFinance == null) {
			mSinaFinance = new SinaFinance(this);
		}

		if (mLeanCloudManager == null) {
			mLeanCloudManager = new LeanCloudManager(this);
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		msg.obj = intent;
		mServiceHandler.sendMessage(msg);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		onStart(intent, startId);
		return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		mServiceLooper.quit();

		if (mDatabaseContentOberserver != null) {
			mContentResolver
					.unregisterContentObserver(mDatabaseContentOberserver);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	void onHandleIntent(Intent intent) {
		int serviceType = Constants.SERVICE_TYPE_NONE;
		Bundle bundle;

		if (mSinaFinance == null) {
			return;
		}

		if (mLeanCloudManager == null) {
			return;
		}

		bundle = intent.getExtras();
		serviceType = bundle.getInt(Constants.EXTRA_KEY_SERVICE_TYPE,
				Constants.SERVICE_TYPE_NONE);

		mSinaFinance.downloadStockIndexes();
		mSinaFinance.downloadStockHSA();
		mSinaFinance.loadStockArrayMapFavorite();

		switch (serviceType) {
		case Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE:
			mSinaFinance.downloadStock(bundle);
			break;

		case Constants.SERVICE_ADD_STOCK_FAVORITE:
			mSinaFinance.fixPinyin();
			break;

		case Constants.SERVICE_REMOVE_STOCK_FAVORITE:
			break;

		case Constants.SERVICE_CLOUD_DOWNLOAD_STOCK_FAVORITE:
			mLeanCloudManager.fetchStockFavorite();
			mSinaFinance.loadStockArrayMapFavorite();
			mSinaFinance.fixPinyin();
			break;

		case Constants.SERVICE_CLOUD_UPLOAD_STOCK_FAVORITE:
			if (mLeanCloudManager.saveStockFavorite()) {
				mServiceHandler.post(new DisplayToast(this,
						getString(R.string.action_upload_ok)));
			} else {
				mServiceHandler.post(new DisplayToast(this,
						getString(R.string.action_upload_failed)));
			}
			break;

		case Constants.SERVICE_SIMULATE_STOCK_FAVORITE:
			mSinaFinance.simulateStock(bundle);
			break;

		default:
			break;
		}
	}

	public class DisplayToast implements Runnable {
		Context mContext;
		String mText;

		public DisplayToast(Context context, String text) {
			mContext = context;
			mText = text;
		}

		@Override
		public void run() {
			Toast.makeText(mContext, mText, Toast.LENGTH_LONG).show();
		}
	}

	class DatabaseContentOberserver extends ContentObserver {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			if (mSinaFinance != null) {
				// __TEST_CASE__
				// mSinaFinance.writeMessage();
			}
		}

		public DatabaseContentOberserver(Handler handler) {
			super(handler);
		}
	}
}