package com.android.orion.analyzer;

import android.text.TextUtils;

import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockTrend;
import com.android.orion.manager.DatabaseManager;
import com.android.orion.provider.StockPerceptronProvider;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Utility;

import java.util.ArrayList;

public class TrendAnalyzer {
	Logger Log = Logger.getLogger();
	Stock mStock;
	String mPeriod;
	ArrayList<StockData> mStockDataList = new ArrayList<>();
	ArrayList<StockTrend> mStockTrendList = new ArrayList<>();
	DatabaseManager mDatabaseManager = DatabaseManager.getInstance();
	StockPerceptronProvider mStockPerceptronProvider = StockPerceptronProvider.getInstance();

	private TrendAnalyzer() {
	}

	public static TrendAnalyzer getInstance() {
		return Holder.INSTANCE;
	}

	void setup(Stock stock, String period, ArrayList<StockData> stockDataList) {
		mStock = stock;
		mPeriod = period;
		mStockDataList = stockDataList;
	}

	void analyzeVertex(ArrayList<StockData> vertexList) {
		if ((mStockDataList == null) || (vertexList == null)) {
			return;
		}

		ArrayList<StockData> dataList = new ArrayList<>();
		for (StockData stockData : mStockDataList) {
			dataList.add(new StockData(stockData));
		}

		int size = dataList.size();
		if (size < StockTrend.VERTEX_SIZE) {
			return;
		}

		vertexList.clear();
		StockData prev = new StockData();
		StockData current = new StockData();
		StockData next = new StockData();

		int direction = StockTrend.DIRECTION_NONE;
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

				mStockDataList.get(i).setDirection(direction);
				mStockDataList.get(i).setVertex(vertex);

				if ((vertex == StockTrend.VERTEX_TOP)
						|| (vertex == StockTrend.VERTEX_BOTTOM)) {
					vertexList.add(dataList.get(i));

					dataList.get(i).setDirection(StockTrend.DIRECTION_NONE);
					mStockDataList.get(i).setDirection(StockTrend.DIRECTION_NONE);
				}

				if (current.include(next) || current.includedBy(next)) {
					current.merge(direction, next);
					next.merge(direction, current);

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
			extendVertexList(mStockDataList, vertexList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void extendVertexList(ArrayList<StockData> dataList, ArrayList<StockData> vertexList) {
		if (dataList == null || dataList.size() < StockTrend.VERTEX_SIZE || vertexList == null || vertexList.isEmpty()) {
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
//			StockData stockData = dataList.get(index);
			StockData vertex = new StockData(dataList.get(index));

			StockData trend = isStart ? vertexList.get(0) : vertexList.get(vertexList.size() - 1);

			if (trend.vertexOf(StockTrend.VERTEX_TOP)) {
				vertex.setVertex(StockTrend.VERTEX_BOTTOM);
			} else if (trend.vertexOf(StockTrend.VERTEX_BOTTOM)) {
				vertex.setVertex(StockTrend.VERTEX_TOP);
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

	void analyzeLine(int level, ArrayList<StockData> dataList, ArrayList<StockData> vertexList) {
		if ((dataList == null) || (vertexList == null)) {
			return;
		}

		int size = dataList.size();
		if (size < StockTrend.VERTEX_SIZE) {
			return;
		}

		int vertexTypeTop = StockTrend.VERTEX_TOP;
		int vertexTypeBottom = StockTrend.VERTEX_BOTTOM;
		switch (level) {
			case StockTrend.LEVEL_DRAW:
				vertexTypeTop = StockTrend.VERTEX_TOP_STROKE;
				vertexTypeBottom = StockTrend.VERTEX_BOTTOM_STROKE;
				break;
			case StockTrend.LEVEL_STROKE:
				vertexTypeTop = StockTrend.VERTEX_TOP_SEGMENT;
				vertexTypeBottom = StockTrend.VERTEX_BOTTOM_SEGMENT;
				break;
			case StockTrend.LEVEL_SEGMENT:
				vertexTypeTop = StockTrend.VERTEX_TOP_LINE;
				vertexTypeBottom = StockTrend.VERTEX_BOTTOM_LINE;
				break;
			case StockTrend.LEVEL_LINE:
				vertexTypeTop = StockTrend.VERTEX_TOP_OUTLINE;
				vertexTypeBottom = StockTrend.VERTEX_BOTTOM_OUTLINE;
				break;
			case StockTrend.LEVEL_OUT_LINE:
				vertexTypeTop = StockTrend.VERTEX_TOP_SUPERLINE;
				vertexTypeBottom = StockTrend.VERTEX_BOTTOM_SUPERLINE;
				break;
			case StockTrend.LEVEL_SUPER_LINE:
				vertexTypeTop = StockTrend.VERTEX_TOP_TREND_LINE;
				vertexTypeBottom = StockTrend.VERTEX_BOTTOM_TREND_LINE;
				break;
			default:
				break;
		}

		try {
			int direction = dataList.get(0).getDirection();
			int baseDirection = direction;
			String type = StockTrend.TYPE_NONE;
			vertexList.clear();
			mStockTrendList.clear();
			for (int i = 2; i < size; i++) {
				StockData current = dataList.get(i);
				StockData prev = dataList.get(i - 1);
				StockData prevPrev = dataList.get(i - 2);

				if (current == null || prev == null || prevPrev == null) {
					continue;
				}

				StockData start_1 = StockData.getSafely(mStockDataList, prev.getIndexStart());
				StockData end_1 = StockData.getSafely(mStockDataList, prev.getIndexEnd());
				StockData start_2 = StockData.getSafely(mStockDataList, prevPrev.getIndexStart());
				StockData end_2 = StockData.getSafely(mStockDataList, prevPrev.getIndexEnd());
				if (start_1 == null || end_1 == null || start_2 == null || end_2 == null) {
					continue;
				}

				int directionTo = current.directionTo(prevPrev);
				switch (directionTo) {
					case StockTrend.DIRECTION_UP:
						if (direction == StockTrend.DIRECTION_DOWN) {
							type = StockTrend.TYPE_DOWN_UP;
							addVertex(end_2, vertexTypeBottom, vertexList);
							addStockTrend(level, type, prevPrev, prev, current);
						} else if (direction == StockTrend.DIRECTION_NONE) {
							StockData vertexData = chooseVertex(start_2, end_2, StockTrend.VERTEX_BOTTOM);
							if (baseDirection == StockTrend.DIRECTION_UP) {
								type = StockTrend.TYPE_UP_NONE_UP;
								addVertex(vertexData, vertexTypeBottom, vertexList);
								addStockTrend(level, type, prevPrev, prev, current);
							} else if (baseDirection == StockTrend.DIRECTION_DOWN) {
								type = StockTrend.TYPE_DOWN_NONE_UP;
							}
						} else if (direction == StockTrend.DIRECTION_UP) {
							type = StockTrend.TYPE_UP_UP;
						}
						break;
					case StockTrend.DIRECTION_DOWN:
						if (direction == StockTrend.DIRECTION_UP) {
							type = StockTrend.TYPE_UP_DOWN;
							addVertex(end_2, vertexTypeTop, vertexList);
							addStockTrend(level, type, prevPrev, prev, current);
						} else if (direction == StockTrend.DIRECTION_NONE) {
							StockData vertexData = chooseVertex(start_2, end_2, StockTrend.VERTEX_TOP);
							if (baseDirection == StockTrend.DIRECTION_UP) {
								type = StockTrend.TYPE_UP_NONE_DOWN;
							} else if (baseDirection == StockTrend.DIRECTION_DOWN) {
								type = StockTrend.TYPE_DOWN_NONE_DOWN;
								addVertex(vertexData, vertexTypeTop, vertexList);
								addStockTrend(level, type, prevPrev, prev, current);
							}
						} else if (direction == StockTrend.DIRECTION_DOWN) {
							type = StockTrend.TYPE_DOWN_DOWN;
						}
						break;
					case StockTrend.DIRECTION_NONE:
						if (direction == StockTrend.DIRECTION_UP) {
							baseDirection = StockTrend.DIRECTION_UP;
							StockData vertexData = chooseVertex(start_1, end_1, StockTrend.VERTEX_TOP);
							type = StockTrend.TYPE_UP_NONE;
							addVertex(vertexData, vertexTypeTop, vertexList);
							addStockTrend(level, type, prevPrev, prev, current);
						} else if (direction == StockTrend.DIRECTION_DOWN) {
							baseDirection = StockTrend.DIRECTION_DOWN;
							StockData vertexData = chooseVertex(start_1, end_1, StockTrend.VERTEX_BOTTOM);
							type = StockTrend.TYPE_DOWN_NONE;
							addVertex(vertexData, vertexTypeBottom, vertexList);
							addStockTrend(level, type, prevPrev, prev, current);
						} else if (direction == StockTrend.DIRECTION_NONE) {
						}
						break;
				}
				direction = directionTo;
			}
			extendVertexList(dataList, vertexList);

			if (mStockTrendList.size() > 0) {
				StockTrend stockTrend = mStockTrendList.get(mStockTrendList.size() - 1);
				if (mDatabaseManager.isStockTrendExist(stockTrend)) {
					mDatabaseManager.getStockTrend(stockTrend);
					if (TextUtils.equals(type, stockTrend.getType())) {
						stockTrend.removeFlag(StockTrend.FLAG_CHANGED);
						stockTrend.setModified(Utility.getCurrentDateTimeString());
						mDatabaseManager.updateStockTrend(stockTrend, stockTrend.getContentValues());

//						mStockPerceptronProvider.train(stockTrend.getPeriod(), stockTrend.getLevel(), stockTrend.getType());
					} else {
						stockTrend.addFlag(StockTrend.FLAG_CHANGED);
						stockTrend.setModified(Utility.getCurrentDateTimeString());
						mDatabaseManager.updateStockTrend(stockTrend, stockTrend.getContentValues());

						if (Setting.getDisplayAdaptive() && level >= mStock.getLevel(mPeriod)) {
							StockData stockData = StockData.getLast(mStockDataList, 0);
							stockData.setAction(StockTrend.MARK_LEVEL + level + type + Constant.NEW_LINE + (int) stockTrend.getNet2() + "|" + (int) stockTrend.getNet1() + "|" + (int) stockTrend.getNet());
							StockAnalyzer.getInstance().notifyStockTrend(stockTrend);
						}
					}
				} else {
					stockTrend.setFlag(StockTrend.FLAG_NONE);
					stockTrend.setCreated(Utility.getCurrentDateTimeString());
					mDatabaseManager.insertStockTrend(stockTrend);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private StockData chooseVertex(StockData start, StockData end, int vertexType) {
		if (start == null || end == null) {
			return null;
		}
		return start.vertexOf(vertexType) ? start : end;
	}

	private void addVertex(StockData stockData, int vertex, ArrayList<StockData> vertexList) {
		if (stockData == null || vertexList == null) {
			return;
		}
		stockData.addVertexFlag(vertex);
		vertexList.add(stockData);
	}

	private void addStockTrend(int level, String type, StockData prevPrev, StockData prev, StockData current) {
		if (TextUtils.isEmpty(type) || prevPrev == null || prev == null || current == null) {
			return;
		}

		StockTrend stockTrend = new StockTrend();
		stockTrend.setSE(mStock.getSE());
		stockTrend.setCode(mStock.getCode());
		stockTrend.setName(mStock.getName());
		stockTrend.setPeriod(mPeriod);
		stockTrend.setLevel(level);
		stockTrend.setType(type);
		stockTrend.setFlag(StockTrend.FLAG_NONE);

		stockTrend.setNet2(prevPrev.getNet());
		stockTrend.setNet1(prev.getNet());
		stockTrend.setNet(current.getNet());

		stockTrend.setDateTime(current);
		stockTrend.setCreated(Utility.getCurrentDateTimeString());
		mStockTrendList.add(stockTrend);
	}

	void vertexListToDataList(ArrayList<StockData> vertexList, ArrayList<StockData> dataList) {
		if (vertexList == null || dataList == null) {
			return;
		}

		if (vertexList.isEmpty()) {
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
			stockData.setIndexStart(prev.getIndexStart());
			stockData.setIndexEnd(current.getIndexEnd());
			stockData.merge(StockTrend.DIRECTION_NONE, prev);

			int direction = StockTrend.DIRECTION_NONE;
			if (prev.vertexOf(StockTrend.VERTEX_TOP)) {
				direction = current.vertexOf(StockTrend.VERTEX_BOTTOM) ? StockTrend.DIRECTION_DOWN : StockTrend.DIRECTION_UP;
			} else if (prev.vertexOf(StockTrend.VERTEX_BOTTOM)) {
				direction = current.vertexOf(StockTrend.VERTEX_TOP) ? StockTrend.DIRECTION_UP : StockTrend.DIRECTION_DOWN;
			}
			stockData.setDirection(direction);
			stockData.setupChange();
			stockData.setupNet();
			dataList.add(stockData);
		}
	}

	private static class Holder {
		private static final TrendAnalyzer INSTANCE = new TrendAnalyzer();
	}
}
