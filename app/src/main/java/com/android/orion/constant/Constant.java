package com.android.orion.constant;

import java.util.concurrent.TimeUnit;

public class Constant {

	public static final String FILE_EXT_TEXT = ".txt";
	public static final String FILE_EXT_XML = ".xml";

	public static final String ACTION_STOCK_DEAL_EDIT = "orion.intent.action.ACTION_STOCK_DEAL_EDIT";
	public static final String ACTION_STOCK_DEAL_NEW = "orion.intent.action.ACTION_STOCK_DEAL_NEW";
	public static final String ACTION_STOCK_ID = "orion.intent.action.ACTION_STOCK_ID";
	public static final String ACTION_STOCK_EDIT = "orion.intent.action.ACTION_STOCK_EDIT";
	public static final String ACTION_STOCK_NEW = "orion.intent.action.ACTION_STOCK_NEW";

	public static final String DEAL = "deal";
	public static final String DEAL_INSERT = "insert";
	public static final String DEAL_EDIT = "edit";
	public static final String DEAL_DELETE = "delete";
	public static final String FAVORITE = "favorite";
	public static final String NOTIFICATION = "notification";

	public static final String EXTRA_SHOW_STOCK_DEAL = "show_stock_deal";
	public static final String EXTRA_STOCK_ID = "stock_id";
	public static final String EXTRA_STOCK_SE = "stock_se";
	public static final String EXTRA_STOCK_CODE = "stock_code";
	public static final String EXTRA_STOCK_DEAL_ID = "stock_deal_id";
	public static final String EXTRA_STOCK_TREND_ID = "stock_trend_id";
	public static final String EXTRA_STOCK_PERCEPTRON_ID = "stock_perceptron_id";
	public static final String EXTRA_STOCK_ID_LIST = "stock_id_list";
	public static final String EXTRA_STOCK_LIST_SORT_ORDER = "stock_list_sort_order";

	public static final int FILE_TYPE_NONE = 0;
	public static final int FILE_TYPE_FAVORITE = 1;
	public static final int FILE_TYPE_TDX_DATA = 2;

	public static final int DOUBLE_FIXED_DECIMAL_1 = 1;
	public static final int DOUBLE_FIXED_DECIMAL_2 = 2;
	public static final int DOUBLE_FIXED_DECIMAL_4 = 4;

	public static final int PULSE_HIGH = 1;
	public static final int PULSE_MIDDLE = 0;
	public static final int PULSE_LOW = -1;

	public static final int SEASONS_IN_A_YEAR = 4;

	public static final int MIN5 = 5;
	public static final int MIN15 = 15;
	public static final int MIN30 = 30;
	public static final int MIN60 = 60;

	public static final int MIN60_PER_TRADE_DAY = 4;
	public static final int MIN30_PER_TRADE_DAY = 2 * MIN60_PER_TRADE_DAY;
	public static final int MIN15_PER_TRADE_DAY = 2 * MIN30_PER_TRADE_DAY;
	public static final int MIN5_PER_TRADE_DAY = 3 * MIN15_PER_TRADE_DAY;

	public static final long SECOND_IN_MILLIS = TimeUnit.SECONDS.toMillis(1);
	public static final long MINUTE_IN_MILLIS = TimeUnit.MINUTES.toMillis(1);
	public static final long HOUR_IN_MILLIS = TimeUnit.HOURS.toMillis(1);
	public static final long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);

	public static final long WAN = 10000;
	public static final long YI = (WAN * WAN);
}
