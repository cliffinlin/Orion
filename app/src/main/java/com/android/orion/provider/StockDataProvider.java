package com.android.orion.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.text.TextUtils;
import android.util.ArrayMap;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.orion.analyzer.StockAnalyzer;
import com.android.orion.application.MainApplication;
import com.android.orion.config.Config;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.IndexComponent;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.interfaces.StockEditListener;
import com.android.orion.interfaces.StockListChangedListener;
import com.android.orion.manager.DatabaseManager;
import com.android.orion.manager.StockManager;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

public class StockDataProvider implements StockListChangedListener, StockEditListener, IStockDataProvider {

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

	public static final int RESULT_SUCCESS = 1;
	public static final int RESULT_NONE = 0;
	public static final int RESULT_FAILED = -1;

	static ArrayMap<String, Stock> mStockArrayMap = new ArrayMap<>();
	static ArrayMap<String, Stock> mRemovedArrayMap = new ArrayMap<>();
	static ArrayList<IndexComponent> mIndexComponentList = new ArrayList<>();
	static Map<String, StockData> mIndexStockDataMap = new HashMap<>();

	protected static IStockDataProvider mInstance;

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

	public static synchronized IStockDataProvider getInstance() {
		if (TextUtils.equals(Config.stockDataProvider, SinaFinance.PROVIDER_NAME)) {
			mInstance = SinaFinance.getInstance();
		} else {
			if (mInstance == null) {
				mInstance = new StockDataProvider();
			}
		}
		return mInstance;
	}

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

		StockManager.getInstance().registerStockEditListener(this);
		StockManager.getInstance().registerStockListChangedListener(this);
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

	public int getPeriodMinutes(String period) {
		int result = 0;

		if (TextUtils.equals(period, DatabaseContract.COLUMN_MIN1)) {
			result = PERIOD_MINUTES_MIN1;
		} else if (TextUtils.equals(period, DatabaseContract.COLUMN_MIN5)) {
			result = PERIOD_MINUTES_MIN5;
		} else if (TextUtils.equals(period, DatabaseContract.COLUMN_MIN15)) {
			result = PERIOD_MINUTES_MIN15;
		} else if (TextUtils.equals(period, DatabaseContract.COLUMN_MIN30)) {
			result = PERIOD_MINUTES_MIN30;
		} else if (TextUtils.equals(period, DatabaseContract.COLUMN_MIN60)) {
			result = PERIOD_MINUTES_MIN60;
		} else if (TextUtils.equals(period, DatabaseContract.COLUMN_DAY)) {
			result = PERIOD_MINUTES_DAY;
		} else if (TextUtils.equals(period, DatabaseContract.COLUMN_WEEK)) {
			result = PERIOD_MINUTES_WEEK;
		} else if (TextUtils.equals(period, DatabaseContract.COLUMN_MONTH)) {
			result = PERIOD_MINUTES_MONTH;
		} else if (TextUtils.equals(period, DatabaseContract.COLUMN_QUARTER)) {
			result = PERIOD_MINUTES_QUARTER;
		} else if (TextUtils.equals(period, DatabaseContract.COLUMN_YEAR)) {
			result = PERIOD_MINUTES_YEAR;
		} else {
		}

		return result;
	}

