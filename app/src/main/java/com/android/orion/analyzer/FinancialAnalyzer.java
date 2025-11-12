package com.android.orion.analyzer;

import android.text.TextUtils;

import com.android.orion.data.Period;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockBonus;
import com.android.orion.database.StockData;
import com.android.orion.database.StockFinancial;
import com.android.orion.database.StockShare;
import com.android.orion.database.StockTrend;
import com.android.orion.manager.StockDatabaseManager;
import com.android.orion.constant.Constant;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Utility;

import java.util.ArrayList;

public class FinancialAnalyzer {
	ArrayList<StockData> mStockDataList;
	ArrayList<StockFinancial> mStockFinancialList;
	ArrayList<StockShare> mStockShareList;
	ArrayList<StockBonus> mStockBonusList;

	StockDatabaseManager mStockDatabaseManager = StockDatabaseManager.getInstance();
	Logger Log = Logger.getLogger();

	private FinancialAnalyzer() {
	}

	public static FinancialAnalyzer getInstance() {
		return FinancialAnalyzer.Holder.INSTANCE;
	}

	public void analyzeFinancial(Stock stock) {
		String sortOrder = DatabaseContract.ORDER_DATE_DESC;

		if (stock == null || TextUtils.equals(stock.getClasses(), Stock.CLASS_INDEX)) {
			return;
		}

		for (int i = Period.indexOf(Period.MONTH); i < Period.PERIODS.length; i++) {
			String period = Period.PERIODS[i];
			mStockDataList = stock.getStockDataList(period, StockTrend.LEVEL_NONE);
			mStockDatabaseManager.loadStockDataList(stock, period, mStockDataList);
			if (mStockDataList.size() > 0) {
				break;
			}
		}

		mStockFinancialList = stock.getFinancialList();
		mStockShareList = stock.getStockShareList();
		mStockBonusList = stock.getStockBonusList();

		mStockDatabaseManager.getStockFinancialList(stock, mStockFinancialList,
				sortOrder);
		mStockDatabaseManager.getStockShareList(stock, mStockShareList,
				sortOrder);
		mStockDatabaseManager.getStockBonusList(stock, mStockBonusList,
				sortOrder);

		setupStockShare();
		setupNetProfitPerShareInYear();
		setupNetProfitPerShare();
		setupRate();
		setupRoe();
		mStockDatabaseManager.updateStockFinancial(stock, mStockFinancialList);
	}

	public void setNetProfileInYear(Stock stock, ArrayList<StockData> stockDataList) {
		if (stock == null || stockDataList == null || stockDataList.size() == 0 || mStockFinancialList == null || mStockFinancialList.size() == 0) {
			return;
		}

		mStockFinancialList = stock.getFinancialList();
		mStockDatabaseManager.getStockFinancialList(stock, mStockFinancialList, DatabaseContract.ORDER_DATE_DESC);

		int j = 0;
		for (int i = stockDataList.size() - 1; i >= 0; i--) {
			StockData stockData = stockDataList.get(i);
			while (j < mStockFinancialList.size()) {
				StockFinancial stockFinancial = mStockFinancialList.get(j);
				if (Utility.getCalendar(stockData.getDate(),
						Utility.CALENDAR_DATE_FORMAT).after(
						Utility.getCalendar(stockFinancial.getDate(),
								Utility.CALENDAR_DATE_FORMAT))) {
					break;
				} else {
					j++;
				}
			}
		}
	}

	private void setupStockShare() {
		if (mStockFinancialList.size() == 0 || mStockShareList.size() == 0) {
			return;
		}

		int j = 0;
		for (StockFinancial stockFinancial : mStockFinancialList) {
			stockFinancial.setShare(mStockShareList.get(mStockShareList.size() - 1).getStockShare());
			while (j < mStockShareList.size()) {
				StockShare stockShare = mStockShareList.get(j);
				if (Utility.getCalendar(stockFinancial.getDate(),
						Utility.CALENDAR_DATE_FORMAT).after(
						Utility.getCalendar(stockShare.getDate(),
								Utility.CALENDAR_DATE_FORMAT))) {
					stockFinancial.setShare(stockShare.getStockShare());
					break;
				} else {
					j++;
				}
			}
		}
	}

