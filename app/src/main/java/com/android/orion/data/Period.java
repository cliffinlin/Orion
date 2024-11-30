package com.android.orion.data;

import android.database.Cursor;

import com.android.orion.database.StockData;

import java.util.ArrayList;

public class Period {
	public static final int TYPE_STOCK_DATA = 0;
	public static final int TYPE_DRAW_VERTEX = 1;
	public static final int TYPE_DRAW_DATA = 2;
	public static final int TYPE_STROKE_VERTEX = 3;
	public static final int TYPE_STROKE_DATA = 4;
	public static final int TYPE_SEGMENT_VERTEX = 5;
	public static final int TYPE_SEGMENT_DATA = 6;
	public static final int TYPE_LINE_VERTEX = 7;
	public static final int TYPE_LINE_DATA = 8;
	public static final int TYPE_OUTLINE_VERTEX = 9;
	public static final int TYPE_OUTLINE_DATA = 10;
	public static final int TYPE_TREND_VERTEX = 11;
	public static final int TYPE_TREND_DATA = 12;

	public static final String MONTH = "month";
	public static final String WEEK = "week";
	public static final String DAY = "day";
	public static final String MIN60 = "min60";
	public static final String MIN30 = "min30";
	public static final String MIN15 = "min15";
	public static final String MIN5 = "min5";

	public static final String[] PERIODS = {MONTH, WEEK, DAY,
			MIN60, MIN30, MIN15, MIN5};

	public final String mName;

	public ArrayList<StockData> mStockDataList = new ArrayList<>();
	public ArrayList<StockData> mDrawVertexList = new ArrayList<>();
	public ArrayList<StockData> mDrawDataList = new ArrayList<>();
	public ArrayList<StockData> mStrokeVertexList = new ArrayList<>();
	public ArrayList<StockData> mStrokeDataList = new ArrayList<>();
	public ArrayList<StockData> mSegmentVertexList = new ArrayList<>();
	public ArrayList<StockData> mSegmentDataList = new ArrayList<>();
	public ArrayList<StockData> mLineVertexList = new ArrayList<>();
	public ArrayList<StockData> mLineDataList = new ArrayList<>();
	public ArrayList<StockData> mOutlineVertexList = new ArrayList<>();
	public ArrayList<StockData> mOutlineDataList = new ArrayList<>();
	public ArrayList<StockData> mTrendVertexList = new ArrayList<>();
	public ArrayList<StockData> mTrendDataList = new ArrayList<>();

	private String mAction = "";

	public Period(String name) {
		mName = name;
	}

	public ArrayList<StockData> getArrayList(int type) {
		ArrayList<StockData> result;
		switch (type) {
			case TYPE_STOCK_DATA:
				result = mStockDataList;
				break;
			case TYPE_DRAW_VERTEX:
				result = mDrawVertexList;
				break;
			case TYPE_DRAW_DATA:
				result = mDrawDataList;
				break;
			case TYPE_STROKE_VERTEX:
				result = mStrokeVertexList;
				break;
			case TYPE_STROKE_DATA:
				result = mStrokeDataList;
				break;
			case TYPE_SEGMENT_VERTEX:
				result = mSegmentVertexList;
				break;
			case TYPE_SEGMENT_DATA:
				result = mSegmentDataList;
				break;
			case TYPE_LINE_VERTEX:
				result = mLineVertexList;
				break;
			case TYPE_LINE_DATA:
				result = mLineDataList;
				break;
			case TYPE_OUTLINE_VERTEX:
				result = mOutlineVertexList;
				break;
			case TYPE_OUTLINE_DATA:
				result = mOutlineDataList;
				break;
			case TYPE_TREND_VERTEX:
				result = mTrendVertexList;
				break;
			case TYPE_TREND_DATA:
				result = mTrendDataList;
				break;
			default:
				result = new ArrayList<>();
				break;
		}
		return result;
	}

	public String getAction() {
		return mAction;
	}

	public void setAction(String action) {
		mAction = action;
	}

	public void setAction(Cursor cursor) {
		if (cursor == null) {
			return;
		}
		setAction(cursor.getString(cursor
				.getColumnIndex(mName)));
	}
}
