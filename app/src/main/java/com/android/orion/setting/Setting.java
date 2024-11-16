package com.android.orion.setting;

import androidx.annotation.NonNull;

import com.android.orion.database.DatabaseContract;
import com.android.orion.utility.Preferences;

public class Setting {

	static final String SETTING_PREFERENCE_INIT = "SETTING_PREFERENCE_INIT";

	static final String SETTING_PERIOD_ = "SETTING_PERIOD_";
	static final String SETTING_PERIOD_MONTH = "SETTING_PERIOD_MONTH";
	static final String SETTING_PERIOD_WEEK = "SETTING_PERIOD_WEEK";
	static final String SETTING_PERIOD_DAY = "SETTING_PERIOD_DAY";
	static final String SETTING_PERIOD_MIN60 = "SETTING_PERIOD_MIN60";
	static final String SETTING_PERIOD_MIN30 = "SETTING_PERIOD_MIN30";
	static final String SETTING_PERIOD_MIN15 = "SETTING_PERIOD_MIN15";
	static final String SETTING_PERIOD_MIN5 = "SETTING_PERIOD_MIN5";

	public static final String SETTING_SORT_ORDER_COMPONENT_LIST = "SETTING_SORT_ORDER_COMPONENT_LIST";
	public static final String SETTING_SORT_ORDER_STOCK_LIST = "SETTING_SORT_ORDER_STOCK_LIST";
	public static final String SETTING_SORT_ORDER_STOCK_TREND_LIST = "SETTING_SORT_ORDER_STOCK_TREND_LIST";
	public static final String SETTING_SORT_ORDER_FINANCIAL_LIST = "SETTING_SORT_ORDER_FINANCIAL_LIST";
	public static final String SETTING_SORT_ORDER_DEAL_LIST = "SETTING_SORT_ORDER_DEAL_LIST";

	public static final String SETTING_STOCK_FILTER_ENABLED = "SETTING_STOCK_FILTER_ENABLED";
	public static final String SETTING_STOCK_FILTER_FAVORITE = "SETTING_STOCK_FILTER_FAVORITE";
	public static final String SETTING_STOCK_FILTER_HOLD = "SETTING_STOCK_FILTER_HOLD";
	public static final String SETTING_STOCK_FILTER_ROI = "SETTING_STOCK_FILTER_ROI";
	public static final String SETTING_STOCK_FILTER_RATE = "SETTING_STOCK_FILTER_RATE";
	public static final String SETTING_STOCK_FILTER_ROE = "SETTING_STOCK_FILTER_ROE";
	public static final String SETTING_STOCK_FILTER_PE = "SETTING_STOCK_FILTER_PE";
	public static final String SETTING_STOCK_FILTER_PB = "SETTING_STOCK_FILTER_PB";
	public static final String SETTING_STOCK_FILTER_DIVIDEND = "SETTING_STOCK_FILTER_DIVIDEND";
	public static final String SETTING_STOCK_FILTER_YIELD = "SETTING_STOCK_FILTER_YIELD";
	public static final String SETTING_STOCK_FILTER_DIVIDEND_RATIO = "SETTING_STOCK_FILTER_DIVIDEND_RATIO";

	static final String SETTING_DISPLAY_NET = "SETTING_DISPLAY_NET";
	public static final String SETTING_DISPLAY_CANDLE = "SETTING_DISPLAY_CANDLE";
	public static final String SETTING_DISPLAY_DRAW = "SETTING_DISPLAY_DRAW";
	public static final String SETTING_DISPLAY_STROKE = "SETTING_DISPLAY_STROKE";
	public static final String SETTING_DISPLAY_SEGMENT = "SETTING_DISPLAY_SEGMENT";
	public static final String SETTING_DISPLAY_LINE = "SETTING_DISPLAY_LINE";

	public static final String SETTING_DEBUG_LOG = "SETTING_DEBUG_LOG";
	public static final String SETTING_DEBUG_DIRECT = "SETTING_DEBUG_DIRECT";
	public static final String SETTING_DEBUG_LOOPBACK = "SETTING_DEBUG_LOOPBACK";
	public static final String SETTING_DEBUG_LOOPBACK_DATE_TIME = "SETTING_DEBUG_LOOPBACK_DATE_TIME";
	public static final String SETTING_DEBUG_WIFI = "SETTING_DEBUG_WIFI";
	public static final String SETTING_DEBUG_DATAFILE = "SETTING_DEBUG_DATAFILE";

	public static final String SETTING_DOWNLOAD_STOCK_HSA_TIMEMILLIS = "SETTING_DOWNLOAD_STOCK_HSA_TIMEMILLIS";

