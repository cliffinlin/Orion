package com.android.orion;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Process;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.FinancialData;
import com.android.orion.database.IPO;
import com.android.orion.database.Setting;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.utility.Market;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.Utility;
import com.android.volley.RequestQueue;

public abstract class StockDataProvider extends StockAnalyzer {
	static final String TAG = Constants.TAG + " "
			+ StockDataProvider.class.getSimpleName();

	long mLastSendBroadcast = 0;
	ArrayMap<String, Stock> mStockArrayMapFavorite = new ArrayMap<String, Stock>();
	RequestQueue mRequestQueue;
	Set<String> mCurrentRequests = new HashSet<String>();

	abstract int getAvailableHistoryLength(String period);

	abstract String getStockHSAURLString();

	abstract void handleResponseStockHSA(String response);

	abstract String getStockInformationURLString(Stock stock);

	abstract void handleResponseStockInformation(Stock stock, String response);

	abstract String getStockRealTimeURLString(Stock stock);

	abstract void handleResponseStockRealTime(Stock stock, String response);

	abstract String getStockDataHistoryURLString(Stock stock,
			StockData stockData, int len);

	abstract void handleResponseStockDataHistory(Stock stock,
			StockData stockData, String response);

	abstract String getStockDataRealTimeURLString(Stock stock);

	abstract void handleResponseStockDataRealTime(Stock stock,
			StockData stockData, String response);

	abstract String getFinancialDataURLString(Stock stock);

	abstract void handleResponseFinancialData(Stock stock,
			FinancialData financialData, String response);

	abstract String getShareBonusURLString(Stock stock);

	abstract void handleResponseShareBonus(Stock stock, ShareBonus shareBonus,
			String response);

	abstract String getIPOURLString();

	abstract void handleResponseIPO(IPO ipo, String response);

	private HandlerThread mHandlerThread;

	OkHttpClient mOkHttpClient = new OkHttpClient();

	public StockDataProvider(Context context) {
		super(context);

		mHandlerThread = new HandlerThread("StockDataProvider",
				Process.THREAD_PRIORITY_BACKGROUND);
		mHandlerThread.start();

		mRequestQueue = VolleySingleton.getInstance(
				mContext.getApplicationContext()).getRequestQueue();

		removeAllCurrrentRequests();
	}

