package com.android.orion.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.Constants;
import com.android.orion.utility.Utility;

public class Stock extends StockBase {
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
	private String mActionMin1;
	private String mActionMin5;
	private String mActionMin15;
	private String mActionMin30;
	private String mActionMin60;
	private String mActionDay;
	private String mActionWeek;
	private String mActionMonth;
	private String mActionQuarter;
	private String mActionYear;
	private String mOperation;
	private long mHold;
	private double mCost;
	private double mProfit;
	private double mPE;
	private double mPB;

	public ArrayList<StockData> mStockDataListMin1 = new ArrayList<StockData>();
	public ArrayList<StockData> mStockDataListMin5 = new ArrayList<StockData>();
	public ArrayList<StockData> mStockDataListMin15 = new ArrayList<StockData>();
	public ArrayList<StockData> mStockDataListMin30 = new ArrayList<StockData>();
	public ArrayList<StockData> mStockDataListMin60 = new ArrayList<StockData>();
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
		mActionMin1 = "";
		mActionMin5 = "";
		mActionMin15 = "";
		mActionMin30 = "";
		mActionMin60 = "";
		mActionDay = "";
		mActionWeek = "";
		mActionMonth = "";
		mActionQuarter = "";
		mActionYear = "";
		mOperation = "";
		mHold = 0;
		mCost = 0;
		mProfit = 0;
		mPE = 0;
		mPB = 0;
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
		contentValues.put(DatabaseContract.COLUMN_MIN1, mActionMin1);
		contentValues.put(DatabaseContract.COLUMN_MIN5, mActionMin5);
		contentValues.put(DatabaseContract.COLUMN_MIN15, mActionMin15);
		contentValues.put(DatabaseContract.COLUMN_MIN30, mActionMin30);
		contentValues.put(DatabaseContract.COLUMN_MIN60, mActionMin60);
		contentValues.put(DatabaseContract.COLUMN_DAY, mActionDay);
		contentValues.put(DatabaseContract.COLUMN_WEEK, mActionWeek);
		contentValues.put(DatabaseContract.COLUMN_MONTH, mActionMonth);
		contentValues.put(DatabaseContract.COLUMN_QUARTER, mActionQuarter);
		contentValues.put(DatabaseContract.COLUMN_YEAR, mActionYear);
		contentValues.put(DatabaseContract.COLUMN_OPERATION, mOperation);
		contentValues.put(DatabaseContract.COLUMN_HOLD, mHold);
		contentValues.put(DatabaseContract.COLUMN_COST, mCost);
		contentValues.put(DatabaseContract.COLUMN_PROFIT, mProfit);
		contentValues.put(DatabaseContract.COLUMN_PE, mPE);
		contentValues.put(DatabaseContract.COLUMN_PB, mPB);

