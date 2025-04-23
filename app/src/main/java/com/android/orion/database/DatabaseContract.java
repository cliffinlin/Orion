package com.android.orion.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.android.orion.data.Period;

public final class DatabaseContract {
	public static final String AUTHORITY = "com.android.orion";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	public static final long INVALID_ID = 0;

	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "orion";
	public static final String DATABASE_EXT = ".db";
	public static final String DATABASE_FILE_NAME = DATABASE_NAME + DATABASE_EXT;

	public static final String COLUMN_ID = BaseColumns._ID;
	public static final String COLUMN_SE = "se";
	public static final String COLUMN_CODE = "code";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_PINYIN = "pinyin";
	public static final String COLUMN_CLASSES = "classes";
	public static final String COLUMN_FLAG = "flag";
	public static final String COLUMN_PRICE = "price";
	public static final String COLUMN_CHANGE = "change";
	public static final String COLUMN_PREV_NET = "prev_net";
	public static final String COLUMN_NET = "net";
	public static final String COLUMN_NEXT_NET = "next_net";
	public static final String COLUMN_PREDICT = "predict";
	public static final String COLUMN_VOLUME = "volume";
	public static final String COLUMN_VALUE = "value";
	public static final String COLUMN_TEXT = "text";
	public static final String COLUMN_LEVEL = "level";

	public static final String COLUMN_MIN5 = Period.MIN5;
	public static final String COLUMN_MIN15 = Period.MIN15;
	public static final String COLUMN_MIN30 = Period.MIN30;
	public static final String COLUMN_MIN60 = Period.MIN60;
	public static final String COLUMN_DAY = Period.DAY;
	public static final String COLUMN_WEEK = Period.WEEK;
	public static final String COLUMN_MONTH = Period.MONTH;

	public static final String COLUMN_MIN5_LEVEL = COLUMN_MIN5 + "_" + COLUMN_LEVEL;
	public static final String COLUMN_MIN15_LEVEL = COLUMN_MIN15 + "_" + COLUMN_LEVEL;
	public static final String COLUMN_MIN30_LEVEL = COLUMN_MIN30 + "_" + COLUMN_LEVEL;
	public static final String COLUMN_MIN60_LEVEL = COLUMN_MIN60 + "_" + COLUMN_LEVEL;
	public static final String COLUMN_DAY_LEVEL = COLUMN_DAY + "_" + COLUMN_LEVEL;
	public static final String COLUMN_WEEK_LEVEL = COLUMN_WEEK + "_" + COLUMN_LEVEL;
	public static final String COLUMN_MONTH_LEVEL = COLUMN_MONTH + "_" + COLUMN_LEVEL;

