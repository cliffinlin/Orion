package com.android.orion;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Setting;
import com.android.orion.database.Stock;
import com.android.orion.stocklist.StockCellViewGroup;
import com.android.orion.stocklist.StockTableFixHeaderAdapter;
import com.inqbarna.tablefixheaders.TableFixHeaders;
import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;
import com.miguelbcr.tablefixheaders.TableFixHeaderAdapter;

public class StockListActivity extends StorageActivity {
	public static final int EXECUTE_STOCK_LIST_LOAD = 1;

	String mSortOrderColumn = DatabaseContract.COLUMN_CODE;
	String mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_ASC;
	String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
	String mSortOrder = mSortOrderDefault;

	List<Stock> mStockList = new ArrayList<Stock>();

	TableFixHeaders mTableFixHeaders;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_list);

		mSortOrder = getSetting(Setting.KEY_SORT_ORDER_STOCK_LIST,
				mSortOrderDefault);

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
			Cursor cursor = null;

			try {
				mStockList = new ArrayList<Stock>();
				cursor = mStockDatabaseManager.queryStock(null, null,
						mSortOrder);
				if ((cursor != null) && (cursor.getCount() > 0)) {
					while (cursor.moveToNext()) {
						Stock stock = Stock.obtain();
						stock.set(cursor);
						mStockList.add(stock);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				mStockDatabaseManager.closeCursor(cursor);
			}

			break;

		default:
			break;
		}

		return RESULT_SUCCESS;
	}

	@Override
	void onPostExecuteLoad(Long result) {
		super.onPostExecuteLoad(result);

		mTableFixHeaders.setAdapter(getAdapter());
	}

	public BaseTableAdapter getAdapter() {
		StockTableFixHeaderAdapter adapter = new StockTableFixHeaderAdapter(
				mContext);

		adapter.setFirstHeader(mContext.getResources().getString(
				R.string.stock_name_code));
		adapter.setHeader(getHeader());
		adapter.setFirstBody(mStockList);
		adapter.setBody(mStockList);
		adapter.setSection(mStockList);

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
}
