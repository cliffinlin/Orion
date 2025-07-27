package com.android.orion.manager;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.android.orion.interfaces.NetworkChangedListener;

import java.util.ArrayList;

public class ConnectionManager {
	public static final int MSG_CONNECTED = 0;
	public static final int MSG_DISCONNECTED = 1;
	ArrayList<NetworkChangedListener> mListener = new ArrayList<>();
	private final Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
				case MSG_CONNECTED:
					for (NetworkChangedListener listener : mListener) {
						listener.onConnected();
					}
					break;
				case MSG_DISCONNECTED:
					for (NetworkChangedListener listener : mListener) {
						listener.onDisconnected();
					}
					break;
				default:
					break;
			}
		}
	};

	private ConnectionManager() {
	}

	public static ConnectionManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void registerListener(NetworkChangedListener listener) {
		if (listener == null) {
			return;
		}
		if (!mListener.contains(listener)) {
			mListener.add(listener);
		}
	}

	public void unregisterListener(@NonNull NetworkChangedListener listener) {
		mListener.remove(listener);
	}

	public void onConnected() {
		mHandler.sendEmptyMessage(MSG_CONNECTED);
	}

	public void onDisconnected() {
		mHandler.sendEmptyMessage(MSG_DISCONNECTED);
	}

	private static class SingletonHolder {
		private static final ConnectionManager INSTANCE = new ConnectionManager();
	}
}
