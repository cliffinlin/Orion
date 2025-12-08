package com.android.orion.analyzer;

import com.android.orion.data.Period;
import com.android.orion.database.StockData;
import com.android.orion.database.StockTrend;
import com.android.orion.constant.Constant;
import com.android.orion.utility.Logger;

import java.util.ArrayList;
import java.util.List;

public class MACDAnalyzer {

    private static String mPeriod;
    private static int mAverage5 = 0;
    private static int mAverage10 = 0;
    private static int mFast = 0;
    private static int mSlow = 0;
    private static int mSignal = 0;

    private static final ArrayList<Double> mPriceList = new ArrayList<>();
    private static final ArrayList<Double> mEMAAverage5List = new ArrayList<>();
    private static final ArrayList<Double> mEMAAverage10List = new ArrayList<>();
    private static final ArrayList<Double> mEMAFastList = new ArrayList<>();
    private static final ArrayList<Double> mEMASlowList = new ArrayList<>();
    private static final ArrayList<Double> mDEAList = new ArrayList<>();
    private static final ArrayList<Double> mDIFList = new ArrayList<>();
    private static final ArrayList<Double> mHistogramList = new ArrayList<>();

    static Logger Log = Logger.getLogger();

    private static final int AVERAGE5 = 5;
    private static final int AVERAGE10 = 10;

    private static final int FAST = 10; //12;
    private static final int SLOW = 20; //26;
    private static final int SIGNAL = 8; //9;

    public static ArrayList<Double> getEMAAverage5List() {
        return mEMAAverage5List;
    }

    public static ArrayList<Double> getEMAAverage10List() {
        return mEMAAverage10List;
    }

    public static ArrayList<Double> getDEAList() {
        return mDEAList;
    }

    public static ArrayList<Double> getDIFList() {
        return mDIFList;
    }

    public static ArrayList<Double> getHistogramList() {
        return mHistogramList;
    }

    public static void init(String period, ArrayList<StockData> stockDataList) {
        if (stockDataList == null || stockDataList.isEmpty()) {
            return;
        }
        mPeriod = period;
        switch (period) {
            case Period.MONTH:
            case Period.WEEK:
            case Period.DAY:
                mAverage5 = AVERAGE5;
                mAverage10 = AVERAGE10;
                mFast = FAST;
                mSlow = SLOW;
                mSignal = SIGNAL;
                break;
            case Period.MIN60:
                mAverage5 = Constant.MIN60_PER_TRADE_DAY * AVERAGE5;
                mAverage10 = Constant.MIN60_PER_TRADE_DAY * AVERAGE10;
                mFast = Constant.MIN60_PER_TRADE_DAY * FAST;
                mSlow = Constant.MIN60_PER_TRADE_DAY * SLOW;
                mSignal = Constant.MIN60_PER_TRADE_DAY * SIGNAL;
                break;
            case Period.MIN30:
                mAverage5 = Constant.MIN30_PER_TRADE_DAY * AVERAGE5;
                mAverage10 = Constant.MIN30_PER_TRADE_DAY * AVERAGE10;
                mFast = Constant.MIN30_PER_TRADE_DAY * FAST;
                mSlow = Constant.MIN30_PER_TRADE_DAY * SLOW;
                mSignal = Constant.MIN30_PER_TRADE_DAY * SIGNAL;
                break;
            case Period.MIN15:
                mAverage5 = Constant.MIN15_PER_TRADE_DAY * AVERAGE5;
                mAverage10 = Constant.MIN15_PER_TRADE_DAY * AVERAGE10;
                mFast = Constant.MIN15_PER_TRADE_DAY * FAST;
                mSlow = Constant.MIN15_PER_TRADE_DAY * SLOW;
                mSignal = Constant.MIN15_PER_TRADE_DAY * SIGNAL;
                break;
            case Period.MIN5:
                mAverage5 = Constant.MIN5_PER_TRADE_DAY * AVERAGE5;
                mAverage10 = Constant.MIN5_PER_TRADE_DAY * AVERAGE10;
                mFast = Constant.MIN5_PER_TRADE_DAY * FAST;
                mSlow = Constant.MIN5_PER_TRADE_DAY * SLOW;
                mSignal = Constant.MIN5_PER_TRADE_DAY * SIGNAL;
                break;
        }
        mPriceList.clear();
        mEMAAverage5List.clear();
        mEMAAverage10List.clear();
        mEMAFastList.clear();
        mEMASlowList.clear();
        mDEAList.clear();
        mDIFList.clear();
        mHistogramList.clear();
        for (int i = 0; i < stockDataList.size(); i++) {
            mPriceList.add(stockDataList.get(i).getCandle().getClose());
        }
    }

    private static double getAlpha(int n) {
        double result = 0.0;
        if (n > 0) {
            result = 2.0 / (n + 1.0);
        }
        return result;
    }

    private static void EMA(int N, List<Double> dataList, List<Double> emaList) {
        double alpha = getAlpha(N);

        if (dataList == null || dataList.isEmpty() || emaList == null) {
            return;
        }

        double result = 0.0;
        for (int i = 0; i < dataList.size(); i++) {
            if (i == 0) {
                result = dataList.get(0);
            } else {
                result = alpha * dataList.get(i) + (1.0 - alpha) * emaList.get(i - 1);
            }
            emaList.add(result);
        }
    }

    public static void calculateMACD(String period, ArrayList<StockData> stockDataList) {
        init(period, stockDataList);

        if (mPriceList.size() < StockTrend.VERTEX_SIZE) {
            return;
        }

        mEMAAverage5List.clear();
        mEMAAverage10List.clear();
        mEMAFastList.clear();
        mEMASlowList.clear();
        mDEAList.clear();
        mDIFList.clear();
        mHistogramList.clear();

        EMA(mAverage5, mPriceList, mEMAAverage5List);
        EMA(mAverage10, mPriceList, mEMAAverage10List);
        EMA(mFast, mPriceList, mEMAFastList);
        EMA(mSlow, mPriceList, mEMASlowList);

        int i = 0;
        while (i < mPriceList.size()) {
            mDIFList.add(mEMAFastList.get(i) - mEMASlowList.get(i));
            i++;
        }
        EMA(mSignal, mDIFList, mDEAList);

        i = 0;
        while (i < mPriceList.size()) {
            mHistogramList.add(mDIFList.get(i) - mDEAList.get(i));
            i++;
        }
    }
}