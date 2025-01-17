package com.android.orion.ai.ml;

import android.util.Log;

import java.util.ArrayList;

public class PatternClassification {
    public static String TAG = "AI ML " + PatternClassification.class.getSimpleName();

    public static final int xMax = 100;
    public static final int yMax = 100;

    int trainTimes = 10000;
    int trainPoints = 1000;
    int testPoints = 500;

    double[] xPoints = new double[trainPoints];
    double[] yPoints = new double[trainPoints];
    int[] desired = new int[trainPoints];

    public PatternClassification() {
        init();
    }

    void init() {
        for (int i = 0; i < trainPoints; i++) {
            xPoints[i] = Math.random() * xMax;
            yPoints[i] = Math.random() * yMax;
        }

        for (int i = 0; i < trainPoints; i++) {
            desired[i] = 0;
            if (yPoints[i] > f(xPoints[i])) {
                desired[i] = 1;
            }
        }
    }

    double f(double x) {
        return x * 1.2 + 50;
    }

    public void test() {
        Perceptron ptron = new Perceptron();
        for (int j = 0; j < trainTimes; j++) {
            for (int i = 0; i < trainPoints; i++) {
                ptron.train(new double[]{xPoints[i], yPoints[i], ptron.bias}, desired[i]);
            }
        }
        Log.d(TAG, "train finished: trainTimes=" + trainTimes + " trainPoints=" + trainPoints + ptron.toString());

        int errors = 0;
        for (int i = 0; i < testPoints; i++) {
            double x = Math.random() * xMax;
            double y = Math.random() * yMax;
            double guess = ptron.activate(new double[]{x, y, ptron.bias});
            if ((y > f(x) && guess == 0) || (y < f(x) && guess == 1)) {
                errors++;
            }
        }
        Log.d(TAG, "test result: " + errors + " errors out of "  + testPoints);
    }
}
