package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.text.TextUtils;

import com.android.orion.setting.Constant;
import com.android.orion.utility.Utility;

public class StockTrend extends DatabaseTable {

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

	private String mSE;
	private String mCode;
	private String mName;
	private String mPeriod;
	private int mLevel;
	private String mType;
	private int mFlag;
	private double mNet2;
	private double mNet1;
	private double mNet;
	private String mDate;
	private String mTime;

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

	public boolean isEmpty() {
		return TextUtils.isEmpty(mDate)
				&& TextUtils.isEmpty(mTime);
	}

	public void init() {
		super.init();

		setTableName(DatabaseContract.StockTrend.TABLE_NAME);

		mSE = "";
		mCode = "";
		mName = "";
		mPeriod = "";
		mLevel = LEVEL_NONE;
		mType = TYPE_NONE;
		mFlag = FLAG_NONE;
		mNet2 = 0;
		mNet1 = 0;
		mNet = 0;
		mDate = "";
		mTime = "";
	}

	@Override
	public ContentValues getContentValues() {
		ContentValues contentValues = super.getContentValues();

		contentValues.put(DatabaseContract.COLUMN_SE, mSE);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		contentValues.put(DatabaseContract.COLUMN_PERIOD, mPeriod);
		contentValues.put(DatabaseContract.COLUMN_LEVEL, mLevel);
		contentValues.put(DatabaseContract.COLUMN_TYPE, mType);
		contentValues.put(DatabaseContract.COLUMN_FLAG, mFlag);
		contentValues.put(DatabaseContract.COLUMN_NET2, mNet2);
		contentValues.put(DatabaseContract.COLUMN_NET1, mNet1);
		contentValues.put(DatabaseContract.COLUMN_NET, mNet);
		contentValues.put(DatabaseContract.COLUMN_DATE, mDate);
		contentValues.put(DatabaseContract.COLUMN_TIME, mTime);
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

		setSE(stockTrend.mSE);
		setCode(stockTrend.mCode);
		setName(stockTrend.mName);
		setPeriod(stockTrend.mPeriod);
		setLevel(stockTrend.mLevel);
		setType(stockTrend.mType);
		setFlag(stockTrend.mFlag);
		setNet2(stockTrend.mNet2);
		setNet1(stockTrend.mNet1);
		setNet(stockTrend.mNet);
		setDate(stockTrend.mDate);
		setTime(stockTrend.mTime);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		init();

		super.set(cursor);

		setSE(cursor);
		setCode(cursor);
		setName(cursor);
		setPeriod(cursor);
		setLevel(cursor);
		setType(cursor);
		setFlag(cursor);
		setNet2(cursor);
		setNet1(cursor);
		setNet(cursor);
		setDate(cursor);
		setTime(cursor);
	}

	public String getSE() {
		return mSE;
	}

	public void setSE(String se) {
		mSE = se;
	}

	void setSE(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setSE(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_SE)));
	}

	public String getCode() {
		return mCode;
	}

	public void setCode(String code) {
		mCode = code;
	}

	void setCode(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setCode(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_CODE)));
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	void setName(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setName(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NAME)));
	}

	public String getPeriod() {
		return mPeriod;
	}

	public void setPeriod(String period) {
		mPeriod = period;
	}

	void setPeriod(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setPeriod(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PERIOD)));
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

	public double getNet2() {
		return mNet2;
	}

	public void setNet2(double net2) {
		mNet2 = net2;
	}

	void setNet2(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setNet2(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NET2)));
	}

	public double getNet1() {
		return mNet1;
	}

	public void setNet1(double net1) {
		mNet1 = net1;
	}

	void setNet1(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setNet1(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NET1)));
	}

	public double getNet() {
		return mNet;
	}

	public void setNet(double net) {
		mNet = net;
	}

	void setNet(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setNet(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NET)));
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(String date) {
		mDate = date;
	}

	public void setDate(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setDate(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DATE)));
	}

	public String getTime() {
		return mTime;
	}

	public void setTime(String time) {
		mTime = time;
	}

	void setTime(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setTime(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TIME)));
	}

	public String getDateTime() {
		if (!TextUtils.isEmpty(mTime)) {
			return mDate + " " + mTime;
		} else {
			return mDate + " " + "00:00:00";
		}
	}

	public void setDateTime(StockData stockData) {
		if (stockData == null) {
			return;
		}
		setDate(stockData.getDate());
		setTime(stockData.getTime());
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
		return mSE + Constant.TAB
				+ mCode + Constant.TAB
				+ mName + Constant.TAB
				+ mPeriod + Constant.TAB
				+ mLevel + Constant.TAB
				+ mType + Constant.TAB
				+ mFlag + Constant.TAB
				+ mNet2 + Constant.TAB
				+ mNet1 + Constant.TAB
				+ mNet + Constant.TAB
				+ mDate + Constant.TAB
				+ mTime + Constant.TAB;
	}

	public String toTrendString() {
		return getPeriod() + " " + MARK_LEVEL + getLevel() + " " + getType() + " " + (int) mNet1 + "/" + (int) mNet;
	}
}