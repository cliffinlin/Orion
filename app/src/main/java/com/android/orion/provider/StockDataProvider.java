package com.android.orion.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.text.TextUtils;
import android.util.ArrayMap;

import androidx.annotation.NonNull;

import com.android.orion.analyzer.StockAnalyzer;
import com.android.orion.application.MainApplication;
import com.android.orion.config.Config;
import com.android.orion.data.Period;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.TDXData;
import com.android.orion.interfaces.AnalyzeListener;
import com.android.orion.interfaces.DownloadListener;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

public class StockDataProvider implements StockListener, IStockDataProvider {

	public static final int RESULT_SUCCESS = 1;
	public static final int RESULT_NONE = 0;
	public static final int RESULT_FAILED = -1;
	protected static volatile IStockDataProvider mInstance;
	static ArrayMap<String, Stock> mStockArrayMap = new ArrayMap<>();
	static ArrayMap<String, Stock> mRemovedArrayMap = new ArrayMap<>();
	Context mContext = MainApplication.getContext();
	StockAnalyzer mStockAnalyzer = StockAnalyzer.getInstance();
	DatabaseManager mDatabaseManager = DatabaseManager.getInstance();
	OkHttpClient mOkHttpClient = new OkHttpClient();

	PowerManager mPowerManager;
	PowerManager.WakeLock mWakeLock;
	HandlerThread mHandlerThread;
	ServiceHandler mHandler;

	ArrayList<AnalyzeListener> mAnalyzeListenerList = new ArrayList<>();
	ArrayList<DownloadListener> mDownloadListenerList = new ArrayList<>();

	Logger Log = Logger.getLogger();

	public StockDataProvider() {
		mPowerManager = (PowerManager) mContext
				.getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				Config.TAG + ":" + StockDataProvider.class.getSimpleName());
		mHandlerThread = new HandlerThread(StockDataProvider.class.getSimpleName(),
				Process.THREAD_PRIORITY_LOWEST);
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

	@NonNull
	public static ArrayList<String> getDatetimeMonth6List() {
		ArrayList<String> datetimeList = new ArrayList<>();
		datetimeList.add("01");
		datetimeList.add("07");
		return datetimeList;
	}

	@NonNull
	public static ArrayList<String> getDatetimeQuarterList() {
		ArrayList<String> datetimeList = new ArrayList<>();
		datetimeList.add("01");
		datetimeList.add("04");
		datetimeList.add("07");
		datetimeList.add("10");
		return datetimeList;
	}

	@NonNull
	public static ArrayList<String> getDatetimeMonth2List() {
		ArrayList<String> datetimeList = new ArrayList<>();
		datetimeList.add("01");
		datetimeList.add("03");
		datetimeList.add("05");
		datetimeList.add("07");
		datetimeList.add("09");
		datetimeList.add("11");
		return datetimeList;
	}

	@Override
	public void registerAnalyzeListener(AnalyzeListener listener) {
		if (listener == null) {
			return;
		}
		if (!mAnalyzeListenerList.contains(listener)) {
			mAnalyzeListenerList.add(listener);
		}
	}

	@Override
	public void unRegisterAnalyzeListener(AnalyzeListener listener) {
		mAnalyzeListenerList.remove(listener);
	}

	@Override
	public void registerDownloadListener(DownloadListener listener) {
		if (listener == null) {
			return;
		}
		if (!mDownloadListenerList.contains(listener)) {
			mDownloadListenerList.add(listener);
		}
	}

	@Override
	public void unRegisterDownloadListener(DownloadListener listener) {
		mDownloadListenerList.remove(listener);
	}

	void onAnalyzeStart(String stockCode) {
		for (AnalyzeListener listener : mAnalyzeListenerList) {
			listener.onAnalyzeStart(stockCode);
		}
	}

	void onAnalyzeFinish(String stockCode) {
		for (AnalyzeListener listener : mAnalyzeListenerList) {
			listener.onAnalyzeFinish(stockCode);
		}
	}

	void onDownloadStart(String stockCode) {
		for (DownloadListener listener : mDownloadListenerList) {
			listener.onDownloadStart(stockCode);
		}
	}

