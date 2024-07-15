package com.android.orion.utility;

public class StopWatch {
	private long mStart = 0;
	private long mStop = 0;
	private double mInterval = 0;

	private static StopWatch mInstance;

	public static synchronized StopWatch getInstance() {
		if (mInstance == null) {
			mInstance = new StopWatch();
		}
		return mInstance;
	}

	private StopWatch() {
	}

	public void start() {
		mStart = System.currentTimeMillis();
	}

	public void stop() {
		mStop = System.currentTimeMillis();
		mInterval = (mStop - mStart) / 1000.0;
	}

	public double getInterval() {
		return mInterval;
	}
}
