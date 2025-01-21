package com.android.orion.chart;

import android.graphics.Color;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

import java.util.ArrayList;

public class StockTrendChart {
	public ArrayList<String> mXValues = new ArrayList<>();

	public ArrayList<Entry> mPointEntryList = new ArrayList<>();
	public ArrayList<Entry> mLineEntryList = new ArrayList<>();
	public CombinedData mCombinedDataMain = new CombinedData(mXValues);

	public StockTrendChart() {
		setMainChartData();
	}

	public void setMainChartData() {
		mCombinedDataMain = new CombinedData(mXValues);

		ScatterData scatterData = new ScatterData(mXValues);
		ScatterDataSet scatterDataSet = new ScatterDataSet(mPointEntryList, "point");
		scatterDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
		scatterDataSet.setColor(Color.BLUE);
		scatterData.addDataSet(scatterDataSet);
		mCombinedDataMain.setData(scatterData);

		LineData lineData = new LineData(mXValues);
		LineDataSet lineDataSet = new LineDataSet(mLineEntryList, "line");
		lineDataSet.setColor(Color.RED);
		lineDataSet.setCircleColor(Color.RED);
		lineDataSet.setCircleSize(3f);
		lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(lineDataSet);
		mCombinedDataMain.setData(lineData);
	}

	public void clear() {
		mXValues.clear();

		mPointEntryList.clear();
		mLineEntryList.clear();
	}
}
