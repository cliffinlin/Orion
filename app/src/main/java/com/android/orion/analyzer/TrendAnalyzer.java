package com.android.orion.analyzer;

import com.android.orion.data.Trend;
import com.android.orion.database.StockData;
import com.android.orion.utility.Logger;

import java.util.ArrayList;

public class TrendAnalyzer {

	private static TrendAnalyzer mInstance;
	Logger Log = Logger.getLogger();

	private TrendAnalyzer() {
	}

	public static synchronized TrendAnalyzer getInstance() {
		if (mInstance == null) {
			mInstance = new TrendAnalyzer();
		}
		return mInstance;
	}

	void analyzeVertex(ArrayList<StockData> dataList,
	                   ArrayList<StockData> vertexList) {
		int i;
		int size;
		int direction = Trend.DIRECTION_NONE;
		int vertex = Trend.VERTEX_NONE;

		StockData prev;
		StockData current;
		StockData next;

		if ((dataList == null) || (vertexList == null)) {
			return;
		}

		size = dataList.size();
		if (size < Trend.VERTEX_SIZE) {
			return;
		}

		vertexList.clear();

		prev = new StockData();
		current = new StockData();
		next = new StockData();

		prev.init();
		current.init();
		next.init();

		for (i = 1; i < size - 1; i++) {
			if (prev.isEmpty()) {
				prev.set(dataList.get(i - 1));
			}

			if (current.isEmpty()) {
				current.set(dataList.get(i));
			}

			if (next.isEmpty()) {
				next.set(dataList.get(i + 1));
			}

			if (current.getTrend().include(prev.getTrend()) || current.getTrend().includedBy(prev.getTrend())) {
				prev.getTrend().merge(direction, current.getTrend());
				current.getTrend().merge(direction, prev.getTrend());

				dataList.get(i - 1).set(prev);
				dataList.get(i).set(current);

				prev.set(current);
				current.init();
				next.init();
				continue;
			}

			direction = current.getTrend().directionTo(prev.getTrend());
			vertex = current.getTrend().vertexTo(prev.getTrend(), next.getTrend());

			dataList.get(i).getTrend().setDirection(direction);
			dataList.get(i).getTrend().setVertex(vertex);

			if ((vertex == Trend.VERTEX_TOP)
					|| (vertex == Trend.VERTEX_BOTTOM)) {
				vertexList.add(dataList.get(i));
			}

			if (current.getTrend().include(next.getTrend()) || current.getTrend().includedBy(next.getTrend())) {
				if (i < size - 2) {//TODO
				current.getTrend().merge(direction, next.getTrend());
				next.getTrend().merge(direction, current.getTrend());

				dataList.get(i).set(current);
				dataList.get(i + 1).set(next);

				current.set(next);
				next.init();
				continue;
				}
			}

			prev.set(current);
			current.set(next);
			next.init();
		}

		i = size - 1;
		if (vertex == Trend.VERTEX_TOP) {
			direction = Trend.DIRECTION_DOWN;
		} else if (vertex == Trend.VERTEX_BOTTOM) {
			direction = Trend.DIRECTION_UP;
		}

		dataList.get(i).getTrend().setDirection(direction);

		if (vertexList.size() > 0) {
			i = 0;
			if (vertexList.get(0).getTrend().vertexOf(Trend.VERTEX_TOP)) {
				dataList.get(i).getTrend().setVertex(Trend.VERTEX_BOTTOM);
			} else if (vertexList.get(0).getTrend().vertexOf(Trend.VERTEX_BOTTOM)) {
				dataList.get(i).getTrend().setVertex(Trend.VERTEX_TOP);
			}
		}
	}

