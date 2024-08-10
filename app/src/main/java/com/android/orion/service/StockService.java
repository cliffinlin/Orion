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
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.android.orion.R;
import com.android.orion.config.Config;
import com.android.orion.database.Stock;
import com.android.orion.manager.ConnectionManager;
import com.android.orion.provider.StockDataProvider;
import com.android.orion.receiver.DownloadBroadcastReceiver;
import com.android.orion.receiver.ReceiverConnection;
import com.android.orion.utility.Utility;

public class StockService extends Service implements ConnectionManager.OnConnectionChangeListener {
	private static StockService mInstance;

	boolean mRedelivery = true;
	IntentFilter mIntentFilter;
	NotificationManager mNotificationManager;
	DownloadBroadcastReceiver mDownloadBroadcastReceiver;
	HandlerThread mHandlerThread;
	IBinder mServiceBinder;
	volatile ServiceHandler mHandler;

	public static StockService getInstance() {
		return mInstance;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mInstance = this;

		mServiceBinder = new StockServiceBinder();

		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		mHandlerThread = new HandlerThread(StockService.class.getSimpleName(),
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
		ReceiverConnection.getInstance().registerReceiver(this);
		ConnectionManager.getInstance().registerListener(this);
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
			if (mNotificationManager != null) {
				mNotificationManager.cancel(Config.SERVICE_NOTIFICATION_ID);
			}
			mHandlerThread.quit();

			unregisterReceiver(mDownloadBroadcastReceiver);
			ReceiverConnection.getInstance().unregisterReceiver(this);
			ConnectionManager.getInstance().unregisterListener(this);

			StockDataProvider.getInstance().onDestroy();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
//			System.exit(0);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mServiceBinder;
	}

	@Override
	public void onConnected() {
		StockDataProvider.getInstance().download();
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this,
				getResources().getString(R.string.network_unavailable),
				Toast.LENGTH_SHORT).show();
	}

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
		}
	}

	public class StockServiceBinder extends Binder {

		public StockService getService() {
			return StockService.this;
		}
	}
}