	public static final String SETTING_DOWNLOAD_STOCK_INFORMATION_TIMEMILLIS = "SETTING_DOWNLOAD_STOCK_INFORMATION_TIMEMILLIS";
	public static final String SETTING_DOWNLOAD_STOCK_FINANCIAL_TIMEMILLIS = "SETTING_DOWNLOAD_STOCK_FINANCIAL_TIMEMILLIS";
	public static final String SETTING_DOWNLOAD_SHARE_BONUS_TIMEMILLIS = "SETTING_DOWNLOAD_SHARE_BONUS_TIMEMILLIS";
	public static final String SETTING_DOWNLOAD_TOTAL_SHARE_TIMEMILLIS = "SETTING_DOWNLOAD_TOTAL_SHARE_TIMEMILLIS";
	public static final String SETTING_DOWNLOAD_STOCK_DATA_HISTORY_TIMEMILLIS = "SETTING_DOWNLOAD_STOCK_DATA_HISTORY_TIMEMILLIS";
	public static final String SETTING_DOWNLOAD_STOCK_DATA_REALTIME_TIMEMILLIS = "SETTING_DOWNLOAD_STOCK_DATA_REALTIME_TIMEMILLIS";
	public static final String SETTING_DOWNLOAD_STOCK_REALTIME_TIMEMILLIS = "SETTING_DOWNLOAD_STOCK_REALTIME_TIMEMILLIS";

	public static final String SETTING_STOCK_DATA_CHANGED = "SETTING_STOCK_DATA_CHANGED";

	public static final String SETTING_STOCK_ARRAY_MAP_INDEX = "SETTING_STOCK_ARRAY_MAP_INDEX";

	private Setting() {
	}

	public static boolean getPreferenceInit() {
		return Preferences.getBoolean(Setting.SETTING_PREFERENCE_INIT, false);
	}

	public static void setPreferenceInit(boolean value) {
		Preferences.getBoolean(Setting.SETTING_PREFERENCE_INIT, value);
	}

	public static boolean getPeriod(@NonNull String period) {
		if (period == null) {
			return false;
		}
		return Preferences.getBoolean(SETTING_PERIOD_ + period.toUpperCase(), false);
	}

	public static void setPeriod(@NonNull String period, boolean value) {
		if (period == null) {
			return;
		}
		Preferences.putBoolean(SETTING_PERIOD_ + period.toUpperCase(), value);
	}

	public static boolean getDisplayNet() {
		return Preferences.getBoolean(SETTING_DISPLAY_NET, true);
	}

