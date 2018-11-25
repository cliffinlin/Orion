package com.android.orion.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.android.orion.Constants;
import com.android.orion.utility.Utility;

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

	Uri insertSetting(Setting setting) {
		Uri uri = null;

		if (setting == null) {
			return uri;
		}

		if (mContentResolver == null) {
			return uri;
		}

		uri = mContentResolver.insert(DatabaseContract.Setting.CONTENT_URI,
				setting.getContentValues());

		return uri;
	}

	public Cursor querySetting(String selection, String[] selectionArgs,
			String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(DatabaseContract.Setting.CONTENT_URI,
				DatabaseContract.Setting.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor querySetting(Setting setting) {
		Cursor cursor = null;

		if (setting == null) {
			return cursor;
		}

		String selection = DatabaseContract.Setting.COLUMN_KEY + " = " + "\'"
				+ setting.getKey() + "\'";

		cursor = querySetting(selection, null, null);

		return cursor;
	}

	public boolean isSettingExist(Setting setting) {
		boolean result = false;
		Cursor cursor = null;

		if (setting == null) {
			return result;
		}

		try {
			cursor = querySetting(setting);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				setting.setCreated(cursor);
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	public int updateSetting(Setting setting) {
		int result = 0;

		if (setting == null) {
			return result;
		}

		if (mContentResolver == null) {
			return result;
		}

		String where = DatabaseContract.Setting.COLUMN_KEY + " = " + "\'"
				+ setting.getKey() + "\'";

		result = mContentResolver.update(DatabaseContract.Setting.CONTENT_URI,
				setting.getContentValues(), where, null);

		return result;
	}

	public void saveSetting(Setting setting) {
		String now = Utility.getCurrentDateTimeString();

		if (setting == null) {
			return;
		}

		try {
			if (!isSettingExist(setting)) {
				setting.setCreated(now);
				insertSetting(setting);
			} else {
				setting.setModified(now);
				updateSetting(setting);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveSetting(String key, boolean value) {
		Setting setting = new Setting();

		setting.setKey(key);
		if (value) {
			setting.setValue("1");
		} else {
			setting.setValue("0");
		}

		saveSetting(setting);
	}

	public void saveSetting(String key, String value) {
		Setting setting = new Setting();

		setting.setKey(key);
		setting.setValue(value);

		saveSetting(setting);
	}

	public String getSettingString(String key) {
		String value = "";
		Cursor cursor = null;
		Setting setting = new Setting();

		setting.setKey(key);

		try {
			cursor = querySetting(setting);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				setting.set(cursor);
				value = setting.getValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return value;
	}

	public int getSettingInt(String key) {
		int result = 0;
		String value = getSettingString(key);

		if (!TextUtils.isEmpty(value)) {
			try {
				result = Integer.parseInt(value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	public boolean getSettingBoolean(String key, boolean defaultValue) {
		boolean result = defaultValue;
		String value = getSettingString(key);

		if (!TextUtils.isEmpty(value)) {
			if ("1".equals(value)) {
				result = true;
			} else {
				result = false;
			}
		}

		return result;
	}

	public int getSettingInt(String key, int defaultValue) {
		int result = defaultValue;
		String value = getSettingString(key);

		if (!TextUtils.isEmpty(value)) {
			try {
				result = Integer.parseInt(value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	public long getSettingLong(String key, long defaultValue) {
		long result = defaultValue;
		String value = getSettingString(key);

		if (!TextUtils.isEmpty(value)) {
			try {
				result = Long.parseLong(value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	public double getSettingDouble(String key, double defaultValue) {
		double result = defaultValue;
		String value = getSettingString(key);

		if (!TextUtils.isEmpty(value)) {
			try {
				result = Double.parseDouble(value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
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

	public void deleteStockData(String stockID) {
		Uri uri = DatabaseContract.StockData.CONTENT_URI;
		String where = null;

		if (!TextUtils.isEmpty(stockID)) {
			where = DatabaseContract.COLUMN_STOCK_ID + "=" + stockID;
		}

		try {
			mContentResolver.delete(uri, where, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		String where = "";
		String period = "";

		if ((stockData == null) || (mContentResolver == null)) {
			return where;
		}

		where = DatabaseContract.COLUMN_STOCK_ID + " = "
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
			where += " AND " + DatabaseContract.COLUMN_TIME + " = " + "\'"
					+ stockData.getTime() + "\'";
		}

		return where;
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
		double value = 0;

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
					stockDeal.setDividend(stock.getDividend());
					stockDeal.setupDividendYield();
					stockDeal.setupNet();
					stockDeal.setupProfit();

					result += updateStockDealByID(stockDeal);

					value += stockDeal.getDeal() * stockDeal.getVolume();
					hold += stockDeal.getVolume();
				}
			}

			stock.setupDividendYield();
			stock.setHold(hold);
			stock.setupCost(value);
			stock.setupProfit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	public void deleteStockDealById(StockDeal stockDeal) {
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

	public void getStockDealList(Stock stock, ArrayList<StockDeal> stockDealList) {
		Cursor cursor = null;
		String selection = "";

		if ((stock == null) || (stockDealList == null)) {
			return;
		}

		stockDealList.clear();

		selection = DatabaseContract.COLUMN_SE + " = " + "\'" + stock.getSE()
				+ "\'" + " AND " + DatabaseContract.COLUMN_CODE + " = " + "\'"
				+ stock.getCode() + "\'";

		try {
			cursor = queryStockDeal(selection, null, null);

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

	public void getStockDeal(Stock stock, StockDeal stockDeal, String sortOrder) {
		Cursor cursor = null;
		String selection = "";

		if ((stock == null) || (stockDeal == null)) {
			return;
		}

		selection = DatabaseContract.COLUMN_SE + " = " + "\'" + stock.getSE()
				+ "\'" + " AND " + DatabaseContract.COLUMN_CODE + " = " + "\'"
				+ stock.getCode() + "\'" + " AND "
				+ DatabaseContract.COLUMN_VOLUME + " > " + 0;

		try {
			cursor = queryStockDeal(selection, null, null);

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

	public void getStockDealMax(Stock stock, StockDeal stockDeal) {
		String sortOrder = DatabaseContract.COLUMN_DEAL + " DESC ";

		getStockDeal(stock, stockDeal, sortOrder);
	}

	public void getStockDealMin(Stock stock, StockDeal stockDeal) {
		String sortOrder = DatabaseContract.COLUMN_DEAL + " ASC ";

		getStockDeal(stock, stockDeal, sortOrder);
	}

	public void getStockDealTarget(Stock stock, StockDeal stockDeal) {
		Cursor cursor = null;
		String selection = "";

		if ((stock == null) || (stockDeal == null)) {
			return;
		}

		selection = DatabaseContract.COLUMN_SE + " = " + "\'" + stock.getSE()
				+ "\'" + " AND " + DatabaseContract.COLUMN_CODE + " = " + "\'"
				+ stock.getCode() + "\'" + " AND "
				+ DatabaseContract.COLUMN_VOLUME + " = " + 0;
		
		String sortOrder = DatabaseContract.COLUMN_DEAL + " DESC ";
		
		try {
			cursor = queryStockDeal(selection, null, sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					stockDeal.set(cursor);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
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
}
