package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.Constants;
import com.android.orion.utility.Utility;

import java.util.Calendar;

public class StockData extends StockDatabaseTable {

    public static final String ACTION_NONE = "";

	public static final String ACTION_BUY = "B";
	public static final String ACTION_BUY1 = "B1";
	public static final String ACTION_BUY2 = "B2";
	public static final String ACTION_BUY3 = "B3";
    public static final String ACTION_SELL = "S";
    public static final String ACTION_SELL1 = "S1";
    public static final String ACTION_SELL2 = "S2";
    public static final String ACTION_SELL3 = "S3";

	public static final String ACTION_D = "D";
	public static final String ACTION_G = "G";

    public static final String ACTION_HIGH = "H";
    public static final String ACTION_LOW = "L";

    public static final char ACTION_ADD = '+';
    public static final char ACTION_MINUS = '-';

    public static final String ACTION_STAR = "*";

	public static final int LEVEL_NONE = 0;
	public static final int LEVEL_DRAW = 1 << 0;
	public static final int LEVEL_STROKE = 1 << 1;
	public static final int LEVEL_SEGMENT = 1 << 2;

	public static final int DIVERGENCE_NONE = 0;
	public static final int DIVERGENCE_UP = 1;
	public static final int DIVERGENCE_DOWN = -1;

	public static final int DIRECTION_NONE = 0;
	public static final int DIRECTION_UP = 1 << 0;
	public static final int DIRECTION_DOWN = 1 << 1;
	public static final int DIRECTION_UP_STROKE = 1 << 2;
	public static final int DIRECTION_DOWN_STROKE = 1 << 3;
	public static final int DIRECTION_UP_SEGMENT = 1 << 4;
	public static final int DIRECTION_DOWN_SEGMENT = 1 << 5;

	public static final int VERTEX_NONE = 0;
	public static final int VERTEX_TOP = 1 << 0;
	public static final int VERTEX_BOTTOM = 1 << 1;
	public static final int VERTEX_TOP_STROKE = 1 << 2;
	public static final int VERTEX_BOTTOM_STROKE = 1 << 3;
	public static final int VERTEX_TOP_SEGMENT = 1 << 4;
	public static final int VERTEX_BOTTOM_SEGMENT = 1 << 5;

    public static final int VERTEX_TYPING_SIZE = 3;

	public static final int POSITION_ABOVE = 1;
	public static final int POSITION_NONE = 0;
	public static final int POSITION_BELOW = -1;

	private long mStockId;
	private int mLevel;
	private String mDate;
	private String mTime;
	private String mPeriod;
	private double mOpen;
	private double mHigh;
	private double mLow;
	private double mClose;
	private double mChange;
	private double mNet;
	private int mDirection;
	private int mVertex;
	private double mVertexLow;
	private double mVertexHigh;
	private double mAverage5;
	private double mAverage10;
	private double mDIF;
	private double mDEA;
	private double mHistogram;
	private double mSigmaHistogram;
	private double mVelocity;
	private int mDivergence;
	private String mAction;

	private double mRoi;
	private double mPe;
	private double mPb;
	private double mYield;

	private double mSecondaryRally;
	private double mNaturalRally;
	private double mUpwardTrend;
	private double mDownwardTrend;
	private double mNaturalReaction;
	private double mSecondaryReaction;

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
		boolean result = false;

		if ((mStockId == 0) && TextUtils.isEmpty(mDate)
				&& TextUtils.isEmpty(mTime)) {
			result = true;
		}

