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

import com.android.orion.database.IndexComponent;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.utility.Preferences;

import java.util.ArrayList;

public class IndexComponentListActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
		OnItemLongClickListener, OnClickListener {

	public static final String ACTION_STOCK_ID = "orion.intent.action.ACTION_STOCK_ID";
	public static final String ACTION_INDEX_COMPONENT_LIST = "orion.intent.action.ACTION_INDEX_COMPONENT_LIST";

	public static final int LOADER_ID_INDEX_COMPONENT_LIST = 0;

	public static final int REQUEST_CODE_INDEX_COMPONENT_INSERT = 0;
    public static final int REQUEST_CODE_INDEX_COMPONENT_SELECT = 1;

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
	TextView mTextViewMonth = null;
	TextView mTextViewWeek = null;
	TextView mTextViewDay = null;
	TextView mTextViewMin60 = null;
	TextView mTextViewMin30 = null;
	TextView mTextViewMin15 = null;
	TextView mTextViewMin5 = null;
	TextView mTextViewOperate = null;
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
				if (mOrionService != null) {
					mStockDatabaseManager.deleteStockData();
					mOrionService.download();
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

		setContentView(R.layout.activity_stock_list);

		mSortOrder = Preferences.getString(mContext, Settings.KEY_SORT_ORDER_MARKET_LIST,
				mSortOrderDefault);

		initHeader();

		initListView();

		mLoaderManager.initLoader(LOADER_ID_INDEX_COMPONENT_LIST, null, this);

		if (!Preferences.getBoolean(mContext,
				Settings.KEY_NOTIFICATION_MESSAGE, false)) {
			Toast.makeText(
					this,
					getResources().getString(R.string.notification_is_off),
					Toast.LENGTH_LONG).show();
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
			Intent intentNew = new Intent(this, StockActivity.class);
			intentNew.setAction(StockActivity.ACTION_INDEX_COMPONENT_INSERT);
			intentNew.putExtra(Constants.EXTRA_INDEX_ID, mIntent.getStringExtra(Constants.EXTRA_INDEX_ID));
			startActivityForResult(intentNew, REQUEST_CODE_INDEX_COMPONENT_INSERT);
			return true;

		case R.id.action_search:
//            startActivityForResult(new Intent(this, StockSearchActivity.class), REQUEST_CODE_INDEX_COMPONENT_SELECT);
			Intent intentSearch = new Intent(this, StockSearchActivity.class);
			intentSearch.setAction(StockListEditActivity.ACTION_INDEX_COMPONENT_SELECT);
			intentSearch.putExtra(Constants.EXTRA_INDEX_ID, mIntent.getStringExtra(Constants.EXTRA_INDEX_ID));
			startActivityForResult(intentSearch, REQUEST_CODE_INDEX_COMPONENT_SELECT);
			return true;

		case R.id.action_refresh:
			mHandler.sendEmptyMessage(MESSAGE_REFRESH);
			return true;

		case R.id.action_settings:
			startActivity(new Intent(this, ServiceSettingActivity.class));
			return true;

		case R.id.action_filter:
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
			case REQUEST_CODE_INDEX_COMPONENT_INSERT:
			case REQUEST_CODE_INDEX_COMPONENT_SELECT:
				if (mOrionService != null) {
					mOrionService.download(mStock);
				}
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
		case R.id.net:
			mSortOrderColumn = DatabaseContract.COLUMN_NET;
			break;
		case R.id.action_month:
			mSortOrderColumn = DatabaseContract.COLUMN_MONTH;
			break;
		case R.id.action_week:
			mSortOrderColumn = DatabaseContract.COLUMN_WEEK;
			break;
		case R.id.action_day:
			mSortOrderColumn = DatabaseContract.COLUMN_DAY;
			break;
		case R.id.action_60min:
			mSortOrderColumn = DatabaseContract.COLUMN_MIN60;
			break;
		case R.id.action_30min:
			mSortOrderColumn = DatabaseContract.COLUMN_MIN30;
			break;
		case R.id.action_15min:
			mSortOrderColumn = DatabaseContract.COLUMN_MIN15;
			break;
		case R.id.action_5min:
			mSortOrderColumn = DatabaseContract.COLUMN_MIN5;
			break;
		case R.id.operate:
			mSortOrderColumn = DatabaseContract.COLUMN_OPERATE;
			break;
		case R.id.modified:
			mSortOrderColumn = DatabaseContract.COLUMN_MODIFIED;
			break;
		default:
			mSortOrderColumn = DatabaseContract.COLUMN_NET;
			break;
		}

		if (mSortOrderDirection.equals(DatabaseContract.ORDER_DIRECTION_ASC)) {
			mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_DESC;
		} else {
			mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_ASC;
		}

		mSortOrder = mSortOrderColumn + mSortOrderDirection;

		Preferences.putString(mContext, Settings.KEY_SORT_ORDER_MARKET_LIST, mSortOrder);

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
		setHeaderTextColor(mTextViewMonth, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewWeek, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDay, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewMin60, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewMin30, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewMin15, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewMin5, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewOperate, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewModified, mHeaderTextDefaultColor);
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

		mTextViewMonth = (TextView) findViewById(R.id.action_month);
		if (mTextViewMonth != null) {
			mTextViewMonth.setOnClickListener(this);
			setVisibility(Settings.KEY_PERIOD_MONTH, mTextViewMonth);
		}

		mTextViewWeek = (TextView) findViewById(R.id.action_week);
		if (mTextViewWeek != null) {
			mTextViewWeek.setOnClickListener(this);
			setVisibility(Settings.KEY_PERIOD_WEEK, mTextViewWeek);
		}

		mTextViewDay = (TextView) findViewById(R.id.action_day);
		if (mTextViewDay != null) {
			mTextViewDay.setOnClickListener(this);
			setVisibility(Settings.KEY_PERIOD_DAY, mTextViewDay);
		}

		mTextViewMin60 = (TextView) findViewById(R.id.action_60min);
		if (mTextViewMin60 != null) {
			mTextViewMin60.setOnClickListener(this);
			setVisibility(Settings.KEY_PERIOD_MIN60, mTextViewMin60);
		}

		mTextViewMin30 = (TextView) findViewById(R.id.action_30min);
		if (mTextViewMin30 != null) {
			mTextViewMin30.setOnClickListener(this);
			setVisibility(Settings.KEY_PERIOD_MIN30, mTextViewMin30);
		}

		mTextViewMin15 = (TextView) findViewById(R.id.action_15min);
		if (mTextViewMin15 != null) {
			mTextViewMin15.setOnClickListener(this);
			setVisibility(Settings.KEY_PERIOD_MIN15, mTextViewMin15);
		}

		mTextViewMin5 = (TextView) findViewById(R.id.action_5min);
		if (mTextViewMin5 != null) {
			mTextViewMin5.setOnClickListener(this);
			setVisibility(Settings.KEY_PERIOD_MIN5, mTextViewMin5);
		}

		mTextViewOperate = (TextView) findViewById(R.id.operate);
		if (mTextViewOperate != null) {
			mTextViewOperate.setOnClickListener(this);
		}

		mTextViewModified = (TextView) findViewById(R.id.modified);
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
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_OPERATE)) {
			setHeaderTextColor(mTextViewOperate, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_MODIFIED)) {
			setHeaderTextColor(mTextViewModified, mHeaderTextHighlightColor);
		} else {
		}
	}

	void initListView() {
		String[] mLeftFrom = new String[] { DatabaseContract.COLUMN_NAME,
				DatabaseContract.COLUMN_CODE };
		int[] mLeftTo = new int[] { R.id.name, R.id.code };

		String[] mRightFrom = new String[] { DatabaseContract.COLUMN_PRICE,
				DatabaseContract.COLUMN_NET, DatabaseContract.COLUMN_MONTH,
				DatabaseContract.COLUMN_WEEK, DatabaseContract.COLUMN_DAY,
				DatabaseContract.COLUMN_MIN60, DatabaseContract.COLUMN_MIN30,
				DatabaseContract.COLUMN_MIN15, DatabaseContract.COLUMN_MIN5,
				DatabaseContract.COLUMN_OPERATE, DatabaseContract.COLUMN_MODIFIED };
		int[] mRightTo = new int[] { R.id.price, R.id.net, R.id.type_month,
				R.id.type_week, R.id.type_day, R.id.type_60min,
				R.id.type_30min, R.id.type_15min, R.id.type_5min, R.id.operate, R.id.modified };

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
				R.layout.activity_stock_list_right_item, null, mRightFrom,
				mRightTo, 0);
		if ((mRightListView != null) && (mRightAdapter != null)) {
			mRightAdapter.setViewBinder(new RightViewBinder());
			mRightListView.setAdapter(mRightAdapter);
			mRightListView.setOnItemClickListener(this);
			mRightListView.setOnItemLongClickListener(this);
		}
	}

	void restartLoader(Intent intent) {
		restartLoader();
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
		CursorLoader loader = null;

		switch (id) {
		case LOADER_ID_INDEX_COMPONENT_LIST:
			ArrayList<IndexComponent> indexComponentList = new ArrayList<>();
			String componentSelection = "";
			StringBuilder placeHolder = new StringBuilder();
			StringBuilder indexIds = new StringBuilder();

			//TODO
//			componentSelection += DatabaseContract.COLUMN_INDEX_ID + " = " + mIntent.getStringExtra(Constants.EXTRA_INDEX_ID);

			mStockDatabaseManager.getIndexComponentList(indexComponentList, componentSelection, null);

			if (indexComponentList.size() > 0) {
				//TODO
				/*
				placeHolder.append("?");
				indexIds.append(indexComponentList.get(0).getStockId());
				for (int i = 1; i < indexComponentList.size(); i++) {
					placeHolder.append("," + "?");
					indexIds.append("," + indexComponentList.get(i).getStockId());
				}

				selection = DatabaseContract.COLUMN_ID + " in (" + placeHolder.toString() + " )";
				String[] selectionArgs = indexIds.toString().split(",");

				loader = new CursorLoader(this, DatabaseContract.Stock.CONTENT_URI,
						DatabaseContract.Stock.PROJECTION_ALL, selection, selectionArgs,
						mSortOrder);
						*/
			} else {
				selection = DatabaseContract.COLUMN_ID + " = " + Constants.STOCK_ID_INVALID;
				loader = new CursorLoader(this, DatabaseContract.Stock.CONTENT_URI,
						DatabaseContract.Stock.PROJECTION_ALL, selection, null,
						mSortOrder);
			}

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
				mStock.setId(id);
				mStockDatabaseManager.getStockById(mStock);

				Intent intent = new Intent(mContext,
						StockDealListActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString(Constants.EXTRA_STOCK_SE, mStock.getSE());
				bundle.putString(Constants.EXTRA_STOCK_CODE, mStock.getCode());
				intent.putExtras(bundle);
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
//		Intent intent = new Intent(this, StockListEditActivity.class);
//		intent.putExtra(Constants.EXTRA_STOCK_LIST_SORT_ORDER, mSortOrder);
//		startActivity(intent);

        Intent intentSearch = new Intent(this, StockSearchActivity.class);
        intentSearch.setAction(StockListEditActivity.ACTION_INDEX_COMPONENT_SELECT);
        intentSearch.putExtra(Constants.EXTRA_INDEX_ID, mIntent.getStringExtra(Constants.EXTRA_INDEX_ID));
        startActivityForResult(intentSearch, REQUEST_CODE_INDEX_COMPONENT_SELECT);
		return true;
	}

	boolean setRightTextViewVisibility(String key, View textView) {
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

	private class RightViewBinder implements SimpleCursorAdapter.ViewBinder {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if ((view == null) || (cursor == null) || (columnIndex == -1)) {
				return false;
			}

			if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MONTH)) {
				return setRightTextViewVisibility(Settings.KEY_PERIOD_MONTH, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_WEEK)) {
				return setRightTextViewVisibility(Settings.KEY_PERIOD_WEEK, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_DAY)) {
				return setRightTextViewVisibility(Settings.KEY_PERIOD_DAY, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN60)) {
				return setRightTextViewVisibility(Settings.KEY_PERIOD_MIN60, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN30)) {
				return setRightTextViewVisibility(Settings.KEY_PERIOD_MIN30, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN15)) {
				return setRightTextViewVisibility(Settings.KEY_PERIOD_MIN15, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN5)) {
				return setRightTextViewVisibility(Settings.KEY_PERIOD_MIN5, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MODIFIED)) {
			}

			return false;
		}
	}
}