package com.android.orion;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Setting;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.utility.Utility;
import com.android.volley.RequestQueue;

public abstract class StockDataProvider extends StockAnalyzer {
	// WifiLockManager mWifiLockManager = null;
	RequestQueue mRequestQueue;
	Set<String> mCurrentRequests = new HashSet<String>();

	abstract int getAvailableHistoryLength(String period);

	abstract String getStockHSAURLString();

	abstract void handleResponseStockHSA(String response);

	abstract String getStockRealTimeURLString(Stock stock);

	abstract void handleResponseStockRealTime(Stock stock, String response);

	abstract String getStockDataHistoryURLString(Stock stock,
			StockData stockData, int len);

	abstract void handleResponseStockDataHistory(Stock stock,
			StockData stockData, String response);

	abstract String getStockDataRealTimeURLString(Stock stock);

	abstract void handleResponseStockDataRealTime(Stock stock,
			StockData stockData, String response);

	public StockDataProvider(Context context) {
		super(context);

		mRequestQueue = VolleySingleton.getInstance(
				mContext.getApplicationContext()).getRequestQueue();
		mCurrentRequests.clear();
		/*
		 * if (mWifiLockManager == null) { mWifiLockManager = new
		 * WifiLockManager(mContext); }
		 */
	}

	void sendBroadcast(String action, Bundle bundle) {
		Intent intent = new Intent();

		if (!TextUtils.isEmpty(action)) {
			intent.setAction(action);
		}

		if ((bundle != null) && (!bundle.isEmpty())) {
			intent.putExtras(bundle);
		}

		mLocalBroadcastManager.sendBroadcast(intent);
	}

	void loadStockArrayMapFavorite() {
		loadStockArrayMap(selectStock(Constants.STOCK_FLAG_MARK_FAVORITE),
				null, null, mStockArrayMapFavorite);

		insertStockMatchFromFavoriteMap();
	}

	void downloadStockIndexes() {
		String selection;
		String urlString;
		List<Stock> stockList = null;

		if (!Utility.isNetworkConnected(mContext)) {
			removeAllCurrrentRequests();
			return;
		}

		initStockIndexes();

		selection = DatabaseContract.Stock.COLUMN_CLASSES + " = " + "\'"
				+ Constants.STOCK_FLAG_CLASS_INDEXES + "\'" + " AND "
				+ DatabaseContract.COLUMN_NAME + " = \'\'";
		stockList = loadStockList(selection, null, null);
		if ((stockList == null) || (stockList.size() == 0)) {
			return;
		}

		for (Stock stock : stockList) {
			urlString = getStockRealTimeURLString(stock);
			if (addToCurrentRequests(urlString)) {
				Utility.Log("getStockRealTimeURLString:" + urlString);
				StockRealTimeDownloader downloader = new StockRealTimeDownloader(
						urlString);
				downloader.setStock(stock);
				mRequestQueue.add(downloader.mStringRequest);
			}
		}
	}

	void downloadStockHSA() {
		String urlString = "";

		if (!Utility.isNetworkConnected(mContext)) {
			removeAllCurrrentRequests();
			return;
		}

		if (Utility.getCurrentDateString().equals(
				mStockDatabaseManager
						.getSettingString(Setting.KEY_STOCK_HSA_UPDATED))) {
			return;
		}

		urlString = getStockHSAURLString();
		if (addToCurrentRequests(urlString)) {
			Utility.Log("getStockHSAURLString:" + urlString);
			StockHSADownloader downloader = new StockHSADownloader(urlString);
			mRequestQueue.add(downloader.mStringRequest);
			mStockDatabaseManager.saveSetting(Setting.KEY_STOCK_HSA_UPDATED,
					Utility.getCurrentDateString());
		}
	}

	void downloadStock(Bundle bundle) {
		int executeType = Constants.EXECUTE_TYPE_NONE;
		Stock stock = null;

		if (!Utility.isNetworkConnected(mContext)) {
			removeAllCurrrentRequests();
			return;
		}

		executeType = bundle.getInt(Constants.EXTRA_EXECUTE_TYPE,
				Constants.EXECUTE_TYPE_NONE);

		stock = getStock(bundle);

		if (stock != null) {
			downloadStockRealTime(executeType, stock);
			downloadStockDataHistory(executeType, stock);
			downloadStockDataRealTime(executeType, stock);
		} else {
			downloadStockRealTime(executeType);
			downloadStockDataHistory(executeType);
			downloadStockDataRealTime(executeType);
		}
	}

