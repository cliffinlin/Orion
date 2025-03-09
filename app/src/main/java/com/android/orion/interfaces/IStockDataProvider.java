package com.android.orion.interfaces;

import android.content.SharedPreferences;
import android.net.Uri;

import com.android.orion.database.Stock;

import java.util.ArrayList;

public interface IStockDataProvider {

	void download();

	void download(Stock stock);

	int downloadStockHSA();

	int downloadStockInformation(Stock stock);

	int downloadStockFinancial(Stock stock);

	int downloadStockBonus(Stock stock);

	int downloadStockShare(Stock stock);

	int downloadStockDataHistory(Stock stock);

	int downloadStockRealTime(Stock stock);

	int downloadStockDataRealTime(Stock stock);

	void onDestroy();

	void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key);

	void registerAnalyzeListener(AnalyzeListener listener);

	void unRegisterAnalyzeListener(AnalyzeListener listener);

	void registerDownloadListener(DownloadListener listener);

	void unRegisterDownloadListener(DownloadListener listener);

	void importTDXDataFile(ArrayList<Uri> mUriList);
}
