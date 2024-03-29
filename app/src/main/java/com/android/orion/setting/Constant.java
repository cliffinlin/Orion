package com.android.orion.setting;

public class Constant {
	public static final String APP_NAME = "Orion";
	public static final String TAG = APP_NAME;

	public static final String TAB = "\t";

	public static final String SERVICE_CHANNEL_ID = "service_channel";
	public static final String SERVICE_CHANNEL_NAME = "Service Channel";

	public static final int SERVICE_NOTIFICATION_ID = 600000;

	public static final String MESSAGE_CHANNEL_ID = "message_channel";
	public static final String MESSAGE_CHANNEL_NAME = "Message Channel";

	public static final String FILE_EXT_TEXT = ".txt";
	public static final String FILE_EXT_XML = ".xml";

	public static final String ACTION = "action";
	public static final String DEAL = "deal";
	public static final String DEAL_OPERATE_INSERT = "insert";
	public static final String DEAL_OPERATE_EDIT = "edit";
	public static final String DEAL_OPERATE_DELETE = "delete";
	public static final String FAVORITE = "favorite";
	public static final String NOTIFICATION = "notification";

	public static final String ACTION_RESTART_LOADER = "orion.intent.action.RESTART_LOADER";

	public static final int DOUBLE_CONSTANT_WAN = 10000;
	public static final int DOUBLE_CONSTANT_YI = (DOUBLE_CONSTANT_WAN * DOUBLE_CONSTANT_WAN);
	public static final int DOUBLE_FIXED_DECIMAL = 2;

	public static final int DOWNLOAD_HISTORY_LENGTH_DEFAULT = 120;
	public static final int DOWNLOAD_HISTORY_LENGTH_NONE = 0;
	public static final int DOWNLOAD_HISTORY_LENGTH_UNLIMITED = -1;

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

	public static final int SCHEDULE_INTERVAL_MIN1 = 1;
	public static final int SCHEDULE_INTERVAL_MIN5 = 5;
	public static final int SCHEDULE_INTERVAL_MIN15 = 15;
	public static final int SCHEDULE_INTERVAL_MIN30 = 30;
	public static final int SCHEDULE_INTERVAL_MIN60 = 60;

	public static final int SEASONS_IN_A_YEAR = 4;

	public static final long WAKELOCK_TIMEOUT = 10 * 60 * 1000L; /*10 minutes*/
	public static final long DEFAULT_ALARM_INTERVAL = 5 * 60 * 1000;
	public static final long DEFAULT_DOWNLOAD_SLEEP_INTERVAL = 1 * 1000;

	public static final double ROI_COEFFICIENT = 10.0;
}
