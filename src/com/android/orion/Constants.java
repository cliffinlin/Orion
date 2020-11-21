package com.android.orion;

public class Constants {
	public static final String APP_NAME = "Orion";
	public static final String TAG = APP_NAME;

	public static final int FAIL = -1;
	public static final int SUCCESS = 0;

	public static final String STOCK_CLASSES_INDEX = "I";
	public static final String STOCK_CLASSES_A = "A";

	public static final String FAVORITE = "favorite";
	public static final String XML_FILE_EXT = ".xml";

	public static final String ACTION_SERVICE_FINISHED = "orion.intent.action.SERVICE_FINISHED";

	public static final long STOCK_DOWNLOAD_ALARM_INTERVAL_DEFAULT = 5 * 60000;

	public static final int BENZIER_CURVE_GRADE_MAX = 50;

	public static final int DOUBLE_CONSTANT_WAN = 10000;
	public static final int DOUBLE_CONSTANT_YI = (DOUBLE_CONSTANT_WAN * DOUBLE_CONSTANT_WAN);
	public static final int DOUBLE_FIXED_DECIMAL = 2;

	public static final int DOWNLOAD_HISTORY_LENGTH_DEFAULT = 120;
	public static final int DOWNLOAD_HISTORY_LENGTH_NONE = 0;
	public static final int DOWNLOAD_HISTORY_LENGTH_UNLIMITED = -1;

	public static final int EXECUTE_TYPE_NONE = 0;
	public static final int EXECUTE_IMMEDIATE = 1;
	public static final int EXECUTE_SCHEDULE = 2;
	public static final int EXECUTE_SCHEDULE_MIN1 = EXECUTE_SCHEDULE << 0;
	public static final int EXECUTE_SCHEDULE_MIN5 = EXECUTE_SCHEDULE << 1;
	public static final int EXECUTE_SCHEDULE_MIN15 = EXECUTE_SCHEDULE << 2;
	public static final int EXECUTE_SCHEDULE_MIN30 = EXECUTE_SCHEDULE << 3;
	public static final int EXECUTE_SCHEDULE_MIN60 = EXECUTE_SCHEDULE << 4;

	public static final String EXTRA_EXECUTE_TYPE = "execute_type";
	public static final String EXTRA_SERVICE_TYPE = "service_type";
	public static final String EXTRA_STOCK_ID = "stock_id";
	public static final String EXTRA_STOCK_SE = "stock_se";
	public static final String EXTRA_STOCK_CODE = "stock_code";
	public static final String EXTRA_STOCK_DEAL_PRICE = "stock_deal_price";
	public static final String EXTRA_STOCK_DEAL_VOLUME = "stock_deal_volume";
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

	public static final int SERVICE_TYPE_NONE = -1;
	public static final int SERVICE_DOWNLOAD_STOCK_FAVORITE = 0;
	public static final int SERVICE_DATABASE_UPDATE = 1;

	public static final String SETTING_SHARED_PREFERENCE = "setting";
	public static final String SETTING_KEY_ALARM = "alarm";
	public static final String SETTING_KEY_LIMIT_LINE = "limit_line";
	public static final String SETTING_KEY_CURRENT_PERIOD = "current_period";
	public static final String SETTING_KEY_CONNECTION_WIFI_ONLY = "connection_wifi_only";
	public static final String SETTING_KEY_NOTIFICATION_MESSAGE = "notification_message";
	public static final String SETTING_KEY_NOTIFICATION_LIGHTS = "notification_lights";
	public static final String SETTING_KEY_NOTIFICATION_VIBRATE = "notification_vibrate";
	public static final String SETTING_KEY_NOTIFICATION_SOUND = "notification_sound";
	public static final String SETTING_KEY_USER_NAME = "user_name";

	public static final String STOCK_ACTION_NONE = "";
	public static final String STOCK_ACTION_STAR = "*";
	public static final String STOCK_ACTION_DX = "DX";
	public static final String STOCK_ACTION_BUY = "B";
	public static final String STOCK_ACTION_BUY1 = "1B";
	public static final String STOCK_ACTION_BUY2 = "2B";
	public static final String STOCK_ACTION_BUY3 = "3B";
	public static final String STOCK_ACTION_GX = "GX";
	public static final String STOCK_ACTION_SELL = "S";
	public static final String STOCK_ACTION_SELL1 = "1S";
	public static final String STOCK_ACTION_SELL2 = "2S";
	public static final String STOCK_ACTION_SELL3 = "3S";
	public static final char STOCK_ACTION_UP = '¡ü';
	public static final char STOCK_ACTION_DOWN = '¡ý';
	public static final char STOCK_ACTION_ADD = '+';
	public static final char STOCK_ACTION_MINUS = '-';

