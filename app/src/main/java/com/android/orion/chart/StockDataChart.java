package com.android.orion.chart;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.android.orion.config.Config;
import com.android.orion.data.Trend;
import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.android.orion.database.StockTrend;
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

	public static final int LINE_CIRCLE_SIZE = 0;
	public static final int GROUP_CIRCLE_SIZE = 3;

	public StringBuffer mDescription = new StringBuffer();
	public ArrayList<String> mXValues = new ArrayList<>();
	public ArrayList<CandleEntry> mCandleEntryList = new ArrayList<>();
	public ArrayList<Entry> mAverage5EntryList = new ArrayList<>();
	public ArrayList<Entry> mAverage10EntryList = new ArrayList<>();
	public ArrayList<LimitLine> mLimitLineList = new ArrayList<>();
	public ArrayList<Entry> mDIFEntryList = new ArrayList<>();
	public ArrayList<Entry> mDEAEntryList = new ArrayList<>();
	public ArrayList<BarEntry> mHistogramEntryList = new ArrayList<>();
	public ArrayList<Entry> mVelocityEntryList = new ArrayList<>();
	public ArrayList<Entry> mDrawFirstEntryList = new ArrayList<>();
	public ArrayList<Entry> mDrawLastEntryList = new ArrayList<>();
	public List<Entry>[] mTrendEntryList = new List[Trend.LEVEL_MAX];
	public List<Entry>[] mGroupEntryList = new List[Trend.LEVEL_MAX];
	public int[] mLineColors = {Color.WHITE, Color.GRAY, Color.YELLOW, Color.BLACK, Color.RED, Color.MAGENTA, Color.BLUE};
	public int[] mGroupColors = {Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.GRAY};
	public CombinedData mCombinedDataMain = new CombinedData(mXValues);
	public CombinedData mCombinedDataSub = new CombinedData(mXValues);

	Stock mStock;
	String mPeriod;
	ArrayMap<String, StockTrend> mStockTrendMap = new ArrayMap<>();
	double mMainChartYMin = 0;
	double mMainChartYMax = 0;
	double mSubChartYMin = 0;
	double mSubChartYMax = 0;
	boolean mNotifyTrend = false;

	public StockDataChart(Stock stock, String period) {
		mStock = stock;
		mPeriod = period;
		for (int i = 0; i < Trend.LEVEL_MAX; i++) {
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

	public void setupStockTrendMap(Stock stock, ArrayList<StockTrend> stockTrendList) {
		if (stock == null || stockTrendList == null) {
			return;
		}
		mStock = stock;
		mStockTrendMap.clear();
		for (StockTrend stockTrend : stockTrendList) {
			if (stockTrend != null) {
				mStockTrendMap.put(stockTrend.getPeriod() + Trend.MARK_LEVEL + stockTrend.getLevel(), stockTrend);
			}
		}
	}

	public void setNotifyTrend(boolean notifyTrend) {
		mNotifyTrend = notifyTrend;
	}

	public boolean isOperate() {
		boolean result = false;

		if (mStock == null) {
			return result;
		}

		if (!TextUtils.isEmpty(mStock.getOperate()) && TextUtils.equals(mPeriod, mStock.getOperate())) {
			result = true;
		}

		return result;
	}

	public void setMainChartData(Context context) {
		mCombinedDataMain = new CombinedData(mXValues);

		if (mCandleEntryList.size() > 0) {
			CandleData candleData = new CandleData(mXValues);

			CandleDataSet candleDataSet = new CandleDataSet(mCandleEntryList, "K");
			candleDataSet.setDecreasingColor(Config.COLOR_RGB_GREEN);
			candleDataSet.setDecreasingPaintStyle(Paint.Style.FILL);
			candleDataSet.setIncreasingColor(Config.COLOR_RGB_RED);
			candleDataSet.setIncreasingPaintStyle(Paint.Style.FILL);
			candleDataSet.setShadowColorSameAsCandle(true);
			candleDataSet.setAxisDependency(AxisDependency.LEFT);
			candleDataSet.setColor(Color.RED);
			candleDataSet.setHighLightColor(Color.TRANSPARENT);
			candleDataSet.setDrawTags(true);

			candleData.addDataSet(candleDataSet);
			mCombinedDataMain.setData(candleData);
		}

		LineData lineData = new LineData(mXValues);

		if (mAverage5EntryList.size() > 0) {
			LineDataSet lineDataSet = new LineDataSet(mAverage5EntryList,
					"MA5");
			lineDataSet.setColor(Color.WHITE);
			lineDataSet.setDrawCircles(false);
			lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
			lineData.addDataSet(lineDataSet);
		}

		if (mAverage10EntryList.size() > 0) {
			LineDataSet lineDataSet = new LineDataSet(mAverage10EntryList,
					"MA10");
			lineDataSet.setColor(Color.CYAN);
			lineDataSet.setDrawCircles(false);
			lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
			lineData.addDataSet(lineDataSet);
		}

		addLineDataSet(mDrawFirstEntryList, "", mLineColors[Trend.LEVEL_DRAW], lineData, false, mLineColors[Trend.LEVEL_DRAW]);
		addLineDataSet(mDrawLastEntryList, "", mLineColors[Trend.LEVEL_DRAW], lineData, false, mLineColors[Trend.LEVEL_DRAW]);
		if (Setting.getDisplayDraw()) {
			addLineDataSet(mTrendEntryList, Trend.LABEL_DRAW, Trend.LEVEL_DRAW, lineData);
		}
		addLineDataSet(mGroupEntryList, Trend.LABEL_GROUP, Trend.LEVEL_DRAW, lineData, groupFilled(Trend.LEVEL_DRAW), groupFilledColor(Trend.LEVEL_DRAW));

		if (Setting.getDisplayStroke()) {
			addLineDataSet(mTrendEntryList, Trend.LABEL_STROKE, Trend.LEVEL_STROKE, lineData);
		}
		addLineDataSet(mGroupEntryList, Trend.LABEL_GROUP, Trend.LEVEL_STROKE, lineData, groupFilled(Trend.LEVEL_STROKE), groupFilledColor(Trend.LEVEL_STROKE));

		if (Setting.getDisplaySegment()) {
			addLineDataSet(mTrendEntryList, Trend.LABEL_SEGMENT, Trend.LEVEL_SEGMENT, lineData);
		}
		addLineDataSet(mGroupEntryList, Trend.LABEL_GROUP, Trend.LEVEL_SEGMENT, lineData, groupFilled(Trend.LEVEL_SEGMENT), groupFilledColor(Trend.LEVEL_SEGMENT));

		if (Setting.getDisplayLine()) {
			addLineDataSet(mTrendEntryList, Trend.LABEL_LINE, Trend.LEVEL_LINE, lineData);
			addLineDataSet(mTrendEntryList, Trend.LABEL_OUTLINE, Trend.LEVEL_OUTLINE, lineData);
		}
		addLineDataSet(mGroupEntryList, Trend.LABEL_GROUP, Trend.LEVEL_LINE, lineData, groupFilled(Trend.LEVEL_LINE), groupFilledColor(Trend.LEVEL_LINE));
		addLineDataSet(mGroupEntryList, Trend.LABEL_GROUP, Trend.LEVEL_OUTLINE, lineData, groupFilled(Trend.LEVEL_OUTLINE), groupFilledColor(Trend.LEVEL_OUTLINE));

		mCombinedDataMain.setData(lineData);
	}

	public void setSubChartData(Context context) {
		mCombinedDataSub = new CombinedData(mXValues);

		BarData barData = new BarData(mXValues);
		BarDataSet histogramDataSet = new BarDataSet(mHistogramEntryList,
				"Histogram");
		histogramDataSet.setBarSpacePercent(40f);
		histogramDataSet.setIncreasingColor(Config.COLOR_RGB_RED);
		histogramDataSet.setDecreasingColor(Config.COLOR_RGB_GREEN);
		histogramDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		barData.addDataSet(histogramDataSet);

		LineData lineData = new LineData(mXValues);

		LineDataSet difDataSet = new LineDataSet(mDIFEntryList, "DIF");
		difDataSet.setColor(Color.YELLOW);
		difDataSet.setDrawCircles(false);
		difDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(difDataSet);

		LineDataSet deaDataSet = new LineDataSet(mDEAEntryList, "DEA");
		deaDataSet.setColor(Color.WHITE);
		deaDataSet.setDrawCircles(false);
		deaDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(deaDataSet);

//		if (mVelocityEntryList.size() > 0) {
			LineDataSet velocityDataSet = new LineDataSet(mVelocityEntryList, "Velocity");
			velocityDataSet.setColor(Color.BLUE);
			velocityDataSet.setDrawCircles(false);
			velocityDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
			lineData.addDataSet(velocityDataSet);
//		}

		mCombinedDataSub.setData(barData);
		mCombinedDataSub.setData(lineData);
	}

	void addLineDataSet(List<Entry>[]entryList, String label, int level, LineData lineData) {
		addLineDataSet(entryList[level], label, mLineColors[level], lineData, false, 0);
	}

	void addLineDataSet(List<Entry>[]entryList, String label, int level, LineData lineData, boolean drawFilled, int fillColor) {
		addLineDataSet(entryList[level], label, mLineColors[level], lineData, drawFilled, fillColor);
	}

	void addLineDataSet(List<Entry>entryList, String label, int lineColor, LineData lineData, boolean drawFilled, int fillColor) {
		if (entryList != null && entryList.size() > 0) {
			LineDataSet lineDataSet = new LineDataSet(entryList, label);
			lineDataSet.setDrawFilled(drawFilled);
			lineDataSet.setFillColor(fillColor);
			lineDataSet.setColor(lineColor);
			lineDataSet.setCircleColor(lineColor);
			if (drawFilled) {
				lineDataSet.setCircleSize(GROUP_CIRCLE_SIZE);
			} else {
				lineDataSet.setCircleSize(LINE_CIRCLE_SIZE);
			}
			lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
			lineData.addDataSet(lineDataSet);
		}
	}

	StockTrend getStockTrend(int level) {
		StockTrend stockTrend = null;
		String key = mPeriod + Trend.MARK_LEVEL + level;
		if (mStockTrendMap.containsKey(key)) {
			stockTrend = mStockTrendMap.get(key);
		}
		return stockTrend;
	}

	boolean groupFilled(int level) {
		boolean result = false;
		StockTrend stockTrend = getStockTrend(level);
		if (stockTrend != null) {
			result = true;
		}
		return result;
	}

	int groupFilledColor(int level) {
		StockTrend stockTrend = getStockTrend(level);
		if (stockTrend == null) {
			return mGroupColors[Trend.GROUPED_NONE];
		}

		int grouped = stockTrend.getGrouped();
		if (grouped < Trend.GROUPED_NONE) {
			return mGroupColors[Trend.GROUPED_NONE];
		}

		return grouped < mGroupColors.length ? mGroupColors[grouped] : mGroupColors[mGroupColors.length - 1];
	}


	public void setMainChartYMinMax(int index) {
		double draw = 0;
		double stroke = 0;
		double segment = 0;

		List<Entry> drawEntryList = mTrendEntryList[Trend.LEVEL_DRAW];
		List<Entry> strokeEntryList = mTrendEntryList[Trend.LEVEL_STROKE];
		List<Entry> segmentEntryList = mTrendEntryList[Trend.LEVEL_SEGMENT];

		if (drawEntryList == null || drawEntryList.size() == 0) {
			return;
		}

		if (strokeEntryList == null || strokeEntryList.size() == 0) {
			return;
		}

		if (segmentEntryList == null || segmentEntryList.size() == 0) {
			return;
		}

		draw = drawEntryList.get(drawEntryList.size() - 1).getVal();
		stroke = strokeEntryList.get(strokeEntryList.size() - 1).getVal();
		segment = segmentEntryList.get(segmentEntryList.size() - 1).getVal();

		if (index == 0) {
			mMainChartYMin = Math.min(Math.min(draw, stroke), segment);
			mMainChartYMax = Math.max(Math.max(draw, stroke), segment);
		} else {
			mMainChartYMin = Math.min(Math.min(Math.min(draw, stroke), segment), mMainChartYMin);
			mMainChartYMax = Math.max(Math.max(Math.max(draw, stroke), segment), mMainChartYMax);
		}
	}

	public void setupSubChartYMinMax(int index) {
		double dif = 0;
		double dea = 0;

		if (mDIFEntryList == null || mDIFEntryList.size() == 0) {
			return;
		}

		if (mDEAEntryList == null || mDEAEntryList.size() == 0) {
			return;
		}

		dif = mDIFEntryList.get(mDIFEntryList.size() - 1).getVal();
		dea = mDEAEntryList.get(mDEAEntryList.size() - 1).getVal();

		if (index == 0) {
			mSubChartYMin = Math.min(dif, dea);
			mSubChartYMax = Math.max(dif, dea);
		} else {
			mSubChartYMin = Math.min(Math.min(dif, dea), mSubChartYMin);
			mSubChartYMax = Math.max(Math.max(dif, dea), mSubChartYMax);
		}
	}

	public void updateDescription(Stock stock) {
		mDescription.setLength(0);

		if (stock == null) {
			return;
		}

		mDescription.append(stock.getName() + " ");
		mDescription.append(mPeriod + " ");
		mDescription.append(stock.getPrice() + "  ");
		if (stock.getNet() > 0) {
			mDescription.append(Constant.MARK_ADD);
		} else if (stock.getNet() < 0) {
			mDescription.append(Constant.MARK_MINUS);
		}
		mDescription.append(stock.getNet() + "%" + "  ");
		mDescription.append(stock.getAction(mPeriod));
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
		String action = "";
		String label = "";
		LimitLine limitLine;

		action = stock.getAction(mPeriod);

		if (action.contains(Trend.MARK_BUY)) {
			color = Color.MAGENTA;
		} else if (action.contains(Trend.MARK_SELL)) {
			color = Color.CYAN;
		} else if (action.contains(Trend.MARK_LEVEL)) {
			try {
				int index = action.indexOf(Trend.MARK_LEVEL);
				String levelString = action.substring(index + 1, index + 2);
				int level = Integer.parseInt(levelString);
				color = mLineColors[level];
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		label = "                                                     " + " ";
		if (mNotifyTrend) {
			label += "Trend:" + Constant.TAB2 + action;
		} else {
			label += "Action:" + Constant.TAB2 + action;
		}
		limitLine = createLimitLine(stock.getPrice(), color, label);
		mLimitLineList.add(limitLine);
	}

	void updateCostLimitLine(Stock stock) {
		int color = Color.WHITE;
		double cost = 0;
		double net = 0;
		String label = "";
		LimitLine limitLine;

		if (stock == null || mLimitLineList == null) {
			return;
		}

		cost = stock.getCost();

		if ((cost > 0) && (stock.getHold() > 0)) {
			net = Utility.Round2(100 * (stock.getPrice() - cost) / cost);
			color = Color.BLUE;
			label = "                                                     "
					+ " " + cost + " " + net + "%";
			limitLine = createLimitLine(cost, color, label);

			mLimitLineList.add(limitLine);
		}
	}

	void updateDealLimitLine(Stock stock, ArrayList<StockDeal> stockDealList) {
		double limit = 0;
		int color = Color.WHITE;
		String label = "";
		LimitLine limitLineDeal = new LimitLine(0);

		if (stock == null || stockDealList == null || mLimitLineList == null) {
			return;
		}

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

			limitLineDeal = createLimitLine(limit, color, label);

			mLimitLineList.add(limitLineDeal);
		}
	}

	public void updateGroupEntry() {
		for (int level = Trend.LEVEL_DRAW; level < Trend.LEVEL_MAX; level++) {
			if (mTrendEntryList[level].size() > 2) {
				if (level == Trend.LEVEL_DRAW) {
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
		mVelocityEntryList.clear();
		mDrawFirstEntryList.clear();
		mDrawLastEntryList.clear();
		for (int i = 0; i < Trend.LEVEL_MAX; i++) {
			mTrendEntryList[i].clear();
			mGroupEntryList[i].clear();
		}
	}
}
