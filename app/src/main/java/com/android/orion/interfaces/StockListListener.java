package com.android.orion.interfaces;

import com.android.orion.database.Stock;

public interface StockListListener {
    void onStockAdd(Stock stock);

    void onStockRemove(Stock stock);
}