	void onDownloadComplete(String stockCode) {
		for (DownloadListener listener : mDownloadListenerList) {
			listener.onDownloadComplete(stockCode);
		}
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
				n = size - Config.MAX_CONTENT_LENGTH_MIN5;
				break;
			case Period.MIN15:
				n = size - Config.MAX_CONTENT_LENGTH_MIN15;
				break;
			case Period.MIN30:
				n = size - Config.MAX_CONTENT_LENGTH_MIN30;
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
				Setting.setDownloadStockDataTimeMillis(current, 0);
			}
			download();
		}
	}

	@Override
	public void download() {
		if (!Utility.isNetworkConnected(mContext)) {
			mHandler.removeMessages(0);
			Log.d("return, No network connection");
			return;
		}

		mDatabaseManager.loadStockArrayMap(mStockArrayMap);
		if (mStockArrayMap.isEmpty()) {
			Log.d("return, Stock array map is empty");
			return;
		}

		int index = 0;
		for (Stock current : mStockArrayMap.values()) {
			Log.d("index=" + index++);

			String stockCodeStr = current.getCode();
			int messageID;
			try {
				messageID = Integer.parseInt(stockCodeStr);
			} catch (Exception e) {
				Log.d("Invalid stock code: " + stockCodeStr);
				continue;
			}

			if (mHandler.hasMessages(messageID)) {
				Log.d("Message already exists for code: " + stockCodeStr + ", skip!");
			} else {
				Message msg = mHandler.obtainMessage(messageID, current);
				mHandler.sendMessage(msg);
				Log.d("Sent message: " + msg);
			}
		}
	}

	@Override
	public void download(Stock stock) {
		if (!Utility.isNetworkConnected(mContext)) {
			mHandler.removeMessages(0);
			Log.d("return, isNetworkConnected=" + Utility.isNetworkConnected(mContext));
			return;
		}

		if (stock == null || TextUtils.isEmpty(stock.getCode())) {
			return;
		}

		mRemovedArrayMap.clear();

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

	StockData mergeStockDataMin(ArrayList<StockData> stockDataList, int size) {
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
				high = stockData.getCandle().getHigh();
				low = stockData.getCandle().getLow();

				result.setDate(stockData.getDate());
				result.setTime(stockData.getTime());
			}

			result.getCandle().setOpen(stockData.getCandle().getOpen());

			if (stockData.getCandle().getHigh() > high) {
				high = stockData.getCandle().getHigh();
			}
			result.getCandle().setHigh(high);

			if (stockData.getCandle().getLow() < low) {
				low = stockData.getCandle().getLow();
			}
			result.getCandle().setLow(low);

			if (i == stockDataList.size() - 1) {
				result.getCandle().setClose(stockData.getCandle().getClose());
			}
		}

		return result;
	}

	void setupStockDataFile(Stock stock, Uri uri) {
		if (stock == null) {
			return;
		}

		InputStream inputStream = null;
		String fileName = "";
		ArrayList<String> lineList = new ArrayList<>();
		try {
			if (uri.getScheme().equals("file")) {
				fileName = uri.getPath();
				Utility.readFile(fileName, lineList);
			} else if (uri.getScheme().equals("content")) {
				inputStream = mContext.getContentResolver().openInputStream(uri);
				if (inputStream != null) {
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
					String strLine;
					while ((strLine = bufferedReader.readLine()) != null) {
						lineList.add(strLine);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Utility.closeQuietly(inputStream);
		}

		try {
			if (lineList.size() == 0) {
				return;
			}

			if (lineList.size() > Config.MAX_CONTENT_LENGTH_MIN5) {
				lineList = new ArrayList<>(lineList.subList(lineList.size() - Config.MAX_CONTENT_LENGTH_MIN5, lineList.size()));
			}

//			ArrayList<String> datetimeMin5List = new ArrayList<>();//based on min5
			ArrayList<String> datetimeMin15List = getDatetimeMin15List();
			ArrayList<String> datetimeMin30List = getDatetimeMinL30ist();
			ArrayList<String> datetimeMin60List = getDatetimeMin60List();

			ArrayList<StockData> stockDataMin5List = new ArrayList<>();
			ArrayList<StockData> stockDataMin15List = new ArrayList<>();
			ArrayList<StockData> stockDataMin30List = new ArrayList<>();
			ArrayList<StockData> stockDataMin60List = new ArrayList<>();
			for (int i = 0; i < lineList.size(); i++) {
				String line = lineList.get(i);
				if (TextUtils.isEmpty(line)) {
					continue;
				}

				StockData stockDataMin5 = new StockData();
				if (stockDataMin5.fromString(line) == null) {
					continue;
				}

				stockDataMin5List.add(stockDataMin5);

				if (datetimeMin15List.contains(stockDataMin5.getTime())) {
					StockData stockData15 = mergeStockDataMin(stockDataMin5List, 15 / 5);
					if (stockData15 != null) {
						stockDataMin15List.add(stockData15);
					}
				}

				if (datetimeMin30List.contains(stockDataMin5.getTime())) {
					StockData stockData30 = mergeStockDataMin(stockDataMin15List, 30 / 15);
					if (stockData30 != null) {
						stockDataMin30List.add(stockData30);
					}
				}

				if (datetimeMin60List.contains(stockDataMin5.getTime())) {
					StockData stockData60 = mergeStockDataMin(stockDataMin30List, 60 / 30);
					if (stockData60 != null) {
						stockDataMin60List.add(stockData60);
					}
				}
			}
			saveTDXData(stock, Period.MIN5, stockDataMin5List);
			saveTDXData(stock, Period.MIN15, stockDataMin15List);
			saveTDXData(stock, Period.MIN30, stockDataMin30List);
			saveTDXData(stock, Period.MIN60, stockDataMin60List);

			mDatabaseManager.deleteStockData(stock);
			mDatabaseManager.deleteStockTrend(stock);
			mDatabaseManager.deleteStockPerceptron(stock.getId());
			Setting.setDownloadStockTimeMillis(stock, 0);
			Setting.setDownloadStockDataTimeMillis(stock, 0);
			download(stock);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void importTDXDataFile(ArrayList<Uri> uriList) {
		if (uriList == null || uriList.size() == 0) {
			return;
		}

		try {
			for (Uri uri : uriList) {
				if (uri == null) {
					continue;
				}

				String fileName = Utility.getFileNameFromContentUri(mContext, uri);
				if (TextUtils.isEmpty(fileName)) {
					continue;
				}

				Log.d("uri = " + uri + " fileName = " + fileName);
				String[] stockInfo = fileName.split(("[#.]"));
				if (stockInfo == null || stockInfo.length < 3) {
					continue;
				}

				Stock stock = new Stock();
				stock.setClasses(Stock.CLASS_A);
				stock.setSE(stockInfo[0].toLowerCase());
				stock.setCode(stockInfo[1]);
				Setting.setTdxDataFileUri(stock, uri.toString());
				if (!mDatabaseManager.isStockExist(stock)) {
					stock.setCreated(Utility.getCurrentDateTimeString());
					mDatabaseManager.insertStock(stock);
				} else {
					mDatabaseManager.getStock(stock);
				}
				setupStockDataFile(stock, uri);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void loadTDXData(Stock stock, StockData stockData,
	                 ArrayList<ContentValues> contentValuesList,
	                 ArrayMap<String, StockData> stockDataMap) {
		if (stock == null || stockData == null || contentValuesList == null || stockDataMap == null) {
			return;
		}

		if (!Period.isMinutePeriod(stockData.getPeriod())) {
			return;
		}

		try {
			ArrayList<String> contentList = new ArrayList<>();
			mDatabaseManager.getTDXDataContentList(stock, stockData.getPeriod(), contentList);
			for (int i = 0; i < contentList.size(); i++) {
				String content = contentList.get(i);
				if (TextUtils.isEmpty(content)) {
					continue;
				}
				if (stockData.fromString(content) != null) {
					contentValuesList.add(stockData.getContentValues());
					stockDataMap.put(stockData.getDateTime(), new StockData(stockData));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void saveStockDataAboveMonth(Stock stock, StockData stockData, ArrayList<StockData> stockDataList) {
		if (stock == null || stockData == null || stockDataList == null || stockDataList.size() == 0) {
			return;
		}

		if (!TextUtils.equals(Period.MONTH, stockData.getPeriod())) {
			return;
		}

		mDatabaseManager.deleteStockData(stockData.getSE(), stockData.getCode(), Period.YEAR);
		mDatabaseManager.deleteStockData(stockData.getSE(), stockData.getCode(), Period.MONTH6);
		mDatabaseManager.deleteStockData(stockData.getSE(), stockData.getCode(), Period.QUARTER);
		mDatabaseManager.deleteStockData(stockData.getSE(), stockData.getCode(), Period.MONTH2);

		mergeStockDataMonth(stock, Period.YEAR, stockDataList);
		mergeStockDataMonth(stock, Period.MONTH6, getDatetimeMonth6List(), stockDataList);
		mergeStockDataMonth(stock, Period.QUARTER, getDatetimeQuarterList(), stockDataList);
		mergeStockDataMonth(stock, Period.MONTH2, getDatetimeMonth2List(), stockDataList);
	}

	void mergeStockDataMonth(Stock stock, String period, ArrayList<StockData> stockDataList) {
		if (stockDataList == null || stockDataList.size() == 0) {
			return;
		}

		ArrayList<StockData> resultList = new ArrayList<>();
		StockData result = null;
		String year = "";
		double high = 0;
		double low = 0;
		for (int i = 0; i < stockDataList.size(); i++) {
			StockData stockData = stockDataList.get(i);
			if (!TextUtils.equals(stockData.getYear(), year)) {
				year = stockData.getYear();

				result = new StockData();
				resultList.add(result);

				result.setSE(stockData.getSE());
				result.setCode(stockData.getCode());
				result.setName(stockData.getName());
				result.setPeriod(period);
				result.getCandle().setOpen(stockData.getCandle().getOpen());
				high = stockData.getCandle().getHigh();
				low = stockData.getCandle().getLow();
			}

			if (result == null) {
				continue;
			}

			if (stockData.getCandle().getHigh() > high) {
				high = stockData.getCandle().getHigh();
			}
			result.getCandle().setHigh(high);

			if (stockData.getCandle().getLow() < low) {
				low = stockData.getCandle().getLow();
			}
			result.getCandle().setLow(low);

			result.getCandle().setClose(stockData.getCandle().getClose());

			result.setDate(stockData.getDate());
			result.setTime(stockData.getTime());
		}
		mDatabaseManager.updateStockData(stock, period, resultList);
	}

	void mergeStockDataMonth(Stock stock, String period, ArrayList<String> datetimeList, ArrayList<StockData> stockDataList) {
		if (datetimeList == null || stockDataList == null || stockDataList.size() == 0) {
			return;
		}

		ArrayList<StockData> resultList = new ArrayList<>();
		StockData result = null;
		double high = 0;
		double low = 0;
		for (int i = 0; i < stockDataList.size(); i++) {
			StockData stockData = stockDataList.get(i);
			if (datetimeList.contains(stockData.getMonth())) {
				result = new StockData();
				resultList.add(result);

				result.setSE(stockData.getSE());
				result.setCode(stockData.getCode());
				result.setName(stockData.getName());
				result.setPeriod(period);
				result.getCandle().setOpen(stockData.getCandle().getOpen());
				high = stockData.getCandle().getHigh();
				low = stockData.getCandle().getLow();
			}

			if (result == null) {
				continue;
			}

			if (stockData.getCandle().getHigh() > high) {
				high = stockData.getCandle().getHigh();
			}
			result.getCandle().setHigh(high);

			if (stockData.getCandle().getLow() < low) {
				low = stockData.getCandle().getLow();
			}
			result.getCandle().setLow(low);

			result.getCandle().setClose(stockData.getCandle().getClose());

			result.setDate(stockData.getDate());
			result.setTime(stockData.getTime());
		}
		mDatabaseManager.updateStockData(stock, period, resultList);
	}

	void saveTDXData(Stock stock, StockData stockData, ArrayMap<String, StockData> stockDataMap) {
		if (stock == null || stockData == null || stockDataMap == null || stockDataMap.size() == 0) {
			return;
		}

		if (!Period.isMinutePeriod(stockData.getPeriod())) {
			return;
		}

		ArrayList<StockData> stockDataList = new ArrayList<>(stockDataMap.values());
		Collections.sort(stockDataList, StockData.comparator);
		saveTDXData(stock, stockData.getPeriod(), stockDataList);
		if (stockData.getPeriod().equals(Period.MIN5)) {
			exportTDXData(stock, stockData.getPeriod(), stockDataList);
		}
	}

	void saveTDXData(Stock stock, String period, ArrayList<StockData> stockDataList) {
		if (stock == null || stockDataList == null || stockDataList.size() == 0) {
			return;
		}

		if (!Period.isMinutePeriod(period)) {
			return;
		}

		try {
			TDXData tdxData = new TDXData();
			mDatabaseManager.deleteTDXData(stock.getSE(), stock.getCode(), period);
			ContentValues[] contentValuesArray = new ContentValues[stockDataList.size()];
			for (StockData stockData : stockDataList) {
				if (stockData == null) {
					continue;
				}
				tdxData.init();
				tdxData.setSE(stock.getSE());
				tdxData.setCode(stock.getCode());
				tdxData.setName(stock.getName());
				tdxData.setPeriod(period);
				tdxData.setContent(stockData.toString());
				tdxData.setCreated(Utility.getCurrentDateTimeString());
				contentValuesArray[stockDataList.indexOf(stockData)] = tdxData.getContentValues();
			}
			mDatabaseManager.bulkInsertTDXData(contentValuesArray);
			Log.d("bulkInsertTDXData " + stock.getName() + " " + stock.getSE() + stock.getCode() + " " + period);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void exportTDXData(Stock stock, String period, ArrayList<StockData> stockDataList) {
		if (stock == null || stockDataList == null || stockDataList.size() < Config.MAX_CONTENT_LENGTH_MIN5) {
			return;
		}

		OutputStream outputStream = null;
		try {
			String uriString = Setting.getTdxDataFileUri(stock);
			if (TextUtils.isEmpty(uriString)) {
				return;
			}
			Uri uri = Uri.parse(uriString);
			outputStream = mContext.getContentResolver().openOutputStream(uri);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
			ArrayList<String> contentList = new ArrayList<>();
			mDatabaseManager.getTDXDataContentList(stock, period, contentList);
			int index = 0;
			if (writer != null) {
				for (String content : contentList) {
					writer.write(content);
					index++;
				}
				writer.close();
			}
			Log.d("exportTDXData " + stock.getName() + " " + stock.getSE() + stock.getCode() + " " + period + " index=" + index);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Utility.closeQuietly(outputStream);
		}
	}

	@Override
	public void onAddFavorite(Stock stock) {
		if (stock == null) {
			return;
		}

		Setting.setDownloadStockTimeMillis(stock, 0);
		Setting.setDownloadStockDataTimeMillis(stock, 0);
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

		Setting.setDownloadStockTimeMillis(stock, 0);
		Setting.setDownloadStockDataTimeMillis(stock, 0);
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

		Setting.setDownloadStockTimeMillis(stock, 0);
		Setting.setDownloadStockDataTimeMillis(stock, 0);
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
	public int downloadStockBonus(Stock stock) {
		return 0;
	}

	@Override
	public int downloadStockRZRQ(Stock stock) {
		return 0;
	}

	@Override
	public int downloadStockShare(Stock stock) {
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
			try {
				acquireWakeLock();

				if (msg == null) {
					return;
				}

				Stock stock = (Stock) msg.obj;

				if (stock == null) {
					return;
				}

				if (TextUtils.isEmpty(stock.getSE()) || TextUtils.isEmpty(stock.getCode())) {
					return;
				}

				if (!Utility.isNetworkConnected(mContext)) {
					removeMessages(0);
					Log.d("return, isNetworkConnected=" + Utility.isNetworkConnected(mContext));
					return;
				}

				onDownloadStart(stock.getCode());
				if (TextUtils.equals(stock.getClasses(), Stock.CLASS_A)) {
					long interval = System.currentTimeMillis() - Setting.getDownloadStockTimeMillis(stock);
					if ((Market.isTradingHours() && interval > Constant.HOUR_IN_MILLIS) || interval > Config.downloadStockInterval) {
						if (downloadStockInformation(stock) == RESULT_FAILED) {
							StockService.getInstance().onDisconnected();
							return;
						}

						if (downloadStockFinancial(stock) == RESULT_FAILED) {
							StockService.getInstance().onDisconnected();
							return;
						}

						if (downloadStockBonus(stock) == RESULT_FAILED) {
							StockService.getInstance().onDisconnected();
							return;
						}

						if (downloadStockShare(stock) == RESULT_FAILED) {
							StockService.getInstance().onDisconnected();
							return;
						}

						if (downloadStockRZRQ(stock) == RESULT_FAILED) {
							StockService.getInstance().onDisconnected();
							return;
						}

						Setting.setDownloadStockTimeMillis(stock, System.currentTimeMillis());
					}

					interval = System.currentTimeMillis() - Setting.getDownloadStockDataTimeMillis(stock);
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

						if (Market.isTradingHours() || Market.isLunchTime()) {
							Setting.setDownloadStockDataTimeMillis(stock, 0);
						} else {
							Setting.setDownloadStockDataTimeMillis(stock, System.currentTimeMillis());
						}
					}
				}
				onDownloadComplete(stock.getCode());

				if (Setting.getStockDataChanged(stock)) {
					Setting.setStockDataChanged(stock, false);

					onAnalyzeStart(stock.getCode());
					mStockAnalyzer.analyze(stock);
					onAnalyzeFinish(stock.getCode());
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				releaseWakeLock();
			}
		}
	}
}