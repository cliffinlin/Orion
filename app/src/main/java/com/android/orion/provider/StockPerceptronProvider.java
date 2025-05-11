package com.android.orion.provider;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.android.orion.application.MainApplication;
import com.android.orion.config.Config;
import com.android.orion.data.Period;
import com.android.orion.database.StockPerceptron;
import com.android.orion.database.StockTrend;
import com.android.orion.manager.DatabaseManager;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.Collections;

public class StockPerceptronProvider {

	public static final int MSG_TRAIN_ALL_IN_ONE = 999999 + 1;

	Context mContext = MainApplication.getContext();
	DatabaseManager mDatabaseManager = DatabaseManager.getInstance();
	PowerManager mPowerManager;
	PowerManager.WakeLock mWakeLock;
	HandlerThread mHandlerThread;
	ServiceHandler mHandler;
	ArrayList<Double> mXArray = new ArrayList<>();
	ArrayList<Double> mYArray = new ArrayList<>();
	StockPerceptron mStockPerceptron = new StockPerceptron();
	ArrayMap<String, StockPerceptron> mTypeMap;
	ArrayMap<Integer, ArrayMap<String, StockPerceptron>> mLevelMap;
	ArrayMap<String, ArrayMap<Integer, ArrayMap<String, StockPerceptron>>> mPeriodMap;

	Logger Log = Logger.getLogger();

	private StockPerceptronProvider() {
		mPowerManager = (PowerManager) mContext
				.getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				Config.TAG + ":" + StockPerceptronProvider.class.getSimpleName());
		mHandlerThread = new HandlerThread(StockPerceptronProvider.class.getSimpleName(),
				Process.THREAD_PRIORITY_LOWEST);
		mHandlerThread.start();
		mHandler = new ServiceHandler(mHandlerThread.getLooper());

		mPeriodMap = new ArrayMap<>();
		for (String period : Period.PERIODS) {
			mLevelMap = new ArrayMap<>();
			for (int level = 1; level < StockTrend.LEVELS.length; level++) {
				mTypeMap = new ArrayMap<>();
				for (String type : StockTrend.TYPES) {
					StockPerceptron stockPerceptron = new StockPerceptron(period, level, type);
					if (!mDatabaseManager.isStockPerceptronExist(stockPerceptron)) {
						stockPerceptron.setCreated(Utility.getCurrentDateTimeString());
						mDatabaseManager.insertStockPerceptron(stockPerceptron);
					} else {
						mDatabaseManager.getStockPerceptron(stockPerceptron);
					}
					mTypeMap.put(type, stockPerceptron);
				}
				mLevelMap.put(level, mTypeMap);
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
		}
	}

	public void releaseWakeLock() {
		if (mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}

	public void onDestroy() {
		releaseWakeLock();
	}

	public StockPerceptron getStockPerceptron(String period, int level, String type) {
		return mPeriodMap.get(period).get(level).get(type);
	}

	public void train(ArrayMap<Double, Double> stockTrendNetMap) {
		if (stockTrendNetMap == null || stockTrendNetMap.size() == 0) {
			return;
		}

		if (mHandler.hasMessages(MSG_TRAIN_ALL_IN_ONE)) {
			mHandler.removeMessages(MSG_TRAIN_ALL_IN_ONE);
		}
		Message msg = mHandler.obtainMessage(MSG_TRAIN_ALL_IN_ONE, stockTrendNetMap);
		mHandler.sendMessage(msg);
	}

	public void train(String period, int level, String type) {
		if (TextUtils.isEmpty(period) || level < 0 || TextUtils.isEmpty(type)) {
			return;
		}

		String keyString = period + level + type;
		int what = keyString.hashCode();
		StockPerceptron stockPerceptron = new StockPerceptron(period, level, type);
		if (mHandler.hasMessages(what)) {
			mHandler.removeMessages(what);
		}
		Message msg = mHandler.obtainMessage(what, stockPerceptron);
		mHandler.sendMessage(msg);
	}

	private static class Holder {
		private static final StockPerceptronProvider INSTANCE = new StockPerceptronProvider();
	}

	private final class ServiceHandler extends Handler {
		ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			try {
				acquireWakeLock();

				if (msg == null) {
					return;
				}

				switch (msg.what) {
					case MSG_TRAIN_ALL_IN_ONE: {
						ArrayMap<Double, Double> stockTrendNetMap = (ArrayMap<Double, Double>) msg.obj;
						if (stockTrendNetMap == null) {
							return;
						}

						mXArray = new ArrayList<>(stockTrendNetMap.keySet());
						Collections.sort(mXArray);
						mYArray = new ArrayList<>();
						for (Double key : mXArray) {
							mYArray.add(stockTrendNetMap.get(key));
						}
						mStockPerceptron.train(mXArray, mYArray, Config.MAX_ML_TRAIN_TIMES);
						mStockPerceptron.setModified(Utility.getCurrentDateTimeString());
						Log.d("MSG_TRAIN_ALL_IN_ONE ---------->" + mStockPerceptron.toLogString());
						break;
					}

					default: {
						StockPerceptron stockPerceptron = (StockPerceptron) msg.obj;
						if (stockPerceptron == null) {
							return;
						}

						String period = stockPerceptron.getPeriod();
						int level = stockPerceptron.getLevel();
						String type = stockPerceptron.getType();
						if (TextUtils.isEmpty(period) || level < 0 || TextUtils.isEmpty(type)) {
							return;
						}

						mStockPerceptron = getStockPerceptron(period, level, type);
						if (mStockPerceptron == null) {
							return;
						}

						mXArray = new ArrayList<>(mStockPerceptron.getNetMap().keySet());
						Collections.sort(mXArray);
						mYArray = new ArrayList<>();
						for (Double key : mXArray) {
							mYArray.add(mStockPerceptron.getNetMap().get(key));
						}

						mStockPerceptron.train(mXArray, mYArray, Config.MAX_ML_TRAIN_TIMES);
						mStockPerceptron.setModified(Utility.getCurrentDateTimeString());
						mDatabaseManager.updateStockPerceptron(mStockPerceptron, mStockPerceptron.getContentValuesPerceptron());
//						Log.d("default ---------->" + mStockPerceptron.toLogString());
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				releaseWakeLock();
			}
		}
	}
}