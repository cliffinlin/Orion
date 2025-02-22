package com.android.orion.chart;

import android.graphics.Color;

import com.android.orion.config.Config;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class StockFinancialChart {
	public String mPeriod;
	public String mDescription;

	public ArrayList<String> mXValues = new ArrayList<>();

	public ArrayList<Entry> mTotalCurrentAssetsEntryList = new ArrayList<>();
	public ArrayList<Entry> mTotalAssetsEntryList = new ArrayList<>();
	public ArrayList<Entry> mTotalLongTermLiabilitiesEntryList = new ArrayList<>();
	public ArrayList<Entry> mMainBusinessIncomeEntryList = new ArrayList<>();
	public ArrayList<Entry> mNetProfitEntryList = new ArrayList<>();
	public ArrayList<Entry> mStockShareEntryList = new ArrayList<>();

	public ArrayList<Entry> mBookValuePerShareEntryList = new ArrayList<>();
	public ArrayList<Entry> mCashFlowPerShareEntryList = new ArrayList<>();
	public ArrayList<Entry> mNetProfitPerShareEntryList = new ArrayList<>();
	public ArrayList<BarEntry> mDividendEntryList = new ArrayList<>();
	public ArrayList<Entry> mRoeEntryList = new ArrayList<>();

	public ArrayList<LimitLine> mLimitLineList = new ArrayList<>();

	public CombinedData mCombinedDataMain = new CombinedData(mXValues);
	public CombinedData mCombinedDataSub = new CombinedData(mXValues);

	public StockFinancialChart(String period) {
		mPeriod = period;

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

		LineDataSet stockShareDataSet = new LineDataSet(mStockShareEntryList,
				"StockShare");
		stockShareDataSet.setColor(Color.RED);
		stockShareDataSet.setDrawCircles(false);
		stockShareDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(stockShareDataSet);

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

		if (mDividendEntryList.size() > 0) {
			BarData barData = new BarData(mXValues);
			BarDataSet dividendDataSet = new BarDataSet(mDividendEntryList,
					"Dividend");
			dividendDataSet.setBarSpacePercent(40f);
			dividendDataSet.setIncreasingColor(Config.COLOR_RGB_RED);
			dividendDataSet.setDecreasingColor(Config.COLOR_RGB_GREEN);
			dividendDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
			barData.addDataSet(dividendDataSet);

			mCombinedDataSub.setData(barData);
		}
	}

	public void clear() {
		mXValues.clear();
		mTotalCurrentAssetsEntryList.clear();
		mTotalAssetsEntryList.clear();
		mTotalLongTermLiabilitiesEntryList.clear();
		mMainBusinessIncomeEntryList.clear();
		mNetProfitEntryList.clear();
		mStockShareEntryList.clear();
		mBookValuePerShareEntryList.clear();
		mCashFlowPerShareEntryList.clear();
		mNetProfitPerShareEntryList.clear();
		mDividendEntryList.clear();
		mRoeEntryList.clear();
	}
}
