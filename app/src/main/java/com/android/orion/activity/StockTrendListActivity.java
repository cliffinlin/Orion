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

import androidx.annotation.NonNull;

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
	TextView mTextViewPeriod = null;
	TextView mTextViewLevel = null;
	TextView mTextViewType = null;
	TextView mTextViewFlag = null;
	TextView mTextViewPrevNet = null;
	TextView mTextViewNet = null;
	TextView mTextViewNextNet = null;
	TextView mTextViewPredict = null;
	TextView mTextViewDate = null;
	TextView mTextViewTime = null;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_trend_list);

		if (mBundle != null) {
			mStock.setSE(mBundle.getString(Constant.EXTRA_STOCK_SE));
			mStock.setCode(mBundle.getString(Constant.EXTRA_STOCK_CODE));
			mSelection = DatabaseContract.SELECTION_STOCK(mStock.getSE(), mStock.getCode());
		}
		mSortOrder = Preferences.getString(Setting.SETTING_SORT_ORDER_TREND_LIST,
				mSortOrderDefault);

		initHeader();
		setupListView();

		mLoaderManager.initLoader(LOADER_ID_TREND_LIST, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_trend_list, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void handleOnOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_trend:
				mHandler.sendEmptyMessage(MESSAGE_VIEW_STOCK_TREND_CHAT);
				break;
			default:
				super.handleOnOptionsItemSelected(item);
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
		int viewId = view.getId();

		resetHeaderTextColor();
		setHeaderTextColor(viewId, mHeaderTextHighlightColor);

		switch (viewId) {
			case R.id.stock_name_code:
				mSortOrderColumn = DatabaseContract.COLUMN_CODE;
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
			case R.id.flag:
				mSortOrderColumn = DatabaseContract.COLUMN_FLAG;
				break;
			case R.id.prev_net:
				mSortOrderColumn = DatabaseContract.COLUMN_PREV_NET;
				break;
			case R.id.net:
				mSortOrderColumn = DatabaseContract.COLUMN_NET;
				break;
			case R.id.next_net:
				mSortOrderColumn = DatabaseContract.COLUMN_NEXT_NET;
				break;
			case R.id.predict:
				mSortOrderColumn = DatabaseContract.COLUMN_PREDICT;
				break;
			case R.id.date:
				mSortOrderColumn = DatabaseContract.COLUMN_DATE;
				break;
			case R.id.time:
				mSortOrderColumn = DatabaseContract.COLUMN_TIME;
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
		setHeaderTextColor(mTextViewPeriod, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewLevel, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewType, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewFlag, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewPrevNet, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewNet, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewNextNet, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewPredict, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDate, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewTime, mHeaderTextDefaultColor);
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

		mTextViewFlag = findViewById(R.id.flag);
		if (mTextViewFlag != null) {
			mTextViewFlag.setOnClickListener(this);
		}

		mTextViewPrevNet = findViewById(R.id.prev_net);
		if (mTextViewPrevNet != null) {
			mTextViewPrevNet.setOnClickListener(this);
		}

		mTextViewNet = findViewById(R.id.net);
		if (mTextViewNet != null) {
			mTextViewNet.setOnClickListener(this);
		}

		mTextViewNextNet = findViewById(R.id.next_net);
		if (mTextViewNextNet != null) {
			mTextViewNextNet.setOnClickListener(this);
		}

		mTextViewPredict = findViewById(R.id.predict);
		if (mTextViewPredict != null) {
			mTextViewPredict.setOnClickListener(this);
		}

		mTextViewDate = findViewById(R.id.date);
		if (mTextViewDate != null) {
			mTextViewDate.setOnClickListener(this);
		}

		mTextViewTime = findViewById(R.id.time);
		if (mTextViewTime != null) {
			mTextViewTime.setOnClickListener(this);
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
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_FLAG)) {
			setHeaderTextColor(mTextViewFlag, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PREV_NET)) {
			setHeaderTextColor(mTextViewPrevNet, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_NET)) {
			setHeaderTextColor(mTextViewNet, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_NEXT_NET)) {
			setHeaderTextColor(mTextViewNextNet, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PREDICT)) {
			setHeaderTextColor(mTextViewPredict, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_DATE)) {
			setHeaderTextColor(mTextViewDate, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_TIME)) {
			setHeaderTextColor(mTextViewTime, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_CREATED)) {
			setHeaderTextColor(mTextViewCreated, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_MODIFIED)) {
			setHeaderTextColor(mTextViewModified, mHeaderTextHighlightColor);
		} else {
		}
	}

	void setupListView() {
		String[] mLeftFrom = new String[]{DatabaseContract.COLUMN_NAME,
				DatabaseContract.COLUMN_CODE};
		int[] mLeftTo = new int[]{R.id.name, R.id.code};

		String[] mRightFrom = new String[]{
				DatabaseContract.COLUMN_PERIOD,
				DatabaseContract.COLUMN_LEVEL,
				DatabaseContract.COLUMN_TYPE,
				DatabaseContract.COLUMN_FLAG,
				DatabaseContract.COLUMN_PREV_NET,
				DatabaseContract.COLUMN_NET,
				DatabaseContract.COLUMN_NEXT_NET,
				DatabaseContract.COLUMN_PREDICT,
				DatabaseContract.COLUMN_DATE,
				DatabaseContract.COLUMN_TIME,
				DatabaseContract.COLUMN_CREATED,
				DatabaseContract.COLUMN_MODIFIED};
		int[] mRightTo = new int[]{
				R.id.period,
				R.id.level,
				R.id.type,
				R.id.flag,
				R.id.prev_net,
				R.id.net,
				R.id.next_net,
				R.id.predict,
				R.id.date,
				R.id.time,
				R.id.created,
				R.id.modified};

		mLeftListView = findViewById(R.id.left_listview);
		mLeftAdapter = new SimpleCursorAdapter(this,
				R.layout.activity_stock_favorite_list_left_item, null, mLeftFrom,
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
				mStockDatabaseManager.getStockTrendById(mStockTrend);
				mStock.setSE(mStockTrend.getSE());
				mStock.setCode(mStockTrend.getCode());
				mStockDatabaseManager.getStock(mStock);

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
//			mStockDatabaseManager.deleteStockTrend(stock);
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
			if (view == null || cursor == null || columnIndex == -1) {
				return false;
			}

			return false;
		}
	}
}
