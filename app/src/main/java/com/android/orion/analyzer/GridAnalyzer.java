package com.android.orion.analyzer;

import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.android.orion.database.StockGrid;
import com.android.orion.manager.DatabaseManager;
import com.android.orion.utility.Logger;

import java.util.ArrayList;

public class GridAnalyzer {
	Logger Log = Logger.getLogger();
	Stock mStock;
	StockGrid mStockGridBuy = new StockGrid();
	StockGrid mStockGridSell = new StockGrid();
	ArrayList<StockDeal> mStockDealList = new ArrayList<>();
	DatabaseManager mDatabaseManager = DatabaseManager.getInstance();

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

		if (mStock.getGridGap() == 0) {
			return;
		}

		mDatabaseManager.getStockDealList(mStock, mStockDealList);
		if (mStockDealList == null || mStockDealList.size() == 0) {
			return;
		}

		mStockGridBuy.init();
		mStockGridBuy.setSE(mStock.getSE());
		mStockGridBuy.setCode(mStock.getCode());
		mStockGridBuy.setName(mStock.getName());
		mStockGridBuy.setType(StockGrid.TYPE_BUY);
		mStockGridBuy.setGridGap(mStock.getGridGap());
		mStockGridBuy.setHigh(mStockDealList.get(0).getBuy());
		mStockGridBuy.setLow(mStockDealList.get(mStockDealList.size() - 1).getBuy());
		mStockGridBuy.setVolume(mStockDealList.get(0).getVolume());
		mStockGridBuy.setupPrice();
		mStockGridBuy.setupValue();
		if (!mDatabaseManager.isStockGridExist(mStockGridBuy)) {
			mDatabaseManager.insertStockGrid(mStockGridBuy);
		} else {
			mDatabaseManager.updateStockGrid(mStockGridBuy, mStockGridBuy.getContentValues());
		}
		mStock.setGridBuy(mStockGridBuy.getPrice());

		mStockGridSell.init();
		mStockGridSell.setSE(mStock.getSE());
		mStockGridSell.setCode(mStock.getCode());
		mStockGridSell.setName(mStock.getName());
		mStockGridSell.setType(StockGrid.TYPE_SELL);
		mStockGridSell.setGridGap(mStock.getGridGap());
		mStockGridSell.setHigh(mStockDealList.get(0).getBuy());
		mStockGridSell.setLow(mStockDealList.get(mStockDealList.size() - 1).getBuy());
		mStockGridSell.setVolume(mStockDealList.get(mStockDealList.size() - 1).getVolume());
		mStockGridSell.setupPrice();
		mStockGridSell.setupValue();
		if (!mDatabaseManager.isStockGridExist(mStockGridSell)) {
			mDatabaseManager.insertStockGrid(mStockGridSell);
		} else {
			mDatabaseManager.updateStockGrid(mStockGridSell, mStockGridSell.getContentValues());
		}
		mStock.setGridSell(mStockGridSell.getPrice());
	}

	public double getBuyPrice() {
		return mStockGridBuy.getPrice();
	}

	public double getSellPrice() {
		return mStockGridSell.getPrice();
	}

	public double getSellProfit() {
		mDatabaseManager.getStockDealList(mStock, mStockDealList);
		if (mStockDealList == null || mStockDealList.size() == 0) {
			return 0;
		}
		return mStockDealList.get(mStockDealList.size() - 1).getProfit();
	}

	public String getBuyNotifyString() {
		return mStockGridBuy.toNotifyString();
	}

	public String getSellNotifyString() {
		return mStockGridSell.toNotifyString();
	}

	private static class Holder {
		private static final GridAnalyzer INSTANCE = new GridAnalyzer();
	}
}
