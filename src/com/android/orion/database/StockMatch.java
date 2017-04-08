package com.android.orion.database;

import com.android.orion.Constants;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

public class StockMatch extends DatabaseTable {
	private String mSE_X;
	private String mCode_X;
	private String mName_X;
	private String mSE_Y;
	private String mCode_Y;
	private String mName_Y;
	private double mValue1Min;
	private double mValue5Min;
	private double mValue15Min;
	private double mValue30Min;
	private double mValue60Min;
	private double mValueDay;
	private double mValueWeek;
	private double mValueMonth;
	private double mValueQuarter;
	private double mValueYear;

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
		mValue1Min = 0;
		mValue5Min = 0;
		mValue15Min = 0;
		mValue30Min = 0;
		mValue60Min = 0;
		mValueDay = 0;
		mValueWeek = 0;
		mValueMonth = 0;
		mValueQuarter = 0;
		mValueYear = 0;
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
		contentValues.put(DatabaseContract.StockMatch.COLUMN_VALUE_1MIN,
				mValue1Min);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_VALUE_5MIN,
				mValue5Min);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_VALUE_15MIN,
				mValue15Min);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_VALUE_30MIN,
				mValue30Min);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_VALUE_60MIN,
				mValue60Min);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_VALUE_DAY, mValueDay);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_VALUE_WEEK,
				mValueWeek);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_VALUE_MONTH,
				mValueMonth);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_VALUE_QUARTER,
				mValueQuarter);
		contentValues.put(DatabaseContract.StockMatch.COLUMN_VALUE_YEAR,
				mValueYear);

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
		setValue1Min(stockMatch.mValue1Min);
		setValue5Min(stockMatch.mValue5Min);
		setValue15Min(stockMatch.mValue15Min);
		setValue30Min(stockMatch.mValue30Min);
		setValue60Min(stockMatch.mValue60Min);
		setValueDay(stockMatch.mValueDay);
		setValueWeek(stockMatch.mValueWeek);
		setValueMonth(stockMatch.mValueMonth);
		setValueQuarter(stockMatch.mValueQuarter);
		setValueYear(stockMatch.mValueYear);
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
		setValue1Min(cursor);
		setValue5Min(cursor);
		setValue15Min(cursor);
		setValue30Min(cursor);
		setValue60Min(cursor);
		setValueDay(cursor);
		setValueWeek(cursor);
		setValueMonth(cursor);
		setValueQuarter(cursor);
		setValueYear(cursor);
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

	double getValue1Min() {
		return mValue1Min;
	}

	void setValue1Min(double value) {
		mValue1Min = value;
	}

	void setValue1Min(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setValue1Min(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_VALUE_1MIN)));
	}

	double getValue5Min() {
		return mValue5Min;
	}

	void setValue5Min(double value) {
		mValue5Min = value;
	}

	void setValue5Min(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setValue5Min(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_VALUE_5MIN)));
	}

	double getValue15Min() {
		return mValue15Min;
	}

	void setValue15Min(double value) {
		mValue15Min = value;
	}

	void setValue15Min(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setValue15Min(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_VALUE_15MIN)));
	}

	double getValue30Min() {
		return mValue30Min;
	}

	void setValue30Min(double value) {
		mValue30Min = value;
	}

	void setValue30Min(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setValue30Min(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_VALUE_30MIN)));
	}

	double getValue60Min() {
		return mValue60Min;
	}

	void setValue60Min(double value) {
		mValue60Min = value;
	}

	void setValue60Min(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setValue60Min(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_VALUE_60MIN)));
	}

	double getValueDay() {
		return mValueDay;
	}

	void setValueDay(double value) {
		mValueDay = value;
	}

	void setValueDay(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setValueDay(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_VALUE_DAY)));
	}

	double getValueWeek() {
		return mValueWeek;
	}

	void setValueWeek(double value) {
		mValueWeek = value;
	}

	void setValueWeek(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setValueWeek(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_VALUE_WEEK)));
	}

	double getValueMonth() {
		return mValueMonth;
	}

	void setValueMonth(double value) {
		mValueMonth = value;
	}

	void setValueMonth(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setValueMonth(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_VALUE_MONTH)));
	}

	double getValueQuarter() {
		return mValueQuarter;
	}

	void setValueQuarter(double value) {
		mValueQuarter = value;
	}

	void setValueQuarter(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setValueQuarter(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_VALUE_QUARTER)));
	}

	double getValueYear() {
		return mValueYear;
	}

	void setValueYear(double value) {
		mValueYear = value;
	}

	void setValueYear(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setValueYear(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.StockMatch.COLUMN_VALUE_YEAR)));
	}

	public double getValue(String period) {
		double value = 0;

		if (period.equals(Constants.PERIOD_1MIN)) {
			value = getValue1Min();
		} else if (period.equals(Constants.PERIOD_5MIN)) {
			value = getValue5Min();
		} else if (period.equals(Constants.PERIOD_15MIN)) {
			value = getValue15Min();
		} else if (period.equals(Constants.PERIOD_30MIN)) {
			value = getValue30Min();
		} else if (period.equals(Constants.PERIOD_60MIN)) {
			value = getValue60Min();
		} else if (period.equals(Constants.PERIOD_DAY)) {
			value = getValueDay();
		} else if (period.equals(Constants.PERIOD_WEEK)) {
			value = getValueWeek();
		} else if (period.equals(Constants.PERIOD_MONTH)) {
			value = getValueMonth();
		} else if (period.equals(Constants.PERIOD_QUARTER)) {
			value = getValueQuarter();
		} else if (period.equals(Constants.PERIOD_YEAR)) {
			value = getValueYear();
		}

		return value;
	}

	public void setValue(String period, double value) {
		if (period.equals(Constants.PERIOD_1MIN)) {
			setValue1Min(value);
		} else if (period.equals(Constants.PERIOD_5MIN)) {
			setValue5Min(value);
		} else if (period.equals(Constants.PERIOD_15MIN)) {
			setValue15Min(value);
		} else if (period.equals(Constants.PERIOD_30MIN)) {
			setValue30Min(value);
		} else if (period.equals(Constants.PERIOD_60MIN)) {
			setValue60Min(value);
		} else if (period.equals(Constants.PERIOD_DAY)) {
			setValueDay(value);
		} else if (period.equals(Constants.PERIOD_WEEK)) {
			setValueWeek(value);
		} else if (period.equals(Constants.PERIOD_MONTH)) {
			setValueMonth(value);
		} else if (period.equals(Constants.PERIOD_QUARTER)) {
			setValueQuarter(value);
		} else if (period.equals(Constants.PERIOD_YEAR)) {
			setValueYear(value);
		}
	}
}
