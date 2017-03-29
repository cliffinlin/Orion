package com.android.orion;

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
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.android.orion.database.DatabaseContract;
import com.android.orion.leancloud.LeanCloudManager;

public class OrionService extends Service {
	boolean mRedelivery = true;
	String mName = "OrionService";

	AlarmManager mAlarmManager;
	AudioManager mAudioManager;

	Context mContext;
	ContentResolver mContentResolver;

	NotificationManager mNotificationManager;
	TelephonyManager mTelephonyManager;
	Vibrator mVibrator;

	HandlerThread mHandlerThread;

	volatile Looper mLooper;
	volatile ServiceHandler mHandler;

	IBinder mBinder = new OrionServiceBinder();

	SettingObserver mSettingObserver;

	SinaFinance mSinaFinance;
	LeanCloudManager mLeanCloudManager;

	OrionBroadcastReceiver mBroadcastReceiver;
	
	public class OrionBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
		}
	}

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

			switch (msg.what) {
			default:
				break;
			}
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

		mContext = getApplicationContext();

		mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mContentResolver = getContentResolver();

		mHandlerThread = new HandlerThread(mName,
				Process.THREAD_PRIORITY_BACKGROUND);
		mHandlerThread.start();

		mLooper = mHandlerThread.getLooper();
		mHandler = new ServiceHandler(mLooper);

		mSettingObserver = new SettingObserver(mHandler);
		mContentResolver.registerContentObserver(
				DatabaseContract.Setting.CONTENT_URI, true, mSettingObserver);

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		mBroadcastReceiver = new OrionBroadcastReceiver();
		registerReceiver(mBroadcastReceiver, filter);

		mSinaFinance = new SinaFinance(this);
		mLeanCloudManager = new LeanCloudManager(this);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Message msg = mHandler.obtainMessage();
		msg.arg1 = startId;
		msg.obj = intent;
		mHandler.sendMessage(msg);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		onStart(intent, startId);
		return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		mLooper.quit();

		try {
			unregisterReceiver(mBroadcastReceiver);
			mContentResolver.unregisterContentObserver(mSettingObserver);
		} catch (Exception e) {
			e.printStackTrace();
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
				mHandler.post(new DisplayToast(
						getString(R.string.action_upload_ok)));
			} else {
				mHandler.post(new DisplayToast(
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

	class SettingObserver extends ContentObserver {

		public SettingObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);

			String urlString = uri.toString();
		}
	}

	public class DisplayToast implements Runnable {
		String mText;

		public DisplayToast(String text) {
			mText = text;
		}

		@Override
		public void run() {
			Toast.makeText(mContext, mText, Toast.LENGTH_LONG).show();
		}
	}
}