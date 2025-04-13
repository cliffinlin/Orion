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
import com.android.orion.database.StockData;
import com.android.orion.database.StockPerceptron;
import com.android.orion.database.StockTrend;
import com.android.orion.manager.DatabaseManager;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class StockPerceptronProvider {

	public static final int MSG_TRAIN = 1000;

	Context mContext = MainApplication.getContext();
	DatabaseManager mDatabaseManager = DatabaseManager.getInstance();
	PowerManager mPowerManager;
	PowerManager.WakeLock mWakeLock;
	HandlerThread mHandlerThread;
	ServiceHandler mHandler;
	ArrayList<StockTrend> mStockTrendList = new ArrayList<>();
	ArrayList<Double> mXArray = new ArrayList<>();
	ArrayList<Double> mYArray = new ArrayList<>();
	StockPerceptron mStockPerceptron;
	ArrayMap<Integer, StockPerceptron> mLevelMap;
	ArrayMap<String, ArrayMap<Integer, StockPerceptron>> mPeriodMap;

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
			for (int level = 1; level < StockTrend.LEVEL_MAX; level++) {
				StockPerceptron stockPerceptron = new StockPerceptron(period, level);
				if (!mDatabaseManager.isStockPerceptronExist(stockPerceptron)) {
					stockPerceptron.setCreated(Utility.getCurrentDateTimeString());
					mDatabaseManager.insertStockPerceptron(stockPerceptron);
				} else {
					mDatabaseManager.getStockPerceptron(stockPerceptron);
				}
				mLevelMap.put(level, stockPerceptron);
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

	public StockPerceptron getStockPerceptron(String period, int level) {
		return mPeriodMap.get(period).get(level);
	}

	public void train(String period, int level, ArrayList<StockTrend> stockTrendList) {
		if (TextUtils.isEmpty(period) || period.equals(Period.MONTH) || level < StockTrend.LEVEL_DRAW) {
			return;
		}

		if (stockTrendList == null || stockTrendList.size() == 1) {
			return;
		}

		mStockTrendList = new ArrayList<>(Arrays.asList(new StockTrend[stockTrendList.size()]));
		Collections.copy(mStockTrendList, stockTrendList);
		StockPerceptron stockPerceptron = new StockPerceptron(period, level);
		if (mHandler.hasMessages(MSG_TRAIN)) {
			Log.d("has message MSG_TRAIN, remove message MSG_TRAIN first!");
			mHandler.removeMessages(MSG_TRAIN);
		}

		Message msg = mHandler.obtainMessage(MSG_TRAIN, stockPerceptron);
		mHandler.sendMessage(msg);
	}

	public void train(String period, int level, String type) {
		if (TextUtils.isEmpty(period) || level < 0 || TextUtils.isEmpty(type)) {
			return;
		}

		String keyString = period + level + type;
		int what = keyString.hashCode();
		StockPerceptron stockPerceptron = new StockPerceptron(period, level, type);
		if (mHandler.hasMessages(keyString.hashCode())) {
//			Log.d("mHandler.hasMessages " + what + ", skip!");
		} else {
//			Log.d("mHandler.sendMessage " + what);
			Message msg = mHandler.obtainMessage(what, stockPerceptron);
			mHandler.sendMessage(msg);
		}
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
				if (TextUtils.isEmpty(period) || level < 0) {
					return;
				}

				if (mStockTrendList.isEmpty()) {
					return;
				}
				mXArray.clear();
				mYArray.clear();
				for (int i = 0; i < mStockTrendList.size() - 1; i++) {
					StockTrend stockTrend = mStockTrendList.get(i);
					mXArray.add(stockTrend.getNet1());
					mYArray.add(stockTrend.getNet());
				}
				mStockPerceptron = getStockPerceptron(period, level);
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
}