package com.android.orion.setting;

import androidx.annotation.NonNull;

import com.android.orion.database.Stock;
import com.android.orion.utility.Preferences;

public class Setting {

	public static final String SETTING_PREFERENCE_INIT = "SETTING_PREFERENCE_INIT";

	public static final String SETTING_PERIOD_ = "SETTING_PERIOD_";
	public static final boolean SETTING_PERIOD_MONTH_DEFAULT = true;
	public static final boolean SETTING_PERIOD_WEEK_DEFAULT = true;
	public static final boolean SETTING_PERIOD_DAY_DEFAULT = true;
	public static final boolean SETTING_PERIOD_MIN60_DEFAULT = true;
	public static final boolean SETTING_PERIOD_MIN30_DEFAULT = true;
	public static final boolean SETTING_PERIOD_MIN15_DEFAULT = true;
	public static final boolean SETTING_PERIOD_MIN5_DEFAULT = true;

	public static final String SETTING_SORT_ORDER_COMPONENT_LIST = "SETTING_SORT_ORDER_COMPONENT_LIST";
	public static final String SETTING_SORT_ORDER_STOCK_LIST = "SETTING_SORT_ORDER_STOCK_LIST";
	public static final String SETTING_SORT_ORDER_FINANCIAL_LIST = "SETTING_SORT_ORDER_FINANCIAL_LIST";
	public static final String SETTING_SORT_ORDER_DEAL_LIST = "SETTING_SORT_ORDER_DEAL_LIST";
	public static final String SETTING_SORT_ORDER_TREND_LIST = "SETTING_SORT_ORDER_TREND_LIST";
	public static final String SETTING_SORT_ORDER_PERCEPTRON_LIST = "SETTING_SORT_ORDER_PERCEPTRON_LIST";

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

	public static final String SETTING_DISPLAY_MONOCHROME = "SETTING_DISPLAY_MONOCHROME";
	public static final boolean SETTING_DISPLAY_MONOCHROME_DEFAULT = false;

	public static final String SETTING_DISPLAY_ADAPTIVE = "SETTING_DISPLAY_ADAPTIVE";
	public static final boolean SETTING_DISPLAY_ADAPTIVE_DEFAULT = true;

	public static final String SETTING_DISPLAY_FILLED = "SETTING_DISPLAY_FILLED";
	public static final boolean SETTING_DISPLAY_FILLED_DEFAULT = true;

	public static final String SETTING_DISPLAY_NET = "SETTING_DISPLAY_NET";
	public static final boolean SETTING_DISPLAY_NET_DEFAULT = true;

	public static final String SETTING_DISPLAY_CANDLE = "SETTING_DISPLAY_CANDLE";
	public static final boolean SETTING_DISPLAY_CANDLE_DEFAULT = false;

	public static final String SETTING_DISPLAY_AVERAGE = "SETTING_DISPLAY_AVERAGE";
	public static final boolean SETTING_DISPLAY_AVERAGE_DEFAULT = false;

	public static final String SETTING_DISPLAY_DRAW = "SETTING_DISPLAY_DRAW";
	public static final boolean SETTING_DISPLAY_DRAW_DEFAULT = true;

	public static final String SETTING_DISPLAY_STROKE = "SETTING_DISPLAY_STROKE";
	public static final boolean SETTING_DISPLAY_STROKE_DEFAULT = true;

	public static final String SETTING_DISPLAY_SEGMENT = "SETTING_DISPLAY_SEGMENT";
	public static final boolean SETTING_DISPLAY_SEGMENT_DEFAULT = true;

	public static final String SETTING_DISPLAY_LINE = "SETTING_DISPLAY_LINE";
	public static final boolean SETTING_DISPLAY_LINE_DEFAULT = true;

	public static final String SETTING_DISPLAY_OUT_LINE = "SETTING_DISPLAY_OUT_LINE";
	public static final boolean SETTING_DISPLAY_OUT_LINE_DEFAULT = true;

	public static final String SETTING_DISPLAY_SUPER_LINE = "SETTING_DISPLAY_SUPER_LINE";
	public static final boolean SETTING_DISPLAY_SUPER_LINE_DEFAULT = true;

	public static final String SETTING_DISPLAY_TREND_LINE = "SETTING_DISPLAY_TREND_LINE";
	public static final boolean SETTING_DISPLAY_TREND_LINE_DEFAULT = true;

