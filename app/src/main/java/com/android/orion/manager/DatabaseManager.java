package com.android.orion.manager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.android.orion.database.DatabaseOpenHelper;

public class DatabaseManager {


	Context mContext;

	public ContentResolver mContentResolver;
	public SQLiteDatabase mDatabase = null;
	public DatabaseOpenHelper mDatabaseHelper;

	private static final Object mLock = new Object();
	private static DatabaseManager mInstance;

	@NonNull
	public static DatabaseManager getInstance(@NonNull Context context) {
		synchronized (mLock) {
			if (mInstance == null) {
				mInstance = new DatabaseManager(context.getApplicationContext());
			}
			return mInstance;
		}
	}

	DatabaseManager(@NonNull Context context) {
		mContext = context;

		mContentResolver = mContext.getContentResolver();
		mDatabaseHelper = new DatabaseOpenHelper(mContext);
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