	public static final String COLUMN_WEIGHT = "weight";
	public static final String COLUMN_BIAS = "bias";
	public static final String COLUMN_ERROR = "error";
	public static final String COLUMN_DELTA = "delta";
	public static final String COLUMN_TIMES = "times";
	public static final String COLUMN_X_MIN = "x_min";
	public static final String COLUMN_X_MAX = "x_max";
	public static final String COLUMN_Y_MIN = "y_min";
	public static final String COLUMN_Y_MAX = "y_max";
	public static final String COLUMN_STATUS = "status";
	public static final String COLUMN_OPERATE = "operate";
	public static final String COLUMN_BUY = "buy";
	public static final String COLUMN_SELL = "sell";
	public static final String COLUMN_HOLD = "hold";
	public static final String COLUMN_COST = "cost";
	public static final String COLUMN_PROFIT = "profit";
	public static final String COLUMN_FEE = "fee";
	public static final String COLUMN_BONUS = "bonus";
	public static final String COLUMN_VALUATION = "valuation";
	public static final String COLUMN_SHARE = "share";
	public static final String COLUMN_MARKET_VALUE = "market_value";
	public static final String COLUMN_RATE = "rate";
	public static final String COLUMN_ROI = "roi";
	public static final String COLUMN_IR = "ir";
	public static final String COLUMN_IRR = "irr";
	public static final String COLUMN_ROE = "roe";
	public static final String COLUMN_PE = "pe";
	public static final String COLUMN_PB = "pb";
	public static final String COLUMN_DIVIDEND = "dividend";
	public static final String COLUMN_YIELD = "yield";
	public static final String COLUMN_DIVIDEND_RATIO = "dividend_ratio";
	public static final String COLUMN_CREATED = "created";
	public static final String COLUMN_MODIFIED = "modified";
	public static final String COLUMN_INDEX_SE = "index_se";
	public static final String COLUMN_INDEX_CODE = "index_code";
	public static final String COLUMN_INDEX_NAME = "index_name";

	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_PERIOD = "period";
	public static final String COLUMN_OPEN = "open";
	public static final String COLUMN_HIGH = "high";
	public static final String COLUMN_LOW = "low";
	public static final String COLUMN_CLOSE = "close";
	public static final String COLUMN_DIRECTION = "direction";
	public static final String COLUMN_VERTEX = "vertex";
	public static final String COLUMN_AVERAGE5 = "average5";
	public static final String COLUMN_AVERAGE10 = "average10";
	public static final String COLUMN_DIF = "dif";
	public static final String COLUMN_DEA = "dea";
	public static final String COLUMN_HISTOGRAM = "histogram";
	public static final String COLUMN_ACCOUNT = "account";

	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_CONTENT = "content";
	// http://money.finance.sina.com.cn/corp/go.php/vFD_FinanceSummary/stockid/600028.phtml
	public static final String COLUMN_BOOK_VALUE_PER_SHARE = "book_value_per_share";
	public static final String COLUMN_CASH_FLOW_PER_SHARE = "cash_flow_per_share";
	public static final String COLUMN_TOTAL_CURRENT_ASSETS = "total_current_assets";
	public static final String COLUMN_TOTAL_ASSETS = "total_assets";
	public static final String COLUMN_TOTAL_LONG_TERM_LIABILITIES = "total_long_term_liabilities";
	public static final String COLUMN_MAIN_BUSINESS_INCOME = "main_business_income";
	public static final String COLUMN_FINANCIAL_EXPENSES = "financial_expenses";
	public static final String COLUMN_NET_PROFIT = "net_profit";
	public static final String COLUMN_MAIN_BUSINESS_INCOME_IN_YEAR = "main_business_income_in_year";
	public static final String COLUMN_NET_PROFIT_IN_YEAR = "net_profit_in_year";
	public static final String COLUMN_NET_PROFIT_MARGIN = "net_profit_margin";
	public static final String COLUMN_NET_PROFIT_PER_SHARE = "net_profit_per_share";
	public static final String COLUMN_NET_PROFIT_PER_SHARE_IN_YEAR = "net_profit_per_share_in_year";
	public static final String COLUMN_DEBT_TO_NET_ASSETS_RATIO = "debt_to_net_assets_ratio";
	// http://vip.stock.finance.sina.com.cn/corp/go.php/vISSUE_ShareBonus/stockid/600028.phtml
	public static final String COLUMN_R_DATE = "r_date";
	public static final String COLUMN_TIME_TO_MARKET = "time_to_market";

	public static final String ORDER_BY = " ORDER BY ";
	public static final String ORDER_ASC = " ASC ";
	public static final String ORDER_DESC = " DESC ";
	public static final String TEXT_TYPE = " TEXT";
	public static final String INTEGER_TYPE = " INTEGER";
	public static final String DOUBLE_TYPE = " DOUBLE";
	public static final String BLOB_TYPE = " BLOB";
	public static final String UNIQUE_TYPE = " UNIQUE";
	public static final String COMMA_SEP = ",";
	public static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";

	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	private DatabaseContract() {
	}

