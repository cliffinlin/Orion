package com.android.orion.ai.ml.training;

import androidx.annotation.NonNull;

public class Perceptron {
    public static String TAG = "AI ML " + Perceptron.class.getSimpleName();
    int no = 2;
    double learnc = 0.00001;
    double bias = 1;
    double[] weights = new double[no + 1];

    Perceptron() {
        init();
    }

    void init() {
        for (int i = 0; i <= no; i++) {
            this.weights[i] = Math.random() * 2 - 1;
        }
    }

    double activate(@NonNull double[] inputs) {
        double sum = 0;
        for (int i = 0; i < inputs.length; i++) {
            sum += inputs[i] * this.weights[i];
        }
        if (sum > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public void train(@NonNull double[] inputs, int desired) {
        double guess = this.activate(inputs);
        double error = desired - guess;
//        Log.d(TAG, "train guess=" + guess + " desired=" + desired + " error=" + error);
        if (error != 0) {
            for (int i = 0; i < inputs.length; i++) {
                this.weights[i] += this.learnc * error * inputs[i];
            }
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < weights.length; i++) {
            builder.append("\n\t" + "weights[" + i + "]=" + weights[i]);
        }
        return builder.toString();
    }
}
