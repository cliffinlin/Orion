package com.android.orion.data;

import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
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
	public static final int MINUTES_MONTH = 7200;
	public static final int MINUTES_WEEK = 1680;
	public static final int MINUTES_DAY = 240;
	public static final int MINUTES_MIN60 = 60;
	public static final int MINUTES_MIN30 = 30;
	public static final int MINUTES_MIN15 = 15;
	public static final int MINUTES_MIN5 = 5;

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
	private static final Set<String> MINUTE_PERIODS = new HashSet<>(Arrays.asList(MIN5, MIN15, MIN30, MIN60));
	private static final Map<String, Integer> MINUTES_MAP = new HashMap<String, Integer>() {{
		put(MONTH, MINUTES_MONTH);
		put(WEEK, MINUTES_WEEK);
		put(DAY, MINUTES_DAY);
		put(MIN60, MINUTES_MIN60);
		put(MIN30, MINUTES_MIN30);
		put(MIN15, MINUTES_MIN15);
		put(MIN5, MINUTES_MIN5);
	}};

	public String mName = "";
	public byte[] mThumbnail;
	public int mLevel = StockTrend.LEVEL_NONE;

	public ArrayList<StockData> mStockDataList = new ArrayList<>();
	public ArrayList<ArrayList<StockData>> mVertexLists = new ArrayList<>();
	public ArrayList<ArrayList<StockData>> mDataLists = new ArrayList<>();
	public ArrayList<ArrayList<StockTrend>> mStockTrendLists = new ArrayList<>();

	public Period(String name) {
		mName = name;

		for (int i = 0; i < StockTrend.LEVELS.length; i++) {
			mVertexLists.add(new ArrayList<>());
		}
		for (int i = 0; i < StockTrend.LEVELS.length; i++) {
			mDataLists.add(new ArrayList<>());
		}
		for (int i = 0; i < StockTrend.LEVELS.length; i++) {
			mStockTrendLists.add(new ArrayList<>());
		}
	}

	public static int getPeriodMinutes(String period) {
		int result = 0;

		if (TextUtils.isEmpty(period)) {
			return result;
		}

		if (MINUTES_MAP.containsKey(period)) {
			result = MINUTES_MAP.get(period);
		}

		return result;
	}

	public static boolean isMinutePeriod(String period) {
		if (TextUtils.isEmpty(period)) {
			return false;
		}
		return MINUTE_PERIODS.contains(period);
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

	public ArrayList<StockData> getStockDataList() {
		return mStockDataList;
	}

	public ArrayList<StockData> getDataList(int level) {
		return mDataLists.get(level);
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
				.getColumnIndex(mName)));
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
				.getColumnIndex(mName + "_" + DatabaseContract.COLUMN_LEVEL)));
	}
}
