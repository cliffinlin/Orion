package com.android.orion.analyzer;

import android.graphics.Color;
import android.text.TextUtils;

import com.android.orion.chart.CurveThumbnail;
import com.android.orion.data.Period;
import com.android.orion.data.Radar;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockPerceptron;
import com.android.orion.database.StockTrend;
import com.android.orion.manager.StockDatabaseManager;
import com.android.orion.manager.StockNotificationManager;
import com.android.orion.provider.StockPerceptronProvider;
import com.android.orion.constant.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Symbol;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TrendAnalyzer {
	public static final int THUMBNAIL_SIZE = 160;
	public static final int THUMBNAIL_WIDTH = THUMBNAIL_SIZE;
	public static final int THUMBNAIL_HEIGHT = THUMBNAIL_SIZE;
	public static final int THUMBNAIL_STROKE_WIDTH = 1;
	public static final int THUMBNAIL_ADAPTIVE_BAR_WIDTH = 6 * THUMBNAIL_STROKE_WIDTH;
	public static final int THUMBNAIL_TARGET_BAR_WIDTH = 12 * THUMBNAIL_STROKE_WIDTH;
	public static final int THUMBNAIL_AXIS_WIDTH = 6 * THUMBNAIL_STROKE_WIDTH;
	public static final int THUMBNAIL_STROKE_COLOR = Color.GRAY;
	public static final int THUMBNAIL_MARKER_SIZE = 20;
	public static final int THUMBNAIL_MARKER_STROKE_WIDTH = 5;
	public static final int THUMBNAIL_SCATTER_SIZE = 10;
	public static final int THUMBNAIL_ADAPTIVE_COLOR_UP = Color.MAGENTA;
	public static final int THUMBNAIL_ADAPTIVE_COLOR_DOWN = Color.CYAN;
	public static final int THUMBNAIL_TARGET_COLOR_UP = Color.RED;
	public static final int THUMBNAIL_TARGET_COLOR_DOWN = Color.GREEN;

	int mPeriods;
	Logger Log = Logger.getLogger();
	Stock mStock;
	String mPeriod;
	List<Float>[] mXValues = new List[StockTrend.LEVELS.length];
	List<Float>[] mYValues = new List[StockTrend.LEVELS.length];
	ArrayList<CurveThumbnail.LineConfig> mLineConfigList = new ArrayList<>();
	ArrayList<CurveThumbnail.ScatterConfig> mScatterConfigList = new ArrayList<>();
	ArrayList<CurveThumbnail.CircleConfig> mCircleConfigList = new ArrayList<>();
	ArrayList<StockData> mStockDataList = new ArrayList<>();
	StockDatabaseManager mStockDatabaseManager = StockDatabaseManager.getInstance();
	StockPerceptronProvider mStockPerceptronProvider = StockPerceptronProvider.getInstance();

	private TrendAnalyzer() {
	}

	public static TrendAnalyzer getInstance() {
		return Holder.INSTANCE;
	}

	void setup(Stock stock) {
		mStock = stock;
		mStockDatabaseManager.getStockTrendMap(mStock, mStock.getStockTrendMap());
		mPeriods = 0;
		for (String period : Period.PERIODS) {
			if (Setting.getPeriod(period)) {
				mPeriods++;
			}
		}
	}

	void setup(Stock stock, String period, ArrayList<StockData> stockDataList) {
		mStock = stock;
		mPeriod = period;
		mStockDataList = stockDataList;
	}

	void analyzeVertex(int level) {
		ArrayList<StockData> dataList;
		ArrayList<StockData> vertexList = mStock.getVertexList(mPeriod, level);
		if (mStockDataList == null || mStockDataList.size() < StockTrend.VERTEX_SIZE || vertexList == null) {
			return;
		}

		dataList = new ArrayList<>();
		for (StockData stockData : mStockDataList) {
			stockData.getCandle().setTop(stockData.getCandle().getHigh());
			stockData.getCandle().setBottom(stockData.getCandle().getLow());
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
				mStockDataList.get(i).getCandle().setTop(dataList.get(i).getCandle().getTop());
				mStockDataList.get(i).getCandle().setBottom(dataList.get(i).getCandle().getBottom());

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
			turn = current.getCandle().getTop();
		} else if (current.getDirection() == StockTrend.DIRECTION_DOWN) {
			turn = current.getCandle().getBottom();
		}
		stockTrend.setTurn(turn);

		stockTrend.setPrevNet(prev.getNet());
		stockTrend.setNet(current.getNet());
		stockTrend.setNextNet(next.getNet());
		if (!finished && turn > 0) {
			double nextNet = Utility.Round2(100.0 * (mStock.getPrice() - turn) / turn);
			stockTrend.setNextNet(nextNet);
		}

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
				if (vertex.getCandle().getTop() > current.getCandle().getTop()) {
					vertex.setVertex(StockTrend.VERTEX_TOP);
				}
			} else if (current.vertexOf(StockTrend.VERTEX_BOTTOM)) {
				vertex.setVertex(StockTrend.VERTEX_TOP);
				if (vertex.getCandle().getBottom() < current.getCandle().getBottom()) {
					vertex.setVertex(StockTrend.VERTEX_BOTTOM);
				}
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

	public void setupThumbnail(Stock stock) {
		setup(stock);
		try {
			for (String period : Period.PERIODS) {
				if (Setting.getPeriod(period)) {
					setupPeriodThumbnail(period);
				}
			}
			setupTrendThumbnail();
			setupRadarThumbnail();
		} catch (Exception e) {
			e.printStackTrace();
		}
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

		mStock.setTrend(period, StockTrend.TREND_NONE);
		for (int index = 0; index < mStockDataList.size(); index++) {
			StockData stockData = mStockDataList.get(index);
			for (int level = StockTrend.LEVEL_DRAW; level < StockTrend.LEVELS.length; level++) {
				if (stockData.vertexOf(StockTrend.getVertexTOP(level))) {
					mXValues[level].add((float) index);
					mYValues[level].add((float) stockData.getCandle().getTop());
					if (mStock.getAdaptive(period) == level) {
						mStock.setTrend(period, Symbol.MINUS);
						if (mStock.getPrice() > stockData.getCandle().getTop()) {
							mStock.setTrend(period, Symbol.ADD);//TODO
						}
					}
				} else if (stockData.vertexOf(StockTrend.getVertexBottom(level))) {
					mXValues[level].add((float) index);
					mYValues[level].add((float) stockData.getCandle().getBottom());
					if (mStock.getAdaptive(period) == level) {
						mStock.setTrend(period, Symbol.ADD);
						if (mStock.getPrice() < stockData.getCandle().getBottom()) {
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
			mLineConfigList.add(new CurveThumbnail.LineConfig(mXValues[level], mYValues[level], THUMBNAIL_STROKE_COLOR, THUMBNAIL_STROKE_WIDTH));
		}

		CurveThumbnail.CrossMarkerConfig markerConfig =
				new CurveThumbnail.CrossMarkerConfig(mStockDataList.size() - 1, (float) mStock.getPrice(), TextUtils.equals(mStock.getTrend(period), Symbol.ADD) ? Color.RED : Color.GREEN, THUMBNAIL_MARKER_STROKE_WIDTH, THUMBNAIL_MARKER_SIZE);
		mStock.setPeriodThumbnail(period, Utility.thumbnailToBytes(new CurveThumbnail(THUMBNAIL_SIZE, Color.TRANSPARENT, mLineConfigList, markerConfig)));
	}

	public void setupTrendThumbnail() {
		mLineConfigList.clear();

		if (mPeriods == 0) {
			return;
		}

		int i = 0;
		float totalWidth = THUMBNAIL_WIDTH;
		float segmentWidth = totalWidth / mPeriods;
		float spacing = segmentWidth * 0.1f;
		float lineWidth = segmentWidth - spacing;

		for (String period : Period.PERIODS) {
			if (Setting.getPeriod(period)) {
				float startX = i * segmentWidth + spacing / 2f;
				float endX = startX + lineWidth;
				float centerY = THUMBNAIL_HEIGHT / 2f;
				float centerX = (startX + endX) / 2f;

				int adaptiveLevel = mStock.getAdaptive(period);
				int targetLevel = mStock.getTarget(period);
				StockTrend adaptiveTrend = mStock.getStockTrend(period, adaptiveLevel);
				StockTrend targetTrend = mStock.getStockTrend(period, targetLevel);

				if (targetLevel > StockTrend.LEVEL_NONE) {
					if (targetLevel == adaptiveLevel) {
						drawSingleBar(startX, endX, centerX, centerY, getAxisColor(targetLevel), targetTrend,
								getTargetBarColor(targetTrend.getNextNet()), THUMBNAIL_TARGET_BAR_WIDTH);
					} else {
						drawDoubleBars(startX, endX, centerY, adaptiveTrend, adaptiveLevel,
								targetTrend, targetLevel);
					}
				} else {
					drawSingleBar(startX, endX, centerX, centerY, getAxisColor(adaptiveLevel), adaptiveTrend,
							getAdaptiveBarColor(adaptiveTrend.getNextNet()), THUMBNAIL_ADAPTIVE_BAR_WIDTH);
				}

				i++;
			}
		}

		mStock.setTrendThumbnail(Utility.thumbnailToBytes(
				new CurveThumbnail(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT,
						Color.TRANSPARENT, mLineConfigList, null)));
	}

	private void drawSingleBar(float startX, float endX, float centerX, float centerY, int axisColor,
							   StockTrend trend, int barColor, float barWidth) {
		if (trend == null) {
			return;
		}
		List<Float> xValuesBar = Arrays.asList(centerX, centerX);
		List<Float> yValuesBar = Arrays.asList(centerY, centerY + (float) trend.getNextNet());

		List<Float> xValuesAxis = Arrays.asList(startX, endX);
		List<Float> yValuesAxis = Arrays.asList(centerY, centerY);

		addToLineConfigList(xValuesBar, yValuesBar,
				xValuesAxis, yValuesAxis, barColor, barWidth, axisColor);
	}

	private void drawDoubleBars(float startX, float endX, float centerY,
								StockTrend adaptiveTrend, int adaptiveLevel,
								StockTrend targetTrend, int targetLevel) {
		float lineWidth = endX - startX;

		// 柱子位置（40%和60%位置）
		float adaptiveCenterX = startX + lineWidth * 0.4f;
		float targetCenterX = startX + lineWidth * 0.6f;

		drawDoubleBars(adaptiveCenterX, centerY, getAxisColor(adaptiveLevel),
				startX, adaptiveCenterX + lineWidth * 0.05f, adaptiveTrend, getAdaptiveBarColor(adaptiveTrend.getNextNet()), THUMBNAIL_ADAPTIVE_BAR_WIDTH);

		drawDoubleBars(targetCenterX, centerY, getAxisColor(targetLevel),
				targetCenterX - lineWidth * 0.05f, endX, targetTrend, getTargetBarColor(targetTrend.getNextNet()), THUMBNAIL_TARGET_BAR_WIDTH);
	}

	private void drawDoubleBars(float centerX, float centerY, int axisColor,
								float axisStartX, float axisEndX, StockTrend trend, int barColor, float barWidth) {
		if (trend == null) {
			return;
		}
		List<Float> xValuesBar = Arrays.asList(centerX, centerX);
		List<Float> yValuesBar = Arrays.asList(centerY, centerY + (float) trend.getNextNet());

		List<Float> xValuesAxis = Arrays.asList(axisStartX, axisEndX);
		List<Float> yValuesAxis = Arrays.asList(centerY, centerY);

		addToLineConfigList(xValuesBar, yValuesBar,
				xValuesAxis, yValuesAxis, barColor, barWidth, axisColor);
	}

	private void addToLineConfigList(List<Float> xValuesBar, List<Float> yValuesBar,
									 List<Float> xValuesAxis, List<Float> yValuesAxis,
									 int barColor, float barWidth, int axisColor) {
		mLineConfigList.add(new CurveThumbnail.LineConfig(
				xValuesBar, yValuesBar, barColor, barWidth));

		mLineConfigList.add(new CurveThumbnail.LineConfig(
				xValuesAxis, yValuesAxis, axisColor, THUMBNAIL_AXIS_WIDTH));
	}

	private int getAxisColor(int level) {
		return StockTrend.getColor(level);
	}

	private int getAdaptiveBarColor(double value) {
		if (value > 0) {
			return THUMBNAIL_ADAPTIVE_COLOR_UP;
		} else {
			return THUMBNAIL_ADAPTIVE_COLOR_DOWN;
		}
	}

	private int getTargetBarColor(double value) {
		if (value > 0) {
			return THUMBNAIL_TARGET_COLOR_UP;
		} else {
			return THUMBNAIL_TARGET_COLOR_DOWN;
		}
	}

	public void setupRadarThumbnail() {
		mLineConfigList.clear();
		mScatterConfigList.clear();
		mCircleConfigList.clear();

		setupBaseLines();

		for (String period : Period.PERIODS) {
			if (Setting.getPeriod(period)) {
				setupRadarPoint(mStock.getAdaptiveRadar(period), period, THUMBNAIL_ADAPTIVE_COLOR_UP, THUMBNAIL_ADAPTIVE_COLOR_DOWN);
				setupRadarPoint(mStock.getTargetRadar(period), period, THUMBNAIL_TARGET_COLOR_UP, THUMBNAIL_TARGET_COLOR_DOWN);
			}
		}

		CurveThumbnail thumbnail = new CurveThumbnail(
				THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Color.TRANSPARENT,
				mLineConfigList, mScatterConfigList, mCircleConfigList, null
		);

		byte[] thumbnailBytes = Utility.thumbnailToBytes(thumbnail);
		mStock.setRadarThumbnail(thumbnailBytes);
	}

	private void setupBaseLines() {
		float centerX = (float) THUMBNAIL_SIZE / 2f;
		float centerY = (float) THUMBNAIL_SIZE / 2f;

		mLineConfigList.add(createLineConfig(
				Arrays.asList(0f, (float) THUMBNAIL_SIZE),
				Arrays.asList(centerY, centerY),
				Color.BLACK, THUMBNAIL_STROKE_WIDTH
		));

		mLineConfigList.add(createLineConfig(
				Arrays.asList(centerX, centerX),
				Arrays.asList(0f, (float) THUMBNAIL_SIZE),
				Color.BLACK, THUMBNAIL_STROKE_WIDTH
		));

		mScatterConfigList.add(new CurveThumbnail.ScatterConfig(
				centerX, centerY, Color.LTGRAY, THUMBNAIL_SCATTER_SIZE
		));

		mCircleConfigList.add(new CurveThumbnail.CircleConfig(centerX, centerY, Color.BLACK, THUMBNAIL_SIZE / 4.5f, THUMBNAIL_STROKE_WIDTH));
		mCircleConfigList.add(new CurveThumbnail.CircleConfig(centerX, centerY, Color.BLACK, THUMBNAIL_SIZE / 2.5f, THUMBNAIL_STROKE_WIDTH));
	}

	private void setupRadarPoint(Radar radar, String period, int upColor, int downColor) {
		if (radar == null) {
			return;
		}

		float centerX = (float) THUMBNAIL_SIZE / 2f;
		float centerY = (float) THUMBNAIL_SIZE / 2f;
		float radius;
		final float lineRadius = THUMBNAIL_SIZE / 5f;
		final float strokeWidth = 4f * THUMBNAIL_STROKE_WIDTH;

		radius = (float) (THUMBNAIL_SIZE / 2f * Math.abs(radar.amplitude));
		double angle = radar.phase;
		float x = centerX + (float) (radius * Math.cos(angle));
		float y = centerY + (float) (radius * Math.sin(angle));

		int color = (radar.direction == StockTrend.DIRECTION_UP) ? upColor : downColor;

		mScatterConfigList.add(new CurveThumbnail.ScatterConfig(
				x, y, color, THUMBNAIL_SCATTER_SIZE
		));

		switch (period) {
			case Period.DAY:
				addRadialLine(x, y, -lineRadius, 0, color, strokeWidth);
				break;
			case Period.MIN60:
				addRadialLine(x, y, 0, lineRadius, color, strokeWidth);
				break;
			case Period.MIN30:
				addRadialLine(x, y, 0, -lineRadius, color, strokeWidth);
				break;
			case Period.MIN15:
				addRadialLine(x, y, lineRadius, 0, color, strokeWidth);
				break;
			case Period.MIN5:
				double angleMin5 = Math.toRadians((float) Constant.MIN5 / (float) Constant.MIN60 * 360f);
				float deltaX = (float) (lineRadius * Math.sin(angleMin5));
				float deltaY = (float) (lineRadius * Math.cos(angleMin5));
				addRadialLine(x, y, deltaX, deltaY, color, strokeWidth);
				break;
		}
	}

	private void addRadialLine(float startX, float startY, float deltaX, float deltaY, int color, float strokeWidth) {
		mLineConfigList.add(createLineConfig(
				Arrays.asList(startX, startX + deltaX),
				Arrays.asList(startY, startY + deltaY),
				color, strokeWidth
		));
	}

	private CurveThumbnail.LineConfig createLineConfig(List<Float> xValues, List<Float> yValues,
													   int color, float strokeWidth) {
		return new CurveThumbnail.LineConfig(xValues, yValues, color, strokeWidth);
	}

	private static class Holder {
		private static final TrendAnalyzer INSTANCE = new TrendAnalyzer();
	}
}