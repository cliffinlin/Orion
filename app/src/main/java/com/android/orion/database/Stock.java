package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.Calendar;

public class Stock extends DatabaseTable {

	public static final String CLASS_A = "A";
	public static final String CLASS_INDEX = "I";

	public static final String SE_SH = "sh";
	public static final String SE_SZ = "sz";

	public static final String STATUS_SUSPENSION = "--";

	public static final long INVALID_ID = 0;

	public static final int FLAG_NONE = 0;
	public static final int FLAG_FAVORITE = 1 << 0;

	private int mFlag;
	private String mClasses;
	private String mSE;
	private String mCode;
	private String mName;
	private String mPinyin;

	private double mPrice;
	private double mChange;
	private double mNet;
	private long mVolume;
	private long mValue;

	private String mDate;
	private String mTime;
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
	private String mTrend;

	private double mThreshold;
	private long mQuantVolume;
	private double mQuantProfit;
	private double mQuantProfitMargin;

	private String mOperate;
	private long mHold;
	private double mCost;
	private double mProfit;
	private double mBonus;
	private double mValuation;
	private double mTotalShare;
	private double mMarketValue;
	private double mTotalAssets;
	private double mTotalLongTermLiabilities;
	private double mMainBusinessIncome;
	private double mMainBusinessIncomeInYear;
	private double mDebtToNetAssetsRatio;
	private double mBookValuePerShare;
	private double mCashFlowPerShare;
	private double mNetProfit;
	private double mNetProfitInYear;
	private double mNetProfitMargin;
	private double mNetProfitPerShare;
	private double mNetProfitPerShareInYear;
	private double mRoi;
	private double mRoe;
	private double mPe;
	private double mPb;
	private double mRate;
	private double mDividend;
	private double mYield;
	private double mDividendRatio;
	private String mRDate;
	private String mStatus;
	private String mRealTimeModified;
	private String mInformationModified;

	private ArrayList<StockData> mStockDataListMin1 = new ArrayList<StockData>();
	private ArrayList<StockData> mStockDataListMin5 = new ArrayList<StockData>();
	private ArrayList<StockData> mStockDataListMin15 = new ArrayList<StockData>();
	private ArrayList<StockData> mStockDataListMin30 = new ArrayList<StockData>();
	private ArrayList<StockData> mStockDataListMin60 = new ArrayList<StockData>();
	private ArrayList<StockData> mStockDataListDay = new ArrayList<StockData>();
	private ArrayList<StockData> mStockDataListWeek = new ArrayList<StockData>();
	private ArrayList<StockData> mStockDataListMonth = new ArrayList<StockData>();
	private ArrayList<StockData> mStockDataListQuarter = new ArrayList<StockData>();
	private ArrayList<StockData> mStockDataListYear = new ArrayList<StockData>();

	private ArrayList<StockData> mDrawVertexListMin1 = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawVertexListMin5 = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawVertexListMin15 = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawVertexListMin30 = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawVertexListMin60 = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawVertexListDay = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawVertexListWeek = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawVertexListMonth = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawVertexListQuarter = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawVertexListYear = new ArrayList<StockData>();

	private ArrayList<StockData> mDrawDataListMin1 = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawDataListMin5 = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawDataListMin15 = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawDataListMin30 = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawDataListMin60 = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawDataListDay = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawDataListWeek = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawDataListMonth = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawDataListQuarter = new ArrayList<StockData>();
	private ArrayList<StockData> mDrawDataListYear = new ArrayList<StockData>();

	private ArrayList<StockData> mStrokeVertexListMin1 = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeVertexListMin5 = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeVertexListMin15 = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeVertexListMin30 = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeVertexListMin60 = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeVertexListDay = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeVertexListWeek = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeVertexListMonth = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeVertexListQuarter = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeVertexListYear = new ArrayList<StockData>();

	private ArrayList<StockData> mStrokeDataListMin1 = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeDataListMin5 = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeDataListMin15 = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeDataListMin30 = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeDataListMin60 = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeDataListDay = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeDataListWeek = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeDataListMonth = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeDataListQuarter = new ArrayList<StockData>();
	private ArrayList<StockData> mStrokeDataListYear = new ArrayList<StockData>();

	private ArrayList<StockData> mSegmentVertexListMin1 = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentVertexListMin5 = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentVertexListMin15 = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentVertexListMin30 = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentVertexListMin60 = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentVertexListDay = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentVertexListWeek = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentVertexListMonth = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentVertexListQuarter = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentVertexListYear = new ArrayList<StockData>();

	private ArrayList<StockData> mSegmentDataListMin1 = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentDataListMin5 = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentDataListMin15 = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentDataListMin30 = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentDataListMin60 = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentDataListDay = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentDataListWeek = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentDataListMonth = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentDataListQuarter = new ArrayList<StockData>();
	private ArrayList<StockData> mSegmentDataListYear = new ArrayList<StockData>();

	private ArrayList<StockData> mLineVertexListMin1 = new ArrayList<StockData>();
	private ArrayList<StockData> mLineVertexListMin5 = new ArrayList<StockData>();
	private ArrayList<StockData> mLineVertexListMin15 = new ArrayList<StockData>();
	private ArrayList<StockData> mLineVertexListMin30 = new ArrayList<StockData>();
	private ArrayList<StockData> mLineVertexListMin60 = new ArrayList<StockData>();
	private ArrayList<StockData> mLineVertexListDay = new ArrayList<StockData>();
	private ArrayList<StockData> mLineVertexListWeek = new ArrayList<StockData>();
	private ArrayList<StockData> mLineVertexListMonth = new ArrayList<StockData>();
	private ArrayList<StockData> mLineVertexListQuarter = new ArrayList<StockData>();
	private ArrayList<StockData> mLineVertexListYear = new ArrayList<StockData>();

	private ArrayList<StockData> mLineDataListMin1 = new ArrayList<StockData>();
	private ArrayList<StockData> mLineDataListMin5 = new ArrayList<StockData>();
	private ArrayList<StockData> mLineDataListMin15 = new ArrayList<StockData>();
	private ArrayList<StockData> mLineDataListMin30 = new ArrayList<StockData>();
	private ArrayList<StockData> mLineDataListMin60 = new ArrayList<StockData>();
	private ArrayList<StockData> mLineDataListDay = new ArrayList<StockData>();
	private ArrayList<StockData> mLineDataListWeek = new ArrayList<StockData>();
	private ArrayList<StockData> mLineDataListMonth = new ArrayList<StockData>();
	private ArrayList<StockData> mLineDataListQuarter = new ArrayList<StockData>();
	private ArrayList<StockData> mLineDataListYear = new ArrayList<StockData>();

