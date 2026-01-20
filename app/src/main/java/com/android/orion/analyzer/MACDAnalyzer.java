package com.android.orion.analyzer;

import com.android.orion.config.Config;
import com.android.orion.data.Period;
import com.android.orion.database.StockData;
import com.android.orion.database.StockTrend;
import com.android.orion.constant.Constant;
import com.android.orion.utility.Logger;

import java.util.ArrayList;
import java.util.List;

public class MACDAnalyzer {

    private static int mFast = 0;
    private static int mSlow = 0;
    private static int mNormal = 0;

    private static final ArrayList<Double> mPriceList = new ArrayList<>();
    private static final ArrayList<Double> mEMAFastList = new ArrayList<>();
    private static final ArrayList<Double> mEMASlowList = new ArrayList<>();
    private static final ArrayList<Double> mDEAList = new ArrayList<>();
    private static final ArrayList<Double> mDIFList = new ArrayList<>();
    private static final ArrayList<Double> mHistogramList = new ArrayList<>();

    static Logger Log = Logger.getLogger();

    private static final int FAST = 10; //12;
    private static final int SLOW = 20; //26;
    private static final int NORMAL = 8; //9;

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
        switch (period) {
            case Period.MONTH:
            case Period.WEEK:
            case Period.DAY:
                mFast = FAST;
                mSlow = SLOW;
                mNormal = NORMAL;
                break;
            case Period.MIN60:
                mFast = Constant.MIN60_PER_TRADE_DAY * FAST;
                mSlow = Constant.MIN60_PER_TRADE_DAY * SLOW;
                mNormal = Constant.MIN60_PER_TRADE_DAY * NORMAL;
                break;
            case Period.MIN30:
                mFast = Constant.MIN30_PER_TRADE_DAY * FAST;
                mSlow = Constant.MIN30_PER_TRADE_DAY * SLOW;
                mNormal = Constant.MIN30_PER_TRADE_DAY * NORMAL;
                break;
            case Period.MIN15:
                mFast = Constant.MIN15_PER_TRADE_DAY * FAST;
                mSlow = Constant.MIN15_PER_TRADE_DAY * SLOW;
                mNormal = Constant.MIN15_PER_TRADE_DAY * NORMAL;
                break;
            case Period.MIN5:
                mFast = Constant.MIN5_PER_TRADE_DAY * FAST;
                mSlow = Constant.MIN5_PER_TRADE_DAY * SLOW;
                mNormal = Constant.MIN5_PER_TRADE_DAY * NORMAL;
                break;
        }
        mPriceList.clear();
        mEMAFastList.clear();
        mEMASlowList.clear();
        mDEAList.clear();
        mDIFList.clear();
        mHistogramList.clear();

        // 1. 获取原始收盘价
        for (int i = 0; i < stockDataList.size(); i++) {
            mPriceList.add(stockDataList.get(i).getCandle().getClose());
        }

        // 2. 进行归一化处理 (Min-Max Normalization)
        normalizePriceList();
    }

    /**
     * 将 mPriceList 中的数据归一化到 [0, 10] 区间
     */
    private static void normalizePriceList() {
        if (mPriceList.isEmpty()) return;

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        // 寻找最大值和最小值
        for (Double price : mPriceList) {
            if (price < min) min = price;
            if (price > max) max = price;
        }

        double range = max - min;

        // 如果 range 为 0，说明所有价格都一样，归一化为 0.5 或者保持不变
        if (range <= 0) {
            for (int i = 0; i < mPriceList.size(); i++) {
                mPriceList.set(i, 0.0);
            }
            return;
        }

        // 执行归一化计算: (x - min) / (max - min)
        for (int i = 0; i < mPriceList.size(); i++) {
            double normalizedValue = Config.MACD_NORMALIZED_VALUE * (mPriceList.get(i) - min) / range;
            mPriceList.set(i, normalizedValue);
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
        // init 中已经包含了获取价格和归一化的逻辑
        init(period, stockDataList);

        if (mPriceList.size() < StockTrend.VERTEX_SIZE) {
            return;
        }

        // 此时 EMA 计算所使用的 mPriceList 已经是 0-1 之间的数值
        EMA(mFast, mPriceList, mEMAFastList);
        EMA(mSlow, mPriceList, mEMASlowList);

        int i = 0;
        while (i < mPriceList.size()) {
            mDIFList.add(mEMAFastList.get(i) - mEMASlowList.get(i));
            i++;
        }
        EMA(mNormal, mDIFList, mDEAList);

        i = 0;
        while (i < mPriceList.size()) {
            mHistogramList.add(mDIFList.get(i) - mDEAList.get(i));
            i++;
        }
    }
}