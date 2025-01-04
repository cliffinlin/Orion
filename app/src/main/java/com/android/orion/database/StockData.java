package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.android.orion.data.Candlestick;
import com.android.orion.data.Macd;
import com.android.orion.data.Period;
import com.android.orion.data.Trend;
import com.android.orion.setting.Constant;
import com.android.orion.utility.Utility;

import java.util.Calendar;
import java.util.Comparator;

public class StockData extends DatabaseTable {

	public static final String MARK_NATURAL_RALLY = "NG";
	public static final String MARK_UPWARD_TREND = "GG";
	public static final String MARK_DOWNWARD_TREND = "DD";
	public static final String MARK_NATURAL_REACTION = "ND";

	public static final String NAME_NATURAL_RALLY = "Rally";
	public static final String NAME_UPWARD_TREND = "Up";
	public static final String NAME_DOWNWARD_TREND = "Down";
	public static final String NAME_NATURAL_REACTION = "React";

	public static final int THRESHOLD_UPWARD_TREND = 2;
	public static final int THRESHOLD_NATURAL_RALLY = 1;
	public static final int THRESHOLD_NONE = 0;
	public static final int THRESHOLD_NATURAL_REACTION = -1;
	public static final int THRESHOLD_DOWNWARD_TREND = -2;
	public static Comparator<StockData> comparator = new Comparator<StockData>() {

		@Override
		public int compare(StockData arg0, StockData arg1) {
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
		}
	};
	private final Candlestick mCandlestick = new Candlestick();
	private final Macd mMacd = new Macd();
	private final Trend mTrend = new Trend();
	private long mStockId;
	private String mSE;
	private String mCode;
	private String mName;
	private int mLevel;
	private String mDate;
	private String mTime;
	private String mPeriod;
	private String mAction;
	private double mNaturalRally;
	private double mUpwardTrend;
	private double mDownwardTrend;
	private double mNaturalReaction;
	private int mIndex;

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
		return (mStockId == 0) && TextUtils.isEmpty(mDate)
				&& TextUtils.isEmpty(mTime);
	}

	public void init() {
		super.init();

		setTableName(DatabaseContract.StockData.TABLE_NAME);

		mStockId = 0;
		mSE = "";
		mCode = "";
		mName = "";
		mLevel = Trend.LEVEL_NONE;
		mDate = "";
		mTime = "";
		mPeriod = "";
		mAction = "";

		mIndex = 0;

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
		contentValues.put(DatabaseContract.COLUMN_OPEN, mCandlestick.getOpen());
		contentValues.put(DatabaseContract.COLUMN_HIGH, mCandlestick.getHigh());
		contentValues.put(DatabaseContract.COLUMN_LOW, mCandlestick.getLow());
		contentValues.put(DatabaseContract.COLUMN_CLOSE, mCandlestick.getClose());
		contentValues.put(DatabaseContract.COLUMN_AVERAGE5, mMacd.getAverage5());
		contentValues.put(DatabaseContract.COLUMN_AVERAGE10, mMacd.getAverage10());
		contentValues.put(DatabaseContract.COLUMN_DIF, mMacd.getDIF());
		contentValues.put(DatabaseContract.COLUMN_DEA, mMacd.getDEA());
		contentValues.put(DatabaseContract.COLUMN_HISTOGRAM, mMacd.getHistogram());
		contentValues.put(DatabaseContract.COLUMN_VELOCITY, mMacd.getVelocity());
		contentValues.put(DatabaseContract.COLUMN_DIRECTION, mTrend.getDirection());
		contentValues.put(DatabaseContract.COLUMN_VERTEX, mTrend.getVertex());
		contentValues.put(DatabaseContract.COLUMN_VERTEX_LOW, mTrend.getVertexLow());
		contentValues.put(DatabaseContract.COLUMN_VERTEX_HIGH, mTrend.getVertexHigh());
		contentValues.put(DatabaseContract.COLUMN_ACTION, mAction);

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
		mCandlestick.set(stockData.mCandlestick);
		mMacd.set(stockData.mMacd);
		mTrend.set(stockData.mTrend);
		setAction(stockData.mAction);

		setNaturalRally(stockData.mNaturalRally);
		setUpwardTrend(stockData.mUpwardTrend);
		setDownwardTrend(stockData.mDownwardTrend);
		setNaturalReaction(stockData.mNaturalReaction);

		setIndex(stockData.mIndex);
		mTrend.setIndexStart(stockData.getTrend().getIndexStart());
		mTrend.setIndexEnd(stockData.getTrend().getIndexEnd());
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
		mCandlestick.set(cursor);
		mMacd.set(cursor);
		mTrend.set(cursor);
		setAction(cursor);

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

	public Candlestick getCandlestick() {
		return mCandlestick;
	}

	public Macd getMacd() {
		return mMacd;
	}

	public Trend getTrend() {
		return mTrend;
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

	public int getIndex() {
		return mIndex;
	}

	public void setIndex(int index) {
		mIndex = index;
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

	public void merge(StockData prev) {
		if ((prev == null)) {
			return;
		}

		setStockId(prev.getStockId());
		setIndex(prev.getIndex());
		mTrend.setIndexStart(prev.mTrend.getIndexStart());
		mTrend.setVertexLow(Math.min(prev.mTrend.getVertexLow(), mTrend.getVertexLow()));
		mTrend.setVertexHigh(Math.max(prev.mTrend.getVertexHigh(), mTrend.getVertexHigh()));
	}

	public void add(StockData stockData, long weight) {
		if (stockData == null) {
			return;
		}
		mCandlestick.add(stockData.mCandlestick, weight);
		mTrend.setVertexHigh(mCandlestick.getHigh());
		mTrend.setVertexLow(mCandlestick.getLow());
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

		mCandlestick.setOpen(Double.parseDouble(strings[2]));
		mCandlestick.setHigh(Double.parseDouble(strings[3]));
		mCandlestick.setLow(Double.parseDouble(strings[4]));
		mCandlestick.setClose(Double.parseDouble(strings[5]));

		mTrend.setVertexHigh(mCandlestick.getHigh());
		mTrend.setVertexLow(mCandlestick.getLow());

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
				+ mCandlestick.toString()
				+ 0 + Constant.TAB
				+ 0);
		stringBuffer.append("\r\n");
		return stringBuffer.toString();
	}
}