	void downloadStockRealTime(int executeType) {
		for (Stock stock : mStockArrayMapFavorite.values()) {
			downloadStockRealTime(executeType, stock);
		}
	}

	void downloadStockRealTime(int executeType, Stock stock) {
		String urlString;
		String modified = "";

		if (stock == null) {
			return;
		}

		modified = stock.getModified();
		if (TextUtils.isEmpty(modified)) {
			modified = stock.getCreated();
		}

		if (executeType == Constants.EXECUTE_IMMEDIATE
				|| executeTypeOf(executeType, Constants.EXECUTE_SCHEDULE_1MIN)
				|| Utility.isOutOfDate(modified)) {
			urlString = getStockRealTimeURLString(stock);
			if (addToCurrentRequests(urlString)) {
				Utility.Log("getStockRealTimeURLString:" + urlString);
				StockRealTimeDownloader downloader = new StockRealTimeDownloader(
						urlString);
				downloader.setStock(stock);
				mRequestQueue.add(downloader.mStringRequest);
			}
		}
	}

	void downloadStockDataHistory(int executeType) {
		for (Stock stock : mStockArrayMapFavorite.values()) {
			downloadStockDataHistory(executeType, stock);
		}
	}

	void downloadStockDataHistory(int executeType, Stock stock) {
		if (stock == null) {
			return;
		}

		for (String period : Constants.PERIODS) {
			if (Utility.getSettingBoolean(mContext, period)) {
				downloadStockDataHistory(executeType, stock, period);
			}
		}
	}

	void downloadStockDataHistory(int executeType, Stock stock, String period) {
		int len = 0;
		String urlString;
		StockData stockData = StockData.obtain(period);

		if (stock == null) {
			return;
		}

		stockData.setStockId(stock.getId());

		len = getDownloadStockDataLength(executeType, stockData);
		if (len > 0) {
			urlString = getStockDataHistoryURLString(stock, stockData, len);
			if (addToCurrentRequests(urlString)) {
				Utility.Log("getStockDataHistoryURLString:" + urlString);
				StockDataHistoryDownloader downloader = new StockDataHistoryDownloader(
						urlString);
				downloader.setStock(stock);
				downloader.setStockData(stockData);
				downloader.setExecuteType(executeType);
				mRequestQueue.add(downloader.mStringRequest);
			}
		}
	}

	void downloadStockDataRealTime(int executeType) {
		for (Stock stock : mStockArrayMapFavorite.values()) {
			downloadStockDataRealTime(executeType, stock);
		}
	}

	void downloadStockDataRealTime(int executeType, Stock stock) {
		String period = Constants.PERIOD_DAY;
		String urlString;
		StockData stockData = StockData.obtain(period);

		if ((stock == null) || !Utility.getSettingBoolean(mContext, period)) {
			return;
		}

		stockData.setStockId(stock.getId());

		if (executeType == Constants.EXECUTE_IMMEDIATE
				|| Utility.isOpeningHours(Calendar.getInstance())) {
			urlString = getStockDataRealTimeURLString(stock);
			if (addToCurrentRequests(urlString)) {
				Utility.Log("getStockDataRealTimeURLString:" + urlString);
				StockDataRealTimeDownloader downloader = new StockDataRealTimeDownloader(
						urlString);
				downloader.setStock(stock);
				downloader.setStockData(stockData);
				downloader.setExecuteType(executeType);
				mRequestQueue.add(downloader.mStringRequest);
			}
		}
	}

	int getDownloadHistoryLengthDefault(String period) {
		int result = 0;
		int availableHistoryLength = 0;

		availableHistoryLength = getAvailableHistoryLength(period);

		// __TEST_CASE__
		// availableHistoryLength = 80;
		// __TEST_CASE__

		result = Constants.DOWNLOAD_HISTORY_LENGTH_DEFAULT
				* getPeriodCoefficient(period);

		if (availableHistoryLength > 0) {
			if (result > availableHistoryLength) {
				result = availableHistoryLength;
			}
		} else if (availableHistoryLength == Constants.DOWNLOAD_HISTORY_LENGTH_NONE) {
			result = 0;
		} else if (availableHistoryLength == Constants.DOWNLOAD_HISTORY_LENGTH_UNLIMITED) {
		}

		return result;
	}

