package com.android.orion.database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.ArrayMap;

import com.android.orion.Constants;
import com.android.orion.Settings;

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

	public void getStockList(String selection, ArrayList<Stock> stockList) {
		Cursor cursor = null;

		if (stockList == null) {
			return;
		}

		stockList.clear();

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

	public void getFavoriteStockList(ArrayList<Stock> stockList) {
		String selection = DatabaseContract.COLUMN_FLAG + " = "
				+ Stock.FLAG_FAVORITE;

		getStockList(selection, stockList);
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

	public void getStock(Uri uri, Stock stock) {
        Cursor cursor = null;

        if (stock == null) {
            return;
        }

        try {
            cursor = mContentResolver
                    .query(uri, DatabaseContract.Stock.PROJECTION_ALL,
                            null, null, null);
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

		String selection = getStockDataSelection(stock.getId(), period, StockData.LEVEL_NONE);

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

		if (period.equals(Settings.KEY_PERIOD_MIN1)
				|| period.equals(Settings.KEY_PERIOD_MIN5)
				|| period.equals(Settings.KEY_PERIOD_MIN15)
				|| period.equals(Settings.KEY_PERIOD_MIN30)
				|| period.equals(Settings.KEY_PERIOD_MIN60)) {
			selection += " AND " + DatabaseContract.COLUMN_TIME + " = " + "\'"
					+ stockData.getTime() + "\'";
		}

		return selection;
	}

	public String getStockDataSelection(long stockId, String period) {
		return DatabaseContract.COLUMN_STOCK_ID + " = " + stockId + " AND "
				+ DatabaseContract.COLUMN_PERIOD + " = '" + period + "'";
	}

	public String getStockDataSelection(long stockId, String period, int level) {
		return DatabaseContract.COLUMN_STOCK_ID + " = " + stockId
				+ " AND " + DatabaseContract.COLUMN_PERIOD + " = '" + period + "'"
				+ " AND " + DatabaseContract.COLUMN_LEVEL + " = '" + level + "'";
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

    public int bulkInsertStockDeal(ContentValues[] contentValuesArray) {
        int result = 0;

        if ((contentValuesArray.length == 0) || (mContentResolver == null)) {
            return result;
        }

        result = mContentResolver.bulkInsert(
                DatabaseContract.StockDeal.CONTENT_URI, contentValuesArray);

        return result;
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
					stockDeal.setupFee(stock.getRDate(), stock.getDividend());
					stockDeal.setupNet();
					stockDeal.setupValue();
					stockDeal.setupProfit();
					stockDeal.setupBonus(stock.getDividend());
					stockDeal.setupYield(stock.getDividend());

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

	public void getStockDealList(Stock stock,
			ArrayList<StockDeal> stockDealList, String selection, String sortOrder) {
		Cursor cursor = null;

		if ((stock == null) || (stockDealList == null)) {
			return;
		}

		stockDealList.clear();

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

    public void getStockDealListToSell(Stock stock, ArrayList<StockDeal> stockDealList) {
        String sortOrder = DatabaseContract.COLUMN_NET + DatabaseContract.ORDER_DIRECTION_ASC;

        if ((stock == null) || (stockDealList == null)) {
            return;
        }

        String selection = DatabaseContract.COLUMN_SE + " = " + "\'" + stock.getSE()
                + "\'" + " AND " + DatabaseContract.COLUMN_CODE + " = " + "\'"
                + stock.getCode() + "\'";

		selection += " AND " + DatabaseContract.COLUMN_VOLUME + " > " + 0 ;
		selection += " AND " + DatabaseContract.COLUMN_PROFIT + " > " + DatabaseContract.COLUMN_BONUS;
		selection += " AND " + DatabaseContract.COLUMN_NET + " > " + Constants.AVERAGE_DIVIDEND_YIELD;

        getStockDealList(stock, stockDealList, selection, sortOrder);
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
			result = (1.0 - order * StockDeal.DISTRIBUTION_RATE)
					* stock.getPrice();
		} else {
			result = (1.0 - order * StockDeal.DISTRIBUTION_RATE)
					* stockDealMax.getDeal();
		}

		return result;
	}

	public Uri insertStockFinancial(StockFinancial stockFinancial) {
		Uri uri = null;

		if ((stockFinancial == null) || (mContentResolver == null)) {
			return uri;
		}

		uri = mContentResolver.insert(
				DatabaseContract.StockFinancial.CONTENT_URI,
				stockFinancial.getContentValues());

		return uri;
	}

	public int bulkInsertStockFinancial(ContentValues[] contentValuesArray) {
		int result = 0;

		if ((contentValuesArray.length == 0) || (mContentResolver == null)) {
			return result;
		}

		result = mContentResolver.bulkInsert(
				DatabaseContract.StockFinancial.CONTENT_URI, contentValuesArray);

		return result;
	}

	public Cursor queryStockFinancial(String selection, String[] selectionArgs,
			String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(
				DatabaseContract.StockFinancial.CONTENT_URI,
				DatabaseContract.StockFinancial.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor queryStockFinancial(StockFinancial stockFinancial) {
		Cursor cursor = null;

		if ((stockFinancial == null) || (mContentResolver == null)) {
			return cursor;
		}

		String selection = getStockFinancialSelection(stockFinancial);
		String sortOrder = getStockFinancialOrder();

		cursor = mContentResolver.query(
				DatabaseContract.StockFinancial.CONTENT_URI,
				DatabaseContract.StockFinancial.PROJECTION_ALL, selection, null,
				sortOrder);

		return cursor;
	}

	public void getStockFinancial(StockFinancial stockFinancial) {
		Cursor cursor = null;

		if ((stockFinancial == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getStockFinancialSelection(stockFinancial);
			String sortOrder = getStockFinancialOrder();

			cursor = mContentResolver.query(
					DatabaseContract.StockFinancial.CONTENT_URI,
					DatabaseContract.StockFinancial.PROJECTION_ALL, selection,
					null, sortOrder);

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

	public void getStockFinancial(Stock stock, StockFinancial stockFinancial) {
		Cursor cursor = null;

		if ((stockFinancial == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getStockFinancialSelection(stock.getId());
			String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";

			cursor = mContentResolver.query(
					DatabaseContract.StockFinancial.CONTENT_URI,
					DatabaseContract.StockFinancial.PROJECTION_ALL, selection,
					null, sortOrder);

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

	public void getStockFinancialList(Stock stock,
			ArrayList<StockFinancial> stockFinancialList, String sortOrder) {
		Cursor cursor = null;

		if ((stock == null) || (stockFinancialList == null)) {
			return;
		}

		stockFinancialList.clear();

		String selection = getStockFinancialSelection(stock.getId());

		try {
			cursor = queryStockFinancial(selection, null, sortOrder);

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
		Cursor cursor = null;

		if (stockFinancial == null) {
			return result;
		}

		try {
			cursor = queryStockFinancial(stockFinancial);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stockFinancial.setCreated(cursor);
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	public int updateStockFinancial(StockFinancial stockFinancial,
			ContentValues contentValues) {
		int result = 0;

		if ((stockFinancial == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getStockFinancialSelection(stockFinancial);

		result = mContentResolver.update(
				DatabaseContract.StockFinancial.CONTENT_URI, contentValues,
				where, null);

		return result;
	}

	public int deleteStockFinancial() {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		result = mContentResolver.delete(
				DatabaseContract.StockFinancial.CONTENT_URI, null, null);

		return result;
	}

	public int deleteStockFinancial(StockFinancial stockFinancial) {
		int result = 0;

		if ((stockFinancial == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getStockFinancialSelection(stockFinancial);

		result = mContentResolver.delete(
				DatabaseContract.StockFinancial.CONTENT_URI, where, null);

		return result;
	}

	public void deleteStockFinancial(long stockId) {
		Uri uri = DatabaseContract.StockFinancial.CONTENT_URI;

		String where = getStockFinancialSelection(stockId);

		try {
			mContentResolver.delete(uri, where, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int deleteStockFinancial(long stockId, String date) {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		String where = getStockFinancialSelection(stockId, date);

		result = mContentResolver.delete(
				DatabaseContract.StockFinancial.CONTENT_URI, where, null);

		return result;
	}

	public String getStockFinancialSelection(StockFinancial stockFinancial) {
		String selection = "";

		if (stockFinancial == null) {
			return selection;
		}

		selection = getStockFinancialSelection(stockFinancial.getStockId(),
				stockFinancial.getDate());

		return selection;
	}

	public String getStockFinancialSelection(long stockId) {
		return DatabaseContract.COLUMN_STOCK_ID + " = " + stockId;
	}

	public String getStockFinancialSelection(long stockId, String date) {
		return DatabaseContract.COLUMN_STOCK_ID + " = " + stockId + " AND "
				+ DatabaseContract.COLUMN_DATE + " = '" + date + "'";
	}

	public String getStockFinancialOrder() {
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

	public Uri insertIndexComponent(IndexComponent indexComponent) {
		Uri uri = null;

		if ((indexComponent == null) || (mContentResolver == null)) {
			return uri;
		}

		uri = mContentResolver.insert(DatabaseContract.IndexComponent.CONTENT_URI,
				indexComponent.getContentValues());

		return uri;
	}

	public int bulkInsertIndexComponent(ContentValues[] contentValuesArray) {
		int result = 0;

		if ((contentValuesArray.length == 0) || (mContentResolver == null)) {
			return result;
		}

		result = mContentResolver.bulkInsert(DatabaseContract.IndexComponent.CONTENT_URI,
				contentValuesArray);

		return result;
	}

	public Cursor queryIndexComponent(String selection, String[] selectionArgs,
						   String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(DatabaseContract.IndexComponent.CONTENT_URI,
				DatabaseContract.IndexComponent.PROJECTION_ALL, selection, selectionArgs,
				sortOrder);

		return cursor;
	}

	public Cursor queryIndexComponent(IndexComponent indexComponent) {
		Cursor cursor = null;

		if ((indexComponent == null) || (mContentResolver == null)) {
			return cursor;
		}

		String selection = getIndexComponentSelection(indexComponent);
		String sortOrder = getIndexComponentOrder();

		cursor = mContentResolver
				.query(DatabaseContract.IndexComponent.CONTENT_URI,
						DatabaseContract.IndexComponent.PROJECTION_ALL, selection, null,
						sortOrder);

		return cursor;
	}

	public void getIndexComponent(IndexComponent indexComponent) {
		Cursor cursor = null;

		if ((indexComponent == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getIndexComponentSelection(indexComponent);
			String sortOrder = getIndexComponentOrder();

			cursor = mContentResolver.query(DatabaseContract.IndexComponent.CONTENT_URI,
					DatabaseContract.IndexComponent.PROJECTION_ALL, selection, null,
					sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				indexComponent.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getIndexComponent(String stockCode, IndexComponent indexComponent) {
		Cursor cursor = null;

		if ((indexComponent == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getIndexComponentSelection(stockCode);
			String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";

			cursor = mContentResolver.query(DatabaseContract.IndexComponent.CONTENT_URI,
					DatabaseContract.IndexComponent.PROJECTION_ALL, selection, null,
					sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				indexComponent.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getIndexComponentList(ArrayList<IndexComponent> indexComponentList, String selection, String sortOrder) {
		Cursor cursor = null;

		if (indexComponentList == null) {
			return;
		}

		indexComponentList.clear();

		try {
			cursor = queryIndexComponent(selection, null, sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					IndexComponent indexComponent = new IndexComponent();
					indexComponent.set(cursor);
					indexComponentList.add(indexComponent);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public boolean isIndexComponentExist(IndexComponent indexComponent) {
		boolean result = false;
		Cursor cursor = null;

		if (indexComponent == null) {
			return result;
		}

		try {
			cursor = queryIndexComponent(indexComponent);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				indexComponent.setCreated(cursor);
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	public int updateIndexComponent(IndexComponent indexComponent, ContentValues contentValues) {
		int result = 0;

		if ((indexComponent == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getIndexComponentSelection(indexComponent);

		result = mContentResolver.update(DatabaseContract.IndexComponent.CONTENT_URI,
				contentValues, where, null);

		return result;
	}

	public int deleteIndexComponent() {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		result = mContentResolver.delete(DatabaseContract.IndexComponent.CONTENT_URI,
				null, null);

		return result;
	}

	public int deleteIndexComponent(IndexComponent indexComponent) {
		int result = 0;

		if ((indexComponent == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getIndexComponentSelection(indexComponent);

		result = mContentResolver.delete(DatabaseContract.IndexComponent.CONTENT_URI,
				where, null);

		return result;
	}

	public int deleteIndexComponent(String indexCode, String stockCode) {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		String where = getIndexComponentSelection(indexCode, stockCode);

		result = mContentResolver.delete(DatabaseContract.IndexComponent.CONTENT_URI,
				where, null);

		return result;
	}

	public String getIndexComponentSelection(IndexComponent indexComponent) {
		return getIndexComponentSelection(indexComponent.getCode());
	}

	public String getIndexComponentSelection(String stockCode) {
		return DatabaseContract.COLUMN_CODE + " = " + stockCode;
	}

	public String getIndexComponentSelection(String indexCode, String stockCode) {
		return DatabaseContract.COLUMN_INDEX_CODE + " = " + indexCode
				+ " AND "
				+ DatabaseContract.COLUMN_CODE + " = " + stockCode;
	}

	public String getIndexComponentOrder() {
		return DatabaseContract.IndexComponent.SORT_ORDER_DEFAULT;
	}

	public Uri insertStockTrends(StockTrends stockTrends) {
		Uri uri = null;

		if ((stockTrends == null) || (mContentResolver == null)) {
			return uri;
		}

		uri = mContentResolver.insert(DatabaseContract.StockTrends.CONTENT_URI,
				stockTrends.getContentValues());

		return uri;
	}

	public int bulkInsertStockTrends(ContentValues[] contentValuesArray) {
		int result = 0;

		if ((contentValuesArray.length == 0) || (mContentResolver == null)) {
			return result;
		}

		result = mContentResolver.bulkInsert(DatabaseContract.StockTrends.CONTENT_URI,
				contentValuesArray);

		return result;
	}

	public Cursor queryStockTrends(String selection, String[] selectionArgs,
									  String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(DatabaseContract.StockTrends.CONTENT_URI,
				DatabaseContract.StockTrends.PROJECTION_ALL, selection, selectionArgs,
				sortOrder);

		return cursor;
	}

	public Cursor queryStockTrends(StockTrends stockTrends) {
		Cursor cursor = null;

		if ((stockTrends == null) || (stockTrends == null)) {
			return cursor;
		}

		String selection = DatabaseContract.COLUMN_STOCK_ID + " = " + stockTrends.getStockId()
				+ " AND " + DatabaseContract.COLUMN_DATE + " = " + "\'" + stockTrends.getDate() + "\'"
				+ " AND " + DatabaseContract.COLUMN_TIME + " = " + "\'" + stockTrends.getTime() + "\'";
		String sortOrder = getStockTrendsOrder();

		cursor = mContentResolver
				.query(DatabaseContract.StockTrends.CONTENT_URI,
						DatabaseContract.StockTrends.PROJECTION_ALL, selection, null,
						sortOrder);

		return cursor;
	}

	public Cursor queryStockTrendsById(StockTrends stockTrends) {
		Cursor cursor = null;

		if (stockTrends == null) {
			return cursor;
		}

		String selection = DatabaseContract.COLUMN_ID + "=" + stockTrends.getId();

		cursor = queryStockTrends(selection, null, null);

		return cursor;
	}

	public void getStockTrendsById(StockTrends stockTrends) {
		Cursor cursor = null;

		if (stockTrends == null) {
			return;
		}

		try {
			cursor = queryStockTrendsById(stockTrends);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stockTrends.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStockTrends(StockTrends stockTrends) {
		Cursor cursor = null;

		if ((stockTrends == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getStockTrendsSelection(stockTrends);
			String sortOrder = getStockTrendsOrder();

			cursor = mContentResolver.query(DatabaseContract.StockTrends.CONTENT_URI,
					DatabaseContract.StockTrends.PROJECTION_ALL, selection, null,
					sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stockTrends.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStockTrends(String stockCode, StockTrends stockTrends) {
		Cursor cursor = null;

		if ((stockTrends == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getStockTrendsSelection(stockCode);
			String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";

			cursor = mContentResolver.query(DatabaseContract.StockTrends.CONTENT_URI,
					DatabaseContract.StockTrends.PROJECTION_ALL, selection, null,
					sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stockTrends.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStockTrendsList(ArrayList<StockTrends> stockTrendsList, String selection, String sortOrder) {
		Cursor cursor = null;

		if (stockTrendsList == null) {
			return;
		}

		stockTrendsList.clear();

		try {
			cursor = queryStockTrends(selection, null, sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockTrends stockTrends = new StockTrends();
					stockTrends.set(cursor);
					stockTrendsList.add(stockTrends);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public boolean isStockTrendsExist(StockTrends stockTrends) {
		boolean result = false;
		Cursor cursor = null;

		if (stockTrends == null) {
			return result;
		}

		try {
			cursor = queryStockTrends(stockTrends);

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

	public int updateStockTrends(StockTrends stockTrends, ContentValues contentValues) {
		int result = 0;

		if ((stockTrends == null) || (mContentResolver == null)) {
			return result;
		}

		String where = DatabaseContract.COLUMN_STOCK_ID + " = " + stockTrends.getStockId()
				+ " AND " + DatabaseContract.COLUMN_DATE + " = " + "\'" + stockTrends.getDate() + "\'"
				+ " AND " + DatabaseContract.COLUMN_TIME + " = " + "\'" + stockTrends.getTime() + "\'";

		result = mContentResolver.update(DatabaseContract.StockTrends.CONTENT_URI,
				contentValues, where, null);

		return result;
	}

	public int deleteStockTrends() {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		result = mContentResolver.delete(DatabaseContract.StockTrends.CONTENT_URI,
				null, null);

		return result;
	}

	public int deleteStockTrends(StockTrends stockTrends) {
		int result = 0;

		if ((stockTrends == null) || (mContentResolver == null)) {
			return result;
		}

		String where = DatabaseContract.COLUMN_STOCK_ID + " = " + stockTrends.getStockId();

		result = mContentResolver.delete(DatabaseContract.StockTrends.CONTENT_URI,
				where, null);

		return result;
	}

	public int deleteStockTrends(String indexCode, String stockCode) {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		String where = getStockTrendsSelection(indexCode, stockCode);

		result = mContentResolver.delete(DatabaseContract.StockTrends.CONTENT_URI,
				where, null);

		return result;
	}

	public String getStockTrendsSelection(StockTrends stockTrends) {
		return getStockTrendsSelection(stockTrends.getCode());
	}

	public String getStockTrendsSelection(String stockCode) {
		return DatabaseContract.COLUMN_CODE + " = " + stockCode;
	}

	public String getStockTrendsSelection(String indexCode, String stockCode) {
		return DatabaseContract.COLUMN_INDEX_CODE + " = " + indexCode
				+ " AND "
				+ DatabaseContract.COLUMN_CODE + " = " + stockCode;
	}

	public String getStockTrendsOrder() {
		return DatabaseContract.COLUMN_DATE + " ASC " + ","
				+ DatabaseContract.COLUMN_TIME + " ASC ";
	}
}
