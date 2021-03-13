package com.android.orion;

import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.HandlerThread;
import android.os.Process;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.android.orion.database.FinancialData;
import com.android.orion.database.IPO;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.TotalShare;
import com.android.orion.utility.Market;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.Utility;

public abstract class StockDataProvider extends StockAnalyzer {
	static final String TAG = Constants.TAG + " "
			+ StockDataProvider.class.getSimpleName();

	long mLastSendBroadcast = 0;
	ArrayMap<String, Stock> mStockArrayMap = new ArrayMap<String, Stock>();

	Status mAsyncTaskStatus = Status.FINISHED;

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

	abstract String getTotalShareURLString(Stock stock);

	abstract void handleResponseTotalShare(Stock stock, TotalShare totalShare,
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
	}

	void sendBroadcast(String action, long stockID) {
		long delt = System.currentTimeMillis() - mLastSendBroadcast;
		if (delt > Constants.DEFAULT_SEND_BROADCAST_INTERVAL) {
			mLastSendBroadcast = System.currentTimeMillis();

			Intent intent = new Intent(action);
			intent.putExtra(Constants.EXTRA_STOCK_ID, stockID);

			mLocalBroadcastManager.sendBroadcast(intent);
		}
	}

	void loadStockArrayMap(ArrayMap<String, Stock> stockArrayMap) {
		String selection = "";
		Cursor cursor = null;

		if ((mStockDatabaseManager == null) || (stockArrayMap == null)) {
			return;
		}

		mStockFilter.read();
		selection += mStockFilter.getSelection();

		try {
			stockArrayMap.clear();
			cursor = mStockDatabaseManager.queryStock(selection, null, null);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					Stock stock = new Stock();
					stock.set(cursor);

					stockArrayMap.put(stock.getSE() + stock.getCode(), stock);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}
	}

	//
	// void downloadStockHSA() {
	// String urlString = "";
	//
	// if (!Utility.isNetworkConnected(mContext)) {
	// return;
	// }
	//
	// if (Utility.getCurrentDateString().equals(
	// Preferences.getString(mContext, Settings.KEY_STOCK_HSA_UPDATED,
	// ""))) {
	// return;
	// }
	//
	// urlString = getStockHSAURLString();
	// if (addToCurrentRequests(urlString)) {
	// Log.d(TAG, "getStockHSAURLString:" + urlString);
	// StockHSADownloader downloader = new StockHSADownloader(urlString);
	// mRequestQueue.add(downloader.mStringRequest);
	// Preferences.putString(mContext, Settings.KEY_STOCK_HSA_UPDATED,
	// Utility.getCurrentDateString());
	// }
	// }

	void downloadStock(Intent intent) {
		String se = "";
		String code = "";
		Stock stock = null;

		loadStockArrayMap(mStockArrayMap);

		se = intent.getStringExtra(Constants.EXTRA_STOCK_SE);
		code = intent.getStringExtra(Constants.EXTRA_STOCK_CODE);
		stock = mStockArrayMap.get(se + code);

		download(stock);
	}

	void download(Stock stock) {
		if (!Utility.isNetworkConnected(mContext)) {
			return;
		}

		if (mAsyncTaskStatus != Status.FINISHED) {
			Log.d(TAG, "download return, mAsyncTaskStatus=" + mAsyncTaskStatus);
			return;
		}

		DownloadAsyncTask task = new DownloadAsyncTask();

		task.setStock(stock);
		task.execute();
	}

