package com.android.orion.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.os.StrictMode;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.android.orion.R;
import com.android.orion.activity.MainActivity;
import com.android.orion.config.Config;
import com.android.orion.interfaces.NetworkChangedListener;
import com.android.orion.manager.ConnectionManager;
import com.android.orion.manager.StockAlarmManager;
import com.android.orion.manager.StockNotificationManager;
import com.android.orion.provider.StockDataProvider;
import com.android.orion.receiver.DownloadBroadcastReceiver;
import com.android.orion.receiver.ReceiverConnection;
import com.android.orion.utility.Logger;

public class StockService extends Service implements NetworkChangedListener {
	private static StockService mInstance;

	boolean mRedelivery = true;
	IntentFilter mIntentFilter;
	DownloadBroadcastReceiver mDownloadBroadcastReceiver;
	HandlerThread mHandlerThread;
	IBinder mServiceBinder;
	volatile ServiceHandler mHandler;
	NotificationManager mNotificationManager;
	Logger Log = Logger.getLogger();

	public static StockService getInstance() {
		return mInstance;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mInstance = this;
		mServiceBinder = new StockServiceBinder();
		mHandlerThread = new HandlerThread(StockService.class.getSimpleName(),
				Process.THREAD_PRIORITY_LOWEST);
		mHandlerThread.start();
		mHandler = new ServiceHandler(mHandlerThread.getLooper());
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(
					Config.SERVICE_CHANNEL_ID,
					Config.SERVICE_CHANNEL_NAME,
					NotificationManager.IMPORTANCE_LOW
			);
			mNotificationManager.createNotificationChannel(channel);

			try {
				Notification notification = buildNotification();
				startForeground(StockNotificationManager.SERVICE_NOTIFICATION_ID, notification);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		mDownloadBroadcastReceiver = new DownloadBroadcastReceiver();
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
		mIntentFilter.addAction(Intent.ACTION_DATE_CHANGED);
		mIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		registerReceiver(mDownloadBroadcastReceiver, mIntentFilter);
		ReceiverConnection.getInstance().registerReceiver(this);
		ConnectionManager.getInstance().registerListener(this);
		StockAlarmManager.getInstance().startAlarm();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				Notification notification = buildNotification();
				startForeground(StockNotificationManager.SERVICE_NOTIFICATION_ID, notification);
			}

			onStart(intent, startId);
			return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
		} catch (Exception e) {
			e.printStackTrace();
			stopSelf(); // Prevent ANR
			return START_NOT_STICKY;
		}
	}

	private Notification buildNotification() {
		return new NotificationCompat.Builder(this, Config.SERVICE_CHANNEL_ID)
				.setSmallIcon(R.drawable.ic_dialog_email)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.service_running))
				.setAutoCancel(false)
				.setCategory(NotificationCompat.CATEGORY_SERVICE)
				.setOngoing(true)
				.setPriority(NotificationCompat.PRIORITY_LOW)
				.setContentIntent(createPendingIntent())
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.build();
	}

	private PendingIntent createPendingIntent() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

		int flags = PendingIntent.FLAG_UPDATE_CURRENT;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			flags |= PendingIntent.FLAG_IMMUTABLE;
		}
		return PendingIntent.getActivity(this, 0, intent, flags);
	}

	public static boolean isServiceRunning(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (StockService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static void startService(Context context) {
		Intent intent = new Intent(context, StockService.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			context.startForegroundService(intent);
		} else {
			context.startService(intent);
		}
	}

	public static void stopService(Context context) {
		Intent intent = new Intent(context, StockService.class);
		context.stopService(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		try {
			StockNotificationManager.getInstance().cancel(StockNotificationManager.SERVICE_NOTIFICATION_ID);
			unregisterReceiver(mDownloadBroadcastReceiver);
			ReceiverConnection.getInstance().unregisterReceiver(this);
			ConnectionManager.getInstance().unregisterListener(this);
			StockAlarmManager.getInstance().stopAlarm();

			StockDataProvider.getInstance().onDestroy();

			if (mHandlerThread != null && mHandlerThread.isAlive()) {
				mHandlerThread.quitSafely();
			}
			mHandlerThread = null;
			mHandler = null;
			mInstance = null;
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