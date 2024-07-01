package com.android.orion.manager;

import android.content.Context;

import com.android.orion.application.OrionApplication;
import com.android.orion.database.Stock;
import com.android.orion.interfaces.StockEditListener;
import com.android.orion.interfaces.StockListChangedListener;

import java.util.ArrayList;

public class StockManager {

    private static Context mContext = OrionApplication.getContext();
    private static StockManager mInstance;

    ArrayList<StockEditListener> mStockEditListener = new ArrayList<>();
    ArrayList<StockListChangedListener> mStockListChangedListener = new ArrayList<>();

    public static synchronized StockManager getInstance() {
        if (mInstance == null) {
            mInstance = new StockManager();
        }
        return mInstance;
    }

    public void registerStockEditListener(StockEditListener listener) {
        if (listener == null) {
            return;
        }
        if (!mStockEditListener.contains(listener)) {
            mStockEditListener.add(listener);
        }
    }

    public void onStockAddFavorite(Stock stock) {
        if (stock == null) {
            return;
        }
        for (StockEditListener listener: mStockEditListener) {
            listener.onStockAddFavorite(stock);
        }
    }

    public void onStockRemoveFavorite(Stock stock) {
        if (stock == null) {
            return;
        }
        for (StockEditListener listener: mStockEditListener) {
            listener.onStockRemoveFavorite(stock);
        }
    }

    public void unregisterStockEditListener(StockEditListener listener) {
        if (listener == null) {
            return;
        }
        if (mStockEditListener.contains(listener)) {
            mStockEditListener.remove(listener);
        }
    }

    public void registerStockListChangedListener(StockListChangedListener listener) {
        if (listener == null) {
            return;
        }
        if (!mStockListChangedListener.contains(listener)) {
            mStockListChangedListener.add(listener);
        }
    }

    public void onStockAdd(Stock stock) {
        if (stock == null) {
            return;
        }
        for (StockListChangedListener listener: mStockListChangedListener) {
            listener.onStockAdd(stock);
        }
    }

    public void onStockRemove(Stock stock) {
        if (stock == null) {
            return;
        }
        for (StockListChangedListener listener: mStockListChangedListener) {
            listener.onStockRemove(stock);
        }
    }

    public void unregisterStockListChangedListener(StockListChangedListener listener) {
        if (listener == null) {
            return;
        }
        if (mStockListChangedListener.contains(listener)) {
            mStockListChangedListener.remove(listener);
        }
    }
}
