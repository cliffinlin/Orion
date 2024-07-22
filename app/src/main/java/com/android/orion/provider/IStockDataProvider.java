package com.android.orion.provider;

import com.android.orion.database.Stock;

public interface IStockDataProvider {

	int downloadStockHSA();

	int downloadStockInformation(Stock stock);

	int downloadStockFinancial(Stock stock);

	int downloadShareBonus(Stock stock);

	int downloadTotalShare(Stock stock);

	int downloadStockDataHistory(Stock stock);

	int downloadStockRealTime(Stock stock);

	int downloadStockDataRealTime(Stock stock);
}
