package com.android.orion.analyzer;

import android.content.Context;
import android.text.TextUtils;

import com.android.orion.application.MainApplication;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockFinancial;
import com.android.orion.database.TotalShare;
import com.android.orion.manager.DatabaseManager;
import com.android.orion.setting.Constant;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Utility;

import java.util.ArrayList;

public class FinancialAnalyzer {
	static ArrayList<StockFinancial> mStockFinancialList;
	static ArrayList<TotalShare> mTotalShareList;
	static ArrayList<ShareBonus> mShareBonusList;

	Context mContext;
	DatabaseManager mDatabaseManager;
	Logger Log = Logger.getLogger();

	private FinancialAnalyzer() {
		mContext = MainApplication.getContext();
		mDatabaseManager = DatabaseManager.getInstance();
	}

	private static class Holder {
		private static final FinancialAnalyzer INSTANCE = new FinancialAnalyzer();
	}

	public static FinancialAnalyzer getInstance() {
		return FinancialAnalyzer.Holder.INSTANCE;
	}

	public void analyzeFinancial(Stock stock) {
		if (stock == null) {
			return;
		}

		mStockFinancialList = stock.getFinancialList();
		mTotalShareList = stock.getTotalShareList();
		mShareBonusList = stock.getShareBonusList();

		String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";

		if (TextUtils.equals(stock.getClasses(), Stock.CLASS_INDEX)) {
			return;
		}

		mDatabaseManager.getStockFinancialList(stock, mStockFinancialList,
				sortOrder);
		mDatabaseManager.getTotalShareList(stock, mTotalShareList,
				sortOrder);
		mDatabaseManager.getShareBonusList(stock, mShareBonusList,
				sortOrder);

		setupTotalShare(mTotalShareList);
		setupNetProfitPerShareInYear();
		setupNetProfitPerShare();
		setupRate();
		setupRoe();

		mDatabaseManager.updateStockFinancial(stock, mStockFinancialList);
	}

	private void setupTotalShare(ArrayList<TotalShare> totalShareList) {
		int j = 0;
		for (StockFinancial stockFinancial : mStockFinancialList) {
			while (j < totalShareList.size()) {
				TotalShare totalShare = totalShareList.get(j);
				if (Utility.getCalendar(stockFinancial.getDate(),
						Utility.CALENDAR_DATE_FORMAT).after(
						Utility.getCalendar(totalShare.getDate(),
								Utility.CALENDAR_DATE_FORMAT))) {
					stockFinancial.setTotalShare(totalShare.getTotalShare());
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

		for (int i = 0; i < mStockFinancialList.size()
				- Constant.SEASONS_IN_A_YEAR; i++) {
			mainBusinessIncomeInYear = 0;
			netProfitInYear = 0;
			netProfitPerShareInYear = 0;
			for (int j = 0; j < Constant.SEASONS_IN_A_YEAR; j++) {
				StockFinancial current = mStockFinancialList.get(i + j);
				StockFinancial prev = mStockFinancialList.get(i + j + 1);

				if (current == null || prev == null) {
					continue;
				}

				if (current.getTotalShare() == 0) {
					continue;
				}

				if (current.getDate().contains("03-31")) {
					mainBusinessIncome = current.getMainBusinessIncome();
					netProfit = current.getNetProfit();
					netProfitPerShare = current.getNetProfit()
							/ current.getTotalShare();
				} else {
					mainBusinessIncome = current.getMainBusinessIncome() - prev.getMainBusinessIncome();
					netProfit = current.getNetProfit() - prev.getNetProfit();
					netProfitPerShare = (current.getNetProfit() - prev
							.getNetProfit()) / current.getTotalShare();
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

			rate = Utility.Round(stockFinancial.getNetProfitPerShareInYear()
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

			roe = Utility.Round(
					100.0 * stockFinancial.getNetProfitPerShareInYear()
							/ prev.getBookValuePerShare());
			if (roe < 0) {
				roe = 0;
			}

			stockFinancial.setRoe(roe);
		}
	}

	public void setupFinancial(Stock stock) {
		if (stock == null) {
			return;
		}

		String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";
		StockFinancial stockFinancial = new StockFinancial();

		if (TextUtils.equals(stock.getClasses(), Stock.CLASS_INDEX)) {
			return;
		}

		stockFinancial.setStockId(stock.getId());

		mDatabaseManager.getStockFinancial(stock, stockFinancial);
		mDatabaseManager.getStockFinancialList(stock, mStockFinancialList,
				sortOrder);
		mDatabaseManager.updateStockDeal(stock);

		stock.setBookValuePerShare(stockFinancial.getBookValuePerShare());
		stock.setTotalAssets(stockFinancial.getTotalAssets());
		stock.setTotalLongTermLiabilities(stockFinancial
				.getTotalLongTermLiabilities());
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
		stock.setupRoi();
	}

	public void setupShareBonus(Stock stock) {
		if (stock == null) {
			return;
		}

		double totalDivident = 0;

		String yearString = "";
		String prevYearString = "";
		String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";

		if (TextUtils.equals(stock.getClasses(), Stock.CLASS_INDEX)) {
			return;
		}

		mDatabaseManager.getShareBonusList(stock, mShareBonusList,
				sortOrder);

		int i = 0;
		for (ShareBonus shareBonus : mShareBonusList) {
			String dateString = shareBonus.getDate();
			if (!TextUtils.isEmpty(dateString)) {
				String[] strings = dateString.split("-");
				if (strings != null && strings.length > 0) {
					yearString = strings[0];
				}

				if (!TextUtils.isEmpty(prevYearString)) {
					if (!prevYearString.equals(yearString)) {
						break;
					}
				}
			}

			totalDivident += shareBonus.getDividend();

			if (i == 0) {
				stock.setRDate(shareBonus.getRDate());
			}
			stock.setDividend(Utility.Round(totalDivident));
			stock.setupBonus();
			stock.setupYield();
			stock.setupDividendRatio();

			prevYearString = yearString;
			i++;
		}
	}
}