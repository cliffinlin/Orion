package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

import com.android.orion.utility.Utility;

import java.util.Calendar;

public class StockQuant extends StockDeal {
	long mHold;
	double mValuation;
	double mQuantProfit;
	double mQuantProfitMargin;
	long mQuantX;
	double mThreshold;

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
		mQuantProfit = 0;
		mQuantProfitMargin = 0;
		mQuantX = 0;
		mThreshold = 0;
	}

	@Override
	ContentValues getContentValues(ContentValues contentValues) {
		super.getContentValues(contentValues);

		contentValues.put(DatabaseContract.COLUMN_HOLD, mHold);
		contentValues.put(DatabaseContract.COLUMN_VALUATION, mValuation);
		contentValues.put(DatabaseContract.COLUMN_QUANT_PROFIT, mQuantProfit);
		contentValues.put(DatabaseContract.COLUMN_QUANT_PROFIT_MARGIN, mQuantProfitMargin);
		contentValues.put(DatabaseContract.COLUMN_QUANT_X, mQuantX);
		contentValues.put(DatabaseContract.COLUMN_THRESHOLD, mThreshold);

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
		setQuantProfit(stockQuant.mQuantProfit);
		setQuantProfitMargin(stockQuant.mQuantProfitMargin);
		setQuantX(stockQuant.mQuantX);
		setThreshold(stockQuant.mThreshold);
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
		setQuantProfit(cursor);
		setQuantProfitMargin(cursor);
		setQuantX(cursor);
		setThreshold(cursor);
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

	public double getQuantProfit() {
		return mQuantProfit;
	}

	public void setQuantProfit(double quantProfit) {
		mQuantProfit = quantProfit;
	}

	void setQuantProfit(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setQuantProfit(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_QUANT_PROFIT)));
	}

	public double getQuantProfitMargin() {
		return mQuantProfitMargin;
	}

	public void setQuantProfitMargin(double quantProfitMargin) {
		mQuantProfitMargin = quantProfitMargin;
	}

	void setQuantProfitMargin(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setQuantProfitMargin(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_QUANT_PROFIT_MARGIN)));
	}

	public double getQuantX() {
		return mQuantX;
	}

	public void setQuantX(long quantX) {
		mQuantX = quantX;
	}

	void setQuantX(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setQuantX(cursor.getLong(cursor
				.getColumnIndex(DatabaseContract.COLUMN_QUANT_X)));
	}

	public double getThreshold() {
		return mThreshold;
	}

	public void setThreshold(double threshold) {
		mThreshold = threshold;
	}

	void setThreshold(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setThreshold(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_THRESHOLD)));
	}

	public double getBuyValueDay() {
		int days = 0;
		double result = 0;

		Calendar todayCalendar = Utility.getCalendar(
				Utility.getCurrentDateString(), Utility.CALENDAR_DATE_FORMAT);

		Calendar buyCalendar = Utility.getCalendar(
				getCreated(), Utility.CALENDAR_DATE_FORMAT);

		days = (int) ((todayCalendar.getTime().getTime() - buyCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
		result = mValue * days;

		return result;
	}

	public double getSellValueDay() {
		int days = 0;
		double result = 0;

		Calendar todayCalendar = Utility.getCalendar(
				Utility.getCurrentDateString(), Utility.CALENDAR_DATE_FORMAT);

		Calendar sellCalendar = Utility.getCalendar(
				getModified(), Utility.CALENDAR_DATE_FORMAT);

		days = (int) ((todayCalendar.getTime().getTime() - sellCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
		result = mValue * days;

		return result;
	}

	public void setupQuantProfitMargin() {
		if (mValuation != 0) {
			mQuantProfitMargin = Utility.Round(100 * mQuantProfit / mValuation);
		} else {
			mQuantProfitMargin = 0;
		}
	}

	public void setupQuantProfitMargin(double valuation) {
		if (valuation != 0) {
			mQuantProfitMargin = Utility.Round(100 * mQuantProfit / valuation);
		} else {
			mQuantProfitMargin = 0;
		}
	}

	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mName + " ");
		stringBuffer.append(mAction + " ");
		stringBuffer.append("mBuy=" + mBuy + ", ");
		stringBuffer.append("mSell=" + mSell + ",  ");
		stringBuffer.append("mNet=" + mNet + ",  ");
		stringBuffer.append("mProfit=" + mProfit + ",  ");
		stringBuffer.append("mHold=" + mHold + ",  ");
		stringBuffer.append("mValuation=" + mValuation + ",  ");
		stringBuffer.append("mQuantProfit=" + mQuantProfit + ",  ");
		stringBuffer.append("mQuantProfitMargin=" + mQuantProfitMargin + ",  ");
		stringBuffer.append("mThreshold=" + mThreshold);

		return stringBuffer.toString();
	}
}
