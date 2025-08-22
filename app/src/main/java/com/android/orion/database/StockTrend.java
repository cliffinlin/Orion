package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.text.TextUtils;

import com.android.orion.utility.Market;
import com.android.orion.utility.Symbol;

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
	public static final String TYPE_NONE_NONE = "NN";
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
			Color.WHITE, Color.GRAY, Color.BLACK, Color.RED,
			Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN};

	public static final String[] TYPES = {
			TYPE_UP_NONE_UP, TYPE_UP_NONE_DOWN, TYPE_UP_NONE,
			TYPE_UP_DOWN, TYPE_UP_UP,
			TYPE_NONE_NONE,
			TYPE_DOWN_DOWN, TYPE_DOWN_UP,
			TYPE_DOWN_NONE, TYPE_DOWN_NONE_UP, TYPE_DOWN_NONE_DOWN};

	public static final int VERTEX_SIZE = 3;
	public static final int ADAPTIVE_SIZE = 8;

	private String mSE;
	private String mCode;
	private String mName;
	private String mPeriod;
	private String mDate;
	private String mTime;

	private int mLevel;
	private String mType;
	private int mFlag;
	private int mDirection;

	private double mTurn;
	private double mPrevNet;
	private double mNet;
	private double mNextNet;
	private double mPredict;

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
		mDate = "";
		mTime = "";

		mLevel = LEVEL_NONE;
		mType = TYPE_NONE;
		mFlag = FLAG_NONE;
		mDirection = DIRECTION_UP;

		mTurn = 0;
		mPrevNet = 0;
		mNet = 0;
		mNextNet = 0;
		mPredict = 0;
	}

	@Override
	public ContentValues getContentValues() {
		ContentValues contentValues = super.getContentValues();

		contentValues.put(DatabaseContract.COLUMN_SE, mSE);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		contentValues.put(DatabaseContract.COLUMN_PERIOD, mPeriod);
		contentValues.put(DatabaseContract.COLUMN_DATE, mDate);
		contentValues.put(DatabaseContract.COLUMN_TIME, mTime);

		contentValues.put(DatabaseContract.COLUMN_LEVEL, mLevel);
		contentValues.put(DatabaseContract.COLUMN_TYPE, mType);
		contentValues.put(DatabaseContract.COLUMN_FLAG, mFlag);
		contentValues.put(DatabaseContract.COLUMN_DIRECTION, mDirection);

		contentValues.put(DatabaseContract.COLUMN_TURN, mTurn);
		contentValues.put(DatabaseContract.COLUMN_PREV_NET, mPrevNet);
		contentValues.put(DatabaseContract.COLUMN_NET, mNet);
		contentValues.put(DatabaseContract.COLUMN_NEXT_NET, mNextNet);
		contentValues.put(DatabaseContract.COLUMN_PREDICT, mPredict);

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
		setDate(stockTrend.mDate);
		setTime(stockTrend.mTime);

		setLevel(stockTrend.mLevel);
		setType(stockTrend.mType);
		setFlag(stockTrend.mFlag);
		setDirection(stockTrend.mDirection);

		setTurn(stockTrend.mTurn);
		setPrevNet(stockTrend.mPrevNet);
		setNet(stockTrend.mNet);
		setNextNet(stockTrend.mNextNet);
		setPredict(stockTrend.mPredict);
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
		setDate(cursor);
		setTime(cursor);

		setLevel(cursor);
		setType(cursor);
		setFlag(cursor);
		setDirection(cursor);

		setTurn(cursor);
		setPrevNet(cursor);
		setNet(cursor);
		setNextNet(cursor);
		setPredict(cursor);
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
			return mDate + " " + Market.SECOND_HALF_END_TIME;
		}
	}

	public void setDateTime(StockData stockData) {
		if (stockData == null) {
			return;
		}
		setDate(stockData.getDate());
		setTime(stockData.getTime());
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

	public int getDirection() {
		return mDirection;
	}

	public void setDirection(int direction) {
		mDirection = direction;
	}

	void setDirection(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setDirection(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIRECTION)));
	}

	public double getTurn() {
		return mTurn;
	}

	public void setTurn(double turn) {
		mTurn = turn;
	}

	void setTurn(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setTurn(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TURN)));
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

	public double getPredict() {
		return mPredict;
	}

	public void setPredict(double predict) {
		mPredict = predict;
	}

	void setPredict(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setPredict(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PREDICT)));
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
		return mSE + Symbol.TAB
				+ mCode + Symbol.TAB
				+ mName + Symbol.TAB
				+ mPeriod + Symbol.TAB
				+ mDate + Symbol.TAB
				+ mTime + Symbol.TAB
				+ mLevel + Symbol.TAB
				+ mType + Symbol.TAB
				+ mFlag + Symbol.TAB
				+ mDirection + Symbol.TAB
				+ mTurn + Symbol.TAB
				+ mPrevNet + Symbol.TAB
				+ mNet + Symbol.TAB
				+ mNextNet + Symbol.TAB
				+ mPredict + Symbol.TAB;
	}

	public String toChartString() {
		return Symbol.L + mLevel + " "
				+ mType + " "
				+ (int)mNet + "/" + (int)mNextNet + Symbol.PERCENT + " "
				+ Symbol.QUESTION + " " + (int)mPredict + Symbol.PERCENT;
	}

	public String toNotifyString() {
		return mPeriod + " " + toChartString();
	}
}