	public static abstract class Stock implements BaseColumns {
		public static final String TABLE_NAME = "stock";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				DatabaseContract.CONTENT_URI, TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String SORT_ORDER_DEFAULT = COLUMN_NET + " ASC";
		public static final String[] PROJECTION_ALL = {_ID,
				COLUMN_CLASSES, COLUMN_SE, COLUMN_CODE, COLUMN_NAME, COLUMN_PINYIN,
				COLUMN_PRICE, COLUMN_CHANGE, COLUMN_NET, COLUMN_VOLUME, COLUMN_VALUE,
				COLUMN_MONTH, COLUMN_WEEK, COLUMN_DAY, COLUMN_MIN60, COLUMN_MIN30, COLUMN_MIN15, COLUMN_MIN5,
				COLUMN_MONTH_LEVEL, COLUMN_WEEK_LEVEL, COLUMN_DAY_LEVEL, COLUMN_MIN60_LEVEL, COLUMN_MIN30_LEVEL, COLUMN_MIN15_LEVEL, COLUMN_MIN5_LEVEL,
				COLUMN_FLAG, COLUMN_OPERATE,
				COLUMN_ROI, COLUMN_IR, COLUMN_IRR, COLUMN_ROE, COLUMN_PE, COLUMN_PB,
				COLUMN_HOLD, COLUMN_PROFIT, COLUMN_BONUS, COLUMN_VALUATION, COLUMN_COST,
				COLUMN_SHARE, COLUMN_MARKET_VALUE,
				COLUMN_MAIN_BUSINESS_INCOME, COLUMN_MAIN_BUSINESS_INCOME_IN_YEAR,
				COLUMN_NET_PROFIT, COLUMN_NET_PROFIT_IN_YEAR,
				COLUMN_NET_PROFIT_MARGIN,
				COLUMN_DEBT_TO_NET_ASSETS_RATIO,
				COLUMN_BOOK_VALUE_PER_SHARE,
				COLUMN_CASH_FLOW_PER_SHARE,
				COLUMN_NET_PROFIT_PER_SHARE,
				COLUMN_NET_PROFIT_PER_SHARE_IN_YEAR,
				COLUMN_RATE, COLUMN_DIVIDEND, COLUMN_YIELD, COLUMN_DIVIDEND_RATIO, COLUMN_R_DATE, COLUMN_STATUS,
				COLUMN_CREATED, COLUMN_MODIFIED};
		static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
		private static final String CREATE_TABLE_CONTENT = " (" + _ID
				+ " INTEGER PRIMARY KEY,"
				+ COLUMN_CLASSES + TEXT_TYPE + COMMA_SEP
				+ COLUMN_SE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CODE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_NAME + TEXT_TYPE + COMMA_SEP
				+ COLUMN_PINYIN + TEXT_TYPE + COMMA_SEP
				+ COLUMN_PRICE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_CHANGE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_NET + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_VOLUME + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_VALUE + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_MONTH + TEXT_TYPE + COMMA_SEP
				+ COLUMN_WEEK + TEXT_TYPE + COMMA_SEP
				+ COLUMN_DAY + TEXT_TYPE + COMMA_SEP
				+ COLUMN_MIN60 + TEXT_TYPE + COMMA_SEP
				+ COLUMN_MIN30 + TEXT_TYPE + COMMA_SEP
				+ COLUMN_MIN15 + TEXT_TYPE + COMMA_SEP
				+ COLUMN_MIN5 + TEXT_TYPE + COMMA_SEP
				+ COLUMN_MONTH_LEVEL + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_WEEK_LEVEL + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_DAY_LEVEL + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_MIN60_LEVEL + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_MIN30_LEVEL + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_MIN15_LEVEL + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_MIN5_LEVEL + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_FLAG + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_OPERATE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ROI + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_IR + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_IRR + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_ROE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_PE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_PB + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_HOLD + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_PROFIT + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_BONUS + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_VALUATION + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_COST + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_SHARE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_MARKET_VALUE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_MAIN_BUSINESS_INCOME + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_MAIN_BUSINESS_INCOME_IN_YEAR + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_NET_PROFIT + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_NET_PROFIT_IN_YEAR + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_NET_PROFIT_MARGIN + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_DEBT_TO_NET_ASSETS_RATIO + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_BOOK_VALUE_PER_SHARE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_CASH_FLOW_PER_SHARE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_NET_PROFIT_PER_SHARE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_NET_PROFIT_PER_SHARE_IN_YEAR + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_RATE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_DIVIDEND + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_YIELD + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_DIVIDEND_RATIO + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_R_DATE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_STATUS + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CREATED + TEXT_TYPE + COMMA_SEP
				+ COLUMN_MODIFIED + TEXT_TYPE + " )";
		static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ CREATE_TABLE_CONTENT;
	}

	public static abstract class StockData implements BaseColumns {
		public static final String TABLE_NAME = "stock_data";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				DatabaseContract.CONTENT_URI, TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String SORT_ORDER_DEFAULT = COLUMN_CODE
				+ " ASC";

