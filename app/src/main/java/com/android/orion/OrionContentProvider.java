package com.android.orion;

import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.DatabaseManager;

public class OrionContentProvider extends ContentProvider {
	private static final int STOCK = 200;
	private static final int STOCK_ID = 201;

	private static final int STOCK_DATA = 300;
	private static final int STOCK_DATA_ID = 301;

	private static final int STOCK_DEAL = 400;
	private static final int STOCK_DEAL_ID = 401;

	private static final int FINANCIAL_DATA = 500;
	private static final int FINANCIAL_DATA_ID = 501;

	private static final int SHARE_BONUS = 600;
	private static final int SHARE_BONUS_ID = 601;

	private static final int TOTAL_SHARE = 700;
	private static final int TOTAL_SHARE_ID = 701;

	private static final int IPO = 800;
	private static final int IPO_ID = 801;

	private static final int INDEX_COMPONENT = 900;
	private static final int INDEX_COMPONENT_ID = 901;

	private static final UriMatcher mUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		mUriMatcher.addURI(DatabaseContract.AUTHORITY,
				DatabaseContract.Stock.TABLE_NAME, STOCK);
		mUriMatcher.addURI(DatabaseContract.AUTHORITY,
				DatabaseContract.Stock.TABLE_NAME + "/#", STOCK_ID);

		mUriMatcher.addURI(DatabaseContract.AUTHORITY,
				DatabaseContract.StockData.TABLE_NAME, STOCK_DATA);
		mUriMatcher.addURI(DatabaseContract.AUTHORITY,
				DatabaseContract.StockData.TABLE_NAME + "/#", STOCK_DATA_ID);

		mUriMatcher.addURI(DatabaseContract.AUTHORITY,
				DatabaseContract.StockDeal.TABLE_NAME, STOCK_DEAL);
		mUriMatcher.addURI(DatabaseContract.AUTHORITY,
				DatabaseContract.StockDeal.TABLE_NAME + "/#", STOCK_DEAL_ID);

		mUriMatcher.addURI(DatabaseContract.AUTHORITY,
				DatabaseContract.FinancialData.TABLE_NAME, FINANCIAL_DATA);
		mUriMatcher.addURI(DatabaseContract.AUTHORITY,
				DatabaseContract.FinancialData.TABLE_NAME + "/#",
				FINANCIAL_DATA_ID);

		mUriMatcher.addURI(DatabaseContract.AUTHORITY,
				DatabaseContract.ShareBonus.TABLE_NAME, SHARE_BONUS);
		mUriMatcher.addURI(DatabaseContract.AUTHORITY,
				DatabaseContract.ShareBonus.TABLE_NAME + "/#", SHARE_BONUS_ID);

		mUriMatcher.addURI(DatabaseContract.AUTHORITY,
				DatabaseContract.TotalShare.TABLE_NAME, TOTAL_SHARE);
		mUriMatcher.addURI(DatabaseContract.AUTHORITY,
				DatabaseContract.TotalShare.TABLE_NAME + "/#", TOTAL_SHARE_ID);

		mUriMatcher.addURI(DatabaseContract.AUTHORITY,
				DatabaseContract.IPO.TABLE_NAME, IPO);
		mUriMatcher.addURI(DatabaseContract.AUTHORITY,
				DatabaseContract.IPO.TABLE_NAME + "/#", IPO_ID);

