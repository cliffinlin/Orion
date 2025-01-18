package com.android.orion.ai.ml.learning;

public class Trainer {
	public static String TAG = "AI ML " + Trainer.class.getSimpleName();
	public double[] xArr;
	public double[] yArr;
	public int points;
	public double learnc;
	public double weight;
	public double bias;
	public double cost;

	public Trainer(double[] xArray, double[] yArray) {
		this.xArr = xArray;
		this.yArr = yArray;
		this.points = this.xArr.length;
		this.learnc = 0.00001;
		this.weight = 0;
		this.bias = 1;
		this.cost = 0;
	}

	public double predict(double x) {
		return this.weight * x + this.bias;
	}

	public double costError() {
		double total = 0;
		for (int i = 0; i < this.points; i++) {
			total += Math.pow((this.yArr[i] - predict(this.xArr[i])), 2);
		}
		return total / this.points;
	}

	public void updateWeights() {
		double wx;
		double w_deriv = 0;
		double b_deriv = 0;
		for (int i = 0; i < this.points; i++) {
			wx = this.yArr[i] - predict(this.xArr[i]);
			w_deriv += -2 * wx * this.xArr[i];
			b_deriv += -2 * wx;
		}
		this.weight -= (w_deriv / this.points) * this.learnc;
		this.bias -= (b_deriv / this.points) * this.learnc;
	}

	public void train(int iter) {
		for (int i = 0; i < iter; i++) {
			this.updateWeights();
		}
		this.cost = this.costError();
	}

	public String toString() {
		return "Cost: " + this.cost + " Weight: " + this.weight + " Bias: " + this.bias;
	}
}
