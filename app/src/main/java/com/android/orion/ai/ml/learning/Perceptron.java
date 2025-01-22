package com.android.orion.ai.ml.learning;

import java.util.ArrayList;
import java.util.Arrays;

public class Perceptron {
	public ArrayList<Double> xArr;
	public ArrayList<Double> yArr;
	public int points;
	public double learnc;
	public double weight;
	public double bias;
	public double error;

	public Perceptron() {
	}

	public void init() {
		this.learnc = 0.00001;
		this.weight = 0;
		this.bias = 1;
		this.error = 0;
	}

	public double predict(double x) {
		return this.weight * x + this.bias;
	}

	public static double predict(double weight, double x, double bias) {
		return weight * x + bias;
	}

	public double costError() {
		double total = 0;
		for (int i = 0; i < this.points; i++) {
			total += Math.pow((this.yArr.get(i) - predict(this.xArr.get(i))), 2);
		}
		return total / this.points;
	}

	public void updateWeights() {
		double wx;
		double w_deriv = 0;
		double b_deriv = 0;
		for (int i = 0; i < this.points; i++) {
			wx = this.yArr.get(i) - predict(this.xArr.get(i));
			w_deriv += -2 * wx * this.xArr.get(i);
			b_deriv += -2 * wx;
		}
		this.weight -= (w_deriv / this.points) * this.learnc;
		this.bias -= (b_deriv / this.points) * this.learnc;
	}

	public void train(ArrayList<Double> xArray, ArrayList<Double> yArray, int times) {
		if (xArray == null || xArray.size() < 2) {
			return;
		}

		if (yArray == null || yArray.size() < 2) {
			return;
		}

		this.xArr = xArray;
		this.yArr = yArray;
		this.points = this.xArr.size();

		for (int i = 0; i < times; i++) {
			this.updateWeights();
		}

		this.error = this.costError();
	}

	public String toString() {
		return " Weight: " + this.weight + " Bias: " + this.bias + " Error: " + this.error;
	}
}
