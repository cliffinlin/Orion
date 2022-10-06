package com.android.orion;

import android.text.TextUtils;

import com.android.orion.utility.Utility;

public class Settings {
	public static final String KEY_SHARED_PREFERENCES_INIT = "shared_preferences_init";

	public static final String KEY_NOTIFICATION_MESSAGE = "notification_message";
	public static final String KEY_NOTIFICATION_OPERATE = "notification_operate";
	public static final String KEY_NOTIFICATION_MARKET_KEY = "notification_market_key";

	public static final String KEY_PERIOD_MIN1 = "min1";
	public static final String KEY_PERIOD_MIN5 = "min5";
	public static final String KEY_PERIOD_MIN15 = "min15";
	public static final String KEY_PERIOD_MIN30 = "min30";
	public static final String KEY_PERIOD_MIN60 = "min60";
	public static final String KEY_PERIOD_DAY = "day";
	public static final String KEY_PERIOD_WEEK = "week";
	public static final String KEY_PERIOD_MONTH = "month";
	public static final String KEY_PERIOD_QUARTER = "quarter";
	public static final String KEY_PERIOD_YEAR = "year";

	public static final String KEY_PERIODS[] = { KEY_PERIOD_YEAR, KEY_PERIOD_QUARTER,
			KEY_PERIOD_MONTH, KEY_PERIOD_WEEK, KEY_PERIOD_DAY, KEY_PERIOD_MIN60, KEY_PERIOD_MIN30,
			KEY_PERIOD_MIN15, KEY_PERIOD_MIN5, KEY_PERIOD_MIN1 };

	public static final String KEY_DISPLAY_NET = "display_net";
	public static final String KEY_DISPLAY_MARKET_KEY = "display_market_key";
	public static final String KEY_DISPLAY_CANDLE = "display_candle";
	public static final String KEY_DISPLAY_DRAW = "display_draw";
	public static final String KEY_DISPLAY_STROKE = "display_stroke";
	public static final String KEY_DISPLAY_SEGMENT = "display_segment";
	public static final String KEY_DISPLAY_OVERLAP = "display_overlap";
	public static final String KEY_DISPLAY_LATEST = "display_latest";
	public static final String KEY_DISPLAY_COST = "display_cost";
	public static final String KEY_DISPLAY_DIRECT = "display_direct";
	public static final String KEY_DISPLAY_DEAL = "display_deal";
	public static final String KEY_DISPLAY_BONUS = "display_bonus";
	public static final String KEY_DISPLAY_BPS = "display_bps";
	public static final String KEY_DISPLAY_NPS = "display_nps";
	public static final String KEY_DISPLAY_ROE = "display_roe";
	public static final String KEY_DISPLAY_ROI = "display_roi";

	public static final String KEY_BACKTEST = "backtest";
	public static final String KEY_BACKTEST_DATE_TIME = "backtest_date_time";

	public static final String KEY_SORT_ORDER_IPO_LIST = "sort_order_ipo_list";
	public static final String KEY_SORT_ORDER_COMPONENT_LIST = "sort_order_component_list";
	public static final String KEY_SORT_ORDER_STOCK_LIST = "sort_order_stock_list";
	public static final String KEY_SORT_ORDER_STOCK_TRENDS_LIST = "sort_order_stock_trends_list";
	public static final String KEY_SORT_ORDER_FINANCIAL_LIST = "sort_order_financial_list";
	public static final String KEY_SORT_ORDER_DEAL_LIST = "sort_order_deal_list";

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

	public static final String KEY_STOCK_ACCOUNT_A = "A";
	public static final String KEY_STOCK_ACCOUNT_B = "B";

	public static final String KEY_STOCK_HSA_UPDATED = "stock_hsa_updated";

	private Settings() {
	}

    public static < E extends Enum < E >> boolean isInEnum(String value, Class < E > enumClass) {
        for (E e: enumClass.getEnumConstants()) {
            if (e.name().equals(value)) {
                return true;
            }
        }
        return false;
    }

	public static boolean checkOperatePeriod(String lastPeriod, String period, String operate) {
	    boolean result = false;
	    int periodIndex = -1;
	    int operateIndex = -1;

		if (TextUtils.isEmpty(lastPeriod) || TextUtils.isEmpty(period) || TextUtils.isEmpty(operate)) {
	        return result;
        }

		periodIndex = Utility.indexOfStrings(period, KEY_PERIODS);
	    operateIndex = Utility.indexOfStrings(operate, KEY_PERIODS);

	    if (periodIndex <= operateIndex) {
	        result = true;
        }

	    return result;
    }
}
