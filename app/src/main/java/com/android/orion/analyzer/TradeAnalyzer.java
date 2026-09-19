package com.android.orion.analyzer;

import android.text.TextUtils;

import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.android.orion.manager.StockDatabaseManager;
import com.android.orion.utility.Logger;

import java.util.ArrayList;

public class TradeAnalyzer {
	Logger Log = Logger.getLogger();
	double mProfit;
	long mHedgeable;
	Stock mStock;
	StockDeal mBuyDeal;
	StockDeal mSellDeal;
	ArrayList<StockDeal> mStockDealList;
	StockDatabaseManager mStockDatabaseManager = StockDatabaseManager.getInstance();

	private TradeAnalyzer() {
	}

	public static TradeAnalyzer getInstance() {
		return Holder.INSTANCE;
	}

	public void analyzeProfit(Stock stock) {
		if (stock == null) {
			return;
		}

		mStock = stock;
		if (!mStock.hasFlag(Stock.FLAG_TARGET)) {
			return;
		}

		mProfit = 0;
		if (mStock.getTee() > 0) {
			mProfit += mStock.getTee();
		}
		mHedgeable = 0;

		mStock.setBuyProfit(0);
		mStock.setSellProfit(0);

		mStockDealList = mStock.getStockDealList();
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
				if (mBuyDeal.getProfit() > 0) {
					mProfit += mBuyDeal.getProfit();
				}
			} else if (TextUtils.equals(stockDeal.getType(), StockDeal.TYPE_SELL)) {
				if (mSellDeal == null) {
					mSellDeal = stockDeal;
				}
			}
		}

		if (mBuyDeal != null) {
			mStock.setBuyProfit(mBuyDeal.getProfit());
		}

		if (mSellDeal != null) {
			mStock.setSellProfit(mSellDeal.getProfit());
		}

		for (int i = 0; i < mStockDealList.size(); i++) {
			if (mProfit <= 0) {
				break;
			}

			StockDeal stockDeal = mStockDealList.get(i);
			if (TextUtils.equals(stockDeal.getType(), StockDeal.TYPE_BUY)) {
				double profit = stockDeal.getProfit();
				long volume = stockDeal.getVolume();
				if (profit >= 0 || volume <= 0) {
					continue;
				}

				profit = Math.abs(profit);
				if (mProfit >= profit) {
					mHedgeable += volume;
					mProfit -= profit;
				} else {
					mHedgeable += (mProfit * volume)/profit;
					mProfit = 0;
				}
			}
		}
		mStock.setHedgeable(mHedgeable);
	}

	public String getBuyDealString() {
		return mBuyDeal == null ? "" : mBuyDeal.getString();
	}

	public String getSellDealString() {
		return mSellDeal == null ? "" : mSellDeal.getString();
	}

	private static class Holder {
		private static final TradeAnalyzer INSTANCE = new TradeAnalyzer();
	}
}
