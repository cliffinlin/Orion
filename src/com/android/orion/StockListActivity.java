package com.android.orion;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.android.orion.database.Stock;
import com.android.orion.stocklist.StockCellViewGroup;
import com.android.orion.stocklist.StockTableFixHeaderAdapter;
import com.inqbarna.tablefixheaders.TableFixHeaders;
import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;
import com.miguelbcr.tablefixheaders.TableFixHeaderAdapter;

public class StockListActivity extends StorageActivity {
	public static final int EXECUTE_STOCK_LIST_LOAD = 1;

	TableFixHeaders mTableFixHeaders;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_list);

		mTableFixHeaders = (TableFixHeaders) findViewById(R.id.tablefixheaders);
		mTableFixHeaders.setAdapter(getAdapter());

		startLoadTask(EXECUTE_STOCK_LIST_LOAD);
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

	@Override
	Long doInBackgroundLoad(Object... params) {
		super.doInBackgroundLoad(params);
		int execute = (Integer) params[0];

		switch (execute) {
		case EXECUTE_STOCK_LIST_LOAD:
			// mStockDatabaseManager.getStockById(mStock);
			break;

		default:
			break;
		}

		return RESULT_SUCCESS;
	}

	public BaseTableAdapter getAdapter() {
		StockTableFixHeaderAdapter adapter = new StockTableFixHeaderAdapter(
				mContext);

		List<Stock> body = getBody();

		adapter.setFirstHeader(mContext.getResources().getString(
				R.string.stock_name_code));
		adapter.setHeader(getHeader());
		adapter.setFirstBody(body);
		adapter.setBody(body);
		adapter.setSection(body);

		setListeners(adapter);

		return adapter;
	}

	private void setListeners(StockTableFixHeaderAdapter adapter) {
		TableFixHeaderAdapter.ClickListener<String, StockCellViewGroup> clickListenerHeader = new TableFixHeaderAdapter.ClickListener<String, StockCellViewGroup>() {
			@Override
			public void onClickItem(String s, StockCellViewGroup viewGroup,
					int row, int column) {
				// Snackbar.make(viewGroup, "Click on " + s + " (" + row + "," +
				// column + ")", Snackbar.LENGTH_SHORT).show();
			}
		};

		TableFixHeaderAdapter.ClickListener<Stock, StockCellViewGroup> clickListenerBody = new TableFixHeaderAdapter.ClickListener<Stock, StockCellViewGroup>() {
			@Override
			public void onClickItem(Stock item, StockCellViewGroup viewGroup,
					int row, int column) {
				// Snackbar.make(viewGroup, "Click on " + item.data[column + 1]
				// + " (" + row + "," + column + ")",
				// Snackbar.LENGTH_SHORT).show();
			}
		};

		TableFixHeaderAdapter.ClickListener<Stock, StockCellViewGroup> clickListenerSection = new TableFixHeaderAdapter.ClickListener<Stock, StockCellViewGroup>() {
			@Override
			public void onClickItem(Stock item, StockCellViewGroup viewGroup,
					int row, int column) {
				// Snackbar.make(viewGroup, "Click on " + item.type + " (" + row
				// + "," + column + ")", Snackbar.LENGTH_SHORT).show();
			}
		};

		adapter.setClickListenerFirstHeader(clickListenerHeader);
		adapter.setClickListenerHeader(clickListenerHeader);
		adapter.setClickListenerFirstBody(clickListenerBody);
		adapter.setClickListenerBody(clickListenerBody);
		adapter.setClickListenerSection(clickListenerSection);
	}

	private List<String> getHeader() {
		List<String> headerStrings = new ArrayList<String>();

		headerStrings.add(mContext.getResources().getString(R.string.price));
		headerStrings
				.add(mContext.getResources().getString(R.string.price_net));

		headerStrings.add(mContext.getResources().getString(
				R.string.stock_action_5min));
		headerStrings.add(mContext.getResources().getString(
				R.string.stock_action_15min));
		headerStrings.add(mContext.getResources().getString(
				R.string.stock_action_30min));
		headerStrings.add(mContext.getResources().getString(
				R.string.stock_action_60min));

		// headerStrings.add(mContext.getResources().getString(
		// R.string.stock_action_day));
		//
		// headerStrings.add(mContext.getResources().getString(
		// R.string.stock_action_week));
		//
		// headerStrings.add(mContext.getResources().getString(
		// R.string.stock_action_month));

		return headerStrings;
	}

	private List<Stock> getBody() {
		List<Stock> items = new ArrayList<Stock>();
		// items.add(new Nexus("Nexus One", "HTC", "Gingerbread", "10",
		// "512 MB",
		// "3.7\"", "512 MB"));
		// items.add(new Nexus("Nexus S", "Samsung", "Gingerbread", "10",
		// "16 GB",
		// "4\"", "512 MB"));
		// items.add(new Nexus("Galaxy Nexus (16 GB)", "Samsung",
		// "Ice cream Sandwich", "15", "16 GB", "4.65\"", "1 GB"));
		// items.add(new Nexus("Galaxy Nexus (32 GB)", "Samsung",
		// "Ice cream Sandwich", "15", "32 GB", "4.65\"", "1 GB"));
		// items.add(new Nexus("Nexus 4 (8 GB)", "LG", "Jelly Bean", "17",
		// "8 GB",
		// "4.7\"", "2 GB"));
		// items.add(new Nexus("Nexus 4 (16 GB)", "LG", "Jelly Bean", "17",
		// "16 GB", "4.7\"", "2 GB"));
		// items.add(new Nexus("Nexus 7 (16 GB)", "Asus", "Jelly Bean", "16",
		// "16 GB", "7\"", "1 GB"));
		// items.add(new Nexus("Nexus 7 (32 GB)", "Asus", "Jelly Bean", "16",
		// "32 GB", "7\"", "1 GB"));
		// items.add(new Nexus("Nexus 10 (16 GB)", "Samsung", "Jelly Bean",
		// "17",
		// "16 GB", "10\"", "2 GB"));
		// items.add(new Nexus("Nexus 10 (32 GB)", "Samsung", "Jelly Bean",
		// "17",
		// "32 GB", "10\"", "2 GB"));
		// items.add(new Nexus("Nexus Q", "--", "Honeycomb", "13", "--", "--",
		// "--"));
		// items.add(new Nexus("Galaxy Nexus (16 GB)", "Samsung",
		// "Ice cream Sandwich", "15", "16 GB", "4.65\"", "1 GB"));
		// items.add(new Nexus("Galaxy Nexus (32 GB)", "Samsung",
		// "Ice cream Sandwich", "15", "32 GB", "4.65\"", "1 GB"));
		// items.add(new Nexus("Nexus 4 (8 GB)", "LG", "Jelly Bean", "17",
		// "8 GB",
		// "4.7\"", "2 GB"));
		// items.add(new Nexus("Nexus 4 (16 GB)", "LG", "Jelly Bean", "17",
		// "16 GB", "4.7\"", "2 GB"));
		// items.add(new Nexus("Nexus 7 (16 GB)", "Asus", "Jelly Bean", "16",
		// "16 GB", "7\"", "1 GB"));
		// items.add(new Nexus("Nexus 7 (32 GB)", "Asus", "Jelly Bean", "16",
		// "32 GB", "7\"", "1 GB"));
		// items.add(new Nexus("Nexus 10 (16 GB)", "Samsung", "Jelly Bean",
		// "17",
		// "16 GB", "10\"", "2 GB"));
		// items.add(new Nexus("Nexus 10 (32 GB)", "Samsung", "Jelly Bean",
		// "17",
		// "32 GB", "10\"", "2 GB"));
		// items.add(new Nexus("Nexus Q", "--", "Honeycomb", "13", "--", "--",
		// "--"));
		return items;
	}
}
