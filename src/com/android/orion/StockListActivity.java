package com.android.orion;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Setting;
import com.android.orion.database.Stock;
import com.inqbarna.tablefixheaders.TableFixHeaders;
import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;

public class StockListActivity extends StorageActivity {
	public static final int EXECUTE_STOCK_LIST_LOAD = 1;

	float mDensity = 0;
	int[] mWidths = { 90, 60, 60, 60, 60, 60, 60, 60, 60, 60 };

	String mSortOrderColumn = DatabaseContract.COLUMN_CODE;
	String mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_ASC;
	String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
	String mSortOrder = mSortOrderDefault;

	List<String> mHeaderList = new ArrayList<String>();
	List<Stock> mStockList = new ArrayList<Stock>();

	TableFixHeaders mTableFixHeaders;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.table);

		mDensity = getResources().getDisplayMetrics().density;

		mHeaderList = getHeader();

		mSortOrder = getSetting(Setting.KEY_SORT_ORDER_STOCK_LIST,
				mSortOrderDefault);

		mTableFixHeaders = (TableFixHeaders) findViewById(R.id.table);

		mTableFixHeaders.setAdapter(new StockTableAdapter());

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
	void onPostExecuteLoad(Long result) {
		super.onPostExecuteLoad(result);

		mTableFixHeaders.setAdapter(new StockTableAdapter());
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

	List<String> getHeader() {
		List<String> headerStrings = new ArrayList<String>();

		headerStrings.add(mContext.getResources().getString(
				R.string.stock_name_code));
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
		headerStrings.add(mContext.getResources().getString(
				R.string.stock_action_day));
		headerStrings.add(mContext.getResources().getString(
				R.string.stock_action_week));
		headerStrings.add(mContext.getResources().getString(
				R.string.stock_action_month));

		return headerStrings;
	}

	public class StockTableAdapter extends BaseTableAdapter {

		public StockTableAdapter() {
		}

		@Override
		public int getRowCount() {
			return mStockList.size();
		}

		@Override
		public int getColumnCount() {
			return mHeaderList.size() - 1;
		}

		@Override
		public View getView(int row, int column, View convertView,
				ViewGroup parent) {
			View view = null;

			switch (getItemViewType(row, column)) {
			case 0:
				view = getFirstHeader(row, column, convertView, parent);
				break;
			case 1:
				view = getHeader(row, column, convertView, parent);
				break;
			case 2:
				view = getFirstBody(row, column, convertView, parent);
				break;
			case 3:
				view = getBody(row, column, convertView, parent);
				break;
			default:
				break;
			}

			return view;
		}

		private View getFirstHeader(int row, int column, View convertView,
				ViewGroup parent) {
			TextView textView;

			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.item_table_header_first, parent, false);
			}
			convertView
					.setBackgroundResource(R.drawable.cell_header_border_bottom_right_gray);

			textView = (TextView) convertView.findViewById(android.R.id.text1);
			textView.setTypeface(null, Typeface.BOLD);
			textView.setGravity(Gravity.CENTER);
			textView.setText(mHeaderList.get(0));

			return convertView;
		}

		private View getHeader(int row, int column, View convertView,
				ViewGroup parent) {
			TextView textView;

			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.item_table_header, parent, false);
			}
			convertView
					.setBackgroundResource(R.drawable.cell_header_border_bottom_right_gray);

			textView = (TextView) convertView.findViewById(android.R.id.text1);
			textView.setTypeface(null, Typeface.BOLD);
			textView.setGravity(Gravity.CENTER);
			textView.setText(mHeaderList.get(column + 1));

			return convertView;
		}

		private View getFirstBody(int row, int column, View convertView,
				ViewGroup parent) {
			TextView textView;

			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.item_table_first, parent, false);
			}
			convertView
					.setBackgroundResource(row % 2 == 0 ? R.drawable.cell_lightgray_border_bottom_right_gray
							: R.drawable.cell_white_border_bottom_right_gray);

			textView = (TextView) convertView.findViewById(android.R.id.text1);
			textView.setTypeface(null, Typeface.NORMAL);
			textView.setGravity(Gravity.CENTER);
			if (mStockList.size() > row) {
				textView.setText(mStockList.get(row).getName() + "\n"
						+ mStockList.get(row).getCode());
			}

			return convertView;
		}

		private View getBody(int row, int column, View convertView,
				ViewGroup parent) {
			TextView textView;
			String textString = "";

			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.item_table,
						parent, false);
			}
			convertView
					.setBackgroundResource(row % 2 == 0 ? R.drawable.cell_lightgray_border_bottom_right_gray
							: R.drawable.cell_white_border_bottom_right_gray);

			textView = (TextView) convertView.findViewById(android.R.id.text1);
			textView.setTypeface(null, Typeface.NORMAL);
			textView.setGravity(Gravity.CENTER);

			if (mStockList.size() > row) {
				switch (column) {
				case 0:
					textString = String.valueOf(mStockList.get(row).getPrice());
					break;
				case 1:
					textString = String.valueOf(mStockList.get(row).getNet());
					break;
				case 2:
					textString = mStockList.get(row).getAction(
							Constants.PERIOD_5MIN);
					break;
				case 3:
					textString = mStockList.get(row).getAction(
							Constants.PERIOD_15MIN);
					break;
				case 4:
					textString = mStockList.get(row).getAction(
							Constants.PERIOD_30MIN);
					break;
				case 5:
					textString = mStockList.get(row).getAction(
							Constants.PERIOD_60MIN);
					break;
				case 6:
					textString = mStockList.get(row).getAction(
							Constants.PERIOD_DAY);
					break;
				case 7:
					textString = mStockList.get(row).getAction(
							Constants.PERIOD_WEEK);
					break;
				case 8:
					textString = mStockList.get(row).getAction(
							Constants.PERIOD_MONTH);
					break;
				default:
					break;
				}
			}

			textView.setText(textString);

			return convertView;
		}

		@Override
		public int getWidth(int column) {
			return Math.round(mWidths[column + 1] * mDensity);
		}

		@Override
		public int getHeight(int row) {
			final int height;
			if (row == -1) {
				height = 35;
			} else {
				height = 45;
			}

			return Math.round(height * mDensity);
		}

		@Override
		public int getItemViewType(int row, int column) {
			final int itemViewType;
			if (row == -1 && column == -1) {
				itemViewType = 0;
			} else if (row == -1) {
				itemViewType = 1;
			} else if (column == -1) {
				itemViewType = 2;
			} else {
				itemViewType = 3;
			}

			return itemViewType;
		}

		@Override
		public int getViewTypeCount() {
			return 4;
		}
	}
}
