package com.android.orion;

import java.util.ArrayList;

import com.android.orion.database.StockData;
import com.android.orion.utility.Utility;

public class VertexAnalyzer {
	public VertexAnalyzer() {

	}

	private void setDirectionVertex(ArrayList<StockData> dataList, int index,
			StockData prev, StockData current, StockData next) {
		int directionType = Constants.STOCK_DIRECTION_NONE;
		int vertexType = Constants.STOCK_VERTEX_NONE;

		directionType = Constants.STOCK_DIRECTION_NONE;
		vertexType = Constants.STOCK_VERTEX_NONE;

		if ((current.getVertexHigh() > prev.getVertexHigh())
				&& (current.getVertexLow() > prev.getVertexLow())) {
			directionType = Constants.STOCK_DIRECTION_UP;

			if ((current.getVertexHigh() > next.getVertexHigh())
					&& (current.getVertexLow() > next.getVertexLow())) {
				vertexType = Constants.STOCK_VERTEX_TOP;
			}
		} else if ((current.getVertexHigh() < prev.getVertexHigh())
				&& (current.getVertexLow() < prev.getVertexLow())) {
			directionType = Constants.STOCK_DIRECTION_DOWN;

			if ((current.getVertexHigh() < next.getVertexHigh())
					&& (current.getVertexLow() < next.getVertexLow())) {
				vertexType = Constants.STOCK_VERTEX_BOTTOM;
			}
		} else {
			directionType = Constants.STOCK_DIRECTION_NONE;
			vertexType = Constants.STOCK_VERTEX_NONE;
		}

		dataList.get(index).setDirection(directionType);
		dataList.get(index).setVertex(vertexType);
	}

	void analyzeVertex(ArrayList<StockData> dataList,
			ArrayList<StockData> vertexList) {
		int i = 0;
		int size = 0;
		int directionType = Constants.STOCK_DIRECTION_NONE;
		int vertexType = Constants.STOCK_VERTEX_NONE;

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

		prev = StockData.obtain();
		current = StockData.obtain();
		next = StockData.obtain();

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
				prev.merge(directionType, current);
				current.merge(directionType, prev);

				dataList.get(i - 1).set(prev);
				dataList.get(i).set(current);

				prev.set(current);

				current.init();
				next.init();
				continue;
			}

			if (current.include(next) || current.includedBy(next)) {
				setDirectionVertex(dataList, i, prev, current, next);
				directionType = dataList.get(i).getDirection();
				vertexType = dataList.get(i).getVertex();
				if ((vertexType == Constants.STOCK_VERTEX_TOP)
						|| (vertexType == Constants.STOCK_VERTEX_BOTTOM)) {
					vertexList.add(dataList.get(i));
				}

				current.merge(directionType, next);
				next.merge(directionType, current);

				dataList.get(i).set(current);
				dataList.get(i + 1).set(next);

				current.set(next);

				next.init();
				continue;
			}

			setDirectionVertex(dataList, i, prev, current, next);
			directionType = dataList.get(i).getDirection();
			vertexType = dataList.get(i).getVertex();
			if ((vertexType == Constants.STOCK_VERTEX_TOP)
					|| (vertexType == Constants.STOCK_VERTEX_BOTTOM)) {
				vertexList.add(dataList.get(i));
			}

			prev.set(current);
			current.set(next);

