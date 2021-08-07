package com.android.orion.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.android.orion.Constants;

public class StockDatabaseManager extends DatabaseManager {
	private static StockDatabaseManager mInstance = null;

	public static synchronized StockDatabaseManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new StockDatabaseManager(context);
		}
		return mInstance;
	}

	private StockDatabaseManager() {
		super();
	}

	private StockDatabaseManager(Context context) {
		super(context);
	}

	public Uri insertStock(Stock stock) {
		Uri uri = null;

		if ((stock == null) || (mContentResolver == null)) {
			return uri;
		}

		uri = mContentResolver.insert(DatabaseContract.Stock.CONTENT_URI,
				stock.getContentValues());

		return uri;
	}

	public int bulkInsertStock(ContentValues[] contentValuesArray) {
		int result = 0;

		if ((contentValuesArray.length == 0) || (mContentResolver == null)) {
			return result;
		}

		result = mContentResolver.bulkInsert(
				DatabaseContract.Stock.CONTENT_URI, contentValuesArray);

		return result;
	}

	public Cursor queryStock(String selection, String[] selectionArgs,
			String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(DatabaseContract.Stock.CONTENT_URI,
				DatabaseContract.Stock.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor queryStock(Stock stock) {
		Cursor cursor = null;

		if (stock == null) {
			return cursor;
		}

		String selection = DatabaseContract.COLUMN_SE + " = " + "\'"
				+ stock.getSE() + "\'" + " AND " + DatabaseContract.COLUMN_CODE
				+ " = " + "\'" + stock.getCode() + "\'";

		cursor = queryStock(selection, null, null);

		return cursor;
	}

	public void getStockById(Stock stock) {
		Cursor cursor = null;
		String selection = null;

		if (stock == null) {
			return;
		}

		selection = DatabaseContract.COLUMN_ID + "=" + stock.getId();

		try {
			cursor = queryStock(selection, null, null);

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

	public int getStockCount(String selection, String[] selectionArgs,
			String sortOrder) {
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
		Cursor cursor = null;

		if (stock == null) {
			return;
		}

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

	public boolean isStockExist(Stock stock) {
		boolean result = false;
		Cursor cursor = null;

		if (stock == null) {
			return result;
		}

		try {
			cursor = queryStock(stock);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stock.setCreated(cursor);
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	public int updateStock(Stock stock, ContentValues contentValues) {
		int result = 0;

		if ((stock == null) || (contentValues == null)
				|| (mContentResolver == null)) {
			return result;
		}

		String where = DatabaseContract.COLUMN_SE + " = " + "\'"
				+ stock.getSE() + "\'" + " AND " + DatabaseContract.COLUMN_CODE
				+ " = " + "\'" + stock.getCode() + "\'";

		result = mContentResolver.update(DatabaseContract.Stock.CONTENT_URI,
				contentValues, where, null);

		return result;
	}

	public int deleteStock(long stockId) {
		int result = 0;
		String where = null;

		if (mContentResolver == null) {
			return result;
		}

		if (stockId > 0) {
			where = DatabaseContract.COLUMN_ID + "=" + stockId;
		}

		try {
			result = mContentResolver.delete(
					DatabaseContract.Stock.CONTENT_URI, where, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public Uri insertStockData(StockData stockData) {
		Uri uri = null;

		if ((stockData == null) || (mContentResolver == null)) {
			return uri;
		}

		uri = mContentResolver.insert(DatabaseContract.StockData.CONTENT_URI,
				stockData.getContentValues());

		return uri;
	}

	public int bulkInsertStockData(ContentValues[] contentValuesArray) {
		int result = 0;

		if ((contentValuesArray.length == 0) || (mContentResolver == null)) {
			return result;
		}

		result = mContentResolver.bulkInsert(
				DatabaseContract.StockData.CONTENT_URI, contentValuesArray);

		return result;
	}

	public Cursor queryStockData(String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(DatabaseContract.StockData.CONTENT_URI,
				projection, selection, selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor queryStockData(String selection, String[] selectionArgs,
			String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(DatabaseContract.StockData.CONTENT_URI,
				DatabaseContract.StockData.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor queryStockData(StockData stockData) {
		Cursor cursor = null;

		if ((stockData == null) || (mContentResolver == null)) {
			return cursor;
		}

		String selection = getStockDataSelection(stockData);
		String sortOrder = getStockDataOrder();

		cursor = mContentResolver.query(DatabaseContract.StockData.CONTENT_URI,
				DatabaseContract.StockData.PROJECTION_ALL, selection, null,
				sortOrder);

		return cursor;
	}

	public void getStockData(StockData stockData) {
		Cursor cursor = null;

		if ((stockData == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = DatabaseContract.COLUMN_STOCK_ID + " = "
					+ stockData.getStockId() + " AND "
					+ DatabaseContract.COLUMN_PERIOD + " = " + "\'"
					+ stockData.getPeriod() + "\'";
			String sortOrder = DatabaseContract.COLUMN_DATE + " DESC " + ","
					+ DatabaseContract.COLUMN_TIME + " DESC ";

			cursor = mContentResolver.query(
					DatabaseContract.StockData.CONTENT_URI,
					DatabaseContract.StockData.PROJECTION_ALL, selection, null,
					sortOrder);

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

	public void getStockDataList(Stock stock, String period,
			ArrayList<StockData> stockDataList, String sortOrder) {
		Cursor cursor = null;

		if ((stock == null) || (stockDataList == null)) {
			return;
		}

		stockDataList.clear();

		String selection = getStockDataSelection(stock.getId(), period);

		try {
			cursor = queryStockData(selection, null, sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockData stockData = new StockData();
					stockData.set(cursor);
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
		Cursor cursor = null;

		if (stockData == null) {
			return result;
		}

		try {
			cursor = queryStockData(stockData);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stockData.setCreated(cursor);
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	public int updateStockData(StockData stockData, ContentValues contentValues) {
		int result = 0;

		if ((stockData == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getStockDataSelection(stockData);

		result = mContentResolver.update(
				DatabaseContract.StockData.CONTENT_URI, contentValues, where,
				null);

		return result;
	}

	public int deleteStockData() {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		result = mContentResolver.delete(
				DatabaseContract.StockData.CONTENT_URI, null, null);

		return result;
	}

	public int deleteStockData(StockData stockData) {
		int result = 0;

		if ((stockData == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getStockDataSelection(stockData);

		result = mContentResolver.delete(
				DatabaseContract.StockData.CONTENT_URI, where, null);

		return result;
	}

	public int deleteStockData(long stockId) {
		int result = 0;
		String where = null;

		if (mContentResolver == null) {
			return result;
		}

		if (stockId > 0) {
			where = DatabaseContract.COLUMN_STOCK_ID + "=" + stockId;
		}

		try {
			result = mContentResolver.delete(
					DatabaseContract.StockData.CONTENT_URI, where, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public int deleteStockData(long stockId, String period) {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		String where = getStockDataSelection(stockId, period);

		result = mContentResolver.delete(
				DatabaseContract.StockData.CONTENT_URI, where, null);

		return result;
	}

	public String getStockDataSelection(StockData stockData) {
		String selection = "";
		String period = "";

		if (stockData == null) {
			return selection;
		}

		selection = DatabaseContract.COLUMN_STOCK_ID + " = "
				+ stockData.getStockId() + " AND "
				+ DatabaseContract.COLUMN_PERIOD + " = " + "\'"
				+ stockData.getPeriod() + "\'" + " AND "
				+ DatabaseContract.COLUMN_DATE + " = " + "\'"
				+ stockData.getDate() + "\'";

		period = stockData.getPeriod();

		if (period.equals(Constants.PERIOD_MIN1)
				|| period.equals(Constants.PERIOD_MIN5)
				|| period.equals(Constants.PERIOD_MIN15)
				|| period.equals(Constants.PERIOD_MIN30)
				|| period.equals(Constants.PERIOD_MIN60)) {
			selection += " AND " + DatabaseContract.COLUMN_TIME + " = " + "\'"
					+ stockData.getTime() + "\'";
		}

		return selection;
	}

	public String getStockDataSelection(long stockId, String period) {
		return DatabaseContract.COLUMN_STOCK_ID + " = " + stockId + " AND "
				+ DatabaseContract.COLUMN_PERIOD + " = '" + period + "'";
	}

	public String getStockDataOrder() {
		return DatabaseContract.COLUMN_DATE + " ASC " + ","
				+ DatabaseContract.COLUMN_TIME + " ASC ";
	}

	public Uri insertStockDeal(StockDeal stockDeal) {
		Uri uri = null;

		if ((stockDeal == null) || (mContentResolver == null)) {
			return uri;
		}

		uri = mContentResolver.insert(DatabaseContract.StockDeal.CONTENT_URI,
				stockDeal.getContentValues());

		return uri;
	}

	public boolean isStockDealExist(StockDeal stockDeal) {
		boolean result = false;
		Cursor cursor = null;

		if (stockDeal == null) {
			return result;
		}

		try {
			cursor = queryStockDeal(stockDeal);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stockDeal.setCreated(cursor);
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	public int updateStockDealByID(StockDeal stockDeal) {
		int result = 0;

		if ((stockDeal == null) || (mContentResolver == null)) {
			return result;
		}

		String where = DatabaseContract.COLUMN_ID + "=" + stockDeal.getId();

		result = mContentResolver.update(
				DatabaseContract.StockDeal.CONTENT_URI,
				stockDeal.getContentValues(), where, null);

		return result;
	}

	public int updateStockDeal(Stock stock) {
		int result = 0;
		long hold = 0;
		double profit = 0;
		double valuation = 0;

		Cursor cursor = null;
		StockDeal stockDeal = null;

		if ((stock == null) || (mContentResolver == null)) {
			return result;
		}

		stock.setHold(0);

		stockDeal = new StockDeal();

		String selection = DatabaseContract.COLUMN_SE + " = " + "\'"
				+ stock.getSE() + "\'" + " AND " + DatabaseContract.COLUMN_CODE
				+ " = " + "\'" + stock.getCode() + "\'";
		String sortOrder = DatabaseContract.COLUMN_DEAL + " ASC ";

		try {
			cursor = queryStockDeal(selection, null, sortOrder);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					stockDeal.set(cursor);

					stockDeal.setPrice(stock.getPrice());
					stockDeal.setupNet();
					stockDeal.setupProfit(stock.getRDate(), stock.getDividend());
					stockDeal.setupValue();

					if (stockDeal.getVolume() > 0) {
						hold += stockDeal.getVolume();
						profit += stockDeal.getProfit();
						valuation += stockDeal.getValue();
					}

					result += updateStockDealByID(stockDeal);
				}
			}

			stock.setHold(hold);
			stock.setProfit(profit);
			stock.setValuation(valuation);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	public int deleteStockDeal() {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		result = mContentResolver.delete(
				DatabaseContract.StockDeal.CONTENT_URI, null, null);

		return result;
	}

	public void deleteStockDeal(StockDeal stockDeal) {
		if ((stockDeal == null) || (mContentResolver == null)) {
			return;
		}

		String where = DatabaseContract.COLUMN_ID + "=" + stockDeal.getId();

		try {
			mContentResolver.delete(DatabaseContract.StockDeal.CONTENT_URI,
					where, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Cursor queryStockDeal(StockDeal stockDeal) {
		Cursor cursor = null;

		if (stockDeal == null) {
			return cursor;
		}

		String selection = DatabaseContract.COLUMN_SE + " = " + "\'"
				+ stockDeal.getSE() + "\'" + " AND "
				+ DatabaseContract.COLUMN_CODE + " = " + "\'"
				+ stockDeal.getCode() + "\'" + " AND "
				+ DatabaseContract.COLUMN_DEAL + " = " + stockDeal.getDeal()
				+ " AND " + DatabaseContract.COLUMN_VOLUME + " = "
				+ stockDeal.getVolume();

		cursor = queryStockDeal(selection, null, null);

		return cursor;
	}

	public Cursor queryStockDeal(String selection, String[] selectionArgs,
			String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(DatabaseContract.StockDeal.CONTENT_URI,
				DatabaseContract.StockDeal.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor queryStockDealById(StockDeal stockDeal) {
		Cursor cursor = null;

		if (stockDeal == null) {
			return cursor;
		}

		String selection = DatabaseContract.COLUMN_ID + "=" + stockDeal.getId();

		cursor = queryStockDeal(selection, null, null);

		return cursor;
	}

	public void getStockDealById(StockDeal stockDeal) {
		Cursor cursor = null;

		if (stockDeal == null) {
			return;
		}

		try {
			cursor = queryStockDealById(stockDeal);

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

	public String getStockDealListAllSelection(Stock stock) {
		String selection = "";

		if (stock == null) {
			return selection;
		}

		selection = DatabaseContract.COLUMN_SE + " = " + "\'" + stock.getSE()
				+ "\'" + " AND " + DatabaseContract.COLUMN_CODE + " = " + "\'"
				+ stock.getCode() + "\'";

		return selection;
	}

	public void getStockDealList(Stock stock,
			ArrayList<StockDeal> stockDealList, String selection) {
		Cursor cursor = null;

		if ((stock == null) || (stockDealList == null)) {
			return;
		}

		stockDealList.clear();

		String sortOrder = DatabaseContract.COLUMN_DEAL + " DESC ";

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

	public void getStockDealList(List<StockDeal> stockDealList) {
		Cursor cursor = null;

		if (stockDealList == null) {
			return;
		}

		stockDealList.clear();

		try {
			cursor = queryStockDeal(null, null, null);

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

	public void getStockDeal(Stock stock, StockDeal stockDeal, String selection, String sortOrder) {
		Cursor cursor = null;

		if ((stock == null) || (stockDeal == null)) {
			return;
		}

		try {
			cursor = queryStockDeal(selection, null, sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					stockDeal.set(cursor);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStockDealToBuy(Stock stock, StockDeal stockDeal) {
        String sortOrder = DatabaseContract.COLUMN_NET + DatabaseContract.ORDER_DIRECTION_ASC;

		if ((stock == null) || (stockDeal == null)) {
			return;
		}

        String selection = DatabaseContract.COLUMN_SE + " = " + "\'" + stock.getSE()
                + "\'" + " AND " + DatabaseContract.COLUMN_CODE + " = " + "\'"
                + stock.getCode() + "\'";

		selection +=   " AND " + DatabaseContract.COLUMN_VOLUME + " <= " + 0 + " AND "
				+ DatabaseContract.COLUMN_PROFIT + " >= " + 0;

		getStockDeal(stock, stockDeal, selection, sortOrder);
	}

	public void getStockDealToSell(Stock stock, StockDeal stockDeal) {
        String sortOrder = DatabaseContract.COLUMN_NET + DatabaseContract.ORDER_DIRECTION_ASC;

		if ((stock == null) || (stockDeal == null)) {
			return;
		}

        String selection = DatabaseContract.COLUMN_SE + " = " + "\'" + stock.getSE()
                + "\'" + " AND " + DatabaseContract.COLUMN_CODE + " = " + "\'"
                + stock.getCode() + "\'";

		selection +=  " AND " + DatabaseContract.COLUMN_VOLUME + " > " + 0 + " AND "
				+ DatabaseContract.COLUMN_PROFIT + " > " + 0;

		getStockDeal(stock, stockDeal, selection, sortOrder);
	}

	public void getStockDealMax(Stock stock, StockDeal stockDeal) {
		String sortOrder = DatabaseContract.COLUMN_DEAL + " DESC ";

		if (stock == null) {
			return;
		}

		String selection = DatabaseContract.COLUMN_SE + " = " + "\'" + stock.getSE()
				+ "\'" + " AND " + DatabaseContract.COLUMN_CODE + " = " + "\'"
				+ stock.getCode() + "\'";

		getStockDeal(stock, stockDeal, selection, sortOrder);
	}

	public void getStockDealMin(Stock stock, StockDeal stockDeal) {
		String sortOrder = DatabaseContract.COLUMN_DEAL + " ASC ";

		if (stock == null) {
			return;
		}

		String selection = DatabaseContract.COLUMN_SE + " = " + "\'" + stock.getSE()
				+ "\'" + " AND " + DatabaseContract.COLUMN_CODE + " = " + "\'"
				+ stock.getCode() + "\'";

		getStockDeal(stock, stockDeal, selection, sortOrder);
	}

	public double getStockDealTargetPrice(Stock stock, int order) {
		double result = 0;

		StockDeal stockDealMax = new StockDeal();

		getStockDealMax(stock, stockDealMax);

		if (stock.getPrice() > 0) {
			result = (1.0 - order * Constants.STOCK_DEAL_DISTRIBUTION_RATE)
					* stock.getPrice();
		} else {
			result = (1.0 - order * Constants.STOCK_DEAL_DISTRIBUTION_RATE)
					* stockDealMax.getDeal();
		}

		return result;
	}

	public Uri insertFinancialData(FinancialData financialData) {
		Uri uri = null;

		if ((financialData == null) || (mContentResolver == null)) {
			return uri;
		}

		uri = mContentResolver.insert(
				DatabaseContract.FinancialData.CONTENT_URI,
				financialData.getContentValues());

		return uri;
	}

	public int bulkInsertFinancialData(ContentValues[] contentValuesArray) {
		int result = 0;

		if ((contentValuesArray.length == 0) || (mContentResolver == null)) {
			return result;
		}

		result = mContentResolver.bulkInsert(
				DatabaseContract.FinancialData.CONTENT_URI, contentValuesArray);

		return result;
	}

	public Cursor queryFinancialData(String selection, String[] selectionArgs,
			String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(
				DatabaseContract.FinancialData.CONTENT_URI,
				DatabaseContract.FinancialData.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor queryFinancialData(FinancialData financialData) {
		Cursor cursor = null;

		if ((financialData == null) || (mContentResolver == null)) {
			return cursor;
		}

		String selection = getFinancialDataSelection(financialData);
		String sortOrder = getFinancialDataOrder();

		cursor = mContentResolver.query(
				DatabaseContract.FinancialData.CONTENT_URI,
				DatabaseContract.FinancialData.PROJECTION_ALL, selection, null,
				sortOrder);

		return cursor;
	}

	public void getFinancialData(FinancialData financialData) {
		Cursor cursor = null;

		if ((financialData == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getFinancialDataSelection(financialData);
			String sortOrder = getFinancialDataOrder();

			cursor = mContentResolver.query(
					DatabaseContract.FinancialData.CONTENT_URI,
					DatabaseContract.FinancialData.PROJECTION_ALL, selection,
					null, sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				financialData.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getFinancialData(Stock stock, FinancialData financialData) {
		Cursor cursor = null;

		if ((financialData == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getFinancialDataSelection(stock.getId());
			String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";

			cursor = mContentResolver.query(
					DatabaseContract.FinancialData.CONTENT_URI,
					DatabaseContract.FinancialData.PROJECTION_ALL, selection,
					null, sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				financialData.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getFinancialDataList(Stock stock,
			ArrayList<FinancialData> financialDataList, String sortOrder) {
		Cursor cursor = null;

		if ((stock == null) || (financialDataList == null)) {
			return;
		}

		financialDataList.clear();

		String selection = getFinancialDataSelection(stock.getId());

		try {
			cursor = queryFinancialData(selection, null, sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					FinancialData financialData = new FinancialData();
					financialData.set(cursor);
					financialDataList.add(financialData);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public boolean isFinancialDataExist(FinancialData financialData) {
		boolean result = false;
		Cursor cursor = null;

		if (financialData == null) {
			return result;
		}

		try {
			cursor = queryFinancialData(financialData);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				financialData.setCreated(cursor);
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	public int updateFinancialData(FinancialData financialData,
			ContentValues contentValues) {
		int result = 0;

		if ((financialData == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getFinancialDataSelection(financialData);

		result = mContentResolver.update(
				DatabaseContract.FinancialData.CONTENT_URI, contentValues,
				where, null);

		return result;
	}

	public int deleteFinancialData() {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		result = mContentResolver.delete(
				DatabaseContract.FinancialData.CONTENT_URI, null, null);

		return result;
	}

	public int deleteFinancialData(FinancialData financialData) {
		int result = 0;

		if ((financialData == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getFinancialDataSelection(financialData);

		result = mContentResolver.delete(
				DatabaseContract.FinancialData.CONTENT_URI, where, null);

		return result;
	}

	public void deleteFinancialData(long stockId) {
		Uri uri = DatabaseContract.FinancialData.CONTENT_URI;

		String where = getFinancialDataSelection(stockId);

		try {
			mContentResolver.delete(uri, where, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int deleteFinancialData(long stockId, String date) {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		String where = getFinancialDataSelection(stockId, date);

		result = mContentResolver.delete(
				DatabaseContract.FinancialData.CONTENT_URI, where, null);

		return result;
	}

	public String getFinancialDataSelection(FinancialData financialData) {
		String selection = "";

		if (financialData == null) {
			return selection;
		}

		selection = getFinancialDataSelection(financialData.getStockId(),
				financialData.getDate());

		return selection;
	}

	public String getFinancialDataSelection(long stockId) {
		return DatabaseContract.COLUMN_STOCK_ID + " = " + stockId;
	}

	public String getFinancialDataSelection(long stockId, String date) {
		return DatabaseContract.COLUMN_STOCK_ID + " = " + stockId + " AND "
				+ DatabaseContract.COLUMN_DATE + " = '" + date + "'";
	}

	public String getFinancialDataOrder() {
		return DatabaseContract.COLUMN_DATE + " ASC ";
	}

	public Uri insertShareBonus(ShareBonus shareBonus) {
		Uri uri = null;

		if ((shareBonus == null) || (mContentResolver == null)) {
			return uri;
		}

		uri = mContentResolver.insert(DatabaseContract.ShareBonus.CONTENT_URI,
				shareBonus.getContentValues());

		return uri;
	}

	public int bulkInsertShareBonus(ContentValues[] contentValuesArray) {
		int result = 0;

		if ((contentValuesArray.length == 0) || (mContentResolver == null)) {
			return result;
		}

		result = mContentResolver.bulkInsert(
				DatabaseContract.ShareBonus.CONTENT_URI, contentValuesArray);

		return result;
	}

	public Cursor queryShareBonus(String selection, String[] selectionArgs,
			String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(
				DatabaseContract.ShareBonus.CONTENT_URI,
				DatabaseContract.ShareBonus.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor queryShareBonus(ShareBonus shareBonus) {
		Cursor cursor = null;

		if ((shareBonus == null) || (mContentResolver == null)) {
			return cursor;
		}

		String selection = getShareBonusSelection(shareBonus);
		String sortOrder = getShareBonusOrder();

		cursor = mContentResolver.query(
				DatabaseContract.ShareBonus.CONTENT_URI,
				DatabaseContract.ShareBonus.PROJECTION_ALL, selection, null,
				sortOrder);

		return cursor;
	}

	public void getShareBonus(ShareBonus shareBonus) {
		Cursor cursor = null;

		if ((shareBonus == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getShareBonusSelection(shareBonus);
			String sortOrder = getShareBonusOrder();

			cursor = mContentResolver.query(
					DatabaseContract.ShareBonus.CONTENT_URI,
					DatabaseContract.ShareBonus.PROJECTION_ALL, selection,
					null, sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				shareBonus.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getShareBonus(long stockId, ShareBonus shareBonus) {
		Cursor cursor = null;

		if ((shareBonus == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getShareBonusSelection(stockId);
			String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";

			cursor = mContentResolver.query(
					DatabaseContract.ShareBonus.CONTENT_URI,
					DatabaseContract.ShareBonus.PROJECTION_ALL, selection,
					null, sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				shareBonus.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getShareBonusList(Stock stock,
			ArrayList<ShareBonus> shareBonusList, String sortOrder) {
		Cursor cursor = null;

		if ((stock == null) || (shareBonusList == null)) {
			return;
		}

		shareBonusList.clear();

		String selection = getShareBonusSelection(stock.getId());

		try {
			cursor = queryShareBonus(selection, null, sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					ShareBonus shareBonus = new ShareBonus();
					shareBonus.set(cursor);
					shareBonusList.add(shareBonus);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public boolean isShareBonusExist(ShareBonus shareBonus) {
		boolean result = false;
		Cursor cursor = null;

		if (shareBonus == null) {
			return result;
		}

		try {
			cursor = queryShareBonus(shareBonus);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				shareBonus.setCreated(cursor);
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	public int updateShareBonus(ShareBonus shareBonus,
			ContentValues contentValues) {
		int result = 0;

		if ((shareBonus == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getShareBonusSelection(shareBonus);

		result = mContentResolver.update(
				DatabaseContract.ShareBonus.CONTENT_URI, contentValues, where,
				null);

		return result;
	}

	public int deleteShareBonus() {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		result = mContentResolver.delete(
				DatabaseContract.ShareBonus.CONTENT_URI, null, null);

		return result;
	}

	public int deleteShareBonus(ShareBonus shareBonus) {
		int result = 0;

		if ((shareBonus == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getShareBonusSelection(shareBonus);

		result = mContentResolver.delete(
				DatabaseContract.ShareBonus.CONTENT_URI, where, null);

		return result;
	}

	public void deleteShareBonus(long stockId) {
		Uri uri = DatabaseContract.ShareBonus.CONTENT_URI;

		String where = getShareBonusSelection(stockId);

		try {
			mContentResolver.delete(uri, where, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int deleteShareBonus(long stockId, String date) {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		String where = getShareBonusSelection(stockId, date);

		result = mContentResolver.delete(
				DatabaseContract.ShareBonus.CONTENT_URI, where, null);

		return result;
	}

	public String getShareBonusSelection(ShareBonus shareBonus) {
		String selection = "";

		if (shareBonus == null) {
			return selection;
		}

		selection = getShareBonusSelection(shareBonus.getStockId(),
				shareBonus.getDate());

		return selection;
	}

	public String getShareBonusSelection(long stockId) {
		return DatabaseContract.COLUMN_STOCK_ID + " = " + stockId;
	}

	public String getShareBonusSelection(long stockId, String date) {
		return DatabaseContract.COLUMN_STOCK_ID + " = " + stockId + " AND "
				+ DatabaseContract.COLUMN_DATE + " = '" + date + "'";
	}

	public String getShareBonusOrder() {
		return DatabaseContract.COLUMN_DATE + " ASC ";
	}

	public Uri insertTotalShare(TotalShare totalShare) {
		Uri uri = null;

		if ((totalShare == null) || (mContentResolver == null)) {
			return uri;
		}

		uri = mContentResolver.insert(DatabaseContract.TotalShare.CONTENT_URI,
				totalShare.getContentValues());

		return uri;
	}

	public int bulkInsertTotalShare(ContentValues[] contentValuesArray) {
		int result = 0;

		if ((contentValuesArray.length == 0) || (mContentResolver == null)) {
			return result;
		}

		result = mContentResolver.bulkInsert(
				DatabaseContract.TotalShare.CONTENT_URI, contentValuesArray);

		return result;
	}

	public Cursor queryTotalShare(String selection, String[] selectionArgs,
			String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(
				DatabaseContract.TotalShare.CONTENT_URI,
				DatabaseContract.TotalShare.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor queryTotalShare(TotalShare totalShare) {
		Cursor cursor = null;

		if ((totalShare == null) || (mContentResolver == null)) {
			return cursor;
		}

		String selection = getTotalShareSelection(totalShare);
		String sortOrder = getTotalShareOrder();

		cursor = mContentResolver.query(
				DatabaseContract.TotalShare.CONTENT_URI,
				DatabaseContract.TotalShare.PROJECTION_ALL, selection, null,
				sortOrder);

		return cursor;
	}

	public void getTotalShare(TotalShare totalShare) {
		Cursor cursor = null;

		if ((totalShare == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getTotalShareSelection(totalShare);
			String sortOrder = getTotalShareOrder();

			cursor = mContentResolver.query(
					DatabaseContract.TotalShare.CONTENT_URI,
					DatabaseContract.TotalShare.PROJECTION_ALL, selection,
					null, sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				totalShare.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getTotalShare(long stockId, TotalShare totalShare) {
		Cursor cursor = null;

		if ((totalShare == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getTotalShareSelection(stockId);
			String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";

			cursor = mContentResolver.query(
					DatabaseContract.TotalShare.CONTENT_URI,
					DatabaseContract.TotalShare.PROJECTION_ALL, selection,
					null, sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				totalShare.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getTotalShareList(Stock stock,
			ArrayList<TotalShare> totalShareList, String sortOrder) {
		Cursor cursor = null;

		if ((stock == null) || (totalShareList == null)) {
			return;
		}

		totalShareList.clear();

		String selection = getTotalShareSelection(stock.getId());

		try {
			cursor = queryTotalShare(selection, null, sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					TotalShare totalShare = new TotalShare();
					totalShare.set(cursor);
					totalShareList.add(totalShare);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public boolean isTotalShareExist(TotalShare totalShare) {
		boolean result = false;
		Cursor cursor = null;

		if (totalShare == null) {
			return result;
		}

		try {
			cursor = queryTotalShare(totalShare);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				totalShare.setCreated(cursor);
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	public int updateTotalShare(TotalShare totalShare,
			ContentValues contentValues) {
		int result = 0;

		if ((totalShare == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getTotalShareSelection(totalShare);

		result = mContentResolver.update(
				DatabaseContract.TotalShare.CONTENT_URI, contentValues, where,
				null);

		return result;
	}

	public int deleteTotalShare() {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		result = mContentResolver.delete(
				DatabaseContract.TotalShare.CONTENT_URI, null, null);

		return result;
	}

	public int deleteTotalShare(TotalShare totalShare) {
		int result = 0;

		if ((totalShare == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getTotalShareSelection(totalShare);

		result = mContentResolver.delete(
				DatabaseContract.TotalShare.CONTENT_URI, where, null);

		return result;
	}

	public void deleteTotalShare(long stockId) {
		Uri uri = DatabaseContract.TotalShare.CONTENT_URI;

		String where = getTotalShareSelection(stockId);

		try {
			mContentResolver.delete(uri, where, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int deleteTotalShare(long stockId, String date) {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		String where = getTotalShareSelection(stockId, date);

		result = mContentResolver.delete(
				DatabaseContract.TotalShare.CONTENT_URI, where, null);

		return result;
	}

	public String getTotalShareSelection(TotalShare totalShare) {
		String selection = "";

		if (totalShare == null) {
			return selection;
		}

		selection = getTotalShareSelection(totalShare.getStockId(),
				totalShare.getDate());

		return selection;
	}
	
	public String getTotalShareSelection(long stockId) {
		return DatabaseContract.COLUMN_STOCK_ID + " = " + stockId;
	}

	public String getTotalShareSelection(long stockId, String date) {
		return DatabaseContract.COLUMN_STOCK_ID + " = " + stockId + " AND "
				+ DatabaseContract.COLUMN_DATE + " = '" + date + "'";
	}

	public String getTotalShareOrder() {
		return DatabaseContract.COLUMN_DATE + " ASC ";
	}

	public Uri insertIPO(IPO ipo) {
		Uri uri = null;

		if ((ipo == null) || (mContentResolver == null)) {
			return uri;
		}

		uri = mContentResolver.insert(DatabaseContract.IPO.CONTENT_URI,
				ipo.getContentValues());

		return uri;
	}

	public int bulkInsertIPO(ContentValues[] contentValuesArray) {
		int result = 0;

		if ((contentValuesArray.length == 0) || (mContentResolver == null)) {
			return result;
		}

		result = mContentResolver.bulkInsert(DatabaseContract.IPO.CONTENT_URI,
				contentValuesArray);

		return result;
	}

	public Cursor queryIPO(String selection, String[] selectionArgs,
			String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(DatabaseContract.IPO.CONTENT_URI,
				DatabaseContract.IPO.PROJECTION_ALL, selection, selectionArgs,
				sortOrder);

		return cursor;
	}

	public Cursor queryIPO(IPO ipo) {
		Cursor cursor = null;

		if ((ipo == null) || (mContentResolver == null)) {
			return cursor;
		}

		String selection = getIPOSelection(ipo);
		String sortOrder = getIPOOrder();

		cursor = mContentResolver
				.query(DatabaseContract.IPO.CONTENT_URI,
						DatabaseContract.IPO.PROJECTION_ALL, selection, null,
						sortOrder);

		return cursor;
	}

	public void getIPO(IPO ipo) {
		Cursor cursor = null;

		if ((ipo == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getIPOSelection(ipo);
			String sortOrder = getIPOOrder();

			cursor = mContentResolver.query(DatabaseContract.IPO.CONTENT_URI,
					DatabaseContract.IPO.PROJECTION_ALL, selection, null,
					sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				ipo.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getIPO(long stockId, IPO ipo) {
		Cursor cursor = null;

		if ((ipo == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getIPOSelection(stockId);
			String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";

			cursor = mContentResolver.query(DatabaseContract.IPO.CONTENT_URI,
					DatabaseContract.IPO.PROJECTION_ALL, selection, null,
					sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				ipo.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getIPOList(ArrayList<IPO> ipoList, String sortOrder) {
		Cursor cursor = null;

		if (ipoList == null) {
			return;
		}

		ipoList.clear();

		String selection = null;

		try {
			cursor = queryIPO(selection, null, sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					IPO ipo = new IPO();
					ipo.set(cursor);
					ipoList.add(ipo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public boolean isIPOExist(IPO ipo) {
		boolean result = false;
		Cursor cursor = null;

		if (ipo == null) {
			return result;
		}

		try {
			cursor = queryIPO(ipo);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				ipo.setCreated(cursor);
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	public int updateIPO(IPO ipo, ContentValues contentValues) {
		int result = 0;

		if ((ipo == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getIPOSelection(ipo);

		result = mContentResolver.update(DatabaseContract.IPO.CONTENT_URI,
				contentValues, where, null);

		return result;
	}

	public int deleteIPO() {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		result = mContentResolver.delete(DatabaseContract.IPO.CONTENT_URI,
				null, null);

		return result;
	}

	public int deleteIPO(IPO ipo) {
		int result = 0;

		if ((ipo == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getIPOSelection(ipo);

		result = mContentResolver.delete(DatabaseContract.IPO.CONTENT_URI,
				where, null);

		return result;
	}

	public int deleteIPO(long stockId, String date) {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		String where = getIPOSelection(stockId, date);

		result = mContentResolver.delete(DatabaseContract.IPO.CONTENT_URI,
				where, null);

		return result;
	}

	public String getIPOSelection(IPO ipo) {
		return getIPOSelection(ipo.getStockId(), ipo.getDate());
	}

	public String getIPOSelection(long stockId) {
		return DatabaseContract.COLUMN_STOCK_ID + " = " + stockId;
	}

	public String getIPOSelection(long stockId, String date) {
		return DatabaseContract.COLUMN_STOCK_ID + " = " + stockId + " AND "
				+ DatabaseContract.COLUMN_DATE + " = '" + date + "'";
	}

	public String getIPOOrder() {
		return DatabaseContract.COLUMN_DATE + " ASC ";
	}
}