	private void setupNetProfitPerShare() {
		for (StockFinancial stockFinancial : mStockFinancialList) {
			stockFinancial.setupNetProfitMargin();
			stockFinancial.setupNetProfitPerShare();
		}
	}

	private void setupNetProfitPerShareInYear() {
		double mainBusinessIncome = 0;
		double mainBusinessIncomeInYear = 0;
		double netProfit = 0;
		double netProfitInYear = 0;
		double netProfitPerShareInYear = 0;
		double netProfitPerShare = 0;

		if (mStockFinancialList.size() < Constant.SEASONS_IN_A_YEAR + 1) {
			return;
		}

		for (int i = 0; i < mStockFinancialList.size(); i++) {
			mainBusinessIncomeInYear = 0;
			netProfitInYear = 0;
			netProfitPerShareInYear = 0;
			for (int j = 0; j < Constant.SEASONS_IN_A_YEAR; j++) {
				if (i + j >= mStockFinancialList.size() || i + j + 1 >= mStockFinancialList.size()) {
					continue;
				}

				StockFinancial current = mStockFinancialList.get(i + j);
				StockFinancial prev = mStockFinancialList.get(i + j + 1);

				if (current == null || prev == null || current.getShare() == 0) {
					continue;
				}

				if (current.getDate().contains("03-31")) {
					mainBusinessIncome = current.getMainBusinessIncome();
					netProfit = current.getNetProfit();
					netProfitPerShare = current.getNetProfit()
							/ current.getShare();
				} else {
					mainBusinessIncome = current.getMainBusinessIncome() - prev.getMainBusinessIncome();
					netProfit = current.getNetProfit() - prev.getNetProfit();
					netProfitPerShare = (current.getNetProfit() - prev
							.getNetProfit()) / current.getShare();
				}

				mainBusinessIncomeInYear += mainBusinessIncome;
				netProfitInYear += netProfit;
				netProfitPerShareInYear += netProfitPerShare;
			}

			StockFinancial stockFinancial = mStockFinancialList.get(i);
			stockFinancial.setMainBusinessIncomeInYear(mainBusinessIncomeInYear);
			stockFinancial.setNetProfitInYear(netProfitInYear);
			stockFinancial.setNetProfitPerShareInYear(netProfitPerShareInYear);
		}
	}

	private void setupRate() {
		double rate = 0;

		if (mStockFinancialList.size() < Constant.SEASONS_IN_A_YEAR + 1) {
			return;
		}

		for (int i = 0; i < mStockFinancialList.size()
				- Constant.SEASONS_IN_A_YEAR; i++) {
			StockFinancial stockFinancial = mStockFinancialList.get(i);
			StockFinancial prev = mStockFinancialList.get(i
					+ Constant.SEASONS_IN_A_YEAR);

			if (prev == null || prev.getNetProfitPerShareInYear() == 0) {
				continue;
			}

			rate = Utility.Round2(stockFinancial.getNetProfitPerShareInYear()
					/ prev.getNetProfitPerShareInYear());

			stockFinancial.setRate(rate);
		}
	}

	private void setupRoe() {
		double roe = 0;

		if (mStockFinancialList.size() < Constant.SEASONS_IN_A_YEAR + 1) {
			return;
		}

		for (int i = 0; i < mStockFinancialList.size()
				- Constant.SEASONS_IN_A_YEAR; i++) {
			StockFinancial stockFinancial = mStockFinancialList.get(i);
			StockFinancial prev = mStockFinancialList.get(i
					+ Constant.SEASONS_IN_A_YEAR);

			if (prev == null || prev.getBookValuePerShare() == 0) {
				continue;
			}

			roe = Utility.Round2(
					100.0 * stockFinancial.getNetProfitPerShareInYear()
							/ prev.getBookValuePerShare());
			if (roe < 0) {
				roe = 0;
			}

			stockFinancial.setRoe(roe);
		}
	}