	void sendBroadcast(String action, int serviceType, long stockID) {
		long delt = System.currentTimeMillis() - mLastSendBroadcast;
		if (delt > Constants.DEFAULT_SEND_BROADCAST_INTERAL) {
			mLastSendBroadcast = System.currentTimeMillis();

			Bundle bundle = new Bundle();
			bundle.putInt(Constants.EXTRA_SERVICE_TYPE, serviceType);
			bundle.putLong(Constants.EXTRA_STOCK_ID, stockID);

			sendBroadcast(action, bundle);
		}
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

	void downloadStockIndexes() {
		String selection;
		String urlString;
		List<Stock> stockList = null;

		if (!Utility.isNetworkConnected(mContext)) {
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
				Log.d(TAG, "getStockRealTimeURLString:" + urlString);
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
			return;
		}

		if (Utility.getCurrentDateString().equals(
				mStockDatabaseManager
						.getSettingString(Setting.KEY_STOCK_HSA_UPDATED))) {
			return;
		}

		urlString = getStockHSAURLString();
		if (addToCurrentRequests(urlString)) {
			Log.d(TAG, "getStockHSAURLString:" + urlString);
			StockHSADownloader downloader = new StockHSADownloader(urlString);
			mRequestQueue.add(downloader.mStringRequest);
			mStockDatabaseManager.saveSetting(Setting.KEY_STOCK_HSA_UPDATED,
					Utility.getCurrentDateString());
		}
	}

	void downloadStock(Bundle bundle) {
		String se = "";
		String code = "";
		Stock stock = null;

		if (!Utility.isNetworkConnected(mContext)) {
			return;
		}

		if (!Preferences.readBoolean(mContext, Constants.SETTING_KEY_ALARM,
				false)) {
			return;
		}

		removeAllCurrrentRequests();

		loadStockArrayMap(selectStock(Constants.STOCK_FLAG_MARK_FAVORITE),
				null, null, mStockArrayMapFavorite);

		se = bundle.getString(Constants.EXTRA_STOCK_SE);
		code = bundle.getString(Constants.EXTRA_STOCK_CODE);
		stock = mStockArrayMapFavorite.get(se + code);

		downloadStock(stock);
	}

	void downloadStock(Stock stock) {
		DownloadStockAsyncTask task = new DownloadStockAsyncTask();

		task.setStock(stock);
		task.execute();
	}

	void downloadIPO() {
		DownloadIPOAsyncTask task = new DownloadIPOAsyncTask();

		task.setIPO(new IPO());
		task.execute();
	}

	void downloadStockRealTime(int executeType) {
		if (mStockArrayMapFavorite.size() == 0) {
			return;
		}

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

		mStockDatabaseManager.getStock(stock);

		modified = stock.getModified();
		if (TextUtils.isEmpty(modified)) {
			modified = stock.getCreated();
		}

		if (executeType == Constants.EXECUTE_IMMEDIATE
				|| executeTypeOf(executeType, Constants.EXECUTE_SCHEDULE)
				|| Market.isOutOfDate(modified)) {
			urlString = getStockRealTimeURLString(stock);
			if (addToCurrentRequests(urlString)) {
				Log.d(TAG, "getStockRealTimeURLString:" + urlString);
				StockRealTimeDownloader downloader = new StockRealTimeDownloader(
						urlString);
				downloader.setStock(stock);
				mRequestQueue.add(downloader.mStringRequest);
			}
		}
	}

	void downloadStockDataHistory(int executeType) {
		if (mStockArrayMapFavorite.size() == 0) {
			return;
		}

		for (Stock stock : mStockArrayMapFavorite.values()) {
			downloadStockDataHistory(executeType, stock);
		}
	}

	void downloadStockDataHistory(int executeType, Stock stock) {
		if (stock == null) {
			return;
		}

		mStockDatabaseManager.getStock(stock);

		for (String period : Constants.PERIODS) {
			if (Preferences.readBoolean(mContext, period, false)) {
				downloadStockDataHistory(executeType, stock, period);
			}
		}
	}

	void downloadStockDataHistory(int executeType, Stock stock, String period) {
		int len = 0;
		String urlString;
		StockData stockData = new StockData(period);

		if (stock == null) {
			return;
		}

		if (executeType == Constants.EXECUTE_IMMEDIATE
				|| executeTypeOf(executeType, Constants.EXECUTE_SCHEDULE)) {
			stockData.setStockId(stock.getId());
			len = getDownloadStockDataLength(executeType, stockData);
			if (len > 0) {
				urlString = getStockDataHistoryURLString(stock, stockData, len);
				if (addToCurrentRequests(urlString)) {
					Log.d(TAG, "getStockDataHistoryURLString:" + urlString);
					StockDataHistoryDownloader downloader = new StockDataHistoryDownloader(
							urlString);
					downloader.setStock(stock);
					downloader.setStockData(stockData);
					downloader.setExecuteType(executeType);
					mRequestQueue.add(downloader.mStringRequest);
				}
			}
		}
	}

	void downloadStockDataRealTime(int executeType) {
		if (mStockArrayMapFavorite.size() == 0) {
			return;
		}

		for (Stock stock : mStockArrayMapFavorite.values()) {
			downloadStockDataRealTime(executeType, stock);
		}
	}

	void downloadStockDataRealTime(int executeType, Stock stock) {
		String period = Constants.PERIOD_DAY;
		String urlString;
		StockData stockData = new StockData(period);

		if ((stock == null)
				|| !Preferences.readBoolean(mContext, period, false)) {
			return;
		}

		mStockDatabaseManager.getStock(stock);

		stockData.setStockId(stock.getId());

		if (executeType == Constants.EXECUTE_IMMEDIATE
				|| Market.isOpeningHours(Calendar.getInstance())) {
			urlString = getStockDataRealTimeURLString(stock);
			if (addToCurrentRequests(urlString)) {
				Log.d(TAG, "getStockDataRealTimeURLString:" + urlString);
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
		StockData stockData = new StockData();

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

	int getScheduleMaxLengthAtMin60(int scheduleMinutes, String period) {
		int result = 0;

		if (period.equals(Constants.PERIOD_MIN5)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_MIN5;
		} else if (period.equals(Constants.PERIOD_MIN15)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_MIN15;
		} else if (period.equals(Constants.PERIOD_MIN30)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_MIN30;
		} else if (period.equals(Constants.PERIOD_MIN60)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_MIN60;
		}

		return result;
	}

	int getScheduleMaxLengthAtMin30(int scheduleMinutes, String period) {
		int result = 0;

		if (period.equals(Constants.PERIOD_MIN5)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_MIN5;
		} else if (period.equals(Constants.PERIOD_MIN15)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_MIN15;
		} else if (period.equals(Constants.PERIOD_MIN30)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_MIN30;
		}

		return result;
	}

	int getScheduleMaxLengthAtMin15(int scheduleMinutes, String period) {
		int result = 0;

		if (period.equals(Constants.PERIOD_MIN5)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_MIN5;
		} else if (period.equals(Constants.PERIOD_MIN15)) {
			result = scheduleMinutes / Constants.SCHEDULE_INTERVAL_MIN15;
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
				scheduleMinutes = Market.getScheduleMinutes();
				if (Market.isOutOfDateNotToday(modified)) {
					removeStockDataRedundant(cursor, defaultValue);
					result = defaultValue;
				} else if (Market.isOutOfDateFirstHalf(modified)) {
					result = getScheduleMaxLengthAtMin60(scheduleMinutes,
							period);
				} else if (Market.isOutOfDateSecendHalf(modified)) {
					result = getScheduleMaxLengthAtMin60(scheduleMinutes,
							period);
				} else if ((executeType & Constants.EXECUTE_SCHEDULE_MIN60) == Constants.EXECUTE_SCHEDULE_MIN60) {
					result = getScheduleMaxLengthAtMin60(scheduleMinutes,
							period);
				} else if ((executeType & Constants.EXECUTE_SCHEDULE_MIN30) == Constants.EXECUTE_SCHEDULE_MIN30) {
					result = getScheduleMaxLengthAtMin30(scheduleMinutes,
							period);
				} else if ((executeType & Constants.EXECUTE_SCHEDULE_MIN15) == Constants.EXECUTE_SCHEDULE_MIN15) {
					result = getScheduleMaxLengthAtMin15(scheduleMinutes,
							period);
				} else if ((executeType & Constants.EXECUTE_SCHEDULE_MIN5) == Constants.EXECUTE_SCHEDULE_MIN5) {
					result = 1;
				} else if ((executeType & Constants.EXECUTE_IMMEDIATE) == Constants.EXECUTE_IMMEDIATE) {
					if (defaultValue == cursor.getCount()) {
						return 0;
					} else {
						result = defaultValue;
					}
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

	public class StockHSADownloader extends VolleyStringDownloader {

		public StockHSADownloader() {
			super();
		}

		public StockHSADownloader(String urlString) {
			super(urlString);
		}

		@Override
		void handleResponse(String response) {
			removeFromCurrrentRequests(mStringRequest.getUrl());
			handleResponseStockHSA(response);
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
				mStock = new Stock();
			}
			mStock.set(stock);
		}

		@Override
		public void handleResponse(String response) {
			removeFromCurrrentRequests(mStringRequest.getUrl());

			handleResponseStockRealTime(mStock, response);

			mStockDatabaseManager.updateStockDeal(mStock);
			mStockDatabaseManager
					.updateStock(mStock, mStock.getContentValues());

			sendBroadcast(Constants.ACTION_SERVICE_FINISHED,
					Constants.SERVICE_DATABASE_UPDATE, mStock.getId());
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
				mStock = new Stock();
			}
			mStock.set(stock);
		}

		public void setStockData(StockData stockData) {
			if (mStockData == null) {
				mStockData = new StockData();
			}
			mStockData.set(stockData);
		}

		public void setExecuteType(int executeType) {
			mExecuteType = executeType;
		}

		@Override
		public void handleResponse(String response) {
			removeFromCurrrentRequests(mStringRequest.getUrl());
			mStockDatabaseManager.getStock(mStock);
			handleResponseStockDataHistory(mStock, mStockData, response);
			analyze(mStock, mStockData.getPeriod(),
					getStockDataList(mStock, mStockData.getPeriod()));

			sendBroadcast(Constants.ACTION_SERVICE_FINISHED,
					Constants.SERVICE_DATABASE_UPDATE, mStock.getId());
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
				mStock = new Stock();
			}
			mStock.set(stock);
		}

		public void setStockData(StockData stockData) {
			if (mStockData == null) {
				mStockData = new StockData();
			}
			mStockData.set(stockData);
		}

		public void setExecuteType(int executeType) {
			mExecuteType = executeType;
		}

		@Override
		public void handleResponse(String response) {
			removeFromCurrrentRequests(mStringRequest.getUrl());
			mStockDatabaseManager.getStock(mStock);
			handleResponseStockDataRealTime(mStock, mStockData, response);
			analyze(mStock, mStockData.getPeriod(),
					getStockDataList(mStock, mStockData.getPeriod()));

			sendBroadcast(Constants.ACTION_SERVICE_FINISHED,
					Constants.SERVICE_DATABASE_UPDATE, mStock.getId());
		}
	}

	class DownloadAsyncTask extends AsyncTask<String, Void, String> {
		Stock mStock = null;

		String mAccessDeniedString = mContext.getResources().getString(
				R.string.access_denied);

		public void setStock(Stock stock) {
			if (stock == null) {
				mStock = null;
			} else {
				if (mStock == null) {
					mStock = new Stock();
				}
				mStock.set(stock);
			}
		}

		@Override
		protected String doInBackground(String... arg0) {
			return null;
		}

		@Override
		protected void onPostExecute(String string) {
			super.onPostExecute(string);
			if (!TextUtils.isEmpty(string)
					&& string.contains(mAccessDeniedString)) {
				Toast.makeText(
						mContext,
						mContext.getResources().getString(
								R.string.access_denied), Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	class DownloadStockAsyncTask extends DownloadAsyncTask {
		StockData mStockData = null;
		FinancialData mFinancialData = null;
		ShareBonus mShareBonus = null;

		String downloadStockInformation(Stock stock) {
			boolean needDownload = false;

			if (stock == null) {
				return "";
			}

			mStockDatabaseManager.getStock(stock);

			if (TextUtils.isEmpty(stock.getClases())) {
				needDownload = true;
			} else if (TextUtils.isEmpty(stock.getPinyin())) {
				needDownload = true;
			} else if (stock.getTotalShare() == 0) {
				needDownload = true;
				if (Constants.STOCK_CLASSES_INDEX.equals(stock.getClases())) {
					needDownload = false;
				}
			}

			if (!needDownload) {
				return "";
			}

			setStock(stock);

			String urlString = getStockInformationURLString(stock);
			String responseString = "";

			Log.d(TAG, "downloadStockInformation:" + urlString);

			Request.Builder builder = new Request.Builder();
			builder.url(urlString);
			Request request = builder.build();

			try {
				Response response = mOkHttpClient.newCall(request).execute();
				if (response != null) {
					responseString = response.body().string();
					handleResponseStockInformation(mStock, responseString);

					Thread.sleep(Constants.DEFAULT_DOWNLOAD_INTERVAL);

					sendBroadcast(Constants.ACTION_SERVICE_FINISHED,
							Constants.SERVICE_DATABASE_UPDATE, mStock.getId());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return responseString;
		}

		String downloadStockRealTime(Stock stock) {
			if (stock == null) {
				return "";
			}

			mStockDatabaseManager.getStock(stock);

			if (stock.getCreated().contains(Utility.getCurrentDateString())
					|| stock.getModified().contains(
							Utility.getCurrentDateString())) {
				if (stock.getPrice() > 0) {
					return "";
				}
			}

			setStock(stock);

			String urlString = getStockRealTimeURLString(stock);
			String responseString = "";

			Log.d(TAG, "downloadStockRealTime:" + urlString);

			Request.Builder builder = new Request.Builder();
			builder.url(urlString);
			Request request = builder.build();

			try {
				Response response = mOkHttpClient.newCall(request).execute();
				if (response != null) {
					responseString = response.body().string();
					handleResponseStockRealTime(mStock, responseString);

					mStockDatabaseManager.updateStockDeal(mStock);
					mStockDatabaseManager.updateStock(mStock,
							mStock.getContentValues());

					Thread.sleep(Constants.DEFAULT_DOWNLOAD_INTERVAL);

					sendBroadcast(Constants.ACTION_SERVICE_FINISHED,
							Constants.SERVICE_DATABASE_UPDATE, mStock.getId());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return responseString;
		}

		String downloadStockDataHistory(Stock stock) {
			String responseString = "";

			if (stock == null) {
				return "";
			}

			mStockDatabaseManager.getStock(stock);

			for (String period : Constants.PERIODS) {
				if (Preferences.readBoolean(mContext, period, false)) {
					responseString = downloadStockDataHistory(stock, period);
				}
			}

			return responseString;
		}

		String downloadStockDataHistory(Stock stock, String period) {
			int len = 0;
			mStockData = new StockData(period);

			if (stock == null) {
				return "";
			}

			mStockData.setStockId(stock.getId());
			len = getDownloadStockDataLength(Constants.EXECUTE_IMMEDIATE,
					mStockData);
			if (len <= 0) {
				return "";
			}

			setStock(stock);

			String urlString = getStockDataHistoryURLString(stock, mStockData,
					len);
			String responseString = "";

			Log.d(TAG, "downloadStockDataHistory:" + urlString);

			Request.Builder builder = new Request.Builder();
			builder.url(urlString);
			Request request = builder.build();

			try {
				Response response = mOkHttpClient.newCall(request).execute();
				if (response != null) {
					responseString = response.body().string();
					handleResponseStockDataHistory(mStock, mStockData,
							responseString);

					analyze(mStock, mStockData.getPeriod(),
							getStockDataList(mStock, mStockData.getPeriod()));

					Thread.sleep(Constants.DEFAULT_DOWNLOAD_INTERVAL);

					sendBroadcast(Constants.ACTION_SERVICE_FINISHED,
							Constants.SERVICE_DATABASE_UPDATE, mStock.getId());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return responseString;
		}

		String downloadStockDataRealTime(Stock stock) {
			String period = Constants.PERIOD_DAY;
			mStockData = new StockData(period);

			if ((stock == null)
					|| !Preferences.readBoolean(mContext, period, false)) {
				return "";
			}

			mStockData.setStockId(stock.getId());

			if (!Market.isOpeningHours(Calendar.getInstance())) {
				return "";
			}

			setStock(stock);

			String urlString = getStockDataRealTimeURLString(stock);
			;
			String responseString = "";

			Log.d(TAG, "downloadStockDataRealTime:" + urlString);

			Request.Builder builder = new Request.Builder();
			builder.url(urlString);
			Request request = builder.build();

			try {
				Response response = mOkHttpClient.newCall(request).execute();
				if (response != null) {
					responseString = response.body().string();
					handleResponseStockDataRealTime(mStock, mStockData,
							responseString);

					analyze(mStock, mStockData.getPeriod(),
							getStockDataList(mStock, mStockData.getPeriod()));

					Thread.sleep(Constants.DEFAULT_DOWNLOAD_INTERVAL);

					sendBroadcast(Constants.ACTION_SERVICE_FINISHED,
							Constants.SERVICE_DATABASE_UPDATE, mStock.getId());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return responseString;
		}

		String downloadFinancialData(Stock stock) {
			if (stock == null) {
				return "";
			}

			mStockDatabaseManager.getStock(stock);

			mFinancialData = new FinancialData();
			mFinancialData.setStockId(stock.getId());

			mStockDatabaseManager.getFinancialData(stock, mFinancialData);

			if (mFinancialData.getCreated().contains(
					Utility.getCurrentDateString())
					|| mFinancialData.getModified().contains(
							Utility.getCurrentDateString())) {
				if ((mFinancialData.getBookValuePerShare() != 0)
						&& (mFinancialData.getNetProfit() != 0)) {
					return "";
				}
			}

			setStock(stock);

			String urlString = getFinancialDataURLString(stock);
			String responseString = "";

			Log.d(TAG, "downloadFinancialData:" + urlString);

			Request.Builder builder = new Request.Builder();
			builder.url(urlString);
			Request request = builder.build();

			try {
				Response response = mOkHttpClient.newCall(request).execute();
				if (response != null) {
					responseString = response.body().string();
					handleResponseFinancialData(mStock, mFinancialData,
							responseString);

					Thread.sleep(Constants.DEFAULT_DOWNLOAD_INTERVAL);

					sendBroadcast(Constants.ACTION_SERVICE_FINISHED,
							Constants.SERVICE_DATABASE_UPDATE, mStock.getId());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return responseString;
		}

		String downloadShareBonus(Stock stock) {
			if (stock == null) {
				return "";
			}

			mStockDatabaseManager.getStock(stock);

			mShareBonus = new ShareBonus();
			mShareBonus.setStockId(stock.getId());

			mStockDatabaseManager.getShareBonus(stock.getId(), mShareBonus);
			if (mShareBonus.getCreated().contains(
					Utility.getCurrentDateString())
					|| mShareBonus.getModified().contains(
							Utility.getCurrentDateString())) {
				if (mShareBonus.getDividend() > 0) {
					return "";
				}
			}

			setStock(stock);

			String urlString = getShareBonusURLString(stock);
			String responseString = "";

			Log.d(TAG, "downloadShareBonus:" + urlString);

			Request.Builder builder = new Request.Builder();
			builder.url(urlString);
			Request request = builder.build();

			try {
				Response response = mOkHttpClient.newCall(request).execute();
				if (response != null) {
					responseString = response.body().string();
					handleResponseShareBonus(mStock, mShareBonus,
							responseString);

					Thread.sleep(Constants.DEFAULT_DOWNLOAD_INTERVAL);

					sendBroadcast(Constants.ACTION_SERVICE_FINISHED,
							Constants.SERVICE_DATABASE_UPDATE, 0);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return responseString;
		}

		@Override
		protected String doInBackground(String... params) {
			ArrayMap<String, Stock> stockArrayMapFavorite = new ArrayMap<String, Stock>();
			String responseString = "";
			long stockId = 0;

			if (mStock != null) {
				stockId = mStock.getId();
			}

			loadStockArrayMap(selectStock(Constants.STOCK_FLAG_MARK_FAVORITE),
					null, null, stockArrayMapFavorite);

			for (Stock stock : stockArrayMapFavorite.values()) {
				if (stockId != 0) {
					if (stock.getId() != stockId) {
						continue;
					}
				}

				responseString = downloadStockInformation(stock);
				if (responseString.contains(mAccessDeniedString)) {
					break;
				}

				responseString = downloadStockRealTime(stock);
				if (responseString.contains(mAccessDeniedString)) {
					break;
				}

				responseString = downloadStockDataHistory(stock);
				if (responseString.contains(mAccessDeniedString)) {
					break;
				}

				responseString = downloadStockDataRealTime(stock);
				if (responseString.contains(mAccessDeniedString)) {
					break;
				}

				responseString = downloadFinancialData(stock);
				if (responseString.contains(mAccessDeniedString)) {
					break;
				}

				responseString = downloadShareBonus(stock);
				if (responseString.contains(mAccessDeniedString)) {
					break;
				}

				analyze(stock);

				sendBroadcast(Constants.ACTION_SERVICE_FINISHED,
						Constants.SERVICE_DATABASE_UPDATE, 0);
			}

			return responseString;
		}
	}

	class DownloadIPOAsyncTask extends DownloadAsyncTask {
		IPO mIPO = null;

		public void setIPO(IPO ipo) {
			if (mIPO == null) {
				mIPO = new IPO();
			}

			mIPO.set(ipo);
		}

		String downloadIPO() {
			ArrayList<IPO> ipoList = new ArrayList<IPO>();
			boolean needDownload = false;

			mStockDatabaseManager.getIPOList(ipoList, null);
			if (ipoList.size() == 0) {
				needDownload = true;
			} else {
				for (IPO ipo : ipoList) {
					if (!ipo.getCreated().contains(
							Utility.getCurrentDateString())) {
						needDownload = true;
						break;
					}
				}
			}

			if (!needDownload) {
				return "";
			}

			return downloadIPO(getIPOURLString());
		}

		String downloadIPO(String urlString) {
			String responseString = "";

			Log.d(TAG, "downloadIPO:" + urlString);

			Request.Builder builder = new Request.Builder();
			builder.url(urlString);
			Request request = builder.build();

			try {
				Response response = mOkHttpClient.newCall(request).execute();
				if (response != null) {
					responseString = response.body().string();
					handleResponseIPO(mIPO, responseString);

					sendBroadcast(Constants.ACTION_SERVICE_FINISHED,
							Constants.SERVICE_DATABASE_UPDATE, 0);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return responseString;
		}

		@Override
		protected String doInBackground(String... params) {
			String responseString = "";

			responseString = downloadIPO();

			return responseString;
		}
	}
}
