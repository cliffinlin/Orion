package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

public class DatabaseTable {
	private String mTableName;
	private long mId;
	private String mCreated;
	private String mModified;

	public DatabaseTable() {
		init();
	}

	void init() {
		mId = 0;

		reset();
	}

	void reset() {
		mCreated = "";
		mModified = "";
	}

	public void contentValuesPut(ContentValues contentValues, String key,
								 int value) {
		if (value != 0) {
			contentValues.put(key, value);
		}
	}

	public void contentValuesPut(ContentValues contentValues, String key,
								 long value) {
		if (value != 0) {
			contentValues.put(key, value);
		}
	}

	public void contentValuesPut(ContentValues contentValues, String key,
								 double value) {
		if (value != 0) {
			contentValues.put(key, value);
		}
	}

	public void contentValuesPut(ContentValues contentValues, String key,
								 String value) {
		if (!TextUtils.isEmpty(value)) {
			contentValues.put(key, value);
		}
	}

	public ContentValues getContentValues() {
		ContentValues contentValues = new ContentValues();
		contentValues = getContentValues(contentValues);
		return contentValues;
	}

	ContentValues getContentValues(ContentValues contentValues) {
		contentValues.put(DatabaseContract.COLUMN_CREATED, mCreated);
		contentValues.put(DatabaseContract.COLUMN_MODIFIED, mModified);

		return contentValues;
	}

	void set(DatabaseTable databaseTable) {
		if (databaseTable == null) {
			return;
		}

		init();

		setId(databaseTable.mId);
		setCreated(databaseTable.mCreated);
		setModified(databaseTable.mModified);
	}

	void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		setId(cursor);
		setCreated(cursor);
		setModified(cursor);
	}

	String getTableName() {
		return mTableName;
	}

	void setTableName(String tableName) {
		mTableName = tableName;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	void setId(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setId(cursor.getLong(cursor.getColumnIndex(DatabaseContract.COLUMN_ID)));
	}

	public String getCreated() {
		return mCreated;
	}

	public void setCreated(String created) {
		mCreated = created;
	}

	public void setCreated(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setCreated(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_CREATED)));
	}

	public String getModified() {
		return mModified;
	}

	public void setModified(String modified) {
		mModified = modified;
	}

	void setModified(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setModified(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_MODIFIED)));
	}
}
