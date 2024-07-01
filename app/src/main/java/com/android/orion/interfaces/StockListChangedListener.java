package com.android.orion.interfaces;

import com.android.orion.database.Stock;

public interface StockListChangedListener {
    void onStockAdd(Stock stock);

    void onStockRemove(Stock stock);
}