	void analyzeVertex(ArrayList<StockData> stockDataList, ArrayList<StockData> dataList, ArrayList<StockData> vertexList, int vertexTop, int vertexBottom) {
		int i;
		int size;
		int direction = Trend.DIRECTION_NONE;
		int vertex = Trend.VERTEX_NONE;
		StockData stockDataTop;
		StockData stockDataBottom;

		StockData prev;
		StockData current;
		StockData next;

		if ((stockDataList == null) || (dataList == null) || (vertexList == null)) {
			return;
		}

		size = dataList.size();
		if (size < Trend.VERTEX_SIZE) {
			return;
		}

		vertexList.clear();

		prev = new StockData();
		current = new StockData();
		next = new StockData();

		prev.init();
		current.init();
		next.init();

		for (i = 1; i < size - 1; i++) {
			if (prev.isEmpty()) {
				prev.set(dataList.get(i - 1));
			}

			if (current.isEmpty()) {
				current.set(dataList.get(i));
			}

			if (next.isEmpty()) {
				next.set(dataList.get(i + 1));
			}

			if (current.getTrend().include(prev.getTrend()) || current.getTrend().includedBy(prev.getTrend())) {
				prev.getTrend().merge(direction, current.getTrend());
				current.getTrend().merge(direction, prev.getTrend());

				dataList.get(i - 1).set(prev);
				dataList.get(i).set(current);

				prev.set(current);
				current.init();
				next.init();
				continue;
			}

			direction = current.getTrend().directionTo(prev.getTrend());
			dataList.get(i).getTrend().setDirection(direction);

			if (stockDataList.get(dataList.get(i - 1).getTrend().getIndexStart()).getTrend().vertexOf(Trend.VERTEX_TOP)) {
				stockDataTop = stockDataList.get(dataList.get(i - 1).getTrend().getIndexStart());
				stockDataBottom = stockDataList.get(dataList.get(i - 1).getTrend().getIndexEnd());
			} else {
				stockDataTop = stockDataList.get(dataList.get(i - 1).getTrend().getIndexEnd());
				stockDataBottom = stockDataList.get(dataList.get(i - 1).getTrend().getIndexStart());
			}
			vertex = current.getTrend().vertexTo(prev.getTrend(), next.getTrend());
			if (vertex == Trend.VERTEX_TOP) {
				stockDataTop.getTrend().addVertex(vertexTop);
				vertexList.add(dataList.get(i));
			} else if (vertex == Trend.VERTEX_BOTTOM) {
				stockDataBottom.getTrend().addVertex(vertexBottom);
				vertexList.add(dataList.get(i));
			}

			if (current.getTrend().include(next.getTrend()) || current.getTrend().includedBy(next.getTrend())) {
				if (i < size - 2) {//TODO
					current.getTrend().merge(direction, next.getTrend());
					next.getTrend().merge(direction, current.getTrend());

					dataList.get(i).set(current);
					dataList.get(i + 1).set(next);

					current.set(next);
					next.init();
					continue;
				}
			}

			prev.set(current);
			current.set(next);
			next.init();
		}

		i = size - 1;
		if (vertex == Trend.VERTEX_TOP) {
			direction = Trend.DIRECTION_DOWN;
		} else if (vertex == Trend.VERTEX_BOTTOM) {
			direction = Trend.DIRECTION_UP;
		}

		dataList.get(i).getTrend().setDirection(direction);

		if (vertexList.size() > 0) {
			i = 0;
			if (vertexList.get(0).getTrend().vertexOf(Trend.VERTEX_TOP)) {
				dataList.get(i).getTrend().setVertex(Trend.VERTEX_BOTTOM);
			} else if (vertexList.get(0).getTrend().vertexOf(Trend.VERTEX_BOTTOM)) {
				dataList.get(i).getTrend().setVertex(Trend.VERTEX_TOP);
			}
		}
	}

	void analyzeLine(ArrayList<StockData> stockDataList,
	                 ArrayList<StockData> dataList, ArrayList<StockData> vertexList,
	                 int vertexTypeTop, int vertexTypeBottom) {
		int i;
		int size;
		int direction;
		int vertex = Trend.VERTEX_NONE;
		StockData stockData;

		if ((stockDataList == null) || (dataList == null)
				|| (vertexList == null)) {
			return;
		}

		size = dataList.size();
		if (size < Trend.VERTEX_SIZE) {
			return;
		}

		vertexList.clear();

		i = 0;
		direction = dataList.get(i).getTrend().getDirection();

		for (i = 2; i < size; i++) {
			if (dataList.get(i).getTrend().directionTo(dataList.get(i - 2).getTrend()) == direction) {
				i++;

				if (i == size - 1) {
					stockData = stockDataList.get(dataList.get(i - 1)
							.getTrend().getIndexEnd());
					if (direction == Trend.DIRECTION_UP) {
						if (dataList.get(i).getTrend().getVertexLow() < dataList
								.get(i - 1).getTrend().getVertexLow()) {
							vertex = vertexTypeTop;
							stockData.getTrend().addVertex(vertex);
							vertexList.add(stockData);
						}
					} else if (direction == Trend.DIRECTION_DOWN) {
						if (dataList.get(i).getTrend().getVertexHigh() > dataList.get(
								i - 1).getTrend().getVertexHigh()) {
							vertex = vertexTypeBottom;
							stockData.getTrend().addVertex(vertex);
							vertexList.add(stockData);
						}
					}
				}
			} else {
				stockData = stockDataList
						.get(dataList.get(i - 2).getTrend().getIndexEnd());
				if (direction == Trend.DIRECTION_UP) {
					vertex = vertexTypeTop;
				} else if (direction == Trend.DIRECTION_DOWN) {
					vertex = vertexTypeBottom;
				} else {
					Log.d("directionType = " + direction);
				}
				stockData.getTrend().addVertex(vertex);
				vertexList.add(stockData);
				direction = dataList.get(i - 1).getTrend().getDirection();
			}
		}
	}

