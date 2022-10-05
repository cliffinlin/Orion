package com.android.orion;

import android.util.Log;

import com.android.orion.database.StockData;

import java.util.ArrayList;

public class MarketKeyAnalyzer {
    static final String TAG = Constants.TAG + " "
            + MarketKeyAnalyzer.class.getSimpleName();

    public static final boolean LOG = false;

    public static final double NATURAL_THRESHOLD = 6.0 / 100.0;

    private int mMarketKeyType = StockData.MARKET_KEY_NONE;

    private double mSecondaryRally;
    private double mNaturalRally;
    private double mUpwardTrend;
    private double mDownwardTrend;
    private double mNaturalReaction;
    private double mSecondaryReaction;

    private double mPrevHigh;
    private double mPrevLow;

    public MarketKeyAnalyzer() {
        init();
    }

    void init() {
        mMarketKeyType = StockData.MARKET_KEY_NONE;

        mSecondaryRally = 0;
        mNaturalRally = 0;
        mUpwardTrend = 0;
        mDownwardTrend = 0;
        mNaturalReaction = 0;
        mSecondaryReaction = 0;

        mPrevHigh = 0;
        mPrevLow = 0;
    }

    void analyzeMarketKey(ArrayList<StockData> dataList) {
        int i = 0;
        int size = 0;

        StockData prev = null;
        StockData current = null;

        if (dataList == null) {
            return;
        }

        size = dataList.size();
        if (size < StockData.VERTEX_TYPING_SIZE) {
            return;
        }

        for (i = 1; i < size; i++) {
            prev = dataList.get(i - 1);
            current = dataList.get(i);

            DEBUG(TAG, "i=" + i + " current.getDate()=" + current.getDate() + " current.getLow()=" + current.getLow() + " current.getHigh()=" + current.getHigh());

            switch (mMarketKeyType) {
                case StockData.MARKET_KEY_SECONDARY_RALLY:
                    break;

                case StockData.MARKET_KEY_NATURAL_RALLY:
                    DEBUG(TAG, "case StockData.MARKET_KEY_NATURAL_RALLY: mNaturalRally=" + mNaturalRally);
                    if (current.getHigh() > mNaturalRally) {
                        DEBUG(TAG, "case StockData.MARKET_KEY_NATURAL_RALLY: current.getHigh()=" + current.getHigh() + " > mNaturalRally=" +  mNaturalRally);
                        setNaturalRally(current);
                        if (current.getHigh() > mPrevHigh * (1.0 + NATURAL_THRESHOLD / 2.0)) {
                            DEBUG(TAG, "case StockData.MARKET_KEY_NATURAL_RALLY: current.getHigh()=" + current.getHigh() + " > mPrevHigh * (1.0 + NATURAL_THRESHOLD / 2.0)=" + mPrevHigh * (1.0 + NATURAL_THRESHOLD / 2.0));
                            mMarketKeyType = StockData.MARKET_KEY_UPWARD_TREND;
                            DEBUG(TAG, "case StockData.MARKET_KEY_NATURAL_RALLY: mMarketKeyType = StockData.MARKET_KEY_UPWARD_TREND");
                            setUpwardTrend(current);
                            current.setNaturalRally(0);
                            DEBUG(TAG, "case StockData.MARKET_KEY_NATURAL_RALLY: current.setNaturalRally(0)");
                        }
                    } else if (current.getLow() < mNaturalRally * (1.0 - NATURAL_THRESHOLD)) {
                        DEBUG(TAG, "case StockData.MARKET_KEY_NATURAL_RALLY: current.getLow()=" + current.getLow() + " < mNaturalRally * (1.0 - NATURAL_THRESHOLD)=" + mNaturalRally * (1.0 - NATURAL_THRESHOLD));
                        mPrevHigh = mNaturalRally;
                        DEBUG(TAG, "case StockData.MARKET_KEY_NATURAL_RALLY: mPrevHigh = mNaturalRally=" + mPrevHigh);
                        mMarketKeyType = StockData.MARKET_KEY_NATURAL_REACTION;
                        DEBUG(TAG, "case StockData.MARKET_KEY_NATURAL_RALLY: mMarketKeyType = StockData.MARKET_KEY_NATURAL_REACTION");
                        setNaturalReaction(current);
                    }
                    break;

                case StockData.MARKET_KEY_UPWARD_TREND:
                    DEBUG(TAG, "case StockData.MARKET_KEY_UPWARD_TREND: mUpwardTrend=" + mUpwardTrend);
                    if (current.getHigh() > mUpwardTrend) {
                        DEBUG(TAG, "case StockData.MARKET_KEY_UPWARD_TREND: current.getHigh()=" + current.getHigh() + " > mUpwardTrend=" + mUpwardTrend);
                        setUpwardTrend(current);
                    } else if (current.getLow() < mUpwardTrend * (1.0 - NATURAL_THRESHOLD)) {
                        DEBUG(TAG, "case StockData.MARKET_KEY_UPWARD_TREND: current.getLow()=" + current.getLow() + " < mUpwardTrend * (1.0 - NATURAL_THRESHOLD)=" + mUpwardTrend * (1.0 - NATURAL_THRESHOLD));
                        mPrevHigh = mUpwardTrend;
                        DEBUG(TAG, "case StockData.MARKET_KEY_UPWARD_TREND: mPrevHigh = mUpwardTrend=" + mPrevHigh);
                        mMarketKeyType = StockData.MARKET_KEY_NATURAL_REACTION;
                        DEBUG(TAG, "case StockData.MARKET_KEY_UPWARD_TREND: mMarketKeyType = StockData.MARKET_KEY_NATURAL_REACTION");
                        setNaturalReaction(current);
                    }
                    break;

                case StockData.MARKET_KEY_DOWNWARD_TREND:
                    DEBUG(TAG, "case StockData.MARKET_KEY_DOWNWARD_TREND: mDownwardTrend=" + mDownwardTrend);
                    if (current.getLow() < mDownwardTrend) {
                        DEBUG(TAG, "case StockData.MARKET_KEY_DOWNWARD_TREND: current.getLow()=" + current.getLow() + " < mDownwardTrend=" + mDownwardTrend);
                        setDownwardTrend(current);
                    } else if (current.getHigh() > mDownwardTrend * (1.0 + NATURAL_THRESHOLD)) {
                        DEBUG(TAG, "case StockData.MARKET_KEY_DOWNWARD_TREND: current.getHigh()=" + current.getHigh() + " > mDownwardTrend * (1.0 + NATURAL_THRESHOLD)=" + mDownwardTrend * (1.0 + NATURAL_THRESHOLD));
                        mPrevLow = mDownwardTrend;
                        DEBUG(TAG, "case StockData.MARKET_KEY_DOWNWARD_TREND: mPrevLow = mDownwardTrend=" + mPrevLow);
                        mMarketKeyType = StockData.MARKET_KEY_NATURAL_RALLY;
                        DEBUG(TAG, "case StockData.MARKET_KEY_DOWNWARD_TREND: mMarketKeyType = StockData.MARKET_KEY_NATURAL_RALLY");
                        setNaturalRally(current);
                    }
                    break;

                case StockData.MARKET_KEY_NATURAL_REACTION:
                    DEBUG(TAG, "case StockData.MARKET_KEY_NATURAL_REACTION: mNaturalReaction=" + mNaturalReaction);
                    if (current.getLow() < mNaturalReaction) {
                        DEBUG(TAG, "case StockData.MARKET_KEY_NATURAL_REACTION: current.getLow()=" + current.getLow() + " < mNaturalReaction=" + mNaturalReaction);
                        setNaturalReaction(current);
                        if (current.getLow() < mPrevLow * (1.0 - NATURAL_THRESHOLD / 2.0)) {
                            DEBUG(TAG, "case StockData.MARKET_KEY_NATURAL_REACTION: current.getLow()=" + current.getLow() + "  < mPrevLow * (1.0 - NATURAL_THRESHOLD / 2.0)=" +  mPrevLow * (1.0 - NATURAL_THRESHOLD / 2.0));
                            mMarketKeyType = StockData.MARKET_KEY_DOWNWARD_TREND;
                            DEBUG(TAG, "case StockData.MARKET_KEY_NATURAL_REACTION: mMarketKeyType = StockData.MARKET_KEY_DOWNWARD_TREND");
                            setDownwardTrend(current);
                            current.setNaturalReaction(0);
                            DEBUG(TAG, "case StockData.MARKET_KEY_NATURAL_REACTION: current.setNaturalReaction(0)");
                        }
                    } else if (current.getHigh() > mNaturalReaction * (1.0 + NATURAL_THRESHOLD)) {
                        DEBUG(TAG, "case StockData.MARKET_KEY_NATURAL_REACTION: current.getHigh()=" + current.getHigh() + " > mNaturalReaction * (1.0 + NATURAL_THRESHOLD)=" + mNaturalReaction * (1.0 + NATURAL_THRESHOLD));
                        mPrevLow = mNaturalReaction;
                        DEBUG(TAG, "case StockData.MARKET_KEY_NATURAL_REACTION: mPrevLow = mNaturalReaction=" + mPrevLow);
                        mMarketKeyType = StockData.MARKET_KEY_NATURAL_RALLY;
                        DEBUG(TAG, "case StockData.MARKET_KEY_NATURAL_REACTION: mMarketKeyType = StockData.MARKET_KEY_NATURAL_RALLY");
                        setNaturalRally(current);
                    }
                    break;

                case StockData.MARKET_KEY_SECONDARY_REACTION:
                    break;

                default:
                    DEBUG(TAG, "default:");
                    if (current.getHigh() > prev.getHigh()) {
                        DEBUG(TAG, "current.getHigh() > prev.getHigh()");
                        mMarketKeyType = StockData.MARKET_KEY_UPWARD_TREND;
                        DEBUG(TAG, "mMarketKeyType = StockData.MARKET_KEY_UPWARD_TREND");
                        mUpwardTrend = current.getHigh();
                        mDownwardTrend = current.getHigh();
                        mPrevHigh = current.getHigh();
                        mPrevLow = current.getHigh();
                        DEBUG(TAG, "mUpwardTrend = mDownwardTrend = mPrevHigh = mPrevLow = " + current.getHigh());
                    } else if (current.getLow() < prev.getLow()) {
                        DEBUG(TAG, "current.getLow() < prev.getLow()");
                        mMarketKeyType = StockData.MARKET_KEY_DOWNWARD_TREND;
                        DEBUG(TAG, "mMarketKeyType = StockData.MARKET_KEY_DOWNWARD_TREND");
                        mUpwardTrend = current.getLow();
                        mDownwardTrend = current.getLow();
                        mPrevHigh = current.getLow();
                        mPrevLow = current.getLow();
                        DEBUG(TAG, "mUpwardTrend = mDownwardTrend = mPrevHigh = mPrevLow = " + current.getLow());
                    }
                    break;
            }
        }
    }

