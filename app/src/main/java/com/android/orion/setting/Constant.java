package com.android.orion.setting;

import java.util.concurrent.TimeUnit;

public class Constant {

	public static final String MARK_NONE = "";
	public static final String MARK_ADD = "+";
	public static final String MARK_MINUS = "-";
	public static final String MARK_ASTERISK = "*";
	public static final String MARK_DOLLAR = "$";
	public static final String MARK_LEVEL = "L";
	public static final String MARK_PREDICT = "?";

	public static final String WHITE_SPACE = " ";
	public static final String NEW_LINE = "\n";
	public static final String TAB = "\t";
	public static final String TAB2 = "\t\t";

	public static final String FILE_EXT_TEXT = ".txt";
	public static final String FILE_EXT_XML = ".xml";

	public static final String ACTION_DEAL_EDIT = "orion.intent.action.ACTION_DEAL_EDIT";
	public static final String ACTION_DEAL_INSERT = "orion.intent.action.ACTION_DEAL_INSERT";
	public static final String ACTION_FAVORITE_STOCK_INSERT = "orion.intent.action.ACTION_FAVORITE_STOCK_INSERT";
	public static final String ACTION_STOCK_EDIT = "orion.intent.action.ACTION_STOCK_EDIT";
	public static final String ACTION_STOCK_ID = "orion.intent.action.ACTION_STOCK_ID";
	public static final String ACTION_STOCK_TREND_LIST = "orion.intent.action.ACTION_STOCK_TREND_LIST";

	public static final String DEAL = "deal";
	public static final String DEAL_INSERT = "insert";
	public static final String DEAL_EDIT = "edit";
	public static final String DEAL_DELETE = "delete";
	public static final String FAVORITE = "favorite";
	public static final String NOTIFICATION = "notification";

	public static final String EXTRA_DEAL_ID = "deal_id";
	public static final String EXTRA_INDEX_CODE = "index_code";
	public static final String EXTRA_INDEX_NAME = "index_name";
	public static final String EXTRA_INDEX_SE = "index_se";
	public static final String EXTRA_STOCK_ID = "stock_id";
	public static final String EXTRA_STOCK_SE = "stock_se";
	public static final String EXTRA_STOCK_CODE = "stock_code";
	public static final String EXTRA_STOCK_DEAL = "stock_deal";
	public static final String EXTRA_STOCK_TREND_ID = "stock_trend_id";
	public static final String EXTRA_STOCK_PERCEPTRON_ID = "stock_perceptron_id";
	public static final String EXTRA_STOCK_ID_LIST = "stock_id_list";
	public static final String EXTRA_STOCK_LIST_SORT_ORDER = "stock_list_sort_order";

	public static final int DOUBLE_FIXED_DECIMAL_2 = 2;
	public static final int DOUBLE_FIXED_DECIMAL_4 = 4;

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

	public static final long DOUBLE_CONSTANT_WAN = 10000;
	public static final long DOUBLE_CONSTANT_YI = (DOUBLE_CONSTANT_WAN * DOUBLE_CONSTANT_WAN);
}
