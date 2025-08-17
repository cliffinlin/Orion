package com.android.orion.activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListActivity extends StorageActivity {
	String mAnalyzingStockCode = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void setListViewHeightBasedOnChildren(ListView listView) {
		if (listView == null) {
			return;
		}

		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;

		for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));

		listView.setLayoutParams(params);
	}

	boolean setVisibility(View view, boolean visibility) {
		if (view == null) {
			return true;
		}

		if (visibility) {
			view.setVisibility(View.VISIBLE);
			return false;
		} else {
			view.setVisibility(View.GONE);
			return true;
		}
	}

	boolean setVisibility(TextView textView, boolean visibility) {
		return setVisibility((View) textView, visibility);
	}

	@Override
	public void onAnalyzeStart(String stockCode) {
		mAnalyzingStockCode = stockCode;
	}

	@Override
	public void onAnalyzeFinish(String stockCode) {
		super.onAnalyzeFinish(stockCode);
		mAnalyzingStockCode = "";
	}

	@Override
	public void onDownloadStart(String stockCode) {
		mAnalyzingStockCode = stockCode;
	}

	@Override
	public void onDownloadComplete(String stockCode) {
		mAnalyzingStockCode = "";
	}
}
