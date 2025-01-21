package com.android.orion.ai.ml.learning;

import android.util.Log;

import java.util.ArrayList;

public class HousePrices {
	public static String TAG = "AI ML " + HousePrices.class.getSimpleName();

	public double[] xArray = new double[]{32,53,61,47,59,55,52,39,48,52,45,54,44,58,56,48,44,60};
	public double[] yArray = new double[]{31,68,62,71,87,78,79,59,75,71,55,82,62,75,81,60,82,97};
	public Perceptron perceptron;

	public HousePrices() {
	}

	public void train() {
		ArrayList<Double> xList = new ArrayList<>();
		ArrayList<Double> yList = new ArrayList<>();

		for (int i = 0; i < xArray.length; i++) {
			xList.add(xArray[i]);
		}
		for (int i = 0; i < yArray.length; i++) {
			yList.add(yArray[i]);
		}

		int i = 100;
		perceptron = new Perceptron();
		perceptron.init(xList, yList);
		perceptron.train(i);
		Log.d(TAG, "------->training " + i + " times: " + perceptron.toString());
	}
}
