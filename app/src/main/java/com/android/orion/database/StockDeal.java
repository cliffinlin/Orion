package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.orion.setting.Constant;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.Calendar;

public class StockDeal extends DatabaseTable {

	public static final String ACCOUNT_A = "A";
	public static final String ACCOUNT_B = "B";

	public static final double BUY_STAMP_DUTY_RATE = 0;
	public static final double SELL_STAMP_DUTY_RATE = 1.0 / 1000.0;

	public static final double BUY_TRANSFER_FEE_MIN = 1.0;
	public static final double BUY_TRANSFER_FEE_RATE = 0.2 / 10000.0;

	public static final double SELL_TRANSFER_FEE_MIN = 1.0;
	public static final double SELL_TRANSFER_FEE_RATE = 0.2 / 10000.0;

	public static final double BUY_COMMISSION_FEE_MIN = 5.0;
	public static final double BUY_COMMISSION_FEE_RATE = 5.0 / 10000.0;

	public static final double SELL_COMMISSION_FEE_MIN = 5.0;
	public static final double SELL_COMMISSION_FEE_RATE = 5.0 / 10000.0;

	public static final double DIVIDEND_INCOME_TAX_RATE_10_PERCENT = 10.0 / 100.0;
	public static final double DIVIDEND_INCOME_TAX_RATE_20_PERCENT = 20.0 / 100.0;

	protected String mSE;
	protected String mCode;
	protected String mName;
	protected String mAccount;
	protected String mAction;
	protected double mPrice;
	protected double mNet;
	protected double mBuy;
	protected double mSell;
	protected long mVolume;
	protected double mValue;
	protected double mProfit;


	private double mFee;
	private double mBonus;
	private double mYield;

	public StockDeal() {
		init();
	}

	public StockDeal(StockDeal stockDeal) {
		set(stockDeal);
	}

	public StockDeal(Cursor cursor) {
		set(cursor);
	}

	public void init() {
		super.init();

		setTableName(DatabaseContract.StockDeal.TABLE_NAME);

		mSE = "";
		mCode = "";
		mName = "";
		mAccount = "";
		mAction = "";
		mPrice = 0;
		mNet = 0;
		mBuy = 0;
		mSell = 0;
		mVolume = 0;
		mValue = 0;
		mProfit = 0;
		mFee = 0;
		mBonus = 0;
		mYield = 0;
	}

	@Override
	ContentValues getContentValues(ContentValues contentValues) {
		super.getContentValues(contentValues);

		contentValues.put(DatabaseContract.COLUMN_SE, mSE);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		contentValues.put(DatabaseContract.COLUMN_ACCOUNT, mAccount);
		contentValues.put(DatabaseContract.COLUMN_ACTION, mAction);
		contentValues.put(DatabaseContract.COLUMN_PRICE, mPrice);
		contentValues.put(DatabaseContract.COLUMN_NET, mNet);
		contentValues.put(DatabaseContract.COLUMN_BUY, mBuy);
		contentValues.put(DatabaseContract.COLUMN_SELL, mSell);
		contentValues.put(DatabaseContract.COLUMN_VOLUME, mVolume);
		contentValues.put(DatabaseContract.COLUMN_VALUE, mValue);
		contentValues.put(DatabaseContract.COLUMN_PROFIT, mProfit);
		contentValues.put(DatabaseContract.COLUMN_FEE, mFee);
		contentValues.put(DatabaseContract.COLUMN_BONUS, mBonus);
		contentValues.put(DatabaseContract.COLUMN_YIELD, mYield);

		return contentValues;
	}

