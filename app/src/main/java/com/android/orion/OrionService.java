package com.android.orion;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.Vibrator;
import android.telephony.TelephonyManager;

import androidx.core.app.NotificationCompat;

import com.android.orion.database.Stock;

public class OrionService extends Service {
	boolean mRedelivery = true;
	String mName = "OrionService";

	AlarmManager mAlarmManager;
	AudioManager mAudioManager;

	Context mContext;

	IntentFilter mIntentFilter;
	DownloadBroadcastReceiver mDownloadBroadcastReceiver;

	NotificationManager mNotificationManager;
	TelephonyManager mTelephonyManager;
	Vibrator mVibrator;

	HandlerThread mHandlerThread;

	IBinder mBinder;

	volatile Looper mLooper;
	volatile ServiceHandler mHandler;

	SinaFinance mSinaFinance;

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

		mBinder = new OrionBinder();

		mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mHandlerThread = new HandlerThread(mName,
				Process.THREAD_PRIORITY_BACKGROUND);
		mHandlerThread.start();

		mLooper = mHandlerThread.getLooper();
		mHandler = new ServiceHandler(mLooper);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID_SERVICE,
					mContext.getResources().getString(R.string.service), NotificationManager.IMPORTANCE_LOW);
			mNotificationManager.createNotificationChannel(channel);
			Notification notification = new NotificationCompat.Builder(this, Constants.CHANNEL_ID_SERVICE)
					.setAutoCancel(true)
					.setCategory(Notification.CATEGORY_SERVICE)
					.setOngoing(true)
					.setPriority(NotificationManager.IMPORTANCE_LOW)
					.build();
			startForeground(1, notification);
		}

		mDownloadBroadcastReceiver = new DownloadBroadcastReceiver();

		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
		mIntentFilter.addAction(Intent.ACTION_DATE_CHANGED);
		mIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		registerReceiver(mDownloadBroadcastReceiver, mIntentFilter);

		mSinaFinance = new SinaFinance(this);
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
			unregisterReceiver(mDownloadBroadcastReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void download(Stock stock) {
		if (mSinaFinance == null) {
			return;
		}

		mSinaFinance.download(stock);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	void onHandleIntent(Intent intent) {
		if (mSinaFinance == null) {
			return;
		}

		mSinaFinance.downloadStock(intent);
	}

	public class OrionBinder extends Binder {

		OrionService getService() {
			return OrionService.this;
		}
	}
}