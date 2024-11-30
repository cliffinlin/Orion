package com.android.orion.interfaces;

import com.android.orion.database.Stock;

public interface StockListener {
	void onAddFavorite(Stock stock);

	void onRemoveFavorite(Stock stock);

	void onAddStock(Stock stock);

	void onRemoveStock(Stock stock);
}
