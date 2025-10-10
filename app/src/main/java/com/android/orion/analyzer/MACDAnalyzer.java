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
import java.util.Collections;
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
    private static final List<Double> mComponentList = new ArrayList<>();
    private static PolarComponent mPolarComponent;

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

    public static List<Double> getComponentList() {
        return mComponentList;
    }

    public static PolarComponent getPolarComponent() {
        return mPolarComponent;
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
        mComponentList.clear();
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
        mComponentList.clear();

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
     * 对MACD线进行傅立叶变换分析并重建第一主成分
     */
    public static void analyzeMACDWithFourier() {
        List<PeriodAmplitude> histogramSpectrum = performFourierAnalysis(mHistogramList, "Histogram");
        printDominantPeriods(histogramSpectrum, "Histogram");

        // 重建第一主成分
        List<Double> reconstructedData = reconstructFirstComponent(mHistogramList, histogramSpectrum);
        mComponentList.clear();
        mComponentList.addAll(reconstructedData);

//        Log.d("傅立叶重建完成，重建数据点数: " + reconstructedData.size());
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

//        Log.d(lineName + "数据点: " + n + ", FFT大小: " + fftSize);

        return spectrum;
    }

    /**
     * 重建第一主成分（振幅最大的频率成分）
     */
    private static List<Double> reconstructFirstComponent(List<Double> originalData, List<PeriodAmplitude> spectrum) {
        if (originalData == null || spectrum == null || spectrum.isEmpty()) {
            return new ArrayList<>();
        }

        // 过滤掉NaN值
        List<Double> cleanData = new ArrayList<>();
        for (Double value : originalData) {
            if (value != null && !value.isNaN()) {
                cleanData.add(value);
            }
        }

        if (cleanData.size() < 2) {
            return new ArrayList<>();
        }

        int n = cleanData.size();
        int fftSize = findNextPowerOfTwo(n);

        // 准备FFT输入数据
        double[] fftInput = new double[fftSize];
        for (int i = 0; i < n; i++) {
            fftInput[i] = cleanData.get(i);
        }

        // 执行FFT
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] fftResult = fft.transform(fftInput, TransformType.FORWARD);

        // 直接从频谱中获取第一主成分对应的频率索引
        int dominantIndex = findFrequencyIndexFromSpectrum(fftResult, n, spectrum.get(0));

        // 获取该频率对应的复数
        Complex dominantComplex = fftResult[dominantIndex];
        double amplitude = dominantComplex.abs() / n * 2;
        double phase = dominantComplex.getArgument();
        double phaseDegrees = Math.toDegrees(phase);
        if (phaseDegrees < 0) phaseDegrees += 360;

        Log.d("第一主成分复数信息 - " +
                "实部: " + String.format("%.6f", dominantComplex.getReal()) +
                ", 虚部: " + String.format("%.6f", dominantComplex.getImaginary()) +
                ", 振幅: " + String.format("%.6f", amplitude) +
                ", 相位: " + String.format("%.2f", phaseDegrees) + "°");

        double dominantPeriodInDataPoints = (double) n / dominantIndex;
        double dominantPeriodInDays = convertPeriodToDays(dominantPeriodInDataPoints, n);

        Log.d("第一主成分频率索引: " + dominantIndex +
                ", 周期(数据点): " + dominantPeriodInDataPoints +
                ", 周期(天): " + dominantPeriodInDays);

        // 创建只包含第一主成分的频谱
        Complex[] singleComponentSpectrum = createSingleComponentSpectrum(fftResult, dominantIndex);

        // 执行逆傅立叶变换
        Complex[] reconstructedComplex = fft.transform(singleComponentSpectrum, TransformType.INVERSE);

        // 转换为实数列表
        List<Double> reconstructedData = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            // 取实部作为重建值
            reconstructedData.add(reconstructedComplex[i].getReal());
        }

        // 计算并存储极坐标信息
        double lastPointValue = reconstructedData.isEmpty() ? 0.0 : reconstructedData.get(reconstructedData.size() - 1);
        double frequency = 1.0 / dominantPeriodInDays;
        mPolarComponent = new PolarComponent(amplitude, dominantPeriodInDays, frequency,
                phase, phaseDegrees, dominantIndex, lastPointValue);

        Log.d("第一主成分极坐标 - " +
                "振幅: " + String.format("%.6f", amplitude) +
                ", 周期: " + String.format("%.2f", dominantPeriodInDays) + "天" +
                ", 频率: " + String.format("%.4f", frequency) + "/天" +
                ", 相位: " + String.format("%.2f", phaseDegrees) + "°" +
                ", 最后点: " + String.format("%.6f", lastPointValue));

        return reconstructedData;
    }

    /**
     * 根据频谱中的周期信息找到对应的频率索引
     */
    private static int findFrequencyIndexFromSpectrum(Complex[] fftResult, int dataLength, PeriodAmplitude dominantPeriod) {
        // 将天数转换回数据点数量
        double targetPeriodInDataPoints = convertDaysToDataPoints(dominantPeriod.period, dataLength);

        // 计算对应的频率索引
        int dominantIndex = (int) Math.round(dataLength / targetPeriodInDataPoints);

        // 确保索引在有效范围内
        int halfLength = fftResult.length / 2;
        if (dominantIndex < 1) dominantIndex = 1;
        if (dominantIndex >= halfLength) dominantIndex = halfLength - 1;

//        Log.d("根据周期 " + dominantPeriod.period + " 天找到频率索引: " + dominantIndex +
//                ", 对应周期(数据点): " + (double)dataLength/dominantIndex);

        return dominantIndex;
    }

    /**
     * 将天数转换为数据点数量
     */
    private static double convertDaysToDataPoints(double days, int totalDataPoints) {
        switch (mPeriod) {
            case Period.YEAR:
            case Period.MONTH6:
            case Period.QUARTER:
            case Period.MONTH2:
            case Period.MONTH:
            case Period.WEEK:
            case Period.DAY:
                return days;

            case Period.MIN60:
                return days * Constant.MIN60_PER_TRADE_DAY;

            case Period.MIN30:
                return days * Constant.MIN30_PER_TRADE_DAY;

            case Period.MIN15:
                return days * Constant.MIN15_PER_TRADE_DAY;

            case Period.MIN5:
                return days * Constant.MIN5_PER_TRADE_DAY;

            default:
                return days;
        }
    }

    /**
     * 创建只包含单一频率成分的频谱
     */
    private static Complex[] createSingleComponentSpectrum(Complex[] originalSpectrum, int targetIndex) {
        int length = originalSpectrum.length;
        Complex[] singleComponent = new Complex[length];

        // 初始化所有频率成分为0
        for (int i = 0; i < length; i++) {
            singleComponent[i] = Complex.ZERO;
        }

        // 保留直流分量（索引0）
        singleComponent[0] = originalSpectrum[0];

        // 设置目标频率成分（正频率）
        singleComponent[targetIndex] = originalSpectrum[targetIndex];

        // 设置对应的负频率成分（保持对称性）
        int negativeIndex = length - targetIndex;
        singleComponent[negativeIndex] = originalSpectrum[negativeIndex];

//        Log.d("创建单一成分频谱: 目标索引=" + targetIndex + ", 负频率索引=" + negativeIndex);

        return singleComponent;
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
            double periodInDataPoints = (double) dataLength / k; // 计算周期（数据点数量）

            if (periodInDataPoints > 1 && periodInDataPoints <= dataLength / 2) { // 过滤无效周期
                double periodInDays = convertPeriodToDays(periodInDataPoints, dataLength);

                if (periodInDays > 0.1 && periodInDays <= 365) { // 合理的周期范围：0.1天到1年
                    spectrum.add(new PeriodAmplitude(periodInDays, amplitude));
                }
            }
        }

        // 按振幅降序排序
