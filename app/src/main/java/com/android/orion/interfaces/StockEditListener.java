package com.android.orion.interfaces;

import com.android.orion.database.Stock;

public interface StockEditListener {
    void onStockAddFavorite(Stock stock);

    void onStockRemoveFavorite(Stock stock);
}
