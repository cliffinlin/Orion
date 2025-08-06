package com.android.orion.analyzer;

import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.android.orion.manager.StockDatabaseManager;
import com.android.orion.utility.Logger;

import java.util.ArrayList;

public class GridAnalyzer {
	Logger Log = Logger.getLogger();
	Stock mStock;
	StockDeal mStockDeal;
	ArrayList<StockDeal> mStockDealList = new ArrayList<>();
	StockDatabaseManager mStockDatabaseManager = StockDatabaseManager.getInstance();

	private GridAnalyzer() {
	}

	public static GridAnalyzer getInstance() {
		return Holder.INSTANCE;
	}

	public void analyze(Stock stock) {
		mStock = stock;
		if (mStock == null || !mStock.hasFlag(Stock.FLAG_GRID)) {
			return;
		}

		mStockDatabaseManager.getStockDealList(mStock, mStockDealList);
		if (mStockDealList == null || mStockDealList.size() == 0) {
			return;
		}

		mStockDeal = mStockDealList.get(mStockDealList.size() - 1);
		if (mStockDeal == null) {
			return;
		}

		mStock.setGridProfit(mStockDeal.getProfit());
	}

	public String getNotifyString() {
		return mStockDeal == null ? "" : mStockDeal.toString();
	}

	private static class Holder {
		private static final GridAnalyzer INSTANCE = new GridAnalyzer();
	}
}
