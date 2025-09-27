package com.android.orion.analyzer;

import android.text.TextUtils;

import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.android.orion.manager.StockDatabaseManager;
import com.android.orion.utility.Logger;

import java.util.ArrayList;

public class TradeAnalyzer {
	Logger Log = Logger.getLogger();
	Stock mStock;
	StockDeal mBuyDeal;
	StockDeal mSellDeal;
	ArrayList<StockDeal> mStockDealList = new ArrayList<>();
	StockDatabaseManager mStockDatabaseManager = StockDatabaseManager.getInstance();

	private TradeAnalyzer() {
	}

	public static TradeAnalyzer getInstance() {
		return Holder.INSTANCE;
	}

	public void analyze(Stock stock) {
		mStock = stock;
		if (mStock == null) {
			return;
		}

		mStock.setBuyProfit(0);
		mStock.setSellProfit(0);

		if (!mStock.hasFlag(Stock.FLAG_TRADE)) {
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
				mBuyDeal = stockDeal;
			} else if (TextUtils.equals(stockDeal.getType(), StockDeal.TYPE_SELL)) {
				mSellDeal = stockDeal;
			}
		}

		if (mBuyDeal != null) {
			mStock.setBuyProfit(mBuyDeal.getProfit());
		}

		if (mSellDeal != null) {
			mStock.setSellProfit(mSellDeal.getProfit());
		}
	}

	public String getBuyDealString() {
		return mBuyDeal == null ? "" : mBuyDeal.toString();
	}

	public String getSellDealString() {
		return mSellDeal == null ? "" : mSellDeal.toString();
	}

	private static class Holder {
		private static final TradeAnalyzer INSTANCE = new TradeAnalyzer();
	}
}