	public static final int STOCK_BREAKPOINT_TOP = 1;
	public static final int STOCK_BREAKPOINT_NONE = 0;
	public static final int STOCK_BREAKPOINT_BOTTOM = -1;

	public static final int STOCK_DIRECTION_UP = 1;
	public static final int STOCK_DIRECTION_NONE = 0;
	public static final int STOCK_DIRECTION_DOWN = -1;

	public static final int STOCK_DIVERGENCE_NONE = 0;
	public static final int STOCK_DIVERGENCE_HISTOGRAM = 1 << 0;
	public static final int STOCK_DIVERGENCE_DIF = 1 << 1;
	public static final int STOCK_DIVERGENCE_SIGMA_HISTOGRAM = 1 << 2;

	public static final int STOCK_FLAG_NONE = 0;
	public static final int STOCK_FLAG_FAVORITE = 1 << 0;
	public static final int STOCK_FLAG_RECENT = 1 << 2;

	public static final int STOCK_ID_INVALID = 0;

	public static final String STOCK_CLASS_HSA = "hsa";
	public static final String STOCK_CLASS_INDEXES = "indexes";

	public static final String STOCK_INDEXES_CODE_BASE_SH = "000000";
	public static final String STOCK_INDEXES_CODE_BASE_SZ = "399000";

	public static final int STOCK_MARKET_OPEN_MINUTES = 9 * 60 + 30;
	public static final int STOCK_MARKET_LUNCH_MINUTES = 1 * 60 + 30;

	public static final String STOCK_MARKET_OPEN_TIME = "09:30:00";
	public static final String STOCK_MARKET_LUNCH_BEGIN_TIME = "11:35:00";
	public static final String STOCK_MARKET_LUNCH_END_TIME = "13:00:00";
	public static final String STOCK_MARKET_CLOSE_TIME = "15:05:00";

	public static final int STOCK_OVERLAP_MOVE_UP = 1;
	public static final int STOCK_OVERLAP_MOVE_NONE = 0;
	public static final int STOCK_OVERLAP_MOVE_DOWN = -1;

	public static final int STOCK_POSITION_ABOVE = 1;
	public static final int STOCK_POSITION_NONE = 0;
	public static final int STOCK_POSITION_BELOW = -1;

	public static final String STOCK_SE_SH = "sh";
	public static final String STOCK_SE_SZ = "sz";

	public static final int STOCK_SEGMENT_UP = 1;
	public static final int STOCK_SEGMENT_NONE = 0;
	public static final int STOCK_SEGMENT_DOWN = -1;

	public static final int STOCK_VERTEX_TYPING_SIZE = 3;
	public static final int STOCK_STROKE_TYPING_SIZE = STOCK_VERTEX_TYPING_SIZE;

	public static final int STOCK_VERTEX_NONE = 0;
	public static final int STOCK_VERTEX_TOP = 1 << 0;
	public static final int STOCK_VERTEX_BOTTOM = 1 << 1;
	public static final int STOCK_VERTEX_TOP_STROKE = 1 << 2;
	public static final int STOCK_VERTEX_BOTTOM_STROKE = 1 << 3;
	public static final int STOCK_VERTEX_TOP_SEGMENT = 1 << 4;
	public static final int STOCK_VERTEX_BOTTOM_SEGMENT = 1 << 5;

	public static final int SEASONS_IN_A_YEAR = 4;

	public static final double STOCK_DEAL_DISTRIBUTION_RATE = 5.0 / 100.0;

	public static final long DEFAULT_DOWNLOAD_INTERVAL = 3 * 1000;
	public static final long DEFAULT_RESTART_LOADER_INTERAL = 10 * 1000;
	public static final long DEFAULT_SEND_BROADCAST_INTERAL = 15 * 1000;

	public static final double ROI_COEFFICIENT = 1.0 / 10;
}
