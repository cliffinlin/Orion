package com.android.orion.analyzer;

import android.content.Context;
import android.util.Log;

import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDatabaseManager;
import com.android.orion.database.StockQuant;
import com.android.orion.setting.Constants;
import com.android.orion.setting.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class StockQuantAnalyzer {
    static final String TAG = Constants.TAG + " "
            + StockQuantAnalyzer.class.getSimpleName();

    ArrayList<ShareBonus> mShareBonusList;
    ArrayList<StockQuant> mStockQuantList;
    Context mContext;
	
    long mHold;
    long mVolume;
    double mNetProfit;
    double mValuation;

    public StockQuantAnalyzer() {
        init();
    }

    void init() {
        mHold = 0;
        mNetProfit = 0;
        mValuation = 0;
			
        if (mStockQuantList == null) {
            mStockQuantList = new ArrayList<>();
        }
    }

    void setupStockQuantBuy(Stock stock, StockData stockData, double buyPrice, StockQuant stockQuant) {
        stockQuant.setSE(stock.getSE());
        stockQuant.setCode(stock.getCode());
        stockQuant.setName(stock.getName());

        stockQuant.setCreated(stockData.getDate());

        stockQuant.setAction(StockData.ACTION_BUY);

        stockQuant.setVolume(mVolume);
        stockQuant.setPrice(buyPrice);
        stockQuant.setBuy(buyPrice);

        stockQuant.setupBuyFee();
        stockQuant.setupNet();
        stockQuant.setupValue();
        stockQuant.setupProfit();

        mHold += stockQuant.getVolume();
        stockQuant.setHold(mHold);

        mNetProfit += stockQuant.getProfit();
        stockQuant.setNetProfit(mNetProfit);

        mValuation += stockQuant.getValue();
        stockQuant.setValuation(mValuation);

        stockQuant.setupNetProfitMargin();

        Log.d(TAG, stockQuant.toString());

        mStockQuantList.add(stockQuant);
        Collections.sort(mStockQuantList, mComparator);

        StockDatabaseManager.getInstance(mContext).insertStockQuant(stockQuant);
    }

    void setupStockQuantSell(Stock stock, StockData stockData, double sellPrice, StockQuant stockQuant) {
        stockQuant.setModified(stockData.getDate());

        stockQuant.setAction(StockData.ACTION_SELL);

        stockQuant.setPrice(sellPrice);
        stockQuant.setSell(sellPrice);

        stockQuant.setupSellFee(mShareBonusList);
        stockQuant.setupNet();
        stockQuant.setupValue();
        stockQuant.setupProfit();

        if (stockQuant.getNet() <= 0) {
            return;
        }

        mHold -= stockQuant.getVolume();
        stockQuant.setHold(mHold);

        mNetProfit += stockQuant.getProfit();
        stockQuant.setNetProfit(mNetProfit);

        mValuation -= stockQuant.getValue();
        stockQuant.setValuation(mValuation);

        stockQuant.setupNetProfitMargin();

        Log.d(TAG, stockQuant.toString());

        mStockQuantList.remove(stockQuant);
        Collections.sort(mStockQuantList, mComparator);

        StockDatabaseManager.getInstance(mContext).insertStockQuant(stockQuant);
    }

    public void analyze(Context context, Stock stock, String period,
                        ArrayList<StockData> stockDataList, ArrayList<ShareBonus> shareBonusList) {
        if (context == null) {
            return;
        }

        mContext = context;

        if (stock == null) {
            return;
        }

        mVolume = stock.getQuantVolume();
        if (mVolume == 0) {
            return;
        }

        if (!period.equals(Settings.KEY_PERIOD_DAY)) {
            return;
        }

        if (stockDataList == null || stockDataList.size() < StockData.VERTEX_TYPING_SIZE) {
            return;
        }

        if (shareBonusList == null || shareBonusList.size() < StockData.VERTEX_TYPING_SIZE) {
            return;
        }

        mShareBonusList = shareBonusList;

        StockDatabaseManager.getInstance(mContext).deleteStockQuant(stock);

        mStockQuantList.clear();

        for (int i = 0; i < stockDataList.size(); i++) {
            StockData stockData = stockDataList.get(i);
            if (stockData == null) {
                continue;
            }

            if (stockData.getNaturalReaction() > 0) {
                StockQuant stockQuant = new StockQuant();
                setupStockQuantBuy(stock, stockData, stockData.getNaturalReaction(), stockQuant);
            } else if (stockData.getDownwardTrend() > 0) {
                StockQuant stockQuant = new StockQuant();
                setupStockQuantBuy(stock, stockData, stockData.getDownwardTrend(), stockQuant);
            } else if (stockData.getNaturalRally() > 0) {
                if (mStockQuantList.size() == 0) {
                    continue;
                }

                StockQuant stockQuant = mStockQuantList.get(0);
                setupStockQuantSell(stock, stockData, stockData.getNaturalRally(), stockQuant);

            } else if (stockData.getUpwardTrend() > 0) {
                if (mStockQuantList.size() == 0) {
                    continue;
                }

                StockQuant stockQuant = mStockQuantList.get(0);
                setupStockQuantSell(stock, stockData, stockData.getUpwardTrend(), stockQuant);
            }
        }
    }

    Comparator<StockQuant> mComparator = new Comparator<StockQuant>() {

        @Override
        public int compare(StockQuant arg0, StockQuant arg1) {
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