	public static final String SETTING_DEBUG_LOG = "SETTING_DEBUG_LOG";
	public static final boolean SETTING_DEBUG_LOG_DEFAULT = true;
	public static final String SETTING_DEBUG_DIRECT = "SETTING_DEBUG_DIRECT";
	public static final String SETTING_DEBUG_WIFI = "SETTING_DEBUG_WIFI";

	public static final String SETTING_DOWNLOAD_STOCK_TIME_MILLIS_ = "SETTING_DOWNLOAD_STOCK_TIME_MILLIS_";
	public static final String SETTING_DOWNLOAD_STOCK_DATA_TIME_MILLIS_ = "SETTING_DOWNLOAD_STOCK_DATA_TIME_MILLIS_";
	public static final String SETTING_DOWNLOAD_STOCK_HSA_TIME_MILLIS = "SETTING_DOWNLOAD_STOCK_HSA_TIME_MILLIS";
	public static final String SETTING_TDX_DATA_FILE_URI_ = "SETTING_TDX_DATA_FILE_URI_";

	public static final String SETTING_STOCK_DATA_CHANGED_ = "SETTING_STOCK_DATA_CHANGED_";
	public static final String SETTING_STOCK_ARRAY_MAP_INDEX = "SETTING_STOCK_ARRAY_MAP_INDEX";

	private Setting() {
	}

	public static boolean getPreferenceInit() {
		return Preferences.getBoolean(SETTING_PREFERENCE_INIT, false);
	}

	public static void setPreferenceInit(boolean value) {
		Preferences.putBoolean(SETTING_PREFERENCE_INIT, value);
	}

	public static boolean getPeriod(@NonNull String period) {
		return Preferences.getBoolean(SETTING_PERIOD_ + period.toUpperCase(), false);
	}

	public static void setPeriod(@NonNull String period, boolean value) {
		Preferences.putBoolean(SETTING_PERIOD_ + period.toUpperCase(), value);
	}

	public static boolean getDisplayMonochrome() {
		return Preferences.getBoolean(SETTING_DISPLAY_MONOCHROME, SETTING_DISPLAY_MONOCHROME_DEFAULT);
	}

