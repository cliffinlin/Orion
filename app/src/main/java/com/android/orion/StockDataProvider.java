package com.android.orion;

import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.android.orion.database.IndexComponent;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.StockFinancial;
import com.android.orion.database.IPO;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.TotalShare;
import com.android.orion.utility.Market;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.Utility;

import static java.lang.Thread.State.RUNNABLE;
import static java.lang.Thread.State.TERMINATED;

public abstract class StockDataProvider extends StockAnalyzer {
    static final String TAG = Constants.TAG + " "
            + StockDataProvider.class.getSimpleName();

    long mLastSendBroadcast = 0;

    abstract int getAvailableHistoryLength(String period);

    abstract ArrayMap<String, String> getRequestHeader();

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

    abstract String getStockFinancialURLString(Stock stock);

    abstract void handleResponseStockFinancial(Stock stock,
                                              StockFinancial stockFinancial, String response);

    abstract String getShareBonusURLString(Stock stock);

    abstract void handleResponseShareBonus(Stock stock, ShareBonus shareBonus,
                                           String response);

    abstract String getTotalShareURLString(Stock stock);

    abstract void handleResponseTotalShare(Stock stock, TotalShare totalShare,
                                           String response);

    abstract String getIPOURLString();

    abstract void handleResponseIPO(IPO ipo, String response);

    private HandlerThread mHandlerThread;
    private ServiceHandler mHandler;

    OkHttpClient mOkHttpClient = new OkHttpClient();

    IPO mIPO = null;
    Stock mStock = null;
    StockData mStockData = null;
    StockFinancial mStockFinancial = null;
    ShareBonus mShareBonus = null;
    TotalShare mTotalShare = null;

    ArrayList<String> mAccessDeniedStringArray = new ArrayList<>();

    public static int DOWNLOAD_RESULT_NONE = 0;
    public static int DOWNLOAD_RESULT_FAILED = -1;

    public StockDataProvider(Context context) {
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

    void sendBroadcast(String action, long stockID) {
        mLastSendBroadcast = System.currentTimeMillis();

        Intent intent = new Intent(action);
        intent.putExtra(Constants.EXTRA_STOCK_ID, stockID);

        mLocalBroadcastManager.sendBroadcast(intent);
    }

    void loadStockArrayMap(ArrayMap<String, Stock> stockArrayMap) {
        String selection = "";
        Cursor cursor = null;

        if ((mStockDatabaseManager == null) || (stockArrayMap == null)) {
            return;
        }

        selection += DatabaseContract.COLUMN_FLAG + " = "
                + Stock.FLAG_FAVORITE;

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

    void downloadStock(Intent intent) {
        String se = "";
        String code = "";
        Stock stock = null;

        se = intent.getStringExtra(Constants.EXTRA_STOCK_SE);
        code = intent.getStringExtra(Constants.EXTRA_STOCK_CODE);

        if (!TextUtils.isEmpty(se) && !TextUtils.isEmpty(code)) {
            stock = new Stock();
            stock.setSE(se);
            stock.setCode(code);

            mStockDatabaseManager.getStock(stock);
        }

        download(stock);
    }

    void download(Stock stock) {
        if (!Utility.isNetworkConnected(mContext)) {
            return;
        }

        Thread.State state = mHandlerThread.getState();
        if (state != RUNNABLE) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.handlerthread_not_running), Toast.LENGTH_LONG).show();
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

           if (mHandler.hasMessages(Integer.valueOf(stock.getCode()))) {
               Log.d(TAG, "mHandler.hasMessages " + Integer.valueOf(stock.getCode()) + ", skip!");
           } else {
               Message msg = mHandler.obtainMessage(Integer.valueOf(stock.getCode()), stock);
               mHandler.sendMessage(msg);
               Log.d(TAG, "mHandler.sendMessage" + msg);
           }
        }
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

