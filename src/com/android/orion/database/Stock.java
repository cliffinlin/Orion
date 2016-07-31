package com.android.orion.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.Constants;

public class Stock extends StockDatabaseTable {
	private String mClasses;
	private String mSE;
	private String mCode;
	private String mName;
	private String mPinyin;
	private String mPinyinFixed;
	private String mMark;
	private double mPrice;
	private double mChange;
	private double mNet;
	private String mVolume;
	private String mValue;
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

	public ArrayList<StockData> mStockDataList1Min = new ArrayList<StockData>();
	public ArrayList<StockData> mStockDataList5Min = new ArrayList<StockData>();
	public ArrayList<StockData> mStockDataList15Min = new ArrayList<StockData>();
	public ArrayList<StockData> mStockDataList30Min = new ArrayList<StockData>();
	public ArrayList<StockData> mStockDataList60Min = new ArrayList<StockData>();
	public ArrayList<StockData> mStockDataListDay = new ArrayList<StockData>();
	public ArrayList<StockData> mStockDataListWeek = new ArrayList<StockData>();
	public ArrayList<StockData> mStockDataListMonth = new ArrayList<StockData>();
	public ArrayList<StockData> mStockDataListQuarter = new ArrayList<StockData>();
	public ArrayList<StockData> mStockDataListYear = new ArrayList<StockData>();

	private Stock next;
	private static final Object sPoolSync = new Object();
	private static Stock sPool;

	public static Stock obtain() {
		synchronized (sPoolSync) {
			if (sPool != null) {
				Stock m = sPool;
				sPool = m.next;
				m.next = null;
				return m;
			}
		}
		return new Stock();
	}

	public Stock() {
		init();
	}

	public Stock(Stock stock) {
		set(stock);
	}