	int getDownloadHistoryLengthDefault(String period) {
		int result = 0;
		int availableHistoryLength = 0;

		availableHistoryLength = getAvailableHistoryLength(period);

		if (availableHistoryLength > 0) {
			result = availableHistoryLength;
		} else if (availableHistoryLength == Constants.DOWNLOAD_HISTORY_LENGTH_NONE) {
			result = 0;
		} else if (availableHistoryLength == Constants.DOWNLOAD_HISTORY_LENGTH_UNLIMITED) {
			result = Constants.DOWNLOAD_HISTORY_LENGTH_DEFAULT;
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

	int getDownloadStockDataLength(StockData stockData) {
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

				if (Market.isOutOfDateToday(modified)) {
					removeStockDataRedundant(cursor, defaultValue);
					result = defaultValue;
				} else if (Market.isTradingHours(Calendar.getInstance())) {
					scheduleMinutes = Market.getScheduleMinutes();
					if (scheduleMinutes != 0) {
						result = 1;

						if (period.equals(Constants.PERIOD_MIN60)) {
							result += scheduleMinutes
									/ Constants.SCHEDULE_INTERVAL_MIN60;
						} else if (period.equals(Constants.PERIOD_MIN30)) {
							result += scheduleMinutes
									/ Constants.SCHEDULE_INTERVAL_MIN30;
						} else if (period.equals(Constants.PERIOD_MIN15)) {
							result += scheduleMinutes
									/ Constants.SCHEDULE_INTERVAL_MIN15;
						} else if (period.equals(Constants.PERIOD_MIN5)) {
							result += scheduleMinutes
									/ Constants.SCHEDULE_INTERVAL_MIN5;
						}
					}
				} else if (Market.isHalfTime(Calendar.getInstance())) {
					if (period.equals(Constants.PERIOD_DAY)) {
						if (!stockData.getDate().equals(
								Utility.getCalendarDateString(Calendar
										.getInstance()))) {
							result = 1;
						}
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

	// public class StockHSADownloader extends VolleyStringDownloader {
	//
	// public StockHSADownloader() {
	// super();
	// }
	//
	// public StockHSADownloader(String urlString) {
	// super(urlString);
	// }
	//
	// @Override
	// void handleResponse(String response) {
	// removeFromCurrrentRequests(mStringRequest.getUrl());
	// handleResponseStockHSA(response);
	// }
	// }

	class DownloadAsyncTask extends AsyncTask<String, Void, String> {
		FinancialData mFinancialData = null;
		IPO mIPO = null;
		Stock mStock = null;
		StockData mStockData = null;
		ShareBonus mShareBonus = null;
		TotalShare mTotalShare = null;

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

		public void setIPO(IPO ipo) {
			if (mIPO == null) {
				mIPO = new IPO();
			}

			mIPO.set(ipo);
		}

		String downloadStockRealTime(Stock stock) {
			String result = "";
			
			if (stock == null) {
				return result;
			}

			mStockDatabaseManager.getStock(stock);

			if (Market.isTradingHours(Calendar.getInstance())) {
				// download in trading hours
			} else if (stock.getCreated().contains(
					Utility.getCurrentDateString())
					|| stock.getModified().contains(
							Utility.getCurrentDateString())) {
				if (stock.getPrice() > 0) {
					return result;
				}
			}

			setStock(stock);

			return downloadStockRealTime(getStockRealTimeURLString(stock));
		}

		String downloadStockRealTime(String urlString) {
			String result = "";

			Log.d(TAG, "downloadStockRealTime:" + urlString);

			Request.Builder builder = new Request.Builder();
			builder.url(urlString);
			Request request = builder.build();

			try {
				Response response = mOkHttpClient.newCall(request).execute();
				if (response != null) {
					result = response.body().string();
					handleResponseStockRealTime(mStock, result);

					mStockDatabaseManager.updateStockDeal(mStock);
					mStockDatabaseManager.updateStock(mStock,
							mStock.getContentValues());

					Thread.sleep(Constants.DEFAULT_SLEEP_INTERVAL);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return result;
		}

		String downloadStockInformation(Stock stock) {
			String result = "";
			
			boolean needDownload = false;

			if (stock == null) {
				return result;
			}

			mStockDatabaseManager.getStock(stock);

			if (TextUtils.isEmpty(stock.getClases())) {
				needDownload = true;
			} else if (TextUtils.isEmpty(stock.getPinyin())) {
				needDownload = true;
			} else if (stock.getTotalShare() == 0) {
				needDownload = true;
				if (Constants.STOCK_CLASS_INDEX.equals(stock.getClases())) {
					needDownload = false;
				}
			}

			if (!needDownload) {
				return result;
			}

			setStock(stock);

			return downloadStockInformation(getStockInformationURLString(stock));
		}

		String downloadStockInformation(String urlString) {
			String result = "";

			Log.d(TAG, "downloadStockInformation:" + urlString);

			Request.Builder builder = new Request.Builder();
			builder.url(urlString);
			Request request = builder.build();

			try {
				Response response = mOkHttpClient.newCall(request).execute();
				if (response != null) {
					result = response.body().string();
					handleResponseStockInformation(mStock, result);

					Thread.sleep(Constants.DEFAULT_SLEEP_INTERVAL);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return result;
		}

		String downloadFinancialData(Stock stock) {
			String result = "";
			
			if (stock == null) {
				return result;
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
					return result;
				}
			}

			setStock(stock);

			return downloadFinancialData(getFinancialDataURLString(stock));
		}

		String downloadFinancialData(String urlString) {
			String result = "";

			Log.d(TAG, "downloadFinancialData:" + urlString);

			Request.Builder builder = new Request.Builder();
			builder.url(urlString);
			Request request = builder.build();

			try {
				Response response = mOkHttpClient.newCall(request).execute();
				if (response != null) {
					result = response.body().string();
					handleResponseFinancialData(mStock, mFinancialData,
							result);

					Thread.sleep(Constants.DEFAULT_SLEEP_INTERVAL);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return result;
		}

		String downloadIPO() {
			String result = "";
			
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
				return result;
			}

			return downloadIPO(getIPOURLString());
		}

		String downloadIPO(String urlString) {
			String result = "";

			Log.d(TAG, "downloadIPO:" + urlString);

			Request.Builder builder = new Request.Builder();
			builder.url(urlString);
			Request request = builder.build();

			try {
				Response response = mOkHttpClient.newCall(request).execute();
				if (response != null) {
					result = response.body().string();
					handleResponseIPO(mIPO, result);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return result;
		}

		String downloadShareBonus(Stock stock) {
			String result = "";
			
			if (stock == null) {
				return result;
			}

			mStockDatabaseManager.getStock(stock);

			mShareBonus = new ShareBonus();
			mShareBonus.setStockId(stock.getId());

			mStockDatabaseManager.getShareBonus(stock.getId(), mShareBonus);
			if (mShareBonus.getCreated().contains(
					Utility.getCurrentDateString())
					|| mShareBonus.getModified().contains(
							Utility.getCurrentDateString())) {
				if (!TextUtils.isEmpty(mShareBonus.getDate())) {
					return result;
				}
			}

			setStock(stock);

			return downloadShareBonus(getShareBonusURLString(stock));
		}

		String downloadShareBonus(String urlString) {
			String result = "";

			Log.d(TAG, "downloadShareBonus:" + urlString);

			Request.Builder builder = new Request.Builder();
			builder.url(urlString);
			Request request = builder.build();

			try {
				Response response = mOkHttpClient.newCall(request).execute();
				if (response != null) {
					result = response.body().string();
					handleResponseShareBonus(mStock, mShareBonus,
							result);

					Thread.sleep(Constants.DEFAULT_SLEEP_INTERVAL);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return result;
		}

		String downloadTotalShare(Stock stock) {
			String result = "";
			
			if (stock == null) {
				return result;
			}

			mStockDatabaseManager.getStock(stock);

			mTotalShare = new TotalShare();
			mTotalShare.setStockId(stock.getId());

			mStockDatabaseManager.getTotalShare(stock.getId(), mTotalShare);
			if (mTotalShare.getCreated().contains(
					Utility.getCurrentDateString())
					|| mTotalShare.getModified().contains(
							Utility.getCurrentDateString())) {
				if (mTotalShare.getTotalShare() > 0) {
					return result;
				}
			}

			setStock(stock);

			return downloadTotalShare(getTotalShareURLString(stock));
		}

		String downloadTotalShare(String urlString) {
			String result = "";

			Log.d(TAG, "downloadTotalShare:" + urlString);

			Request.Builder builder = new Request.Builder();
			builder.url(urlString);
			Request request = builder.build();

			try {
				Response response = mOkHttpClient.newCall(request).execute();
				if (response != null) {
					result = response.body().string();
					handleResponseTotalShare(mStock, mTotalShare,
							result);

					Thread.sleep(Constants.DEFAULT_SLEEP_INTERVAL);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return result;
		}

		String downloadStockDataHistory(Stock stock) {
			String result = "";
			
			if (stock == null) {
				return result;
			}

			mStockDatabaseManager.getStock(stock);

			for (String period : Constants.PERIODS) {
				if (Preferences.getBoolean(mContext, period, false)) {
					result = downloadStockDataHistory(stock, period);
				}
			}

			return result;
		}

		String downloadStockDataHistory(Stock stock, String period) {
			String result = "";
			int len = 0;
			mStockData = new StockData(period);

			if (stock == null) {
				return result;
			}

			mStockData.setStockId(stock.getId());
			mStockDatabaseManager.getStockData(mStockData);

			len = getDownloadStockDataLength(mStockData);
			if (len <= 0) {
				return result;
			}

			setStock(stock);

			return downloadStockDataHistory(getStockDataHistoryURLString(stock,
					mStockData, len));
		}

		String downloadStockDataHistory(String urlString) {
			String result = "";

			Log.d(TAG, "downloadStockDataHistory:" + urlString);

			Request.Builder builder = new Request.Builder();
			builder.url(urlString);
			Request request = builder.build();

			try {
				Response response = mOkHttpClient.newCall(request).execute();
				if (response != null) {
					result = response.body().string();
					handleResponseStockDataHistory(mStock, mStockData,
							result);

					Thread.sleep(Constants.DEFAULT_SLEEP_INTERVAL);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return result;
		}

		String downloadStockDataRealTime(Stock stock) {
			String result = "";
			int len = 0;
			String period = Constants.PERIOD_DAY;

			if (!Preferences.getBoolean(mContext, period, false)) {
				return result;
			}

			if (stock == null) {
				return result;
			}

			mStockDatabaseManager.getStock(stock);

			mStockData = new StockData(period);
			mStockData.setStockId(stock.getId());
			mStockDatabaseManager.getStockData(mStockData);

			len = getDownloadStockDataLength(mStockData);
			if (len <= 0) {
				return result;
			}

			setStock(stock);

			return downloadStockDataRealTime(getStockDataRealTimeURLString(stock));
		}

		String downloadStockDataRealTime(String urlString) {
			String result = "";

			Log.d(TAG, "downloadStockDataRealTime:" + urlString);

			Request.Builder builder = new Request.Builder();
			builder.url(urlString);
			Request request = builder.build();

			try {
				Response response = mOkHttpClient.newCall(request).execute();
				if (response != null) {
					result = response.body().string();
					handleResponseStockDataRealTime(mStock, mStockData,
							result);

					Thread.sleep(Constants.DEFAULT_SLEEP_INTERVAL);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return result;
		}

		@Override
		protected String doInBackground(String... params) {
			ArrayMap<String, Stock> stockArrayMapFavorite = new ArrayMap<String, Stock>();
			String result = "";
			long stockId = 0;

			mAsyncTaskStatus = Status.RUNNING;
			Log.d(TAG, "doInBackground, mAsyncTaskStatus=" + mAsyncTaskStatus);

			if (mStock != null) {
				stockId = mStock.getId();
			}

			loadStockArrayMap(stockArrayMapFavorite);

			for (Stock stock : stockArrayMapFavorite.values()) {
				if (stockId != 0) {
					if (stock.getId() != stockId) {
						continue;
					}
				}

				result = downloadStockRealTime(stock);
				if (result.contains(mAccessDeniedString)) {
					break;
				}

				result = downloadStockInformation(stock);
				if (result.contains(mAccessDeniedString)) {
					break;
				}

				result = downloadFinancialData(stock);
				if (result.contains(mAccessDeniedString)) {
					break;
				}

				result = downloadShareBonus(stock);
				if (result.contains(mAccessDeniedString)) {
					break;
				}

				result = downloadTotalShare(stock);
				if (result.contains(mAccessDeniedString)) {
					break;
				}

				result = downloadStockDataHistory(stock);
				if (result.contains(mAccessDeniedString)) {
					break;
				}

				result = downloadStockDataRealTime(stock);
				if (result.contains(mAccessDeniedString)) {
					break;
				}

				for (String period : Constants.PERIODS) {
					if (Preferences.getBoolean(mContext, period, false)) {
						analyze(stock, period);
					}
				}

				analyze(stock);

				sendBroadcast(Constants.ACTION_RESTART_LOADER, stockId);
			}

			result = downloadIPO();
			sendBroadcast(Constants.ACTION_RESTART_LOADER,
					Constants.STOCK_ID_INVALID);

			return result;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mAsyncTaskStatus = Status.PENDING;
			Log.d(TAG, "onPreExecute, mAsyncTaskStatus=" + mAsyncTaskStatus);

			acquireWakeLock();
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

			releaseWakeLock();

			mAsyncTaskStatus = Status.FINISHED;
			Log.d(TAG, "onPostExecute, mAsyncTaskStatus=" + mAsyncTaskStatus);
		}
	}
}