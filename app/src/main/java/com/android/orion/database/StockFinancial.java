package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

import com.android.orion.utility.Utility;

public class StockFinancial extends DatabaseTable {
	private long mStockId;
	private String mDate;
	private double mBookValuePerShare;
	private double mCashFlowPerShare;
	private double mTotalCurrentAssets;
	private double mTotalAssets;
	private double mTotalLongTermLiabilities;
	private double mMainBusinessIncome;
	private double mMainBusinessIncomeInYear;
	private double mFinancialExpenses;
	private double mNetProfit;
	private double mNetProfitInYear;
	private double mNetProfitMargin;
	private double mTotalShare;
	private double mDebtToNetAssetsRatio;
	private double mNetProfitPerShare;
	private double mNetProfitPerShareInYear;
	private double mRate;
	private double mRoe;
	private double mDividendRatio;

	public StockFinancial() {
		init();
	}

	public StockFinancial(StockFinancial stockFinancial) {
		set(stockFinancial);
	}

	public StockFinancial(Cursor cursor) {
		set(cursor);
	}

	void init() {
		super.init();

		setTableName(DatabaseContract.StockFinancial.TABLE_NAME);

		mStockId = 0;
		mDate = "";
		mBookValuePerShare = 0;
		mCashFlowPerShare = 0;
		mTotalCurrentAssets = 0;
		mTotalAssets = 0;
		mTotalLongTermLiabilities = 0;
		mMainBusinessIncome = 0;
		mMainBusinessIncomeInYear = 0;
		mFinancialExpenses = 0;
		mNetProfit = 0;
		mNetProfitInYear = 0;
		mNetProfitMargin = 0;
		mTotalShare = 0;
		mDebtToNetAssetsRatio = 0;
		mNetProfitPerShare = 0;
		mNetProfitPerShareInYear = 0;
		mRate = 0;
		mRoe = 0;
		mDividendRatio = 0;
	}

	@Override
	ContentValues getContentValues(ContentValues contentValues) {
		super.getContentValues(contentValues);

		contentValues.put(DatabaseContract.COLUMN_STOCK_ID, mStockId);
		contentValues.put(DatabaseContract.COLUMN_DATE, mDate);
		contentValues.put(DatabaseContract.COLUMN_BOOK_VALUE_PER_SHARE,
				mBookValuePerShare);
		contentValues.put(DatabaseContract.COLUMN_CASH_FLOW_PER_SHARE,
				mCashFlowPerShare);
		contentValues.put(DatabaseContract.COLUMN_TOTAL_CURRENT_ASSETS,
				mTotalCurrentAssets);
		contentValues.put(DatabaseContract.COLUMN_TOTAL_ASSETS, mTotalAssets);
		contentValues.put(DatabaseContract.COLUMN_TOTAL_LONG_TERM_LIABILITIES,
				mTotalLongTermLiabilities);
		contentValues.put(DatabaseContract.COLUMN_MAIN_BUSINESS_INCOME,
				mMainBusinessIncome);
		contentValues.put(DatabaseContract.COLUMN_MAIN_BUSINESS_INCOME_IN_YEAR,
				mMainBusinessIncomeInYear);
		contentValues.put(DatabaseContract.COLUMN_FINANCIAL_EXPENSES,
				mFinancialExpenses);
		contentValues.put(DatabaseContract.COLUMN_NET_PROFIT, mNetProfit);
		contentValues.put(DatabaseContract.COLUMN_NET_PROFIT_IN_YEAR, mNetProfitInYear);
		contentValues.put(DatabaseContract.COLUMN_NET_PROFIT_MARGIN, mNetProfitMargin);
		contentValues.put(DatabaseContract.COLUMN_TOTAL_SHARE, mTotalShare);
		contentValues.put(DatabaseContract.COLUMN_DEBT_TO_NET_ASSETS_RATIO,
				mDebtToNetAssetsRatio);
		contentValues.put(DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE,
				mNetProfitPerShare);
		contentValues.put(DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE_IN_YEAR,
				mNetProfitPerShareInYear);
		contentValues.put(DatabaseContract.COLUMN_RATE, mRate);
		contentValues.put(DatabaseContract.COLUMN_ROE, mRoe);
		contentValues.put(DatabaseContract.COLUMN_DIVIDEND_RATIO,
				mDividendRatio);

		return contentValues;
	}

