package com.android.orion.chart;

import android.graphics.Color;

import com.android.orion.config.Config;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.Random;

public class StockStatisticsChart {

	public ArrayList<String> mXValues = null;

	public ArrayList<BarEntry> mPeEntryList = null;
	public ArrayList<Entry> mRoiEntryList = null;
	public ArrayList<Entry> mRoeEntryList = null;
	public ArrayList<Entry> mRateEntryList = null;
	public ArrayList<Entry> mYieldEntryList = null;
	public ArrayList<Entry> mDividendRatioEntryList = null;
	public ArrayList<Entry> mValuationEntryList = null;
	public CombinedData mCombinedDataMain = null;
	public PieData mPieData = null;
	ArrayList<Entry> mDIFEntryList = null;
	ArrayList<Entry> mDEAEntryList = null;
	ArrayList<BarEntry> mHistogramEntryList = null;
	CombinedData mCombinedDataSub = null;

	public StockStatisticsChart() {
		if (mXValues == null) {
			mXValues = new ArrayList<>();
		}

		if (mPeEntryList == null) {
			mPeEntryList = new ArrayList<>();
		}

		if (mRoiEntryList == null) {
			mRoiEntryList = new ArrayList<>();
		}

		if (mRoeEntryList == null) {
			mRoeEntryList = new ArrayList<>();
		}

		if (mRateEntryList == null) {
			mRateEntryList = new ArrayList<>();
		}

		if (mYieldEntryList == null) {
			mYieldEntryList = new ArrayList<>();
		}

		if (mDividendRatioEntryList == null) {
			mDividendRatioEntryList = new ArrayList<>();
		}

		if (mDIFEntryList == null) {
			mDIFEntryList = new ArrayList<>();
		}

		if (mDEAEntryList == null) {
			mDEAEntryList = new ArrayList<>();
		}

		if (mHistogramEntryList == null) {
			mHistogramEntryList = new ArrayList<>();
		}

		if (mValuationEntryList == null) {
			mValuationEntryList = new ArrayList<>();
		}

		if (mCombinedDataMain == null) {
			mCombinedDataMain = new CombinedData(mXValues);
		}

		if (mCombinedDataSub == null) {
			mCombinedDataSub = new CombinedData(mXValues);
		}

		if (mPieData == null) {
			mPieData = new PieData();
		}

		setMainChartData();
		setSubChartData();
	}

	public void setMainChartData() {
		LineData lineData = new LineData(mXValues);

		BarData barData = new BarData(mXValues);
		BarDataSet peDataSet = new BarDataSet(mPeEntryList, "pe");
		peDataSet.setBarSpacePercent(40f);
		peDataSet.setIncreasingColor(Config.COLOR_RGB_RED);
		peDataSet.setDecreasingColor(Config.COLOR_RGB_GREEN);
		peDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		barData.addDataSet(peDataSet);

		LineDataSet roiDataSet = new LineDataSet(mRoiEntryList, "roi");
		roiDataSet.setColor(Color.RED);
		roiDataSet.setCircleColor(Color.RED);
		roiDataSet.setCircleSize(3f);
		roiDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(roiDataSet);

		LineDataSet roeDataSet = new LineDataSet(mRoeEntryList, "roe");
		roeDataSet.setColor(Color.GREEN);
		roeDataSet.setCircleColor(Color.GREEN);
		roeDataSet.setCircleSize(3f);
		roeDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(roeDataSet);

		LineDataSet rateDataSet = new LineDataSet(mRateEntryList, "rate");
		rateDataSet.setColor(Color.CYAN);
		rateDataSet.setCircleColor(Color.CYAN);
		rateDataSet.setCircleSize(3f);
		rateDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(rateDataSet);

		LineDataSet yieldDataSet = new LineDataSet(mYieldEntryList, "yield");
		yieldDataSet.setColor(Color.YELLOW);
		yieldDataSet.setCircleColor(Color.YELLOW);
		yieldDataSet.setCircleSize(3f);
		yieldDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(yieldDataSet);

		LineDataSet dividendRatioDataSet = new LineDataSet(
				mDividendRatioEntryList, "dividen_ratio");
		dividendRatioDataSet.setColor(Color.BLUE);
		dividendRatioDataSet.setCircleColor(Color.BLUE);
		dividendRatioDataSet.setCircleSize(3f);
		dividendRatioDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(dividendRatioDataSet);

		mCombinedDataMain.setData(lineData);
		mCombinedDataMain.setData(barData);
	}

	public void setSubChartData() {
		mPieData = new PieData(mXValues);
		PieDataSet pieDataSet = new PieDataSet(mValuationEntryList, "Valuation");

		int[] values = {0x00, 0x3f, 0x6f, 0x9f, 0xcf, 0xff};
		for (int i = 0; i < mXValues.size(); i++) {
			Random random = new Random();
			int r = values[random.nextInt(values.length)];
			int g = 0xff;
			int b = values[random.nextInt(values.length)];
			pieDataSet.addColor(Color.rgb(r, g, b));
		}

		mPieData.addDataSet(pieDataSet);
		mPieData.setValueTextSize(9f);
		mPieData.setValueFormatter(new PercentFormatter());

		/*
		 * BarData barData = new BarData(mXValues); BarDataSet histogramDataSet
		 * = new BarDataSet(mHistogramEntryList, "Histogram");
		 * histogramDataSet.setBarSpacePercent(40f);
		 * histogramDataSet.setIncreasingColor(Color.rgb(255, 50, 50));
		 * histogramDataSet.setDecreasingColor(Color.rgb(50, 128, 50));
		 * histogramDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		 * barData.addDataSet(histogramDataSet);
		 *
		 * LineData lineData = new LineData(mXValues);
		 *
		 * LineDataSet difDataSet = new LineDataSet(mDIFEntryList, "DIF");
		 * difDataSet.setColor(Color.YELLOW); //
		 * difDataSet.setCircleColor(Color.YELLOW);
		 * difDataSet.setDrawCircles(false);
		 * difDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		 * lineData.addDataSet(difDataSet);
		 *
		 * LineDataSet deaDataSet = new LineDataSet(mDEAEntryList, "DEA");
		 * deaDataSet.setColor(Color.WHITE); //
		 * deaDataSet.setCircleColor(Color.WHITE);
		 * deaDataSet.setDrawCircles(false);
		 * deaDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		 * lineData.addDataSet(deaDataSet);
		 *
		 * mCombinedDataSub.setData(barData);
		 * mCombinedDataSub.setData(lineData);
		 */
	}

	public void clear() {
		mXValues.clear();

		mPeEntryList.clear();
		mRoiEntryList.clear();
		mRoeEntryList.clear();
		mRateEntryList.clear();
		mYieldEntryList.clear();
		mDividendRatioEntryList.clear();
		mDIFEntryList.clear();
		mDEAEntryList.clear();
		mHistogramEntryList.clear();
		mValuationEntryList.clear();
	}
}
