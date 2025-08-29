package com.android.orion.config;

import android.graphics.Color;

import com.android.orion.provider.SinaFinance;
import com.android.orion.setting.Constant;

public class Config {
	public static final String APP_NAME = "Orion";
	public static final String TAG = APP_NAME;

	public static final String SERVICE_CHANNEL_ID = "service_channel";
	public static final String SERVICE_CHANNEL_NAME = "Service Channel";

	public static final String MESSAGE_CHANNEL_ID = "message_channel";
	public static final String MESSAGE_CHANNEL_NAME = "Message Channel";

	public static final int COLOR_RGB_RED = Color.rgb(255, 50, 50);
	public static final int COLOR_RGB_GREEN = Color.rgb(50, 128, 50);

	public static final int GRID_PROFIT_BACKGROUND_COLOR = Color.rgb(240, 240, 240);
	public static final int MARKER_COLOR_RED = Color.RED;
	public static final int MARKER_COLOR_GREEN = Color.GREEN;

	public static final int MAX_CONTENT_LENGTH_DAY = SinaFinance.DOWNLOAD_HISTORY_LENGTH_DEFAULT;
	public static final int MAX_CONTENT_LENGTH_MIN60 = MAX_CONTENT_LENGTH_DAY * 4;
	public static final int MAX_CONTENT_LENGTH_MIN30 = MAX_CONTENT_LENGTH_DAY * 8;
	public static final int MAX_CONTENT_LENGTH_MIN15 = MAX_CONTENT_LENGTH_DAY * 16;
	public static final int MAX_CONTENT_LENGTH_MIN5 = MAX_CONTENT_LENGTH_DAY * 48;

	public static final int MAX_ITERATIONS = 1000;

	public static String stockDataProvider = SinaFinance.PROVIDER_NAME;

	public static long alarmInterval = 1 * Constant.MINUTE_IN_MILLIS;
	public static long downloadSleep = 1 * Constant.SECOND_IN_MILLIS;
	public static long wakelockTimeout = 10 * Constant.MINUTE_IN_MILLIS;

	public static long downloadStockHSAInterval = 24 * Constant.HOUR_IN_MILLIS;
	public static long downloadStockInterval = 8 * Constant.HOUR_IN_MILLIS;
	public static long downloadStockDataInterval = 8 * Constant.HOUR_IN_MILLIS;
}