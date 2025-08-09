package com.android.orion.analyzer;

import android.text.TextUtils;

import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.android.orion.manager.StockDatabaseManager;
import com.android.orion.utility.Logger;

import java.util.ArrayList;

public class GridAnalyzer {
	Logger Log = Logger.getLogger();
	Stock mStock;
	StockDeal mBuyDeal;
	StockDeal mSellDeal;
	ArrayList<StockDeal> mStockDealList = new ArrayList<>();
	StockDatabaseManager mStockDatabaseManager = StockDatabaseManager.getInstance();

	private GridAnalyzer() {
	}

	public static GridAnalyzer getInstance() {
		return Holder.INSTANCE;
	}

	public void analyze(Stock stock) {
		mStock = stock;
		if (mStock == null) {
			return;
		}

		mStock.setGridProfit(0);

		if (!mStock.hasFlag(Stock.FLAG_GRID)) {
			return;
		}

		mStockDatabaseManager.getStockDealList(mStock, mStockDealList);
		if (mStockDealList == null || mStockDealList.size() == 0) {
			return;
		}

		mBuyDeal = null;
		mSellDeal = null;
		for (int i = mStockDealList.size() - 1; i >= 0; i--) {
			StockDeal stockDeal = mStockDealList.get(i);
			if (TextUtils.equals(stockDeal.getType(), StockDeal.TYPE_BUY)) {
				if (mBuyDeal == null) {
					mBuyDeal = stockDeal;
				}
			} else if (TextUtils.equals(stockDeal.getType(), StockDeal.TYPE_SELL)) {
				if (mSellDeal == null) {
					mSellDeal = stockDeal;
				}
			}

			if (mBuyDeal != null && mSellDeal != null) {
				break;
			}
		}

		if (mBuyDeal != null) {
			mStock.setGridProfit(mBuyDeal.getProfit());
		}
	}

	public String getBuyDealString() {
		return mBuyDeal == null ? "" : mBuyDeal.toString();
	}

	public String getSellDealString() {
		return mSellDeal == null ? "" : mSellDeal.toString();
	}

	private static class Holder {
		private static final GridAnalyzer INSTANCE = new GridAnalyzer();
	}
}
