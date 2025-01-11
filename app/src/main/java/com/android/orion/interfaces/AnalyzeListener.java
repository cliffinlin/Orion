package com.android.orion.interfaces;

public interface AnalyzeListener {
	void onAnalyzeStart(String stockCode);

	void onAnalyzeFinish(String stockCode);
}