		mUriMatcher.addURI(DatabaseContract.AUTHORITY,
				DatabaseContract.IndexComponent.TABLE_NAME, INDEX_COMPONENT);
		mUriMatcher.addURI(DatabaseContract.AUTHORITY,
				DatabaseContract.IndexComponent.TABLE_NAME + "/#", INDEX_COMPONENT_ID);
	}

	ContentResolver mContentResolver = null;
	DatabaseManager mDatabaseManager = null;

	@Override
	public boolean onCreate() {
		if (mContentResolver == null) {
			mContentResolver = getContext().getContentResolver();
		}

		if (mDatabaseManager == null) {
			mDatabaseManager = new DatabaseManager(getContext());
		}

		if (mDatabaseManager != null) {
			mDatabaseManager.openDatabase();
		}

		return true;
	}

	@Override
	public String getType(Uri uri) {
		String type = null;

		switch (mUriMatcher.match(uri)) {
		case STOCK:
			type = DatabaseContract.Stock.CONTENT_TYPE;
			break;
		case STOCK_ID:
			type = DatabaseContract.Stock.CONTENT_ITEM_TYPE;
			break;

		case STOCK_DATA:
			type = DatabaseContract.StockData.CONTENT_TYPE;
			break;
		case STOCK_DATA_ID:
			type = DatabaseContract.StockData.CONTENT_ITEM_TYPE;
			break;

		case STOCK_DEAL:
			type = DatabaseContract.StockDeal.CONTENT_TYPE;
			break;
		case STOCK_DEAL_ID:
			type = DatabaseContract.StockDeal.CONTENT_ITEM_TYPE;
			break;

		case FINANCIAL_DATA:
			type = DatabaseContract.FinancialData.CONTENT_TYPE;
			break;
		case FINANCIAL_DATA_ID:
			type = DatabaseContract.FinancialData.CONTENT_ITEM_TYPE;
			break;

		case SHARE_BONUS:
			type = DatabaseContract.ShareBonus.CONTENT_TYPE;
			break;
		case SHARE_BONUS_ID:
			type = DatabaseContract.ShareBonus.CONTENT_ITEM_TYPE;
			break;

		case TOTAL_SHARE:
			type = DatabaseContract.TotalShare.CONTENT_TYPE;
			break;
		case TOTAL_SHARE_ID:
			type = DatabaseContract.TotalShare.CONTENT_ITEM_TYPE;
			break;

		case IPO:
			type = DatabaseContract.IPO.CONTENT_TYPE;
			break;
		case IPO_ID:
			type = DatabaseContract.IPO.CONTENT_ITEM_TYPE;
			break;

		case INDEX_COMPONENT:
			type = DatabaseContract.IndexComponent.CONTENT_TYPE;
			break;
		case INDEX_COMPONENT_ID:
			type = DatabaseContract.IndexComponent.CONTENT_ITEM_TYPE;
			break;
		default:
			break;
		}

		return type;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;

		if (mDatabaseManager == null) {
			return null;
		}

		if (mDatabaseManager.mDatabase == null) {
			return null;
		}

		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

		switch (mUriMatcher.match(uri)) {
		case STOCK:
			builder.setTables(DatabaseContract.Stock.TABLE_NAME);
			break;
		case STOCK_ID:
			builder.setTables(DatabaseContract.Stock.TABLE_NAME);
			builder.appendWhere(BaseColumns._ID + " = "
					+ uri.getLastPathSegment());
			break;

		case STOCK_DATA:
			builder.setTables(DatabaseContract.StockData.TABLE_NAME);
			break;
		case STOCK_DATA_ID:
			builder.setTables(DatabaseContract.StockData.TABLE_NAME);
			builder.appendWhere(BaseColumns._ID + " = "
					+ uri.getLastPathSegment());
			break;

		case STOCK_DEAL:
			builder.setTables(DatabaseContract.StockDeal.TABLE_NAME);
			break;
		case STOCK_DEAL_ID:
			builder.setTables(DatabaseContract.StockDeal.TABLE_NAME);
			builder.appendWhere(BaseColumns._ID + " = "
					+ uri.getLastPathSegment());
			break;

		case FINANCIAL_DATA:
			builder.setTables(DatabaseContract.FinancialData.TABLE_NAME);
			break;
		case FINANCIAL_DATA_ID:
			builder.setTables(DatabaseContract.FinancialData.TABLE_NAME);
			builder.appendWhere(BaseColumns._ID + " = "
					+ uri.getLastPathSegment());
			break;

		case SHARE_BONUS:
			builder.setTables(DatabaseContract.ShareBonus.TABLE_NAME);
			break;
		case SHARE_BONUS_ID:
			builder.setTables(DatabaseContract.ShareBonus.TABLE_NAME);
			builder.appendWhere(BaseColumns._ID + " = "
					+ uri.getLastPathSegment());
			break;

		case TOTAL_SHARE:
			builder.setTables(DatabaseContract.TotalShare.TABLE_NAME);
			break;
		case TOTAL_SHARE_ID:
			builder.setTables(DatabaseContract.TotalShare.TABLE_NAME);
			builder.appendWhere(BaseColumns._ID + " = "
					+ uri.getLastPathSegment());
			break;

		case IPO:
			builder.setTables(DatabaseContract.IPO.TABLE_NAME);
			break;
		case IPO_ID:
			builder.setTables(DatabaseContract.IPO.TABLE_NAME);
			builder.appendWhere(BaseColumns._ID + " = "
					+ uri.getLastPathSegment());
			break;

		case INDEX_COMPONENT:
			builder.setTables(DatabaseContract.IndexComponent.TABLE_NAME);
			break;
		case INDEX_COMPONENT_ID:
			builder.setTables(DatabaseContract.IndexComponent.TABLE_NAME);
			builder.appendWhere(BaseColumns._ID + " = "
					+ uri.getLastPathSegment());
			break;
		default:
			break;
		}

		cursor = builder.query(mDatabaseManager.mDatabase, projection,
				selection, selectionArgs, null, null, sortOrder);

		if (cursor != null) {
			cursor.setNotificationUri(mContentResolver, uri);
		}

		return cursor;
	}

	public Uri insert(Uri uri, ContentValues contentValues, boolean notifyChange) {
		long id = 0;
		Uri itemUri = null;

		if (mDatabaseManager == null) {
			return itemUri;
		}

		if (mDatabaseManager.mDatabase == null) {
			return itemUri;
		}

		switch (mUriMatcher.match(uri)) {
		case STOCK:
			id = mDatabaseManager.mDatabase.insert(
					DatabaseContract.Stock.TABLE_NAME, null, contentValues);
			break;

		case STOCK_DATA:
			id = mDatabaseManager.mDatabase.insert(
					DatabaseContract.StockData.TABLE_NAME, null, contentValues);
			break;

		case STOCK_DEAL:
			id = mDatabaseManager.mDatabase.insert(
					DatabaseContract.StockDeal.TABLE_NAME, null, contentValues);
			break;

		case FINANCIAL_DATA:
			id = mDatabaseManager.mDatabase.insert(
					DatabaseContract.FinancialData.TABLE_NAME, null,
					contentValues);
			break;

		case SHARE_BONUS:
			id = mDatabaseManager.mDatabase
					.insert(DatabaseContract.ShareBonus.TABLE_NAME, null,
							contentValues);
			break;

		case TOTAL_SHARE:
			id = mDatabaseManager.mDatabase
					.insert(DatabaseContract.TotalShare.TABLE_NAME, null,
							contentValues);
			break;

		case IPO:
			id = mDatabaseManager.mDatabase.insert(
					DatabaseContract.IPO.TABLE_NAME, null, contentValues);
			break;

		case INDEX_COMPONENT:
			id = mDatabaseManager.mDatabase.insert(
					DatabaseContract.IndexComponent.TABLE_NAME, null, contentValues);
			break;
		default:
			break;
		}

		if (id > 0) {
			itemUri = ContentUris.withAppendedId(uri, id);

			if (notifyChange) {
				mContentResolver.notifyChange(itemUri, null);
			}
		}

		return itemUri;
	}

	@Override
	public Uri insert(Uri uri, ContentValues contentValues) {
		return insert(uri, contentValues, true);
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		int result = 0;

		if (mDatabaseManager == null) {
			return result;
		}

		if (mDatabaseManager.mDatabase == null) {
			return result;
		}

		mDatabaseManager.mDatabase.beginTransaction();

		try {
			for (ContentValues contentValues : values) {
				if (insert(uri, contentValues, false) != null) {
					result++;
				}
			}

			mDatabaseManager.mDatabase.setTransactionSuccessful();

			if (result > 0) {
				mContentResolver.notifyChange(uri, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mDatabaseManager.mDatabase.endTransaction();
		}

		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int result = 0;
		String whereClause;

		if (mDatabaseManager == null) {
			return result;
		}

		if (mDatabaseManager.mDatabase == null) {
			return result;
		}

		switch (mUriMatcher.match(uri)) {
		case STOCK:
			result = mDatabaseManager.mDatabase.update(
					DatabaseContract.Stock.TABLE_NAME, values, selection,
					selectionArgs);
			break;
		case STOCK_ID:
			whereClause = BaseColumns._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection)) {
				whereClause += " AND " + whereClause;
			}
			result = mDatabaseManager.mDatabase.update(
					DatabaseContract.Stock.TABLE_NAME, values, whereClause,
					selectionArgs);
			break;

		case STOCK_DATA:
			result = mDatabaseManager.mDatabase.update(
					DatabaseContract.StockData.TABLE_NAME, values, selection,
					selectionArgs);
			break;
		case STOCK_DATA_ID:
			whereClause = BaseColumns._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection)) {
				whereClause += " AND " + whereClause;
			}
			result = mDatabaseManager.mDatabase.update(
					DatabaseContract.StockData.TABLE_NAME, values, whereClause,
					selectionArgs);
			break;

		case STOCK_DEAL:
			result = mDatabaseManager.mDatabase.update(
					DatabaseContract.StockDeal.TABLE_NAME, values, selection,
					selectionArgs);
			break;
		case STOCK_DEAL_ID:
			whereClause = BaseColumns._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection)) {
				whereClause += " AND " + whereClause;
			}
			result = mDatabaseManager.mDatabase.update(
					DatabaseContract.StockDeal.TABLE_NAME, values, whereClause,
					selectionArgs);
			break;

		case FINANCIAL_DATA:
			result = mDatabaseManager.mDatabase.update(
					DatabaseContract.FinancialData.TABLE_NAME, values,
					selection, selectionArgs);
			break;
		case FINANCIAL_DATA_ID:
			whereClause = BaseColumns._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection)) {
				whereClause += " AND " + whereClause;
			}
			result = mDatabaseManager.mDatabase.update(
					DatabaseContract.FinancialData.TABLE_NAME, values,
					whereClause, selectionArgs);
			break;

		case SHARE_BONUS:
			result = mDatabaseManager.mDatabase.update(
					DatabaseContract.ShareBonus.TABLE_NAME, values, selection,
					selectionArgs);
			break;
		case SHARE_BONUS_ID:
			whereClause = BaseColumns._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection)) {
				whereClause += " AND " + whereClause;
			}
			result = mDatabaseManager.mDatabase.update(
					DatabaseContract.ShareBonus.TABLE_NAME, values,
					whereClause, selectionArgs);
			break;

		case TOTAL_SHARE:
			result = mDatabaseManager.mDatabase.update(
					DatabaseContract.TotalShare.TABLE_NAME, values, selection,
					selectionArgs);
			break;
		case TOTAL_SHARE_ID:
			whereClause = BaseColumns._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection)) {
				whereClause += " AND " + whereClause;
			}
			result = mDatabaseManager.mDatabase.update(
					DatabaseContract.TotalShare.TABLE_NAME, values,
					whereClause, selectionArgs);
			break;

		case IPO:
			result = mDatabaseManager.mDatabase.update(
					DatabaseContract.IPO.TABLE_NAME, values, selection,
					selectionArgs);
			break;
		case IPO_ID:
			whereClause = BaseColumns._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection)) {
				whereClause += " AND " + whereClause;
			}
			result = mDatabaseManager.mDatabase.update(
					DatabaseContract.IPO.TABLE_NAME, values, whereClause,
					selectionArgs);
			break;

		case INDEX_COMPONENT:
			result = mDatabaseManager.mDatabase.update(
					DatabaseContract.IndexComponent.TABLE_NAME, values, selection,
					selectionArgs);
			break;
		case INDEX_COMPONENT_ID:
			whereClause = BaseColumns._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection)) {
				whereClause += " AND " + whereClause;
			}
			result = mDatabaseManager.mDatabase.update(
					DatabaseContract.IndexComponent.TABLE_NAME, values, whereClause,
					selectionArgs);
			break;
		default:
			break;
		}

		if (result > 0) {
			mContentResolver.notifyChange(uri, null);
		}

		return result;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int result = 0;
		String whereClause;

		if (mDatabaseManager == null) {
			return result;
		}

		if (mDatabaseManager.mDatabase == null) {
			return result;
		}

		switch (mUriMatcher.match(uri)) {
		case STOCK:
			result = mDatabaseManager.mDatabase
					.delete(DatabaseContract.Stock.TABLE_NAME, selection,
							selectionArgs);
			break;

		case STOCK_ID:
			whereClause = BaseColumns._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection)) {
				whereClause += " AND " + whereClause;
			}
			result = mDatabaseManager.mDatabase.delete(
					DatabaseContract.Stock.TABLE_NAME, whereClause,
					selectionArgs);
			break;

		case STOCK_DATA:
			result = mDatabaseManager.mDatabase.delete(
					DatabaseContract.StockData.TABLE_NAME, selection,
					selectionArgs);
			break;

		case STOCK_DATA_ID:
			whereClause = BaseColumns._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection)) {
				whereClause += " AND " + whereClause;
			}
			result = mDatabaseManager.mDatabase.delete(
					DatabaseContract.StockData.TABLE_NAME, whereClause,
					selectionArgs);
			break;

		case STOCK_DEAL:
			result = mDatabaseManager.mDatabase.delete(
					DatabaseContract.StockDeal.TABLE_NAME, selection,
					selectionArgs);
			break;

		case STOCK_DEAL_ID:
			whereClause = BaseColumns._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection)) {
				whereClause += " AND " + whereClause;
			}
			result = mDatabaseManager.mDatabase.delete(
					DatabaseContract.StockDeal.TABLE_NAME, whereClause,
					selectionArgs);
			break;

		case FINANCIAL_DATA:
			result = mDatabaseManager.mDatabase.delete(
					DatabaseContract.FinancialData.TABLE_NAME, selection,
					selectionArgs);
			break;

		case FINANCIAL_DATA_ID:
			whereClause = BaseColumns._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection)) {
				whereClause += " AND " + whereClause;
			}
			result = mDatabaseManager.mDatabase.delete(
					DatabaseContract.FinancialData.TABLE_NAME, whereClause,
					selectionArgs);
			break;

		case SHARE_BONUS:
			result = mDatabaseManager.mDatabase.delete(
					DatabaseContract.ShareBonus.TABLE_NAME, selection,
					selectionArgs);
			break;

		case SHARE_BONUS_ID:
			whereClause = BaseColumns._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection)) {
				whereClause += " AND " + whereClause;
			}
			result = mDatabaseManager.mDatabase.delete(
					DatabaseContract.ShareBonus.TABLE_NAME, whereClause,
					selectionArgs);
			break;

		case TOTAL_SHARE:
			result = mDatabaseManager.mDatabase.delete(
					DatabaseContract.TotalShare.TABLE_NAME, selection,
					selectionArgs);
			break;

		case TOTAL_SHARE_ID:
			whereClause = BaseColumns._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection)) {
				whereClause += " AND " + whereClause;
			}
			result = mDatabaseManager.mDatabase.delete(
					DatabaseContract.TotalShare.TABLE_NAME, whereClause,
					selectionArgs);
			break;

		case IPO:
			result = mDatabaseManager.mDatabase.delete(
					DatabaseContract.IPO.TABLE_NAME, selection, selectionArgs);
			break;

		case IPO_ID:
			whereClause = BaseColumns._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection)) {
				whereClause += " AND " + whereClause;
			}
			result = mDatabaseManager.mDatabase
					.delete(DatabaseContract.IPO.TABLE_NAME, whereClause,
							selectionArgs);
			break;

		case INDEX_COMPONENT:
			result = mDatabaseManager.mDatabase.delete(
					DatabaseContract.IndexComponent.TABLE_NAME, selection, selectionArgs);
			break;

		case INDEX_COMPONENT_ID:
			whereClause = BaseColumns._ID + " = " + uri.getLastPathSegment();
			if (!TextUtils.isEmpty(selection)) {
				whereClause += " AND " + whereClause;
			}
			result = mDatabaseManager.mDatabase
					.delete(DatabaseContract.IndexComponent.TABLE_NAME, whereClause,
							selectionArgs);
			break;
		default:
			break;
		}

		if (result > 0) {
			mContentResolver.notifyChange(uri, null);
		}

		return result;
	}

	@Override
	public ContentProviderResult[] applyBatch(
			ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		ContentProviderResult[] results = null;

		if (mDatabaseManager == null) {
			return results;
		}

		if (mDatabaseManager.mDatabase == null) {
			return results;
		}

		mDatabaseManager.mDatabase.beginTransaction();

		try {
			results = super.applyBatch(operations);
			mDatabaseManager.mDatabase.setTransactionSuccessful();
			return results;
		} finally {
			mDatabaseManager.mDatabase.endTransaction();
		}
	}
}
