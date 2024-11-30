package com.android.orion.analyzer;

import com.android.orion.database.StockData;
import com.android.orion.utility.Logger;

import java.util.ArrayList;

public class StockVertexAnalyzer {

	private static StockVertexAnalyzer mInstance;
	Logger Log = Logger.getLogger();

	private StockVertexAnalyzer() {
	}

	public static synchronized StockVertexAnalyzer getInstance() {
		if (mInstance == null) {
			mInstance = new StockVertexAnalyzer();
		}
		return mInstance;
	}

	void analyzeVertex(ArrayList<StockData> dataList,
					   ArrayList<StockData> vertexList) {
		int i = 0;
		int size = 0;
		int direction = StockData.DIRECTION_NONE;
		int vertex = StockData.VERTEX_NONE;

		StockData prev = null;
		StockData current = null;
		StockData next = null;

		if ((dataList == null) || (vertexList == null)) {
			return;
		}

		size = dataList.size();
		if (size < StockData.VERTEX_SIZE) {
			return;
		}

		vertexList.clear();

		prev = new StockData();
		current = new StockData();
		next = new StockData();

		if ((prev == null) || (current == null) || (next == null)) {
			return;
		}

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

			if (current.include(prev) || current.includedBy(prev)) {
				prev.merge(direction, current);
				current.merge(direction, prev);

				dataList.get(i - 1).set(prev);
				dataList.get(i).set(current);

				prev.set(current);

				current.init();
				next.init();
				continue;
			}

			direction = current.directionTo(prev);
			vertex = current.vertexTo(prev, next);

			dataList.get(i).setDirection(direction);
			dataList.get(i).setVertex(vertex);

			if ((vertex == StockData.VERTEX_TOP_LEVEL_1)
					|| (vertex == StockData.VERTEX_BOTTOM_LEVEL_1)) {
				vertexList.add(dataList.get(i));
			}

			if (current.include(next) || current.includedBy(next)) {
				if (i < size - 2) {
					current.merge(direction, next);
					next.merge(direction, current);

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
		if (vertex == StockData.VERTEX_TOP_LEVEL_1) {
			direction = StockData.DIRECTION_DOWN_LEVEL_1;
		} else if (vertex == StockData.VERTEX_BOTTOM_LEVEL_1) {
			direction = StockData.DIRECTION_UP_LEVEL_1;
		}

		dataList.get(i).setDirection(direction);

		if (vertexList.size() > 0) {
			i = 0;
			if (vertexList.get(0).vertexOf(StockData.VERTEX_TOP_LEVEL_1)) {
				dataList.get(i).setVertex(StockData.VERTEX_BOTTOM_LEVEL_1);
			} else if (vertexList.get(0).vertexOf(StockData.VERTEX_BOTTOM_LEVEL_1)) {
				dataList.get(i).setVertex(StockData.VERTEX_TOP_LEVEL_1);
			}
		}
	}

	void analyzeLine(ArrayList<StockData> stockDataList,
					 ArrayList<StockData> dataList, ArrayList<StockData> vertexList,
					 int vertexTypeTop, int vertexTypeBottom) {
		int i = 0;
		int size = 0;
		int direction = StockData.DIRECTION_NONE;
		int vertex = StockData.VERTEX_NONE;

		StockData stockData = null;

		if ((stockDataList == null) || (dataList == null)
				|| (vertexList == null)) {
			return;
		}

		size = dataList.size();
		if (size < StockData.VERTEX_SIZE) {
			return;
		}

		vertexList.clear();

		i = 0;
		direction = dataList.get(i).getDirection();

		for (i = 2; i < size; i++) {
			if (dataList.get(i).directionTo(dataList.get(i - 2)) == direction) {
				i++;

				if (i == size - 1) {
					stockData = stockDataList.get(dataList.get(i - 1)
							.getIndexEnd());
					if (direction == StockData.DIRECTION_UP_LEVEL_1) {
						if (dataList.get(i).getVertexLow() < dataList
								.get(i - 1).getVertexLow()) {
							vertex = vertexTypeTop;
							stockData.setVertex(stockData.getVertex() | vertex);
							vertexList.add(stockData);
						}
					} else if (direction == StockData.DIRECTION_DOWN_LEVEL_1) {
						if (dataList.get(i).getVertexHigh() > dataList.get(
								i - 1).getVertexHigh()) {
							vertex = vertexTypeBottom;
							stockData.setVertex(stockData.getVertex() | vertex);
							vertexList.add(stockData);
						}
					}
				}
			} else {
				stockData = stockDataList
						.get(dataList.get(i - 2).getIndexEnd());
				if (direction == StockData.DIRECTION_UP_LEVEL_1) {
					vertex = vertexTypeTop;
				} else if (direction == StockData.DIRECTION_DOWN_LEVEL_1) {
					vertex = vertexTypeBottom;
				} else {
					Log.d("directionType = " + direction);
				}
				stockData.setVertex(stockData.getVertex() | vertex);
				vertexList.add(stockData);
				direction = dataList.get(i - 1).getDirection();
			}
		}
	}

	void extendVertexList(int index, ArrayList<StockData> stockDataList,
						  ArrayList<StockData> vertexList) {
		int i = 0;
		int j = 0;
		StockData stockData = null;

		if (index == 0) {
			i = 0;
			j = 0;
		} else {
			i = stockDataList.size() - 1;
			j = vertexList.size() - 1;
		}

		stockData = new StockData();
		stockData.set(stockDataList.get(i));

		if (vertexList.get(j).vertexOf(StockData.VERTEX_TOP_LEVEL_1)) {
			if (stockDataList.get(i).getVertexHigh() > vertexList.get(j).getVertexHigh()) {
				stockData.setVertex(StockData.VERTEX_TOP_LEVEL_1);
			} else {
				stockData.setVertex(StockData.VERTEX_BOTTOM_LEVEL_1);
			}
		} else if (vertexList.get(j).vertexOf(StockData.VERTEX_BOTTOM_LEVEL_1)) {
			if (stockDataList.get(i).getVertexLow() < vertexList.get(j).getVertexLow()) {
				stockData.setVertex(StockData.VERTEX_BOTTOM_LEVEL_1);
			} else {
				stockData.setVertex(StockData.VERTEX_TOP_LEVEL_1);
			}
		}

		if (index == 0) {
			vertexList.add(0, stockData);
		} else {
			vertexList.add(stockData);
		}
	}

	void vertexListToDataList(ArrayList<StockData> stockDataList,
							  ArrayList<StockData> vertexList, ArrayList<StockData> dataList, int level) {
		int size = 0;
		int direction = StockData.DIRECTION_NONE;

		StockData prev = null;
		StockData current = null;
		StockData stockData = null;

		if ((vertexList == null) || (stockDataList == null)
				|| (dataList == null)) {
			return;
		}

		if (stockDataList.size() < StockData.VERTEX_SIZE) {
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

			direction = StockData.DIRECTION_NONE;

			if (prev.vertexOf(StockData.VERTEX_TOP_LEVEL_1)) {
				if (current.vertexOf(StockData.VERTEX_BOTTOM_LEVEL_1)) {
					direction = StockData.DIRECTION_DOWN_LEVEL_1;
				} else {
					direction = StockData.DIRECTION_UP_LEVEL_1;
				}
			} else if (prev.vertexOf(StockData.VERTEX_BOTTOM_LEVEL_1)) {
				if (current.vertexOf(StockData.VERTEX_TOP_LEVEL_1)) {
					direction = StockData.DIRECTION_UP_LEVEL_1;
				} else {
					direction = StockData.DIRECTION_DOWN_LEVEL_1;
				}
			}

			stockData.setDirection(direction);
			stockData.setupChange();
			stockData.setupNet();
			stockData.setLevel(level);

			dataList.add(stockData);
		}
	}

	boolean checkStockDateVertex(ArrayList<StockData> stockDataList,
								 StockData stockData, int bottomVertexType, int topVertexType) {
		StockData start = null;
		StockData end = null;
		boolean result = false;

		if (stockDataList == null) {
			return result;
		}

		if (stockData == null) {
			return result;
		}

		start = stockDataList.get(stockData.getIndexStart());
		if (start == null) {
			return result;
		}
		if ((start.getVertex() != bottomVertexType)
				|| (start.getVertex() != topVertexType)) {
			return result;
		}
		end = stockDataList.get(stockData.getIndexEnd());
		if (end == null) {
			return result;
		}
		if ((end.getVertex() != bottomVertexType)
				|| (end.getVertex() != topVertexType)) {
			return result;
		}

		result = true;

		return result;
	}

	void analyzeDirection(ArrayList<StockData> stockDataList) {
		int i = 0;
		int direction = StockData.DIRECTION_NONE;
		int directionBase = StockData.DIRECTION_NONE;
		int directionStroke = StockData.DIRECTION_NONE;
		int directionSegment = StockData.DIRECTION_NONE;

		StockData prev = null;
		StockData stockData = null;
		StockData strokeTop = null;
		StockData strokeBottom = null;
		StockData segmentTop = null;
		StockData segmentBottom = null;

		if (stockDataList == null) {
			return;
		}

		if (stockDataList.size() < StockData.VERTEX_SIZE) {
			return;
		}

		i = 0;
		prev = stockDataList.get(i);

		for (i = 1; i < stockDataList.size(); i++) {
			stockData = stockDataList.get(i);

			direction = StockData.DIRECTION_NONE;

			if (prev.vertexOf(StockData.VERTEX_TOP_LEVEL_1)) {
				directionBase = StockData.DIRECTION_DOWN_LEVEL_1;
			} else if (prev.vertexOf(StockData.VERTEX_BOTTOM_LEVEL_1)) {
				directionBase = StockData.DIRECTION_UP_LEVEL_1;
			}

			if (!(stockData.vertexOf(StockData.VERTEX_TOP_LEVEL_1) || stockData
					.vertexOf(StockData.VERTEX_BOTTOM_LEVEL_1))) {
				direction |= directionBase;
			}

			if (prev.vertexOf(StockData.VERTEX_TOP_LEVEL_2)) {
				strokeTop = new StockData(prev);
				directionStroke = StockData.DIRECTION_DOWN_LEVEL_2;
			} else if (prev.vertexOf(StockData.VERTEX_BOTTOM_LEVEL_2)) {
				strokeBottom = new StockData(prev);
				directionStroke = StockData.DIRECTION_UP_LEVEL_2;
			}

			if (strokeTop != null) {
				if (stockData.getCandlestickChart().getHigh() > strokeTop.getCandlestickChart().getHigh()) {
					directionStroke = StockData.DIRECTION_UP_LEVEL_2;
				}
			}

			if (strokeBottom != null) {
				if (stockData.getCandlestickChart().getLow() < strokeBottom.getCandlestickChart().getLow()) {
					directionStroke = StockData.DIRECTION_DOWN_LEVEL_2;
				}
			}

			if (!(stockData.vertexOf(StockData.VERTEX_TOP_LEVEL_2) || stockData
					.vertexOf(StockData.VERTEX_BOTTOM_LEVEL_2))) {
				direction |= directionStroke;
			}

			if (prev.vertexOf(StockData.VERTEX_TOP_LEVEL_3)) {
				segmentTop = new StockData(prev);
				directionSegment = StockData.DIRECTION_DOWN_LEVEL_3;
			} else if (prev.vertexOf(StockData.VERTEX_BOTTOM_LEVEL_3)) {
				segmentBottom = new StockData(prev);
				directionSegment = StockData.DIRECTION_UP_LEVEL_3;
			}

			if (segmentTop != null) {
				if (stockData.getCandlestickChart().getHigh() > segmentTop.getCandlestickChart().getHigh()) {
					directionSegment = StockData.DIRECTION_UP_LEVEL_3;
				}
			}

			if (segmentBottom != null) {
				if (stockData.getCandlestickChart().getLow() < segmentBottom.getCandlestickChart().getLow()) {
					directionSegment = StockData.DIRECTION_DOWN_LEVEL_3;
				}
			}

			if (!(stockData.vertexOf(StockData.VERTEX_TOP_LEVEL_3) || stockData
					.vertexOf(StockData.VERTEX_BOTTOM_LEVEL_3))) {
				direction |= directionSegment;
			}

			stockData.setDirection(direction);

			if (stockData.getVertex() != StockData.VERTEX_NONE) {
				prev = stockDataList.get(i);
			}
		}
	}

	void debugShow(ArrayList<StockData> stockDataList,
				   ArrayList<StockData> dataList) {
		int index = 0;
		StockData data = null;
		StockData stockData = null;
		String actionString = "";

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

			if (data.getDirection() == StockData.DIRECTION_UP_LEVEL_1) {
				stockData.getCandlestickChart().setOpen(data.getVertexLow());
				stockData.getCandlestickChart().setClose(data.getVertexHigh());
			} else if (data.getDirection() == StockData.DIRECTION_DOWN_LEVEL_1) {
				stockData.getCandlestickChart().setOpen(data.getVertexHigh());
				stockData.getCandlestickChart().setClose(data.getVertexLow());
			}

			stockData.getCandlestickChart().setHigh(data.getVertexHigh());
			stockData.getCandlestickChart().setLow(data.getVertexLow());

			actionString = String.valueOf(i);

			if (data.getNet() > 0) {
				actionString += " +" + data.getNet();
			} else if (data.getNet() < 0) {
				actionString += " " + data.getNet();
			}

			stockData.setAction(actionString);
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
