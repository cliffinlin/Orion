package com.android.orion.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import androidx.core.app.NotificationCompat;

import com.android.orion.application.OrionApplication;
import com.android.orion.config.Config;
import com.android.orion.database.Stock;
import com.android.orion.provider.SinaFinance;
import com.android.orion.receiver.DownloadBroadcastReceiver;
import com.android.orion.utility.Logger;

public class OrionService extends Service {
	static final int MESSAGE_CHECK_BACKGROUND = 0;
	static final int MAX_CHECK_BACKGROUND = 1;

	private static OrionService mInstance;

	boolean mRedelivery = true;
	int mCheckBackgroundCounter = 0;
	IntentFilter mIntentFilter;
	NotificationManager mNotificationManager;
	DownloadBroadcastReceiver mDownloadBroadcastReceiver;
	HandlerThread mHandlerThread;
	IBinder mServiceBinder;
	volatile ServiceHandler mHandler;
	SinaFinance mSinaFinance;
	Logger Log = Logger.getLogger();

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
		mHandler = new ServiceHandler(mHandlerThread.getLooper());

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

		mSinaFinance = SinaFinance.getInstance();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		onStart(intent, startId);
		return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		try {
			mHandlerThread.quit();
			mSinaFinance.onDestroy();
			unregisterReceiver(mDownloadBroadcastReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
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

		mSinaFinance.download();
	}

	public void download(Stock stock) {
		if (mSinaFinance == null) {
			return;
		}

		mSinaFinance.download(stock);
	}

	public void checkBackground() {
		mHandler.sendEmptyMessage(MESSAGE_CHECK_BACKGROUND);
	}

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MESSAGE_CHECK_BACKGROUND:
					mCheckBackgroundCounter++;
					if (mCheckBackgroundCounter > MAX_CHECK_BACKGROUND) {
						if (OrionApplication.getInstance().getActivityStartedCounter() == 0) {
							Log.d("onTerminate mCheckBackgroundCounter=" + mCheckBackgroundCounter);
							OrionApplication.getInstance().onTerminate();
						}
					}
					break;
			}
		}
	}

	public class OrionServiceBinder extends Binder {

		public OrionService getService() {
			return OrionService.this;
		}
	}
}