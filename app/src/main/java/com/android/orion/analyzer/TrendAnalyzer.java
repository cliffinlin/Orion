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

	void analyzeVertex(ArrayList<StockData> stockDataList,
	                   ArrayList<StockData> vertexList) {
		int i;
		int size;
		int direction = Trend.DIRECTION_NONE;
		int vertex = Trend.VERTEX_NONE;

		StockData prev;
		StockData current;
		StockData next;

		if ((stockDataList == null) || (vertexList == null)) {
			return;
		}

		size = stockDataList.size();
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
				prev.set(stockDataList.get(i - 1));
			}

			if (current.isEmpty()) {
				current.set(stockDataList.get(i));
			}

			if (next.isEmpty()) {
				next.set(stockDataList.get(i + 1));
			}

			if (current.getTrend().include(prev.getTrend()) || current.getTrend().includedBy(prev.getTrend())) {
				prev.getTrend().merge(direction, current.getTrend());
				current.getTrend().merge(direction, prev.getTrend());

				stockDataList.get(i - 1).set(prev);
				stockDataList.get(i).set(current);

				prev.set(current);
				current.init();
				next.init();
				continue;
			}

			direction = current.getTrend().directionTo(prev.getTrend());
			vertex = current.getTrend().vertexTo(prev.getTrend(), next.getTrend());

			stockDataList.get(i).getTrend().setDirection(direction);
			stockDataList.get(i).getTrend().setVertex(vertex);

			if ((vertex == Trend.VERTEX_TOP)
					|| (vertex == Trend.VERTEX_BOTTOM)) {
				vertexList.add(stockDataList.get(i));
			}

			if (current.getTrend().include(next.getTrend()) || current.getTrend().includedBy(next.getTrend())) {
				if (i < size - 2) {//TODO
				current.getTrend().merge(direction, next.getTrend());
				next.getTrend().merge(direction, current.getTrend());

				stockDataList.get(i).set(current);
				stockDataList.get(i + 1).set(next);

				current.set(next);
				next.init();
				continue;
				}
			}

			prev.set(current);
			current.set(next);
			next.init();
		}

		extendVertexList(stockDataList, vertexList);
		setupStockDataTrend(stockDataList, vertexList);
	}

	void setupStockDataTrend(ArrayList<StockData> stockDataList, ArrayList<StockData> vertexList) {
		StockData stockData;
		StockData vertex;

		if (stockDataList == null || stockDataList.size() < Trend.VERTEX_SIZE) {
			return;
		}

		stockData = stockDataList.get(0);
		vertex = vertexList.get(0);
		stockData.getTrend().setVertex(vertex.getTrend().getVertex());
		if (vertex.getTrend().vertexOf(Trend.VERTEX_TOP)) {
			stockData.getTrend().setDirection(Trend.DIRECTION_DOWN);
		} else if (vertex.getTrend().vertexOf(Trend.VERTEX_BOTTOM)) {
			stockData.getTrend().setDirection(Trend.DIRECTION_UP);
		}

		stockData = stockDataList.get(stockDataList.size() - 1);
		vertex = vertexList.get(vertexList.size() - 1);
		stockData.getTrend().setVertex(vertex.getTrend().getVertex());
		if (vertex.getTrend().vertexOf(Trend.VERTEX_TOP)) {
			stockData.getTrend().setDirection(Trend.DIRECTION_UP);
		} else if (vertex.getTrend().vertexOf(Trend.VERTEX_BOTTOM)) {
			stockData.getTrend().setDirection(Trend.DIRECTION_DOWN);
		}
	}

	void extendVertexList(ArrayList<StockData> dataList, ArrayList<StockData> vertexList) {
		StockData stockData;
		StockData vertex;
		Trend trend;
		if (dataList == null || dataList.size() < Trend.VERTEX_SIZE) {
			return;
		}

		if (vertexList == null || vertexList.size() == 0) {
			return;
		}

		stockData = dataList.get(0);
		vertex = new StockData();
		vertex.set(stockData);
		trend = vertexList.get(0).getTrend();
		for (int i = 0; i < trend.getIndexStart(); i++) {
			if (i > dataList.size() - 1) {
				return;
			}
			vertex.getTrend().merge(Trend.DIRECTION_NONE, dataList.get(i).getTrend());
		}
		if (trend.vertexOf(Trend.VERTEX_TOP)) {
			vertex.getTrend().setVertex(Trend.VERTEX_BOTTOM);
		} else if (trend.vertexOf(Trend.VERTEX_BOTTOM)) {
			vertex.getTrend().setVertex(Trend.VERTEX_TOP);
		}
		vertexList.add(0, vertex);

		stockData = dataList.get(dataList.size() - 1);
		vertex = new StockData();
		vertex.set(stockData);
		trend = vertexList.get(vertexList.size() - 1).getTrend();
		for (int i = trend.getIndexEnd() + 1; i < dataList.size(); i++) {
			if (i > dataList.size() - 1) {
				return;
			}
			vertex.getTrend().merge(Trend.DIRECTION_NONE, dataList.get(i).getTrend());
		}
		if (trend.vertexOf(Trend.VERTEX_TOP)) {
			vertex.getTrend().setVertex(Trend.VERTEX_BOTTOM);
		} else if (trend.vertexOf(Trend.VERTEX_BOTTOM)) {
			vertex.getTrend().setVertex(Trend.VERTEX_TOP);
		}
		vertexList.add(vertex);
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

		extendVertexList(dataList, vertexList);
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

		size = vertexList.size();
		for (int i = 1; i < size; i++) {
			prev = vertexList.get(i - 1);
			current = vertexList.get(i);

			if ((prev == null) || (current == null)) {
				return;
			}

			stockData = new StockData();
			stockData.set(current);

			stockData.getTrend().setIndexStart(prev.getTrend().getIndexStart());
			stockData.getTrend().setIndexEnd(current.getTrend().getIndexEnd());

			stockData.getTrend().merge(Trend.DIRECTION_NONE, prev.getTrend());

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
