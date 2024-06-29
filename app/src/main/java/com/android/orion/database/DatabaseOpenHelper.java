package com.android.orion.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

	public DatabaseOpenHelper(Context context) {
		super(context, DatabaseContract.DATABASE_FILE, null,
				DatabaseContract.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		if (db == null) {
			return;
		}
		db.execSQL(DatabaseContract.Stock.CREATE_TABLE);
		db.execSQL(DatabaseContract.StockData.CREATE_TABLE);
		db.execSQL(DatabaseContract.StockDeal.CREATE_TABLE);
		db.execSQL(DatabaseContract.StockQuant.CREATE_TABLE);
		db.execSQL(DatabaseContract.StockFinancial.CREATE_TABLE);
		db.execSQL(DatabaseContract.ShareBonus.CREATE_TABLE);
		db.execSQL(DatabaseContract.TotalShare.CREATE_TABLE);
		db.execSQL(DatabaseContract.IndexComponent.CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (db == null) {
			return;
		}
		db.execSQL(DatabaseContract.Stock.DELETE_TABLE);
		db.execSQL(DatabaseContract.StockData.DELETE_TABLE);
		db.execSQL(DatabaseContract.StockDeal.DELETE_TABLE);
		db.execSQL(DatabaseContract.StockQuant.DELETE_TABLE);
		db.execSQL(DatabaseContract.StockFinancial.DELETE_TABLE);
		db.execSQL(DatabaseContract.ShareBonus.DELETE_TABLE);
		db.execSQL(DatabaseContract.TotalShare.DELETE_TABLE);
		db.execSQL(DatabaseContract.IndexComponent.DELETE_TABLE);
		onCreate(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}