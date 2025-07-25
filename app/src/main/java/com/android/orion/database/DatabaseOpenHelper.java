package com.android.orion.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

	public DatabaseOpenHelper(Context context) {
		super(context, DatabaseContract.DATABASE_FILE_NAME, null,
				DatabaseContract.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		if (db == null) {
			return;
		}
		try {
			db.beginTransaction();
			db.execSQL(DatabaseContract.Stock.CREATE_TABLE);
			db.execSQL(DatabaseContract.StockData.CREATE_TABLE);
			db.execSQL(DatabaseContract.TDXData.CREATE_TABLE);
			db.execSQL(DatabaseContract.StockTrend.CREATE_TABLE);
			db.execSQL(DatabaseContract.StockPerceptron.CREATE_TABLE);
			db.execSQL(DatabaseContract.StockDeal.CREATE_TABLE);
			db.execSQL(DatabaseContract.StockGrid.CREATE_TABLE);
			db.execSQL(DatabaseContract.StockFinancial.CREATE_TABLE);
			db.execSQL(DatabaseContract.StockBonus.CREATE_TABLE);
			db.execSQL(DatabaseContract.StockShare.CREATE_TABLE);
			db.execSQL(DatabaseContract.StockRZRQ.CREATE_TABLE);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (db == null) {
			return;
		}
		try {
			db.beginTransaction();
			db.execSQL(DatabaseContract.Stock.DELETE_TABLE);
			db.execSQL(DatabaseContract.StockData.DELETE_TABLE);
			db.execSQL(DatabaseContract.TDXData.DELETE_TABLE);
			db.execSQL(DatabaseContract.StockTrend.DELETE_TABLE);
			db.execSQL(DatabaseContract.StockPerceptron.DELETE_TABLE);
			db.execSQL(DatabaseContract.StockDeal.DELETE_TABLE);
			db.execSQL(DatabaseContract.StockGrid.DELETE_TABLE);
			db.execSQL(DatabaseContract.StockFinancial.DELETE_TABLE);
			db.execSQL(DatabaseContract.StockBonus.DELETE_TABLE);
			db.execSQL(DatabaseContract.StockShare.DELETE_TABLE);
			db.execSQL(DatabaseContract.StockRZRQ.DELETE_TABLE);
			db.setTransactionSuccessful();
			onCreate(db);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}