package com.android.orion;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
	TextView mTextViewRoi = null;
	TextView mTextViewRoe = null;
	TextView mTextViewPE = null;
	TextView mTextViewRate = null;
	TextView mTextViewPB = null;
	TextView mTextViewValuation = null;
	TextView mTextViewHold = null;
	TextView mTextViewCost = null;
	TextView mTextViewBonus = null;
	TextView mTextViewTotalShare = null;
	TextView mTextViewMarketValue = null;
	TextView mTextViewNetProfit = null;
	TextView mDebtToNetAssetsRato = null;
	TextView mTextViewBookValuePerShare = null;
	TextView mTextViewCashFlowPerShare = null;
	TextView mTextViewNetProfitPerShare = null;
	TextView mTextViewNetProfitPerShareInYear = null;
	TextView mTextViewDividend = null;
	TextView mTextViewYield = null;
	TextView mTextViewDividendRatio = null;
	TextView mTextViewRDate = null;
	TextView mTextViewDate = null;

	ListView mLeftListView = null;
	ListView mRightListView = null;

	SimpleCursorAdapter mLeftAdapter = null;
	SimpleCursorAdapter mRightAdapter = null;

	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MESSAGE_REFRESH:
				if (mOrionService != null) {
					mStockDatabaseManager.deleteFinancialData();
					mStockDatabaseManager.deleteShareBonus();
					mOrionService.download(null);
					restartLoader();
				}
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_financial_list);

		mStockFilter.read();

		mSortOrder = Preferences.getString(mContext, Settings.KEY_SORT_ORDER_FINANCIAL_LIST,
				mSortOrderDefault);

		initHeader();

		initListView();

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

		case R.id.action_refresh:
			mHandler.sendEmptyMessage(MESSAGE_REFRESH);
			return true;

		case R.id.action_settings:
			startActivity(new Intent(this, ServiceSettingActivity.class));
			return true;

		case R.id.action_filter:
			startActivityForResult(new Intent(this, StockFilterActivity.class),
					REQUEST_CODE_STOCK_FILTER);
			return true;

		case R.id.action_load:
			performLoadFromFile();
			return true;

		case R.id.action_save:
			performSaveToFile();
			return true;

		case R.id.action_deal:
			startActivity(new Intent(this, DealListActivity.class));
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
				if (mOrionService != null) {
					mOrionService.download(mStock);
				}
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
	void onServiceConnected() {
		if (mOrionService != null) {
			mOrionService.download(null);
		}
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
		case R.id.roi:
			mSortOrderColumn = DatabaseContract.COLUMN_ROI;
			break;
		case R.id.roe:
			mSortOrderColumn = DatabaseContract.COLUMN_ROE;
			break;
		case R.id.pe:
			mSortOrderColumn = DatabaseContract.COLUMN_PE;
			break;
		case R.id.rate:
			mSortOrderColumn = DatabaseContract.COLUMN_RATE;
			break;
		case R.id.pb:
			mSortOrderColumn = DatabaseContract.COLUMN_PB;
			break;
		case R.id.valuation:
			mSortOrderColumn = DatabaseContract.COLUMN_VALUATION;
			break;
		case R.id.hold:
			mSortOrderColumn = DatabaseContract.COLUMN_HOLD;
			break;
		case R.id.cost:
			mSortOrderColumn = DatabaseContract.COLUMN_COST;
			break;
		case R.id.bonus:
			mSortOrderColumn = DatabaseContract.COLUMN_BONUS;
			break;
		case R.id.total_share:
			mSortOrderColumn = DatabaseContract.COLUMN_TOTAL_SHARE;
			break;
		case R.id.market_value:
			mSortOrderColumn = DatabaseContract.COLUMN_MARKET_VALUE;
			break;
		case R.id.net_profit:
			mSortOrderColumn = DatabaseContract.COLUMN_NET_PROFIT;
			break;
		case R.id.debt_to_net_assets_rato:
			mSortOrderColumn = DatabaseContract.COLUMN_DEBT_TO_NET_ASSETS_RATIO;
			break;
		case R.id.book_value_per_share:
			mSortOrderColumn = DatabaseContract.COLUMN_BOOK_VALUE_PER_SHARE;
			break;
		case R.id.cash_flow_per_share:
			mSortOrderColumn = DatabaseContract.COLUMN_CASH_FLOW_PER_SHARE;
			break;
		case R.id.net_profit_per_share:
			mSortOrderColumn = DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE;
			break;
		case R.id.net_profit_per_share_in_year:
			mSortOrderColumn = DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE_IN_YEAR;
			break;
		case R.id.dividend:
			mSortOrderColumn = DatabaseContract.COLUMN_DIVIDEND;
			break;
		case R.id.yield:
			mSortOrderColumn = DatabaseContract.COLUMN_YIELD;
			break;
		case R.id.dividend_ratio:
			mSortOrderColumn = DatabaseContract.COLUMN_DIVIDEND_RATIO;
			break;
		case R.id.r_date:
			mSortOrderColumn = DatabaseContract.COLUMN_R_DATE;
			break;
		case R.id.date:
			mSortOrderColumn = DatabaseContract.COLUMN_DATE;
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

		Preferences.putString(mContext, Settings.KEY_SORT_ORDER_FINANCIAL_LIST, mSortOrder);

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
		setHeaderTextColor(mTextViewRoi, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewRoe, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewPE, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewRate, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewPB, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewValuation, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewHold, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewCost, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewBonus, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewTotalShare, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewMarketValue, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewNetProfit, mHeaderTextDefaultColor);
		setHeaderTextColor(mDebtToNetAssetsRato, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewBookValuePerShare, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewCashFlowPerShare, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewNetProfitPerShare, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewNetProfitPerShareInYear,
				mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDividend, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewYield, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDividendRatio, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewRDate, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDate, mHeaderTextDefaultColor);
	}

	void setVisibility(String key, TextView textView) {
		if (textView != null) {
			if (Preferences.getBoolean(this, key, false)) {
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

		mTextViewMarketValue = (TextView) findViewById(R.id.market_value);
		if (mTextViewMarketValue != null) {
			mTextViewMarketValue.setOnClickListener(this);
		}

		mTextViewNetProfit = (TextView) findViewById(R.id.net_profit);
		if (mTextViewNetProfit != null) {
			mTextViewNetProfit.setOnClickListener(this);
		}

		mDebtToNetAssetsRato = (TextView) findViewById(R.id.debt_to_net_assets_rato);
		if (mDebtToNetAssetsRato != null) {
			mDebtToNetAssetsRato.setOnClickListener(this);
		}

		mTextViewBookValuePerShare = (TextView) findViewById(R.id.book_value_per_share);
		if (mTextViewBookValuePerShare != null) {
			mTextViewBookValuePerShare.setOnClickListener(this);
		}

		mTextViewCashFlowPerShare = (TextView) findViewById(R.id.cash_flow_per_share);
		if (mTextViewCashFlowPerShare != null) {
			mTextViewCashFlowPerShare.setOnClickListener(this);
		}

		mTextViewNetProfitPerShare = (TextView) findViewById(R.id.net_profit_per_share);
		if (mTextViewNetProfitPerShare != null) {
			mTextViewNetProfitPerShare.setOnClickListener(this);
		}

		mTextViewNetProfitPerShareInYear = (TextView) findViewById(R.id.net_profit_per_share_in_year);
		if (mTextViewNetProfitPerShareInYear != null) {
			mTextViewNetProfitPerShareInYear.setOnClickListener(this);
		}

		mTextViewRoi = (TextView) findViewById(R.id.roi);
		if (mTextViewRoi != null) {
			mTextViewRoi.setOnClickListener(this);
		}

		mTextViewRoe = (TextView) findViewById(R.id.roe);
		if (mTextViewRoe != null) {
			mTextViewRoe.setOnClickListener(this);
		}

		mTextViewPE = (TextView) findViewById(R.id.pe);
		if (mTextViewPE != null) {
			mTextViewPE.setOnClickListener(this);
		}

		mTextViewRate = (TextView) findViewById(R.id.rate);
		if (mTextViewRate != null) {
			mTextViewRate.setOnClickListener(this);
		}

		mTextViewPB = (TextView) findViewById(R.id.pb);
		if (mTextViewPB != null) {
			mTextViewPB.setOnClickListener(this);
		}

		mTextViewValuation = (TextView) findViewById(R.id.valuation);
		if (mTextViewValuation != null) {
			mTextViewValuation.setOnClickListener(this);
		}

		mTextViewHold = (TextView) findViewById(R.id.hold);
		if (mTextViewHold != null) {
			mTextViewHold.setOnClickListener(this);
		}

		mTextViewCost = (TextView) findViewById(R.id.cost);
		if (mTextViewCost != null) {
			mTextViewCost.setOnClickListener(this);
		}

		mTextViewBonus = (TextView) findViewById(R.id.bonus);
		if (mTextViewBonus != null) {
			mTextViewBonus.setOnClickListener(this);
		}

		mTextViewDividend = (TextView) findViewById(R.id.dividend);
		if (mTextViewDividend != null) {
			mTextViewDividend.setOnClickListener(this);
		}

		mTextViewYield = (TextView) findViewById(R.id.yield);
		if (mTextViewYield != null) {
			mTextViewYield.setOnClickListener(this);
		}

		mTextViewDividendRatio = (TextView) findViewById(R.id.dividend_ratio);
		if (mTextViewDividendRatio != null) {
			mTextViewDividendRatio.setOnClickListener(this);
		}

		mTextViewRDate = (TextView) findViewById(R.id.r_date);
		if (mTextViewRDate != null) {
			mTextViewRDate.setOnClickListener(this);
		}

		mTextViewDate = (TextView) findViewById(R.id.date);
		if (mTextViewDate != null) {
			mTextViewDate.setOnClickListener(this);
		}

		if (mSortOrder.contains(DatabaseContract.COLUMN_CODE)) {
			setHeaderTextColor(mTextViewNameCode, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PRICE)) {
			setHeaderTextColor(mTextViewPrice, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_NET)) {
			setHeaderTextColor(mTextViewNet, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_TOTAL_SHARE)) {
			setHeaderTextColor(mTextViewTotalShare, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_MARKET_VALUE)) {
			setHeaderTextColor(mTextViewMarketValue, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_NET_PROFIT)) {
			setHeaderTextColor(mTextViewNetProfit, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.COLUMN_DEBT_TO_NET_ASSETS_RATIO)) {
			setHeaderTextColor(mDebtToNetAssetsRato, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.COLUMN_BOOK_VALUE_PER_SHARE)) {
			setHeaderTextColor(mTextViewBookValuePerShare,
					mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.COLUMN_CASH_FLOW_PER_SHARE)) {
			setHeaderTextColor(mTextViewCashFlowPerShare,
					mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE)) {
			setHeaderTextColor(mTextViewNetProfitPerShare,
					mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE_IN_YEAR)) {
			setHeaderTextColor(mTextViewNetProfitPerShareInYear,
					mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_ROI)) {
			setHeaderTextColor(mTextViewRoi, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_ROE)) {
			setHeaderTextColor(mTextViewRoe, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PE)) {
			setHeaderTextColor(mTextViewPE, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_RATE)) {
			setHeaderTextColor(mTextViewRate, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PB)) {
			setHeaderTextColor(mTextViewPB, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_VALUATION)) {
			setHeaderTextColor(mTextViewValuation, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_HOLD)) {
			setHeaderTextColor(mTextViewHold, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_COST)) {
			setHeaderTextColor(mTextViewCost, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_BONUS)) {
			setHeaderTextColor(mTextViewBonus, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_DIVIDEND)) {
			setHeaderTextColor(mTextViewDividend, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_YIELD)) {
			setHeaderTextColor(mTextViewYield, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_DIVIDEND_RATIO)) {
			setHeaderTextColor(mTextViewDividendRatio,
					mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_R_DATE)) {
			setHeaderTextColor(mTextViewRDate, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_DATE)) {
			setHeaderTextColor(mTextViewDate, mHeaderTextHighlightColor);
		} else {
		}
	}

	void initListView() {
		String[] mLeftFrom = new String[] { DatabaseContract.COLUMN_NAME,
				DatabaseContract.COLUMN_CODE };
		int[] mLeftTo = new int[] { R.id.name, R.id.code };

		String[] mRightFrom = new String[] { DatabaseContract.COLUMN_PRICE,
				DatabaseContract.COLUMN_NET, DatabaseContract.COLUMN_ROI,
				DatabaseContract.COLUMN_ROE, DatabaseContract.COLUMN_PE,
				DatabaseContract.COLUMN_RATE, DatabaseContract.COLUMN_PB,
				DatabaseContract.COLUMN_VALUATION,
				DatabaseContract.COLUMN_HOLD, DatabaseContract.COLUMN_COST,
				DatabaseContract.COLUMN_BONUS,
				DatabaseContract.COLUMN_TOTAL_SHARE,
				DatabaseContract.COLUMN_MARKET_VALUE,
				DatabaseContract.COLUMN_NET_PROFIT,
				DatabaseContract.COLUMN_DEBT_TO_NET_ASSETS_RATIO,
				DatabaseContract.COLUMN_BOOK_VALUE_PER_SHARE,
				DatabaseContract.COLUMN_CASH_FLOW_PER_SHARE,
				DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE,
				DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE_IN_YEAR,
				DatabaseContract.COLUMN_DIVIDEND,
				DatabaseContract.COLUMN_YIELD,
				DatabaseContract.COLUMN_DIVIDEND_RATIO,
				DatabaseContract.COLUMN_R_DATE, DatabaseContract.COLUMN_DATE };
		int[] mRightTo = new int[] { R.id.price, R.id.net, R.id.roi, R.id.roe,
				R.id.pe, R.id.rate, R.id.pb, R.id.valuation, R.id.hold,
				R.id.cost, R.id.bonus, R.id.total_share, R.id.market_value,
				R.id.net_profit, R.id.debt_to_net_assets_rato,
				R.id.book_value_per_share, R.id.cash_flow_per_share,
				R.id.net_profit_per_share, R.id.net_profit_per_share_in_year,
				R.id.dividend, R.id.yield, R.id.dividend_ratio, R.id.r_date,
				R.id.date };

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

	void restartLoader(Intent intent) {
		restartLoader();
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
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		String selection = "";
		CursorLoader loader = null;

		switch (id) {
		case LOADER_ID_STOCK_FINANCIAL_LIST:
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
						StockFinancialChartListActivity.class);
				intent.putExtra(Constants.EXTRA_STOCK_LIST_SORT_ORDER,
						mSortOrder);
				intent.putExtra(Constants.EXTRA_STOCK_ID, id);
				startActivity(intent);
			} else {
				Intent intent = new Intent(this,
						StockDataChartListActivity.class);
				intent.putExtra(Constants.EXTRA_STOCK_LIST_SORT_ORDER,
						mSortOrder);
				intent.putExtra(Constants.EXTRA_STOCK_ID, id);
				startActivity(intent);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Intent intent = new Intent(this, StockListEditActivity.class);
		intent.putExtra(Constants.EXTRA_STOCK_LIST_SORT_ORDER, mSortOrder);
		startActivity(intent);
		return true;
	}

	boolean setTextViewValue(String key, View textView) {
		if (textView != null) {
			if (Preferences.getBoolean(this, key, false)) {
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
