package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.data.IRR;
import com.android.orion.data.Period;
import com.android.orion.setting.Constant;
import com.android.orion.utility.Symbol;
import com.android.orion.utility.Utility;

import java.util.ArrayList;

public class Stock extends DatabaseTable {

	public static final String CLASS_A = "A";
	public static final String CLASS_INDEX = "I";

	public static final String CODE_PREFIX_0 = "0";
	public static final String CODE_PREFIX_3 = "3";
	public static final String CODE_PREFIX_5 = "5";
	public static final String CODE_PREFIX_6 = "6";

	public static final String SE_SH = "sh";
	public static final String SE_SZ = "sz";

	public static final String SE_SH_URL_BASE = "https://www.sse.com.cn/assortment/stock/list/info/company/index.shtml?COMPANY_CODE=";
	public static final String SE_SZ_URL_BASE = "http://www.szse.cn/certificate/individual/index.html?code=";

	public static final String STATUS_SUSPENSION = "--";

	public static final int CODE_LENGTH = 6;

	public static final int FLAG_NONE = 0;
	public static final int FLAG_FAVORITE = 1 << 0;
	public static final int FLAG_NOTIFY = 1 << 1;
	public static final int FLAG_GRID = 1 << 2;

	public static final double ROI_COEFFICIENT = 10;

	static ArrayList<StockFinancial> mStockFinancialList = new ArrayList<>();
	static ArrayList<StockShare> mStockShareList = new ArrayList<>();
	static ArrayList<StockBonus> mStockBonusList = new ArrayList<>();

	private final Period mYear = new Period(Period.YEAR);
	private final Period mMonth6 = new Period(Period.MONTH6);
	private final Period mQuarter = new Period(Period.QUARTER);
	private final Period mMonth2 = new Period(Period.MONTH2);
	private final Period mMonth = new Period(Period.MONTH);
	private final Period mWeek = new Period(Period.WEEK);
	private final Period mDay = new Period(Period.DAY);
	private final Period mMin60 = new Period(Period.MIN60);
	private final Period mMin30 = new Period(Period.MIN30);
	private final Period mMin15 = new Period(Period.MIN15);
	private final Period mMin5 = new Period(Period.MIN5);

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
	private double mValue;

	private double mRoi;
	private double mIR;
	private double mIRR;
	private double mRoe;
	private double mPe;
	private double mPb;
	private double mPr;
	private long mHold;
	private double mProfit;
	private double mBonus;
	private double mBonusInYear;
	private double mValuation;
	private double mCost;
	private double mShare;
	private double mMarketValue;
	private double mMainBusinessIncome;
	private double mNetProfit;
	private double mNetProfitInYear;
	private double mMainBusinessIncomeInYear;
	private double mNetProfitMargin;
	private double mDebtToNetAssetsRatio;
	private double mBookValuePerShare;
	private double mCashFlowPerShare;
	private double mNetProfitPerShare;
	private double mNetProfitPerShareInYear;
	private double mGridProfit;
	private double mRate;
	private double mDividend;
	private double mDividendInYear;
	private double mYield;
	private double mYieldInYear;
	private double mDividendRatio;
	private double mDividendRatioInYear;
	private String mRDate;
	private String mAdaptiveDate;
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

		mClasses = "";
		mSE = "";
		mCode = "";
		mName = "";
		mPinyin = "";

		mFlag = 0;
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

