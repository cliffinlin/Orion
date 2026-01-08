package com.android.orion.config;

import android.graphics.Color;

import com.android.orion.provider.SinaFinance;
import com.android.orion.constant.Constant;

public class Config {
	public static final String APP_NAME = "Orion";
	public static final String TAG = APP_NAME;

	public static final String SERVICE_CHANNEL_ID = "service_channel";
	public static final String SERVICE_CHANNEL_NAME = "Service Channel";

	public static final String MESSAGE_CHANNEL_ID = "message_channel";
	public static final String MESSAGE_CHANNEL_NAME = "Message Channel";

	public static final int COLOR_DARK_GREEN = Color.rgb(50, 128, 50);
	public static final int COLOR_DARK_RED = Color.rgb(255, 50, 50);
	public static final int COLOR_CANDLE = Color.RED;
	public static final int COLOR_DECREASING = COLOR_DARK_GREEN;
	public static final int COLOR_INCREASING = COLOR_DARK_RED;
	public static final int COLOR_HISTOGRAM = Color.RED;
	public static final int COLOR_DIF = Color.YELLOW;
	public static final int COLOR_DEA = Color.WHITE;

	public static final int DOWNLOAD_HISTORY_LENGTH_UNLIMITED = -1;
	public static final int DOWNLOAD_HISTORY_LENGTH_NONE = 0;
	public static final int DOWNLOAD_HISTORY_LENGTH_DEFAULT = 120;

	public static final int DOWNLOAD_HISTORY_LENGTH_MIN5 = 242;
	public static final int DOWNLOAD_HISTORY_LENGTH_MIN15 = 192;
	public static final int DOWNLOAD_HISTORY_LENGTH_MIN30 = 192;
	public static final int DOWNLOAD_HISTORY_LENGTH_MIN60 = 192;

	public static final int HISTORY_LENGTH_DAY = DOWNLOAD_HISTORY_LENGTH_DEFAULT;
	public static final int HISTORY_LENGTH_MIN60 = HISTORY_LENGTH_DAY * 4;
	public static final int HISTORY_LENGTH_MIN30 = HISTORY_LENGTH_DAY * 8;
	public static final int HISTORY_LENGTH_MIN15 = HISTORY_LENGTH_DAY * 16;
	public static final int HISTORY_LENGTH_MIN5 = HISTORY_LENGTH_DAY * 48;

	public static String stockDataProvider = SinaFinance.PROVIDER_NAME;

	public static long alarmInterval = Constant.MINUTE_IN_MILLIS;
	public static long downloadSleep = Constant.SECOND_IN_MILLIS;
	public static long wakelockTimeout = 10 * Constant.MINUTE_IN_MILLIS;

	public static long downloadStockHSAInterval = 24 * Constant.HOUR_IN_MILLIS;
	public static long downloadStockInterval = 8 * Constant.HOUR_IN_MILLIS;
	public static long downloadStockDataInterval = 8 * Constant.HOUR_IN_MILLIS;
}