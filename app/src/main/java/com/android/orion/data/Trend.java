package com.android.orion.data;

import android.database.Cursor;
import android.graphics.Color;

import com.android.orion.database.DatabaseContract;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Trend {

	public static final String LABEL_NONE = "";
	public static final String LABEL_DRAW = "Draw";
	public static final String LABEL_STROKE = "Stroke";
	public static final String LABEL_SEGMENT = "Segment";
	public static final String LABEL_LINE = "Line";
	public static final String LABEL_OUTLINE = "OutLine";
	public static final String LABEL_SUPERLINE = "SuperLine";
	public static final String LABEL_TREND_LINE = "TrendLine";

	public static final int LEVEL_NONE = 0;//DATA
	public static final int LEVEL_DRAW = 1;
	public static final int LEVEL_STROKE = 2;
	public static final int LEVEL_SEGMENT = 3;
	public static final int LEVEL_LINE = 4;
	public static final int LEVEL_OUT_LINE = 5;
	public static final int LEVEL_SUPER_LINE = 6;
	public static final int LEVEL_TREND_LINE = 7;
	public static final int LEVEL_MAX = LEVEL_TREND_LINE + 1;

	public static final int DIRECTION_NONE = 0;
	public static final int DIRECTION_UP = 1 << 0;
	public static final int DIRECTION_DOWN = 1 << 1;

	public static final int VERTEX_NONE = 0;
	public static final int VERTEX_TOP = 1 << 0;
	public static final int VERTEX_BOTTOM = 1 << 1;
	public static final int VERTEX_TOP_STROKE = 1 << 2;
	public static final int VERTEX_BOTTOM_STROKE = 1 << 3;
	public static final int VERTEX_TOP_SEGMENT = 1 << 4;
	public static final int VERTEX_BOTTOM_SEGMENT = 1 << 5;
	public static final int VERTEX_TOP_LINE = 1 << 6;
	public static final int VERTEX_BOTTOM_LINE = 1 << 7;
	public static final int VERTEX_TOP_OUTLINE = 1 << 8;
	public static final int VERTEX_BOTTOM_OUTLINE = 1 << 9;
	public static final int VERTEX_TOP_SUPERLINE = 1 << 10;
	public static final int VERTEX_BOTTOM_SUPERLINE = 1 << 11;
	public static final int VERTEX_TOP_TREND_LINE = 1 << 12;
	public static final int VERTEX_BOTTOM_TREND_LINE = 1 << 13;

	public static final String TYPE_UP_NONE_UP = "UNU";
	public static final String TYPE_UP_NONE_DOWN = "UND";
	public static final String TYPE_UP_NONE = "UN";
	public static final String TYPE_UP_DOWN = "UD";
	public static final String TYPE_UP_UP = "UU";
	public static final String TYPE_NONE = "";
	public static final String TYPE_DOWN_DOWN = "DD";
	public static final String TYPE_DOWN_UP = "DU";
	public static final String TYPE_DOWN_NONE = "DN";
	public static final String TYPE_DOWN_NONE_UP = "DNU";
	public static final String TYPE_DOWN_NONE_DOWN = "DND";

	public static final int FLAG_UNUSED = -1;
	public static final int FLAG_NONE = 0;
	public static final int FLAG_CHANGED = 1 << 0;
	public static final int FLAG_ADAPTIVE = 1 << 1;

	public static final int GROUPED_NONE = 0;

	public static final int[] LEVELS = { LEVEL_NONE,
			LEVEL_DRAW, LEVEL_STROKE, LEVEL_SEGMENT, LEVEL_LINE,
			LEVEL_OUT_LINE, LEVEL_SUPER_LINE, LEVEL_TREND_LINE};

	public static final int[] COLORS = {
			Color.WHITE, Color.GRAY, Color.YELLOW, Color.BLACK,
			Color.BLUE, Color.RED, Color.MAGENTA, Color.CYAN};

	public static final String[] TYPES = {
			TYPE_UP_NONE_UP, TYPE_UP_NONE_DOWN, TYPE_UP_NONE,
			TYPE_UP_DOWN, TYPE_UP_UP,
			TYPE_DOWN_DOWN, TYPE_DOWN_UP,
			TYPE_DOWN_NONE, TYPE_DOWN_NONE_UP, TYPE_DOWN_NONE_DOWN};

	public static final String MARK_NONE = "";

	public static final String MARK_BUY = "B";
	public static final String MARK_SELL = "S";
	public static final String MARK_BUY1 = "B1";
	public static final String MARK_BUY2 = "B2";
	public static final String MARK_SELL1 = "S1";
	public static final String MARK_SELL2 = "S2";

	public static final String MARK_LEVEL = "L";

	public static final int VERTEX_SIZE = 3;
	public static final int ADAPTIVE_SIZE = 8;

	public static Set<String> NOTIFYACTIONS = new HashSet<>(Arrays.asList(
			MARK_BUY, MARK_BUY1, MARK_BUY2,
			MARK_SELL, MARK_SELL1, MARK_SELL2));

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
		int vertex = VERTEX_NONE;
		if (prev == null || next == null) {
			return vertex;
		}

		if (upTo(prev) && upTo(next)) {
			vertex = VERTEX_TOP;
		} else if (downTo(prev) && downTo(next)) {
			vertex = VERTEX_BOTTOM;
		}
		return vertex;
	}

	public int directionTo(Trend trend) {
		int result = DIRECTION_NONE;
		if (trend == null) {
			return result;
		}
		if (upTo(trend)) {
			result = DIRECTION_UP;
		} else if (downTo(trend)) {
			result = DIRECTION_DOWN;
		}
		return result;
	}

	public void merge(int direction, Trend trend) {
		if (direction == DIRECTION_UP) {
			setVertexHigh(Math.max(getVertexHigh(), trend.getVertexHigh()));
			setVertexLow(Math.max(getVertexLow(), trend.getVertexLow()));
		} else if (direction == DIRECTION_DOWN) {
			setVertexHigh(Math.min(getVertexHigh(), trend.getVertexHigh()));
			setVertexLow(Math.min(getVertexLow(), trend.getVertexLow()));
		} else {
			setVertexHigh(Math.max(getVertexHigh(), trend.getVertexHigh()));
			setVertexLow(Math.min(getVertexLow(), trend.getVertexLow()));
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
}
