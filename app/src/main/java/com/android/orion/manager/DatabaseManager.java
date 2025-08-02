package com.android.orion.manager;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.android.orion.config.Config;
import com.android.orion.database.DatabaseOpenHelper;

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
}
