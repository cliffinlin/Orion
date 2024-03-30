package com.android.orion.setting;

import com.android.orion.database.DatabaseContract;
import com.android.orion.utility.Preferences;

public class Setting {

	public static final String SETTING_PREFERENCES_INIT = "SETTING_PREFERENCES_INIT";

	public static final String SETTING_NOTIFICATION = "SETTING_NOTIFICATION";

	public static final String SETTING_PERIOD_YEAR = DatabaseContract.COLUMN_YEAR;
	public static final String SETTING_PERIOD_QUARTER = DatabaseContract.COLUMN_QUARTER;
	public static final String SETTING_PERIOD_MONTH = DatabaseContract.COLUMN_MONTH;
	public static final String SETTING_PERIOD_WEEK = DatabaseContract.COLUMN_WEEK;
	public static final String SETTING_PERIOD_DAY = DatabaseContract.COLUMN_DAY;
	public static final String SETTING_PERIOD_MIN60 = DatabaseContract.COLUMN_MIN60;
	public static final String SETTING_PERIOD_MIN30 = DatabaseContract.COLUMN_MIN30;
	public static final String SETTING_PERIOD_MIN15 = DatabaseContract.COLUMN_MIN15;
	public static final String SETTING_PERIOD_MIN5 = DatabaseContract.COLUMN_MIN5;
	public static final String SETTING_PERIOD_MIN1 = DatabaseContract.COLUMN_MIN1;

	public static final String SETTING_SORT_ORDER_COMPONENT_LIST = "SETTING_SORT_ORDER_COMPONENT_LIST";
	public static final String SETTING_SORT_ORDER_STOCK_LIST = "SETTING_SORT_ORDER_STOCK_LIST";
	public static final String SETTING_SORT_ORDER_STOCK_TREND_LIST = "SETTING_SORT_ORDER_STOCK_TREND_LIST";
	public static final String SETTING_SORT_ORDER_FINANCIAL_LIST = "SETTING_SORT_ORDER_FINANCIAL_LIST";
	public static final String SETTING_SORT_ORDER_DEAL_LIST = "SETTING_SORT_ORDER_DEAL_LIST";
	public static final String SETTING_SORT_ORDER_QUANT_LIST = "SETTING_SORT_ORDER_QUANT_LIST";

	public static final String SETTING_STOCK_FILTER = "SETTING_STOCK_FILTER";
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

	public static final String SETTING_DISPLAY = "SETTING_DISPLAY";
	public static final String SETTING_DISPLAY_NET = "SETTING_DISPLAY_NET";
	public static final String SETTING_DISPLAY_CANDLE = "SETTING_DISPLAY_CANDLE";
	public static final String SETTING_DISPLAY_DRAW = "SETTING_DISPLAY_DRAW";
	public static final String SETTING_DISPLAY_STROKE = "SETTING_DISPLAY_STROKE";
	public static final String SETTING_DISPLAY_SEGMENT = "SETTING_DISPLAY_SEGMENT";
	public static final String SETTING_DISPLAY_LINE = "SETTING_DISPLAY_LINE";
	public static final String SETTING_DISPLAY_LATEST = "SETTING_DISPLAY_LATEST";
	public static final String SETTING_DISPLAY_COST = "SETTING_DISPLAY_COST";
	public static final String SETTING_DISPLAY_DEAL = "SETTING_DISPLAY_DEAL";
	public static final String SETTING_DISPLAY_BONUS = "SETTING_DISPLAY_BONUS";
	public static final String SETTING_DISPLAY_BPS = "SETTING_DISPLAY_BPS";
	public static final String SETTING_DISPLAY_NPS = "SETTING_DISPLAY_NPS";
	public static final String SETTING_DISPLAY_ROE = "SETTING_DISPLAY_ROE";
	public static final String SETTING_DISPLAY_ROI = "SETTING_DISPLAY_ROI";
	public static final String SETTING_DISPLAY_THRESHOLD = "SETTING_DISPLAY_THRESHOLD";
	public static final String SETTING_DISPLAY_QUANT = "SETTING_DISPLAY_QUANT";

	public static final String SETTING_DEBUG_LOG = "SETTING_DEBUG_LOG";
	public static final String SETTING_DEBUG_DIRECT = "SETTING_DEBUG_DIRECT";
	public static final String SETTING_DEBUG_LOOPBACK = "SETTING_DEBUG_LOOPBACK";
	public static final String SETTING_DEBUG_LOOPBACK_DATE_TIME = "SETTING_DEBUG_LOOPBACK_DATE_TIME";

	public static final String SETTING_DOWNLOAD_STOCK_INFORMATION_TIMEMILLIS = "SETTING_DOWNLOAD_STOCK_INFORMATION_TIMEMILLIS";
	public static final String SETTING_DOWNLOAD_STOCK_FINANCIAL_TIMEMILLIS = "SETTING_DOWNLOAD_STOCK_FINANCIAL_TIMEMILLIS";
	public static final String SETTING_DOWNLOAD_SHARE_BONUS_TIMEMILLIS = "SETTING_DOWNLOAD_SHARE_BONUS_TIMEMILLIS";
	public static final String SETTING_DOWNLOAD_TOTAL_SHARE_TIMEMILLIS = "SETTING_DOWNLOAD_TOTAL_SHARE_TIMEMILLIS";
	public static final String SETTING_DOWNLOAD_STOCK_REALTIME_TIMEMILLIS = "SETTING_DOWNLOAD_STOCK_REALTIME_TIMEMILLIS";

