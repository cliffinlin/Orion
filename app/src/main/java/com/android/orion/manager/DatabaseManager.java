package com.android.orion.manager;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.android.orion.data.Period;
import com.android.orion.data.Trend;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.DatabaseOpenHelper;
import com.android.orion.database.IndexComponent;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDeal;
import com.android.orion.database.StockFinancial;
import com.android.orion.database.StockQuant;
import com.android.orion.database.StockTrend;
import com.android.orion.database.TotalShare;
import com.android.orion.interfaces.StockListener;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatabaseManager implements StockListener {
	private static volatile DatabaseManager mInstance;
	private static Context mContext;

	public ContentResolver mContentResolver;
	public SQLiteDatabase mDatabase = null;
	public DatabaseOpenHelper mDatabaseHelper;

	private DatabaseManager(Context context) {
		mContext = context; //ContentProvider getContext()
		mContentResolver = mContext.getContentResolver();
		mDatabaseHelper = new DatabaseOpenHelper(mContext);
		StockManager.getInstance().registerStockListener(this);
	}

	public static synchronized DatabaseManager getInstance() {
		return mInstance;
	}

	public static synchronized DatabaseManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DatabaseManager(context);
		}
		return mInstance;
	}

	public void openDatabase() {
		if (mDatabaseHelper != null) {
			mDatabase = mDatabaseHelper.getWritableDatabase();
		}
	}

	public void closeDatabase() {
		if (mDatabaseHelper != null) {
			mDatabaseHelper.close();
		}
	}

	public void closeCursor(Cursor cursor) {
		if (cursor != null) {
			if (!cursor.isClosed()) {
				cursor.close();
			}
		}
	}

	public int delete(Uri uri) {
		return delete(uri, null);
	}

	public int delete(Uri uri, String where) {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		if (uri == null) {
			return result;
		}

		try {
			result = mContentResolver.delete(uri, where, null);
		} catch (Exception e) {
			e.printStackTrace();
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

		if (contentValuesArray == null) {
			return result;
		}

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

		String selection = DatabaseContract.COLUMN_SE + " = " + "'"
				+ stock.getSE() + "'" + " AND " + DatabaseContract.COLUMN_CODE
				+ " = " + "'" + stock.getCode() + "'";

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
		String selection = DatabaseContract.COLUMN_FLAG + " >= "
				+ Stock.FLAG_FAVORITE;

		getStockList(selection, stockList);
	}

	public void loadStockArrayMap(ArrayMap<String, Stock> stockArrayMap) {
		String selection = "";
		Cursor cursor = null;

		if (stockArrayMap == null) {
			return;
		}

		selection += DatabaseContract.COLUMN_FLAG + " >= "
				+ Stock.FLAG_FAVORITE;

		try {
			stockArrayMap.clear();
			cursor = queryStock(selection, null, null);
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

		String where = DatabaseContract.COLUMN_SE + " = " + "'"
				+ stock.getSE() + "'" + " AND " + DatabaseContract.COLUMN_CODE
				+ " = " + "'" + stock.getCode() + "'";

		result = mContentResolver.update(DatabaseContract.Stock.CONTENT_URI,
				contentValues, where, null);

		return result;
	}

	public void updateStockFlag(Stock stock) {
		Uri uri;

		if ((stock == null) || (mContentResolver == null)) {
			return;
		}

		uri = ContentUris.withAppendedId(
				DatabaseContract.Stock.CONTENT_URI, stock.getId());

		try {
			ContentValues contentValues = new ContentValues();
			contentValues.put(DatabaseContract.COLUMN_FLAG, stock.getFlag());
			mContentResolver.update(uri, contentValues, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int deleteStock() {
		return delete(DatabaseContract.Stock.CONTENT_URI);
	}

	public int deleteStock(long id) {
		int result = 0;
		String where = DatabaseContract.COLUMN_ID + "=" + id;

		try {
			result = delete(DatabaseContract.Stock.CONTENT_URI, where);
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

		if (contentValuesArray == null) {
			return result;
		}

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
					+ DatabaseContract.COLUMN_PERIOD + " = " + "'"
					+ stockData.getPeriod() + "'";
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

		String selection = getStockDataSelection(stock.getId(), period, Trend.LEVEL_NONE);

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

	public void loadStockDataList(Stock stock, String period,
	                              ArrayList<StockData> stockDataList) {
		Calendar calendar = Calendar.getInstance();
		Cursor cursor = null;
		String selection = null;
		String sortOrder = null;

		if ((stock == null) || TextUtils.isEmpty(period)
				|| (stockDataList == null)) {
			return;
		}

		boolean loopback = Setting.getDebugLoopback();
		if (loopback) {
			String dateTime = Preferences.getString(Setting.SETTING_DEBUG_LOOPBACK_DATE_TIME, "");
			if (!TextUtils.isEmpty(dateTime)) {
				calendar = Utility.getCalendar(dateTime, Utility.CALENDAR_DATE_TIME_FORMAT);
			} else {
				calendar = Calendar.getInstance();
			}
		}

		try {
			stockDataList.clear();

			selection = getStockDataSelection(
					stock.getId(), period, Trend.LEVEL_NONE);
			sortOrder = getStockDataOrder();
			cursor = queryStockData(selection, null,
					sortOrder);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				int index = 0;
				while (cursor.moveToNext()) {
					StockData stockData = new StockData(period);
					stockData.set(cursor);
					index = stockDataList.size();
					stockData.setIndex(index);
					stockData.getTrend().setIndexStart(index);
					stockData.getTrend().setIndexEnd(index);
					stockData.setAction(Trend.MARK_NONE);

					if (loopback) {
						if (stockData.getCalendar().after(calendar)) {
							stock.setPrice(stockData.getCandlestick().getClose());
							break;
						}
					}

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

	public void updateStockData(Stock stock, String period, ArrayList<StockData> stockDataList) {
		if ((stockDataList == null) || (stockDataList.size() == 0)) {
			return;
		}

		try {
			deleteStockData(stock.getId(), period);

			ContentValues[] contentValues = new ContentValues[stockDataList.size()];
			for (int i = 0; i < stockDataList.size(); i++) {
				StockData stockData = stockDataList.get(i);
				stockData.setModified(Utility.getCurrentDateTimeString());
				contentValues[i] = stockData.getContentValues();
			}

			bulkInsertStockData(contentValues);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int deleteStockData() {
		return delete(DatabaseContract.StockData.CONTENT_URI);
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
		String where = DatabaseContract.COLUMN_STOCK_ID + "=" + stockId;

		try {
			result = delete(DatabaseContract.StockData.CONTENT_URI, where);
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
				+ DatabaseContract.COLUMN_PERIOD + " = " + "'"
				+ stockData.getPeriod() + "'" + " AND "
				+ DatabaseContract.COLUMN_DATE + " = " + "'"
				+ stockData.getDate() + "'";

		period = stockData.getPeriod();

		if (Period.isMinutePeriod(period)) {
			selection += " AND " + DatabaseContract.COLUMN_TIME + " = " + "'"
					+ stockData.getTime() + "'";
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

	public Uri insertStockTrend(StockTrend stockTrend) {
		Uri uri = null;

		if ((stockTrend == null) || (mContentResolver == null)) {
			return uri;
		}

		uri = mContentResolver.insert(DatabaseContract.StockTrend.CONTENT_URI,
				stockTrend.getContentValues());

		return uri;
	}

	public int bulkInsertStockTrend(ContentValues[] contentValuesArray) {
		int result = 0;

		if (contentValuesArray == null) {
			return result;
		}

		if ((contentValuesArray.length == 0) || (mContentResolver == null)) {
			return result;
		}

		result = mContentResolver.bulkInsert(
				DatabaseContract.StockTrend.CONTENT_URI, contentValuesArray);

		return result;
	}

	public Cursor queryStockTrend(String[] projection, String selection,
	                             String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(DatabaseContract.StockTrend.CONTENT_URI,
				projection, selection, selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor queryStockTrend(String selection, String[] selectionArgs,
	                             String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(DatabaseContract.StockTrend.CONTENT_URI,
				DatabaseContract.StockTrend.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor queryStockTrend(StockTrend stockTrend) {
		Cursor cursor = null;

		if ((stockTrend == null) || (mContentResolver == null)) {
			return cursor;
		}

		String selection = getStockTrendSelection(stockTrend);
		String sortOrder = getStockTrendOrder();

		cursor = mContentResolver.query(DatabaseContract.StockTrend.CONTENT_URI,
				DatabaseContract.StockTrend.PROJECTION_ALL, selection, null,
				sortOrder);

		return cursor;
	}

	public void getStockTrend(StockTrend stockTrend) {
		Cursor cursor = null;

		if ((stockTrend == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getStockTrendSelection(stockTrend);
			String sortOrder = DatabaseContract.COLUMN_DATE + " DESC " + ","
					+ DatabaseContract.COLUMN_TIME + " DESC ";

			cursor = mContentResolver.query(
					DatabaseContract.StockTrend.CONTENT_URI,
					DatabaseContract.StockTrend.PROJECTION_ALL, selection, null,
					sortOrder);
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

	public void getStockTrendList(String period, int level, String trend,
								  ArrayList<StockTrend> stockTrendList) {
		if (stockTrendList == null) {
			return;
		}

		String selection = DatabaseContract.COLUMN_PERIOD + " = '" + period + "'"
				+ " AND " + DatabaseContract.COLUMN_LEVEL + " = " + level
				+ " AND " + DatabaseContract.COLUMN_PERIOD + " = '" + period + "'";
		String sortOrder = DatabaseContract.COLUMN_PRICE + DatabaseContract.ORDER_ASC;

		stockTrendList.clear();
		Cursor cursor = null;
		try {
			cursor = queryStockTrend(selection, null, sortOrder);
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

	public void getStockTrendList(Stock stock, String period,
	                             ArrayList<StockTrend> stockTrendList, String sortOrder) {
		Cursor cursor = null;

		if ((stock == null) || (stockTrendList == null)) {
			return;
		}

		stockTrendList.clear();

		String selection = getStockTrendSelection(stock.getId(), period, Trend.LEVEL_NONE);

		try {
			cursor = queryStockTrend(selection, null, sortOrder);
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

	public void loadStockTrendList(Stock stock, String period,
	                              ArrayList<StockTrend> stockTrendList) {
		Calendar calendar = Calendar.getInstance();
		Cursor cursor = null;
		String selection = null;
		String sortOrder = null;

		if ((stock == null) || TextUtils.isEmpty(period)
				|| (stockTrendList == null)) {
			return;
		}

		try {
			stockTrendList.clear();

			selection = getStockTrendSelection(
					stock.getId(), period, Trend.LEVEL_NONE);
			sortOrder = getStockTrendOrder();
			cursor = queryStockTrend(selection, null,
					sortOrder);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockTrend stockTrend = new StockTrend(period);
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
		Cursor cursor = null;

		if (stockTrend == null) {
			return result;
		}

		try {
			cursor = queryStockTrend(stockTrend);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				stockTrend.setCreated(cursor);
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	public int updateStockTrend(StockTrend stockTrend, ContentValues contentValues) {
		int result = 0;

		if ((stockTrend == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getStockTrendSelection(stockTrend);

		result = mContentResolver.update(
				DatabaseContract.StockTrend.CONTENT_URI, contentValues, where,
				null);

		return result;
	}

	public void updateStockTrend(Stock stock, String period, ArrayList<StockTrend> stockTrendList) {
		if ((stockTrendList == null) || (stockTrendList.size() == 0)) {
			return;
		}

		try {
			deleteStockTrend(stock.getId(), period);

			ContentValues[] contentValues = new ContentValues[stockTrendList.size()];
			for (int i = 0; i < stockTrendList.size(); i++) {
				StockTrend stockTrend = stockTrendList.get(i);
				stockTrend.setModified(Utility.getCurrentDateTimeString());
				contentValues[i] = stockTrend.getContentValues();
			}

			bulkInsertStockTrend(contentValues);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int deleteStockTrend() {
		return delete(DatabaseContract.StockTrend.CONTENT_URI);
	}

	public int deleteStockTrend(StockTrend stockTrend) {
		int result = 0;

		if ((stockTrend == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getStockTrendSelection(stockTrend);

		result = mContentResolver.delete(
				DatabaseContract.StockTrend.CONTENT_URI, where, null);

		return result;
	}

	public int deleteStockTrend(long stockId) {
		int result = 0;
		String where = DatabaseContract.COLUMN_STOCK_ID + "=" + stockId;

		try {
			result = delete(DatabaseContract.StockTrend.CONTENT_URI, where);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public int deleteStockTrend(long stockId, String period) {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		String where = getStockTrendSelection(stockId, period);

		result = mContentResolver.delete(
				DatabaseContract.StockTrend.CONTENT_URI, where, null);

		return result;
	}

	public String getStockTrendSelection(StockTrend stockTrend) {
		String selection = "";
		String period = "";

		if (stockTrend == null) {
			return selection;
		}

		selection = DatabaseContract.COLUMN_STOCK_ID + " = "
				+ stockTrend.getStockId() + " AND "
				+ DatabaseContract.COLUMN_PERIOD + " = " + "'"
				+ stockTrend.getPeriod() + "'" + " AND "
				+ DatabaseContract.COLUMN_LEVEL + " = " +
				+ stockTrend.getLevel();

		return selection;
	}

	public String getStockTrendSelection(long stockId, String period) {
		return DatabaseContract.COLUMN_STOCK_ID + " = " + stockId + " AND "
				+ DatabaseContract.COLUMN_PERIOD + " = '" + period + "'";
	}

	public String getStockTrendSelection(long stockId, String period, int level) {
		return DatabaseContract.COLUMN_STOCK_ID + " = " + stockId
				+ " AND " + DatabaseContract.COLUMN_PERIOD + " = '" + period + "'"
				+ " AND " + DatabaseContract.COLUMN_LEVEL + " = '" + level + "'";
	}

	public String getStockTrendOrder() {
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

		if (contentValuesArray == null) {
			return result;
		}

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
		double cost = 0;

		Cursor cursor = null;
		StockDeal stockDeal = null;

		if ((stock == null) || (mContentResolver == null)) {
			return result;
		}

		stockDeal = new StockDeal();

		String selection = DatabaseContract.COLUMN_SE + " = " + "'"
				+ stock.getSE() + "'" + " AND " + DatabaseContract.COLUMN_CODE
				+ " = " + "'" + stock.getCode() + "'";
		String sortOrder = DatabaseContract.COLUMN_BUY + " ASC ";

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
						cost += stockDeal.getValue();
					}

					result += updateStockDealByID(stockDeal);
				}
			}

			stock.setHold(hold);
			if (hold > 0) {
				stock.setCost(Utility.Round(cost));
				stock.setProfit(Utility.Round(hold * stock.getNetProfitPerShareInYear()));
				stock.setValuation(hold * stock.getPrice());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	public int deleteStockDeal() {
		return delete(DatabaseContract.StockDeal.CONTENT_URI);
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

		String selection = DatabaseContract.COLUMN_SE + " = " + "'"
				+ stockDeal.getSE() + "'" + " AND "
				+ DatabaseContract.COLUMN_CODE + " = " + "'"
				+ stockDeal.getCode() + "'" + " AND "
				+ DatabaseContract.COLUMN_BUY + " = " + stockDeal.getBuy()
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

	public Cursor queryStockDeal(long id) {
		Cursor cursor = null;

		if (id == 0) {
			return cursor;
		}

		String selection = DatabaseContract.COLUMN_ID + "=" + id;
		cursor = queryStockDeal(selection, null, null);
		return cursor;
	}

	public void getStockDeal(StockDeal stockDeal) {
		Cursor cursor = null;

		if (stockDeal == null) {
			return;
		}

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
		Cursor cursor = null;

		if (stock == null) {
			return result;
		}

		try {
			String selection = DatabaseContract.COLUMN_STOCK_ID + "=" + stock.getId();
			selection = DatabaseContract.COLUMN_SE + " = " + "'" + stock.getSE() + "'"
					+ " AND " + DatabaseContract.COLUMN_CODE + " = " + "'"
					+ stock.getCode() + "'";
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
		Cursor cursor = null;

		if (stockDealList == null) {
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

	public Uri insertStockQuant(StockQuant StockQuant) {
		Uri uri = null;

		if ((StockQuant == null) || (mContentResolver == null)) {
			return uri;
		}

		uri = mContentResolver.insert(DatabaseContract.StockQuant.CONTENT_URI,
				StockQuant.getContentValues());

		return uri;
	}

	public int bulkInsertStockQuant(ContentValues[] contentValuesArray) {
		int result = 0;

		if (contentValuesArray == null) {
			return result;
		}

		if ((contentValuesArray.length == 0) || (mContentResolver == null)) {
			return result;
		}

		result = mContentResolver.bulkInsert(
				DatabaseContract.StockQuant.CONTENT_URI, contentValuesArray);

		return result;
	}

	public boolean isStockQuantExist(StockQuant StockQuant) {
		boolean result = false;
		Cursor cursor = null;

		if (StockQuant == null) {
			return result;
		}

		try {
			cursor = queryStockQuant(StockQuant);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				StockQuant.setCreated(cursor);
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	public int updateStockQuantById(StockQuant StockQuant) {
		int result = 0;

		if ((StockQuant == null) || (mContentResolver == null)) {
			return result;
		}

		String where = DatabaseContract.COLUMN_ID + "=" + StockQuant.getId();

		result = mContentResolver.update(
				DatabaseContract.StockQuant.CONTENT_URI,
				StockQuant.getContentValues(), where, null);

		return result;
	}

	public int deleteStockQuant() {
		return delete(DatabaseContract.StockQuant.CONTENT_URI);
	}

	public void deleteStockQuant(Stock stock) {
		if ((stock == null) || (mContentResolver == null)) {
			return;
		}

		String selection = DatabaseContract.COLUMN_SE + " = " + "'"
				+ stock.getSE() + "'" + " AND " + DatabaseContract.COLUMN_CODE
				+ " = " + "'" + stock.getCode() + "'";

		try {
			mContentResolver.delete(DatabaseContract.StockQuant.CONTENT_URI,
					selection, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteStockQuant(StockQuant StockQuant) {
		if ((StockQuant == null) || (mContentResolver == null)) {
			return;
		}

		String where = DatabaseContract.COLUMN_ID + "=" + StockQuant.getId();

		try {
			mContentResolver.delete(DatabaseContract.StockQuant.CONTENT_URI,
					where, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Cursor queryStockQuant(StockQuant StockQuant) {
		Cursor cursor = null;

		if (StockQuant == null) {
			return cursor;
		}

		String selection = DatabaseContract.COLUMN_SE + " = " + "'" + StockQuant.getSE() + "'"
				+ " AND " + DatabaseContract.COLUMN_CODE + " = " + "'" + StockQuant.getCode() + "'"
				+ " AND " + DatabaseContract.COLUMN_CREATED + " = " + "'" + StockQuant.getCreated() + "'"
				+ " AND " + DatabaseContract.COLUMN_MODIFIED + " = " + "'" + StockQuant.getModified() + "'";

		cursor = queryStockQuant(selection, null, null);

		return cursor;
	}

	public Cursor queryStockQuant(String selection, String[] selectionArgs,
								  String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(DatabaseContract.StockQuant.CONTENT_URI,
				DatabaseContract.StockQuant.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor queryStockQuantById(StockQuant StockQuant) {
		Cursor cursor = null;

		if (StockQuant == null) {
			return cursor;
		}

		String selection = DatabaseContract.COLUMN_ID + "=" + StockQuant.getId();

		cursor = queryStockQuant(selection, null, null);

		return cursor;
	}

	public void getStockQuantById(StockQuant StockQuant) {
		Cursor cursor = null;

		if (StockQuant == null) {
			return;
		}

		try {
			cursor = queryStockQuantById(StockQuant);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				StockQuant.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStockQuantList(Stock stock,
								  ArrayList<StockQuant> StockQuantList, String selection, String sortOrder) {
		Cursor cursor = null;

		if ((stock == null) || (StockQuantList == null)) {
			return;
		}

		StockQuantList.clear();

		try {
			cursor = queryStockQuant(selection, null, sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockQuant StockQuant = new StockQuant();
					StockQuant.set(cursor);
					StockQuantList.add(StockQuant);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStockQuantList(List<StockQuant> StockQuantList) {
		Cursor cursor = null;

		if (StockQuantList == null) {
			return;
		}

		StockQuantList.clear();

		try {
			cursor = queryStockQuant(null, null, null);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockQuant StockQuant = new StockQuant();
					StockQuant.set(cursor);
					StockQuantList.add(StockQuant);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStockQuant(Stock stock, StockQuant StockQuant, String selection, String sortOrder) {
		Cursor cursor = null;

		if ((stock == null) || (StockQuant == null)) {
			return;
		}

		try {
			cursor = queryStockQuant(selection, null, sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockQuant.set(cursor);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStockQuantList(Stock stock, ArrayList<StockQuant> StockQuantList) {
		String sortOrder = DatabaseContract.COLUMN_ID + DatabaseContract.ORDER_ASC;

		if (stock == null || StockQuantList == null) {
			return;
		}

		String selection = DatabaseContract.COLUMN_SE + " = " + "'" + stock.getSE()
				+ "'" + " AND " + DatabaseContract.COLUMN_CODE + " = " + "'"
				+ stock.getCode() + "'";

		getStockQuantList(stock, StockQuantList, selection, sortOrder);
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

		if (contentValuesArray == null) {
			return result;
		}

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

	public void updateStockFinancial(Stock stock, ArrayList<StockFinancial> stockFinancialList) {
		if ((stockFinancialList == null) || (stockFinancialList.size() == 0)) {
			return;
		}

		try {
			deleteStockFinancial(stock.getId());

			ContentValues[] contentValues = new ContentValues[stockFinancialList.size()];
			for (int i = 0; i < stockFinancialList.size(); i++) {
				StockFinancial stockFinancial = stockFinancialList.get(i);
				stockFinancial.setModified(Utility.getCurrentDateTimeString());
				contentValues[i] = stockFinancial.getContentValues();
			}

			bulkInsertStockFinancial(contentValues);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int deleteStockFinancial() {
		return delete(DatabaseContract.StockFinancial.CONTENT_URI);
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

	public int deleteStockFinancial(long stockId) {
		int result = 0;
		String where = DatabaseContract.COLUMN_STOCK_ID + "=" + stockId;

		try {
			result = delete(DatabaseContract.StockFinancial.CONTENT_URI, where);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
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

		if (contentValuesArray == null) {
			return result;
		}

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
		return delete(DatabaseContract.ShareBonus.CONTENT_URI);
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

	public int deleteShareBonus(long stockId) {
		int result = 0;
		String where = DatabaseContract.COLUMN_STOCK_ID + "=" + stockId;

		try {
			result = delete(DatabaseContract.ShareBonus.CONTENT_URI, where);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
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

		if (contentValuesArray == null) {
			return result;
		}

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
		return delete(DatabaseContract.TotalShare.CONTENT_URI);
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

	public int deleteTotalShare(long stockId) {
		int result = 0;
		String where = DatabaseContract.COLUMN_STOCK_ID + "=" + stockId;

		try {
			result = delete(DatabaseContract.TotalShare.CONTENT_URI, where);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
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

		if (contentValuesArray == null) {
			return result;
		}

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
		return delete(DatabaseContract.IndexComponent.CONTENT_URI);
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
		if (indexComponent == null) {
			return "";
		}
		return getIndexComponentSelection(indexComponent.getIndexCode(), indexComponent.getCode());
	}

	public String getIndexComponentSelection(String indexCode, String stockCode) {
		return DatabaseContract.COLUMN_INDEX_CODE + " = " + "'" + indexCode + "'"
				+ " AND "
				+ DatabaseContract.COLUMN_CODE + " = " + "'" + stockCode + "'";
	}

	public String getIndexComponentOrder() {
		return DatabaseContract.IndexComponent.SORT_ORDER_DEFAULT;
	}

	@Override
	public void onAddFavorite(Stock stock) {
		if (stock == null) {
			return;
		}
		updateStockFlag(stock);
	}

	@Override
	public void onRemoveFavorite(Stock stock) {
		if (stock == null) {
			return;
		}
		updateStockFlag(stock);
	}

	@Override
	public void onAddNotify(Stock stock) {
		if (stock == null) {
			return;
		}
		updateStockFlag(stock);
	}

	@Override
	public void onRemoveNotify(Stock stock) {
		if (stock == null) {
			return;
		}
		updateStockFlag(stock);
	}

	@Override
	public void onAddStock(Stock stock) {
		if (stock == null) {
			return;
		}
	}

	@Override
	public void onRemoveStock(Stock stock) {
		if (stock == null) {
			return;
		}
	}
}
