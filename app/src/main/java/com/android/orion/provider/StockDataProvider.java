package com.android.orion.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.text.TextUtils;
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.orion.analyzer.StockAnalyzer;
import com.android.orion.application.MainApplication;
import com.android.orion.config.Config;
import com.android.orion.data.Period;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.IndexComponent;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.interfaces.IStockDataProvider;
import com.android.orion.interfaces.StockListener;
import com.android.orion.manager.DatabaseManager;
import com.android.orion.manager.StockManager;
import com.android.orion.service.StockService;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Market;
import com.android.orion.utility.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.OkHttpClient;

public class StockDataProvider implements StockListener, IStockDataProvider {

	public static final int RESULT_SUCCESS = 1;
	public static final int RESULT_NONE = 0;
	public static final int RESULT_FAILED = -1;
	protected static volatile IStockDataProvider mInstance;
	static ArrayMap<String, Stock> mStockArrayMap = new ArrayMap<>();
	static ArrayMap<String, Stock> mRemovedArrayMap = new ArrayMap<>();
	static ArrayList<IndexComponent> mIndexComponentList = new ArrayList<>();
	static Map<String, StockData> mIndexStockDataMap = new HashMap<>();
	protected Context mContext;
	protected StockAnalyzer mStockAnalyzer = StockAnalyzer.getInstance();
	protected DatabaseManager mDatabaseManager = DatabaseManager.getInstance();
	protected OkHttpClient mOkHttpClient = new OkHttpClient();

	PowerManager mPowerManager;
	PowerManager.WakeLock mWakeLock;
	LocalBroadcastManager mLocalBroadcastManager;

	HandlerThread mHandlerThread;
	ServiceHandler mHandler;

	Logger Log = Logger.getLogger();

