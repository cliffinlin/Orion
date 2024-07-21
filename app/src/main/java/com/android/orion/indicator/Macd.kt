package com.android.orion.indicator

import com.android.orion.database.StockData
import com.android.orion.setting.Constant
import com.android.orion.setting.Setting
import java.util.*

class Macd {
    private val AVERAGE5 = 5
    private val AVERAGE10 = 10

    private val FAST = 10 //12;
    private val SLOW = 20 //26;
    private val SIGNAL = 8 //9;

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

    companion object {

        private var mInstance: Macd? = null

        @JvmStatic
        fun getInstance(): Macd? {
            if (mInstance == null) {
                mInstance = Macd()
            }
            return mInstance
        }
    }

    fun getEMAAverage5List(): List<Double> {
        return mEMAAverage5List
    }

    fun getEMAAverage10List(): List<Double> {
        return mEMAAverage10List
    }

    fun getDEAList(): List<Double> {
        return mDEAList
    }

    fun getDIFList(): List<Double> {
        return mDIFList
    }

    fun getHistogramList(): List<Double> {
        return mHistogramList
    }

    fun init(period: String, stockDataList: ArrayList<StockData>) {
        mPeriod = period
        when (mPeriod) {
            Setting.SETTING_PERIOD_MONTH -> {
                mAverage5 = AVERAGE5
                mAverage10 = AVERAGE10
                mFast = 2 * FAST
                mSlow = 2 * SLOW
                mSignal = 2 * SIGNAL
            }
            Setting.SETTING_PERIOD_WEEK -> {
                mAverage5 = AVERAGE5
                mAverage10 = AVERAGE10
                mFast = 2 * FAST
                mSlow = 2 * SLOW
                mSignal = 2 * SIGNAL
            }
            Setting.SETTING_PERIOD_DAY -> {
                mAverage5 = AVERAGE5
                mAverage10 = AVERAGE10
                mFast = FAST
                mSlow = SLOW
                mSignal = SIGNAL
            }
            Setting.SETTING_PERIOD_MIN60 -> {
                mAverage5 = Constant.MIN60_PER_TRADE_DAY * AVERAGE5
                mAverage10 = Constant.MIN60_PER_TRADE_DAY * AVERAGE10
                mFast = Constant.MIN60_PER_TRADE_DAY * FAST
                mSlow = Constant.MIN60_PER_TRADE_DAY * SLOW
                mSignal = Constant.MIN60_PER_TRADE_DAY * SIGNAL
            }
            Setting.SETTING_PERIOD_MIN30 -> {
                mAverage5 = Constant.MIN30_PER_TRADE_DAY * AVERAGE5
                mAverage10 = Constant.MIN30_PER_TRADE_DAY * AVERAGE10
                mFast = Constant.MIN30_PER_TRADE_DAY * FAST
                mSlow = Constant.MIN30_PER_TRADE_DAY * SLOW
                mSignal = Constant.MIN30_PER_TRADE_DAY * SIGNAL
            }
            Setting.SETTING_PERIOD_MIN15 -> {
                mAverage5 = Constant.MIN15_PER_TRADE_DAY * AVERAGE5
                mAverage10 = Constant.MIN15_PER_TRADE_DAY * AVERAGE10
                mFast = Constant.MIN15_PER_TRADE_DAY * FAST
                mSlow = Constant.MIN15_PER_TRADE_DAY * SLOW
                mSignal = Constant.MIN15_PER_TRADE_DAY * SIGNAL
            }
            Setting.SETTING_PERIOD_MIN5 -> {
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

        for (i in 0..stockDataList.size) {
            mPriceList.add(stockDataList[i].close)
        }
    }

    private fun getAlpha(n: Int): Double {
        var result = 0.0
        if (n > 0) {
            result = 2.0 / (n + 1.0)
        }
        return result
    }

    private fun EMA(N: Int, dataList: List<Double>?, emaList: MutableList<Double>?): Double {
        var result = 0.0
        val alpha = getAlpha(N)
        if (dataList == null || dataList.isEmpty() || emaList == null) {
            return result
        }
        for (i in 0..dataList.size) {
            if (i == 0) {
                result = dataList[0]
            } else {
                result = alpha * dataList[i] + (1.0 - alpha) * emaList[i - 1]
            }
            emaList.add(result)
        }
        return result
    }

    fun calculate(period: String, stockDataList: ArrayList<StockData>?) {
        var size = 0
        var dif = 0.0
        var histogram = 0.0

        if (stockDataList == null) {
            return
        }

        init(period, stockDataList)

        size = mPriceList.size
        if (size < 1) {
            return
        }

        mEMAAverage5List.clear()
        mEMAAverage10List.clear()
        mEMAFastList.clear()
        mEMASlowList.clear()
        mDEAList.clear()
        mDIFList.clear()
        mHistogramList.clear()
        EMA(mAverage5, mPriceList, mEMAAverage5List)
        EMA(mAverage10, mPriceList, mEMAAverage10List)
        EMA(mFast, mPriceList, mEMAFastList)
        EMA(mSlow, mPriceList, mEMASlowList)
        var i: Int = 0
        while (i < size) {
            dif = mEMAFastList[i] - mEMASlowList[i]
            mDIFList.add(dif)
            i++
        }
        EMA(mSignal, mDIFList, mDEAList)
        i = 0
        while (i < size) {
            histogram = mDIFList[i] - mDEAList[i]
            mHistogramList.add(histogram)
            i++
        }
    }
}