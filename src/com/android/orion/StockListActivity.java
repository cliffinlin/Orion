package com.android.orion;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.android.orion.stocklist.Nexus;
import com.android.orion.stocklist.StockCellViewGroup;
import com.android.orion.stocklist.StockTableFixHeaderAdapter;
import com.android.orion.utility.Utility;
import com.inqbarna.tablefixheaders.TableFixHeaders;
import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;
import com.miguelbcr.tablefixheaders.TableFixHeaderAdapter;

public class StockListActivity extends StorageActivity {
	TableFixHeaders mTableFixHeaders;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_list);

		mTableFixHeaders = (TableFixHeaders) findViewById(R.id.tablefixheaders);
		mTableFixHeaders.setAdapter(getAdapter());
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

	public BaseTableAdapter getAdapter() {
		StockTableFixHeaderAdapter adapter = new StockTableFixHeaderAdapter(
				mContext);
		List<Nexus> body = getBody();

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

		TableFixHeaderAdapter.ClickListener<Nexus, StockCellViewGroup> clickListenerBody = new TableFixHeaderAdapter.ClickListener<Nexus, StockCellViewGroup>() {
			@Override
			public void onClickItem(Nexus item, StockCellViewGroup viewGroup,
					int row, int column) {
				// Snackbar.make(viewGroup, "Click on " + item.data[column + 1]
				// + " (" + row + "," + column + ")",
				// Snackbar.LENGTH_SHORT).show();
			}
		};

		TableFixHeaderAdapter.ClickListener<Nexus, StockCellViewGroup> clickListenerSection = new TableFixHeaderAdapter.ClickListener<Nexus, StockCellViewGroup>() {
			@Override
			public void onClickItem(Nexus item, StockCellViewGroup viewGroup,
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

		if (Utility.getSettingBoolean(mContext, Constants.PERIOD_5MIN)) {
			headerStrings.add(mContext.getResources().getString(
					R.string.stock_action_5min));
		}

		if (Utility.getSettingBoolean(mContext, Constants.PERIOD_15MIN)) {
			headerStrings.add(mContext.getResources().getString(
					R.string.stock_action_15min));
		}

		if (Utility.getSettingBoolean(mContext, Constants.PERIOD_30MIN)) {
			headerStrings.add(mContext.getResources().getString(
					R.string.stock_action_30min));
		}

		if (Utility.getSettingBoolean(mContext, Constants.PERIOD_60MIN)) {
			headerStrings.add(mContext.getResources().getString(
					R.string.stock_action_60min));
		}

		// if (Utility.getSettingBoolean(mContext, Constants.PERIOD_DAY)) {
		// headerStrings.add(mContext.getResources().getString(
		// R.string.stock_action_day));
		// }
		//
		// if (Utility.getSettingBoolean(mContext, Constants.PERIOD_WEEK)) {
		// headerStrings.add(mContext.getResources().getString(
		// R.string.stock_action_week));
		// }
		//
		// if (Utility.getSettingBoolean(mContext, Constants.PERIOD_MONTH)) {
		// headerStrings.add(mContext.getResources().getString(
		// R.string.stock_action_month));
		// }

		return headerStrings;
	}

	private List<Nexus> getBody() {
		List<Nexus> items = new ArrayList<Nexus>();
		items.add(new Nexus("Nexus One", "HTC", "Gingerbread", "10", "512 MB",
				"3.7\"", "512 MB"));
		items.add(new Nexus("Nexus S", "Samsung", "Gingerbread", "10", "16 GB",
				"4\"", "512 MB"));
		items.add(new Nexus("Galaxy Nexus (16 GB)", "Samsung",
				"Ice cream Sandwich", "15", "16 GB", "4.65\"", "1 GB"));
		items.add(new Nexus("Galaxy Nexus (32 GB)", "Samsung",
				"Ice cream Sandwich", "15", "32 GB", "4.65\"", "1 GB"));
		items.add(new Nexus("Nexus 4 (8 GB)", "LG", "Jelly Bean", "17", "8 GB",
				"4.7\"", "2 GB"));
		items.add(new Nexus("Nexus 4 (16 GB)", "LG", "Jelly Bean", "17",
				"16 GB", "4.7\"", "2 GB"));
		items.add(new Nexus("Nexus 7 (16 GB)", "Asus", "Jelly Bean", "16",
				"16 GB", "7\"", "1 GB"));
		items.add(new Nexus("Nexus 7 (32 GB)", "Asus", "Jelly Bean", "16",
				"32 GB", "7\"", "1 GB"));
		items.add(new Nexus("Nexus 10 (16 GB)", "Samsung", "Jelly Bean", "17",
				"16 GB", "10\"", "2 GB"));
		items.add(new Nexus("Nexus 10 (32 GB)", "Samsung", "Jelly Bean", "17",
				"32 GB", "10\"", "2 GB"));
		items.add(new Nexus("Nexus Q", "--", "Honeycomb", "13", "--", "--",
				"--"));
		items.add(new Nexus("Galaxy Nexus (16 GB)", "Samsung",
				"Ice cream Sandwich", "15", "16 GB", "4.65\"", "1 GB"));
		items.add(new Nexus("Galaxy Nexus (32 GB)", "Samsung",
				"Ice cream Sandwich", "15", "32 GB", "4.65\"", "1 GB"));
		items.add(new Nexus("Nexus 4 (8 GB)", "LG", "Jelly Bean", "17", "8 GB",
				"4.7\"", "2 GB"));
		items.add(new Nexus("Nexus 4 (16 GB)", "LG", "Jelly Bean", "17",
				"16 GB", "4.7\"", "2 GB"));
		items.add(new Nexus("Nexus 7 (16 GB)", "Asus", "Jelly Bean", "16",
				"16 GB", "7\"", "1 GB"));
		items.add(new Nexus("Nexus 7 (32 GB)", "Asus", "Jelly Bean", "16",
				"32 GB", "7\"", "1 GB"));
		items.add(new Nexus("Nexus 10 (16 GB)", "Samsung", "Jelly Bean", "17",
				"16 GB", "10\"", "2 GB"));
		items.add(new Nexus("Nexus 10 (32 GB)", "Samsung", "Jelly Bean", "17",
				"32 GB", "10\"", "2 GB"));
		items.add(new Nexus("Nexus Q", "--", "Honeycomb", "13", "--", "--",
				"--"));
		return items;
	}
}
