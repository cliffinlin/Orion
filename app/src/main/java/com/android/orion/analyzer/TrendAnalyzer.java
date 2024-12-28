package com.android.orion.analyzer;

import com.android.orion.data.Trend;
import com.android.orion.database.StockData;
import com.android.orion.utility.Logger;

import java.util.ArrayList;

public class TrendAnalyzer {
	Logger Log = Logger.getLogger();

	private TrendAnalyzer() {
	}

	private static class Holder {
		private static final TrendAnalyzer INSTANCE = new TrendAnalyzer();
	}

	public static TrendAnalyzer getInstance() {
		return Holder.INSTANCE;
	}

	void analyzeVertex(ArrayList<StockData> stockDataList,
	                   ArrayList<StockData> vertexList) {
		if ((stockDataList == null) || (vertexList == null)) {
			return;
		}

		int size = stockDataList.size();
		if (size < Trend.VERTEX_SIZE) {
			return;
		}

		vertexList.clear();
		StockData prev = new StockData();
		StockData current = new StockData();
		StockData next = new StockData();

		int direction = Trend.DIRECTION_NONE;
		int vertex;
		try {
			for (int i = 1; i < size - 1; i++) {
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
					current.getTrend().merge(direction, next.getTrend());
					next.getTrend().merge(direction, current.getTrend());

					stockDataList.get(i).set(current);
					stockDataList.get(i + 1).set(next);

					current.set(next);
					next.init();
					continue;
				}

				prev.set(current);
				current.set(next);
				next.init();
			}
			extendVertexList(stockDataList, vertexList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void extendVertexList(ArrayList<StockData> dataList, ArrayList<StockData> vertexList) {
		if (dataList == null || dataList.size() < Trend.VERTEX_SIZE || vertexList == null || vertexList.isEmpty()) {
			return;
		}

		addVertex(dataList, vertexList, 0, true);
		addVertex(dataList, vertexList, dataList.size() - 1, false);
	}

	private void addVertex(ArrayList<StockData> dataList, ArrayList<StockData> vertexList, int index, boolean isStart) {
		if (dataList == null || dataList.isEmpty() || vertexList == null || vertexList.isEmpty() || index < 0 || index >= dataList.size()) {
			return;
		}

		try {
			StockData stockData = dataList.get(index);
			StockData vertex = new StockData(stockData);

			Trend trend = isStart ? vertexList.get(0).getTrend() : vertexList.get(vertexList.size() - 1).getTrend();
			int direction = Trend.DIRECTION_NONE;

			if (trend.vertexOf(Trend.VERTEX_TOP)) {
				vertex.getTrend().setVertex(Trend.VERTEX_BOTTOM);
				direction = isStart ? Trend.DIRECTION_UP : Trend.DIRECTION_DOWN;
			} else if (trend.vertexOf(Trend.VERTEX_BOTTOM)) {
				vertex.getTrend().setVertex(Trend.VERTEX_TOP);
				direction = isStart ? Trend.DIRECTION_DOWN : Trend.DIRECTION_UP;
			}

			int start = isStart ? 0 : trend.getIndexEnd() + 1;
			int end = isStart ? trend.getIndexStart() : dataList.size();

			for (int i = start; i < end; i++) {
				if (i >= dataList.size()) {
					return;
				}
				dataList.get(i).getTrend().setDirection(direction);
				vertex.getTrend().merge(direction, dataList.get(i).getTrend());
			}

			if (isStart) {
				vertexList.add(0, vertex);
			} else {
				vertexList.add(vertex);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void analyzeLine(ArrayList<StockData> stockDataList,
	                 ArrayList<StockData> dataList, ArrayList<StockData> vertexList,
	                 int vertexTypeTop, int vertexTypeBottom) {
		if ((stockDataList == null) || (dataList == null) || (vertexList == null)) {
			return;
		}

		int size = dataList.size();
		if (size < Trend.VERTEX_SIZE) {
			return;
		}

		vertexList.clear();

		int i = 0;
		int direction = dataList.get(i).getTrend().getDirection();

		int vertex = Trend.VERTEX_NONE;
		try {
			for (i = 2; i < size; i++) {
				if (dataList.get(i).getTrend().directionTo(dataList.get(i - 2).getTrend()) == direction) {
					i++;
				} else {
					StockData stockData = stockDataList
							.get(dataList.get(i - 2).getTrend().getIndexEnd());
					if (direction == Trend.DIRECTION_UP) {
						vertex = vertexTypeTop;
					} else if (direction == Trend.DIRECTION_DOWN) {
						vertex = vertexTypeBottom;
					} else {
						Log.d("Unexpected directionType = " + direction);
					}
					stockData.getTrend().addVertex(vertex);
					vertexList.add(stockData);
					direction = dataList.get(i - 1).getTrend().getDirection();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		extendVertexList(dataList, vertexList);
	}

	void vertexListToDataList(ArrayList<StockData> stockDataList, ArrayList<StockData> vertexList, ArrayList<StockData> dataList) {
		if (stockDataList == null || vertexList == null || dataList == null) {
			return;
		}

		if (stockDataList.size() < Trend.VERTEX_SIZE || vertexList.isEmpty()) {
			return;
		}

		dataList.clear();

		int size = vertexList.size();
		for (int i = 1; i < size; i++) {
			StockData prev = vertexList.get(i - 1);
			StockData current = vertexList.get(i);

			if ((prev == null) || (current == null)) {
				return;
			}

			StockData stockData = new StockData(current);
			stockData.getTrend().setIndexStart(prev.getTrend().getIndexStart());
			stockData.getTrend().setIndexEnd(current.getTrend().getIndexEnd());
			stockData.getTrend().merge(Trend.DIRECTION_NONE, prev.getTrend());

			int direction = Trend.DIRECTION_NONE;
			if (prev.getTrend().vertexOf(Trend.VERTEX_TOP)) {
				direction = current.getTrend().vertexOf(Trend.VERTEX_BOTTOM) ? Trend.DIRECTION_DOWN : Trend.DIRECTION_UP;
			} else if (prev.getTrend().vertexOf(Trend.VERTEX_BOTTOM)) {
				direction = current.getTrend().vertexOf(Trend.VERTEX_TOP) ? Trend.DIRECTION_UP : Trend.DIRECTION_DOWN;
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
