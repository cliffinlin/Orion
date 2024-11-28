package com.android.orion.data;

import android.database.Cursor;

import com.android.orion.database.StockData;

import java.util.ArrayList;

public class Period {
	public static final int TYPE_DATA_LEVEL_0 = 0;
	public static final int TYPE_VERTEX_LEVEL_1 = 1;
	public static final int TYPE_DATA_LEVEL_1 = 2;
	public static final int TYPE_VERTEX_LEVEL_2 = 3;
	public static final int TYPE_DATA_LEVEL_2 = 4;
	public static final int TYPE_VERTEX_LEVEL_3 = 5;
	public static final int TYPE_DATA_LEVEL_3 = 6;
	public static final int TYPE_VERTEX_LEVEL_4 = 7;
	public static final int TYPE_DATA_LEVEL_4 = 8;
	public static final int TYPE_VERTEX_LEVEL_5 = 9;
	public static final int TYPE_DATA_LEVEL_5 = 10;
	public static final int TYPE_VERTEX_LEVEL_6 = 11;
	public static final int TYPE_DATA_LEVEL_6 = 12;

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
	public ArrayList<StockData> mVertexList1 = new ArrayList<>();
	public ArrayList<StockData> mDataList1 = new ArrayList<>();
	public ArrayList<StockData> mVertexList2 = new ArrayList<>();
	public ArrayList<StockData> mDataList2 = new ArrayList<>();
	public ArrayList<StockData> mVertexList3 = new ArrayList<>();
	public ArrayList<StockData> mDataList3 = new ArrayList<>();
	public ArrayList<StockData> mVertexList4 = new ArrayList<>();
	public ArrayList<StockData> mDataList4 = new ArrayList<>();
	public ArrayList<StockData> mVertexList5 = new ArrayList<>();
	public ArrayList<StockData> mDataList5 = new ArrayList<>();
	public ArrayList<StockData> mVertexList6 = new ArrayList<>();
	public ArrayList<StockData> mDataList6 = new ArrayList<>();

	private String mAction = "";

	public Period(String name) {
		mName = name;
	}

	public ArrayList<StockData> getArrayList(int type) {
		ArrayList<StockData> result;
		switch (type) {
			case TYPE_DATA_LEVEL_0:
				result = mStockDataList;
				break;
			case TYPE_VERTEX_LEVEL_1:
				result = mVertexList1;
				break;
			case TYPE_DATA_LEVEL_1:
				result = mDataList1;
				break;
			case TYPE_VERTEX_LEVEL_2:
				result = mVertexList2;
				break;
			case TYPE_DATA_LEVEL_2:
				result = mDataList2;
				break;
			case TYPE_VERTEX_LEVEL_3:
				result = mVertexList3;
				break;
			case TYPE_DATA_LEVEL_3:
				result = mDataList3;
				break;
			case TYPE_VERTEX_LEVEL_4:
				result = mVertexList4;
				break;
			case TYPE_DATA_LEVEL_4:
				result = mDataList4;
				break;
			case TYPE_VERTEX_LEVEL_5:
				result = mVertexList5;
				break;
			case TYPE_DATA_LEVEL_5:
				result = mDataList5;
				break;
			case TYPE_VERTEX_LEVEL_6:
				result = mVertexList6;
				break;
			case TYPE_DATA_LEVEL_6:
				result = mDataList6;
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
