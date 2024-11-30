package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.android.orion.chart.CandlestickChart;
import com.android.orion.data.Macd;
import com.android.orion.data.Period;
import com.android.orion.setting.Constant;
import com.android.orion.utility.Utility;

import java.util.Calendar;
import java.util.Comparator;

public class StockData extends DatabaseTable {

	public static final String MARK_NONE = "";

	public static final String MARK_BUY = "B";
	public static final String MARK_SELL = "S";
	public static final String MARK_BUY1 = "B1";
	public static final String MARK_BUY2 = "B2";
	public static final String MARK_SELL1 = "S1";
	public static final String MARK_SELL2 = "S2";

	public static final String MARK_G = "G";
	public static final String MARK_D = "D";

	public static final String MARK_HIGH = "H";
	public static final String MARK_LOW = "L";

	public static final char MARK_ADD = '+';
	public static final char MARK_MINUS = '-';

	public static final String MARK_STAR = "*";

	public static final String MARK_NATURAL_RALLY = "NG";
	public static final String MARK_UPWARD_TREND = "GG";
	public static final String MARK_DOWNWARD_TREND = "DD";
	public static final String MARK_NATURAL_REACTION = "ND";

	public static final String NAME_NATURAL_RALLY = "Rally";
	public static final String NAME_UPWARD_TREND = "Up";
	public static final String NAME_DOWNWARD_TREND = "Down";
	public static final String NAME_NATURAL_REACTION = "React";

	public static final int LEVEL_NONE = 0;
	public static final int LEVEL_1 = 1;//DRAW
	public static final int LEVEL_2 = 2;//STROKE
	public static final int LEVEL_3 = 3;//SEGMENT
	public static final int LEVEL_4 = 4;//LINE
	public static final int LEVEL_5 = 5;//OUTLINE
	public static final int LEVEL_6 = 6;//
	public static final int LEVEL_MAX = LEVEL_6;

	public static final int DIRECTION_NONE = 0;
	public static final int DIRECTION_UP_LEVEL_1 = 1 << 0;
	public static final int DIRECTION_DOWN_LEVEL_1 = 1 << 1;
	public static final int DIRECTION_UP_LEVEL_2 = 1 << 2;
	public static final int DIRECTION_DOWN_LEVEL_2 = 1 << 3;
	public static final int DIRECTION_UP_LEVEL_3 = 1 << 4;
	public static final int DIRECTION_DOWN_LEVEL_3 = 1 << 5;
	public static final int DIRECTION_UP_LEVEL_4 = 1 << 6;
	public static final int DIRECTION_DOWN_LEVEL_4 = 1 << 7;
	public static final int DIRECTION_UP_LEVEL_5 = 1 << 8;
	public static final int DIRECTION_DOWN_LEVEL_5 = 1 << 9;
	public static final int DIRECTION_UP_LEVEL_6 = 1 << 10;
	public static final int DIRECTION_DOWN_LEVEL_6 = 1 << 11;

	public static final int VERTEX_NONE = 0;
	public static final int VERTEX_TOP_LEVEL_1 = 1 << 0;
	public static final int VERTEX_BOTTOM_LEVEL_1 = 1 << 1;
	public static final int VERTEX_TOP_LEVEL_2 = 1 << 2;
	public static final int VERTEX_BOTTOM_LEVEL_2 = 1 << 3;
	public static final int VERTEX_TOP_LEVEL_3 = 1 << 4;
	public static final int VERTEX_BOTTOM_LEVEL_3 = 1 << 5;
	public static final int VERTEX_TOP_LEVEL_4 = 1 << 6;
	public static final int VERTEX_BOTTOM_LEVEL_4 = 1 << 7;
	public static final int VERTEX_TOP_LEVEL_5 = 1 << 8;
	public static final int VERTEX_BOTTOM_LEVEL_5 = 1 << 9;
	public static final int VERTEX_TOP_LEVEL_6 = 1 << 10;
	public static final int VERTEX_BOTTOM_LEVEL_6 = 1 << 11;

	public static final int VERTEX_SIZE = 3;

