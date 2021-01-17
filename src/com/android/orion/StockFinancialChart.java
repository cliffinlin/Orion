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
	ArrayList<Entry> mTotalShareEntryList = null;

	ArrayList<Entry> mBookValuePerShareEntryList = null;
	ArrayList<Entry> mCashFlowPerShareEntryList = null;
	ArrayList<Entry> mNetProfitPerShareEntryList = null;
	ArrayList<Entry> mRoeEntryList = null;

	ArrayList<LimitLine> mLimitLineList = null;

	CombinedData mCombinedDataMain = null;
	CombinedData mCombinedDataSub = null;

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

		if (mFinancialExpensesEntryList == null) {
			mFinancialExpensesEntryList = new ArrayList<Entry>();
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

	void setMainChartData() {
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

		// LineDataSet financialExpensesDataSet = new LineDataSet(
		// mFinancialExpensesEntryList, "FinancialExpenses");
		// financialExpensesDataSet.setColor(Color.BLACK);
		// financialExpensesDataSet.setCircleColor(Color.BLACK);
		// financialExpensesDataSet.setCircleSize(3f);
		// financialExpensesDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		// lineData.addDataSet(financialExpensesDataSet);

		LineDataSet netProfitDataSet = new LineDataSet(mNetProfitEntryList,
				"NetProfit");
		netProfitDataSet.setColor(Color.YELLOW);
		netProfitDataSet.setDrawCircles(false);
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

	void setSubChartData() {
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
		mTotalCurrentAssetsEntryList.clear();
		mTotalAssetsEntryList.clear();
		mTotalLongTermLiabilitiesEntryList.clear();
		mMainBusinessIncomeEntryList.clear();
		mFinancialExpensesEntryList.clear();
		mNetProfitEntryList.clear();
		mTotalShareEntryList.clear();
		mBookValuePerShareEntryList.clear();
		mCashFlowPerShareEntryList.clear();
		mNetProfitPerShareEntryList.clear();
		mRoeEntryList.clear();
	}
}
