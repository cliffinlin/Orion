package com.android.orion.utility;

public class StopWatch {
	long mStart = 0;
	long mStop = 0;
	double mInterval = 0;

	public StopWatch() {
	}

	public void start() {
		mStart = System.currentTimeMillis();
	}

	public void stop() {
		mStop = System.currentTimeMillis();
	}

	public double getInterval() {
		mInterval = (mStop - mStart) / 1000.0;
		return mInterval;
	}
}
