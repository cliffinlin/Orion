package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.data.Candle;
import com.android.orion.data.Macd;
import com.android.orion.utility.Market;
import com.android.orion.utility.Symbol;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StockRadar extends DatabaseTable {

	public static Comparator<StockRadar> comparator = new Comparator<StockRadar>() {

		@Override
		public int compare(StockRadar arg0, StockRadar arg1) {
			Calendar calendar0;
			Calendar calendar1;

			if (arg0 == null || arg1 == null) {
				return 0;
			}

			calendar0 = Utility.getCalendar(arg0.getDateTime(),
					Utility.CALENDAR_DATE_TIME_FORMAT);
			calendar1 = Utility.getCalendar(arg1.getDateTime(),
					Utility.CALENDAR_DATE_TIME_FORMAT);
			if (calendar0.before(calendar1)) {
				return -1;
			} else if (calendar0.after(calendar1)) {
				return 1;
			} else {
				return 0;
			}
		}
	};

	private String mSE;
	private String mCode;
	private String mName;
	private String mPeriod;
	private String mDate;
	private String mTime;
	private double mSignal;

	public StockRadar() {
		init();
	}

	public StockRadar(String period) {
		init();
		setPeriod(period);
	}

	public StockRadar(StockRadar stockRadar) {
		set(stockRadar);
	}

	public StockRadar(Cursor cursor) {
		set(cursor);
	}

	public boolean isEmpty() {
		return TextUtils.isEmpty(mDate)
				&& TextUtils.isEmpty(mTime);
	}

	public void init() {
		super.init();

		setTableName(DatabaseContract.StockRadar.TABLE_NAME);

		mSE = "";
		mCode = "";
		mName = "";
		mPeriod = "";
		mDate = "";
		mTime = "";
		mSignal = 0;
	}

	@Override
	public ContentValues getContentValues() {
		ContentValues contentValues = super.getContentValues();

		contentValues.put(DatabaseContract.COLUMN_SE, mSE);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		contentValues.put(DatabaseContract.COLUMN_PERIOD, mPeriod);
		contentValues.put(DatabaseContract.COLUMN_DATE, mDate);
		contentValues.put(DatabaseContract.COLUMN_TIME, mTime);
		contentValues.put(DatabaseContract.COLUMN_SIGNAL, mSignal);
		return contentValues;
	}

	public void set(StockRadar stockRadar) {
		if (stockRadar == null) {
			return;
		}

		init();

		super.set(stockRadar);

		setSE(stockRadar.mSE);
		setCode(stockRadar.mCode);
		setName(stockRadar.mName);
		setPeriod(stockRadar.mPeriod);
		setDate(stockRadar.mDate);
		setTime(stockRadar.mTime);
		setSignal(stockRadar.mSignal);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		init();

		super.set(cursor);

		setSE(cursor);
		setCode(cursor);
		setName(cursor);
		setPeriod(cursor);
		setDate(cursor);
		setTime(cursor);
		setSignal(cursor);
	}

	public String getSE() {
		return mSE;
	}

	public void setSE(String se) {
		mSE = se;
	}

	void setSE(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setName(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NAME)));
	}

	public String getPeriod() {
		return mPeriod;
	}

	public void setPeriod(String period) {
		mPeriod = period;
	}

	void setPeriod(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setPeriod(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PERIOD)));
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(String date) {
		mDate = date;
	}

	public void setDate(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setDate(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DATE)));
	}

	public String getTime() {
		return mTime;
	}

	public void setTime(String time) {
		mTime = time;
	}

	void setTime(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setTime(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TIME)));
	}

	public String getDateTime() {
		if (!TextUtils.isEmpty(mTime)) {
			return mDate + " " + mTime;
		} else {
			return mDate + " " + Market.SECOND_HALF_END_TIME;
		}
	}

	public double getSignal() {
		return mSignal;
	}

	public void setSignal(double signal) {
		mSignal = signal;
	}

	void setSignal(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setSignal(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_SIGNAL)));
	}
}