    void setNaturalRally(StockData stockData) {
        if (stockData == null) {
            return;
        }

        mNaturalRally = stockData.getHigh();
        stockData.setNaturalRally(mNaturalRally);
        DEBUG(TAG, "setNaturalRally mNaturalRally=" + mNaturalRally);
    }


    void setUpwardTrend(StockData stockData) {
        if (stockData == null) {
            return;
        }

        mUpwardTrend = stockData.getHigh();
        stockData.setUpwardTrend(mUpwardTrend);
        DEBUG(TAG, "setUpwardTrend mUpwardTrend=" + mUpwardTrend);
    }

    void setDownwardTrend(StockData stockData) {
        if (stockData == null) {
            return;
        }

        mDownwardTrend = stockData.getLow();
        stockData.setDownwardTrend(mDownwardTrend);
        DEBUG(TAG, "setDownwardTrend mDownwardTrend=" + mDownwardTrend);
    }


    void setNaturalReaction(StockData stockData) {
        if (stockData == null) {
            return;
        }

        mNaturalReaction = stockData.getLow();
        stockData.setNaturalReaction(mNaturalReaction);
        DEBUG(TAG, "setNaturalReaction mNaturalReaction=" + mNaturalReaction);
    }

    void DEBUG(String tag, String msg) {
        if (LOG) {
            Log.d(tag, msg);
        }
    }
}
