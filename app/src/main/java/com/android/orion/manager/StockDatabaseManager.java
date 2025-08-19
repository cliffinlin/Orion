package com.android.orion.manager;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.android.orion.config.Config;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockBonus;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDeal;
import com.android.orion.database.StockFinancial;
import com.android.orion.database.StockPerceptron;
import com.android.orion.database.StockShare;
import com.android.orion.database.StockTrend;
import com.android.orion.database.TDXData;
import com.android.orion.interfaces.StockListener;
import com.android.orion.utility.Utility;

import java.util.ArrayList;

public class StockDatabaseManager extends DatabaseManager implements StockListener {
	public static String TAG = Config.TAG + StockDatabaseManager.class.getSimpleName();
	private static volatile StockDatabaseManager mInstance;

	public StockDatabaseManager(Context context) {
		super(context);
		StockManager.getInstance().registerStockListener(this);
	}

	public static synchronized StockDatabaseManager getInstance() {
		return mInstance;
	}

	public static synchronized StockDatabaseManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new StockDatabaseManager(context);
		}
		return mInstance;
	}

	public Uri insert(Stock stock) {
		if (stock == null) {
			return null;
		}
		return insert(DatabaseContract.Stock.CONTENT_URI, stock.getContentValues());
	}

	public int bulkInsertStock(ContentValues[] contentValuesArray) {
		return bulkInsert(DatabaseContract.Stock.CONTENT_URI, contentValuesArray);
	}

	public int deleteStock(long id) {
		return delete(DatabaseContract.Stock.CONTENT_URI, DatabaseContract.SELECTION_ID(id), null);
	}

	public int updateStock(Stock stock, ContentValues contentValues) {
		return update(DatabaseContract.Stock.CONTENT_URI, contentValues, DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode()), null);
	}

	public void updateStockFlag(Stock stock) {
		if (stock == null) {
			return;
		}
		Uri uri = ContentUris.withAppendedId(DatabaseContract.Stock.CONTENT_URI, stock.getId());
		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseContract.COLUMN_FLAG, stock.getFlag());
		update(uri, contentValues, null, null);
	}

	public Cursor queryStock(String selection, String[] selectionArgs, String sortOrder) {
		return query(DatabaseContract.Stock.CONTENT_URI, DatabaseContract.Stock.PROJECTION_ALL, selection, selectionArgs, sortOrder);
	}

	public Cursor queryStock(Stock stock) {
		return queryStock(DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode()), null, null);
	}

	public void getStockList(String selection, ArrayList<Stock> stockList) {
		if (stockList == null) {
			return;
		}
		stockList.clear();
		Cursor cursor = null;
		try {
			cursor = queryStock(selection, null, null);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					Stock stock = new Stock();
					stock.set(cursor);
					stockList.add(stock);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void loadStockArrayMap(ArrayMap<String, Stock> stockArrayMap) {
		if (stockArrayMap == null) {
			return;
		}
		stockArrayMap.clear();
		Cursor cursor = null;
		try {
			cursor = queryStock(DatabaseContract.SELECTION_FLAG(Stock.FLAG_FAVORITE), null, null);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					Stock stock = new Stock();
					stock.set(cursor);
					stockArrayMap.put(stock.getCode(), stock);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStockById(Stock stock) {
		if (stock == null) {
			return;
		}
		Cursor cursor = null;
		try {
			cursor = queryStock(DatabaseContract.SELECTION_ID(stock.getId()), null, null);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stock.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public int getStockCount(String selection, String[] selectionArgs, String sortOrder) {
		int result = 0;
		Cursor cursor = null;
		try {
			cursor = queryStock(selection, selectionArgs, sortOrder);
			if (cursor != null) {
				result = cursor.getCount();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return result;
	}

	public void getStock(Stock stock) {
		if (stock == null) {
			return;
		}
		Cursor cursor = null;
		try {
			cursor = queryStock(stock);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stock.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStock(Uri uri, Stock stock) {
		if (stock == null) {
			return;
		}
		Cursor cursor = null;
		try {
			cursor = query(uri, DatabaseContract.Stock.PROJECTION_ALL, null, null, null);
			if (cursor != null) {
				cursor.moveToNext();
				stock.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public boolean isStockExist(Stock stock) {
		boolean result = false;
		if (stock == null) {
			return result;
		}
		Cursor cursor = null;
		try {
			cursor = queryStock(stock);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return result;
	}

	public Uri insertStockBonus(StockBonus stockBonus) {
		if (stockBonus == null) {
			return null;
		}
		return insert(DatabaseContract.StockBonus.CONTENT_URI, stockBonus.getContentValues());
	}

	public int bulkInsertStockBonus(ContentValues[] contentValuesArray) {
		if (contentValuesArray == null) {
			return 0;
		}
		return bulkInsert(DatabaseContract.StockBonus.CONTENT_URI, contentValuesArray);
	}

	public int deleteStockBonus(Stock stock) {
		return delete(DatabaseContract.StockBonus.CONTENT_URI, DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode()), null);
	}

	public int updateStockBonus(StockBonus stockBonus, ContentValues contentValues) {
		if (stockBonus == null) {
			return 0;
		}
		return update(DatabaseContract.StockBonus.CONTENT_URI, contentValues, DatabaseContract.SELECTION_STOCK_DATE(stockBonus.getSE(), stockBonus.getCode(), stockBonus.getDate()), null);
	}

	public Cursor queryStockBonus(String selection, String[] selectionArgs, String sortOrder) {
		return query(DatabaseContract.StockBonus.CONTENT_URI,
				DatabaseContract.StockBonus.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);
	}

	public Cursor queryStockBonus(StockBonus stockBonus) {
		if (stockBonus == null) {
			return null;
		}
		String selection = DatabaseContract.SELECTION_STOCK_DATE(stockBonus.getSE(), stockBonus.getCode(), stockBonus.getDate());
		return query(DatabaseContract.StockBonus.CONTENT_URI, DatabaseContract.StockBonus.PROJECTION_ALL, selection, null, DatabaseContract.ORDER_DATE_ASC);
	}

	public void getStockBonus(Stock stock, StockBonus stockBonus) {
		if (stockBonus == null) {
			return;
		}
		Cursor cursor = null;
		try {
			String selection = DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode());
			cursor = query(DatabaseContract.StockBonus.CONTENT_URI, DatabaseContract.StockBonus.PROJECTION_ALL, selection,null, DatabaseContract.ORDER_DATE_DESC);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stockBonus.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStockBonusList(Stock stock, ArrayList<StockBonus> stockBonusList, String sortOrder) {
		if (stock == null || stockBonusList == null) {
			return;
		}
		stockBonusList.clear();
		Cursor cursor = null;
		try {
			cursor = queryStockBonus(DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode()), null, sortOrder);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockBonus stockBonus = new StockBonus();
					stockBonus.set(cursor);
					stockBonusList.add(stockBonus);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public boolean isStockBonusExist(StockBonus stockBonus) {
		boolean result = false;
		if (stockBonus == null) {
			return result;
		}
		Cursor cursor = null;
		try {
			cursor = queryStockBonus(stockBonus);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return result;
	}

	public Uri insert(StockData stockData) {
		if (stockData == null) {
			return null;
		}
		return insert(DatabaseContract.StockData.CONTENT_URI, stockData.getContentValues());
	}

	public int bulkInsertStockData(ContentValues[] contentValuesArray) {
		return bulkInsert(DatabaseContract.StockData.CONTENT_URI, contentValuesArray);
	}

	public int deleteStockData(Stock stock) {
		return delete(DatabaseContract.StockData.CONTENT_URI, DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode()), null);
	}

	public int deleteStockData(String se, String code, String period) {
		return delete(DatabaseContract.StockData.CONTENT_URI, DatabaseContract.SELECTION_STOCK_PERIOD(se, code, period), null);
	}

	public int updateStockData(long id, ContentValues contentValues) {
		return update(DatabaseContract.StockData.CONTENT_URI, contentValues, DatabaseContract.SELECTION_ID(id), null);
	}

	public int updateStockData(StockData stockData, ContentValues contentValues) {
		return update(DatabaseContract.StockData.CONTENT_URI, contentValues, DatabaseContract.SELECTION_STOCK_PERIOD_DATE_TIME(stockData.getSE(), stockData.getCode(), stockData.getPeriod(), stockData.getDate(), stockData.getTime()), null);
	}

	public void updateStockData(Stock stock, String period, ArrayList<StockData> stockDataList) {
		if (stock == null || stockDataList == null || stockDataList.size() == 0) {
			return;
		}

		try {
			deleteStockData(stock.getSE(), stock.getCode(), period); //TODO
			ContentValues[] contentValues = new ContentValues[stockDataList.size()];
			for (int i = 0; i < stockDataList.size(); i++) {
				StockData stockData = stockDataList.get(i);
				stockData.setCreated(Utility.getCurrentDateTimeString());
				stockData.setModified(Utility.getCurrentDateTimeString());
				contentValues[i] = stockData.getContentValues();
			}
			bulkInsertStockData(contentValues); //TODO
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Cursor queryStockData(String selection, String[] selectionArgs, String sortOrder) {
		return query(DatabaseContract.StockData.CONTENT_URI, DatabaseContract.StockData.PROJECTION_ALL, selection, selectionArgs, sortOrder);
	}

	public Cursor queryStockData(StockData stockData) {
		return query(DatabaseContract.StockData.CONTENT_URI, DatabaseContract.StockData.PROJECTION_ALL, DatabaseContract.SELECTION_STOCK_PERIOD_DATE_TIME(stockData.getSE(), stockData.getCode(), stockData.getPeriod(), stockData.getDate(), stockData.getTime()), null, DatabaseContract.ORDER_DATE_TIME_ASC);
	}

	public void getStockData(StockData stockData) {
		if (stockData == null) {
			return;
		}
		Cursor cursor = null;
		try {
			cursor = query(DatabaseContract.StockData.CONTENT_URI, DatabaseContract.StockData.PROJECTION_ALL, DatabaseContract.SELECTION_STOCK_PERIOD(stockData.getSE(), stockData.getCode(), stockData.getPeriod()), null, DatabaseContract.ORDER_DATE_TIME_DESC);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stockData.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void loadStockDataList(Stock stock, String period, ArrayList<StockData> stockDataList) {
		if (stock == null || stockDataList == null) {
			return;
		}
		stockDataList.clear();
		Cursor cursor = null;
		try {
			cursor = queryStockData(DatabaseContract.SELECTION_STOCK_PERIOD(stock.getSE(), stock.getCode(), period), null, DatabaseContract.ORDER_DATE_TIME_ASC);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				int index = 0;
				while (cursor.moveToNext()) {
					StockData stockData = new StockData(period);
					stockData.set(cursor);
					index = stockDataList.size();
					stockData.setIndex(index);
					stockData.setIndexStart(index);
					stockData.setIndexEnd(index);
					stockDataList.add(stockData);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public boolean isStockDataExist(StockData stockData) {
		boolean result = false;
		if (stockData == null) {
			return result;
		}
		Cursor cursor = null;
		try {
			cursor = queryStockData(stockData);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return result;
	}

	public Uri insertStockDeal(StockDeal stockDeal) {
		if (stockDeal == null) {
			return null;
		}
		return insert(DatabaseContract.StockDeal.CONTENT_URI, stockDeal.getContentValues());
	}

	public int bulkInsertStockDeal(ContentValues[] contentValuesArray) {
		return bulkInsert(DatabaseContract.StockDeal.CONTENT_URI, contentValuesArray);
	}

	public int deleteStockDeal() {
		return delete(DatabaseContract.StockDeal.CONTENT_URI);
	}

	public void deleteStockDeal(StockDeal stockDeal) {
		if (stockDeal == null) {
			return;
		}
		delete(DatabaseContract.StockDeal.CONTENT_URI, DatabaseContract.SELECTION_ID(stockDeal.getId()), null);
	}

	public int updateStockDealByID(StockDeal stockDeal) {
		if (stockDeal == null) {
			return 0;
		}
		return update(DatabaseContract.StockDeal.CONTENT_URI, stockDeal.getContentValues(), DatabaseContract.SELECTION_ID(stockDeal.getId()), null);
	}

	public int updateStockDeal(Stock stock) {
		int result = 0;
		long hold = 0;
		double cost = 0;
		if (stock == null) {
			return result;
		}
		StockDeal stockDeal = new StockDeal();
		String selection = DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode());
		Cursor cursor = null;
		try {
			cursor = queryStockDeal(selection, null, DatabaseContract.ORDER_BUY_ASC);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					stockDeal.set(cursor);

					stockDeal.setPrice(stock.getPrice());
					stockDeal.setupValue();
					stockDeal.setupFee(stock.getRDate(), stock.getDividend());
					stockDeal.setupProfit();
					stockDeal.setupNet();
					stockDeal.setupBonus(stock.getDividend());
					stockDeal.setupYield(stock.getDividend());

					if (stockDeal.getVolume() > 0 && TextUtils.equals(stockDeal.getType(), StockDeal.TYPE_BUY)) {
						hold += stockDeal.getVolume();
						cost += stockDeal.getValue();
					}

					result += updateStockDealByID(stockDeal);
				}
			}

			stock.setHold(hold);
			if (hold > 0) {
				stock.setCost(Utility.Round2(cost));
				stock.setProfit(Utility.Round2(hold * stock.getNetProfitPerShareInYear()));
				stock.setValuation(hold * stock.getPrice());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return result;
	}

	public Cursor queryStockDeal(String selection, String[] selectionArgs, String sortOrder) {
		return query(DatabaseContract.StockDeal.CONTENT_URI,
				DatabaseContract.StockDeal.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);
	}

	public Cursor queryStockDeal(long id) {
		return queryStockDeal(DatabaseContract.SELECTION_ID(id), null, null);
	}

	public void getStockDeal(StockDeal stockDeal) {
		if (stockDeal == null) {
			return;
		}

		Cursor cursor = null;
		try {
			cursor = queryStockDeal(stockDeal.getId());
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stockDeal.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public int getStockDealCount(Stock stock) {
		int result = 0;
		if (stock == null) {
			return result;
		}
		Cursor cursor = null;
		try {
			String selection = DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode());
			cursor = queryStockDeal(selection, null, null);
			if (cursor != null) {
				result = cursor.getCount();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return result;
	}

	public void getStockDealList(ArrayList<StockDeal> stockDealList, String selection, String sortOrder) {
		if (stockDealList == null) {
			return;
		}
		stockDealList.clear();
		Cursor cursor = null;
		try {
			cursor = queryStockDeal(selection, null, sortOrder);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockDeal stockDeal = new StockDeal();
					stockDeal.set(cursor);
					stockDealList.add(stockDeal);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStockDealList(Stock stock, ArrayList<StockDeal> stockDealList) {
		if (stock == null || stockDealList == null) {
			return;
		}
		getStockDealList(stockDealList, DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode()), DatabaseContract.ORDER_BUY_DESC);
	}

	public Uri insertStockFinancial(StockFinancial stockFinancial) {
		if (stockFinancial == null) {
			return null;
		}
		return insert(DatabaseContract.StockFinancial.CONTENT_URI, stockFinancial.getContentValues());
	}

	public int bulkInsertStockFinancial(ContentValues[] contentValuesArray) {
		if (contentValuesArray == null) {
			return 0;
		}
		return bulkInsert(DatabaseContract.StockFinancial.CONTENT_URI, contentValuesArray);
	}

	public int deleteStockFinancial(Stock stock) {
		return delete(DatabaseContract.StockFinancial.CONTENT_URI, DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode()), null);
	}

	public int updateStockFinancial(StockFinancial stockFinancial, ContentValues contentValues) {
		if (stockFinancial == null) {
			return 0;
		}
		return update(DatabaseContract.StockFinancial.CONTENT_URI, contentValues, DatabaseContract.SELECTION_STOCK_DATE(stockFinancial.getSE(), stockFinancial.getCode(), stockFinancial.getDate()), null);
	}

	public void updateStockFinancial(Stock stock, ArrayList<StockFinancial> stockFinancialList) {
		if (stockFinancialList == null || stockFinancialList.size() == 0) {
			return;
		}
		try {
			deleteStockFinancial(stock);//TODO
			ContentValues[] contentValues = new ContentValues[stockFinancialList.size()];
			for (int i = 0; i < stockFinancialList.size(); i++) {
				StockFinancial stockFinancial = stockFinancialList.get(i);
				stockFinancial.setModified(Utility.getCurrentDateTimeString());
				contentValues[i] = stockFinancial.getContentValues();
			}
			bulkInsertStockFinancial(contentValues);//TODO
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Cursor queryStockFinancial(String selection, String[] selectionArgs, String sortOrder) {
		return query(DatabaseContract.StockFinancial.CONTENT_URI,
				DatabaseContract.StockFinancial.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);
	}

	public Cursor queryStockFinancial(StockFinancial stockFinancial) {
		if (stockFinancial == null) {
			return null;
		}
		String selection = DatabaseContract.SELECTION_STOCK_DATE(stockFinancial.getSE(), stockFinancial.getCode(), stockFinancial.getDate());
		return query(DatabaseContract.StockFinancial.CONTENT_URI, DatabaseContract.StockFinancial.PROJECTION_ALL, selection, null, DatabaseContract.ORDER_DATE_ASC);
	}

	public void getStockFinancial(Stock stock, StockFinancial stockFinancial) {
		if (stockFinancial == null) {
			return;
		}
		Cursor cursor = null;
		try {
			String selection = DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode());
			cursor = query(DatabaseContract.StockFinancial.CONTENT_URI,	DatabaseContract.StockFinancial.PROJECTION_ALL, selection,null, DatabaseContract.ORDER_DATE_DESC);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stockFinancial.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStockFinancialList(Stock stock, ArrayList<StockFinancial> stockFinancialList, String sortOrder) {
		if (stock == null || stockFinancialList == null) {
			return;
		}
		stockFinancialList.clear();
		Cursor cursor = null;
		try {
			cursor = queryStockFinancial(DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode()), null, sortOrder);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockFinancial stockFinancial = new StockFinancial();
					stockFinancial.set(cursor);
					stockFinancialList.add(stockFinancial);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public boolean isStockFinancialExist(StockFinancial stockFinancial) {
		boolean result = false;
		if (stockFinancial == null) {
			return result;
		}
		Cursor cursor = null;
		try {
			cursor = queryStockFinancial(stockFinancial);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return result;
	}

	public Uri insertStockPerceptron(StockPerceptron stockPerceptron) {
		if (stockPerceptron == null) {
			return null;
		}
		return insert(DatabaseContract.StockPerceptron.CONTENT_URI, stockPerceptron.getContentValues());
	}

	public int deleteStockPerceptron(long id) {
		return delete(DatabaseContract.StockPerceptron.CONTENT_URI, DatabaseContract.SELECTION_ID(id), null);
	}

	public int updateStockPerceptron(StockPerceptron stockPerceptron, ContentValues contentValues) {
		if (stockPerceptron == null) {
			return 0;
		}
		return update(DatabaseContract.StockPerceptron.CONTENT_URI, contentValues, DatabaseContract.SELECTION_PERIOD_LEVEL_TYPE(stockPerceptron.getPeriod(), stockPerceptron.getLevel(), stockPerceptron.getType()), null);
	}

	public Cursor queryStockPerceptron(String selection, String[] selectionArgs, String sortOrder) {
		return query(DatabaseContract.StockPerceptron.CONTENT_URI,
				DatabaseContract.StockPerceptron.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);
	}

	public Cursor queryStockPerceptron(StockPerceptron stockPerceptron) {
		if (stockPerceptron == null) {
			return null;
		}
		return query(DatabaseContract.StockPerceptron.CONTENT_URI,
				DatabaseContract.StockPerceptron.PROJECTION_ALL, DatabaseContract.SELECTION_PERIOD_LEVEL_TYPE(stockPerceptron.getPeriod(), stockPerceptron.getLevel(), stockPerceptron.getType()), null, DatabaseContract.ORDER_PERIOD_LEVEL_ASC);
	}

	public void getStockPerceptron(StockPerceptron stockPerceptron) {
		if (stockPerceptron == null) {
			return;
		}

		Cursor cursor = null;
		try {
			String selection = DatabaseContract.SELECTION_PERIOD_LEVEL_TYPE(stockPerceptron.getPeriod(), stockPerceptron.getLevel(), stockPerceptron.getType());
			cursor = query(DatabaseContract.StockPerceptron.CONTENT_URI, DatabaseContract.StockPerceptron.PROJECTION_ALL, selection, null, DatabaseContract.ORDER_PERIOD_LEVEL_DESC);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stockPerceptron.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStockPerceptronById(StockPerceptron stockPerceptron) {
		if (stockPerceptron == null) {
			return;
		}
		Cursor cursor = null;
		try {
			cursor = queryStockPerceptron(DatabaseContract.SELECTION_ID(stockPerceptron.getId()), null, null);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stockPerceptron.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public boolean isStockPerceptronExist(StockPerceptron stockPerceptron) {
		boolean result = false;
		if (stockPerceptron == null) {
			return result;
		}
		Cursor cursor = null;
		try {
			cursor = queryStockPerceptron(stockPerceptron);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return result;
	}

	public Uri insertStockShare(StockShare stockShare) {
		if (stockShare == null) {
			return null;
		}
		return insert(DatabaseContract.StockShare.CONTENT_URI, stockShare.getContentValues());
	}

	public int bulkInsertStockShare(ContentValues[] contentValuesArray) {
		return bulkInsert(DatabaseContract.StockShare.CONTENT_URI, contentValuesArray);
	}

	public Cursor queryStockShare(String selection, String[] selectionArgs, String sortOrder) {
		return query(DatabaseContract.StockShare.CONTENT_URI, DatabaseContract.StockShare.PROJECTION_ALL, selection, selectionArgs, sortOrder);
	}

	public Cursor queryStockShare(StockShare stockShare) {
		if (stockShare == null) {
			return null;
		}
		return query(DatabaseContract.StockShare.CONTENT_URI, DatabaseContract.StockShare.PROJECTION_ALL, DatabaseContract.SELECTION_STOCK_DATE(stockShare.getSE(), stockShare.getCode(), stockShare.getDate()), null, DatabaseContract.ORDER_DATE_ASC);
	}

	public void getStockShare(Stock stock, StockShare stockShare) {
		if (stockShare == null) {
			return;
		}
		Cursor cursor = null;
		try {
			cursor = query(DatabaseContract.StockShare.CONTENT_URI,	DatabaseContract.StockShare.PROJECTION_ALL,	DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode()),null, DatabaseContract.ORDER_DATE_DESC);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stockShare.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStockShareList(Stock stock, ArrayList<StockShare> stockShareList, String sortOrder) {
		if (stock == null || stockShareList == null) {
			return;
		}
		stockShareList.clear();
		Cursor cursor = null;
		try {
			cursor = queryStockShare(DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode()), null, sortOrder);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockShare stockShare = new StockShare();
					stockShare.set(cursor);
					stockShareList.add(stockShare);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public boolean isStockShareExist(StockShare stockShare) {
		boolean result = false;
		if (stockShare == null) {
			return result;
		}
		Cursor cursor = null;
		try {
			cursor = queryStockShare(stockShare);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return result;
	}

	public int deleteStockShare(Stock stock) {
		return delete(DatabaseContract.StockShare.CONTENT_URI, DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode()), null);
	}

	public int updateStockShare(StockShare stockShare, ContentValues contentValues) {
		if (stockShare == null) {
			return 0;
		}
		return update(DatabaseContract.StockShare.CONTENT_URI, contentValues, DatabaseContract.SELECTION_STOCK_DATE(stockShare.getSE(), stockShare.getCode(), stockShare.getDate()), null);
	}

	public int bulkInsertTDXData(ContentValues[] contentValuesArray) {
		return bulkInsert(DatabaseContract.TDXData.CONTENT_URI, contentValuesArray);
	}

	public int deleteTDXData(Stock stock) {
		return delete(DatabaseContract.TDXData.CONTENT_URI, DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode()), null);
	}

	public int deleteTDXData(String se, String code, String period) {
		return delete(DatabaseContract.TDXData.CONTENT_URI, DatabaseContract.SELECTION_STOCK_PERIOD(se, code, period), null);
	}

	public Cursor queryTDXData(String selection, String[] selectionArgs, String sortOrder) {
		return query(DatabaseContract.TDXData.CONTENT_URI, DatabaseContract.TDXData.PROJECTION_ALL, selection, selectionArgs, sortOrder);
	}

	public void getTDXDataContentList(Stock stock, String period, ArrayList<String> contentList) {
		if (stock == null || contentList == null) {
			return;
		}
		contentList.clear();
		Cursor cursor = null;
		try {
			cursor = queryTDXData(DatabaseContract.SELECTION_STOCK_PERIOD(stock.getSE(), stock.getCode(), period), null, null);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					TDXData tdxData = new TDXData();
					tdxData.set(cursor);
					contentList.add(tdxData.getContent());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public Uri insertStockTrend(StockTrend stockTrend) {
		if (stockTrend == null) {
			return null;
		}
		return insert(DatabaseContract.StockTrend.CONTENT_URI, stockTrend.getContentValues());
	}

	public int deleteStockTrend(Stock stock) {
		return delete(DatabaseContract.StockTrend.CONTENT_URI, DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode()), null);
	}

	public int updateStockTrend(StockTrend stockTrend, ContentValues contentValues) {
		if (stockTrend == null) {
			return 0;
		}
		return update(DatabaseContract.StockTrend.CONTENT_URI, contentValues, DatabaseContract.SELECTION_STOCK_PERIOD_LEVEL(stockTrend.getSE(), stockTrend.getCode(), stockTrend.getPeriod(), stockTrend.getLevel()), null);
	}

	public Cursor queryStockTrend(String selection, String[] selectionArgs, String sortOrder) {
		return query(DatabaseContract.StockTrend.CONTENT_URI, DatabaseContract.StockTrend.PROJECTION_ALL, selection, selectionArgs, sortOrder);
	}

	public Cursor queryStockTrend(StockTrend stockTrend) {
		return query(DatabaseContract.StockTrend.CONTENT_URI, DatabaseContract.StockTrend.PROJECTION_ALL, DatabaseContract.SELECTION_STOCK_PERIOD_LEVEL(stockTrend.getSE(), stockTrend.getCode(), stockTrend.getPeriod(), stockTrend.getLevel()), null, DatabaseContract.ORDER_DATE_TIME_ASC);
	}

	public void getStockTrend(StockTrend stockTrend) {
		if (stockTrend == null) {
			return;
		}
		Cursor cursor = null;
		try {
			cursor = query(DatabaseContract.StockTrend.CONTENT_URI, DatabaseContract.StockTrend.PROJECTION_ALL, DatabaseContract.SELECTION_STOCK_PERIOD_LEVEL(stockTrend.getSE(), stockTrend.getCode(), stockTrend.getPeriod(), stockTrend.getLevel()), null, DatabaseContract.ORDER_DATE_TIME_DESC);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				if (cursor.getCount() > 1) {
					Log.d(TAG, "getStockTrend cursor.getCount()=" + cursor.getCount());
				}
				cursor.moveToNext();
				stockTrend.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStockTrendById(StockTrend stockTrend) {
		if (stockTrend == null) {
			return;
		}
		Cursor cursor = null;
		try {
			cursor = queryStockTrend(DatabaseContract.SELECTION_ID(stockTrend.getId()), null, null);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stockTrend.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStockTrendList(Stock stock, ArrayList<StockTrend> stockTrendList) {
		if (stock == null || stockTrendList == null) {
			return;
		}
		stockTrendList.clear();
		Cursor cursor = null;
		try {
			cursor = queryStockTrend(DatabaseContract.SELECTION_STOCK(stock.getSE(), stock.getCode()), null, DatabaseContract.ORDER_PERIOD_ASC);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockTrend stockTrend = new StockTrend();
					stockTrend.set(cursor);
					stockTrendList.add(stockTrend);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public boolean isStockTrendExist(StockTrend stockTrend) {
		boolean result = false;
		if (stockTrend == null) {
			return result;
		}
		Cursor cursor = null;
		try {
			cursor = queryStockTrend(stockTrend);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				if (cursor.getCount() > 1) {
					Log.d(TAG, "isStockTrendExist cursor.getCount()=" + cursor.getCount());
				}
				cursor.moveToNext();
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		return result;
	}

	public void onAddFavorite(Stock stock) {
		if (stock == null) {
			return;
		}
		updateStockFlag(stock);
	}

	public void onRemoveFavorite(Stock stock) {
		if (stock == null) {
			return;
		}
		updateStockFlag(stock);
	}

	public void onAddNotify(Stock stock) {
		if (stock == null) {
			return;
		}
		updateStockFlag(stock);
	}

	public void onRemoveNotify(Stock stock) {
		if (stock == null) {
			return;
		}
		updateStockFlag(stock);
	}

	public void onAddStock(Stock stock) {
		if (stock == null) {
			return;
		}
	}

	public void onRemoveStock(Stock stock) {
		if (stock == null) {
			return;
		}
	}
}
