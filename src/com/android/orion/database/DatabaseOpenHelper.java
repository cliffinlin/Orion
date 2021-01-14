package com.android.orion.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

	public DatabaseOpenHelper(Context context) {
		super(context, DatabaseContract.DATABASE_FILE, null,
				DatabaseContract.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DatabaseContract.Setting.CREATE_TABLE);
		db.execSQL(DatabaseContract.Stock.CREATE_TABLE);
		db.execSQL(DatabaseContract.StockData.CREATE_TABLE);
		db.execSQL(DatabaseContract.StockDeal.CREATE_TABLE);
		db.execSQL(DatabaseContract.FinancialData.CREATE_TABLE);
		db.execSQL(DatabaseContract.ShareBonus.CREATE_TABLE);
		db.execSQL(DatabaseContract.TotalShare.CREATE_TABLE);
		db.execSQL(DatabaseContract.IPO.CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DatabaseContract.Setting.DELETE_TABLE);
		db.execSQL(DatabaseContract.Stock.DELETE_TABLE);
		db.execSQL(DatabaseContract.StockData.DELETE_TABLE);
		db.execSQL(DatabaseContract.StockDeal.DELETE_TABLE);
		db.execSQL(DatabaseContract.FinancialData.DELETE_TABLE);
		db.execSQL(DatabaseContract.ShareBonus.DELETE_TABLE);
		db.execSQL(DatabaseContract.TotalShare.DELETE_TABLE);
		db.execSQL(DatabaseContract.IPO.DELETE_TABLE);
		onCreate(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}