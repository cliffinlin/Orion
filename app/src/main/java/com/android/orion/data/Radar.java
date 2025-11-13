package com.android.orion.data;

import com.android.orion.database.StockTrend;

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
    public int direction;
    public int vertex;

    public Radar(double amplitude, double period, double frequency,
                 double phase, double phaseDegrees, int frequencyIndex, int direction, int vertex) {
        this.amplitude = amplitude;
        this.period = period;
        this.frequency = frequency;
        this.phase = phase;
        this.phaseDegrees = phaseDegrees;
        this.frequencyIndex = frequencyIndex;
        this.direction = direction;
        this.vertex = vertex;
    }

    public String toNotifyString() {
        return "Radar " + "vertex=" + StockTrend.vertexToString(vertex);
    }
}