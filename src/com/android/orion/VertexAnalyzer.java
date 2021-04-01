package com.android.orion;

import java.util.ArrayList;

import android.util.Log;

import com.android.orion.database.StockData;
import com.android.orion.utility.Utility;

public class VertexAnalyzer {
	static final String TAG = Constants.TAG + " "
			+ VertexAnalyzer.class.getSimpleName();

	public VertexAnalyzer() {
	}

	private void setDirectionVertex(ArrayList<StockData> dataList, int index,
			StockData prev, StockData current, StockData next) {
		int direction = Constants.STOCK_DIRECTION_NONE;
		int vertex = Constants.STOCK_VERTEX_NONE;

		if ((current.getVertexHigh() > prev.getVertexHigh())
				&& (current.getVertexLow() > prev.getVertexLow())) {
			direction = Constants.STOCK_DIRECTION_UP;

			if ((current.getVertexHigh() > next.getVertexHigh())
					&& (current.getVertexLow() > next.getVertexLow())) {
				vertex = Constants.STOCK_VERTEX_TOP;
			}
		} else if ((current.getVertexHigh() < prev.getVertexHigh())
				&& (current.getVertexLow() < prev.getVertexLow())) {
			direction = Constants.STOCK_DIRECTION_DOWN;

			if ((current.getVertexHigh() < next.getVertexHigh())
					&& (current.getVertexLow() < next.getVertexLow())) {
				vertex = Constants.STOCK_VERTEX_BOTTOM;
			}
		} else {
			direction = Constants.STOCK_DIRECTION_NONE;
			vertex = Constants.STOCK_VERTEX_NONE;
		}

		dataList.get(index).setDirection(direction);
		dataList.get(index).setVertex(vertex);
	}

