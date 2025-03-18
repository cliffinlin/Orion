package com.android.orion.data;

import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.database.StockData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Period {
	public static final int PERIOD_MINUTES_MIN5 = 5;
	public static final int PERIOD_MINUTES_MIN15 = 15;
	public static final int PERIOD_MINUTES_MIN30 = 30;
	public static final int PERIOD_MINUTES_MIN60 = 60;
	public static final int PERIOD_MINUTES_DAY = 240;
	public static final int PERIOD_MINUTES_WEEK = 1680;
	public static final int PERIOD_MINUTES_MONTH = 7200;

	public static final String MONTH = "month";
	public static final String WEEK = "week";
	public static final String DAY = "day";
	public static final String MIN60 = "min60";
	public static final String MIN30 = "min30";
	public static final String MIN15 = "min15";
	public static final String MIN5 = "min5";

	public static final String[] PERIODS = {MONTH, WEEK, DAY,
			MIN60, MIN30, MIN15, MIN5};
	private static final Set<String> MINUTE_PERIODS = new HashSet<>();
	private static final Map<String, Integer> PERIOD_MINUTES_MAP = new HashMap<>();

	public String mName = "";
	public String mAction = "";

	public ArrayList<StockData> mStockDataList = new ArrayList<>();
	public ArrayList<ArrayList<StockData>> mVertexLists = new ArrayList<>();
	public ArrayList<ArrayList<StockData>> mDataLists = new ArrayList<>();

	public Period(String name) {
		mName = name;
		MINUTE_PERIODS.add(MIN5);
		MINUTE_PERIODS.add(MIN15);
		MINUTE_PERIODS.add(MIN30);
		MINUTE_PERIODS.add(MIN60);

		PERIOD_MINUTES_MAP.put(MIN5, PERIOD_MINUTES_MIN5);
		PERIOD_MINUTES_MAP.put(MIN15, PERIOD_MINUTES_MIN15);
		PERIOD_MINUTES_MAP.put(MIN30, PERIOD_MINUTES_MIN30);
		PERIOD_MINUTES_MAP.put(MIN60, PERIOD_MINUTES_MIN60);
		PERIOD_MINUTES_MAP.put(DAY, PERIOD_MINUTES_DAY);
		PERIOD_MINUTES_MAP.put(WEEK, PERIOD_MINUTES_WEEK);
		PERIOD_MINUTES_MAP.put(MONTH, PERIOD_MINUTES_MONTH);

		for (int i = 0; i < Trend.LEVEL_MAX; i++) {
			mVertexLists.add(new ArrayList<>());
		}
		for (int i = 0; i < Trend.LEVEL_MAX; i++) {
			mDataLists.add(new ArrayList<>());
		}
	}

	public static int getPeriodMinutes(String period) {
		int result = 0;

		if (TextUtils.isEmpty(period)) {
			return result;
		}

		if (PERIOD_MINUTES_MAP.containsKey(period)) {
			result = PERIOD_MINUTES_MAP.get(period);
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

	public ArrayList<StockData> getVertexList(int level) {
		return mVertexLists.get(level);
	}

	public String getAction() {
		return mAction;
	}

	public void setAction(String action) {
		mAction = action;
	}

	public void setAction(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}
		setAction(cursor.getString(cursor
				.getColumnIndex(mName)));
	}
}
