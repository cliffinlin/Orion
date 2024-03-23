package com.android.orion.manager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.android.orion.database.DatabaseOpenHelper;

public class DatabaseManager {
	private static DatabaseManager mInstance;

	public ContentResolver mContentResolver;
	public SQLiteDatabase mDatabase = null;
	public DatabaseOpenHelper mDatabaseHelper;
	Context mContext;

	protected DatabaseManager(@NonNull Context context) {
		mContext = context;

		mContentResolver = mContext.getContentResolver();
		mDatabaseHelper = new DatabaseOpenHelper(mContext);
	}

	@NonNull
	public static DatabaseManager getInstance(@NonNull Context context) {
		synchronized (DatabaseManager.class) {
			if (mInstance == null) {
				mInstance = new DatabaseManager(context.getApplicationContext());
			}
			return mInstance;
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