	void analyzeVertex(ArrayList<StockData> dataList,
			ArrayList<StockData> vertexList) {
		int i = 0;
		int size = 0;
		int direction = Constants.STOCK_DIRECTION_NONE;
		int vertex = Constants.STOCK_VERTEX_NONE;

		StockData prev = null;
		StockData current = null;
		StockData next = null;

		if ((dataList == null) || (vertexList == null)) {
			return;
		}

		size = dataList.size();
		if (size < Constants.STOCK_VERTEX_TYPING_SIZE) {
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
					if ((vertex == Constants.STOCK_VERTEX_TOP)
							|| (vertex == Constants.STOCK_VERTEX_BOTTOM)) {
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
			if ((vertex == Constants.STOCK_VERTEX_TOP)
					|| (vertex == Constants.STOCK_VERTEX_BOTTOM)) {
				vertexList.add(dataList.get(i));
			}

			prev.set(current);
			current.set(next);

			next.init();
		}

		i = size - 1;
		if (vertex == Constants.STOCK_VERTEX_TOP) {
			direction = Constants.STOCK_DIRECTION_DOWN;
		} else if (vertex == Constants.STOCK_VERTEX_BOTTOM) {
			direction = Constants.STOCK_DIRECTION_UP;
		}

		dataList.get(i).setDirection(direction);
	}

	void analyzeLine(ArrayList<StockData> stockDataList,
			ArrayList<StockData> dataList, ArrayList<StockData> vertexList,
			int vertexTypeTop, int vertexTypeBottom) {
		int i = 0;
		int size = 0;
		int direction = Constants.STOCK_DIRECTION_NONE;
		int vertex = Constants.STOCK_VERTEX_NONE;

		StockData stockData = null;

		if ((stockDataList == null) || (dataList == null)
				|| (vertexList == null)) {
			return;
		}

		size = dataList.size();
		if (size < Constants.STOCK_VERTEX_TYPING_SIZE) {
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
					if (direction == Constants.STOCK_DIRECTION_UP) {
						if (dataList.get(i).getVertexLow() < dataList
								.get(i - 1).getVertexLow()) {
							vertex = vertexTypeTop;
							stockData.setVertex(stockData.getVertex() | vertex);
							vertexList.add(stockData);
						}
					} else if (direction == Constants.STOCK_DIRECTION_DOWN) {
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
				if (direction == Constants.STOCK_DIRECTION_UP) {
					vertex = vertexTypeTop;
				} else if (direction == Constants.STOCK_DIRECTION_DOWN) {
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

		if (vertexList.get(j).vertexOf(Constants.STOCK_VERTEX_TOP)) {
			stockData.set(stockDataList.get(i));
			stockData.setVertex(Constants.STOCK_VERTEX_BOTTOM);
		} else if (vertexList.get(j).vertexOf(Constants.STOCK_VERTEX_BOTTOM)) {
			stockData.set(stockDataList.get(i));
			stockData.setVertex(Constants.STOCK_VERTEX_TOP);
		}

		if (index == 0) {
			vertexList.add(0, stockData);
		} else {
			vertexList.add(stockData);
		}
	}

	void sigmaHistogram(StockData stockData, ArrayList<StockData> stockDataList) {
		double difDea = 0;
		double difDeaMax = 0;
		double histogram = 0;
		double histogramMax = 0;
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

			difDea = (current.getDIF() + current.getDEA()) / 2;
			histogram = current.getHistogram();

			if (stockData.getDirection() == Constants.STOCK_DIRECTION_UP) {
				if (difDea > 0) {
					if (difDea > difDeaMax) {
						difDeaMax = difDea;
					}
				}

				if (histogram > 0) {
					if (histogram > histogramMax) {
						histogramMax = histogram;
					}

					sigmaHistogram += histogram;
				}
			} else if (stockData.getDirection() == Constants.STOCK_DIRECTION_DOWN) {
				if (difDea < 0) {
					if (difDea < difDeaMax) {
						difDeaMax = difDea;
					}
				}

				if (histogram < 0) {
					if (histogram < histogramMax) {
						histogramMax = histogram;
					}

					sigmaHistogram += histogram;
				}
			}

			if (i == stockData.getIndexEnd()) {
				stockData.setDIF(difDeaMax);
				stockData.setHistogram(histogram);
				stockData.setSigmaHistogram(sigmaHistogram);
			}
		}
	}

	void vertexListToDataList(ArrayList<StockData> stockDataList,
			ArrayList<StockData> vertexList, ArrayList<StockData> dataList) {
		int size = 0;
		int direction = Constants.STOCK_DIRECTION_NONE;

		StockData prev = null;
		StockData current = null;
		StockData stockData = null;

		if ((vertexList == null) || (stockDataList == null)
				|| (dataList == null)) {
			return;
		}

		if (stockDataList.size() < Constants.STOCK_VERTEX_TYPING_SIZE) {
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

			if (prev.vertexOf(Constants.STOCK_VERTEX_TOP)
					&& current.vertexOf(Constants.STOCK_VERTEX_BOTTOM)) {
				direction = Constants.STOCK_DIRECTION_DOWN;
			} else if (prev.vertexOf(Constants.STOCK_VERTEX_BOTTOM)
					&& current.vertexOf(Constants.STOCK_VERTEX_TOP)) {
				direction = Constants.STOCK_DIRECTION_UP;
			} else {
				direction = Constants.STOCK_DIRECTION_NONE;
			}

			stockData.setDirection(direction);

			sigmaHistogram(stockData, stockDataList);

			dataList.add(stockData);
		}
	}

	void analyzeOverlap(ArrayList<StockData> stockDataList,
			ArrayList<StockData> segmentDataList,
			ArrayList<StockData> overlapList) {
		int size = 0;
		double Zg = 0;
		double Zd = 0;
		double overlapValue = 0;

		StockData prev = null;
		StockData current = null;

		StockData stockData = null;
		StockData segmentData = null;
		StockData overlap = null;

		if ((stockDataList == null) || (segmentDataList == null)
				|| (overlapList == null)) {
			return;
		}

		overlapList.clear();

		size = segmentDataList.size();

		for (int i = 2; i < size; i++) {
			prev = segmentDataList.get(i - 1);
			current = segmentDataList.get(i);

			if ((prev == null) || current == null) {
				continue;
			}

			if (overlap == null) {
				overlap = new StockData();
				overlap.set(prev);
				overlap.setIndex(i - 2);
				overlap.setIndexStart(i - 2);
				overlap.setIndexEnd(i);
				Zg = Math.min(prev.getVertexHigh(), current.getVertexHigh());
				Zd = Math.max(prev.getVertexLow(), current.getVertexLow());
				overlap.setHigh(Zg);
				overlap.setLow(Zd);
				if (overlap.getVertexLow() > 0) {
					overlapValue = 100
							* (overlap.getVertexHigh() - overlap.getVertexLow())
							/ overlap.getVertexLow();
					overlapValue = Utility.Round(overlapValue,
							Constants.DOUBLE_FIXED_DECIMAL);
				}
				overlap.setOverlapHigh(overlap.getVertexHigh());
				overlap.setOverlapLow(overlap.getVertexLow());
				overlap.setOverlap(overlapValue);
				overlapList.add(overlap);
				continue;
			}

			if (current.positionTo(overlap) == Constants.STOCK_POSITION_NONE) {
				overlap.setIndexEnd(i);
			} else {
				overlap = null;
			}
		}

		for (int i = 0; i < overlapList.size(); i++) {
			overlap = overlapList.get(i);
			for (int j = overlap.getIndexStart(); j <= overlap.getIndexEnd(); j++) {
				segmentData = segmentDataList.get(j);
				for (int k = segmentData.getIndexStart(); k <= segmentData
						.getIndexEnd(); k++) {
					stockData = stockDataList.get(k);
					stockData.setOverlapHigh(overlap.getVertexHigh());
					stockData.setOverlapLow(overlap.getVertexLow());
					stockData.setOverlap(overlap.getOverlap());
				}
			}
		}
	}

	void analyzeAction(ArrayList<StockData> stockDataList,
			ArrayList<StockData> lineDataList, int type) {
		int divergence = Constants.STOCK_DIVERGENCE_NONE;
		String action = Constants.STOCK_ACTION_NONE;
		StockData stockData = null;
		StockData current = null;
		StockData base = null;

		if ((stockDataList == null) || (lineDataList == null)) {
			return;
		}

		if (stockDataList.size() < Constants.STOCK_VERTEX_TYPING_SIZE) {
			return;
		}

		if (lineDataList.size() < Constants.STOCK_VERTEX_TYPING_SIZE) {
			return;
		}

		stockData = stockDataList.get(stockDataList.size() - 1);
		current = lineDataList.get(lineDataList.size() - 1);
		base = lineDataList.get(lineDataList.size() - 3);

		if ((base == null) || (current == null) || (stockData == null)) {
			return;
		}

		if (current.getDirection() != base.getDirection()) {
			return;
		}

		divergence = current.divergenceValue(current.getDirection(), base);
		stockData.setDivergence(divergence);

		if (divergence > Constants.STOCK_DIVERGENCE_SIGMA_HISTOGRAM) {
			if (current.directionOf(Constants.STOCK_DIRECTION_UP)
					&& stockData.directionOf(Constants.STOCK_DIRECTION_UP)) {
				action = Constants.STOCK_ACTION_HIGH + String.valueOf(type);
			} else if (current.directionOf(Constants.STOCK_DIRECTION_DOWN)
					&& stockData.directionOf(Constants.STOCK_DIRECTION_DOWN)) {
				action = Constants.STOCK_ACTION_LOW + String.valueOf(type);
			}
		}

		stockData.setAction(stockData.getAction() + action);
	}

	void analyzeDirection(ArrayList<StockData> stockDataList) {
		int i = 0;
		int direction = Constants.STOCK_DIRECTION_NONE;
		int directionBase = Constants.STOCK_DIRECTION_NONE;
		int directionStroke = Constants.STOCK_DIRECTION_NONE;
		int directionSegment = Constants.STOCK_DIRECTION_NONE;

		StockData prev = null;
		StockData stockData = null;

		if (stockDataList.size() < Constants.STOCK_VERTEX_TYPING_SIZE) {
			return;
		}

		i = 0;
		prev = stockDataList.get(i);

		for (i = 1; i < stockDataList.size(); i++) {
			stockData = stockDataList.get(i);

			direction = Constants.STOCK_DIRECTION_NONE;

			if (prev.vertexOf(Constants.STOCK_VERTEX_TOP)) {
				directionBase = Constants.STOCK_DIRECTION_DOWN;
			} else if (prev.vertexOf(Constants.STOCK_VERTEX_BOTTOM)) {
				directionBase = Constants.STOCK_DIRECTION_UP;
			}

			if (!(stockData.vertexOf(Constants.STOCK_VERTEX_TOP) || stockData
					.vertexOf(Constants.STOCK_VERTEX_BOTTOM))) {
				direction |= directionBase;
			}

			if (prev.vertexOf(Constants.STOCK_VERTEX_TOP_STROKE)) {
				directionStroke = Constants.STOCK_DIRECTION_DOWN_STROKE;
			} else if (prev.vertexOf(Constants.STOCK_VERTEX_BOTTOM_STROKE)) {
				directionStroke = Constants.STOCK_DIRECTION_UP_STROKE;
			}

			if (!(stockData.vertexOf(Constants.STOCK_VERTEX_TOP_STROKE) || stockData
					.vertexOf(Constants.STOCK_VERTEX_BOTTOM_STROKE))) {
				direction |= directionStroke;
			}

			if (prev.vertexOf(Constants.STOCK_VERTEX_TOP_SEGMENT)) {
				directionSegment = Constants.STOCK_DIRECTION_DOWN_SEGMENT;
			} else if (prev.vertexOf(Constants.STOCK_VERTEX_BOTTOM_SEGMENT)) {
				directionSegment = Constants.STOCK_DIRECTION_UP_SEGMENT;
			}

			if (!(stockData.vertexOf(Constants.STOCK_VERTEX_TOP_SEGMENT) || stockData
					.vertexOf(Constants.STOCK_VERTEX_BOTTOM_SEGMENT))) {
				direction |= directionSegment;
			}

			stockData.setDirection(direction);

			if (stockData.getVertex() != Constants.STOCK_VERTEX_NONE) {
				prev = stockDataList.get(i);
			}
		}
	}

	void testShow(ArrayList<StockData> stockDataList,
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

			if (data.getDirection() == Constants.STOCK_DIRECTION_UP) {
				stockData.setOpen(data.getVertexLow());
				stockData.setClose(data.getVertexHigh());
			} else if (data.getDirection() == Constants.STOCK_DIRECTION_DOWN) {
				stockData.setOpen(data.getVertexHigh());
				stockData.setClose(data.getVertexLow());
			}

			stockData.setHigh(data.getVertexHigh());
			stockData.setLow(data.getVertexLow());
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
