package com.android.orion;

import java.lang.ref.WeakReference;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.TelephonyManager;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.StockDatabaseManager;

//import com.android.orion.leancloud.LeanCloudManager;

public class OrionService extends Service {
	private static final String TAG = "OrionService";
	private static final boolean DEBUG = true;

	private String mName = "OrionService";
	private boolean mRedelivery = true;

	private volatile Looper mServiceLooper;
	private volatile ServiceHandler mServiceHandler;

	private Context mContext;
	private AlarmManager mAlarmManager;
	private AudioManager mAudioManager;
	private NotificationManager mNotificationManager;
	private TelephonyManager mTelephonyManager;
	private Vibrator mVibrator;

	private ContentResolver mContentResolver;
	private OrionBroadcastReceiver mOrionBroadcastReceiver;
	private OrionContentOberserver mOrionContentOberserver;

	private final IBinder mBinder = new OrionServiceStub(this);

	SinaFinance mSinaFinance;
	StockDatabaseManager mStockDatabaseManager;

	// LeanCloudManager mLeanCloudManager = null;

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			onHandleIntent((Intent) msg.obj);
		}
	}

	public OrionService() {
	}

	public OrionService(String name) {
		super();

		mName = name;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mContext = getApplicationContext();

		HandlerThread handlerThread = new HandlerThread(mName);
		handlerThread.start();
		mServiceLooper = handlerThread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);

		mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mStockDatabaseManager = StockDatabaseManager.getInstance(mContext);

		mOrionBroadcastReceiver = new OrionBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_SERVICE_FINISHED);
		registerReceiver(mOrionBroadcastReceiver, intentFilter);

		mOrionContentOberserver = new OrionContentOberserver(mServiceHandler);
		mContentResolver = getContentResolver();
		mContentResolver.registerContentObserver(
				DatabaseContract.Stock.CONTENT_URI, false,
				mOrionContentOberserver);
		mContentResolver.registerContentObserver(
				DatabaseContract.StockData.CONTENT_URI, false,
				mOrionContentOberserver);

		mSinaFinance = new SinaFinance(this);
		// mLeanCloudManager = new LeanCloudManager(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
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

	void restartService() {
		Intent intent = new Intent();
		intent.setClass(mContext, OrionService.class);
		mContext.startService(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mServiceLooper.quit();

		try {
			mContentResolver.unregisterContentObserver(mOrionContentOberserver);
		} catch (Exception e) {
			e.printStackTrace();
		}

		restartService();
	}

	void onHandleIntent(Intent intent) {
		int serviceType = Constants.SERVICE_TYPE_NONE;
		Bundle bundle;

		if (mSinaFinance == null) {
			return;
		}

		// if (mLeanCloudManager == null) {
		// return;
		// }

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
			// mLeanCloudManager.fetchStockFavorite();
			// mSinaFinance.loadStockArrayMapFavorite();
			// mSinaFinance.fixPinyin();
			break;

		case Constants.SERVICE_CLOUD_UPLOAD_STOCK_FAVORITE:
			// if (mLeanCloudManager.saveStockFavorite()) {
			// mServiceHandler.post(new DisplayToast(this,
			// getString(R.string.action_upload_ok)));
			// } else {
			// mServiceHandler.post(new DisplayToast(this,
			// getString(R.string.action_upload_failed)));
			// }
			break;

		case Constants.SERVICE_SIMULATE_STOCK_FAVORITE:
			mSinaFinance.simulateStock(bundle);
			break;

		default:
			break;
		}
	}

	// public class DisplayToast implements Runnable {
	// Context mContext;
	// String mText;
	//
	// public DisplayToast(Context context, String text) {
	// mContext = context;
	// mText = text;
	// }
	//
	// @Override
	// public void run() {
	// Toast.makeText(mContext, mText, Toast.LENGTH_LONG).show();
	// }
	// }

	class OrionBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (Constants.ACTION_SERVICE_FINISHED.equals(action)) {

			} else {

			}
		}
	}

	class OrionContentOberserver extends ContentObserver {

		public OrionContentOberserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);

			if (uri.equals(DatabaseContract.Setting.CONTENT_URI)) {

			} else if (uri.equals(DatabaseContract.Stock.CONTENT_URI)) {

			} else if (uri.equals(DatabaseContract.StockData.CONTENT_URI)) {

			} else {

			}
		}
	}

	/*
	 * By making this a static class with a WeakReference to the Service, we
	 * ensure that the Service can be GCd even when the system process still has
	 * a remote reference to the stub.
	 */
	static class OrionServiceStub extends IOrionService.Stub {
		WeakReference<OrionService> mOrionService;

		OrionServiceStub(OrionService orionService) {
			mOrionService = new WeakReference<OrionService>(orionService);
		}
	}
}