			next.init();
		}

		i = size - 1;
		if (vertexType == Constants.STOCK_VERTEX_TOP) {
			directionType = Constants.STOCK_DIRECTION_DOWN;
		} else if (vertexType == Constants.STOCK_VERTEX_BOTTOM) {
			directionType = Constants.STOCK_DIRECTION_UP;
		}

		dataList.get(i).setDirection(directionType);
	}

	void analyzeLine(ArrayList<StockData> stockDataList,
			ArrayList<StockData> dataList, ArrayList<StockData> vertexList,
			int vertexTypeTop, int vertexTypeBottom) {
		int i = 0;
		int size = 0;
		int directionType = Constants.STOCK_DIRECTION_NONE;
		int vertexType = Constants.STOCK_VERTEX_NONE;

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
		directionType = dataList.get(i).getDirection();

		for (i = 2; i < size; i++) {
			if (dataList.get(i).directionTo(dataList.get(i - 2)) == directionType) {
				i++;

				if (i == size - 1) {
					stockData = stockDataList.get(dataList.get(i - 1)
							.getIndexEnd());
					if (directionType == Constants.STOCK_DIRECTION_UP) {
						if (dataList.get(i).getVertexLow() < dataList
								.get(i - 1).getVertexLow()) {
							vertexType = vertexTypeTop;
							stockData.setVertex(stockData.getVertex()
									| vertexType);
							vertexList.add(stockData);
						}
					} else if (directionType == Constants.STOCK_DIRECTION_DOWN) {
						if (dataList.get(i).getVertexHigh() > dataList.get(
								i - 1).getVertexHigh()) {
							vertexType = vertexTypeBottom;
							stockData.setVertex(stockData.getVertex()
									| vertexType);
							vertexList.add(stockData);
						}
					}
				}
			} else {
				stockData = stockDataList
						.get(dataList.get(i - 2).getIndexEnd());
				if (directionType == Constants.STOCK_DIRECTION_UP) {
					vertexType = vertexTypeTop;
				} else if (directionType == Constants.STOCK_DIRECTION_DOWN) {
					vertexType = vertexTypeBottom;
				} else {
					Utility.Log("analyzeLine: directionType = " + directionType);
				}
				stockData.setVertex(stockData.getVertex() | vertexType);
				vertexList.add(stockData);
				directionType = dataList.get(i - 1).getDirection();
			}
		}
	}

	void extendVertexList(int index, ArrayList<StockData> stockDataList,
			ArrayList<StockData> vertexList) {
		int i = 0;
		int j = 0;
		StockData stockData = null;

		stockData = StockData.obtain();
		if (stockData == null) {
			return;
		}

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
		double histogram = 0;
		double sigmaHistogram = 0;

		StockData current = null;

		if ((stockData == null) || (stockDataList == null)) {
			return;
		}

		sigmaHistogram = 0;

		for (int i = stockData.getIndexStart() + 1; i <= stockData
				.getIndexEnd(); i++) {
			current = stockDataList.get(i);
			if (current == null) {
				return;
			}

			histogram = current.getHistogram();

			if ((stockData.getDirection() == Constants.STOCK_DIRECTION_UP)
					&& (histogram > 0)) {
				sigmaHistogram += histogram;
			} else if ((stockData.getDirection() == Constants.STOCK_DIRECTION_DOWN)
					&& (histogram < 0)) {
				sigmaHistogram += histogram;
			}

			if (i == stockData.getIndexEnd()) {
				stockData.setSigmaHistogram(sigmaHistogram);
			}
		}
	}

	void vertexListToDataList(ArrayList<StockData> stockDataList,
			ArrayList<StockData> vertexList, ArrayList<StockData> dataList,
			boolean sigmaHistogram) {
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

			stockData = StockData.obtain();
			if (stockData == null) {
				return;
			}

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

			if (sigmaHistogram) {
				sigmaHistogram(stockData, stockDataList);
			}

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
				overlap = StockData.obtain();
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
			ArrayList<StockData> segmentDataList,
			ArrayList<StockData> overlapList) {
		int i = 0;
		int indexStart = 0;
		int indexEnd = 0;
		String action = Constants.STOCK_ACTION_NONE;
		StockData baseSegmentData = null;
		StockData segmentData = null;
		StockData endStockData = null;
		StockData prevOverlap = null;

		if ((stockDataList == null) || (segmentDataList == null)
				|| (overlapList == null)) {
			return;
		}

		for (StockData overlap : overlapList) {
			indexStart = overlap.getIndexStart();
			indexEnd = overlap.getIndexEnd();

			i = indexStart + 1;
			if (prevOverlap != null) {
				segmentData = segmentDataList.get(i);
				endStockData = stockDataList.get(segmentData.getIndexEnd());
				action = endStockData.getAction();
				if (overlap.getVertexLow() > prevOverlap.getVertexHigh()) {
					action += Constants.STOCK_ACTION_BUY3;
				} else if (overlap.getVertexHigh() < prevOverlap.getVertexLow()) {
					action += Constants.STOCK_ACTION_SELL3;
				}
				endStockData.setAction(action);
			}
			prevOverlap = overlap;

			for (i = indexStart + 2; i <= indexEnd; i++) {
				segmentData = segmentDataList.get(i);
				endStockData = stockDataList.get(segmentData.getIndexEnd());

				baseSegmentData = segmentDataList.get(indexStart);
				setAction(baseSegmentData, segmentData, endStockData);

				baseSegmentData = segmentDataList.get(i - 2);
				setAction(baseSegmentData, segmentData, endStockData);
			}
		}
	}

	void setAction(StockData baseSegmentData, StockData segmentData,
			StockData endStockData) {
		int direction = Constants.STOCK_DIRECTION_NONE;
		int divergence = Constants.STOCK_DIVERGENCE_NONE;
		String action = Constants.STOCK_ACTION_NONE;

		if ((baseSegmentData == null) || (segmentData == null)
				|| (endStockData == null)) {
			return;
		}

		if (segmentData.getDirection() != baseSegmentData.getDirection()) {
			return;
		}

		action = Constants.STOCK_ACTION_NONE;
		direction = baseSegmentData.getDirection();
		divergence = segmentData.divergenceValue(direction, baseSegmentData);
		endStockData.setDivergence(divergence);

		if (direction == Constants.STOCK_DIRECTION_UP) {
			if (divergence != Constants.STOCK_DIVERGENCE_NONE) {
				action = Constants.STOCK_ACTION_SELL + divergence;
			}
		} else if (direction == Constants.STOCK_DIRECTION_DOWN) {
			if (divergence != Constants.STOCK_DIVERGENCE_NONE) {
				action = Constants.STOCK_ACTION_BUY + divergence;
			}
		}

		endStockData.setAction(endStockData.getAction() + action);
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
}
