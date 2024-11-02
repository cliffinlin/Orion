package com.android.orion.chart;

import android.graphics.Color;

import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class StockFinancialChart {
	public String mPeriod;
	public String mDescription;

	public ArrayList<String> mXValues = null;

	public ArrayList<Entry> mTotalCurrentAssetsEntryList = null;
	public ArrayList<Entry> mTotalAssetsEntryList = null;
	public ArrayList<Entry> mTotalLongTermLiabilitiesEntryList = null;
	public ArrayList<Entry> mMainBusinessIncomeEntryList = null;
	public ArrayList<Entry> mNetProfitEntryList = null;
	public ArrayList<Entry> mTotalShareEntryList = null;

	public ArrayList<Entry> mBookValuePerShareEntryList = null;
	public ArrayList<Entry> mCashFlowPerShareEntryList = null;
	public ArrayList<Entry> mNetProfitPerShareEntryList = null;
	public ArrayList<Entry> mRoeEntryList = null;

	public ArrayList<LimitLine> mLimitLineList = null;

	public CombinedData mCombinedDataMain = null;
	public CombinedData mCombinedDataSub = null;

	public StockFinancialChart() {
	}

	public StockFinancialChart(String period) {
		if (mXValues == null) {
			mXValues = new ArrayList<String>();
		}

		if (mTotalCurrentAssetsEntryList == null) {
			mTotalCurrentAssetsEntryList = new ArrayList<Entry>();
		}

		if (mTotalAssetsEntryList == null) {
			mTotalAssetsEntryList = new ArrayList<Entry>();
		}

		if (mTotalLongTermLiabilitiesEntryList == null) {
			mTotalLongTermLiabilitiesEntryList = new ArrayList<Entry>();
		}

		if (mMainBusinessIncomeEntryList == null) {
			mMainBusinessIncomeEntryList = new ArrayList<Entry>();
		}

		if (mNetProfitEntryList == null) {
			mNetProfitEntryList = new ArrayList<Entry>();
		}

		if (mTotalShareEntryList == null) {
			mTotalShareEntryList = new ArrayList<Entry>();
		}

		if (mBookValuePerShareEntryList == null) {
			mBookValuePerShareEntryList = new ArrayList<Entry>();
		}

		if (mCashFlowPerShareEntryList == null) {
			mCashFlowPerShareEntryList = new ArrayList<Entry>();
		}

		if (mNetProfitPerShareEntryList == null) {
			mNetProfitPerShareEntryList = new ArrayList<Entry>();
		}

		if (mRoeEntryList == null) {
			mRoeEntryList = new ArrayList<Entry>();
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
		setSubChartData();
	}

	public void setMainChartData() {
		LineData lineData = new LineData(mXValues);

		LineDataSet totalAssetsDataSet = new LineDataSet(mTotalAssetsEntryList,
				"Assets");
		totalAssetsDataSet.setColor(Color.CYAN);
		totalAssetsDataSet.setDrawCircles(false);
		totalAssetsDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(totalAssetsDataSet);

		LineDataSet totalCurrentAssetsDataSet = new LineDataSet(
				mTotalCurrentAssetsEntryList, "Current");
		totalCurrentAssetsDataSet.setColor(Color.WHITE);
		totalCurrentAssetsDataSet.setDrawCircles(false);
		totalCurrentAssetsDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(totalCurrentAssetsDataSet);

		LineDataSet totalLongTermLiabilitiesDataSet = new LineDataSet(
				mTotalLongTermLiabilitiesEntryList, "Liabilities");
		totalLongTermLiabilitiesDataSet.setColor(Color.GRAY);
		totalLongTermLiabilitiesDataSet.setCircleColor(Color.GRAY);
		totalLongTermLiabilitiesDataSet.setCircleSize(3f);
		totalLongTermLiabilitiesDataSet
				.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(totalLongTermLiabilitiesDataSet);

		LineDataSet mainBusinessIncomeDataSet = new LineDataSet(
				mMainBusinessIncomeEntryList, "Income");
		mainBusinessIncomeDataSet.setColor(Color.MAGENTA);
		mainBusinessIncomeDataSet.setCircleColor(Color.MAGENTA);
		mainBusinessIncomeDataSet.setCircleSize(3f);
		mainBusinessIncomeDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(mainBusinessIncomeDataSet);

		LineDataSet netProfitDataSet = new LineDataSet(mNetProfitEntryList,
				"NetProfit");
		netProfitDataSet.setColor(Color.YELLOW);
		netProfitDataSet.setCircleColor(Color.YELLOW);
		netProfitDataSet.setCircleSize(3f);
		netProfitDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(netProfitDataSet);

		LineDataSet totalShareDataSet = new LineDataSet(mTotalShareEntryList,
				"TotalShare");
		totalShareDataSet.setColor(Color.RED);
		totalShareDataSet.setDrawCircles(false);
		totalShareDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(totalShareDataSet);

		mCombinedDataMain.setData(lineData);
	}

	public void setSubChartData() {
		LineData lineData = new LineData(mXValues);

		LineDataSet roeDataSet = new LineDataSet(mRoeEntryList, "Roe");
		roeDataSet.setColor(Color.DKGRAY);
		roeDataSet.setDrawCircles(false);
		roeDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(roeDataSet);

		LineDataSet bookValuePerShareDataSet = new LineDataSet(
				mBookValuePerShareEntryList, "BookValue");
		bookValuePerShareDataSet.setColor(Color.BLUE);
		bookValuePerShareDataSet.setDrawCircles(false);
		bookValuePerShareDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(bookValuePerShareDataSet);

		LineDataSet netProfitPerShareDataSet = new LineDataSet(
				mNetProfitPerShareEntryList, "NetProfit");
		netProfitPerShareDataSet.setColor(Color.YELLOW);
		netProfitPerShareDataSet.setDrawCircles(false);
		netProfitPerShareDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(netProfitPerShareDataSet);

		LineDataSet cashFlowPerShareDataSet = new LineDataSet(
				mCashFlowPerShareEntryList, "CashFlow");
		cashFlowPerShareDataSet.setColor(Color.GREEN);
		cashFlowPerShareDataSet.setDrawCircles(false);
		cashFlowPerShareDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(cashFlowPerShareDataSet);

		mCombinedDataSub.setData(lineData);
	}

	public void clear() {
		mXValues.clear();
		mTotalCurrentAssetsEntryList.clear();
		mTotalAssetsEntryList.clear();
		mTotalLongTermLiabilitiesEntryList.clear();
		mMainBusinessIncomeEntryList.clear();
		mNetProfitEntryList.clear();
		mTotalShareEntryList.clear();
		mBookValuePerShareEntryList.clear();
		mCashFlowPerShareEntryList.clear();
		mNetProfitPerShareEntryList.clear();
		mRoeEntryList.clear();
	}
}
