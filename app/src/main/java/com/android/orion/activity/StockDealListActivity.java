package com.android.orion.activity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
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

import androidx.annotation.NonNull;

import com.android.orion.R;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.android.orion.constant.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.RecordFile;
import com.android.orion.view.SyncHorizontalScrollView;

import java.util.ArrayList;

public class StockDealListActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
		OnItemLongClickListener, OnClickListener {

	static final int LOADER_ID_DEAL_LIST = 0;

	static final int FILTER_TYPE_NONE = 0;
	static final int FILTER_TYPE_BUY = 1;
	static final int FILTER_TYPE_SELL = 2;
	static final int FILTER_TYPE_ALL = 3;

	static final int MESSAGE_DELETE_DEAL = 0;
	static final int MESSAGE_DELETE_DEAL_LIST = 1;

	static final int MESSAGE_VIEW_STOCK_DEAL = 4;
	static final int MESSAGE_VIEW_STOCK_CHAT = 5;

	static final int REQUEST_CODE_DEAL_INSERT = 0;
	static final int REQUEST_CODE_DEAL_EDIT = 1;

	static final int mHeaderTextDefaultColor = Color.BLACK;
	static final int mHeaderTextHighlightColor = Color.RED;

	String mSelection = null;
	String mSortOrderColumn = DatabaseContract.COLUMN_NET;
	String mSortOrderDirection = DatabaseContract.ORDER_ASC;
	String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
	String mSortOrder = mSortOrderDefault;
	String mGroupBy = "";

	SyncHorizontalScrollView mTitleSHSV = null;
	SyncHorizontalScrollView mContentSHSV = null;

	TextView mTextViewNameCode = null;
	TextView mTextViewPrice = null;
	TextView mTextViewNet = null;
	TextView mTextViewBuy = null;
	TextView mTextViewSell = null;
	TextView mTextViewVolume = null;
	TextView mTextViewValue = null;
	TextView mTextViewBonus = null;
	TextView mTextViewYield = null;
	TextView mTextViewFee = null;
	TextView mTextViewProfit = null;
	TextView mTextViewAccount = null;
	TextView mTextViewDate = null;
	TextView mTextViewType = null;
	TextView mTextViewCreated = null;
	TextView mTextViewModified = null;

	ListView mLeftListView = null;
	ListView mRightListView = null;

	SimpleCursorAdapter mLeftAdapter = null;
	SimpleCursorAdapter mRightAdapter = null;

	ActionMode mCurrentActionMode = null;

	StockDeal mStockDeal = new StockDeal();

	Stock mStock = new Stock();

	int mFilterType = FILTER_TYPE_NONE;

	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			Intent intent = null;

			switch (msg.what) {
				case MESSAGE_DELETE_DEAL:
					mStockDatabaseManager.getStockDeal(mStockDeal);
					getStock();
					RecordFile.writeDealFile(mStock, mStockDeal, Constant.DEAL_DELETE);
					mStockDatabaseManager.deleteStockDeal(mStockDeal);
					mStockDatabaseManager.updateStockDeal(mStock);
					mStockDatabaseManager.updateStock(mStock,
							mStock.getContentValues());
					break;

				case MESSAGE_DELETE_DEAL_LIST:
					break;

				case MESSAGE_VIEW_STOCK_DEAL:
					mStockDatabaseManager.getStockDeal(mStockDeal);
					getStock();

					intent = new Intent(mContext, StockActivity.class);
					intent.setAction(Constant.ACTION_STOCK_EDIT);
					intent.putExtra(Constant.EXTRA_STOCK_ID, mStock.getId());
					startActivity(intent);
					break;

				case MESSAGE_VIEW_STOCK_CHAT:
					mStockDatabaseManager.getStockDeal(mStockDeal);
					getStock();

					ArrayList<String> stockIDList = new ArrayList<>();
					for (Stock stock : mStockList) {
						stockIDList.add(String.valueOf(stock.getId()));
					}

					intent = new Intent(mContext, StockFavoriteChartListActivity.class);
					intent.putExtra(Constant.EXTRA_STOCK_ID, mStock.getId());
					intent.putStringArrayListExtra(Constant.EXTRA_STOCK_ID_LIST,
							stockIDList);
					intent.putExtra(Constant.EXTRA_SHOW_STOCK_DEAL, true);
					startActivity(intent);
					break;

				default:
					break;
			}
		}
	};
	private final ActionMode.Callback mModeCallBack = new ActionMode.Callback() {
		@Override
		public boolean onCreateActionMode(@NonNull ActionMode mode, Menu menu) {
			mode.setTitle("Actions");
			mode.getMenuInflater().inflate(R.menu.stock_deal_list_action, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(final ActionMode mode, @NonNull MenuItem item) {
			switch (item.getItemId()) {
				case R.id.menu_edit:
					mIntent = new Intent(mContext, StockDealActivity.class);
					mIntent.setAction(Constant.ACTION_DEAL_EDIT);
					mIntent.putExtra(Constant.EXTRA_STOCK_DEAL_ID,
							mStockDeal.getId());
					startActivityForResult(mIntent, REQUEST_CODE_DEAL_EDIT);
					mode.finish();
					return true;
				case R.id.menu_delete:
					mStockDatabaseManager.getStockDeal(mStockDeal);
					new AlertDialog.Builder(mContext)
							.setTitle(R.string.delete)
							.setMessage(getString(R.string.delete_confirm, mStockDeal.toString()))
							.setPositiveButton(R.string.ok,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
										                    int which) {
											mHandler.sendEmptyMessage(MESSAGE_DELETE_DEAL);
											mode.finish();
										}
									})
							.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
										                    int which) {
											mode.finish();
										}
									}).setIcon(android.R.drawable.ic_dialog_alert)
							.show();
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
	ContentObserver mContentObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_deal_list);

		mFilterType = FILTER_TYPE_SELL;
		if (mBundle != null) {
			mFilterType = FILTER_TYPE_ALL;
		}

		mSortOrder = Preferences.getString(Setting.SETTING_SORT_ORDER_DEAL_LIST,
				mSortOrderDefault);

		initHeader();
		setupListView();

		mLoaderManager.initLoader(LOADER_ID_DEAL_LIST, null, this);

		getContentResolver().registerContentObserver(
				DatabaseContract.StockDeal.CONTENT_URI, true, mContentObserver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_deal_list, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void handleOnOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_new:
				mIntent = new Intent(this, StockDealActivity.class);
				mIntent.setAction(Constant.ACTION_DEAL_INSERT);
				if (mBundle != null) {
					mIntent.putExtras(mBundle);
				}
				startActivityForResult(mIntent, REQUEST_CODE_DEAL_INSERT);
				break;
			case R.id.action_none:
				mFilterType = FILTER_TYPE_NONE;
				restartLoader();
				break;
			case R.id.action_buy:
				mFilterType = FILTER_TYPE_BUY;
				restartLoader();
				break;
			case R.id.action_sell:
				mFilterType = FILTER_TYPE_SELL;
				restartLoader();
				break;
			case R.id.action_all:
				mFilterType = FILTER_TYPE_ALL;
				restartLoader();
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
				case REQUEST_CODE_DEAL_INSERT:
				case REQUEST_CODE_DEAL_EDIT:
					break;

				default:
					break;
			}
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
			case R.id.profit:
				mSortOrderColumn = DatabaseContract.COLUMN_PROFIT;
				break;
			case R.id.fee:
				mSortOrderColumn = DatabaseContract.COLUMN_FEE;
				break;
			case R.id.account:
				mSortOrderColumn = DatabaseContract.COLUMN_ACCOUNT;
				break;
			case R.id.date:
				mSortOrderColumn = DatabaseContract.COLUMN_DATE;
				break;
			case R.id.type:
				mSortOrderColumn = DatabaseContract.COLUMN_TYPE;
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

		Preferences.putString(Setting.SETTING_SORT_ORDER_DEAL_LIST, mSortOrder);

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
		setHeaderTextColor(mTextViewBuy, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewSell, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewVolume, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewValue, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewBonus, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewYield, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewFee, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewProfit, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewAccount, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDate, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewType, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewCreated, mHeaderTextDefaultColor);
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

		mTextViewBonus = findViewById(R.id.bonus);
		if (mTextViewBonus != null) {
			mTextViewBonus.setOnClickListener(this);
		}

		mTextViewYield = findViewById(R.id.yield);
		if (mTextViewYield != null) {
			mTextViewYield.setOnClickListener(this);
		}

		mTextViewFee = findViewById(R.id.fee);
		if (mTextViewFee != null) {
			mTextViewFee.setOnClickListener(this);
		}

		mTextViewProfit = findViewById(R.id.profit);
		if (mTextViewProfit != null) {
			mTextViewProfit.setOnClickListener(this);
		}

		mTextViewAccount = findViewById(R.id.account);
		if (mTextViewAccount != null) {
			mTextViewAccount.setOnClickListener(this);
		}

		mTextViewDate = findViewById(R.id.date);
		if (mTextViewDate != null) {
			mTextViewDate.setOnClickListener(this);
		}

		mTextViewType = findViewById(R.id.type);
		if (mTextViewType != null) {
			mTextViewType.setOnClickListener(this);
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
			setHeaderTextColor(mTextViewNameCode,
					mHeaderTextHighlightColor);
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
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_BONUS)) {
			setHeaderTextColor(mTextViewBonus, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_YIELD)) {
			setHeaderTextColor(mTextViewYield, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_FEE)) {
			setHeaderTextColor(mTextViewFee, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PROFIT)) {
			setHeaderTextColor(mTextViewProfit, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_ACCOUNT)) {
			setHeaderTextColor(mTextViewAccount, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_DATE)) {
			setHeaderTextColor(mTextViewDate, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_TYPE)) {
			setHeaderTextColor(mTextViewType, mHeaderTextHighlightColor);
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
				DatabaseContract.COLUMN_PRICE,
				DatabaseContract.COLUMN_NET,
				DatabaseContract.COLUMN_BUY,
				DatabaseContract.COLUMN_SELL,
				DatabaseContract.COLUMN_VOLUME,
				DatabaseContract.COLUMN_VALUE,
				DatabaseContract.COLUMN_BONUS,
				DatabaseContract.COLUMN_YIELD,
				DatabaseContract.COLUMN_FEE,
				DatabaseContract.COLUMN_PROFIT,
				DatabaseContract.COLUMN_ACCOUNT,
				DatabaseContract.COLUMN_DATE,
				DatabaseContract.COLUMN_TYPE,
				DatabaseContract.COLUMN_CREATED,
				DatabaseContract.COLUMN_MODIFIED};
		int[] mRightTo = new int[]{
				R.id.price,
				R.id.net,
				R.id.buy,
				R.id.sell,
				R.id.volume,
				R.id.value,
				R.id.bonus,
				R.id.yield,
				R.id.fee,
				R.id.profit,
				R.id.account,
				R.id.date,
				R.id.type,
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
				R.layout.activity_stock_deal_list_right_item, null, mRightFrom,
				mRightTo, 0);
		if ((mRightListView != null) && (mRightAdapter != null)) {
			mRightAdapter.setViewBinder(new RightViewBinder());
			mRightListView.setAdapter(mRightAdapter);
			mRightListView.setOnItemClickListener(this);
			mRightListView.setOnItemLongClickListener(this);
		}
	}

	void restartLoader() {
		mLoaderManager.restartLoader(LOADER_ID_DEAL_LIST, null, this);
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
		String typeSelection = null;

		switch (mFilterType) {
			case FILTER_TYPE_NONE:
				break;

			case FILTER_TYPE_BUY:
				typeSelection = DatabaseContract.COLUMN_VOLUME + " < " + 0;
				break;

			case FILTER_TYPE_SELL:
				typeSelection = DatabaseContract.COLUMN_VOLUME + " > " + 0;
				typeSelection += " AND " + DatabaseContract.COLUMN_NET + " > " + 0;
				typeSelection += " AND " + DatabaseContract.COLUMN_PROFIT + " > " + DatabaseContract.COLUMN_BONUS;
				break;

			case FILTER_TYPE_ALL:
				typeSelection = null;
				break;

			default:
				typeSelection = null;
				break;
		}

		mSelection = typeSelection;

		if (mBundle != null) {
			String se = mBundle.getString(Constant.EXTRA_STOCK_SE);
			String code = mBundle.getString(Constant.EXTRA_STOCK_CODE);

			mStock.setSE(se);
			mStock.setCode(code);

			mSelection = DatabaseContract.SELECTION_STOCK(se, code);
			if (!TextUtils.isEmpty(typeSelection)) {
				mSelection += " AND " + typeSelection;
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader loader = null;

		switch (id) {
			case LOADER_ID_DEAL_LIST:
				setupSelection();

				loader = new CursorLoader(this,
						DatabaseContract.StockDeal.CONTENT_URI,
						DatabaseContract.StockDeal.PROJECTION_ALL, mSelection,
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
			case LOADER_ID_DEAL_LIST:
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
		StockDeal stockDeal = new StockDeal();

		try {
			mStockList.clear();
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					stockDeal.set(cursor);
					if (!stockMap.containsKey(stockDeal.getSE() + stockDeal.getCode())) {
						Stock stock = new Stock();
						stock.setSE(stockDeal.getSE());
						stock.setCode(stockDeal.getCode());
						mStockDatabaseManager.getStock(stock);
						stockMap.put(stockDeal.getSE() + stockDeal.getCode(), stock);
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
	public void onItemClick(@NonNull AdapterView<?> parent, View view, int position,
	                        long id) {

		if (parent.getId() == R.id.left_listview) {
			mStockDeal.setId(id);
			mHandler.sendEmptyMessage(MESSAGE_VIEW_STOCK_DEAL);
		} else {
			if (mCurrentActionMode == null) {
				mStockDeal.setId(id);
				mHandler.sendEmptyMessage(MESSAGE_VIEW_STOCK_CHAT);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
	                               int position, long id) {

		if (mCurrentActionMode != null) {
			return false;
		}

		mStockDeal.setId(id);
		mCurrentActionMode = startActionMode(mModeCallBack);
		view.setSelected(true);
		return true;
	}

	void getStock() {
		mStock.setSE(mStockDeal.getSE());
		mStock.setCode(mStockDeal.getCode());
		mStockDatabaseManager.getStock(mStock);
	}

	private class RightViewBinder implements SimpleCursorAdapter.ViewBinder {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (view == null || cursor == null || columnIndex == -1) {
				return false;
			}

			if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_PRICE)) {
				setViewColor(view, cursor);
				return setVisibility(view, Setting.getDisplayNet());
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_NET)) {
				setViewColor(view, cursor);
				return setVisibility(view, Setting.getDisplayNet());
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_BUY)) {
				setViewColor(view, cursor);
				return setVisibility(view, Setting.getDisplayNet());
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_SELL)) {
				setViewColor(view, cursor);
				return setVisibility(view, Setting.getDisplayNet());
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_VOLUME)) {
				setViewColor(view, cursor);
				return setVisibility(view, Setting.getDisplayNet());
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_VALUE)) {
				setViewColor(view, cursor);
				return setVisibility(view, Setting.getDisplayNet());
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_BONUS)) {
				setViewColor(view, cursor);
				return setVisibility(view, Setting.getDisplayNet());
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_YIELD)) {
				setViewColor(view, cursor);
				return setVisibility(view, Setting.getDisplayNet());
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_FEE)) {
				setViewColor(view, cursor);
				return setVisibility(view, Setting.getDisplayNet());
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_PROFIT)) {
				setViewColor(view, cursor);
				return setVisibility(view, Setting.getDisplayNet());
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_ACCOUNT)) {
				setViewColor(view, cursor);
				return setVisibility(view, Setting.getDisplayNet());
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_DATE)) {
				setViewColor(view, cursor);
				return setVisibility(view, Setting.getDisplayNet());
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_TYPE)) {
				setViewColor(view, cursor);
				return setVisibility(view, Setting.getDisplayNet());
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_CREATED)) {
				setViewColor(view, cursor);
				return setVisibility(view, Setting.getDisplayNet());
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MODIFIED)) {
				setViewColor(view, cursor);
				return setVisibility(view, Setting.getDisplayNet());
			}

			return false;
		}

		void setViewColor(View view, Cursor cursor) {
			if (view == null || cursor == null) {
				return;
			}

			double net = cursor.getDouble(cursor
					.getColumnIndex(DatabaseContract.COLUMN_NET));
			String type = cursor.getString(cursor
					.getColumnIndex(DatabaseContract.COLUMN_TYPE));

			TextView textView = (TextView) view;
			int textColor;
			if (TextUtils.equals(type, StockDeal.TYPE_BUY)) {
				textColor = net > 0 ? Color.RED : Color.GREEN;
			} else if (TextUtils.equals(type, StockDeal.TYPE_SELL)) {
				textColor = net > 0 ? Color.GREEN : Color.RED;
			} else {
				textColor = Color.BLACK;
			}
			textView.setTextColor(textColor);
		}
	}
}
