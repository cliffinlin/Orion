package com.android.orion;

import java.util.ArrayList;

import android.graphics.Color;

import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

public class StockFinancialChart {
	String mPeriod;
	String mDescription;

	ArrayList<String> mXValues = null;

	ArrayList<Entry> mTotalCurrentAssetsEntryList = null;
	ArrayList<Entry> mTotalAssetsEntryList = null;
	ArrayList<Entry> mTotalLongTermLiabilitiesEntryList = null;
	ArrayList<Entry> mMainBusinessIncomeEntryList = null;
	ArrayList<Entry> mFinancialExpensesEntryList = null;
	ArrayList<Entry> mNetProfitEntryList = null;

	ArrayList<Entry> mBookValuePerShareEntryList = null;
	ArrayList<Entry> mEarningsPerShareEntryList = null;
	ArrayList<Entry> mCashFlowPerShareEntryList = null;
	ArrayList<Entry> mROEEntryList = null;

	ArrayList<LimitLine> mLimitLineList = null;

	CombinedData mCombinedDataMain = null;
	CombinedData mCombinedDataSub = null;

	public StockFinancialChart() {
	}

	public StockFinancialChart(String period) {
		if (mXValues == null) {
			mXValues = new ArrayList<String>();
		}

		if (mTotalLongTermLiabilitiesEntryList == null) {
			mTotalLongTermLiabilitiesEntryList = new ArrayList<Entry>();
		}

		if (mMainBusinessIncomeEntryList == null) {
			mMainBusinessIncomeEntryList = new ArrayList<Entry>();
		}

		if (mFinancialExpensesEntryList == null) {
			mFinancialExpensesEntryList = new ArrayList<Entry>();
		}

		if (mNetProfitEntryList == null) {
			mNetProfitEntryList = new ArrayList<Entry>();
		}

		if (mTotalCurrentAssetsEntryList == null) {
			mTotalCurrentAssetsEntryList = new ArrayList<Entry>();
		}

		if (mTotalAssetsEntryList == null) {
			mTotalAssetsEntryList = new ArrayList<Entry>();
		}

		if (mBookValuePerShareEntryList == null) {
			mBookValuePerShareEntryList = new ArrayList<Entry>();
		}

		if (mEarningsPerShareEntryList == null) {
			mEarningsPerShareEntryList = new ArrayList<Entry>();
		}

		if (mCashFlowPerShareEntryList == null) {
			mCashFlowPerShareEntryList = new ArrayList<Entry>();
		}

		if (mROEEntryList == null) {
			mROEEntryList = new ArrayList<Entry>();
		}

		if (mCombinedDataMain == null) {
			mCombinedDataMain = new CombinedData(mXValues);
		}

		if (mCombinedDataSub == null) {
			mCombinedDataSub = new CombinedData(mXValues);
		}

		if (mLimitLineList == null) {
			mLimitLineList = new ArrayList<LimitLine>();
		}

		mPeriod = period;
		mDescription = mPeriod;

		setMainChartData();
		// setSubChartData();
	}

	void setMainChartData() {
		LineData lineData = new LineData(mXValues);

		LineDataSet average5DataSet = new LineDataSet(
				mTotalCurrentAssetsEntryList, "MA5");
		average5DataSet.setColor(Color.WHITE);
		// average5DataSet.setCircleColor(Color.YELLOW);
		average5DataSet.setDrawCircles(false);
		average5DataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(average5DataSet);

		LineDataSet average10DataSet = new LineDataSet(mTotalAssetsEntryList,
				"MA10");
		average10DataSet.setColor(Color.CYAN);
		// average10DataSet.setCircleColor(Color.RED);
		average10DataSet.setDrawCircles(false);
		average10DataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(average10DataSet);

		LineDataSet drawDataSet = new LineDataSet(
				mTotalLongTermLiabilitiesEntryList, "Draw");
		drawDataSet.setColor(Color.GRAY);
		drawDataSet.setCircleColor(Color.GRAY);
		drawDataSet.setCircleSize(3f);
		drawDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(drawDataSet);

		LineDataSet strokeDataSet = new LineDataSet(
				mMainBusinessIncomeEntryList, "Stroke");
		strokeDataSet.setColor(Color.YELLOW);
		strokeDataSet.setCircleColor(Color.YELLOW);
		strokeDataSet.setCircleSize(3f);
		strokeDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(strokeDataSet);

		LineDataSet segmentDataSet = new LineDataSet(
				mFinancialExpensesEntryList, "Segment");
		segmentDataSet.setColor(Color.BLACK);
		segmentDataSet.setCircleColor(Color.BLACK);
		segmentDataSet.setCircleSize(3f);
		segmentDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(segmentDataSet);

		LineDataSet overlapHighDataSet = new LineDataSet(mNetProfitEntryList,
				"overHigh");
		overlapHighDataSet.setColor(Color.MAGENTA);
		overlapHighDataSet.setDrawCircles(false);
		overlapHighDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(overlapHighDataSet);

		mCombinedDataMain.setData(lineData);
	}