	public void set(StockFinancial stockFinancial) {
		if (stockFinancial == null) {
			return;
		}

		init();

		super.set(stockFinancial);

		setStockId(stockFinancial.mStockId);
		setDate(stockFinancial.mDate);
		setBookValuePerShare(stockFinancial.mBookValuePerShare);
		setCashFlowPerShare(stockFinancial.mCashFlowPerShare);
		setTotalCurrentAssets(stockFinancial.mTotalCurrentAssets);
		setTotalAssets(stockFinancial.mTotalAssets);
		setTotalLongTermLiabilities(stockFinancial.mTotalLongTermLiabilities);
		setMainBusinessIncome(stockFinancial.mMainBusinessIncome);
		setMainBusinessIncomeInYear(stockFinancial.mMainBusinessIncomeInYear);
		setFinancialExpenses(stockFinancial.mFinancialExpenses);
		setNetProfit(stockFinancial.mNetProfit);
		setNetProfitInYear(stockFinancial.mNetProfitInYear);
		setNetProfitMargin(stockFinancial.mNetProfitMargin);
		setTotalShare(stockFinancial.mTotalShare);
		setDebtToNetAssetsRatio(stockFinancial.mDebtToNetAssetsRatio);
		setNetProfitPerShare(stockFinancial.mNetProfitPerShare);
		setNetProfitPerShareInYear(stockFinancial.mNetProfitPerShareInYear);
		setRate(stockFinancial.mRate);
		setRoe(stockFinancial.mRoe);
		setDividendRatio(mDividendRatio);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		init();

		super.set(cursor);

		setStockID(cursor);
		setDate(cursor);
		setBookValuePerShare(cursor);
		setCashFlowPerShare(cursor);
		setTotalCurrentAssets(cursor);
		setTotalAssets(cursor);
		setTotalLongTermLiabilities(cursor);
		setMainBusinessIncome(cursor);
		setMainBusinessIncomeInYear(cursor);
		setFinancialExpenses(cursor);
		setNetProfit(cursor);
		setNetProfitInYear(cursor);
		setNetProfitMargin(cursor);
		setTotalShare(cursor);
		setDebtToNetAssetsRatio(cursor);
		setNetProfitPerShare(cursor);
		setNetProfitPerShareInYear(cursor);
		setRate(cursor);
		setRoe(cursor);
		setDividendRatio(cursor);
	}

	public long getStockId() {
		return mStockId;
	}

	public void setStockId(long stockId) {
		mStockId = stockId;
	}

	void setStockID(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setStockId(cursor.getLong(cursor
				.getColumnIndex(DatabaseContract.COLUMN_STOCK_ID)));
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

	public double getBookValuePerShare() {
		return mBookValuePerShare;
	}

	public void setBookValuePerShare(double bookValuePerShare) {
		mBookValuePerShare = Utility.Round2(bookValuePerShare);
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
		mCashFlowPerShare = Utility.Round2(cashFlowPerShare);
	}

	void setCashFlowPerShare(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setCashFlowPerShare(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_CASH_FLOW_PER_SHARE)));
	}

	public double getTotalCurrentAssets() {
		return mTotalCurrentAssets;
	}

	public void setTotalCurrentAssets(double totalCurrentAssets) {
		mTotalCurrentAssets = totalCurrentAssets;
	}

	void setTotalCurrentAssets(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setTotalCurrentAssets(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_TOTAL_CURRENT_ASSETS)));
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

	public double getFinancialExpenses() {
		return mFinancialExpenses;
	}

	public void setFinancialExpenses(double financialExpenses) {
		mFinancialExpenses = financialExpenses;
	}

	void setFinancialExpenses(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setFinancialExpenses(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_FINANCIAL_EXPENSES)));
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

	public void setupDebtToNetAssetsRatio() {
		if ((mBookValuePerShare == 0) || (mTotalShare == 0)) {
			return;
		}

		mDebtToNetAssetsRatio = Utility.Round2(mTotalLongTermLiabilities / mTotalShare
				/ mBookValuePerShare);
	}

	public void setupNetProfitMargin() {
		if ((mNetProfitInYear == 0) || (mMainBusinessIncomeInYear == 0)) {
			return;
		}

		mNetProfitMargin = Utility.Round2(mNetProfitInYear / mMainBusinessIncomeInYear);
	}

	public void setupNetProfitPerShare() {
		if ((mNetProfit == 0) || (mTotalShare == 0)) {
			return;
		}

		mNetProfitPerShare = Utility.Round2(mNetProfit / mTotalShare);
	}

	public void setupNetProfitPerShare(double totalShare) {
		if ((mNetProfit == 0) || (totalShare == 0)) {
			return;
		}

		mNetProfitPerShare = Utility.Round2(mNetProfit / totalShare);
	}
}
