package com.android.orion.ai.ml.learning;

import android.util.Log;

public class HousePrices {
	public static String TAG = "AI ML " + HousePrices.class.getSimpleName();

	double[] xArray = new double[]{32,53,61,47,59,55,52,39,48,52,45,54,44,58,56,48,44,60};
	double[] yArray = new double[]{31,68,62,71,87,78,79,59,75,71,55,82,62,75,81,60,82,97};
	Trainer trainer = new Trainer(xArray, yArray);

	public HousePrices() {
	}

	public void test() {
		trainer = new Trainer(xArray, yArray);
		for (int i = 100; i < 10000; i+=100) {
			trainer.train(i);
			Log.d(TAG, "training " + i + " times: " + trainer.toString());
		}
	}
}
