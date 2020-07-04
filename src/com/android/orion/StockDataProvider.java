package com.android.orion;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

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
import com.android.volley.toolbox.StringRequest;

public abstract class StockDataProvider extends StockAnalyzer {
	static final String TAG = Constants.TAG + " "
			+ StockDataProvider.class.getSimpleName();

	private static final int MSG_DOWNLOAD_INFORMATION = 1;

	Calendar mNotifyCalendar = Calendar.getInstance();
	RequestQueue mRequestQueue;
	Set<String> mCurrentRequests = new HashSet<String>();

	DelayQueue<DelayedRequest> mDelayQueue = new DelayQueue<DelayedRequest>();
	long mTargetTime = 0;

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
	volatile DownloadHandler mHandler;

	public StockDataProvider(Context context) {
		super(context);

		mHandlerThread = new HandlerThread("StockDataProvider",
				Process.THREAD_PRIORITY_BACKGROUND);
		mHandlerThread.start();

		mHandler = new DownloadHandler(mHandlerThread.getLooper());

		mRequestQueue = VolleySingleton.getInstance(
				mContext.getApplicationContext()).getRequestQueue();
		removeAllCurrrentRequests();

		DelayQueueConsumer queueConsumer = new DelayQueueConsumer();
		new Thread(queueConsumer).start();
	}

