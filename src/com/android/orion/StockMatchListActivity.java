package com.android.orion;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

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
import android.widget.Toast;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.StockDeal;
import com.android.orion.database.Setting;
import com.android.orion.database.Stock;
import com.android.orion.leancloud.LeanCloudLoginActivity;
import com.android.orion.utility.Utility;
import com.avos.avoscloud.AVUser;

public class StockMatchListActivity extends StorageActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
		OnItemLongClickListener, OnClickListener {

	static final String DEAL_LIST_XML_FILE_NAME = "deal.xml";

	static final int EXECUTE_DEAL_DELETE = 0;

	static final int EXECUTE_DEAL_LIST_ON_ITEM_CLICK = 1;

	static final int EXECUTE_DEAL_LIST_LOAD_FROM_SD_CARD = 11;
	static final int EXECUTE_DEAL_LIST_SAVE_TO_SD_CARD = 12;

	static final int LOADER_ID_DEAL_LIST = 2;

	static final int mHeaderTextDefaultColor = Color.BLACK;
	static final int mHeaderTextHighlightColor = Color.RED;

	String mSortOrderColumn = DatabaseContract.COLUMN_CODE;
	String mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_ASC;
	String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
	String mSortOrder = mSortOrderDefault;

	AVUser mCurrentUser = null;

	SyncHorizontalScrollView mTitleSHSV = null;
	SyncHorizontalScrollView mContentSHSV = null;

	TextView mTextViewStockNameCode = null;
	TextView mTextViewPrice = null;
	TextView mTextViewNet = null;
	TextView mTextViewDeal = null;
	TextView mTextViewVolume = null;
	TextView mTextViewProfit = null;
	TextView mTextViewCreated = null;
	TextView mTextViewModified = null;

	ListView mLeftListView = null;
	ListView mRightListView = null;

	SimpleCursorAdapter mLeftAdapter = null;
	SimpleCursorAdapter mRightAdapter = null;

	ActionMode mCurrentActionMode = null;
	StockDeal mDeal = new StockDeal();
	List<StockDeal> mStockDealList = new ArrayList<StockDeal>();
	Stock mStock = new Stock();

	ContentObserver mContentObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);
			restartLoader();
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			restartLoader();
		}
	};

	private ActionMode.Callback mModeCallBack = new ActionMode.Callback() {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.setTitle("Actions");
			mode.getMenuInflater().inflate(R.menu.deal_list_action, menu);
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
				Intent intent = new Intent(StockMatchListActivity.this,
						StockDealActivity.class);
				intent.setAction(StockDealActivity.ACTION_DEAL_EDIT);
				intent.putExtra(StockDealActivity.EXTRA_DEAL_ID, mDeal.getId());
				startActivity(intent);
				mode.finish();
				return true;
			case R.id.menu_delete:
				new AlertDialog.Builder(StockMatchListActivity.this)
						.setTitle(R.string.delete)
						.setMessage(R.string.delete_confirm)
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										startSaveTask(EXECUTE_DEAL_DELETE);
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deal_list);

		mSortOrder = getSetting(Setting.KEY_SORT_ORDER_STOCK_DEAL_LIST,
				mSortOrderDefault);

		initHeader();

		initListView();

		mLoaderManager.initLoader(LOADER_ID_DEAL_LIST, null, this);

		if (!Utility.isNetworkConnected(this)) {
			Toast.makeText(this,
					getResources().getString(R.string.network_unavailable),
					Toast.LENGTH_SHORT).show();
		}

		getContentResolver().registerContentObserver(
				DatabaseContract.StockDeal.CONTENT_URI, true, mContentObserver);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.deal_list, menu);
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
			mIntent = new Intent(this, StockDealActivity.class);
			mIntent.setAction(StockDealActivity.ACTION_DEAL_INSERT);
			startActivity(mIntent);
			return true;

		case R.id.action_save_sd:
			showSaveSDAlertDialog();
			return true;

		case R.id.action_load_sd:
			startLoadTask(EXECUTE_DEAL_LIST_LOAD_FROM_SD_CARD);
			return true;

		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	void onSaveSD() {
		super.onSaveSD();
		startSaveTask(EXECUTE_DEAL_LIST_SAVE_TO_SD_CARD);
	}

	void onActionSync(int serviceType) {
		Intent intent = null;
		mCurrentUser = AVUser.getCurrentUser();

		if (mCurrentUser == null) {
			intent = new Intent(this, LeanCloudLoginActivity.class);
			startActivityForResult(intent, serviceType);
		} else {
			startService(serviceType, Constants.EXECUTE_IMMEDIATE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			startService(requestCode, Constants.EXECUTE_IMMEDIATE);
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
		case R.id.deal:
			mSortOrderColumn = DatabaseContract.COLUMN_DEAL;
			break;
		case R.id.volume:
			mSortOrderColumn = DatabaseContract.COLUMN_VOLUME;
			break;
		case R.id.profile:
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

		if (mSortOrderDirection.equals(DatabaseContract.ORDER_DIRECTION_ASC)) {
			mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_DESC;
		} else {
			mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_ASC;
		}

		mSortOrder = mSortOrderColumn + mSortOrderDirection;

		saveSetting(Setting.KEY_SORT_ORDER_STOCK_DEAL_LIST, mSortOrder);

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
		setHeaderTextColor(mTextViewStockNameCode, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewPrice, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewNet, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDeal, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewVolume, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewProfit, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewCreated, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewModified, mHeaderTextDefaultColor);
	}

	void setVisibility(String key, TextView textView) {
		if (textView != null) {
			if (Utility.getSettingBoolean(this, key)) {
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

		mTextViewStockNameCode = (TextView) findViewById(R.id.stock_name_code);
		mTextViewStockNameCode.setOnClickListener(this);

		mTextViewPrice = (TextView) findViewById(R.id.price);
		mTextViewPrice.setOnClickListener(this);

		mTextViewNet = (TextView) findViewById(R.id.net);
		mTextViewNet.setOnClickListener(this);

		mTextViewDeal = (TextView) findViewById(R.id.deal);
		mTextViewDeal.setOnClickListener(this);

		mTextViewVolume = (TextView) findViewById(R.id.volume);
		mTextViewVolume.setOnClickListener(this);

		mTextViewProfit = (TextView) findViewById(R.id.profile);
		mTextViewProfit.setOnClickListener(this);

		mTextViewCreated = (TextView) findViewById(R.id.created);
		mTextViewCreated.setOnClickListener(this);

		mTextViewModified = (TextView) findViewById(R.id.modified);
		mTextViewModified.setOnClickListener(this);

		if (mSortOrder.contains(DatabaseContract.COLUMN_CODE)) {
			setHeaderTextColor(mTextViewStockNameCode,
					mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PRICE)) {
			setHeaderTextColor(mTextViewPrice, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_NET)) {
			setHeaderTextColor(mTextViewNet, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_DEAL)) {
			setHeaderTextColor(mTextViewDeal, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_VOLUME)) {
			setHeaderTextColor(mTextViewVolume, mHeaderTextHighlightColor);
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
		String[] mLeftFrom = new String[] { DatabaseContract.COLUMN_NAME,
				DatabaseContract.COLUMN_CODE };
		int[] mLeftTo = new int[] { R.id.name, R.id.code };

		String[] mRightFrom = new String[] { DatabaseContract.COLUMN_PRICE,
				DatabaseContract.COLUMN_NET, DatabaseContract.COLUMN_DEAL,
				DatabaseContract.COLUMN_VOLUME, DatabaseContract.COLUMN_PROFIT,
				DatabaseContract.COLUMN_CREATED,
				DatabaseContract.COLUMN_MODIFIED };
		int[] mRightTo = new int[] { R.id.price, R.id.net, R.id.deal,
				R.id.volume, R.id.profile, R.id.created, R.id.modified };

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
				R.layout.activity_deal_list_right_item, null, mRightFrom,
				mRightTo, 0);
		if ((mRightListView != null) && (mRightAdapter != null)) {
			mRightAdapter.setViewBinder(new CustomViewBinder());
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

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		String selection = null;
		CursorLoader loader = null;

		switch (id) {
		case LOADER_ID_DEAL_LIST:
			loader = new CursorLoader(this, DatabaseContract.StockDeal.CONTENT_URI,
					DatabaseContract.StockDeal.PROJECTION_ALL, selection, null,
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
		case LOADER_ID_DEAL_LIST:
			setStockDealList(cursor);

			mLeftAdapter.swapCursor(cursor);
			mRightAdapter.swapCursor(cursor);
			break;

		default:
			break;
		}

		Utility.setListViewHeightBasedOnChildren(mLeftListView);
		Utility.setListViewHeightBasedOnChildren(mRightListView);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mLeftAdapter.swapCursor(null);
		mRightAdapter.swapCursor(null);
	}

	void setStockDealList(Cursor cursor) {
		mStockDealList.clear();

		try {
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockDeal stockDeal = new StockDeal();
					stockDeal.set(cursor);
					mStockDealList.add(stockDeal);
				}
				cursor.moveToFirst();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		if (mCurrentActionMode == null) {
			mDeal.setId(id);
			startLoadTask(EXECUTE_DEAL_LIST_ON_ITEM_CLICK);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {

		if (mCurrentActionMode != null) {
			return false;
		}

		mDeal.setId(id);
		mCurrentActionMode = startActionMode(mModeCallBack);
		view.setSelected(true);
		return true;
	}

	boolean setTextViewValue(String key, View textView) {
		if (textView != null) {
			if (Utility.getSettingBoolean(this, key)) {
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

	Long doInBackgroundLoad(Object... params) {
		super.doInBackgroundSave(params);
		int execute = (Integer) params[0];

		switch (execute) {
		case EXECUTE_DEAL_LIST_ON_ITEM_CLICK:
			mStockDatabaseManager.getStockDealById(mDeal);

			mStock.setSE(mDeal.getSE());
			mStock.setCode(mDeal.getCode());
			mStockDatabaseManager.getStock(mStock);

			Intent intent = new Intent(this, StockChartListActivity.class);
			intent.putExtra(Setting.KEY_SORT_ORDER_STOCK_DEAL_LIST, mSortOrder);
			intent.putExtra(StockChartListActivity.EXTRA_STOCK_ID,
					mStock.getId());
			startActivity(intent);
			break;

		case EXECUTE_DEAL_LIST_LOAD_FROM_SD_CARD:
			loadListFromSD(DEAL_LIST_XML_FILE_NAME);
			break;

		default:
			break;
		}

		return RESULT_SUCCESS;
	}

	void onPostExecuteLoad(Long result) {
		super.onPostExecuteLoad(result);
	}

	@Override
	Long doInBackgroundSave(Object... params) {
		super.doInBackgroundSave(params);
		int execute = (Integer) params[0];

		switch (execute) {
		case EXECUTE_DEAL_DELETE:
			mStockDatabaseManager.deleteStockDealById(mDeal);
			break;

		case EXECUTE_DEAL_LIST_SAVE_TO_SD_CARD:
			SaveListToSD(DEAL_LIST_XML_FILE_NAME);
			break;

		default:
			break;
		}

		return RESULT_SUCCESS;
	}

	@Override
	void onPostExecuteSave(Long result) {
		super.onPostExecuteSave(result);
	}

	@Override
	void xmlParse(XmlPullParser parser) {
		super.xmlParse(parser);

		int eventType;
		String tagName = "";
		StockDeal stockDeal = null;

		try {
			eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					if (XML_TAG_ITEM.equals(tagName)) {
						stockDeal = new StockDeal();
					} else if (DatabaseContract.COLUMN_SE.equals(tagName)) {
						stockDeal.setSE(parser.nextText());
					} else if (DatabaseContract.COLUMN_CODE.equals(tagName)) {
						stockDeal.setCode(parser.nextText());
					} else if (DatabaseContract.COLUMN_NAME.equals(tagName)) {
						stockDeal.setName(parser.nextText());
					} else if (DatabaseContract.COLUMN_PRICE.equals(tagName)) {
						stockDeal.setPrice(Double.valueOf(parser.nextText()));
					} else if (DatabaseContract.COLUMN_NET.equals(tagName)) {
						stockDeal.setNet(Double.valueOf(parser.nextText()));
					} else if (DatabaseContract.COLUMN_DEAL.equals(tagName)) {
						stockDeal.setDeal(Double.valueOf(parser.nextText()));
					} else if (DatabaseContract.COLUMN_VOLUME.equals(tagName)) {
						stockDeal.setVolume(Long.valueOf(parser.nextText()));
					} else if (DatabaseContract.COLUMN_PROFIT.equals(tagName)) {
						stockDeal.setProfit(Double.valueOf(parser.nextText()));
					} else if (DatabaseContract.COLUMN_CREATED.equals(tagName)) {
						stockDeal.setCreated(parser.nextText());
					} else if (DatabaseContract.COLUMN_MODIFIED.equals(tagName)) {
						stockDeal.setModified(parser.nextText());
					} else {
					}
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (XML_TAG_ITEM.equals(tagName)) {
						if (stockDeal != null) {
							mStock.setSE(stockDeal.getSE());
							mStock.setCode(stockDeal.getCode());
							mStockDatabaseManager.getStock(mStock);

							if (!mStock.getName().equals(stockDeal.getName())) {
								stockDeal.setName(mStock.getName());
							}

							if (mStock.getPrice() != stockDeal.getPrice()) {
								stockDeal.setPrice(mStock.getPrice());
								stockDeal.setupDeal();
							}

							if (!mStockDatabaseManager.isStockDealExist(stockDeal)) {
								mStockDatabaseManager.insertStockDeal(stockDeal);
							}
						}
					}
					break;
				default:
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	void xmlSerialize(XmlSerializer xmlSerializer) {
		super.xmlSerialize(xmlSerializer);

		try {
			for (StockDeal stockDeal : mStockDealList) {
				xmlSerializer.startTag(null, XML_TAG_ITEM);
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_SE,
						stockDeal.getSE());
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_CODE,
						stockDeal.getCode());
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_NAME,
						stockDeal.getName());
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_PRICE,
						String.valueOf(stockDeal.getPrice()));
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_NET,
						String.valueOf(stockDeal.getNet()));
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_DEAL,
						String.valueOf(stockDeal.getDeal()));
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_VOLUME,
						String.valueOf(stockDeal.getVolume()));
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_PROFIT,
						String.valueOf(stockDeal.getProfit()));
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_CREATED,
						stockDeal.getCreated());
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_MODIFIED,
						stockDeal.getModified());
				xmlSerializer.endTag(null, XML_TAG_ITEM);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}