		return contentValues;
	}

	public ContentValues getContentValuesAnalyze(String period) {
		ContentValues contentValues = new ContentValues();

		super.getContentValues(contentValues);

		if (period.equals(Constants.PERIOD_MIN1)) {
			contentValues.put(DatabaseContract.COLUMN_MIN1, mActionMin1);
		} else if (period.equals(Constants.PERIOD_MIN5)) {
			contentValues.put(DatabaseContract.COLUMN_MIN5, mActionMin5);
		} else if (period.equals(Constants.PERIOD_MIN15)) {
			contentValues.put(DatabaseContract.COLUMN_MIN15, mActionMin15);
		} else if (period.equals(Constants.PERIOD_MIN30)) {
			contentValues.put(DatabaseContract.COLUMN_MIN30, mActionMin30);
		} else if (period.equals(Constants.PERIOD_MIN60)) {
			contentValues.put(DatabaseContract.COLUMN_MIN60, mActionMin60);
		} else if (period.equals(Constants.PERIOD_DAY)) {
			contentValues.put(DatabaseContract.COLUMN_DAY, mActionDay);
		} else if (period.equals(Constants.PERIOD_WEEK)) {
			contentValues.put(DatabaseContract.COLUMN_WEEK, mActionWeek);
		} else if (period.equals(Constants.PERIOD_MONTH)) {
			contentValues.put(DatabaseContract.COLUMN_MONTH, mActionMonth);
		} else if (period.equals(Constants.PERIOD_QUARTER)) {
			contentValues.put(DatabaseContract.COLUMN_QUARTER, mActionQuarter);
		} else if (period.equals(Constants.PERIOD_YEAR)) {
			contentValues.put(DatabaseContract.COLUMN_YEAR, mActionYear);
		}
		
		contentValues.put(DatabaseContract.COLUMN_PB, mPB);
		contentValues.put(DatabaseContract.COLUMN_PE, mPE);

		return contentValues;
	}

	public ContentValues getContentValuesDividendYield() {
		ContentValues contentValues = new ContentValues();

		super.getContentValues(contentValues);


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
		setActionMin1(stock.mActionMin1);
		setActionMin5(stock.mActionMin5);
		setActionMin15(stock.mActionMin15);
		setActionMin30(stock.mActionMin30);
		setActionMin60(stock.mActionMin60);
		setActionDay(stock.mActionDay);
		setActionWeek(stock.mActionWeek);
		setActionMonth(stock.mActionMonth);
		setActionQuarter(stock.mActionQuarter);
		setActionYear(stock.mActionYear);
		setOperation(stock.mOperation);
		setHold(stock.mHold);
		setCost(stock.mCost);
		setProfit(stock.mProfit);
		setPE(stock.mPE);
		setPB(stock.mPB);
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
		setActionMin1(cursor);
		setActionMin5(cursor);
		setActionMin15(cursor);
		setActionMin30(cursor);
		setActionMin60(cursor);
		setActionDay(cursor);
		setActionWeek(cursor);
		setActionMonth(cursor);
		setActionQuarter(cursor);
		setActionYear(cursor);
		setOperation(cursor);
		setHold(cursor);
		setCost(cursor);
		setProfit(cursor);
		setPE(cursor);
		setPB(cursor);
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

	String getActionMin1() {
		return mActionMin1;
	}

	void setActionMin1(String action) {
		mActionMin1 = action;
	}

	void setActionMin1(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setActionMin1(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_MIN1)));
	}

	String getActionMin5() {
		return mActionMin5;
	}

	void setActionMin5(String action) {
		mActionMin5 = action;
	}

	void setActionMin5(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setActionMin5(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_MIN5)));
	}

	String getActionMin15() {
		return mActionMin15;
	}

	void setActionMin15(String action) {
		mActionMin15 = action;
	}

	void setActionMin15(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setActionMin15(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_MIN15)));
	}

	String getActionMin30() {
		return mActionMin30;
	}

	void setActionMin30(String action) {
		mActionMin30 = action;
	}

	void setActionMin30(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setActionMin30(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_MIN30)));
	}

	String getActionMin60() {
		return mActionMin60;
	}

	void setActionMin60(String action) {
		mActionMin60 = action;
	}

	void setActionMin60(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setActionMin60(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_MIN60)));
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
				.getColumnIndex(DatabaseContract.COLUMN_DAY)));
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
				.getColumnIndex(DatabaseContract.COLUMN_WEEK)));
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
				.getColumnIndex(DatabaseContract.COLUMN_MONTH)));
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
				.getColumnIndex(DatabaseContract.COLUMN_QUARTER)));
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
				.getColumnIndex(DatabaseContract.COLUMN_YEAR)));
	}

	public String getOperation() {
		return mOperation;
	}

	public void setOperation(String operation) {
		mOperation = operation;
	}

	void setOperation(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setOperation(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_OPERATION)));
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

	public double getCost() {
		return mCost;
	}

	public void setCost(double cost) {
		mCost = cost;
	}

	void setCost(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setCost(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_COST)));
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

	public double getPE() {
		return mPE;
	}

	public void setPE(double pe) {
		mPE = pe;
	}

	void setPE(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setPE(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PE)));
	}

	public double getPB() {
		return mPB;
	}

	public void setPB(double pb) {
		mPB = pb;
	}

	void setPB(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setPB(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PB)));
	}

	public String getAction(String period) {
		String action = "";

		if (period.equals(Constants.PERIOD_MIN1)) {
			action = getActionMin1();
		} else if (period.equals(Constants.PERIOD_MIN5)) {
			action = getActionMin5();
		} else if (period.equals(Constants.PERIOD_MIN15)) {
			action = getActionMin15();
		} else if (period.equals(Constants.PERIOD_MIN30)) {
			action = getActionMin30();
		} else if (period.equals(Constants.PERIOD_MIN60)) {
			action = getActionMin60();
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
		if (period.equals(Constants.PERIOD_MIN1)) {
			setActionMin1(action);
		} else if (period.equals(Constants.PERIOD_MIN5)) {
			setActionMin5(action);
		} else if (period.equals(Constants.PERIOD_MIN15)) {
			setActionMin15(action);
		} else if (period.equals(Constants.PERIOD_MIN30)) {
			setActionMin30(action);
		} else if (period.equals(Constants.PERIOD_MIN60)) {
			setActionMin60(action);
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

	public void setupPE(double value) {
		if ((value == 0) || (value < 0)) {
			return;
		}

		mPE = 100.0 * value / mPrice;
		mPE = Utility.Round(mPE, Constants.DOUBLE_FIXED_DECIMAL);
	}
	
	public void setupPB(double value) {
		if (value == 0) {
			return;
		}

		mPB = mPrice / value;
		mPB = Utility.Round(mPB, Constants.DOUBLE_FIXED_DECIMAL);
	}
	
	public void setupCost(double value) {
		if (mHold == 0) {
			return;
		}

		mCost = value / mHold;
		mCost = Utility.Round(mCost, Constants.DOUBLE_FIXED_DECIMAL);
	}

	public void setupDividendYield() {
		if (mPrice == 0) {
			return;
		}

		mYield = 100.0 * mDividend / 10.0 / mPrice;
		mYield = Utility.Round(mYield,
				Constants.DOUBLE_FIXED_DECIMAL);
	}
}
