package com.android.orion;

import java.util.ArrayList;

import android.graphics.Color;

import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.github.mikephil.charting.charts.ScatterChart.ScatterShape;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

public class StockMatchChartData {
	String mPeriod;
	String mDescription;

	ArrayList<String> mXValuesMain = null;
	ArrayList<String> mXValuesSub = null;

	ArrayList<Entry> mScatterEntryList = null;
	ArrayList<Entry> mFitEntryList = null;

	ArrayList<Entry> mDIFEntryList = null;

	ArrayList<LimitLine> mLimitLineList = null;

	CombinedData mCombinedDataMain = null;
	CombinedData mCombinedDataSub = null;

	public StockMatchChartData() {
	}

	public StockMatchChartData(String period) {
		if (mXValuesMain == null) {
			mXValuesMain = new ArrayList<String>();
		}

		if (mXValuesSub == null) {
			mXValuesSub = new ArrayList<String>();
		}

		if (mScatterEntryList == null) {
			mScatterEntryList = new ArrayList<Entry>();
		}

		if (mFitEntryList == null) {
			mFitEntryList = new ArrayList<Entry>();
		}

		if (mDIFEntryList == null) {
			mDIFEntryList = new ArrayList<Entry>();
		}

		if (mCombinedDataMain == null) {
			mCombinedDataMain = new CombinedData(mXValuesMain);
		}

		if (mCombinedDataSub == null) {
			mCombinedDataSub = new CombinedData(mXValuesSub);
		}

		if (mLimitLineList == null) {
			mLimitLineList = new ArrayList<LimitLine>();
		}

		mPeriod = period;
		mDescription = mPeriod;

		setMainChartData();
		setSubChartData();
	}

	void setMainChartData() {
		ScatterData scatterData = new ScatterData();

		ScatterDataSet scatterDataSet = new ScatterDataSet(mScatterEntryList,
				"Scatter");
		scatterDataSet.setScatterShape(ScatterShape.CIRCLE);
		scatterDataSet.setColor(Color.BLUE);
		scatterDataSet.setScatterShapeSize(8f);
		scatterDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		scatterData.addDataSet(scatterDataSet);

		LineData lineData = new LineData(mXValuesMain);

		LineDataSet drawDataSet = new LineDataSet(mFitEntryList, "Fit");
		drawDataSet.setColor(Color.RED);
		drawDataSet.setDrawCircles(false);
		drawDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(drawDataSet);

		mCombinedDataMain.setData(scatterData);
		mCombinedDataMain.setData(lineData);
	}

	void setSubChartData() {
		LineData lineData = new LineData(mXValuesSub);

		LineDataSet difDataSet = new LineDataSet(mDIFEntryList, "DIF");
		difDataSet.setColor(Color.RED);
		difDataSet.setCircleColor(Color.RED);
		difDataSet.setDrawCircles(false);
		difDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(difDataSet);

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
		mDescription += stock.getNet();
	}

	void updateLimitLine(ArrayList<StockDeal> stockDealList) {
		double stockDealPrice = 0;
		long stockDealVolume = 0;

		if ((stockDealList == null) || (mLimitLineList == null)) {
			return;
		}

		mLimitLineList.clear();

		for (StockDeal stockDeal : stockDealList) {
			LimitLine limitLine = new LimitLine(0);
			limitLine.enableDashedLine(10f, 10f, 0f);
			limitLine.setLineWidth(3);
			limitLine.setTextSize(10f);
			limitLine.setLabelPosition(LimitLabelPosition.LEFT_TOP);

			stockDealPrice = stockDeal.getDeal();
			stockDealVolume = stockDeal.getVolume();

			limitLine.setLimit((float) stockDealPrice);

			if (stockDealVolume > 0) {
				limitLine.setLineColor(Color.RED);
			} else {
				limitLine.setLineColor(Color.GREEN);
			}

			limitLine.setLabel(stockDealPrice + " " + stockDealVolume + " "
					+ stockDeal.getNet() + " " + stockDeal.getProfit());

			mLimitLineList.add(limitLine);
		}
	}

	void clear() {
		mXValuesMain.clear();
		mXValuesSub.clear();
		mScatterEntryList.clear();
		mFitEntryList.clear();
		mDIFEntryList.clear();
	}
}
