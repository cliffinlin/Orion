package com.android.orion.chart;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import com.android.orion.data.Trend;
import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Utility;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
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
	public StringBuffer mDescription = new StringBuffer();
	public ArrayList<String> mXValues = new ArrayList<>();
	public ArrayList<BubbleEntry> mNaturalRallyList = new ArrayList<>();
	public ArrayList<BubbleEntry> mUpwardTrendList = new ArrayList<>();
	public ArrayList<BubbleEntry> mDownwardTrendList = new ArrayList<>();
	public ArrayList<BubbleEntry> mNaturalReactionList = new ArrayList<>();
	public ArrayList<CandleEntry> mCandleEntryList = new ArrayList<>();
	public ArrayList<Entry> mAverage5EntryList = new ArrayList<>();
	public ArrayList<Entry> mAverage10EntryList = new ArrayList<>();
	public ArrayList<Entry> mDIFEntryList = new ArrayList<>();
	public ArrayList<Entry> mDEAEntryList = new ArrayList<>();
	public ArrayList<BarEntry> mHistogramEntryList = new ArrayList<>();
	public ArrayList<Entry> mVelocityEntryList = new ArrayList<>();
	public ArrayList<LimitLine> mXLimitLineList = new ArrayList<>();
	public List<Entry>[] mLineList = new List[Trend.LEVEL_MAX];
	public int[] mLineColors = {Color.GRAY, Color.YELLOW, Color.BLACK, Color.RED, Color.MAGENTA};
	public CombinedData mCombinedDataMain = new CombinedData(mXValues);
	public CombinedData mCombinedDataSub = new CombinedData(mXValues);
	Stock mStock;
	String mPeriod;
	double mMainChartYMin = 0;
	double mMainChartYMax = 0;
	double mSubChartYMin = 0;
	double mSubChartYMax = 0;

	public StockDataChart(Stock stock, String period) {
		mStock = stock;
		mPeriod = period;
		for (int i = 0; i < Trend.LEVEL_MAX; i++) {
			mLineList[i] = new ArrayList<>();
		}
	}

	public void setStock(Stock stock) {
		mStock = stock;
	}

	public void setMainChartData(Context context) {
		mCombinedDataMain = new CombinedData(mXValues);

		if (true) {
			BubbleData bubbleData = new BubbleData(mXValues);
			if (mNaturalRallyList.size() > 0) {
				BubbleDataSet bubbleDataSet = new BubbleDataSet(mNaturalRallyList, "NUp");
				bubbleDataSet.setColor(Color.BLUE);
				bubbleData.addDataSet(bubbleDataSet);
			}

			if (mUpwardTrendList.size() > 0) {
				BubbleDataSet bubbleDataSet = new BubbleDataSet(mUpwardTrendList, "Up");
				bubbleDataSet.setColor(Color.RED);
				bubbleData.addDataSet(bubbleDataSet);
			}

			if (mDownwardTrendList.size() > 0) {
				BubbleDataSet bubbleDataSet = new BubbleDataSet(mDownwardTrendList, "Dn");
				bubbleDataSet.setColor(Color.GREEN);
				bubbleData.addDataSet(bubbleDataSet);
			}

			if (mNaturalReactionList.size() > 0) {
				BubbleDataSet bubbleDataSet = new BubbleDataSet(mNaturalReactionList, "NDn");
				bubbleDataSet.setColor(Color.YELLOW);
				bubbleData.addDataSet(bubbleDataSet);
			}
			mCombinedDataMain.setData(bubbleData);
		}

		if (mCandleEntryList.size() > 0) {
			CandleData candleData = new CandleData(mXValues);

			CandleDataSet candleDataSet = new CandleDataSet(mCandleEntryList,
					"K");
			candleDataSet.setDecreasingColor(Color.rgb(50, 128, 50));
			candleDataSet.setDecreasingPaintStyle(Paint.Style.FILL);
			candleDataSet.setIncreasingColor(Color.rgb(255, 50, 50));
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

		if (mCandleEntryList.size() > 0) {
			{
				LineDataSet lineDataSet = new LineDataSet(mAverage5EntryList,
						"MA5");
				lineDataSet.setColor(Color.WHITE);
				lineDataSet.setDrawCircles(false);
				lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
				lineData.addDataSet(lineDataSet);
			}

			{
				LineDataSet lineDataSet = new LineDataSet(mAverage10EntryList,
						"MA10");
				lineDataSet.setColor(Color.CYAN);
				lineDataSet.setDrawCircles(false);
				lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
				lineData.addDataSet(lineDataSet);
			}
		}

		if (Setting.getDisplayDraw()) {
			if (mLineList[0].size() > 0) {
				LineDataSet lineDataSet = new LineDataSet(mLineList[0], "Draw");
				lineDataSet.setColor(mLineColors[0]);
				lineDataSet.setCircleColor(mLineColors[0]);
				lineDataSet.setCircleSize(0);
				lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
				lineData.addDataSet(lineDataSet);
			}
		}

		if (Setting.getDisplayStroke()) {
			if (mLineList[1].size() > 0) {
				LineDataSet lineDataSet = new LineDataSet(mLineList[1], "Stroke");
				if (Setting.getDisplayFilled()) {
					lineDataSet.setDrawFilled(true);
				}
				lineDataSet.setColor(mLineColors[1]);
				lineDataSet.setCircleColor(mLineColors[1]);
				lineDataSet.setCircleSize(0);
				lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
				lineData.addDataSet(lineDataSet);
			}
		}

		if (Setting.getDisplaySegment()) {
			if (mLineList[2].size() > 0) {
				LineDataSet lineDataSet = new LineDataSet(mLineList[2],
						"Segment");
				if (Setting.getDisplayFilled()) {
					lineDataSet.setDrawFilled(true);
				}
				lineDataSet.setColor(mLineColors[2]);
				lineDataSet.setCircleColor(mLineColors[2]);
				lineDataSet.setCircleSize(0);
				lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
				lineData.addDataSet(lineDataSet);
			}
		}

		if (Setting.getDisplayLine()) {
			if (mLineList[3].size() > 0) {
				LineDataSet lineDataSet = new LineDataSet(mLineList[3],
						"Line");
				if (Setting.getDisplayFilled()) {
					lineDataSet.setDrawFilled(true);
				}
				lineDataSet.setColor(mLineColors[3]);
				lineDataSet.setCircleColor(mLineColors[3]);
				lineDataSet.setCircleSize(0);
				lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
				lineData.addDataSet(lineDataSet);
			}

			if (mLineList[4].size() > 0) {
				LineDataSet lineDataSet = new LineDataSet(mLineList[4],
						"Outline");
				if (Setting.getDisplayFilled()) {
					lineDataSet.setDrawFilled(true);
				}
				lineDataSet.setColor(mLineColors[4]);
				lineDataSet.setCircleColor(mLineColors[4]);
				lineDataSet.setCircleSize(0);
				lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
				lineData.addDataSet(lineDataSet);
			}
		}
		mCombinedDataMain.setData(lineData);
	}

	public void setSubChartData(Context context) {
		mCombinedDataSub = new CombinedData(mXValues);

		BarData barData = new BarData(mXValues);
		BarDataSet histogramDataSet = new BarDataSet(mHistogramEntryList,
				"Histogram");
		histogramDataSet.setBarSpacePercent(40f);
		histogramDataSet.setIncreasingColor(Color.rgb(255, 50, 50));
		histogramDataSet.setDecreasingColor(Color.rgb(50, 128, 50));
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

		if (mVelocityEntryList.size() > 0) {
			LineDataSet velocityDataSet = new LineDataSet(mVelocityEntryList, "Velocity");
			velocityDataSet.setColor(Color.BLUE);
			velocityDataSet.setDrawCircles(false);
			velocityDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
			lineData.addDataSet(velocityDataSet);
		}

		mCombinedDataSub.setData(barData);
		mCombinedDataSub.setData(lineData);
	}

	public void setMainChartYMinMax(int index, List<Entry> drawEntryList, List<Entry> strokeEntryList, List<Entry> segmentEntryList) {
		double draw = 0;
		double stroke = 0;
		double segment = 0;

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

	public void setSubChartYMinMax(int index, List<Entry> difEntryList, List<Entry> deaEntryList) {
		double dif = 0;
		double dea = 0;

		if (difEntryList == null || difEntryList.size() == 0) {
			return;
		}

		if (deaEntryList == null || deaEntryList.size() == 0) {
			return;
		}

		dif = difEntryList.get(difEntryList.size() - 1).getVal();
		dea = deaEntryList.get(deaEntryList.size() - 1).getVal();

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
			mDescription.append("+");
		} else if (stock.getNet() < 0) {
			mDescription.append("-");
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

	public void updateLimitLines(Stock stock, ArrayList<StockDeal> stockDealList,
	                             boolean keyDisplayLatest, boolean keyDisplayCost, boolean keyDisplayDeal) {
		if (stock == null || stockDealList == null) {
			return;
		}

		if (mXLimitLineList == null) {
			return;
		}

		mXLimitLineList.clear();

		updateLatestLimitLine(stock, stockDealList, keyDisplayLatest);
		updateCostLimitLine(stock, stockDealList, keyDisplayCost);
		updateDealLimitLine(stock, stockDealList, keyDisplayDeal);
	}

	void updateLatestLimitLine(Stock stock, ArrayList<StockDeal> stockDealList, boolean keyDisplayLatest) {
		if (stock == null || stockDealList == null) {
			return;
		}

		int color = Color.WHITE;
		String action = "";
		String label = "";
		LimitLine limitLine;

		if (mXLimitLineList == null) {
			return;
		}

		if (!keyDisplayLatest) {
			return;
		}

		action = stock.getAction(mPeriod);

		if (action.contains("L") || action.contains("B")) {
			color = Color.CYAN;
		} else if (action.contains("H") || action.contains("S")) {
			color = Color.MAGENTA;
		}

		label = "                                                     " + " "
				+ "Action" + " " + action + " ";
		limitLine = createLimitLine(stock.getPrice(), color, label);
		mXLimitLineList.add(limitLine);
	}

	void updateCostLimitLine(Stock stock, ArrayList<StockDeal> stockDealList, boolean keyDisplayCost) {
		int color = Color.WHITE;
		double cost = 0;
		double net = 0;
		String label = "";
		LimitLine limitLine;

		if (stock == null || stockDealList == null) {
			return;
		}

		if (mXLimitLineList == null) {
			return;
		}

		if (!keyDisplayCost) {
			return;
		}

		cost = stock.getCost();

		if ((cost > 0) && (stock.getHold() > 0)) {
			net = Utility.Round(100 * (stock.getPrice() - cost) / cost);
			color = Color.BLUE;
			label = "                                                     "
					+ " " + cost + " " + net + "%";
			limitLine = createLimitLine(cost, color, label);

			mXLimitLineList.add(limitLine);
		}
	}

	void updateDealLimitLine(Stock stock, ArrayList<StockDeal> stockDealList,
	                         boolean keyDisplayDeal) {
		double limit = 0;
		int color = Color.WHITE;
		String label = "";
		LimitLine limitLineDeal = new LimitLine(0);

		if (stock == null || stockDealList == null) {
			return;
		}

		if (mXLimitLineList == null) {
			return;
		}

		if (!keyDisplayDeal) {
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

			mXLimitLineList.add(limitLineDeal);
		}
	}

	public void clear() {
		mXValues.clear();

		mNaturalRallyList.clear();
		mUpwardTrendList.clear();
		mDownwardTrendList.clear();
		mNaturalReactionList.clear();
		mCandleEntryList.clear();
		mAverage5EntryList.clear();
		mAverage10EntryList.clear();
		mDIFEntryList.clear();
		mDEAEntryList.clear();
		mHistogramEntryList.clear();
		mVelocityEntryList.clear();

		for (int i = 0; i < Trend.LEVEL_MAX; i++) {
			mLineList[i].clear();
		}
	}
}
