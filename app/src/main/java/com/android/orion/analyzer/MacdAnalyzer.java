package com.android.orion.analyzer;

import com.android.orion.data.Period;
import com.android.orion.database.StockData;
import com.android.orion.database.StockTrend;
import com.android.orion.setting.Constant;
import com.android.orion.utility.Logger;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.ArrayList;
import java.util.List;

public class MACDAnalyzer {

    private static String mPeriod;
    private static int mAverage5 = 0;
    private static int mAverage10 = 0;
    private static int mFast = 0;
    private static int mSlow = 0;
    private static int mSignal = 0;

    private static final List<Double> mPriceList = new ArrayList<>();
    private static final List<Double> mEMAAverage5List = new ArrayList<>();
    private static final List<Double> mEMAAverage10List = new ArrayList<>();
    private static final List<Double> mEMAFastList = new ArrayList<>();
    private static final List<Double> mEMASlowList = new ArrayList<>();
    private static final List<Double> mDEAList = new ArrayList<>();
    private static final List<Double> mDIFList = new ArrayList<>();
    private static final List<Double> mHistogramList = new ArrayList<>();

    static Logger Log = Logger.getLogger();

    private static final int AVERAGE5 = 5;
    private static final int AVERAGE10 = 10;

    private static final int FAST = 10; //12;
    private static final int SLOW = 20; //26;
    private static final int SIGNAL = 8; //9;

    public static List<Double> getEMAAverage5List() {
        return mEMAAverage5List;
    }

    public static List<Double> getEMAAverage10List() {
        return mEMAAverage10List;
    }

    public static List<Double> getDEAList() {
        return mDEAList;
    }

    public static List<Double> getDIFList() {
        return mDIFList;
    }

    public static List<Double> getHistogramList() {
        return mHistogramList;
    }

    public static void init(String period, ArrayList<StockData> stockDataList) {
        if (stockDataList == null || stockDataList.isEmpty()) {
            return;
        }
        mPeriod = period;
        switch (period) {
            case Period.YEAR:
            case Period.MONTH6:
            case Period.QUARTER:
            case Period.MONTH2:
            case Period.MONTH:
            case Period.WEEK:
            case Period.DAY:
                mAverage5 = AVERAGE5;
                mAverage10 = AVERAGE10;
                mFast = FAST;
                mSlow = SLOW;
                mSignal = SIGNAL;
                break;
            case Period.MIN60:
                mAverage5 = Constant.MIN60_PER_TRADE_DAY * AVERAGE5;
                mAverage10 = Constant.MIN60_PER_TRADE_DAY * AVERAGE10;
                mFast = Constant.MIN60_PER_TRADE_DAY * FAST;
                mSlow = Constant.MIN60_PER_TRADE_DAY * SLOW;
                mSignal = Constant.MIN60_PER_TRADE_DAY * SIGNAL;
                break;
            case Period.MIN30:
                mAverage5 = Constant.MIN30_PER_TRADE_DAY * AVERAGE5;
                mAverage10 = Constant.MIN30_PER_TRADE_DAY * AVERAGE10;
                mFast = Constant.MIN30_PER_TRADE_DAY * FAST;
                mSlow = Constant.MIN30_PER_TRADE_DAY * SLOW;
                mSignal = Constant.MIN30_PER_TRADE_DAY * SIGNAL;
                break;
            case Period.MIN15:
                mAverage5 = Constant.MIN15_PER_TRADE_DAY * AVERAGE5;
                mAverage10 = Constant.MIN15_PER_TRADE_DAY * AVERAGE10;
                mFast = Constant.MIN15_PER_TRADE_DAY * FAST;
                mSlow = Constant.MIN15_PER_TRADE_DAY * SLOW;
                mSignal = Constant.MIN15_PER_TRADE_DAY * SIGNAL;
                break;
            case Period.MIN5:
                mAverage5 = Constant.MIN5_PER_TRADE_DAY * AVERAGE5;
                mAverage10 = Constant.MIN5_PER_TRADE_DAY * AVERAGE10;
                mFast = Constant.MIN5_PER_TRADE_DAY * FAST;
                mSlow = Constant.MIN5_PER_TRADE_DAY * SLOW;
                mSignal = Constant.MIN5_PER_TRADE_DAY * SIGNAL;
                break;
        }
        mPriceList.clear();
        mEMAAverage5List.clear();
        mEMAAverage10List.clear();
        mEMAFastList.clear();
        mEMASlowList.clear();
        mDEAList.clear();
        mDIFList.clear();
        mHistogramList.clear();
        for (int i = 0; i < stockDataList.size(); i++) {
            mPriceList.add(stockDataList.get(i).getCandle().getClose());
        }
    }

