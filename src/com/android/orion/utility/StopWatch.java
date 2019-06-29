package com.android.orion.utility;

public class StopWatch {
	private long mStart = 0;
	private long mStop = 0;
	private double mInterval = 0;

	public StopWatch() {
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
