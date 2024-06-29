package com.android.orion.manager;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.orion.application.OrionApplication;
import com.android.orion.database.Stock;
import com.android.orion.interfaces.StockFavoriteListener;
import com.android.orion.interfaces.StockListListener;

import java.util.ArrayList;

public class StockManager {

    private static Context mContext = OrionApplication.getContext();
    private static StockManager mInstance;

    ArrayList<StockListListener> mStockListListener = new ArrayList<>();
    ArrayList<StockFavoriteListener> mStockFavoriteListener = new ArrayList<>();

    public static synchronized StockManager getInstance() {
        if (mInstance == null) {
            mInstance = new StockManager();
        }
        return mInstance;
    }

    public void registerStockListListener(StockListListener listener) {
        if (listener == null) {
            return;
        }
        if (!mStockListListener.contains(listener)) {
            mStockListListener.add(listener);
        }
    }

    public void unregisterStockListListener(StockListListener listener) {
        if (listener == null) {
            return;
        }
        if (mStockListListener.contains(listener)) {
            mStockListListener.remove(listener);
        }
    }

    public void registerStockFavoriteListener(StockFavoriteListener listener) {
        if (listener == null) {
            return;
        }
        if (!mStockFavoriteListener.contains(listener)) {
            mStockFavoriteListener.add(listener);
        }
    }

    public void unregisterStockFavoriteListener(StockFavoriteListener listener) {
        if (listener == null) {
            return;
        }
        if (mStockFavoriteListener.contains(listener)) {
            mStockFavoriteListener.remove(listener);
        }
    }

    public void onStockAdd(Stock stock) {
        if (stock == null) {
            return;
        }
        for (StockListListener listener: mStockListListener) {
            listener.onStockAdd(stock);
        }
    }

    public void onStockRemove(Stock stock) {
        if (stock == null) {
            return;
        }
        for (StockListListener listener: mStockListListener) {
            listener.onStockRemove(stock);
        }
    }

    public void onStockFavoriteAdd(Stock stock) {
        if (stock == null) {
            return;
        }
        for (StockFavoriteListener listener: mStockFavoriteListener) {
            listener.onStockFavoriteAdd(stock);
        }
    }

    public void onStockFavoriteRemove(Stock stock) {
        if (stock == null) {
            return;
        }
        for (StockFavoriteListener listener: mStockFavoriteListener) {
            listener.onStockFavoriteRemove(stock);
        }
    }
}