		public static final String[] PROJECTION_ALL = {_ID,
				COLUMN_SE, COLUMN_CODE, COLUMN_NAME,
				COLUMN_PERIOD, COLUMN_DATE, COLUMN_TIME, COLUMN_TEXT,
				COLUMN_OPEN, COLUMN_HIGH, COLUMN_LOW, COLUMN_CLOSE, COLUMN_CHANGE, COLUMN_NET,
				COLUMN_DIRECTION, COLUMN_VERTEX,
				COLUMN_AVERAGE5, COLUMN_AVERAGE10, COLUMN_DIF, COLUMN_DEA, COLUMN_HISTOGRAM,
				COLUMN_CREATED, COLUMN_MODIFIED};
		static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
		private static final String CREATE_TABLE_CONTENT = " (" + _ID
				+ " INTEGER PRIMARY KEY,"
				+ COLUMN_SE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CODE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_NAME + TEXT_TYPE + COMMA_SEP
				+ COLUMN_PERIOD + TEXT_TYPE + COMMA_SEP
				+ COLUMN_DATE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_TIME + TEXT_TYPE + COMMA_SEP
				+ COLUMN_TEXT + TEXT_TYPE + COMMA_SEP
				+ COLUMN_OPEN + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_HIGH + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_LOW + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_CLOSE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_CHANGE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_NET + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_DIRECTION + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_VERTEX + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_AVERAGE5 + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_AVERAGE10 + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_DIF + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_DEA + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_HISTOGRAM + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_CREATED + TEXT_TYPE + COMMA_SEP
				+ COLUMN_MODIFIED + TEXT_TYPE + " )";
		static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ CREATE_TABLE_CONTENT;
	}

	public static abstract class StockTrend implements BaseColumns {
		public static final String TABLE_NAME = "stock_trend";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				DatabaseContract.CONTENT_URI, TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String SORT_ORDER_DEFAULT = COLUMN_CODE
				+ " ASC";

		public static final String[] PROJECTION_ALL = {_ID,
				COLUMN_SE, COLUMN_CODE, COLUMN_NAME,
				COLUMN_PERIOD, COLUMN_DATE, COLUMN_TIME,
				COLUMN_LEVEL, COLUMN_TYPE, COLUMN_FLAG,
				COLUMN_PREV_NET, COLUMN_NET, COLUMN_NEXT_NET, COLUMN_PREDICT,
				COLUMN_CREATED, COLUMN_MODIFIED};
		static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
		private static final String CREATE_TABLE_CONTENT = " (" + _ID
				+ " INTEGER PRIMARY KEY,"
				+ COLUMN_SE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CODE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_NAME + TEXT_TYPE + COMMA_SEP
				+ COLUMN_PERIOD + TEXT_TYPE + COMMA_SEP
				+ COLUMN_DATE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_TIME + TEXT_TYPE + COMMA_SEP
				+ COLUMN_LEVEL + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_TYPE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_FLAG + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_PREV_NET + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_NET + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_NEXT_NET + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_PREDICT + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_CREATED + TEXT_TYPE + COMMA_SEP
				+ COLUMN_MODIFIED + TEXT_TYPE + " )";
		static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ CREATE_TABLE_CONTENT;
	}

	public static abstract class StockPerceptron implements BaseColumns {
		public static final String TABLE_NAME = "stock_perceptron";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				DatabaseContract.CONTENT_URI, TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String SORT_ORDER_DEFAULT = COLUMN_PERIOD + " ASC";

		public static final String[] PROJECTION_ALL = {_ID,
				COLUMN_PERIOD, COLUMN_LEVEL, COLUMN_TYPE,
				COLUMN_WEIGHT, COLUMN_BIAS, COLUMN_ERROR, COLUMN_DELTA, COLUMN_TIMES,
				COLUMN_X_MIN, COLUMN_X_MAX, COLUMN_Y_MIN, COLUMN_Y_MAX,
				COLUMN_CREATED, COLUMN_MODIFIED};
		static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
		private static final String CREATE_TABLE_CONTENT = " (" + _ID
				+ " INTEGER PRIMARY KEY,"
				+ COLUMN_PERIOD + TEXT_TYPE + COMMA_SEP
				+ COLUMN_LEVEL + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_TYPE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_WEIGHT + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_BIAS + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_ERROR + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_DELTA + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_TIMES + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_X_MIN + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_X_MAX + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_Y_MIN + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_Y_MAX + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_CREATED + TEXT_TYPE + COMMA_SEP
				+ COLUMN_MODIFIED + TEXT_TYPE + " )";
		static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ CREATE_TABLE_CONTENT;
	}

