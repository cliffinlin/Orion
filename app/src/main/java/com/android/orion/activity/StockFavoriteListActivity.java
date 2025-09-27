package com.android.orion.activity;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.orion.R;
import com.android.orion.config.Config;
import com.android.orion.data.Period;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.Symbol;
import com.android.orion.utility.Utility;
import com.android.orion.view.SyncHorizontalScrollView;

public class StockFavoriteListActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
		OnItemLongClickListener, OnClickListener {

	static final int LOADER_ID_STOCK_FAVORITE_LIST = 0;

	static final int mHeaderTextDefaultColor = Color.BLACK;
	static final int mHeaderTextHighlightColor = Color.RED;

	String mSortOrderColumn = DatabaseContract.COLUMN_NET;
	String mSortOrderDirection = DatabaseContract.ORDER_DESC;
	String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
	String mSortOrder = mSortOrderDefault;

	SyncHorizontalScrollView mTitleSHSV = null;
	SyncHorizontalScrollView mContentSHSV = null;

	TextView mTextViewNameCode = null;
	TextView mTextViewPrice = null;
	TextView mTextViewNet = null;
	TextView mTextViewTrade = null;
	TextView mTextViewPast = null;
	TextView mTextViewDuration = null;
	TextView mTextViewYear = null;
	TextView mTextViewMonth6 = null;
	TextView mTextViewQuarter = null;
	TextView mTextViewMonth2 = null;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_favorite_list);

		initHeader();
		setupListView();
	}

	@Override
	public void handleOnCreate(Bundle savedInstanceState) {
		super.handleOnCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		super.onStart();

		initLoader();
	}

	@Override
	protected void onStop() {
		super.onStop();

		destroyLoader();
	}

	@Override
	protected void onResume() {
		super.onResume();

		resetHeaderTextColor();
		initHeader();
		setupListView();
	}

	@Override
	public void handleOnResume() {
		super.handleOnResume();

		restartLoader();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_favorite_list, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void handleOnOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_new:
				Intent intent = new Intent(this, StockActivity.class);
				intent.setAction(Constant.ACTION_FAVORITE_STOCK_INSERT);
				startActivity(intent);
				break;
			case R.id.action_refresh:
				try {
					mStockDatabaseManager.loadStockArrayMap(mStockArrayMap);
					for (Stock stock : mStockArrayMap.values()) {
						mStockDatabaseManager.deleteStockData(stock);
						mStockDatabaseManager.deleteStockTrend(stock);
						mStockDatabaseManager.deleteStockPerceptron(stock.getId());
						Setting.setDownloadStockDataTimeMillis(stock, 0);
						mStockDataProvider.download(stock);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case R.id.action_load:
				performLoadFromFile(FILE_TYPE_FAVORITE, false);
				break;
			case R.id.action_save:
				performSaveToFile(FILE_TYPE_FAVORITE);
				break;
			case R.id.action_deal:
				startActivity(new Intent(this, StockDealListActivity.class));
				break;
			case R.id.action_list:
				startActivity(new Intent(this, StockListActivity.class));
				break;
			case R.id.action_import:
				performLoadFromFile(FILE_TYPE_TDX_DATA, true);
				break;
			default:
				super.handleOnOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(@NonNull View view) {
		int viewId = view.getId();

		resetHeaderTextColor();
		setHeaderTextColor(viewId, mHeaderTextHighlightColor);

		switch (viewId) {
			case R.id.stock_name_code:
				mSortOrderColumn = DatabaseContract.COLUMN_CODE;
				break;
			case R.id.price:
				mSortOrderColumn = DatabaseContract.COLUMN_PRICE;
				break;
			case R.id.net:
				mSortOrderColumn = DatabaseContract.COLUMN_NET;
				break;
			case R.id.buy_profit:
				mSortOrderColumn = DatabaseContract.COLUMN_BUY_PROFIT;
				break;
			case R.id.sell_profit:
				mSortOrderColumn = DatabaseContract.COLUMN_SELL_PROFIT;
				break;
			case R.id.past:
				mSortOrderColumn = DatabaseContract.COLUMN_PAST;
				break;
			case R.id.duration:
				mSortOrderColumn = DatabaseContract.COLUMN_DURATION;
				break;
			case R.id.period_year:
				mSortOrderColumn = DatabaseContract.COLUMN_YEAR_THUMBNAIL;
				break;
			case R.id.period_month6:
				mSortOrderColumn = DatabaseContract.COLUMN_MONTH6_THUMBNAIL;
				break;
			case R.id.period_quarter:
				mSortOrderColumn = DatabaseContract.COLUMN_QUARTER_THUMBNAIL;
				break;
			case R.id.period_month2:
				mSortOrderColumn = DatabaseContract.COLUMN_MONTH2_THUMBNAIL;
				break;
			case R.id.period_month:
				mSortOrderColumn = DatabaseContract.COLUMN_MONTH_THUMBNAIL;
				break;
			case R.id.period_week:
				mSortOrderColumn = DatabaseContract.COLUMN_WEEK_THUMBNAIL;
				break;
			case R.id.period_day:
				mSortOrderColumn = DatabaseContract.COLUMN_DAY_THUMBNAIL;
				break;
			case R.id.period_min60:
				mSortOrderColumn = DatabaseContract.COLUMN_MIN60_THUMBNAIL;
				break;
			case R.id.period_min30:
				mSortOrderColumn = DatabaseContract.COLUMN_MIN30_THUMBNAIL;
				break;
			case R.id.period_min15:
				mSortOrderColumn = DatabaseContract.COLUMN_MIN15_THUMBNAIL;
				break;
			case R.id.period_min5:
				mSortOrderColumn = DatabaseContract.COLUMN_MIN5_THUMBNAIL;
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

		Preferences.putString(Setting.SETTING_SORT_ORDER_FAVORITE_LIST, mSortOrder);

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
		setHeaderTextColor(mTextViewTrade, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewPast, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDuration, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewYear, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewMonth6, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewQuarter, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewMonth2, mHeaderTextDefaultColor);
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

		mTextViewTrade = findViewById(R.id.trade);
		if (mTextViewTrade != null) {
			mTextViewTrade.setOnClickListener(this);
		}

		mTextViewPast = findViewById(R.id.past);
		if (mTextViewPast != null) {
			mTextViewPast.setOnClickListener(this);
			mTextViewPast.setVisibility(View.GONE);
		}

		mTextViewDuration = findViewById(R.id.duration);
		if (mTextViewDuration != null) {
			mTextViewDuration.setOnClickListener(this);
		}

		mTextViewYear = findViewById(R.id.period_year);
		if (mTextViewYear != null) {
			mTextViewYear.setOnClickListener(this);
			setVisibility(mTextViewYear, Setting.getPeriod(Period.YEAR));
		}

		mTextViewMonth6 = findViewById(R.id.period_month6);
		if (mTextViewMonth6 != null) {
			mTextViewMonth6.setOnClickListener(this);
			setVisibility(mTextViewMonth6, Setting.getPeriod(Period.MONTH6));
		}

		mTextViewQuarter = findViewById(R.id.period_quarter);
		if (mTextViewQuarter != null) {
			mTextViewQuarter.setOnClickListener(this);
			setVisibility(mTextViewQuarter, Setting.getPeriod(Period.QUARTER));
		}

		mTextViewMonth2 = findViewById(R.id.period_month2);
		if (mTextViewMonth2 != null) {
			mTextViewMonth2.setOnClickListener(this);
			setVisibility(mTextViewMonth2, Setting.getPeriod(Period.MONTH2));
		}

		mTextViewMonth = findViewById(R.id.period_month);
		if (mTextViewMonth != null) {
			mTextViewMonth.setOnClickListener(this);
			setVisibility(mTextViewMonth, Setting.getPeriod(Period.MONTH));
		}

		mTextViewWeek = findViewById(R.id.period_week);
		if (mTextViewWeek != null) {
			mTextViewWeek.setOnClickListener(this);
			setVisibility(mTextViewWeek, Setting.getPeriod(Period.WEEK));
		}

		mTextViewDay = findViewById(R.id.period_day);
		if (mTextViewDay != null) {
			mTextViewDay.setOnClickListener(this);
			setVisibility(mTextViewDay, Setting.getPeriod(Period.DAY));
		}

		mTextViewMin60 = findViewById(R.id.period_min60);
		if (mTextViewMin60 != null) {
			mTextViewMin60.setOnClickListener(this);
			setVisibility(mTextViewMin60, Setting.getPeriod(Period.MIN60));
		}

		mTextViewMin30 = findViewById(R.id.period_min30);
		if (mTextViewMin30 != null) {
			mTextViewMin30.setOnClickListener(this);
			setVisibility(mTextViewMin30, Setting.getPeriod(Period.MIN30));
		}

		mTextViewMin15 = findViewById(R.id.period_min15);
		if (mTextViewMin15 != null) {
			mTextViewMin15.setOnClickListener(this);
			setVisibility(mTextViewMin15, Setting.getPeriod(Period.MIN15));
		}

		mTextViewMin5 = findViewById(R.id.period_min5);
		if (mTextViewMin5 != null) {
			mTextViewMin5.setOnClickListener(this);
			setVisibility(mTextViewMin5, Setting.getPeriod(Period.MIN5));
		}

		mTextViewFlag = findViewById(R.id.flag);
		if (mTextViewFlag != null) {
			mTextViewFlag.setOnClickListener(this);
		}

		mTextViewModified = findViewById(R.id.modified);
		if (mTextViewModified != null) {
			mTextViewModified.setOnClickListener(this);
		}

		if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_CODE)) {
			setHeaderTextColor(mTextViewNameCode, mHeaderTextHighlightColor);
		} else if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_PRICE)) {
			setHeaderTextColor(mTextViewPrice, mHeaderTextHighlightColor);
		} else if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_NET)) {
			setHeaderTextColor(mTextViewNet, mHeaderTextHighlightColor);
		} else if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_PAST)) {
			setHeaderTextColor(mTextViewPast, mHeaderTextHighlightColor);
		} else if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_DURATION)) {
			setHeaderTextColor(mTextViewDuration, mHeaderTextHighlightColor);
		} else if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_YEAR_THUMBNAIL)) {
			setHeaderTextColor(mTextViewYear, mHeaderTextHighlightColor);
		} else if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_MONTH6_THUMBNAIL)) {
			setHeaderTextColor(mTextViewMonth6, mHeaderTextHighlightColor);
		} else if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_QUARTER_THUMBNAIL)) {
			setHeaderTextColor(mTextViewQuarter, mHeaderTextHighlightColor);
		} else if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_MONTH2_THUMBNAIL)) {
			setHeaderTextColor(mTextViewMonth2, mHeaderTextHighlightColor);
		} else if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_MONTH_THUMBNAIL)) {
			setHeaderTextColor(mTextViewMonth, mHeaderTextHighlightColor);
		} else if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_WEEK_THUMBNAIL)) {
			setHeaderTextColor(mTextViewWeek, mHeaderTextHighlightColor);
		} else if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_DAY_THUMBNAIL)) {
			setHeaderTextColor(mTextViewDay, mHeaderTextHighlightColor);
		} else if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_MIN60_THUMBNAIL)) {
			setHeaderTextColor(mTextViewMin60, mHeaderTextHighlightColor);
		} else if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_MIN30_THUMBNAIL)) {
			setHeaderTextColor(mTextViewMin30, mHeaderTextHighlightColor);
		} else if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_MIN15_THUMBNAIL)) {
			setHeaderTextColor(mTextViewMin15, mHeaderTextHighlightColor);
		} else if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_MIN5_THUMBNAIL)) {
			setHeaderTextColor(mTextViewMin5, mHeaderTextHighlightColor);
		} else if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_FLAG)) {
			setHeaderTextColor(mTextViewFlag, mHeaderTextHighlightColor);
		} else if (TextUtils.equals(mSortOrderColumn, DatabaseContract.COLUMN_MODIFIED)) {
			setHeaderTextColor(mTextViewModified, mHeaderTextHighlightColor);
		} else {
		}
	}

	void setupListView() {
		String[] mLeftFrom = new String[]{DatabaseContract.COLUMN_NAME,
				DatabaseContract.COLUMN_CODE};
		int[] mLeftTo = new int[]{R.id.name, R.id.code};

		String[] mRightFrom = new String[]{
				DatabaseContract.COLUMN_PRICE,
				DatabaseContract.COLUMN_NET,
				DatabaseContract.COLUMN_BUY_PROFIT,
				DatabaseContract.COLUMN_SELL_PROFIT,
				DatabaseContract.COLUMN_PAST,
				DatabaseContract.COLUMN_DURATION,
				DatabaseContract.COLUMN_THUMBNAIL,
				DatabaseContract.COLUMN_YEAR_THUMBNAIL,
				DatabaseContract.COLUMN_MONTH6_THUMBNAIL,
				DatabaseContract.COLUMN_QUARTER_THUMBNAIL,
				DatabaseContract.COLUMN_MONTH2_THUMBNAIL,
				DatabaseContract.COLUMN_MONTH_THUMBNAIL,
				DatabaseContract.COLUMN_WEEK_THUMBNAIL,
				DatabaseContract.COLUMN_DAY_THUMBNAIL,
				DatabaseContract.COLUMN_MIN60_THUMBNAIL,
				DatabaseContract.COLUMN_MIN30_THUMBNAIL,
				DatabaseContract.COLUMN_MIN15_THUMBNAIL,
				DatabaseContract.COLUMN_MIN5_THUMBNAIL,
				DatabaseContract.COLUMN_FLAG,
				DatabaseContract.COLUMN_MODIFIED};
		int[] mRightTo = new int[]{
				R.id.price,
				R.id.net,
				R.id.buy_profit,
				R.id.sell_profit,
				R.id.past,
				R.id.duration,
				R.id.thumbnail,
				R.id.year,
				R.id.month6,
				R.id.quarter,
				R.id.month2,
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
				R.layout.activity_stock_favorite_list_left_item, null, mLeftFrom,
				mLeftTo, 0);
		if (mLeftListView != null) {
			mLeftAdapter.setViewBinder(new LeftViewBinder());
			mLeftListView.setAdapter(mLeftAdapter);
			mLeftListView.setOnItemClickListener(this);
			mLeftListView.setOnItemLongClickListener(this);
		}

		mRightListView = findViewById(R.id.right_listview);
		mRightAdapter = new SimpleCursorAdapter(this,
				R.layout.activity_stock_favorite_list_right_item, null, mRightFrom, mRightTo, 0);
		if (mRightListView != null) {
			mRightAdapter.setViewBinder(new RightViewBinder());
			mRightListView.setAdapter(mRightAdapter);
			mRightListView.setOnItemClickListener(this);
			mRightListView.setOnItemLongClickListener(this);
		}
	}

	void initLoader() {
		mSortOrder = Preferences.getString(Setting.SETTING_SORT_ORDER_FAVORITE_LIST,
				mSortOrderDefault);
		if (!TextUtils.isEmpty(mSortOrder)) {
			String[] strings = mSortOrder.split(Symbol.WHITE_SPACE);
			if (strings != null && strings.length > 1) {
				mSortOrderColumn = strings[0];
			}
		}
		mLoaderManager.initLoader(LOADER_ID_STOCK_FAVORITE_LIST, null, this);
	}

	void destroyLoader() {
		mLoaderManager.destroyLoader(LOADER_ID_STOCK_FAVORITE_LIST);
	}

	void restartLoader() {
		mLoaderManager.restartLoader(LOADER_ID_STOCK_FAVORITE_LIST, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader loader = null;

		switch (id) {
			case LOADER_ID_STOCK_FAVORITE_LIST:
				String selection = DatabaseContract.SELECTION_FLAG(Stock.FLAG_FAVORITE);
				loader = new CursorLoader(this, DatabaseContract.Stock.CONTENT_URI,
						DatabaseContract.Stock.PROJECTION_ALL, selection, null,
						mSortOrder);
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
			case LOADER_ID_STOCK_FAVORITE_LIST:
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
		if (id <= DatabaseContract.INVALID_ID) {
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
				mStockDatabaseManager.getStockById(mStock);
				if (mStock.getHold() > 0) {
					Intent intent = new Intent(mContext,
							StockDealListActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString(Constant.EXTRA_STOCK_SE, mStock.getSE());
					bundle.putString(Constant.EXTRA_STOCK_CODE, mStock.getCode());
					intent.putExtras(bundle);
					startActivity(intent);
				} else {
					mIntent = new Intent(this, StockActivity.class);
					mIntent.setAction(Constant.ACTION_STOCK_EDIT);
					mIntent.putExtra(Constant.EXTRA_STOCK_ID, mStock.getId());
					startActivity(mIntent);
				}
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
		Intent intent = new Intent(this, StockListActivity.class);
		startActivity(intent);
		return true;
	}

	private class LeftViewBinder implements SimpleCursorAdapter.ViewBinder {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (view == null || cursor == null || columnIndex == -1) {
				return false;
			}

			if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_CODE)) {
				setViewColor(view, cursor);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_NAME)) {
				setViewColor(view, cursor);
			}

			return false;
		}

		void setViewColor(View view, Cursor cursor) {
			if (view == null || cursor == null) {
				return;
			}

			String code = cursor.getString(cursor.getColumnIndex(DatabaseContract.COLUMN_CODE));
			TextView textView = (TextView) view;
			if (TextUtils.equals(mAnalyzingStockCode, code)) {
				textView.setTextColor(Color.RED);
			} else {
				textView.setTextColor(Color.GRAY);
			}
		}
	}

	private class RightViewBinder implements SimpleCursorAdapter.ViewBinder {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (view == null || cursor == null || columnIndex == -1) {
				return false;
			}

			String columnName = cursor.getColumnName(columnIndex);
			if (columnName == null) {
				return false;
			}

			if (isPeriodColumn(columnName) || DatabaseContract.COLUMN_THUMBNAIL.equals(columnName)) {
				if (isPeriodColumn(columnName)) {
					String period = Period.fromColumnName(columnName);
					if (Setting.getPeriod(period)) {
						view.setVisibility(View.VISIBLE);
					} else {
						view.setVisibility(View.GONE);
					}
				} else {
					view.setVisibility(View.VISIBLE);
				}

				if (view instanceof ImageView) {
					try {
						byte[] blobData = cursor.getBlob(columnIndex);
						if (blobData != null && blobData.length > 0) {
							((ImageView) view).setImageDrawable(
									Utility.bytesToThumbnail(StockFavoriteListActivity.this, blobData)
							);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						return true;
					}
				}
			}

			if (view instanceof TextView) {
				TextView textView = (TextView) view;
				int flag = cursor.getInt(cursor
						.getColumnIndex(DatabaseContract.COLUMN_FLAG));
				double buyProfit = cursor.getDouble(cursor
						.getColumnIndex(DatabaseContract.COLUMN_BUY_PROFIT));
				double sellProfit = cursor.getDouble(cursor
						.getColumnIndex(DatabaseContract.COLUMN_SELL_PROFIT));

				if (DatabaseContract.COLUMN_PRICE.equals(columnName)) {
				} else if (DatabaseContract.COLUMN_NET.equals(columnName)) {
				} else if (DatabaseContract.COLUMN_BUY_PROFIT.equals(columnName)) {
					if (Utility.hasFlag(flag, Stock.FLAG_MANUAL)) {
						textView.setBackgroundColor(Config.COLOR_BACKGROUND_MANUAL);
					}
					if (Utility.hasFlag(flag, Stock.FLAG_TRADE)) {
						textView.setTextColor(buyProfit > 0 ? Color.RED : Color.GRAY);
						return false;
					} else {
						textView.setText("");
						return true;
					}
				} else if (DatabaseContract.COLUMN_SELL_PROFIT.equals(columnName)) {
					if (Utility.hasFlag(flag, Stock.FLAG_MANUAL)) {
						textView.setBackgroundColor(Config.COLOR_BACKGROUND_MANUAL);
					}
					if (Utility.hasFlag(flag, Stock.FLAG_TRADE)) {
						textView.setTextColor(sellProfit < 0 ? Color.RED : Color.GRAY);
						return false;
					} else {
						textView.setText("");
						return true;
					}
				} else if (DatabaseContract.COLUMN_PAST.equals(columnName)) {
					return setVisibility(view, false);
				} else if (DatabaseContract.COLUMN_DURATION.equals(columnName)) {
				} else if (DatabaseContract.COLUMN_MODIFIED.equals(columnName)) {
				}
			}

			return false;
		}

		boolean isPeriodColumn(String columnName) {
			return DatabaseContract.COLUMN_YEAR_THUMBNAIL.equals(columnName) ||
					DatabaseContract.COLUMN_MONTH6_THUMBNAIL.equals(columnName) ||
					DatabaseContract.COLUMN_QUARTER_THUMBNAIL.equals(columnName) ||
					DatabaseContract.COLUMN_MONTH2_THUMBNAIL.equals(columnName) ||
					DatabaseContract.COLUMN_MONTH_THUMBNAIL.equals(columnName) ||
					DatabaseContract.COLUMN_WEEK_THUMBNAIL.equals(columnName) ||
					DatabaseContract.COLUMN_DAY_THUMBNAIL.equals(columnName) ||
					DatabaseContract.COLUMN_MIN60_THUMBNAIL.equals(columnName) ||
					DatabaseContract.COLUMN_MIN30_THUMBNAIL.equals(columnName) ||
					DatabaseContract.COLUMN_MIN15_THUMBNAIL.equals(columnName) ||
					DatabaseContract.COLUMN_MIN5_THUMBNAIL.equals(columnName);
		}
	}
}