	public StockDataProvider() {
		mContext = MainApplication.getContext();

		mPowerManager = (PowerManager) mContext
				.getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				Config.TAG + ":" + StockDataProvider.class.getSimpleName());
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);

		mHandlerThread = new HandlerThread(StockDataProvider.class.getSimpleName(),
				Process.THREAD_PRIORITY_BACKGROUND);
		mHandlerThread.start();
		mHandler = new ServiceHandler(mHandlerThread.getLooper());

		StockManager.getInstance().registerStockListener(this);
	}

	public static IStockDataProvider getInstance() {
		if (TextUtils.equals(Config.stockDataProvider, SinaFinance.PROVIDER_NAME)) {
			mInstance = SinaFinance.getInstance();
		} else {
			if (mInstance == null) {
				synchronized (StockDataProvider.class) {
					if (mInstance == null) {
						mInstance = new StockDataProvider();
					}
				}
			}
		}
		return mInstance;
	}

	@NonNull
	public static ArrayList<String> getDatetimeMin15List() {
		ArrayList<String> datetimeList = new ArrayList<>();
		datetimeList.add("09:45:00");
		datetimeList.add("10:00:00");
		datetimeList.add("10:15:00");
		datetimeList.add("10:30:00");
		datetimeList.add("10:45:00");
		datetimeList.add("11:00:00");
		datetimeList.add("11:15:00");
		datetimeList.add("11:30:00");
		datetimeList.add("13:15:00");
		datetimeList.add("13:30:00");
		datetimeList.add("13:45:00");
		datetimeList.add("14:00:00");
		datetimeList.add("14:15:00");
		datetimeList.add("14:30:00");
		datetimeList.add("14:45:00");
		datetimeList.add("15:00:00");
		return datetimeList;
	}

	@NonNull
	public static ArrayList<String> getDatetimeMinL30ist() {
		ArrayList<String> datetimeList = new ArrayList<>();
		datetimeList.add("10:00:00");
		datetimeList.add("10:30:00");
		datetimeList.add("11:00:00");
		datetimeList.add("11:30:00");
		datetimeList.add("13:30:00");
		datetimeList.add("14:00:00");
		datetimeList.add("14:30:00");
		datetimeList.add("15:00:00");
		return datetimeList;
	}

	@NonNull
	public static ArrayList<String> getDatetimeMin60List() {
		ArrayList<String> datetimeList = new ArrayList<>();
		datetimeList.add("10:30:00");
		datetimeList.add("11:30:00");
		datetimeList.add("14:00:00");
		datetimeList.add("15:00:00");
		return datetimeList;
	}

	public void acquireWakeLock() {
		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire(Config.wakelockTimeout);
			Log.d("mWakeLock acquired.");
		}
	}

	public void releaseWakeLock() {
		if (mWakeLock.isHeld()) {
			mWakeLock.release();
			Log.d("mWakeLock released.");
		}
	}

	public void fixContentValuesList(StockData stockData, ArrayList<ContentValues> contentValuesList) {
		if (stockData == null || contentValuesList == null) {
			return;
		}

		int size = contentValuesList.size();
		int n = 0;

		switch (stockData.getPeriod()) {
			case Period.MIN5:
				n = size - (Setting.getDebugDataFile() ? Config.MAX_CONTENT_LENGTH_MIN5_LONG : Config.MAX_CONTENT_LENGTH_MIN5_SHORT);
				break;
			case Period.MIN15:
				n = size - (Setting.getDebugDataFile() ? Config.MAX_CONTENT_LENGTH_MIN15_LONG : Config.MAX_CONTENT_LENGTH_MIN15_SHORT);
				break;
			case Period.MIN30:
				n = size - (Setting.getDebugDataFile() ? Config.MAX_CONTENT_LENGTH_MIN30_LONG : Config.MAX_CONTENT_LENGTH_MIN30_SHORT);
				break;
			case Period.MIN60:
				n = size - Config.MAX_CONTENT_LENGTH_MIN60;
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

	@Override
	public void onDestroy() {
		releaseWakeLock();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
	                                      @NonNull String key) {
		if (key.contains(Setting.SETTING_PERIOD_)) {
			if (!sharedPreferences.getBoolean(key, false)) {
				return;
			}

			mDatabaseManager.loadStockArrayMap(mStockArrayMap);
			for (Stock current : mStockArrayMap.values()) {
				Setting.setDownloadStockData(current.getSE(), current.getCode(), 0);
			}
			download();
		}
	}

	@Override
	public void download() {
		if (!Utility.isNetworkConnected(mContext)) {
			Log.d("return, isNetworkConnected=" + Utility.isNetworkConnected(mContext));
			return;
		}

		mDatabaseManager.loadStockArrayMap(mStockArrayMap);

		int index = -1;
		for (Stock current : mStockArrayMap.values()) {
			index++;
			Log.d("index=" + index);

			if (mHandler.hasMessages(Integer.parseInt(current.getCode()))) {
				Log.d("mHandler.hasMessages " + Integer.parseInt(current.getCode()) + ", skip!");
			} else {
				Message msg = mHandler.obtainMessage(Integer.parseInt(current.getCode()), current);
				mHandler.sendMessage(msg);
				Log.d("mHandler.sendMessage " + msg);
			}
		}
	}

	@Override
	public void download(Stock stock) {
		if (!Utility.isNetworkConnected(mContext)) {
			Log.d("return, isNetworkConnected=" + Utility.isNetworkConnected(mContext));
			return;
		}

		if (stock == null || TextUtils.isEmpty(stock.getCode())) {
			return;
		}

		mDatabaseManager.loadStockArrayMap(mStockArrayMap);

		for (Stock current : mStockArrayMap.values()) {
			if (TextUtils.equals(current.getCode(), stock.getCode())) {
				continue;
			}

			if (mHandler.hasMessages(Integer.parseInt(current.getCode()))) {
				mHandler.removeMessages(Integer.parseInt(current.getCode()));
				Log.d("mHandler.hasMessages " + Integer.parseInt(current.getCode()) + ", removed!");
				mRemovedArrayMap.put(current.getCode(), current);
			}
		}

		if (mHandler.hasMessages(Integer.parseInt(stock.getCode()))) {
			Log.d("mHandler.hasMessages " + Integer.parseInt(stock.getCode()) + ", skip!");
		} else {
			Message msg = mHandler.obtainMessage(Integer.parseInt(stock.getCode()), stock);
			mHandler.sendMessage(msg);
			Log.d("mHandler.sendMessage" + msg);
		}

		for (Stock current : mRemovedArrayMap.values()) {
			if (mHandler.hasMessages(Integer.parseInt(current.getCode()))) {
				Log.d("mHandler.hasMessages " + Integer.valueOf(current.getCode()) + ", skip!");
			} else {
				Message msg = mHandler.obtainMessage(Integer.parseInt(current.getCode()), current);
				mHandler.sendMessage(msg);
				Log.d("mHandler.sendMessage " + msg);
			}
		}
	}

	void loadIndexComponentStockList(Stock index, ArrayList<Stock> stockList) {
		if (index == null || stockList == null) {
			return;
		}

		mDatabaseManager.loadStockArrayMap(mStockArrayMap);

		String selection = DatabaseContract.COLUMN_INDEX_CODE + " = " + index.getCode();
		mDatabaseManager.getIndexComponentList(mIndexComponentList, selection, null);

		stockList.clear();
		for (IndexComponent indexComponent : mIndexComponentList) {
			if (mStockArrayMap.containsKey(indexComponent.getCode())) {
				stockList.add(mStockArrayMap.get(indexComponent.getCode()));
			}
		}
	}

	private void setupIndex(Stock index) {
		ArrayList<Stock> stockList = new ArrayList<>();
		ArrayList<StockData> stockDataList;
		ArrayList<StockData> indexStockDataList;
		long weight;

		if (index == null) {
			return;
		}

		try {
			loadIndexComponentStockList(index, stockList);

			for (String period : Period.PERIODS) {
				if (!Setting.getPeriod(period)) {
					continue;
				}

				mIndexStockDataMap.clear();

				Calendar begin = null;
				Calendar end = null;

				for (Stock stock : stockList) {
					stockDataList = stock.getArrayList(period, Period.TYPE_STOCK_DATA);
					mDatabaseManager.loadStockDataList(stock, period, stockDataList);
					if ((stockDataList == null) || (stockDataList.size() == 0)) {
						continue;
					}

					weight = stock.getHold();

					StockData first = stockDataList.get(0);
					if (begin == null) {
						begin = first.getCalendar();
					} else if (first.getCalendar().after(begin)) {
						begin = first.getCalendar();
					}

					StockData last = stockDataList.get(stockDataList.size() - 1);
					if (end == null) {
						end = last.getCalendar();
					} else if (last.getCalendar().before(end)) {
						end = last.getCalendar();
					}

					for (StockData stockData : stockDataList) {
						StockData indexStockData;
						String keyString = stockData.getDateTime();

						if (stockData.getCalendar().before(begin) || stockData.getCalendar().after(end)) {
							continue;
						}

						if (mIndexStockDataMap.containsKey(keyString)) {
							indexStockData = mIndexStockDataMap.get(keyString);

							indexStockData.add(stockData, weight);
						} else {
							indexStockData = new StockData(period);

							indexStockData.setStockId(index.getId());
							indexStockData.setSE(index.getSE());
							indexStockData.setCode(index.getCode());
							indexStockData.setName(index.getName());
							indexStockData.setDate(stockData.getDate());
							indexStockData.setTime(stockData.getTime());

							indexStockData.add(stockData, weight);
						}

						mIndexStockDataMap.put(keyString, indexStockData);
					}
				}

				if (mIndexStockDataMap.size() == 0) {
					continue;
				}

				indexStockDataList = new ArrayList<>(mIndexStockDataMap.values());
				Collections.sort(indexStockDataList, StockData.comparator);

				while (indexStockDataList.size() > 0) {
					StockData indexStockDataBegin = indexStockDataList.get(0);
					StockData indexStockDataEnd = indexStockDataList.get(indexStockDataList.size() - 1);
					if (indexStockDataBegin.getCalendar().before(begin)) {
						indexStockDataList.remove(indexStockDataBegin);
					} else if (indexStockDataEnd.getCalendar().after(end)) {
						indexStockDataList.remove(indexStockDataEnd);
					} else {
						break;
					}
				}

				mDatabaseManager.updateStockData(index, period, indexStockDataList);
				index.setModified(Utility.getCurrentDateTimeString());
				mDatabaseManager.updateStock(index, index.getContentValues());

				if (TextUtils.equals(period, Period.DAY) && (indexStockDataList.size() > 1)) {
					double prevPrice = indexStockDataList.get(indexStockDataList.size() - 2).getCandlestick().getClose();
					double price = indexStockDataList.get(indexStockDataList.size() - 1).getCandlestick().getClose();
					double net = 0;
					if (prevPrice > 0) {
						net = 100.0 * (price - prevPrice) / prevPrice;
					}
					index.setPrice(Utility.Round(price));
					index.setNet(Utility.Round(net));
				}
			}

			index.setModified(Utility.getCurrentDateTimeString());
			mDatabaseManager.updateStock(index,
					index.getContentValues());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String getStockDataFileName(Stock stock) {
		String result = "";

		if (stock == null) {
			return result;
		}

		try {
			result = Environment.getExternalStorageDirectory().getCanonicalPath() + "/Orion/"
					+ stock.getSE().toUpperCase(Locale.getDefault()) + "#" + stock.getCode() + Constant.FILE_EXT_TEXT;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	String getStockDataFileName(Stock stock, String period) {
		String result = "";

		if (stock == null) {
			return result;
		}

		try {
			result = Environment.getExternalStorageDirectory().getCanonicalPath() + "/Orion/"
					+ stock.getSE().toUpperCase(Locale.getDefault()) + "#" + stock.getCode() + "#" + period + Constant.FILE_EXT_TEXT;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	//					SH#600938.txt
	//					日期	    时间	    开盘	    最高	    最低	    收盘	    成交量	    成交额
	//					2023/01/03	0935	37.08	37.08	36.72	36.81	6066500	223727792.00

	StockData mergeStockData(ArrayList<StockData> stockDataList, int size) {
		if (stockDataList == null || stockDataList.size() == 0 || size <= 0) {
			return null;
		}

		StockData result = new StockData();
		double high = 0;
		double low = 0;
		int j = 0;
		for (int i = stockDataList.size() - 1; i >= 0; i--, j++) {
			if (j >= size) {
				break;
			}

			StockData stockData = stockDataList.get(i);

			if (i == stockDataList.size() - 1) {
				high = stockData.getCandlestick().getHigh();
				low = stockData.getCandlestick().getLow();

				result.setDate(stockData.getDate());
				result.setTime(stockData.getTime());
			}

			result.getCandlestick().setOpen(stockData.getCandlestick().getOpen());

			if (stockData.getCandlestick().getHigh() > high) {
				high = stockData.getCandlestick().getHigh();
			}
			result.getCandlestick().setHigh(high);

			if (stockData.getCandlestick().getLow() < low) {
				low = stockData.getCandlestick().getLow();
			}
			result.getCandlestick().setLow(low);

			if (i == stockDataList.size() - 1) {
				result.getCandlestick().setClose(stockData.getCandlestick().getClose());
			}
		}

		return result;
	}

	void setupStockDataFile(Stock stock) {
		if (stock == null) {
			return;
		}

		try {
			//same as min5
			String fileName = getStockDataFileName(stock);
			ArrayList<String> lineList = new ArrayList<>();
			Utility.readFile(fileName, lineList);
			if (lineList.size() == 0) {
				return;
			}

//			ArrayList<String> datetimeMin5List = new ArrayList<>();//based on min5
			ArrayList<String> datetimeMin15List = getDatetimeMin15List();
			ArrayList<String> datetimeMin30List = getDatetimeMinL30ist();
			ArrayList<String> datetimeMin60List = getDatetimeMin60List();

			ArrayList<StockData> StockDataMin5List = new ArrayList<>();
			ArrayList<StockData> StockDataMin15List = new ArrayList<>();
			ArrayList<StockData> StockDataMin30List = new ArrayList<>();
			ArrayList<StockData> StockDataMin60List = new ArrayList<>();
			for (int i = 0; i < lineList.size(); i++) {
				String line = lineList.get(i);
				if (TextUtils.isEmpty(line)) {
					continue;
				}

				StockData stockDataMin5 = new StockData();
				if (stockDataMin5.fromString(line) == null) {
					continue;
				}

				StockDataMin5List.add(stockDataMin5);

				if (datetimeMin15List.contains(stockDataMin5.getTime())) {
					StockData stockData15 = mergeStockData(StockDataMin5List, 15 / 5);
					if (stockData15 != null) {
						StockDataMin15List.add(stockData15);
					}
				}

				if (datetimeMin30List.contains(stockDataMin5.getTime())) {
					StockData stockData30 = mergeStockData(StockDataMin15List, 30 / 15);
					if (stockData30 != null) {
						StockDataMin30List.add(stockData30);
					}
				}

				if (datetimeMin60List.contains(stockDataMin5.getTime())) {
					StockData stockData60 = mergeStockData(StockDataMin30List, 60 / 30);
					if (stockData60 != null) {
						StockDataMin60List.add(stockData60);
					}
				}
			}

			exportStockDataFile(stock, Period.MIN5, StockDataMin5List);
			exportStockDataFile(stock, Period.MIN15, StockDataMin15List);
			exportStockDataFile(stock, Period.MIN30, StockDataMin30List);
			exportStockDataFile(stock, Period.MIN60, StockDataMin60List);
			Utility.deleteFile(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void importStockDataFile(Stock stock, StockData stockData,
	                         ArrayList<ContentValues> contentValuesList,
	                         ArrayMap<String, StockData> stockDataMap) {
		if (stock == null || stockData == null || contentValuesList == null || stockDataMap == null) {
			return;
		}

		if (!Period.isMinutePeriod(stockData.getPeriod())) {
			return;
		}

		setupStockDataFile(stock);

		try {
			String fileName = getStockDataFileName(stock, stockData.getPeriod());
			ArrayList<String> lineList = new ArrayList<>();
			Utility.readFile(fileName, lineList);

			for (int i = 0; i < lineList.size(); i++) {
				String line = lineList.get(i);
				if (TextUtils.isEmpty(line)) {
					continue;
				}

				if (stockData.fromString(line) != null) {
					contentValuesList.add(stockData.getContentValues());
					stockDataMap.put(stockData.getDateTime(), new StockData(stockData));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void exportStockDataFile(Stock stock, StockData stockData, ArrayMap<String, StockData> stockDataMap) {
		if (stock == null || stockData == null || stockDataMap == null) {
			return;
		}

		if (!Period.isMinutePeriod(stockData.getPeriod())) {
			return;
		}

		if (stockDataMap.size() == 0) {
			return;
		}

		ArrayList<StockData> stockDataList = new ArrayList<>(stockDataMap.values());
		Collections.sort(stockDataList, StockData.comparator);
		exportStockDataFile(stock, stockData.getPeriod(), stockDataList);
	}

	void exportStockDataFile(Stock stock, String period, ArrayList<StockData> stockDataList) {
		if (stock == null || stockDataList == null) {
			return;
		}

		if (!Period.isMinutePeriod(period)) {
			return;
		}

		try {
			String fileName = getStockDataFileName(stock, period);
			ArrayList<String> lineList = new ArrayList<>();
			for (int i = 0; i < stockDataList.size(); i++) {
				lineList.add(stockDataList.get(i).toString());
			}
			Utility.writeFile(fileName, lineList, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onAddFavorite(Stock stock) {
		if (stock == null) {
			return;
		}

		Setting.setDownloadStock(stock.getSE(), stock.getCode(), 0);
		Setting.setDownloadStockData(stock.getSE(), stock.getCode(), 0);
		download(stock);
	}

	@Override
	public void onRemoveFavorite(Stock stock) {
		if (stock == null) {
			return;
		}

		mStockArrayMap.remove(stock.getCode());

		if (mHandler.hasMessages(Integer.parseInt(stock.getCode()))) {
			mHandler.removeMessages(Integer.parseInt(stock.getCode()));
		}
	}

	@Override
	public void onAddNotify(Stock stock) {
		if (stock == null) {
			return;
		}

		Setting.setDownloadStock(stock.getSE(), stock.getCode(), 0);
		Setting.setDownloadStockData(stock.getSE(), stock.getCode(), 0);
		download(stock);
	}

	@Override
	public void onRemoveNotify(Stock stock) {
		if (stock == null) {
			return;
		}

		mStockArrayMap.remove(stock.getCode());

		if (mHandler.hasMessages(Integer.parseInt(stock.getCode()))) {
			mHandler.removeMessages(Integer.parseInt(stock.getCode()));
		}
	}

	@Override
	public void onAddStock(Stock stock) {
		if (stock == null) {
			return;
		}

		Setting.setDownloadStock(stock.getSE(), stock.getCode(), 0);
		Setting.setDownloadStockData(stock.getSE(), stock.getCode(), 0);
		download(stock);
	}

	@Override
	public void onRemoveStock(Stock stock) {
		if (stock == null) {
			return;
		}

		if (mHandler.hasMessages(Integer.parseInt(stock.getCode()))) {
			mHandler.removeMessages(Integer.parseInt(stock.getCode()));
		}
	}

	@Override
	public int downloadStockHSA() {
		return 0;
	}

	@Override
	public int downloadStockInformation(Stock stock) {
		return 0;
	}

	@Override
	public int downloadStockFinancial(Stock stock) {
		return 0;
	}

	@Override
	public int downloadShareBonus(Stock stock) {
		return 0;
	}

	@Override
	public int downloadTotalShare(Stock stock) {
		return 0;
	}

	@Override
	public int downloadStockDataHistory(Stock stock) {
		return 0;
	}

	@Override
	public int downloadStockRealTime(Stock stock) {
		return 0;
	}

	@Override
	public int downloadStockDataRealTime(Stock stock) {
		return 0;
	}

	private final class ServiceHandler extends Handler {
		ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
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

				if (TextUtils.equals(stock.getClasses(), Stock.CLASS_A)) {
					long interval = System.currentTimeMillis() - Setting.getDownloadStock(stock.getSE(), stock.getCode());
					if (interval > Config.downloadStockInterval) {
						if (downloadStockInformation(stock) == RESULT_FAILED) {
							StockService.getInstance().onDisconnected();
							return;
						}

						if (downloadStockFinancial(stock) == RESULT_FAILED) {
							StockService.getInstance().onDisconnected();
							return;
						}

						if (downloadShareBonus(stock) == RESULT_FAILED) {
							StockService.getInstance().onDisconnected();
							return;
						}

						if (downloadTotalShare(stock) == RESULT_FAILED) {
							StockService.getInstance().onDisconnected();
							return;
						}
						Setting.setDownloadStock(stock.getSE(), stock.getCode(), System.currentTimeMillis());
					}

					interval = System.currentTimeMillis() - Setting.getDownloadStockData(stock.getSE(), stock.getCode());
					if (Market.isTradingHours() || Market.isLunchTime() || (interval > Config.downloadStockDataInterval)) {
						if (downloadStockDataHistory(stock) == RESULT_FAILED) {
							StockService.getInstance().onDisconnected();
							return;
						}

						if (downloadStockDataRealTime(stock) == RESULT_FAILED) {
							StockService.getInstance().onDisconnected();
							return;
						}

						if (downloadStockRealTime(stock) == RESULT_FAILED) {
							StockService.getInstance().onDisconnected();
							return;
						}
						Setting.setDownloadStockData(stock.getSE(), stock.getCode(), System.currentTimeMillis());
					}
				} else if (TextUtils.equals(stock.getClasses(), Stock.CLASS_INDEX)) {
					setupIndex(stock);
					Setting.setStockDataChanged(stock.getSE(), stock.getCode(), true);
				} else {
				}

				if (Setting.getStockDataChanged(stock.getSE(), stock.getCode())) {
					Setting.setStockDataChanged(stock.getSE(), stock.getCode(), false);
					for (String period : Period.PERIODS) {
						if (Setting.getPeriod(period)) {
							mStockAnalyzer.analyze(stock, period);
						}
					}

					mStockAnalyzer.analyze(stock);
					sendBroadcast(Constant.ACTION_RESTART_LOADER, stock.getId());
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				releaseWakeLock();
			}
		}
	}
}