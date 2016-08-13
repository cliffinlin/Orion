package com.android.orion.leancloud;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.android.orion.Constants;
import com.android.orion.StockManager;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.utility.Utility;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;

public class LeanCloudManager extends StockManager {

	AVQuery<AVObject> mQuery = null;
	List<AVObject> mAVObjectList = null;

	public LeanCloudManager(Context context) {
		super(context);

		if (mAVObjectList == null) {
			mAVObjectList = new ArrayList<AVObject>();
		}
	}

	public void fetchStockFavorite() {
		String now = Utility.getCurrentDateTimeString();
		Stock stock = Stock.obtain();

		findAll();

		if ((mAVObjectList == null) || (mAVObjectList.size() == 0)) {
			return;
		}

		for (AVObject avObject : mAVObjectList) {
			if (avObject != null) {
				stock.setSE(avObject
						.getString(DatabaseContract.Stock.TABLE_NAME + "_"
								+ DatabaseContract.COLUMN_SE));
				stock.setCode(avObject
						.getString(DatabaseContract.Stock.TABLE_NAME + "_"
								+ DatabaseContract.COLUMN_CODE));
				mStockDatabaseManager.getStock(stock);
				stock.setMark(Constants.STOCK_FLAG_MARK_FAVORITE);
				if (!mStockDatabaseManager.isStockExist(stock)) {
					stock.setCreated(now);
					stock.setModified(now);
					mStockDatabaseManager.insertStock(stock);
				} else {
					mStockDatabaseManager.updateStock(stock,
							stock.getContentValues());
				}
			}
		}

		AVUser.logOut();
	}

	public boolean saveStockFavorite() {
		boolean result = false;
		int i = 0;
		List<Stock> stockList = null;

		findAll();
		deleteAll();

		stockList = loadStockList(
				selectStock(Constants.STOCK_FLAG_MARK_FAVORITE), null, null);
		if ((stockList == null) || (stockList.size() == 0)) {
			return result;
		}

		for (Stock stock : stockList) {
			AVObject avObject = getFirst(stock);

			if (avObject == null) {
				avObject = new AVObject(LeanCloudContract.CLASS_FAVORITE);
			}

			avObject.put(LeanCloudContract.COLUMN_AUTHOR,
					AVUser.getCurrentUser());
			avObject.put(DatabaseContract.Stock.TABLE_NAME + "_"
					+ DatabaseContract.COLUMN_SE, stock.getSE());
			avObject.put(DatabaseContract.Stock.TABLE_NAME + "_"
					+ DatabaseContract.COLUMN_CODE, stock.getCode());
			avObject.put(DatabaseContract.Stock.TABLE_NAME + "_"
					+ DatabaseContract.COLUMN_NAME, stock.getName());

			try {
				avObject.save();
				i++;
			} catch (AVException e) {
				e.printStackTrace();
			}
		}

		if (i == mAVObjectList.size()) {
			result = true;
		}

		AVUser.logOut();

		return result;
	}

	void findAll() {
		mQuery = AVQuery.getQuery(LeanCloudContract.CLASS_FAVORITE);

		if (mQuery == null) {
			return;
		}

		mQuery.whereEqualTo(LeanCloudContract.COLUMN_AUTHOR,
				AVUser.getCurrentUser());

		try {
			mAVObjectList = mQuery.find();
		} catch (AVException e) {
			e.printStackTrace();
		}
	}

	void deleteAll() {
		if ((mAVObjectList == null) || (mAVObjectList.size() == 0)) {
			return;
		}

		try {
			AVObject.deleteAll(mAVObjectList);
		} catch (AVException e1) {
			e1.printStackTrace();
		}
	}

	AVObject getFirst(Stock stock) {
		AVObject result = null;

		if (stock == null) {
			return result;
		}

		mQuery = AVQuery.getQuery(LeanCloudContract.CLASS_FAVORITE);

		if (mQuery == null) {
			return result;
		}

		mQuery.whereEqualTo(LeanCloudContract.COLUMN_AUTHOR,
				AVUser.getCurrentUser());
		mQuery.whereEqualTo(DatabaseContract.Stock.TABLE_NAME + "_"
				+ DatabaseContract.COLUMN_SE, stock.getSE());
		mQuery.whereEqualTo(DatabaseContract.Stock.TABLE_NAME + "_"
				+ DatabaseContract.COLUMN_CODE, stock.getCode());

		try {
			result = mQuery.getFirst();
		} catch (AVException e) {
			if (e.getCode() != AVException.OBJECT_NOT_FOUND) {
				e.printStackTrace();
			}
		}

		return result;
	}
}
