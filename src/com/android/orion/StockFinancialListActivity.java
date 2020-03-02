package com.android.orion;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Setting;
import com.android.orion.database.Stock;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.Utility;

public class StockFinancialListActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
		OnItemLongClickListener, OnClickListener {

	public static final String ACTION_STOCK_ID = "orion.intent.action.ACTION_STOCK_ID";
	public static final int LOADER_ID_STOCK_FINANCIAL_LIST = 0;
	public static final int EXECUTE_STOCK_FINANCIAL_LOAD = 1;

	public static final int REQUEST_CODE_STOCK_INSERT = 0;
	public static final int REQUEST_CODE_STOCK_FILTER = 1;

	static final int mHeaderTextDefaultColor = Color.BLACK;
	static final int mHeaderTextHighlightColor = Color.RED;

	String mSortOrderColumn = DatabaseContract.COLUMN_CODE;
	String mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_ASC;
	String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
	String mSortOrder = mSortOrderDefault;

	SyncHorizontalScrollView mTitleSHSV = null;
	SyncHorizontalScrollView mContentSHSV = null;

	TextView mTextViewNameCode = null;
	TextView mTextViewPrice = null;
	TextView mTextViewNet = null;
	TextView mTextViewTotalShare = null;
	TextView mTextViewBookValuePerShare = null;
	TextView mTextViewNetProfit = null;
	TextView mTextViewNetProfitPerShare = null;
	TextView mTextViewRate = null;
	TextView mTextViewRoe = null;
	TextView mTextViewValuation = null;
	TextView mTextViewDiscount = null;
	TextView mTextViewPE = null;
	TextView mTextViewPB = null;
	TextView mTextViewDividend = null;
	TextView mTextViewYield = null;
	TextView mTextViewDelta = null;

	ListView mLeftListView = null;
	ListView mRightListView = null;

	SimpleCursorAdapter mLeftAdapter = null;
	SimpleCursorAdapter mRightAdapter = null;

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mResumed) {
				if (intent.getIntExtra(Constants.EXTRA_SERVICE_TYPE,
						Constants.SERVICE_TYPE_NONE) == Constants.SERVICE_DATABASE_UPDATE) {
					if (System.currentTimeMillis() - mLastRestartLoader > Constants.DEFAULT_RESTART_LOADER_INTERAL) {
						mLastRestartLoader = System.currentTimeMillis();
						restartLoader();
					}
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_financial_list);

		mStockFilter.read();

		mSortOrder = getSetting(Setting.KEY_SORT_ORDER_STOCK_LIST,
				mSortOrderDefault);

		initHeader();

		initListView();

		LocalBroadcastManager.getInstance(this).registerReceiver(
				mBroadcastReceiver,
				new IntentFilter(Constants.ACTION_SERVICE_FINISHED));

		mLoaderManager.initLoader(LOADER_ID_STOCK_FINANCIAL_LIST, null, this);

		if (!Utility.isNetworkConnected(this)) {
			Toast.makeText(this,
					getResources().getString(R.string.network_unavailable),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_list, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		case R.id.action_new:
			Intent intent = new Intent(this, StockActivity.class);
			intent.setAction(StockActivity.ACTION_STOCK_INSERT);
			startActivityForResult(intent, REQUEST_CODE_STOCK_INSERT);
			return true;

		case R.id.action_search:
			startActivity(new Intent(this, StockSearchActivity.class));
			return true;

		case R.id.action_deal:
			startActivity(new Intent(this, DealListActivity.class));
			return true;

		case R.id.action_settings:
			startActivityForResult(new Intent(this, StockFilterActivity.class),
					REQUEST_CODE_STOCK_FILTER);
			return true;

		case R.id.action_save_to_file:
			performSaveToFile();
			return true;

		case R.id.action_load_from_file:
			performLoadFromFile();
			return true;

		case R.id.action_refresh:
			mHandler.sendEmptyMessage(MESSAGE_REFRESH);
			return true;

		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_STOCK_INSERT:
				mHandler.sendEmptyMessage(MESSAGE_REFRESH);
				break;

			case REQUEST_CODE_STOCK_FILTER:
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					mStockFilter.get(bundle);
				}
				break;

			default:
				break;
			}
		}
	}

	@Override
	void onMessageRefresh() {
		startLoadTask(EXECUTE_STOCK_FINANCIAL_LOAD);
	}

	Long doInBackgroundLoad(Object... params) {
		super.doInBackgroundLoad(params);
		int execute = (Integer) params[0];

		switch (execute) {
		case EXECUTE_STOCK_FINANCIAL_LOAD:
			if (mOrionService != null) {
				mOrionService.downloadFinancialData();
				mOrionService.downloadShareBonus();
			}
			break;

		default:
			break;
		}

		return RESULT_SUCCESS;
	}

	void onPostExecuteLoad(Long result) {
		super.onPostExecuteLoad(result);
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();

		resetHeaderTextColor();
		setHeaderTextColor(id, mHeaderTextHighlightColor);

		switch (id) {
		case R.id.stock_name_code:
			mSortOrderColumn = DatabaseContract.COLUMN_CODE;
			break;
		case R.id.price:
			mSortOrderColumn = DatabaseContract.COLUMN_PRICE;
			break;
		case R.id.net:
			mSortOrderColumn = DatabaseContract.COLUMN_NET;
			break;
		case R.id.total_share:
			mSortOrderColumn = DatabaseContract.COLUMN_TOTAL_SHARE;
			break;
		case R.id.book_value_per_share:
			mSortOrderColumn = DatabaseContract.COLUMN_BOOK_VALUE_PER_SHARE;
			break;
		case R.id.net_profit:
			mSortOrderColumn = DatabaseContract.COLUMN_NET_PROFIT;
			break;
		case R.id.net_profit_per_share:
			mSortOrderColumn = DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE;
			break;
		case R.id.rate:
			mSortOrderColumn = DatabaseContract.COLUMN_RATE;
			break;
		case R.id.roe:
			mSortOrderColumn = DatabaseContract.COLUMN_ROE;
			break;
		case R.id.valuation:
			mSortOrderColumn = DatabaseContract.COLUMN_VALUATION;
			break;
		case R.id.discount:
			mSortOrderColumn = DatabaseContract.COLUMN_DISCOUNT;
			break;
		case R.id.pe:
			mSortOrderColumn = DatabaseContract.COLUMN_PE;
			break;
		case R.id.pb:
			mSortOrderColumn = DatabaseContract.COLUMN_PB;
			break;
		case R.id.dividend:
			mSortOrderColumn = DatabaseContract.COLUMN_DIVIDEND;
			break;
		case R.id.yield:
			mSortOrderColumn = DatabaseContract.COLUMN_YIELD;
			break;
		case R.id.delta:
			mSortOrderColumn = DatabaseContract.COLUMN_DELTA;
			break;
		default:
			mSortOrderColumn = DatabaseContract.COLUMN_CODE;
			break;
		}

		if (mSortOrderDirection.equals(DatabaseContract.ORDER_DIRECTION_ASC)) {
			mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_DESC;
		} else {
			mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_ASC;
		}

		mSortOrder = mSortOrderColumn + mSortOrderDirection;

		saveSetting(Setting.KEY_SORT_ORDER_STOCK_LIST, mSortOrder);

		restartLoader();
	}

	void setHeaderTextColor(int id, int color) {
		TextView textView = (TextView) findViewById(id);
		setHeaderTextColor(textView, color);
	}

	void setHeaderTextColor(TextView textView, int color) {
		if (textView != null) {
			textView.setTextColor(color);
		}
	}

	void resetHeaderTextColor() {
		setHeaderTextColor(mTextViewNameCode, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewPrice, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewNet, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewTotalShare, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewBookValuePerShare, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewNetProfit, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewNetProfitPerShare, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewRate, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewRoe, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewValuation, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDiscount, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewPE, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewPB, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDividend, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewYield, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDelta, mHeaderTextDefaultColor);
	}

	void setVisibility(String key, TextView textView) {
		if (textView != null) {
			if (Preferences.readBoolean(this, key, false)) {
				textView.setVisibility(View.VISIBLE);
			} else {
				textView.setVisibility(View.GONE);
			}
		}
	}

	void initHeader() {
		mTitleSHSV = (SyncHorizontalScrollView) findViewById(R.id.title_shsv);
		mContentSHSV = (SyncHorizontalScrollView) findViewById(R.id.content_shsv);

		if (mTitleSHSV != null && mContentSHSV != null) {
			mTitleSHSV.setScrollView(mContentSHSV);
			mContentSHSV.setScrollView(mTitleSHSV);
		}

		mTextViewNameCode = (TextView) findViewById(R.id.stock_name_code);
		if (mTextViewNameCode != null) {
			mTextViewNameCode.setOnClickListener(this);
		}

		mTextViewPrice = (TextView) findViewById(R.id.price);
		if (mTextViewPrice != null) {
			mTextViewPrice.setOnClickListener(this);
		}

		mTextViewNet = (TextView) findViewById(R.id.net);
		if (mTextViewNet != null) {
			mTextViewNet.setOnClickListener(this);
		}

		mTextViewTotalShare = (TextView) findViewById(R.id.total_share);
		if (mTextViewTotalShare != null) {
			mTextViewTotalShare.setOnClickListener(this);
		}

		mTextViewBookValuePerShare = (TextView) findViewById(R.id.book_value_per_share);
		if (mTextViewBookValuePerShare != null) {
			mTextViewBookValuePerShare.setOnClickListener(this);
		}

		mTextViewNetProfit = (TextView) findViewById(R.id.net_profit);
		if (mTextViewNetProfit != null) {
			mTextViewNetProfit.setOnClickListener(this);
		}

		mTextViewNetProfitPerShare = (TextView) findViewById(R.id.net_profit_per_share);
		if (mTextViewNetProfitPerShare != null) {
			mTextViewNetProfitPerShare.setOnClickListener(this);
		}

		mTextViewRate = (TextView) findViewById(R.id.rate);
		if (mTextViewRate != null) {
			mTextViewRate.setOnClickListener(this);
		}

		mTextViewRoe = (TextView) findViewById(R.id.roe);
		if (mTextViewRoe != null) {
			mTextViewRoe.setOnClickListener(this);
		}

		mTextViewValuation = (TextView) findViewById(R.id.valuation);
		if (mTextViewValuation != null) {
			mTextViewValuation.setOnClickListener(this);
		}

		mTextViewDiscount = (TextView) findViewById(R.id.discount);
		if (mTextViewDiscount != null) {
			mTextViewDiscount.setOnClickListener(this);
		}

		mTextViewPE = (TextView) findViewById(R.id.pe);
		if (mTextViewPE != null) {
			mTextViewPE.setOnClickListener(this);
		}

		mTextViewPB = (TextView) findViewById(R.id.pb);
		if (mTextViewPB != null) {
			mTextViewPB.setOnClickListener(this);
		}

		mTextViewDividend = (TextView) findViewById(R.id.dividend);
		if (mTextViewDividend != null) {
			mTextViewDividend.setOnClickListener(this);
		}

		mTextViewYield = (TextView) findViewById(R.id.yield);
		if (mTextViewYield != null) {
			mTextViewYield.setOnClickListener(this);
		}

		mTextViewDelta = (TextView) findViewById(R.id.delta);
		if (mTextViewDelta != null) {
			mTextViewDelta.setOnClickListener(this);
		}

		if (mSortOrder.contains(DatabaseContract.COLUMN_CODE)) {
			setHeaderTextColor(mTextViewNameCode, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PRICE)) {
			setHeaderTextColor(mTextViewPrice, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_NET)) {
			setHeaderTextColor(mTextViewNet, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_TOTAL_SHARE)) {
			setHeaderTextColor(mTextViewTotalShare, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.COLUMN_BOOK_VALUE_PER_SHARE)) {
			setHeaderTextColor(mTextViewBookValuePerShare,
					mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_NET_PROFIT)) {
			setHeaderTextColor(mTextViewNetProfit, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE)) {
			setHeaderTextColor(mTextViewNetProfitPerShare,
					mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_RATE)) {
			setHeaderTextColor(mTextViewRate, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_ROE)) {
			setHeaderTextColor(mTextViewRoe, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_VALUATION)) {
			setHeaderTextColor(mTextViewValuation, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_DISCOUNT)) {
			setHeaderTextColor(mTextViewDiscount, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PE)) {
			setHeaderTextColor(mTextViewPE, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PB)) {
			setHeaderTextColor(mTextViewPB, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_DIVIDEND)) {
			setHeaderTextColor(mTextViewDividend, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_YIELD)) {
			setHeaderTextColor(mTextViewYield, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_DELTA)) {
			setHeaderTextColor(mTextViewDelta, mHeaderTextHighlightColor);
		} else {
		}
	}

	void initListView() {
		String[] mLeftFrom = new String[] { DatabaseContract.COLUMN_NAME,
				DatabaseContract.COLUMN_CODE };
		int[] mLeftTo = new int[] { R.id.name, R.id.code };

		String[] mRightFrom = new String[] { DatabaseContract.COLUMN_PRICE,
				DatabaseContract.COLUMN_NET,
				DatabaseContract.COLUMN_TOTAL_SHARE,
				DatabaseContract.COLUMN_BOOK_VALUE_PER_SHARE,
				DatabaseContract.COLUMN_NET_PROFIT,
				DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE,
				DatabaseContract.COLUMN_RATE, DatabaseContract.COLUMN_ROE,
				DatabaseContract.COLUMN_VALUATION,
				DatabaseContract.COLUMN_DISCOUNT, DatabaseContract.COLUMN_PE,
				DatabaseContract.COLUMN_PB, DatabaseContract.COLUMN_DIVIDEND,
				DatabaseContract.COLUMN_YIELD, DatabaseContract.COLUMN_DELTA };
		int[] mRightTo = new int[] { R.id.price, R.id.net, R.id.total_share,
				R.id.book_value_per_share, R.id.net_profit,
				R.id.net_profit_per_share, R.id.rate, R.id.roe, R.id.valuation,
				R.id.discount, R.id.pe, R.id.pb, R.id.dividend, R.id.yield,
				R.id.delta };

		mLeftListView = (ListView) findViewById(R.id.left_listview);
		mLeftAdapter = new SimpleCursorAdapter(this,
				R.layout.activity_stock_list_left_item, null, mLeftFrom,
				mLeftTo, 0);
		if ((mLeftListView != null) && (mLeftAdapter != null)) {
			mLeftListView.setAdapter(mLeftAdapter);
			mLeftListView.setOnItemClickListener(this);
			mLeftListView.setOnItemLongClickListener(this);
		}

		mRightListView = (ListView) findViewById(R.id.right_listview);
		mRightAdapter = new SimpleCursorAdapter(this,
				R.layout.activity_stock_financial_list_right_item, null,
				mRightFrom, mRightTo, 0);
		if ((mRightListView != null) && (mRightAdapter != null)) {
			mRightAdapter.setViewBinder(new CustomViewBinder());
			mRightListView.setAdapter(mRightAdapter);
			mRightListView.setOnItemClickListener(this);
			mRightListView.setOnItemLongClickListener(this);
		}
	}

	void restartLoader() {
		mLoaderManager
				.restartLoader(LOADER_ID_STOCK_FINANCIAL_LIST, null, this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		restartLoader();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mBroadcastReceiver);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		String selection = "";
		CursorLoader loader = null;

		switch (id) {
		case LOADER_ID_STOCK_FINANCIAL_LIST:
			selection = DatabaseContract.Stock.COLUMN_MARK + " = '"
					+ Constants.STOCK_FLAG_MARK_FAVORITE + "'";

			selection += mStockFilter.getSelection();

			loader = new CursorLoader(this, DatabaseContract.Stock.CONTENT_URI,
					DatabaseContract.Stock.PROJECTION_ALL, selection, null,
					mSortOrder);

			mStockList.clear();
			break;

		default:
			break;
		}

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader == null) {
			return;
		}

		switch (loader.getId()) {
		case LOADER_ID_STOCK_FINANCIAL_LIST:
			mLeftAdapter.swapCursor(cursor);
			mRightAdapter.swapCursor(cursor);

			if ((cursor != null) && cursor.getCount() > 0) {
				cursor.moveToPosition(-1);
				while (cursor.moveToNext()) {
					Stock stock = new Stock();
					stock.set(cursor);
					mStockList.add(stock);
				}
			}
			break;

		default:
			break;
		}

		setListViewHeightBasedOnChildren(mLeftListView);
		setListViewHeightBasedOnChildren(mRightListView);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mLeftAdapter.swapCursor(null);
		mRightAdapter.swapCursor(null);

		mStockList.clear();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		if (id <= Constants.STOCK_ID_INVALID) {
			return;
		}

		if (ACTION_STOCK_ID.equals(mAction)) {
			if (mIntent != null) {
				mIntent.putExtra(Constants.EXTRA_STOCK_ID, id);
				setResult(RESULT_OK, mIntent);
				finish();
			}
		} else {
			if (parent.getId() == R.id.left_listview) {
				Intent intent = new Intent(this,
						FinancialDataChartListActivity.class);
				intent.putExtra(Setting.KEY_SORT_ORDER_STOCK_LIST, mSortOrder);
				intent.putExtra(Constants.EXTRA_STOCK_ID, id);
				startActivity(intent);
			} else {
				Intent intent = new Intent(this,
						StockDataChartListActivity.class);
				intent.putExtra(Setting.KEY_SORT_ORDER_STOCK_LIST, mSortOrder);
				intent.putExtra(Constants.EXTRA_STOCK_ID, id);
				startActivity(intent);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Intent intent = new Intent(this, StockFavoriteEditActivity.class);
		intent.putExtra(Setting.KEY_SORT_ORDER_STOCK_LIST, mSortOrder);
		startActivity(intent);
		return true;
	}

	boolean setTextViewValue(String key, View textView) {
		if (textView != null) {
			if (Preferences.readBoolean(this, key, false)) {
				textView.setVisibility(View.VISIBLE);
				return false;
			} else {
				textView.setVisibility(View.GONE);
				return true;
			}
		}

		return false;
	}

	private class CustomViewBinder implements SimpleCursorAdapter.ViewBinder {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if ((view == null) || (cursor == null) || (columnIndex == -1)) {
				return false;
			}

			if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN5)) {
				return setTextViewValue(Constants.PERIOD_MIN5, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN15)) {
				return setTextViewValue(Constants.PERIOD_MIN15, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN30)) {
				return setTextViewValue(Constants.PERIOD_MIN30, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN60)) {
				return setTextViewValue(Constants.PERIOD_MIN60, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_DAY)) {
				return setTextViewValue(Constants.PERIOD_DAY, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_WEEK)) {
				return setTextViewValue(Constants.PERIOD_WEEK, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MONTH)) {
				return setTextViewValue(Constants.PERIOD_MONTH, view);
			}

			return false;
		}
	}
}