	void extendVertexList(int index, ArrayList<StockData> stockDataList,
	                      ArrayList<StockData> vertexList) {
		int i;
		int j;
		StockData stockData;

		if (index == 0) {
			i = 0;
			j = 0;
		} else {
			i = stockDataList.size() - 1;
			j = vertexList.size() - 1;
		}

		stockData = new StockData();
		stockData.set(stockDataList.get(i));

		if (vertexList.get(j).getTrend().vertexOf(Trend.VERTEX_TOP)) {
			if (stockDataList.get(i).getTrend().getVertexHigh() > vertexList.get(j).getTrend().getVertexHigh()) {
				stockData.getTrend().setVertex(Trend.VERTEX_TOP);
			} else {
				stockData.getTrend().setVertex(Trend.VERTEX_BOTTOM);
			}
		} else if (vertexList.get(j).getTrend().vertexOf(Trend.VERTEX_BOTTOM)) {
			if (stockDataList.get(i).getTrend().getVertexLow() < vertexList.get(j).getTrend().getVertexLow()) {
				stockData.getTrend().setVertex(Trend.VERTEX_BOTTOM);
			} else {
				stockData.getTrend().setVertex(Trend.VERTEX_TOP);
			}
		}

		if (index == 0) {
			vertexList.add(0, stockData);
		} else {
			vertexList.add(stockData);
		}
	}

	void vertexListToDataList(ArrayList<StockData> stockDataList,
	                          ArrayList<StockData> vertexList, ArrayList<StockData> dataList) {
		int size;
		int direction;

		StockData prev;
		StockData current;
		StockData stockData;

		if ((stockDataList == null) || (vertexList == null)
				|| (dataList == null)) {
			return;
		}

		if (stockDataList.size() < Trend.VERTEX_SIZE) {
			return;
		}

		if (vertexList.size() == 0) {
			return;
		}

		dataList.clear();

		extendVertexList(0, stockDataList, vertexList);
		extendVertexList(stockDataList.size(), stockDataList, vertexList);

		size = vertexList.size();
		for (int i = 1; i < size; i++) {
			prev = vertexList.get(i - 1);
			current = vertexList.get(i);

			if ((prev == null) || (current == null)) {
				return;
			}

			stockData = new StockData();
			stockData.set(current);
			stockData.merge(prev);

			direction = Trend.DIRECTION_NONE;

			if (prev.getTrend().vertexOf(Trend.VERTEX_TOP)) {
				if (current.getTrend().vertexOf(Trend.VERTEX_BOTTOM)) {
					direction = Trend.DIRECTION_DOWN;
				} else {
					direction = Trend.DIRECTION_UP;
				}
			} else if (prev.getTrend().vertexOf(Trend.VERTEX_BOTTOM)) {
				if (current.getTrend().vertexOf(Trend.VERTEX_TOP)) {
					direction = Trend.DIRECTION_UP;
				} else {
					direction = Trend.DIRECTION_DOWN;
				}
			}

			stockData.getTrend().setDirection(direction);
			dataList.add(stockData);
		}
	}