	void removeStockDataRedundant(Cursor cursor, int defaultValue) {
		int i = 0;
		StockData stockData = StockData.obtain();

		if (cursor == null) {
			return;
		}

		if (defaultValue <= 0) {
			return;
		}

		cursor.moveToFirst();

		i = cursor.getCount() - defaultValue;
		while (i > 0) {
			stockData.set(cursor);
			mStockDatabaseManager.deleteStockData(stockData);
			cursor.moveToNext();
			i--;
		}
	}

	int getScheduleMaxLengthAt60Min(int scheduleMinutes, String period) {
		int result = 0;

		if (period.equals(Constants.PERIOD_5MIN)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_5MIN;
		} else if (period.equals(Constants.PERIOD_15MIN)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_15MIN;
		} else if (period.equals(Constants.PERIOD_30MIN)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_30MIN;
		} else if (period.equals(Constants.PERIOD_60MIN)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_60MIN;
		}

		return result;
	}

	int getScheduleMaxLengthAt30Min(int scheduleMinutes, String period) {
		int result = 0;

		if (period.equals(Constants.PERIOD_5MIN)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_5MIN;
		} else if (period.equals(Constants.PERIOD_15MIN)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_15MIN;
		} else if (period.equals(Constants.PERIOD_30MIN)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_30MIN;
		}

		return result;
	}

	int getScheduleMaxLengthAt15Min(int scheduleMinutes, String period) {
		int result = 0;

		if (period.equals(Constants.PERIOD_5MIN)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_5MIN;
		} else if (period.equals(Constants.PERIOD_15MIN)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_15MIN;
		}

		return result;
	}