	public static final int THRESHOLD_UPWARD_TREND = 2;
	public static final int THRESHOLD_NATURAL_RALLY = 1;
	public static final int THRESHOLD_NONE = 0;
	public static final int THRESHOLD_NATURAL_REACTION = -1;
	public static final int THRESHOLD_DOWNWARD_TREND = -2;

	public static Comparator<StockData> comparator = (arg0, arg1) -> {
		Calendar calendar0;
		Calendar calendar1;

		if (arg0 == null || arg1 == null) {
			return 0;
		}

		calendar0 = Utility.getCalendar(arg0.getDateTime(),
				Utility.CALENDAR_DATE_TIME_FORMAT);
		calendar1 = Utility.getCalendar(arg1.getDateTime(),
				Utility.CALENDAR_DATE_TIME_FORMAT);
		if (calendar0.before(calendar1)) {
			return -1;
		} else if (calendar0.after(calendar1)) {
			return 1;
		} else {
			return 0;
		}
	};

	private long mStockId;
	private String mSE;
	private String mCode;
	private String mName;
	private int mLevel;
	private String mDate;
	private String mTime;
	private String mPeriod;
	private CandlestickChart mCandlestickChart = new CandlestickChart();
	private double mChange;
	private double mNet;
	private int mDirection;
	private int mVertex;
	private double mVertexLow;
	private double mVertexHigh;
	private final Macd mMacd = new Macd();
	private String mAction;
	private double mRoi;
	private double mPe;
	private double mPb;
	private double mYield;
	private double mNaturalRally;
	private double mUpwardTrend;
	private double mDownwardTrend;
	private double mNaturalReaction;
	private int mIndex;
	private int mIndexStart;
	private int mIndexEnd;

	public StockData() {
		init();
	}

	public StockData(String period) {
		init();
		setPeriod(period);
	}

	public StockData(StockData stockData) {
		set(stockData);
	}

	public StockData(Cursor cursor) {
		set(cursor);
	}

	public boolean isEmpty() {

		boolean result = (mStockId == 0) && TextUtils.isEmpty(mDate)
				&& TextUtils.isEmpty(mTime);

		return result;
	}

	public void init() {
		super.init();

		setTableName(DatabaseContract.StockData.TABLE_NAME);

		mStockId = 0;
		mSE = "";
		mCode = "";
		mName = "";
		mLevel = LEVEL_NONE;
		mDate = "";
		mTime = "";
		mPeriod = "";
		mChange = 0;
		mNet = 0;
		mDirection = DIRECTION_NONE;
		mVertex = VERTEX_NONE;
		mVertexLow = 0;
		mVertexHigh = 0;
		mAction = "";

		mRoi = 0;
		mPe = 0;
		mPb = 0;
		mYield = 0;

		mIndex = 0;
		mIndexStart = 0;
		mIndexEnd = 0;

		mNaturalRally = 0;
		mUpwardTrend = 0;
		mDownwardTrend = 0;
		mNaturalReaction = 0;
	}