		return result;
	}

	public void init() {
		super.init();

		setTableName(DatabaseContract.StockData.TABLE_NAME);

		mStockId = 0;
		mLevel = LEVEL_NONE;
		mDate = "";
		mTime = "";
		mPeriod = "";
		mOpen = 0;
		mHigh = 0;
		mLow = 0;
		mClose = 0;
		mChange = 0;
		mNet = 0;
		mDirection = DIRECTION_NONE;
		mVertex = VERTEX_NONE;
		mVertexLow = 0;
		mVertexHigh = 0;
		mAverage5 = 0;
		mAverage10 = 0;
		mDIF = 0;
		mDEA = 0;
		mHistogram = 0;
		mSigmaHistogram = 0;
		mVelocity = 0;
		mDivergence = DIVERGENCE_NONE;
		mAction = "";

		mRoi = 0;
		mPe = 0;
		mPb = 0;
		mYield = 0;

		mIndex = 0;
		mIndexStart = 0;
		mIndexEnd = 0;

		mSecondaryRally = 0;
		mNaturalRally = 0;
		mUpwardTrend = 0;
		mDownwardTrend = 0;
		mNaturalReaction = 0;
		mSecondaryReaction = 0;
	}

	@Override
	ContentValues getContentValues(ContentValues contentValues) {
		super.getContentValues(contentValues);

		contentValues.put(DatabaseContract.COLUMN_STOCK_ID, mStockId);
		contentValues.put(DatabaseContract.COLUMN_LEVEL, mLevel);
		contentValues.put(DatabaseContract.COLUMN_DATE, mDate);
		contentValues.put(DatabaseContract.COLUMN_TIME, mTime);
		contentValues.put(DatabaseContract.COLUMN_PERIOD, mPeriod);
		contentValues.put(DatabaseContract.COLUMN_OPEN, mOpen);
		contentValues.put(DatabaseContract.COLUMN_HIGH, mHigh);
		contentValues.put(DatabaseContract.COLUMN_LOW, mLow);
		contentValues.put(DatabaseContract.COLUMN_CLOSE, mClose);
		contentValues.put(DatabaseContract.COLUMN_CHANGE, mChange);
		contentValues.put(DatabaseContract.COLUMN_NET, mNet);
		contentValues.put(DatabaseContract.COLUMN_DIRECTION, mDirection);
		contentValues.put(DatabaseContract.COLUMN_VERTEX, mVertex);
		contentValues.put(DatabaseContract.COLUMN_VERTEX_LOW, mVertexLow);
		contentValues.put(DatabaseContract.COLUMN_VERTEX_HIGH, mVertexHigh);
		contentValues.put(DatabaseContract.COLUMN_OVERLAP, mOverlap);
		contentValues.put(DatabaseContract.COLUMN_AVERAGE5, mAverage5);
		contentValues.put(DatabaseContract.COLUMN_AVERAGE10, mAverage10);
		contentValues.put(DatabaseContract.COLUMN_DIF, mDIF);
		contentValues.put(DatabaseContract.COLUMN_DEA, mDEA);
		contentValues.put(DatabaseContract.COLUMN_HISTOGRAM, mHistogram);
		contentValues.put(DatabaseContract.COLUMN_SIGMA_HISTOGRAM,
				mSigmaHistogram);
		contentValues.put(DatabaseContract.COLUMN_VELOCITY, mVelocity);
		contentValues.put(DatabaseContract.COLUMN_DIVERGENCE, mDivergence);
		contentValues.put(DatabaseContract.COLUMN_ACTION, mAction);

		contentValues.put(DatabaseContract.COLUMN_ROI, mRoi);
		contentValues.put(DatabaseContract.COLUMN_PE, mPe);
		contentValues.put(DatabaseContract.COLUMN_PB, mPb);
		contentValues.put(DatabaseContract.COLUMN_YIELD, mYield);

		contentValues.put(DatabaseContract.COLUMN_SECONDARY_RALLY, mSecondaryRally);
		contentValues.put(DatabaseContract.COLUMN_NATURAL_RALLY, mNaturalRally);
		contentValues.put(DatabaseContract.COLUMN_UPWARD_TREND, mUpwardTrend);
		contentValues.put(DatabaseContract.COLUMN_DOWNWARD_TREND, mDownwardTrend);
		contentValues.put(DatabaseContract.COLUMN_NATURAL_REACTION, mNaturalReaction);
		contentValues.put(DatabaseContract.COLUMN_SECONDARY_REACTION, mSecondaryReaction);

		return contentValues;
	}

	public void set(StockData stockData) {
		if (stockData == null) {
			return;
		}

		init();

		super.set(stockData);

		setStockId(stockData.mStockId);
		setLevel(stockData.mLevel);
		setDate(stockData.mDate);
		setTime(stockData.mTime);
		setPeriod(stockData.mPeriod);
		setOpen(stockData.mOpen);
		setHigh(stockData.mHigh);
		setLow(stockData.mLow);
		setClose(stockData.mClose);
		setChange(stockData.mChange);
		setNet(stockData.mNet);
		setDirection(stockData.mDirection);
		setVertex(stockData.mVertex);
		setVertexLow(stockData.mVertexLow);
		setVertexHigh(stockData.mVertexHigh);
		setAverage5(stockData.mAverage5);
		setAverage10(stockData.mAverage10);
		setDIF(stockData.mDIF);
		setDEA(stockData.mDEA);
		setHistogram(stockData.mHistogram);
		setSigmaHistogram(stockData.mSigmaHistogram);
		setVelocity(stockData.mVelocity);
		setDivergence(stockData.mDivergence);
		setAction(stockData.mAction);

		setRoi(stockData.mRoi);
		setPe(stockData.mPe);
		setPb(stockData.mPb);
		setYield(stockData.mYield);

		setSecondaryRally(stockData.mSecondaryRally);
		setNaturalRally(stockData.mNaturalRally);
		setUpwardTrend(stockData.mUpwardTrend);
		setDownwardTrend(stockData.mDownwardTrend);
		setNaturalReaction(stockData.mNaturalReaction);
		setSecondaryReaction(stockData.mSecondaryReaction);

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
		setLevel(cursor);
		setDate(cursor);
		setTime(cursor);
		setPeriod(cursor);
		setOpen(cursor);
		setHigh(cursor);
		setLow(cursor);
		setClose(cursor);
		setChange(cursor);
		setNet(cursor);
		setDirection(cursor);
		setVertex(cursor);
		setVertexLow(cursor);
		setVertexHigh(cursor);
		setAverage5(cursor);
		setAverage10(cursor);
		setDIF(cursor);
		setDEA(cursor);
		setHistogram(cursor);
		setSigmaHistogram(cursor);
		setVelocity(cursor);
		setDivergence(cursor);
		setAction(cursor);

		setRoi(cursor);
		setPe(cursor);
		setPb(cursor);
		setYield(cursor);

		setSecondaryRally(cursor);
		setNaturalRally(cursor);
		setUpwardTrend(cursor);
		setDownwardTrend(cursor);
		setNaturalReaction(cursor);
		setSecondaryReaction(cursor);
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

	public double getOpen() {
		return mOpen;
	}

	public void setOpen(double open) {
		mOpen = open;
	}

	public void setOpen(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setOpen(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_OPEN)));
	}

	public double getHigh() {
		return mHigh;
	}

	public void setHigh(double high) {
		mHigh = high;
	}

	public void setHigh(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setHigh(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_HIGH)));
	}

	public double getLow() {
		return mLow;
	}

	public void setLow(double low) {
		mLow = low;
	}

	public void setLow(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setLow(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_LOW)));
	}

	public double getClose() {
		return mClose;
	}

	public void setClose(double close) {
		mClose = close;
	}

	public void setClose(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setClose(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_CLOSE)));
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

	public double getAverage5() {
		return mAverage5;
	}

	public void setAverage5(double average) {
		mAverage5 = average;
	}

	void setAverage5(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setAverage5(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_AVERAGE5)));
	}

	public double getAverage10() {
		return mAverage10;
	}

	public void setAverage10(double average) {
		mAverage10 = average;
	}

	void setAverage10(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setAverage10(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_AVERAGE10)));
	}

	public double getDIF() {
		return mDIF;
	}

	public void setDIF(double dif) {
		mDIF = dif;
	}

	void setDIF(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDIF(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIF)));
	}

	public double getDEA() {
		return mDEA;
	}

	public void setDEA(double dea) {
		mDEA = dea;
	}

	void setDEA(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDEA(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DEA)));
	}

	public double getHistogram() {
		return mHistogram;
	}

	public void setHistogram(double histogram) {
		mHistogram = histogram;
	}

	void setHistogram(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setHistogram(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_HISTOGRAM)));
	}

	double getSigmaHistogram() {
		return mSigmaHistogram;
	}

	public void setSigmaHistogram(double sigmaHistogram) {
		mSigmaHistogram = sigmaHistogram;
	}

	void setSigmaHistogram(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setSigmaHistogram(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_SIGMA_HISTOGRAM)));
	}

	public double getVelocity() {
		return mVelocity;
	}

	public void setVelocity(double velocity) {
		mVelocity = velocity;
	}

	void setVelocity(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setVelocity(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VELOCITY)));
	}

	public int getDivergence() {
		return mDivergence;
	}

	public void setDivergence(int divergence) {
		mDivergence = divergence;
	}

	void setDivergence(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDivergence(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIVERGENCE)));
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

	public double getSecondaryRally() {
		return mSecondaryRally;
	}

	public void setSecondaryRally(double secondaryRally) {
		mSecondaryRally = secondaryRally;
	}

	public void setSecondaryRally(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setSecondaryRally(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_SECONDARY_RALLY)));
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

	public double getSecondaryReaction() {
		return mSecondaryReaction;
	}

	public void setSecondaryReaction(double secondaryReaction) {
		mSecondaryReaction = secondaryReaction;
	}

	void setSecondaryReaction(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setSecondaryReaction(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_SECONDARY_REACTION)));
	}

	public boolean directionOf(int direction) {
		boolean result = false;

		if ((mDirection & direction) == direction) {
			result = true;
		}

		return result;
	}

	public boolean vertexOf(int vertex) {
		boolean result = false;

		if ((mVertex & vertex) == vertex) {
			result = true;
		}

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

	String getOHLCString() {
		return String.valueOf(getOpen()) + String.valueOf(getHigh())
				+ String.valueOf(getLow()) + String.valueOf(getClose());
	}

	public boolean include(StockData stockData) {
		boolean result = false;

		if ((getVertexHigh() >= stockData.getVertexHigh())
				&& (getVertexLow() <= stockData.getVertexLow())) {
			result = true;
		}

		return result;
	}

	public boolean includedBy(StockData stockData) {
		boolean result = false;

		if ((getVertexHigh() <= stockData.getVertexHigh())
				&& (getVertexLow() >= stockData.getVertexLow())) {
			result = true;
		}

		return result;
	}

	public int directionTo(StockData stockData) {
		int result = DIRECTION_NONE;

		if ((getVertexHigh() >= stockData.getVertexHigh())
				&& (getVertexLow() > stockData.getVertexLow())) {
			result = DIRECTION_UP;
		} else if ((getVertexHigh() < stockData.getVertexHigh())
				&& (getVertexLow() <= stockData.getVertexLow())) {
			result = DIRECTION_DOWN;
		} else {
			result = DIRECTION_NONE;
		}

		return result;
	}

	public int positionTo(StockData overlap) {
		int position = POSITION_NONE;
		
		if (overlap == null) {
			return position;
		}

		if (getVertexLow() > overlap.getOverlapHigh()) {
			position = POSITION_ABOVE;
		} else if (getVertexHigh() < overlap.getOverlapLow()) {
			position = POSITION_BELOW;
		} else {
			position = POSITION_NONE;
		}

		return position;
	}

	public void merge(int directionType, StockData stockData) {
		if (directionType == DIRECTION_UP) {
			setVertexHigh(Math.max(getVertexHigh(), stockData.getVertexHigh()));
			setVertexLow(Math.max(getVertexLow(), stockData.getVertexLow()));
		} else if (directionType == DIRECTION_DOWN) {
			setVertexHigh(Math.min(getVertexHigh(), stockData.getVertexHigh()));
			setVertexLow(Math.min(getVertexLow(), stockData.getVertexLow()));
		} else {
			setVertexHigh(Math.max(getVertexHigh(), stockData.getVertexHigh()));
			setVertexLow(Math.min(getVertexLow(), stockData.getVertexLow()));
		}
	}

	public void merge(StockData prev, StockData current) {
		if ((prev == null) || (current == null)) {
			return;
		}

		setStockId(prev.getStockId());
		setIndex(prev.getIndex());
		setIndexStart(prev.getIndexStart());
		setVertexLow(Math.min(prev.getVertexLow(), current.getVertexLow()));
		setVertexHigh(Math.max(prev.getVertexHigh(), current.getVertexHigh()));
	}

	public int divergenceTo(StockData stockData) {
		int result = DIVERGENCE_NONE;

		if (mDirection == DIRECTION_UP) {
			if ((getVertexHigh() > stockData.getVertexHigh())
					&& (getVertexLow() > stockData.getVertexLow())) {
				if (Math.abs(getSigmaHistogram()) < Math.abs(stockData.getSigmaHistogram())) {
					if (Math.abs(getVelocity()) < Math.abs(stockData.getVelocity())) {
						result = DIVERGENCE_UP;
					}
				}
			}
		} else if (mDirection == DIRECTION_DOWN) {
			if ((getVertexHigh() < stockData.getVertexHigh())
					&& (getVertexLow() < stockData.getVertexLow())) {
				if (Math.abs(getSigmaHistogram()) < Math.abs(stockData.getSigmaHistogram())) {
					if (Math.abs(getVelocity()) < Math.abs(stockData.getVelocity())) {
						result = DIVERGENCE_DOWN;
					}
				}
			}
		}

		return result;
	}

	public void setupChange() {
		mChange = 0;

		if (directionOf(DIRECTION_UP)) {
			mChange = mVertexHigh - mVertexLow;
		} else if (directionOf(DIRECTION_DOWN)) {
			mChange = mVertexLow - mVertexHigh;
		} else {
			mChange = mVertexHigh - mVertexLow;
		}

		mChange = Utility.Round(mChange, Constants.DOUBLE_FIXED_DECIMAL);
	}

	public void setupNet() {
		mNet = 0;

		if ((mVertexHigh == 0) || (mVertexLow == 0)) {
			return;
		}

		if (directionOf(DIRECTION_UP)) {
			mNet = 100.0 * mChange / mVertexLow;
		} else if (directionOf(DIRECTION_DOWN)) {
			mNet = 100.0 * mChange / mVertexHigh;
		} else {
			mNet = 100.0 * mChange / mVertexLow;
		}

		mNet = Utility.Round(mNet, Constants.DOUBLE_FIXED_DECIMAL);
	}

    public void setupVelocity() {
		mVelocity = 0;
		int dt = 0;

		dt = getIndexEnd() - getIndexStart();
		if (dt == 0) {
			return;
		}

		mVelocity = mChange / dt;

		mVelocity = Utility.Round(mVelocity, Constants.DOUBLE_FIXED_DECIMAL);
	}
}