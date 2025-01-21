package com.android.orion.activity;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.ActionMode;
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

import com.android.orion.R;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockQuant;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Preferences;
import com.android.orion.view.SyncHorizontalScrollView;

import java.util.ArrayList;

public class StockTrendListActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
		OnItemLongClickListener, OnClickListener {

	static final int LOADER_ID_QUANT_LIST = 0;

	static final int FILTER_TYPE_NONE = 0;
	static final int FILTER_TYPE_OPERATE = 1;
	static final int FILTER_TYPE_BUY = 2;
	static final int FILTER_TYPE_SELL = 3;
	static final int FILTER_TYPE_ALL = 4;

	static final int MESSAGE_DELETE_DEAL = 0;
	static final int MESSAGE_DELETE_DEAL_LIST = 1;

	static final int MESSAGE_VIEW_QUANT_DEAL = 4;
	static final int MESSAGE_VIEW_STOCK_CHAT = 5;
	static final int MESSAGE_VIEW_STOCK_TREND = 6;

	static final int REQUEST_CODE_DEAL_INSERT = 0;
	static final int REQUEST_CODE_DEAL_EDIT = 1;

	static final int mHeaderTextDefaultColor = Color.BLACK;
	static final int mHeaderTextHighlightColor = Color.RED;

	String mSelection = null;
	String mSortOrderColumn = DatabaseContract.COLUMN_ID;
	String mSortOrderDirection = DatabaseContract.ORDER_ASC;
	String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
	String mSortOrder = mSortOrderDefault;
	String mGroupBy = "";

	SyncHorizontalScrollView mTitleSHSV = null;
	SyncHorizontalScrollView mContentSHSV = null;

	TextView mTextViewNameCode = null;
	TextView mTextViewId = null;
	TextView mTextViewPrice = null;
	TextView mTextViewNet = null;
	TextView mTextViewBuy = null;
	TextView mTextViewSell = null;
	TextView mTextViewVolume = null;
	TextView mTextViewValue = null;
	TextView mTextViewFee = null;
	TextView mTextViewProfit = null;
	TextView mTextViewAction = null;
	TextView mTextViewHold = null;
	TextView mTextViewValuation = null;
	TextView mTextViewQuantProfit = null;
	TextView mTextViewQuantProfitMargin = null;
	TextView mTextViewQuantX = null;
	TextView mTextViewThreshold = null;
	TextView mTextViewCreated = null;
	TextView mTextViewModified = null;

	ListView mLeftListView = null;
	ListView mRightListView = null;

	SimpleCursorAdapter mLeftAdapter = null;
	SimpleCursorAdapter mRightAdapter = null;

	ActionMode mCurrentActionMode = null;

	StockQuant mStockQuant = new StockQuant();

	Stock mStock = new Stock();

	int mFilterType = FILTER_TYPE_NONE;

	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			Intent intent = null;

			switch (msg.what) {
				case MESSAGE_DELETE_DEAL:
					break;

				case MESSAGE_DELETE_DEAL_LIST:
					break;

				case MESSAGE_VIEW_QUANT_DEAL:
					getStock();

					intent = new Intent(mContext,
							StockQuantDealListActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString(Constant.EXTRA_STOCK_SE, mStock.getSE());
					bundle.putString(Constant.EXTRA_STOCK_CODE, mStock.getCode());
					intent.putExtras(bundle);
					startActivity(intent);
					break;

				case MESSAGE_VIEW_STOCK_CHAT:
					getStock();

					ArrayList<String> stockIDList = new ArrayList<String>();
					for (Stock stock : mStockList) {
						stockIDList.add(String.valueOf(stock.getId()));
					}

					intent = new Intent(mContext, StockFavoriteChartListActivity.class);
					intent.putExtra(Constant.EXTRA_STOCK_ID, mStock.getId());
					intent.putStringArrayListExtra(Constant.EXTRA_STOCK_ID_LIST,
							stockIDList);
					intent.putExtra(Constant.EXTRA_STOCK_QUANT, true);
					startActivity(intent);
					break;

				case MESSAGE_VIEW_STOCK_TREND:
//					mDatabaseManager.getStock(mStock);
//
//					mIntent = new Intent(mContext, StockTrendListActivity.class);
//					mIntent.setAction(Constant.ACTION_STOCK_TREND_LIST);
//					mIntent.putExtra(Constant.EXTRA_STOCK_ID, mStock.getId());
//					startActivity(mIntent);

					intent = new Intent(mContext, MachineLearningChartListActivity.class);
					startActivity(intent);
					break;

				default:
					break;
			}
		}
	};

	ContentObserver mContentObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);
			// restartLoader();
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
		}
	};

	private ActionMode.Callback mModeCallBack = new ActionMode.Callback() {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.setTitle("Actions");
			mode.getMenuInflater().inflate(R.menu.stock_trend_list_action, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
				case R.id.menu_edit:
					return true;
				case R.id.menu_delete:
					return true;
				default:
					return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mCurrentActionMode = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_trend_list);

		mFilterType = FILTER_TYPE_ALL;

		mSortOrder = Preferences.getString(Setting.SETTING_SORT_ORDER_QUANT_LIST,
				mSortOrderDefault);

		initHeader();

		initListView();

		mLoaderManager.initLoader(LOADER_ID_QUANT_LIST, null, this);

		getContentResolver().registerContentObserver(
				DatabaseContract.StockQuant.CONTENT_URI, true, mContentObserver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_trend_list, menu);
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
				return true;

			case R.id.action_none:
				mFilterType = FILTER_TYPE_NONE;
				restartLoader();
				return true;

			case R.id.action_operate:
				mFilterType = FILTER_TYPE_OPERATE;
				restartLoader();
				return true;

			case R.id.action_buy:
				mFilterType = FILTER_TYPE_BUY;
				restartLoader();
				return true;

			case R.id.action_sell:
				mFilterType = FILTER_TYPE_SELL;
				restartLoader();
				return true;

			case R.id.action_all:
				mFilterType = FILTER_TYPE_ALL;
				restartLoader();
				return true;

			case R.id.action_trend:
				mHandler.sendEmptyMessage(MESSAGE_VIEW_STOCK_TREND);
				return true;

			default:
				return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case REQUEST_CODE_DEAL_INSERT:
				case REQUEST_CODE_DEAL_EDIT:
					break;

				default:
					break;
			}
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
			case R.id.account:
				mSortOrderColumn = DatabaseContract.COLUMN_ACCOUNT;
				break;

			case R.id.id:
				mSortOrderColumn = BaseColumns._ID;
				break;
			case R.id.price:
				mSortOrderColumn = DatabaseContract.COLUMN_PRICE;
				break;
			case R.id.net:
				mSortOrderColumn = DatabaseContract.COLUMN_NET;
				break;
			case R.id.buy:
				mSortOrderColumn = DatabaseContract.COLUMN_BUY;
				break;
			case R.id.sell:
				mSortOrderColumn = DatabaseContract.COLUMN_SELL;
				break;
			case R.id.volume:
				mSortOrderColumn = DatabaseContract.COLUMN_VOLUME;
				break;
			case R.id.value:
				mSortOrderColumn = DatabaseContract.COLUMN_VALUE;
				break;
			case R.id.fee:
				mSortOrderColumn = DatabaseContract.COLUMN_FEE;
				break;
			case R.id.profit:
				mSortOrderColumn = DatabaseContract.COLUMN_PROFIT;
				break;
			case R.id.action:
				mSortOrderColumn = DatabaseContract.COLUMN_ACTION;
				break;
			case R.id.hold:
				mSortOrderColumn = DatabaseContract.COLUMN_HOLD;
				break;
			case R.id.valuation:
				mSortOrderColumn = DatabaseContract.COLUMN_VALUATION;
				break;
			case R.id.quant_profit:
				mSortOrderColumn = DatabaseContract.COLUMN_QUANT_PROFIT;
				break;
			case R.id.quant_profit_margin:
				mSortOrderColumn = DatabaseContract.COLUMN_QUANT_PROFIT_MARGIN;
				break;
			case R.id.quant_x:
				mSortOrderColumn = DatabaseContract.COLUMN_QUANT_X;
				break;
			case R.id.threshold:
				mSortOrderColumn = DatabaseContract.COLUMN_THRESHOLD;
				break;
			case R.id.created:
				mSortOrderColumn = DatabaseContract.COLUMN_CREATED;
				break;
			case R.id.modified:
				mSortOrderColumn = DatabaseContract.COLUMN_MODIFIED;
				break;
			default:
				mSortOrderColumn = DatabaseContract.COLUMN_CODE;
				break;
		}

		if (TextUtils.equals(mSortOrderDirection, DatabaseContract.ORDER_ASC)) {
			mSortOrderDirection = DatabaseContract.ORDER_DESC;
		} else {
			mSortOrderDirection = DatabaseContract.ORDER_ASC;
		}

		mSortOrder = mSortOrderColumn + mSortOrderDirection;

		Preferences.putString(Setting.SETTING_SORT_ORDER_QUANT_LIST, mSortOrder);

		restartLoader();
	}

	void setHeaderTextColor(int id, int color) {
		TextView textView = findViewById(id);
		setHeaderTextColor(textView, color);
	}

	void setHeaderTextColor(TextView textView, int color) {
		if (textView != null) {
			textView.setTextColor(color);
		}
	}

	void resetHeaderTextColor() {
		setHeaderTextColor(mTextViewNameCode, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewId, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewPrice, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewNet, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewBuy, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewSell, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewVolume, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewValue, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewFee, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewProfit, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewAction, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewHold, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewValuation, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewQuantProfit, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewQuantProfitMargin, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewQuantX, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewThreshold, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewCreated, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewModified, mHeaderTextDefaultColor);
	}

	void setVisibility(String key, TextView textView) {
		if (textView != null) {
			if (Preferences.getBoolean(key, false)) {
				textView.setVisibility(View.VISIBLE);
			} else {
				textView.setVisibility(View.GONE);
			}
		}
	}

	void initHeader() {
		mTitleSHSV = findViewById(R.id.title_shsv);
		mContentSHSV = findViewById(R.id.content_shsv);

		if (mTitleSHSV != null && mContentSHSV != null) {
			mTitleSHSV.setScrollView(mContentSHSV);
			mContentSHSV.setScrollView(mTitleSHSV);
		}

		mTextViewNameCode = findViewById(R.id.stock_name_code);
		if (mTextViewNameCode != null) {
			mTextViewNameCode.setOnClickListener(this);
		}

		mTextViewId = findViewById(R.id.id);
		if (mTextViewId != null) {
			mTextViewId.setOnClickListener(this);
		}

		mTextViewPrice = findViewById(R.id.price);
		if (mTextViewPrice != null) {
			mTextViewPrice.setOnClickListener(this);
		}

		mTextViewNet = findViewById(R.id.net);
		if (mTextViewNet != null) {
			mTextViewNet.setOnClickListener(this);
		}

		mTextViewBuy = findViewById(R.id.buy);
		if (mTextViewBuy != null) {
			mTextViewBuy.setOnClickListener(this);
		}

		mTextViewSell = findViewById(R.id.sell);
		if (mTextViewSell != null) {
			mTextViewSell.setOnClickListener(this);
		}

		mTextViewVolume = findViewById(R.id.volume);
		if (mTextViewVolume != null) {
			mTextViewVolume.setOnClickListener(this);
		}

		mTextViewValue = findViewById(R.id.value);
		if (mTextViewValue != null) {
			mTextViewValue.setOnClickListener(this);
		}

		mTextViewFee = findViewById(R.id.fee);
		if (mTextViewFee != null) {
			mTextViewFee.setOnClickListener(this);
		}

		mTextViewProfit = findViewById(R.id.profit);
		if (mTextViewProfit != null) {
			mTextViewProfit.setOnClickListener(this);
		}

		mTextViewAction = findViewById(R.id.action);
		if (mTextViewAction != null) {
			mTextViewAction.setOnClickListener(this);
		}

		mTextViewHold = findViewById(R.id.hold);
		if (mTextViewHold != null) {
			mTextViewHold.setOnClickListener(this);
		}

		mTextViewValuation = findViewById(R.id.valuation);
		if (mTextViewValuation != null) {
			mTextViewValuation.setOnClickListener(this);
		}

		mTextViewQuantProfit = findViewById(R.id.quant_profit);
		if (mTextViewQuantProfit != null) {
			mTextViewQuantProfit.setOnClickListener(this);
		}

		mTextViewQuantProfitMargin = findViewById(R.id.quant_profit_margin);
		if (mTextViewQuantProfitMargin != null) {
			mTextViewQuantProfitMargin.setOnClickListener(this);
		}

		mTextViewQuantX = findViewById(R.id.quant_x);
		if (mTextViewQuantX != null) {
			mTextViewQuantX.setOnClickListener(this);
		}

		mTextViewThreshold = findViewById(R.id.threshold);
		if (mTextViewThreshold != null) {
			mTextViewThreshold.setOnClickListener(this);
		}

		mTextViewCreated = findViewById(R.id.created);
		if (mTextViewCreated != null) {
			mTextViewCreated.setOnClickListener(this);
		}

		mTextViewModified = findViewById(R.id.modified);
		if (mTextViewModified != null) {
			mTextViewModified.setOnClickListener(this);
		}

		if (mSortOrder.contains(DatabaseContract.COLUMN_CODE)) {
			setHeaderTextColor(mTextViewNameCode, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(BaseColumns._ID)) {
			setHeaderTextColor(mTextViewId, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PRICE)) {
			setHeaderTextColor(mTextViewPrice, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_NET)) {
			setHeaderTextColor(mTextViewNet, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_BUY)) {
			setHeaderTextColor(mTextViewBuy, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_SELL)) {
			setHeaderTextColor(mTextViewSell, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_VOLUME)) {
			setHeaderTextColor(mTextViewVolume, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_VALUE)) {
			setHeaderTextColor(mTextViewValue, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_FEE)) {
			setHeaderTextColor(mTextViewFee, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PROFIT)) {
			setHeaderTextColor(mTextViewProfit, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_ACTION)) {
			setHeaderTextColor(mTextViewAction, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_HOLD)) {
			setHeaderTextColor(mTextViewHold, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_VALUATION)) {
			setHeaderTextColor(mTextViewValuation, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_QUANT_PROFIT)) {
			setHeaderTextColor(mTextViewQuantProfit, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_QUANT_PROFIT_MARGIN)) {
			setHeaderTextColor(mTextViewQuantProfitMargin, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_QUANT_X)) {
			setHeaderTextColor(mTextViewQuantX, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_THRESHOLD)) {
			setHeaderTextColor(mTextViewThreshold, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_CREATED)) {
			setHeaderTextColor(mTextViewCreated, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_MODIFIED)) {
			setHeaderTextColor(mTextViewModified, mHeaderTextHighlightColor);
		} else {
		}
	}

	void initListView() {
		String[] mLeftFrom = new String[]{DatabaseContract.COLUMN_NAME,
				DatabaseContract.COLUMN_CODE};
		int[] mLeftTo = new int[]{R.id.name, R.id.code};

		String[] mRightFrom = new String[]{
				BaseColumns._ID,
				DatabaseContract.COLUMN_PRICE,
				DatabaseContract.COLUMN_NET,
				DatabaseContract.COLUMN_BUY,
				DatabaseContract.COLUMN_SELL,
				DatabaseContract.COLUMN_VOLUME,
				DatabaseContract.COLUMN_VALUE,
				DatabaseContract.COLUMN_FEE,
				DatabaseContract.COLUMN_PROFIT,
				DatabaseContract.COLUMN_ACTION,
				DatabaseContract.COLUMN_HOLD,
				DatabaseContract.COLUMN_VALUATION,
				DatabaseContract.COLUMN_QUANT_PROFIT,
				DatabaseContract.COLUMN_QUANT_PROFIT_MARGIN,
				DatabaseContract.COLUMN_QUANT_X,
				DatabaseContract.COLUMN_THRESHOLD,
				DatabaseContract.COLUMN_CREATED,
				DatabaseContract.COLUMN_MODIFIED};
		int[] mRightTo = new int[]{
				R.id.id,
				R.id.price,
				R.id.net,
				R.id.buy,
				R.id.sell,
				R.id.volume,
				R.id.value,
				R.id.fee,
				R.id.profit,
				R.id.action,
				R.id.hold,
				R.id.valuation,
				R.id.quant_profit,
				R.id.quant_profit_margin,
				R.id.quant_x,
				R.id.threshold,
				R.id.created,
				R.id.modified};

		mLeftListView = findViewById(R.id.left_listview);
		mLeftAdapter = new SimpleCursorAdapter(this,
				R.layout.activity_stock_list_left_item, null, mLeftFrom,
				mLeftTo, 0);
		if ((mLeftListView != null) && (mLeftAdapter != null)) {
			mLeftListView.setAdapter(mLeftAdapter);
			mLeftListView.setOnItemClickListener(this);
			mLeftListView.setOnItemLongClickListener(this);
		}

		mRightListView = findViewById(R.id.right_listview);
		mRightAdapter = new SimpleCursorAdapter(this,
				R.layout.activity_stock_trend_list_right_item, null, mRightFrom,
				mRightTo, 0);
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
		mLoaderManager.restartLoader(LOADER_ID_QUANT_LIST, null, this);
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

		getContentResolver().unregisterContentObserver(mContentObserver);
	}

	void setupSelection() {
		mSelection = null;

		if (mBundle != null) {
			String se = mBundle.getString(Constant.EXTRA_STOCK_SE);
			String code = mBundle.getString(Constant.EXTRA_STOCK_CODE);

			mStock.setSE(se);
			mStock.setCode(code);

			mSelection = DatabaseContract.COLUMN_SE + " = " + "'" + se + "'"
					+ " AND " + DatabaseContract.COLUMN_CODE + " = " + "'"
					+ code + "'";
			mGroupBy = "";
		} else {
			mSelection = "1";
			mGroupBy = ") GROUP BY (" + DatabaseContract.COLUMN_SE
					+ " + " + DatabaseContract.COLUMN_CODE;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader loader = null;

		switch (id) {
			case LOADER_ID_QUANT_LIST:
				setupSelection();
				//selection+") GROUP BY (coloum_name"
				loader = new CursorLoader(this,
						DatabaseContract.StockQuant.CONTENT_URI,
						DatabaseContract.StockQuant.PROJECTION_ALL, mSelection + mGroupBy,
						null, mSortOrder);
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
			case LOADER_ID_QUANT_LIST:
				setStockList(cursor);

				mLeftAdapter.swapCursor(cursor);
				mRightAdapter.swapCursor(cursor);
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
	}

	void setStockList(Cursor cursor) {
		ArrayMap<String, Stock> stockMap = new ArrayMap<>();
		StockQuant stockQuant = new StockQuant();

		try {
			mStockList.clear();
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					stockQuant.set(cursor);
					if (!stockMap.containsKey(stockQuant.getSE() + stockQuant.getCode())) {
						Stock stock = new Stock();
						stock.setSE(stockQuant.getSE());
						stock.setCode(stockQuant.getCode());
						mDatabaseManager.getStock(stock);
						//TODO
//						if (TextUtils.isEmpty(stock.getOperate())) {
//							mDatabaseManager.deleteStockQuant(stock);
//						} else {
//							stockMap.put(stockQuant.getSE() + stockQuant.getCode(), stock);
//						}
					}
				}
				cursor.moveToFirst();
				mStockList = new ArrayList<>(stockMap.values());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {

		if (parent.getId() == R.id.left_listview) {
			mStockQuant.setId(id);
			mHandler.sendEmptyMessage(MESSAGE_VIEW_QUANT_DEAL);
		} else {
			if (mCurrentActionMode == null) {
				mStockQuant.setId(id);
				mHandler.sendEmptyMessage(MESSAGE_VIEW_STOCK_CHAT);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
								   int position, long id) {

		return mCurrentActionMode == null;
	}

	boolean setTextViewValue(String key, View textView) {
		if (textView != null) {
			if (Preferences.getBoolean(key, false)) {
				textView.setVisibility(View.VISIBLE);
				return false;
			} else {
				textView.setVisibility(View.GONE);
				return true;
			}
		}

		return false;
	}

	void getStock() {
		mDatabaseManager.getStockQuantById(mStockQuant);

		mStock.setSE(mStockQuant.getSE());
		mStock.setCode(mStockQuant.getCode());

		mDatabaseManager.getStock(mStock);
	}

	private class CustomViewBinder implements SimpleCursorAdapter.ViewBinder {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if ((view == null) || (cursor == null) || (columnIndex == -1)) {
				return false;
			}

			return false;
		}
	}
}
