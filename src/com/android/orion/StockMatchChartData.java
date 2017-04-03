package com.android.orion;

import java.util.ArrayList;

import android.graphics.Color;

import com.android.orion.database.Stock;
import com.github.mikephil.charting.charts.ScatterChart.ScatterShape;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
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
		difDataSet.setColor(Color.BLUE);
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
		
		mDescription += stock.getName();
		mDescription += " ";

		mDescription += stock.getPrice() + "  ";
		if (stock.getNet() > 0) {
			mDescription += "+";
		} else if (stock.getNet() < 0) {
			mDescription += "-";
		}
		
		mDescription += stock.getNet();
	}

	void updateLimitLine(Stock stock_X, Stock stock_Y) {
		if ((stock_X == null) || (stock_Y == null) || (mLimitLineList == null)) {
			return;
		}

		mLimitLineList.clear();

		for (int i = 0; i < 3; i++) {
			LimitLine limitLine = new LimitLine(0);

			limitLine.setLineWidth(1);
			limitLine.setTextSize(10f);
			

			if (i == 0) {
				limitLine.setLimit(-1);
				limitLine.setLineColor(Color.RED);
				limitLine.setTextColor(Color.RED);
				limitLine.setLabel("          " + stock_X.getName());
				limitLine.setLabelPosition(LimitLabelPosition.LEFT_BOTTOM);
			} else if (i == 1) {
				limitLine.setLimit(0);
				limitLine.setLineColor(Color.GREEN);
			} else {
				limitLine.setLimit(1);
				limitLine.setLineColor(Color.RED);
				limitLine.setTextColor(Color.RED);
				limitLine.setLabel("          " + stock_Y.getName());
				limitLine.setLabelPosition(LimitLabelPosition.LEFT_TOP);
			}

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
