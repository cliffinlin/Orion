package com.android.orion.manager;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.android.orion.application.MainApplication;
import com.android.orion.config.Config;

import java.util.ArrayList;

public class ConnectionManager {
    public static final int MSG_CONNECTED = 0;
    public static final int MSG_DISCONNECTED = 1;

    private static ConnectionManager mInstance;
    private static Context mContext = MainApplication.getContext();

    ArrayList<OnConnectionChangeListener> mListener = new ArrayList<>();

    public interface OnConnectionChangeListener {
        public void onConnected();

        public void onDisconnected();
    }

    public static ConnectionManager getInstance() {
        synchronized (ConnectionManager.class) {
            if (mInstance == null) {
                mInstance = new ConnectionManager();
            }
        }
        return mInstance;
    }

    public void registerListener(@NonNull OnConnectionChangeListener listener) {
        if (!mListener.contains(listener)) {
            mListener.add(listener);
        }
    }

    public void unregisterListener(@NonNull OnConnectionChangeListener listener) {
        if (mListener.contains(listener)) {
            mListener.remove(listener);
        }
    }

    public void onConnected() {
        mHandler.sendEmptyMessage(MSG_CONNECTED);
    }

    public void onDisconnected() {
        mHandler.sendEmptyMessage(MSG_DISCONNECTED);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            if (!Config.funcCheckNetwork) {
                return;
            }

            switch (msg.what) {
                case MSG_CONNECTED:
                    for (OnConnectionChangeListener listener: mListener) {
                        listener.onConnected();
                    }
                    break;
                case MSG_DISCONNECTED:
                    for (OnConnectionChangeListener listener: mListener) {
                        listener.onDisconnected();
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
