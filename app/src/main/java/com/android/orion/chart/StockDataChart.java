package com.android.orion.chart;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.android.orion.config.Config;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDeal;
import com.android.orion.database.StockTrend;
import com.android.orion.provider.StockPerceptronProvider;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
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
	public ArrayList<LimitLine> mLimitLineList = new ArrayList<>();
	public ArrayList<Entry> mDIFEntryList = new ArrayList<>();
	public ArrayList<Entry> mDEAEntryList = new ArrayList<>();
	public ArrayList<BarEntry> mHistogramEntryList = new ArrayList<>();
	public ArrayList<Entry> mDrawFirstEntryList = new ArrayList<>();
	public ArrayList<Entry> mDrawLastEntryList = new ArrayList<>();
	public List<Entry>[] mTrendEntryList = new List[StockTrend.LEVEL_MAX];
	public List<Entry>[] mGroupEntryList = new List[StockTrend.LEVEL_MAX];
	public CombinedData mCombinedDataMain = new CombinedData(mXValues);
	public CombinedData mCombinedDataSub = new CombinedData(mXValues);
	StockPerceptronProvider mStockPerceptronProvider = StockPerceptronProvider.getInstance();

	Stock mStock;
	String mPeriod;
	int mAdaptiveLevel;
	ArrayMap<String, StockTrend> mStockTrendMap = new ArrayMap<>();

	public StockDataChart(Stock stock, String period) {
		mStock = stock;
		mPeriod = period;
		mAdaptiveLevel = mStock.getLevel(period);

		for (int i = 0; i < StockTrend.LEVEL_MAX; i++) {
			if (mTrendEntryList[i] == null) {
				mTrendEntryList[i] = new ArrayList<>();
			} else {
				mTrendEntryList[i].clear();
			}

			if (mGroupEntryList[i] == null) {
				mGroupEntryList[i] = new ArrayList<>();
			} else {
				mGroupEntryList[i].clear();
			}
		}
	}

	public void setupStockTrendMap(Stock stock, String period, ArrayList<StockTrend> stockTrendList) {
		if (stock == null || stockTrendList == null) {
			return;
		}
		mStock = stock;
		mPeriod = period;
		mAdaptiveLevel = mStock.getLevel(period);

		mStockTrendMap.clear();
		for (StockTrend stockTrend : stockTrendList) {
			if (stockTrend != null) {
				mStockTrendMap.put(stockTrend.getPeriod() + StockTrend.MARK_LEVEL + stockTrend.getLevel(), stockTrend);
			}
		}
	}

	public boolean isOperate() {
		boolean result = false;

		if (mStock == null) {
			return false;
		}

		if (!TextUtils.isEmpty(mStock.getOperate()) && TextUtils.equals(mPeriod, mStock.getOperate())) {
			result = true;
		}

		return result;
	}

	public void setMainChartData() {
		mCombinedDataMain = new CombinedData(mXValues);

		if (Setting.getDisplayCandle()) {
			CandleData candleData = new CandleData(mXValues);
			CandleDataSet candleDataSet = new CandleDataSet(mCandleEntryList, "K");
			candleDataSet.setIncreasingColor(increasingColor(Config.COLOR_RGB_RED));
			candleDataSet.setDecreasingColor(decreasingColor(Config.COLOR_RGB_GREEN));
			candleDataSet.setIncreasingPaintStyle(Paint.Style.FILL);
			candleDataSet.setDecreasingPaintStyle(Paint.Style.FILL);
			candleDataSet.setShadowColorSameAsCandle(true);
			candleDataSet.setAxisDependency(AxisDependency.LEFT);
			candleDataSet.setColor(dataSetColor(Color.RED));
			candleDataSet.setHighLightColor(Color.TRANSPARENT);
			candleDataSet.setDrawTags(true);
			candleData.addDataSet(candleDataSet);
			mCombinedDataMain.setData(candleData);
		}

		LineData lineData = new LineData(mXValues);

		if (Setting.getDisplayAverage()) {
			LineDataSet lineDataSet5 = new LineDataSet(mAverage5EntryList,
					"MA5");
			lineDataSet5.setColor(dataSetColor(Color.WHITE));
			lineDataSet5.setDrawCircles(false);
			lineDataSet5.setAxisDependency(YAxis.AxisDependency.LEFT);
			lineData.addDataSet(lineDataSet5);

			LineDataSet lineDataSet10 = new LineDataSet(mAverage10EntryList,
					"MA10");
			lineDataSet10.setColor(dataSetColor(Color.CYAN));
			lineDataSet10.setDrawCircles(false);
			lineDataSet10.setAxisDependency(YAxis.AxisDependency.LEFT);
			lineData.addDataSet(lineDataSet10);
		}

		addLineDataSet(mDrawFirstEntryList, StockTrend.LABEL_NONE, lineColor(StockTrend.LEVEL_DRAW), false, lineData, false, lineColor(StockTrend.LEVEL_DRAW));
		addLineDataSet(mDrawLastEntryList, StockTrend.LABEL_NONE, lineColor(StockTrend.LEVEL_DRAW), false, lineData, false, lineColor(StockTrend.LEVEL_DRAW));
		if (displayTrend(StockTrend.LEVEL_DRAW)) {
			addLineDataSet(mTrendEntryList, StockTrend.LABEL_DRAW, StockTrend.LEVEL_DRAW, lineData);
		}
		addLineDataSet(mGroupEntryList, StockTrend.LEVEL_DRAW, lineData, fillChanged(StockTrend.LEVEL_DRAW), fillColor(StockTrend.LEVEL_DRAW));

		if (displayTrend(StockTrend.LEVEL_STROKE)) {
			addLineDataSet(mTrendEntryList, StockTrend.LABEL_STROKE, StockTrend.LEVEL_STROKE, lineData);
		}
		addLineDataSet(mGroupEntryList, StockTrend.LEVEL_STROKE, lineData, fillChanged(StockTrend.LEVEL_STROKE), fillColor(StockTrend.LEVEL_STROKE));

		if (displayTrend(StockTrend.LEVEL_SEGMENT)) {
			addLineDataSet(mTrendEntryList, StockTrend.LABEL_SEGMENT, StockTrend.LEVEL_SEGMENT, lineData);
		}
		addLineDataSet(mGroupEntryList, StockTrend.LEVEL_SEGMENT, lineData, fillChanged(StockTrend.LEVEL_SEGMENT), fillColor(StockTrend.LEVEL_SEGMENT));

		if (displayTrend(StockTrend.LEVEL_LINE)) {
			addLineDataSet(mTrendEntryList, StockTrend.LABEL_LINE, StockTrend.LEVEL_LINE, lineData);
		}
		addLineDataSet(mGroupEntryList, StockTrend.LEVEL_LINE, lineData, fillChanged(StockTrend.LEVEL_LINE), fillColor(StockTrend.LEVEL_LINE));

		if (displayTrend(StockTrend.LEVEL_OUT_LINE)) {
			addLineDataSet(mTrendEntryList, StockTrend.LABEL_OUTLINE, StockTrend.LEVEL_OUT_LINE, lineData);
		}
		addLineDataSet(mGroupEntryList, StockTrend.LEVEL_OUT_LINE, lineData, fillChanged(StockTrend.LEVEL_OUT_LINE), fillColor(StockTrend.LEVEL_OUT_LINE));

		if (displayTrend(StockTrend.LEVEL_SUPER_LINE)) {
			addLineDataSet(mTrendEntryList, StockTrend.LABEL_SUPERLINE, StockTrend.LEVEL_SUPER_LINE, lineData);
		}
		addLineDataSet(mGroupEntryList, StockTrend.LEVEL_SUPER_LINE, lineData, fillChanged(StockTrend.LEVEL_SUPER_LINE), fillColor(StockTrend.LEVEL_SUPER_LINE));

		if (displayTrend(StockTrend.LEVEL_TREND_LINE)) {
			addLineDataSet(mTrendEntryList, StockTrend.LABEL_TREND_LINE, StockTrend.LEVEL_TREND_LINE, lineData);
		}
		addLineDataSet(mGroupEntryList, StockTrend.LEVEL_TREND_LINE, lineData, fillChanged(StockTrend.LEVEL_TREND_LINE), fillColor(StockTrend.LEVEL_TREND_LINE));

		mCombinedDataMain.setData(lineData);
	}

	public void setSubChartData() {
		mCombinedDataSub = new CombinedData(mXValues);

		BarData barData = new BarData(mXValues);
		BarDataSet histogramDataSet = new BarDataSet(mHistogramEntryList,
				"Histogram");
		histogramDataSet.setBarSpacePercent(40f);
		histogramDataSet.setIncreasingColor(increasingColor(Config.COLOR_RGB_RED));
		histogramDataSet.setDecreasingColor(decreasingColor(Config.COLOR_RGB_GREEN));
		histogramDataSet.setColor(dataSetColor(Color.RED));
		histogramDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		barData.addDataSet(histogramDataSet);

		LineData lineData = new LineData(mXValues);

		LineDataSet difDataSet = new LineDataSet(mDIFEntryList, "DIF");
		difDataSet.setColor(dataSetColor(Color.YELLOW));
		difDataSet.setDrawCircles(false);
		difDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(difDataSet);

		LineDataSet deaDataSet = new LineDataSet(mDEAEntryList, "DEA");
		deaDataSet.setColor(dataSetColor(Color.WHITE));
		deaDataSet.setDrawCircles(false);
		deaDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(deaDataSet);

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
		addLineDataSet(entryList[level], StockTrend.LABEL_NONE, lineColor(level), true, lineData, drawFilled, fillColor);
	}

	void addLineDataSet(List<Entry> entryList, String label, int lineColor, boolean drawCircle, LineData lineData, boolean drawFilled, int fillColor) {
		if (entryList != null && entryList.size() > 0) {
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
	}

	StockTrend getStockTrend(int level) {
		StockTrend stockTrend = null;
		String key = mPeriod + StockTrend.MARK_LEVEL + level;
		if (mStockTrendMap.containsKey(key)) {
			stockTrend = mStockTrendMap.get(key);
		}
		return stockTrend;
	}

	boolean fillChanged(int level) {
		boolean result = false;
		if (!Setting.getDisplayFilled()) {
			return false;
		}

		StockTrend stockTrend = getStockTrend(level);
		if (stockTrend != null && stockTrend.hasFlag(StockTrend.FLAG_CHANGED)) {
			result = true;
		}
		return result;
	}

	int dataSetColor(int defaultColor) {
		if (Setting.getDisplayMonochrome()) {
			return Color.GRAY;
		}
		return defaultColor;
	}

	int increasingColor(int defaultColor) {
		if (Setting.getDisplayMonochrome()) {
			return Color.GRAY;
		}
		return defaultColor;
	}

	int decreasingColor(int defaultColor) {
		if (Setting.getDisplayMonochrome()) {
			return Color.GRAY;
		}
		return defaultColor;
	}

	int fillColor(int level) {
		if (level < 0 || level >= StockTrend.COLORS.length || Setting.getDisplayMonochrome()) {
			return StockTrend.COLORS[0];
		}
		return StockTrend.COLORS[level];
	}

	int lineColor(int level) {
		if (level < 0 || level >= StockTrend.COLORS.length || Setting.getDisplayMonochrome()) {
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
		return Setting.getDisplayAdaptive() ? level >= mAdaptiveLevel : result;
	}

	public void updateDescription(Stock stock) {
		mDescription.setLength(0);

		if (stock == null) {
			return;
		}

		mDescription.append(stock.getName()).append(" ");
		mDescription.append(mPeriod).append(" ");
		mDescription.append(stock.getPrice()).append("  ");
		if (stock.getNet() > 0) {
			mDescription.append(Constant.MARK_ADD);
		} else if (stock.getNet() < 0) {
			mDescription.append(Constant.MARK_MINUS);
		}
		mDescription.append(stock.getNet()).append("%").append("  ");

		StockTrend stockTrend = getStockTrend(mAdaptiveLevel);
		if (stockTrend != null) {
			mDescription.append(stockTrend.toTrendString());
			mDescription.append(" ? " + mStockPerceptronProvider.getStockPerceptron().predict(stockTrend.getNet1()));
		} else {
			mDescription.append(stock.getAction(mPeriod));
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
		updateCostLimitLine(stock);
		updateDealLimitLine(stock, stockDealList);
	}

	void updateLatestLimitLine(Stock stock) {
		if (stock == null) {
			return;
		}

		if (mLimitLineList == null) {
			return;
		}

		int color = Color.WHITE;
		String action;
		LimitLine limitLine;

		action = stock.getAction(mPeriod);
		if (action.contains(StockTrend.MARK_BUY)) {
			color = Color.MAGENTA;
		} else if (action.contains(StockTrend.MARK_SELL)) {
			color = Color.CYAN;
		}

		StockTrend stockTrend = getStockTrend(mAdaptiveLevel);
		String label = "                                                     " + " ";
		if (stockTrend != null && stockTrend.hasFlag(StockTrend.FLAG_CHANGED)) {
			color = lineColor(mAdaptiveLevel);
			label += "Trend:" + Constant.TAB2 + stockTrend.toTrendString();
			label += " ? " + mStockPerceptronProvider.getStockPerceptron().predict(stockTrend.getNet1());
		} else {
			label += "Action:" + Constant.TAB2 + action;
		}
		limitLine = createLimitLine(stock.getPrice(), color, label);
		mLimitLineList.add(limitLine);
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

			if (stockDeal.getProfit() > 0) {
				color = Color.RED;
			} else {
				color = Color.GREEN;
			}

			if (stockDeal.getVolume() <= 0) {
				color = Color.YELLOW;
			}

			label = "               "
					+ "  " + limit
					+ "  " + stockDeal.getNet() + "%"
					+ "  " + stockDeal.getVolume()
					+ "  " + (int) stockDeal.getProfit()
					+ "  " + stockDeal.getAccount();
			LimitLine limitLineDeal = createLimitLine(limit, color, label);
			if (limitLineDeal != null) {
				mLimitLineList.add(limitLineDeal);
			}
		}
	}

	public void updateGroupEntry() {
		for (int level = StockTrend.LEVEL_DRAW; level < StockTrend.LEVEL_MAX; level++) {
			if (mTrendEntryList[level].size() > 2) {
				if (level == StockTrend.LEVEL_DRAW) {
					mDrawFirstEntryList.add(mTrendEntryList[level].get(0));
					mDrawLastEntryList.add(0, mTrendEntryList[level].get(mTrendEntryList[level].size() - 1));
				}
				mGroupEntryList[level].add(mTrendEntryList[level].get(mTrendEntryList[level].size() - 2));
				mGroupEntryList[level].add(mTrendEntryList[level].get(mTrendEntryList[level].size() - 1));
			}
		}
	}

	public void clear() {
		mXValues.clear();
		mCandleEntryList.clear();
		mAverage5EntryList.clear();
		mAverage10EntryList.clear();
		mDIFEntryList.clear();
		mDEAEntryList.clear();
		mHistogramEntryList.clear();
		mDrawFirstEntryList.clear();
		mDrawLastEntryList.clear();
		for (int i = 0; i < StockTrend.LEVEL_MAX; i++) {
			mTrendEntryList[i].clear();
			mGroupEntryList[i].clear();
		}
	}
}
