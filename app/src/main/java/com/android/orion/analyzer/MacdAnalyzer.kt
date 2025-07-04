package com.android.orion.analyzer

import com.android.orion.data.Period
import com.android.orion.database.StockData
import com.android.orion.database.StockTrend
import com.android.orion.setting.Constant

object MacdAnalyzer {

    private var mPeriod = ""
    private var mAverage5 = 0
    private var mAverage10 = 0
    private var mFast = 0
    private var mSlow = 0
    private var mSignal = 0

    private var mPriceList: MutableList<Double> = ArrayList()
    private var mEMAAverage5List: MutableList<Double> = ArrayList()
    private var mEMAAverage10List: MutableList<Double> = ArrayList()
    private var mEMAFastList: MutableList<Double> = ArrayList()
    private var mEMASlowList: MutableList<Double> = ArrayList()
    private var mDEAList: MutableList<Double> = ArrayList()
    private var mDIFList: MutableList<Double> = ArrayList()
    private var mHistogramList: MutableList<Double> = ArrayList()
    private var mVelocityList: MutableList<Double> = ArrayList()
    private var mAccelerationList: MutableList<Double> = ArrayList()

    private const val AVERAGE5 = 5
    private const val AVERAGE10 = 10

    private const val FAST = 10 //12;
    private const val SLOW = 20 //26;
    private const val SIGNAL = 8 //9;

    @JvmStatic
    fun getEMAAverage5List(): List<Double> {
        return mEMAAverage5List
    }

    @JvmStatic
    fun getEMAAverage10List(): List<Double> {
        return mEMAAverage10List
    }

    @JvmStatic
    fun getDEAList(): List<Double> {
        return mDEAList
    }

    @JvmStatic
    fun getDIFList(): List<Double> {
        return mDIFList
    }

    @JvmStatic
    fun getHistogramList(): List<Double> {
        return mHistogramList
    }

    @JvmStatic
    fun getVelocityList(): List<Double> {
        return mVelocityList
    }

    @JvmStatic
    fun getAccelerationList(): List<Double> {
        return mAccelerationList
    }

    fun init(period: String, stockDataList: ArrayList<StockData>) {
        mPeriod = period
        when (mPeriod) {
            Period.YEAR -> {
                mAverage5 = AVERAGE5
                mAverage10 = AVERAGE10
                mFast = FAST
                mSlow = SLOW
                mSignal = SIGNAL
            }
            Period.QUARTER -> {
                mAverage5 = AVERAGE5
                mAverage10 = AVERAGE10
                mFast = FAST
                mSlow = SLOW
                mSignal = SIGNAL
            }
            Period.MONTH -> {
                mAverage5 = AVERAGE5
                mAverage10 = AVERAGE10
                mFast = FAST
                mSlow = SLOW
                mSignal = SIGNAL
            }
            Period.WEEK -> {
                mAverage5 = AVERAGE5
                mAverage10 = AVERAGE10
                mFast = FAST
                mSlow = SLOW
                mSignal = SIGNAL
            }
            Period.DAY -> {
                mAverage5 = AVERAGE5
                mAverage10 = AVERAGE10
                mFast = FAST
                mSlow = SLOW
                mSignal = SIGNAL
            }
            Period.MIN60 -> {
                mAverage5 = Constant.MIN60_PER_TRADE_DAY * AVERAGE5
                mAverage10 = Constant.MIN60_PER_TRADE_DAY * AVERAGE10
                mFast = Constant.MIN60_PER_TRADE_DAY * FAST
                mSlow = Constant.MIN60_PER_TRADE_DAY * SLOW
                mSignal = Constant.MIN60_PER_TRADE_DAY * SIGNAL
            }
            Period.MIN30 -> {
                mAverage5 = Constant.MIN30_PER_TRADE_DAY * AVERAGE5
                mAverage10 = Constant.MIN30_PER_TRADE_DAY * AVERAGE10
                mFast = Constant.MIN30_PER_TRADE_DAY * FAST
                mSlow = Constant.MIN30_PER_TRADE_DAY * SLOW
                mSignal = Constant.MIN30_PER_TRADE_DAY * SIGNAL
            }
            Period.MIN15 -> {
                mAverage5 = Constant.MIN15_PER_TRADE_DAY * AVERAGE5
                mAverage10 = Constant.MIN15_PER_TRADE_DAY * AVERAGE10
                mFast = Constant.MIN15_PER_TRADE_DAY * FAST
                mSlow = Constant.MIN15_PER_TRADE_DAY * SLOW
                mSignal = Constant.MIN15_PER_TRADE_DAY * SIGNAL
            }
            Period.MIN5 -> {
                mAverage5 = Constant.MIN5_PER_TRADE_DAY * AVERAGE5
                mAverage10 = Constant.MIN5_PER_TRADE_DAY * AVERAGE10
                mFast = Constant.MIN5_PER_TRADE_DAY * FAST
                mSlow = Constant.MIN5_PER_TRADE_DAY * SLOW
                mSignal = Constant.MIN5_PER_TRADE_DAY * SIGNAL
            }
        }
        mPriceList.clear()
        mEMAAverage5List.clear()
        mEMAAverage10List.clear()
        mEMAFastList.clear()
        mEMASlowList.clear()
        mDEAList.clear()
        mDIFList.clear()
        mHistogramList.clear()
        mVelocityList.clear()
        mAccelerationList.clear()
        for (i in 0 until stockDataList.size) {
            mPriceList.add(stockDataList[i].candle.close)
        }
    }

