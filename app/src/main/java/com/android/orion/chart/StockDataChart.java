package com.android.orion.chart;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.android.orion.config.Config;
import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.android.orion.database.StockTrend;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Symbol;
import com.android.orion.utility.Utility;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class StockDataChart {

	public static final int NONE_CIRCLE_SIZE = 0;
	public static final int TREND_CIRCLE_SIZE = 3;

	public StringBuffer mDescription = new StringBuffer();
	public ArrayList<String> mXValues = new ArrayList<>();
	public ArrayList<CandleEntry> mCandleEntryList = new ArrayList<>();
	public ArrayList<Entry> mAverage5EntryList = new ArrayList<>();
	public ArrayList<Entry> mAverage10EntryList = new ArrayList<>();
	public ArrayList<Integer> mDrawVertexList = new ArrayList<>();
	public ArrayList<Entry> mExtendFirstEntryList = new ArrayList<>();
	public ArrayList<Entry> mExtendLastEntryList = new ArrayList<>();
	public ArrayList<LimitLine> mLimitLineList = new ArrayList<>();
	public ArrayList<Entry> mDIFEntryList = new ArrayList<>();
	public ArrayList<Entry> mDEAEntryList = new ArrayList<>();
	public ArrayList<BarEntry> mHistogramEntryList = new ArrayList<>();
	public ArrayList<Entry> mRadarEntryList = new ArrayList<>();
	public List<Entry>[] mTrendEntryList = new List[StockTrend.LEVELS.length];
	public List<Entry>[] mChangedEntryList = new List[StockTrend.LEVELS.length];
	public CombinedData mCombinedDataMain = new CombinedData(mXValues);
	public CombinedData mCombinedDataSub = new CombinedData(mXValues);

	Stock mStock;
	String mPeriod;
	int mAdaptiveLevel;
	ArrayMap<String, StockTrend> mStockTrendMap;

	public StockDataChart(Stock stock, String period) {
		mStock = stock;
		mPeriod = period;
		mAdaptiveLevel = mStock.getLevel(period);

		for (int i = 0; i < StockTrend.LEVELS.length; i++) {
			if (mTrendEntryList[i] == null) {
				mTrendEntryList[i] = new ArrayList<>();
			} else {
				mTrendEntryList[i].clear();
			}

			if (mChangedEntryList[i] == null) {
				mChangedEntryList[i] = new ArrayList<>();
			} else {
				mChangedEntryList[i].clear();
			}
		}
	}

	public void setupStockTrendMap(Stock stock, String period, ArrayMap<String, StockTrend> stockTrendMap) {
		if (stock == null || stockTrendMap == null) {
			return;
		}
		mStock = stock;
		mPeriod = period;
		mAdaptiveLevel = mStock.getLevel(period);
		mStockTrendMap = stockTrendMap;
	}

	public void setMainChartData() {
		mCombinedDataMain = new CombinedData(mXValues);

		if (Setting.getDisplayCandle()) {
			CandleData candleData = new CandleData(mXValues);
			CandleDataSet candleDataSet = new CandleDataSet(mCandleEntryList, "K");
			candleDataSet.setIncreasingColor(Config.COLOR_INCREASING);
			candleDataSet.setDecreasingColor(Config.COLOR_DECREASING);
			candleDataSet.setIncreasingPaintStyle(Paint.Style.FILL);
			candleDataSet.setDecreasingPaintStyle(Paint.Style.FILL);
			candleDataSet.setShadowColorSameAsCandle(true);
			candleDataSet.setAxisDependency(AxisDependency.LEFT);
			candleDataSet.setColor(Config.COLOR_CANDLE);
			candleDataSet.setHighLightColor(Color.TRANSPARENT);
			candleDataSet.setDrawTags(true);
			candleData.addDataSet(candleDataSet);
			mCombinedDataMain.setData(candleData);
		}

		LineData lineData = new LineData(mXValues);

		if (Setting.getDisplayAverage()) {
			LineDataSet lineDataSet5 = new LineDataSet(mAverage5EntryList,
					"MA5");
			lineDataSet5.setColor(Config.COLOR_MA5);
			lineDataSet5.setDrawCircles(false);
			lineDataSet5.setAxisDependency(YAxis.AxisDependency.LEFT);
			lineData.addDataSet(lineDataSet5);

			LineDataSet lineDataSet10 = new LineDataSet(mAverage10EntryList,
					"MA10");
			lineDataSet10.setColor(Config.COLOR_MA10);
			lineDataSet10.setDrawCircles(false);
			lineDataSet10.setAxisDependency(YAxis.AxisDependency.LEFT);
			lineData.addDataSet(lineDataSet10);
		}

		addLineDataSet(mTrendEntryList, StockTrend.LABEL_DRAW, StockTrend.LEVEL_DRAW, lineData);
		if (displayTrend(StockTrend.LEVEL_DRAW)) {
			addLineDataSet(mChangedEntryList, StockTrend.LEVEL_DRAW, lineData, fillChanged(StockTrend.LEVEL_DRAW), fillColor(StockTrend.LEVEL_DRAW));
		}
		addLineDataSet(mExtendFirstEntryList, StockTrend.LABEL_NONE, lineColor(StockTrend.LEVEL_DRAW), false, lineData, false, 0);
		addLineDataSet(mExtendLastEntryList, StockTrend.LABEL_NONE, lineColor(StockTrend.LEVEL_DRAW), false, lineData, false, 0);

		if (displayTrend(StockTrend.LEVEL_STROKE)) {
			addLineDataSet(mTrendEntryList, StockTrend.LABEL_STROKE, StockTrend.LEVEL_STROKE, lineData);
			addLineDataSet(mChangedEntryList, StockTrend.LEVEL_STROKE, lineData, fillChanged(StockTrend.LEVEL_STROKE), fillColor(StockTrend.LEVEL_STROKE));
		}

		if (displayTrend(StockTrend.LEVEL_SEGMENT)) {
			addLineDataSet(mTrendEntryList, StockTrend.LABEL_SEGMENT, StockTrend.LEVEL_SEGMENT, lineData);
			addLineDataSet(mChangedEntryList, StockTrend.LEVEL_SEGMENT, lineData, fillChanged(StockTrend.LEVEL_SEGMENT), fillColor(StockTrend.LEVEL_SEGMENT));
		}

		if (displayTrend(StockTrend.LEVEL_LINE)) {
			addLineDataSet(mTrendEntryList, StockTrend.LABEL_LINE, StockTrend.LEVEL_LINE, lineData);
			addLineDataSet(mChangedEntryList, StockTrend.LEVEL_LINE, lineData, fillChanged(StockTrend.LEVEL_LINE), fillColor(StockTrend.LEVEL_LINE));
		}

		if (displayTrend(StockTrend.LEVEL_OUT_LINE)) {
			addLineDataSet(mTrendEntryList, StockTrend.LABEL_OUTLINE, StockTrend.LEVEL_OUT_LINE, lineData);
			addLineDataSet(mChangedEntryList, StockTrend.LEVEL_OUT_LINE, lineData, fillChanged(StockTrend.LEVEL_OUT_LINE), fillColor(StockTrend.LEVEL_OUT_LINE));
		}

		if (displayTrend(StockTrend.LEVEL_SUPER_LINE)) {
			addLineDataSet(mTrendEntryList, StockTrend.LABEL_SUPERLINE, StockTrend.LEVEL_SUPER_LINE, lineData);
			addLineDataSet(mChangedEntryList, StockTrend.LEVEL_SUPER_LINE, lineData, fillChanged(StockTrend.LEVEL_SUPER_LINE), fillColor(StockTrend.LEVEL_SUPER_LINE));
		}

		if (displayTrend(StockTrend.LEVEL_TREND_LINE)) {
			addLineDataSet(mTrendEntryList, StockTrend.LABEL_TREND_LINE, StockTrend.LEVEL_TREND_LINE, lineData);
			addLineDataSet(mChangedEntryList, StockTrend.LEVEL_TREND_LINE, lineData, fillChanged(StockTrend.LEVEL_TREND_LINE), fillColor(StockTrend.LEVEL_TREND_LINE));
		}

		mCombinedDataMain.setData(lineData);
	}

	public void setSubChartData() {
		mCombinedDataSub = new CombinedData(mXValues);

		BarData barData = new BarData(mXValues);
		BarDataSet histogramDataSet = new BarDataSet(mHistogramEntryList,
				"Histogram");
		histogramDataSet.setBarSpacePercent(40f);
		histogramDataSet.setIncreasingColor(Config.COLOR_INCREASING);
		histogramDataSet.setDecreasingColor(Config.COLOR_DECREASING);
		histogramDataSet.setColor(Config.COLOR_HISTOGRAM);
		histogramDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		barData.addDataSet(histogramDataSet);

		LineData lineData = new LineData(mXValues);

		LineDataSet difDataSet = new LineDataSet(mDIFEntryList, "DIF");
		difDataSet.setColor(Config.COLOR_DIF);
		difDataSet.setDrawCircles(false);
		difDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(difDataSet);

		LineDataSet deaDataSet = new LineDataSet(mDEAEntryList, "DEA");
		deaDataSet.setColor(Config.COLOR_DEA);
		deaDataSet.setDrawCircles(false);
		deaDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(deaDataSet);

		LineDataSet radarDataSet = new LineDataSet(mRadarEntryList, "Radar");
		radarDataSet.setColor(lineColor(mAdaptiveLevel));
		radarDataSet.setDrawCircles(false);
		radarDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(radarDataSet);

		mCombinedDataSub.setData(barData);
		mCombinedDataSub.setData(lineData);
	}

	void addLineDataSet(List<Entry>[] entryList, String label, int level, LineData lineData) {
		if (entryList == null || entryList[level] == null || entryList[level].size() == 0) {
			return;
		}
		addLineDataSet(entryList[level], label, lineColor(level), false, lineData, false, 0);
	}

	void addLineDataSet(List<Entry>[] entryList, int level, LineData lineData, boolean drawFilled, int fillColor) {
		if (entryList == null || entryList[level] == null || entryList[level].size() == 0) {
			return;
		}
		addLineDataSet(entryList[level], StockTrend.LABEL_NONE, lineColor(level), false, lineData, drawFilled, fillColor);
	}

	void addLineDataSet(List<Entry> entryList, String label, int lineColor, boolean drawCircle, LineData lineData, boolean drawFilled, int fillColor) {
		if (entryList == null || entryList.isEmpty()) {
			return;
		}
		LineDataSet lineDataSet = new LineDataSet(entryList, label);
		lineDataSet.setColor(lineColor);
		lineDataSet.setCircleColor(lineColor);
		if (drawCircle) {
			lineDataSet.setCircleSize(TREND_CIRCLE_SIZE);
		} else {
			lineDataSet.setCircleSize(NONE_CIRCLE_SIZE);
		}
		lineDataSet.setDrawFilled(drawFilled);
		lineDataSet.setFillColor(fillColor);
		lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(lineDataSet);
	}

	boolean fillChanged(int level) {
		boolean result = false;
		StockTrend stockTrend = mStock.getStockTrend(mPeriod, level);
		if (stockTrend != null && stockTrend.hasFlag(StockTrend.FLAG_CHANGED)) {
			result = true;
		}
		return result;
	}


	int fillColor(int level) {
		if (level < 0 || level >= StockTrend.COLORS.length) {
			return StockTrend.COLORS[0];
		}
		return StockTrend.COLORS[level];
	}

	int lineColor(int level) {
		if (level < 0 || level >= StockTrend.COLORS.length) {
			return StockTrend.COLORS[0];
		}
		return StockTrend.COLORS[level];
	}

	public boolean displayTrend(int level) {
		boolean result = false;
		switch (level) {
			case StockTrend.LEVEL_DRAW:
				result = Setting.getDisplayDraw();
				break;
			case StockTrend.LEVEL_STROKE:
				result = Setting.getDisplayStroke();
				break;
			case StockTrend.LEVEL_SEGMENT:
				result = Setting.getDisplaySegment();
				break;
			case StockTrend.LEVEL_LINE:
				result = Setting.getDisplayLine();
				break;
			case StockTrend.LEVEL_OUT_LINE:
				result = Setting.getDisplayOutLine();
				break;
			case StockTrend.LEVEL_SUPER_LINE:
				result = Setting.getDisplaySuperLine();
				break;
			case StockTrend.LEVEL_TREND_LINE:
				result = Setting.getDisplayTrendLine();
				break;
			default:
				break;
		}

		if (Setting.getDisplayAdaptive()) {
			if (mStock.hasFlag(Stock.FLAG_CUSTOM)) {
				result = (level == mAdaptiveLevel || level == mAdaptiveLevel + 1);
			} else {
				result = (level >= mAdaptiveLevel);
			}
		}

		return result;
	}

	public void updateDescription(Stock stock) {
		if (stock == null) {
			return;
		}

		mDescription.setLength(0);
		mDescription.append(mPeriod).append(" ");
		mDescription.append(stock.getNamePriceNetString(" ")).append(" ");
		StockTrend stockTrend = mStock.getStockTrend(mPeriod, mAdaptiveLevel);
		if (stockTrend != null) {
			mDescription.append(stockTrend.toChartString());
		}
	}

	LimitLine createLimitLine(double limit, int color, String label) {
		LimitLine limitLine = new LimitLine(0);

		limitLine.enableDashedLine(10f, 10f, 0f);
		limitLine.setLineWidth(3);
		limitLine.setTextSize(10f);
		limitLine.setLabelPosition(LimitLabelPosition.LEFT_TOP);
		limitLine.setLimit((float) limit);
		limitLine.setLineColor(color);
		limitLine.setLabel(label);

		return limitLine;
	}

	public void updateLimitLines(Stock stock, ArrayList<StockDeal> stockDealList) {
		if (stock == null || mLimitLineList == null) {
			return;
		}

		mLimitLineList.clear();

		updateLatestLimitLine(stock);
		updateTrendLimitLine(stock);
		updateCostLimitLine(stock);
		updateDealLimitLine(stock, stockDealList);
	}

	void updateLatestLimitLine(Stock stock) {
		if (stock == null || mLimitLineList == null) {
			return;
		}

		LimitLine limitLine;
		int color = Color.WHITE;
		String label = "                                                     ";
		label += stock.getPriceNetString(" ");
		limitLine = createLimitLine(stock.getPrice(), color, label);
		mLimitLineList.add(limitLine);
	}

	void updateTrendLimitLine(Stock stock) {
		if (stock == null || mLimitLineList == null) {
			return;
		}

		ArrayMap<Double, LimitLine> limitLineMap = new ArrayMap<>();
		for (int i = StockTrend.LEVEL_DRAW; i < StockTrend.LEVELS.length; i++) {
			if (Setting.getDisplayAdaptive()) {
				if (i != mAdaptiveLevel) {
					continue;
				}
			}

			StockTrend stockTrend = mStock.getStockTrend(mPeriod, i);
			if (stockTrend == null) {
				continue;
			}

			double turn = stockTrend.getTurn();
			if (limitLineMap.containsKey(turn)) {
				LimitLine limitLine = limitLineMap.get(turn);
				if (limitLine != null) {
					limitLine.setLabel(limitLine.getLabel() + Symbol.TAB2 + stockTrend.toChartString());
				}
			} else {
				int color = lineColor(i);
				String label = "              " + stockTrend.getTurn() + Symbol.TAB2 + stockTrend.toChartString();
				LimitLine limitLine = createLimitLine(stockTrend.getTurn(), color, label);
				limitLineMap.put(turn, limitLine);
			}
		}
		if (limitLineMap.size() > 0) {
			mLimitLineList.addAll(limitLineMap.values());
		}
	}

	void updateCostLimitLine(Stock stock) {
		if (stock == null || mLimitLineList == null) {
			return;
		}

		double cost = stock.getCost();
		if ((cost > 0) && (stock.getHold() > 0)) {
			double net = Utility.Round2(100 * (stock.getPrice() - cost) / cost);
			int color = Color.BLUE;
			String label = "                                                     "
					+ " " + cost + " " + net + "%";
			LimitLine limitLine = createLimitLine(cost, color, label);
			mLimitLineList.add(limitLine);
		}
	}

	void updateDealLimitLine(Stock stock, ArrayList<StockDeal> stockDealList) {
		if (stock == null || stockDealList == null || mLimitLineList == null) {
			return;
		}

		double limit = 0;
		int color;
		String label;
		for (StockDeal stockDeal : stockDealList) {
			if ((stockDeal.getBuy() > 0) && (stockDeal.getSell() > 0)) {
				limit = stockDeal.getBuy();
			} else if (stockDeal.getBuy() > 0) {
				limit = stockDeal.getBuy();
			} else if (stockDeal.getSell() > 0) {
				limit = stockDeal.getSell();
			}

			if (TextUtils.equals(stockDeal.getType(), StockDeal.TYPE_BUY)) {
				color = stockDeal.getProfit() > 0 ? Color.RED : Color.GREEN;
			} else if (TextUtils.equals(stockDeal.getType(), StockDeal.TYPE_SELL)) {
				color = stockDeal.getProfit() > 0 ? Color.GREEN : Color.RED;
			} else {
				color = Color.BLACK;
			}

			label = "               "
					+ "  " + limit
					+ "  " + stockDeal.getNet() + "%"
					+ "  " + stockDeal.getVolume()
					+ "  " + (int) stockDeal.getProfit()
					+ "  " + stockDeal.getAccount()
					+ "  " + (TextUtils.equals(stockDeal.getType(), StockDeal.TYPE_SELL) ? stockDeal.getType() : StockDeal.TYPE_BUY);
			LimitLine limitLine = createLimitLine(limit, color, label);
			if (limitLine != null) {
				mLimitLineList.add(limitLine);
			}
		}
	}

	public void updateExtendEntry() {
		List<Entry> drawLevelEntries = mTrendEntryList[StockTrend.LEVEL_DRAW];

		if (drawLevelEntries == null || drawLevelEntries.isEmpty()) {
			return;
		}

		int firstIndex = 0;
		int lastIndex = mXValues.size() - 1;
		int lastEntryIndex = drawLevelEntries.size() - 1;

		Entry firstEntry = createExtendedEntry(firstIndex, mDrawVertexList.get(firstIndex));
		mExtendFirstEntryList.add(firstEntry);
		mExtendFirstEntryList.add(drawLevelEntries.get(firstIndex));

		mExtendLastEntryList.add(drawLevelEntries.get(lastEntryIndex));
		Entry lastEntry = createExtendedEntry(lastIndex, mDrawVertexList.get(mDrawVertexList.size() - 1));
		mExtendLastEntryList.add(lastEntry);
	}

	private Entry createExtendedEntry(int index, int vertexType) {
		double value;
		if (vertexType == StockTrend.VERTEX_TOP) {
			value = mCandleEntryList.get(index).getLow();
		} else {
			value = mCandleEntryList.get(index).getHigh();
		}
		return new Entry((float) value, index);
	}

	public void updateChangedEntry() {
		for (int level = StockTrend.LEVEL_DRAW; level < StockTrend.LEVELS.length; level++) {
			if (mTrendEntryList[level] == null || mTrendEntryList[level].size() < StockTrend.VERTEX_SIZE) {
				continue;
			}

			mChangedEntryList[level].add(mTrendEntryList[level].get(mTrendEntryList[level].size() - 2));
			mChangedEntryList[level].add(mTrendEntryList[level].get(mTrendEntryList[level].size() - 1));

			if (Setting.getDisplayAdaptive()) {
				if (mStock.hasFlag(Stock.FLAG_CUSTOM)) {
				} else {
					if (level > StockTrend.LEVEL_STROKE) {
						trendEntryListSubList(level);
					}
				}
			}
		}
	}

	void trendEntryListSubList(int level) {
		int index = mTrendEntryList[level].get(mTrendEntryList[level].size() - 1).getXIndex();
		for (int i = 0; i < mTrendEntryList[level - 1].size(); i++) {
			if (mTrendEntryList[level - 1].get(i).getXIndex() == index) {
				mTrendEntryList[level - 1] = new ArrayList<>(mTrendEntryList[level - 1].subList(i, mTrendEntryList[level - 1].size()));
				break;
			}
		}
	}

	public void clear() {
		mXValues.clear();
		mCandleEntryList.clear();
		mDrawVertexList.clear();
		mExtendFirstEntryList.clear();
		mExtendLastEntryList.clear();
		mAverage5EntryList.clear();
		mAverage10EntryList.clear();
		mDIFEntryList.clear();
		mDEAEntryList.clear();
		mHistogramEntryList.clear();
		mRadarEntryList.clear();
		for (int i = 0; i < StockTrend.LEVELS.length; i++) {
			if (mTrendEntryList[i] != null) {
				mTrendEntryList[i].clear();
			}
			if (mChangedEntryList[i] != null) {
				mChangedEntryList[i].clear();
			}
		}
	}
}