	public static abstract class StockDeal implements BaseColumns {
		public static final String TABLE_NAME = "stock_deal";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				DatabaseContract.CONTENT_URI, TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String SORT_ORDER_DEFAULT = COLUMN_CODE + " ASC";

		public static final String[] PROJECTION_ALL = {_ID,
				COLUMN_SE, COLUMN_CODE, COLUMN_NAME,
				COLUMN_ACCOUNT, COLUMN_PRICE, COLUMN_NET,
				COLUMN_BUY, COLUMN_SELL, COLUMN_VOLUME, COLUMN_VALUE, COLUMN_PROFIT,
				COLUMN_FEE, COLUMN_BONUS, COLUMN_YIELD,
				COLUMN_CREATED, COLUMN_MODIFIED};
		static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
		private static final String CREATE_TABLE_CONTENT = " (" + _ID
				+ " INTEGER PRIMARY KEY,"
				+ COLUMN_SE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CODE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_NAME + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACCOUNT + TEXT_TYPE + COMMA_SEP
				+ COLUMN_PRICE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_NET + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_BUY + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_SELL + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_VOLUME + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_VALUE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_PROFIT + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_FEE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_BONUS + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_YIELD + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_CREATED + TEXT_TYPE
				+ COMMA_SEP + COLUMN_MODIFIED + TEXT_TYPE + " )";
		static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ CREATE_TABLE_CONTENT;
	}

	public static abstract class StockFinancial implements BaseColumns {
		public static final String TABLE_NAME = "stock_financial";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				DatabaseContract.CONTENT_URI, TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String SORT_ORDER_DEFAULT = COLUMN_CODE + " ASC";

		public static final String[] PROJECTION_ALL = {_ID,
				COLUMN_SE, COLUMN_CODE, COLUMN_NAME,
				COLUMN_DATE, COLUMN_BOOK_VALUE_PER_SHARE,
				COLUMN_CASH_FLOW_PER_SHARE, COLUMN_TOTAL_CURRENT_ASSETS,
				COLUMN_TOTAL_ASSETS, COLUMN_TOTAL_LONG_TERM_LIABILITIES,
				COLUMN_MAIN_BUSINESS_INCOME, COLUMN_FINANCIAL_EXPENSES,
				COLUMN_NET_PROFIT, COLUMN_SHARE,
				COLUMN_MAIN_BUSINESS_INCOME_IN_YEAR, COLUMN_NET_PROFIT_IN_YEAR, COLUMN_NET_PROFIT_MARGIN,
				COLUMN_DEBT_TO_NET_ASSETS_RATIO, COLUMN_NET_PROFIT_PER_SHARE,
				COLUMN_NET_PROFIT_PER_SHARE_IN_YEAR, COLUMN_RATE, COLUMN_ROE,
				COLUMN_DIVIDEND_RATIO, COLUMN_CREATED, COLUMN_MODIFIED};
		static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
		private static final String CREATE_TABLE_CONTENT = " (" + _ID
				+ " INTEGER PRIMARY KEY,"
				+ COLUMN_SE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CODE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_NAME + TEXT_TYPE + COMMA_SEP
				+ COLUMN_DATE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_BOOK_VALUE_PER_SHARE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_CASH_FLOW_PER_SHARE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_TOTAL_CURRENT_ASSETS + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_TOTAL_ASSETS + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_TOTAL_LONG_TERM_LIABILITIES + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_MAIN_BUSINESS_INCOME + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_FINANCIAL_EXPENSES + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_NET_PROFIT + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_SHARE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_MAIN_BUSINESS_INCOME_IN_YEAR + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_NET_PROFIT_IN_YEAR + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_NET_PROFIT_MARGIN + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_DEBT_TO_NET_ASSETS_RATIO + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_NET_PROFIT_PER_SHARE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_NET_PROFIT_PER_SHARE_IN_YEAR + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_RATE + DOUBLE_TYPE + COMMA_SEP + COLUMN_ROE
				+ DOUBLE_TYPE + COMMA_SEP + COLUMN_DIVIDEND_RATIO + DOUBLE_TYPE
				+ COMMA_SEP + COLUMN_CREATED + TEXT_TYPE + COMMA_SEP
				+ COLUMN_MODIFIED + TEXT_TYPE + " )";
		static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ CREATE_TABLE_CONTENT;
	}

