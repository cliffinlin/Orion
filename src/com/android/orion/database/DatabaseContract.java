package com.android.orion.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class DatabaseContract {
	public static final String AUTHORITY = "com.android.orion";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "orion";
	public static final String DATABASE_FILE = DATABASE_NAME + ".db";

	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String DOUBLE_TYPE = " DOUBLE";
	// private static final String BLOB_TYPE = " BLOB";
	// private static final String UNIQUE_TYPE = " UNIQUE";
	private static final String COMMA_SEP = ",";

	public static final String COLUMN_ID = BaseColumns._ID;
	public static final String COLUMN_STOCK_ID = "stock_id";
	public static final String COLUMN_SE = "se";
	public static final String COLUMN_CODE = "code";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_PRICE = "price";
	public static final String COLUMN_DEAL = "deal";
	public static final String COLUMN_CHANGE = "change";
	public static final String COLUMN_NET = "net";
	public static final String COLUMN_VOLUME = "volume";
	public static final String COLUMN_VALUE = "value";
	public static final String COLUMN_OVERLAP = "overlap";
	public static final String COLUMN_VELOCITY = "velocity";
	public static final String COLUMN_ACCELERATION = "acceleration";
	public static final String COLUMN_MIN1 = "min1";
	public static final String COLUMN_MIN5 = "min5";
	public static final String COLUMN_MIN15 = "min15";
	public static final String COLUMN_MIN30 = "min30";
	public static final String COLUMN_MIN60 = "min60";
	public static final String COLUMN_DAY = "day";
	public static final String COLUMN_WEEK = "week";
	public static final String COLUMN_MONTH = "month";
	public static final String COLUMN_QUARTER = "quarter";
	public static final String COLUMN_YEAR = "year";
	public static final String COLUMN_OPERATION = "operation";
	public static final String COLUMN_HOLD = "hold";
	public static final String COLUMN_COST = "cost";
	public static final String COLUMN_PROFIT = "profit";
	public static final String COLUMN_TOTAL_SHARE = "total_share";
	public static final String COLUMN_ROE = "roe";
	public static final String COLUMN_PE = "pe";
	public static final String COLUMN_PB = "pb";
	public static final String COLUMN_DIVIDEND = "dividend";
	public static final String COLUMN_YIELD = "yield";
	public static final String COLUMN_DELTA = "delta";
	public static final String COLUMN_VALUATION = "valuation";
	public static final String COLUMN_DISCOUNT = "discount";
	public static final String COLUMN_CREATED = "created";
	public static final String COLUMN_MODIFIED = "modified";

	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_PERIOD = "period";
	public static final String COLUMN_OPEN = "open";
	public static final String COLUMN_HIGH = "high";
	public static final String COLUMN_LOW = "low";
	public static final String COLUMN_CLOSE = "close";
	public static final String COLUMN_DIRECTION = "direction";
	public static final String COLUMN_VERTEX = "vertex";
	public static final String COLUMN_VERTEX_LOW = "vertex_low";
	public static final String COLUMN_VERTEX_HIGH = "vertex_high";
	public static final String COLUMN_OVERLAP_LOW = "overlap_low";
	public static final String COLUMN_OVERLAP_HIGH = "overlap_high";
	public static final String COLUMN_AVERAGE5 = "average5";
	public static final String COLUMN_AVERAGE10 = "average10";
	public static final String COLUMN_DIF = "dif";
	public static final String COLUMN_DEA = "dea";
	public static final String COLUMN_HISTOGRAM = "histogram";
	public static final String COLUMN_SIGMA_HISTOGRAM = "sigma_histogram";
	public static final String COLUMN_DIVERGENCE = "divergence";
	public static final String COLUMN_TRENDS_EFFORTS = "trends_efforts";
	public static final String COLUMN_AVERAGE = "average";
	public static final String COLUMN_ACTION = "action";

	// http://money.finance.sina.com.cn/corp/go.php/vFD_FinanceSummary/stockid/600028.phtml
	public static final String COLUMN_BOOK_VALUE_PER_SHARE = "book_value_per_share";// 每股净资产-摊薄/期末股数
	public static final String COLUMN_EARNINGS_PER_SHARE = "earnings_per_share";// 每股收益-摊薄/期末股数
	public static final String COLUMN_CASH_FLOW_PER_SHARE = "cash_flow_per_share";// 每股现金流
	public static final String COLUMN_CURRENT_ASSETS = "current_assets";// 流动资产合计
	public static final String COLUMN_TOTAL_ASSETS = "total_assets";// 资产总计
	public static final String COLUMN_TOTAL_LONG_TERM_LIABILITIES = "total_long_term_liabilities";// 长期负债合计
	public static final String COLUMN_MAIN_BUSINESS_INCOME = "main_business_income";// 主营业务收入
	public static final String COLUMN_FINANCIAL_EXPENSES = "financial_expenses";// 财务费用
	public static final String COLUMN_NET_PROFIT = "net_profit";// 净利润

	// http://vip.stock.finance.sina.com.cn/corp/go.php/vISSUE_ShareBonus/stockid/600028.phtml
	public static final String COLUMN_DIVIDEND_DATE = "dividend_date";

	public static final String COLUMN_TIME_TO_MARKET = "time_to_market";

	private static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";

	public static final String ORDER_BY = " ORDER BY ";
	public static final String ORDER_DIRECTION_ASC = " ASC ";
	public static final String ORDER_DIRECTION_DESC = " DESC ";

	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	private DatabaseContract() {
	}

	public static abstract class Setting implements BaseColumns {
		public static final String TABLE_NAME = "setting";

		public static final String COLUMN_KEY = "key";
		public static final String COLUMN_VALUE = "value";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				DatabaseContract.CONTENT_URI, TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String SORT_ORDER_DEFAULT = COLUMN_KEY + " ASC";
		public static final String[] PROJECTION_ALL = { _ID, COLUMN_KEY,
				COLUMN_VALUE, COLUMN_CREATED, COLUMN_MODIFIED };

		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ " (" + _ID + " INTEGER PRIMARY KEY," + COLUMN_KEY + TEXT_TYPE
				+ COMMA_SEP + COLUMN_VALUE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CREATED + TEXT_TYPE + COMMA_SEP + COLUMN_MODIFIED
				+ TEXT_TYPE + " )";

		public static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
	}

	public static abstract class Stock implements BaseColumns {
		public static final String TABLE_NAME = "stock";

		public static final String COLUMN_CLASSES = "classes";
		public static final String COLUMN_PINYIN = "pinyin";
		public static final String COLUMN_MARK = "mark";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				DatabaseContract.CONTENT_URI, TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String SORT_ORDER_DEFAULT = COLUMN_CODE + " ASC";
		public static final String[] PROJECTION_ALL = { _ID, COLUMN_CLASSES,
				COLUMN_SE, COLUMN_CODE, COLUMN_NAME, COLUMN_PINYIN,
				COLUMN_MARK, COLUMN_PRICE, COLUMN_CHANGE, COLUMN_NET,
				COLUMN_VOLUME, COLUMN_VALUE, COLUMN_MIN1, COLUMN_MIN5,
				COLUMN_MIN15, COLUMN_MIN30, COLUMN_MIN60, COLUMN_DAY,
				COLUMN_WEEK, COLUMN_MONTH, COLUMN_QUARTER, COLUMN_YEAR,
				COLUMN_OPERATION, COLUMN_HOLD, COLUMN_COST, COLUMN_PROFIT,
				COLUMN_TOTAL_SHARE, COLUMN_ROE, COLUMN_PE, COLUMN_PB,
				COLUMN_DIVIDEND, COLUMN_YIELD, COLUMN_DELTA, COLUMN_VALUATION,
				COLUMN_DISCOUNT, COLUMN_CREATED, COLUMN_MODIFIED };

		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ " (" + _ID + " INTEGER PRIMARY KEY," + COLUMN_CLASSES
				+ TEXT_TYPE + COMMA_SEP + COLUMN_SE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CODE + TEXT_TYPE + COMMA_SEP + COLUMN_NAME + TEXT_TYPE
				+ COMMA_SEP + COLUMN_PINYIN + TEXT_TYPE + COMMA_SEP
				+ COLUMN_MARK + TEXT_TYPE + COMMA_SEP + COLUMN_PRICE
				+ DOUBLE_TYPE + COMMA_SEP + COLUMN_CHANGE + DOUBLE_TYPE
				+ COMMA_SEP + COLUMN_NET + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_VOLUME + INTEGER_TYPE + COMMA_SEP + COLUMN_VALUE
				+ INTEGER_TYPE + COMMA_SEP + COLUMN_MIN1 + TEXT_TYPE
				+ COMMA_SEP + COLUMN_MIN5 + TEXT_TYPE + COMMA_SEP
				+ COLUMN_MIN15 + TEXT_TYPE + COMMA_SEP + COLUMN_MIN30
				+ TEXT_TYPE + COMMA_SEP + COLUMN_MIN60 + TEXT_TYPE + COMMA_SEP
				+ COLUMN_DAY + TEXT_TYPE + COMMA_SEP + COLUMN_WEEK + TEXT_TYPE
				+ COMMA_SEP + COLUMN_MONTH + TEXT_TYPE + COMMA_SEP
				+ COLUMN_QUARTER + TEXT_TYPE + COMMA_SEP + COLUMN_YEAR
				+ TEXT_TYPE + COMMA_SEP + COLUMN_OPERATION + TEXT_TYPE
				+ COMMA_SEP + COLUMN_HOLD + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_COST + DOUBLE_TYPE + COMMA_SEP + COLUMN_PROFIT
				+ DOUBLE_TYPE + COMMA_SEP + COLUMN_TOTAL_SHARE + DOUBLE_TYPE
				+ COMMA_SEP + COLUMN_ROE + DOUBLE_TYPE + COMMA_SEP + COLUMN_PE
				+ DOUBLE_TYPE + COMMA_SEP + COLUMN_PB + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_DIVIDEND + DOUBLE_TYPE + COMMA_SEP + COLUMN_YIELD
				+ DOUBLE_TYPE + COMMA_SEP + COLUMN_DELTA + DOUBLE_TYPE
				+ COMMA_SEP + COLUMN_VALUATION + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_DISCOUNT + DOUBLE_TYPE + COMMA_SEP + COLUMN_CREATED
				+ TEXT_TYPE + COMMA_SEP + COLUMN_MODIFIED + TEXT_TYPE + " )";

		public static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
	}

	public static abstract class StockData implements BaseColumns {
		public static final String TABLE_NAME = "stock_data";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				DatabaseContract.CONTENT_URI, TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String SORT_ORDER_DEFAULT = COLUMN_STOCK_ID
				+ " ASC";

		public static final String[] PROJECTION_ALL = { _ID, COLUMN_STOCK_ID,
				COLUMN_DATE, COLUMN_TIME, COLUMN_PERIOD, COLUMN_OPEN,
				COLUMN_HIGH, COLUMN_LOW, COLUMN_CLOSE, COLUMN_DIRECTION,
				COLUMN_VERTEX, COLUMN_VERTEX_LOW, COLUMN_VERTEX_HIGH,
				COLUMN_OVERLAP, COLUMN_OVERLAP_LOW, COLUMN_OVERLAP_HIGH,
				COLUMN_AVERAGE5, COLUMN_AVERAGE10, COLUMN_DIF, COLUMN_DEA,
				COLUMN_HISTOGRAM, COLUMN_SIGMA_HISTOGRAM, COLUMN_DIVERGENCE,
				COLUMN_TRENDS_EFFORTS, COLUMN_AVERAGE, COLUMN_VELOCITY,
				COLUMN_ACCELERATION, COLUMN_ACTION, COLUMN_CREATED,
				COLUMN_MODIFIED };

		private static final String CREATE_TABLE_CONTENT = " (" + _ID
				+ " INTEGER PRIMARY KEY," + COLUMN_STOCK_ID + TEXT_TYPE
				+ COMMA_SEP + COLUMN_DATE + TEXT_TYPE + COMMA_SEP + COLUMN_TIME
				+ TEXT_TYPE + COMMA_SEP + COLUMN_PERIOD + TEXT_TYPE + COMMA_SEP
				+ COLUMN_OPEN + DOUBLE_TYPE + COMMA_SEP + COLUMN_HIGH
				+ DOUBLE_TYPE + COMMA_SEP + COLUMN_LOW + DOUBLE_TYPE
				+ COMMA_SEP + COLUMN_CLOSE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_DIRECTION + INTEGER_TYPE + COMMA_SEP + COLUMN_VERTEX
				+ INTEGER_TYPE + COMMA_SEP + COLUMN_VERTEX_LOW + DOUBLE_TYPE
				+ COMMA_SEP + COLUMN_VERTEX_HIGH + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_OVERLAP + DOUBLE_TYPE + COMMA_SEP + COLUMN_OVERLAP_LOW
				+ DOUBLE_TYPE + COMMA_SEP + COLUMN_OVERLAP_HIGH + DOUBLE_TYPE
				+ COMMA_SEP + COLUMN_AVERAGE5 + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_AVERAGE10 + DOUBLE_TYPE + COMMA_SEP + COLUMN_DIF
				+ DOUBLE_TYPE + COMMA_SEP + COLUMN_DEA + DOUBLE_TYPE
				+ COMMA_SEP + COLUMN_HISTOGRAM + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_SIGMA_HISTOGRAM + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_DIVERGENCE + INTEGER_TYPE + COMMA_SEP
				+ COLUMN_TRENDS_EFFORTS + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_AVERAGE + DOUBLE_TYPE + COMMA_SEP + COLUMN_VELOCITY
				+ DOUBLE_TYPE + COMMA_SEP + COLUMN_ACCELERATION + DOUBLE_TYPE
				+ COMMA_SEP + COLUMN_ACTION + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CREATED + TEXT_TYPE + COMMA_SEP + COLUMN_MODIFIED
				+ TEXT_TYPE + " )";

		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ CREATE_TABLE_CONTENT;

		public static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
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

		public static final String[] PROJECTION_ALL = { _ID, COLUMN_SE,
				COLUMN_CODE, COLUMN_NAME, COLUMN_PRICE, COLUMN_DEAL,
				COLUMN_NET, COLUMN_VOLUME, COLUMN_PROFIT, COLUMN_PROFIT,
				COLUMN_CREATED, COLUMN_MODIFIED };

		private static final String CREATE_TABLE_CONTENT = " (" + _ID
				+ " INTEGER PRIMARY KEY," + COLUMN_SE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CODE + TEXT_TYPE + COMMA_SEP + COLUMN_NAME + TEXT_TYPE
				+ COMMA_SEP + COLUMN_PRICE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_DEAL + DOUBLE_TYPE + COMMA_SEP + COLUMN_NET
				+ DOUBLE_TYPE + COMMA_SEP + COLUMN_VOLUME + INTEGER_TYPE
				+ COMMA_SEP + COLUMN_PROFIT + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_CREATED + TEXT_TYPE + COMMA_SEP + COLUMN_MODIFIED
				+ TEXT_TYPE + " )";

		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ CREATE_TABLE_CONTENT;

		public static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
	}

	public static abstract class FinancialData implements BaseColumns {
		public static final String TABLE_NAME = "financial_data";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				DatabaseContract.CONTENT_URI, TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String SORT_ORDER_DEFAULT = COLUMN_STOCK_ID
				+ " ASC";

		public static final String[] PROJECTION_ALL = { _ID, COLUMN_STOCK_ID,
				COLUMN_DATE, COLUMN_BOOK_VALUE_PER_SHARE,
				COLUMN_EARNINGS_PER_SHARE, COLUMN_CASH_FLOW_PER_SHARE,
				COLUMN_CURRENT_ASSETS, COLUMN_TOTAL_ASSETS,
				COLUMN_TOTAL_LONG_TERM_LIABILITIES,
				COLUMN_MAIN_BUSINESS_INCOME, COLUMN_FINANCIAL_EXPENSES,
				COLUMN_NET_PROFIT, COLUMN_CREATED, COLUMN_MODIFIED };

		private static final String CREATE_TABLE_CONTENT = " (" + _ID
				+ " INTEGER PRIMARY KEY," + COLUMN_STOCK_ID + TEXT_TYPE
				+ COMMA_SEP + COLUMN_DATE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_BOOK_VALUE_PER_SHARE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_EARNINGS_PER_SHARE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_CASH_FLOW_PER_SHARE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_CURRENT_ASSETS + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_TOTAL_ASSETS + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_TOTAL_LONG_TERM_LIABILITIES + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_MAIN_BUSINESS_INCOME + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_FINANCIAL_EXPENSES + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_NET_PROFIT + DOUBLE_TYPE + COMMA_SEP + COLUMN_CREATED
				+ TEXT_TYPE + COMMA_SEP + COLUMN_MODIFIED + TEXT_TYPE + " )";

		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ CREATE_TABLE_CONTENT;

		public static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
	}

	public static abstract class ShareBonus implements BaseColumns {
		public static final String TABLE_NAME = "share_bonus";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				DatabaseContract.CONTENT_URI, TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String SORT_ORDER_DEFAULT = COLUMN_STOCK_ID
				+ " ASC";

		public static final String[] PROJECTION_ALL = { _ID, COLUMN_STOCK_ID,
				COLUMN_DATE, COLUMN_DIVIDEND, COLUMN_DIVIDEND_DATE,
				COLUMN_CREATED, COLUMN_MODIFIED };

		private static final String CREATE_TABLE_CONTENT = " (" + _ID
				+ " INTEGER PRIMARY KEY," + COLUMN_STOCK_ID + TEXT_TYPE
				+ COMMA_SEP + COLUMN_DATE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_DIVIDEND + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_DIVIDEND_DATE + TEXT_TYPE + COMMA_SEP + COLUMN_CREATED
				+ TEXT_TYPE + COMMA_SEP + COLUMN_MODIFIED + TEXT_TYPE + " )";

		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ CREATE_TABLE_CONTENT;

		public static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
	}

	public static abstract class IPO implements BaseColumns {
		public static final String TABLE_NAME = "ipo";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				DatabaseContract.CONTENT_URI, TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String SORT_ORDER_DEFAULT = COLUMN_DATE + " ASC";

		public static final String[] PROJECTION_ALL = { _ID, COLUMN_STOCK_ID,
				COLUMN_CODE, COLUMN_NAME, COLUMN_PRICE, COLUMN_DATE,
				COLUMN_TIME_TO_MARKET, COLUMN_PE, COLUMN_CREATED,
				COLUMN_MODIFIED };

		private static final String CREATE_TABLE_CONTENT = " (" + _ID
				+ " INTEGER PRIMARY KEY," + COLUMN_STOCK_ID + TEXT_TYPE
				+ COMMA_SEP + COLUMN_CODE + TEXT_TYPE + COMMA_SEP + COLUMN_NAME
				+ TEXT_TYPE + COMMA_SEP + COLUMN_PRICE + DOUBLE_TYPE
				+ COMMA_SEP + COLUMN_DATE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_TIME_TO_MARKET + TEXT_TYPE + COMMA_SEP + COLUMN_PE
				+ DOUBLE_TYPE + COMMA_SEP + COLUMN_CREATED + TEXT_TYPE
				+ COMMA_SEP + COLUMN_MODIFIED + TEXT_TYPE + " )";

		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ CREATE_TABLE_CONTENT;

		public static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
	}
}