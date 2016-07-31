package com.android.orion;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.orion.database.DatabaseOpenHelper;

public class DatabaseManager {
	Context mContext;
	ContentResolver mContentResolver = null;
	SQLiteDatabase mDatabase = null;
	DatabaseOpenHelper mDatabaseHelper = null;

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