	void set(StockDeal stockDeal) {
		if (stockDeal == null) {
			return;
		}

		init();

		super.set(stockDeal);

		setSE(stockDeal.mSE);
		setCode(stockDeal.mCode);
		setName(stockDeal.mName);
		setAccount(stockDeal.mAccount);
		setAction(stockDeal.mAction);
		setPrice(stockDeal.mPrice);
		setNet(stockDeal.mNet);
		setBuy(stockDeal.mBuy);
		setSell(stockDeal.mSell);
		setVolume(stockDeal.mVolume);
		setValue(stockDeal.mValue);
		setProfit(stockDeal.mProfit);
		setFee(stockDeal.mFee);
		setBonus(stockDeal.mBonus);
		setYield(stockDeal.mYield);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		super.set(cursor);

		setSE(cursor);
		setCode(cursor);
		setName(cursor);
		setAccount(cursor);
		setAction(cursor);
		setPrice(cursor);
		setNet(cursor);
		setBuy(cursor);
		setSell(cursor);
		setVolume(cursor);
		setValue(cursor);
		setProfit(cursor);
		setFee(cursor);
		setBonus(cursor);
		setYield(cursor);
	}

	public String getSE() {
		return mSE;
	}

	public void setSE(String se) {
		mSE = se;
	}

	void setSE(Cursor cursor) {
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

	void setCode(Cursor cursor) {
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

	void setName(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setName(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NAME)));
	}

	public String getAccount() {
		return mAccount;
	}

	public void setAccount(String account) {
		mAccount = account;
	}

