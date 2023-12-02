package com.android.orion.setting;

import com.android.orion.database.DatabaseContract;
import com.android.orion.utility.Preferences;

public class Setting {

	public static final String KEY_PREFERENCES_INIT = "preferences_init";

	public static final String KEY_NOTIFICATION = "notification";

	public static final String KEY_PERIOD_YEAR = DatabaseContract.COLUMN_YEAR;
	public static final String KEY_PERIOD_QUARTER = DatabaseContract.COLUMN_QUARTER;
	public static final String KEY_PERIOD_MONTH = DatabaseContract.COLUMN_MONTH;
	public static final String KEY_PERIOD_WEEK = DatabaseContract.COLUMN_WEEK;
	public static final String KEY_PERIOD_DAY = DatabaseContract.COLUMN_DAY;
	public static final String KEY_PERIOD_MIN60 = DatabaseContract.COLUMN_MIN60;
	public static final String KEY_PERIOD_MIN30 = DatabaseContract.COLUMN_MIN30;
	public static final String KEY_PERIOD_MIN15 = DatabaseContract.COLUMN_MIN15;
	public static final String KEY_PERIOD_MIN5 = DatabaseContract.COLUMN_MIN5;
	public static final String KEY_PERIOD_MIN1 = DatabaseContract.COLUMN_MIN1;

	public static final String[] KEY_PERIODS = {KEY_PERIOD_YEAR, KEY_PERIOD_QUARTER,
			KEY_PERIOD_MONTH, KEY_PERIOD_WEEK, KEY_PERIOD_DAY, KEY_PERIOD_MIN60, KEY_PERIOD_MIN30,
			KEY_PERIOD_MIN15, KEY_PERIOD_MIN5, KEY_PERIOD_MIN1};

	public static final String KEY_INDEXES_WEIGHT = "indexes_weight";

	public static final String KEY_SORT_ORDER_IPO_LIST = "sort_order_ipo_list";
	public static final String KEY_SORT_ORDER_COMPONENT_LIST = "sort_order_component_list";
	public static final String KEY_SORT_ORDER_STOCK_LIST = "sort_order_stock_list";
	public static final String KEY_SORT_ORDER_STOCK_TREND_LIST = "sort_order_stock_trend_list";
	public static final String KEY_SORT_ORDER_FINANCIAL_LIST = "sort_order_financial_list";
	public static final String KEY_SORT_ORDER_DEAL_LIST = "sort_order_deal_list";
	public static final String KEY_SORT_ORDER_QUANT_LIST = "sort_order_quant_list";

	public static final String KEY_STOCK_FILTER_ENABLED = "stock_filter_enabled";
	public static final String KEY_STOCK_FILTER_FAVORITE = "stock_filter_favorite";
	public static final String KEY_STOCK_FILTER_HOLD = "stock_filter_hold";
	public static final String KEY_STOCK_FILTER_ROI = "stock_filter_roi";
	public static final String KEY_STOCK_FILTER_RATE = "stock_filter_rate";
	public static final String KEY_STOCK_FILTER_ROE = "stock_filter_roe";
	public static final String KEY_STOCK_FILTER_PE = "stock_filter_pe";
	public static final String KEY_STOCK_FILTER_PB = "stock_filter_pb";
	public static final String KEY_STOCK_FILTER_DIVIDEND = "stock_filter_dividend";
	public static final String KEY_STOCK_FILTER_YIELD = "stock_filter_yield";
	public static final String KEY_STOCK_FILTER_DIVIDEND_RATIO = "stock_filter_dividend_ratio";

	public static final String SETTING_DISPLAY_NET = "SETTING_DISPLAY_NET";
	public static final String SETTING_DISPLAY_CANDLE = "SETTING_DISPLAY_CANDLE";
	public static final String SETTING_DISPLAY_DRAW = "SETTING_DISPLAY_DRAW";
	public static final String SETTING_DISPLAY_STROKE = "SETTING_DISPLAY_STROKE";
	public static final String SETTING_DISPLAY_SEGMENT = "SETTING_DISPLAY_SEGMENT";
	public static final String SETTING_DISPLAY_LINE = "SETTING_DISPLAY_LINE";
	public static final String SETTING_DISPLAY_OVERLAP = "SETTING_DISPLAY_OVERLAP";
	public static final String SETTING_DISPLAY_LATEST = "SETTING_DISPLAY_LATEST";
	public static final String SETTING_DISPLAY_COST = "SETTING_DISPLAY_COST";
	public static final String SETTING_DISPLAY_DEAL = "SETTING_DISPLAY_DEAL";
	public static final String SETTING_DISPLAY_BONUS = "SETTING_DISPLAY_BONUS";
	public static final String SETTING_DISPLAY_BPS = "SETTING_DISPLAY_BPS";
	public static final String SETTING_DISPLAY_NPS = "SETTING_DISPLAY_NPS";
	public static final String SETTING_DISPLAY_ROE = "SETTING_DISPLAY_ROE";
	public static final String SETTING_DISPLAY_ROI = "SETTING_DISPLAY_ROI";

	public static final String SETTING_DISPLAY_THRESHOLD = "SETTING_DISPLAY_THRESHOLD";
	public static final String SETTING_DISPLAY_DIRECT = "SETTING_DISPLAY_DIRECT";
	public static final String SETTING_DISPLAY_QUANT = "SETTING_DISPLAY_QUANT";

	private static final String SETTING_DEBUG = "SETTING_DEBUG";
	public static final String SETTING_DEBUG_LOG = "SETTING_DEBUG_LOG";

	public static final String KEY_LOOPBACK = "loopback";
	public static final String KEY_LOOPBACK_DATE_TIME = "loopback_date_time";

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

	public static boolean getDisplayOverlap() {
		return Preferences.getBoolean(SETTING_DISPLAY_OVERLAP, true);
	}

	public static void setDisplayOverlap(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_OVERLAP, value);
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

	public static boolean getDisplayDirect() {
		return Preferences.getBoolean(SETTING_DISPLAY_DIRECT, true);
	}

	public static void setDisplayDirect(boolean value) {
		Preferences.putBoolean(SETTING_DISPLAY_DIRECT, value);
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

	public static void setDebugLog(boolean log) {
		Preferences.putBoolean(SETTING_DEBUG_LOG, log);
	}
}