	int getDownloadStockDataLength(int executeType, StockData stockData) {
		int result = 0;
		int defaultValue = 0;
		int scheduleMinutes = 0;
		long stockId = 0;
		String period = "";
		String modified = "";
		String selection = null;
		String sortOrder = null;
		Cursor cursor = null;

		if (stockData == null) {
			return result;
		}

		stockId = stockData.getStockId();
		period = stockData.getPeriod();

		defaultValue = getDownloadHistoryLengthDefault(period);

		try {
			selection = mStockDatabaseManager.getStockDataSelection(stockId,
					period);
			sortOrder = mStockDatabaseManager.getStockDataOrder();
			cursor = mStockDatabaseManager.queryStockData(selection, null,
					sortOrder);

			if (cursor == null) {
				return result;
			}

			if (cursor.getCount() == 0) {
				result = defaultValue;
			} else {
				cursor.moveToLast();
				stockData.set(cursor);
				modified = stockData.getModified();
				if (TextUtils.isEmpty(modified)) {
					modified = stockData.getCreated();
				}
				scheduleMinutes = Utility.getScheduleMinutes();
				if (Utility.isOutOfDateNotToday(modified)) {
					removeStockDataRedundant(cursor, defaultValue);
					result = defaultValue;
				} else if (Utility.isOutOfDateFirstHalf(modified)) {
					result = getScheduleMaxLengthAt60Min(scheduleMinutes,
							period);
				} else if (Utility.isOutOfDateSecendHalf(modified)) {
					result = getScheduleMaxLengthAt60Min(scheduleMinutes,
							period);
				} else if ((executeType & Constants.EXECUTE_SCHEDULE_60MIN) == Constants.EXECUTE_SCHEDULE_60MIN) {
					result = getScheduleMaxLengthAt60Min(scheduleMinutes,
							period);
				} else if ((executeType & Constants.EXECUTE_SCHEDULE_30MIN) == Constants.EXECUTE_SCHEDULE_30MIN) {
					result = getScheduleMaxLengthAt30Min(scheduleMinutes,
							period);
				} else if ((executeType & Constants.EXECUTE_SCHEDULE_15MIN) == Constants.EXECUTE_SCHEDULE_15MIN) {
					result = getScheduleMaxLengthAt15Min(scheduleMinutes,
							period);
				} else if ((executeType & Constants.EXECUTE_SCHEDULE_5MIN) == Constants.EXECUTE_SCHEDULE_5MIN) {
					result = 1;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}

		return result;
	}

	boolean executeTypeOf(int executeType, final int type) {
		boolean result = false;

		if ((executeType & type) == type) {
			result = true;
		}

		return result;
	}

	boolean addToCurrentRequests(String urlString) {
		boolean result = false;

		synchronized (mCurrentRequests) {
			if (!mCurrentRequests.contains(urlString)) {
				mCurrentRequests.add(urlString);
				result = true;
			}
		}

		return result;
	}

	void removeFromCurrrentRequests(String urlString) {
		synchronized (mCurrentRequests) {
			if (mCurrentRequests.contains(urlString)) {
				mCurrentRequests.remove(urlString);
			}
		}
	}

	void removeAllCurrrentRequests() {
		synchronized (mCurrentRequests) {
			mCurrentRequests.clear();
		}
	}

	Stock getStock(Bundle bundle) {
		String se = bundle.getString(Constants.EXTRA_STOCK_SE);
		String code = bundle.getString(Constants.EXTRA_STOCK_CODE);
		Stock stock = null;

		stock = mStockArrayMapFavorite.get(se + code);

		return stock;
	}

	public class StockHSADownloader extends VolleyStringDownloader {

		public StockHSADownloader() {
			super();
		}

		public StockHSADownloader(String urlString) {
			super(urlString);
		}

		@Override
		void handleResponse(String response) {
			handleResponseStockHSA(response);
			fixPinyin();
			removeFromCurrrentRequests(mStringRequest.getUrl());
		}
	}

	public class StockRealTimeDownloader extends VolleyStringDownloader {
		public Stock mStock = null;

		public StockRealTimeDownloader() {
			super();
		}

		public StockRealTimeDownloader(String urlString) {
			super(urlString);
		}

		public void setStock(Stock stock) {
			if (mStock == null) {
				mStock = Stock.obtain();
			}
			mStock.set(stock);
		}

		@Override
		public void handleResponse(String response) {
			removeFromCurrrentRequests(mStringRequest.getUrl());
			handleResponseStockRealTime(mStock, response);
			mStockDatabaseManager.updateStockDeal(mStock);
			Bundle bundle = new Bundle();
			bundle.putInt(Constants.EXTRA_SERVICE_TYPE,
					Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE_REALTIME);
			bundle.putLong(Constants.EXTRA_STOCK_ID, mStock.getId());
			sendBroadcast(Constants.ACTION_SERVICE_FINISHED, bundle);
		}
	}

	public class StockDataHistoryDownloader extends VolleyStringDownloader {
		public Stock mStock = null;
		public StockData mStockData = null;
		public int mExecuteType = 0;

		public StockDataHistoryDownloader() {
			super();
		}

		public StockDataHistoryDownloader(String urlString) {
			super(urlString);
		}

		public void setStock(Stock stock) {
			if (mStock == null) {
				mStock = Stock.obtain();
			}
			mStock.set(stock);
		}

		public void setStockData(StockData stockData) {
			if (mStockData == null) {
				mStockData = StockData.obtain();
			}
			mStockData.set(stockData);
		}

		public void setExecuteType(int executeType) {
			mExecuteType = executeType;
		}

		@Override
		public void handleResponse(String response) {
			removeFromCurrrentRequests(mStringRequest.getUrl());
			handleResponseStockDataHistory(mStock, mStockData, response);
			analyze(mStock, mStockData.getPeriod(),
					getStockDataList(mStock, mStockData.getPeriod()));
			Bundle bundle = new Bundle();
			bundle.putInt(Constants.EXTRA_SERVICE_TYPE,
					Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE_DATA_HISTORY);
			bundle.putLong(Constants.EXTRA_STOCK_ID, mStock.getId());
			sendBroadcast(Constants.ACTION_SERVICE_FINISHED, bundle);
		}
	}

	public class StockDataRealTimeDownloader extends VolleyStringDownloader {
		public Stock mStock = null;
		public StockData mStockData = null;
		public int mExecuteType = 0;

		public StockDataRealTimeDownloader() {
			super();
		}

		public StockDataRealTimeDownloader(String urlString) {
			super(urlString);
		}

		public void setStock(Stock stock) {
			if (mStock == null) {
				mStock = Stock.obtain();
			}
			mStock.set(stock);
		}

		public void setStockData(StockData stockData) {
			if (mStockData == null) {
				mStockData = StockData.obtain();
			}
			mStockData.set(stockData);
		}

		public void setExecuteType(int executeType) {
			mExecuteType = executeType;
		}

		@Override
		public void handleResponse(String response) {
			removeFromCurrrentRequests(mStringRequest.getUrl());
			handleResponseStockDataRealTime(mStock, mStockData, response);
			analyze(mStock, mStockData.getPeriod(),
					getStockDataList(mStock, mStockData.getPeriod()));
			Bundle bundle = new Bundle();
			bundle.putInt(Constants.EXTRA_SERVICE_TYPE,
					Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE_DATA_REALTIME);
			bundle.putLong(Constants.EXTRA_STOCK_ID, mStock.getId());
			sendBroadcast(Constants.ACTION_SERVICE_FINISHED, bundle);
		}
	}
}
