package com.android.orion.data;

import com.android.orion.config.Config;

import java.util.ArrayList;

public class LinearRegression {
	public double slope; // 斜率
	public double bias; // 偏置
	public double learningRate = 0.01; // 学习率
	public double xMin, xMax, yMin, yMax; // 归一化的最小值和最大值
	public double mse; //Mean Squared Error
	private ArrayList<Double> xList = new ArrayList<>(); // 输入特征
	private ArrayList<Double> yList = new ArrayList<>(); // 目标值

	// 构造函数
	public LinearRegression() {
	}

	// 构造函数
	public LinearRegression(ArrayList<Double> xList, ArrayList<Double> yList) {
		if (xList.size() != yList.size()) {
			throw new IllegalArgumentException("xList and yList must have the same size");
		}

		// 归一化数据
		this.xList = normalize(xList, true); // 归一化 x
		this.yList = normalize(yList, false); // 归一化 y
		this.slope = 0; // 初始化斜率为 0
		this.bias = 0; // 初始化偏置为 0
	}

	public static void test() {
		// 初始化数据
		ArrayList<Double> xList = new ArrayList<>();
		ArrayList<Double> yList = new ArrayList<>();
		xList.add(32.0);
		xList.add(53.0);
		xList.add(6011.0);
		xList.add(47.0);
		xList.add(59.0);
		xList.add(55.0);
		xList.add(52.0);
		xList.add(39.0);
		xList.add(48.0);
		xList.add(52.0);
		xList.add(45.0);
		xList.add(54.0);
		xList.add(44.0);
		xList.add(58.0);
		xList.add(56.0);
		xList.add(48.0);
		xList.add(44.0);
		xList.add(60.0);

		yList.add(31.0);
		yList.add(68.0);
		yList.add(62.0);
		yList.add(71.0);
		yList.add(87.0);
		yList.add(78.0);
		yList.add(79.0);
		yList.add(59.0);
		yList.add(75.0);
		yList.add(71.0);
		yList.add(55.0);
		yList.add(82.0);
		yList.add(62.0);
		yList.add(75.0);
		yList.add(81.0);
		yList.add(60.0);
		yList.add(82.0);
		yList.add(97.0);

		// 创建线性回归对象
		LinearRegression linearRegression = new LinearRegression(xList, yList);

		// 训练模型
		linearRegression.train(Config.MAX_ITERATIONS);

		// 获取斜率和偏置
		double slope = linearRegression.getSlope();
		double bias = linearRegression.getBias();
		System.out.println("Slope: " + slope);
		System.out.println("Bias: " + bias);

		// 计算均方误差
		double error = linearRegression.calculateError();
		System.out.println("Mean Squared Error: " + error);

		// 预测新数据点
		double x = 50.0;
		double predictedY = linearRegression.predict(x);
		System.out.println("Predicted value for x = " + x + ": " + predictedY);
	}

	public static void test(ArrayList<Double> xList, ArrayList<Double> yList) {
		// 创建线性回归对象
		LinearRegression linearRegression = new LinearRegression();

		// 训练模型
		linearRegression.train(xList, yList, Config.MAX_ITERATIONS);

		// 获取斜率和偏置
		double slope = linearRegression.getSlope();
		double bias = linearRegression.getBias();
		System.out.println("Slope: " + slope);
		System.out.println("Bias: " + bias);

		// 计算均方误差
		double error = linearRegression.calculateError();
		System.out.println("Mean Squared Error: " + error);

		// 预测新数据点
		double x = xList.get(0);
		double predictedY = linearRegression.predict(x);
		System.out.println("Predicted value for x = " + x + ": " + predictedY);
	}

	public void init(ArrayList<Double> xList, ArrayList<Double> yList) {
		if (xList.size() != yList.size()) {
			throw new IllegalArgumentException("xList and yList must have the same size");
		}

		// 归一化数据
		this.xList = normalize(xList, true); // 归一化 x
		this.yList = normalize(yList, false); // 归一化 y
//		this.slope = 0; // 初始化斜率为 0
//		this.bias = 0; // 初始化偏置为 0
	}

