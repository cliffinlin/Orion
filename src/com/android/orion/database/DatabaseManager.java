package com.android.orion.database;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class DatabaseManager {
	Context mContext;
	public ContentResolver mContentResolver = null;
	public SQLiteDatabase mDatabase = null;
	public DatabaseOpenHelper mDatabaseHelper = null;

	public DatabaseManager() {
	}

	public DatabaseManager(Context context) {
		mContext = context;

		if (mContentResolver == null) {
			mContentResolver = mContext.getContentResolver();
		}

		if (mDatabaseHelper == null) {
			mDatabaseHelper = new DatabaseOpenHelper(mContext);
		}
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
