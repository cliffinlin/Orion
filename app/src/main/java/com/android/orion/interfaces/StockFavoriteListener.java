package com.android.orion.interfaces;

import com.android.orion.database.Stock;

public interface StockFavoriteListener {
    void onStockFavoriteAdd(Stock stock);

    void onStockFavoriteRemove(Stock stock);
}
