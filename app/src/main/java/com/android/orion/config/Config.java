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

	public static final int SERVICE_NOTIFICATION_ID = 600000;

	public static final int MAX_CONTENT_LENGTH_MIN60 = 60 * 4;
	public static final int MAX_CONTENT_LENGTH_MIN30_LONG = 60 * 8;
	public static final int MAX_CONTENT_LENGTH_MIN15_LONG = 60 * 16;
	public static final int MAX_CONTENT_LENGTH_MIN5_LONG = 60 * 48;

	public static final int MAX_CONTENT_LENGTH_MIN30_SHORT = 30 * 8;
	public static final int MAX_CONTENT_LENGTH_MIN15_SHORT = 20 * 16;
	public static final int MAX_CONTENT_LENGTH_MIN5_SHORT = 20 * 48;

	public static final int DISPLAY_ADAPTIVE_LENGTH_TREND_LEVEL_NONE = -1;
	public static final int DISPLAY_ADAPTIVE_LENGTH_TREND_LEVEL_DRAW = -1;
	public static final int DISPLAY_ADAPTIVE_LENGTH_TREND_LEVEL_STROKE = 15;
	public static final int DISPLAY_ADAPTIVE_LENGTH_TREND_LEVEL_SEGMENT = 15;
	public static final int DISPLAY_ADAPTIVE_LENGTH_TREND_LEVEL_LINE = 15;
	public static final int DISPLAY_ADAPTIVE_LENGTH_TREND_LEVEL_OUTLINE = 15;
	public static final int DISPLAY_ADAPTIVE_LENGTH_TREND_LEVEL_TREND = 15;
	public static final int[] DISPLAY_ADAPTIVE_LENGTH = {
			DISPLAY_ADAPTIVE_LENGTH_TREND_LEVEL_NONE,
			DISPLAY_ADAPTIVE_LENGTH_TREND_LEVEL_DRAW,
			DISPLAY_ADAPTIVE_LENGTH_TREND_LEVEL_STROKE,
			DISPLAY_ADAPTIVE_LENGTH_TREND_LEVEL_SEGMENT,
			DISPLAY_ADAPTIVE_LENGTH_TREND_LEVEL_LINE,
			DISPLAY_ADAPTIVE_LENGTH_TREND_LEVEL_OUTLINE,
			DISPLAY_ADAPTIVE_LENGTH_TREND_LEVEL_TREND};

	public static final int MAX_ML_TRAIN_TIMES = 100;

	public static String stockDataProvider = SinaFinance.PROVIDER_NAME;

	public static long alarmInterval = 1 * Constant.MINUTE_IN_MILLIS;
	public static long downloadSleep = 1 * Constant.SECOND_IN_MILLIS;
	public static long wakelockTimeout = 10 * Constant.MINUTE_IN_MILLIS;

	public static long downloadStockHSAInterval = 24 * Constant.HOUR_IN_MILLIS;
	public static long downloadStockInterval = 8 * Constant.HOUR_IN_MILLIS;
	public static long downloadStockDataInterval = 8 * Constant.HOUR_IN_MILLIS;
}