	// 归一化数据到 [0, 1] 范围
	private ArrayList<Double> normalize(ArrayList<Double> data, boolean isX) {
		if (data == null || data.isEmpty()) {
			return new ArrayList<>(); // or throw an exception
		}

		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		// Find min and max, skipping null values
		for (Double value : data) {
			if (value == null) {
				continue; // skip null values
			}
			if (value < min) min = value;
			if (value > max) max = value;
		}

		// Check if we found any valid values
		if (min == Double.POSITIVE_INFINITY || max == Double.NEGATIVE_INFINITY) {
			return new ArrayList<>(); // or throw an exception
		}

		// Record min and max
		if (isX) {
			this.xMin = min;
			this.xMax = max;
		} else {
			this.yMin = min;
			this.yMax = max;
		}

		// Normalize data, handling null values
		ArrayList<Double> normalizedData = new ArrayList<>();
		for (Double value : data) {
			if (value == null) {
				normalizedData.add(0.0); // or 0.0 or other default value
			} else {
				normalizedData.add((value - min) / (max - min));
			}
		}
		return normalizedData;
	}

	// 反归一化斜率
	private double denormalizeSlope(double slope) {
		return slope * (yMax - yMin) / (xMax - xMin);
	}

	// 反归一化偏置
	private double denormalizeBias(double bias) {
		return bias * (yMax - yMin) + yMin - denormalizeSlope(slope) * xMin;
	}

	// 使用梯度下降法训练模型
	public void train(int iterations) {
		for (int i = 0; i < iterations; i++) {
			double[] gradients = calculateGradients();
			slope -= learningRate * gradients[0]; // 更新斜率
			bias -= learningRate * gradients[1]; // 更新偏置
		}
	}

	// 使用梯度下降法训练模型
	public void train(ArrayList<Double> xList, ArrayList<Double> yList, int iterations) {
		if (xList == null || yList == null || xList.isEmpty() || yList.isEmpty() || xList.size() != yList.size()) {
			return;
		}
		init(xList, yList);
		for (int i = 0; i < iterations; i++) {
			double[] gradients = calculateGradients();
			slope -= learningRate * gradients[0]; // 更新斜率
			bias -= learningRate * gradients[1]; // 更新偏置
		}
	}

	// 计算斜率和偏置的梯度
	private double[] calculateGradients() {
		double slopeGradient = 0;
		double biasGradient = 0;
		int n = xList.size();

		for (int i = 0; i < n; i++) {
			double prediction = slope * xList.get(i) + bias; // 预测值
			slopeGradient += (-2.0 / n) * xList.get(i) * (yList.get(i) - prediction); // 斜率梯度
			biasGradient += (-2.0 / n) * (yList.get(i) - prediction); // 偏置梯度
		}

		return new double[]{slopeGradient, biasGradient};
	}

	// 获取反归一化后的斜率
	public double getSlope() {
		return denormalizeSlope(slope);
	}

	// 获取反归一化后的偏置
	public double getBias() {
		return denormalizeBias(bias);
	}

	// 预测新数据点的值
	public double predict(double x) {
		// 归一化输入
		double normalizedX = (x - xMin) / (xMax - xMin);
		// 计算预测值
		double normalizedY = slope * normalizedX + bias;
		// 反归一化输出
		return normalizedY * (yMax - yMin) + yMin;
	}

	// 计算均方误差 (MSE)
	public double calculateError() {
		double error = 0;
		int n = xList.size();

		for (int i = 0; i < n; i++) {
			double prediction = slope * xList.get(i) + bias; // 预测值
			double actual = yList.get(i); // 实际值
			error += Math.pow(prediction - actual, 2); // 平方误差
		}

		mse = error / n;

		return error / n; // 返回均方误差
	}

	public int getListSize() {
		return xList.size();
	}
}