	public Stock(Cursor cursor) {
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

		setTableName(DatabaseContract.Stock.TABLE_NAME);

		mClasses = "";
		mSE = "";
		mCode = "";
		mName = "";
		mPinyin = "";
		mPinyinFixed = "";
		mMark = "";
		mPrice = 0;
		mChange = 0;
		mNet = 0;
		mVolume = "";
		mValue = "";
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

	@Override
	public ContentValues getContentValues(ContentValues contentValues) {
		super.getContentValues(contentValues);

		contentValues.put(DatabaseContract.Stock.COLUMN_CLASSES, mClasses);
		contentValues.put(DatabaseContract.COLUMN_SE, mSE);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		contentValues.put(DatabaseContract.Stock.COLUMN_PINYIN, mPinyin);
		contentValues.put(DatabaseContract.Stock.COLUMN_PINYIN_FIXED,
				mPinyinFixed);
		contentValues.put(DatabaseContract.Stock.COLUMN_MARK, mMark);
		contentValues.put(DatabaseContract.COLUMN_PRICE, mPrice);
		contentValues.put(DatabaseContract.COLUMN_CHANGE, mChange);
		contentValues.put(DatabaseContract.COLUMN_NET, mNet);
		contentValues.put(DatabaseContract.COLUMN_VOLUME, mVolume);
		contentValues.put(DatabaseContract.COLUMN_VALUE, mValue);
		contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_1MIN,
				mAction1Min);
		contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_5MIN,
				mAction5Min);
		contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_15MIN,
				mAction15Min);
		contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_30MIN,
				mAction30Min);
		contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_60MIN,
				mAction60Min);
		contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_DAY, mActionDay);
		contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_WEEK,
				mActionWeek);
		contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_MONTH,
				mActionMonth);
		contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_QUARTER,
				mActionQuarter);
		contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_YEAR,
				mActionYear);

		return contentValues;
	}

	public ContentValues getContentValuesAnalyze(String period) {
		ContentValues contentValues = new ContentValues();

		super.getContentValues(contentValues);

		if (period.equals(Constants.PERIOD_1MIN)) {
			contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_1MIN,
					mAction1Min);
		} else if (period.equals(Constants.PERIOD_5MIN)) {
			contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_5MIN,
					mAction5Min);
		} else if (period.equals(Constants.PERIOD_15MIN)) {
			contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_15MIN,
					mAction15Min);
		} else if (period.equals(Constants.PERIOD_30MIN)) {
			contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_30MIN,
					mAction30Min);
		} else if (period.equals(Constants.PERIOD_60MIN)) {
			contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_60MIN,
					mAction60Min);
		} else if (period.equals(Constants.PERIOD_DAY)) {
			contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_DAY,
					mActionDay);
		} else if (period.equals(Constants.PERIOD_WEEK)) {
			contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_WEEK,
					mActionWeek);
		} else if (period.equals(Constants.PERIOD_MONTH)) {
			contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_MONTH,
					mActionMonth);
		} else if (period.equals(Constants.PERIOD_QUARTER)) {
			contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_QUARTER,
					mActionQuarter);
		} else if (period.equals(Constants.PERIOD_YEAR)) {
			contentValues.put(DatabaseContract.Stock.COLUMN_ACTION_YEAR,
					mActionYear);
		}

		return contentValues;
	}

	public void set(Stock stock) {
		if (stock == null) {
			return;
		}

		init();

		super.set(stock);

		setClasses(stock.mClasses);
		setSE(stock.mSE);
		setCode(stock.mCode);
		setName(stock.mName);
		setPinyin(stock.mPinyin);
		setPinyinFixed(stock.mPinyinFixed);
		setMark(stock.mMark);
		setPrice(stock.mPrice);
		setChange(stock.mChange);
		setNet(stock.mNet);
		setVolume(stock.mVolume);
		setValue(stock.mValue);
		setAction1Min(stock.mAction1Min);
		setAction5Min(stock.mAction5Min);
		setAction15Min(stock.mAction15Min);
		setAction30Min(stock.mAction30Min);
		setAction60Min(stock.mAction60Min);
		setActionDay(stock.mActionDay);
		setActionWeek(stock.mActionWeek);
		setActionMonth(stock.mActionMonth);
		setActionQuarter(stock.mActionQuarter);
		setActionYear(stock.mActionYear);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		super.set(cursor);

		setClasses(cursor);
		setSE(cursor);
		setCode(cursor);
		setName(cursor);
		setPinyin(cursor);
		setPinyinFiexd(cursor);
		setMark(cursor);
		setPrice(cursor);
		setChange(cursor);
		setNet(cursor);
		setVolume(cursor);
		setValue(cursor);
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

	String getClases() {
		return mClasses;
	}

	public void setClasses(String classes) {
		mClasses = classes;
	}

	void setClasses(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setClasses(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.Stock.COLUMN_CLASSES)));
	}

	public String getSE() {
		return mSE;
	}

	public void setSE(String se) {
		mSE = se;
	}

	public void setSE(Cursor cursor) {
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

	public void setCode(Cursor cursor) {
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

	public void setName(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setName(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NAME)));
	}

	String getPinyin() {
		return mPinyin;
	}

	public void setPinyin(String pinyin) {
		mPinyin = pinyin;
	}

	void setPinyin(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setPinyin(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.Stock.COLUMN_PINYIN)));
	}

	String getPinyinFixed() {
		return mPinyinFixed;
	}

	public void setPinyinFixed(String pinyinFixed) {
		mPinyinFixed = pinyinFixed;
	}

	void setPinyinFiexd(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setPinyinFixed(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.Stock.COLUMN_PINYIN_FIXED)));
	}

	public String getMark() {
		return mMark;
	}

	public void setMark(String mark) {
		mMark = mark;
	}

	void setMark(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setMark(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.Stock.COLUMN_MARK)));
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

	public double getChange() {
		return mChange;
	}

	public void setChange(double change) {
		mChange = change;
	}

	void setChange(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setChange(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_CHANGE)));
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

	public String getVolume() {
		return mVolume;
	}

	public void setVolume(String volume) {
		mVolume = volume;
	}

	void setVolume(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setVolume(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VOLUME)));
	}

	public String getValue() {
		return mValue;
	}

	public void setValue(String value) {
		mValue = value;
	}

	void setValue(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setValue(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VALUE)));
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
				.getColumnIndex(DatabaseContract.Stock.COLUMN_ACTION_1MIN)));
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
				.getColumnIndex(DatabaseContract.Stock.COLUMN_ACTION_5MIN)));
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
				.getColumnIndex(DatabaseContract.Stock.COLUMN_ACTION_15MIN)));
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
				.getColumnIndex(DatabaseContract.Stock.COLUMN_ACTION_30MIN)));
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
				.getColumnIndex(DatabaseContract.Stock.COLUMN_ACTION_60MIN)));
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
				.getColumnIndex(DatabaseContract.Stock.COLUMN_ACTION_DAY)));
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
				.getColumnIndex(DatabaseContract.Stock.COLUMN_ACTION_WEEK)));
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
				.getColumnIndex(DatabaseContract.Stock.COLUMN_ACTION_MONTH)));
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
				.getColumnIndex(DatabaseContract.Stock.COLUMN_ACTION_QUARTER)));
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
				.getColumnIndex(DatabaseContract.Stock.COLUMN_ACTION_YEAR)));
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