	void analyzeDirection(ArrayList<StockData> stockDataList) {
		int i;
		int direction;
		int directionBase = Trend.DIRECTION_NONE;
		int directionStroke = Trend.DIRECTION_NONE;
		int directionSegment = Trend.DIRECTION_NONE;

		StockData prev;
		StockData stockData;
		StockData strokeTop = null;
		StockData strokeBottom = null;
		StockData segmentTop = null;
		StockData segmentBottom = null;

		if (stockDataList == null) {
			return;
		}

		if (stockDataList.size() < Trend.VERTEX_SIZE) {
			return;
		}

		i = 0;
		prev = stockDataList.get(i);

		for (i = 1; i < stockDataList.size(); i++) {
			stockData = stockDataList.get(i);

			direction = Trend.DIRECTION_NONE;

			if (prev.getTrend().vertexOf(Trend.VERTEX_TOP)) {
				directionBase = Trend.DIRECTION_DOWN;
			} else if (prev.getTrend().vertexOf(Trend.VERTEX_BOTTOM)) {
				directionBase = Trend.DIRECTION_UP;
			}

			if (!(stockData.getTrend().vertexOf(Trend.VERTEX_TOP) || stockData
					.getTrend().vertexOf(Trend.VERTEX_BOTTOM))) {
				direction |= directionBase;
			}

			if (prev.getTrend().vertexOf(Trend.VERTEX_TOP_STROKE)) {
				strokeTop = new StockData(prev);
				directionStroke = Trend.DIRECTION_DOWN_STROKE;
			} else if (prev.getTrend().vertexOf(Trend.VERTEX_BOTTOM_STROKE)) {
				strokeBottom = new StockData(prev);
				directionStroke = Trend.DIRECTION_UP_STROKE;
			}

			if (strokeTop != null) {
				if (stockData.getCandlestick().getHigh() > strokeTop.getCandlestick().getHigh()) {
					directionStroke = Trend.DIRECTION_UP_STROKE;
				}
			}

			if (strokeBottom != null) {
				if (stockData.getCandlestick().getLow() < strokeBottom.getCandlestick().getLow()) {
					directionStroke = Trend.DIRECTION_DOWN_STROKE;
				}
			}

			if (!(stockData.getTrend().vertexOf(Trend.VERTEX_TOP_STROKE) || stockData
					.getTrend().vertexOf(Trend.VERTEX_BOTTOM_STROKE))) {
				direction |= directionStroke;
			}

			if (prev.getTrend().vertexOf(Trend.VERTEX_TOP_SEGMENT)) {
				segmentTop = new StockData(prev);
				directionSegment = Trend.DIRECTION_DOWN_SEGMENT;
			} else if (prev.getTrend().vertexOf(Trend.VERTEX_BOTTOM_SEGMENT)) {
				segmentBottom = new StockData(prev);
				directionSegment = Trend.DIRECTION_UP_SEGMENT;
			}

			if (segmentTop != null) {
				if (stockData.getCandlestick().getHigh() > segmentTop.getCandlestick().getHigh()) {
					directionSegment = Trend.DIRECTION_UP_SEGMENT;
				}
			}

			if (segmentBottom != null) {
				if (stockData.getCandlestick().getLow() < segmentBottom.getCandlestick().getLow()) {
					directionSegment = Trend.DIRECTION_DOWN_SEGMENT;
				}
			}

			if (!(stockData.getTrend().vertexOf(Trend.VERTEX_TOP_SEGMENT) || stockData
					.getTrend().vertexOf(Trend.VERTEX_BOTTOM_SEGMENT))) {
				direction |= directionSegment;
			}

			stockData.getTrend().setDirection(direction);

			if (stockData.getTrend().getVertex() != Trend.VERTEX_NONE) {
				prev = stockDataList.get(i);
			}
		}
	}

	void debugShow(ArrayList<StockData> stockDataList,
	               ArrayList<StockData> dataList) {
		int index = 0;
		StockData data = null;
		StockData stockData = null;

		if ((stockDataList == null) || (dataList == null)) {
			return;
		}

		if ((stockDataList.size() == 0) || (dataList.size() == 0)) {
			return;
		}

		if (dataList.size() > stockDataList.size()) {
			return;
		}

		for (int i = 0; i < dataList.size(); i++) {
			data = dataList.get(i);

			if (data == null) {
				return;
			}

			index = data.getIndex();
			if (index > stockDataList.size() - 1) {
				return;
			}

			stockData = stockDataList.get(index);

			if (stockData == null) {
				return;
			}

			if (data.getTrend().getDirection() == Trend.DIRECTION_UP) {
				stockData.getCandlestick().setOpen(data.getTrend().getVertexLow());
				stockData.getCandlestick().setClose(data.getTrend().getVertexHigh());
			} else if (data.getTrend().getDirection() == Trend.DIRECTION_DOWN) {
				stockData.getCandlestick().setOpen(data.getTrend().getVertexHigh());
				stockData.getCandlestick().setClose(data.getTrend().getVertexLow());
			}

			stockData.getCandlestick().setHigh(data.getTrend().getVertexHigh());
			stockData.getCandlestick().setLow(data.getTrend().getVertexLow());

			stockData.setAction(String.valueOf(i));
		}
	}

	void testShowVertextNumber(ArrayList<StockData> stockDataList,
	                           ArrayList<StockData> dataList) {
		int index = 0;
		StockData data = null;
		StockData stockData = null;

		if ((stockDataList == null) || (dataList == null)) {
			return;
		}

		if ((stockDataList.size() == 0) || (dataList.size() == 0)) {
			return;
		}

		if (dataList.size() > stockDataList.size()) {
			return;
		}

		for (int i = 0; i < dataList.size(); i++) {
			data = dataList.get(i);

			if (data == null) {
				return;
			}

			index = data.getIndex();
			if (index > stockDataList.size() - 1) {
				return;
			}

			stockData = stockDataList.get(index);

			if (stockData == null) {
				return;
			}

			stockData.setAction(String.valueOf(i));
		}
	}
}