	private Setting() {
	}

	public static boolean getBoolean(String key) {
		return Preferences.getBoolean(key, false);
	}

	public static void setBoolean(String key, boolean value) {
		Preferences.putBoolean(key, value);
	}

	public static boolean getDisplayNet() {
		return Preferences.getBoolean(SETTING_DISPLAY_NET, false);
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

	public static boolean getDisplayLatest() {
		return Preferences.getBoolean(SETTING_DISPLAY_LATEST, true);
	}

	public static void setDisplayLatest(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_LATEST, value);
	}

	public static boolean getDisplayCost() {
		return Preferences.getBoolean(SETTING_DISPLAY_COST, true);
	}

	public static void setDisplayCost(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_COST, value);
	}

	public static boolean getDisplayDeal() {
		return Preferences.getBoolean(SETTING_DISPLAY_DEAL, true);
	}

	public static void setDisplayDeal(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_DEAL, value);
	}

	public static boolean getDisplayBonus() {
		return Preferences.getBoolean(SETTING_DISPLAY_BONUS, true);
	}

	public static void setDisplayBonus(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_BONUS, value);
	}

	public static boolean getDisplayBps() {
		return Preferences.getBoolean(SETTING_DISPLAY_BPS, true);
	}

	public static void setDisplayBps(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_BPS, value);
	}

	public static boolean getDisplayNps() {
		return Preferences.getBoolean(SETTING_DISPLAY_NPS, true);
	}

	public static void setDisplayNps(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_NPS, value);
	}

	public static boolean getDisplayRoe() {
		return Preferences.getBoolean(SETTING_DISPLAY_ROE, true);
	}

	public static void setDisplayRoe(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_ROE, value);
	}

	public static boolean getDisplayRoi() {
		return Preferences.getBoolean(SETTING_DISPLAY_ROI, true);
	}

	public static void setDisplayRoi(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_ROI, value);
	}

	public static boolean getDisplayThreshold() {
		return Preferences.getBoolean(SETTING_DISPLAY_THRESHOLD, true);
	}

	public static void setDisplayThreshold(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_THRESHOLD, value);
	}

	public static boolean getDisplayQuant() {
		return Preferences.getBoolean(SETTING_DISPLAY_QUANT, true);
	}

	public static void setDisplayQuant(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_QUANT, value);
	}

	public static boolean getDebugLog() {
		return Preferences.getBoolean(SETTING_DEBUG_LOG, false);
	}

	public static void setDebugLog(boolean value) {
		Preferences.putBoolean(SETTING_DEBUG_LOG, value);
	}

	public static boolean getDebugDirect() {
		return Preferences.getBoolean(SETTING_DEBUG_DIRECT, true);
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

	public static long getDownloadStockInformationTimemillis(String se, String code) {
		return Preferences.getLong(SETTING_DOWNLOAD_STOCK_INFORMATION_TIMEMILLIS + "_" + se + "_" + code, 0);
	}

	public static void setDownloadStockInformationTimemillis(String se, String code, long timemillis) {
		Preferences.putLong(SETTING_DOWNLOAD_STOCK_INFORMATION_TIMEMILLIS + "_" + se + "_" + code, timemillis);
	}

	public static long getDownloadStockFinancialTimemillis(String se, String code) {
		return Preferences.getLong(SETTING_DOWNLOAD_STOCK_FINANCIAL_TIMEMILLIS + "_" + se + "_" + code, 0);
	}

	public static void setDownloadStockFinancialTimemillis(String se, String code, long timemillis) {
		Preferences.putLong(SETTING_DOWNLOAD_STOCK_FINANCIAL_TIMEMILLIS + "_" + se + "_" + code, timemillis);
	}

	public static long getDownloadShareBonusTimemillis(String se, String code) {
		return Preferences.getLong(SETTING_DOWNLOAD_SHARE_BONUS_TIMEMILLIS + "_" + se + "_" + code, 0);
	}

	public static void setDownloadShareBonusTimemillis(String se, String code, long timemillis) {
		Preferences.putLong(SETTING_DOWNLOAD_SHARE_BONUS_TIMEMILLIS + "_" + se + "_" + code, timemillis);
	}

	public static long getDownloadTotalShareTimemillis(String se, String code) {
		return Preferences.getLong(SETTING_DOWNLOAD_TOTAL_SHARE_TIMEMILLIS + "_" + se + "_" + code, 0);
	}

	public static void setDownloadTotalShareTimemillis(String se, String code, long timemillis) {
		Preferences.putLong(SETTING_DOWNLOAD_TOTAL_SHARE_TIMEMILLIS + "_" + se + "_" + code, timemillis);
	}

	public static long getDownloadStockRealTimeTimemillis(String se, String code) {
		return Preferences.getLong(SETTING_DOWNLOAD_STOCK_REALTIME_TIMEMILLIS + "_" + se + "_" + code, 0);
	}

	public static void setDownloadStockRealTimeTimemillis(String se, String code, long timemillis) {
		Preferences.putLong(SETTING_DOWNLOAD_STOCK_REALTIME_TIMEMILLIS + "_" + se + "_" + code, timemillis);
	}
}
