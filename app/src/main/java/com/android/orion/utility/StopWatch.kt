package com.android.orion.utility

import java.util.concurrent.TimeUnit

object StopWatch {
    private var mStart: Long = 0
    private var mStop: Long = 0
    private var mInterval = 0.0

    @JvmStatic
    fun start() {
        mStart = System.currentTimeMillis()
    }

    @JvmStatic
    fun stop() {
        mStop = System.currentTimeMillis()
        mInterval = (mStop - mStart) / (1.0 * TimeUnit.SECONDS.toMillis(1));
    }

    @JvmStatic
    fun getInterval(): Double {
        return mInterval
    }
}