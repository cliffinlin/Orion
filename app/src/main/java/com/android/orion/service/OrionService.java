package com.android.orion.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
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

import com.android.orion.config.Config;
import com.android.orion.database.Stock;
import com.android.orion.receiver.DownloadBroadcastReceiver;
import com.android.orion.sina.SinaFinance;

public class OrionService extends Service {
	private static OrionService mInstance;

	boolean mRedelivery = true;
	IntentFilter mIntentFilter;
	NotificationManager mNotificationManager;
	DownloadBroadcastReceiver mDownloadBroadcastReceiver;
	HandlerThread mHandlerThread;
	IBinder mServiceBinder;
	volatile Looper mLooper;
	volatile ServiceHandler mHandler;
	SinaFinance mSinaFinance;

	public static OrionService getInstance() {
		return mInstance;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mInstance = this;

		mServiceBinder = new OrionServiceBinder();

		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		mHandlerThread = new HandlerThread(OrionService.class.getSimpleName(),
				Process.THREAD_PRIORITY_BACKGROUND);
		mHandlerThread.start();

		mLooper = mHandlerThread.getLooper();
		mHandler = new ServiceHandler(mLooper);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(Config.SERVICE_CHANNEL_ID,
					Config.SERVICE_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
			if (mNotificationManager != null) {
				mNotificationManager.createNotificationChannel(channel);
			}
			Notification notification = new NotificationCompat.Builder(this, Config.SERVICE_CHANNEL_ID)
					.setAutoCancel(true)
					.setCategory(NotificationCompat.CATEGORY_SERVICE)
					.setOngoing(true)
					.setPriority(NotificationManager.IMPORTANCE_LOW)
					.build();
			startForeground(Config.SERVICE_NOTIFICATION_ID, notification);
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

	@Override
	public IBinder onBind(Intent intent) {
		return mServiceBinder;
	}

	public void download(String se, String code) {
		if (mSinaFinance == null) {
			return;
		}

		mSinaFinance.download(se, code);
	}

	public void download() {
		if (mSinaFinance == null) {
			return;
		}

		mSinaFinance.download(null);
	}

	public void download(Stock stock) {
		if (mSinaFinance == null) {
			return;
		}

		mSinaFinance.download(stock);
	}

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
		}
	}

	public class OrionServiceBinder extends Binder {

		public OrionService getService() {
			return OrionService.this;
		}
	}
}