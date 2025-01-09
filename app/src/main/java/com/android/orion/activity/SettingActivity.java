package com.android.orion.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.android.orion.R;

public class SettingActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.service_setting, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				setResult(RESULT_OK, mIntent);
				finish();
				return true;

			default:
				return super.onMenuItemSelected(featureId, item);
		}
	}
}
