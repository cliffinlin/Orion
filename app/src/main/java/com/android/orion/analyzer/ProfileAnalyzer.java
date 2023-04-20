package com.android.orion.analyzer;

import android.util.Log;

import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDeal;
import com.android.orion.setting.Constants;
import com.android.orion.setting.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ProfileAnalyzer {
    static final String TAG = Constants.TAG + " "
            + ProfileAnalyzer.class.getSimpleName();

    long mHold;
    long mVolume;
    double mProfit;
    double mValuation;

    ArrayList<StockDeal> mStockDealList;


    public ProfileAnalyzer() {
        init();
    }

    void init() {
        mHold = 0;
        mProfit = 0;
        mValuation = 0;
        mVolume = 200;

        mStockDealList = new ArrayList<>();
    }

    public void setHold(long hold) {
        mHold = hold;
    }

    public void setmVolume(long volume) {
        mVolume = volume;
    }

    void setupStockDealBuy(Stock stock, StockData stockData, double buyPrice, StockDeal stockDeal) {
        stockDeal.setSE(stock.getSE());
        stockDeal.setCode(stock.getCode());
        stockDeal.setName(stock.getName());

        stockDeal.setPrice(buyPrice);
        stockDeal.setBuy(buyPrice);

        Log.d(TAG, stockData.getDate() + " buy price=" + buyPrice);

        stockDeal.setVolume(mVolume);

        stockDeal.setupFee(stock.getRDate(), stock.getDividend());
        stockDeal.setupNet();
        stockDeal.setupValue();
        stockDeal.setupProfit();
        stockDeal.setupBonus(stock.getDividend());
        stockDeal.setupYield(stock.getDividend());

        stockDeal.setCreated(stockData.getDate());
    }

    void setupStockDealSel(Stock stock, StockData stockData, double sellPrice, StockDeal stockDeal) {
        stockDeal.setPrice(sellPrice);
        stockDeal.setSell(sellPrice);


        stockDeal.setupFee(stock.getRDate(), stock.getDividend());
        stockDeal.setupNet();
        stockDeal.setupValue();
        stockDeal.setupProfit();
    }

    public void analyzeProfite(Stock stock, String period,
                                ArrayList<StockData> stockDataList) {

        mStockDealList.clear();
        StockData stockData;

        if (!period.equals(Settings.KEY_PERIOD_DAY)) {
            //TODO, day only!
            return;
        }

        if (stockDataList == null) {
            Log.d(TAG, "analyzeProfite return" + " stockDataList = " + stockDataList);
            return;
        }

        if (stockDataList.size() < StockData.VERTEX_TYPING_SIZE) {
            return;
        }

        for (int i = 0; i < stockDataList.size(); i++) {
            stockData = stockDataList.get(i);
            if (stockData == null) {
                continue;
            }

            if (stockData.getNaturalReaction() > 0) {
                StockDeal stockDeal = new StockDeal();
                setupStockDealBuy(stock, stockData, stockData.getNaturalReaction(), stockDeal);
                mStockDealList.add(stockDeal);
                Collections.sort(mStockDealList, mBuyComparator);

                if (stockDeal.getVolume() > 0) {
                    mHold += stockDeal.getVolume();
                    mProfit += stockDeal.getProfit();
                    mValuation += stockDeal.getValue();
                    Log.d(TAG, "mHold=" + mHold + ", mProfit=" + mProfit + ", mValuation=" + mValuation);
                }
            } else if (stockData.getDownwardTrend() > 0) {
                StockDeal stockDeal = new StockDeal();
                setupStockDealBuy(stock, stockData, stockData.getDownwardTrend(), stockDeal);
                mStockDealList.add(stockDeal);
                Collections.sort(mStockDealList, mBuyComparator);

                if (stockDeal.getVolume() > 0) {
                    mHold += stockDeal.getVolume();
                    mProfit += stockDeal.getProfit();
                    mValuation += stockDeal.getValue();
                    Log.d(TAG, "mHold=" + mHold + ", mProfit=" + mProfit + ", mValuation=" + mValuation);
                }
            } else if (stockData.getNaturalRally() > 0) {
                if (mStockDealList.size() == 0) {
                    continue;
                }

                StockDeal stockDeal = mStockDealList.get(0);
                setupStockDealSel(stock, stockData, stockData.getNaturalRally(), stockDeal);

                if (stockDeal.getNet() > 0) {
                    if (stockDeal.getVolume() > 0) {
                        Log.d(TAG, stockData.getDate() + " sell price=" + stockDeal.getSell() + "net=" + stockDeal.getNet());

                        mHold -= stockDeal.getVolume();
                        mProfit += stockDeal.getProfit();
                        mValuation -= stockDeal.getValue();
                        Log.d(TAG, "mHold=" + mHold + ", mProfit=" + mProfit + ", mValuation=" + mValuation);

                        mStockDealList.remove(stockDeal);
                        Collections.sort(mStockDealList, mBuyComparator);
                    }
                }
            } else if (stockData.getUpwardTrend() > 0) {
                if (mStockDealList.size() == 0) {
                    continue;
                }

                StockDeal stockDeal = mStockDealList.get(0);
                setupStockDealSel(stock, stockData, stockData.getUpwardTrend(), stockDeal);

                if (stockDeal.getNet() > 0) {
                    if (stockDeal.getVolume() > 0) {
                        Log.d(TAG, stockData.getDateTime() + " sell price=" + stockDeal.getSell());

                        mHold -= stockDeal.getVolume();
                        mProfit += stockDeal.getProfit();
                        mValuation -= stockDeal.getValue();

                        Log.d(TAG, "mHold=" + mHold + ", mProfit=" + mProfit + ", mValuation=" + mValuation);

                        mStockDealList.remove(stockDeal);
                        Collections.sort(mStockDealList, mBuyComparator);
                    }
                }
            }
        }

        double rate = 0;
        if (mValuation != 0) {
            rate = mProfit / mValuation;
        }

        Log.d(TAG, "rate=" + rate);
    }

    Comparator<StockDeal> mBuyComparator = new Comparator<StockDeal>() {

        @Override
        public int compare(StockDeal arg0, StockDeal arg1) {
            if (arg0.getBuy() < arg1.getBuy()) {
                return -1;
            } else if (arg0.getBuy() > arg1.getBuy()) {
                return 1;
            } else {
                return 0;
            }
        }
    };
}
