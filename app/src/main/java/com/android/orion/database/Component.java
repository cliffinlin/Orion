package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

public class Component extends DatabaseTable {
	private long mIndexId;
	private long mStockId;
	private String mSE;
	private String mCode;
	private String mName;
	private double mPrice;
	private double mNet;
	private long mFlag;
	private String mOperate;

	public Component() {
		init();
	}

	public Component(Component component) {
		set(component);
	}

	public Component(Cursor cursor) {
		set(cursor);
	}

	void init() {
		super.init();

		setTableName(DatabaseContract.Component.TABLE_NAME);

		mIndexId = 0;
		mStockId = 0;
		mSE = "";
		mCode = "";
		mName = "";
		mPrice = 0;
		mNet = 0;
		mFlag = 0;
		mOperate = "";
	}

	public ContentValues getContentValues() {
		ContentValues contentValues = new ContentValues();
		super.getContentValues(contentValues);
		contentValues = getContentValues(contentValues);

		return contentValues;
	}

	ContentValues getContentValues(ContentValues contentValues) {
		contentValues.put(DatabaseContract.COLUMN_INDEX_ID, mIndexId);
		contentValues.put(DatabaseContract.COLUMN_STOCK_ID, mStockId);
		contentValues.put(DatabaseContract.COLUMN_SE, mSE);
		contentValues.put(DatabaseContract.COLUMN_CODE, mCode);
		contentValues.put(DatabaseContract.COLUMN_NAME, mName);
		contentValues.put(DatabaseContract.COLUMN_PRICE, mPrice);
		contentValues.put(DatabaseContract.COLUMN_NET, mNet);
		contentValues.put(DatabaseContract.COLUMN_FLAG, mFlag);
		contentValues
				.put(DatabaseContract.COLUMN_OPERATE, mOperate);

		return contentValues;
	}

	public void set(Component component) {
		if (component == null) {
			return;
		}

		init();

		super.set(component);

		setIndexId(component.mIndexId);
		setStockId(component.mStockId);
		setSE(component.mSE);
		setCode(component.mCode);
		setName(component.mName);
		setPrice(component.mPrice);
		setNet(component.mNet);
		setFlag(component.mFlag);
		setOperate(component.mOperate);
	}

	@Override
	public void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		super.set(cursor);

		setIndexID(cursor);
		setStockID(cursor);
		setSE(cursor);
		setCode(cursor);
		setName(cursor);
		setPrice(cursor);
		setNet(cursor);
		setFlag(cursor);
		setOperate(cursor);
	}

	public long getIndexId() {
		return mIndexId;
	}

	public void setIndexId(long indexId) {
		mIndexId = indexId;
	}

	void setIndexID(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setIndexId(cursor.getLong(cursor
				.getColumnIndex(DatabaseContract.COLUMN_INDEX_ID)));
	}

	public long getStockId() {
		return mStockId;
	}

	public void setStockId(long stockId) {
		mStockId = stockId;
	}

	void setStockID(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setStockId(cursor.getLong(cursor
				.getColumnIndex(DatabaseContract.COLUMN_STOCK_ID)));
	}

	public String getSE() {
		return mSE;
	}

	public void setSE(String se) {
		mSE = se;
	}

	public void setSE(Cursor cursor) {
		if (cursor == null) {
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
		if (cursor == null) {
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
		if (cursor == null) {
			return;
		}

		setName(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NAME)));
	}

	public double getPrice() {
		return mPrice;
	}

	public void setPrice(double price) {
		mPrice = price;
	}

	void setPrice(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setPrice(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_PRICE)));
	}

	public double getNet() {
		return mNet;
	}

	public void setNet(double net) {
		mNet = net;
	}

	void setNet(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setNet(cursor.getDouble(cursor
				.getColumnIndex(DatabaseContract.COLUMN_NET)));
	}

	public long getFlag() {
		return mFlag;
	}

	public void setFlag(long flag) {
		mFlag = flag;
	}

	void setFlag(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setFlag(cursor.getLong(cursor
				.getColumnIndex(DatabaseContract.COLUMN_FLAG)));
	}

	public String getOperate() {
		return mOperate;
	}

	public void setOperate(String operate) {
		mOperate = operate;
	}

	void setOperate(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setOperate(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.COLUMN_OPERATE)));
	}
}