		mRoi = 0;
		mIR = 0;
		mIRR = 0;
		mRoe = 0;
		mPe = 0;
		mPb = 0;
		mPr = 0;
		mProfit = 0;
		mBonus = 0;
		mBonusInYear = 0;
		mValuation = 0;
		mCost = 0;
		mShare = 0;
		mMarketValue = 0;
		mMainBusinessIncome = 0;
		mMainBusinessIncomeInYear = 0;
		mNetProfit = 0;
		mNetProfitInYear = 0;
		mNetProfitMargin = 0;
		mDebtToNetAssetsRatio = 0;
		mBookValuePerShare = 0;
		mCashFlowPerShare = 0;
		mNetProfitPerShare = 0;
		mNetProfitPerShareInYear = 0;
		mGridProfit = 0;
		mRate = 0;
		mDividend = 0;
		mDividendInYear = 0;
		mYield = 0;
		mYieldInYear = 0;
		mDividendRatio = 0;
		mDividendRatioInYear = 0;
		mRDate = "";
		mAdaptiveDate = "";
		mStatus = "";
	}

	@Override
	public ContentValues getContentValues() {
		ContentValues contentValues = super.getContentValues();

		contentValues.put(DatabaseContract.COLUMN_FLAG, mFlag);

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

		contentValues.put(DatabaseContract.COLUMN_YEAR, mYear.getThumbnail());
		contentValues.put(DatabaseContract.COLUMN_MONTH6, mMonth6.getThumbnail());
		contentValues.put(DatabaseContract.COLUMN_QUARTER, mQuarter.getThumbnail());
		contentValues.put(DatabaseContract.COLUMN_MONTH2, mMonth2.getThumbnail());
		contentValues.put(DatabaseContract.COLUMN_MONTH, mMonth.getThumbnail());
		contentValues.put(DatabaseContract.COLUMN_WEEK, mWeek.getThumbnail());
		contentValues.put(DatabaseContract.COLUMN_DAY, mDay.getThumbnail());
		contentValues.put(DatabaseContract.COLUMN_MIN60, mMin60.getThumbnail());
		contentValues.put(DatabaseContract.COLUMN_MIN30, mMin30.getThumbnail());
		contentValues.put(DatabaseContract.COLUMN_MIN15, mMin15.getThumbnail());
		contentValues.put(DatabaseContract.COLUMN_MIN5, mMin5.getThumbnail());

		contentValues.put(DatabaseContract.COLUMN_YEAR_LEVEL, mYear.getLevel());
		contentValues.put(DatabaseContract.COLUMN_MONTH6_LEVEL, mMonth6.getLevel());
		contentValues.put(DatabaseContract.COLUMN_QUARTER_LEVEL, mQuarter.getLevel());
		contentValues.put(DatabaseContract.COLUMN_MONTH2_LEVEL, mMonth2.getLevel());
		contentValues.put(DatabaseContract.COLUMN_MONTH_LEVEL, mMonth.getLevel());
		contentValues.put(DatabaseContract.COLUMN_WEEK_LEVEL, mWeek.getLevel());
		contentValues.put(DatabaseContract.COLUMN_DAY_LEVEL, mDay.getLevel());
		contentValues.put(DatabaseContract.COLUMN_MIN60_LEVEL, mMin60.getLevel());
		contentValues.put(DatabaseContract.COLUMN_MIN30_LEVEL, mMin30.getLevel());
		contentValues.put(DatabaseContract.COLUMN_MIN15_LEVEL, mMin15.getLevel());
		contentValues.put(DatabaseContract.COLUMN_MIN5_LEVEL, mMin5.getLevel());

		contentValues.put(DatabaseContract.COLUMN_ROI, mRoi);
		contentValues.put(DatabaseContract.COLUMN_IR, mIR);
		contentValues.put(DatabaseContract.COLUMN_IRR, mIRR);
		contentValues.put(DatabaseContract.COLUMN_ROE, mRoe);
		contentValues.put(DatabaseContract.COLUMN_PE, mPe);
		contentValues.put(DatabaseContract.COLUMN_PB, mPb);
		contentValues.put(DatabaseContract.COLUMN_PR, mPr);

		contentValues.put(DatabaseContract.COLUMN_HOLD, mHold);
		contentValues.put(DatabaseContract.COLUMN_PROFIT, mProfit);
		contentValues.put(DatabaseContract.COLUMN_BONUS, mBonus);
		contentValues.put(DatabaseContract.COLUMN_BONUS_IN_YEAR, mBonusInYear);
		contentValues.put(DatabaseContract.COLUMN_VALUATION, mValuation);
		contentValues.put(DatabaseContract.COLUMN_COST, mCost);
		contentValues.put(DatabaseContract.COLUMN_SHARE, mShare);
		contentValues.put(DatabaseContract.COLUMN_MARKET_VALUE, mMarketValue);
		contentValues.put(DatabaseContract.COLUMN_MAIN_BUSINESS_INCOME, mMainBusinessIncome);
		contentValues.put(DatabaseContract.COLUMN_MAIN_BUSINESS_INCOME_IN_YEAR, mMainBusinessIncomeInYear);
		contentValues.put(DatabaseContract.COLUMN_NET_PROFIT, mNetProfit);
		contentValues.put(DatabaseContract.COLUMN_NET_PROFIT_IN_YEAR, mNetProfitInYear);
		contentValues.put(DatabaseContract.COLUMN_NET_PROFIT_MARGIN, mNetProfitMargin);
		contentValues.put(DatabaseContract.COLUMN_DEBT_TO_NET_ASSETS_RATIO, mDebtToNetAssetsRatio);
		contentValues.put(DatabaseContract.COLUMN_BOOK_VALUE_PER_SHARE, mBookValuePerShare);
		contentValues.put(DatabaseContract.COLUMN_CASH_FLOW_PER_SHARE, mCashFlowPerShare);
		contentValues.put(DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE, mNetProfitPerShare);
		contentValues.put(DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE_IN_YEAR, mNetProfitPerShareInYear);

		contentValues.put(DatabaseContract.COLUMN_GRID_PROFIT, mGridProfit);
		contentValues.put(DatabaseContract.COLUMN_RATE, mRate);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND, mDividend);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND_IN_YEAR, mDividendInYear);
		contentValues.put(DatabaseContract.COLUMN_YIELD, mYield);
		contentValues.put(DatabaseContract.COLUMN_YIELD_IN_YEAR, mYieldInYear);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND_RATIO, mDividendRatio);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND_RATIO_IN_YEAR, mDividendRatioInYear);
		contentValues.put(DatabaseContract.COLUMN_R_DATE, mRDate);
		contentValues.put(DatabaseContract.COLUMN_ADAPTIVE_DATE, mAdaptiveDate);
		contentValues.put(DatabaseContract.COLUMN_STATUS, mStatus);

		return contentValues;
	}

	public ContentValues getContentValuesInformation() {
		ContentValues contentValues = super.getContentValues();

		contentValues.put(DatabaseContract.COLUMN_CLASSES, mClasses);
		contentValues.put(DatabaseContract.COLUMN_PINYIN, mPinyin);
		contentValues.put(DatabaseContract.COLUMN_SHARE, mShare);
		return contentValues;
	}

	public ContentValues getContentValuesRealTime() {
		ContentValues contentValues = super.getContentValues();

		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		contentValues.put(DatabaseContract.COLUMN_PRICE, mPrice);
		contentValues.put(DatabaseContract.COLUMN_CHANGE, mChange);
		contentValues.put(DatabaseContract.COLUMN_NET, mNet);
		contentValues.put(DatabaseContract.COLUMN_VOLUME, mVolume);
		contentValues.put(DatabaseContract.COLUMN_VALUE, mValue);
		return contentValues;
	}

	public ContentValues getContentValuesEdit() {
		ContentValues contentValues = super.getContentValues();

		contentValues.put(DatabaseContract.COLUMN_FLAG, mFlag);
		contentValues.put(DatabaseContract.COLUMN_CLASSES, mClasses);
		contentValues.put(DatabaseContract.COLUMN_SE, mSE);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		contentValues.put(DatabaseContract.COLUMN_GRID_PROFIT, mGridProfit);
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

		setPrice(stock.mPrice);
		setChange(stock.mChange);
		setNet(stock.mNet);
		setVolume(stock.mVolume);
		setValue(stock.mValue);

		mYear.setThumbnail(stock.mYear.getThumbnail());
		mMonth6.setThumbnail(stock.mMonth6.getThumbnail());
		mQuarter.setThumbnail(stock.mQuarter.getThumbnail());
		mMonth2.setThumbnail(stock.mMonth2.getThumbnail());
		mMonth.setThumbnail(stock.mMonth.getThumbnail());
		mWeek.setThumbnail(stock.mWeek.getThumbnail());
		mDay.setThumbnail(stock.mDay.getThumbnail());
		mMin60.setThumbnail(stock.mMin60.getThumbnail());
		mMin30.setThumbnail(stock.mMin30.getThumbnail());
		mMin15.setThumbnail(stock.mMin15.getThumbnail());
		mMin5.setThumbnail(stock.mMin5.getThumbnail());

		mYear.setLevel(stock.mYear.getLevel());
		mMonth6.setLevel(stock.mMonth6.getLevel());
		mQuarter.setLevel(stock.mQuarter.getLevel());
		mMonth2.setLevel(stock.mMonth2.getLevel());
		mMonth.setLevel(stock.mMonth.getLevel());
		mWeek.setLevel(stock.mWeek.getLevel());
		mDay.setLevel(stock.mDay.getLevel());
		mMin60.setLevel(stock.mMin60.getLevel());
		mMin30.setLevel(stock.mMin30.getLevel());
		mMin15.setLevel(stock.mMin15.getLevel());
		mMin5.setLevel(stock.mMin5.getLevel());

		setFlag(stock.mFlag);

		setRoi(stock.mRoi);
		setIR(stock.mIR);
		setIRR(stock.mIRR);
		setRoe(stock.mRoe);
		setPe(stock.mPe);
		setPb(stock.mPb);
		setPr(stock.mPr);
		setHold(stock.mHold);
		setProfit(stock.mProfit);
		setBonus(stock.mBonus);
		setBonusInYear(stock.mBonusInYear);
		setValuation(stock.mValuation);
		setCost(stock.mCost);
		setShare(stock.mShare);
		setMarketValue(stock.mMarketValue);
		setMainBusinessIncome(stock.mMainBusinessIncome);
		setMainBusinessIncomeInYear(stock.mMainBusinessIncomeInYear);
		setNetProfit(stock.mNetProfit);
		setNetProfitInYear(stock.mNetProfitInYear);
		setNetProfitMargin(stock.mNetProfitMargin);
		setDebtToNetAssetsRatio(stock.mDebtToNetAssetsRatio);
		setBookValuePerShare(stock.mBookValuePerShare);
		setCashFlowPerShare(stock.mCashFlowPerShare);
		setNetProfitPerShare(stock.mNetProfitPerShare);
		setNetProfitPerShareInYear(stock.mNetProfitPerShareInYear);
		setGridProfit(stock.mGridProfit);
		setRate(stock.mRate);
		setDividend(stock.mDividend);
		setDividendInYear(stock.mDividendInYear);
		setYield(stock.mYield);
		setYieldInYear(stock.mYieldInYear);
		setDividendRatio(stock.mDividendRatio);
		setDividendRatioInYear(stock.mDividendRatioInYear);
		setRDate(stock.mRDate);
		setAdaptiveDate(stock.mAdaptiveDate);
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
		setPrice(cursor);
		setChange(cursor);
		setNet(cursor);
		setVolume(cursor);
		setValue(cursor);

		mYear.setThumbnail(cursor);
		mMonth6.setThumbnail(cursor);
		mQuarter.setThumbnail(cursor);
		mMonth2.setThumbnail(cursor);
		mMonth.setThumbnail(cursor);
		mWeek.setThumbnail(cursor);
		mDay.setThumbnail(cursor);
		mMin60.setThumbnail(cursor);
		mMin30.setThumbnail(cursor);
		mMin15.setThumbnail(cursor);
		mMin5.setThumbnail(cursor);

		mYear.setLevel(cursor);
		mMonth6.setLevel(cursor);
		mQuarter.setLevel(cursor);
		mMonth2.setLevel(cursor);
		mMonth.setLevel(cursor);
		mWeek.setLevel(cursor);
		mDay.setLevel(cursor);
		mMin60.setLevel(cursor);
		mMin30.setLevel(cursor);
		mMin15.setLevel(cursor);
		mMin5.setLevel(cursor);

		setFlag(cursor);

		setRoi(cursor);
		setIR(cursor);
		setIRR(cursor);
		setRoe(cursor);
		setPe(cursor);
		setPb(cursor);
		setPr(cursor);
		setHold(cursor);
		setProfit(cursor);
		setBonus(cursor);
		setBonusInYear(cursor);
		setValuation(cursor);
		setCost(cursor);
		setShare(cursor);
		setMarketValue(cursor);
		setMainBusinessIncome(cursor);
		setMainBusinessIncomeInYear(cursor);
		setNetProfit(cursor);
		setNetProfitInYear(cursor);
		setNetProfitMargin(cursor);
		setDebtToNetAssetsRatio(cursor);
		setBookValuePerShare(cursor);
		setCashFlowPerShare(cursor);
		setNetProfitPerShare(cursor);
		setNetProfitPerShareInYear(cursor);

		setGridProfit(cursor);
		setRate(cursor);
		setDividend(cursor);
		setDividendInYear(cursor);
		setYield(cursor);
		setYieldInYear(cursor);
		setDividendRatio(cursor);
		setDividendRatioInYear(cursor);
		setRDate(cursor);
		setAdaptiveDate(cursor);
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

	public String getSeCode() {
		return mSE + mCode;
	}

	public String getSeCodeUpperCase() {
		return mSE.toUpperCase() + mCode;
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

	public double getValue() {
		return mValue;
	}

	public void setValue(double value) {
		mValue = value;
	}

	void setValue(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setValue(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VALUE)));
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

	public double getBonusInYear() {
		return mBonusInYear;
	}

	public void setBonusInYear(double bonusInYear) {
		mBonusInYear = bonusInYear;
	}

	void setBonusInYear(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setBonusInYear(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_BONUS_IN_YEAR)));
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

	public double getShare() {
		return mShare;
	}

	public void setShare(double share) {
		mShare = share;
	}

	void setShare(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setShare(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_SHARE)));
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

	public double getGridProfit() {
		return mGridProfit;
	}

	public void setGridProfit(double gridProfit) {
		mGridProfit = gridProfit;
	}

	void setGridProfit(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setGridProfit(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_GRID_PROFIT)));
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

	public double getIR() {
		return mIR;
	}

	public void setIR(double ir) {
		mIR = ir;
	}

	void setIR(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setIR(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_IR)));
	}

	public double getIRR() {
		return mIRR;
	}

	public void setIRR(double irr) {
		mIRR = irr;
	}

	void setIRR(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setIRR(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_IRR)));
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

	public double getPr() {
		return mPr;
	}

	public void setPr(double pr) {
		mPr = pr;
	}

	void setPr(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setPr(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PR)));
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

	public double getDividendInYear() {
		return mDividendInYear;
	}

	public void setDividendInYear(double dividendInYear) {
		mDividendInYear = dividendInYear;
	}

	void setDividendInYear(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setDividendInYear(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIVIDEND_IN_YEAR)));
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

	public double getYieldInYear() {
		return mYieldInYear;
	}

	public void setYieldInYear(double yieldInYear) {
		mYieldInYear = yieldInYear;
	}

	void setYieldInYear(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setYieldInYear(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_YIELD_IN_YEAR)));
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

	public double getDividendRatioInYear() {
		return mDividendRatioInYear;
	}

	public void setDividendRatioInYear(double dividendRatioInYear) {
		mDividendRatioInYear = dividendRatioInYear;
	}

	void setDividendRatioInYear(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setDividendRatioInYear(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DIVIDEND_RATIO_IN_YEAR)));
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

	public String getAdaptiveDate() {
		return mAdaptiveDate;
	}

	public void setAdaptiveDate(String adaptiveDate) {
		mAdaptiveDate = adaptiveDate;
	}

	void setAdaptiveDate(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setAdaptiveDate(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ADAPTIVE_DATE)));
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

	public ArrayList<StockShare> getStockShareList() {
		return mStockShareList;
	}

	public ArrayList<StockBonus> getStockBonusList() {
		return mStockBonusList;
	}

	public ArrayList<StockData> getStockDataList(String period) {
		ArrayList<StockData> result;
		if (TextUtils.equals(period, Period.YEAR)) {
			result = mYear.getStockDataList();
		} else if (TextUtils.equals(period, Period.MONTH6)) {
			result = mMonth6.getStockDataList();
		} else if (TextUtils.equals(period, Period.QUARTER)) {
			result = mQuarter.getStockDataList();
		} else if (TextUtils.equals(period, Period.MONTH2)) {
			result = mMonth2.getStockDataList();
		} else if (TextUtils.equals(period, Period.MONTH)) {
			result = mMonth.getStockDataList();
		} else if (TextUtils.equals(period, Period.WEEK)) {
			result = mWeek.getStockDataList();
		} else if (TextUtils.equals(period, Period.DAY)) {
			result = mDay.getStockDataList();
		} else if (TextUtils.equals(period, Period.MIN60)) {
			result = mMin60.getStockDataList();
		} else if (TextUtils.equals(period, Period.MIN30)) {
			result = mMin30.getStockDataList();
		} else if (TextUtils.equals(period, Period.MIN15)) {
			result = mMin15.getStockDataList();
		} else if (TextUtils.equals(period, Period.MIN5)) {
			result = mMin5.getStockDataList();
		} else {
			result = new ArrayList<>();
		}
		return result;
	}

	public ArrayList<StockData> getVertexList(String period, int level) {
		ArrayList<StockData> result;
		if (TextUtils.equals(period, Period.YEAR)) {
			result = mYear.getVertexList(level);
		} else if (TextUtils.equals(period, Period.MONTH6)) {
			result = mMonth6.getVertexList(level);
		} else if (TextUtils.equals(period, Period.QUARTER)) {
			result = mQuarter.getVertexList(level);
		} else if (TextUtils.equals(period, Period.MONTH2)) {
			result = mMonth2.getVertexList(level);
		} else if (TextUtils.equals(period, Period.MONTH)) {
			result = mMonth.getVertexList(level);
		} else if (TextUtils.equals(period, Period.WEEK)) {
			result = mWeek.getVertexList(level);
		} else if (TextUtils.equals(period, Period.DAY)) {
			result = mDay.getVertexList(level);
		} else if (TextUtils.equals(period, Period.MIN60)) {
			result = mMin60.getVertexList(level);
		} else if (TextUtils.equals(period, Period.MIN30)) {
			result = mMin30.getVertexList(level);
		} else if (TextUtils.equals(period, Period.MIN15)) {
			result = mMin15.getVertexList(level);
		} else if (TextUtils.equals(period, Period.MIN5)) {
			result = mMin5.getVertexList(level);
		} else {
			result = new ArrayList<>();
		}
		return result;
	}

	public ArrayList<StockData> getDataList(String period, int level) {
		ArrayList<StockData> result;
		if (TextUtils.equals(period, Period.YEAR)) {
			result = mYear.getDataList(level);
		} else if (TextUtils.equals(period, Period.MONTH6)) {
			result = mMonth6.getDataList(level);
		} else if (TextUtils.equals(period, Period.QUARTER)) {
			result = mQuarter.getDataList(level);
		} else if (TextUtils.equals(period, Period.MONTH2)) {
			result = mMonth2.getDataList(level);
		} else if (TextUtils.equals(period, Period.MONTH)) {
			result = mMonth.getDataList(level);
		} else if (TextUtils.equals(period, Period.WEEK)) {
			result = mWeek.getDataList(level);
		} else if (TextUtils.equals(period, Period.DAY)) {
			result = mDay.getDataList(level);
		} else if (TextUtils.equals(period, Period.MIN60)) {
			result = mMin60.getDataList(level);
		} else if (TextUtils.equals(period, Period.MIN30)) {
			result = mMin30.getDataList(level);
		} else if (TextUtils.equals(period, Period.MIN15)) {
			result = mMin15.getDataList(level);
		} else if (TextUtils.equals(period, Period.MIN5)) {
			result = mMin5.getDataList(level);
		} else {
			result = new ArrayList<>();
		}
		return result;
	}

	public ArrayList<StockTrend> getStockTrendList(String period, int level) {
		ArrayList<StockTrend> result;
		if (TextUtils.equals(period, Period.YEAR)) {
			result = mYear.getStockTrendList(level);
		} else if (TextUtils.equals(period, Period.MONTH6)) {
			result = mMonth6.getStockTrendList(level);
		} else if (TextUtils.equals(period, Period.QUARTER)) {
			result = mQuarter.getStockTrendList(level);
		} else if (TextUtils.equals(period, Period.MONTH2)) {
			result = mMonth2.getStockTrendList(level);
		} else if (TextUtils.equals(period, Period.MONTH)) {
			result = mMonth.getStockTrendList(level);
		} else if (TextUtils.equals(period, Period.WEEK)) {
			result = mWeek.getStockTrendList(level);
		} else if (TextUtils.equals(period, Period.DAY)) {
			result = mDay.getStockTrendList(level);
		} else if (TextUtils.equals(period, Period.MIN60)) {
			result = mMin60.getStockTrendList(level);
		} else if (TextUtils.equals(period, Period.MIN30)) {
			result = mMin30.getStockTrendList(level);
		} else if (TextUtils.equals(period, Period.MIN15)) {
			result = mMin15.getStockTrendList(level);
		} else if (TextUtils.equals(period, Period.MIN5)) {
			result = mMin5.getStockTrendList(level);
		} else {
			result = new ArrayList<>();
		}
		return result;
	}

	public byte[] getThumbnail(String period) {
		byte[] thumbnail = null;
		if (TextUtils.equals(period, Period.YEAR)) {
			thumbnail = mYear.getThumbnail();
		} else if (TextUtils.equals(period, Period.MONTH6)) {
			thumbnail = mMonth6.getThumbnail();
		} else if (TextUtils.equals(period, Period.QUARTER)) {
			thumbnail = mQuarter.getThumbnail();
		} else if (TextUtils.equals(period, Period.MONTH2)) {
			thumbnail = mMonth2.getThumbnail();
		} else if (TextUtils.equals(period, Period.MONTH)) {
			thumbnail = mMonth.getThumbnail();
		} else if (TextUtils.equals(period, Period.WEEK)) {
			thumbnail = mWeek.getThumbnail();
		} else if (TextUtils.equals(period, Period.DAY)) {
			thumbnail = mDay.getThumbnail();
		} else if (TextUtils.equals(period, Period.MIN60)) {
			thumbnail = mMin60.getThumbnail();
		} else if (TextUtils.equals(period, Period.MIN30)) {
			thumbnail = mMin30.getThumbnail();
		} else if (TextUtils.equals(period, Period.MIN15)) {
			thumbnail = mMin15.getThumbnail();
		} else if (TextUtils.equals(period, Period.MIN5)) {
			thumbnail = mMin5.getThumbnail();
		}
		return thumbnail;
	}

	public void setThumbnail(String period, byte[] thumbnail) {
		if (TextUtils.equals(period, Period.YEAR)) {
			mYear.setThumbnail(thumbnail);
		} else if (TextUtils.equals(period, Period.MONTH6)) {
			mMonth6.setThumbnail(thumbnail);
		} else if (TextUtils.equals(period, Period.QUARTER)) {
			mQuarter.setThumbnail(thumbnail);
		} else if (TextUtils.equals(period, Period.MONTH2)) {
			mMonth2.setThumbnail(thumbnail);
		} else if (TextUtils.equals(period, Period.MONTH)) {
			mMonth.setThumbnail(thumbnail);
		} else if (TextUtils.equals(period, Period.WEEK)) {
			mWeek.setThumbnail(thumbnail);
		} else if (TextUtils.equals(period, Period.DAY)) {
			mDay.setThumbnail(thumbnail);
		} else if (TextUtils.equals(period, Period.MIN60)) {
			mMin60.setThumbnail(thumbnail);
		} else if (TextUtils.equals(period, Period.MIN30)) {
			mMin30.setThumbnail(thumbnail);
		} else if (TextUtils.equals(period, Period.MIN15)) {
			mMin15.setThumbnail(thumbnail);
		} else if (TextUtils.equals(period, Period.MIN5)) {
			mMin5.setThumbnail(thumbnail);
		}
	}

	public int getLevel(String period) {
		int level = StockTrend.LEVEL_NONE;
		if (TextUtils.equals(period, Period.YEAR)) {
			level = mYear.getLevel();
		} else if (TextUtils.equals(period, Period.MONTH6)) {
			level = mMonth6.getLevel();
		} else if (TextUtils.equals(period, Period.QUARTER)) {
			level = mQuarter.getLevel();
		} else if (TextUtils.equals(period, Period.MONTH2)) {
			level = mMonth2.getLevel();
		} else if (TextUtils.equals(period, Period.MONTH)) {
			level = mMonth.getLevel();
		} else if (TextUtils.equals(period, Period.WEEK)) {
			level = mWeek.getLevel();
		} else if (TextUtils.equals(period, Period.DAY)) {
			level = mDay.getLevel();
		} else if (TextUtils.equals(period, Period.MIN60)) {
			level = mMin60.getLevel();
		} else if (TextUtils.equals(period, Period.MIN30)) {
			level = mMin30.getLevel();
		} else if (TextUtils.equals(period, Period.MIN15)) {
			level = mMin15.getLevel();
		} else if (TextUtils.equals(period, Period.MIN5)) {
			level = mMin5.getLevel();
		}
		return level;
	}

	public void setLevel(String period, int level) {
		if (TextUtils.equals(period, Period.YEAR)) {
			mYear.setLevel(level);
		} else if (TextUtils.equals(period, Period.MONTH6)) {
			mMonth6.setLevel(level);
		} else if (TextUtils.equals(period, Period.QUARTER)) {
			mQuarter.setLevel(level);
		} else if (TextUtils.equals(period, Period.MONTH2)) {
			mMonth2.setLevel(level);
		} else if (TextUtils.equals(period, Period.MONTH)) {
			mMonth.setLevel(level);
		} else if (TextUtils.equals(period, Period.WEEK)) {
			mWeek.setLevel(level);
		} else if (TextUtils.equals(period, Period.DAY)) {
			mDay.setLevel(level);
		} else if (TextUtils.equals(period, Period.MIN60)) {
			mMin60.setLevel(level);
		} else if (TextUtils.equals(period, Period.MIN30)) {
			mMin30.setLevel(level);
		} else if (TextUtils.equals(period, Period.MIN15)) {
			mMin15.setLevel(level);
		} else if (TextUtils.equals(period, Period.MIN5)) {
			mMin5.setLevel(level);
		}
	}

	public void setupMarketValue() {
		if (mPrice == 0) {
			mMarketValue = 0;
			return;
		}

		if (mShare == 0) {
			mMarketValue = 0;
			return;
		}

		mMarketValue = Utility.Round2(mPrice * mShare);
	}

	public void setupNetProfitMargin() {
		if ((mNetProfitInYear <= 0) || (mMainBusinessIncomeInYear <= 0)) {
			mNetProfitMargin = 0;
			return;
		}

		mNetProfitMargin = Utility.Round2(100 * mNetProfitInYear / mMainBusinessIncomeInYear);
	}

	public void setupNetProfitPerShare() {
		if (mShare == 0) {
			mNetProfitPerShare = 0;
			return;
		}

		mNetProfitPerShare = Utility.Round2(mNetProfit / mShare);
	}

	public void setupNetProfitPerShareInYear(
			ArrayList<StockFinancial> stockFinancialList) {
		double mainBusinessIncome = 0;
		double netProfit = 0;
		double netProfitPerShare = 0;

		if (mShare == 0) {
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
				netProfitPerShare = stockFinancial.getNetProfit() / mShare;
			} else {
				mainBusinessIncome = stockFinancial.getMainBusinessIncome() - prev.getMainBusinessIncome();
				netProfit = stockFinancial.getNetProfit() - prev.getNetProfit();
				netProfitPerShare = (stockFinancial.getNetProfit() - prev
						.getNetProfit()) / mShare;
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

		if (mShare == 0) {
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
				netProfitPerShare = stockFinancial.getNetProfit() / mShare;
			} else {
				netProfitPerShare = (stockFinancial.getNetProfit() - prev
						.getNetProfit()) / mShare;
			}

			netProfitPerShareLastYear += netProfitPerShare;
		}

		if (netProfitPerShareLastYear == 0) {
			mRate = 0;
			return;
		}

		mRate = Utility.Round2(mNetProfitPerShareInYear / netProfitPerShareLastYear);
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

		mRoe = Utility.Round2(100 * mNetProfitPerShareInYear
				/ bookValuePerShare);
	}

	public void setupPe() {
		if (mPrice == 0 || mNetProfitPerShareInYear <= 0) {
			mPe = 0;
			return;
		}

		mPe = Utility.Round2(mPrice / mNetProfitPerShareInYear);
	}

	public void setupPb() {
		if (mBookValuePerShare == 0) {
			mPb = 0;
			return;
		}

		mPb = Utility.Round2(mPrice / mBookValuePerShare);
	}

	public void setupPr() {
		if (mPb == 0 || mPe == 0 || mRoe == 0) {
			mPr = 0;
			return;
		}

		mPr = Utility.Round2(mRoe / mPb);
	}

	public void setupBonus() {
		if (mHold == 0) {
			mBonus = 0;
			mBonusInYear = 0;
			return;
		}

		mBonus = mDividend / 10.0 * mHold;
		mBonusInYear = mDividendInYear / 10.0 * mHold;
	}

	public void setupYield() {
		if (mPrice == 0) {
			mYield = 0;
			mYieldInYear = 0;
			return;
		}

		mYield = Utility.Round2(100.0 * mDividend / 10.0 / mPrice);
		mYieldInYear = Utility.Round2(100.0 * mDividendInYear / 10.0 / mPrice);
	}

	public void setupDividendRatio() {
		if (mNetProfitPerShareInYear <= 0) {
			mDividendRatio = 0;
			mDividendRatioInYear = 0;
			return;
		}

		mDividendRatio = Utility.Round2((100 * mDividend / 10.0) / mNetProfitPerShareInYear);
		mDividendRatioInYear = Utility.Round2((100 * mDividendInYear / 10.0) / mNetProfitPerShareInYear);
	}

	public void setupRoi() {
		if ((mNetProfitPerShareInYear <= 0 || mPrice == 0 || mNetProfitInYear <= 0) || (mMainBusinessIncomeInYear <= 0)) {
			mRoi = 0;
			return;
		}

//		mRoi = Utility.Round(mRoe * (100.0 * 1.0 / mPe + mYield) * mNetProfitMargin * mRate * ROI_COEFFICIENT,
//				Constant.DOUBLE_FIXED_DECIMAL);
//		mRoi = Utility.Round(mRoe * ( mNetProfitPerShareInYear / mPrice) * (mNetProfitInYear / mMainBusinessIncomeInYear) * ROI_COEFFICIENT);
		mRoi = Utility.Round2(mRoe * (mNetProfitPerShareInYear / mPrice) * (mNetProfitInYear / mMainBusinessIncomeInYear) * mRate * mYieldInYear * ROI_COEFFICIENT);
	}

	public void setupIRR() {
		if (mPe == 0 || mRoe == 0 || mPrice == 0) {
			mIR = 0;
			mIRR = 0;
			return;
		}

		IRR.calculate(mPe, mRoe / 100, mDividendRatioInYear / 100, mPrice);
		mIR = Utility.Round2(IRR.getIR());
		mIRR = Utility.Round2(100 * IRR.getIRR());
	}

	public String getSeUrl() {
		String result = "";
		if (TextUtils.equals(getSE(), SE_SH)) {
			result = SE_SH_URL_BASE + getCode();
		} else if (TextUtils.equals(getSE(), SE_SZ)) {
			result = SE_SZ_URL_BASE + getCode();
		}
		return result;
	}

	public String getPriceNetString() {
		StringBuilder result = new StringBuilder();
		result.append(getPrice()).append(" ");
		result.append(getNet()).append(Symbol.PERCENT);
		return result.toString();
	}

	public String getNamePriceNetString() {
		StringBuilder result = new StringBuilder();
		result.append(getName()).append(" ");
		result.append(getPriceNetString());
		return result.toString();
	}
}
