package com.android.orion.activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.orion.R;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Preferences;

public class ListActivity extends StorageActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Preferences.getBoolean(
				Setting.SETTING_DEBUG_LOOPBACK, false)) {
			Toast.makeText(
					this,
					getResources().getString(R.string.loopback_is_on),
					Toast.LENGTH_LONG).show();
		}
	}

	public void setListViewHeightBasedOnChildren(@NonNull ListView listView) {
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
}
