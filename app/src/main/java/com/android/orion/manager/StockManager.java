package com.android.orion.manager;

import android.content.Context;

import com.android.orion.application.MainApplication;
import com.android.orion.database.Stock;
import com.android.orion.interfaces.StockListener;

import java.util.ArrayList;

public class StockManager {

	ArrayList<StockListener> mStockListener = new ArrayList<>();

	private StockManager() {
	}

	public static StockManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void registerStockListener(StockListener listener) {
		if (listener == null) {
			return;
		}
		if (!mStockListener.contains(listener)) {
			mStockListener.add(listener);
		}
	}

	public void onAddFavorite(Stock stock) {
		if (stock == null) {
			return;
		}
		for (StockListener listener : mStockListener) {
			listener.onAddFavorite(stock);
		}
	}

	public void onRemoveFavorite(Stock stock) {
		if (stock == null) {
			return;
		}
		for (StockListener listener : mStockListener) {
			listener.onRemoveFavorite(stock);
		}
	}

	public void onAddStock(Stock stock) {
		if (stock == null) {
			return;
		}
		for (StockListener listener : mStockListener) {
			listener.onAddStock(stock);
		}
	}

	public void onRemoveStock(Stock stock) {
		if (stock == null) {
			return;
		}
		for (StockListener listener : mStockListener) {
			listener.onRemoveStock(stock);
		}
	}

	public void unregisterStockListener(StockListener listener) {
		if (listener == null) {
			return;
		}
		mStockListener.remove(listener);
	}

	private static class SingletonHolder {
		private static final StockManager INSTANCE = new StockManager();
	}
}
