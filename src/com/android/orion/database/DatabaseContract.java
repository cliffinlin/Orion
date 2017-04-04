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
	public static final String COLUMN_PROFIT = "profit";
	public static final String COLUMN_OVERLAP = "overlap";
	public static final String COLUMN_VELOCITY = "velocity";
	public static final String COLUMN_ACCELERATION = "acceleration";
	public static final String COLUMN_CREATED = "created";
	public static final String COLUMN_MODIFIED = "modified";

	public static final String COLUMN_ACTION_1MIN = "action_1min";
	public static final String COLUMN_ACTION_5MIN = "action_5min";
	public static final String COLUMN_ACTION_15MIN = "action_15min";
	public static final String COLUMN_ACTION_30MIN = "action_30min";
	public static final String COLUMN_ACTION_60MIN = "action_60min";
	public static final String COLUMN_ACTION_DAY = "action_day";
	public static final String COLUMN_ACTION_WEEK = "action_week";
	public static final String COLUMN_ACTION_MONTH = "action_month";
	public static final String COLUMN_ACTION_QUARTER = "action_quarter";
	public static final String COLUMN_ACTION_YEAR = "action_year";

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
		public static final String COLUMN_PINYIN_FIXED = "pinyin_fixed";
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
				COLUMN_PINYIN_FIXED, COLUMN_MARK, COLUMN_PRICE, COLUMN_CHANGE,
				COLUMN_NET, COLUMN_VOLUME, COLUMN_VALUE, COLUMN_ACTION_1MIN,
				COLUMN_ACTION_5MIN, COLUMN_ACTION_15MIN, COLUMN_ACTION_30MIN,
				COLUMN_ACTION_60MIN, COLUMN_ACTION_DAY, COLUMN_ACTION_WEEK,
				COLUMN_ACTION_MONTH, COLUMN_ACTION_QUARTER, COLUMN_ACTION_YEAR,
				COLUMN_OVERLAP, COLUMN_VELOCITY, COLUMN_ACCELERATION,
				COLUMN_CREATED, COLUMN_MODIFIED };

		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ " (" + _ID + " INTEGER PRIMARY KEY," + COLUMN_CLASSES
				+ TEXT_TYPE + COMMA_SEP + COLUMN_SE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CODE + TEXT_TYPE + COMMA_SEP + COLUMN_NAME + TEXT_TYPE
				+ COMMA_SEP + COLUMN_PINYIN + TEXT_TYPE + COMMA_SEP
				+ COLUMN_PINYIN_FIXED + TEXT_TYPE + COMMA_SEP + COLUMN_MARK
				+ TEXT_TYPE + COMMA_SEP + COLUMN_PRICE + DOUBLE_TYPE
				+ COMMA_SEP + COLUMN_CHANGE + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_NET + DOUBLE_TYPE + COMMA_SEP + COLUMN_VOLUME
				+ TEXT_TYPE + COMMA_SEP + COLUMN_VALUE + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_1MIN + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_5MIN + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_15MIN + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_30MIN + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_60MIN + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_DAY + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_WEEK + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_MONTH + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_QUARTER + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_YEAR + TEXT_TYPE + COMMA_SEP + COLUMN_OVERLAP
				+ DOUBLE_TYPE + COMMA_SEP + COLUMN_VELOCITY + DOUBLE_TYPE
				+ COMMA_SEP + COLUMN_ACCELERATION + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_CREATED + TEXT_TYPE + COMMA_SEP + COLUMN_MODIFIED
				+ TEXT_TYPE + " )";

		public static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
	}

	public static abstract class StockData implements BaseColumns {
		public static final String TABLE_NAME = "stock_data";

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
		public static final String COLUMN_POSITION = "position";
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
				COLUMN_POSITION, COLUMN_OVERLAP, COLUMN_OVERLAP_LOW,
				COLUMN_OVERLAP_HIGH, COLUMN_AVERAGE5, COLUMN_AVERAGE10,
				COLUMN_DIF, COLUMN_DEA, COLUMN_HISTOGRAM,
				COLUMN_SIGMA_HISTOGRAM, COLUMN_DIVERGENCE,
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
				+ COLUMN_POSITION + INTEGER_TYPE + COMMA_SEP + COLUMN_OVERLAP
				+ DOUBLE_TYPE + COMMA_SEP + COLUMN_OVERLAP_LOW + DOUBLE_TYPE
				+ COMMA_SEP + COLUMN_OVERLAP_HIGH + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_AVERAGE5 + DOUBLE_TYPE + COMMA_SEP + COLUMN_AVERAGE10
				+ DOUBLE_TYPE + COMMA_SEP + COLUMN_DIF + DOUBLE_TYPE
				+ COMMA_SEP + COLUMN_DEA + DOUBLE_TYPE + COMMA_SEP
				+ COLUMN_HISTOGRAM + DOUBLE_TYPE + COMMA_SEP
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
				COLUMN_NET, COLUMN_VOLUME, COLUMN_PROFIT, COLUMN_CREATED,
				COLUMN_MODIFIED };

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

	public static abstract class StockMatch implements BaseColumns {
		public static final String TABLE_NAME = "stock_match";

		public static final String COLUMN_SE_X = COLUMN_SE + "_" + "x";
		public static final String COLUMN_CODE_X = COLUMN_CODE + "_" + "x";
		public static final String COLUMN_NAME_X = COLUMN_NAME + "_" + "x";

		public static final String COLUMN_SE_Y = COLUMN_SE + "_" + "y";
		public static final String COLUMN_CODE_Y = COLUMN_CODE + "_" + "y";
		public static final String COLUMN_NAME_Y = COLUMN_NAME + "_" + "y";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				DatabaseContract.CONTENT_URI, TABLE_NAME);
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/" + DATABASE_NAME + "/" + TABLE_NAME;
		public static final String SORT_ORDER_DEFAULT = COLUMN_CODE + " ASC";

		public static final String[] PROJECTION_ALL = { _ID, COLUMN_SE_X,
				COLUMN_CODE_X, COLUMN_NAME_X, COLUMN_SE_Y, COLUMN_CODE_Y,
				COLUMN_NAME_Y, COLUMN_ACTION_1MIN, COLUMN_ACTION_5MIN,
				COLUMN_ACTION_15MIN, COLUMN_ACTION_30MIN, COLUMN_ACTION_60MIN,
				COLUMN_ACTION_DAY, COLUMN_ACTION_WEEK, COLUMN_ACTION_MONTH,
				COLUMN_ACTION_QUARTER, COLUMN_ACTION_YEAR, COLUMN_CREATED,
				COLUMN_MODIFIED };

		private static final String CREATE_TABLE_CONTENT = " (" + _ID
				+ " INTEGER PRIMARY KEY," + COLUMN_SE_X + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CODE_X + TEXT_TYPE + COMMA_SEP + COLUMN_NAME_X
				+ TEXT_TYPE + COMMA_SEP + COLUMN_SE_Y + TEXT_TYPE + COMMA_SEP
				+ COLUMN_CODE_Y + TEXT_TYPE + COMMA_SEP + COLUMN_NAME_Y
				+ TEXT_TYPE + COMMA_SEP + COLUMN_ACTION_1MIN + TEXT_TYPE
				+ COMMA_SEP + COLUMN_ACTION_5MIN + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_15MIN + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_30MIN + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_60MIN + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_DAY + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_WEEK + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_MONTH + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_QUARTER + TEXT_TYPE + COMMA_SEP
				+ COLUMN_ACTION_YEAR + TEXT_TYPE + COMMA_SEP + COLUMN_CREATED
				+ TEXT_TYPE + COMMA_SEP + COLUMN_MODIFIED + TEXT_TYPE + " )";

		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
				+ CREATE_TABLE_CONTENT;

		public static final String DELETE_TABLE = DROP_TABLE_IF_EXISTS
				+ TABLE_NAME;
	}
}