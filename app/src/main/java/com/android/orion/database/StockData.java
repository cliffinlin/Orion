package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.data.Candlestick;
import com.android.orion.data.Macd;
import com.android.orion.data.Trend;
import com.android.orion.setting.Constant;
import com.android.orion.utility.Utility;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public class StockData extends DatabaseTable {

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
	private String mSE;
	private String mCode;
	private String mName;
	private int mLevel;
	private String mDate;
	private String mTime;
	private String mPeriod;
	private String mAction;
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
		return TextUtils.isEmpty(mDate)
				&& TextUtils.isEmpty(mTime);
	}

	public void init() {
		super.init();

		setTableName(DatabaseContract.StockData.TABLE_NAME);

		mSE = "";
		mCode = "";
		mName = "";
		mLevel = Trend.LEVEL_NONE;
		mDate = "";
		mTime = "";
		mPeriod = "";
		mAction = "";
		mIndex = 0;
	}

	@Override
	public ContentValues getContentValues() {
		ContentValues contentValues = super.getContentValues();

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
		return contentValues;
	}

	public void set(StockData stockData) {
		if (stockData == null) {
			return;
		}

		init();

		super.set(stockData);

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

		setIndex(stockData.mIndex);
		mTrend.setIndexStart(stockData.getTrend().getIndexStart());
		mTrend.setIndexEnd(stockData.getTrend().getIndexEnd());
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
		setLevel(cursor);
		setDate(cursor);
		setTime(cursor);
		setPeriod(cursor);
		mCandlestick.set(cursor);
		mMacd.set(cursor);
		mTrend.set(cursor);
		setAction(cursor);
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
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setAction(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ACTION)));
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

		setSE(prev.getSE());
		setCode(prev.getCode());
		setName(prev.getName());
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

		dateString = strings[0].replace("/", Constant.MARK_MINUS);
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
		String dateString = getDate().replace(Constant.MARK_MINUS, "/");
		String timeString = getTime().substring(0, 5).replace(":", "");
		stringBuffer.append(dateString + Constant.TAB
				+ timeString + Constant.TAB
				+ mCandlestick.toString()
				+ 0 + Constant.TAB
				+ 0);
		stringBuffer.append("\r\n");
		return stringBuffer.toString();
	}

	public static StockData getSafely(List<StockData> list, int index) {
		if (list == null) {
			return null;
		}
		if (index < 0 || index >= list.size()) {
			return null;
		}
		return list.get(index);
	}

	public static StockData getLast(List<StockData> list, int index) {
		if (list == null) {
			return null;
		}

		int size = list.size();
		if (index < 0 || index >= size) {
			return null;
		}

		int i = size - 1 - index;
		return list.get(i);
	}

	public static Trend getLastTrend(List<StockData> list, int index) {
		if (list == null || index < 0 || index >= list.size()) {
			return null;
		}

		StockData data = getLast(list, index);
		return data != null ? data.getTrend() : null;
	}

	public static StockData getLast(List<StockData> list, int index, List<StockData> stockDataList) {
		if (list == null || stockDataList == null || stockDataList.isEmpty()) {
			return null;
		}

		Trend trend = getLastTrend(list, index);
		if (trend == null) {
			return null;
		}

		int startIndex = trend.getIndexStart();
		if (startIndex < 0 || startIndex >= stockDataList.size()) {
			return null;
		}
		return stockDataList.get(startIndex);
	}
}