	void setAccount(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setAccount(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ACCOUNT)));
	}

	public String getAction() {
		return mAction;
	}

	public void setAction(String action) {
		mAction = action;
	}

	void setAction(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setAction(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_ACTION)));
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

	public double getBuy() {
		return mBuy;
	}

	public void setBuy(double buy) {
		mBuy = buy;
	}

	void setBuy(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setBuy(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_BUY)));
	}

	public double getSell() {
		return mSell;
	}

	public void setSell(double sell) {
		mSell = sell;
	}

	void setSell(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setSell(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_SELL)));
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

	public double getValue() {
		return mValue;
	}

	public void setValue(double value) {
		mValue = value;
	}

	void setValue(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setValue(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_VALUE)));
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

	public double getFee() {
		return mFee;
	}

	public void setFee(double fee) {
		mFee = fee;
	}

	void setFee(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setFee(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_FEE)));
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

	public void setupNet() {
		if ((mPrice == 0) || (mVolume == 0)) {
			mNet = 0;
			return;
		}

		if ((mBuy > 0) && (mSell > 0)) {
			mNet = Utility.Round(100 * ((mSell - mBuy) * Math.abs(mVolume) - mFee) / Math.abs(mVolume) / mBuy);
		} else if (mBuy > 0) {
			mNet = Utility.Round(100 * ((mPrice - mBuy) * Math.abs(mVolume) - mFee) / Math.abs(mVolume) / mBuy);
		} else if (mSell > 0) {
			mNet = Utility.Round(100 * (mSell - mPrice) / mSell);
		}
	}

	public void setupValue() {
		if (mVolume == 0) {
			mValue = 0;
			return;
		}

		mValue = Utility.Round(mBuy * Math.abs(mVolume));
	}

	public void setupBuyFee() {
		double buyStampDuty = 0;
		double buyTransferFee = 0;
		double buyCommissionFee = 0;

		if ((mPrice == 0) || (mBuy == 0) || (mVolume == 0)) {
			return;
		}

		buyStampDuty = mBuy * Math.abs(mVolume) * BUY_STAMP_DUTY_RATE;

		buyTransferFee = mBuy * Math.abs(mVolume) * BUY_TRANSFER_FEE_RATE;
		if (buyTransferFee < BUY_TRANSFER_FEE_MIN) {
			buyTransferFee = BUY_TRANSFER_FEE_MIN;
		}

		buyCommissionFee = mBuy * Math.abs(mVolume) * BUY_COMMISSION_FEE_RATE;
		if (buyCommissionFee < BUY_COMMISSION_FEE_MIN) {
			buyCommissionFee = BUY_COMMISSION_FEE_MIN;
		}

		mFee = Utility.Round(buyStampDuty + buyTransferFee + buyCommissionFee);
	}

	public void setupSellFee(ArrayList<ShareBonus> shareBonusList) {
		double sellStampDuty = 0;
		double sellTransferFee = 0;
		double sellCommissionFee = 0;
		double dividendIncomeTax = 0;

		if ((mPrice == 0) || (mSell == 0) || (mVolume == 0)) {
			return;
		}

		if (mSell > 0) {
			sellStampDuty = mSell * Math.abs(mVolume) * SELL_STAMP_DUTY_RATE;
		} else {
			sellStampDuty = mPrice * Math.abs(mVolume) * SELL_STAMP_DUTY_RATE;
		}

		if (mSell > 0) {
			sellTransferFee = mSell * Math.abs(mVolume) * SELL_TRANSFER_FEE_RATE;
		} else {
			sellTransferFee = mPrice * Math.abs(mVolume) * SELL_TRANSFER_FEE_RATE;
		}
		if (sellTransferFee < SELL_TRANSFER_FEE_MIN) {
			sellTransferFee = SELL_TRANSFER_FEE_MIN;
		}

		if (mSell > 0) {
			sellCommissionFee = mSell * Math.abs(mVolume) * SELL_COMMISSION_FEE_RATE;
		} else {
			sellCommissionFee = mPrice * Math.abs(mVolume) * SELL_COMMISSION_FEE_RATE;
		}

		if (sellCommissionFee < SELL_COMMISSION_FEE_MIN) {
			sellCommissionFee = SELL_COMMISSION_FEE_MIN;
		}

		dividendIncomeTax = getDividendIncomeTax(shareBonusList);

		mFee = Utility.Round(sellStampDuty + sellTransferFee + sellCommissionFee + dividendIncomeTax);
	}

	double getDividendIncomeTax(ArrayList<ShareBonus> shareBonusList) {
		ShareBonus shareBonus = new ShareBonus();
		String rDate = "";
		double dividend = 0;
		double result = 0;

		if (shareBonusList == null || shareBonusList.size() < StockData.VERTEX_TYPING_SIZE) {
			return result;
		}

		if ((mPrice == 0) || (mBuy == 0) || (mVolume == 0)) {
			return result;
		}

		for (int i = 0; i < 2; i++) {
			shareBonus = shareBonusList.get(i);
			rDate = shareBonus.getRDate();
			if (TextUtils.isEmpty(rDate) || rDate.contains("--")) {
				continue;
			} else {
				break;
			}
		}

		if (TextUtils.isEmpty(rDate) || rDate.contains("--")) {
			return result;
		}

		dividend = shareBonus.getDividend();
		if (dividend == 0) {
			return result;
		}

		Calendar buyCalendar = Utility.getCalendar(
				getCreated(), Utility.CALENDAR_DATE_FORMAT);

		Calendar sellCalendar = Utility.getCalendar(
				getModified(), Utility.CALENDAR_DATE_FORMAT);

		Calendar rDateCalendar = Utility.getCalendar(
				rDate, Utility.CALENDAR_DATE_FORMAT);

		Calendar rDateCalendarAfterMonth = Utility.getCalendar(
				rDate, Utility.CALENDAR_DATE_FORMAT);
		rDateCalendarAfterMonth.add(Calendar.MONTH, 1);

		Calendar rDateCalendarAfterYear = Utility.getCalendar(
				rDate, Utility.CALENDAR_DATE_FORMAT);
		rDateCalendarAfterYear.add(Calendar.YEAR, 1);

		if (buyCalendar.after(rDateCalendar)) {
			return result;
		}

		if (sellCalendar.before(rDateCalendarAfterMonth)) {
			result = dividend / 10.0 * Math.abs(mVolume) * DIVIDEND_INCOME_TAX_RATE_20_PERCENT;
		} else if (sellCalendar.before(rDateCalendarAfterYear)) {
			result = dividend / 10.0 * Math.abs(mVolume) * DIVIDEND_INCOME_TAX_RATE_10_PERCENT;
		} else {
			result = 0;
		}

		return result;
	}

	public void setupFee(String rDate, double dividend) {
		double buyStampDuty = 0;
		double sellStampDuty = 0;
		double buyTransferFee = 0;
		double sellTransferFee = 0;
		double buyCommissionFee = 0;
		double sellCommissionFee = 0;
		double dividendIncomeTax = 0;

		if ((mPrice == 0) || (mBuy == 0) || (mVolume == 0)) {
			mFee = 0;
			return;
		}

		buyStampDuty = mBuy * Math.abs(mVolume) * BUY_STAMP_DUTY_RATE;

		if (mSell > 0) {
			sellStampDuty = mSell * Math.abs(mVolume) * SELL_STAMP_DUTY_RATE;
		} else {
			sellStampDuty = mPrice * Math.abs(mVolume) * SELL_STAMP_DUTY_RATE;
		}

		buyTransferFee = mBuy * Math.abs(mVolume) * BUY_TRANSFER_FEE_RATE;
		if (buyTransferFee < BUY_TRANSFER_FEE_MIN) {
			buyTransferFee = BUY_TRANSFER_FEE_MIN;
		}

		if (mSell > 0) {
			sellTransferFee = mSell * Math.abs(mVolume) * SELL_TRANSFER_FEE_RATE;
		} else {
			sellTransferFee = mPrice * Math.abs(mVolume) * SELL_TRANSFER_FEE_RATE;
		}

		if (sellTransferFee < SELL_TRANSFER_FEE_MIN) {
			sellTransferFee = SELL_TRANSFER_FEE_MIN;
		}

		buyCommissionFee = mBuy * Math.abs(mVolume) * BUY_COMMISSION_FEE_RATE;
		if (buyCommissionFee < BUY_COMMISSION_FEE_MIN) {
			buyCommissionFee = BUY_COMMISSION_FEE_MIN;
		}

		sellCommissionFee = mPrice * Math.abs(mVolume) * SELL_COMMISSION_FEE_RATE;
		if (sellCommissionFee < SELL_COMMISSION_FEE_MIN) {
			sellCommissionFee = SELL_COMMISSION_FEE_MIN;
		}

		if (dividend > 0) {
			Calendar todayCalendar = Utility.getCalendar(
					Utility.getCurrentDateString(), Utility.CALENDAR_DATE_FORMAT);

			Calendar rDateCalendarAfterMonth = Utility.getCalendar(
					rDate, Utility.CALENDAR_DATE_FORMAT);
			rDateCalendarAfterMonth.add(Calendar.MONTH, 1);

			Calendar rDateCalendarAfterYear = Utility.getCalendar(
					rDate, Utility.CALENDAR_DATE_FORMAT);
			rDateCalendarAfterYear.add(Calendar.YEAR, 1);

			if (todayCalendar.before(rDateCalendarAfterMonth)) {
				dividendIncomeTax = dividend / 10.0 * Math.abs(mVolume) * DIVIDEND_INCOME_TAX_RATE_20_PERCENT;
			} else if (todayCalendar.before(rDateCalendarAfterYear)) {
				dividendIncomeTax = dividend / 10.0 * Math.abs(mVolume) * DIVIDEND_INCOME_TAX_RATE_10_PERCENT;
			} else {
				dividendIncomeTax = 0;
			}
		}

		mFee = Utility.Round(buyStampDuty + sellStampDuty
						+ buyTransferFee + sellTransferFee
						+ buyCommissionFee + sellCommissionFee
						+ dividendIncomeTax);
	}

	public void setupProfit() {
		if ((mPrice == 0) || (mVolume == 0)) {
			mProfit = 0;
			return;
		}

		if ((mBuy > 0) && (mSell > 0)) {
			mProfit = Utility.Round((mSell - mBuy) * Math.abs(mVolume) - mFee);
		} else if (mBuy > 0) {
			mProfit = Utility.Round((mPrice - mBuy) * Math.abs(mVolume) - mFee);
		} else if (mSell > 0) {
			mProfit = Utility.Round((mPrice - mSell) * Math.abs(mVolume));
		}
	}

	public void setupBonus(double dividend) {
		if ((dividend == 0) || (mVolume == 0)) {
			mBonus = 0;
			return;
		}

		mBonus = Utility.Round(dividend / 10.0 * Math.abs(mVolume));
	}

	public void setupYield(double dividend) {
		if (dividend == 0) {
			mYield = 0;
			return;
		}

		if (mBuy > 0) {
			mYield = Utility.Round(100.0 * dividend / 10.0 / mBuy);
		} else {
			mYield = Utility.Round(100.0 * dividend / 10.0 / mPrice);
		}
	}
}
