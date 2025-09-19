package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.android.orion.data.IRR;
import com.android.orion.data.Period;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Symbol;
import com.android.orion.utility.Utility;

import java.util.ArrayList;

public class Stock extends DatabaseTable {

	public static final String ACCOUNT_A = "A";
	public static final String ACCOUNT_B = "B";

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

	private final ArrayMap<String, Period> mPeriodMap = new ArrayMap<>();
	{
		for (String period : Period.PERIODS) {
			mPeriodMap.put(period, new Period(period));
		}
	}
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
	private double mRate;
	private double mDividend;
	private double mDividendInYear;
	private double mYield;
	private double mYieldInYear;
	private double mDividendRatio;
	private double mDividendRatioInYear;
	private String mRDate;
	private String mStatus;
	private double mBuyProfit;
	private double mSellProfit;
	private double mPast;
	private double mDuration;
	private byte[] mThumbnail;

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
		mRate = 0;
		mDividend = 0;
		mDividendInYear = 0;
		mYield = 0;
		mYieldInYear = 0;
		mDividendRatio = 0;
		mDividendRatioInYear = 0;
		mRDate = "";
		mStatus = "";
		mBuyProfit = 0;
		mSellProfit = 0;
		mPast = 0;
		mDuration = 0;
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

		for (String period : Period.PERIODS) {
			contentValues.put(DatabaseContract.COLUMN_PERIOD_THUMBNAIL(period), getPeriod(period).getThumbnail());
			contentValues.put(DatabaseContract.COLUMN_PERIOD_LEVEL(period), getPeriod(period).getLevel());
			contentValues.put(DatabaseContract.COLUMN_PERIOD_TREND(period), getPeriod(period).getTrend());
		}

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

		contentValues.put(DatabaseContract.COLUMN_RATE, mRate);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND, mDividend);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND_IN_YEAR, mDividendInYear);
		contentValues.put(DatabaseContract.COLUMN_YIELD, mYield);
		contentValues.put(DatabaseContract.COLUMN_YIELD_IN_YEAR, mYieldInYear);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND_RATIO, mDividendRatio);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND_RATIO_IN_YEAR, mDividendRatioInYear);
		contentValues.put(DatabaseContract.COLUMN_R_DATE, mRDate);
		contentValues.put(DatabaseContract.COLUMN_STATUS, mStatus);
		contentValues.put(DatabaseContract.COLUMN_BUY_PROFIT, mBuyProfit);
		contentValues.put(DatabaseContract.COLUMN_SELL_PROFIT, mSellProfit);
		contentValues.put(DatabaseContract.COLUMN_PAST, mPast);
		contentValues.put(DatabaseContract.COLUMN_DURATION, mDuration);
		contentValues.put(DatabaseContract.COLUMN_THUMBNAIL, mThumbnail);
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
		contentValues.put(DatabaseContract.COLUMN_BUY_PROFIT, mBuyProfit);
		contentValues.put(DatabaseContract.COLUMN_SELL_PROFIT, mSellProfit);
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

		for (String period : Period.PERIODS) {
			getPeriod(period).setThumbnail(stock.getPeriod(period).getThumbnail());
			getPeriod(period).setLevel(stock.getPeriod(period).getLevel());
			getPeriod(period).setTrend(stock.getPeriod(period).getTrend());
		}

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
		setRate(stock.mRate);
		setDividend(stock.mDividend);
		setDividendInYear(stock.mDividendInYear);
		setYield(stock.mYield);
		setYieldInYear(stock.mYieldInYear);
		setDividendRatio(stock.mDividendRatio);
		setDividendRatioInYear(stock.mDividendRatioInYear);
		setRDate(stock.mRDate);
		setStatus(stock.mStatus);
		setBuyProfit(stock.mBuyProfit);
		setSellProfit(stock.mSellProfit);
		setPast(stock.mPast);
		setDuration(stock.mDuration);
		setThumbnail(stock.mThumbnail);
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

		for (String period : Period.PERIODS) {
			getPeriod(period).setThumbnail(cursor);
			getPeriod(period).setLevel(cursor);
			getPeriod(period).setTrend(cursor);
		}

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

		setRate(cursor);
		setDividend(cursor);
		setDividendInYear(cursor);
		setYield(cursor);
		setYieldInYear(cursor);
		setDividendRatio(cursor);
		setDividendRatioInYear(cursor);
		setRDate(cursor);
		setStatus(cursor);
		setBuyProfit(cursor);
		setSellProfit(cursor);
		setPast(cursor);
		setDuration(cursor);
		setThumbnail(cursor);
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

	public double getBuyProfit() {
		return mBuyProfit;
	}

	public void setBuyProfit(double buyProfit) {
		mBuyProfit = buyProfit;
	}

	void setBuyProfit(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setBuyProfit(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_BUY_PROFIT)));
	}

	public double getSellProfit() {
		return mSellProfit;
	}

	public void setSellProfit(double sellProfit) {
		mSellProfit = sellProfit;
	}

	void setSellProfit(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setSellProfit(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_SELL_PROFIT)));
	}

	public double getPast() {
		return mPast;
	}

	public void setPast(double past) {
		mPast = past;
	}

	void setPast(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setPast(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PAST)));
	}

	public double getDuration() {
		return mDuration;
	}

	public void setDuration(double duration) {
		mDuration = duration;
	}

	void setDuration(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setDuration(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_DURATION)));
	}

	public byte[] getThumbnail() {
		return mThumbnail;
	}

	public void setThumbnail(byte[] thumbnail) {
		mThumbnail = thumbnail;
	}

	public void setThumbnail(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}
		setThumbnail(cursor.getBlob(cursor
				.getColumnIndex(DatabaseContract.COLUMN_THUMBNAIL)));
	}

	public byte[] getThumbnail(String period) {
		return getPeriod(period).getThumbnail();
	}

	public void setThumbnail(String period, byte[] thumbnail) {
		getPeriod(period).setThumbnail(thumbnail);
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

	public Period getPeriod(String period) {
		return mPeriodMap.get(period);
	}

	public ArrayList<StockData> getVertexList(String period, int level) {
		return getPeriod(period).getVertexList(level);
	}

	public ArrayList<StockData> getStockDataList(String period, int level) {
		return getPeriod(period).getStockDataList(level);
	}

	public ArrayList<StockTrend> getStockTrendList(String period, int level) {
		return getPeriod(period).getStockTrendList(level);
	}

	public int getLevel(String period) {
		return getPeriod(period).getLevel();
	}

	public void setLevel(String period, int level) {
		getPeriod(period).setLevel(level);
	}

	public String getTrend(String period) {
		return getPeriod(period).getTrend();
	}

	public void setTrend(String period, String trend) {
		getPeriod(period).setTrend(trend);
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

	public String getPriceNetString(String separator) {
		StringBuilder builder = new StringBuilder();
		builder.append(getPrice()).append(separator);
		builder.append(getNet()).append(Symbol.PERCENT);
		return builder.toString();
	}

	public String getNamePriceNetString(String separator) {
		StringBuilder builder = new StringBuilder();
		builder.append(getName()).append(separator);
		builder.append(getPriceNetString(separator));
		return builder.toString();
	}

	public String getTrendString() {
		StringBuilder builder = new StringBuilder();
		for (String period : Period.PERIODS) {
			if (Setting.getPeriod(period)) {
				builder.append(getTrend(period));
			}
		}
		return builder.toString();
	}

	public String toString() {
		return mName + Symbol.TAB
				+ mCode + Symbol.TAB;
	}

	public String toLogString() {
		return mPinyin + Symbol.TAB
				+ mCode + Symbol.TAB;
	}
}
