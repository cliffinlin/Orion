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

	public static boolean funcCheckNetwork = true;

	public static String stockDataProvider = SinaFinance.PROVIDER_NAME;

	public static long alarmInterval = 5 * Constant.MINUTE_INTERVAL;
	public static long backgroundTerminate = 1 * Constant.MINUTE_INTERVAL;
	public static long wakelockTimeout = 10 * Constant.MINUTE_INTERVAL;

	public static long downloadSleepInterval = 1 * Constant.SECOND_INTERVAL;

	public static long downloadStockHSAInterval = 24 * Constant.HOUR_INTERVAL;
	public static long downloadStockInformationInterval = 8 * Constant.HOUR_INTERVAL;
	public static long downloadStockFinancialInterval = 8 * Constant.HOUR_INTERVAL;
	public static long downloadShareBonusInterval = 8 * Constant.HOUR_INTERVAL;
	public static long downloadTotalShareInterval = 8 * Constant.HOUR_INTERVAL;
	public static long downloadStockRealTimeInterval = 8 * Constant.HOUR_INTERVAL;
}