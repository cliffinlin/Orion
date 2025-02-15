package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.data.Period;
import com.android.orion.setting.Constant;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.Calendar;

public class Stock extends DatabaseTable {

	public static final String CLASS_A = "A";
	public static final String CLASS_INDEX = "I";

	public static final String SE_SH = "sh";
	public static final String SE_SZ = "sz";

	public static final String STATUS_SUSPENSION = "--";

	public static final int FLAG_NONE = 0;
	public static final int FLAG_FAVORITE = 1 << 0;
	public static final int FLAG_NOTIFY = 1 << 1;

	public static final long INVALID_ID = 0;
	public static final double ROI_COEFFICIENT = 10.0;

	static ArrayList<StockFinancial> mStockFinancialList = new ArrayList<>();
	static ArrayList<TotalShare> mTotalShareList = new ArrayList<>();
	static ArrayList<ShareBonus> mShareBonusList = new ArrayList<>();
	private final Period mMin5 = new Period(Period.MIN5);
	private final Period mMin15 = new Period(Period.MIN15);
	private final Period mMin30 = new Period(Period.MIN30);
	private final Period mMin60 = new Period(Period.MIN60);
	private final Period mDay = new Period(Period.DAY);
	private final Period mWeek = new Period(Period.WEEK);
	private final Period mMonth = new Period(Period.MONTH);

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
	private double mEp;
	private double mEp5;
	private double mEp10;
	private double mPb;
	private double mRate;
	private double mDividend;
	private double mYield;
	private double mDividendRatio;
	private String mRDate;
	private String mStatus;

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
		mEp = 0;
		mEp5 = 0;
		mEp10 = 0;
		mPb = 0;
		mRate = 0;
		mDividend = 0;
		mYield = 0;
		mDividendRatio = 0;
		mRDate = "";
		mStatus = "";
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
		contentValues.put(DatabaseContract.COLUMN_MIN5, mMin5.getAction());
		contentValues.put(DatabaseContract.COLUMN_MIN15, mMin15.getAction());
		contentValues.put(DatabaseContract.COLUMN_MIN30, mMin30.getAction());
		contentValues.put(DatabaseContract.COLUMN_MIN60, mMin60.getAction());
		contentValues.put(DatabaseContract.COLUMN_DAY, mDay.getAction());
		contentValues.put(DatabaseContract.COLUMN_WEEK, mWeek.getAction());
		contentValues.put(DatabaseContract.COLUMN_MONTH, mMonth.getAction());

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
		contentValues.put(DatabaseContract.COLUMN_EP, mEp);
		contentValues.put(DatabaseContract.COLUMN_EP5, mEp5);
		contentValues.put(DatabaseContract.COLUMN_EP10, mEp10);
		contentValues.put(DatabaseContract.COLUMN_PB, mPb);
		contentValues.put(DatabaseContract.COLUMN_RATE, mRate);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND, mDividend);
		contentValues.put(DatabaseContract.COLUMN_YIELD, mYield);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND_RATIO, mDividendRatio);
		contentValues.put(DatabaseContract.COLUMN_R_DATE, mRDate);
		contentValues.put(DatabaseContract.COLUMN_STATUS, mStatus);

		return contentValues;
	}

	public ContentValues getContentValuesInformation() {
		ContentValues contentValues = getContentValues();

		contentValues.put(DatabaseContract.COLUMN_CLASSES, mClasses);
		contentValues.put(DatabaseContract.COLUMN_PINYIN, mPinyin);
		contentValues.put(DatabaseContract.COLUMN_TOTAL_SHARE, mTotalShare);

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

		return contentValues;
	}

	public ContentValues getContentValuesForEdit() {
		ContentValues contentValues = getContentValues();

		contentValues.put(DatabaseContract.COLUMN_CLASSES, mClasses);
		contentValues.put(DatabaseContract.COLUMN_SE, mSE);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);

		contentValues.put(DatabaseContract.COLUMN_FLAG, mFlag);

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
		mMin5.setAction(stock.mMin5.getAction());
		mMin15.setAction(stock.mMin15.getAction());
		mMin30.setAction(stock.mMin30.getAction());
		mMin60.setAction(stock.mMin60.getAction());
		mDay.setAction(stock.mDay.getAction());
		mWeek.setAction(stock.mWeek.getAction());
		mMonth.setAction(stock.mMonth.getAction());

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
		setEp(stock.mEp);
		setEp5(stock.mEp5);
		setEp10(stock.mEp10);
		setPb(stock.mPb);
		setRate(stock.mRate);
		setDividend(stock.mDividend);
		setYield(stock.mYield);
		setDividendRatio(stock.mDividendRatio);
		setRDate(stock.mRDate);
		setStatus(stock.mStatus);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
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
		mMin5.setAction(cursor);
		mMin15.setAction(cursor);
		mMin30.setAction(cursor);
		mMin60.setAction(cursor);
		mDay.setAction(cursor);
		mWeek.setAction(cursor);
		mMonth.setAction(cursor);
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
		setEp(cursor);
		setEp5(cursor);
		setEp10(cursor);
		setPb(cursor);
		setDividend(cursor);
		setYield(cursor);
		setDividendRatio(cursor);
		setRDate(cursor);
		setStatus(cursor);
	}

	public String getClasses() {
		return mClasses;
	}

	public void setClasses(String classes) {
		mClasses = classes;
	}

	void setClasses(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
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

	public void setCode(Cursor cursor) {
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

	public void setName(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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

	public long getHold() {
		return mHold;
	}

	public void setHold(long hold) {
		mHold = hold;
	}

	void setHold(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setPe(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PE)));
	}

	public double getEp() {
		return mEp;
	}

	public void setEp(double ep) {
		mEp = ep;
	}

	void setEp(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setEp(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_EP)));
	}

	public double getEp5() {
		return mEp5;
	}

	public void setEp5(double ep5) {
		mEp5 = ep5;
	}

	void setEp5(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setEp5(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_EP5)));
	}

	public double getEp10() {
		return mEp;
	}

	public void setEp10(double ep10) {
		mEp10 = ep10;
	}

	void setEp10(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setEp10(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_EP10)));
	}

	public double getPb() {
		return mPb;
	}

	public void setPb(double pb) {
		mPb = pb;
	}

	void setPb(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
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
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setStatus(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_STATUS)));
	}

	public void addFlag(int flag) {
		mFlag |= flag;
	}

	public void removeFlag(int flag) {
		if (hasFlag(flag)) {
			mFlag &= ~flag;
		}
	}

	public boolean hasFlag(int flag) {
		return (mFlag & flag) == flag;
	}

	public ArrayList<StockFinancial> getFinancialList() {
		return mStockFinancialList;
	}

	public ArrayList<TotalShare> getTotalShareList() {
		return mTotalShareList;
	}

	public ArrayList<ShareBonus> getShareBonusList() {
		return mShareBonusList;
	}

	public ArrayList<StockData> getArrayList(String period, int type) {
		ArrayList<StockData> result;

		if (TextUtils.equals(period, Period.MIN5)) {
			result = mMin5.getArrayList(type);
		} else if (TextUtils.equals(period, Period.MIN15)) {
			result = mMin15.getArrayList(type);
		} else if (TextUtils.equals(period, Period.MIN30)) {
			result = mMin30.getArrayList(type);
		} else if (TextUtils.equals(period, Period.MIN60)) {
			result = mMin60.getArrayList(type);
		} else if (TextUtils.equals(period, Period.DAY)) {
			result = mDay.getArrayList(type);
		} else if (TextUtils.equals(period, Period.WEEK)) {
			result = mWeek.getArrayList(type);
		} else if (TextUtils.equals(period, Period.MONTH)) {
			result = mMonth.getArrayList(type);
		} else {
			result = new ArrayList<>();
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
		if (TextUtils.equals(period, Period.MIN5)) {
			action = mMin5.getAction();
		} else if (TextUtils.equals(period, Period.MIN15)) {
			action = mMin15.getAction();
		} else if (TextUtils.equals(period, Period.MIN30)) {
			action = mMin30.getAction();
		} else if (TextUtils.equals(period, Period.MIN60)) {
			action = mMin60.getAction();
		} else if (TextUtils.equals(period, Period.DAY)) {
			action = mDay.getAction();
		} else if (TextUtils.equals(period, Period.WEEK)) {
			action = mWeek.getAction();
		} else if (TextUtils.equals(period, Period.MONTH)) {
			action = mMonth.getAction();
		}
		return action;
	}

	public void setAction(String period, String action) {
		if (TextUtils.equals(period, Period.MIN5)) {
			mMin5.setAction(action);
		} else if (TextUtils.equals(period, Period.MIN15)) {
			mMin15.setAction(action);
		} else if (TextUtils.equals(period, Period.MIN30)) {
			mMin30.setAction(action);
		} else if (TextUtils.equals(period, Period.MIN60)) {
			mMin60.setAction(action);
		} else if (TextUtils.equals(period, Period.DAY)) {
			mDay.setAction(action);
		} else if (TextUtils.equals(period, Period.WEEK)) {
			mWeek.setAction(action);
		} else if (TextUtils.equals(period, Period.MONTH)) {
			mMonth.setAction(action);
		}
	}

	public void setupMarketValue() {
		if (mPrice == 0) {
			mMarketValue = 0;
			return;
		}

		if (mTotalShare == 0) {
			mMarketValue = 0;
			return;
		}

		mMarketValue = Utility.Round2(mPrice * mTotalShare);
	}

	public void setupNetProfitMargin() {
		if ((mNetProfitInYear <= 0) || (mMainBusinessIncomeInYear <= 0)) {
			mNetProfitMargin = 0;
			return;
		}

		mNetProfitMargin = Utility.Round4(mNetProfitInYear / mMainBusinessIncomeInYear);
	}

	public void setupNetProfitPerShare() {
		if (mTotalShare == 0) {
			mNetProfitPerShare = 0;
			return;
		}

		mNetProfitPerShare = Utility.Round2(mNetProfit / mTotalShare);
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

		mNetProfitPerShareInYear = Utility.Round2(mNetProfitPerShareInYear);
	}

	public void setupRate(
			ArrayList<StockFinancial> stockFinancialList) {
		double netProfitPerShare = 0;
		double netProfitPerShareLastYear = 0;

		if (mTotalShare == 0) {
			mRate = 0;
			return;
		}

		if ((stockFinancialList == null)
				|| (stockFinancialList.size() < 2 * Constant.SEASONS_IN_A_YEAR + 1)) {
			mRate = 0;
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
			mRate = 0;
			return;
		}

		mRate = mNetProfitPerShareInYear / netProfitPerShareLastYear;
		mRate = Utility.Round2(mRate);
	}

	public void setupDebtToNetAssetsRatio(ArrayList<StockFinancial> stockFinancialList) {
		if ((stockFinancialList == null)
				|| (stockFinancialList.size() < Constant.SEASONS_IN_A_YEAR + 1)) {
			mDebtToNetAssetsRatio = 0;
			return;
		}

		mDebtToNetAssetsRatio = Utility.Round2(stockFinancialList.get(0).getDebtToNetAssetsRatio());
	}

	public void setupRoe(ArrayList<StockFinancial> stockFinancialList) {
		double bookValuePerShare = 0;

		if ((stockFinancialList == null)
				|| (stockFinancialList.size() < Constant.SEASONS_IN_A_YEAR + 1)) {
			mRoe = 0;
			return;
		}

		bookValuePerShare = stockFinancialList.get(Constant.SEASONS_IN_A_YEAR)
				.getBookValuePerShare();

		if (bookValuePerShare == 0) {
			mRoe = 0;
			return;
		}

		mRoe = Utility.Round4(mNetProfitPerShareInYear
				/ bookValuePerShare);
	}

	public void setupPe() {
		if (mPrice == 0 || mNetProfitPerShareInYear <= 0) {
			mPe = 0;
			return;
		}

		mPe = Utility.Round2(mPrice / mNetProfitPerShareInYear);

		double ep = 1.0 + mRoe;
		mEp = Utility.Round2(ep);
		mEp5 = Utility.Round2(investmentReturn(5));
		mEp10 = Utility.Round2(investmentReturn(10));
	}

	public void setupRoi() {
		if ((mNetProfitPerShareInYear <= 0 || mPrice == 0 || mNetProfitInYear <= 0) || (mMainBusinessIncomeInYear <= 0)) {
			mRoi = 0;
			return;
		}

//		mRoi = Utility.Round(mRoe * (100.0 * 1.0 / mPe + mYield) * mNetProfitMargin * mRate * ROI_COEFFICIENT,
//				Constant.DOUBLE_FIXED_DECIMAL);
//		mRoi = Utility.Round(mRoe * ( mNetProfitPerShareInYear / mPrice) * (mNetProfitInYear / mMainBusinessIncomeInYear) * ROI_COEFFICIENT);
		mRoi = Utility.Round2(mRoe * (mNetProfitPerShareInYear / mPrice) * (mNetProfitInYear / mMainBusinessIncomeInYear) * mRate * mYield * ROI_COEFFICIENT);
	}

	public void setupPb() {
		if (mBookValuePerShare == 0) {
			mPb = 0;
			return;
		}

		mPb = Utility.Round2(mPrice / mBookValuePerShare);
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
			mYield = 0;
			return;
		}

		mYield = Utility.Round2(100.0 * mDividend / 10.0 / mPrice);
	}

	public void setupDividendRatio() {
		if (mDividend == 0 || mNetProfitPerShareInYear <= 0) {
			mDividendRatio = 0;
			return;
		}

		mDividendRatio = (mDividend / 10.0) / mNetProfitPerShareInYear;
		mDividendRatio = Utility.Round2(mDividendRatio);
	}

	public double investmentReturn(int totalYears) {
		// 初始参数设置
		double pe = mPe;//21.61;
		double roe = mRoe;//0.3936; // ROE 转换为小数形式
		double dividendPayoutRatio = mDividendRatio;//0.8; // 分红股息率转换为小数形式
		double initialStockPrice = mPrice;//1475;
		int initialShares = 1000;
//			int totalYears = 10;

		// 初始投资
		double initialInvestment = initialShares * initialStockPrice;

		// 计算初始每股净资产（假设初始股价合理反映净资产）
		double initialEPS = initialStockPrice / pe;
		double initialBookValuePerShare = mBookValuePerShare;//initialEPS / roe;
		double totalBookValue = initialBookValuePerShare * initialShares;
		double currentShares = initialShares;

		// 逐年模拟红利再投资
		for (int year = 1; year <= totalYears; year++) {
			// 根据 ROE 计算当年的净利润
			double netProfit = totalBookValue * roe;

			// 计算每股收益
			double eps = netProfit / currentShares;

			// 计算每股分红
			double dividendPerShare = eps * dividendPayoutRatio;

			// 计算当年的红利
			double totalDividend = currentShares * dividendPerShare;

			// 计算红利可以购买的股票数量
			double additionalShares = totalDividend / initialStockPrice;

			// 更新股票数量
			currentShares += additionalShares;

			// 更新总净资产，减去分红部分
			totalBookValue = totalBookValue + netProfit - totalDividend;
		}

		// 计算最终的每股净资产
		double finalBookValuePerShare = totalBookValue / currentShares;

		// 计算最终的股价（基于 PE 保持不变）
		double finalStockPrice = finalBookValuePerShare * roe * pe;

		// 计算总价值
		double finalValue = currentShares * finalStockPrice;

		// 计算投资回报率
		double returnRate = finalValue / initialInvestment;

		// 计算年化投资回报率 (IRR)
		double irr = Math.pow(finalValue / initialInvestment, 1.0 / totalYears) - 1;

		System.out.printf("红利再投%d年的投资回报率为:%.2f%% 年化:%.2f%%\n", totalYears, returnRate * 100, irr * 100);

		return returnRate;
	}
}
