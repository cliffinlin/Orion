package com.android.orion.utility

class StopWatch {
    private var mStart: Long = 0
    private var mStop: Long = 0
    private var mInterval = 0.0

    companion object {
        private var mInstance: StopWatch = StopWatch()

        @JvmStatic
        fun getInstance(): StopWatch {
            return mInstance
        }
    }

    fun start() {
        mStart = System.currentTimeMillis()
    }

    fun stop() {
        mStop = System.currentTimeMillis()
        mInterval = (mStop - mStart) / 1000.0
    }

    fun getInterval(): Double {
        return mInterval
    }
}