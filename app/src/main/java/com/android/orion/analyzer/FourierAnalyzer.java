package com.android.orion.analyzer;

import com.android.orion.data.Period;
import com.android.orion.data.Radar;
import com.android.orion.constant.Constant;
import com.android.orion.database.StockTrend;
import com.android.orion.utility.Logger;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FourierAnalyzer {
    private static String mPeriod;
    private static ArrayList<Double> mDataList = new ArrayList<>();
    private static ArrayList<Double> mRadarList = new ArrayList<>();
    private static Radar mRadar;
    private static boolean logMore = false;
    private static int mComponentCount = 32;

    static Logger Log = Logger.getLogger();

    public static Radar getRadar() {
        return mRadar;
    }

    public static ArrayList<Double> getRadarList() {
        return new ArrayList<>(mRadarList);
    }

    /**
     * 设置要重建的成分数量
     */
    public static void setComponentCount(int count) {
        mComponentCount = Math.max(1, count);
    }

    /**
     * 进行傅立叶变换分析并重建前n个主成分（使用IFFT重建）
     */
    public static void analyze(String period, ArrayList<Double> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        mPeriod = period;
        mDataList = dataList;

        List<PeriodAmplitude> spectrum = performFourierAnalysis(mDataList);
        if (logMore) {
            printDominantPeriods(spectrum);
        }

        // 使用IFFT重建前n个主成分
        List<Double> reconstructedData = reconstructWithIFFT(mDataList, spectrum, mComponentCount);
        mRadarList.clear();
        mRadarList.addAll(reconstructedData);
    }

    /**
     * 执行傅立叶变换分析
     */
    private static List<PeriodAmplitude> performFourierAnalysis(List<Double> data) {
        if (data == null) {
            Log.d("data is null");
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
            Log.d("数据不足，无法进行傅立叶分析");
            return new ArrayList<>();
        }

        int n = cleanData.size();

        // 找到最接近的2的幂次方长度
        int fftSize = findNextPowerOfTwo(n);

        // 准备FFT输入数据
        double[] fftInput = new double[fftSize];
        for (int i = 0; i < n; i++) {
            fftInput[i] = cleanData.get(i);
        }

        // 执行FFT
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] fftResult = fft.transform(fftInput, TransformType.FORWARD);

        // 计算振幅和周期
        List<PeriodAmplitude> spectrum = calculateSpectrum(fftResult, n);

        return spectrum;
    }

    /**
     * 使用IFFT重建信号（主要成分重建）
     */
    private static List<Double> reconstructWithIFFT(List<Double> originalData,
                                                    List<PeriodAmplitude> spectrum,
                                                    int componentCount) {
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

        // 执行FFT获取完整的频域数据
        int fftSize = findNextPowerOfTwo(n);
        double[] fftInput = new double[fftSize];
        for (int i = 0; i < n; i++) {
            fftInput[i] = cleanData.get(i);
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] fullSpectrum = fft.transform(fftInput, TransformType.FORWARD);

        // 按周期从大到小排序频谱
        List<PeriodAmplitude> periodSortedSpectrum = new ArrayList<>(spectrum);
        Collections.sort(periodSortedSpectrum, (a, b) -> Double.compare(b.period, a.period));

        // 确定实际要重建的成分数量
        int actualCount = Math.min(componentCount, periodSortedSpectrum.size());

        if (logMore) {
            Log.d("IFFT重建 - 总共: " + spectrum.size() + ", 重建成分: " + actualCount);
        }

        // 创建滤波后的频域数据（只保留主要成分）
        Complex[] filteredSpectrum = createFilteredSpectrum(fullSpectrum, periodSortedSpectrum, actualCount, n);

        // 使用IFFT重建信号
        Complex[] reconstructedComplex = fft.transform(filteredSpectrum, TransformType.INVERSE);

        // 转换为实数列表
        List<Double> reconstructedData = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            // 取实部作为重建信号（虚部应该接近0）
            reconstructedData.add(reconstructedComplex[i].getReal());
        }

        // 设置雷达数据
        if (!periodSortedSpectrum.isEmpty()) {
            PeriodAmplitude firstComponent = periodSortedSpectrum.get(0);
            double value1 = 0;
            double value2 = 0;
            double value3 = 0;
            int direction = StockTrend.DIRECTION_NONE;
            int vertex = StockTrend.VERTEX_NONE;
            if (periodSortedSpectrum.size() > StockTrend.VERTEX_SIZE) {
                value1 = reconstructedData.get(reconstructedData.size() - 1);
                value2 = reconstructedData.get(reconstructedData.size() - 2);
                value3 = reconstructedData.get(reconstructedData.size() - 3);
                if (value1 > value2) {
                    direction = StockTrend.DIRECTION_UP;
                } else if (value1 < value2) {
                    direction = StockTrend.DIRECTION_DOWN;
                }
                if (value2 > value1 && value2 > value3) {
                    vertex = StockTrend.VERTEX_TOP;
                } else if (value2 < value1 && value2 < value3) {
                    vertex = StockTrend.VERTEX_BOTTOM;
                }
            }
            mRadar = new Radar(Math.abs(value3), firstComponent.period, firstComponent.frequency,
                    firstComponent.phase, firstComponent.phaseDegrees, 0, direction, vertex);
        }

        if (logMore) {
            Log.d("IFFT重建完成，使用 " + actualCount + " 个主要成分");
            validateReconstruction(reconstructedData, cleanData);
        }

        return reconstructedData;
    }

    /**
     * 创建滤波后的频域数据（只保留主要成分）
     */
    private static Complex[] createFilteredSpectrum(Complex[] fullSpectrum,
                                                    List<PeriodAmplitude> mainComponents,
                                                    int componentCount, int dataLength) {
        // 创建全零的频域数据
        Complex[] filtered = new Complex[fullSpectrum.length];
        for (int i = 0; i < filtered.length; i++) {
            filtered[i] = Complex.ZERO;
        }

        // 保留直流分量（索引0）
        filtered[0] = fullSpectrum[0];

        // 保留主要频率成分
        for (int i = 0; i < componentCount; i++) {
            PeriodAmplitude component = mainComponents.get(i);
            int frequencyIndex = findFrequencyIndexFromSpectrum(component, dataLength);

            // 确保索引在有效范围内
            if (frequencyIndex > 0 && frequencyIndex < fullSpectrum.length / 2) {
                // 保留正频率成分
                filtered[frequencyIndex] = fullSpectrum[frequencyIndex];
                // 保留对应的负频率成分（对称性）
                int negativeIndex = fullSpectrum.length - frequencyIndex;
                if (negativeIndex < fullSpectrum.length) {
                    filtered[negativeIndex] = fullSpectrum[negativeIndex];
                }
            }
        }

        if (logMore) {
            Log.d("频域滤波完成，保留了 " + componentCount + " 个主要频率成分");
        }

        return filtered;
    }

    /**
     * 验证重建结果
     */
    private static void validateReconstruction(List<Double> reconstructed, List<Double> original) {
        if (reconstructed.size() != original.size()) return;

        // 计算整体误差
        double totalError = 0;
        for (int i = 0; i < reconstructed.size(); i++) {
            totalError += Math.abs(reconstructed.get(i) - original.get(i));
        }
        totalError /= reconstructed.size();

        // 检查尾部数据
        int tailLength = Math.min(6, reconstructed.size());
        StringBuilder tailInfo = new StringBuilder();
        tailInfo.append("尾部数据 - 原始: ");
        for (int i = reconstructed.size() - tailLength; i < reconstructed.size(); i++) {
            tailInfo.append(String.format("%.3f", original.get(i))).append(" ");
        }
        tailInfo.append("重建: ");
        for (int i = reconstructed.size() - tailLength; i < reconstructed.size(); i++) {
            tailInfo.append(String.format("%.3f", reconstructed.get(i))).append(" ");
        }

        Log.d("重建验证 - 平均误差: " + String.format("%.4f", totalError));
        Log.d(tailInfo.toString());
    }

    /**
     * 计算频谱（周期、振幅和相位）
     */
    private static List<PeriodAmplitude> calculateSpectrum(Complex[] fftResult, int dataLength) {
        List<PeriodAmplitude> spectrum = new ArrayList<>();
        if (fftResult == null) {
            return spectrum;
        }

        int halfLength = fftResult.length / 2;

        for (int k = 1; k < halfLength; k++) {
            Complex complex = fftResult[k];
            double amplitude = complex.abs() / dataLength * 2;
            double phase = complex.getArgument();
            double phaseDegrees = Math.toDegrees(phase);
            if (phaseDegrees < 0) phaseDegrees += 360;

            double periodInDataPoints = (double) dataLength / k;

            if (periodInDataPoints > 1 && periodInDataPoints <= dataLength / 2) {
                double periodInDays = convertPeriodToDays(periodInDataPoints, dataLength);

                if (periodInDays > 0.1 && periodInDays <= 365) {
                    double frequency = 1.0 / periodInDays;
                    spectrum.add(new PeriodAmplitude(periodInDays, amplitude, phase, phaseDegrees, frequency));
                }
            }
        }

        Collections.sort(spectrum, (a, b) -> Double.compare(b.period, a.period));
        return spectrum;
    }

    /**
     * 根据频谱中的周期信息找到对应的频率索引
     */
    private static int findFrequencyIndexFromSpectrum(PeriodAmplitude dominantPeriod, int dataLength) {
        double targetPeriodInDataPoints = convertDaysToDataPoints(dominantPeriod.period, dataLength);
        int dominantIndex = (int) Math.round(dataLength / targetPeriodInDataPoints);
        int halfLength = dataLength / 2;
        if (dominantIndex < 1) dominantIndex = 1;
        if (dominantIndex >= halfLength) dominantIndex = halfLength - 1;
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
                return periodInDataPoints;

            case Period.DAY:
                return periodInDataPoints;

            case Period.MIN60:
                return periodInDataPoints / Constant.MIN60_PER_TRADE_DAY;

            case Period.MIN30:
                return periodInDataPoints / Constant.MIN30_PER_TRADE_DAY;

            case Period.MIN15:
                return periodInDataPoints / Constant.MIN15_PER_TRADE_DAY;

            case Period.MIN5:
                return periodInDataPoints / Constant.MIN5_PER_TRADE_DAY;

            default:
                return periodInDataPoints;
        }
    }

    /**
     * 打印主要周期
     */
    private static void printDominantPeriods(List<PeriodAmplitude> spectrum) {
        if (spectrum == null) return;

        Log.d("=== 主要周期（按周期从大到小排序） ===");
        Log.d("排名 | 周期(天) | 频率(/天) | 振幅强度 | 相位(度)");
        Log.d("----|----------|-----------|----------|----------");

        int count = Math.min(10, spectrum.size());
        for (int i = 0; i < count; i++) {
            PeriodAmplitude pa = spectrum.get(i);
            Log.d(String.format("%2d  | %8.2f | %9.4f | %8.4f | %8.2f",
                    i + 1, pa.period, pa.frequency, pa.amplitude, pa.phaseDegrees));
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
     * 内部类：用于存储周期、振幅、相位和频率
     */
    private static class PeriodAmplitude {
        double period;
        double amplitude;
        double phase;
        double phaseDegrees;
        double frequency;

        PeriodAmplitude(double period, double amplitude) {
            this.period = period;
            this.amplitude = amplitude;
        }

        PeriodAmplitude(double period, double amplitude, double phase, double phaseDegrees) {
            this.period = period;
            this.amplitude = amplitude;
            this.phase = phase;
            this.phaseDegrees = phaseDegrees;
        }

        PeriodAmplitude(double period, double amplitude, double phase, double phaseDegrees, double frequency) {
            this.period = period;
            this.amplitude = amplitude;
            this.phase = phase;
            this.phaseDegrees = phaseDegrees;
            this.frequency = frequency;
        }
    }
}