	private ArrayList<StockData> mOutlineVertexListMin1 = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineVertexListMin5 = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineVertexListMin15 = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineVertexListMin30 = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineVertexListMin60 = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineVertexListDay = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineVertexListWeek = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineVertexListMonth = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineVertexListQuarter = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineVertexListYear = new ArrayList<StockData>();

	private ArrayList<StockData> mOutlineDataListMin1 = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineDataListMin5 = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineDataListMin15 = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineDataListMin30 = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineDataListMin60 = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineDataListDay = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineDataListWeek = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineDataListMonth = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineDataListQuarter = new ArrayList<StockData>();
	private ArrayList<StockData> mOutlineDataListYear = new ArrayList<StockData>();

	public Stock() {
		init();
	}

	public Stock(Stock stock) {
		set(stock);
	}

	public Stock(Cursor cursor) {
		set(cursor);
	}

	public void init() {
		super.init();

		setTableName(DatabaseContract.Stock.TABLE_NAME);

		mFlag = 0;
		mClasses = "";
		mSE = "";
		mCode = "";
		mName = "";
		mPinyin = "";

		mOperate = "";
		mHold = 0;

		reset();
	}

	public void reset() {
		super.reset();

		mPrice = 0;
		mChange = 0;
		mNet = 0;
		mVolume = 0;
		mValue = 0;

		mDate = "";
		mTime = "";
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
		mTrend = "";

		mThreshold = 0;
		mQuantVolume = 0;
		mQuantProfit = 0;
		mQuantProfitMargin = 0;

		mCost = 0;
		mProfit = 0;
		mBonus = 0;
		mValuation = 0;
		mTotalShare = 0;
		mMarketValue = 0;
		mTotalAssets = 0;
		mTotalLongTermLiabilities = 0;
		mMainBusinessIncome = 0;
		mMainBusinessIncomeInYear = 0;
		mDebtToNetAssetsRatio = 0;
		mBookValuePerShare = 0;
		mCashFlowPerShare = 0;
		mNetProfit = 0;
		mNetProfitInYear = 0;
		mNetProfitMargin = 0;
		mNetProfitPerShare = 0;
		mNetProfitPerShareInYear = 0;
		mRoi = 0;
		mRoe = 0;
		mPe = 0;
		mPb = 0;
		mRate = 0;
		mDividend = 0;
		mYield = 0;
		mDividendRatio = 0;
		mRDate = "";
		mStatus = "";
		mRealTimeModified = "";
		mInformationModified = "";
	}