    private static double getAlpha(int n) {
        double result = 0.0;
        if (n > 0) {
            result = 2.0 / (n + 1.0);
        }
        return result;
    }

    private static void EMA(int N, List<Double> dataList, List<Double> emaList) {
        double alpha = getAlpha(N);

        if (dataList == null || dataList.isEmpty() || emaList == null) {
            return;
        }

        double result = 0.0;
        for (int i = 0; i < dataList.size(); i++) {
            if (i == 0) {
                result = dataList.get(0);
            } else {
                result = alpha * dataList.get(i) + (1.0 - alpha) * emaList.get(i - 1);
            }
            emaList.add(result);
        }
    }

    public static void calculateMACD(String period, ArrayList<StockData> stockDataList) {
        init(period, stockDataList);

        if (mPriceList.size() < StockTrend.VERTEX_SIZE) {
            return;
        }

        mEMAAverage5List.clear();
        mEMAAverage10List.clear();
        mEMAFastList.clear();
        mEMASlowList.clear();
        mDEAList.clear();
        mDIFList.clear();
        mHistogramList.clear();

        EMA(mAverage5, mPriceList, mEMAAverage5List);
        EMA(mAverage10, mPriceList, mEMAAverage10List);
        EMA(mFast, mPriceList, mEMAFastList);
        EMA(mSlow, mPriceList, mEMASlowList);

        int i = 0;
        while (i < mPriceList.size()) {
            mDIFList.add(mEMAFastList.get(i) - mEMASlowList.get(i));
            i++;
        }
        EMA(mSignal, mDIFList, mDEAList);

        i = 0;
        while (i < mPriceList.size()) {
            mHistogramList.add(mDIFList.get(i) - mDEAList.get(i));
            i++;
        }
    }

    /**
     * 对MACD线进行傅立叶变换分析
     */
    public static void analyzeMACDWithFourier() {
        // 分析DIF线
        List<PeriodAmplitude> difSpectrum = performFourierAnalysis(mDIFList, "DIF");
        printDominantPeriods(difSpectrum, "DIF");

        // 分析DEA线
        List<PeriodAmplitude> deaSpectrum = performFourierAnalysis(mDEAList, "DEA");
        printDominantPeriods(deaSpectrum, "DEA");

        List<PeriodAmplitude> histogramSpectrum = performFourierAnalysis(mHistogramList, "Histogram");
        printDominantPeriods(histogramSpectrum, "Histogram");

        // 比较分析
        compareSpectrums(difSpectrum, deaSpectrum);
    }

    /**
     * 执行傅立叶变换分析
     */
    private static List<PeriodAmplitude> performFourierAnalysis(List<Double> data, String lineName) {
        if (data == null) {
            Log.d(lineName + " data is null");
            return new ArrayList<>();
        }

        // 过滤掉NaN值
        List<Double> cleanData = new ArrayList<>();
        for (Double value : data) {
            if (value != null && !value.isNaN()) {
                cleanData.add(value);
            }
        }

        if (cleanData.size() < 2) {
            Log.d(lineName + "数据不足，无法进行傅立叶分析");
            return new ArrayList<>();
        }

        int n = cleanData.size();

        // 找到最接近的2的幂次方长度（FFT要求）
        int fftSize = findNextPowerOfTwo(n);

        // 准备FFT输入数据
        double[] fftInput = new double[fftSize];
        for (int i = 0; i < n; i++) {
            fftInput[i] = cleanData.get(i);
        }
        // 剩余部分补零

        // 执行FFT
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] fftResult = fft.transform(fftInput, TransformType.FORWARD);

        // 计算振幅和周期
        List<PeriodAmplitude> spectrum = calculateSpectrum(fftResult, n);

        Log.d(lineName + "数据点: " + n + ", FFT大小: " + fftSize);

