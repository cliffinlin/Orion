package com.android.orion;

import java.util.ArrayList;

import android.util.Log;

import com.android.orion.database.Stock;
import com.android.orion.database.StockData;

public class VertexAnalyzer {
	static final String TAG = Constants.TAG + " "
			+ VertexAnalyzer.class.getSimpleName();

	public VertexAnalyzer() {
	}

	private void setDirectionVertex(ArrayList<StockData> dataList, int index,
			StockData prev, StockData current, StockData next) {
		int direction = StockData.DIRECTION_NONE;
		int vertex = StockData.VERTEX_NONE;

		if ((current.getVertexHigh() > prev.getVertexHigh())
				&& (current.getVertexLow() > prev.getVertexLow())) {
			direction = StockData.DIRECTION_UP;

			if ((current.getVertexHigh() > next.getVertexHigh())
					&& (current.getVertexLow() > next.getVertexLow())) {
				vertex = StockData.VERTEX_TOP;
			}
		} else if ((current.getVertexHigh() < prev.getVertexHigh())
				&& (current.getVertexLow() < prev.getVertexLow())) {
			direction = StockData.DIRECTION_DOWN;

			if ((current.getVertexHigh() < next.getVertexHigh())
					&& (current.getVertexLow() < next.getVertexLow())) {
				vertex = StockData.VERTEX_BOTTOM;
			}
		} else {
			direction = StockData.DIRECTION_NONE;
			vertex = StockData.VERTEX_NONE;
		}

		dataList.get(index).setDirection(direction);
		dataList.get(index).setVertex(vertex);
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
		if (size < StockData.VERTEX_TYPING_SIZE) {
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

			if (i < size - 2) {
				if (current.include(next) || current.includedBy(next)) {
					setDirectionVertex(dataList, i, prev, current, next);
					direction = dataList.get(i).getDirection();
					vertex = dataList.get(i).getVertex();
					if ((vertex == StockData.VERTEX_TOP)
							|| (vertex == StockData.VERTEX_BOTTOM)) {
						vertexList.add(dataList.get(i));
					}

					current.merge(direction, next);
					next.merge(direction, current);

					dataList.get(i).set(current);
					dataList.get(i + 1).set(next);

					current.set(next);

					next.init();
					continue;
				}
			}

			setDirectionVertex(dataList, i, prev, current, next);
			direction = dataList.get(i).getDirection();
			vertex = dataList.get(i).getVertex();
			if ((vertex == StockData.VERTEX_TOP)
					|| (vertex == StockData.VERTEX_BOTTOM)) {
				vertexList.add(dataList.get(i));
			}

			prev.set(current);
			current.set(next);

			next.init();
		}

		i = size - 1;
		if (vertex == StockData.VERTEX_TOP) {
			direction = StockData.DIRECTION_DOWN;
		} else if (vertex == StockData.VERTEX_BOTTOM) {
			direction = StockData.DIRECTION_UP;
		}

		dataList.get(i).setDirection(direction);
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
		if (size < StockData.VERTEX_TYPING_SIZE) {
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
					if (direction == StockData.DIRECTION_UP) {
						if (dataList.get(i).getVertexLow() < dataList
								.get(i - 1).getVertexLow()) {
							vertex = vertexTypeTop;
							stockData.setVertex(stockData.getVertex() | vertex);
							vertexList.add(stockData);
						}
					} else if (direction == StockData.DIRECTION_DOWN) {
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
				if (direction == StockData.DIRECTION_UP) {
					vertex = vertexTypeTop;
				} else if (direction == StockData.DIRECTION_DOWN) {
					vertex = vertexTypeBottom;
				} else {
					Log.d(TAG, "analyzeLine: directionType = " + direction);
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

		stockData = new StockData();

		if (index == 0) {
			i = 0;
			j = 0;
		} else {
			i = stockDataList.size() - 1;
			j = vertexList.size() - 1;
		}

		if (vertexList.get(j).vertexOf(StockData.VERTEX_TOP)) {
			stockData.set(stockDataList.get(i));
			stockData.setVertex(StockData.VERTEX_BOTTOM);
		} else if (vertexList.get(j).vertexOf(StockData.VERTEX_BOTTOM)) {
			stockData.set(stockDataList.get(i));
			stockData.setVertex(StockData.VERTEX_TOP);
		}

		if (index == 0) {
			vertexList.add(0, stockData);
		} else {
			vertexList.add(stockData);
		}
	}

	void sigmaHistogram(StockData stockData, ArrayList<StockData> stockDataList) {
		double histogram = 0;
		double sigmaHistogram = 0;

		StockData current = null;

		if ((stockData == null) || (stockDataList == null)) {
			return;
		}

		for (int i = stockData.getIndexStart() + 1; i <= stockData
				.getIndexEnd(); i++) {
			current = stockDataList.get(i);
			if (current == null) {
				return;
			}

			histogram = current.getHistogram();

			if (stockData.getDirection() == StockData.DIRECTION_UP) {
				if (histogram > 0) {
					sigmaHistogram += histogram;
				}
			} else if (stockData.getDirection() == StockData.DIRECTION_DOWN) {
				if (histogram < 0) {
					sigmaHistogram += histogram;
				}
			}

			if (i == stockData.getIndexEnd()) {
				stockData.setDIF(current.getDIF());
				stockData.setDEA(current.getDEA());
				stockData.setHistogram(current.getHistogram());
				stockData.setSigmaHistogram(sigmaHistogram);
			}
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

		if (stockDataList.size() < StockData.VERTEX_TYPING_SIZE) {
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

			stockData.init();
			stockData.set(current);
			stockData.merge(prev, current);

			if (prev.vertexOf(StockData.VERTEX_TOP)
					&& current.vertexOf(StockData.VERTEX_BOTTOM)) {
				direction = StockData.DIRECTION_DOWN;
			} else if (prev.vertexOf(StockData.VERTEX_BOTTOM)
					&& current.vertexOf(StockData.VERTEX_TOP)) {
				direction = StockData.DIRECTION_UP;
			} else {
				direction = StockData.DIRECTION_NONE;
			}

			stockData.setDirection(direction);
			stockData.setupAmplitude();

			sigmaHistogram(stockData, stockDataList);

			stockData.setLevel(level);

			dataList.add(stockData);
		}
	}

	void analyzeOverlap(ArrayList<StockData> stockDataList,
			ArrayList<StockData> lineDataList,
			ArrayList<StockData> overlapList) {
		int size = 0;
		double Zg = 0;
		double Zd = 0;

		StockData prev = null;
		StockData current = null;
		StockData next = null;

		StockData stockData = null;
		StockData lineData = null;
		StockData overlap = null;

		if ((stockDataList == null) || (lineDataList == null)
				|| (overlapList == null)) {
			return;
		}

		overlapList.clear();

		size = lineDataList.size();

		if (size < StockData.VERTEX_TYPING_SIZE) {
			return;
		}

		for (int i = 2; i < size - 1; i++) {
			prev = lineDataList.get(i - 1);
			current = lineDataList.get(i);
			next = lineDataList.get(i + 1);

			if ((prev == null) || (current == null) || (next == null)) {
				continue;
			}

			if ((overlap == null)
					|| (current.positionTo(overlap) != StockData.POSITION_NONE)) {
				overlap = new StockData();

				overlap.set(current);
				overlap.setIndex(i - 2);
				overlap.setIndexStart(i - 2);

				Zg = Math
						.min(Math.min(prev.getVertexHigh(),
								current.getVertexHigh()), next.getVertexHigh());
				Zd = Math.max(
						Math.max(prev.getVertexLow(), current.getVertexLow()),
						next.getVertexLow());

				overlap.setOverlapHigh(Zg);
				overlap.setOverlapLow(Zd);
				overlapList.add(overlap);
			}

			overlap.setIndexEnd(i);
		}

		for (int i = 0; i < overlapList.size(); i++) {
			overlap = overlapList.get(i);
			for (int j = overlap.getIndexStart(); j <= overlap.getIndexEnd(); j++) {
				lineData = lineDataList.get(j);
				for (int k = lineData.getIndexStart(); k <= lineData
						.getIndexEnd(); k++) {
					stockData = stockDataList.get(k);
					stockData.setOverlapHigh(overlap.getOverlapHigh());
					stockData.setOverlapLow(overlap.getOverlapLow());
				}
			}
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

	void analyzeDivergence(Stock stock, ArrayList<StockData> stockDataList,
						   ArrayList<StockData> lineDataList) {
		StockData stockData = null;
		StockData current = null;
		StockData middle = null;
		StockData base = null;
		int divergence = StockData.DIVERGENCE_NONE;

		if ((stock == null) || (stockDataList == null) || (lineDataList == null)) {
			return;
		}

		if (stockDataList.size() < StockData.VERTEX_TYPING_SIZE) {
			return;
		}

		if (lineDataList.size() < StockData.VERTEX_TYPING_SIZE) {
			return;
		}

		stockData = stockDataList.get(stockDataList.size() - 1);

		current = lineDataList.get(lineDataList.size() - 1);
		middle = lineDataList.get(lineDataList.size() - 2);
		base = lineDataList.get(lineDataList.size() - 3);

		if ((stockData == null) || (current == null) || (middle == null)
				|| (base == null)) {
			return;
		}

		if (stock.getPrice() > base.getVertexHigh() || stock.getPrice() < base.getVertexLow()) {
			divergence = current.divergenceValue(current.getDirection(), base);
		}

		current.setDivergence(divergence);
	}

	void analyzeDirection(ArrayList<StockData> stockDataList) {
		int i = 0;
		int direction = StockData.DIRECTION_NONE;
		int directionBase = StockData.DIRECTION_NONE;
		int directionStroke = StockData.DIRECTION_NONE;
		int directionSegment = StockData.DIRECTION_NONE;

		StockData prev = null;
		StockData stockData = null;

		if (stockDataList.size() < StockData.VERTEX_TYPING_SIZE) {
			return;
		}

		i = 0;
		prev = stockDataList.get(i);

		for (i = 1; i < stockDataList.size(); i++) {
			stockData = stockDataList.get(i);

			direction = StockData.DIRECTION_NONE;

			if (prev.vertexOf(StockData.VERTEX_TOP)) {
				directionBase = StockData.DIRECTION_DOWN;
			} else if (prev.vertexOf(StockData.VERTEX_BOTTOM)) {
				directionBase = StockData.DIRECTION_UP;
			}

			if (!(stockData.vertexOf(StockData.VERTEX_TOP) || stockData
					.vertexOf(StockData.VERTEX_BOTTOM))) {
				direction |= directionBase;
			}

			if (prev.vertexOf(StockData.VERTEX_TOP_STROKE)) {
				directionStroke = StockData.DIRECTION_DOWN_STROKE;
			} else if (prev.vertexOf(StockData.VERTEX_BOTTOM_STROKE)) {
				directionStroke = StockData.DIRECTION_UP_STROKE;
			}

			if (!(stockData.vertexOf(StockData.VERTEX_TOP_STROKE) || stockData
					.vertexOf(StockData.VERTEX_BOTTOM_STROKE))) {
				direction |= directionStroke;
			}

			if (prev.vertexOf(StockData.VERTEX_TOP_SEGMENT)) {
				directionSegment = StockData.DIRECTION_DOWN_SEGMENT;
			} else if (prev.vertexOf(StockData.VERTEX_BOTTOM_SEGMENT)) {
				directionSegment = StockData.DIRECTION_UP_SEGMENT;
			}

			if (!(stockData.vertexOf(StockData.VERTEX_TOP_SEGMENT) || stockData
					.vertexOf(StockData.VERTEX_BOTTOM_SEGMENT))) {
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

			if (data.getDirection() == StockData.DIRECTION_UP) {
				stockData.setOpen(data.getVertexLow());
				stockData.setClose(data.getVertexHigh());
			} else if (data.getDirection() == StockData.DIRECTION_DOWN) {
				stockData.setOpen(data.getVertexHigh());
				stockData.setClose(data.getVertexLow());
			}

			stockData.setHigh(data.getVertexHigh());
			stockData.setLow(data.getVertexLow());

			actionString = String.valueOf(i);

			if (data.getAmplitude() > 0) {
				actionString += " +" + data.getAmplitude();
			} else if (data.getAmplitude() < 0) {
                actionString += " " + data.getAmplitude();
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
