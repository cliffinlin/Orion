package com.android.orion;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.android.orion.R;
import com.android.orion.stocklist.StockTableFixHeader;
import com.inqbarna.tablefixheaders.TableFixHeaders;
import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;

public class StockListActivity extends Activity {
	StockTableFixHeader mStockTableFixHeader = null;
	
	private TableFixHeaders tableFixHeaders;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_list);

		tableFixHeaders = (TableFixHeaders) findViewById(R.id.tablefixheaders);
		mStockTableFixHeader = new StockTableFixHeader(this);
		BaseTableAdapter baseTableAdapter = mStockTableFixHeader.getAdapter();
		
		tableFixHeaders.setAdapter(baseTableAdapter);
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
