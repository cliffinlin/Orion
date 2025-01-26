package com.android.orion.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.text.TextUtils;
import android.util.ArrayMap;

import androidx.annotation.NonNull;

import com.android.orion.application.MainApplication;
import com.android.orion.config.Config;
import com.android.orion.data.Period;
import com.android.orion.data.Trend;
import com.android.orion.database.StockPerceptron;
import com.android.orion.database.StockTrend;
import com.android.orion.manager.DatabaseManager;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Utility;

import java.util.ArrayList;

public class StockPerceptronProvider {

	Context mContext = MainApplication.getContext();
	DatabaseManager mDatabaseManager = DatabaseManager.getInstance();
	PowerManager mPowerManager;
	PowerManager.WakeLock mWakeLock;
	HandlerThread mHandlerThread;
	ServiceHandler mHandler;
	ArrayList<StockTrend> mStockTrendList= new ArrayList<>();
	ArrayList<Double> mXArray = new ArrayList<>();
	ArrayList<Double> mYArray = new ArrayList<>();
	StockPerceptron mStockPerceptron;
	ArrayMap<String, StockPerceptron> mTrendMap;
	ArrayMap<Integer, ArrayMap<String, StockPerceptron>> mLevelMap;
	ArrayMap<String, ArrayMap<Integer, ArrayMap<String, StockPerceptron>>> mPeriodMap;

	Logger Log = Logger.getLogger();

	private StockPerceptronProvider() {
		mPowerManager = (PowerManager) mContext
				.getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				Config.TAG + ":" + StockPerceptronProvider.class.getSimpleName());
		mHandlerThread = new HandlerThread(StockPerceptronProvider.class.getSimpleName(),
				Process.THREAD_PRIORITY_BACKGROUND);
		mHandlerThread.start();
		mHandler = new ServiceHandler(mHandlerThread.getLooper());

		mPeriodMap = new ArrayMap<>();
		for (String period : Period.PERIODS) {
			mLevelMap = new ArrayMap<>();
			for (int level = 1; level < Trend.LEVEL_MAX; level++) {
				mTrendMap = new ArrayMap<>();
				for (String trend : Trend.TRENDS) {
					StockPerceptron stockPerceptron = new StockPerceptron(period, level, trend);
					if (!mDatabaseManager.isStockPerceptronExist(stockPerceptron)) {
						stockPerceptron.setCreated(Utility.getCurrentDateTimeString());
						mDatabaseManager.insertStockPerceptron(stockPerceptron);
					} else {
						mDatabaseManager.getStockPerceptron(stockPerceptron);
					}
					mTrendMap.put(trend, stockPerceptron);
				}
				mLevelMap.put(level, mTrendMap);
			}
			mPeriodMap.put(period, mLevelMap);
		}
	}

	public static StockPerceptronProvider getInstance() {
		return StockPerceptronProvider.Holder.INSTANCE;
	}

	public void acquireWakeLock() {
		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire(Config.wakelockTimeout);
//			Log.d("mWakeLock acquired.");
		}
	}

	public void releaseWakeLock() {
		if (mWakeLock.isHeld()) {
			mWakeLock.release();
//			Log.d("mWakeLock released.");
		}
	}

	public void onDestroy() {
		releaseWakeLock();
	}

	public StockPerceptron getStockPerceptron(String period, int level, String trend) {
		return mPeriodMap.get(period).get(level).get(trend);
	}

	public void train(String period, int level, String trend) {
		if (TextUtils.isEmpty(period) || level < 0 || TextUtils.isEmpty(trend)) {
			return;
		}

		String keyString = period + level + trend;
		int what = keyString.hashCode();
		StockPerceptron stockPerceptron = new StockPerceptron(period, level, trend);
		if (mHandler.hasMessages(keyString.hashCode())) {
			Log.d("mHandler.hasMessages " + what + ", skip!");
		} else {
//			Log.d("mHandler.sendMessage " + what);
			Message msg = mHandler.obtainMessage(what, stockPerceptron);
			mHandler.sendMessage(msg);
		}
	}

	private final class ServiceHandler extends Handler {
		ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			try {
				acquireWakeLock();

				if (msg == null) {
					return;
				}

				StockPerceptron stockPerceptron = (StockPerceptron) msg.obj;
				if (stockPerceptron == null) {
					return;
				}

				String period = stockPerceptron.getPeriod();
				int level = stockPerceptron.getLevel();
				String trend = stockPerceptron.getTrend();
				if (TextUtils.isEmpty(period) || level < 0 || TextUtils.isEmpty(trend)) {
					return;
				}

				mDatabaseManager.getStockTrendList(period, level, trend, mStockTrendList);
				if (mStockTrendList.isEmpty()) {
					return;
				}
				mXArray.clear();
				mYArray.clear();
				for (StockTrend stockTrend : mStockTrendList) {
					mXArray.add(stockTrend.getPrice());
					mYArray.add(stockTrend.getNet());
				}
				mStockPerceptron = getStockPerceptron(period, level, trend);
				mStockPerceptron.train(mXArray, mYArray, Config.MAX_ML_TRAIN_TIMES);
				mStockPerceptron.setModified(Utility.getCurrentDateTimeString());
				mDatabaseManager.updateStockPerceptron(mStockPerceptron, mStockPerceptron.getContentValuesPerceptron());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				releaseWakeLock();
			}
		}
	}

	private static class Holder {
		private static final StockPerceptronProvider INSTANCE = new StockPerceptronProvider();
	}
}