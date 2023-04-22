package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

import com.android.orion.setting.Constants;
import com.android.orion.utility.Utility;

public class StockQuant extends StockDeal {

    private long mHold;
    private double mValuation;
    private double mNetProfit;
    private double mNetProfitMargin;

    public StockQuant() {
        init();
    }

    public StockQuant(StockQuant stockQuant) {
        set(stockQuant);
    }

    public StockQuant(Cursor cursor) {
        set(cursor);
    }

    public void init() {
        super.init();

        setTableName(DatabaseContract.StockQuant.TABLE_NAME);

        mHold = 0;
        mValuation = 0;
        mNetProfit = 0;
        mNetProfitMargin = 0;
    }

    @Override
    ContentValues getContentValues(ContentValues contentValues) {
        super.getContentValues(contentValues);

        contentValues.put(DatabaseContract.COLUMN_HOLD, mHold);
        contentValues.put(DatabaseContract.COLUMN_VALUATION, mValuation);
        contentValues.put(DatabaseContract.COLUMN_NET_PROFIT, mNetProfit);
        contentValues.put(DatabaseContract.COLUMN_NET_PROFIT_MARGIN, mNetProfitMargin);

        return contentValues;
    }

    void set(StockQuant stockQuant) {
        if (stockQuant == null) {
            return;
        }

        init();

        super.set(stockQuant);

        setHold(stockQuant.mHold);
        setValuation(stockQuant.mValuation);
        setNetProfit(stockQuant.mNetProfit);
        setNetProfitMargin(stockQuant.mNetProfitMargin);
    }

    @Override
    public void set(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        init();

        super.set(cursor);

        setHold(cursor);
        setValuation(cursor);
        setNetProfit(cursor);
        setNetProfitMargin(cursor);
    }

    public long getHold() {
        return mHold;
    }

    public void setHold(long hold) {
        mHold = hold;
    }

    void setHold(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        setHold(cursor.getLong(cursor
                .getColumnIndex(DatabaseContract.COLUMN_HOLD)));
    }

    public double getValuation() {
        return mValuation;
    }

    public void setValuation(double valuation) {
        mValuation = valuation;
    }

    void setValuation(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        setValuation(cursor.getDouble(cursor
                .getColumnIndex(DatabaseContract.COLUMN_VALUATION)));
    }

    public double getNetProfit() {
        return mNetProfit;
    }

    public void setNetProfit(double netProfit) {
        mNetProfit = netProfit;
    }

    void setNetProfit(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        setNetProfit(cursor.getDouble(cursor
                .getColumnIndex(DatabaseContract.COLUMN_NET_PROFIT)));
    }

    public double getNetProfitMargin() {
        return mNetProfitMargin;
    }

    public void setNetProfitMargin(double netProfitMargin) {
        mNetProfitMargin = netProfitMargin;
    }

    void setNetProfitMargin(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        setNetProfitMargin(cursor.getDouble(cursor
                .getColumnIndex(DatabaseContract.COLUMN_NET_PROFIT_MARGIN)));
    }

    public void setupNetProfitMargin() {
        if (mValuation != 0) {
            mNetProfitMargin = Utility.Round(100 * mNetProfit / mValuation,
                    Constants.DOUBLE_FIXED_DECIMAL);
        } else {
            mNetProfitMargin = 0;
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(mName + " ");
        stringBuilder.append(mAction + " ");
        stringBuilder.append("mBuy=" + mBuy + ", ");
        stringBuilder.append("mSell=" + mSell + ",  ");
        stringBuilder.append("mNet=" + mNet + ",  ");
        stringBuilder.append("mProfit=" + mProfit + ",  ");
        stringBuilder.append("mHold=" + mHold + ",  ");
        stringBuilder.append("mValuation=" + mValuation + ",  ");
        stringBuilder.append("mNetProfit=" + mNetProfit + ",  ");
        stringBuilder.append("mNetProfitMargin=" + mNetProfitMargin);

        return stringBuilder.toString();
    }
}