	@Override
	public ContentValues getContentValues(ContentValues contentValues) {
		super.getContentValues(contentValues);

//		contentValues.put(DatabaseContract.COLUMN_FLAG, mFlag);

		contentValues.put(DatabaseContract.COLUMN_CLASSES, mClasses);
		contentValues.put(DatabaseContract.COLUMN_SE, mSE);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		contentValues.put(DatabaseContract.COLUMN_PINYIN, mPinyin);

		contentValues.put(DatabaseContract.COLUMN_PRICE, mPrice);
		contentValues.put(DatabaseContract.COLUMN_CHANGE, mChange);
		contentValues.put(DatabaseContract.COLUMN_NET, mNet);
		contentValues.put(DatabaseContract.COLUMN_VOLUME, mVolume);
		contentValues.put(DatabaseContract.COLUMN_VALUE, mValue);

		contentValues.put(DatabaseContract.COLUMN_DATE, mDate);
		contentValues.put(DatabaseContract.COLUMN_TIME, mTime);
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
		contentValues.put(DatabaseContract.COLUMN_TREND, mTrend);
		contentValues.put(DatabaseContract.COLUMN_QUANT_PROFIT, mQuantProfit);
		contentValues.put(DatabaseContract.COLUMN_QUANT_PROFIT_MARGIN, mQuantProfitMargin);

		contentValues.put(DatabaseContract.COLUMN_HOLD, mHold);
		contentValues.put(DatabaseContract.COLUMN_COST, mCost);
		contentValues.put(DatabaseContract.COLUMN_PROFIT, mProfit);
		contentValues.put(DatabaseContract.COLUMN_BONUS, mBonus);
		contentValues.put(DatabaseContract.COLUMN_VALUATION, mValuation);
		contentValues.put(DatabaseContract.COLUMN_TOTAL_SHARE, mTotalShare);
		contentValues.put(DatabaseContract.COLUMN_MARKET_VALUE, mMarketValue);
		contentValues.put(DatabaseContract.COLUMN_TOTAL_ASSETS, mTotalAssets);
		contentValues.put(DatabaseContract.COLUMN_TOTAL_LONG_TERM_LIABILITIES, mTotalLongTermLiabilities);
		contentValues.put(DatabaseContract.COLUMN_MAIN_BUSINESS_INCOME, mMainBusinessIncome);
		contentValues.put(DatabaseContract.COLUMN_MAIN_BUSINESS_INCOME_IN_YEAR, mMainBusinessIncomeInYear);
		contentValues.put(DatabaseContract.COLUMN_DEBT_TO_NET_ASSETS_RATIO, mDebtToNetAssetsRatio);
		contentValues.put(DatabaseContract.COLUMN_BOOK_VALUE_PER_SHARE, mBookValuePerShare);
		contentValues.put(DatabaseContract.COLUMN_CASH_FLOW_PER_SHARE, mCashFlowPerShare);
		contentValues.put(DatabaseContract.COLUMN_NET_PROFIT, mNetProfit);
		contentValues.put(DatabaseContract.COLUMN_NET_PROFIT_IN_YEAR, mNetProfitInYear);
		contentValues.put(DatabaseContract.COLUMN_NET_PROFIT_MARGIN, mNetProfitMargin);
		contentValues.put(DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE, mNetProfitPerShare);
		contentValues.put(DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE_IN_YEAR, mNetProfitPerShareInYear);
		contentValues.put(DatabaseContract.COLUMN_ROI, mRoi);
		contentValues.put(DatabaseContract.COLUMN_ROE, mRoe);
		contentValues.put(DatabaseContract.COLUMN_PE, mPe);
		contentValues.put(DatabaseContract.COLUMN_PB, mPb);
		contentValues.put(DatabaseContract.COLUMN_RATE, mRate);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND, mDividend);
		contentValues.put(DatabaseContract.COLUMN_YIELD, mYield);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND_RATIO, mDividendRatio);
		contentValues.put(DatabaseContract.COLUMN_R_DATE, mRDate);
		contentValues.put(DatabaseContract.COLUMN_STATUS, mStatus);
		contentValues.put(DatabaseContract.COLUMN_INFORMATION_MODIFIED, mInformationModified);
		contentValues.put(DatabaseContract.COLUMN_REALTIME_MODIFIED, mRealTimeModified);

		return contentValues;
	}

	public ContentValues getContentValuesInformation() {
		ContentValues contentValues = getContentValues();

		contentValues.put(DatabaseContract.COLUMN_CLASSES, mClasses);
		contentValues.put(DatabaseContract.COLUMN_PINYIN, mPinyin);
		contentValues.put(DatabaseContract.COLUMN_TOTAL_SHARE, mTotalShare);
		contentValues.put(DatabaseContract.COLUMN_INFORMATION_MODIFIED, mInformationModified);

		return contentValues;
	}

	public ContentValues getContentValuesRealTime() {
		ContentValues contentValues = getContentValues();

		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		contentValues.put(DatabaseContract.COLUMN_PRICE, mPrice);
		contentValues.put(DatabaseContract.COLUMN_CHANGE, mChange);
		contentValues.put(DatabaseContract.COLUMN_NET, mNet);
		contentValues.put(DatabaseContract.COLUMN_VOLUME, mVolume);
		contentValues.put(DatabaseContract.COLUMN_VALUE, mValue);
		contentValues.put(DatabaseContract.COLUMN_REALTIME_MODIFIED, mRealTimeModified);

		return contentValues;
	}

	public ContentValues getContentValuesForEdit() {
		ContentValues contentValues = getContentValues();

		contentValues.put(DatabaseContract.COLUMN_CLASSES, mClasses);
		contentValues.put(DatabaseContract.COLUMN_SE, mSE);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);

		contentValues.put(DatabaseContract.COLUMN_FLAG, mFlag);
		contentValues.put(DatabaseContract.COLUMN_THRESHOLD, mThreshold);
		contentValues.put(DatabaseContract.COLUMN_QUANT_VOLUME, mQuantVolume);
		contentValues.put(DatabaseContract.COLUMN_OPERATE, mOperate);

		return contentValues;
	}

	public void set(Stock stock) {
		if (stock == null) {
			return;
		}

		init();

		super.set(stock);

		setFlag(stock.mFlag);
		setClasses(stock.mClasses);
		setSE(stock.mSE);
		setCode(stock.mCode);
		setName(stock.mName);
		setPinyin(stock.mPinyin);

		setPrice(stock.mPrice);
		setChange(stock.mChange);
		setNet(stock.mNet);
		setVolume(stock.mVolume);
		setValue(stock.mValue);

		setDate(stock.mDate);
		setTime(stock.mTime);
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
		setTrend(stock.mTrend);

		setThreshold(stock.mThreshold);
		setQuantVolume(stock.mQuantVolume);
		setQuantProfit(stock.mQuantProfit);
		setQuantProfitMargin(stock.mQuantProfitMargin);

		setOperate(stock.mOperate);
		setHold(stock.mHold);
		setCost(stock.mCost);
		setProfit(stock.mProfit);
		setBonus(stock.mBonus);
		setValuation(stock.mValuation);
		setTotalShare(stock.mTotalShare);
		setMarketValue(stock.mMarketValue);
		setTotalAssets(stock.mTotalAssets);
		setTotalLongTermLiabilities(stock.mTotalLongTermLiabilities);
		setMainBusinessIncome(stock.mMainBusinessIncome);
		setMainBusinessIncomeInYear(stock.mMainBusinessIncomeInYear);
		setDebtToNetAssetsRatio(stock.mDebtToNetAssetsRatio);
		setBookValuePerShare(stock.mBookValuePerShare);
		setCashFlowPerShare(stock.mCashFlowPerShare);
		setNetProfit(stock.mNetProfit);
		setNetProfitInYear(stock.mNetProfitInYear);
		setNetProfitMargin(stock.mNetProfitMargin);
		setNetProfitPerShare(stock.mNetProfitPerShare);
		setNetProfitPerShareInYear(stock.mNetProfitPerShareInYear);
		setRoi(stock.mRoi);
		setRoe(stock.mRoe);
		setPe(stock.mPe);
		setPb(stock.mPb);
		setRate(stock.mRate);
		setDividend(stock.mDividend);
		setYield(stock.mYield);
		setDividendRatio(stock.mDividendRatio);
		setRDate(stock.mRDate);
		setStatus(stock.mStatus);
		setRealTimeModified(mRealTimeModified);
		setInformationModified(mInformationModified);
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
		setFlag(cursor);
		setPrice(cursor);
		setChange(cursor);
		setNet(cursor);
		setVolume(cursor);
		setValue(cursor);
		setDate(cursor);
		setTime(cursor);
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
		setTrend(cursor);
		setThreshold(cursor);
		setQuantVolume(cursor);
		setQuantProfit(cursor);
		setQuantProfitMargin(cursor);
		setOperate(cursor);
		setHold(cursor);
		setCost(cursor);
		setProfit(cursor);
		setBonus(cursor);
		setValuation(cursor);
		setTotalShare(cursor);
		setMarketValue(cursor);
		setTotalAssets(cursor);
		setTotalLongTermLiabilities(cursor);
		setMainBusinessIncome(cursor);
		setMainBusinessIncomeInYear(cursor);
		setNetProfit(cursor);
		setNetProfitInYear(cursor);
		setNetProfitMargin(cursor);
		setNetProfitPerShare(cursor);
		setNetProfitPerShareInYear(cursor);
		setDebtToNetAssetsRatio(cursor);
		setBookValuePerShare(cursor);
		setCashFlowPerShare(cursor);
		setRoi(cursor);
		setRate(cursor);
		setRoe(cursor);
		setPe(cursor);
		setPb(cursor);
		setDividend(cursor);
		setYield(cursor);
		setDividendRatio(cursor);
		setRDate(cursor);
		setStatus(cursor);
		setRealTimeModified(cursor);
		setInformationModified(cursor);
	}

	public String getClasses() {
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
				.getColumnIndex(DatabaseContract.COLUMN_CLASSES)));
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

	public String getPinyin() {
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
				.getColumnIndex(DatabaseContract.COLUMN_PINYIN)));
	}

	public int getFlag() {
		return mFlag;
	}

	public void setFlag(int flag) {
		mFlag = flag;
	}

	void setFlag(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setFlag(cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_FLAG)));
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

	public long getValue() {
		return mValue;
	}

	public void setValue(long value) {
		mValue = value;
	}

	void setValue(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setValue(cursor.getLong(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VALUE)));
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(String date) {
		mDate = date;
	}

	void setDate(Cursor cursor) {
		if (cursor == null) {
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
		if (cursor == null) {
			return;
		}

		setTime(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TIME)));
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

	String getTrend() {
		return mTrend;
	}

	public void setTrend(String trend) {
		mTrend = trend;
	}

	void setTrend(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setTrend(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TREND)));
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

	public long getQuantVolume() {
		return mQuantVolume;
	}

	public void setQuantVolume(long quantVolume) {
		mQuantVolume = quantVolume;
	}

	void setQuantVolume(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setQuantVolume(cursor.getLong(cursor
				.getColumnIndex(DatabaseContract.COLUMN_QUANT_VOLUME)));
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

	public String getOperate() {
		return mOperate;
	}

	public void setOperate(String operate) {
		mOperate = operate;
	}

	void setOperate(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setOperate(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_OPERATE)));
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

	public double getBonus() {
		return mBonus;
	}

	public void setBonus(double bonus) {
		mBonus = bonus;
	}

	void setBonus(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setBonus(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_BONUS)));
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

	public double getTotalShare() {
		return mTotalShare;
	}

	public void setTotalShare(double totalShare) {
		mTotalShare = totalShare;
	}

	void setTotalShare(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setTotalShare(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TOTAL_SHARE)));
	}

	public double getMarkerValue() {
		return mMarketValue;
	}

	public void setMarketValue(double marketValue) {
		mMarketValue = marketValue;
	}

	void setMarketValue(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setMarketValue(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_MARKET_VALUE)));
	}

	public double getTotalAssets() {
		return mTotalAssets;
	}

	public void setTotalAssets(double totalAssets) {
		mTotalAssets = totalAssets;
	}

	void setTotalAssets(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setTotalAssets(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TOTAL_ASSETS)));
	}

	public double getTotalLongTermLiabilities() {
		return mTotalLongTermLiabilities;
	}

	public void setTotalLongTermLiabilities(double totalLongTermLiabilities) {
		mTotalLongTermLiabilities = totalLongTermLiabilities;
	}

	void setTotalLongTermLiabilities(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setTotalLongTermLiabilities(cursor
				.getDouble(cursor
						.getColumnIndex(DatabaseContract.COLUMN_TOTAL_LONG_TERM_LIABILITIES)));
	}

	public double getMainBusinessIncome() {
		return mMainBusinessIncome;
	}

	public void setMainBusinessIncome(double mainBusinessIncome) {
		mMainBusinessIncome = mainBusinessIncome;
	}

	void setMainBusinessIncome(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setMainBusinessIncome(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_MAIN_BUSINESS_INCOME)));
	}

	public double getMainBusinessIncomeInYear() {
		return mMainBusinessIncomeInYear;
	}

	public void setMainBusinessIncomeInYear(double mainBusinessIncomeInYear) {
		mMainBusinessIncomeInYear = mainBusinessIncomeInYear;
	}

	void setMainBusinessIncomeInYear(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setMainBusinessIncomeInYear(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_MAIN_BUSINESS_INCOME_IN_YEAR)));
	}

	public double getDebtToNetAssetsRatio() {
		return mDebtToNetAssetsRatio;
	}

	public void setDebtToNetAssetsRatio(double debtToNetAssetsRatio) {
		mDebtToNetAssetsRatio = debtToNetAssetsRatio;
	}

	void setDebtToNetAssetsRatio(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDebtToNetAssetsRatio(cursor
				.getDouble(cursor
						.getColumnIndex(DatabaseContract.COLUMN_DEBT_TO_NET_ASSETS_RATIO)));
	}

	public double getBookValuePerShare() {
		return mBookValuePerShare;
	}

	public void setBookValuePerShare(double bookValuePerShare) {
		mBookValuePerShare = bookValuePerShare;
	}

	void setBookValuePerShare(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setBookValuePerShare(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_BOOK_VALUE_PER_SHARE)));
	}

	public double getCashFlowPerShare() {
		return mCashFlowPerShare;
	}

	public void setCashFlowPerShare(double cashFlowPerShare) {
		mCashFlowPerShare = cashFlowPerShare;
	}

	void setCashFlowPerShare(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setCashFlowPerShare(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_CASH_FLOW_PER_SHARE)));
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

	public double getNetProfitInYear() {
		return mNetProfitInYear;
	}

	public void setNetProfitInYear(double netProfitInYear) {
		mNetProfitInYear = netProfitInYear;
	}

	void setNetProfitInYear(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setNetProfitInYear(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NET_PROFIT_IN_YEAR)));
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

	public double getNetProfitPerShare() {
		return mNetProfitPerShare;
	}

	public void setNetProfitPerShare(double netProfitPerShare) {
		mNetProfitPerShare = netProfitPerShare;
	}

	void setNetProfitPerShare(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setNetProfitPerShare(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE)));
	}

	public double getNetProfitPerShareInYear() {
		return mNetProfitPerShareInYear;
	}

	public void setNetProfitPerShareInYear(double netProfitPerShareInYear) {
		mNetProfitPerShareInYear = netProfitPerShareInYear;
	}

	void setNetProfitPerShareInYear(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setNetProfitPerShareInYear(cursor
				.getDouble(cursor
						.getColumnIndex(DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE_IN_YEAR)));
	}

	public double getRate() {
		return mRate;
	}

	public void setRate(double rate) {
		mRate = rate;
	}

	void setRate(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setRate(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_RATE)));
	}

	public double getRoi() {
		return mRoi;
	}

	public void setRoi(double roi) {
		mRoi = roi;
	}

	void setRoi(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setRoi(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ROI)));
	}

	public double getRoe() {
		return mRoe;
	}

	public void setRoe(double roe) {
		mRoe = roe;
	}

	void setRoe(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setRoe(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ROE)));
	}

	public double getPe() {
		return mPe;
	}

	public void setPe(double pe) {
		mPe = pe;
	}

	void setPe(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setPe(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PE)));
	}

	public double getPb() {
		return mPb;
	}

	public void setPb(double pb) {
		mPb = pb;
	}

	void setPb(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setPb(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PB)));
	}

	public double getDividend() {
		return mDividend;
	}

	public void setDividend(double dividend) {
		mDividend = dividend;
	}

	void setDividend(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDividend(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIVIDEND)));
	}

	public double getYield() {
		return mYield;
	}

	public void setYield(double yield) {
		mYield = yield;
	}

	void setYield(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setYield(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_YIELD)));
	}

	public double getDividendRatio() {
		return mDividendRatio;
	}

	public void setDividendRatio(double dividendRatio) {
		mDividendRatio = dividendRatio;
	}

	void setDividendRatio(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setDividendRatio(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIVIDEND_RATIO)));
	}

	public String getRDate() {
		return mRDate;
	}

	public void setRDate(String rDate) {
		mRDate = rDate;
	}

	void setRDate(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setRDate(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_R_DATE)));
	}

	public String getStatus() {
		return mStatus;
	}

	public void setStatus(String status) {
		mStatus = status;
	}

	void setStatus(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setStatus(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_STATUS)));
	}

	public String getRealTimeModified() {
		return mRealTimeModified;
	}

	public void setRealTimeModified(String realtimeModified) {
		mRealTimeModified = realtimeModified;
	}

	void setRealTimeModified(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setRealTimeModified(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_REALTIME_MODIFIED)));
	}

	public String getInformationModified() {
		return mInformationModified;
	}

	public void setInformationModified(String informationModified) {
		mInformationModified = informationModified;
	}

	void setInformationModified(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setInformationModified(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_INFORMATION_MODIFIED)));
	}

	public void addFlag(int flag) {
		mFlag |= flag;
	}

	public void removeFlag(int flag) {
		mFlag &= ~flag;
	}

	public boolean hasFlag(int flag) {
		boolean result = false;

		if ((mFlag & flag) == flag) {
			result = true;
		}

		return result;
	}

	public ArrayList<StockData> getStockDataList(String period) {
		ArrayList<StockData> result = null;

		if (period.equals(DatabaseContract.COLUMN_MIN1)) {
			result = mStockDataListMin1;
		} else if (period.equals(DatabaseContract.COLUMN_MIN5)) {
			result = mStockDataListMin5;
		} else if (period.equals(DatabaseContract.COLUMN_MIN15)) {
			result = mStockDataListMin15;
		} else if (period.equals(DatabaseContract.COLUMN_MIN30)) {
			result = mStockDataListMin30;
		} else if (period.equals(DatabaseContract.COLUMN_MIN60)) {
			result = mStockDataListMin60;
		} else if (period.equals(DatabaseContract.COLUMN_DAY)) {
			result = mStockDataListDay;
		} else if (period.equals(DatabaseContract.COLUMN_WEEK)) {
			result = mStockDataListWeek;
		} else if (period.equals(DatabaseContract.COLUMN_MONTH)) {
			result = mStockDataListMonth;
		} else if (period.equals(DatabaseContract.COLUMN_QUARTER)) {
			result = mStockDataListQuarter;
		} else if (period.equals(DatabaseContract.COLUMN_YEAR)) {
			result = mStockDataListYear;
		}

		return result;
	}

	public ArrayList<StockData> getDrawVertexList(String period) {
		ArrayList<StockData> result = null;

		if (period.equals(DatabaseContract.COLUMN_MIN1)) {
			result = mDrawVertexListMin1;
		} else if (period.equals(DatabaseContract.COLUMN_MIN5)) {
			result = mDrawVertexListMin5;
		} else if (period.equals(DatabaseContract.COLUMN_MIN15)) {
			result = mDrawVertexListMin15;
		} else if (period.equals(DatabaseContract.COLUMN_MIN30)) {
			result = mDrawVertexListMin30;
		} else if (period.equals(DatabaseContract.COLUMN_MIN60)) {
			result = mDrawVertexListMin60;
		} else if (period.equals(DatabaseContract.COLUMN_DAY)) {
			result = mDrawVertexListDay;
		} else if (period.equals(DatabaseContract.COLUMN_WEEK)) {
			result = mDrawVertexListWeek;
		} else if (period.equals(DatabaseContract.COLUMN_MONTH)) {
			result = mDrawVertexListMonth;
		} else if (period.equals(DatabaseContract.COLUMN_QUARTER)) {
			result = mDrawVertexListQuarter;
		} else if (period.equals(DatabaseContract.COLUMN_YEAR)) {
			result = mDrawVertexListYear;
		}

		return result;
	}

	public ArrayList<StockData> getDrawDataList(String period) {
		ArrayList<StockData> result = null;

		if (period.equals(DatabaseContract.COLUMN_MIN1)) {
			result = mDrawDataListMin1;
		} else if (period.equals(DatabaseContract.COLUMN_MIN5)) {
			result = mDrawDataListMin5;
		} else if (period.equals(DatabaseContract.COLUMN_MIN15)) {
			result = mDrawDataListMin15;
		} else if (period.equals(DatabaseContract.COLUMN_MIN30)) {
			result = mDrawDataListMin30;
		} else if (period.equals(DatabaseContract.COLUMN_MIN60)) {
			result = mDrawDataListMin60;
		} else if (period.equals(DatabaseContract.COLUMN_DAY)) {
			result = mDrawDataListDay;
		} else if (period.equals(DatabaseContract.COLUMN_WEEK)) {
			result = mDrawDataListWeek;
		} else if (period.equals(DatabaseContract.COLUMN_MONTH)) {
			result = mDrawDataListMonth;
		} else if (period.equals(DatabaseContract.COLUMN_QUARTER)) {
			result = mDrawDataListQuarter;
		} else if (period.equals(DatabaseContract.COLUMN_YEAR)) {
			result = mDrawDataListYear;
		}

		return result;
	}

	public ArrayList<StockData> getStrokeVertexList(String period) {
		ArrayList<StockData> result = null;

		if (period.equals(DatabaseContract.COLUMN_MIN1)) {
			result = mStrokeVertexListMin1;
		} else if (period.equals(DatabaseContract.COLUMN_MIN5)) {
			result = mStrokeVertexListMin5;
		} else if (period.equals(DatabaseContract.COLUMN_MIN15)) {
			result = mStrokeVertexListMin15;
		} else if (period.equals(DatabaseContract.COLUMN_MIN30)) {
			result = mStrokeVertexListMin30;
		} else if (period.equals(DatabaseContract.COLUMN_MIN60)) {
			result = mStrokeVertexListMin60;
		} else if (period.equals(DatabaseContract.COLUMN_DAY)) {
			result = mStrokeVertexListDay;
		} else if (period.equals(DatabaseContract.COLUMN_WEEK)) {
			result = mStrokeVertexListWeek;
		} else if (period.equals(DatabaseContract.COLUMN_MONTH)) {
			result = mStrokeVertexListMonth;
		} else if (period.equals(DatabaseContract.COLUMN_QUARTER)) {
			result = mStrokeVertexListQuarter;
		} else if (period.equals(DatabaseContract.COLUMN_YEAR)) {
			result = mStrokeVertexListYear;
		}

		return result;
	}

	public ArrayList<StockData> getStrokeDataList(String period) {
		ArrayList<StockData> result = null;

		if (period.equals(DatabaseContract.COLUMN_MIN1)) {
			result = mStrokeDataListMin1;
		} else if (period.equals(DatabaseContract.COLUMN_MIN5)) {
			result = mStrokeDataListMin5;
		} else if (period.equals(DatabaseContract.COLUMN_MIN15)) {
			result = mStrokeDataListMin15;
		} else if (period.equals(DatabaseContract.COLUMN_MIN30)) {
			result = mStrokeDataListMin30;
		} else if (period.equals(DatabaseContract.COLUMN_MIN60)) {
			result = mStrokeDataListMin60;
		} else if (period.equals(DatabaseContract.COLUMN_DAY)) {
			result = mStrokeDataListDay;
		} else if (period.equals(DatabaseContract.COLUMN_WEEK)) {
			result = mStrokeDataListWeek;
		} else if (period.equals(DatabaseContract.COLUMN_MONTH)) {
			result = mStrokeDataListMonth;
		} else if (period.equals(DatabaseContract.COLUMN_QUARTER)) {
			result = mStrokeDataListQuarter;
		} else if (period.equals(DatabaseContract.COLUMN_YEAR)) {
			result = mStrokeDataListYear;
		}

		return result;
	}

	public ArrayList<StockData> getSegmentVertexList(String period) {
		ArrayList<StockData> result = null;

		if (period.equals(DatabaseContract.COLUMN_MIN1)) {
			result = mSegmentVertexListMin1;
		} else if (period.equals(DatabaseContract.COLUMN_MIN5)) {
			result = mSegmentVertexListMin5;
		} else if (period.equals(DatabaseContract.COLUMN_MIN15)) {
			result = mSegmentVertexListMin15;
		} else if (period.equals(DatabaseContract.COLUMN_MIN30)) {
			result = mSegmentVertexListMin30;
		} else if (period.equals(DatabaseContract.COLUMN_MIN60)) {
			result = mSegmentVertexListMin60;
		} else if (period.equals(DatabaseContract.COLUMN_DAY)) {
			result = mSegmentVertexListDay;
		} else if (period.equals(DatabaseContract.COLUMN_WEEK)) {
			result = mSegmentVertexListWeek;
		} else if (period.equals(DatabaseContract.COLUMN_MONTH)) {
			result = mSegmentVertexListMonth;
		} else if (period.equals(DatabaseContract.COLUMN_QUARTER)) {
			result = mSegmentVertexListQuarter;
		} else if (period.equals(DatabaseContract.COLUMN_YEAR)) {
			result = mSegmentVertexListYear;
		}

		return result;
	}

	public ArrayList<StockData> getSegmentDataList(String period) {
		ArrayList<StockData> result = null;

		if (period.equals(DatabaseContract.COLUMN_MIN1)) {
			result = mSegmentDataListMin1;
		} else if (period.equals(DatabaseContract.COLUMN_MIN5)) {
			result = mSegmentDataListMin5;
		} else if (period.equals(DatabaseContract.COLUMN_MIN15)) {
			result = mSegmentDataListMin15;
		} else if (period.equals(DatabaseContract.COLUMN_MIN30)) {
			result = mSegmentDataListMin30;
		} else if (period.equals(DatabaseContract.COLUMN_MIN60)) {
			result = mSegmentDataListMin60;
		} else if (period.equals(DatabaseContract.COLUMN_DAY)) {
			result = mSegmentDataListDay;
		} else if (period.equals(DatabaseContract.COLUMN_WEEK)) {
			result = mSegmentDataListWeek;
		} else if (period.equals(DatabaseContract.COLUMN_MONTH)) {
			result = mSegmentDataListMonth;
		} else if (period.equals(DatabaseContract.COLUMN_QUARTER)) {
			result = mSegmentDataListQuarter;
		} else if (period.equals(DatabaseContract.COLUMN_YEAR)) {
			result = mSegmentDataListYear;
		}

		return result;
	}

	public ArrayList<StockData> getLineVertexList(String period) {
		ArrayList<StockData> result = null;

		if (period.equals(DatabaseContract.COLUMN_MIN1)) {
			result = mLineVertexListMin1;
		} else if (period.equals(DatabaseContract.COLUMN_MIN5)) {
			result = mLineVertexListMin5;
		} else if (period.equals(DatabaseContract.COLUMN_MIN15)) {
			result = mLineVertexListMin15;
		} else if (period.equals(DatabaseContract.COLUMN_MIN30)) {
			result = mLineVertexListMin30;
		} else if (period.equals(DatabaseContract.COLUMN_MIN60)) {
			result = mLineVertexListMin60;
		} else if (period.equals(DatabaseContract.COLUMN_DAY)) {
			result = mLineVertexListDay;
		} else if (period.equals(DatabaseContract.COLUMN_WEEK)) {
			result = mLineVertexListWeek;
		} else if (period.equals(DatabaseContract.COLUMN_MONTH)) {
			result = mLineVertexListMonth;
		} else if (period.equals(DatabaseContract.COLUMN_QUARTER)) {
			result = mLineVertexListQuarter;
		} else if (period.equals(DatabaseContract.COLUMN_YEAR)) {
			result = mLineVertexListYear;
		}

		return result;
	}

	public ArrayList<StockData> getLineDataList(String period) {
		ArrayList<StockData> result = null;

		if (period.equals(DatabaseContract.COLUMN_MIN1)) {
			result = mLineDataListMin1;
		} else if (period.equals(DatabaseContract.COLUMN_MIN5)) {
			result = mLineDataListMin5;
		} else if (period.equals(DatabaseContract.COLUMN_MIN15)) {
			result = mLineDataListMin15;
		} else if (period.equals(DatabaseContract.COLUMN_MIN30)) {
			result = mLineDataListMin30;
		} else if (period.equals(DatabaseContract.COLUMN_MIN60)) {
			result = mLineDataListMin60;
		} else if (period.equals(DatabaseContract.COLUMN_DAY)) {
			result = mLineDataListDay;
		} else if (period.equals(DatabaseContract.COLUMN_WEEK)) {
			result = mLineDataListWeek;
		} else if (period.equals(DatabaseContract.COLUMN_MONTH)) {
			result = mLineDataListMonth;
		} else if (period.equals(DatabaseContract.COLUMN_QUARTER)) {
			result = mLineDataListQuarter;
		} else if (period.equals(DatabaseContract.COLUMN_YEAR)) {
			result = mLineDataListYear;
		}

		return result;
	}

	public ArrayList<StockData> getOutlineVertexList(String period) {
		ArrayList<StockData> result = null;

		if (period.equals(DatabaseContract.COLUMN_MIN1)) {
			result = mOutlineVertexListMin1;
		} else if (period.equals(DatabaseContract.COLUMN_MIN5)) {
			result = mOutlineVertexListMin5;
		} else if (period.equals(DatabaseContract.COLUMN_MIN15)) {
			result = mOutlineVertexListMin15;
		} else if (period.equals(DatabaseContract.COLUMN_MIN30)) {
			result = mOutlineVertexListMin30;
		} else if (period.equals(DatabaseContract.COLUMN_MIN60)) {
			result = mOutlineVertexListMin60;
		} else if (period.equals(DatabaseContract.COLUMN_DAY)) {
			result = mOutlineVertexListDay;
		} else if (period.equals(DatabaseContract.COLUMN_WEEK)) {
			result = mOutlineVertexListWeek;
		} else if (period.equals(DatabaseContract.COLUMN_MONTH)) {
			result = mOutlineVertexListMonth;
		} else if (period.equals(DatabaseContract.COLUMN_QUARTER)) {
			result = mOutlineVertexListQuarter;
		} else if (period.equals(DatabaseContract.COLUMN_YEAR)) {
			result = mOutlineVertexListYear;
		}

		return result;
	}

	public ArrayList<StockData> getOutlineDataList(String period) {
		ArrayList<StockData> result = null;

		if (period.equals(DatabaseContract.COLUMN_MIN1)) {
			result = mOutlineDataListMin1;
		} else if (period.equals(DatabaseContract.COLUMN_MIN5)) {
			result = mOutlineDataListMin5;
		} else if (period.equals(DatabaseContract.COLUMN_MIN15)) {
			result = mOutlineDataListMin15;
		} else if (period.equals(DatabaseContract.COLUMN_MIN30)) {
			result = mOutlineDataListMin30;
		} else if (period.equals(DatabaseContract.COLUMN_MIN60)) {
			result = mOutlineDataListMin60;
		} else if (period.equals(DatabaseContract.COLUMN_DAY)) {
			result = mOutlineDataListDay;
		} else if (period.equals(DatabaseContract.COLUMN_WEEK)) {
			result = mOutlineDataListWeek;
		} else if (period.equals(DatabaseContract.COLUMN_MONTH)) {
			result = mOutlineDataListMonth;
		} else if (period.equals(DatabaseContract.COLUMN_QUARTER)) {
			result = mOutlineDataListQuarter;
		} else if (period.equals(DatabaseContract.COLUMN_YEAR)) {
			result = mOutlineDataListYear;
		}

		return result;
	}

	public Calendar getCalendar() {
		Calendar result = null;

		if (TextUtils.isEmpty(getDate())) {
			return result;
		}

		if (TextUtils.isEmpty(getTime())) {
			result = Utility.getCalendar(getDate(),
					Utility.CALENDAR_DATE_FORMAT);
		} else {
			result = Utility.getCalendar(getDate() + " " + getTime(),
					Utility.CALENDAR_DATE_TIME_FORMAT);
		}

		return result;
	}

	public void setDateTime(String date, String time) {
		Calendar calendar;
		Calendar current;

		if (TextUtils.isEmpty(date)) {
			return;
		}

		if (TextUtils.isEmpty(time)) {
			current = Utility.getCalendar(date,
					Utility.CALENDAR_DATE_FORMAT);
		} else {
			current = Utility.getCalendar(date + " " + time,
					Utility.CALENDAR_DATE_TIME_FORMAT);
		}

		calendar = getCalendar();
		if (calendar == null) {
			setDate(date);
			setTime(time);
			return;
		}

		if (current.after(calendar)) {
			setDate(date);
			setTime(time);
		}
	}

	public String getAction(String period) {
		String action = "";

		if (period.equals(DatabaseContract.COLUMN_MIN1)) {
			action = getActionMin1();
		} else if (period.equals(DatabaseContract.COLUMN_MIN5)) {
			action = getActionMin5();
		} else if (period.equals(DatabaseContract.COLUMN_MIN15)) {
			action = getActionMin15();
		} else if (period.equals(DatabaseContract.COLUMN_MIN30)) {
			action = getActionMin30();
		} else if (period.equals(DatabaseContract.COLUMN_MIN60)) {
			action = getActionMin60();
		} else if (period.equals(DatabaseContract.COLUMN_DAY)) {
			action = getActionDay();
		} else if (period.equals(DatabaseContract.COLUMN_WEEK)) {
			action = getActionWeek();
		} else if (period.equals(DatabaseContract.COLUMN_MONTH)) {
			action = getActionMonth();
		} else if (period.equals(DatabaseContract.COLUMN_QUARTER)) {
			action = getActionQuarter();
		} else if (period.equals(DatabaseContract.COLUMN_YEAR)) {
			action = getActionYear();
		}

		return action;
	}

	public void setAction(String period, String action) {
		if (period.equals(DatabaseContract.COLUMN_MIN1)) {
			setActionMin1(action);
		} else if (period.equals(DatabaseContract.COLUMN_MIN5)) {
			setActionMin5(action);
		} else if (period.equals(DatabaseContract.COLUMN_MIN15)) {
			setActionMin15(action);
		} else if (period.equals(DatabaseContract.COLUMN_MIN30)) {
			setActionMin30(action);
		} else if (period.equals(DatabaseContract.COLUMN_MIN60)) {
			setActionMin60(action);
		} else if (period.equals(DatabaseContract.COLUMN_DAY)) {
			setActionDay(action);
		} else if (period.equals(DatabaseContract.COLUMN_WEEK)) {
			setActionWeek(action);
		} else if (period.equals(DatabaseContract.COLUMN_MONTH)) {
			setActionMonth(action);
		} else if (period.equals(DatabaseContract.COLUMN_QUARTER)) {
			setActionQuarter(action);
		} else if (period.equals(DatabaseContract.COLUMN_YEAR)) {
			setActionYear(action);
		}
	}

	public void setupMarketValue() {
		if (mPrice == 0) {
			return;
		}

		if (mTotalShare == 0) {
			return;
		}

		mMarketValue = Utility.Round(mPrice * mTotalShare,
				Constant.DOUBLE_FIXED_DECIMAL);
	}

	public void setupNetProfitMargin() {
		if ((mNetProfitInYear == 0) || (mMainBusinessIncomeInYear == 0)) {
			return;
		}

		mNetProfitMargin = Utility.Round(100.0 * mNetProfitInYear / mMainBusinessIncomeInYear,
				Constant.DOUBLE_FIXED_DECIMAL);
	}

	public void setupNetProfitPerShare() {
		if (mTotalShare == 0) {
			return;
		}

		mNetProfitPerShare = Utility.Round(mNetProfit / mTotalShare,
				Constant.DOUBLE_FIXED_DECIMAL);
	}

	public void setupNetProfitPerShareInYear(
			ArrayList<StockFinancial> stockFinancialList) {
		double mainBusinessIncome = 0;
		double netProfit = 0;
		double netProfitPerShare = 0;

		if (mTotalShare == 0) {
			return;
		}

		if ((stockFinancialList == null)
				|| (stockFinancialList.size() < Constant.SEASONS_IN_A_YEAR + 1)) {
			return;
		}

		mMainBusinessIncomeInYear = 0;
		mNetProfitInYear = 0;
		mNetProfitPerShareInYear = 0;

		for (int i = 0; i < Constant.SEASONS_IN_A_YEAR; i++) {
			StockFinancial stockFinancial = stockFinancialList.get(i);
			StockFinancial prev = stockFinancialList.get(i + 1);

			if (stockFinancial.getDate().contains("03-31")) {
				mainBusinessIncome = stockFinancial.getMainBusinessIncome();
				netProfit = stockFinancial.getNetProfit();
				netProfitPerShare = stockFinancial.getNetProfit() / mTotalShare;
			} else {
				mainBusinessIncome = stockFinancial.getMainBusinessIncome() - prev.getMainBusinessIncome();
				netProfit = stockFinancial.getNetProfit() - prev.getNetProfit();
				netProfitPerShare = (stockFinancial.getNetProfit() - prev
						.getNetProfit()) / mTotalShare;
			}

			mMainBusinessIncomeInYear += mainBusinessIncome;
			mNetProfitInYear += netProfit;
			mNetProfitPerShareInYear += netProfitPerShare;
		}
	}

	public void setupRate(
			ArrayList<StockFinancial> stockFinancialList) {
		double netProfitPerShare = 0;
		double netProfitPerShareLastYear = 0;

		if (mTotalShare == 0) {
			return;
		}

		if ((stockFinancialList == null)
				|| (stockFinancialList.size() < 2 * Constant.SEASONS_IN_A_YEAR + 1)) {
			return;
		}

		netProfitPerShareLastYear = 0;

		for (int i = Constant.SEASONS_IN_A_YEAR; i < 2 * Constant.SEASONS_IN_A_YEAR; i++) {
			StockFinancial stockFinancial = stockFinancialList.get(i);
			StockFinancial prev = stockFinancialList.get(i + 1);

			if (stockFinancial.getDate().contains("03-31")) {
				netProfitPerShare = stockFinancial.getNetProfit() / mTotalShare;
			} else {
				netProfitPerShare = (stockFinancial.getNetProfit() - prev
						.getNetProfit()) / mTotalShare;
			}

			netProfitPerShareLastYear += netProfitPerShare;
		}

		if (netProfitPerShareLastYear == 0) {
			return;
		}

		mRate = mNetProfitPerShareInYear / netProfitPerShareLastYear;

		mRate = Utility.Round(mRate, Constant.DOUBLE_FIXED_DECIMAL);
	}

	public void setupDebtToNetAssetsRatio(ArrayList<StockFinancial> stockFinancialList) {
		if ((stockFinancialList == null)
				|| (stockFinancialList.size() < Constant.SEASONS_IN_A_YEAR + 1)) {
			return;
		}

		mDebtToNetAssetsRatio = stockFinancialList.get(0).getDebtToNetAssetsRatio();
	}

	public void setupRoe(ArrayList<StockFinancial> stockFinancialList) {
		double bookValuePerShare = 0;

		if ((stockFinancialList == null)
				|| (stockFinancialList.size() < Constant.SEASONS_IN_A_YEAR + 1)) {
			return;
		}

		bookValuePerShare = stockFinancialList.get(Constant.SEASONS_IN_A_YEAR)
				.getBookValuePerShare();

		if (bookValuePerShare == 0) {
			return;
		}

		mRoe = Utility.Round(100.0 * mNetProfitPerShareInYear
				/ bookValuePerShare, Constant.DOUBLE_FIXED_DECIMAL);
	}

	public void setupPe() {
		if (mPrice == 0) {
			return;
		}

		if (mNetProfitPerShareInYear == 0) {
			return;
		}

		mPe = Utility.Round(mPrice / mNetProfitPerShareInYear,
				Constant.DOUBLE_FIXED_DECIMAL);
	}

	public void setupRoi() {
		if (mPe == 0) {
			return;
		}

//		mRoi = Utility.Round(mRoe * (100.0 * 1.0 / mPe + mYield) * mNetProfitMargin * mRate * Constant.ROI_COEFFICIENT,
//				Constant.DOUBLE_FIXED_DECIMAL);
		mRoi = Utility.Round(1.0 / mPe * mNetProfitMargin * Constant.ROI_COEFFICIENT,
				Constant.DOUBLE_FIXED_DECIMAL);
	}

	public void setupPb() {
		if (mBookValuePerShare == 0) {
			return;
		}

		mPb = Utility.Round(mPrice / mBookValuePerShare,
				Constant.DOUBLE_FIXED_DECIMAL);
	}

	public void setupBonus() {
		if (mDividend == 0) {
			mBonus = 0;
			return;
		}

		if (mHold == 0) {
			mBonus = 0;
			return;
		}

		mBonus = mDividend / 10.0 * mHold;
	}

	public void setupYield() {
		if (mPrice == 0) {
			return;
		}

		mYield = Utility.Round(100.0 * mDividend / 10.0 / mPrice,
				Constant.DOUBLE_FIXED_DECIMAL);
	}

	public void setupDividendRatio() {
		if (mDividend == 0) {
			return;
		}

		if (mNetProfitPerShareInYear <= 0) {
			return;
		}

		mDividendRatio = (mDividend / 10.0) / mNetProfitPerShareInYear;

		mDividendRatio = Utility.Round(mDividendRatio,
				Constant.DOUBLE_FIXED_DECIMAL);
	}
}