//        spectrum.sort((a, b) -> Double.compare(b.amplitude, a.amplitude));
        Collections.sort(spectrum, (a, b) -> Double.compare(b.amplitude, a.amplitude));

        return spectrum;
    }

    /**
     * 将周期从数据点数量转换为天数
     */
    private static double convertPeriodToDays(double periodInDataPoints, int totalDataPoints) {
        switch (mPeriod) {
            case Period.YEAR:
            case Period.MONTH6:
            case Period.QUARTER:
            case Period.MONTH2:
            case Period.MONTH:
            case Period.WEEK:
                // 这些周期的数据点直接对应天数
                return periodInDataPoints;

            case Period.DAY:
                // 日线：1个数据点 = 1天
                return periodInDataPoints;

            case Period.MIN60:
                // 60分钟线：4个数据点 = 1天
                return periodInDataPoints / Constant.MIN60_PER_TRADE_DAY;

            case Period.MIN30:
                // 30分钟线：8个数据点 = 1天
                return periodInDataPoints / Constant.MIN30_PER_TRADE_DAY;

            case Period.MIN15:
                // 15分钟线：16个数据点 = 1天
                return periodInDataPoints / Constant.MIN15_PER_TRADE_DAY;

            case Period.MIN5:
                // 5分钟线：48个数据点 = 1天
                return periodInDataPoints / Constant.MIN5_PER_TRADE_DAY;

            default:
                return periodInDataPoints;
        }
    }

    /**
     * 打印主要周期
     */
    private static void printDominantPeriods(List<PeriodAmplitude> spectrum, String lineName) {
        if (spectrum == null) {
            return;
        }
        Log.d("=== " + lineName + " 主要周期 ===");
        Log.d("排名 | 周期(天) | 振幅强度");
        Log.d("----|----------|----------");

        int count = Math.min(10, spectrum.size()); // 显示前10个主要周期
        for (int i = 0; i < count; i++) {
            PeriodAmplitude pa = spectrum.get(i);
            Log.d(String.format("%2d  | %8.2f | %8.4f",
                    i + 1, pa.period, pa.amplitude));
        }
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

    /**
     * 内部类：用于存储极坐标表示的周期信息
     */
    private static class PolarComponent {
        double amplitude;    // 振幅
        double period;       // 周期（天）
        double frequency;    // 频率（1/天）
        double phase;        // 相位角（弧度）
        double phaseDegrees; // 相位角（度）
        int frequencyIndex;  // 频率索引
        double lastPointValue; // 最后一个点的数值

        PolarComponent(double amplitude, double period, double frequency,
                       double phase, double phaseDegrees, int frequencyIndex, double lastPointValue) {
            this.amplitude = amplitude;
            this.period = period;
            this.frequency = frequency;
            this.phase = phase;
            this.phaseDegrees = phaseDegrees;
            this.frequencyIndex = frequencyIndex;
            this.lastPointValue = lastPointValue;
        }

        @Override
        public String toString() {
            return String.format("振幅: %.4f, 周期: %.2f天, 频率: %.4f/天, 相位: %.2f°(%.2frad), 索引: %d, 最后点: %.4f",
                    amplitude, period, frequency, phaseDegrees, phase, frequencyIndex, lastPointValue);
        }
    }
}