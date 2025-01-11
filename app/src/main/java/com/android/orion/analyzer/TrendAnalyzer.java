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

	void analyzeVertex(ArrayList<StockData> stockDataList, ArrayList<StockData> vertexList) {
		if ((stockDataList == null) || (vertexList == null)) {
			return;
		}

		ArrayList<StockData> dataList = new ArrayList<>();
		for (StockData stockData : stockDataList) {
			dataList.add(new StockData(stockData));
		}

		int size = dataList.size();
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

				stockDataList.get(i).getTrend().setDirection(direction);
				stockDataList.get(i).getTrend().setVertex(vertex);
				stockDataList.get(i).getTrend().setVertexHigh(current.getTrend().getVertexHigh());
				stockDataList.get(i).getTrend().setVertexLow(current.getTrend().getVertexLow());

				if ((vertex == Trend.VERTEX_TOP)
						|| (vertex == Trend.VERTEX_BOTTOM)) {
					vertexList.add(dataList.get(i));

					dataList.get(i).getTrend().setDirection(Trend.DIRECTION_NONE);
					stockDataList.get(i).getTrend().setDirection(Trend.DIRECTION_NONE);
				}

				if (current.getTrend().include(next.getTrend()) || current.getTrend().includedBy(next.getTrend())) {
					current.getTrend().merge(direction, next.getTrend());
					next.getTrend().merge(direction, current.getTrend());

					dataList.get(i).set(current);
					dataList.get(i + 1).set(next);

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

		int trend = dataList.get(0).getTrend().getDirection();
		int baseTrend = trend;
		boolean needUpdate = vertexTypeTop == Trend.VERTEX_TOP_STROKE || vertexTypeTop == Trend.VERTEX_BOTTOM_STROKE;
		String action = "";

		try {
			for (int i = 2; i < size; i++) {
				StockData current = dataList.get(i);
				StockData prev = dataList.get(i - 1);
				StockData prevPrev = dataList.get(i - 2);

				if (current == null || prev == null || prevPrev == null) {
					continue;
				}

				Trend currentTrend = current.getTrend();
				Trend prevTrend = prev.getTrend();
				Trend prevPrevTrend = prevPrev.getTrend();

				if (currentTrend == null || prevTrend == null || prevPrevTrend == null) {
					continue;
				}

				StockData start_1 = stockDataList.get(prevTrend.getIndexStart());
				StockData end_1 = stockDataList.get(prevTrend.getIndexEnd());
				StockData start_2 = stockDataList.get(prevPrevTrend.getIndexStart());
				StockData end_2 = stockDataList.get(prevPrevTrend.getIndexEnd());

				if (start_1 == null || end_1 == null || start_2 == null || end_2 == null) {
					continue;
				}

				int directionTo = currentTrend.directionTo(prevPrevTrend);
				switch (directionTo) {
					case Trend.DIRECTION_UP:
						if (trend == Trend.DIRECTION_DOWN) {
							addVertex(end_2, vertexTypeBottom, vertexList);
							action = updateAction(needUpdate, end_2, Trend.TREND_TYPE_DOWN_UP);
						} else if (trend == Trend.DIRECTION_NONE) {
							StockData vertexData = chooseVertex(start_2, end_2, Trend.VERTEX_BOTTOM);
							if (baseTrend == Trend.DIRECTION_UP) {
								addVertex(vertexData, vertexTypeBottom, vertexList);
								action = updateAction(needUpdate, vertexData, Trend.TREND_TYPE_UP_NONE_UP);
							} else if (baseTrend == Trend.DIRECTION_DOWN) {
								action = updateAction(needUpdate, vertexData, Trend.TREND_TYPE_DOWN_NONE_UP);
							}
						} else if (trend == Trend.DIRECTION_UP) {
							action = updateAction(needUpdate, null, Trend.TREND_TYPE_UP_UP);
						}
						break;
					case Trend.DIRECTION_DOWN:
						if (trend == Trend.DIRECTION_UP) {
							addVertex(end_2, vertexTypeTop, vertexList);
							action = updateAction(needUpdate, end_2, Trend.TREND_TYPE_UP_DOWN);
						} else if (trend == Trend.DIRECTION_NONE) {
							StockData vertexData = chooseVertex(start_2, end_2, Trend.VERTEX_TOP);
							if (baseTrend == Trend.DIRECTION_UP) {
								action = updateAction(needUpdate, vertexData, Trend.TREND_TYPE_UP_NONE_DOWN);
							} else if (baseTrend == Trend.DIRECTION_DOWN) {
								addVertex(vertexData, vertexTypeTop, vertexList);
								action = updateAction(needUpdate, vertexData, Trend.TREND_TYPE_DOWN_NONE_DOWN);
							}
						} else if (trend == Trend.DIRECTION_DOWN) {
							action = updateAction(needUpdate, null, Trend.TREND_TYPE_DOWN_DOWN);
						}
						break;
					case Trend.DIRECTION_NONE:
						if (trend == Trend.DIRECTION_UP) {
							baseTrend = Trend.DIRECTION_UP;
							StockData vertexData = chooseVertex(start_1, end_1, Trend.VERTEX_TOP);
							addVertex(vertexData, vertexTypeTop, vertexList);
							action = updateAction(needUpdate, vertexData, Trend.TREND_TYPE_UP_NONE);
						} else if (trend == Trend.DIRECTION_DOWN) {
							baseTrend = Trend.DIRECTION_DOWN;
							StockData vertexData = chooseVertex(start_1, end_1, Trend.VERTEX_BOTTOM);
							addVertex(vertexData, vertexTypeBottom, vertexList);
							action = updateAction(needUpdate, vertexData, Trend.TREND_TYPE_DOWN_NONE);
						} else if (trend == Trend.DIRECTION_NONE) {
						}
						break;
				}
				trend = directionTo;
			}
			extendVertexList(dataList, vertexList);
			if (needUpdate) {
				StockData stockData = StockData.getLast(stockDataList, 0);
				if (stockData != null) {
					stockData.setAction(action);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String updateAction(boolean needUpdate, StockData stockData, String action) {
		if (!needUpdate || stockData == null) {
			return "";
		}
		stockData.setAction(action);
		return action;
	}

	private StockData chooseVertex(StockData start, StockData end, int vertexType) {
		if (start == null || end == null) {
			return null;
		}
		return start.getTrend().vertexOf(vertexType) ? start : end;
	}

	private void addVertex(StockData stockData, int vertexType, ArrayList<StockData> vertexList) {
		if (stockData == null || vertexList == null) {
			return;
		}
		if (stockData.getTrend() == null) {
			return;
		}
		stockData.getTrend().addVertex(vertexType);
		vertexList.add(stockData);
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

	void logStockDataList(ArrayList<StockData> stockDataList) {
		if (stockDataList == null) {
			return;
		}
		for (int i = 0; i < stockDataList.size(); i++) {
			StockData stockData = stockDataList.get(i);
			Log.d(i + "-->" + stockData.getTrend().getVertex() + " " + stockData.getTrend().getDirection() + " " + stockData.getDateTime());
		}
	}
}