	public static void setDisplayNet(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_NET, value);
	}

	public static boolean getDisplayCandle() {
		return Preferences.getBoolean(SETTING_DISPLAY_CANDLE, false);
	}

	public static void setDisplayCandle(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_CANDLE, value);
	}

	public static boolean getDisplayDraw() {
		return Preferences.getBoolean(SETTING_DISPLAY_DRAW, true);
	}

	public static void setDisplayDraw(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_DRAW, value);
	}

	public static boolean getDisplayStroke() {
		return Preferences.getBoolean(SETTING_DISPLAY_STROKE, true);
	}

	public static void setDisplayStroke(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_STROKE, value);
	}

	public static boolean getDisplaySegment() {
		return Preferences.getBoolean(SETTING_DISPLAY_SEGMENT, true);
	}

	public static void setDisplaySegment(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_SEGMENT, value);
	}

	public static boolean getDisplayLine() {
		return Preferences.getBoolean(SETTING_DISPLAY_LINE, true);
	}

	public static void setDisplayLine(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_LINE, value);
	}

	public static boolean getDebugLog() {
		return Preferences.getBoolean(SETTING_DEBUG_LOG, false);
	}

	public static void setDebugLog(boolean value) {
		Preferences.putBoolean(SETTING_DEBUG_LOG, value);
	}

	public static boolean getDebugDirect() {
		return Preferences.getBoolean(SETTING_DEBUG_DIRECT, false);
	}

	public static void setDebugDirect(boolean value) {
		Preferences.putBoolean(SETTING_DEBUG_DIRECT, value);
	}

	public static boolean getDebugLoopback() {
		return Preferences.getBoolean(SETTING_DEBUG_LOOPBACK, false);
	}

	public static void setDebugLoopback(boolean value) {
		Preferences.putBoolean(SETTING_DEBUG_LOOPBACK, value);
	}

	public static String getDebugLoopbackDateTime() {
		return Preferences.getString(SETTING_DEBUG_LOOPBACK_DATE_TIME, "");
	}

	public static void setDebugLoopbackDateTime(String value) {
		Preferences.putString(SETTING_DEBUG_LOOPBACK_DATE_TIME, value);
	}

	public static boolean getDebugWifi() {
		return Preferences.getBoolean(SETTING_DEBUG_WIFI, false);
	}

	public static void setDebugWifi(boolean value) {
		Preferences.putBoolean(SETTING_DEBUG_WIFI, value);
	}

	public static boolean getDebugDataFile() {
		return Preferences.getBoolean(SETTING_DEBUG_DATAFILE, false);
	}

	public static void setDebugDataFile(boolean value) {
		Preferences.putBoolean(SETTING_DEBUG_DATAFILE, value);
	}

	public static long getDownloadStockHSATimemillis() {
		return Preferences.getLong(SETTING_DOWNLOAD_STOCK_HSA_TIMEMILLIS, 0);
	}

	public static void setDownloadStockHSATimemillis(long value) {
		Preferences.putLong(SETTING_DOWNLOAD_STOCK_HSA_TIMEMILLIS, value);
	}

	public static long getDownloadStockInformationTimemillis(String se, String code) {
		return Preferences.getLong(SETTING_DOWNLOAD_STOCK_INFORMATION_TIMEMILLIS + "_" + se + "_" + code, 0);
	}

	public static void setDownloadStockInformationTimemillis(String se, String code, long value) {
		Preferences.putLong(SETTING_DOWNLOAD_STOCK_INFORMATION_TIMEMILLIS + "_" + se + "_" + code, value);
	}

	public static long getDownloadStockFinancialTimemillis(String se, String code) {
		return Preferences.getLong(SETTING_DOWNLOAD_STOCK_FINANCIAL_TIMEMILLIS + "_" + se + "_" + code, 0);
	}

	public static void setDownloadStockFinancialTimemillis(String se, String code, long value) {
		Preferences.putLong(SETTING_DOWNLOAD_STOCK_FINANCIAL_TIMEMILLIS + "_" + se + "_" + code, value);
	}

	public static long getDownloadShareBonusTimemillis(String se, String code) {
		return Preferences.getLong(SETTING_DOWNLOAD_SHARE_BONUS_TIMEMILLIS + "_" + se + "_" + code, 0);
	}

	public static void setDownloadShareBonusTimemillis(String se, String code, long value) {
		Preferences.putLong(SETTING_DOWNLOAD_SHARE_BONUS_TIMEMILLIS + "_" + se + "_" + code, value);
	}

	public static long getDownloadTotalShareTimemillis(String se, String code) {
		return Preferences.getLong(SETTING_DOWNLOAD_TOTAL_SHARE_TIMEMILLIS + "_" + se + "_" + code, 0);
	}

	public static void setDownloadTotalShareTimemillis(String se, String code, long value) {
		Preferences.putLong(SETTING_DOWNLOAD_TOTAL_SHARE_TIMEMILLIS + "_" + se + "_" + code, value);
	}

	public static long getDownloadStockDataHistoryTimemillis(String se, String code) {
		return Preferences.getLong(SETTING_DOWNLOAD_STOCK_DATA_HISTORY_TIMEMILLIS + "_" + se + "_" + code, 0);
	}

	public static void setDownloadStockDataHistoryTimemillis(String se, String code, long value) {
		Preferences.putLong(SETTING_DOWNLOAD_STOCK_DATA_HISTORY_TIMEMILLIS + "_" + se + "_" + code, value);
	}

	public static long getDownloadStockDataRealTimeTimemillis(String se, String code) {
		return Preferences.getLong(SETTING_DOWNLOAD_STOCK_DATA_REALTIME_TIMEMILLIS + "_" + se + "_" + code, 0);
	}

	public static void setDownloadStockDataRealTimeTimemillis(String se, String code, long value) {
		Preferences.putLong(SETTING_DOWNLOAD_STOCK_DATA_REALTIME_TIMEMILLIS + "_" + se + "_" + code, value);
	}

	public static long getDownloadStockRealTimeTimemillis(String se, String code) {
		return Preferences.getLong(SETTING_DOWNLOAD_STOCK_REALTIME_TIMEMILLIS + "_" + se + "_" + code, 0);
	}

	public static void setDownloadStockRealTimeTimemillis(String se, String code, long value) {
		Preferences.putLong(SETTING_DOWNLOAD_STOCK_REALTIME_TIMEMILLIS + "_" + se + "_" + code, value);
	}

	public static void setDownloadTimemillis(String se, String code, long value) {
		setDownloadStockInformationTimemillis(se, code, 0);
		setDownloadStockFinancialTimemillis(se, code, 0);
		setDownloadShareBonusTimemillis(se, code, 0);
		setDownloadTotalShareTimemillis(se, code, 0);
		setDownloadStockDataHistoryTimemillis(se, code, 0);
		setDownloadStockDataRealTimeTimemillis(se, code, 0);
		setDownloadStockRealTimeTimemillis(se, code, 0);
	}

	public static boolean getStockDataChanged(String se, String code) {
		return Preferences.getBoolean(SETTING_STOCK_DATA_CHANGED + "_" + se + "_" + code, false);
	}

	public static void setStockDataChanged(String se, String code, boolean value) {
		Preferences.putBoolean(SETTING_STOCK_DATA_CHANGED + "_" + se + "_" + code, value);
	}

	public static long getStockArrayMapIndex() {
		return Preferences.getLong(SETTING_STOCK_ARRAY_MAP_INDEX, 0);
	}

	public static void setStockArrayMapIndex(long value) {
		Preferences.putLong(SETTING_STOCK_ARRAY_MAP_INDEX, value);
	}
}
