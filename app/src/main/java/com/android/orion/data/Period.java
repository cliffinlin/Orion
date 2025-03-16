package com.android.orion.data;

import android.database.Cursor;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.android.orion.database.StockData;

import java.util.ArrayList;

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

	public String mName = "";
	public String mAction = "";

	public ArrayList<StockData> mStockDataList = new ArrayList<>();
	public ArrayList<ArrayList<StockData>> mVertexLists = new ArrayList<>();
	public ArrayList<ArrayList<StockData>> mDataLists = new ArrayList<>();

	public Period(String name) {
		mName = name;
		for (int i = 0; i < Trend.LEVEL_MAX; i++) {
			mVertexLists.add(new ArrayList<>());
		}
		for (int i = 0; i < Trend.LEVEL_MAX; i++) {
			mDataLists.add(new ArrayList<>());
		}
	}

	public static int getPeriodMinutes(String period) {
		int result = 0;

		if (TextUtils.equals(period, Period.MIN5)) {
			result = PERIOD_MINUTES_MIN5;
		} else if (TextUtils.equals(period, Period.MIN15)) {
			result = PERIOD_MINUTES_MIN15;
		} else if (TextUtils.equals(period, Period.MIN30)) {
			result = PERIOD_MINUTES_MIN30;
		} else if (TextUtils.equals(period, Period.MIN60)) {
			result = PERIOD_MINUTES_MIN60;
		} else if (TextUtils.equals(period, Period.DAY)) {
			result = PERIOD_MINUTES_DAY;
		} else if (TextUtils.equals(period, Period.WEEK)) {
			result = PERIOD_MINUTES_WEEK;
		} else if (TextUtils.equals(period, Period.MONTH)) {
			result = PERIOD_MINUTES_MONTH;
		}

		return result;
	}

	public static boolean isMinutePeriod(@NonNull String period) {
		boolean result = false;

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
