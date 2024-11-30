package com.android.orion.activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.orion.R;
import com.android.orion.setting.Setting;

public class ListActivity extends StorageActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Setting.getDebugLoopback()) {
			Toast.makeText(
					this,
					getResources().getString(R.string.loopback_is_on),
					Toast.LENGTH_LONG).show();
		}
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
}
