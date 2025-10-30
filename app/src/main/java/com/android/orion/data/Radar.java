package com.android.orion.data;

/**
 * 内部类：用于存储极坐标表示的周期信息
 */
public class Radar {
    public double amplitude;    // 振幅
    public double period;       // 周期（天）
    public double frequency;    // 频率（1/天）
    public double phase;        // 相位角（弧度）
    public double phaseDegrees; // 相位角（度）
    public int frequencyIndex;  // 频率索引
    public double lastPointValue; // 最后一个点的数值

    public Radar(double amplitude, double period, double frequency,
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