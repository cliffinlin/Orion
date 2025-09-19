package com.android.orion.analyzer;

import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.android.orion.chart.CurveThumbnail;
import com.android.orion.data.Period;
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
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TrendAnalyzer {
	public static final int K_MEANS_MAX_ITERATIONS = 100;

	public static final int DURATION_MAX = 30;//TODO
	public static final int PAST_MIN = 5;//TODO

	public static final int THUMBNAIL_SIZE = 160;
	public static final int THUMBNAIL_STROKE_WIDTH = 1;
	public static final int THUMBNAIL_STROKE_COLOR = Color.GRAY;
	public static final int THUMBNAIL_MARKER_SIZE = 20;
	public static final int THUMBNAIL_MARKER_STROKE_WIDTH = 5;

	int mKMeansPeriods;
	int mKMeansK;
	Logger Log = Logger.getLogger();
	Stock mStock;
	String mPeriod;
	List<DataPoint> mDataPointList = new ArrayList<>();
	List<Float>[] mXValues = new List[StockTrend.LEVELS.length];
	List<Float>[] mYValues = new List[StockTrend.LEVELS.length];
	ArrayList<CurveThumbnail.LineConfig> mLineConfigList = new ArrayList<>();
	ArrayList<StockData> mStockDataList = new ArrayList<>();
	ConcurrentMap<String, DataPoint> mDataPointMap = new ConcurrentHashMap<>();
	StockDatabaseManager mStockDatabaseManager = StockDatabaseManager.getInstance();
	StockPerceptronProvider mStockPerceptronProvider = StockPerceptronProvider.getInstance();
	DescriptiveStatistics mDescriptiveStatistics = new DescriptiveStatistics();

	private TrendAnalyzer() {
	}

	public static TrendAnalyzer getInstance() {
		return Holder.INSTANCE;
	}

	void setup() {
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
		mStock.setLevel(period, level);
	}

	public void analyzeAdaptive() {
		setup();

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
			List<CentroidCluster<DataPoint>> sortedClusterList = sortClusterList(clusterList);
			setGroupDistance(sortedClusterList);
			setupStockLevel(sortedClusterList);
			for (String period : Period.PERIODS) {
				if (Setting.getPeriod(period)) {
					setupThumbnail(period);
				}
			}
			setupThumbnail();
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

	void setGroupDistance(List<CentroidCluster<DataPoint>> clusterList) {
		if (clusterList == null) {
			return;
		}
		for (int i = 0; i < clusterList.size(); i++) {
			CentroidCluster<DataPoint> cluster = clusterList.get(i);
			StringBuilder clusterInfo = new StringBuilder();
			clusterInfo.append(Symbol.NEW_LINE + "=== 第 ").append(i).append(" 组 ===")
					.append(" | 中心值: ").append(java.util.Arrays.toString(cluster.getCenter().getPoint()))
					.append(" | 数量: ").append(cluster.getPoints().size());

			for (DataPoint point : cluster.getPoints()) {
				point.setGroup(i);
				point.setDistance(point.distanceTo(cluster.getCenter()));
				clusterInfo.append("  ").append(point);
			}
			Log.d(clusterInfo.toString());
		}
	}

	void setupStockLevel(List<CentroidCluster<DataPoint>> clusterList) {
		if (clusterList == null || clusterList.isEmpty()) {
			return;
		}

		mDataPointMap.clear();
		for (CentroidCluster<DataPoint> cluster : clusterList) {
			if (cluster == null) {
				continue;
			}

			List<DataPoint> validList = getValidListList(cluster.getPoints());
			if (validList == null || validList.isEmpty()) {
				continue;
			}

			if (!mDataPointMap.isEmpty()) {
				if (validList.get(0).duration == 0) {//TODO
					if (mDataPointMap.size() == mKMeansPeriods - 1) {
						setupStockLevelAndDuration();
						if (mStock.getPast() > PAST_MIN) {
							return;
						}
					}
				}

				if (validList.get(0).duration > DURATION_MAX) {
					setupStockLevelAndDuration();
					return;
				}

				if (isDirectionChanged((int)validList.get(0).getPoint()[1])) {
					if (mDataPointMap.size() == mKMeansPeriods) {
						setupStockLevelAndDuration();
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

	void setupStockLevelAndDuration() {
		double count = 0;
		double duration = 0;
		double past = 0;
		StringBuilder builder = new StringBuilder();
		for (String period : Period.PERIODS) {
			if (mDataPointMap.get(period) != null) {
				mStock.setLevel(period, mDataPointMap.get(period).level);
				duration += mDataPointMap.get(period).duration;
				past += mDataPointMap.get(period).past;
				count++;
				builder.append(mDataPointMap.get(period).toString());
			}
		}
		mStock.setDuration((count == 0) ?  0 : duration / count);
		mStock.setPast((count == 0) ?  0 : past / count);
		Log.d("setLevel:" + builder.toString());
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

	public void setupThumbnail(String period) {
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
							mStock.setTrend(period, Symbol.ADD);
						}
					}
				} else if (stockData.vertexOf(StockTrend.getVertexBottom(level))) {
					mXValues[level].add((float) index);
					mYValues[level].add((float) stockData.getCandle().getLow());
					if (mStock.getLevel(period) == level) {
						mStock.setTrend(period, Symbol.ADD);
						if (mStock.getPrice() < stockData.getCandle().getLow()) {
							mStock.setTrend(period, Symbol.MINUS);
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
			mLineConfigList.add(new CurveThumbnail.LineConfig(mXValues[level], mYValues[level], THUMBNAIL_STROKE_COLOR, THUMBNAIL_STROKE_WIDTH));
		}

		CurveThumbnail.CrossMarkerConfig markerConfig =
				new CurveThumbnail.CrossMarkerConfig(mStockDataList.size() - 1, (float) mStock.getPrice(), TextUtils.equals(mStock.getTrend(period), Symbol.ADD) ? Color.RED : Color.GREEN, THUMBNAIL_MARKER_STROKE_WIDTH, THUMBNAIL_MARKER_SIZE);
		mStock.setThumbnail(period, Utility.thumbnailToBytes(new CurveThumbnail(THUMBNAIL_SIZE, Color.TRANSPARENT, mLineConfigList, markerConfig)));
	}

	public void setupThumbnail() {
		mLineConfigList.clear();

		if (mKMeansPeriods == 0) {
			return;
		}

		int i = 0;
		int color;
		for (String period : Period.PERIODS) {
			if (Setting.getPeriod(period)) {
				List<Float> xValues = new ArrayList<>();
				List<Float> yValues = new ArrayList<>();
				xValues.add((float)(i * THUMBNAIL_SIZE / mKMeansPeriods));
				xValues.add((float)((i + 1) * THUMBNAIL_SIZE / mKMeansPeriods));
				yValues.add((float)(THUMBNAIL_SIZE / 2.0));
				yValues.add((float)(THUMBNAIL_SIZE / 2.0));
				if (TextUtils.equals(mStock.getTrend(period), Symbol.ADD)) {
					color = Color.RED;
				} else if (TextUtils.equals(mStock.getTrend(period), Symbol.MINUS)) {
					color = Color.GREEN;
				} else {
					color = Color.BLACK;
				}
				mLineConfigList.add(new CurveThumbnail.LineConfig(xValues, yValues, color, 30 * THUMBNAIL_STROKE_WIDTH));
				i++;
			}
		}

		mStock.setThumbnail(Utility.thumbnailToBytes(new CurveThumbnail(THUMBNAIL_SIZE, Color.TRANSPARENT, mLineConfigList, null)));
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
