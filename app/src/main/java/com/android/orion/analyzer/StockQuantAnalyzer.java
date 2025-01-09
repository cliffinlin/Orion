package com.android.orion.analyzer;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import com.android.orion.data.Trend;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockQuant;
import com.android.orion.manager.DatabaseManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class StockQuantAnalyzer {

	private static StockQuantAnalyzer mInstance;

	boolean mBulkInsert;
	long mHold;
	double mQuantProfit;
	double mValuation;
	double mFirstBuy;
	String mBuyDate = "";
	String mSellDate = "";

	ArrayList<ShareBonus> mShareBonusList = new ArrayList<>();
	ArrayList<StockQuant> mBuyList = new ArrayList<>();
	ArrayList<StockQuant> mStockQuantList = new ArrayList<>();
	ArrayList<ContentValues> mContentValuesList = new ArrayList<ContentValues>();

	Context mContext;
	DatabaseManager mDatabaseManager;
	Comparator<StockQuant> mComparator = new Comparator<StockQuant>() {

		@Override
		public int compare(StockQuant arg0, StockQuant arg1) {
			if (arg0 == null || arg1 == null) {
				return 0;
			}

			if (arg0.getBuy() < arg1.getBuy()) {
				return -1;
			} else if (arg0.getBuy() > arg1.getBuy()) {
				return 1;
			} else {
				return 0;
			}
		}
	};

	private StockQuantAnalyzer() {
	}

	public static synchronized StockQuantAnalyzer getInstance() {
		if (mInstance == null) {
			mInstance = new StockQuantAnalyzer();
		}
		return mInstance;
	}

	void init() {
		mBulkInsert = false;
		mHold = 0;
		mQuantProfit = 0;
		mValuation = 0;
		mFirstBuy = 0;
		mBuyDate = "";
		mSellDate = "";
		mShareBonusList.clear();
		mBuyList.clear();
		mStockQuantList.clear();
		mContentValuesList.clear();
	}

	void setupStockQuantBuy(Stock stock, StockData stockData, double price) {
		if (stockData == null) {
			return;
		}

		if (TextUtils.equals(stockData.getDate(), mBuyDate)) {
			return;
		}

		if (mFirstBuy == 0) {
			mFirstBuy = price;
		}

		StockQuant stockQuant = new StockQuant();

		stockQuant.setAction(Trend.MARK_BUY);

		stockQuant.setSE(stock.getSE());
		stockQuant.setCode(stock.getCode());
		stockQuant.setName(stock.getName());
		stockQuant.setThreshold(stock.getThreshold());

		stockQuant.setVolume(stock.getQuantVolume());
		stockQuant.setPrice(price);
		stockQuant.setBuy(price);

		stockQuant.setupBuyFee();
		stockQuant.setupNet();
		stockQuant.setupValue();
		stockQuant.setupProfit();

		mHold += stock.getQuantVolume();
		stockQuant.setHold(mHold);
		stockQuant.setQuantX(mHold / stock.getQuantVolume());

		mQuantProfit += stockQuant.getProfit();
		stockQuant.setQuantProfit(mQuantProfit);

		mValuation = mHold * price;
		stockQuant.setValuation(mValuation);

		stockQuant.setupQuantProfitMargin(mFirstBuy * stock.getQuantVolume());

		stockQuant.setCreated(stockData.getDateTime());

		mBuyList.add(stockQuant);
		Collections.sort(mBuyList, mComparator);

		stock.setQuantProfit(stockQuant.getQuantProfit());
		stock.setQuantProfitMargin(stockQuant.getQuantProfitMargin());

		mBuyDate = stockData.getDate();

		if (mBulkInsert) {
			mContentValuesList.add(stockQuant.getContentValues());
		} else {
			if (!mDatabaseManager.isStockQuantExist(stockQuant)) {
				mDatabaseManager.insertStockQuant(stockQuant);
			} else {
				mDatabaseManager.updateStockQuantById(stockQuant);
			}
		}
	}

	void setupStockQuantSell(Stock stock, StockData stockData, double price) {
		if (stockData == null) {
			return;
		}

		StockQuant stockQuant;

		if (TextUtils.equals(mSellDate, stockData.getDate())) {
			return;
		}

		while (mBuyList.size() > 0) {
			stockQuant = mBuyList.get(0);

			if (mHold == stock.getQuantVolume()) {
				if (TextUtils.equals(stockData.getDate(), stockQuant.getCreated().split(" ")[0])) {
					return;
				}
			}

			stockQuant.setAction(Trend.MARK_SELL);

			stockQuant.setSE(stock.getSE());
			stockQuant.setCode(stock.getCode());
			stockQuant.setName(stock.getName());
			stockQuant.setThreshold(stock.getThreshold());

			stockQuant.setVolume(-1 * stock.getQuantVolume());
			stockQuant.setPrice(price);
			stockQuant.setSell(price);

			stockQuant.setupSellFee(mShareBonusList);
			stockQuant.setupNet();
			stockQuant.setupValue();
			stockQuant.setupProfit();

			if (stockQuant.getNet() < stock.getThreshold()) {
				return;
			}

			mHold -= stock.getQuantVolume();
			stockQuant.setHold(mHold);
			stockQuant.setQuantX(mHold / stock.getQuantVolume());

			mQuantProfit += stockQuant.getProfit();
			stockQuant.setQuantProfit(mQuantProfit);

			mValuation = mHold * price;
			stockQuant.setValuation(mValuation);

			stockQuant.setupQuantProfitMargin(mFirstBuy * stock.getQuantVolume());

			stockQuant.setModified(stockData.getDateTime());

			mBuyList.remove(stockQuant);
			Collections.sort(mBuyList, mComparator);

			stock.setQuantProfit(stockQuant.getQuantProfit());
			stock.setQuantProfitMargin(stockQuant.getQuantProfitMargin());

			mSellDate = stockData.getDate();

			if (mBulkInsert) {
				mContentValuesList.add(stockQuant.getContentValues());
			} else {
				if (!mDatabaseManager.isStockQuantExist(stockQuant)) {
					mDatabaseManager.insertStockQuant(stockQuant);
				} else {
					mDatabaseManager.updateStockQuantById(stockQuant);
				}
			}
		}
	}

	public void analyze(Context context, Stock stock, ArrayList<StockData> stockDataList, ArrayList<ShareBonus> shareBonusList) {
		if (context == null || stock == null || stockDataList == null || shareBonusList == null) {
			return;
		}

		init();

		mContext = context;
		mDatabaseManager = DatabaseManager.getInstance();

		if (stock.getThreshold() == 0 || stock.getQuantVolume() == 0) {
			return;
		}

		if (stockDataList.size() < Trend.VERTEX_SIZE) {
			return;
		}

		if (shareBonusList == null || shareBonusList.size() < Trend.VERTEX_SIZE) {
//			return;
		}

		mShareBonusList = shareBonusList;
		mBuyList.clear();

		mDatabaseManager.getStockQuantList(stock, mStockQuantList);
		if (mStockQuantList.size() == 0) {
			mBulkInsert = true;
		}

		for (int i = 0; i < stockDataList.size(); i++) {
			StockData stockData = stockDataList.get(i);
			if (stockData == null) {
				continue;
			}

			if (stockData.getNaturalReaction() > 0) {
				setupStockQuantBuy(stock, stockData, stockData.getNaturalReaction());
			} else if (stockData.getDownwardTrend() > 0) {
				setupStockQuantBuy(stock, stockData, stockData.getDownwardTrend());
			} else if (stockData.getNaturalRally() > 0) {
				setupStockQuantSell(stock, stockData, stockData.getNaturalRally());
			} else if (stockData.getUpwardTrend() > 0) {
				setupStockQuantSell(stock, stockData, stockData.getUpwardTrend());
			}
		}

		if (mBulkInsert) {
			if (mContentValuesList.size() > 0) {
				ContentValues[] contentValuesArray = new ContentValues[mContentValuesList
						.size()];
				contentValuesArray = mContentValuesList
						.toArray(contentValuesArray);
				mDatabaseManager.bulkInsertStockQuant(contentValuesArray);
			}
		}
	}
}