    int findStockDataToday(Cursor cursor) {
        String dataString = Utility.getCalendarDateString(Calendar
                .getInstance());
        int result = 0;

        StockData stockData = new StockData();

        if (cursor == null) {
            return result;
        }

        cursor.moveToLast();

        while (cursor.moveToPrevious()) {
            stockData.set(cursor);
            if (stockData.getDate().equals(dataString)) {
                result++;
            } else {
                break;
            }
        }

        return result;
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
            if (TextUtils.isEmpty(modified)) {
                modified = stockData.getCreated();
            }

            if (Market.isOutOfDateToday(modified)) {
                removeStockDataRedundant(cursor, defaultValue);
                return defaultValue;
            }

            if (!Market.isWeekday(Calendar.getInstance())) {
                return result;
            }

            if (Market.isTradingHours(Calendar.getInstance())) {
                scheduleMinutes = Market.getScheduleMinutes();
                if (scheduleMinutes != 0) {
                    result = 1;

                    if (period.equals(Settings.KEY_PERIOD_MIN60)) {
                        result += scheduleMinutes
                                / Constants.SCHEDULE_INTERVAL_MIN60;
                    } else if (period.equals(Settings.KEY_PERIOD_MIN30)) {
                        result += scheduleMinutes
                                / Constants.SCHEDULE_INTERVAL_MIN30;
                    } else if (period.equals(Settings.KEY_PERIOD_MIN15)) {
                        result += scheduleMinutes
                                / Constants.SCHEDULE_INTERVAL_MIN15;
                    } else if (period.equals(Settings.KEY_PERIOD_MIN5)) {
                        result += scheduleMinutes
                                / Constants.SCHEDULE_INTERVAL_MIN5;
                    }
                }
            } else {
                int count = findStockDataToday(cursor);
                Calendar modifiedCalendar = Utility.getCalendar(modified,
                        Utility.CALENDAR_DATE_TIME_FORMAT);
                Calendar stockMarketLunchBeginCalendar = Market
                        .getStockMarketLunchBeginCalendar(Calendar
                                .getInstance());
                Calendar stockMarketCloseCalendar = Market
                        .getStockMarketCloseCalendar(Calendar.getInstance());

                if (Market.inHalfTime(Calendar.getInstance())) {
                    if (modifiedCalendar.after(stockMarketLunchBeginCalendar)) {
                        return result;
                    }

                    if (period.equals(Settings.KEY_PERIOD_MONTH)
                            || period.equals(Settings.KEY_PERIOD_WEEK)
                            || period.equals(Settings.KEY_PERIOD_DAY)) {
                        result = 1 - count;
                    } else if (period.equals(Settings.KEY_PERIOD_MIN60)) {
                        result = 2 - count;
                    } else if (period.equals(Settings.KEY_PERIOD_MIN30)) {
                        result = 4 - count;
                    } else if (period.equals(Settings.KEY_PERIOD_MIN15)) {
                        result = 8 - count;
                    } else if (period.equals(Settings.KEY_PERIOD_MIN5)) {
                        result = 24 - count;
                    }
                } else if (Market.afterStockMarketClose(Calendar.getInstance())) {
                    if (modifiedCalendar.after(stockMarketCloseCalendar)) {
                        return result;
                    }

                    if (period.equals(Settings.KEY_PERIOD_MONTH)
                            || period.equals(Settings.KEY_PERIOD_WEEK)
                            || period.equals(Settings.KEY_PERIOD_DAY)) {
                        result = 1 - count;
                    } else if (period.equals(Settings.KEY_PERIOD_MIN60)) {
                        result = 4 - count;
                    } else if (period.equals(Settings.KEY_PERIOD_MIN30)) {
                        result = 8 - count;
                    } else if (period.equals(Settings.KEY_PERIOD_MIN15)) {
                        result = 16 - count;
                    } else if (period.equals(Settings.KEY_PERIOD_MIN5)) {
                        result = 48 - count;
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

    int downloadStockRealTime(Stock stock) {
        int result = DOWNLOAD_RESULT_NONE;

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

        return downloadStockRealTime(getRequestHeader(), getStockRealTimeURLString(stock));
    }

    int downloadStockRealTime(ArrayMap<String, String> requestHeaderArray, String urlString) {
        String resultString = "";
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
            if (response != null) {
                resultString = response.body().string();
                if (isAccessDenied(resultString)) {
                    result =DOWNLOAD_RESULT_FAILED;
                    return result;
                }

                handleResponseStockRealTime(mStock, resultString);
                Thread.sleep(Constants.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    int downloadStockInformation(Stock stock) {
        int result = DOWNLOAD_RESULT_NONE;

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
        }

        if (!needDownload) {
            return result;
        }

        setStock(stock);

        return downloadStockInformation(getRequestHeader(), getStockInformationURLString(stock));
    }

    int downloadStockInformation(ArrayMap<String, String> requestHeaderArray, String urlString) {
        String resultString = "";
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
            if (response != null) {
                resultString = response.body().string();
                if (isAccessDenied(resultString)) {
                    result = DOWNLOAD_RESULT_FAILED;
                    return result;
                }

                handleResponseStockInformation(mStock, resultString);
                Thread.sleep(Constants.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    int downloadStockFinancial(Stock stock) {
        int result = DOWNLOAD_RESULT_NONE;

        if (stock == null) {
            return result;
        }

        mStockDatabaseManager.getStock(stock);

        mStockFinancial = new StockFinancial();
        mStockFinancial.setStockId(stock.getId());

        mStockDatabaseManager.getStockFinancial(stock, mStockFinancial);

        if (mStockFinancial.getCreated().contains(
                Utility.getCurrentDateString())
                || mStockFinancial.getModified().contains(
                Utility.getCurrentDateString())) {
            if ((mStockFinancial.getBookValuePerShare() != 0)
                    && (mStockFinancial.getNetProfit() != 0)) {
                return result;
            }
        }

        setStock(stock);

        return downloadStockFinancial(getStockFinancialURLString(stock));
    }

    int downloadStockFinancial(String urlString) {
        String resultString = "";
        int result = DOWNLOAD_RESULT_NONE;

        Log.d(TAG, "downloadStockFinancial:" + urlString);

        Request.Builder builder = new Request.Builder();
        builder.url(urlString);
        Request request = builder.build();

        try {
            Response response = mOkHttpClient.newCall(request).execute();
            if (response != null) {
                resultString = response.body().string();
                if (isAccessDenied(resultString)) {
                    result = DOWNLOAD_RESULT_FAILED;
                    return result;
                }

                handleResponseStockFinancial(mStock, mStockFinancial, resultString);
                Thread.sleep(Constants.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    int downloadIPO() {
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

    int downloadIPO(String urlString) {
        String resultString = "";
        int result = DOWNLOAD_RESULT_NONE;

        Log.d(TAG, "downloadIPO:" + urlString);

        Request.Builder builder = new Request.Builder();
        builder.url(urlString);
        Request request = builder.build();

        try {
            Response response = mOkHttpClient.newCall(request).execute();
            if (response != null) {
                resultString = response.body().string();
                if (isAccessDenied(resultString)) {
                    result = DOWNLOAD_RESULT_FAILED;
                    return result;
                }

                handleResponseIPO(mIPO, resultString);
                Thread.sleep(Constants.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    int downloadShareBonus(Stock stock) {
        int result = DOWNLOAD_RESULT_NONE;

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

    int downloadShareBonus(String urlString) {
        String resultString = "";
        int result =DOWNLOAD_RESULT_NONE;

        Log.d(TAG, "downloadShareBonus:" + urlString);

        Request.Builder builder = new Request.Builder();
        builder.url(urlString);
        Request request = builder.build();

        try {
            Response response = mOkHttpClient.newCall(request).execute();
            if (response != null) {
                resultString = response.body().string();
                if (isAccessDenied(resultString)) {
                    result = DOWNLOAD_RESULT_FAILED;
                    return result;
                }

                handleResponseShareBonus(mStock, mShareBonus, resultString);
                Thread.sleep(Constants.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    int downloadTotalShare(Stock stock) {
        int result = DOWNLOAD_RESULT_NONE;

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

    int downloadTotalShare(String urlString) {
        String resultString = "";
        int result = DOWNLOAD_RESULT_NONE;

        Log.d(TAG, "downloadTotalShare:" + urlString);

        Request.Builder builder = new Request.Builder();
        builder.url(urlString);
        Request request = builder.build();

        try {
            Response response = mOkHttpClient.newCall(request).execute();
            if (response != null) {
                resultString = response.body().string();
                if (isAccessDenied(resultString)) {
                    result =DOWNLOAD_RESULT_FAILED;
                    return result;
                }

                handleResponseTotalShare(mStock, mTotalShare, resultString);
                Thread.sleep(Constants.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    int downloadStockDataHistory(Stock stock) {
        int result = DOWNLOAD_RESULT_NONE;

        if (stock == null) {
            return result;
        }

        mStockDatabaseManager.getStock(stock);

        for (String period : Settings.KEY_PERIODS) {
            if (Preferences.getBoolean(mContext, period, false)) {
                result = downloadStockDataHistory(stock, period);
            }
        }

        return result;
    }

    int downloadStockDataHistory(Stock stock, String period) {
        String resultSting = "";
        int result = DOWNLOAD_RESULT_NONE;
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

    int downloadStockDataHistory(String urlString) {
        String resultString = "";
        int result = DOWNLOAD_RESULT_NONE;

        Log.d(TAG, "downloadStockDataHistory:" + urlString);

        Request.Builder builder = new Request.Builder();
        builder.url(urlString);
        Request request = builder.build();

        try {
            Response response = mOkHttpClient.newCall(request).execute();
            if (response != null) {
                resultString = response.body().string();
                if (isAccessDenied(resultString)) {
                    result = DOWNLOAD_RESULT_FAILED;
                    return result;
                }

                handleResponseStockDataHistory(mStock, mStockData, resultString);
                Thread.sleep(Constants.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    void setupIndex(Stock index) {
        ArrayMap<String, Stock> stockArrayMap = new ArrayMap<String, Stock>();
        ArrayList<IndexComponent> indexComponentList = new ArrayList<>();
        ArrayList<StockData> indexStockDataList = null;
        ArrayList<StockData> stockDataList = null;
        Stock stock = null;
        Stock baseStock = null;
        String selection = "";
        double totalPrice = 0;
        double totalNet = 0;

        if (index == null) {
            return;
        }

        loadStockArrayMap(stockArrayMap);

        if ((stockArrayMap == null) || (stockArrayMap.size() == 0)) {
            return;
        }

        try {
            selection = DatabaseContract.COLUMN_INDEX_CODE + " = " + index.getCode();
            mStockDatabaseManager.getIndexComponentList(indexComponentList, selection, null);
            if (indexComponentList.size() == 0) {
                return;
            }

            for (String period : Settings.KEY_PERIODS) {
                if (Preferences.getBoolean(mContext, period, false)) {
                    int maxSize = 0;
                    Calendar begin = null;
                    indexStockDataList = index.getStockDataList(period);

                    for (IndexComponent indexComponent : indexComponentList) {
                        if (stockArrayMap.containsKey(indexComponent.getSE() + indexComponent.getCode())) {
                            stock = stockArrayMap.get(indexComponent.getSE() + indexComponent.getCode());
                            stockDataList = stock.getStockDataList(period);
                            loadStockDataList(stock, period, stockDataList);
                            if ((stockDataList == null) || (stockDataList.size() == 0)) {
                                continue;
                            }

                            if (stockDataList.size() > maxSize) {
                                maxSize = stockDataList.size();
                            }
                        }
                    }

                    for (IndexComponent indexComponent : indexComponentList) {
                        if (stockArrayMap.containsKey(indexComponent.getSE() + indexComponent.getCode())) {
                            stock = stockArrayMap.get(indexComponent.getSE() + indexComponent.getCode());
                            stockDataList = stock.getStockDataList(period);
                            if ((stockDataList == null) || (stockDataList.size() == 0)) {
                                continue;
                            }

                            if (stockDataList.size() == maxSize) {
                                if (begin == null) {
                                    begin = stockDataList.get(0).getCalendar();
                                    baseStock = stock;
                                } else {
                                    if (stockDataList.get(0).getCalendar().after(begin)) {
                                        begin = stockDataList.get(0).getCalendar();
                                        baseStock = stock;
                                    }
                                }
                            }
                        }
                    }

                    indexStockDataList.clear();
                    stockDataList = baseStock.getStockDataList(period);
                    for (StockData stockData : stockDataList) {
                        StockData indexStockData = new StockData(period);

                        indexStockData.setStockId(index.getId());

                        indexStockData.setDate(stockData.getDate());
                        indexStockData.setTime(stockData.getTime());
                        indexStockData.setOpen(stockData.getOpen() / indexComponentList.size());
                        indexStockData.setClose(stockData.getClose() / indexComponentList.size());
                        indexStockData.setHigh(stockData.getHigh() / indexComponentList.size());
                        indexStockData.setLow(stockData.getLow() / indexComponentList.size());

                        indexStockData.setVertexHigh(indexStockData.getHigh());
                        indexStockData.setVertexLow(indexStockData.getLow());

                        indexStockDataList.add(indexStockData);
                    }

                    for (IndexComponent indexComponent : indexComponentList) {
                        if (stockArrayMap.containsKey(indexComponent.getSE() + indexComponent.getCode())) {
                            stock = stockArrayMap.get(indexComponent.getSE() + indexComponent.getCode());
                            stockDataList = stock.getStockDataList(period);
                            if ((stockDataList == null) || (stockDataList.size() == 0)) {
                                continue;
                            }

                            if (stock == baseStock) {
                                continue;
                            }

                            StockData stockDataLastMatched = null;
                            int lastMatched = 0;
                            for (int i = 0; i < indexStockDataList.size(); i++) {
                                StockData indexStockData = indexStockDataList.get(i);
                                Calendar indexStockDataCalendar = indexStockDataList.get(i).getCalendar();
                                for (int j = lastMatched; j < stockDataList.size(); j++) {
                                    StockData stockData = stockDataList.get(j);
                                    Calendar stockDataCalendar = stockDataList.get(j).getCalendar();
                                    if (stockDataCalendar.before(indexStockDataCalendar)) {
                                        continue;
                                    } else if (stockDataCalendar.equals(indexStockDataCalendar)) {
                                        lastMatched = j;
                                        stockDataLastMatched = stockData;
                                        indexStockData.setOpen(indexStockData.getOpen() + stockData.getOpen() / indexComponentList.size());
                                        indexStockData.setClose(indexStockData.getClose() + stockData.getClose() / indexComponentList.size());
                                        indexStockData.setHigh(indexStockData.getHigh() + stockData.getHigh() / indexComponentList.size());
                                        indexStockData.setLow(indexStockData.getLow() + stockData.getLow() / indexComponentList.size());

                                        indexStockData.setVertexHigh(indexStockData.getHigh());
                                        indexStockData.setVertexLow(indexStockData.getLow());
                                        break;
                                    } else if (stockDataCalendar.after(indexStockDataCalendar)) {
                                        if (stockDataLastMatched != null) {
                                            indexStockData.setOpen(indexStockData.getOpen() + stockDataLastMatched.getOpen() / indexComponentList.size());
                                            indexStockData.setClose(indexStockData.getClose() + stockDataLastMatched.getClose() / indexComponentList.size());
                                            indexStockData.setHigh(indexStockData.getHigh() + stockDataLastMatched.getHigh() / indexComponentList.size());
                                            indexStockData.setLow(indexStockData.getLow() + stockDataLastMatched.getLow() / indexComponentList.size());

                                            indexStockData.setVertexHigh(indexStockData.getHigh());
                                            indexStockData.setVertexLow(indexStockData.getLow());
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    updateDatabase(index, period, indexStockDataList);
                }
            }

            for (IndexComponent indexComponent : indexComponentList) {
                if (stockArrayMap.containsKey(indexComponent.getSE() + indexComponent.getCode())) {
                    stock = stockArrayMap.get(indexComponent.getSE() + indexComponent.getCode());
                    if ((stockDataList == null) || (stockDataList.size() == 0)) {
                        continue;
                    }

                    totalPrice += stock.getPrice();
                    totalNet += stock.getNet();
                }
            }

            index.setPrice(Utility.Round(totalPrice / indexComponentList.size(), Constants.DOUBLE_FIXED_DECIMAL));
            index.setNet(Utility.Round(totalNet / indexComponentList.size(), Constants.DOUBLE_FIXED_DECIMAL));

            index.setModified(Utility.getCurrentDateTimeString());
            mStockDatabaseManager.updateStock(index,
                    index.getContentValues());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Stock stock;
            int result = DOWNLOAD_RESULT_NONE;

            switch (msg.what) {
                default:
                    try {
                        acquireWakeLock();

                        stock = (Stock) msg.obj;

                        if (stock == null) {
                            return;
                        }

                        if (TextUtils.isEmpty(stock.getSE()) || TextUtils.isEmpty(stock.getCode())) {
                            return;
                        }

                        if (Stock.CLASS_INDEX.equals(stock.getClases())) {
                            setupIndex(stock);
                        } else {
                            if (downloadStockRealTime(stock) == DOWNLOAD_RESULT_FAILED) {
                                return;
                            }

                            if (downloadStockInformation(stock) == DOWNLOAD_RESULT_FAILED) {
                                return;
                            }

                            if (downloadStockFinancial(stock) == DOWNLOAD_RESULT_FAILED) {
                                return;
                            }

                            if (downloadShareBonus(stock) == DOWNLOAD_RESULT_FAILED) {
                                return;
                            }

                            if (downloadTotalShare(stock) == DOWNLOAD_RESULT_FAILED) {
                                return;
                            }

                            if (downloadStockDataHistory(stock) == DOWNLOAD_RESULT_FAILED) {
                                return;
                            }
                        }

                        for (String period : Settings.KEY_PERIODS) {
                            if (Preferences.getBoolean(mContext, period, false)) {
                                analyze(stock, period);
                            }
                        }

                        analyze(stock);
                        sendBroadcast(Constants.ACTION_RESTART_LOADER, stock.getId());

                        if (downloadIPO() == DOWNLOAD_RESULT_FAILED) {
                            return;
                        }
                        sendBroadcast(Constants.ACTION_RESTART_LOADER,
                                Stock.INVALID_ID);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        releaseWakeLock();
                    }
                    break;
            }
        }
    }

    boolean isAccessDenied(String string) {
        String accessDeniedString = "";
        StringBuilder contentTitle = new StringBuilder();
        boolean result = false;

        if (TextUtils.isEmpty(string)) {
            return result;
        }

        for (int i = 0; i < mAccessDeniedStringArray.size(); i++) {
            accessDeniedString = mAccessDeniedStringArray.get(i);

            if (string.contains(accessDeniedString)) {
                contentTitle.append(mContext.getResources().getString(R.string.action_download) + " ");
                contentTitle.append(accessDeniedString);

                notify(Constants.SERVICE_NOTIFICATION_ID, Constants.MESSAGE_CHANNEL_ID, Constants.MESSAGE_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH,
                        contentTitle.toString(), "");

                mHandlerThread.quit();

                result = true;
                break;
            }
        }

        return result;
    }
}