package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;

import com.android.orion.setting.Constant;

public class StockTrend extends Data {

	public static final String LABEL_NONE = "";
	public static final String LABEL_DRAW = "Draw";
	public static final String LABEL_STROKE = "Stroke";
	public static final String LABEL_SEGMENT = "Segment";
	public static final String LABEL_LINE = "Line";
	public static final String LABEL_OUTLINE = "OutLine";
	public static final String LABEL_SUPERLINE = "SuperLine";
	public static final String LABEL_TREND_LINE = "TrendLine";

	public static final int LEVEL_NONE = 0;//DATA
	public static final int LEVEL_DRAW = 1;
	public static final int LEVEL_STROKE = 2;
	public static final int LEVEL_SEGMENT = 3;
	public static final int LEVEL_LINE = 4;
	public static final int LEVEL_OUT_LINE = 5;
	public static final int LEVEL_SUPER_LINE = 6;
	public static final int LEVEL_TREND_LINE = 7;
	public static final int LEVEL_MAX = LEVEL_TREND_LINE + 1;

	public static final int DIRECTION_NONE = 0;
	public static final int DIRECTION_UP = 1 << 0;
	public static final int DIRECTION_DOWN = 1 << 1;

	public static final int VERTEX_NONE = 0;
	public static final int VERTEX_TOP = 1 << 0;
	public static final int VERTEX_BOTTOM = 1 << 1;
	public static final int VERTEX_TOP_STROKE = 1 << 2;
	public static final int VERTEX_BOTTOM_STROKE = 1 << 3;
	public static final int VERTEX_TOP_SEGMENT = 1 << 4;
	public static final int VERTEX_BOTTOM_SEGMENT = 1 << 5;
	public static final int VERTEX_TOP_LINE = 1 << 6;
	public static final int VERTEX_BOTTOM_LINE = 1 << 7;
	public static final int VERTEX_TOP_OUTLINE = 1 << 8;
	public static final int VERTEX_BOTTOM_OUTLINE = 1 << 9;
	public static final int VERTEX_TOP_SUPERLINE = 1 << 10;
	public static final int VERTEX_BOTTOM_SUPERLINE = 1 << 11;
	public static final int VERTEX_TOP_TREND_LINE = 1 << 12;
	public static final int VERTEX_BOTTOM_TREND_LINE = 1 << 13;

	public static final String TYPE_UP_NONE_UP = "UNU";
	public static final String TYPE_UP_NONE_DOWN = "UND";
	public static final String TYPE_UP_NONE = "UN";
	public static final String TYPE_UP_DOWN = "UD";
	public static final String TYPE_UP_UP = "UU";
	public static final String TYPE_NONE = "";
	public static final String TYPE_DOWN_DOWN = "DD";
	public static final String TYPE_DOWN_UP = "DU";
	public static final String TYPE_DOWN_NONE = "DN";
	public static final String TYPE_DOWN_NONE_UP = "DNU";
	public static final String TYPE_DOWN_NONE_DOWN = "DND";

	public static final int FLAG_UNUSED = -1;
	public static final int FLAG_NONE = 0;
	public static final int FLAG_CHANGED = 1 << 0;

	public static final int[] LEVELS = {LEVEL_NONE,
			LEVEL_DRAW, LEVEL_STROKE, LEVEL_SEGMENT, LEVEL_LINE,
			LEVEL_OUT_LINE, LEVEL_SUPER_LINE, LEVEL_TREND_LINE};

	public static final int[] COLORS = {
			Color.WHITE, Color.GRAY, Color.YELLOW, Color.BLACK,
			Color.BLUE, Color.RED, Color.MAGENTA, Color.CYAN};

	public static final String[] TYPES = {
			TYPE_UP_NONE_UP, TYPE_UP_NONE_DOWN, TYPE_UP_NONE,
			TYPE_UP_DOWN, TYPE_UP_UP,
			TYPE_DOWN_DOWN, TYPE_DOWN_UP,
			TYPE_DOWN_NONE, TYPE_DOWN_NONE_UP, TYPE_DOWN_NONE_DOWN};