    private fun getAlpha(n: Int): Double {
        var result = 0.0
        if (n > 0) {
            result = 2.0 / (n + 1.0)
        }
        return result
    }

    private fun EMA(N: Int, dataList: List<Double>, emaList: MutableList<Double>): Double {
        var result = 0.0
        val alpha = getAlpha(N)

        if (dataList.isEmpty()) {
            return result
        }

        for (i in 0 until dataList.size) {
            result = if (i == 0) {
                dataList[0]
            } else {
                alpha * dataList[i] + (1.0 - alpha) * emaList[i - 1]
            }
            emaList.add(result)
        }

        return result
    }

    @JvmStatic
    fun calculate(period: String, stockDataList: ArrayList<StockData>) {
        init(period, stockDataList)

        if (mPriceList.size < StockTrend.VERTEX_SIZE) {
            return
        }

        mEMAAverage5List.clear()
        mEMAAverage10List.clear()
        mEMAFastList.clear()
        mEMASlowList.clear()
        mDEAList.clear()
        mDIFList.clear()
        mHistogramList.clear()
        mVelocityList.clear()
        mAccelerationList.clear()

        EMA(mAverage5, mPriceList, mEMAAverage5List)
        EMA(mAverage10, mPriceList, mEMAAverage10List)
        EMA(mFast, mPriceList, mEMAFastList)
        EMA(mSlow, mPriceList, mEMASlowList)

        var i = 0
        while (i < mPriceList.size) {
            mDIFList.add(mEMAFastList[i] - mEMASlowList[i])
            i++
        }
        EMA(mSignal, mDIFList, mDEAList)

        i = 0
        while (i < mPriceList.size) {
            mHistogramList.add(mDIFList[i] - mDEAList[i])
            i++
        }

        i = 0
        mVelocityList.add(0.0)
        i++
        while (i < mPriceList.size) {
            mVelocityList.add((mHistogramList[i] - mHistogramList[i - 1]) * mSignal / SIGNAL)
            i++
        }

        i = 0
        mAccelerationList.add(0.0)
        i++
        while (i < mPriceList.size) {
            mAccelerationList.add(mVelocityList[i] - mVelocityList[i - 1])
            i++
        }
    }
}