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
	private String mAction1Min;
	private String mAction5Min;
	private String mAction15Min;
	private String mAction30Min;
	private String mAction60Min;
	private String mActionDay;
	private String mActionWeek;
	private String mActionMonth;
	private String mActionQuarter;
	private String mActionYear;

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
		mAction1Min = "";
		mAction5Min = "";
		mAction15Min = "";
		mAction30Min = "";
		mAction60Min = "";
		mActionDay = "";
		mActionWeek = "";
		mActionMonth = "";
		mActionQuarter = "";
		mActionYear = "";
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
		contentValues.put(DatabaseContract.COLUMN_ACTION_1MIN,
				mAction1Min);
		contentValues.put(DatabaseContract.COLUMN_ACTION_5MIN,
				mAction5Min);
		contentValues.put(DatabaseContract.COLUMN_ACTION_15MIN,
				mAction15Min);
		contentValues.put(DatabaseContract.COLUMN_ACTION_30MIN,
				mAction30Min);
		contentValues.put(DatabaseContract.COLUMN_ACTION_60MIN,
				mAction60Min);
		contentValues.put(DatabaseContract.COLUMN_ACTION_DAY, mActionDay);
		contentValues.put(DatabaseContract.COLUMN_ACTION_WEEK,
				mActionWeek);
		contentValues.put(DatabaseContract.COLUMN_ACTION_MONTH,
				mActionMonth);
		contentValues.put(DatabaseContract.COLUMN_ACTION_QUARTER,
				mActionQuarter);
		contentValues.put(DatabaseContract.COLUMN_ACTION_YEAR,
				mActionYear);

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
		setAction1Min(stockMatch.mAction1Min);
		setAction5Min(stockMatch.mAction5Min);
		setAction15Min(stockMatch.mAction15Min);
		setAction30Min(stockMatch.mAction30Min);
		setAction60Min(stockMatch.mAction60Min);
		setActionDay(stockMatch.mActionDay);
		setActionWeek(stockMatch.mActionWeek);
		setActionMonth(stockMatch.mActionMonth);
		setActionQuarter(stockMatch.mActionQuarter);
		setActionYear(stockMatch.mActionYear);
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
		setAction1Min(cursor);
		setAction5Min(cursor);
		setAction15Min(cursor);
		setAction30Min(cursor);
		setAction60Min(cursor);
		setActionDay(cursor);
		setActionWeek(cursor);
		setActionMonth(cursor);
		setActionQuarter(cursor);
		setActionYear(cursor);
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

	String getAction1Min() {
		return mAction1Min;
	}

	void setAction1Min(String action) {
		mAction1Min = action;
	}

	void setAction1Min(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setAction1Min(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ACTION_1MIN)));
	}

	String getAction5Min() {
		return mAction5Min;
	}

	void setAction5Min(String action) {
		mAction5Min = action;
	}

	void setAction5Min(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setAction5Min(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ACTION_5MIN)));
	}

	String getAction15Min() {
		return mAction15Min;
	}

	void setAction15Min(String action) {
		mAction15Min = action;
	}

	void setAction15Min(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setAction15Min(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ACTION_15MIN)));
	}

	String getAction30Min() {
		return mAction30Min;
	}

	void setAction30Min(String action) {
		mAction30Min = action;
	}

	void setAction30Min(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setAction30Min(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ACTION_30MIN)));
	}

	String getAction60Min() {
		return mAction60Min;
	}

	void setAction60Min(String action) {
		mAction60Min = action;
	}

	void setAction60Min(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setAction60Min(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ACTION_60MIN)));
	}

	String getActionDay() {
		return mActionDay;
	}

	void setActionDay(String action) {
		mActionDay = action;
	}

	void setActionDay(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setActionDay(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ACTION_DAY)));
	}

	String getActionWeek() {
		return mActionWeek;
	}

	void setActionWeek(String action) {
		mActionWeek = action;
	}

	void setActionWeek(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setActionWeek(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ACTION_WEEK)));
	}

	String getActionMonth() {
		return mActionMonth;
	}

	void setActionMonth(String action) {
		mActionMonth = action;
	}

	void setActionMonth(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setActionMonth(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ACTION_MONTH)));
	}

	String getActionQuarter() {
		return mActionQuarter;
	}

	void setActionQuarter(String action) {
		mActionQuarter = action;
	}

	void setActionQuarter(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setActionQuarter(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ACTION_QUARTER)));
	}

	String getActionYear() {
		return mActionYear;
	}

	void setActionYear(String action) {
		mActionYear = action;
	}

	void setActionYear(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setActionYear(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ACTION_YEAR)));
	}

	public String getAction(String period) {
		String action = "";

		if (period.equals(Constants.PERIOD_1MIN)) {
			action = getAction1Min();
		} else if (period.equals(Constants.PERIOD_5MIN)) {
			action = getAction5Min();
		} else if (period.equals(Constants.PERIOD_15MIN)) {
			action = getAction15Min();
		} else if (period.equals(Constants.PERIOD_30MIN)) {
			action = getAction30Min();
		} else if (period.equals(Constants.PERIOD_60MIN)) {
			action = getAction60Min();
		} else if (period.equals(Constants.PERIOD_DAY)) {
			action = getActionDay();
		} else if (period.equals(Constants.PERIOD_WEEK)) {
			action = getActionWeek();
		} else if (period.equals(Constants.PERIOD_MONTH)) {
			action = getActionMonth();
		} else if (period.equals(Constants.PERIOD_QUARTER)) {
			action = getActionQuarter();
		} else if (period.equals(Constants.PERIOD_YEAR)) {
			action = getActionYear();
		}

		return action;
	}

	public void setAction(String period, String action) {
		if (period.equals(Constants.PERIOD_1MIN)) {
			setAction1Min(action);
		} else if (period.equals(Constants.PERIOD_5MIN)) {
			setAction5Min(action);
		} else if (period.equals(Constants.PERIOD_15MIN)) {
			setAction15Min(action);
		} else if (period.equals(Constants.PERIOD_30MIN)) {
			setAction30Min(action);
		} else if (period.equals(Constants.PERIOD_60MIN)) {
			setAction60Min(action);
		} else if (period.equals(Constants.PERIOD_DAY)) {
			setActionDay(action);
		} else if (period.equals(Constants.PERIOD_WEEK)) {
			setActionWeek(action);
		} else if (period.equals(Constants.PERIOD_MONTH)) {
			setActionMonth(action);
		} else if (period.equals(Constants.PERIOD_QUARTER)) {
			setActionQuarter(action);
		} else if (period.equals(Constants.PERIOD_YEAR)) {
			setActionYear(action);
		}
	}
}
