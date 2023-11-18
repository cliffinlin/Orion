package com.android.orion.provider;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.orion.R;
import com.android.orion.analyzer.StockAnalyzer;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.IPO;
import com.android.orion.database.IndexComponent;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDeal;
import com.android.orion.database.StockFinancial;
import com.android.orion.database.TotalShare;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Market;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class StockDataProvider extends StockAnalyzer {
	public static final String TAG = StockDataProvider.class.getSimpleName();

	public static final int PERIOD_MINUTES_MIN1 = 1;
	public static final int PERIOD_MINUTES_MIN5 = 5;
	public static final int PERIOD_MINUTES_MIN15 = 15;
	public static final int PERIOD_MINUTES_MIN30 = 30;
	public static final int PERIOD_MINUTES_MIN60 = 60;
	public static final int PERIOD_MINUTES_DAY = 240;
	public static final int PERIOD_MINUTES_WEEK = 1680;
	public static final int PERIOD_MINUTES_MONTH = 7200;
	public static final int PERIOD_MINUTES_QUARTER = 28800;
	public static final int PERIOD_MINUTES_YEAR = 115200;
	public static final int MAX_CONTENT_LENGTH_MIN60 = 60 * 4;
	public static final int MAX_CONTENT_LENGTH_MIN30 = 30 * 8;
	public static final int MAX_CONTENT_LENGTH_MIN15 = 20 * 16;
	public static final int MAX_CONTENT_LENGTH_MIN5 = 20 * 48;

	private static int DOWNLOAD_RESULT_SUCCESS = 1;
	private static int DOWNLOAD_RESULT_NONE = 0;
	private static int DOWNLOAD_RESULT_FAILED = -1;
	private HandlerThread mHandlerThread;
	private ServiceHandler mHandler;
	private OkHttpClient mOkHttpClient = new OkHttpClient();
	private ArrayList<String> mAccessDeniedStringArray = new ArrayList<>();

	public StockDataProvider(@NonNull Context context) {
		super(context);

		mAccessDeniedStringArray.add(mContext.getResources().getString(
				R.string.access_denied_jp));
		mAccessDeniedStringArray.add(mContext.getResources().getString(
				R.string.access_denied_zh));
		mAccessDeniedStringArray.add(mContext.getResources().getString(
				R.string.access_denied_default));

		mHandlerThread = new HandlerThread("StockDataProvider",
				Process.THREAD_PRIORITY_BACKGROUND);
		mHandlerThread.start();

		mHandler = new ServiceHandler(mHandlerThread.getLooper());
	}

	public abstract int getAvailableHistoryLength(String period);

	public abstract ArrayMap<String, String> getRequestHeader();

	public abstract String getStockHSAURLString();

	public abstract void handleResponseStockHSA(String response);

	public abstract String getStockInformationURLString(Stock stock);

	public abstract void handleResponseStockInformation(Stock stock, String response);

	public abstract String getStockRealTimeURLString(Stock stock);

	public abstract void handleResponseStockRealTime(Stock stock, String response);

	public abstract String getStockDataHistoryURLString(Stock stock,
														StockData stockData, int len);

	public abstract void handleResponseStockDataHistory(Stock stock,
														StockData stockData, String response);

	public abstract String getStockDataRealTimeURLString(Stock stock);

	public abstract void handleResponseStockDataRealTime(Stock stock,
														 StockData stockData, String response);

	public abstract String getStockFinancialURLString(Stock stock);

	public abstract void handleResponseStockFinancial(Stock stock,
													  StockFinancial stockFinancial, String response);

	public abstract String getShareBonusURLString(Stock stock);

	public abstract void handleResponseShareBonus(Stock stock, ShareBonus shareBonus,
												  String response);

	public abstract String getTotalShareURLString(Stock stock);

	public abstract void handleResponseTotalShare(Stock stock, TotalShare totalShare,
												  String response);

	public abstract String getIPOURLString();

	public abstract void handleResponseIPO(String response);

	public int getPeriodMinutes(String period) {
		int result = 0;

		if (period.equals(Setting.KEY_PERIOD_MIN1)) {
			result = PERIOD_MINUTES_MIN1;
		} else if (period.equals(Setting.KEY_PERIOD_MIN5)) {
			result = PERIOD_MINUTES_MIN5;
		} else if (period.equals(Setting.KEY_PERIOD_MIN15)) {
			result = PERIOD_MINUTES_MIN15;
		} else if (period.equals(Setting.KEY_PERIOD_MIN30)) {
			result = PERIOD_MINUTES_MIN30;
		} else if (period.equals(Setting.KEY_PERIOD_MIN60)) {
			result = PERIOD_MINUTES_MIN60;
		} else if (period.equals(Setting.KEY_PERIOD_DAY)) {
			result = PERIOD_MINUTES_DAY;
		} else if (period.equals(Setting.KEY_PERIOD_WEEK)) {
			result = PERIOD_MINUTES_WEEK;
		} else if (period.equals(Setting.KEY_PERIOD_MONTH)) {
			result = PERIOD_MINUTES_MONTH;
		} else if (period.equals(Setting.KEY_PERIOD_QUARTER)) {
			result = PERIOD_MINUTES_QUARTER;
		} else if (period.equals(Setting.KEY_PERIOD_YEAR)) {
			result = PERIOD_MINUTES_YEAR;
		} else {
		}

		return result;
	}

	public void fixContentValuesList(@NonNull StockData stockData, @NonNull ArrayList<ContentValues> contentValuesList) {
		int size = contentValuesList.size();
		int n = 0;

		switch (stockData.getPeriod()) {
			case Setting.KEY_PERIOD_MIN1:
				break;
			case Setting.KEY_PERIOD_MIN5:
				n = size - MAX_CONTENT_LENGTH_MIN5;
				break;
			case Setting.KEY_PERIOD_MIN15:
				n = size - MAX_CONTENT_LENGTH_MIN15;
				break;
			case Setting.KEY_PERIOD_MIN30:
				n = size - MAX_CONTENT_LENGTH_MIN30;
				break;
			case Setting.KEY_PERIOD_MIN60:
				n = size - MAX_CONTENT_LENGTH_MIN60;
				break;
			default:
				break;
		}

		for (int i = 0; i < n; i++) {
			contentValuesList.remove(0);
		}
	}

	private void sendBroadcast(String action, long stockID) {
		Intent intent = new Intent(action);
		intent.putExtra(Constant.EXTRA_STOCK_ID, stockID);

		mLocalBroadcastManager.sendBroadcast(intent);
	}

	private void loadStockArrayMap(ArrayMap<String, Stock> stockArrayMap) {
		String selection = "";
		Cursor cursor = null;

		if ((mStockDatabaseManager == null) || (stockArrayMap == null)) {
			return;
		}

		selection += DatabaseContract.COLUMN_FLAG + " >= "
				+ Stock.FLAG_FAVORITE;

		try {
			stockArrayMap.clear();
			cursor = mStockDatabaseManager.queryStock(selection, null, null);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					Stock stock = new Stock();
					stock.set(cursor);

					stockArrayMap.put(stock.getCode(), stock);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}
	}

	public void download(String se, String code) {
		Stock stock = null;

		if (!TextUtils.isEmpty(se) && !TextUtils.isEmpty(code)) {
			stock = new Stock();
			stock.setSE(se);
			stock.setCode(code);

			mStockDatabaseManager.getStock(stock);
		}

		download(stock);
	}

	public void download(Stock stock) {
		if (!Utility.isNetworkConnected(mContext)) {
			return;
		}

		if (stock == null) {
			ArrayMap<String, Stock> stockArrayMap = new ArrayMap<String, Stock>();

			loadStockArrayMap(stockArrayMap);

			for (Stock current : stockArrayMap.values()) {
				if (mHandler.hasMessages(Integer.valueOf(current.getCode()))) {
					Log.d(TAG, "mHandler.hasMessages " + Integer.valueOf(current.getCode()) + ", skip!");
				} else {
					Message msg = mHandler.obtainMessage(Integer.valueOf(current.getCode()), current);
					mHandler.sendMessage(msg);
					Log.d(TAG, "mHandler.sendMessage " + msg);
				}
			}
		} else {
			if (TextUtils.isEmpty(stock.getCode())) {
				return;
			}

			ArrayMap<String, Stock> stockArrayMap = new ArrayMap<String, Stock>();
			ArrayMap<String, Stock> removedArrayMap = new ArrayMap<String, Stock>();

			loadStockArrayMap(stockArrayMap);

			for (Stock current : stockArrayMap.values()) {
				if (current.getCode().equals(stock.getCode())) {
					continue;
				}

				if (mHandler.hasMessages(Integer.valueOf(current.getCode()))) {
					mHandler.removeMessages(Integer.valueOf(current.getCode()));
					Log.d(TAG, "mHandler.hasMessages " + Integer.valueOf(current.getCode()) + ", removed!");
					removedArrayMap.put(current.getCode(), current);
				}
			}

			if (mHandler.hasMessages(Integer.valueOf(stock.getCode()))) {
				Log.d(TAG, "mHandler.hasMessages " + Integer.valueOf(stock.getCode()) + ", skip!");
			} else {
				Message msg = mHandler.obtainMessage(Integer.valueOf(stock.getCode()), stock);
				mHandler.sendMessage(msg);
				Log.d(TAG, "mHandler.sendMessage" + msg);
			}

			for (Stock current : removedArrayMap.values()) {
				if (mHandler.hasMessages(Integer.valueOf(current.getCode()))) {
					Log.d(TAG, "mHandler.hasMessages " + Integer.valueOf(current.getCode()) + ", skip!");
				} else {
					Message msg = mHandler.obtainMessage(Integer.valueOf(current.getCode()), current);
					mHandler.sendMessage(msg);
					Log.d(TAG, "mHandler.sendMessage " + msg);
				}
			}
		}
	}

	public int getDownloadHistoryLengthDefault(String period) {
		int result = 0;
		int availableHistoryLength = 0;

		availableHistoryLength = getAvailableHistoryLength(period);

		if (availableHistoryLength > 0) {
			result = availableHistoryLength;
		} else if (availableHistoryLength == Constant.DOWNLOAD_HISTORY_LENGTH_UNLIMITED) {
			result = Constant.DOWNLOAD_HISTORY_LENGTH_DEFAULT;
		}

		return result;
	}

	private int getDownloadStockDataLength(StockData stockData) {
		int result = 0;
		int defaultValue = 0;
		int scheduleMinutes = 0;
		long stockId = 0;
		String period = "";
		String modified = "";
		String selection = null;
		String sortOrder = null;
		Cursor cursor = null;

		try {
			if (stockData == null) {
				return result;
			}

			stockId = stockData.getStockId();
			period = stockData.getPeriod();

			defaultValue = getDownloadHistoryLengthDefault(period);

			selection = mStockDatabaseManager.getStockDataSelection(stockId,
					period, StockData.LEVEL_NONE);
			sortOrder = mStockDatabaseManager.getStockDataOrder();
			cursor = mStockDatabaseManager.queryStockData(selection, null,
					sortOrder);

			if ((cursor == null) || (cursor.getCount() == 0)) {
				return defaultValue;
			}

			cursor.moveToLast();
			stockData.set(cursor);

			modified = stockData.getModified();
			if (Market.isOutOfDateToday(modified)) {
				mStockDatabaseManager.deleteStockData(stockId, period);
				return defaultValue;
			}

			if (!Market.isWeekday(Calendar.getInstance())) {
				return result;
			}

//            int count = getStockDataOfToday(cursor, stockData);
//            if (count > 0) {
//                modified = stockData.getModified();
//            }
			Calendar modifiedCalendar = Utility.getCalendar(modified,
					Utility.CALENDAR_DATE_TIME_FORMAT);
			Calendar stockMarketLunchBeginCalendar = Market
					.getMarketLunchBeginCalendar(Calendar
							.getInstance());
			Calendar stockMarketCloseCalendar = Market
					.getMarketCloseCalendar(Calendar.getInstance());

			if (Market.isTradingHours(Calendar.getInstance())) {
				scheduleMinutes = Market.getScheduleMinutes();
				if (scheduleMinutes != 0) {
					result = 1;

					switch (period) {
						case Setting.KEY_PERIOD_MIN60:
							result += scheduleMinutes
									/ Constant.SCHEDULE_INTERVAL_MIN60;
							break;
						case Setting.KEY_PERIOD_MIN30:
							result += scheduleMinutes
									/ Constant.SCHEDULE_INTERVAL_MIN30;
							break;
						case Setting.KEY_PERIOD_MIN15:
							result += scheduleMinutes
									/ Constant.SCHEDULE_INTERVAL_MIN15;
							break;
						case Setting.KEY_PERIOD_MIN5:
							result += scheduleMinutes
									/ Constant.SCHEDULE_INTERVAL_MIN5;
							break;
					}
				}
			} else if (Market.isLunchTime(Calendar.getInstance())) {
				if (period.equals(Setting.KEY_PERIOD_MONTH)
						|| period.equals(Setting.KEY_PERIOD_WEEK)
						|| period.equals(Setting.KEY_PERIOD_DAY)) {
					if (Market.isOutOfDateToday(stockData.getDate())) {
						result = 1;
					}
					return result;
				}

				if (modifiedCalendar.after(stockMarketLunchBeginCalendar)) {
					return result;
				}

				switch (period) {
					case Setting.KEY_PERIOD_MIN60:
						result = 2;
						break;
					case Setting.KEY_PERIOD_MIN30:
						result = 4;
						break;
					case Setting.KEY_PERIOD_MIN15:
						result = 8;
						break;
					case Setting.KEY_PERIOD_MIN5:
						result = 24;
						break;
				}
			} else if (Market.afterClosed(Calendar.getInstance())) {
				if (modifiedCalendar.after(stockMarketCloseCalendar)) {
					return result;
				}

				switch (period) {
					case Setting.KEY_PERIOD_MONTH:
					case Setting.KEY_PERIOD_WEEK:
					case Setting.KEY_PERIOD_DAY:
						result = 1;
						break;
					case Setting.KEY_PERIOD_MIN60:
						result = 4;
						break;
					case Setting.KEY_PERIOD_MIN30:
						result = 8;
						break;
					case Setting.KEY_PERIOD_MIN15:
						result = 16;
						break;
					case Setting.KEY_PERIOD_MIN5:
						result = 48;
						break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}

		return result;
	}

	private int downloadStockInformation(Stock stock) {
		String modified = "";
		int result = DOWNLOAD_RESULT_NONE;

		if (stock == null) {
			return result;
		}

		modified = stock.getInformationModified();

		if (Market.isOutOfDateToday(modified)) {
			return downloadStockInformation(stock, getRequestHeader(), getStockInformationURLString(stock));
		}

		return result;
	}

	private int downloadStockInformation(Stock stock, ArrayMap<String, String> requestHeaderArray, String urlString) {
		int result = DOWNLOAD_RESULT_NONE;

		Log.d(TAG, "downloadStockInformation:" + urlString);

		Request.Builder builder = new Request.Builder();
		for (int i = 0; i < requestHeaderArray.size(); i++) {
			builder.addHeader(requestHeaderArray.keyAt(i), requestHeaderArray.valueAt(i));
		}
		builder.url(urlString);
		Request request = builder.build();

		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if ((response != null) && (response.body() != null)) {
				String resultString = response.body().string();
				if (isAccessDenied(resultString)) {
					return DOWNLOAD_RESULT_FAILED;
				} else {
					result = DOWNLOAD_RESULT_SUCCESS;
				}

				handleResponseStockInformation(stock, resultString);
				Thread.sleep(Constant.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	private int downloadStockRealTime(Stock stock) {
		String modified = "";
		int result = DOWNLOAD_RESULT_NONE;

		if (stock == null) {
			return result;
		}

		modified = stock.getRealTimeModified();

		if (Market.isOutOfDateToday(modified) || Market.isTradingHours(Calendar.getInstance())) {
			return downloadStockRealTime(stock, getRequestHeader(), getStockRealTimeURLString(stock));
		}

		return result;
	}

	private int downloadStockRealTime(Stock stock, ArrayMap<String, String> requestHeaderArray, String urlString) {
		int result = DOWNLOAD_RESULT_NONE;

		Log.d(TAG, "downloadStockRealTime:" + urlString);

		Request.Builder builder = new Request.Builder();
		for (int i = 0; i < requestHeaderArray.size(); i++) {
			builder.addHeader(requestHeaderArray.keyAt(i), requestHeaderArray.valueAt(i));
		}
		builder.url(urlString);
		Request request = builder.build();

		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if ((response != null) && (response.body() != null)) {
				String resultString = response.body().string();
				if (isAccessDenied(resultString)) {
					return DOWNLOAD_RESULT_FAILED;
				} else {
					result = DOWNLOAD_RESULT_SUCCESS;
				}

				handleResponseStockRealTime(stock, resultString);
				Thread.sleep(Constant.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	private int downloadStockFinancial(Stock stock) {
		int result = DOWNLOAD_RESULT_NONE;

		if (stock == null) {
			return result;
		}

		StockFinancial stockFinancial = new StockFinancial();
		stockFinancial.setStockId(stock.getId());

		mStockDatabaseManager.getStockFinancial(stock, stockFinancial);

		if (stockFinancial.getCreated().contains(
				Utility.getCurrentDateString())
				|| stockFinancial.getModified().contains(
				Utility.getCurrentDateString())) {
			if ((stockFinancial.getBookValuePerShare() != 0)
					&& (stockFinancial.getNetProfit() != 0)) {
				return result;
			}
		}

		return downloadStockFinancial(stock, stockFinancial, getStockFinancialURLString(stock));
	}

	private int downloadStockFinancial(Stock stock, StockFinancial stockFinancial, String urlString) {
		int result = DOWNLOAD_RESULT_NONE;

		Log.d(TAG, "downloadStockFinancial:" + urlString);

		Request.Builder builder = new Request.Builder();
		builder.url(urlString);
		Request request = builder.build();

		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if ((response != null) && (response.body() != null)) {
				String resultString = response.body().string();
				if (isAccessDenied(resultString)) {
					return DOWNLOAD_RESULT_FAILED;
				} else {
					result = DOWNLOAD_RESULT_SUCCESS;
				}

				handleResponseStockFinancial(stock, stockFinancial, resultString);
				Thread.sleep(Constant.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	private int downloadIPO() {
		int result = DOWNLOAD_RESULT_NONE;

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

	private int downloadIPO(String urlString) {
		int result = DOWNLOAD_RESULT_NONE;

		Log.d(TAG, "downloadIPO:" + urlString);

		Request.Builder builder = new Request.Builder();
		builder.url(urlString);
		Request request = builder.build();

		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if ((response != null) && (response.body() != null)) {
				String resultString = response.body().string();
				if (isAccessDenied(resultString)) {
					return DOWNLOAD_RESULT_FAILED;
				} else {
					result = DOWNLOAD_RESULT_SUCCESS;
				}

				handleResponseIPO(resultString);
				Thread.sleep(Constant.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	private int downloadShareBonus(Stock stock) {
		int result = DOWNLOAD_RESULT_NONE;

		if (stock == null) {
			return result;
		}

		ShareBonus shareBonus = new ShareBonus();
		shareBonus.setStockId(stock.getId());

		mStockDatabaseManager.getShareBonus(stock.getId(), shareBonus);
		if (shareBonus.getCreated().contains(
				Utility.getCurrentDateString())
				|| shareBonus.getModified().contains(
				Utility.getCurrentDateString())) {
			if (!TextUtils.isEmpty(shareBonus.getDate())) {
				return result;
			}
		}

		return downloadShareBonus(stock, shareBonus, getShareBonusURLString(stock));
	}

	private int downloadShareBonus(Stock stock, ShareBonus shareBonus, String urlString) {
		int result = DOWNLOAD_RESULT_NONE;

		Log.d(TAG, "downloadShareBonus:" + urlString);

		Request.Builder builder = new Request.Builder();
		builder.url(urlString);
		Request request = builder.build();

		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if ((response != null) && (response.body() != null)) {
				String resultString = response.body().string();
				if (isAccessDenied(resultString)) {
					return DOWNLOAD_RESULT_FAILED;
				} else {
					result = DOWNLOAD_RESULT_SUCCESS;
				}

				handleResponseShareBonus(stock, shareBonus, resultString);
				Thread.sleep(Constant.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	private int downloadTotalShare(Stock stock) {
		int result = DOWNLOAD_RESULT_NONE;

		if (stock == null) {
			return result;
		}

		TotalShare totalShare = new TotalShare();
		totalShare.setStockId(stock.getId());

		mStockDatabaseManager.getTotalShare(stock.getId(), totalShare);
		if (totalShare.getCreated().contains(
				Utility.getCurrentDateString())
				|| totalShare.getModified().contains(
				Utility.getCurrentDateString())) {
			if (totalShare.getTotalShare() > 0) {
				return result;
			}
		}

		return downloadTotalShare(stock, totalShare, getTotalShareURLString(stock));
	}

	private int downloadTotalShare(Stock stock, TotalShare totalShare, String urlString) {
		int result = DOWNLOAD_RESULT_NONE;

		Log.d(TAG, "downloadTotalShare:" + urlString);

		Request.Builder builder = new Request.Builder();
		builder.url(urlString);
		Request request = builder.build();

		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if ((response != null) && (response.body() != null)) {
				String resultString = response.body().string();
				if (isAccessDenied(resultString)) {
					return DOWNLOAD_RESULT_FAILED;
				} else {
					result = DOWNLOAD_RESULT_SUCCESS;
				}

				handleResponseTotalShare(stock, totalShare, resultString);
				Thread.sleep(Constant.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	private int downloadStockDataHistory(Stock stock) {
		int result = DOWNLOAD_RESULT_NONE;

		if (stock == null) {
			return result;
		}

		for (String period : Setting.KEY_PERIODS) {
			if (Preferences.getBoolean(mContext, period, false)) {
				result = downloadStockDataHistory(stock, period);
			}
		}

		return result;
	}

	private int downloadStockDataHistory(Stock stock, String period) {
		int result = DOWNLOAD_RESULT_NONE;

		if (stock == null) {
			return result;
		}

		StockData stockData = new StockData(period);
		stockData.setStockId(stock.getId());
		mStockDatabaseManager.getStockData(stockData);
		stockData.setSE(stock.getSE());
		stockData.setCode(stock.getCode());
		stockData.setName(stock.getName());

		int len = getDownloadStockDataLength(stockData);
		if (len <= 0) {
			return result;
		}

		return downloadStockDataHistory(stock, stockData, getStockDataHistoryURLString(stock,
				stockData, len));
	}

	private int downloadStockDataHistory(Stock stock, StockData stockData, String urlString) {
		int result = DOWNLOAD_RESULT_NONE;

		Log.d(TAG, "downloadStockDataHistory:" + urlString);

		Request.Builder builder = new Request.Builder();
		builder.url(urlString);
		Request request = builder.build();

		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if ((response != null) && (response.body() != null)) {
				String resultString = response.body().string();
				if (isAccessDenied(resultString)) {
					return DOWNLOAD_RESULT_FAILED;
				} else {
					result = DOWNLOAD_RESULT_SUCCESS;
				}

				handleResponseStockDataHistory(stock, stockData, resultString);
				Thread.sleep(Constant.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	private int downloadStockDataRealTime(Stock stock) {
		int result = DOWNLOAD_RESULT_NONE;

		if (stock == null) {
			return result;
		}

		for (String period : Setting.KEY_PERIODS) {
			if (Preferences.getBoolean(mContext, period, false)) {
				if (Setting.KEY_PERIOD_DAY.equals(period)) {
					result = downloadStockDataRealTime(stock, period);
				}
			}
		}

		return result;
	}

	private int downloadStockDataRealTime(Stock stock, String period) {
		int result = DOWNLOAD_RESULT_NONE;

		if (stock == null) {
			return result;
		}

		StockData stockData = new StockData(period);
		stockData.setStockId(stock.getId());
		mStockDatabaseManager.getStockData(stockData);
		stockData.setSE(stock.getSE());
		stockData.setCode(stock.getCode());
		stockData.setName(stock.getName());

		int len = getDownloadStockDataLength(stockData);
		if (len <= 0) {
			return result;
		}

		return downloadStockDataRealTime(stock, stockData, getRequestHeader(), getStockDataRealTimeURLString(stock));
	}

	private int downloadStockDataRealTime(Stock stock, StockData stockData, ArrayMap<String, String> requestHeaderArray, String urlString) {
		int result = DOWNLOAD_RESULT_NONE;

		Log.d(TAG, "downloadStockDataRealTime:" + urlString);

		Request.Builder builder = new Request.Builder();
		for (int i = 0; i < requestHeaderArray.size(); i++) {
			builder.addHeader(requestHeaderArray.keyAt(i), requestHeaderArray.valueAt(i));
		}
		builder.url(urlString);
		Request request = builder.build();

		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if ((response != null) && (response.body() != null)) {
				String resultString = response.body().string();
				if (isAccessDenied(resultString)) {
					return DOWNLOAD_RESULT_FAILED;
				} else {
					result = DOWNLOAD_RESULT_SUCCESS;
				}

				handleResponseStockDataRealTime(stock, stockData, resultString);
				Thread.sleep(Constant.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	void loadIndexComponentStockList(@NonNull Stock index, @NonNull ArrayList<Stock> stockList) {
		ArrayList<IndexComponent> indexComponentList = new ArrayList<>();
		ArrayMap<String, Stock> stockArrayMap = new ArrayMap<String, Stock>();

		loadStockArrayMap(stockArrayMap);

		String selection = DatabaseContract.COLUMN_INDEX_CODE + " = " + index.getCode();
		mStockDatabaseManager.getIndexComponentList(indexComponentList, selection, null);

		stockList.clear();
		for (IndexComponent indexComponent : indexComponentList) {
			if (stockArrayMap.containsKey(indexComponent.getCode())) {
				stockList.add(stockArrayMap.get(indexComponent.getCode()));
			}
		}
	}

	private void setupIndex(@NonNull Stock index) {
		ArrayList<Stock> stockList = new ArrayList<>();
		ArrayList<StockData> stockDataList;
		ArrayList<StockData> indexStockDataList;
		Map<String, StockData> indexStockDataMap = new HashMap<>();
		StockData indexStockData = null;

		try {
			loadIndexComponentStockList(index, stockList);

			for (String period : Setting.KEY_PERIODS) {
				if (!Preferences.getBoolean(mContext, period, false)) {
					continue;
				}

				indexStockDataMap.clear();

				for (Stock stock : stockList) {
					stockDataList = stock.getStockDataList(period);
					loadStockDataList(stock, period, stockDataList);
					if ((stockDataList == null) || (stockDataList.size() == 0)) {
						continue;
					}

					long hold = stock.getHold();
					if (hold == 0) {
						hold = 1;
					}

					for (StockData stockData : stockDataList) {
						String keyString = stockData.getDateTime();
						double open = stockData.getOpen() * hold;
						double close = stockData.getClose() * hold;
						double high = stockData.getHigh() * hold;
						double low = stockData.getLow() * hold;

						if (indexStockDataMap.containsKey(keyString)) {
							indexStockData = indexStockDataMap.get(keyString);

							indexStockData.setOpen(indexStockData.getOpen() + open);
							indexStockData.setClose(indexStockData.getClose() + close);
							indexStockData.setHigh(indexStockData.getHigh() + high);
							indexStockData.setLow(indexStockData.getLow() + low);

							indexStockData.setVertexHigh(indexStockData.getHigh());
							indexStockData.setVertexLow(indexStockData.getLow());
						} else {
							indexStockData = new StockData(period);
							indexStockData.setStockId(index.getId());
							indexStockData.setDate(stockData.getDate());
							indexStockData.setTime(stockData.getTime());

							indexStockData.setOpen(open);
							indexStockData.setClose(close);
							indexStockData.setHigh(high);
							indexStockData.setLow(low);

							indexStockData.setVertexHigh(indexStockData.getHigh());
							indexStockData.setVertexLow(indexStockData.getLow());
						}

						indexStockDataMap.put(keyString, indexStockData);
					}
				}

				if (indexStockDataMap.size() > 0) {
					indexStockDataList = new ArrayList<>(indexStockDataMap.values());

					Collections.sort(indexStockDataList, StockData.comparator);
					updateDatabase(index, period, indexStockDataList);

					if (period.equals(Setting.KEY_PERIOD_DAY)) {
						double prevPrice = 0;
						double price = 0;
						double net = 0;
						price = indexStockData.getClose();
						if (price > 0 && indexStockDataList.size() > 1) {
							prevPrice = indexStockDataList.get(indexStockDataList.size() - 2).getClose();
							if (prevPrice > 0) {
								net = (price - prevPrice) / prevPrice;
							}
						}
						index.setPrice(Utility.Round(price, Constant.DOUBLE_FIXED_DECIMAL));
						index.setNet(Utility.Round(net, Constant.DOUBLE_FIXED_DECIMAL));
					}
				}
			}

			index.setModified(Utility.getCurrentDateTimeString());
			mStockDatabaseManager.updateStock(index,
					index.getContentValues());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isAccessDenied(String string) {
		StringBuilder contentTitle = new StringBuilder();
		boolean result = false;

		if (TextUtils.isEmpty(string)) {
			return result;
		}

		String accessDeniedString;
		for (int i = 0; i < mAccessDeniedStringArray.size(); i++) {
			accessDeniedString = mAccessDeniedStringArray.get(i);

			if (string.contains(accessDeniedString)) {
				contentTitle.append(mContext.getResources().getString(R.string.action_download));
				contentTitle.append(" ");
				contentTitle.append(accessDeniedString);

				notify(Constant.SERVICE_NOTIFICATION_ID, Constant.MESSAGE_CHANNEL_ID, Constant.MESSAGE_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH,
						contentTitle.toString(), "");

				mHandlerThread.quit();

				result = true;
				break;
			}
		}

		return result;
	}

	private final class ServiceHandler extends Handler {
		ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			boolean dataChanged = false;

			if (msg == null) {
				return;
			}

			try {
				acquireWakeLock();

				Stock stock = (Stock) msg.obj;

				if (stock == null) {
					return;
				}

				if (TextUtils.isEmpty(stock.getSE()) || TextUtils.isEmpty(stock.getCode())) {
					return;
				}

				if (Stock.CLASS_INDEX.equals(stock.getClasses())) {
					setupIndex(stock);
					dataChanged = true;
				} else {
					int result = DOWNLOAD_RESULT_NONE;

					result = downloadStockInformation(stock);
					if (result == DOWNLOAD_RESULT_FAILED) {
						return;
					} else if (result == DOWNLOAD_RESULT_SUCCESS) {
						dataChanged = true;
					}

					result = downloadStockRealTime(stock);
					if (result == DOWNLOAD_RESULT_FAILED) {
						return;
					} else if (result == DOWNLOAD_RESULT_SUCCESS) {
						dataChanged = true;
					}

					result = downloadStockFinancial(stock);
					if (result == DOWNLOAD_RESULT_FAILED) {
						return;
					} else if (result == DOWNLOAD_RESULT_SUCCESS) {
						dataChanged = true;
					}

					result = downloadShareBonus(stock);
					if (result == DOWNLOAD_RESULT_FAILED) {
						return;
					} else if (result == DOWNLOAD_RESULT_SUCCESS) {
						dataChanged = true;
					}

					result = downloadTotalShare(stock);
					if (result == DOWNLOAD_RESULT_FAILED) {
						return;
					} else if (result == DOWNLOAD_RESULT_SUCCESS) {
						dataChanged = true;
					}

					result = downloadStockDataHistory(stock);
					if (result == DOWNLOAD_RESULT_FAILED) {
						return;
					} else if (result == DOWNLOAD_RESULT_SUCCESS) {
						dataChanged = true;
					}

					result = downloadStockDataRealTime(stock);
					if (result == DOWNLOAD_RESULT_FAILED) {
						return;
					} else if (result == DOWNLOAD_RESULT_SUCCESS) {
						dataChanged = true;
					}
				}

				if (true) {
					for (String period : Setting.KEY_PERIODS) {
						if (Preferences.getBoolean(mContext, period, false)) {
							analyze(stock, period);
						}
					}

					analyze(stock);
					sendBroadcast(Constant.ACTION_RESTART_LOADER, stock.getId());
				}

//                if (downloadIPO() == DOWNLOAD_RESULT_FAILED) {
//                    return;
//                }
//                sendBroadcast(Constant.ACTION_RESTART_LOADER,
//                        Stock.INVALID_ID);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				releaseWakeLock();
			}
		}
	}
}