	@Override
	ContentValues getContentValues(ContentValues contentValues) {
		super.getContentValues(contentValues);

		contentValues.put(DatabaseContract.COLUMN_STOCK_ID, mStockId);
		contentValues.put(DatabaseContract.COLUMN_SE, mSE);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		contentValues.put(DatabaseContract.COLUMN_LEVEL, mLevel);
		contentValues.put(DatabaseContract.COLUMN_DATE, mDate);
		contentValues.put(DatabaseContract.COLUMN_TIME, mTime);
		contentValues.put(DatabaseContract.COLUMN_PERIOD, mPeriod);
		contentValues.put(DatabaseContract.COLUMN_OPEN, mCandlestickChart.getOpen());
		contentValues.put(DatabaseContract.COLUMN_HIGH, mCandlestickChart.getHigh());
		contentValues.put(DatabaseContract.COLUMN_LOW, mCandlestickChart.getLow());
		contentValues.put(DatabaseContract.COLUMN_CLOSE, mCandlestickChart.getClose());
		contentValues.put(DatabaseContract.COLUMN_CHANGE, mChange);
		contentValues.put(DatabaseContract.COLUMN_NET, mNet);
		contentValues.put(DatabaseContract.COLUMN_DIRECTION, mDirection);
		contentValues.put(DatabaseContract.COLUMN_VERTEX, mVertex);
		contentValues.put(DatabaseContract.COLUMN_VERTEX_LOW, mVertexLow);
		contentValues.put(DatabaseContract.COLUMN_VERTEX_HIGH, mVertexHigh);
		contentValues.put(DatabaseContract.COLUMN_AVERAGE5, mMacd.getAverage5());
		contentValues.put(DatabaseContract.COLUMN_AVERAGE10, mMacd.getAverage10());
		contentValues.put(DatabaseContract.COLUMN_DIF, mMacd.getDIF());
		contentValues.put(DatabaseContract.COLUMN_DEA, mMacd.getDEA());
		contentValues.put(DatabaseContract.COLUMN_HISTOGRAM, mMacd.getHistogram());
		contentValues.put(DatabaseContract.COLUMN_VELOCITY, mMacd.getVelocity());
		contentValues.put(DatabaseContract.COLUMN_ACTION, mAction);

		contentValues.put(DatabaseContract.COLUMN_ROI, mRoi);
		contentValues.put(DatabaseContract.COLUMN_PE, mPe);
		contentValues.put(DatabaseContract.COLUMN_PB, mPb);
		contentValues.put(DatabaseContract.COLUMN_YIELD, mYield);

		contentValues.put(DatabaseContract.COLUMN_NATURAL_RALLY, mNaturalRally);
		contentValues.put(DatabaseContract.COLUMN_UPWARD_TREND, mUpwardTrend);
		contentValues.put(DatabaseContract.COLUMN_DOWNWARD_TREND, mDownwardTrend);
		contentValues.put(DatabaseContract.COLUMN_NATURAL_REACTION, mNaturalReaction);

		return contentValues;
	}

	public void set(StockData stockData) {
		if (stockData == null) {
			return;
		}

		init();

		super.set(stockData);

		setStockId(stockData.mStockId);
		setSE(stockData.mSE);
		setCode(stockData.mCode);
		setName(stockData.mName);
		setLevel(stockData.mLevel);
		setDate(stockData.mDate);
		setTime(stockData.mTime);
		setPeriod(stockData.mPeriod);
		mCandlestickChart.set(stockData.mCandlestickChart);
		setChange(stockData.mChange);
		setNet(stockData.mNet);
		setDirection(stockData.mDirection);
		setVertex(stockData.mVertex);
		setVertexLow(stockData.mVertexLow);
		setVertexHigh(stockData.mVertexHigh);
		mMacd.set(stockData.mMacd);
		setAction(stockData.mAction);

		setRoi(stockData.mRoi);
		setPe(stockData.mPe);
		setPb(stockData.mPb);
		setYield(stockData.mYield);

		setNaturalRally(stockData.mNaturalRally);
		setUpwardTrend(stockData.mUpwardTrend);
		setDownwardTrend(stockData.mDownwardTrend);
		setNaturalReaction(stockData.mNaturalReaction);

		setIndex(stockData.mIndex);
		setIndexStart(stockData.mIndexStart);
		setIndexEnd(stockData.mIndexEnd);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		super.set(cursor);

		setStockID(cursor);
		setSE(cursor);
		setCode(cursor);
		setName(cursor);
		setLevel(cursor);
		setDate(cursor);
		setTime(cursor);
		setPeriod(cursor);
		mCandlestickChart.set(cursor);
		setChange(cursor);
		setNet(cursor);
		setDirection(cursor);
		setVertex(cursor);
		setVertexLow(cursor);
		setVertexHigh(cursor);
		mMacd.set(cursor);
		setAction(cursor);

		setRoi(cursor);
		setPe(cursor);
		setPb(cursor);
		setYield(cursor);

		setNaturalRally(cursor);
		setUpwardTrend(cursor);
		setDownwardTrend(cursor);
		setNaturalReaction(cursor);
	}

	public long getStockId() {
		return mStockId;
	}

	public void setStockId(long stockId) {
		mStockId = stockId;
	}

