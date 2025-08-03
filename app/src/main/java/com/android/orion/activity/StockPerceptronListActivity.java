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
import com.android.orion.data.Period;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockTrend;
import com.android.orion.provider.StockContentProvider;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Preferences;
import com.android.orion.view.SyncHorizontalScrollView;

public class StockPerceptronListActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
		OnItemLongClickListener, OnClickListener {

	static final int LOADER_ID_PERCEPTRON_LIST = StockContentProvider.STOCK_PERCEPTRON;
	static final int MESSAGE_INIT_LOADER = 100;
	static final int MESSAGE_RESTART_LOADER = 110;

	static final int mHeaderTextDefaultColor = Color.BLACK;
	static final int mHeaderTextHighlightColor = Color.RED;

	String mSelection = null;
	String mSortOrderColumn = DatabaseContract.COLUMN_PERIOD;
	String mSortOrderDirection = DatabaseContract.ORDER_ASC;
	String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
	String mSortOrder = mSortOrderDefault;
	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
				case MESSAGE_INIT_LOADER:
					mSortOrder = Preferences.getString(Setting.SETTING_SORT_ORDER_PERCEPTRON_LIST,
							mSortOrderDefault);
					mLoaderManager.initLoader(LOADER_ID_PERCEPTRON_LIST, null, StockPerceptronListActivity.this);
					break;
				case MESSAGE_RESTART_LOADER:
					mLoaderManager.restartLoader(LOADER_ID_PERCEPTRON_LIST, null, StockPerceptronListActivity.this);
					break;
				default:
					break;
			}
		}
	};
	SyncHorizontalScrollView mTitleSHSV = null;
	SyncHorizontalScrollView mContentSHSV = null;
	TextView mTextViewNameCode = null;
	TextView mTextViewPeriod = null;
	TextView mTextViewLevel = null;
	TextView mTextViewType = null;
	TextView mTextViewWeight = null;
	TextView mTextViewBias = null;
	TextView mTextViewError = null;
	TextView mTextViewDelta = null;
	TextView mTextViewTimes = null;
	TextView mTextViewXMin = null;
	TextView mTextViewXMax = null;
	TextView mTextViewYMin = null;
	TextView mTextViewYMax = null;
	TextView mTextViewCreated = null;
	TextView mTextViewModified = null;
	ListView mLeftListView = null;
	ListView mRightListView = null;
	SimpleCursorAdapter mLeftAdapter = null;
	SimpleCursorAdapter mRightAdapter = null;
	ActionMode mCurrentActionMode = null;
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

	Stock mStock = new Stock();
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_perceptron_list);

		initHeader();
		initListView();
		initLoader();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_trend_list, menu);
		return super.onCreateOptionsMenu(menu);
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
		int viewId = view.getId();

		resetHeaderTextColor();
		setHeaderTextColor(viewId, mHeaderTextHighlightColor);

		switch (viewId) {
			case R.id.stock_name_code:
				mSortOrderColumn = DatabaseContract.COLUMN_ID;
				break;
			case R.id.period:
				mSortOrderColumn = DatabaseContract.COLUMN_PERIOD;
				break;
			case R.id.level:
				mSortOrderColumn = DatabaseContract.COLUMN_LEVEL;
				break;
			case R.id.type:
				mSortOrderColumn = DatabaseContract.COLUMN_TYPE;
				break;
			case R.id.weight:
				mSortOrderColumn = DatabaseContract.COLUMN_WEIGHT;
				break;
			case R.id.bias:
				mSortOrderColumn = DatabaseContract.COLUMN_BIAS;
				break;
			case R.id.error:
				mSortOrderColumn = DatabaseContract.COLUMN_ERROR;
				break;
			case R.id.delta:
				mSortOrderColumn = DatabaseContract.COLUMN_DELTA;
				break;
			case R.id.times:
				mSortOrderColumn = DatabaseContract.COLUMN_TIMES;
				break;
			case R.id.x_min:
				mSortOrderColumn = DatabaseContract.COLUMN_X_MIN;
				break;
			case R.id.x_max:
				mSortOrderColumn = DatabaseContract.COLUMN_X_MAX;
				break;
			case R.id.y_min:
				mSortOrderColumn = DatabaseContract.COLUMN_Y_MIN;
				break;
			case R.id.y_max:
				mSortOrderColumn = DatabaseContract.COLUMN_Y_MAX;
				break;
			case R.id.price:
				mSortOrderColumn = DatabaseContract.COLUMN_PRICE;
				break;
			case R.id.net:
				mSortOrderColumn = DatabaseContract.COLUMN_NET;
				break;
			case R.id.created:
				mSortOrderColumn = DatabaseContract.COLUMN_CREATED;
				break;
			case R.id.modified:
				mSortOrderColumn = DatabaseContract.COLUMN_MODIFIED;
				break;
			default:
//				mSortOrderColumn = DatabaseContract.COLUMN_CODE;
				break;
		}

		if (TextUtils.equals(mSortOrderDirection, DatabaseContract.ORDER_ASC)) {
			mSortOrderDirection = DatabaseContract.ORDER_DESC;
		} else {
			mSortOrderDirection = DatabaseContract.ORDER_ASC;
		}

		mSortOrder = mSortOrderColumn + mSortOrderDirection;

		Preferences.putString(Setting.SETTING_SORT_ORDER_PERCEPTRON_LIST, mSortOrder);

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
		setHeaderTextColor(mTextViewPeriod, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewLevel, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewType, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewWeight, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewBias, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewError, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDelta, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewTimes, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewXMin, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewXMax, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewYMin, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewYMax, mHeaderTextDefaultColor);
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

		mTextViewPeriod = findViewById(R.id.period);
		if (mTextViewPeriod != null) {
			mTextViewPeriod.setOnClickListener(this);
		}

		mTextViewLevel = findViewById(R.id.level);
		if (mTextViewLevel != null) {
			mTextViewLevel.setOnClickListener(this);
		}

		mTextViewType = findViewById(R.id.type);
		if (mTextViewType != null) {
			mTextViewType.setOnClickListener(this);
		}

		mTextViewWeight = findViewById(R.id.weight);
		if (mTextViewWeight != null) {
			mTextViewWeight.setOnClickListener(this);
		}

		mTextViewBias = findViewById(R.id.bias);
		if (mTextViewBias != null) {
			mTextViewBias.setOnClickListener(this);
		}

		mTextViewError = findViewById(R.id.error);
		if (mTextViewError != null) {
			mTextViewError.setOnClickListener(this);
		}

		mTextViewDelta = findViewById(R.id.delta);
		if (mTextViewDelta != null) {
			mTextViewDelta.setOnClickListener(this);
		}

		mTextViewTimes = findViewById(R.id.times);
		if (mTextViewTimes != null) {
			mTextViewTimes.setOnClickListener(this);
		}

		mTextViewXMin = findViewById(R.id.x_min);
		if (mTextViewXMin != null) {
			mTextViewXMin.setOnClickListener(this);
		}

		mTextViewXMax = findViewById(R.id.x_max);
		if (mTextViewXMax != null) {
			mTextViewXMax.setOnClickListener(this);
		}

		mTextViewYMin = findViewById(R.id.y_min);
		if (mTextViewYMin != null) {
			mTextViewYMin.setOnClickListener(this);
		}

		mTextViewYMax = findViewById(R.id.y_max);
		if (mTextViewYMax != null) {
			mTextViewYMax.setOnClickListener(this);
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
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PERIOD)) {
			setHeaderTextColor(mTextViewPeriod, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_LEVEL)) {
			setHeaderTextColor(mTextViewLevel, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_TYPE)) {
			setHeaderTextColor(mTextViewType, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_WEIGHT)) {
			setHeaderTextColor(mTextViewWeight, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_BIAS)) {
			setHeaderTextColor(mTextViewBias, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_ERROR)) {
			setHeaderTextColor(mTextViewError, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_DELTA)) {
			setHeaderTextColor(mTextViewDelta, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_TIMES)) {
			setHeaderTextColor(mTextViewTimes, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_X_MIN)) {
			setHeaderTextColor(mTextViewXMin, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_X_MAX)) {
			setHeaderTextColor(mTextViewXMax, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_Y_MIN)) {
			setHeaderTextColor(mTextViewYMin, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_Y_MAX)) {
			setHeaderTextColor(mTextViewYMax, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_CREATED)) {
			setHeaderTextColor(mTextViewCreated, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_MODIFIED)) {
			setHeaderTextColor(mTextViewModified, mHeaderTextHighlightColor);
		} else {
		}
	}

	void initListView() {
		String[] mLeftFrom = new String[]{DatabaseContract.COLUMN_PERIOD,
				DatabaseContract.COLUMN_PERIOD};
		int[] mLeftTo = new int[]{R.id.period, R.id.code};

		String[] mRightFrom = new String[]{
				DatabaseContract.COLUMN_LEVEL,
				DatabaseContract.COLUMN_TYPE,
				DatabaseContract.COLUMN_WEIGHT,
				DatabaseContract.COLUMN_BIAS,
				DatabaseContract.COLUMN_ERROR,
				DatabaseContract.COLUMN_DELTA,
				DatabaseContract.COLUMN_TIMES,
				DatabaseContract.COLUMN_X_MIN,
				DatabaseContract.COLUMN_X_MAX,
				DatabaseContract.COLUMN_Y_MIN,
				DatabaseContract.COLUMN_Y_MAX,
				DatabaseContract.COLUMN_CREATED,
				DatabaseContract.COLUMN_MODIFIED};
		int[] mRightTo = new int[]{
				R.id.level,
				R.id.type,
				R.id.weight,
				R.id.bias,
				R.id.error,
				R.id.delta,
				R.id.times,
				R.id.x_min,
				R.id.x_max,
				R.id.y_min,
				R.id.y_max,
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
				R.layout.activity_stock_perceptron_list_right_item, null, mRightFrom,
				mRightTo, 0);
		if ((mRightListView != null) && (mRightAdapter != null)) {
			mRightAdapter.setViewBinder(new CustomViewBinder());
			mRightListView.setAdapter(mRightAdapter);
			mRightListView.setOnItemClickListener(this);
			mRightListView.setOnItemLongClickListener(this);
		}
	}

	void initLoader() {
		Log.d("initLoader");
		mHandler.sendEmptyMessage(MESSAGE_INIT_LOADER);
	}

	void restartLoader() {
		mHandler.sendEmptyMessage(MESSAGE_RESTART_LOADER);
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
		mSelection = "(0";
		for (String period : Period.PERIODS) {
			if (Setting.getPeriod(period)) {
				mSelection += " OR " + DatabaseContract.COLUMN_PERIOD + " = '" + period + "'";
			}
		}
		mSelection += ") AND " + DatabaseContract.COLUMN_WEIGHT + " != 0";
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader loader = null;

		switch (id) {
			case LOADER_ID_PERCEPTRON_LIST:
				setupSelection();
				loader = new CursorLoader(this,
						DatabaseContract.StockPerceptron.CONTENT_URI,
						DatabaseContract.StockPerceptron.PROJECTION_ALL, mSelection,
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
			case LOADER_ID_PERCEPTRON_LIST:
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
			}
		}
		Intent intent = new Intent(mContext, StockTrendChartListActivity.class);
		intent.putExtra(Constant.EXTRA_STOCK_PERCEPTRON_ID, id);
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
	                               int position, long id) {
		StockTrend stockTrend = new StockTrend();
		stockTrend.setId(id);
		mStockDatabaseManager.getStockTrendById(stockTrend);
		Stock stock = new Stock();
		stock.setSE(stockTrend.getSE());
		stock.setCode(stockTrend.getCode());
		mStockDatabaseManager.getStock(stock);
		if (TextUtils.isEmpty(stock.getSE()) || TextUtils.isEmpty(stock.getCode())) {
//			mStockDatabaseManager.deleteStockTrend(stockId);
//			restartLoader();
			return true;
		}
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
			if (view == null || cursor == null || columnIndex == -1) {
				return false;
			}

			return false;
		}
	}
}