	public static void setDisplayMonochrome(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_MONOCHROME, value);
	}

	public static boolean getDisplayAdaptive() {
		return Preferences.getBoolean(SETTING_DISPLAY_ADAPTIVE, SETTING_DISPLAY_ADAPTIVE_DEFAULT);
	}

	public static void setDisplayAdaptive(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_ADAPTIVE, value);
	}

	public static boolean getDisplayFilled() {
		return Preferences.getBoolean(SETTING_DISPLAY_FILLED, SETTING_DISPLAY_FILLED_DEFAULT);
	}

	public static void setDisplayFilled(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_FILLED, value);
	}

	public static boolean getDisplayNet() {
		return Preferences.getBoolean(SETTING_DISPLAY_NET, SETTING_DISPLAY_NET_DEFAULT);
	}

	public static void setDisplayNet(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_NET, value);
	}

	public static boolean getDisplayCandle() {
		return Preferences.getBoolean(SETTING_DISPLAY_CANDLE, SETTING_DISPLAY_CANDLE_DEFAULT);
	}

	public static void setDisplayCandle(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_CANDLE, value);
	}

	public static boolean getDisplayAverage() {
		return Preferences.getBoolean(SETTING_DISPLAY_AVERAGE, SETTING_DISPLAY_AVERAGE_DEFAULT);
	}

	public static void setDisplayAverage(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_AVERAGE, value);
	}

	public static boolean getDisplayDraw() {
		return Preferences.getBoolean(SETTING_DISPLAY_DRAW, SETTING_DISPLAY_DRAW_DEFAULT);
	}

	public static void setDisplayDraw(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_DRAW, value);
	}

	public static boolean getDisplayStroke() {
		return Preferences.getBoolean(SETTING_DISPLAY_STROKE, SETTING_DISPLAY_STROKE_DEFAULT);
	}

	public static void setDisplayStroke(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_STROKE, value);
	}

	public static boolean getDisplaySegment() {
		return Preferences.getBoolean(SETTING_DISPLAY_SEGMENT, SETTING_DISPLAY_SEGMENT_DEFAULT);
	}

	public static void setDisplaySegment(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_SEGMENT, value);
	}

	public static boolean getDisplayLine() {
		return Preferences.getBoolean(SETTING_DISPLAY_LINE, SETTING_DISPLAY_LINE_DEFAULT);
	}

	public static void setDisplayLine(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_LINE, value);
	}

	public static boolean getDisplayOutLine() {
		return Preferences.getBoolean(SETTING_DISPLAY_OUT_LINE, SETTING_DISPLAY_OUT_LINE_DEFAULT);
	}

	public static void setDisplayOutLine(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_OUT_LINE, value);
	}

	public static boolean getDisplaySuperLine() {
		return Preferences.getBoolean(SETTING_DISPLAY_SUPER_LINE, SETTING_DISPLAY_SUPER_LINE_DEFAULT);
	}

	public static void setDisplaySuperLine(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_SUPER_LINE, value);
	}

	public static boolean getDisplayTrendLine() {
		return Preferences.getBoolean(SETTING_DISPLAY_TREND_LINE, SETTING_DISPLAY_TREND_LINE_DEFAULT);
	}

	public static void setDisplayTrendLine(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_TREND_LINE, value);
	}

	public static boolean getDebugLog() {
		return Preferences.getBoolean(SETTING_DEBUG_LOG, SETTING_DEBUG_LOG_DEFAULT);
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

	public static boolean getDebugWifi() {
		return Preferences.getBoolean(SETTING_DEBUG_WIFI, false);
	}

	public static void setDebugWifi(boolean value) {
		Preferences.putBoolean(SETTING_DEBUG_WIFI, value);
	}

	public static long getDownloadStockTimeMillis(Stock stock) {
		if (stock == null) {
			return 0;
		}
		return Preferences.getLong(SETTING_DOWNLOAD_STOCK_TIME_MILLIS_ + stock.getSeCodeUpperCase(), 0);
	}

	public static void setDownloadStockTimeMillis(Stock stock, long value) {
		if (stock == null) {
			return;
		}
		Preferences.putLong(SETTING_DOWNLOAD_STOCK_TIME_MILLIS_ + stock.getSeCodeUpperCase(), value);
	}

	public static long getDownloadStockDataTimeMillis(Stock stock) {
		if (stock == null) {
			return 0;
		}
		return Preferences.getLong(SETTING_DOWNLOAD_STOCK_DATA_TIME_MILLIS_ + stock.getSeCodeUpperCase(), 0);
	}

	public static void setDownloadStockDataTimeMillis(Stock stock, long value) {
		if (stock == null) {
			return;
		}
		Preferences.putLong(SETTING_DOWNLOAD_STOCK_DATA_TIME_MILLIS_ + stock.getSeCodeUpperCase(), value);
	}

	public static long getDownloadStockHSATimeMillis() {
		return Preferences.getLong(SETTING_DOWNLOAD_STOCK_HSA_TIME_MILLIS, 0);
	}

	public static void setDownloadStockHSATimeMillis(long value) {
		Preferences.putLong(SETTING_DOWNLOAD_STOCK_HSA_TIME_MILLIS, value);
	}

	public static String getTdxDataFileUri(Stock stock) {
		if (stock == null) {
			return "";
		}
		return Preferences.getString(SETTING_TDX_DATA_FILE_URI_ + stock.getSeCodeUpperCase(), "");
	}

	public static void setTdxDataFileUri(Stock stock, String value) {
		if (stock == null) {
			return;
		}
		Preferences.putString(SETTING_TDX_DATA_FILE_URI_ + stock.getSeCodeUpperCase(), value);
	}

	public static boolean getStockDataChanged(Stock stock) {
		if (stock == null) {
			return false;
		}
		return Preferences.getBoolean(SETTING_STOCK_DATA_CHANGED_ + stock.getSeCodeUpperCase(), false);
	}

	public static void setStockDataChanged(Stock stock, boolean value) {
		if (stock == null) {
			return;
		}
		Preferences.putBoolean(SETTING_STOCK_DATA_CHANGED_ + stock.getSeCodeUpperCase(), value);
	}

	public static long getStockArrayMapIndex() {
		return Preferences.getLong(SETTING_STOCK_ARRAY_MAP_INDEX, 0);
	}

	public static void setStockArrayMapIndex(long value) {
		Preferences.putLong(SETTING_STOCK_ARRAY_MAP_INDEX, value);
	}
}
