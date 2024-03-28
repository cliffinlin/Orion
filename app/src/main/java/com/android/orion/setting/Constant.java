package com.android.orion.setting;

public class Constant {
	public static final String TAB = "\t";

	public static final String FILE_EXT_TEXT = ".txt";
	public static final String FILE_EXT_XML = ".xml";

	public static final String ACTION_RESTART_LOADER = "orion.intent.action.RESTART_LOADER";

	public static final String DEAL = "deal";
	public static final String DEAL_OPERATE_INSERT = "insert";
	public static final String DEAL_OPERATE_EDIT = "edit";
	public static final String DEAL_OPERATE_DELETE = "delete";
	public static final String FAVORITE = "favorite";
	public static final String NOTIFICATION = "notification";

	public static final String EXTRA_STOCK_ID = "stock_id";
	public static final String EXTRA_STOCK_SE = "stock_se";
	public static final String EXTRA_STOCK_CODE = "stock_code";
	public static final String EXTRA_STOCK_DEAL = "stock_deal";
	public static final String EXTRA_STOCK_QUANT = "stock_quant";
	public static final String EXTRA_STOCK_BONUS = "stock_bonus";
	public static final String EXTRA_STOCK_BPS = "stock_bps";
	public static final String EXTRA_STOCK_NPS = "stock_nps";
	public static final String EXTRA_STOCK_ID_LIST = "stock_id_list";
	public static final String EXTRA_STOCK_LIST_SORT_ORDER = "stock_list_sort_order";

	public static final String EXTRA_INDEX_CODE = "index_code";
	public static final String EXTRA_INDEX_NAME = "index_name";
	public static final String EXTRA_INDEX_SE = "index_se";

	public static final int SEASONS_IN_A_YEAR = 4;

	public static final int MIN1 = 1;
	public static final int MIN5 = 5;
	public static final int MIN15 = 15;
	public static final int MIN30 = 30;
	public static final int MIN60 = 60;

	public static final long SECOND_INTERVAL = 1 * 1000L; //in millis
	public static final long MINUTE_INTERVAL = 60 * SECOND_INTERVAL;
	public static final long HOUR_INTERVAL = 60 * MINUTE_INTERVAL;

	public static final long DOUBLE_CONSTANT_WAN = 10000;
	public static final long DOUBLE_CONSTANT_YI = (DOUBLE_CONSTANT_WAN * DOUBLE_CONSTANT_WAN);
}
