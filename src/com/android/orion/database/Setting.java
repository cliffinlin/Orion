package com.android.orion.database;

import android.content.ContentValues;
import android.database.Cursor;

public class Setting extends DatabaseTable {
	public static final String KEY_STOCK_HSA_UPDATED = "stock_hsa_updated";

	public static final String KEY_SORT_ORDER_IPO_LIST = "sort_order_ipo_list";
	public static final String KEY_SORT_ORDER_MARKET_LIST = "sort_order_market_list";
	public static final String KEY_SORT_ORDER_FINANCIAL_LIST = "sort_order_financial_list";
	public static final String KEY_SORT_ORDER_DEAL_LIST = "sort_order_deal_list";

	public static final String KEY_STOCK_FILTER_ENABLED = "key_stock_filter_enabled";
	public static final String KEY_STOCK_FILTER_FAVORITE = "key_stock_filter_favorite";
	public static final String KEY_STOCK_FILTER_HOLD = "key_stock_filter_hold";
	public static final String KEY_STOCK_FILTER_ROI = "key_stock_filter_roi";
	public static final String KEY_STOCK_FILTER_RATE = "key_stock_filter_rate";
	public static final String KEY_STOCK_FILTER_ROE = "key_stock_filter_roe";
	public static final String KEY_STOCK_FILTER_PE = "key_stock_filter_pe";
	public static final String KEY_STOCK_FILTER_PB = "key_stock_filter_pb";
	public static final String KEY_STOCK_FILTER_DIVIDEND = "key_stock_filter_dividend";
	public static final String KEY_STOCK_FILTER_YIELD = "key_stock_filter_yield";
	public static final String KEY_STOCK_FILTER_DELTA = "key_stock_filter_delta";

	private String mKey;
	private String mValue;

	public Setting() {
		init();
	}

	public Setting(Setting setting) {
		set(setting);
	}

	public Setting(Cursor cursor) {
		set(cursor);
	}

	void init() {
		super.init();

		setTableName(DatabaseContract.Setting.TABLE_NAME);

		mKey = "";
		mValue = "";
	}

	public ContentValues getContentValues() {
		ContentValues contentValues = new ContentValues();

		getContentValues(contentValues);

		return contentValues;
	}

	public ContentValues getContentValues(ContentValues contentValues) {
		super.getContentValues(contentValues);

		contentValues.put(DatabaseContract.Setting.COLUMN_KEY, mKey);
		contentValues.put(DatabaseContract.Setting.COLUMN_VALUE, mValue);

		return contentValues;
	}

	void set(Setting setting) {
		if (setting == null) {
			return;
		}

		init();

		super.set(setting);

		setKey(setting.getKey());
		setValue(setting.getValue());
	}

	public void set(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		init();

		super.set(cursor);

		setKey(cursor);
		setValue(cursor);
	}

	public String getKey() {
		return mKey;
	}

	public void setKey(String key) {
		mKey = key;
	}

	void setKey(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setKey(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.Setting.COLUMN_KEY)));
	}

	public String getValue() {
		return mValue;
	}

	public void setValue(String value) {
		mValue = value;
	}

	void setValue(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		setValue(cursor.getString(cursor
				.getColumnIndex(DatabaseContract.Setting.COLUMN_VALUE)));
	}
}