	public static abstract class StockBonus implements BaseColumns {
		public static final String TABLE_NAME = "stock_bonus";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				DatabaseContract.CONTENT_URI, TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String SORT_ORDER_DEFAULT = COLUMN_CODE + " ASC";

		public static final String[] PROJECTION_ALL = {_ID,
				COLUMN_SE, COLUMN_CODE, COLUMN_NAME,
				COLUMN_DATE, COLUMN_DIVIDEND, COLUMN_R_DATE, COLUMN_CREATED,
				COLUMN_MODIFIED};
		static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
		private static final String CREATE_TABLE_CONTENT = " (" + _ID
				+ " INTEGER PRIMARY KEY,"
				+ COLUMN_SE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CODE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_NAME + TEXT_TYPE + COMMA_SEP
				+ COLUMN_DATE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_DIVIDEND + DOUBLE_TYPE + COMMA_SEP + COLUMN_R_DATE
				+ TEXT_TYPE + COMMA_SEP + COLUMN_CREATED + TEXT_TYPE
				+ COMMA_SEP + COLUMN_MODIFIED + TEXT_TYPE + " )";
		static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ CREATE_TABLE_CONTENT;
	}

	public static abstract class StockShare implements BaseColumns {
		public static final String TABLE_NAME = "stock_share";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				DatabaseContract.CONTENT_URI, TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String SORT_ORDER_DEFAULT = COLUMN_CODE + " ASC";

		public static final String[] PROJECTION_ALL = {_ID,
				COLUMN_SE, COLUMN_CODE, COLUMN_NAME,
				COLUMN_DATE, COLUMN_SHARE, COLUMN_CREATED,
				COLUMN_MODIFIED};
		static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
		private static final String CREATE_TABLE_CONTENT = " (" + _ID
				+ " INTEGER PRIMARY KEY,"
				+ COLUMN_SE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CODE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_NAME + TEXT_TYPE + COMMA_SEP
				+ COLUMN_DATE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_SHARE + DOUBLE_TYPE + COMMA_SEP + COLUMN_CREATED
				+ TEXT_TYPE + COMMA_SEP + COLUMN_MODIFIED + TEXT_TYPE + " )";
		static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ CREATE_TABLE_CONTENT;
	}

	public static abstract class IndexComponent implements BaseColumns {
		public static final String TABLE_NAME = "index_component";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				DatabaseContract.CONTENT_URI, TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String[] PROJECTION_ALL = {_ID, COLUMN_INDEX_SE, COLUMN_INDEX_CODE, COLUMN_INDEX_NAME,
				COLUMN_SE, COLUMN_CODE, COLUMN_NAME, COLUMN_CREATED, COLUMN_MODIFIED};
		public static final String SORT_ORDER_DEFAULT = COLUMN_INDEX_CODE
				+ " ASC";
		public static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
		private static final String CREATE_TABLE_CONTENT = " (" + _ID
				+ " INTEGER PRIMARY KEY,"
				+ COLUMN_INDEX_SE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_INDEX_CODE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_INDEX_NAME + TEXT_TYPE + COMMA_SEP
				+ COLUMN_SE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CODE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_NAME + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CREATED + TEXT_TYPE
				+ COMMA_SEP + COLUMN_MODIFIED + TEXT_TYPE + " )";
		static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ CREATE_TABLE_CONTENT;
	}

	public static abstract class TDXData implements BaseColumns {
		public static final String TABLE_NAME = "tdx_data";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				DatabaseContract.CONTENT_URI, TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String SORT_ORDER_DEFAULT = COLUMN_CODE
				+ " ASC";

		public static final String[] PROJECTION_ALL = {_ID,
				COLUMN_SE, COLUMN_CODE, COLUMN_NAME, COLUMN_PERIOD, COLUMN_CONTENT,
				COLUMN_CREATED, COLUMN_MODIFIED};
		static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
		private static final String CREATE_TABLE_CONTENT = " (" + _ID
				+ " INTEGER PRIMARY KEY,"
				+ COLUMN_SE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CODE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_NAME + TEXT_TYPE + COMMA_SEP
				+ COLUMN_PERIOD + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CONTENT + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CREATED + TEXT_TYPE + COMMA_SEP + COLUMN_MODIFIED
				+ TEXT_TYPE + " )";
		static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ CREATE_TABLE_CONTENT;
	}
}