package com.android.orion;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.android.orion.stocklist.StockTableFixHeader;
import com.inqbarna.tablefixheaders.TableFixHeaders;

public class StockListActivity extends Activity {
	private TableFixHeaders mTableFixHeaders;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_list);

		mTableFixHeaders = (TableFixHeaders) findViewById(R.id.tablefixheaders);
		mTableFixHeaders.setAdapter(new StockTableFixHeader(this).getAdapter());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_list, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
