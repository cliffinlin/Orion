package com.android.orion.analyzer;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.android.orion.data.Period;
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
	ArrayMap<Double, Double> mStockTrendNetMap = new ArrayMap<>();
	ArrayList<StockData> mStockDataList = new ArrayList<>();
	DatabaseManager mDatabaseManager = DatabaseManager.getInstance();
	StockPerceptronProvider mStockPerceptronProvider = StockPerceptronProvider.getInstance();

	private TrendAnalyzer() {
	}

	public static TrendAnalyzer getInstance() {
		return Holder.INSTANCE;
	}

	public ArrayMap<Double, Double> getStockTrendNetMap() {
		return mStockTrendNetMap;
	}

	void setup(Stock stock, String period, ArrayList<StockData> stockDataList) {
		mStock = stock;
		mPeriod = period;
		mStockDataList = stockDataList;
	}

	void analyzeVertex(int level) {
		ArrayList<StockData> dataList;
		ArrayList<StockData> vertexList = mStock.getVertexList(mPeriod, StockTrend.LEVEL_DRAW);
		if (mStockDataList == null || mStockDataList.size() < StockTrend.VERTEX_SIZE || vertexList == null) {
			return;
		}

		if (Setting.getDisplayMerged()) {
			dataList = new ArrayList<>(mStockDataList);
		} else {
			dataList = new ArrayList<>();
			for (StockData stockData : mStockDataList) {
				dataList.add(new StockData(stockData));
			}
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
					dataList.get(i).setDirection(StockTrend.DIRECTION_NONE);
					vertexList.add(dataList.get(i));

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
			if (Setting.getDisplayMerged()) {
				extendVertexList(dataList, vertexList);
			} else {
				extendVertexList(mStockDataList, vertexList);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void addStockDataList(ArrayList<StockData> vertexList, ArrayList<StockData> dataList) {
		if (vertexList == null || vertexList.size() < 2 || dataList == null) {
			return;
		}

		StockData prev = StockData.getLast(vertexList, 1);
		StockData current = StockData.getLast(vertexList, 0);

		if ((prev == null) || (current == null)) {
			return;
		}

		StockData stockData = new StockData(current);
		stockData.setIndexStart(prev.getIndexStart());
		stockData.setIndexEnd(current.getIndexEnd());
		stockData.getCandle().setOpen(prev.getCandle().getOpen());
		stockData.getCandle().setClose(current.getCandle().getClose());
		stockData.merge(StockTrend.DIRECTION_NONE, prev);

		int direction = StockTrend.DIRECTION_NONE;
		if (prev.vertexOf(StockTrend.VERTEX_TOP)) {
			direction = current.vertexOf(StockTrend.VERTEX_BOTTOM) ? StockTrend.DIRECTION_DOWN : StockTrend.DIRECTION_UP;
		} else if (prev.vertexOf(StockTrend.VERTEX_BOTTOM)) {
			direction = current.vertexOf(StockTrend.VERTEX_TOP) ? StockTrend.DIRECTION_UP : StockTrend.DIRECTION_DOWN;
		}
		stockData.setDirection(direction);
		stockData.setupNet();
		dataList.add(stockData);
	}

	void addStockTrendList(int level, String type, ArrayList<StockData> dataList, ArrayList<StockTrend> stockTrendList) {
		if (dataList == null || dataList.size() < StockTrend.VERTEX_SIZE || stockTrendList == null) {
			return;
		}

		StockData prev = StockData.getLast(dataList, 2);
		StockData current = StockData.getLast(dataList, 1);
		StockData next = StockData.getLast(dataList, 0);

		if ((prev == null) || (current == null) || (next == null)) {
			return;
		}

		String action = "";
		StockTrend stockTrend = new StockTrend();
		stockTrend.setSE(mStock.getSE());
		stockTrend.setCode(mStock.getCode());
		stockTrend.setName(mStock.getName());
		stockTrend.setPeriod(mPeriod);
		if (next.getDirection() == StockTrend.DIRECTION_UP) {
			action = Constant.MARK_ADD;
		} else if (next.getDirection() == StockTrend.DIRECTION_DOWN) {
			action = Constant.MARK_MINUS;
		}
		stockTrend.setAction(action);
		stockTrend.setDate(current.getDate());
		stockTrend.setTime(current.getTime());

		stockTrend.setLevel(level);
		stockTrend.setType(type);

		double turn = 0;
		if (next.getDirection() == StockTrend.DIRECTION_UP) {
			turn = next.getCandle().getLow();
		} else if (next.getDirection() == StockTrend.DIRECTION_DOWN) {
			turn = next.getCandle().getHigh();
		}
		stockTrend.setTurn(turn);
		stockTrend.setPrevNet(prev.getNet());
		stockTrend.setNet(current.getNet());
		stockTrend.setNextNet(next.getNet());
		stockTrend.setPredict(mStockPerceptronProvider.getStockPerceptron().predict(stockTrend.getNet()));

		stockTrendList.add(stockTrend);
		mStockTrendNetMap.put(stockTrend.getNet(), stockTrend.getNextNet());
	}

	void extendVertexList(ArrayList<StockData> dataList, ArrayList<StockData> vertexList) {
		if (dataList == null || dataList.size() < StockTrend.VERTEX_SIZE || vertexList == null || vertexList.isEmpty()) {
			return;
		}

		extendVertexList(0, dataList, vertexList);
		extendVertexList(dataList.size() - 1, dataList, vertexList);
	}

	private void extendVertexList(int index, ArrayList<StockData> dataList, ArrayList<StockData> vertexList) {
		if (dataList == null || dataList.isEmpty() || vertexList == null || vertexList.isEmpty() || index < 0 || index >= dataList.size()) {
			return;
		}

		try {
			StockData vertex = new StockData(dataList.get(index));
			StockData current = index == 0 ? vertexList.get(0) : vertexList.get(vertexList.size() - 1);

			if (current.vertexOf(StockTrend.VERTEX_TOP)) {
				vertex.setVertex(StockTrend.VERTEX_BOTTOM);
			} else if (current.vertexOf(StockTrend.VERTEX_BOTTOM)) {
				vertex.setVertex(StockTrend.VERTEX_TOP);
			}

			if (index == 0) {
				vertexList.add(0, vertex);
			} else {
				vertexList.add(vertex);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void analyzeLine(int level) {
		ArrayList<StockData> prevDataList = mStock.getDataList(mPeriod, level - 1);
		ArrayList<StockData> vertexList = mStock.getVertexList(mPeriod, level);
		ArrayList<StockData> dataList = mStock.getDataList(mPeriod, level);
		ArrayList<StockTrend> stockTrendList = mStock.getStockTrendList(mPeriod, level);
		if (prevDataList == null || vertexList == null || dataList == null || stockTrendList == null) {
			return;
		}

		int size = prevDataList.size();
		if (size < StockTrend.VERTEX_SIZE) {
			return;
		}

		int vertexTypeTop = StockTrend.VERTEX_TOP;
		int vertexTypeBottom = StockTrend.VERTEX_BOTTOM;
		switch (level) {
			case StockTrend.LEVEL_STROKE:
				vertexTypeTop = StockTrend.VERTEX_TOP_STROKE;
				vertexTypeBottom = StockTrend.VERTEX_BOTTOM_STROKE;
				break;
			case StockTrend.LEVEL_SEGMENT:
				vertexTypeTop = StockTrend.VERTEX_TOP_SEGMENT;
				vertexTypeBottom = StockTrend.VERTEX_BOTTOM_SEGMENT;
				break;
			case StockTrend.LEVEL_LINE:
				vertexTypeTop = StockTrend.VERTEX_TOP_LINE;
				vertexTypeBottom = StockTrend.VERTEX_BOTTOM_LINE;
				break;
			case StockTrend.LEVEL_OUT_LINE:
				vertexTypeTop = StockTrend.VERTEX_TOP_OUTLINE;
				vertexTypeBottom = StockTrend.VERTEX_BOTTOM_OUTLINE;
				break;
			case StockTrend.LEVEL_SUPER_LINE:
				vertexTypeTop = StockTrend.VERTEX_TOP_SUPERLINE;
				vertexTypeBottom = StockTrend.VERTEX_BOTTOM_SUPERLINE;
				break;
			case StockTrend.LEVEL_TREND_LINE:
				vertexTypeTop = StockTrend.VERTEX_TOP_TREND_LINE;
				vertexTypeBottom = StockTrend.VERTEX_BOTTOM_TREND_LINE;
				break;
			default:
				break;
		}

		try {
			int direction = prevDataList.get(0).getDirection();
			int baseDirection = direction;
			String type = StockTrend.TYPE_NONE;
			vertexList.clear();
			dataList.clear();
			stockTrendList.clear();
			for (int i = 1; i < size - 1; i++) {
				StockData prev = prevDataList.get(i - 1);
				StockData current = prevDataList.get(i);
				StockData next = prevDataList.get(i + 1);

				if (next == null || current == null || prev == null) {
					continue;
				}

				StockData prev_start = StockData.getSafely(mStockDataList, prev.getIndexStart());
				StockData prev_end = StockData.getSafely(mStockDataList, prev.getIndexEnd());
				StockData current_start = StockData.getSafely(mStockDataList, current.getIndexStart());
				StockData current_end = StockData.getSafely(mStockDataList, current.getIndexEnd());
				if (prev_start == null || prev_end == null || current_start == null || current_end == null) {
					continue;
				}

				int directionTo = next.directionTo(prev);
				switch (directionTo) {
					case StockTrend.DIRECTION_UP:
						if (direction == StockTrend.DIRECTION_DOWN) {
							type = StockTrend.TYPE_DOWN_UP;
							addVertex(prev_end, vertexTypeBottom, vertexList);
							addStockDataList(vertexList, dataList);
							addStockTrendList(level, type, dataList, stockTrendList);
						} else if (direction == StockTrend.DIRECTION_NONE) {
							StockData vertexData = chooseVertex(prev_start, prev_end, StockTrend.VERTEX_BOTTOM);
							if (baseDirection == StockTrend.DIRECTION_UP) {
								type = StockTrend.TYPE_UP_NONE_UP;
								addVertex(vertexData, vertexTypeBottom, vertexList);
								addStockDataList(vertexList, dataList);
								addStockTrendList(level, type, dataList, stockTrendList);
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
							addVertex(prev_end, vertexTypeTop, vertexList);
							addStockDataList(vertexList, dataList);
							addStockTrendList(level, type, dataList, stockTrendList);
						} else if (direction == StockTrend.DIRECTION_NONE) {
							StockData vertexData = chooseVertex(prev_start, prev_end, StockTrend.VERTEX_TOP);
							if (baseDirection == StockTrend.DIRECTION_UP) {
								type = StockTrend.TYPE_UP_NONE_DOWN;
							} else if (baseDirection == StockTrend.DIRECTION_DOWN) {
								type = StockTrend.TYPE_DOWN_NONE_DOWN;
								addVertex(vertexData, vertexTypeTop, vertexList);
								addStockDataList(vertexList, dataList);
								addStockTrendList(level, type, dataList, stockTrendList);
							}
						} else if (direction == StockTrend.DIRECTION_DOWN) {
							type = StockTrend.TYPE_DOWN_DOWN;
						}
						break;
					case StockTrend.DIRECTION_NONE:
						if (direction == StockTrend.DIRECTION_UP) {
							baseDirection = StockTrend.DIRECTION_UP;
							StockData vertexData = chooseVertex(current_start, current_end, StockTrend.VERTEX_TOP);
							type = StockTrend.TYPE_UP_NONE;
							addVertex(vertexData, vertexTypeTop, vertexList);
							addStockDataList(vertexList, dataList);
							addStockTrendList(level, type, dataList, stockTrendList);
						} else if (direction == StockTrend.DIRECTION_DOWN) {
							baseDirection = StockTrend.DIRECTION_DOWN;
							StockData vertexData = chooseVertex(current_start, current_end, StockTrend.VERTEX_BOTTOM);
							type = StockTrend.TYPE_DOWN_NONE;
							addVertex(vertexData, vertexTypeBottom, vertexList);
							addStockDataList(vertexList, dataList);
							addStockTrendList(level, type, dataList, stockTrendList);
						} else if (direction == StockTrend.DIRECTION_NONE) {
						}
						break;
				}
				direction = directionTo;
			}

			if (TextUtils.isEmpty(type)) {
				return;
			}

			extendVertexList(mStockDataList.size() - 1, mStockDataList, vertexList);
			addStockDataList(vertexList, dataList);
			addStockTrendList(level, type, dataList, stockTrendList);

			if (stockTrendList.size() == 0) {
				return;
			}

			StockTrend stockTrend = stockTrendList.get(stockTrendList.size() - 1);
			if (mDatabaseManager.isStockTrendExist(stockTrend)) {
				StockTrend stockTrendFromDB = new StockTrend(stockTrend);
				mDatabaseManager.getStockTrend(stockTrendFromDB);
				stockTrend.setId(stockTrendFromDB.getId());
				if (TextUtils.equals(stockTrend.getType(), stockTrendFromDB.getType())) {
					stockTrend.removeFlag(StockTrend.FLAG_CHANGED);
					stockTrend.setModified(Utility.getCurrentDateTimeString());
					mDatabaseManager.updateStockTrend(stockTrend, stockTrend.getContentValues());

					StockAnalyzer.getInstance().cancelNotifyStockTrend(stockTrend);
				} else {
					stockTrend.addFlag(StockTrend.FLAG_CHANGED);
					stockTrend.setModified(Utility.getCurrentDateTimeString());
					mDatabaseManager.updateStockTrend(stockTrend, stockTrend.getContentValues());

					if (Setting.getDisplayAdaptive()) {
						if (level == mStock.getLevel(mPeriod)) {//TODO
							StockAnalyzer.getInstance().notifyStockTrend(stockTrend);
						}
					}
				}
			} else {
				stockTrend.setFlag(StockTrend.FLAG_NONE);
				stockTrend.setCreated(Utility.getCurrentDateTimeString());
				mDatabaseManager.insertStockTrend(stockTrend);
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
		stockData.addVertex(vertex);
		vertexList.add(stockData);
		if (vertexList.size() == 1) {
			extendVertexList(0, mStockDataList, vertexList);
		}
	}

	void vertexListToDataList(ArrayList<StockData> vertexList, ArrayList<StockData> dataList) {
		if (vertexList == null || vertexList.isEmpty() || dataList == null) {
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
			stockData.setupNet();
			dataList.add(stockData);
		}
	}

	private static class Holder {
		private static final TrendAnalyzer INSTANCE = new TrendAnalyzer();
	}
}
