package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.Constants;
import com.android.orion.utility.Utility;

import java.util.Calendar;

public class StockDeal extends DatabaseTable {
    private String mSE;
    private String mCode;
    private String mName;
    private String mAction;
    private double mPrice;
    private double mNet;
    private double mDeal;
    private long mVolume;
    private double mValue;
    private double mProfit;

    public StockDeal() {
        init();
    }

    public StockDeal(StockDeal stockDeal) {
        set(stockDeal);
    }

    public StockDeal(Cursor cursor) {
        set(cursor);
    }

    boolean isEmpty() {
        boolean result = false;

        if (TextUtils.isEmpty(mSE) && TextUtils.isEmpty(mCode)
                && TextUtils.isEmpty(mName)) {
            result = true;
        }

        return result;
    }

    public void init() {
        super.init();

        setTableName(DatabaseContract.StockDeal.TABLE_NAME);

        mSE = "";
        mCode = "";
        mName = "";
        mAction = "";
        mPrice = 0;
        mNet = 0;
        mDeal = 0;
        mVolume = 0;
        mValue = 0;
        mProfit = 0;
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        super.getContentValues(contentValues);
        contentValues = getContentValues(contentValues);
        return contentValues;
    }

    ContentValues getContentValues(ContentValues contentValues) {
        contentValues.put(DatabaseContract.COLUMN_SE, mSE);
        contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
        contentValues.put(DatabaseContract.COLUMN_NAME, mName);
        contentValues.put(DatabaseContract.COLUMN_ACTION, mAction);
        contentValues.put(DatabaseContract.COLUMN_PRICE, mPrice);
        contentValues.put(DatabaseContract.COLUMN_NET, mNet);
        contentValues.put(DatabaseContract.COLUMN_DEAL, mDeal);
        contentValues.put(DatabaseContract.COLUMN_VOLUME, mVolume);
        contentValues.put(DatabaseContract.COLUMN_VALUE, mValue);
        contentValues.put(DatabaseContract.COLUMN_PROFIT, mProfit);

        return contentValues;
    }

    void set(StockDeal stockDeal) {
        if (stockDeal == null) {
            return;
        }

        init();

        super.set(stockDeal);

        setSE(stockDeal.mSE);
        setCode(stockDeal.mCode);
        setName(stockDeal.mName);
        setAction(stockDeal.mAction);
        setPrice(stockDeal.mPrice);
        setNet(stockDeal.mNet);
        setDeal(stockDeal.mDeal);
        setVolume(stockDeal.mVolume);
        setValue(stockDeal.mValue);
        setProfit(stockDeal.mProfit);
    }

    @Override
    public void set(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        init();

        super.set(cursor);

        setSE(cursor);
        setCode(cursor);
        setName(cursor);
        setAction(cursor);
        setPrice(cursor);
        setNet(cursor);
        setDeal(cursor);
        setVolume(cursor);
        setValue(cursor);
        setProfit(cursor);
    }

    public String getSE() {
        return mSE;
    }

    public void setSE(String se) {
        mSE = se;
    }

    void setSE(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        setSE(cursor.getString(cursor
                .getColumnIndex(DatabaseContract.COLUMN_SE)));
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String code) {
        mCode = code;
    }