	public void fixContentValuesList(StockData stockData, ArrayList<ContentValues> contentValuesList) {
		if (stockData == null || contentValuesList == null) {
			return;
		}

		int size = contentValuesList.size();
		int n = 0;

		switch (stockData.getPeriod()) {
			case DatabaseContract.COLUMN_MIN1:
				break;
			case DatabaseContract.COLUMN_MIN5:
				n = size - MAX_CONTENT_LENGTH_MIN5;
				break;
			case DatabaseContract.COLUMN_MIN15:
				n = size - MAX_CONTENT_LENGTH_MIN15;
				break;
			case DatabaseContract.COLUMN_MIN30:
				n = size - MAX_CONTENT_LENGTH_MIN30;
				break;
			case DatabaseContract.COLUMN_MIN60:
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

	@Override
	public void onDestroy() {
		releaseWakeLock();
	}

	@Override
	public void download() {
		if (!Utility.isNetworkConnected(mContext)) {
			return;
		}

		mDatabaseManager.loadStockArrayMap(mStockArrayMap);

		int index = -1;
		for (Stock current : mStockArrayMap.values()) {
			index++;
			Log.d("index=" + index);

			if (mHandler.hasMessages(Integer.valueOf(current.getCode()))) {
				Log.d("mHandler.hasMessages " + Integer.valueOf(current.getCode()) + ", skip!");
			} else {
				Message msg = mHandler.obtainMessage(Integer.valueOf(current.getCode()), current);
				mHandler.sendMessage(msg);
				Log.d("mHandler.sendMessage " + msg);
			}
		}
	}

	@Override
	public void download(Stock stock) {
		if (!Utility.isNetworkConnected(mContext)) {
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

			if (mHandler.hasMessages(Integer.valueOf(current.getCode()))) {
				mHandler.removeMessages(Integer.valueOf(current.getCode()));
				Log.d("mHandler.hasMessages " + Integer.valueOf(current.getCode()) + ", removed!");
				mRemovedArrayMap.put(current.getCode(), current);
			}
		}

		if (mHandler.hasMessages(Integer.valueOf(stock.getCode()))) {
			Log.d("mHandler.hasMessages " + Integer.valueOf(stock.getCode()) + ", skip!");
		} else {
			Message msg = mHandler.obtainMessage(Integer.valueOf(stock.getCode()), stock);
			mHandler.sendMessage(msg);
			Log.d("mHandler.sendMessage" + msg);
		}

		for (Stock current : mRemovedArrayMap.values()) {
			if (mHandler.hasMessages(Integer.valueOf(current.getCode()))) {
				Log.d("mHandler.hasMessages " + Integer.valueOf(current.getCode()) + ", skip!");
			} else {
				Message msg = mHandler.obtainMessage(Integer.valueOf(current.getCode()), current);
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

			for (String period : DatabaseContract.PERIODS) {
				if (!Preferences.getBoolean(period, false)) {
					continue;
				}

				mIndexStockDataMap.clear();

				Calendar begin = null;
				Calendar end = null;

				for (Stock stock : stockList) {
					stockDataList = stock.getStockDataList(period);
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

				if (TextUtils.equals(period, DatabaseContract.COLUMN_DAY) && (indexStockDataList.size() > 1)) {
					double prevPrice = indexStockDataList.get(indexStockDataList.size() - 2).getClose();
					double price = indexStockDataList.get(indexStockDataList.size() - 1).getClose();
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

	@Override
	public void onStockAddFavorite(Stock stock) {
		if (stock == null) {
			return;
		}
		download(stock);
	}

	@Override
	public void onStockRemoveFavorite(Stock stock) {
		if (stock == null) {
			return;
		}
	}

	@Override
	public void onStockAdd(Stock stock) {
		if (stock == null) {
			return;
		}
	}

	@Override
	public void onStockRemove(Stock stock) {
		if (stock == null) {
			return;
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
					if (downloadStockInformation(stock) == RESULT_FAILED) {
						return;
					}

					if (downloadStockFinancial(stock) == RESULT_FAILED) {
						return;
					}

					if (downloadShareBonus(stock) == RESULT_FAILED) {
						return;
					}

					if (downloadTotalShare(stock) == RESULT_FAILED) {
						return;
					}

					if (downloadStockDataHistory(stock) == RESULT_FAILED) {
						return;
					}

					if (downloadStockDataRealTime(stock) == RESULT_FAILED) {
						return;
					}

					if (downloadStockRealTime(stock) == RESULT_FAILED) {
						return;
					}
				} else if (TextUtils.equals(stock.getClasses(), Stock.CLASS_INDEX)) {
					setupIndex(stock);
					Setting.setStockDataChanged(stock.getSE(), stock.getCode(), true);
				} else {
				}

				if (Setting.getStockDataChanged(stock.getSE(), stock.getCode())) {
					Setting.setStockDataChanged(stock.getSE(), stock.getCode(), false);
					for (String period : DatabaseContract.PERIODS) {
						if (Preferences.getBoolean(period, false)) {
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