	public void setupFinancial(Stock stock) {
		if (stock == null || TextUtils.equals(stock.getClasses(), Stock.CLASS_INDEX)) {
			return;
		}

		StockFinancial stockFinancial = new StockFinancial();
		stockFinancial.setSE(stock.getSE());
		stockFinancial.setCode(stock.getCode());
		stockFinancial.setName(stock.getName());

		mStockDatabaseManager.getStockFinancial(stock, stockFinancial);
		mStockDatabaseManager.getStockFinancialList(stock, mStockFinancialList, DatabaseContract.ORDER_DATE_DESC);
		mStockDatabaseManager.updateStockDeal(stock);

		stock.setBookValuePerShare(stockFinancial.getBookValuePerShare());
		stock.setMainBusinessIncome(stockFinancial.getMainBusinessIncome());
		stock.setNetProfit(stockFinancial.getNetProfit());
		stock.setCashFlowPerShare(stockFinancial.getCashFlowPerShare());

		stock.setupMarketValue();
		stock.setupNetProfitPerShare();
		stock.setupNetProfitPerShareInYear(mStockFinancialList);
		stock.setupNetProfitMargin();
		stock.setupRate(mStockFinancialList);
		stock.setupDebtToNetAssetsRatio(mStockFinancialList);
		stock.setupRoe(mStockFinancialList);
		stock.setupPe();
		stock.setupPb();
		stock.setupPr();
		stock.setupRoi();
		stock.setupIRR();
	}

	public void setupStockBonus(Stock stock) {
		if (stock == null || TextUtils.equals(stock.getClasses(), Stock.CLASS_INDEX)) {
			return;
		}

		mStockDatabaseManager.getStockBonusList(stock, mStockBonusList, DatabaseContract.ORDER_DATE_DESC);
		if (mStockBonusList.size() == 0) {
			return;
		}

		ArrayList<StockBonus> currentYearBonusList = new ArrayList<>();
		ArrayList<StockBonus> prevYearBonusList = new ArrayList<>();

		String currentYearString = "";
		String prevYearString = "";
		for (int i = 0; i < mStockBonusList.size(); i++) {
			StockBonus stockBonus = mStockBonusList.get(i);
			if (i == 0) {
				stock.setRDate(stockBonus.getRDate());
				currentYearString = stockBonus.getYear();
			} else {
				if (!TextUtils.equals(currentYearString, stockBonus.getYear())) {
					prevYearString = stockBonus.getYear();
					break;
				}
			}
		}

		for (StockBonus stockBonus : mStockBonusList) {
			if (stockBonus.getYear().contains(currentYearString)) {
				if (stockBonus.getDividend() > 0) {
					currentYearBonusList.add(stockBonus);
				}
			} else if (stockBonus.getYear().contains(prevYearString)) {
				if (stockBonus.getDividend() > 0) {
					prevYearBonusList.add(stockBonus);
				}
			} else {
				break;
			}
		}

		double totalDivident = 0;
		double totalDividendInYear = 0;
		for (int i = 0; i < currentYearBonusList.size(); i++) {
			StockBonus stockBonus = currentYearBonusList.get(i);
			totalDivident += stockBonus.getDividend();
			totalDividendInYear += stockBonus.getDividend();
		}

		if (prevYearBonusList.size() > currentYearBonusList.size()) {
			for (int i = 0; i < prevYearBonusList.size() - currentYearBonusList.size(); i++) {
				totalDividendInYear += prevYearBonusList.get(i).getDividend();
			}
		}

		stock.setDividend(totalDivident);
		stock.setDividendInYear(totalDividendInYear);
		stock.setupBonus();
		stock.setupYield();
		stock.setupDividendRatio();
	}

	private static class Holder {
		private static final FinancialAnalyzer INSTANCE = new FinancialAnalyzer();
	}
}