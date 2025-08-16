package com.android.orion.analyzer;

import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.android.orion.config.Config;
import com.android.orion.data.Period;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockPerceptron;
import com.android.orion.database.StockTrend;
import com.android.orion.manager.StockDatabaseManager;
import com.android.orion.manager.StockNotificationManager;
import com.android.orion.provider.StockPerceptronProvider;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Utility;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TrendAnalyzer {
	public static String TAG = Config.TAG + TrendAnalyzer.class.getSimpleName();
	public static final int K_MEANS_PERIODS = 5;
	public static final int K_MEANS_LEVELS = StockTrend.LEVELS.length - 1;
	public static final int K_MEANS_DATA_SIZE = K_MEANS_PERIODS * K_MEANS_LEVELS;
	public static final int K_MEANS_MAX_ITERATIONS = 1000;

//	Logger Log = Logger.getLogger();
	Stock mStock;
	String mPeriod;
	List<AppData> mAppDataList = new ArrayList<>();
	ArrayList<StockData> mStockDataList = new ArrayList<>();
	StockDatabaseManager mStockDatabaseManager = StockDatabaseManager.getInstance();
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
			stockTrend.setPredict(stockPerceptron.predict(stockTrend.getNet()));
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
							if (addVertex(prev_end, vertexTypeBottom, vertexList)) {
								addStockDataList(vertexList, dataList);
							}
						} else if (direction == StockTrend.DIRECTION_NONE) {
							StockData vertexData = chooseVertex(prev_start, prev_end, StockTrend.VERTEX_BOTTOM);
							if (baseDirection == StockTrend.DIRECTION_UP) {
								type = StockTrend.TYPE_UP_NONE_UP;
								addStockTrendList(finished, level, type, prev, current, next, stockTrendList);
								if (addVertex(vertexData, vertexTypeBottom, vertexList)) {
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
							if (addVertex(prev_end, vertexTypeTop, vertexList)) {
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
								if (addVertex(vertexData, vertexTypeTop, vertexList)) {
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
							if (addVertex(vertexData, vertexTypeTop, vertexList)) {
								addStockDataList(vertexList, dataList);
							}
						} else if (direction == StockTrend.DIRECTION_DOWN) {
							baseDirection = StockTrend.DIRECTION_DOWN;
							type = StockTrend.TYPE_DOWN_NONE;
							addStockTrendList(finished, level, type, prev, current, next, stockTrendList);
							StockData vertexData = chooseVertex(current_start, current_end, StockTrend.VERTEX_BOTTOM);
							if (addVertex(vertexData, vertexTypeBottom, vertexList)) {
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
		if (vertexList == null || vertexList.isEmpty() || dataList == null) {
			return;
		}

		dataList.clear();

		int size = vertexList.size();
		for (int i = 0; i < size - 1; i++) {
			StockData current = vertexList.get(i);
			StockData next = vertexList.get(i + 1);

			if (current == null || next == null) {
				return;
			}

			StockData stockData = new StockData(current);
			stockData.setIndexStart(current.getIndexStart());
			stockData.setIndexEnd(next.getIndexEnd());
			stockData.merge(StockTrend.DIRECTION_NONE, next);

			int direction = StockTrend.DIRECTION_NONE;
			if (current.vertexOf(StockTrend.VERTEX_TOP)) {
				direction = next.vertexOf(StockTrend.VERTEX_BOTTOM) ? StockTrend.DIRECTION_DOWN : StockTrend.DIRECTION_UP;
			} else if (current.vertexOf(StockTrend.VERTEX_BOTTOM)) {
				direction = next.vertexOf(StockTrend.VERTEX_TOP) ? StockTrend.DIRECTION_UP : StockTrend.DIRECTION_DOWN;
			}
			stockData.setDirection(direction);
			stockData.setupNet();
			dataList.add(stockData);
		}
	}

	public void analyzeAdaptive(String period) {
		int level = StockTrend.LEVEL_DRAW;
		for (int i = StockTrend.LEVELS.length - 1; i > StockTrend.LEVEL_NONE; i--) {
			if (mStock.getDataList(period, i).size() >= StockTrend.ADAPTIVE_SIZE) {
				StockData trendData = StockData.getLast(mStock.getDataList(period, i), 0);
				if (trendData != null) {
					int indexStart = trendData.getIndexStart();
					StockData stockData = mStock.getStockDataList(period).get(indexStart);
					String dateString = stockData.getDate();
					if (TextUtils.isEmpty(mStock.getAdaptiveDate())) {
						mStock.setAdaptiveDate(dateString);
					} else if (Utility.getCalendar(dateString, Utility.CALENDAR_DATE_FORMAT).after(Utility.getCalendar(mStock.getAdaptiveDate(), Utility.CALENDAR_DATE_FORMAT))) {
						mStock.setAdaptiveDate(dateString);
					}
				}
				level = i;
				break;
			}
		}
		mStock.setLevel(period, level);
	}

	public void analyzeAdaptive() {
		mAppDataList.clear();

		for (String period : Period.PERIODS) {
			if (Setting.getPeriod(period)) {
				if (Period.getPeriodIndex(period) < Period.getPeriodIndex(Period.DAY)) {
					continue;
				}

				for (int i = StockTrend.LEVEL_DRAW; i < StockTrend.LEVELS.length; i++) {
					ArrayList<StockData> trendDataList = mStock.getDataList(period, i);
					mAppDataList.add(new AppData(period, i, getTrendDays(trendDataList)));
				}
			}
		}

		if (mAppDataList.size() < K_MEANS_DATA_SIZE) {
			return;
		}

		try {
			KMeansPlusPlusClusterer<AppData> clusterer = new KMeansPlusPlusClusterer<>(StockTrend.LEVELS.length - 1, K_MEANS_MAX_ITERATIONS);
			List<CentroidCluster<AppData>> clusters = clusterer.cluster(mAppDataList);
			List<CentroidCluster<AppData>> sortedClusters = sortClusters(clusters);
			printLog(sortedClusters);
			setupStockTrendLevel(sortedClusters);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	long getTrendDays(ArrayList<StockData> dataList) {
		long result = 0;
		if (dataList == null || dataList.size() < 2) {
			return result;
		}

		StockData startData = dataList.get(dataList.size() - 2);
		StockData endData = dataList.get(dataList.size() - 1);
		Calendar startCalendar = Utility.getCalendar(startData.getDate(), Utility.CALENDAR_DATE_FORMAT);
		Calendar endCalendar = Utility.getCalendar(endData.getDate(), Utility.CALENDAR_DATE_FORMAT);
		result = Utility.daysBetween(startCalendar, endCalendar);

		return result;
	}

	List<CentroidCluster<AppData>> sortClusters(List<CentroidCluster<AppData>> clusters) {
		List<CentroidCluster<AppData>> sorted = new ArrayList<>(clusters);

		Collections.sort(sorted, new Comparator<CentroidCluster<AppData>>() {
			@Override
			public int compare(CentroidCluster<AppData> o1, CentroidCluster<AppData> o2) {
				double val1 = o1.getCenter().getPoint()[0];
				double val2 = o2.getCenter().getPoint()[0];
				if (val1 < val2) return -1;
				if (val1 > val2) return 1;
				return 0;
			}
		});

		return sorted;
	}

	void setupStockTrendLevel(List<CentroidCluster<AppData>> clusters) {
		if (clusters == null || clusters.isEmpty()) {
			return;
		}

		for (int i = 0; i < clusters.size(); i++) {
			CentroidCluster<AppData> cluster = clusters.get(i);
			double centerValue = cluster.getCenter().getPoint()[0];
			if (centerValue < 1 || cluster.getPoints().size() < K_MEANS_PERIODS - 1) {
				continue;
			}

			ArrayMap<String, AppData> appDataMap = new ArrayMap<>();
			for (AppData appData : cluster.getPoints()) {
				double distance = Math.abs(appData.value - centerValue);
				appData.setDistance(distance);
				if (appDataMap.containsKey(appData.period)) {
					if (appData.distance < appDataMap.get(appData.period).distance) {
						appDataMap.put(appData.period, appData);
					} else if (appData.distance == appDataMap.get(appData.period).distance){
						if (appData.level > appDataMap.get(appData.period).level) {
							appDataMap.put(appData.period, appData);
						}
					}
				} else {
					appDataMap.put(appData.period, appData);
				}
			}

			if (appDataMap.size() > 0) {
				for (String period : appDataMap.keySet()) {
					AppData appData = appDataMap.get(period);
					mStock.setLevel(period, appData.level);
				}
				return;
			}
		}
	}

	void printLog(List<CentroidCluster<AppData>> clusters) {
		if (clusters == null) {
			return;
		}
		for (int i = 0; i < clusters.size(); i++) {
			CentroidCluster<AppData> cluster = clusters.get(i);
			StringBuilder clusterInfo = new StringBuilder();
			clusterInfo.append("\n=== 第 ").append(i + 1).append(" 组 ===")
					.append(" | 中心值: ").append(cluster.getCenter().getPoint()[0])
					.append(" | 数量: ").append(cluster.getPoints().size()).append("\n");

			for (AppData app : cluster.getPoints()) {
				clusterInfo.append("  ").append(app.toString())
						.append(" delt: ")
						.append(Math.abs(app.value - cluster.getCenter().getPoint()[0]))
						.append("\n");
			}
			Log.d(TAG, clusterInfo.toString());
		}
	}

	private static class AppData implements Clusterable {
		final String period;
		final int level;
		final double value;
		double distance;

		AppData(String period, int level, double value) {
			this.period = period;
			this.level = level;
			this.value = value;
		}

		@Override
		public double[] getPoint() {
			return new double[]{value};
		}

		public void setDistance(double distance) {
			this.distance = distance;
		}

		@Override
		public String toString() {
			return String.format("%s-L%d=%.0f", period, level, value);
		}
	}

	private static class Holder {
		private static final TrendAnalyzer INSTANCE = new TrendAnalyzer();
	}
}
