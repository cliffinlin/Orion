package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

public class IndexComponent extends DatabaseTable {
	private String mIndexSE;
	private String mIndexCode;
	private String mIndexName;
	private String mSE;
	private String mCode;
	private String mName;

	public IndexComponent() {
		init();
	}

	public IndexComponent(IndexComponent indexComponent) {
		set(indexComponent);
	}

	public IndexComponent(Cursor cursor) {
		set(cursor);
	}

	void init() {
		super.init();

		setTableName(DatabaseContract.IndexComponent.TABLE_NAME);

		mIndexSE = "";
		mIndexCode = "";
		mIndexName = "";
		mSE = "";
		mCode = "";
		mName = "";
	}

	@Override
	public ContentValues getContentValues() {
		ContentValues contentValues = super.getContentValues();

		contentValues.put(DatabaseContract.COLUMN_INDEX_SE, mIndexSE);
		contentValues.put(DatabaseContract.COLUMN_INDEX_CODE, mIndexCode);
		contentValues.put(DatabaseContract.COLUMN_INDEX_NAME, mIndexName);
		contentValues.put(DatabaseContract.COLUMN_SE, mSE);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		return contentValues;
	}

	public void set(IndexComponent indexComponent) {
		if (indexComponent == null) {
			return;
		}

		init();

		super.set(indexComponent);

		setIndexSE(indexComponent.mIndexSE);
		setIndexCode(indexComponent.mIndexCode);
		setIndexName(indexComponent.mIndexName);
		setSE(indexComponent.mSE);
		setCode(indexComponent.mCode);
		setName(indexComponent.mName);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		init();

		super.set(cursor);

		setIndexSE(cursor);
		setIndexCode(cursor);
		setIndexName(cursor);
		setSE(cursor);
		setCode(cursor);
		setName(cursor);
	}

	public String geIndexSE() {
		return mIndexSE;
	}

	public void setIndexSE(String indexSe) {
		mIndexSE = indexSe;
	}

	public void setIndexSE(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setIndexSE(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_INDEX_SE)));
	}

	public String getIndexCode() {
		return mIndexCode;
	}

	public void setIndexCode(String indexCode) {
		mIndexCode = indexCode;
	}

	public void setIndexCode(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setIndexCode(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_INDEX_CODE)));
	}

	public String getIndexName() {
		return mIndexName;
	}

	public void setIndexName(String indexName) {
		mIndexName = indexName;
	}

	public void setIndexName(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setIndexName(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_INDEX_NAME)));
	}

	public String getSE() {
		return mSE;
	}

	public void setSE(String se) {
		mSE = se;
	}

	public void setSE(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setSE(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_SE)));
	}

	public String getCode() {
		return mCode;
	}

	public void setCode(String code) {
		mCode = code;
	}

	public void setCode(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setCode(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_CODE)));
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public void setName(Cursor cursor) {
		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setName(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NAME)));
	}
}