	void setStockID(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setStockId(cursor.getLong(cursor
				.getColumnIndex(DatabaseContract.COLUMN_STOCK_ID)));
	}

	public String getSE() {
		return mSE;
	}

	public void setSE(String se) {
		mSE = se;
	}

	void setSE(Cursor cursor) {
		if (cursor == null) {
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
		if (cursor == null) {
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
		if (cursor == null) {
			return;
		}

		setName(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NAME)));
	}

	public int getLevel() {
		return mLevel;
	}

	public void setLevel(int level) {
		mLevel = level;
	}

	void setLevel(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setLevel(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_LEVEL)));
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(String date) {
		mDate = date;
	}

	public void setDate(Cursor cursor) {
		if (cursor == null) {
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
		if (cursor == null) {
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

	public String getPeriod() {
		return mPeriod;
	}

	public void setPeriod(String period) {
		mPeriod = period;
	}

	void setPeriod(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setPeriod(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PERIOD)));
	}

	public double getChange() {
		return mChange;
	}

	public void setChange(double change) {
		mChange = change;
	}

	void setChange(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setChange(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_CHANGE)));
	}

	public double getNet() {
		return mNet;
	}

	public void setNet(double net) {
		mNet = net;
	}

	void setNet(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setNet(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NET)));
	}

	public int getDirection() {
		return mDirection;
	}

	public void setDirection(int direction) {
		mDirection = direction;
	}

	void setDirection(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDirection(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIRECTION)));
	}

	public int getVertex() {
		return mVertex;
	}

	public void setVertex(int vertex) {
		mVertex = vertex;
	}

	void setVertex(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setVertex(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VERTEX)));
	}

	public double getVertexLow() {
		return mVertexLow;
	}

	public void setVertexLow(double vertexLow) {
		mVertexLow = vertexLow;
	}

	void setVertexLow(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setVertexLow(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VERTEX_LOW)));
	}

	public double getVertexHigh() {
		return mVertexHigh;
	}

	public void setVertexHigh(double vertexHigh) {
		mVertexHigh = vertexHigh;
	}

	void setVertexHigh(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setVertexHigh(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VERTEX_HIGH)));
	}

	public Macd getMacd() {
		return mMacd;
	}

	public CandlestickChart getCandlestickChart() {
		return mCandlestickChart;
	}

	public String getAction() {
		return mAction;
	}

	public void setAction(String action) {
		mAction = action;
	}

	void setAction(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setAction(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ACTION)));
	}

	public double getRoi() {
		return mRoi;
	}

	public void setRoi(double roi) {
		mRoi = roi;
	}

	void setRoi(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setRoi(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ROI)));
	}

	public double getPe() {
		return mPe;
	}

	public void setPe(double pe) {
		mPe = pe;
	}

	void setPe(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setPe(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PE)));
	}

	public double getPb() {
		return mPb;
	}

	public void setPb(double pb) {
		mPb = pb;
	}

	void setPb(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setPb(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PB)));
	}

	public double getYield() {
		return mYield;
	}

	public void setYield(double yield) {
		mYield = yield;
	}

	void setYield(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setYield(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_YIELD)));
	}

	public double getNaturalRally() {
		return mNaturalRally;
	}

	public void setNaturalRally(double naturalRally) {
		mNaturalRally = naturalRally;
	}

	public void setNaturalRally(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setNaturalRally(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NATURAL_RALLY)));
	}

	public double getUpwardTrend() {
		return mUpwardTrend;
	}

	public void setUpwardTrend(double upwardTrend) {
		mUpwardTrend = upwardTrend;
	}

	public void setUpwardTrend(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setUpwardTrend(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_UPWARD_TREND)));
	}

	public double getDownwardTrend() {
		return mDownwardTrend;
	}

	public void setDownwardTrend(double downwardTrend) {
		mDownwardTrend = downwardTrend;
	}

	public void setDownwardTrend(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDownwardTrend(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DOWNWARD_TREND)));
	}

	public double getNaturalReaction() {
		return mNaturalReaction;
	}

	public void setNaturalReaction(double naturalReaction) {
		mNaturalReaction = naturalReaction;
	}

	void setNaturalReaction(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setNaturalReaction(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NATURAL_REACTION)));
	}

	public boolean directionOf(int direction) {

		boolean result = (mDirection & direction) == direction;

		return result;
	}

	public boolean vertexOf(int vertex) {

		boolean result = (mVertex & vertex) == vertex;

		return result;
	}

	public int getIndex() {
		return mIndex;
	}

	public void setIndex(int index) {
		mIndex = index;
	}

	public int getIndexStart() {
		return mIndexStart;
	}

	public void setIndexStart(int index) {
		mIndexStart = index;
	}

	public int getIndexEnd() {
		return mIndexEnd;
	}

	public void setIndexEnd(int index) {
		mIndexEnd = index;
	}

	public Calendar getCalendar() {
		Calendar result = Calendar.getInstance();

		if (TextUtils.isEmpty(getDate())) {
			return result;
		}

		if (TextUtils.isEmpty(getTime())) {
			result = Utility.getCalendar(getDate(),
					Utility.CALENDAR_DATE_FORMAT);
		} else {
			result = Utility.getCalendar(getDate() + " " + getTime(),
					Utility.CALENDAR_DATE_TIME_FORMAT);
		}

		return result;
	}

	public boolean include(StockData stockData) {
		if (stockData == null) {
			return false;
		}

		boolean result = (getVertexHigh() >= stockData.getVertexHigh())
				&& (getVertexLow() <= stockData.getVertexLow());

		return result;
	}

	public boolean includedBy(StockData stockData) {
		if (stockData == null) {
			return false;
		}

		boolean result = (getVertexHigh() <= stockData.getVertexHigh())
				&& (getVertexLow() >= stockData.getVertexLow());

		return result;
	}

	public int vertexTo(StockData prev, StockData next) {
		int vertex = StockData.VERTEX_NONE;

		if (prev == null || next == null) {
			return vertex;
		}

		if ((getVertexHigh() > prev.getVertexHigh())
				&& (getVertexLow() > prev.getVertexLow())) {
			if ((getVertexHigh() > next.getVertexHigh())
					&& (getVertexLow() > next.getVertexLow())) {
				vertex = StockData.VERTEX_TOP_LEVEL_1;
			}
		} else if ((getVertexHigh() < prev.getVertexHigh())
				&& (getVertexLow() < prev.getVertexLow())) {
			if ((getVertexHigh() < next.getVertexHigh())
					&& (getVertexLow() < next.getVertexLow())) {
				vertex = StockData.VERTEX_BOTTOM_LEVEL_1;
			}
		} else {
			vertex = StockData.VERTEX_NONE;
		}

		return vertex;
	}

	public int directionTo(StockData stockData) {
		int result = DIRECTION_NONE;

		if (stockData == null) {
			return result;
		}

		if ((getVertexHigh() >= stockData.getVertexHigh())
				&& (getVertexLow() > stockData.getVertexLow())) {
			result = DIRECTION_UP_LEVEL_1;
		} else if ((getVertexHigh() < stockData.getVertexHigh())
				&& (getVertexLow() <= stockData.getVertexLow())) {
			result = DIRECTION_DOWN_LEVEL_1;
		} else {
			result = DIRECTION_NONE;
		}

		return result;
	}

	public void merge(int directionType, StockData stockData) {
		if (directionType == DIRECTION_UP_LEVEL_1) {
			setVertexHigh(Math.max(getVertexHigh(), stockData.getVertexHigh()));
			setVertexLow(Math.max(getVertexLow(), stockData.getVertexLow()));
		} else if (directionType == DIRECTION_DOWN_LEVEL_1) {
			setVertexHigh(Math.min(getVertexHigh(), stockData.getVertexHigh()));
			setVertexLow(Math.min(getVertexLow(), stockData.getVertexLow()));
		} else {
			setVertexHigh(Math.max(getVertexHigh(), stockData.getVertexHigh()));
			setVertexLow(Math.min(getVertexLow(), stockData.getVertexLow()));
		}
	}

	public void merge(StockData prev) {
		if ((prev == null)) {
			return;
		}

		setStockId(prev.getStockId());
		setIndex(prev.getIndex());
		setIndexStart(prev.getIndexStart());
		setVertexLow(Math.min(prev.getVertexLow(), getVertexLow()));
		setVertexHigh(Math.max(prev.getVertexHigh(), getVertexHigh()));
	}

	public void add(StockData stockData, long weight) {
		if (stockData == null) {
			return;
		}
		mCandlestickChart.add(stockData.mCandlestickChart, weight);
		mVertexHigh = mCandlestickChart.getHigh();
		mVertexLow = mCandlestickChart.getLow();
	}

	public void setupChange() {
		mChange = 0;

		if (directionOf(DIRECTION_UP_LEVEL_1)) {
			mChange = mVertexHigh - mVertexLow;
		} else if (directionOf(DIRECTION_DOWN_LEVEL_1)) {
			mChange = mVertexLow - mVertexHigh;
		} else {
			mChange = mVertexHigh - mVertexLow;
		}

		mChange = Utility.Round(mChange);
	}

	public void setupNet() {
		mNet = 0;

		if ((mVertexHigh == 0) || (mVertexLow == 0)) {
			return;
		}

		if (directionOf(DIRECTION_UP_LEVEL_1)) {
			mNet = 100.0 * mChange / mVertexLow;
		} else if (directionOf(DIRECTION_DOWN_LEVEL_1)) {
			mNet = 100.0 * mChange / mVertexHigh;
		} else {
			mNet = 100.0 * mChange / mVertexLow;
		}

		mNet = Utility.Round(mNet);
	}

	public StockData fromString(String string) {
		String dateString = "";
		String timeString = "";

		if (TextUtils.isEmpty(string)) {
			return null;
		}

		//TDX output format
		//date  time    open    high    low close   volume  value
		String[] strings = string.split(Constant.TAB);
		if (strings == null || strings.length < 6) {
			return null;
		}

		dateString = strings[0].replace("/", "-");
		setDate(dateString);
		timeString = strings[1].substring(0, 2) + ":" + strings[1].substring(2, 4) + ":" + "00";
		setTime(timeString);

		mCandlestickChart.setOpen(Double.parseDouble(strings[2]));
		mCandlestickChart.setHigh(Double.parseDouble(strings[3]));
		mCandlestickChart.setLow(Double.parseDouble(strings[4]));
		mCandlestickChart.setClose(Double.parseDouble(strings[5]));

		setVertexHigh(mCandlestickChart.getHigh());
		setVertexLow(mCandlestickChart.getLow());

		setCreated(Utility.getCurrentDateTimeString());
		setModified(Utility.getCurrentDateTimeString());

		return this;
	}

	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		//TDX output format
			//date  time    open    high    low close   volume  value
		String dateString = getDate().replace("-", "/");
		String timeString = getTime().substring(0, 5).replace(":", "");
		stringBuffer.append(dateString + Constant.TAB
				+ timeString + Constant.TAB
				+ mCandlestickChart.toString()
				+ 0 + Constant.TAB
				+ 0);
		stringBuffer.append("\r\n");
		return stringBuffer.toString();
	}

	public static boolean isMinutePeriod(@NonNull String period) {
		boolean result;

		switch (period) {
			case Period.MIN5:
			case Period.MIN15:
			case Period.MIN30:
			case Period.MIN60:
				result = true;
				break;
			case Period.DAY:
			case Period.WEEK:
			case Period.MONTH:
			default:
				result = false;
				break;
		}

		return result;
	}

	public boolean isMinutePeriod() {
		return isMinutePeriod(getPeriod());
	}

	public static int getPeriodIndex(String period) {
		int index = 0;
		if (TextUtils.isEmpty(period)) {
			return index;
		}

		for (int i = 0; i < Period.PERIODS.length; i++) {
			if (TextUtils.equals(period, Period.PERIODS[i])) {
				index = i;
				break;
			}
		}
		return index;
	}

	public int getPeriodIndex() {
		return getPeriodIndex(getPeriod());
	}
}