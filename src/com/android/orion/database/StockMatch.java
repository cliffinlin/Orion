package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.Constants;

public class StockMatch extends DatabaseTable {
	private String mSE_X;
	private String mCode_X;
	private String mName_X;
	private String mSE_Y;
	private String mCode_Y;
	private String mName_Y;
	private double mMin1;
	private double mMin5;
	private double mMin15;
	private double mMin30;
	private double mMin60;
	private double mDay;
	private double mWeek;
	private double mMonth;
	private double mQuarter;
	private double mYear;

	private StockMatch next;
	private static final Object sPoolSync = new Object();
	private static StockMatch sPool;

	public static StockMatch obtain() {
		synchronized (sPoolSync) {
			if (sPool != null) {
				StockMatch m = sPool;
				sPool = m.next;
				m.next = null;
				return m;
			}
		}
		return new StockMatch();
	}

	public StockMatch() {
		init();
	}

	public StockMatch(StockMatch stockMatch) {
		set(stockMatch);
	}

	public StockMatch(Cursor cursor) {
		set(cursor);
	}

	boolean isEmpty() {
		boolean result = false;

		if (TextUtils.isEmpty(mSE_X) && TextUtils.isEmpty(mCode_X)
				&& TextUtils.isEmpty(mName_X) && TextUtils.isEmpty(mSE_Y)
				&& TextUtils.isEmpty(mCode_Y) && TextUtils.isEmpty(mName_Y)) {
			result = true;
		}

		return result;
	}

	void init() {
		super.init();

		setTableName(DatabaseContract.StockMatch.TABLE_NAME);

		mSE_X = "";
		mCode_X = "";
		mName_X = "";
		mSE_Y = "";
		mCode_Y = "";
		mName_Y = "";
		mMin1 = 0;
		mMin5 = 0;
		mMin15 = 0;
		mMin30 = 0;
		mMin60 = 0;
		mDay = 0;
		mWeek = 0;
		mMonth = 0;
		mQuarter = 0;
		mYear = 0;
	}

	public ContentValues getContentValues() {
		ContentValues contentValues = new ContentValues();
		super.getContentValues(contentValues);
		contentValues = getContentValues(contentValues);
		return contentValues;
	}

