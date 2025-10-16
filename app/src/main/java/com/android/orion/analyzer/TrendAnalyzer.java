package com.android.orion.analyzer;

import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.android.orion.chart.CurveThumbnail;
import com.android.orion.config.Config;
import com.android.orion.data.Period;
import com.android.orion.data.PolarComponent;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockPerceptron;
import com.android.orion.database.StockTrend;
import com.android.orion.manager.StockDatabaseManager;
import com.android.orion.manager.StockNotificationManager;
import com.android.orion.provider.StockPerceptronProvider;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Symbol;
import com.android.orion.utility.Utility;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TrendAnalyzer {
	public static final int K_MEANS_MAX_ITERATIONS = 100;

	public static final int DURATION_MAX = 30;//TODO

	int mKMeansPeriods;
	int mKMeansK;
	Logger Log = Logger.getLogger();
	Stock mStock;
	String mPeriod;
	List<DataPoint> mDataPointList = new ArrayList<>();
	List<Float>[] mXValues = new List[StockTrend.LEVELS.length];
	List<Float>[] mYValues = new List[StockTrend.LEVELS.length];
	List<CentroidCluster<DataPoint>> mClusterList;
	ArrayList<CurveThumbnail.LineConfig> mLineConfigList = new ArrayList<>();
	ArrayList<CurveThumbnail.ScatterConfig> mScatterConfigList = new ArrayList<>();
	ArrayList<StockData> mStockDataList = new ArrayList<>();
	ConcurrentMap<String, DataPoint> mAllDataPointMap = new ConcurrentHashMap<>();
	ConcurrentMap<String, DataPoint> mDataPointMap = new ConcurrentHashMap<>();
	StockDatabaseManager mStockDatabaseManager = StockDatabaseManager.getInstance();
	StockPerceptronProvider mStockPerceptronProvider = StockPerceptronProvider.getInstance();

	private TrendAnalyzer() {
	}

	public static TrendAnalyzer getInstance() {
		return Holder.INSTANCE;
	}

	void setup(Stock stock) {
		mStock = stock;
		mStockDatabaseManager.loadStockTrendMap(mStock, mStock.getStockTrendMap());
		mKMeansPeriods = 0;
		for (String period : Period.PERIODS) {
			if (Setting.getPeriod(period)) {
				mKMeansPeriods++;
			}
		}
		mKMeansK = mKMeansPeriods * (StockTrend.LEVELS.length - 1) / 2;
		mDataPointList.clear();
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

		dataList = new ArrayList<>();
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
			extendVertexList(mStockDataList, vertexList);
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

		if (prev == null || current == null) {
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

	void addStockTrendList(boolean finished, int level, String type, StockData prev, StockData current, StockData next, ArrayList<StockTrend> stockTrendList) {
		if (prev == null || current == null || next == null || stockTrendList == null) {
			return;
		}

		StockTrend stockTrend = new StockTrend();
		stockTrend.setSE(mStock.getSE());
		stockTrend.setCode(mStock.getCode());
		stockTrend.setName(mStock.getName());
		stockTrend.setPeriod(mPeriod);
		stockTrend.setDate(current.getDate());
		stockTrend.setTime(current.getTime());

		stockTrend.setLevel(level - 1);
		stockTrend.setType(type);
		stockTrend.setDirection(current.getDirection());

		double turn = 0;
		if (current.getDirection() == StockTrend.DIRECTION_UP) {
			turn = current.getCandle().getHigh();
		} else if (current.getDirection() == StockTrend.DIRECTION_DOWN) {
			turn = current.getCandle().getLow();
		}
		stockTrend.setTurn(turn);

		stockTrend.setPrevNet(prev.getNet());
		stockTrend.setNet(current.getNet());
		stockTrend.setNextNet(next.getNet());

		StockPerceptron stockPerceptron = mStockPerceptronProvider.getStockPerceptron(stockTrend.getPeriod(), stockTrend.getLevel(), stockTrend.getType());
		if (stockPerceptron != null) {
			if (finished) {
				stockPerceptron.getNetMap().put(stockTrend.getNet(), stockTrend.getNextNet());
				mStockPerceptronProvider.train(stockTrend.getPeriod(), stockTrend.getLevel(), stockTrend.getType());
			}
			double predict = stockPerceptron.predict(stockTrend.getNet());
			if ((predict * stockTrend.getNextNet() < 0)) {
				predict = 0;
			}
			if (Math.abs(predict) > Config.MAX_PREDICTION) {
				predict = 0;
			}
			stockTrend.setPredict(predict);
		}
		stockTrendList.add(stockTrend);
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
		ArrayList<StockData> prevDataList = mStock.getStockDataList(mPeriod, level - 1);
		ArrayList<StockData> vertexList = mStock.getVertexList(mPeriod, level);
		ArrayList<StockData> dataList = mStock.getStockDataList(mPeriod, level);
		ArrayList<StockTrend> stockTrendList = mStock.getStockTrendList(mPeriod, level);
		if (prevDataList == null || vertexList == null || dataList == null || stockTrendList == null) {
			return;
		}

		int size = prevDataList.size();
		if (size < StockTrend.VERTEX_SIZE) {
			return;
		}

		try {
			int vertexTop = StockTrend.getVertexTOP(level);
			int vertexBottom = StockTrend.getVertexBottom(level);
			int direction = prevDataList.get(0).getDirection();
			int baseDirection = direction;
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

				boolean finished = i < size - 2;
				int directionTo = next.directionTo(prev);
				String type;
				switch (directionTo) {
					case StockTrend.DIRECTION_UP:
						if (direction == StockTrend.DIRECTION_DOWN) {
							type = StockTrend.TYPE_DOWN_UP;
							addStockTrendList(finished, level, type, prev, current, next, stockTrendList);
							if (addVertex(prev_end, vertexBottom, vertexList)) {
								addStockDataList(vertexList, dataList);
							}
						} else if (direction == StockTrend.DIRECTION_NONE) {
							StockData vertexData = chooseVertex(prev_start, prev_end, StockTrend.VERTEX_BOTTOM);
							if (baseDirection == StockTrend.DIRECTION_UP) {
								type = StockTrend.TYPE_UP_NONE_UP;
								addStockTrendList(finished, level, type, prev, current, next, stockTrendList);
								if (addVertex(vertexData, vertexBottom, vertexList)) {
									addStockDataList(vertexList, dataList);
								}
							} else if (baseDirection == StockTrend.DIRECTION_DOWN) {
								type = StockTrend.TYPE_DOWN_NONE_UP;
								addStockTrendList(finished, level, type, prev, current, next, stockTrendList);
							}
						} else if (direction == StockTrend.DIRECTION_UP) {
							type = StockTrend.TYPE_UP_UP;
							addStockTrendList(finished, level, type, prev, current, next, stockTrendList);
						}
						break;
					case StockTrend.DIRECTION_DOWN:
						if (direction == StockTrend.DIRECTION_UP) {
							type = StockTrend.TYPE_UP_DOWN;
							addStockTrendList(finished, level, type, prev, current, next, stockTrendList);
							if (addVertex(prev_end, vertexTop, vertexList)) {
								addStockDataList(vertexList, dataList);
							}
						} else if (direction == StockTrend.DIRECTION_NONE) {
							StockData vertexData = chooseVertex(prev_start, prev_end, StockTrend.VERTEX_TOP);
							if (baseDirection == StockTrend.DIRECTION_UP) {
								type = StockTrend.TYPE_UP_NONE_DOWN;
								addStockTrendList(finished, level, type, prev, current, next, stockTrendList);
							} else if (baseDirection == StockTrend.DIRECTION_DOWN) {
								type = StockTrend.TYPE_DOWN_NONE_DOWN;
								addStockTrendList(finished, level, type, prev, current, next, stockTrendList);
								if (addVertex(vertexData, vertexTop, vertexList)) {
									addStockDataList(vertexList, dataList);
								}
							}
						} else if (direction == StockTrend.DIRECTION_DOWN) {
							type = StockTrend.TYPE_DOWN_DOWN;
							addStockTrendList(finished, level, type, prev, current, next, stockTrendList);
						}
						break;
					case StockTrend.DIRECTION_NONE:
						if (direction == StockTrend.DIRECTION_UP) {
							baseDirection = StockTrend.DIRECTION_UP;
							type = StockTrend.TYPE_UP_NONE;
							addStockTrendList(finished, level, type, prev, current, next, stockTrendList);
							StockData vertexData = chooseVertex(current_start, current_end, StockTrend.VERTEX_TOP);
							if (addVertex(vertexData, vertexTop, vertexList)) {
								addStockDataList(vertexList, dataList);
							}
						} else if (direction == StockTrend.DIRECTION_DOWN) {
							baseDirection = StockTrend.DIRECTION_DOWN;
							type = StockTrend.TYPE_DOWN_NONE;
							addStockTrendList(finished, level, type, prev, current, next, stockTrendList);
							StockData vertexData = chooseVertex(current_start, current_end, StockTrend.VERTEX_BOTTOM);
							if (addVertex(vertexData, vertexBottom, vertexList)) {
								addStockDataList(vertexList, dataList);
							}
						} else if (direction == StockTrend.DIRECTION_NONE) {
							type = StockTrend.TYPE_NONE_NONE;
							addStockTrendList(finished, level, type, prev, current, next, stockTrendList);
						}
						break;
				}
				direction = directionTo;
			}
			extendVertexList(mStockDataList.size() - 1, mStockDataList, vertexList);
			addStockDataList(vertexList, dataList);

			if (stockTrendList.size() == 0) {
				return;
			}

			mStockDatabaseManager.beginTransaction();
			StockTrend stockTrend = stockTrendList.get(stockTrendList.size() - 1);
			if (mStockDatabaseManager.isStockTrendExist(stockTrend)) {
				StockTrend stockTrendFromDB = new StockTrend(stockTrend);
				mStockDatabaseManager.getStockTrend(stockTrendFromDB);
				stockTrend.setId(stockTrendFromDB.getId());
				if (TextUtils.equals(stockTrend.getType(), stockTrendFromDB.getType())) {
					stockTrend.removeFlag(StockTrend.FLAG_CHANGED);
					stockTrend.setModified(Utility.getCurrentDateTimeString());
					mStockDatabaseManager.updateStockTrend(stockTrend, stockTrend.getContentValues());
					StockNotificationManager.getInstance().cancel((int) stockTrend.getId());
				} else {
					stockTrend.addFlag(StockTrend.FLAG_CHANGED);
					stockTrend.setModified(Utility.getCurrentDateTimeString());
					mStockDatabaseManager.updateStockTrend(stockTrend, stockTrend.getContentValues());
					StockNotificationManager.getInstance().notify(mStock, stockTrend);
				}
			} else {
				stockTrend.setFlag(StockTrend.FLAG_NONE);
				stockTrend.setCreated(Utility.getCurrentDateTimeString());
				mStockDatabaseManager.insertStockTrend(stockTrend);
			}
			mStockDatabaseManager.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.endTransaction();
		}
	}

	private StockData chooseVertex(StockData start, StockData end, int vertexType) {
		if (start == null || end == null) {
			return null;
		}
		return start.vertexOf(vertexType) ? start : end;
	}

	private boolean addVertex(StockData stockData, int vertex, ArrayList<StockData> vertexList) {
		if (stockData == null || vertexList == null) {
			return false;
		}

		if (vertexList.size() > 0) {
			StockData lastVertex = StockData.getLast(vertexList, 0);
			if (lastVertex != null) {
				Calendar lastCalendar = Utility.getCalendar(lastVertex.getDateTime(),
						Utility.CALENDAR_DATE_TIME_FORMAT);
				Calendar calendar = Utility.getCalendar(stockData.getDateTime(),
						Utility.CALENDAR_DATE_TIME_FORMAT);
				if (calendar.before(lastCalendar)) {
					return false;
				}
			}
		}

		stockData.addVertex(vertex);
		vertexList.add(stockData);
		if (vertexList.size() == 1) {
			extendVertexList(0, mStockDataList, vertexList);
		}
		return true;
	}

	void vertexListToDataList(ArrayList<StockData> vertexList, ArrayList<StockData> dataList) {
		if (vertexList == null || vertexList.size() < StockTrend.VERTEX_SIZE || dataList == null) {
			return;
		}

		dataList.clear();
		for (int i = 1; i < vertexList.size(); i++) {
			StockData prev = vertexList.get(i - 1);
			StockData current = vertexList.get(i);

			if (prev == null || current == null) {
				return;
			}

			StockData stockData = new StockData(current);
			stockData.setIndexStart(prev.getIndexStart());
			stockData.setIndexEnd(current.getIndexEnd());
			stockData.merge(StockTrend.DIRECTION_NONE, prev);

			int direction = StockTrend.DIRECTION_NONE;
			if (current.vertexOf(StockTrend.VERTEX_TOP)) {
				direction = prev.vertexOf(StockTrend.VERTEX_BOTTOM) ? StockTrend.DIRECTION_UP : StockTrend.DIRECTION_DOWN;
			} else if (current.vertexOf(StockTrend.VERTEX_BOTTOM)) {
				direction = prev.vertexOf(StockTrend.VERTEX_TOP) ? StockTrend.DIRECTION_DOWN : StockTrend.DIRECTION_UP;
			}
			stockData.setDirection(direction);
			stockData.setupNet();
			dataList.add(stockData);
		}
	}

	public void analyzeAdaptive(String period) {
		int level = StockTrend.LEVEL_DRAW;
		for (int i = StockTrend.LEVELS.length - 1; i > StockTrend.LEVEL_NONE; i--) {
			if (mStock.getVertexList(period, i).size() >= StockTrend.ADAPTIVE_SIZE) {
				level = i;
				break;
			}
		}
		if (Period.indexOf(period) < Period.indexOf(Period.DAY)) {
			mStock.setLevel(period, level);
		}
	}

	public void analyzeAdaptive(Stock stock) {
		setup(stock);

		for (String period : Period.PERIODS) {
			if (Setting.getPeriod(period)) {
				for (int i = StockTrend.LEVEL_DRAW; i < StockTrend.LEVELS.length; i++) {
					ArrayList<StockData> vertexList = mStock.getVertexList(period, i);
					mDataPointList.add(newDataPoint(period, i, vertexList));
				}
			}
		}

		if (mDataPointList.size() < mKMeansK) {
			Log.d("return, mDataPointList.size()=" + mDataPointList.size() + " < " + mKMeansK);
			return;
		}

		try {
			KMeansPlusPlusClusterer<DataPoint> clusterer = new KMeansPlusPlusClusterer<>(mKMeansK, K_MEANS_MAX_ITERATIONS);
			List<CentroidCluster<DataPoint>> clusterList = clusterer.cluster(mDataPointList);
			mClusterList = sortClusterList(clusterList);
			setupGroupDistance();
			analyzeTradeLevel();
			for (String period : Period.PERIODS) {
				if (Setting.getPeriod(period)) {
					setupPeriodThumbnail(period);
				}
			}
			setupTrendThumbnail();
			setupComponentThumbnail();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	DataPoint newDataPoint(String period, int level, ArrayList<StockData> vertexList) {
		if (vertexList == null || vertexList.size() < StockTrend.VERTEX_SIZE) {
			return new DataPoint(period, level, "", "", Integer.MAX_VALUE, StockTrend.DIRECTION_NONE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		}

		StockData prev = vertexList.get(vertexList.size() - 3);
		StockData start = vertexList.get(vertexList.size() - 2);
		StockData end = vertexList.get(vertexList.size() - 1);
		Calendar startCalendar = Utility.getCalendar(start.getDate(), Utility.CALENDAR_DATE_FORMAT);
		Calendar nowCalendar = Calendar.getInstance();
		long x = (nowCalendar.getTimeInMillis() - startCalendar.getTimeInMillis()) / Constant.MINUTE_IN_MILLIS;
		int y = StockTrend.DIRECTION_NONE;
		if (start.vertexOf(StockTrend.getVertexTOP(level))) {
			 y = StockTrend.DIRECTION_DOWN;
			 if (mStock.getPrice() > start.getCandle().getHigh()) {
//				 y = StockTrend.DIRECTION_UP_UP;//TODO
			 }
		} else if (start.vertexOf(StockTrend.getVertexBottom(level))) {
			y = StockTrend.DIRECTION_UP;
			if (mStock.getPrice() < start.getCandle().getLow()) {
//				y = StockTrend.DIRECTION_DOWN_DOWN;//TODO
			}
		}

		int past =  StockData.getDaysBetween(prev.getIndexEnd(), start.getIndexEnd(), mStock.getStockDataList(period, StockTrend.LEVEL_NONE));
		int duration = StockData.getDaysBetween(start.getIndexEnd(), end.getIndexEnd(), mStock.getStockDataList(period, StockTrend.LEVEL_NONE));
		return new DataPoint(period, level, start.getDateTime(), end.getDateTime(), x, y, past, duration);
	}

	List<CentroidCluster<DataPoint>> sortClusterList(List<CentroidCluster<DataPoint>> clusterList) {
		List<CentroidCluster<DataPoint>> sorted = new ArrayList<>(clusterList);

		Collections.sort(sorted, new Comparator<CentroidCluster<DataPoint>>() {
			@Override
			public int compare(CentroidCluster<DataPoint> o1, CentroidCluster<DataPoint> o2) {
				double val1 = o1.getCenter().getPoint()[0];
				double val2 = o2.getCenter().getPoint()[0];
				if (val1 < val2) return -1;
				if (val1 > val2) return 1;
				return 0;
			}
		});

		return sorted;
	}

	void setupGroupDistance() {
		if (mClusterList == null) {
			return;
		}

		mAllDataPointMap.clear();
		for (int i = 0; i < mClusterList.size(); i++) {
			CentroidCluster<DataPoint> cluster = mClusterList.get(i);
			StringBuilder clusterInfo = new StringBuilder();
			clusterInfo.append(Symbol.NEW_LINE + "=== 第 ").append(i).append(" 组 ===")
					.append(" | 中心值: ").append(java.util.Arrays.toString(cluster.getCenter().getPoint()))
					.append(" | 数量: ").append(cluster.getPoints().size());

			for (DataPoint point : cluster.getPoints()) {
				point.setGroup(i);
				point.setDistance(point.distanceTo(cluster.getCenter()));
				mAllDataPointMap.put(point.period + Symbol.L + point.level, point);
				clusterInfo.append("  ").append(point);
			}
//			Log.d(clusterInfo.toString());
		}
	}

	void analyzeTradeLevel() {
		if (mClusterList == null || mClusterList.isEmpty()) {
			return;
		}

		mDataPointMap.clear();
		for (CentroidCluster<DataPoint> cluster : mClusterList) {
			if (cluster == null) {
				continue;
			}

			List<DataPoint> validList = getValidListList(cluster.getPoints());
			if (validList == null || validList.isEmpty()) {
				continue;
			}

			if (!mDataPointMap.isEmpty()) {
				if (validList.get(0).duration > DURATION_MAX) {//TODO
					setupLevel();
					return;
				}

				if (isDirectionChanged((int)validList.get(0).getPoint()[1])) {
					if (mDataPointMap.size() == mKMeansPeriods) {
						setupLevel();
						return;
					} else {
						mDataPointMap.clear();
					}
				}
			}

			for (DataPoint dataPoint : validList) {
				updateDataPointMap(dataPoint);
			}
		}
	}

	List<DataPoint> getValidListList(List<DataPoint> dataPointList) {
		List<DataPoint> validList = new ArrayList<>();
		if (dataPointList == null || dataPointList.isEmpty()) {
			return validList;
		}

		for (DataPoint dataPoint : dataPointList) {
			if (dataPoint == null) {
				continue;
			}
			if (dataPoint.level == StockTrend.LEVEL_NONE) {
				continue;
			}
			if (dataPoint.getPoint() == null || dataPoint.getPoint().length < 2) {
				continue;
			}
			if (dataPoint.getPoint()[1] == StockTrend.DIRECTION_NONE) {
				continue;
			}
			if (dataPoint.past <= 1) {
				continue;
			}
			validList.add(dataPoint);
		}
		return validList;
	}

	boolean isDirectionChanged(int direction) {
		boolean result = false;
		for (DataPoint existingPoint : mDataPointMap.values()) {
			if (existingPoint.getPoint()[1] != direction) {
				result = true;
				break;
			}
		}
		return result;
	}

	void setupLevel() {
		HashSet<Double> predictSet = new HashSet<>();
		double predictSum = 0;
		StringBuilder builder = new StringBuilder();

		try {
			for (String period : Period.PERIODS) {
				if (Setting.getPeriod(period)) {
					DataPoint dataPoint = mDataPointMap.get(period);
					if (dataPoint == null) {
						continue;
					}

					if (mStock.hasFlag(Stock.FLAG_CUSTOM)) {
						dataPoint = mAllDataPointMap.get(period + Symbol.L + mStock.getLevel(period));
						if (dataPoint == null) {
							continue;
						}
					} else {
						mStock.setLevel(period, dataPoint.level);
					}
					builder.append(dataPoint);

					StockTrend stockTrend = mStock.getStockTrend(period, mStock.getLevel(period));
					if (stockTrend != null) {
						predictSet.add(Math.abs(stockTrend.getPredict()));
					}
				}
			}

			// 使用IQR方法去除predictSet中的异常点
			if (predictSet.size() >= 3) {
				// 使用标准严格程度(1.5倍IQR)
				Set<Double> filteredPredictSet = removeOutliersUsingIQR(predictSet, 2);
				predictSet = new HashSet<>(filteredPredictSet);
			}

			for (String period : Period.PERIODS) {
				if (Setting.getPeriod(period)) {
					StockTrend stockTrend = mStock.getStockTrend(period, mStock.getLevel(period));
					if (stockTrend != null) {
						if (predictSet.isEmpty()) {
							break;
						}
						if (!predictSet.contains(Math.abs(stockTrend.getPredict()))) {
							stockTrend.setPredict(0);
							continue;
						}
						predictSum += stockTrend.getPredict();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mStock.setPredict((mKMeansPeriods == 0) ?  0 : Utility.Round1(predictSum / mKMeansPeriods));
		Log.d(builder.toString());
	}

	/**
	 * 增强版IQR异常值检测，可选择不同的严格程度
	 */
	private Set<Double> removeOutliersUsingIQR(Set<Double> dataSet, double iqrMultiplier) {
		if (dataSet == null || dataSet.size() < 3) {
			return dataSet;
		}

		Log.d("IQR异常值检测 - 原始数据: " + dataSet.toString());

		double[] dataArray = new double[dataSet.size()];
		int index = 0;
		for (Double value : dataSet) {
			dataArray[index++] = value;
		}
		Arrays.sort(dataArray);

		double q1 = calculatePercentile(dataArray, 25);
		double q3 = calculatePercentile(dataArray, 75);
		double iqr = q3 - q1;

		// 使用可配置的倍数，默认1.5，更严格可以用1.0，更宽松可以用2.0
		double multiplier = (iqrMultiplier > 0) ? iqrMultiplier : 1.5;
		double lowerBound = q1 - multiplier * iqr;
		double upperBound = q3 + multiplier * iqr;

		Set<Double> filteredSet = new HashSet<>();
		int outlierCount = 0;
		for (double value : dataArray) {
			if (value >= lowerBound && value <= upperBound) {
				filteredSet.add(value);
			} else {
				outlierCount++;
				Log.d("IQR异常值检测: 移除异常值 " + value +
						" [Q1=" + q1 + ", Q3=" + q3 + ", IQR=" + iqr + "]");
			}
		}

		Log.d("IQR异常值检测: 原始数据 " + dataSet.size() + " 个, 移除 " +
				outlierCount + " 个异常值, 剩余 " + filteredSet.size() + " 个");

		return filteredSet;
	}

	/**
	 * 计算百分位数
	 */
	private double calculatePercentile(@NonNull double[] sortedData, double percentile) {
		if (sortedData == null || sortedData.length == 0) {
			Log.e("calculatePercentile: 输入数据为空");
			return 0.0;
		}

		if (percentile < 0 || percentile > 100) {
			Log.e("calculatePercentile: 百分位数参数无效: " + percentile);
			return sortedData[0]; // 返回第一个元素作为默认值
		}

		int n = sortedData.length;
		if (n == 0) return 0;
		if (n == 1) return sortedData[0];

		double index = percentile / 100.0 * (n - 1);
		int lowerIndex = (int) Math.floor(index);
		int upperIndex = (int) Math.ceil(index);

		if (lowerIndex == upperIndex) {
			return sortedData[lowerIndex];
		}

		double weight = index - lowerIndex;
		return sortedData[lowerIndex] * (1 - weight) + sortedData[upperIndex] * weight;
	}

	void updateDataPointMap(DataPoint dataPoint) {
		if (dataPoint == null) {
			return;
		}

		if (mDataPointMap.containsKey(dataPoint.period)) {
			if (dataPoint.level < mDataPointMap.get(dataPoint.period).level) {
				return;
			}
		}

		mDataPointMap.put(dataPoint.period, dataPoint);
	}

	public void setupPeriodThumbnail(String period) {
		mStockDataList = mStock.getStockDataList(period, StockTrend.LEVEL_NONE);
		if (mStockDataList.isEmpty()) {
			return;
		}

		for (int i = 0; i < StockTrend.LEVELS.length; i++) {
			if (mXValues[i] != null) {
				mXValues[i].clear();
			} else {
				mXValues[i] = new ArrayList<>();
			}
			if (mYValues[i] != null) {
				mYValues[i].clear();
			} else {
				mYValues[i] = new ArrayList<>();
			}
		}

		mStock.setTrend(period, "");
		for (int index = 0; index < mStockDataList.size(); index++) {
			StockData stockData = mStockDataList.get(index);
			for (int level = StockTrend.LEVEL_DRAW; level < StockTrend.LEVELS.length; level++) {
				if (stockData.vertexOf(StockTrend.getVertexTOP(level))) {
					mXValues[level].add((float) index);
					mYValues[level].add((float) stockData.getCandle().getHigh());
					if (mStock.getLevel(period) == level) {
						mStock.setTrend(period, Symbol.MINUS);
						if (mStock.getPrice() > stockData.getCandle().getHigh()) {
							mStock.setTrend(period, Symbol.ADD);//TODO
						}
					}
				} else if (stockData.vertexOf(StockTrend.getVertexBottom(level))) {
					mXValues[level].add((float) index);
					mYValues[level].add((float) stockData.getCandle().getLow());
					if (mStock.getLevel(period) == level) {
						mStock.setTrend(period, Symbol.ADD);
						if (mStock.getPrice() < stockData.getCandle().getLow()) {
							mStock.setTrend(period, Symbol.MINUS);//TODO
						}
					}
				}
			}
		}

		for (int level = StockTrend.LEVEL_DRAW; level < StockTrend.LEVELS.length; level++) {
			if (mXValues[level] == null || mXValues[level].size() < StockTrend.VERTEX_SIZE) {
				continue;
			}

			if (Setting.getDisplayAdaptive()) {
				if (level > StockTrend.LEVEL_DRAW) {
					int index = mXValues[level].get(mXValues[level].size() - 1).intValue();
					for (int i = 0; i < mXValues[level - 1].size(); i++) {
						if (mXValues[level - 1].get(i).intValue() == index) {
							mXValues[level - 1] = new ArrayList<>(mXValues[level - 1].subList(i, mXValues[level - 1].size()));
							mYValues[level - 1] = new ArrayList<>(mYValues[level - 1].subList(i, mYValues[level - 1].size()));
							break;
						}
					}
				}
			}
		}

		mLineConfigList.clear();
		for (int level = StockTrend.LEVEL_DRAW; level < StockTrend.LEVELS.length; level++) {
			mLineConfigList.add(new CurveThumbnail.LineConfig(mXValues[level], mYValues[level], Config.THUMBNAIL_STROKE_COLOR, Config.THUMBNAIL_STROKE_WIDTH));
		}

		CurveThumbnail.CrossMarkerConfig markerConfig =
				new CurveThumbnail.CrossMarkerConfig(mStockDataList.size() - 1, (float) mStock.getPrice(), TextUtils.equals(mStock.getTrend(period), Symbol.ADD) ? Color.RED : Color.GREEN, Config.THUMBNAIL_MARKER_STROKE_WIDTH, Config.THUMBNAIL_MARKER_SIZE);
		mStock.setPeriodThumbnail(period, Utility.thumbnailToBytes(new CurveThumbnail(Config.THUMBNAIL_SIZE, Color.TRANSPARENT, mLineConfigList, markerConfig)));
	}

	public void setupTrendThumbnail() {
		mLineConfigList.clear();

		if (mKMeansPeriods == 0) {
			return;
		}

		int i = 0;
		int color;

		float totalWidth = Config.THUMBNAIL_WIDTH;
		float segmentWidth = totalWidth / mKMeansPeriods;
		float spacing = segmentWidth * 0.1f;
		float lineWidth = segmentWidth - spacing;

		for (String period : Period.PERIODS) {
			if (Setting.getPeriod(period)) {
				List<Float> xValuesH = new ArrayList<>();
				List<Float> yValuesH = new ArrayList<>();
				List<Float> xValuesP = new ArrayList<>();
				List<Float> yValuesP = new ArrayList<>();
				List<Float> xValuesV = new ArrayList<>();
				List<Float> yValuesV = new ArrayList<>();

				float startX = i * segmentWidth + spacing / 2f;
				float endX = startX + lineWidth;
				float centerY = Config.THUMBNAIL_HEIGHT / 2f;

				xValuesH.add(startX);
				xValuesH.add(endX);
				yValuesH.add(centerY);
				yValuesH.add(centerY);

				xValuesP.add((startX + endX) / 2f);
				xValuesP.add((startX + endX) / 2f);
				yValuesP.add(centerY);
				yValuesP.add(centerY + (float) mStock.getStockTrend(period, mStock.getLevel(period)).getPredict());

				xValuesV.add((startX + endX) / 2f);
				xValuesV.add((startX + endX) / 2f);
				yValuesV.add(centerY);
				yValuesV.add(centerY + (float) mStock.getStockTrend(period, mStock.getLevel(period)).getNextNet());

				if (TextUtils.equals(mStock.getTrend(period), Symbol.ADD)) {
					color = Color.RED;
				} else if (TextUtils.equals(mStock.getTrend(period), Symbol.MINUS)) {
					color = Color.GREEN;
				} else {
					color = Color.BLACK;
				}

				float strokeWidth = Math.max(1, 10f * Config.THUMBNAIL_STROKE_WIDTH);
				mLineConfigList.add(new CurveThumbnail.LineConfig(xValuesH, yValuesH, color, Config.THUMBNAIL_STROKE_WIDTH));
				mLineConfigList.add(new CurveThumbnail.LineConfig(xValuesP, yValuesP, Color.GRAY, strokeWidth));
				mLineConfigList.add(new CurveThumbnail.LineConfig(xValuesV, yValuesV, color, strokeWidth));
				i++;
			}
		}

		int backgroundColor = Color.TRANSPARENT;
		if (mStock.hasFlag(Stock.FLAG_CUSTOM)) {
			backgroundColor = Config.COLOR_BACKGROUND_CUSTOM;
		}

		mStock.setTrendThumbnail(Utility.thumbnailToBytes(
				new CurveThumbnail(Config.THUMBNAIL_WIDTH, Config.THUMBNAIL_HEIGHT,
						backgroundColor, mLineConfigList, null)));
	}

	public void setupComponentThumbnail() {
		float centerX = (float)Config.THUMBNAIL_SIZE / 2f;
		float centerY = (float)Config.THUMBNAIL_SIZE / 2f;

		try {
			mLineConfigList.clear();
			List<Float> xValuesH = new ArrayList<>();
			List<Float> yValuesH = new ArrayList<>();
			List<Float> xValuesV = new ArrayList<>();
			List<Float> yValuesV = new ArrayList<>();

			xValuesH.add(0f);
			xValuesH.add((float)Config.THUMBNAIL_SIZE);
			yValuesH.add(centerY);
			yValuesH.add(centerY);

			xValuesV.add(centerX);
			xValuesV.add(centerX);
			yValuesV.add(0f);
			yValuesV.add((float)Config.THUMBNAIL_SIZE);

			mLineConfigList.add(new CurveThumbnail.LineConfig(xValuesH, yValuesH, Color.BLACK, Config.THUMBNAIL_STROKE_WIDTH));
			mLineConfigList.add(new CurveThumbnail.LineConfig(xValuesV, yValuesV, Color.BLACK, Config.THUMBNAIL_STROKE_WIDTH));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			mScatterConfigList.clear();
			for (String period : Period.PERIODS) {
				if (Setting.getPeriod(period)) {
					PolarComponent polarComponent = mStock.getPolarComponent(period);
					if (polarComponent == null) {
						continue;
					}

					float radius = (float) Config.THUMBNAIL_SIZE / 2f  * 0.85f - Config.THUMBNAIL_SCATTER_SIZE / 2f;

					// 计算角度（使用相位角）
					double angle = polarComponent.phase; // 使用弧度

					// 计算在画布上的坐标
					float x = centerX + (float) (radius * Math.cos(angle));
					float y = centerY + (float) (radius * Math.sin(angle));

					// 根据数值正负选择颜色
					int color = polarComponent.lastPointValue >= 0 ? Color.RED : Color.BLACK;
//
//					Log.d("Component - Period:" + period +
//							" lastPointValue:" + polarComponent.lastPointValue +
//							" Radius:" + radius +
//							" Position:(" + x + "," + y + ")");

					mScatterConfigList.add(new CurveThumbnail.ScatterConfig(
							x, y, color, Config.THUMBNAIL_SCATTER_SIZE));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		int backgroundColor = Color.TRANSPARENT;
		mStock.setComponentThumbnail(Utility.thumbnailToBytes(
				new CurveThumbnail(Config.THUMBNAIL_WIDTH, Config.THUMBNAIL_HEIGHT,
						backgroundColor, mLineConfigList, mScatterConfigList, null)));
	}

	private static class DataPoint implements Clusterable {
		final String period;
		final int level;
		final String start;
		final String end;
		final double[] points;
		int group;
		int past;
		int duration;
		double distance;

		DataPoint(String period, int level, String start, String end, double x, double y, int past, int duration) {
			this.period = period;
			this.level = level;
			this.start = start;
			this.end = end;
			this.points = new double[]{x, y};
			this.past = past;
			this.duration = duration;
		}

		@Override
		public double[] getPoint() {
			return points;
		}

		public void setGroup(int group) {
			this.group = group;
		}

		public void setDistance(double distance) {
			this.distance = distance;
		}

		public double distanceTo(@NonNull Clusterable other) {
			double[] otherPoint = other.getPoint();
			double sum = 0.0;
			for (int i = 0; i < points.length; i++) {
				double diff = points[i] - otherPoint[i];
				sum += diff * diff;
			}
			return Math.sqrt(sum);
		}

		@Override
		public String toString() {
			return Symbol.NEW_LINE + period + "-L" + level + " " + start + "--" + end + ", {" + points[0] + ", " + points[1] + "}" + " group=" + group + " past=" + past + " duration=" + duration;
		}
	}

	private static class Holder {
		private static final TrendAnalyzer INSTANCE = new TrendAnalyzer();
	}
}
