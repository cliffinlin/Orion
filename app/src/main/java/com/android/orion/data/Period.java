package com.android.orion.data;

import androidx.annotation.NonNull;

import com.android.orion.database.DatabaseContract;
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

	public final String mName;

	public final ArrayList<StockData> mStockDataList = new ArrayList<>();
	public final ArrayList<StockData> mDrawVertexList = new ArrayList<>();
	public final ArrayList<StockData> mDrawDataList = new ArrayList<>();
	public final ArrayList<StockData> mStrokeVertexList = new ArrayList<>();
	public final ArrayList<StockData> mStrokeDataList = new ArrayList<>();
	public final ArrayList<StockData> mSegmentVertexList = new ArrayList<>();
	public final ArrayList<StockData> mSegmentDataList = new ArrayList<>();
	public final ArrayList<StockData> mLineVertexList = new ArrayList<>();
	public final ArrayList<StockData> mLineDataList = new ArrayList<>();
	public final ArrayList<StockData> mOutlineVertexList = new ArrayList<>();
	public final ArrayList<StockData> mOutlineDataList = new ArrayList<>();

	public Period(String name) {
		mName = name;
	}

	public ArrayList<StockData> getArrayList(@NonNull String period, int type) {
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
			default:
				result = new ArrayList<>();
				break;
		}
		return result;
	}
}