	ContentValues getContentValues(ContentValues contentValues) {
		contentValues.put(DatabaseContract.StockMatch.COLUMN_SE_X, mSE_X);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_CODE_X, mCode_X);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_NAME_X, mName_X);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_SE_Y, mSE_Y);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_CODE_Y, mCode_Y);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_NAME_Y, mName_Y);
		contentValues.put(DatabaseContract.COLUMN_MIN1, mMin1);
		contentValues.put(DatabaseContract.COLUMN_MIN5, mMin5);
		contentValues.put(DatabaseContract.COLUMN_MIN15, mMin15);
		contentValues.put(DatabaseContract.COLUMN_MIN30, mMin30);
		contentValues.put(DatabaseContract.COLUMN_MIN60, mMin60);
		contentValues.put(DatabaseContract.COLUMN_DAY, mDay);
		contentValues.put(DatabaseContract.COLUMN_WEEK, mWeek);
		contentValues.put(DatabaseContract.COLUMN_MONTH, mMonth);
		contentValues.put(DatabaseContract.COLUMN_QUARTER, mQuarter);
		contentValues.put(DatabaseContract.COLUMN_YEAR, mYear);

		return contentValues;
	}

	void set(StockMatch stockMatch) {
		if (stockMatch == null) {
			return;
		}

		init();

		super.set(stockMatch);

		setSE_X(stockMatch.mSE_X);
		setCode_X(stockMatch.mCode_X);
		setName_X(stockMatch.mName_X);
		setSE_Y(stockMatch.mSE_Y);
		setCode_Y(stockMatch.mCode_Y);
		setName_Y(stockMatch.mName_Y);
		setMin1(stockMatch.mMin1);
		setMin5(stockMatch.mMin5);
		setMin15(stockMatch.mMin15);
		setMin30(stockMatch.mMin30);
		setMin60(stockMatch.mMin60);
		setDay(stockMatch.mDay);
		setWeek(stockMatch.mWeek);
		setMonth(stockMatch.mMonth);
		setQuarter(stockMatch.mQuarter);
		setYear(stockMatch.mYear);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		super.set(cursor);

		setSE_X(cursor);
		setCode_X(cursor);
		setName_X(cursor);
		setSE_Y(cursor);
		setCode_Y(cursor);
		setName_Y(cursor);
		setMin1(cursor);
		setMin5(cursor);
		setMin15(cursor);
		setMin30(cursor);
		setMin60(cursor);
		setDay(cursor);
		setWeek(cursor);
		setMonth(cursor);
		setQuarter(cursor);
		setYear(cursor);
	}

	public void set(Stock stock_X, Stock stock_Y) {
		setSE_X(stock_X.getSE());
		setCode_X(stock_X.getCode());
		setName_X(stock_X.getName());

		setSE_Y(stock_Y.getSE());
		setCode_Y(stock_Y.getCode());
		setName_Y(stock_Y.getName());
	}

	public String getSE_X() {
		return mSE_X;
	}

	public void setSE_X(String se_x) {
		mSE_X = se_x;
	}

	void setSE_X(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setSE_X(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_SE_X)));
	}

	public String getCode_X() {
		return mCode_X;
	}

	public void setCode_X(String code_x) {
		mCode_X = code_x;
	}

	void setCode_X(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setCode_X(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_CODE_X)));
	}

	public String getName_X() {
		return mName_X;
	}

	public void setName_X(String name_x) {
		mName_X = name_x;
	}

	void setName_X(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setName_X(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_NAME_X)));
	}

	public String getSE_Y() {
		return mSE_Y;
	}

	public void setSE_Y(String se_y) {
		mSE_Y = se_y;
	}

	void setSE_Y(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setSE_Y(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_SE_Y)));
	}

	public String getCode_Y() {
		return mCode_Y;
	}

	public void setCode_Y(String code_y) {
		mCode_Y = code_y;
	}

	void setCode_Y(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setCode_Y(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_CODE_Y)));
	}

	public String getName_Y() {
		return mName_Y;
	}

	public void setName_Y(String name_y) {
		mName_Y = name_y;
	}

	void setName_Y(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setName_Y(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_NAME_Y)));
	}

	double getMin1() {
		return mMin1;
	}

	void setMin1(double value) {
		mMin1 = value;
	}

	void setMin1(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setMin1(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_MIN1)));
	}

	double getMin5() {
		return mMin5;
	}

	void setMin5(double value) {
		mMin5 = value;
	}

	void setMin5(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setMin5(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_MIN5)));
	}

	double getMin15() {
		return mMin15;
	}

	void setMin15(double value) {
		mMin15 = value;
	}

	void setMin15(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setMin15(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_MIN15)));
	}

	double getMin30() {
		return mMin30;
	}

	void setMin30(double value) {
		mMin30 = value;
	}

	void setMin30(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setMin30(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_MIN30)));
	}

	double getMin60() {
		return mMin60;
	}

	void setMin60(double value) {
		mMin60 = value;
	}

	void setMin60(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setMin60(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_MIN60)));
	}

	double getDay() {
		return mDay;
	}

	void setDay(double value) {
		mDay = value;
	}

	void setDay(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDay(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DAY)));
	}

	double getWeek() {
		return mWeek;
	}

	void setWeek(double value) {
		mWeek = value;
	}

	void setWeek(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setWeek(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_WEEK)));
	}

	double getMonth() {
		return mMonth;
	}

	void setMonth(double value) {
		mMonth = value;
	}

	void setMonth(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setMonth(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_MONTH)));
	}

	double getQuarter() {
		return mQuarter;
	}

	void setQuarter(double value) {
		mQuarter = value;
	}

	void setQuarter(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setQuarter(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_QUARTER)));
	}

	double getYear() {
		return mYear;
	}

	void setYear(double value) {
		mYear = value;
	}

	void setYear(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setYear(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_YEAR)));
	}

	public double getValue(String period) {
		double value = 0;

		if (period.equals(Constants.PERIOD_MIN1)) {
			value = getMin1();
		} else if (period.equals(Constants.PERIOD_MIN5)) {
			value = getMin5();
		} else if (period.equals(Constants.PERIOD_MIN15)) {
			value = getMin15();
		} else if (period.equals(Constants.PERIOD_MIN30)) {
			value = getMin30();
		} else if (period.equals(Constants.PERIOD_MIN60)) {
			value = getMin60();
		} else if (period.equals(Constants.PERIOD_DAY)) {
			value = getDay();
		} else if (period.equals(Constants.PERIOD_WEEK)) {
			value = getWeek();
		} else if (period.equals(Constants.PERIOD_MONTH)) {
			value = getMonth();
		} else if (period.equals(Constants.PERIOD_QUARTER)) {
			value = getQuarter();
		} else if (period.equals(Constants.PERIOD_YEAR)) {
			value = getYear();
		}

		return value;
	}

	public void setValue(String period, double value) {
		if (period.equals(Constants.PERIOD_MIN1)) {
			setMin1(value);
		} else if (period.equals(Constants.PERIOD_MIN5)) {
			setMin5(value);
		} else if (period.equals(Constants.PERIOD_MIN15)) {
			setMin15(value);
		} else if (period.equals(Constants.PERIOD_MIN30)) {
			setMin30(value);
		} else if (period.equals(Constants.PERIOD_MIN60)) {
			setMin60(value);
		} else if (period.equals(Constants.PERIOD_DAY)) {
			setDay(value);
		} else if (period.equals(Constants.PERIOD_WEEK)) {
			setWeek(value);
		} else if (period.equals(Constants.PERIOD_MONTH)) {
			setMonth(value);
		} else if (period.equals(Constants.PERIOD_QUARTER)) {
			setQuarter(value);
		} else if (period.equals(Constants.PERIOD_YEAR)) {
			setYear(value);
		}
	}
}
