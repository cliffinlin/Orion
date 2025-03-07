package com.android.orion.handler;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Menu;

import com.android.orion.interfaces.IBackgroundHandler;


public class BackgroundHandler extends Handler {

	public static final int MESSAGE_ON_CREATE = 1000;
	public static final int MESSAGE_ON_RESUME = 1001;
	public static final int MESSAGE_ON_PAUSE = 1002;
	public static final int MESSAGE_ON_DESTROY = 1003;
	public static final int MESSAGE_ON_STOP = 1004;
	public static final int MESSAGE_ON_START = 1005;
	public static final int MESSAGE_ON_RESTART = 1006;
	public static final int MESSAGE_ON_NEW_INTENT = 1007;

	public static final int MESSAGE_ON_CREATE_OPTIONS_MENU = 1100;

	public static final int MESSAGE_ON_MENU_ITEM_SELECTED = 1200;
	public static final int MESSAGE_ON_MENU_ITEM_SELECTED_HOME = 1201;
	public static final int MESSAGE_ON_MENU_ITEM_SELECTED_SEARCH = 1202;
	public static final int MESSAGE_ON_MENU_ITEM_SELECTED_NEW = 1203;
	public static final int MESSAGE_ON_MENU_ITEM_SELECTED_REFRESH = 1204;
	public static final int MESSAGE_ON_MENU_ITEM_SELECTED_SETTINGS = 1205;

	private HandlerThread mHandlerThread;
	private IBackgroundHandler mHandler;

	private BackgroundHandler(IBackgroundHandler handler, HandlerThread handlerThread) {
		super(handlerThread.getLooper());
		mHandler = handler;
		mHandlerThread = handlerThread;
	}

	public static BackgroundHandler create(IBackgroundHandler handler, String threadName) {
		HandlerThread handlerThread = new HandlerThread(threadName);
		handlerThread.start();
		return new BackgroundHandler(handler, handlerThread);
	}

	public void release() {
		if (mHandlerThread != null) {
			mHandlerThread.quitSafely();
			mHandlerThread = null;
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		sendEmptyMessage(MESSAGE_ON_CREATE);
	}

	public void onResume() {
		sendEmptyMessage(MESSAGE_ON_RESUME);
	}

	public void onPause() {
		sendEmptyMessage(MESSAGE_ON_PAUSE);
	}

	public void onDestroy() {
		sendEmptyMessage(MESSAGE_ON_DESTROY);
	}

	public void onNewIntent(Intent intent) {
		sendEmptyMessage(MESSAGE_ON_NEW_INTENT);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		sendEmptyMessage(MESSAGE_ON_CREATE_OPTIONS_MENU);
		return true;
	}

	public void onMenuItemSelectedHome() {
		sendEmptyMessage(MESSAGE_ON_MENU_ITEM_SELECTED_HOME);
	}

	public void onMenuItemSelectedSearch() {
		sendEmptyMessage(MESSAGE_ON_MENU_ITEM_SELECTED_SEARCH);
	}

	public void onMenuItemSelectedNew() {
		sendEmptyMessage(MESSAGE_ON_MENU_ITEM_SELECTED_NEW);
	}

	public void onMenuItemSelectedRefresh() {
		sendEmptyMessage(MESSAGE_ON_MENU_ITEM_SELECTED_REFRESH);
	}

	public void onMenuItemSelectedSettings() {
		sendEmptyMessage(MESSAGE_ON_MENU_ITEM_SELECTED_SETTINGS);
	}

	public void handleMessage(Message msg) {
		if (mHandler == null || msg == null) {
			return;
		}

		switch (msg.what) {
			case MESSAGE_ON_CREATE:
				mHandler.onCreateHandler();
				break;
			case MESSAGE_ON_RESUME:
				mHandler.onResumeHandler();
				break;
			case MESSAGE_ON_PAUSE:
				mHandler.onPauseHandler();
				break;
			case MESSAGE_ON_DESTROY:
				mHandler.onDestroyHandler();
				release();
				break;
			case MESSAGE_ON_STOP:
				break;
			case MESSAGE_ON_START:
				break;
			case MESSAGE_ON_RESTART:
				break;
			case MESSAGE_ON_NEW_INTENT:
				mHandler.onNewIntentHandler();
				break;
			case MESSAGE_ON_CREATE_OPTIONS_MENU:
				mHandler.onCreateOptionsMenuHandler();
				break;
			case MESSAGE_ON_MENU_ITEM_SELECTED:
				mHandler.onMenuItemSelectedHandler();
				break;
			case MESSAGE_ON_MENU_ITEM_SELECTED_HOME:
				mHandler.onMenuItemSelectedHomeHandler();
				break;
			case MESSAGE_ON_MENU_ITEM_SELECTED_SEARCH:
				mHandler.onMenuItemSelectedSearchHandler();
				break;
			case MESSAGE_ON_MENU_ITEM_SELECTED_NEW:
				mHandler.onMenuItemSelectedNewHandler();
				break;
			case MESSAGE_ON_MENU_ITEM_SELECTED_REFRESH:
				mHandler.onMenuItemSelectedRefreshHandler();
				break;
			case MESSAGE_ON_MENU_ITEM_SELECTED_SETTINGS:
				mHandler.onMenuItemSelectedSettingsHandler();
				break;
		}
	}
}