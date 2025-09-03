package com.android.orion.data;

import com.android.orion.config.Config;

public class IRR {
	static double mIR;
	static double mIRR;

	public static double getIR() {
		return mIR;
	}

	public static double getIRR() {
		return mIRR;
	}

	// 定义计算 NPV 的函数
	private static double calculateNPV(double[] cashFlows, double rate) {
		double npv = 0.0;
		for (int t = 0; t < cashFlows.length; t++) {
			npv += cashFlows[t] / Math.pow(1 + rate, t);
		}
		return npv;
	}

	// 定义计算 IRR 的函数
	public static double calculateIRR(double[] cashFlows, double precision, int maxIterations) {
		double lowRate = -1.0; // 最低折现率
		double highRate = 1.0; // 最高折现率
		double irr = 0.0; // 初始 IRR

		for (int i = 0; i < maxIterations; i++) {
			irr = (lowRate + highRate) / 2; // 取中间值
			double npv = calculateNPV(cashFlows, irr); // 计算 NPV

			if (Math.abs(npv) < precision) { // 如果 NPV 接近零，返回 IRR
				return irr;
			} else if (npv > 0) { // 如果 NPV > 0，增加 IRR
				lowRate = irr;
			} else { // 如果 NPV < 0，减少 IRR
				highRate = irr;
			}
		}

		return irr; // 返回最终的 IRR
	}

	public static void calculate(double pe, double roe, double dividendPayoutRatio, double currentPrice) {
		// 输入参数
//		double pe = 21.61; // 市盈率
//		double roe = 0.3936; // 净资产收益率
//		double dividendPayoutRatio = 0.80; // 分红股息率
//		double currentPrice = 1475; // 当前股价
		int initialShares = 100; // 初始投资股数
		int years = 100;

		// 计算每股收益 (EPS)
		double eps = currentPrice / pe;

		// 计算净利润增长率 (g)
		double growthRate = roe * (1 - dividendPayoutRatio);

		// 计算 Years 年后的每股收益 (EPS_Years)
		double epsYears = eps * Math.pow(1 + growthRate, years);

		// 计算 Years 年后的股价 (P_Years)
		double priceYears = pe * epsYears;

		// 构建现金流数组
		double[] cashFlows = new double[years + 1]; // years 个时间点（第 0 年到第 years 年）
		cashFlows[0] = -initialShares * currentPrice; // 初始投资（负值）
		double totalDividend = 0;
		for (int year = 1; year <= years; year++) {
			double epsYear = eps * Math.pow(1 + growthRate, year); // 当年的每股收益
			double dividendPerShare = epsYear * dividendPayoutRatio; // 每股分红
			double dividend = dividendPerShare * initialShares; // 总分红金额
			cashFlows[year] = dividend; // 每年的现金流（分红）
			totalDividend += dividend;
		}

		// 第 years 年的现金流包括股票卖出价值
//		cashFlows[years] += initialShares * priceYears;

		mIR = totalDividend / Math.abs(cashFlows[0]);

		// 计算 IRR
		mIRR = calculateIRR(cashFlows, 0.00001, Config.MAX_ITERATION);

		// 输出结果
//		for (int i = 0; i < cashFlows.length; i++) {
//			System.out.println("cashFlows[" + i + "]: " + cashFlows[i]);
//		}
//		System.out.println(years + "年内部收益率(IRR): " + (mIRR * 100) + "%" + " 内部收益(IR):" + mIR);
	}
}