	void setSubChartData() {
		LineData lineData = new LineData(mXValues);

		LineDataSet difDataSet = new LineDataSet(mBookValuePerShareEntryList,
				"DIF");
		difDataSet.setColor(Color.YELLOW);
		difDataSet.setCircleColor(Color.YELLOW);
		difDataSet.setDrawCircles(false);
		difDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(difDataSet);

		LineDataSet deaDataSet = new LineDataSet(mEarningsPerShareEntryList,
				"DEA");
		deaDataSet.setColor(Color.WHITE);
		deaDataSet.setCircleColor(Color.WHITE);
		deaDataSet.setDrawCircles(false);
		deaDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(deaDataSet);

		LineDataSet averageDataSet = new LineDataSet(
				mCashFlowPerShareEntryList, "average");
		averageDataSet.setColor(Color.GRAY);
		averageDataSet.setDrawCircles(false);
		averageDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(averageDataSet);

		LineDataSet acclerateDataSet = new LineDataSet(mROEEntryList,
				"accelerate");
		acclerateDataSet.setColor(Color.MAGENTA);
		acclerateDataSet.setDrawCircles(false);
		acclerateDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(acclerateDataSet);

		mCombinedDataSub.setData(lineData);
	}

	void updateDescription(Stock stock) {
		mDescription = "";

		if (stock == null) {
			return;
		}
		mDescription += mPeriod;
		mDescription += " ";

		mDescription += stock.getPrice() + "  ";

		if (stock.getNet() > 0) {
			mDescription += "+";
		} else if (stock.getNet() < 0) {
			mDescription += "-";
		}

		mDescription += stock.getNet() + "%" + "  ";

		mDescription += "cost " + stock.getCost() + "  " + stock.getProfit()
				+ "%" + "   " + "hold " + stock.getHold();
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

	void updateLimitLine(Stock stock, ArrayList<StockDeal> stockDealList) {
		int color = Color.WHITE;
		String label = "";
		LimitLine limitLine;

		if ((stockDealList == null) || (mLimitLineList == null)) {
			return;
		}

		mLimitLineList.clear();

		color = Color.BLUE;
		label = "                                                                      "
				+ stock.getCost()
				+ " "
				+ stock.getProfit()
				+ "%"
				+ "   "
				+ "hold " + stock.getHold();
		limitLine = createLimitLine(stock.getCost(), color, label);

		mLimitLineList.add(limitLine);

		for (StockDeal stockDeal : stockDealList) {
			if (stockDeal.getProfit() > 0) {
				color = Color.RED;
			} else {
				color = Color.GREEN;
			}

			if (stockDeal.getVolume() == 0) {
				color = Color.YELLOW;
			}

			label = "        " + stockDeal.getDeal() + " " + stockDeal.getNet()
					+ "%" + " " + stockDeal.getVolume() + " "
					+ (int) stockDeal.getProfit();
			limitLine = createLimitLine(stockDeal.getDeal(), color, label);

			mLimitLineList.add(limitLine);
		}
	}

	void clear() {
		mXValues.clear();
		mTotalLongTermLiabilitiesEntryList.clear();
		mMainBusinessIncomeEntryList.clear();
		mFinancialExpensesEntryList.clear();
		mNetProfitEntryList.clear();
		mCashFlowPerShareEntryList.clear();
		mTotalCurrentAssetsEntryList.clear();
		mTotalAssetsEntryList.clear();
		mBookValuePerShareEntryList.clear();
		mEarningsPerShareEntryList.clear();
		mROEEntryList.clear();
	}
}
