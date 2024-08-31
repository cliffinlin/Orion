package com.android.orion.utility

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
        mInterval = (mStop - mStart) / 1000.0
    }

    @JvmStatic
    fun getInterval(): Double {
        return mInterval
    }
}