package com.android.orion.interfaces;

public interface DownloadListener {
	void onDownloadStart(String stockCode);

	void onDownloadComplete(String stockCode);
}
