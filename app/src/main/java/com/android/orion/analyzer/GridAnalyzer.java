package com.android.orion.analyzer;

import android.text.TextUtils;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDeal;
import com.android.orion.database.StockGrid;
import com.android.orion.database.StockPerceptron;
import com.android.orion.database.StockTrend;
import com.android.orion.manager.DatabaseManager;
import com.android.orion.provider.StockPerceptronProvider;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.Calendar;

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

	void analyze(Stock stock) {
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

		mStockGridBuy.setSE(mStock.getSE());
		mStockGridBuy.setCode(mStock.getCode());
		mStockGridBuy.setName(mStock.getName());
		mStockGridBuy.setType(StockGrid.TYPE_BUY);
		mStockGridBuy.setHold(mStock.getHold());
		mStockGridBuy.setGridGap(mStock.getGridGap());
		mStockGridBuy.setVolume(mStockDealList.get(0).getVolume());
		mStockGridBuy.setGridBase(mStockDealList.get(0).getBuy());
		mStockGridBuy.setupPrice();
		mStockGridBuy.setupValue();
		mStock.setGridBuy(mStockGridBuy.getPrice());
		if (!mDatabaseManager.isStockGridExist(mStockGridBuy)) {
			mDatabaseManager.insertStockGrid(mStockGridBuy);
		} else {
			mDatabaseManager.updateStockGrid(mStockGridBuy, mStockGridBuy.getContentValues());
		}

		mStockGridSell.setSE(mStock.getSE());
		mStockGridSell.setCode(mStock.getCode());
		mStockGridSell.setName(mStock.getName());
		mStockGridSell.setType(StockGrid.TYPE_SELL);
		mStockGridSell.setHold(mStock.getHold());
		mStockGridSell.setGridGap(mStock.getGridGap());
		mStockGridSell.setVolume(mStockDealList.get(mStockDealList.size()-1).getVolume());
		mStockGridSell.setGridBase(mStockDealList.get(mStockDealList.size()-1).getBuy());
		mStockGridSell.setupPrice();
		mStockGridSell.setupValue();
		mStock.setGridSell(mStockGridSell.getPrice());
		if (!mDatabaseManager.isStockGridExist(mStockGridSell)) {
			mDatabaseManager.insertStockGrid(mStockGridSell);
		} else {
			mDatabaseManager.updateStockGrid(mStockGridSell, mStockGridSell.getContentValues());
		}
	}

	private static class Holder {
		private static final GridAnalyzer INSTANCE = new GridAnalyzer();
	}
}
