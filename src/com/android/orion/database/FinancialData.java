package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

public class FinancialData extends DatabaseTable {
	private long mStockId;
	private String mDate;
	private String mTime;
	private double mBookValuePerShare;// BVPS// 每股净资产-摊薄/期末股数
	private double mEarningsPerShare;// EPS// 每股收益-摊薄/期末股数
	private double mCashFlowPerShare;// 每股现金含量
	private double mTotalCurrentAssets;// 流动资产合计
	private double mTotalAssets;// 资产总计
	private double mTotalLongTermLiabilities;// 长期负债合计
	private double mMainBusinessIncome;// 主营业务收入
	private double mFinancialExpenses;// 财务费用
	private double mNetProfit;// 净利润

	private FinancialData next;
	private static final Object sPoolSync = new Object();
	private static FinancialData sPool;

	public static FinancialData obtain() {
		synchronized (sPoolSync) {
			if (sPool != null) {
				FinancialData m = sPool;
				sPool = m.next;
				m.next = null;
				return m;
			}
		}
		return new FinancialData();
	}

	public FinancialData() {
		init();
	}

	public FinancialData(FinancialData financialData) {
		set(financialData);
	}

	public FinancialData(Cursor cursor) {
		set(cursor);
	}

	void init() {
		super.init();

		setTableName(DatabaseContract.FinancialData.TABLE_NAME);

		mStockId = 0;
		mDate = "";
		mTime = "";
		mBookValuePerShare = 0;
		mEarningsPerShare = 0;
		mCashFlowPerShare = 0;
		mTotalCurrentAssets = 0;
		mTotalAssets = 0;
		mTotalLongTermLiabilities = 0;
		mMainBusinessIncome = 0;
		mFinancialExpenses = 0;
		mNetProfit = 0;
	}

	public ContentValues getContentValues() {
		ContentValues contentValues = new ContentValues();
		super.getContentValues(contentValues);
		contentValues = getContentValues(contentValues);
		return contentValues;
	}

	ContentValues getContentValues(ContentValues contentValues) {
		contentValues.put(DatabaseContract.COLUMN_STOCK_ID, mStockId);
		contentValues.put(DatabaseContract.COLUMN_DATE, mDate);
		contentValues.put(DatabaseContract.COLUMN_TIME, mTime);
		contentValues.put(DatabaseContract.COLUMN_BOOK_VALUE_PER_SHARE, mBookValuePerShare);
		contentValues.put(DatabaseContract.COLUMN_EARNINGS_PER_SHARE, mEarningsPerShare);
		contentValues.put(DatabaseContract.COLUMN_CASH_FLOW_PER_SHARE, mCashFlowPerShare);
		contentValues.put(DatabaseContract.COLUMN_CURRENT_ASSETS, mTotalCurrentAssets);
		contentValues.put(DatabaseContract.COLUMN_TOTALASSETS, mTotalAssets);
		contentValues.put(DatabaseContract.COLUMN_TOTAL_LONG_TERM_LIABILITIES,
				mTotalLongTermLiabilities);
		contentValues.put(DatabaseContract.COLUMN_MAIN_BUSINESS_INCOME, mMainBusinessIncome);
		contentValues.put(DatabaseContract.COLUMN_FINANCIAL_EXPENSES, mFinancialExpenses);
		contentValues.put(DatabaseContract.COLUMN_NET_PROFIT, mNetProfit);

		return contentValues;
	}

	public void set(FinancialData financialData) {
		if (financialData == null) {
			return;
		}

		init();

		super.set(financialData);

		setStockId(financialData.mStockId);
		setDate(financialData.mDate);
		setTime(financialData.mTime);
		setBookValuePerShare(financialData.mBookValuePerShare);
		setEarningsPerShare(financialData.mEarningsPerShare);
		setCashFlowPerShare(financialData.mCashFlowPerShare);
		setTotalCurrentAssets(financialData.mTotalCurrentAssets);
		setTotalAssets(financialData.mTotalAssets);
		setTotalLongTermLiabilities(financialData.mTotalLongTermLiabilities);
		setMainBusinessIncome(financialData.mMainBusinessIncome);
		setFinancialExpenses(financialData.mFinancialExpenses);
		setNetProfit(financialData.mNetProfit);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		super.set(cursor);

		setStockID(cursor);
		setDate(cursor);
		setTime(cursor);
		setBookValuePerShare(cursor);
		setEarningsPerShare(cursor);
		setCashFlowPerShare(cursor);
		setCurrentAssets(cursor);
		setTotalAssets(cursor);
		setTotalLongTermLiabilities(cursor);
		setMainBusinessIncome(cursor);
		setFinancialExpenses(cursor);
		setNetProfit(cursor);
	}

	public long getStockId() {
		return mStockId;
	}

	public void setStockId(long stockId) {
		mStockId = stockId;
	}

	void setStockID(Cursor cursor) {
		if (cursor == null) {
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

	public double getEarningsPerShare() {
		return mEarningsPerShare;
	}

	public void setEarningsPerShare(double EarningsPerShare) {
		mEarningsPerShare = EarningsPerShare;
	}

	void setEarningsPerShare(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setEarningsPerShare(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_EARNINGS_PER_SHARE)));
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

	public double getTotalCurrentAssets() {
		return mTotalCurrentAssets;
	}

	public void setTotalCurrentAssets(double totalCurrentAssets) {
		mTotalCurrentAssets = totalCurrentAssets;
	}

	void setCurrentAssets(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setTotalCurrentAssets(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_CURRENT_ASSETS)));
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
				.getColumnIndex(DatabaseContract.COLUMN_TOTALASSETS)));
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

		setTotalLongTermLiabilities(cursor.getDouble(cursor
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

	public double getFinancialExpenses() {
		return mFinancialExpenses;
	}

	public void setFinancialExpenses(double financialExpenses) {
		mFinancialExpenses = financialExpenses;
	}

	void setFinancialExpenses(Cursor cursor) {
		if (cursor == null) {
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
		if (cursor == null) {
			return;
		}

		setNetProfit(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NET_PROFIT)));
	}
}
