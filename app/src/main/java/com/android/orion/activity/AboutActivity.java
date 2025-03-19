package com.android.orion.activity;

import android.os.Bundle;
import android.view.Menu;

import com.android.orion.R;

public class AboutActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.about, menu);
		return super.onCreateOptionsMenu(menu);
	}
}