	void sendBroadcast(String action, int serviceType, long stockID) {
		Calendar calendar = Calendar.getInstance();
		if (calendar.getTimeInMillis() - mNotifyCalendar.getTimeInMillis() < 15 * 1000) {
			return;
		}

		Bundle bundle = new Bundle();
		bundle.putInt(Constants.EXTRA_SERVICE_TYPE, serviceType);
		bundle.putLong(Constants.EXTRA_STOCK_ID, stockID);

		sendBroadcast(action, bundle);

		mNotifyCalendar = Calendar.getInstance();
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
				// addToDelayQueue(downloader.mStringRequest);
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
			// addToDelayQueue(downloader.mStringRequest);
			mStockDatabaseManager.saveSetting(Setting.KEY_STOCK_HSA_UPDATED,
					Utility.getCurrentDateString());
		}
	}

	void downloadStock(Bundle bundle) {
		int executeType = Constants.EXECUTE_TYPE_NONE;
		Stock stock = null;

		if (!Utility.isNetworkConnected(mContext)) {
			return;
		}

		if (!Preferences.readBoolean(mContext, Constants.SETTING_KEY_ALARM,
				false)) {
			return;
		}

		resetTargetTime();
		removeAllCurrrentRequests();

		executeType = bundle.getInt(Constants.EXTRA_EXECUTE_TYPE,
				Constants.EXECUTE_TYPE_NONE);

		stock = getStock(bundle);

		if (stock != null) {
			downloadShareBonus(executeType, stock);
			downloadStockInformation(stock);

			downloadStockRealTime(executeType, stock);
			downloadStockDataHistory(executeType, stock);
			downloadStockDataRealTime(executeType, stock);

			downloadFinancialData(executeType, stock);
		} else {
			downloadShareBonus(executeType);
			downloadStockInformation();

			downloadStockRealTime(executeType);
			downloadStockDataHistory(executeType);
			downloadStockDataRealTime(executeType);

			downloadFinancialData(executeType);
		}
	}

	void downloadFinancialData(int executeType) {
		loadStockArrayMapFavorite();

		for (Stock stock : mStockArrayMapFavorite.values()) {
			downloadStockInformation(stock);

			downloadFinancialData(executeType, stock);
		}
	}

	void downloadFinancialData(int executeType, Stock stock) {
		String urlString;

		if (stock == null) {
			return;
		}

		mStockDatabaseManager.getStock(stock);

		FinancialData financialData = new FinancialData();
		financialData.setStockId(stock.getId());

		mStockDatabaseManager.getFinancialData(stock, financialData);

		if (executeType == Constants.EXECUTE_SCHEDULE) {
			if (financialData.getCreated().contains(
					Utility.getCurrentDateString())
					|| financialData.getModified().contains(
							Utility.getCurrentDateString())) {
				if ((financialData.getBookValuePerShare() != 0)
						&& (financialData.getNetProfit() != 0)) {
					return;
				}
			}
		}

		urlString = getFinancialDataURLString(stock);
		// if (addToCurrentRequests(urlString)) {
		Log.d(TAG, "downloadFinancial:" + urlString);
		FinancialDataDownloader downloader = new FinancialDataDownloader(
				urlString);
		downloader.setStock(stock);
		downloader.setFinancialData(financialData);
		// mRequestQueue.add(downloader.mStringRequest);
		addToDelayQueue(downloader.mStringRequest);
		// }
	}

	void downloadShareBonus(int executeType) {
		for (Stock stock : mStockArrayMapFavorite.values()) {
			downloadShareBonus(executeType, stock);
		}
	}

	void downloadShareBonus(int executeType, Stock stock) {
		String urlString;

		if (stock == null) {
			return;
		}

		mStockDatabaseManager.getStock(stock);

		ShareBonus shareBonus = new ShareBonus();
		shareBonus.setStockId(stock.getId());

		if (executeType == Constants.EXECUTE_SCHEDULE) {
			mStockDatabaseManager.getShareBonus(stock.getId(), shareBonus);
			if (shareBonus.getCreated()
					.contains(Utility.getCurrentDateString())
					|| shareBonus.getModified().contains(
							Utility.getCurrentDateString())) {
				if (shareBonus.getDividend() > 0) {
					return;
				}
			}
		}

		urlString = getShareBonusURLString(stock);
		// if (addToCurrentRequests(urlString)) {
		Log.d(TAG, "downloadShareBonus:" + urlString);
		ShareBonusDownloader downloader = new ShareBonusDownloader(urlString);
		downloader.setStock(stock);
		downloader.setShareBonus(shareBonus);
		// mRequestQueue.add(downloader.mStringRequest);
		addToDelayQueue(downloader.mStringRequest);
		// }
	}

	void downloadIPO() {
		String urlString;
		ArrayList<IPO> ipoList = new ArrayList<IPO>();
		boolean needDownload = false;

		mStockDatabaseManager.getIPOList(ipoList, null);
		if (ipoList.size() == 0) {
			needDownload = true;
		} else {
			for (IPO ipo : ipoList) {
				if (!ipo.getCreated().contains(Utility.getCurrentDateString())) {
					needDownload = true;
					break;
				}
			}
		}

		if (!needDownload) {
			return;
		}

		urlString = getIPOURLString();
		if (addToCurrentRequests(urlString)) {
			Log.d(TAG, "downloadIPO:" + urlString);
			IPODownloader downloader = new IPODownloader(urlString);
			downloader.setIPO(new IPO());
			mRequestQueue.add(downloader.mStringRequest);
			// addToDelayQueue(downloader.mStringRequest);
		}
	}

	void downloadStockInformation() {
		for (Stock stock : mStockArrayMapFavorite.values()) {
			downloadStockInformation(stock);
		}
	}

	void downloadStockInformation(Stock stock) {
		String urlString;
		boolean needDownload = false;

		if (stock == null) {
			return;
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
			return;
		}

		urlString = getStockInformationURLString(stock);
		if (addToCurrentRequests(urlString)) {
			Log.d(TAG, "getStockInformationURLString:" + urlString);
			StockInformationDownloader downloader = new StockInformationDownloader(
					urlString);
			downloader.setStock(stock);
			mRequestQueue.add(downloader.mStringRequest);
			// addToDelayQueue(downloader.mStringRequest);
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
				// addToDelayQueue(downloader.mStringRequest);
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
					// addToDelayQueue(downloader.mStringRequest);
				}
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
				// addToDelayQueue(downloader.mStringRequest);
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
					result = defaultValue;
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

	void resetTargetTime() {
		mTargetTime = System.currentTimeMillis();
	}

	void addToDelayQueue(StringRequest stringRequest) {
		if (stringRequest == null) {
			return;
		}

		DelayedRequest delayedRequest = new DelayedRequest();

		delayedRequest.setStringRequest(stringRequest);
		delayedRequest.setTargetTime(mTargetTime);

		mDelayQueue.put(delayedRequest);

		mTargetTime += (long) (Math.random() * Constants.DEFAULT_DOWNLOAD_INTERVAL);
	}

	Stock getStock(Bundle bundle) {
		String se = bundle.getString(Constants.EXTRA_STOCK_SE);
		String code = bundle.getString(Constants.EXTRA_STOCK_CODE);
		Stock stock = null;

		stock = mStockArrayMapFavorite.get(se + code);

		return stock;
	}

	void removeMessages(int what) {
		if (mHandler.hasMessages(what)) {
			mHandler.removeMessages(what);
		}
	}

	void sendEmptyMessageDelayed(int what, long delayMillis) {
		removeMessages(what);
		mHandler.sendEmptyMessageDelayed(what, delayMillis);
	}

	private final class DownloadHandler extends Handler {
		public DownloadHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MSG_DOWNLOAD_INFORMATION:
				break;

			default:
				break;
			}
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

	public class StockInformationDownloader extends VolleyStringDownloader {
		public Stock mStock = null;

		public StockInformationDownloader() {
			super();
		}

		public StockInformationDownloader(String urlString) {
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

			handleResponseStockInformation(mStock, response);
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

	public class FinancialDataDownloader extends VolleyStringDownloader {
		public Stock mStock = null;
		public FinancialData mFinancialData = null;

		public FinancialDataDownloader() {
			super();
		}

		public FinancialDataDownloader(String urlString) {
			super(urlString);
		}

		public void setStock(Stock stock) {
			if (mStock == null) {
				mStock = new Stock();
			}
			mStock.set(stock);
		}

		public void setFinancialData(FinancialData financialData) {
			if (mFinancialData == null) {
				mFinancialData = new FinancialData();
			}
			mFinancialData.set(financialData);
		}

		@Override
		public void handleResponse(String response) {
			removeFromCurrrentRequests(mStringRequest.getUrl());
			handleResponseFinancialData(mStock, mFinancialData, response);

			sendBroadcast(Constants.ACTION_SERVICE_FINISHED,
					Constants.SERVICE_DATABASE_UPDATE, mStock.getId());
		}
	}

	public class ShareBonusDownloader extends VolleyStringDownloader {
		public Stock mStock = null;
		public ShareBonus mShareBonus = null;

		public ShareBonusDownloader() {
			super();
		}

		public ShareBonusDownloader(String urlString) {
			super(urlString);
		}

		public void setStock(Stock stock) {
			if (mStock == null) {
				mStock = new Stock();
			}
			mStock.set(stock);
		}

		public void setShareBonus(ShareBonus shareBonus) {
			if (mShareBonus == null) {
				mShareBonus = new ShareBonus();
			}
			mShareBonus.set(shareBonus);
		}

		@Override
		public void handleResponse(String response) {
			removeFromCurrrentRequests(mStringRequest.getUrl());
			handleResponseShareBonus(mStock, mShareBonus, response);

			sendBroadcast(Constants.ACTION_SERVICE_FINISHED,
					Constants.SERVICE_DATABASE_UPDATE, mStock.getId());
		}
	}

	public class IPODownloader extends VolleyStringDownloader {
		public IPO mIPO = null;

		public IPODownloader() {
			super();
		}

		public IPODownloader(String urlString) {
			super(urlString);
		}

		public void setIPO(IPO ipo) {
			if (mIPO == null) {
				mIPO = new IPO();
			}

			mIPO.set(ipo);
		}

		@Override
		public void handleResponse(String response) {
			removeFromCurrrentRequests(mStringRequest.getUrl());
			handleResponseIPO(mIPO, response);

			sendBroadcast(Constants.ACTION_SERVICE_FINISHED,
					Constants.SERVICE_DATABASE_UPDATE, 0);
		}
	}

	public class DelayedRequest implements Delayed {
		long mTargetTime = 0;
		StringRequest mStringRequest = null;

		void setTargetTime(long targetTime) {
			mTargetTime = targetTime;
		}

		void setStringRequest(StringRequest stringRequest) {
			mStringRequest = stringRequest;
		}

		@Override
		public int compareTo(Delayed delayed) {
			DelayedRequest that = (DelayedRequest) delayed;

			if (this.mTargetTime < that.mTargetTime) {
				return -1;
			}

			if (this.mTargetTime > that.mTargetTime) {
				return 1;
			}

			return 0;
		}

		@Override
		public long getDelay(TimeUnit timeUnit) {
			long result = 0;

			result = timeUnit.convert(mTargetTime - System.currentTimeMillis(),
					TimeUnit.MILLISECONDS);

			return result;
		}
	}

	public class DelayQueueConsumer implements Runnable {

		public DelayQueueConsumer() {
		}

		@Override
		public void run() {
			while (true) {
				try {
					DelayedRequest delayedRequest = mDelayQueue.take();
					mRequestQueue.add(delayedRequest.mStringRequest);
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
