package com.android.orion.config;

import com.android.orion.provider.SinaFinance;
import com.android.orion.setting.Constant;

public class Config {
	public static final String APP_NAME = "Orion";
	public static final String TAG = APP_NAME;

	public static final String SERVICE_CHANNEL_ID = "service_channel";
	public static final String SERVICE_CHANNEL_NAME = "Service Channel";

	public static final String MESSAGE_CHANNEL_ID = "message_channel";
	public static final String MESSAGE_CHANNEL_NAME = "Message Channel";

	public static final int DOUBLE_FIXED_DECIMAL = 2;
	public static final int SERVICE_NOTIFICATION_ID = 600000;

	public static final int MAX_CONTENT_LENGTH_MIN60 = 60 * 4;
	public static final int MAX_CONTENT_LENGTH_MIN30_LONG = 60 * 8;
	public static final int MAX_CONTENT_LENGTH_MIN15_LONG = 60 * 16;
	public static final int MAX_CONTENT_LENGTH_MIN5_LONG = 60 * 48;

	public static final int MAX_CONTENT_LENGTH_MIN30_SHORT = 30 * 8;
	public static final int MAX_CONTENT_LENGTH_MIN15_SHORT = 20 * 16;
	public static final int MAX_CONTENT_LENGTH_MIN5_SHORT = 20 * 48;

	public static String stockDataProvider = SinaFinance.PROVIDER_NAME;

	public static long alarmInterval = 1 * Constant.MINUTE_IN_MILLIS;
	public static long downloadSleep = 1 * Constant.SECOND_IN_MILLIS;
	public static long wakelockTimeout = 10 * Constant.MINUTE_IN_MILLIS;

	public static long downloadStockHSAInterval = 24 * Constant.HOUR_IN_MILLIS;
	public static long downloadStockInterval = 8 * Constant.HOUR_IN_MILLIS;
	public static long downloadStockDataInterval = 8 * Constant.HOUR_IN_MILLIS;
}