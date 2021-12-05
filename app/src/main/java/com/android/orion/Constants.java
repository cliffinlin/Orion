package com.android.orion;

public class Constants {
	public static final String APP_NAME = "Orion";
	public static final String TAG = APP_NAME;

	public static final String MESSAGE_CHANNEL_ID = "message_channel";
	public static final String MESSAGE_CHANNEL_NAME = "Message Channel";

	public static final String SERVICE_CHANNEL_ID = "service_channel";
	public static final String SERVICE_CHANNEL_NAME = "Service Channel";

	public static final String FAVORITE = "favorite";
	public static final String XML_FILE_EXT = ".xml";

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
	public static final String EXTRA_STOCK_FINANCIAL = "stock_financial";
	public static final String EXTRA_STOCK_ID_LIST = "stock_id_list";
	public static final String EXTRA_STOCK_LIST_SORT_ORDER = "stock_list_sort_order";

	public static final String PERIOD_MIN1 = "min1";
	public static final String PERIOD_MIN5 = "min5";
	public static final String PERIOD_MIN15 = "min15";
	public static final String PERIOD_MIN30 = "min30";
	public static final String PERIOD_MIN60 = "min60";
	public static final String PERIOD_DAY = "day";
	public static final String PERIOD_WEEK = "week";
	public static final String PERIOD_MONTH = "month";
	public static final String PERIOD_QUARTER = "quarter";
	public static final String PERIOD_YEAR = "year";
	public static final String PERIODS[] = { PERIOD_YEAR, PERIOD_QUARTER,
			PERIOD_MONTH, PERIOD_WEEK, PERIOD_DAY, PERIOD_MIN60, PERIOD_MIN30,
			PERIOD_MIN15, PERIOD_MIN5, PERIOD_MIN1 };

	public static final int SCHEDULE_INTERVAL_MIN1 = 1;
	public static final int SCHEDULE_INTERVAL_MIN5 = 5;
	public static final int SCHEDULE_INTERVAL_MIN15 = 15;
	public static final int SCHEDULE_INTERVAL_MIN30 = 30;
	public static final int SCHEDULE_INTERVAL_MIN60 = 60;

	public static final String STOCK_ACTION_NONE = "";
	public static final String STOCK_ACTION_STAR = "*";
	public static final String STOCK_ACTION_D = "D";
	public static final String STOCK_ACTION_BUY = "B";
	public static final String STOCK_ACTION_BUY1 = "B1";
	public static final String STOCK_ACTION_BUY2 = "B2";
	public static final String STOCK_ACTION_BUY3 = "B3";
	public static final String STOCK_ACTION_G = "G";
	public static final String STOCK_ACTION_SELL = "S";
	public static final String STOCK_ACTION_SELL1 = "S1";
	public static final String STOCK_ACTION_SELL2 = "S2";
	public static final String STOCK_ACTION_SELL3 = "S3";
	public static final String STOCK_ACTION_HIGH = "H";
	public static final String STOCK_ACTION_LOW = "L";
	public static final char STOCK_ACTION_ADD = '+';
	public static final char STOCK_ACTION_MINUS = '-';

	public static final String STOCK_CLASS_A = "A";
	public static final String STOCK_CLASS_INDEX = "I";

	public static final String STOCK_OPERATION_NONE = "";
	public static final String STOCK_OPERATION_ALERT = "A";

	public static final int STOCK_DIRECTION_NONE = 0;
	public static final int STOCK_DIRECTION_UP = 1 << 0;
	public static final int STOCK_DIRECTION_DOWN = 1 << 1;
	public static final int STOCK_DIRECTION_UP_STROKE = 1 << 2;
	public static final int STOCK_DIRECTION_DOWN_STROKE = 1 << 3;
	public static final int STOCK_DIRECTION_UP_SEGMENT = 1 << 4;
	public static final int STOCK_DIRECTION_DOWN_SEGMENT = 1 << 5;

	public static final int STOCK_DIVERGENCE_TYPE_NONE = 0;
	public static final int STOCK_DIVERGENCE_TYPE_DRAW = 1 << 0;
	public static final int STOCK_DIVERGENCE_TYPE_STROKE = 1 << 1;
	public static final int STOCK_DIVERGENCE_TYPE_SEGMENT = 1 << 2;

	public static final int STOCK_DIVERGENCE_NONE = 0;
	public static final int STOCK_DIVERGENCE_DIF_DEA = 1 << 0;
	public static final int STOCK_DIVERGENCE_HISTOGRAM = 1 << 1;
	public static final int STOCK_DIVERGENCE_SIGMA_HISTOGRAM = 1 << 2;

	public static final long STOCK_FLAG_NONE = 0;
	public static final long STOCK_FLAG_FAVORITE = 1 << 0;
	public static final long STOCK_FLAG_RECENT = 1 << 2;

	public static final long STOCK_ID_INVALID = 0;

	public static final String STOCK_INDEXES_CODE_BASE_SH = "000000";
	public static final String STOCK_INDEXES_CODE_BASE_SZ = "399000";

	public static final int STOCK_MARKET_OPEN_MINUTES = 9 * 60 + 30;
	public static final int STOCK_MARKET_LUNCH_MINUTES = 1 * 60 + 30;

	public static final String STOCK_MARKET_OPEN_TIME = "09:15:00";
	public static final String STOCK_MARKET_LUNCH_BEGIN_TIME = "11:40:00";
	public static final String STOCK_MARKET_LUNCH_END_TIME = "12:50:00";
	public static final String STOCK_MARKET_CLOSE_TIME = "15:15:00";

	public static final int STOCK_POSITION_ABOVE = 1;
	public static final int STOCK_POSITION_NONE = 0;
	public static final int STOCK_POSITION_BELOW = -1;

	public static final String STOCK_SE_SH = "sh";
	public static final String STOCK_SE_SZ = "sz";

	public static final int STOCK_VERTEX_TYPING_SIZE = 3;

	public static final int STOCK_VERTEX_NONE = 0;
	public static final int STOCK_VERTEX_TOP = 1 << 0;
	public static final int STOCK_VERTEX_BOTTOM = 1 << 1;
	public static final int STOCK_VERTEX_TOP_STROKE = 1 << 2;
	public static final int STOCK_VERTEX_BOTTOM_STROKE = 1 << 3;
	public static final int STOCK_VERTEX_TOP_SEGMENT = 1 << 4;
	public static final int STOCK_VERTEX_BOTTOM_SEGMENT = 1 << 5;

	public static final int SEASONS_IN_A_YEAR = 4;

	public static final double STOCK_DEAL_DISTRIBUTION_RATE = 5.0 / 100.0;

	public static final long DEFAULT_ALARM_INTERVAL = 5 * 60 * 1000;
	public static final long DEFAULT_RESTART_LOADER_INTERVAL = 1 * 60 * 1000;
	public static final long DEFAULT_SEND_BROADCAST_INTERVAL = 1 * 60 * 1000;
	public static final long DEFAULT_SLEEP_INTERVAL = 3 * 100;

	public static final double ROI_COEFFICIENT = 1.0 / 1000;

	public static final int NOTIFY_B2B2_NET = -5;
	public static final int NOTIFY_S2S2_NET = 5;
	public static final int NOTIFY_S2S2_PROFIT = 100;
}
