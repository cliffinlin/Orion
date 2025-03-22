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
import android.util.Log;

import com.android.orion.config.Config;
import com.android.orion.data.Period;
import com.android.orion.data.Trend;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.DatabaseOpenHelper;
import com.android.orion.database.IndexComponent;
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
import com.android.orion.setting.Setting;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatabaseManager implements StockListener {
	public static String TAG = Config.TAG + DatabaseManager.class.getSimpleName();
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

	public void beginTransaction() {
		if (mDatabase != null) {
			mDatabase.beginTransaction();
		}
	}

	public void setTransactionSuccessful() {
		if (mDatabase != null) {
			mDatabase.setTransactionSuccessful();
		}
	}

	public void endTransaction() {
		if (mDatabase != null) {
			mDatabase.endTransaction();
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

	public String hasSelection(String key, int value) {
		return " (" + key + " & " + value + ") = " + value;
	}

	public String hasFlagSelection(int flag) {
		return hasSelection(DatabaseContract.COLUMN_FLAG, flag);
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

		String selection = getStockSelection(stock);

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
		String selection = hasFlagSelection(Stock.FLAG_FAVORITE);
		getStockList(selection, stockList);
	}

	public void loadStockArrayMap(ArrayMap<String, Stock> stockArrayMap) {
		if (stockArrayMap == null) {
			return;
		}

		String selection = hasFlagSelection(Stock.FLAG_FAVORITE);
		Cursor cursor = null;
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

		String where = getStockSelection(stock);

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

	public String getStockSelection(Stock stock) {
		if (stock == null) {
			return null;
		}
		return DatabaseContract.COLUMN_SE + " = " + "'" + stock.getSE() + "'"
				+ " AND " + DatabaseContract.COLUMN_CODE + " = " + "'" + stock.getCode() + "'";
	}

	public String getStockSelection(String se, String code) {
		return DatabaseContract.COLUMN_SE + " = " + "'" + se + "'"
				+ " AND " + DatabaseContract.COLUMN_CODE + " = " + "'" + code + "'";
	}

	public String getStockClassASelection() {
		return DatabaseContract.COLUMN_CLASSES + " = " + "'" + Stock.CLASS_A + "'";
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
			String selection = getStockDataSelection(stockData.getSE(), stockData.getCode(), stockData.getPeriod());
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

		String selection = getStockDataSelection(stock.getSE(), stock.getCode(), period, Trend.LEVEL_NONE);

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

			selection = getStockDataSelection(stock.getSE(), stock.getCode(), period, Trend.LEVEL_NONE);
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
			deleteStockData(stock.getSE(), stock.getCode(), period);

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

	public int deleteStockData(Stock stock) {
		int result = 0;
		String where = getStockSelection(stock);

		try {
			result = delete(DatabaseContract.StockData.CONTENT_URI, where);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public int deleteStockData(String se, String code, String period) {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		String where = getStockDataSelection(se, code, period);

		result = mContentResolver.delete(
				DatabaseContract.StockData.CONTENT_URI, where, null);

		return result;
	}

	public String getStockSelection(StockData stockData) {
		if (stockData == null) {
			return null;
		}
		return DatabaseContract.COLUMN_SE + " = " + "'" + stockData.getSE() + "'"
				+ " AND " + DatabaseContract.COLUMN_CODE + " = " + "'" + stockData.getCode() + "'";
	}

	public String getStockDataSelection(StockData stockData) {
		String selection = "";
		String period = "";

		if (stockData == null) {
			return selection;
		}

		selection = getStockSelection(stockData) + " AND "
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

	public String getStockDataSelection(String se, String code, String period) {
		return getStockSelection(se, code)
				+ " AND " + DatabaseContract.COLUMN_PERIOD + " = '" + period + "'";
	}

	public String getStockDataSelection(String se, String code, String period, int level) {
		return getStockSelection(se, code)
				+ " AND " + DatabaseContract.COLUMN_PERIOD + " = '" + period + "'"
				+ " AND " + DatabaseContract.COLUMN_LEVEL + " = " + level;
	}

	public String getStockDataOrder() {
		return DatabaseContract.COLUMN_DATE + " ASC " + ","
				+ DatabaseContract.COLUMN_TIME + " ASC ";
	}

	public Uri insertTDXData(TDXData tdxData) {
		Uri uri = null;

		if ((tdxData == null) || (mContentResolver == null)) {
			return uri;
		}

		uri = mContentResolver.insert(DatabaseContract.TDXData.CONTENT_URI,
				tdxData.getContentValues());

		return uri;
	}

	public int bulkInsertTDXData(ContentValues[] contentValuesArray) {
		int result = 0;

		if (contentValuesArray == null) {
			return result;
		}

		if ((contentValuesArray.length == 0) || (mContentResolver == null)) {
			return result;
		}

		result = mContentResolver.bulkInsert(
				DatabaseContract.TDXData.CONTENT_URI, contentValuesArray);

		return result;
	}

	public Cursor queryTDXData(String[] projection, String selection,
	                           String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(DatabaseContract.TDXData.CONTENT_URI,
				projection, selection, selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor queryTDXData(String selection, String[] selectionArgs,
	                           String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(DatabaseContract.TDXData.CONTENT_URI,
				DatabaseContract.TDXData.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor queryTDXData(TDXData tdxData) {
		Cursor cursor = null;

		if ((tdxData == null) || (mContentResolver == null)) {
			return cursor;
		}

		String selection = getTDXDataSelection(tdxData);
		String sortOrder = getTDXDataOrder();

		cursor = mContentResolver.query(DatabaseContract.TDXData.CONTENT_URI,
				DatabaseContract.TDXData.PROJECTION_ALL, selection, null,
				sortOrder);

		return cursor;
	}

	public void getTDXData(TDXData tdxData) {
		Cursor cursor = null;

		if ((tdxData == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getTDXDataSelection(tdxData.getSE(), tdxData.getCode(), tdxData.getPeriod());
			String sortOrder = DatabaseContract.COLUMN_DATE + " DESC " + ","
					+ DatabaseContract.COLUMN_TIME + " DESC ";

			cursor = mContentResolver.query(
					DatabaseContract.TDXData.CONTENT_URI,
					DatabaseContract.TDXData.PROJECTION_ALL, selection, null,
					sortOrder);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				tdxData.set(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getTDXDataContentList(Stock stock, String period, ArrayList<String> contentList) {
		Cursor cursor = null;

		if ((stock == null) || (contentList == null)) {
			return;
		}

		contentList.clear();

		String selection = getTDXDataSelection(stock.getSE(), stock.getCode(), period);

		try {
			cursor = queryTDXData(selection, null, null);
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

	public void loadTDXDataList(Stock stock, String period, ArrayList<TDXData> tdxDataList) {
		Calendar calendar = Calendar.getInstance();
		Cursor cursor = null;
		String selection = null;
		String sortOrder = null;

		if ((stock == null) || TextUtils.isEmpty(period)
				|| (tdxDataList == null)) {
			return;
		}

		try {
			tdxDataList.clear();

			selection = getTDXDataSelection(stock.getSE(), stock.getCode(), period);
			sortOrder = getTDXDataOrder();
			cursor = queryTDXData(selection, null,
					sortOrder);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					TDXData tdxData = new TDXData(period);
					tdxData.set(cursor);
					tdxDataList.add(tdxData);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public boolean isTDXDataExist(TDXData tdxData) {
		boolean result = false;
		Cursor cursor = null;

		if (tdxData == null) {
			return result;
		}

		try {
			cursor = queryTDXData(tdxData);
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

	public int updateTDXData(TDXData tdxData, ContentValues contentValues) {
		int result = 0;

		if ((tdxData == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getTDXDataSelection(tdxData);

		result = mContentResolver.update(
				DatabaseContract.TDXData.CONTENT_URI, contentValues, where,
				null);

		return result;
	}

	public void updateTDXData(Stock stock, String period, ArrayList<TDXData> tdxDataList) {
		if ((tdxDataList == null) || (tdxDataList.size() == 0)) {
			return;
		}

		try {
			deleteTDXData(stock.getSE(), stock.getCode(), period);

			ContentValues[] contentValues = new ContentValues[tdxDataList.size()];
			for (int i = 0; i < tdxDataList.size(); i++) {
				TDXData tdxData = tdxDataList.get(i);
				tdxData.setModified(Utility.getCurrentDateTimeString());
				contentValues[i] = tdxData.getContentValues();
			}

			bulkInsertTDXData(contentValues);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int deleteTDXData() {
		return delete(DatabaseContract.TDXData.CONTENT_URI);
	}

	public int deleteTDXData(TDXData tdxData) {
		int result = 0;

		if ((tdxData == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getTDXDataSelection(tdxData);

		result = mContentResolver.delete(
				DatabaseContract.TDXData.CONTENT_URI, where, null);

		return result;
	}

	public int deleteTDXData(Stock stock) {
		int result = 0;
		String where = getStockSelection(stock);

		try {
			result = delete(DatabaseContract.TDXData.CONTENT_URI, where);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public int deleteTDXData(String se, String code, String period) {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		String where = getTDXDataSelection(se, code, period);

		result = mContentResolver.delete(
				DatabaseContract.TDXData.CONTENT_URI, where, null);

		return result;
	}

	public String getStockSelection(TDXData tdxData) {
		if (tdxData == null) {
			return null;
		}
		return DatabaseContract.COLUMN_SE + " = " + "'" + tdxData.getSE() + "'"
				+ " AND " + DatabaseContract.COLUMN_CODE + " = " + "'" + tdxData.getCode() + "'";
	}

	public String getTDXDataSelection(TDXData tdxData) {
		String selection = "";
		String period = "";

		if (tdxData == null) {
			return selection;
		}

		selection = getStockSelection(tdxData) + " AND "
				+ DatabaseContract.COLUMN_PERIOD + " = " + "'"
				+ tdxData.getPeriod();

		period = tdxData.getPeriod();

		return selection;
	}

	public String getTDXDataSelection(String se, String code, String period) {
		return getStockSelection(se, code)
				+ " AND " + DatabaseContract.COLUMN_PERIOD + " = '" + period + "'";
	}

	public String getTDXDataOrder() {
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
		Cursor cursor = null;
		String selection = null;

		if (stockTrend == null) {
			return;
		}

		selection = DatabaseContract.COLUMN_ID + "=" + stockTrend.getId();

		try {
			cursor = queryStockTrend(selection, null, null);
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

		String selection = getStockSelection(stock);
		String sortOrder = DatabaseContract.COLUMN_PERIOD + DatabaseContract.ORDER_ASC;

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

	public void getStockTrendList(Stock stock, String period, ArrayList<StockTrend> stockTrendList) {
		if (stockTrendList == null) {
			return;
		}

		String selection = getStockSelection(stock)
				+ " AND " + DatabaseContract.COLUMN_PERIOD + " = '" + period + "'";
		String sortOrder = DatabaseContract.COLUMN_FLAG + DatabaseContract.ORDER_ASC;

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

	public void getStockTrendList(String period, int level, String type,
	                              ArrayList<StockTrend> stockTrendList) {
		if (stockTrendList == null) {
			return;
		}

		String selection = DatabaseContract.COLUMN_PERIOD + " = '" + period + "'"
				+ " AND " + DatabaseContract.COLUMN_LEVEL + " = " + level
				+ " AND " + DatabaseContract.COLUMN_TYPE + " = '" + type + "'";
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

	public boolean isStockTrendExist(StockTrend stockTrend) {
		boolean result = false;
		Cursor cursor = null;

		if (stockTrend == null) {
			return result;
		}

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

	public int deleteStockTrend(Stock stock) {
		int result = 0;
		String where = getStockSelection(stock);

		try {
			result = delete(DatabaseContract.StockTrend.CONTENT_URI, where);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public String getStockSelection(StockTrend stockTrend) {
		if (stockTrend == null) {
			return null;
		}
		return DatabaseContract.COLUMN_SE + " = " + "'" + stockTrend.getSE() + "'"
				+ " AND " + DatabaseContract.COLUMN_CODE + " = " + "'" + stockTrend.getCode() + "'";
	}

	public String getStockTrendSelection(StockTrend stockTrend) {
		String selection = "";

		if (stockTrend == null) {
			return selection;
		}

		selection = getStockSelection(stockTrend)
				+ " AND " + DatabaseContract.COLUMN_PERIOD + " = " + "'" + stockTrend.getPeriod() + "'"
				+ " AND " + DatabaseContract.COLUMN_LEVEL + " = " + stockTrend.getLevel();
		return selection;
	}

	public String getStockTrendSelection(String se, String code, String period) {
		String selection = getStockSelection(se, code)
				+ " AND " + DatabaseContract.COLUMN_PERIOD + " = " + "'" + period + "'";
		return selection;
	}

	public String getStockTrendOrder() {
		return DatabaseContract.COLUMN_DATE + " ASC " + ","
				+ DatabaseContract.COLUMN_TIME + " ASC ";
	}

	public Uri insertStockPerceptron(StockPerceptron stockPerceptron) {
		Uri uri = null;

		if ((stockPerceptron == null) || (mContentResolver == null)) {
			return uri;
		}

		uri = mContentResolver.insert(DatabaseContract.StockPerceptron.CONTENT_URI,
				stockPerceptron.getContentValues());

		return uri;
	}

	public int bulkInsertStockPerceptron(ContentValues[] contentValuesArray) {
		int result = 0;

		if (contentValuesArray == null) {
			return result;
		}

		if ((contentValuesArray.length == 0) || (mContentResolver == null)) {
			return result;
		}

		result = mContentResolver.bulkInsert(
				DatabaseContract.StockPerceptron.CONTENT_URI, contentValuesArray);

		return result;
	}

	public Cursor queryStockPerceptron(String[] projection, String selection,
	                                   String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(DatabaseContract.StockPerceptron.CONTENT_URI,
				projection, selection, selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor queryStockPerceptron(String selection, String[] selectionArgs,
	                                   String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(DatabaseContract.StockPerceptron.CONTENT_URI,
				DatabaseContract.StockPerceptron.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor queryStockPerceptron(StockPerceptron stockPerceptron) {
		Cursor cursor = null;

		if ((stockPerceptron == null) || (mContentResolver == null)) {
			return cursor;
		}

		String selection = getStockPerceptronSelection(stockPerceptron);
		String sortOrder = getStockPerceptronOrder();

		cursor = mContentResolver.query(DatabaseContract.StockPerceptron.CONTENT_URI,
				DatabaseContract.StockPerceptron.PROJECTION_ALL, selection, null,
				sortOrder);

		return cursor;
	}

	public void getStockPerceptron(StockPerceptron stockPerceptron) {
		Cursor cursor = null;

		if ((stockPerceptron == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getStockPerceptronSelection(stockPerceptron);
			String sortOrder = DatabaseContract.COLUMN_PERIOD + " DESC " + ","
					+ DatabaseContract.COLUMN_LEVEL + " DESC ";

			cursor = mContentResolver.query(
					DatabaseContract.StockPerceptron.CONTENT_URI,
					DatabaseContract.StockPerceptron.PROJECTION_ALL, selection, null,
					sortOrder);
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
		Cursor cursor = null;
		String selection = null;

		if (stockPerceptron == null) {
			return;
		}

		selection = DatabaseContract.COLUMN_ID + "=" + stockPerceptron.getId();

		try {
			cursor = queryStockPerceptron(selection, null, null);
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

	public void getStockPerceptronList(String period, int level, String type,
	                                   ArrayList<StockPerceptron> stockPerceptronList) {
		if (stockPerceptronList == null) {
			return;
		}

		String selection = getStockPerceptronSelection(period, level, type);
		String sortOrder = DatabaseContract.COLUMN_PERIOD + DatabaseContract.ORDER_ASC;

		stockPerceptronList.clear();
		Cursor cursor = null;
		try {
			cursor = queryStockPerceptron(selection, null, sortOrder);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockPerceptron stockPerceptron = new StockPerceptron();
					stockPerceptron.set(cursor);
					stockPerceptronList.add(stockPerceptron);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public void getStockPerceptronList(ArrayList<StockPerceptron> stockPerceptronList, String sortOrder) {
		Cursor cursor = null;

		if (stockPerceptronList == null) {
			return;
		}

		stockPerceptronList.clear();

		try {
			cursor = queryStockPerceptron(null, null, sortOrder);
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockPerceptron stockPerceptron = new StockPerceptron();
					stockPerceptron.set(cursor);
					stockPerceptronList.add(stockPerceptron);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}

	public boolean isStockPerceptronExist(StockPerceptron stockPerceptron) {
		boolean result = false;
		Cursor cursor = null;

		if (stockPerceptron == null) {
			return result;
		}

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

	public int updateStockPerceptron(StockPerceptron stockPerceptron, ContentValues contentValues) {
		int result = 0;

		if ((stockPerceptron == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getStockPerceptronSelection(stockPerceptron);

		result = mContentResolver.update(
				DatabaseContract.StockPerceptron.CONTENT_URI, contentValues, where,
				null);

		return result;
	}

	public int deleteStockPerceptron() {
		return delete(DatabaseContract.StockPerceptron.CONTENT_URI);
	}

	public int deleteStockPerceptron(StockPerceptron stockPerceptron) {
		int result = 0;

		if ((stockPerceptron == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getStockPerceptronSelection(stockPerceptron);

		result = mContentResolver.delete(
				DatabaseContract.StockPerceptron.CONTENT_URI, where, null);

		return result;
	}

	public int deleteStockPerceptron(long id) {
		int result = 0;
		String where = DatabaseContract.COLUMN_ID + "=" + id;

		try {
			result = delete(DatabaseContract.StockPerceptron.CONTENT_URI, where);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public int deleteStockPerceptron(String period, int level, String type) {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		String where = getStockPerceptronSelection(period, level, type);

		result = mContentResolver.delete(
				DatabaseContract.StockPerceptron.CONTENT_URI, where, null);

		return result;
	}

	public String getStockPerceptronSelection(StockPerceptron stockPerceptron) {
		String selection = "";
		if (stockPerceptron == null) {
			return selection;
		}
		selection = getStockPerceptronSelection(stockPerceptron.getPeriod(), stockPerceptron.getLevel(), stockPerceptron.getType());
		return selection;
	}

	public String getStockPerceptronSelection(String period, int level, String type) {
		return DatabaseContract.COLUMN_PERIOD + " = " + "'" + period + "'"
				+ " AND " + DatabaseContract.COLUMN_LEVEL + " = " + level
				+ " AND " + DatabaseContract.COLUMN_TYPE + " = " + "'" + type + "'";
	}

	public String getStockPerceptronOrder() {
		return DatabaseContract.COLUMN_PERIOD + " ASC " + ","
				+ DatabaseContract.COLUMN_LEVEL + " ASC ";
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

		String selection = getStockSelection(stock);
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

		if (id == DatabaseContract.INVALID_ID) {
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
			String selection = getStockSelection(stock);
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

	public void getStockDeal(Stock stock, StockDeal stockDeal) {
		Cursor cursor = null;

		if ((stock == null) || (stockDeal == null)) {
			return;
		}

		try {
			String selection = getStockSelection(stock);
			String sortOrder = DatabaseContract.COLUMN_PROFIT + DatabaseContract.ORDER_DESC;
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
			String selection = getStockSelection(stock);
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

		String selection = getStockSelection(stock);

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
			deleteStockFinancial(stock);

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

	public int deleteStockFinancial(Stock stock) {
		int result = 0;
		String where = getStockSelection(stock);

		try {
			result = delete(DatabaseContract.StockFinancial.CONTENT_URI, where);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public int deleteStockFinancial(Stock stock, String date) {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		String where = getStockFinancialSelection(stock, date);

		result = mContentResolver.delete(
				DatabaseContract.StockFinancial.CONTENT_URI, where, null);

		return result;
	}

	public String getStockSelection(StockFinancial stockFinancial) {
		if (stockFinancial == null) {
			return null;
		}
		return DatabaseContract.COLUMN_SE + " = " + "'" + stockFinancial.getSE() + "'"
				+ " AND " + DatabaseContract.COLUMN_CODE + " = " + "'" + stockFinancial.getCode() + "'";
	}

	public String getStockFinancialSelection(StockFinancial stockFinancial) {
		String selection = "";

		if (stockFinancial == null) {
			return selection;
		}

		selection = getStockFinancialSelection(stockFinancial, stockFinancial.getDate());

		return selection;
	}

	public String getStockFinancialSelection(StockFinancial stockFinancial, String date) {
		return getStockSelection(stockFinancial)
				+ " AND " + DatabaseContract.COLUMN_DATE + " = '" + date + "'";
	}

	public String getStockFinancialSelection(Stock stock, String date) {
		return getStockSelection(stock)
				+ " AND " + DatabaseContract.COLUMN_DATE + " = '" + date + "'";
	}

	public String getStockFinancialOrder() {
		return DatabaseContract.COLUMN_DATE + " ASC ";
	}

	public Uri insertStockBonus(StockBonus stockBonus) {
		Uri uri = null;

		if ((stockBonus == null) || (mContentResolver == null)) {
			return uri;
		}

		uri = mContentResolver.insert(DatabaseContract.StockBonus.CONTENT_URI,
				stockBonus.getContentValues());

		return uri;
	}

	public int bulkInsertStockBonus(ContentValues[] contentValuesArray) {
		int result = 0;

		if (contentValuesArray == null) {
			return result;
		}

		if ((contentValuesArray.length == 0) || (mContentResolver == null)) {
			return result;
		}

		result = mContentResolver.bulkInsert(
				DatabaseContract.StockBonus.CONTENT_URI, contentValuesArray);

		return result;
	}

	public Cursor queryStockBonus(String selection, String[] selectionArgs,
	                              String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(
				DatabaseContract.StockBonus.CONTENT_URI,
				DatabaseContract.StockBonus.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor queryStockBonus(StockBonus stockBonus) {
		Cursor cursor = null;

		if ((stockBonus == null) || (mContentResolver == null)) {
			return cursor;
		}

		String selection = getStockBonusSelection(stockBonus);
		String sortOrder = getStockBonusOrder();

		cursor = mContentResolver.query(
				DatabaseContract.StockBonus.CONTENT_URI,
				DatabaseContract.StockBonus.PROJECTION_ALL, selection, null,
				sortOrder);

		return cursor;
	}

	public void getStockBonus(StockBonus stockBonus) {
		Cursor cursor = null;

		if ((stockBonus == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getStockBonusSelection(stockBonus);
			String sortOrder = getStockBonusOrder();

			cursor = mContentResolver.query(
					DatabaseContract.StockBonus.CONTENT_URI,
					DatabaseContract.StockBonus.PROJECTION_ALL, selection,
					null, sortOrder);
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

	public void getStockBonus(Stock stock, StockBonus stockBonus) {
		Cursor cursor = null;

		if ((stockBonus == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getStockSelection(stock);
			String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";

			cursor = mContentResolver.query(
					DatabaseContract.StockBonus.CONTENT_URI,
					DatabaseContract.StockBonus.PROJECTION_ALL, selection,
					null, sortOrder);
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

	public void getStockBonusList(Stock stock,
	                              ArrayList<StockBonus> stockBonusList, String sortOrder) {
		Cursor cursor = null;

		if ((stock == null) || (stockBonusList == null)) {
			return;
		}

		stockBonusList.clear();
		String selection = getStockSelection(stock);

		try {
			cursor = queryStockBonus(selection, null, sortOrder);
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
		Cursor cursor = null;

		if (stockBonus == null) {
			return result;
		}

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

	public int updateStockBonus(StockBonus stockBonus,
	                            ContentValues contentValues) {
		int result = 0;

		if ((stockBonus == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getStockBonusSelection(stockBonus);

		result = mContentResolver.update(
				DatabaseContract.StockBonus.CONTENT_URI, contentValues, where,
				null);

		return result;
	}

	public int deleteStockBonus() {
		return delete(DatabaseContract.StockBonus.CONTENT_URI);
	}

	public int deleteStockBonus(StockBonus stockBonus) {
		int result = 0;

		if ((stockBonus == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getStockBonusSelection(stockBonus);

		result = mContentResolver.delete(
				DatabaseContract.StockBonus.CONTENT_URI, where, null);

		return result;
	}

	public int deleteStockBonus(Stock stock) {
		int result = 0;
		String where = getStockSelection(stock);

		try {
			result = delete(DatabaseContract.StockBonus.CONTENT_URI, where);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public int deleteStockBonus(StockBonus stockBonus, String date) {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		String where = getStockBonusSelection(stockBonus, date);

		result = mContentResolver.delete(
				DatabaseContract.StockBonus.CONTENT_URI, where, null);

		return result;
	}

	public String getStockBonusSelection(StockBonus stockBonus) {
		String selection = "";

		if (stockBonus == null) {
			return selection;
		}

		selection = getStockBonusSelection(stockBonus, stockBonus.getDate());

		return selection;
	}

	public String getStockSelection(StockBonus stockBonus) {
		if (stockBonus == null) {
			return null;
		}
		return DatabaseContract.COLUMN_SE + " = " + "'" + stockBonus.getSE() + "'"
				+ " AND " + DatabaseContract.COLUMN_CODE + " = " + "'" + stockBonus.getCode() + "'";
	}

	public String getStockBonusSelection(StockBonus stockBonus, String date) {
		return getStockSelection(stockBonus)
				+ " AND " + DatabaseContract.COLUMN_DATE + " = '" + date + "'";
	}

	public String getStockBonusOrder() {
		return DatabaseContract.COLUMN_DATE + " ASC ";
	}

	public Uri insertStockShare(StockShare stockShare) {
		Uri uri = null;

		if ((stockShare == null) || (mContentResolver == null)) {
			return uri;
		}

		uri = mContentResolver.insert(DatabaseContract.StockShare.CONTENT_URI,
				stockShare.getContentValues());

		return uri;
	}

	public int bulkInsertStockShare(ContentValues[] contentValuesArray) {
		int result = 0;

		if (contentValuesArray == null) {
			return result;
		}

		if ((contentValuesArray.length == 0) || (mContentResolver == null)) {
			return result;
		}

		result = mContentResolver.bulkInsert(
				DatabaseContract.StockShare.CONTENT_URI, contentValuesArray);

		return result;
	}

	public Cursor queryStockShare(String selection, String[] selectionArgs,
	                              String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(
				DatabaseContract.StockShare.CONTENT_URI,
				DatabaseContract.StockShare.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);

		return cursor;
	}

	public Cursor queryStockShare(StockShare stockShare) {
		Cursor cursor = null;

		if ((stockShare == null) || (mContentResolver == null)) {
			return cursor;
		}

		String selection = getStockShareSelection(stockShare);
		String sortOrder = getStockShareOrder();

		cursor = mContentResolver.query(
				DatabaseContract.StockShare.CONTENT_URI,
				DatabaseContract.StockShare.PROJECTION_ALL, selection, null,
				sortOrder);

		return cursor;
	}

	public void getStockShare(StockShare stockShare) {
		Cursor cursor = null;

		if ((stockShare == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getStockShareSelection(stockShare);
			String sortOrder = getStockShareOrder();

			cursor = mContentResolver.query(
					DatabaseContract.StockShare.CONTENT_URI,
					DatabaseContract.StockShare.PROJECTION_ALL, selection,
					null, sortOrder);
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

	public void getStockShare(Stock stock, StockShare stockShare) {
		Cursor cursor = null;

		if ((stockShare == null) || (mContentResolver == null)) {
			return;
		}

		try {
			String selection = getStockSelection(stock);
			String sortOrder = DatabaseContract.COLUMN_DATE + " DESC ";

			cursor = mContentResolver.query(
					DatabaseContract.StockShare.CONTENT_URI,
					DatabaseContract.StockShare.PROJECTION_ALL, selection,
					null, sortOrder);
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

	public void getStockShareList(Stock stock,
	                              ArrayList<StockShare> stockShareList, String sortOrder) {
		Cursor cursor = null;

		if ((stock == null) || (stockShareList == null)) {
			return;
		}

		stockShareList.clear();

		String selection = getStockSelection(stock);

		try {
			cursor = queryStockShare(selection, null, sortOrder);
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
		Cursor cursor = null;

		if (stockShare == null) {
			return result;
		}

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

	public int updateStockShare(StockShare stockShare,
	                            ContentValues contentValues) {
		int result = 0;

		if ((stockShare == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getStockShareSelection(stockShare);

		result = mContentResolver.update(
				DatabaseContract.StockShare.CONTENT_URI, contentValues, where,
				null);

		return result;
	}

	public int deleteStockShare() {
		return delete(DatabaseContract.StockShare.CONTENT_URI);
	}

	public int deleteStockShare(StockShare stockShare) {
		int result = 0;

		if ((stockShare == null) || (mContentResolver == null)) {
			return result;
		}

		String where = getStockShareSelection(stockShare);

		result = mContentResolver.delete(
				DatabaseContract.StockShare.CONTENT_URI, where, null);

		return result;
	}

	public int deleteStockShare(Stock stock) {
		int result = 0;
		String where = getStockSelection(stock);

		try {
			result = delete(DatabaseContract.StockShare.CONTENT_URI, where);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public int deleteStockShare(StockShare stockShare, String date) {
		int result = 0;

		if (mContentResolver == null) {
			return result;
		}

		String where = getStockShareSelection(stockShare, date);

		result = mContentResolver.delete(
				DatabaseContract.StockShare.CONTENT_URI, where, null);

		return result;
	}

	public String getStockShareSelection(StockShare stockShare) {
		String selection = "";

		if (stockShare == null) {
			return selection;
		}

		selection = getStockShareSelection(stockShare, stockShare.getDate());

		return selection;
	}

	public String getStockSelection(StockShare stockShare) {
		if (stockShare == null) {
			return null;
		}
		return DatabaseContract.COLUMN_SE + " = " + "'" + stockShare.getSE() + "'"
				+ " AND " + DatabaseContract.COLUMN_CODE + " = " + "'" + stockShare.getCode() + "'";
	}

	public String getStockShareSelection(StockShare stockShare, String date) {
		return getStockSelection(stockShare)
				+ " AND " + DatabaseContract.COLUMN_DATE + " = '" + date + "'";
	}

	public String getStockShareOrder() {
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
