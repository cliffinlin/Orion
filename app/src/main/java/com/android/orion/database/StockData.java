package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.text.TextUtils;

import com.android.orion.data.Candle;
import com.android.orion.data.Macd;
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

	private String mSE;
	private String mCode;
	private String mName;
	private String mPeriod;
	private String mDate;
	private String mTime;
	private Candle mCandle;
	private Macd mMacd;
	private int mLevel;
	private String mAction;
	private int mDirection;
	private int mVertex;
	private double mVertexLow;
	private double mVertexHigh;

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
		return TextUtils.isEmpty(mDate)
				&& TextUtils.isEmpty(mTime);
	}

	public void init() {
		super.init();

		setTableName(DatabaseContract.StockData.TABLE_NAME);

		mSE = "";
		mCode = "";
		mName = "";
		mPeriod = "";
		mDate = "";
		mTime = "";
		if (mCandle == null) {
			mCandle = new Candle();
		}
		if (mMacd == null) {
			mMacd = new Macd();
		}
		mLevel = StockTrend.LEVEL_NONE;
		mAction = StockTrend.TYPE_NONE;
		mDirection = StockTrend.DIRECTION_NONE;
		mVertex = StockTrend.VERTEX_NONE;
		mVertexHigh = StockTrend.VERTEX_NONE;
		mVertexLow = StockTrend.VERTEX_NONE;

		mIndex = 0;
		mIndexStart = 0;
		mIndexEnd = 0;
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
		contentValues.put(DatabaseContract.COLUMN_OPEN, mCandle.getOpen());
		contentValues.put(DatabaseContract.COLUMN_HIGH, mCandle.getHigh());
		contentValues.put(DatabaseContract.COLUMN_LOW, mCandle.getLow());
		contentValues.put(DatabaseContract.COLUMN_CLOSE, mCandle.getClose());
		contentValues.put(DatabaseContract.COLUMN_AVERAGE5, mMacd.getAverage5());
		contentValues.put(DatabaseContract.COLUMN_AVERAGE10, mMacd.getAverage10());
		contentValues.put(DatabaseContract.COLUMN_DIF, mMacd.getDIF());
		contentValues.put(DatabaseContract.COLUMN_DEA, mMacd.getDEA());
		contentValues.put(DatabaseContract.COLUMN_HISTOGRAM, mMacd.getHistogram());
		contentValues.put(DatabaseContract.COLUMN_LEVEL, mLevel);
		contentValues.put(DatabaseContract.COLUMN_ACTION, mAction);
		contentValues.put(DatabaseContract.COLUMN_DIRECTION, mDirection);
		contentValues.put(DatabaseContract.COLUMN_VERTEX, mVertex);
		contentValues.put(DatabaseContract.COLUMN_VERTEX_LOW, mVertexLow);
		contentValues.put(DatabaseContract.COLUMN_VERTEX_HIGH, mVertexHigh);

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
		setPeriod(stockData.mPeriod);
		setDate(stockData.mDate);
		setTime(stockData.mTime);
		mCandle.set(stockData.mCandle);
		mMacd.set(stockData.mMacd);
		setLevel(stockData.mLevel);
		setAction(stockData.mAction);
		setDirection(stockData.mDirection);
		setVertex(stockData.mVertex);
		setVertexLow(stockData.mVertexLow);
		setVertexHigh(stockData.mVertexHigh);
		setIndex(stockData.mIndex);
		setIndexStart(stockData.mIndexStart);
		setIndexEnd(stockData.mIndexEnd);
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
		mCandle.set(cursor);
		mMacd.set(cursor);
		setLevel(cursor);
		setAction(cursor);
		setDirection(cursor);
		setVertex(cursor);
		setVertexLow(cursor);
		setVertexHigh(cursor);
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
			return mDate + " " + "00:00:00";
		}
	}

	public Candle getCandle() {
		return mCandle;
	}

	public Macd getMacd() {
		return mMacd;
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
		setIndexStart(prev.getIndexStart());
		setVertexLow(Math.min(prev.getVertexLow(), getVertexLow()));
		setVertexHigh(Math.max(prev.getVertexHigh(), getVertexHigh()));
	}

	public void add(StockData stockData, long weight) {
		if (stockData == null) {
			return;
		}
		mCandle.add(stockData.mCandle, weight);
		setVertexHigh(mCandle.getHigh());
		setVertexLow(mCandle.getLow());
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

	public int getVertex() {
		return mVertex;
	}

	public void setVertex(int vertex) {
		mVertex = vertex;
	}

	void setVertex(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setVertexHigh(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VERTEX_HIGH)));
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

	public boolean directionOf(int direction) {
		return (mDirection & direction) == direction;
	}

	public boolean vertexOf(int vertex) {
		return (mVertex & vertex) == vertex;
	}

	public boolean upTo(StockData stockData) {
		if (stockData == null) {
			return false;
		}
		return (mVertexHigh > stockData.mVertexHigh) && (mVertexLow > stockData.mVertexLow);
	}

	public boolean downTo(StockData stockData) {
		if (stockData == null) {
			return false;
		}
		return (mVertexHigh < stockData.mVertexHigh) && (mVertexLow < stockData.mVertexLow);
	}

	public boolean include(StockData stockData) {
		if (stockData == null) {
			return false;
		}
		return (mVertexHigh >= stockData.mVertexHigh) && (mVertexLow <= stockData.mVertexLow);
	}

	public boolean includedBy(StockData stockData) {
		if (stockData == null) {
			return false;
		}
		return (mVertexHigh <= stockData.mVertexHigh) && (mVertexLow >= stockData.mVertexLow);
	}

	public int vertexTo(StockData prev, StockData next) {
		int vertex = StockTrend.VERTEX_NONE;
		if (prev == null || next == null) {
			return vertex;
		}

		if (upTo(prev) && upTo(next)) {
			vertex = StockTrend.VERTEX_TOP;
		} else if (downTo(prev) && downTo(next)) {
			vertex = StockTrend.VERTEX_BOTTOM;
		}
		return vertex;
	}

	public int directionTo(StockData stockData) {
		int result = StockTrend.DIRECTION_NONE;
		if (stockData == null) {
			return result;
		}
		if (upTo(stockData)) {
			result = StockTrend.DIRECTION_UP;
		} else if (downTo(stockData)) {
			result = StockTrend.DIRECTION_DOWN;
		}
		return result;
	}

	public void merge(int direction, StockData stockData) {
		if (direction == StockTrend.DIRECTION_UP) {
			setVertexHigh(Math.max(getVertexHigh(), stockData.getVertexHigh()));
			setVertexLow(Math.max(getVertexLow(), stockData.getVertexLow()));
		} else if (direction == StockTrend.DIRECTION_DOWN) {
			setVertexHigh(Math.min(getVertexHigh(), stockData.getVertexHigh()));
			setVertexLow(Math.min(getVertexLow(), stockData.getVertexLow()));
		} else {
			setVertexHigh(Math.max(getVertexHigh(), stockData.getVertexHigh()));
			setVertexLow(Math.min(getVertexLow(), stockData.getVertexLow()));
		}
	}

	public void addVertex(int flag) {
		mVertex |= flag;
	}

	public void removeVertex(int flag) {
		if (hasVertex(flag)) {
			mVertex &= ~flag;
		}
	}

	public boolean hasVertex(int flag) {
		return (mVertex & flag) == flag;
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

		mCandle.setOpen(Double.parseDouble(strings[2]));
		mCandle.setHigh(Double.parseDouble(strings[3]));
		mCandle.setLow(Double.parseDouble(strings[4]));
		mCandle.setClose(Double.parseDouble(strings[5]));

		setVertexHigh(mCandle.getHigh());
		setVertexLow(mCandle.getLow());

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
				+ mCandle.toString()
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

	public static StockData getLast(List<StockData> list, int index, List<StockData> stockDataList) {
		if (list == null || stockDataList == null || stockDataList.isEmpty()) {
			return null;
		}

		StockData stockData = getLast(list, index);
		if (stockData == null) {
			return null;
		}

		int startIndex = stockData.getIndexStart();
		if (startIndex < 0 || startIndex >= stockDataList.size()) {
			return null;
		}
		return stockDataList.get(startIndex);
	}
}