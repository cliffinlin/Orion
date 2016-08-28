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

	public Uri insertDeal(Deal deal) {
		Uri uri = null;

		if ((deal == null) || (mContentResolver == null)) {
			return uri;
		}

		uri = mContentResolver.insert(DatabaseContract.Deal.CONTENT_URI,
				deal.getContentValues());

		return uri;
	}

	public int updateDealByID(Deal deal) {
		int result = 0;

		if ((deal == null) || (mContentResolver == null)) {
			return result;
		}

		String where = DatabaseContract.COLUMN_ID + "=" + deal.getId();

		result = mContentResolver.update(DatabaseContract.Deal.CONTENT_URI,
				deal.getContentValues(), where, null);

		return result;
	}

	public int updateDeal(Stock stock) {
		int result = 0;
		Cursor cursor = null;
		Deal deal = null;

		if ((stock == null) || (mContentResolver == null)) {
			return result;
		}

		deal = new Deal();

		String selection = DatabaseContract.COLUMN_SE + " = " + "\'"
				+ stock.getSE() + "\'" + " AND " + DatabaseContract.COLUMN_CODE
				+ " = " + "\'" + stock.getCode() + "\'";

		try {
			cursor = queryDeal(selection, null, null);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					deal.set(cursor);
					deal.setPrice(stock.getPrice());
					deal.setupDeal();
					result += updateDealByID(deal);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	public void deleteDealById(Deal deal) {
		if ((deal == null) || (mContentResolver == null)) {
			return;
		}

		String where = DatabaseContract.COLUMN_ID + "=" + deal.getId();

		try {
			mContentResolver.delete(DatabaseContract.Deal.CONTENT_URI, where,
					null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Cursor queryDeal(Deal deal) {
		Cursor cursor = null;

		if (deal == null) {
			return cursor;
		}

		String selection = DatabaseContract.COLUMN_SE + " = " + "\'"
				+ deal.getSE() + "\'" + " AND " + DatabaseContract.COLUMN_CODE
				+ " = " + "\'" + deal.getCode() + "\'" + " AND "
				+ DatabaseContract.COLUMN_DEAL + " = " + deal.getDeal()
				+ " AND " + DatabaseContract.COLUMN_VOLUME + " = "
				+ deal.getVolume();

		cursor = queryDeal(selection, null, null);

		return cursor;
	}

	public Cursor queryDeal(String selection, String[] selectionArgs,
			String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(DatabaseContract.Deal.CONTENT_URI,
				DatabaseContract.Deal.PROJECTION_ALL, selection, selectionArgs,
				sortOrder);

		return cursor;
	}

	public Cursor queryDealById(Deal deal) {
		Cursor cursor = null;

		if (deal == null) {
			return cursor;
		}

		String selection = DatabaseContract.COLUMN_ID + "=" + deal.getId();

		cursor = queryDeal(selection, null, null);

		return cursor;
	}

	public void getDealById(Deal deal) {
		Cursor cursor = null;

		if (deal == null) {
			return;
		}

		try {
			cursor = queryDealById(deal);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				deal.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getDealList(Stock stock, ArrayList<Deal> dealList) {
		Cursor cursor = null;
		String selection = "";

		if ((stock == null) || (dealList == null)) {
			return;
		}

		dealList.clear();

		selection = DatabaseContract.COLUMN_SE + " = " + "\'" + stock.getSE()
				+ "\'" + " AND " + DatabaseContract.COLUMN_CODE + " = " + "\'"
				+ stock.getCode() + "\'";

		try {
			cursor = queryDeal(selection, null, null);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					Deal deal = new Deal();
					deal.set(cursor);
					dealList.add(deal);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public boolean isDealExist(Deal deal) {
		boolean result = false;
		Cursor cursor = null;

		if (deal == null) {
			return result;
		}

		try {
			cursor = queryDeal(deal);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				deal.setCreated(cursor);
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
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

	public int deleteStockData(long stockId, String period, String simulation) {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		String where = getStockDataSelection(stockId, period, simulation);

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
				+ DatabaseContract.StockData.COLUMN_SIMULATION + " = " + "\'"
				+ stockData.getSimulation() + "\'" + " AND "
				+ DatabaseContract.StockData.COLUMN_PERIOD + " = " + "\'"
				+ stockData.getPeriod() + "\'" + " AND "
				+ DatabaseContract.StockData.COLUMN_DATE + " = " + "\'"
				+ stockData.getDate() + "\'";

		period = stockData.getPeriod();

		if (period.equals(Constants.PERIOD_1MIN)
				|| period.equals(Constants.PERIOD_5MIN)
				|| period.equals(Constants.PERIOD_15MIN)
				|| period.equals(Constants.PERIOD_30MIN)
				|| period.equals(Constants.PERIOD_60MIN)) {
			where += " AND " + DatabaseContract.StockData.COLUMN_TIME + " = "
					+ "\'" + stockData.getTime() + "\'";
		}

		return where;
	}

	String getStockDataSelection(long stockId, String period) {
		return DatabaseContract.COLUMN_STOCK_ID + " = " + stockId + " AND "
				+ DatabaseContract.StockData.COLUMN_PERIOD + " = '" + period
				+ "'";
	}

	public String getStockDataSelection(long stockId, String period, String simulation) {
		return DatabaseContract.COLUMN_STOCK_ID + " = " + stockId + " AND "
				+ DatabaseContract.StockData.COLUMN_PERIOD + " = '" + period
				+ "'" + " AND " + DatabaseContract.StockData.COLUMN_SIMULATION
				+ " = '" + simulation + "'";
	}

	public String getStockDataOrder() {
		return DatabaseContract.StockData.COLUMN_DATE + " ASC " + ","
				+ DatabaseContract.StockData.COLUMN_TIME + " ASC ";
	}
}
