package com.android.orion.activity;

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
import android.text.TextUtils;
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
import com.android.orion.database.IndexComponent;
import com.android.orion.database.Stock;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Preferences;
import com.android.orion.view.SyncHorizontalScrollView;

import java.util.ArrayList;

public class IndexComponentListActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
		OnItemLongClickListener, OnClickListener {

	public static final int LOADER_ID_INDEX_COMPONENT_LIST = 0;

	public static final int REQUEST_CODE_INDEX_COMPONENT_INSERT = 0;
	public static final int REQUEST_CODE_INDEX_COMPONENT_SELECT = 1;

	static final int mHeaderTextDefaultColor = Color.BLACK;
	static final int mHeaderTextHighlightColor = Color.RED;

	String mSortOrderColumn = DatabaseContract.COLUMN_NET;
	String mSortOrderDirection = DatabaseContract.ORDER_ASC;
	String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
	String mSortOrder = mSortOrderDefault;

	SyncHorizontalScrollView mTitleSHSV = null;
	SyncHorizontalScrollView mContentSHSV = null;

	TextView mTextViewNameCode = null;
	TextView mTextViewPrice = null;
	TextView mTextViewNet = null;
	TextView mTextViewMonth = null;
	TextView mTextViewWeek = null;
	TextView mTextViewDay = null;
	TextView mTextViewMin60 = null;
	TextView mTextViewMin30 = null;
	TextView mTextViewMin15 = null;
	TextView mTextViewMin5 = null;
	TextView mTextViewFlag = null;
	TextView mTextViewModified = null;

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
					for (int i = 0; i < mStockList.size(); i++) {
						Stock stock = mStockList.get(i);
						if (stock != null && (stock.getFlag() >= Stock.FLAG_FAVORITE)) {
							mDatabaseManager.deleteStockData(stock.getId());
							Setting.setDownloadStockData(stock.getSE(), stock.getCode(), 0);
							mStockDataProvider.download(stock);
						}
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

		setContentView(R.layout.activity_stock_list);

		mSortOrder = Preferences.getString(Setting.SETTING_SORT_ORDER_COMPONENT_LIST,
				mSortOrderDefault);

		initHeader();

		initListView();

		mLoaderManager.initLoader(LOADER_ID_INDEX_COMPONENT_LIST, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_favorite_list, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;

			case R.id.action_new:
				Intent intentNew = new Intent(this, StockActivity.class);
				intentNew.setAction(Constant.ACTION_INDEX_COMPONENT_INSERT);
				intentNew.putExtra(Constant.EXTRA_INDEX_CODE, mIntent.getStringExtra(Constant.EXTRA_INDEX_CODE));
				startActivityForResult(intentNew, REQUEST_CODE_INDEX_COMPONENT_INSERT);
				return true;

			case R.id.action_search:
				Intent intentSearch = new Intent(this, StockSearchActivity.class);
				intentSearch.setAction(Constant.ACTION_INDEX_COMPONENT_SELECT);
				intentSearch.putExtra(Constant.EXTRA_INDEX_CODE, mIntent.getStringExtra(Constant.EXTRA_INDEX_CODE));
				startActivityForResult(intentSearch, REQUEST_CODE_INDEX_COMPONENT_SELECT);
				return true;

			case R.id.action_refresh:
				mHandler.sendEmptyMessage(MESSAGE_REFRESH);
				return true;

			case R.id.action_setting:
				startActivity(new Intent(this, SettingActivity.class));
				return true;

			case R.id.action_load:
				performLoadFromFile();
				return true;

			case R.id.action_save:
				performSaveToFile();
				return true;

			case R.id.action_deal:
				startActivity(new Intent(this, StockDealListActivity.class));
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
				case REQUEST_CODE_INDEX_COMPONENT_INSERT:
				case REQUEST_CODE_INDEX_COMPONENT_SELECT:
					mStockDataProvider.download();
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
			case R.id.price:
				mSortOrderColumn = DatabaseContract.COLUMN_PRICE;
				break;
			case R.id.period_month:
				mSortOrderColumn = DatabaseContract.COLUMN_MONTH;
				break;
			case R.id.period_week:
				mSortOrderColumn = DatabaseContract.COLUMN_WEEK;
				break;
			case R.id.period_day:
				mSortOrderColumn = DatabaseContract.COLUMN_DAY;
				break;
			case R.id.period_min60:
				mSortOrderColumn = DatabaseContract.COLUMN_MIN60;
				break;
			case R.id.period_min30:
				mSortOrderColumn = DatabaseContract.COLUMN_MIN30;
				break;
			case R.id.period_min15:
				mSortOrderColumn = DatabaseContract.COLUMN_MIN15;
				break;
			case R.id.period_min5:
				mSortOrderColumn = DatabaseContract.COLUMN_MIN5;
				break;
			case R.id.flag:
				mSortOrderColumn = DatabaseContract.COLUMN_FLAG;
				break;
			case R.id.modified:
				mSortOrderColumn = DatabaseContract.COLUMN_MODIFIED;
				break;
			default:
				mSortOrderColumn = DatabaseContract.COLUMN_NET;
				break;
		}

		if (TextUtils.equals(mSortOrderDirection, DatabaseContract.ORDER_ASC)) {
			mSortOrderDirection = DatabaseContract.ORDER_DESC;
		} else {
			mSortOrderDirection = DatabaseContract.ORDER_ASC;
		}

		mSortOrder = mSortOrderColumn + mSortOrderDirection;

		Preferences.putString(Setting.SETTING_SORT_ORDER_COMPONENT_LIST, mSortOrder);

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
		setHeaderTextColor(mTextViewMonth, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewWeek, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDay, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewMin60, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewMin30, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewMin15, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewMin5, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewFlag, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewModified, mHeaderTextDefaultColor);
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
			setVisibility(mTextViewNet, Setting.getDisplayNet());
		}

		mTextViewMonth = findViewById(R.id.period_month);
		if (mTextViewMonth != null) {
			mTextViewMonth.setOnClickListener(this);
			setVisibility(mTextViewMonth, Setting.getPeriod(DatabaseContract.COLUMN_MONTH));
		}

		mTextViewWeek = findViewById(R.id.period_week);
		if (mTextViewWeek != null) {
			mTextViewWeek.setOnClickListener(this);
			setVisibility(mTextViewWeek, Setting.getPeriod(DatabaseContract.COLUMN_WEEK));
		}

		mTextViewDay = findViewById(R.id.period_day);
		if (mTextViewDay != null) {
			mTextViewDay.setOnClickListener(this);
			setVisibility(mTextViewDay, Setting.getPeriod(DatabaseContract.COLUMN_DAY));
		}

		mTextViewMin60 = findViewById(R.id.period_min60);
		if (mTextViewMin60 != null) {
			mTextViewMin60.setOnClickListener(this);
			setVisibility(mTextViewMin60, Setting.getPeriod(DatabaseContract.COLUMN_MIN60));
		}

		mTextViewMin30 = findViewById(R.id.period_min30);
		if (mTextViewMin30 != null) {
			mTextViewMin30.setOnClickListener(this);
			setVisibility(mTextViewMin30, Setting.getPeriod(DatabaseContract.COLUMN_MIN30));
		}

		mTextViewMin15 = findViewById(R.id.period_min15);
		if (mTextViewMin15 != null) {
			mTextViewMin15.setOnClickListener(this);
			setVisibility(mTextViewMin15, Setting.getPeriod(DatabaseContract.COLUMN_MIN15));
		}

		mTextViewMin5 = findViewById(R.id.period_min5);
		if (mTextViewMin5 != null) {
			mTextViewMin5.setOnClickListener(this);
			setVisibility(mTextViewMin5, Setting.getPeriod(DatabaseContract.COLUMN_MIN5));
		}

		mTextViewFlag = findViewById(R.id.flag);
		if (mTextViewFlag != null) {
			mTextViewFlag.setOnClickListener(this);
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
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_MONTH)) {
			setHeaderTextColor(mTextViewMonth, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_WEEK)) {
			setHeaderTextColor(mTextViewWeek, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_DAY)) {
			setHeaderTextColor(mTextViewDay, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_MIN60)) {
			setHeaderTextColor(mTextViewMin60, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_MIN30)) {
			setHeaderTextColor(mTextViewMin30, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_MIN15)) {
			setHeaderTextColor(mTextViewMin15, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_MIN5)) {
			setHeaderTextColor(mTextViewMin5, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_FLAG)) {
			setHeaderTextColor(mTextViewFlag, mHeaderTextHighlightColor);
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
				DatabaseContract.COLUMN_MONTH,
				DatabaseContract.COLUMN_WEEK,
				DatabaseContract.COLUMN_DAY,
				DatabaseContract.COLUMN_MIN60,
				DatabaseContract.COLUMN_MIN30,
				DatabaseContract.COLUMN_MIN15,
				DatabaseContract.COLUMN_MIN5,
				DatabaseContract.COLUMN_FLAG,
				DatabaseContract.COLUMN_MODIFIED};
		int[] mRightTo = new int[]{
				R.id.price,
				R.id.net,
				R.id.month,
				R.id.week,
				R.id.day,
				R.id.min60,
				R.id.min30,
				R.id.min15,
				R.id.min5,
				R.id.flag,
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
				R.layout.activity_stock_list_right_item, null, mRightFrom,
				mRightTo, 0);
		if ((mRightListView != null) && (mRightAdapter != null)) {
			mRightAdapter.setViewBinder(new RightViewBinder());
			mRightListView.setAdapter(mRightAdapter);
			mRightListView.setOnItemClickListener(this);
			mRightListView.setOnItemLongClickListener(this);
		}
	}

	void restartLoader() {
		mLoaderManager.restartLoader(LOADER_ID_INDEX_COMPONENT_LIST, null, this);
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
		String[] selectionArgs = null;
		CursorLoader loader = null;

		switch (id) {
			case LOADER_ID_INDEX_COMPONENT_LIST:
				ArrayList<IndexComponent> indexComponentList = new ArrayList<>();
				String componentSelection = "";
				StringBuilder placeHolder = new StringBuilder();
				StringBuilder indexIds = new StringBuilder();

				componentSelection += DatabaseContract.COLUMN_INDEX_CODE + " = " + mIntent.getStringExtra(Constant.EXTRA_INDEX_CODE);

				mDatabaseManager.getIndexComponentList(indexComponentList, componentSelection, null);

				if (indexComponentList.size() > 0) {
					placeHolder.append("?");
					indexIds.append(indexComponentList.get(0).getCode());
					for (int i = 1; i < indexComponentList.size(); i++) {
						placeHolder.append("," + "?");
						indexIds.append("," + indexComponentList.get(i).getCode());
					}

					selection = DatabaseContract.COLUMN_CODE + " in (" + placeHolder + " ) AND " + DatabaseContract.COLUMN_FLAG + " >= "
							+ Stock.FLAG_FAVORITE;
					selectionArgs = indexIds.toString().split(",");
				} else {
					selection = DatabaseContract.COLUMN_ID + " = " + Stock.INVALID_ID;
					selectionArgs = null;
				}

				loader = new CursorLoader(this, DatabaseContract.Stock.CONTENT_URI,
						DatabaseContract.Stock.PROJECTION_ALL, selection, selectionArgs,
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
			case LOADER_ID_INDEX_COMPONENT_LIST:
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

		if (id <= Stock.INVALID_ID) {
			return;
		}

		if (TextUtils.equals(mAction, Constant.ACTION_STOCK_ID)) {
			if (mIntent != null) {
				mIntent.putExtra(Constant.EXTRA_STOCK_ID, id);
				setResult(RESULT_OK, mIntent);
				finish();
			}
		} else {
			if (parent.getId() == R.id.left_listview) {
				mStock.setId(id);
				mDatabaseManager.getStockById(mStock);

				Intent intent = new Intent(mContext,
						StockDealListActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString(Constant.EXTRA_STOCK_SE, mStock.getSE());
				bundle.putString(Constant.EXTRA_STOCK_CODE, mStock.getCode());
				intent.putExtras(bundle);
				startActivity(intent);
			} else {
				Intent intent = new Intent(this,
						StockFavoriteChartListActivity.class);
				intent.putExtra(Constant.EXTRA_STOCK_LIST_SORT_ORDER,
						mSortOrder);
				intent.putExtra(Constant.EXTRA_STOCK_ID, id);
				startActivity(intent);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
	                               int position, long id) {
		Intent intentSearch = new Intent(this, StockSearchActivity.class);
		intentSearch.setAction(Constant.ACTION_INDEX_COMPONENT_SELECT);
		intentSearch.putExtra(Constant.EXTRA_INDEX_CODE, mIntent.getStringExtra(Constant.EXTRA_INDEX_CODE));
		intentSearch.putExtra(Constant.EXTRA_INDEX_NAME, mIntent.getStringExtra(Constant.EXTRA_INDEX_NAME));
		intentSearch.putExtra(Constant.EXTRA_INDEX_SE, mIntent.getStringExtra(Constant.EXTRA_INDEX_SE));
		startActivityForResult(intentSearch, REQUEST_CODE_INDEX_COMPONENT_SELECT);
		return true;
	}

	private class RightViewBinder implements SimpleCursorAdapter.ViewBinder {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if ((view == null) || (cursor == null) || (columnIndex == -1)) {
				return false;
			}

			if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_NET)) {
				return setVisibility(view, Setting.getDisplayNet());
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MONTH)) {
				return setVisibility(view, Setting.getPeriod(DatabaseContract.COLUMN_MONTH));
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_WEEK)) {
				return setVisibility(view, Setting.getPeriod(DatabaseContract.COLUMN_WEEK));
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_DAY)) {
				return setVisibility(view, Setting.getPeriod(DatabaseContract.COLUMN_DAY));
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN60)) {
				return setVisibility(view, Setting.getPeriod(DatabaseContract.COLUMN_MIN60));
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN30)) {
				return setVisibility(view, Setting.getPeriod(DatabaseContract.COLUMN_MIN30));
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN15)) {
				return setVisibility(view, Setting.getPeriod(DatabaseContract.COLUMN_MIN15));
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN5)) {
				return setVisibility(view, Setting.getPeriod(DatabaseContract.COLUMN_MIN5));
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MODIFIED)) {
			}

			return false;
		}
	}
}
