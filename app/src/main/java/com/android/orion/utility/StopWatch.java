package com.android.orion.utility;

import java.util.concurrent.TimeUnit;

public class StopWatch {
    private static long mStart = 0;
    private static long mStop = 0;
    private static double mInterval = 0.0;

    public static void start() {
        mStart = System.currentTimeMillis();
    }

    public static void stop() {
        mStop = System.currentTimeMillis();
        mInterval = (mStop - mStart) / (1.0 * TimeUnit.SECONDS.toMillis(1));
    }

    public static double getInterval() {
        return mInterval;
    }

    public static String getIntervalString() {
        return "elapsed " + mInterval + "s ";
    }
}