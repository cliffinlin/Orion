package com.android.orion.manager;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.android.orion.config.Config;
import com.android.orion.data.Period;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.DatabaseOpenHelper;
import com.android.orion.database.Stock;

public class DatabaseManager {
	public static String TAG = Config.TAG + DatabaseManager.class.getSimpleName();
	private static Context mContext;

	public ContentResolver mContentResolver;
	public SQLiteDatabase mDatabase = null;
	public DatabaseOpenHelper mDatabaseHelper;

	public DatabaseManager(Context context) {
		mContext = context; //ContentProvider getContext()
		mContentResolver = mContext.getContentResolver();
		mDatabaseHelper = new DatabaseOpenHelper(mContext);
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
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
	}

	public Uri insert(Uri uri, ContentValues values) {
		if (mContentResolver == null || uri == null || values == null || values.size() == 0) {
			return null;
		}
		return mContentResolver.insert(uri, values);
	}

	public int bulkInsert(Uri uri, ContentValues[] contentValuesArray) {
		if (mContentResolver == null || uri == null || contentValuesArray == null || contentValuesArray.length == 0) {
			return 0;
		}
		return mContentResolver.bulkInsert(uri, contentValuesArray);
	}

	public int delete(Uri uri, String where, String[] selectionArgs) {
		int result = 0;
		if (mContentResolver == null || uri == null) {
			return 0;
		}
		try {
			result = mContentResolver.delete(uri, where, selectionArgs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public int delete(Uri uri) {
		return delete(uri, null, null);
	}

	public int update(Uri uri, ContentValues contentValues, String where, String[] selectionArgs) {
		if (mContentResolver == null || uri == null || contentValues == null || contentValues.size() == 0) {
			return 0;
		}
		return mContentResolver.update(uri, contentValues, where, selectionArgs);
	}

	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		if (mContentResolver == null || uri == null || projection == null || projection.length == 0) {
			return null;
		}
		return mContentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
	}

	public String getClassASelection() {
		return DatabaseContract.COLUMN_CLASSES + " = " + "'" + Stock.CLASS_A + "'";
	}

	public String getDateSelection(String date) {
		return DatabaseContract.COLUMN_DATE + " = " + "'" + date + "'";
	}

	public String getFlagSelection(String key, int value) {
		return " (" + key + " & " + value + ") = " + value;
	}

	public String getFlagSelection(int flag) {
		return getFlagSelection(DatabaseContract.COLUMN_FLAG, flag);
	}

	public String getHoldSelection() {
		return DatabaseContract.COLUMN_HOLD + " > " +  0;
	}

	public String getIDSelection(long id) {
		return DatabaseContract.COLUMN_ID + "=" + id;
	}

	public String getLevelSelection(int level) {
		return DatabaseContract.COLUMN_LEVEL + " = " + level;
	}

	public String getPeriodSelection(String period) {
		return DatabaseContract.COLUMN_PERIOD + " = " + "'" + period + "'";
	}

	public String getTimeSelection(String time) {
		return DatabaseContract.COLUMN_TIME + " = " + "'" + time + "'";
	}

	public String getTypeSelection(String type) {
		return DatabaseContract.COLUMN_TYPE + " = " + "'" + type + "'";
	}

	public String getStockSelection(String se, String code) {
		return DatabaseContract.COLUMN_SE + " = " + "'" + se + "'"
				+ " AND " + DatabaseContract.COLUMN_CODE + " = " + "'" + code + "'";
	}

	public String getStockDateSelection(String se, String code, String date) {
		return getStockSelection(se, code)
				+ " AND " + getDateSelection(date);
	}

	public String getStockPeriodSelection(String se, String code, String period) {
		return getStockSelection(se, code)
				+ " AND " + getPeriodSelection(period);
	}

	public String getStockTypeSelection(String se, String code, String type) {
		return getStockSelection(se, code)
				+ " AND " + getTypeSelection(type);
	}

	public String getPeriodLevelTypeSelection(String period, int level, String type) {
		return getPeriodSelection(period)
				+ " AND " + getLevelSelection(level)
				+ " AND " + getTypeSelection(type);
	}

	public String getStockPeriodLevelSelection(String se, String code, String period, int level) {
		return getStockSelection(se, code)
				+ " AND " + getPeriodSelection(period)
				+ " AND " + getLevelSelection(level);
	}

	public String getStockPeriodDateTimeSelection(String se, String code, String period, String date, String time) {
		String selection = getStockSelection(se, code)
				+ " AND " + getPeriodSelection(period)
				+ " AND " + getDateSelection(date);
		if (Period.isMinutePeriod(period)) {
			selection += " AND " + getTimeSelection(time);
		}
		return selection;
	}
}
