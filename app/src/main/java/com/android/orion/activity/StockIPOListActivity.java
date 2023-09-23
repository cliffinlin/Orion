package com.android.orion.activity;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
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
import com.android.orion.setting.Setting;
import com.android.orion.database.DatabaseContract;
import com.android.orion.utility.Preferences;
import com.android.orion.view.SyncHorizontalScrollView;

public class StockIPOListActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
		OnItemLongClickListener, OnClickListener {

	public static final int LOADER_ID_IPO_LIST = 0;

	static final int mHeaderTextDefaultColor = Color.BLACK;
	static final int mHeaderTextHighlightColor = Color.RED;

	String mSortOrderColumn = DatabaseContract.COLUMN_DATE;
	String mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_DESC;
	String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
	String mSortOrder = mSortOrderDefault;

	SyncHorizontalScrollView mTitleSHSV = null;
	SyncHorizontalScrollView mContentSHSV = null;

	TextView mTextViewNameCode = null;
	TextView mTextViewPrice = null;
	TextView mTextViewDate = null;
	TextView mTextViewTimeToMarket = null;
	TextView mTextViewPE = null;

	ListView mLeftListView = null;
	ListView mRightListView = null;

	SimpleCursorAdapter mLeftAdapter = null;
	SimpleCursorAdapter mRightAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_ipo_list);

		mSortOrder = Preferences.getString(mContext, Setting.KEY_SORT_ORDER_IPO_LIST,
				mSortOrderDefault);

		initHeader();

		initListView();

		mLoaderManager.initLoader(LOADER_ID_IPO_LIST, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_ipo_list, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		case R.id.action_refresh:
			if (mOrionService != null) {
				mStockDatabaseManager.deleteIPO();
				mOrionService.download();
			}
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
		case R.id.date:
			mSortOrderColumn = DatabaseContract.COLUMN_DATE;
			break;
		case R.id.time_to_market:
			mSortOrderColumn = DatabaseContract.COLUMN_TIME_TO_MARKET;
			break;
		case R.id.pe:
			mSortOrderColumn = DatabaseContract.COLUMN_PE;
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

		Preferences.putString(mContext, Setting.KEY_SORT_ORDER_IPO_LIST, mSortOrder);

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
		setHeaderTextColor(mTextViewDate, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewTimeToMarket, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewPE, mHeaderTextDefaultColor);
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

		mTextViewDate = (TextView) findViewById(R.id.date);
		if (mTextViewDate != null) {
			mTextViewDate.setOnClickListener(this);
		}

		mTextViewTimeToMarket = (TextView) findViewById(R.id.time_to_market);
		if (mTextViewTimeToMarket != null) {
			mTextViewTimeToMarket.setOnClickListener(this);
		}

		mTextViewPE = (TextView) findViewById(R.id.pe);
		if (mTextViewPE != null) {
			mTextViewPE.setOnClickListener(this);
		}

		if (mSortOrder.contains(DatabaseContract.COLUMN_CODE)) {
			setHeaderTextColor(mTextViewNameCode, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PRICE)) {
			setHeaderTextColor(mTextViewPrice, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_DATE)) {
			setHeaderTextColor(mTextViewDate, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_TIME_TO_MARKET)) {
			setHeaderTextColor(mTextViewTimeToMarket, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PE)) {
			setHeaderTextColor(mTextViewPE, mHeaderTextHighlightColor);
		} else {
		}
	}

	void initListView() {
		String[] mLeftFrom = new String[] { DatabaseContract.COLUMN_NAME,
				DatabaseContract.COLUMN_CODE };
		int[] mLeftTo = new int[] { R.id.name, R.id.code };

		String[] mRightFrom = new String[] { DatabaseContract.COLUMN_PRICE,
				DatabaseContract.COLUMN_DATE,
				DatabaseContract.COLUMN_TIME_TO_MARKET,
				DatabaseContract.COLUMN_PE };
		int[] mRightTo = new int[] { R.id.price, R.id.date,
				R.id.time_to_market, R.id.pe };

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
				R.layout.activity_stock_ipo_list_right_item, null, mRightFrom,
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
		mLoaderManager.restartLoader(LOADER_ID_IPO_LIST, null, this);
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
		CursorLoader loader = null;

		switch (id) {
		case LOADER_ID_IPO_LIST:

			loader = new CursorLoader(this, DatabaseContract.IPO.CONTENT_URI,
					DatabaseContract.IPO.PROJECTION_ALL, null, null, mSortOrder);

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
		case LOADER_ID_IPO_LIST:
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
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
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
				return setTextViewValue(Setting.KEY_PERIOD_MIN5, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN15)) {
				return setTextViewValue(Setting.KEY_PERIOD_MIN15, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN30)) {
				return setTextViewValue(Setting.KEY_PERIOD_MIN30, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN60)) {
				return setTextViewValue(Setting.KEY_PERIOD_MIN60, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_DAY)) {
				return setTextViewValue(Setting.KEY_PERIOD_DAY, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_WEEK)) {
				return setTextViewValue(Setting.KEY_PERIOD_WEEK, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MONTH)) {
				return setTextViewValue(Setting.KEY_PERIOD_MONTH, view);
			}

			return false;
		}
	}
}