    void setCode(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        setCode(cursor.getString(cursor
                .getColumnIndex(DatabaseContract.COLUMN_CODE)));
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    void setName(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        setName(cursor.getString(cursor
                .getColumnIndex(DatabaseContract.COLUMN_NAME)));
    }

    public String getAction() {
        return mAction;
    }

    public void setAction(String action) {
        mAction = action;
    }

    void setAction(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        setAction(cursor.getString(cursor
                .getColumnIndex(DatabaseContract.COLUMN_ACTION)));
    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(double price) {
        mPrice = price;
    }

    void setPrice(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        setPrice(cursor.getDouble(cursor
                .getColumnIndex(DatabaseContract.COLUMN_PRICE)));
    }

    public double getNet() {
        return mNet;
    }

    public void setNet(double net) {
        mNet = net;
    }

    void setNet(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        setNet(cursor.getDouble(cursor
                .getColumnIndex(DatabaseContract.COLUMN_NET)));
    }

    public double getDeal() {
        return mDeal;
    }

    public void setDeal(double deal) {
        mDeal = deal;
    }

    void setDeal(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        setDeal(cursor.getDouble(cursor
                .getColumnIndex(DatabaseContract.COLUMN_DEAL)));
    }

    public long getVolume() {
        return mVolume;
    }

    public void setVolume(long volume) {
        mVolume = volume;
    }

    void setVolume(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        setVolume(cursor.getLong(cursor
                .getColumnIndex(DatabaseContract.COLUMN_VOLUME)));
    }

    public double getValue() {
        return mValue;
    }

    public void setValue(double value) {
        mValue = value;
    }

    void setValue(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        setValue(cursor.getDouble(cursor
                .getColumnIndex(DatabaseContract.COLUMN_VALUE)));
    }

    public double getProfit() {
        return mProfit;
    }

    public void setProfit(double profit) {
        mProfit = profit;
    }

    void setProfit(Cursor cursor) {
        if (cursor == null) {
            return;
        }

        setProfit(cursor.getDouble(cursor
                .getColumnIndex(DatabaseContract.COLUMN_PROFIT)));
    }

    public void setupNet() {
        if (mPrice == 0) {
            return;
        }

        if (mDeal == 0) {
            mNet = 0;
            return;
        }

        mNet = Utility.Round(100 * (mPrice - mDeal) / mDeal,
                Constants.DOUBLE_FIXED_DECIMAL);
    }

    public void setupValue() {
        if ((mDeal == 0) || (mVolume == 0)) {
            mValue = 0;
            return;
        }

        mValue = Utility.Round(mDeal * mVolume,
                Constants.DOUBLE_FIXED_DECIMAL);
    }

    public void setupProfit(String rDate, double dividend) {
        if (mPrice == 0) {
            return;
        }

        double stampDuty = mPrice * mVolume * 1.0 / 1000.0;

        double buyTransferFee = mDeal * mVolume * 0.2 / 10000.0;
        double sellTransferFee = mPrice * mVolume * 0.2 / 10000.0;

        if (buyTransferFee < 1.0) {
            buyTransferFee = 1.0;
        }

        if (sellTransferFee < 1.0) {
            sellTransferFee = 1.0;
        }

        double buyCommission = mDeal * mVolume * 5.0 / 10000.0;
        double sellCommission = mPrice * mVolume * 5.0 / 10000.0;

        if (buyCommission < 5.0) {
            buyCommission = 5.0;
        }

        if (sellCommission < 5.0) {
            sellCommission = 5.0;
        }

        double dividendTax = 0;

        Calendar todayCalendar = Utility.stringToCalendar(
                Utility.getCurrentDateString(), Utility.CALENDAR_DATE_FORMAT);

        Calendar rDateCalendarAfterMonth = Utility.stringToCalendar(
                rDate, Utility.CALENDAR_DATE_FORMAT);
        rDateCalendarAfterMonth.add(Calendar.MONTH, 1);

        Calendar rDateCalendarAfterYear = Utility.stringToCalendar(
                rDate, Utility.CALENDAR_DATE_FORMAT);
        rDateCalendarAfterYear.add(Calendar.YEAR, 1);

        if (todayCalendar.before(rDateCalendarAfterMonth)) {
            dividendTax = dividend / 10.0 * mVolume * 20.0 / 100.0;
        } else if (todayCalendar.before(rDateCalendarAfterYear)) {
            dividendTax = dividend / 10.0 * mVolume * 10.0 / 100.0;
        } else {
            dividendTax = 0;
        }

        double totalFee = stampDuty + buyTransferFee + sellTransferFee + buyCommission + sellCommission + dividendTax;

        if (mVolume < 0) {
            totalFee = 0;
        }

        mProfit = Utility.Round((mPrice - mDeal) * mVolume - totalFee,
                Constants.DOUBLE_FIXED_DECIMAL);
    }
}
