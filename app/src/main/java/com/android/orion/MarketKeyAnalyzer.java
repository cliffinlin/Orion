package com.android.orion;

import com.android.orion.database.StockData;

import java.util.ArrayList;

public class MarketKeyAnalyzer {
    static final String TAG = Constants.TAG + " "
            + MarketKeyAnalyzer.class.getSimpleName();

    public static final double NATURAL_THRESHOLD = 6.0 / 100.0;

    private int mMarketKeyType = StockData.MARKET_KEY_NONE;

    private double mSecondaryRally;
    private double mNaturalRally;
    private double mUpwardTrend;
    private double mDownwardTrend;
    private double mNaturalReaction;
    private double mSecondaryReaction;

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

            switch (mMarketKeyType) {
                case StockData.MARKET_KEY_SECONDARY_RALLY:
                    break;

                case StockData.MARKET_KEY_NATURAL_RALLY:
                    if (current.getClose() > mNaturalRally) {
                        mNaturalRally = current.getClose();
                        current.setNaturalRally(mNaturalRally);
                        if (current.getClose() > mUpwardTrend * (1.0 + NATURAL_THRESHOLD / 2.0)) {
                            mUpwardTrend = current.getClose();
                            current.setUpwardTrend(mUpwardTrend);
                            mMarketKeyType = StockData.MARKET_KEY_UPWARD_TREND;
                        }
                    } else if (current.getClose() < mNaturalRally * (1.0 - NATURAL_THRESHOLD)) {
                        mNaturalReaction = current.getClose();
                        current.setNaturalReaction(mNaturalReaction);
                        mMarketKeyType = StockData.MARKET_KEY_NATURAL_REACTION;
                    }
                    break;

                case StockData.MARKET_KEY_UPWARD_TREND:
                    if (current.getClose() > mUpwardTrend) {
                        mUpwardTrend = current.getClose();
                        current.setUpwardTrend(mUpwardTrend);
                    } else if (current.getClose() < mUpwardTrend * (1.0 - NATURAL_THRESHOLD)) {
                        mNaturalReaction = current.getClose();
                        current.setNaturalReaction(mNaturalReaction);
                        mMarketKeyType = StockData.MARKET_KEY_NATURAL_REACTION;
                    }
                    break;

                case StockData.MARKET_KEY_DOWNWARD_TREND:
                    if (current.getClose() < mDownwardTrend) {
                        mDownwardTrend = current.getClose();
                        current.setDownwardTrend(mDownwardTrend);
                    } else if (current.getClose() > mDownwardTrend * (1.0 + NATURAL_THRESHOLD)) {
                        mNaturalRally = current.getClose();
                        current.setNaturalRally(mNaturalRally);
                        mMarketKeyType = StockData.MARKET_KEY_NATURAL_RALLY;
                    }
                    break;

                case StockData.MARKET_KEY_NATURAL_REACTION:
                    if (current.getClose() < mNaturalReaction) {
                        mNaturalReaction = current.getClose();
                        current.setNaturalReaction(mNaturalReaction);
                        if (current.getClose() < mDownwardTrend * (1.0 - NATURAL_THRESHOLD / 2.0)) {
                            mDownwardTrend = current.getClose();
                            current.setDownwardTrend(mDownwardTrend);
                            mMarketKeyType = StockData.MARKET_KEY_DOWNWARD_TREND;
                        }
                    } else if (current.getClose() > mNaturalReaction * (1.0 + NATURAL_THRESHOLD)) {
                        mNaturalRally = current.getClose();
                        current.setNaturalRally(mNaturalRally);
                        mMarketKeyType = StockData.MARKET_KEY_NATURAL_RALLY;
                    }
                    break;

                case StockData.MARKET_KEY_SECONDARY_REACTION:
                    break;

                default:
                    if (current.getClose() > prev.getClose()) {
                        mMarketKeyType = StockData.MARKET_KEY_UPWARD_TREND;
                        mUpwardTrend = current.getClose();
                        mDownwardTrend = current.getClose();
                    } else if (current.getClose() < prev.getClose()) {
                        mMarketKeyType = StockData.MARKET_KEY_DOWNWARD_TREND;
                        mUpwardTrend = current.getClose();
                        mDownwardTrend = current.getClose();
                    }
                    break;
            }
        }
    }

    public int getMarketKeyType() {
        return mMarketKeyType;
    }

    public void setMarketKeyType(int marketKeyType) {
        mMarketKeyType = marketKeyType;
    }

    public double getSecondaryRally() {
        return mSecondaryRally;
    }

    public void setSecondaryRally(double secondaryRally) {
        mSecondaryRally = secondaryRally;
    }

    public double getNaturalRally() {
        return mNaturalRally;
    }

    public void setNaturalRally(double naturalRally) {
        mNaturalRally = naturalRally;
    }

    public double getUpwardTrend() {
        return mUpwardTrend;
    }

    public void setUpwardTrend(double upwardTrend) {
        mUpwardTrend = upwardTrend;
    }

    public double getDownwardTrend() {
        return mDownwardTrend;
    }

    public void setDownwardTrend(double downwardTrend) {
        mDownwardTrend = downwardTrend;
    }

    public double getNaturalReaction() {
        return mNaturalReaction;
    }

    public void setNaturalReaction(double naturalReaction) {
        mNaturalReaction = naturalReaction;
    }

    public double getSecondaryReaction() {
        return mSecondaryReaction;
    }

    public void setSecondaryReaction(double secondaryReaction) {
        mSecondaryReaction = secondaryReaction;
    }
}
