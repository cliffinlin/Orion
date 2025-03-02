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
import android.text.TextUtils;
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
import com.android.orion.database.StockTrend;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Preferences;
import com.android.orion.view.SyncHorizontalScrollView;

public class StockTrendListActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
		OnItemLongClickListener, OnClickListener {

	static final int LOADER_ID_TREND_LIST = 0;
	static final int MESSAGE_VIEW_STOCK_TREND_CHAT = 1;

	static final int mHeaderTextDefaultColor = Color.BLACK;
	static final int mHeaderTextHighlightColor = Color.RED;

	String mSelection = null;
	String mSortOrderColumn = DatabaseContract.COLUMN_PERIOD;
	String mSortOrderDirection = DatabaseContract.ORDER_ASC;
	String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
	String mSortOrder = mSortOrderDefault;

	SyncHorizontalScrollView mTitleSHSV = null;
	SyncHorizontalScrollView mContentSHSV = null;

	TextView mTextViewNameCode = null;
	TextView mTextViewPrice = null;
	TextView mTextViewNet = null;
	TextView mTextViewPeriod = null;
	TextView mTextViewDate = null;
	TextView mTextViewTime = null;
	TextView mTextViewLevel = null;
	TextView mTextViewType = null;
	TextView mTextViewFlag = null;
	TextView mTextViewGrouped = null;
	TextView mTextViewDirection = null;
	TextView mTextViewVertexLow = null;
	TextView mTextViewVertexHigh = null;
	TextView mTextViewVertexNet = null;
	TextView mTextViewTurning = null;
	TextView mTextViewTurningNet = null;
	TextView mTextViewTurningRate = null;
	TextView mTextViewProfit = null;
	TextView mTextViewCreated = null;
	TextView mTextViewModified = null;

	ListView mLeftListView = null;
	ListView mRightListView = null;

	SimpleCursorAdapter mLeftAdapter = null;
	SimpleCursorAdapter mRightAdapter = null;

	ActionMode mCurrentActionMode = null;
	StockTrend mStockTrend = new StockTrend();

	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			Intent intent = null;

			switch (msg.what) {
				case MESSAGE_VIEW_STOCK_TREND_CHAT:
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

	private final ActionMode.Callback mModeCallBack = new ActionMode.Callback() {
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

		if (mBundle != null) {
			mStock.setSE(mBundle.getString(Constant.EXTRA_STOCK_SE));
			mStock.setCode(mBundle.getString(Constant.EXTRA_STOCK_CODE));
			mSelection = mDatabaseManager.getStockSelection(mStock);
		}
		mSortOrder = Preferences.getString(Setting.SETTING_SORT_ORDER_TREND_LIST,
				mSortOrderDefault);

		initHeader();
		initListView();

		mLoaderManager.initLoader(LOADER_ID_TREND_LIST, null, this);
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

			case R.id.action_trend:
				mHandler.sendEmptyMessage(MESSAGE_VIEW_STOCK_TREND_CHAT);
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
			case R.id.price:
				mSortOrderColumn = DatabaseContract.COLUMN_PRICE;
				break;
			case R.id.net:
				mSortOrderColumn = DatabaseContract.COLUMN_NET;
				break;
			case R.id.period:
				mSortOrderColumn = DatabaseContract.COLUMN_PERIOD;
				break;
			case R.id.date:
				mSortOrderColumn = DatabaseContract.COLUMN_DATE;
				break;
			case R.id.time:
				mSortOrderColumn = DatabaseContract.COLUMN_TIME;
				break;
			case R.id.level:
				mSortOrderColumn = DatabaseContract.COLUMN_LEVEL;
				break;
			case R.id.type:
				mSortOrderColumn = DatabaseContract.COLUMN_TYPE;
				break;
			case R.id.flag:
				mSortOrderColumn = DatabaseContract.COLUMN_FLAG;
				break;
			case R.id.grouped:
				mSortOrderColumn = DatabaseContract.COLUMN_GROUPED;
				break;
			case R.id.direction:
				mSortOrderColumn = DatabaseContract.COLUMN_DIRECTION;
				break;
			case R.id.vertex_low:
				mSortOrderColumn = DatabaseContract.COLUMN_VERTEX_LOW;
				break;
			case R.id.vertex_high:
				mSortOrderColumn = DatabaseContract.COLUMN_VERTEX_HIGH;
				break;
			case R.id.vertex_net:
				mSortOrderColumn = DatabaseContract.COLUMN_VERTEX_NET;
				break;
			case R.id.turning:
				mSortOrderColumn = DatabaseContract.COLUMN_TURNING;
				break;
			case R.id.turning_net:
				mSortOrderColumn = DatabaseContract.COLUMN_TURNING_NET;
				break;
			case R.id.turning_rate:
				mSortOrderColumn = DatabaseContract.COLUMN_TURNING_RATE;
				break;
			case R.id.profit:
				mSortOrderColumn = DatabaseContract.COLUMN_PROFIT;
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

		Preferences.putString(Setting.SETTING_SORT_ORDER_TREND_LIST, mSortOrder);

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
		setHeaderTextColor(mTextViewPrice, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewNet, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewPeriod, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDate, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewTime, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewLevel, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewType, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewFlag, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewGrouped, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDirection, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewVertexLow, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewVertexHigh, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewVertexNet, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewTurning, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewTurningNet, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewTurningRate, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewProfit, mHeaderTextDefaultColor);
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

		mTextViewPrice = findViewById(R.id.price);
		if (mTextViewPrice != null) {
			mTextViewPrice.setOnClickListener(this);
		}

		mTextViewNet = findViewById(R.id.net);
		if (mTextViewNet != null) {
			mTextViewNet.setOnClickListener(this);
		}

		mTextViewPeriod = findViewById(R.id.period);
		if (mTextViewPeriod != null) {
			mTextViewPeriod.setOnClickListener(this);
		}

		mTextViewDate = findViewById(R.id.date);
		if (mTextViewDate != null) {
			mTextViewDate.setOnClickListener(this);
		}

		mTextViewTime = findViewById(R.id.time);
		if (mTextViewTime != null) {
			mTextViewTime.setOnClickListener(this);
		}

		mTextViewLevel = findViewById(R.id.level);
		if (mTextViewLevel != null) {
			mTextViewLevel.setOnClickListener(this);
		}

		mTextViewType = findViewById(R.id.type);
		if (mTextViewType != null) {
			mTextViewType.setOnClickListener(this);
		}

		mTextViewFlag = findViewById(R.id.flag);
		if (mTextViewFlag != null) {
			mTextViewFlag.setOnClickListener(this);
		}

		mTextViewGrouped = findViewById(R.id.grouped);
		if (mTextViewGrouped != null) {
			mTextViewGrouped.setOnClickListener(this);
		}

		mTextViewDirection = findViewById(R.id.direction);
		if (mTextViewDirection != null) {
			mTextViewDirection.setOnClickListener(this);
		}

		mTextViewVertexLow = findViewById(R.id.vertex_low);
		if (mTextViewVertexLow != null) {
			mTextViewVertexLow.setOnClickListener(this);
		}

		mTextViewVertexHigh = findViewById(R.id.vertex_high);
		if (mTextViewVertexHigh != null) {
			mTextViewVertexHigh.setOnClickListener(this);
		}

		mTextViewVertexNet = findViewById(R.id.vertex_net);
		if (mTextViewVertexNet != null) {
			mTextViewVertexNet.setOnClickListener(this);
		}

		mTextViewTurning = findViewById(R.id.turning);
		if (mTextViewTurning != null) {
			mTextViewTurning.setOnClickListener(this);
		}

		mTextViewTurningNet = findViewById(R.id.turning_net);
		if (mTextViewTurningNet != null) {
			mTextViewTurningNet.setOnClickListener(this);
		}

		mTextViewTurningRate = findViewById(R.id.turning_rate);
		if (mTextViewTurningRate != null) {
			mTextViewTurningRate.setOnClickListener(this);
		}

		mTextViewProfit = findViewById(R.id.profit);
		if (mTextViewProfit != null) {
			mTextViewProfit.setOnClickListener(this);
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
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PRICE)) {
			setHeaderTextColor(mTextViewPrice, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_NET)) {
			setHeaderTextColor(mTextViewNet, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PERIOD)) {
			setHeaderTextColor(mTextViewPeriod, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_DATE)) {
			setHeaderTextColor(mTextViewDate, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_TIME)) {
			setHeaderTextColor(mTextViewTime, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_LEVEL)) {
			setHeaderTextColor(mTextViewLevel, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_TYPE)) {
			setHeaderTextColor(mTextViewType, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_FLAG)) {
			setHeaderTextColor(mTextViewFlag, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_GROUPED)) {
			setHeaderTextColor(mTextViewGrouped, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_DIRECTION)) {
			setHeaderTextColor(mTextViewDirection, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_VERTEX_LOW)) {
			setHeaderTextColor(mTextViewVertexLow, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_VERTEX_HIGH)) {
			setHeaderTextColor(mTextViewVertexHigh, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_VERTEX_NET)) {
			setHeaderTextColor(mTextViewVertexNet, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_TURNING)) {
			setHeaderTextColor(mTextViewTurning, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_TURNING_NET)) {
			setHeaderTextColor(mTextViewTurningNet, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_TURNING_RATE)) {
			setHeaderTextColor(mTextViewTurningRate, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PROFIT)) {
			setHeaderTextColor(mTextViewProfit, mHeaderTextHighlightColor);
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
				DatabaseContract.COLUMN_PRICE,
				DatabaseContract.COLUMN_NET,
				DatabaseContract.COLUMN_PERIOD,
				DatabaseContract.COLUMN_DATE,
				DatabaseContract.COLUMN_TIME,
				DatabaseContract.COLUMN_LEVEL,
				DatabaseContract.COLUMN_TYPE,
				DatabaseContract.COLUMN_FLAG,
				DatabaseContract.COLUMN_GROUPED,
				DatabaseContract.COLUMN_DIRECTION,
				DatabaseContract.COLUMN_VERTEX_LOW,
				DatabaseContract.COLUMN_VERTEX_HIGH,
				DatabaseContract.COLUMN_VERTEX_NET,
				DatabaseContract.COLUMN_TURNING,
				DatabaseContract.COLUMN_TURNING_NET,
				DatabaseContract.COLUMN_TURNING_RATE,
				DatabaseContract.COLUMN_PROFIT,
				DatabaseContract.COLUMN_CREATED,
				DatabaseContract.COLUMN_MODIFIED};
		int[] mRightTo = new int[]{
				R.id.price,
				R.id.net,
				R.id.period,
				R.id.date,
				R.id.time,
				R.id.level,
				R.id.type,
				R.id.flag,
				R.id.grouped,
				R.id.direction,
				R.id.vertex_low,
				R.id.vertex_high,
				R.id.vertex_net,
				R.id.turning,
				R.id.turning_net,
				R.id.turning_rate,
				R.id.profit,
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

	void restartLoader() {
		Log.d("restartLoader");
		mLoaderManager.restartLoader(LOADER_ID_TREND_LIST, null, this);
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
	public void onAnalyzeFinish(String stockCode) {
		restartLoader();
	}

	void setupSelection() {
//		mSelection = "0";
//		for (String period : Period.PERIODS) {
//			if (Setting.getPeriod(period)) {
//				mSelection += " OR " + DatabaseContract.COLUMN_PERIOD + " = '" + period + "'";
//			}
//		}
//		StockContentProvider.setGroupBy(DatabaseContract.COLUMN_PERIOD + ", " + DatabaseContract.COLUMN_LEVEL);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader loader = null;

		switch (id) {
			case LOADER_ID_TREND_LIST:
				loader = new CursorLoader(this,
						DatabaseContract.StockTrend.CONTENT_URI,
						DatabaseContract.StockTrend.PROJECTION_ALL, mSelection,
						null, mSortOrder);
				break;

			default:
				break;
		}

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d("onLoadFinished");
		if (loader == null) {
			return;
		}

		switch (loader.getId()) {
			case LOADER_ID_TREND_LIST:
//				setStockList(cursor);

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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
	                        long id) {

		if (parent.getId() == R.id.left_listview) {
		} else {
			if (mCurrentActionMode == null) {
				mStockTrend.setId(id);
				mDatabaseManager.getStockTrendById(mStockTrend);
				mStock.setSE(mStockTrend.getSE());
				mStock.setCode(mStockTrend.getCode());
				mDatabaseManager.getStock(mStock);

				Intent intent = new Intent(mContext, StockFavoriteChartListActivity.class);
				intent.putExtra(Constant.EXTRA_STOCK_ID, mStock.getId());
				intent.putExtra(Constant.EXTRA_STOCK_TREND_ID, mStockTrend.getId());
				startActivity(intent);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
	                               int position, long id) {
//		if (TextUtils.isEmpty(stock.getSE()) || TextUtils.isEmpty(stock.getCode())) {
//			mDatabaseManager.deleteStockTrend(stock);
//			restartLoader();
//			return true;
//		}
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