        return spectrum;
    }

    /**
     * 计算频谱（周期和振幅）
     */
    private static List<PeriodAmplitude> calculateSpectrum(Complex[] fftResult, int dataLength) {
        List<PeriodAmplitude> spectrum = new ArrayList<>();
        if (fftResult == null) {
            return spectrum;
        }

        // 只取前一半结果（对称的）
        int halfLength = fftResult.length / 2;

        for (int k = 1; k < halfLength; k++) { // 从1开始，跳过直流分量(k=0)
            double amplitude = fftResult[k].abs() / dataLength * 2; // 计算振幅
            double period = (double) dataLength / k; // 计算周期（天数）

            if (period > 1 && period <= dataLength / 2) { // 过滤无效周期
                double days = 0;
                switch (mPeriod) {
                    case Period.YEAR:
                    case Period.MONTH6:
                    case Period.QUARTER:
                    case Period.MONTH2:
                    case Period.MONTH:
                    case Period.WEEK:
                        //TODO
                        break;
                    case Period.DAY:
                        days = period;
                        break;
                    case Period.MIN60:
                        days = period / Constant.MIN60_PER_TRADE_DAY;
                        break;
                    case Period.MIN30:
                        days = period / Constant.MIN30_PER_TRADE_DAY;
                        break;
                    case Period.MIN15:
                        days = period / Constant.MIN15_PER_TRADE_DAY;
                        break;
                    case Period.MIN5:
                        days = period / Constant.MIN5_PER_TRADE_DAY;
                        break;
                    default:
                        break;
                }
                spectrum.add(new PeriodAmplitude(days, amplitude));
            }
        }

        // 按振幅降序排序
        spectrum.sort((a, b) -> Double.compare(b.amplitude, a.amplitude));

        return spectrum;
    }

    /**
     * 打印主要周期
     */
    private static void printDominantPeriods(List<PeriodAmplitude> spectrum, String lineName) {
        if (spectrum == null) {
            return;
        }
        Log.d("排名 | 周期(天) | 振幅强度");
        Log.d("----|----------|----------");

        int count = Math.min(10, spectrum.size()); // 显示前10个主要周期
        for (int i = 0; i < count; i++) {
            PeriodAmplitude pa = spectrum.get(i);
            Log.d(String.format("%2d  | %8.2f | %8.4f\n",
                    i + 1, pa.period, pa.amplitude));
        }
    }

    /**
     * 比较DIF和DEA的频谱
     */
    private static void compareSpectrums(List<PeriodAmplitude> difSpectrum,
                                         List<PeriodAmplitude> deaSpectrum) {
        if (difSpectrum == null || difSpectrum.isEmpty() || deaSpectrum == null || deaSpectrum.isEmpty()) {
            Log.d("数据不足，无法比较");
            return;
        }

        // 获取各自的前5个主要周期
        List<Double> difTopPeriods = getTopPeriods(difSpectrum, 5);
        List<Double> deaTopPeriods = getTopPeriods(deaSpectrum, 5);

        Log.d("DIF线主要周期: " + difTopPeriods);
        Log.d("DEA线主要周期: " + deaTopPeriods);

        double difAvgPeriod = calculateAveragePeriod(difTopPeriods);
        double deaAvgPeriod = calculateAveragePeriod(deaTopPeriods);

        Log.d(String.format("DIF平均周期: %.2f 天\n", difAvgPeriod));
        Log.d(String.format("DEA平均周期: %.2f 天\n", deaAvgPeriod));

        // 寻找共同的主要周期
        List<Double> commonPeriods = findCommonPeriods(difTopPeriods, deaTopPeriods, 3.0);
        if (!commonPeriods.isEmpty()) {
            Log.d("共同主要周期: " + commonPeriods);
        }
    }

    /**
     * 工具方法：获取前N个主要周期
     */
    private static List<Double> getTopPeriods(List<PeriodAmplitude> spectrum, int n) {
        List<Double> periods = new ArrayList<>();
        if (spectrum == null) {
            return periods;
        }
        int count = Math.min(n, spectrum.size());
        for (int i = 0; i < count; i++) {
            periods.add(spectrum.get(i).period);
        }
        return periods;
    }

    /**
     * 计算平均周期
     */
    private static double calculateAveragePeriod(List<Double> periods) {
        if (periods == null || periods.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        for (Double period : periods) {
            sum += period;
        }
        return sum / periods.size();
    }

    /**
     * 寻找共同的周期（考虑误差）
     */
    private static List<Double> findCommonPeriods(List<Double> periods1,
                                                  List<Double> periods2,
                                                  double tolerance) {
        List<Double> common = new ArrayList<>();
        if (periods1 == null || periods2 == null) {
            return common;
        }
        for (Double p1 : periods1) {
            for (Double p2 : periods2) {
                if (Math.abs(p1 - p2) <= tolerance) {
                    common.add((p1 + p2) / 2);
                }
            }
        }
        return common;
    }

    /**
     * 找到大于等于n的最小的2的幂次方
     */
    private static int findNextPowerOfTwo(int n) {
        int power = 1;
        while (power < n) {
            power <<= 1;
        }
        return power;
    }

    /**
     * 内部类：用于存储周期和振幅
     */
    private static class PeriodAmplitude {
        double period;
        double amplitude;

        PeriodAmplitude(double period, double amplitude) {
            this.period = period;
            this.amplitude = amplitude;
        }
    }
}