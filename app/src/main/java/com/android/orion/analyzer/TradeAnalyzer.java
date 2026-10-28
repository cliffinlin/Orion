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
	double mTee;
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

		mStockDealList = mStock.getStockDealList();
		mStockDatabaseManager.getStockDealList(mStock, mStockDealList);
		if (mStockDealList.isEmpty()) {
			mStock.setBuyProfit(0);
			mStock.setSellProfit(0);
			mStock.setHedgeable(0);
			mStock.setTee(0);
			return;
		}

		setStockProfit();
		setHedgeable();
		setTee();
	}

	public void setStockProfit() {
		mStock.setBuyProfit(0);
		mStock.setSellProfit(0);

		mBuyDeal = null;
		mSellDeal = null;
		for (int i = 0; i < mStockDealList.size(); i++) {
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
		}

		if (mBuyDeal != null) {
			mStock.setBuyProfit(mBuyDeal.getProfit());
		}

		if (mSellDeal != null) {
			mStock.setSellProfit(mSellDeal.getProfit());
		}
	}
	public void setHedgeable() {
		mProfit = 0;
		mHedgeable = 0;

		long buyVolume = 0;
		for (int i = 0; i < mStockDealList.size(); i++) {
			StockDeal stockDeal = mStockDealList.get(i);
			if (TextUtils.equals(stockDeal.getType(), StockDeal.TYPE_BUY)) {
				if (buyVolume > (mStock.getHold() - mStock.getLocked())) {
					break;
				}
				buyVolume += stockDeal.getVolume();
				mProfit += stockDeal.getProfit();
			}
		}

		mStock.setBuyVolume(buyVolume);
		if (mProfit > 0) {
			mStock.setBuyProfit(mProfit);
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
					mHedgeable = (long) (mHedgeable + (mProfit * volume) / profit);
					mProfit = 0;
				}
			}
		}
		mStock.setHedgeable(mHedgeable);
	}

	public void setTee() {
		mProfit = 0;
		mTee = 0;

		long sellVolume = 0;
		for (int i = 0; i < mStockDealList.size(); i++) {
			StockDeal stockDeal = mStockDealList.get(i);
			if (TextUtils.equals(stockDeal.getType(), StockDeal.TYPE_SELL)) {
				sellVolume += stockDeal.getVolume();
				mProfit += stockDeal.getProfit();
			}
		}

		mStock.setSellVolume(sellVolume);
		if (mProfit < 0) {
			mStock.setSellProfit(mProfit);
		}
		mTee = mProfit;
		mStock.setTee(mTee);
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
