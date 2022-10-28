package com.android.orion;

import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.app.NotificationManager;
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

abstract class StockDataProvider extends StockAnalyzer {
    static final String TAG = Constants.TAG + " "
            + StockDataProvider.class.getSimpleName();

    private static int DOWNLOAD_RESULT_SUCCESS = 1;
    private static int DOWNLOAD_RESULT_NONE = 0;
    private static int DOWNLOAD_RESULT_FAILED = -1;

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

    abstract void handleResponseIPO(String response);

    private HandlerThread mHandlerThread;
    private ServiceHandler mHandler;

    private OkHttpClient mOkHttpClient = new OkHttpClient();
    private ArrayList<String> mAccessDeniedStringArray = new ArrayList<>();

    StockDataProvider(Context context) {
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

    private void sendBroadcast(String action, long stockID) {
        Intent intent = new Intent(action);
        intent.putExtra(Constants.EXTRA_STOCK_ID, stockID);

        mLocalBroadcastManager.sendBroadcast(intent);
    }

    private void loadStockArrayMap(ArrayMap<String, Stock> stockArrayMap) {
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

                    stockArrayMap.put(stock.getCode(), stock);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mStockDatabaseManager.closeCursor(cursor);
        }
    }

    void download(String se, String code) {
        Stock stock = null;

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

    int getDownloadHistoryLengthDefault(String period) {
        int result = 0;
        int availableHistoryLength = 0;

        availableHistoryLength = getAvailableHistoryLength(period);

        if (availableHistoryLength > 0) {
            result = availableHistoryLength;
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

    private int getStockDataOfToday(Cursor cursor, StockData stockData) {
        String dataString = Utility.getCalendarDateString(Calendar
                .getInstance());
        int result = 0;

        if (cursor == null) {
            return result;
        }

        cursor.moveToLast();

        stockData.set(cursor);
        if (stockData.getDate().equals(dataString)) {
            result++;
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
                mStockDatabaseManager.deleteStockData(stockId,  period);
                return defaultValue;
            }

//            if (!Market.isWeekday(Calendar.getInstance())) {
//                return result;
//            }

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
                        case Settings.KEY_PERIOD_MIN60:
                            result += scheduleMinutes
                                    / Constants.SCHEDULE_INTERVAL_MIN60;
                            break;
                        case Settings.KEY_PERIOD_MIN30:
                            result += scheduleMinutes
                                    / Constants.SCHEDULE_INTERVAL_MIN30;
                            break;
                        case Settings.KEY_PERIOD_MIN15:
                            result += scheduleMinutes
                                    / Constants.SCHEDULE_INTERVAL_MIN15;
                            break;
                        case Settings.KEY_PERIOD_MIN5:
                            result += scheduleMinutes
                                    / Constants.SCHEDULE_INTERVAL_MIN5;
                            break;
                    }
                }
            } else if (Market.isLunchTime(Calendar.getInstance())) {
                if (modifiedCalendar.after(stockMarketLunchBeginCalendar)) {
                    return result;
                }

                switch (period) {
                    case Settings.KEY_PERIOD_MONTH:
                    case Settings.KEY_PERIOD_WEEK:
                    case Settings.KEY_PERIOD_DAY:
                        result = 1;
                        break;
                    case Settings.KEY_PERIOD_MIN60:
                        result = 2;
                        break;
                    case Settings.KEY_PERIOD_MIN30:
                        result = 4;
                        break;
                    case Settings.KEY_PERIOD_MIN15:
                        result = 8;
                        break;
                    case Settings.KEY_PERIOD_MIN5:
                        result = 24;
                        break;
                }
            } else if (Market.afterClosed(Calendar.getInstance())) {
                if (modifiedCalendar.after(stockMarketCloseCalendar)) {
                    return result;
                }

                switch (period) {
                    case Settings.KEY_PERIOD_MONTH:
                    case Settings.KEY_PERIOD_WEEK:
                    case Settings.KEY_PERIOD_DAY:
                        result = 1;
                        break;
                    case Settings.KEY_PERIOD_MIN60:
                        result = 4;
                        break;
                    case Settings.KEY_PERIOD_MIN30:
                        result = 8;
                        break;
                    case Settings.KEY_PERIOD_MIN15:
                        result = 16;
                        break;
                    case Settings.KEY_PERIOD_MIN5:
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
                Thread.sleep(Constants.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
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
                Thread.sleep(Constants.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
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
                Thread.sleep(Constants.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
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
                Thread.sleep(Constants.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
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
        int result =DOWNLOAD_RESULT_NONE;

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
                Thread.sleep(Constants.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
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
                Thread.sleep(Constants.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
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

        String lastPeriod = "";
        for (String period : Settings.KEY_PERIODS) {
            if (Preferences.getBoolean(mContext, period, false)
                    || Settings.checkOperatePeriod(lastPeriod, period, stock.getOperate())) {
                lastPeriod = period;
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
                Thread.sleep(Constants.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
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

        for (String period : Settings.KEY_PERIODS) {
            if (Preferences.getBoolean(mContext, period, false)) {
                if (Settings.KEY_PERIOD_MONTH.equals(period)
                        || Settings.KEY_PERIOD_WEEK.equals(period)
                        || Settings.KEY_PERIOD_DAY.equals(period)
                ) {
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
                Thread.sleep(Constants.DEFAULT_DOWNLOAD_SLEEP_INTERVAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void setupIndex(Stock index) {
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
                        if (stockArrayMap.containsKey(indexComponent.getCode())) {
                            stock = stockArrayMap.get(indexComponent.getCode());
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
                        if (stockArrayMap.containsKey(indexComponent.getCode())) {
                            stock = stockArrayMap.get(indexComponent.getCode());
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
                    if (baseStock != null) {
                        stockDataList = baseStock.getStockDataList(period);
                    }
                    if (stockDataList != null) {
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
                    }

                    for (IndexComponent indexComponent : indexComponentList) {
                        if (stockArrayMap.containsKey(indexComponent.getCode())) {
                            stock = stockArrayMap.get(indexComponent.getCode());
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
//                                        continue;
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
                if (stockArrayMap.containsKey(indexComponent.getCode())) {
                    stock = stockArrayMap.get(indexComponent.getCode());
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

                if (dataChanged) {
                    String lastPeriod = "";
                    for (String period : Settings.KEY_PERIODS) {
                        if (Preferences.getBoolean(mContext, period, false)
                                || Settings.checkOperatePeriod(lastPeriod, period, stock.getOperate())) {
                            lastPeriod = period;
                            analyze(stock, period);
                        }
                    }

                    analyze(stock);
                    sendBroadcast(Constants.ACTION_RESTART_LOADER, stock.getId());
                }

//                if (downloadIPO() == DOWNLOAD_RESULT_FAILED) {
//                    return;
//                }
//                sendBroadcast(Constants.ACTION_RESTART_LOADER,
//                        Stock.INVALID_ID);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                releaseWakeLock();
            }
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