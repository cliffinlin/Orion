package com.android.orion.data;

import android.database.Cursor;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.StockData;

public class Trend {
	private int mIndex;
	private int mIndexStart;
	private int mIndexEnd;
	private int mDirection;
	private int mVertex;
	private double mVertexLow;
	private double mVertexHigh;

	public void set(Trend trend) {
		if (trend == null) {
			return;
		}
		setDirection(trend.mDirection);
		setVertex(trend.mVertex);
		setVertexLow(trend.mVertexLow);
		setVertexHigh(trend.mVertexHigh);
	}

	public void set(Cursor cursor) {
		setDirection(cursor);
		setVertex(cursor);
		setVertexLow(cursor);
		setVertexHigh(cursor);
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

	public int getDirection() {
		return mDirection;
	}

	public void setDirection(int direction) {
		mDirection = direction;
	}

	void setDirection(Cursor cursor) {
		if (cursor == null) {
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
		if (cursor == null) {
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
		if (cursor == null) {
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
		if (cursor == null) {
			return;
		}

		setVertexHigh(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VERTEX_HIGH)));
	}

	public boolean directionOf(int direction) {
		return (mDirection & direction) == direction;
	}

	public boolean vertexOf(int vertex) {
		return (mVertex & vertex) == vertex;
	}

	public boolean upTo(Trend trend) {
		if (trend == null) {
			return false;
		}
		return (mVertexHigh > trend.mVertexHigh) && (mVertexLow > trend.mVertexLow);
	}

	public boolean downTo(Trend trend) {
		if (trend == null) {
			return false;
		}
		return (mVertexHigh < trend.mVertexHigh) && (mVertexLow < trend.mVertexLow);
	}

	public boolean include(Trend trend) {
		if (trend == null) {
			return false;
		}
		return (mVertexHigh >= trend.mVertexHigh) && (mVertexLow <= trend.mVertexLow);
	}

	public boolean includedBy(Trend trend) {
		if (trend == null) {
			return false;
		}
		return (mVertexHigh <= trend.mVertexHigh) && (mVertexLow >= trend.mVertexLow);
	}

	public int vertexTo(Trend prev, Trend next) {
		int vertex = StockData.VERTEX_NONE;
		if (prev == null || next == null) {
			return vertex;
		}

		if (upTo(prev) && upTo(next)) {
			vertex = StockData.VERTEX_TOP_LEVEL_1;
		} else if (downTo(prev) && downTo(next)) {
			vertex = StockData.VERTEX_BOTTOM_LEVEL_1;
		}
		return vertex;
	}

	public int directionTo(Trend trend) {
		int result = StockData.DIRECTION_NONE;
		if (trend == null) {
			return result;
		}
		if (upTo(trend)) {
			result = StockData.DIRECTION_UP_LEVEL_1;
		} else if (downTo(trend)) {
			result = StockData.DIRECTION_DOWN_LEVEL_1;
		}
		return result;
	}

	public void merge(int direction, Trend trend) {
		if (direction == StockData.DIRECTION_UP_LEVEL_1) {
			setVertexHigh(Math.max(getVertexHigh(), trend.getVertexHigh()));
			setVertexLow(Math.max(getVertexLow(), trend.getVertexLow()));
		} else if (direction == StockData.DIRECTION_DOWN_LEVEL_1) {
			setVertexHigh(Math.min(getVertexHigh(), trend.getVertexHigh()));
			setVertexLow(Math.min(getVertexLow(), trend.getVertexLow()));
		} else {
			setVertexHigh(Math.max(getVertexHigh(), trend.getVertexHigh()));
			setVertexLow(Math.min(getVertexLow(), trend.getVertexLow()));
		}
	}
}
