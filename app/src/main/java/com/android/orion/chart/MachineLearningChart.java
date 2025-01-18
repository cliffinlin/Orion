package com.android.orion.chart;

import android.graphics.Color;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Random;

public class MachineLearningChart {

	public ArrayList<String> mXValues = new ArrayList<>();

	public ArrayList<Entry> mPointEntryList = new ArrayList<>();
	public ArrayList<Entry> mLineEntryList = new ArrayList<>();
	public CombinedData mCombinedDataMain;

	public MachineLearningChart() {
		setMainChartData();
	}

	public void setMainChartData() {
		mCombinedDataMain = new CombinedData();

		ScatterData scatterData = new ScatterData();
		ScatterDataSet scatterDataSet = new ScatterDataSet(mPointEntryList, "point");
		scatterDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
		scatterDataSet.setColor(Color.BLUE);
		scatterData.addDataSet(scatterDataSet);
		mCombinedDataMain.setData(scatterData);

		LineData lineData = new LineData();
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
