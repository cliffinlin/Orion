package com.android.orion;

import java.util.ArrayList;

import android.graphics.Color;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

public class StockStatisticsChart {

	ArrayList<String> mXValues = null;

	ArrayList<BarEntry> mPEEntryList = null;
	ArrayList<Entry> mYieldEntryList = null;
	ArrayList<Entry> mDeltaEntryList = null;

	CombinedData mCombinedDataMain = null;

	public StockStatisticsChart() {
		if (mXValues == null) {
			mXValues = new ArrayList<String>();
		}

		if (mPEEntryList == null) {
			mPEEntryList = new ArrayList<BarEntry>();
		}

		if (mYieldEntryList == null) {
			mYieldEntryList = new ArrayList<Entry>();
		}

		if (mDeltaEntryList == null) {
			mDeltaEntryList = new ArrayList<Entry>();
		}

		if (mCombinedDataMain == null) {
			mCombinedDataMain = new CombinedData(mXValues);
		}

		setMainChartData();
	}

	void setMainChartData() {
		LineData lineData = new LineData(mXValues);
		BarData barData = new BarData(mXValues);

		BarDataSet peDataSet = new BarDataSet(mPEEntryList, "pe");
		peDataSet.setBarSpacePercent(40f);
		peDataSet.setIncreasingColor(Color.rgb(255, 50, 50));
		peDataSet.setDecreasingColor(Color.rgb(50, 128, 50));
		peDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		barData.addDataSet(peDataSet);

		LineDataSet yieldDataSet = new LineDataSet(mYieldEntryList, "yield");
		yieldDataSet.setColor(Color.YELLOW);
		yieldDataSet.setCircleColor(Color.YELLOW);
		yieldDataSet.setCircleSize(3f);
		yieldDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(yieldDataSet);

		LineDataSet deltaDataSet = new LineDataSet(mDeltaEntryList, "delta");
		deltaDataSet.setColor(Color.BLUE);
		deltaDataSet.setCircleColor(Color.BLUE);
		deltaDataSet.setCircleSize(3f);
		deltaDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(deltaDataSet);

		mCombinedDataMain.setData(lineData);
		mCombinedDataMain.setData(barData);
	}

	void clear() {
		mXValues.clear();
		mPEEntryList.clear();
		mYieldEntryList.clear();
		mDeltaEntryList.clear();
	}
}
