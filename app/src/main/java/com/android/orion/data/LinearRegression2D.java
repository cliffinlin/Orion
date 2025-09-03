package com.android.orion.data;

import com.android.orion.config.Config;

import java.util.ArrayList;

public class LinearRegression2D {
	public double slope1; // 第一个特征的斜率
	public double slope2; // 第二个特征的斜率
	public double bias;   // 偏置
	public double learningRate = 0.01; // 学习率
	public double x1Min, x1Max, x2Min, x2Max, yMin, yMax; // 归一化的最小值和最大值
	public double mse; // Mean Squared Error
	private ArrayList<Double> x1List = new ArrayList<>(); // 第一个输入特征
	private ArrayList<Double> x2List = new ArrayList<>(); // 第二个输入特征
	private ArrayList<Double> yList = new ArrayList<>();  // 目标值

	// 构造函数
	public LinearRegression2D() {
	}

	// 构造函数
	public LinearRegression2D(ArrayList<Double> x1List, ArrayList<Double> x2List, ArrayList<Double> yList) {
		if (x1List.size() != yList.size() || x2List.size() != yList.size()) {
			throw new IllegalArgumentException("x1List, x2List and yList must have the same size");
		}

		// 归一化数据
		this.x1List = normalize(x1List, true, true);  // 归一化 x1
		this.x2List = normalize(x2List, true, false); // 归一化 x2
		this.yList = normalize(yList, false, false);  // 归一化 y
		this.slope1 = 0; // 初始化斜率为 0
		this.slope2 = 0; // 初始化斜率为 0
		this.bias = 0;   // 初始化偏置为 0
	}

	public static void test() {
		// 初始化数据
		ArrayList<Double> x1List = new ArrayList<>();
		ArrayList<Double> x2List = new ArrayList<>();
		ArrayList<Double> yList = new ArrayList<>();

		// 示例数据 (x1, x2, y)
		x1List.add(32.0);
		x2List.add(10.0);
		yList.add(31.0);
		x1List.add(53.0);
		x2List.add(20.0);
		yList.add(68.0);
		x1List.add(60.0);
		x2List.add(30.0);
		yList.add(97.0);

		// 创建线性回归对象
		LinearRegression2D linearRegression = new LinearRegression2D(x1List, x2List, yList);

		// 训练模型
		linearRegression.train(Config.MAX_ITERATION);

		// 获取斜率和偏置
		double slope1 = linearRegression.getSlope1();
		double slope2 = linearRegression.getSlope2();
		double bias = linearRegression.getBias();
		System.out.println("Slope1: " + slope1);
		System.out.println("Slope2: " + slope2);
		System.out.println("Bias: " + bias);

		// 计算均方误差
		double error = linearRegression.calculateError();
		System.out.println("Mean Squared Error: " + error);

		// 预测新数据点
		double x1 = 50.0;
		double x2 = 25.0;
		double predictedY = linearRegression.predict(x1, x2);
		System.out.println("Predicted value for x1 = " + x1 + ", x2 = " + x2 + ": " + predictedY);
	}

	public void init(ArrayList<Double> x1List, ArrayList<Double> x2List, ArrayList<Double> yList) {
		if (x1List.size() != yList.size() || x2List.size() != yList.size()) {
			throw new IllegalArgumentException("x1List, x2List and yList must have the same size");
		}

		// 归一化数据
		this.x1List = normalize(x1List, true, true);  // 归一化 x1
		this.x2List = normalize(x2List, true, false); // 归一化 x2
		this.yList = normalize(yList, false, false);  // 归一化 y
	}

	// 归一化数据到 [0, 1] 范围
	private ArrayList<Double> normalize(ArrayList<Double> data, boolean isX, boolean isX1) {
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
			if (isX1) {
				this.x1Min = min;
				this.x1Max = max;
			} else {
				this.x2Min = min;
				this.x2Max = max;
			}
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

	// 反归一化斜率1
	private double denormalizeSlope1(double slope1) {
		return slope1 * (yMax - yMin) / (x1Max - x1Min);
	}

	// 反归一化斜率2
	private double denormalizeSlope2(double slope2) {
		return slope2 * (yMax - yMin) / (x2Max - x2Min);
	}

	// 反归一化偏置
	private double denormalizeBias(double bias) {
		return bias * (yMax - yMin) + yMin - denormalizeSlope1(slope1) * x1Min - denormalizeSlope2(slope2) * x2Min;
	}

	// 使用梯度下降法训练模型
	public void train(int iterations) {
		for (int i = 0; i < iterations; i++) {
			double[] gradients = calculateGradients();
			slope1 -= learningRate * gradients[0]; // 更新斜率1
			slope2 -= learningRate * gradients[1]; // 更新斜率2
			bias -= learningRate * gradients[2];    // 更新偏置
		}
	}

	// 计算斜率和偏置的梯度
	private double[] calculateGradients() {
		double slope1Gradient = 0;
		double slope2Gradient = 0;
		double biasGradient = 0;
		int n = x1List.size();

		for (int i = 0; i < n; i++) {
			double prediction = slope1 * x1List.get(i) + slope2 * x2List.get(i) + bias; // 预测值
			double error = yList.get(i) - prediction;
			slope1Gradient += (-2.0 / n) * x1List.get(i) * error; // 斜率1梯度
			slope2Gradient += (-2.0 / n) * x2List.get(i) * error; // 斜率2梯度
			biasGradient += (-2.0 / n) * error;                    // 偏置梯度
		}

		return new double[]{slope1Gradient, slope2Gradient, biasGradient};
	}

	// 获取反归一化后的斜率1
	public double getSlope1() {
		return denormalizeSlope1(slope1);
	}

	// 获取反归一化后的斜率2
	public double getSlope2() {
		return denormalizeSlope2(slope2);
	}

	// 获取反归一化后的偏置
	public double getBias() {
		return denormalizeBias(bias);
	}

	// 预测新数据点的值
	public double predict(double x1, double x2) {
		// 归一化输入
		double normalizedX1 = (x1 - x1Min) / (x1Max - x1Min);
		double normalizedX2 = (x2 - x2Min) / (x2Max - x2Min);
		// 计算预测值
		double normalizedY = slope1 * normalizedX1 + slope2 * normalizedX2 + bias;
		// 反归一化输出
		return normalizedY * (yMax - yMin) + yMin;
	}

	// 计算均方误差 (MSE)
	public double calculateError() {
		double error = 0;
		int n = x1List.size();

		for (int i = 0; i < n; i++) {
			double prediction = slope1 * x1List.get(i) + slope2 * x2List.get(i) + bias; // 预测值
			double actual = yList.get(i); // 实际值
			error += Math.pow(prediction - actual, 2); // 平方误差
		}

		mse = error / n;

		return error / n; // 返回均方误差
	}

	public int getListSize() {
		return x1List.size();
	}
}