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
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.TDXData;
import com.android.orion.interfaces.AnalyzeListener;
import com.android.orion.interfaces.DownloadListener;
import com.android.orion.interfaces.IStockDataProvider;
import com.android.orion.interfaces.StockListener;
import com.android.orion.manager.StockDatabaseManager;
import com.android.orion.manager.StockManager;
import com.android.orion.service.StockService;
import com.android.orion.constant.Constant;
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
import java.util.Collections;

import okhttp3.OkHttpClient;

public class StockDataProvider implements StockListener, IStockDataProvider {

	public static final int MESSAGE_TYPE_DOWNLOAD = 100;
	public static final int MESSAGE_TYPE_ANALYZE = 200;
	public static final int RESULT_SUCCESS = 1;
	public static final int RESULT_NONE = 0;
	public static final int RESULT_FAILED = -1;
	public static final int SEND_MESSAGE_DELAY_DOWNLOAD = 500;

	protected static volatile IStockDataProvider mInstance;
	ArrayMap<String, Stock> mStockArrayMap = new ArrayMap<>();
	Context mContext = MainApplication.getContext();
	StockAnalyzer mStockAnalyzer = StockAnalyzer.getInstance();
	StockDatabaseManager mStockDatabaseManager = StockDatabaseManager.getInstance();
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
	public static ArrayList<String> getDatetimeMin60List() {
		ArrayList<String> datetimeList = new ArrayList<>();
		datetimeList.add("10:30:00");
		datetimeList.add("11:30:00");
		datetimeList.add("14:00:00");
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

	@Override
	public void onDestroy() {
		releaseWakeLock();
		for (Stock stock : mStockArrayMap.values()) {
			removeDownloadMessage(stock);
			mStockArrayMap.remove(stock.getCode());
		}
		if (mHandlerThread != null && mHandlerThread.isAlive()) {
			mHandlerThread.quitSafely();
			mHandlerThread = null;
		}
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
				n = size - Config.HISTORY_LENGTH_MIN5;
				break;
			case Period.MIN15:
				n = size - Config.HISTORY_LENGTH_MIN15;
				break;
			case Period.MIN30:
				n = size - Config.HISTORY_LENGTH_MIN30;
				break;
			case Period.MIN60:
				n = size - Config.HISTORY_LENGTH_MIN60;
				break;
			default:
				break;
		}

		for (int i = 0; i < n; i++) {
			contentValuesList.remove(0);
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
	                                      @NonNull String key) {
		if (key.contains(Setting.SETTING_PERIOD_)) {
			if (!sharedPreferences.getBoolean(key, false)) {
				return;
			}

			mStockDatabaseManager.loadStockArrayMap(mStockArrayMap);
			for (Stock stock : mStockArrayMap.values()) {
				Setting.setDownloadStockDataTimeMillis(stock, 0);
			}
			download();
		}
	}

	void sendAnalyzeMessage(Stock stock) {
		if (stock == null) {
			return;
		}
		int messageID = stock.getCode().hashCode();
		if (mHandler.hasMessages(messageID)) {
			Log.d("return, mHandler.hasMessages " + stock.toLogString());
			return;
		}
		Message msg = mHandler.obtainMessage(messageID, stock);
		msg.arg1 = MESSAGE_TYPE_ANALYZE;
		mHandler.sendMessageAtFrontOfQueue(msg);
	}

	void removeDownloadMessage(Stock stock) {
		if (stock == null) {
			return;
		}
		int messageID = stock.getCode().hashCode();
		if (mHandler.hasMessages(messageID)) {
			Log.d(stock.toLogString());
			mHandler.removeMessages(messageID);
		}
	}

	void sendDownloadMessage(Stock stock, boolean delayed) {
		if (stock == null) {
			return;
		}
		int messageID = stock.getCode().hashCode();
		if (mHandler.hasMessages(messageID)) {
			Log.d("return, mHandler.hasMessages " + stock.toLogString());
			return;
		}
		Message msg = mHandler.obtainMessage(messageID, stock);
		msg.arg1 = MESSAGE_TYPE_DOWNLOAD;
		if (delayed) {
			mHandler.sendMessageDelayed(msg, SEND_MESSAGE_DELAY_DOWNLOAD);
		} else {
			mHandler.sendMessageAtFrontOfQueue(msg);
		}
	}

	@Override
	public void analyze(Stock stock) {
		if (stock == null) {
			return;
		}
		sendAnalyzeMessage(stock);
	}

	@Override
	public void download() {
		if (!Utility.isNetworkConnected(mContext)) {
			Log.d("return, No network connection");
			return;
		}

		mStockDatabaseManager.loadStockArrayMap(mStockArrayMap);
		if (mStockArrayMap.isEmpty()) {
			Log.d("return, Stock array map is empty");
			return;
		}

		int index = 0;
		for (Stock stock : mStockArrayMap.values()) {
			if (stock != null) {
				Log.d("index=" + index++ + " " + stock.toLogString());
				sendDownloadMessage(stock, true);
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

		removeDownloadMessage(stock);
		sendDownloadMessage(stock, false);
	}

	void mergeStockDataDay(StockData stockData) {
		if (stockData == null) {
			return;
		}

		if (stockData.getPeriod().equals(Period.MONTH) || stockData.getPeriod().equals(Period.WEEK)) {
			StockData stockDataDay = new StockData(stockData);
			stockDataDay.setPeriod(Period.DAY);
			mStockDatabaseManager.getStockData(stockDataDay);
			if (TextUtils.equals(stockDataDay.getMonth(), stockData.getMonth()) || TextUtils.equals(stockDataDay.getWeek(), stockData.getWeek())) {
				if (stockDataDay.getCalendar().after(stockData.getCalendar())) {
					stockData.setDate(stockDataDay.getDate());
					stockData.getCandle().merge(stockDataDay.getCandle());
				}
			}
		}
	}

	StockData mergeStockDataMin(ArrayList<StockData> stockDataList, int size) {
		if (stockDataList == null || stockDataList.isEmpty() || size <= 0) {
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

	void loadTDXDataFile(Uri uri, ArrayList<String> contentList) {
		if (contentList == null) {
			return;
		}
		InputStream inputStream = null;
		String fileName = "";
		try {
			if (uri.getScheme().equals("file")) {
				fileName = uri.getPath();
				Utility.readFile(fileName, contentList);
			} else if (uri.getScheme().equals("content")) {
				inputStream = mContext.getContentResolver().openInputStream(uri);
				if (inputStream != null) {
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
					String content;
					while ((content = bufferedReader.readLine()) != null) {
						contentList.add(content);
					}
				}
			}
			if (contentList.size() > Config.HISTORY_LENGTH_MIN5) {
				int n = contentList.size() - Config.HISTORY_LENGTH_MIN5;
				for (int i = 0; i < n; i++) {
					contentList.remove(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Utility.closeQuietly(inputStream);
		}
	}

	void setupStockData(Stock stock, Uri uri) {
		if (stock == null) {
			return;
		}

		ArrayList<String> contentList = new ArrayList<>();
		loadTDXDataFile(uri, contentList);
		if (contentList.isEmpty()) {
			return;
		}

		try {
//			ArrayList<String> datetimeMin5List = new ArrayList<>();//based on min5
			ArrayList<String> datetimeMin15List = getDatetimeMin15List();
			ArrayList<String> datetimeMin30List = getDatetimeMinL30ist();
			ArrayList<String> datetimeMin60List = getDatetimeMin60List();

			ArrayList<StockData> stockDataMin5List = new ArrayList<>();
			ArrayList<StockData> stockDataMin15List = new ArrayList<>();
			ArrayList<StockData> stockDataMin30List = new ArrayList<>();
			ArrayList<StockData> stockDataMin60List = new ArrayList<>();
			for (int i = 0; i < contentList.size(); i++) {
				String content = contentList.get(i);
				if (TextUtils.isEmpty(content)) {
					continue;
				}

				StockData stockDataMin5 = new StockData();
				if (stockDataMin5.fromTDXContent(content) == null) {
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
			saveTDXDatabase(stock, Period.MIN5, stockDataMin5List);
			saveTDXDatabase(stock, Period.MIN15, stockDataMin15List);
			saveTDXDatabase(stock, Period.MIN30, stockDataMin30List);
			saveTDXDatabase(stock, Period.MIN60, stockDataMin60List);

			mStockDatabaseManager.deleteStockData(stock);
			mStockDatabaseManager.deleteStockTrend(stock);
			mStockDatabaseManager.deleteStockPerceptron(stock.getId());
			Setting.setDownloadStockTimeMillis(stock, 0);
			Setting.setDownloadStockDataTimeMillis(stock, 0);
			download(stock);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void importTDXDataFile(ArrayList<Uri> uriList) {
		if (uriList == null || uriList.isEmpty()) {
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
				if (!mStockDatabaseManager.isStockExist(stock)) {
					stock.setCreated(Utility.getCurrentDateTimeString());
					mStockDatabaseManager.insertStock(stock);
				} else {
					mStockDatabaseManager.getStock(stock);
				}
				setupStockData(stock, uri);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void loadTDXDatabase(Stock stock, StockData stockData,
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
			mStockDatabaseManager.getTDXDataContentList(stock, stockData.getPeriod(), contentList);
			for (int i = 0; i < contentList.size(); i++) {
				String content = contentList.get(i);
				if (TextUtils.isEmpty(content)) {
					continue;
				}
				if (stockData.fromTDXContent(content) != null) {
					contentValuesList.add(stockData.getContentValues());
					stockDataMap.put(stockData.getDateTime(), new StockData(stockData));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void saveTDXDatabase(Stock stock, String period, ArrayMap<String, StockData> stockDataMap) {
		if (stock == null || stockDataMap == null || stockDataMap.isEmpty()) {
			Log.d("saveTDXDatabase return");
			return;
		}

		if (!Period.isMinutePeriod(period)) {
			Log.d("saveTDXDatabase return, period=" + period);
			return;
		}

		ArrayList<StockData> stockDataList = new ArrayList<>(stockDataMap.values());
		Collections.sort(stockDataList, StockData.comparator);
		saveTDXDatabase(stock, period, stockDataList);
		if (period.equals(Period.MIN5)) {
			ArrayList<String> contentList = new ArrayList<>();
			if (stockDataList.size() < Config.HISTORY_LENGTH_MIN5) {
				String uriString = Setting.getTdxDataFileUri(stock);
				if (TextUtils.isEmpty(uriString)) {
					Log.d("saveTDXDatabase return, uriString=" + uriString);
					return;
				}
				Uri uri = Uri.parse(uriString);
				loadTDXDataFile(uri, contentList);
				if (contentList.isEmpty()) {
					Log.d("saveTDXDatabase return, contentList=" + contentList);
					return;
				}
				for (String content : contentList) {
					StockData stockData = new StockData();
					if (stockData.fromTDXContent(content) != null) {
						stockDataMap.put(stockData.getDateTime(), stockData);
					}
				}
				stockDataList = new ArrayList<>(stockDataMap.values());
				Collections.sort(stockDataList, StockData.comparator);
				contentList.clear();
				for (StockData stockData : stockDataList) {
					String TDXContent = stockData.toTDXContent();
					if (!TextUtils.isEmpty(TDXContent)) {
						contentList.add(TDXContent);
					}
				}
			} else {
				mStockDatabaseManager.getTDXDataContentList(stock, period, contentList);
			}
			exportTDXDataFile(stock, period, contentList);
		}
	}

	void saveTDXDatabase(Stock stock, String period, ArrayList<StockData> stockDataList) {
		if (stock == null || stockDataList == null || stockDataList.isEmpty()) {
			return;
		}

		if (!Period.isMinutePeriod(period)) {
			return;
		}

		try {
			TDXData tdxData = new TDXData();
			mStockDatabaseManager.deleteTDXData(stock.getSE(), stock.getCode(), period);
			ContentValues[] contentValuesArray = new ContentValues[stockDataList.size()];
			for (int i = 0; i < stockDataList.size(); i++) {
				StockData stockData = stockDataList.get(i);
				if (stockData == null) {
					continue;
				}
				tdxData.init();
				tdxData.setSE(stock.getSE());
				tdxData.setCode(stock.getCode());
				tdxData.setName(stock.getName());
				tdxData.setPeriod(period);
				tdxData.setContent(stockData.toTDXContent());
				tdxData.setCreated(Utility.getCurrentDateTimeString());
				contentValuesArray[i] = tdxData.getContentValues();
			}
			mStockDatabaseManager.bulkInsertTDXData(contentValuesArray);
			Log.d("bulkInsertTDXData " + stock.toLogString() + " " + period);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void exportTDXDataFile(Stock stock, String period, ArrayList<String> contentList) {
		if (stock == null || contentList == null || contentList.size() < Config.HISTORY_LENGTH_MIN5) {
			Log.d("exportTDXDataFile return, contentList.size()=" + contentList.size());
			return;
		}
		OutputStream outputStream = null;
		BufferedWriter writer = null;
		try {
			String uriString = Setting.getTdxDataFileUri(stock);
			if (TextUtils.isEmpty(uriString)) {
				Log.d("exportTDXDataFile return, uriString=" + uriString);
				return;
			}
			Uri uri = Uri.parse(uriString);
			outputStream = mContext.getContentResolver().openOutputStream(uri, "wt");
			writer = new BufferedWriter(new OutputStreamWriter(outputStream));
			int index = 0;
			for (String content : contentList) {
				writer.write(content);
				index++;
			}
			Log.d("exportTDXDataFile " + stock.toLogString() + " " + period + " index=" + index);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Utility.closeQuietly(writer);
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
		removeDownloadMessage(stock);
		mStockArrayMap.remove(stock.getCode());
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
		removeDownloadMessage(stock);
		mStockArrayMap.remove(stock.getCode());
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
		removeDownloadMessage(stock);
		mStockArrayMap.remove(stock.getCode());
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

				if (msg.arg1 == MESSAGE_TYPE_ANALYZE) {
					handleAnalyzeMessage((Stock) msg.obj);
				} else if (msg.arg1 == MESSAGE_TYPE_DOWNLOAD) {
					handleDownloadMessage((Stock) msg.obj);

					if (Setting.getStockDataChanged(stock)) {
						Setting.setStockDataChanged(stock, false);
						handleAnalyzeMessage(stock);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				releaseWakeLock();
			}
		}
	}

	private void handleAnalyzeMessage(Stock stock) {
		if (stock == null) {
			return;
		}

		try {
			onAnalyzeStart(stock.getCode());
			mStockAnalyzer.analyze(stock);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			onAnalyzeFinish(stock.getCode());
		}
	}

	private void handleDownloadMessage(Stock stock) {
		if (stock == null) {
			return;
		}

		if (!Utility.isNetworkConnected(mContext)) {
			Log.d("return, isNetworkConnected=" + Utility.isNetworkConnected(mContext));
			return;
		}

		if (!TextUtils.equals(stock.getClasses(), Stock.CLASS_A)) {
			Log.d("return, stock.getClasses()!=" + Stock.CLASS_A);
			return;
		}

		try {
			onDownloadStart(stock.getCode());
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

				Setting.setDownloadStockTimeMillis(stock, System.currentTimeMillis());
			}

			interval = System.currentTimeMillis() - Setting.getDownloadStockDataTimeMillis(stock);
			if (Market.isTradingHours() || (interval > Config.downloadStockDataInterval)) {
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

				if (Market.isTradingHours()) {
					Setting.setDownloadStockDataTimeMillis(stock, 0);
				} else {
					Setting.setDownloadStockDataTimeMillis(stock, System.currentTimeMillis());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			onDownloadComplete(stock.getCode());
		}
	}
}