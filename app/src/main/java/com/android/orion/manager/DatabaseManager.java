package com.android.orion.manager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.orion.database.DatabaseOpenHelper;

public class DatabaseManager {
	private static DatabaseManager mInstance;

	public ContentResolver mContentResolver;
	public SQLiteDatabase mDatabase = null;
	public DatabaseOpenHelper mDatabaseHelper;
	Context mContext;

	protected DatabaseManager(Context context) {
		mContext = context;

		mContentResolver = mContext.getContentResolver();
		mDatabaseHelper = new DatabaseOpenHelper(mContext);
	}

	public static synchronized DatabaseManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DatabaseManager(context.getApplicationContext());
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
}
