package com.android.orion.data;

import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.StockData;
import com.android.orion.database.StockTrend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Period {
	public static final int MONTH_IN_MINUTES = 7200;
	public static final int WEEK_IN_MINUTES = 1680;
	public static final int DAY_IN_MINUTES = 240;
	public static final int MIN60_IN_MINUTES = 60;
	public static final int MIN30_IN_MINUTES = 30;
	public static final int MIN15_IN_MINUTES = 15;
	public static final int MIN5_IN_MINUTES = 5;

	public static final String YEAR = "year";
	public static final String MONTH6 = "month6";
	public static final String QUARTER = "quarter";
	public static final String MONTH2 = "month2";
	public static final String MONTH = "month";
	public static final String WEEK = "week";
	public static final String DAY = "day";
	public static final String MIN60 = "min60";
	public static final String MIN30 = "min30";
	public static final String MIN15 = "min15";
	public static final String MIN5 = "min5";

	public static final String[] PERIODS = {YEAR, MONTH6, QUARTER, MONTH2, MONTH, WEEK, DAY, MIN60, MIN30, MIN15, MIN5};
	private static final Set<String> PERIOD_SET = new HashSet<>(Arrays.asList(PERIODS));
	private static final Set<String> MINUTE_PERIOD_SET = new HashSet<>(Arrays.asList(MIN60, MIN30, MIN15, MIN5));
	private static final Map<String, Integer> PERIOD_IN_MINUTES_MAP = new HashMap<String, Integer>() {{
		put(MONTH, MONTH_IN_MINUTES);
		put(WEEK, WEEK_IN_MINUTES);
		put(DAY, DAY_IN_MINUTES);
		put(MIN60, MIN60_IN_MINUTES);
		put(MIN30, MIN30_IN_MINUTES);
		put(MIN15, MIN15_IN_MINUTES);
		put(MIN5, MIN5_IN_MINUTES);
	}};

	public String mName = "";
	public byte[] mThumbnail;
	public int mLevel = StockTrend.LEVEL_NONE;
	public String mTrend = "";
	public PolarComponent mPolarComponent;

	public ArrayList<ArrayList<StockData>> mVertexLists = new ArrayList<>();
	public ArrayList<ArrayList<StockData>> mStockDataLists = new ArrayList<>();
	public ArrayList<ArrayList<StockTrend>> mStockTrendLists = new ArrayList<>();

	public Period(String name) {
		mName = name;

		for (int i = 0; i < StockTrend.LEVELS.length; i++) {
			mVertexLists.add(new ArrayList<>());
		}
		for (int i = 0; i < StockTrend.LEVELS.length; i++) {
			mStockDataLists.add(new ArrayList<>());
		}
		for (int i = 0; i < StockTrend.LEVELS.length; i++) {
			mStockTrendLists.add(new ArrayList<>());
		}
	}

	public static int getPeriodInMinutes(String period) {
		int result = 0;

		if (TextUtils.isEmpty(period)) {
			return result;
		}

		if (PERIOD_IN_MINUTES_MAP.containsKey(period)) {
			result = PERIOD_IN_MINUTES_MAP.get(period);
		}

		return result;
	}

	public static boolean isMinutePeriod(String period) {
		if (TextUtils.isEmpty(period)) {
			return false;
		}
		return MINUTE_PERIOD_SET.contains(period);
	}

	public static int indexOf(String period) {
		int index = 0;
		if (TextUtils.isEmpty(period)) {
			return index;
		}

		for (int i = 0; i < PERIODS.length; i++) {
			if (TextUtils.equals(period, PERIODS[i])) {
				index = i;
				break;
			}
		}
		return index;
	}

	public ArrayList<StockData> getStockDataList(int level) {
		return mStockDataLists.get(level);
	}

	public ArrayList<StockTrend> getStockTrendList(int level) {
		return mStockTrendLists.get(level);
	}

	public ArrayList<StockData> getVertexList(int level) {
		return mVertexLists.get(level);
	}

	public byte[] getThumbnail() {
		return mThumbnail;
	}

	public void setThumbnail(byte[] thumbnail) {
		mThumbnail = thumbnail;
	}

	public void setThumbnail(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}
		setThumbnail(cursor.getBlob(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PERIOD_THUMBNAIL(mName))));
	}

	public int getLevel() {
		return mLevel;
	}

	public void setLevel(int Level) {
		mLevel = Level;
	}

	public void setLevel(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}
		setLevel(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PERIOD_LEVEL(mName))));
	}

	public String getTrend() {
		return mTrend;
	}

	public void setTrend(String trend) {
		mTrend = trend;
	}

	public void setTrend(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}
		setTrend(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PERIOD_TREND(mName))));
	}

	public PolarComponent getPolarComponent() {
		return mPolarComponent;
	}

	public void setPolarComponent(PolarComponent polarComponent) {
		mPolarComponent = polarComponent;
	}

	public static final String fromColumnName(String columnName) {
		String result = "";
		if (TextUtils.isEmpty(columnName)) {
			return result;
		}
		String[] strings = columnName.split("_");
		if (strings == null || strings.length < 2) {
			return result;
		}
		String period = strings[0];
		if (PERIOD_SET.contains(period)) {
			result = period;
		}
		return result;
	}
}