	public static final String MARK_NONE = "";
	public static final String MARK_BUY = "B";
	public static final String MARK_SELL = "S";
	public static final String MARK_LEVEL = "L";

	public static final int VERTEX_SIZE = 3;
	public static final int ADAPTIVE_SIZE = 8;

	private int mLevel;
	private String mType;
	private int mFlag;
	private double mPrevNet;
	private double mNextNet;

	public StockTrend() {
		init();
	}

	public StockTrend(String period) {
		init();
		setPeriod(period);
	}

	public StockTrend(StockTrend stockTrend) {
		set(stockTrend);
	}

	public StockTrend(Cursor cursor) {
		set(cursor);
	}

	public void init() {
		super.init();

		setTableName(DatabaseContract.StockTrend.TABLE_NAME);

		mLevel = LEVEL_NONE;
		mType = TYPE_NONE;
		mFlag = FLAG_NONE;
		mPrevNet = 0;
		mNextNet = 0;
	}

	@Override
	public ContentValues getContentValues() {
		ContentValues contentValues = super.getContentValues();

		contentValues.put(DatabaseContract.COLUMN_LEVEL, mLevel);
		contentValues.put(DatabaseContract.COLUMN_TYPE, mType);
		contentValues.put(DatabaseContract.COLUMN_FLAG, mFlag);
		contentValues.put(DatabaseContract.COLUMN_PREV_NET, mPrevNet);
		contentValues.put(DatabaseContract.COLUMN_NEXT_NET, mNextNet);

		return contentValues;
	}

	public ContentValues getContentValuesFlag() {
		ContentValues contentValues = super.getContentValues();

		contentValues.put(DatabaseContract.COLUMN_FLAG, mFlag);
		return contentValues;
	}

	public void set(StockTrend stockTrend) {
		if (stockTrend == null) {
			return;
		}

		init();

		super.set(stockTrend);

		setLevel(stockTrend.mLevel);
		setType(stockTrend.mType);
		setFlag(stockTrend.mFlag);
		setPrevNet(stockTrend.mPrevNet);
		setNextNet(stockTrend.mNextNet);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		init();

		super.set(cursor);

		setLevel(cursor);
		setType(cursor);
		setFlag(cursor);
		setPrevNet(cursor);
		setNextNet(cursor);
	}

	public int getLevel() {
		return mLevel;
	}

	public void setLevel(int level) {
		mLevel = level;
	}

	void setLevel(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setLevel(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_LEVEL)));
	}

	public String getType() {
		return mType;
	}

	public void setType(String type) {
		mType = type;
	}

	void setType(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setType(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TYPE)));
	}

	public int getFlag() {
		return mFlag;
	}

	public void setFlag(int flag) {
		mFlag = flag;
	}

	void setFlag(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setFlag(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_FLAG)));
	}

	public double getPrevNet() {
		return mPrevNet;
	}

	public void setPrevNet(double prevNet) {
		mPrevNet = prevNet;
	}

	void setPrevNet(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setPrevNet(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PREV_NET)));
	}

	public double getNextNet() {
		return mNextNet;
	}

	public void setNextNet(double nextNet) {
		mNextNet = nextNet;
	}

	void setNextNet(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setNextNet(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NEXT_NET)));
	}

	public void addFlag(int flag) {
		mFlag |= flag;
	}

	public void removeFlag(int flag) {
		if (hasFlag(flag)) {
			mFlag &= ~flag;
		}
	}

	public boolean hasFlag(int flag) {
		return (mFlag & flag) == flag;
	}

	public String toString() {
		return getSE() + Constant.TAB
				+ getCode() + Constant.TAB
				+ getName() + Constant.TAB
				+ getPeriod() + Constant.TAB
				+ getDate() + Constant.TAB
				+ getTime() + Constant.TAB
				+ mLevel + Constant.TAB
				+ mType + Constant.TAB
				+ mFlag + Constant.TAB
				+ mPrevNet + Constant.TAB
				+ getNet() + Constant.TAB
				+ mNextNet + Constant.TAB;
	}

	public String toTrendString() {
		return MARK_LEVEL + getLevel() + " " + getType() + " " + (int) getNet() + "/" + (int) getNextNet();
	}
}