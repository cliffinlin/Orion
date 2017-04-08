package com.android.orion;

import java.util.ArrayList;

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
import android.text.TextUtils;
import android.util.SparseArray;
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
import com.android.orion.database.Setting;
import com.android.orion.database.Stock;
import com.android.orion.database.StockMatch;
import com.android.orion.leancloud.LeanCloudLoginActivity;
import com.android.orion.utility.Utility;
import com.avos.avoscloud.AVUser;

public class StockMatchListActivity extends StorageActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
		OnItemLongClickListener, OnClickListener {

	static final int EXECUTE_MATCH_DELETE = 0;
	static final int EXECUTE_MATCH_LIST_ON_ITEM_CLICK = 1;
	static final int EXECUTE_MATCH_LIST_DELETE_ALL = 2;
	static final int EXECUTE_MATCH_LIST_LOAD_FROM_SD_CARD = 3;
	static final int EXECUTE_MATCH_LIST_SAVE_TO_SD_CARD = 4;

	static final int LOADER_ID_MATCH_LIST = 0;

	static final int STOCK_PERIOD_ARRAY_SIZE = 7;

	static final int mHeaderTextDefaultColor = Color.BLACK;
	static final int mHeaderTextHighlightColor = Color.RED;

	static final String MATCH_LIST_XML_FILE_NAME = "match.xml";

	String mSortOrderColumn = DatabaseContract.StockMatch.COLUMN_NAME_X;
	String mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_ASC;
	String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
	String mSortOrder = mSortOrderDefault;

	AVUser mCurrentUser = null;

	SyncHorizontalScrollView mTitleSHSV = null;
	SyncHorizontalScrollView mContentSHSV = null;

	TextView mTextViewNameCode = null;
	TextView mTextView5M = null;
	TextView mTextView15M = null;
	TextView mTextView30M = null;
	TextView mTextView60M = null;
	TextView mTextViewDay = null;
	TextView mTextViewWeek = null;
	TextView mTextViewMonth = null;

	ListView mLeftListView = null;
	ListView mRightListView = null;

	SimpleCursorAdapter mLeftAdapter = null;
	SimpleCursorAdapter mRightAdapter = null;

	ActionMode mCurrentActionMode = null;
	StockMatch mMatch = new StockMatch();
	ArrayList<StockMatch> mStockMatchList = new ArrayList<StockMatch>();
	Stock mStock_Y = new Stock();
	Stock mStock_X = new Stock();

	SparseArray<String> mPeriodArray = null;

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
			mode.getMenuInflater()
					.inflate(R.menu.stock_match_list_action, menu);
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
						StockMatchActivity.class);
				intent.setAction(StockMatchActivity.ACTION_MATCH_EDIT);
				intent.putExtra(StockMatchActivity.EXTRA_MATCH_ID,
						mMatch.getId());
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
										startSaveTask(EXECUTE_MATCH_DELETE);
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

		setContentView(R.layout.activity_stock_match_list);

		fillPeriodArray();

		mSortOrder = getSetting(Setting.KEY_SORT_ORDER_STOCK_MATCH_LIST,
				mSortOrderDefault);

		initHeader();

		initListView();

		getContentResolver()
				.registerContentObserver(
						DatabaseContract.StockMatch.CONTENT_URI, true,
						mContentObserver);

		mLoaderManager.initLoader(LOADER_ID_MATCH_LIST, null, this);

		if (!Utility.isNetworkConnected(this)) {
			Toast.makeText(this,
					getResources().getString(R.string.network_unavailable),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_match_list, menu);
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
			mIntent = new Intent(this, StockMatchActivity.class);
			mIntent.setAction(StockMatchActivity.ACTION_MATCH_INSERT);
			startActivity(mIntent);
			return true;

		case R.id.action_delete_all:
			showDeleteAllAlertDialog();
			return true;

		case R.id.action_save_sd:
			showSaveSDAlertDialog();
			return true;

		case R.id.action_load_sd:
			startLoadTask(EXECUTE_MATCH_LIST_LOAD_FROM_SD_CARD);
			return true;

		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	void onDeleteAll() {
		super.onDeleteAll();
		startSaveTask(EXECUTE_MATCH_LIST_DELETE_ALL);
	}

	@Override
	void onSaveSD() {
		super.onSaveSD();
		startSaveTask(EXECUTE_MATCH_LIST_SAVE_TO_SD_CARD);
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

	Long doInBackgroundLoad(Object... params) {
		super.doInBackgroundSave(params);
		int execute = (Integer) params[0];

		switch (execute) {
		case EXECUTE_MATCH_LIST_ON_ITEM_CLICK:
			mStockDatabaseManager.getStockMatchById(mMatch);

			mStock_X.setSE(mMatch.getSE_X());
			mStock_X.setCode(mMatch.getCode_X());
			mStockDatabaseManager.getStock(mStock_X);

			mStock_Y.setSE(mMatch.getSE_Y());
			mStock_Y.setCode(mMatch.getCode_Y());
			mStockDatabaseManager.getStock(mStock_Y);

			Intent intent = new Intent(this, StockMatchChartListActivity.class);
			intent.putExtra(Setting.KEY_SORT_ORDER_STOCK_MATCH_LIST, mSortOrder);
			intent.putExtra(StockMatchChartListActivity.EXTRA_STOCK_MATCH_ID,
					mMatch.getId());
			startActivity(intent);
			break;

		case EXECUTE_MATCH_LIST_LOAD_FROM_SD_CARD:
			loadListFromSD(MATCH_LIST_XML_FILE_NAME);
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
		case EXECUTE_MATCH_DELETE:
			mStockDatabaseManager.deleteStockMatchById(mMatch);
			break;

		case EXECUTE_MATCH_LIST_DELETE_ALL:
			mStockDatabaseManager.deleteStockMatch();
			break;

		case EXECUTE_MATCH_LIST_SAVE_TO_SD_CARD:
			SaveListToSD(MATCH_LIST_XML_FILE_NAME);
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
		StockMatch stockMatch = null;

		try {
			eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_TAG:
					tagName = parser.getName();
					if (XML_TAG_ITEM.equals(tagName)) {
						stockMatch = new StockMatch();
					} else if (DatabaseContract.StockMatch.COLUMN_SE_X
							.equals(tagName)) {
						stockMatch.setSE_X(parser.nextText());
					} else if (DatabaseContract.StockMatch.COLUMN_CODE_X
							.equals(tagName)) {
						stockMatch.setCode_X(parser.nextText());
					} else if (DatabaseContract.StockMatch.COLUMN_NAME_X
							.equals(tagName)) {
						stockMatch.setName_X(parser.nextText());
					} else if (DatabaseContract.StockMatch.COLUMN_SE_Y
							.equals(tagName)) {
						stockMatch.setSE_Y(parser.nextText());
					} else if (DatabaseContract.StockMatch.COLUMN_CODE_Y
							.equals(tagName)) {
						stockMatch.setCode_Y(parser.nextText());
					} else if (DatabaseContract.StockMatch.COLUMN_NAME_Y
							.equals(tagName)) {
						stockMatch.setName_Y(parser.nextText());
					} else if (DatabaseContract.COLUMN_CREATED.equals(tagName)) {
						stockMatch.setCreated(parser.nextText());
					} else if (DatabaseContract.COLUMN_MODIFIED.equals(tagName)) {
						stockMatch.setModified(parser.nextText());
					} else {
					}
					break;
				case XmlPullParser.END_TAG:
					tagName = parser.getName();
					if (XML_TAG_ITEM.equals(tagName)) {
						if (stockMatch != null) {
							mStock_X.setSE(stockMatch.getSE_X());
							mStock_X.setCode(stockMatch.getCode_X());
							mStockDatabaseManager.getStock(mStock_X);

							mStock_Y.setSE(stockMatch.getSE_Y());
							mStock_Y.setCode(stockMatch.getCode_Y());
							mStockDatabaseManager.getStock(mStock_Y);

							if (!mStock_X.getName().equals(
									stockMatch.getName_X())) {
								stockMatch.setName_X(mStock_X.getName());
							}

							if (!mStock_Y.getName().equals(
									stockMatch.getName_Y())) {
								stockMatch.setName_Y(mStock_Y.getName());
							}

							if (!mStockDatabaseManager
									.isStockMatchExist(stockMatch)) {
								mStockDatabaseManager
										.insertStockMatch(stockMatch);
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
			for (StockMatch stockMatch : mStockMatchList) {
				xmlSerializer.startTag(null, XML_TAG_ITEM);
				xmlSerialize(xmlSerializer,
						DatabaseContract.StockMatch.COLUMN_SE_X,
						stockMatch.getSE_X());
				xmlSerialize(xmlSerializer,
						DatabaseContract.StockMatch.COLUMN_CODE_X,
						stockMatch.getCode_X());
				xmlSerialize(xmlSerializer,
						DatabaseContract.StockMatch.COLUMN_NAME_X,
						stockMatch.getName_X());
				xmlSerialize(xmlSerializer,
						DatabaseContract.StockMatch.COLUMN_SE_Y,
						stockMatch.getSE_Y());
				xmlSerialize(xmlSerializer,
						DatabaseContract.StockMatch.COLUMN_CODE_Y,
						stockMatch.getCode_Y());
				xmlSerialize(xmlSerializer,
						DatabaseContract.StockMatch.COLUMN_NAME_Y,
						stockMatch.getName_Y());
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_CREATED,
						stockMatch.getCreated());
				xmlSerialize(xmlSerializer, DatabaseContract.COLUMN_MODIFIED,
						stockMatch.getModified());
				xmlSerializer.endTag(null, XML_TAG_ITEM);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
			mSortOrderColumn = DatabaseContract.StockMatch.COLUMN_CODE_X;
			break;
		case R.id.action_5min:
			mSortOrderColumn = DatabaseContract.StockMatch.COLUMN_VALUE_5MIN;
			break;
		case R.id.action_15min:
			mSortOrderColumn = DatabaseContract.StockMatch.COLUMN_VALUE_15MIN;
			break;
		case R.id.action_30min:
			mSortOrderColumn = DatabaseContract.StockMatch.COLUMN_VALUE_30MIN;
			break;
		case R.id.action_60min:
			mSortOrderColumn = DatabaseContract.StockMatch.COLUMN_VALUE_60MIN;
			break;
		case R.id.action_day:
			mSortOrderColumn = DatabaseContract.StockMatch.COLUMN_VALUE_DAY;
			break;
		case R.id.action_week:
			mSortOrderColumn = DatabaseContract.StockMatch.COLUMN_VALUE_WEEK;
			break;
		case R.id.action_month:
			mSortOrderColumn = DatabaseContract.StockMatch.COLUMN_VALUE_MONTH;
			break;
		case R.id.created:
			mSortOrderColumn = DatabaseContract.COLUMN_CREATED;
			break;
		case R.id.modified:
			mSortOrderColumn = DatabaseContract.COLUMN_MODIFIED;
			break;
		default:
			mSortOrderColumn = DatabaseContract.StockMatch.COLUMN_CODE_X;
			break;
		}

		if (mSortOrderDirection.equals(DatabaseContract.ORDER_DIRECTION_ASC)) {
			mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_DESC;
		} else {
			mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_ASC;
		}

		mSortOrder = mSortOrderColumn + mSortOrderDirection;

		saveSetting(Setting.KEY_SORT_ORDER_STOCK_MATCH_LIST, mSortOrder);

		restartLoader();
	}

	void fillPeriodArray() {
		if (mPeriodArray == null) {
			mPeriodArray = new SparseArray<String>();

			mPeriodArray.put(mPeriodArray.size(), Constants.PERIOD_MONTH);
			mPeriodArray.put(mPeriodArray.size(), Constants.PERIOD_WEEK);
			mPeriodArray.put(mPeriodArray.size(), Constants.PERIOD_DAY);
			mPeriodArray.put(mPeriodArray.size(), Constants.PERIOD_60MIN);
			mPeriodArray.put(mPeriodArray.size(), Constants.PERIOD_30MIN);
			mPeriodArray.put(mPeriodArray.size(), Constants.PERIOD_15MIN);
			mPeriodArray.put(mPeriodArray.size(), Constants.PERIOD_5MIN);
		}
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
		setHeaderTextColor(mTextView5M, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextView15M, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextView30M, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextView60M, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDay, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewWeek, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewMonth, mHeaderTextDefaultColor);
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

		mTextViewNameCode = (TextView) findViewById(R.id.stock_name_code);
		if (mTextViewNameCode != null) {
			mTextViewNameCode.setOnClickListener(this);
		}

		mTextView5M = (TextView) findViewById(R.id.action_5min);
		if (mTextView5M != null) {
			mTextView5M.setOnClickListener(this);
			setVisibility(Constants.PERIOD_5MIN, mTextView5M);
		}

		mTextView15M = (TextView) findViewById(R.id.action_15min);
		if (mTextView15M != null) {
			mTextView15M.setOnClickListener(this);
			setVisibility(Constants.PERIOD_15MIN, mTextView15M);
		}

		mTextView30M = (TextView) findViewById(R.id.action_30min);
		if (mTextView30M != null) {
			mTextView30M.setOnClickListener(this);
			setVisibility(Constants.PERIOD_30MIN, mTextView30M);
		}

		mTextView60M = (TextView) findViewById(R.id.action_60min);
		if (mTextView60M != null) {
			mTextView60M.setOnClickListener(this);
			setVisibility(Constants.PERIOD_60MIN, mTextView60M);
		}

		mTextViewDay = (TextView) findViewById(R.id.action_day);
		if (mTextViewDay != null) {
			mTextViewDay.setOnClickListener(this);
			setVisibility(Constants.PERIOD_DAY, mTextViewDay);
		}

		mTextViewWeek = (TextView) findViewById(R.id.action_week);
		if (mTextViewWeek != null) {
			mTextViewWeek.setOnClickListener(this);
			setVisibility(Constants.PERIOD_WEEK, mTextViewWeek);
		}

		mTextViewMonth = (TextView) findViewById(R.id.action_month);
		if (mTextViewMonth != null) {
			mTextViewMonth.setOnClickListener(this);
			setVisibility(Constants.PERIOD_MONTH, mTextViewMonth);
		}

		if (mSortOrder.contains(DatabaseContract.StockMatch.COLUMN_CODE_X)) {
			setHeaderTextColor(mTextViewNameCode, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.StockMatch.COLUMN_VALUE_5MIN)) {
			setHeaderTextColor(mTextView15M, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.StockMatch.COLUMN_VALUE_15MIN)) {
			setHeaderTextColor(mTextView15M, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.StockMatch.COLUMN_VALUE_30MIN)) {
			setHeaderTextColor(mTextView30M, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.StockMatch.COLUMN_VALUE_60MIN)) {
			setHeaderTextColor(mTextView60M, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.StockMatch.COLUMN_VALUE_DAY)) {
			setHeaderTextColor(mTextViewDay, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.StockMatch.COLUMN_VALUE_WEEK)) {
			setHeaderTextColor(mTextViewWeek, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.StockMatch.COLUMN_VALUE_MONTH)) {
			setHeaderTextColor(mTextViewMonth, mHeaderTextHighlightColor);
		} else {
		}
	}

	void initListView() {
		String[] mLeftFrom = new String[] {
				DatabaseContract.StockMatch.COLUMN_NAME_Y,
				DatabaseContract.StockMatch.COLUMN_NAME_X };
		int[] mLeftTo = new int[] { R.id.name, R.id.code };

		String[] mRightFrom = new String[] {
				DatabaseContract.StockMatch.COLUMN_VALUE_5MIN,
				DatabaseContract.StockMatch.COLUMN_VALUE_15MIN,
				DatabaseContract.StockMatch.COLUMN_VALUE_30MIN,
				DatabaseContract.StockMatch.COLUMN_VALUE_60MIN,
				DatabaseContract.StockMatch.COLUMN_VALUE_DAY,
				DatabaseContract.StockMatch.COLUMN_VALUE_WEEK,
				DatabaseContract.StockMatch.COLUMN_VALUE_MONTH, };
		int[] mRightTo = new int[] { R.id.type_5min, R.id.type_15min,
				R.id.type_30min, R.id.type_60min, R.id.type_day,
				R.id.type_week, R.id.type_month };

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
				R.layout.activity_stock_match_list_right_item, null,
				mRightFrom, mRightTo, 0);
		if ((mRightListView != null) && (mRightAdapter != null)) {
			mRightAdapter.setViewBinder(new CustomViewBinder());
			mRightListView.setAdapter(mRightAdapter);
			mRightListView.setOnItemClickListener(this);
			mRightListView.setOnItemLongClickListener(this);
		}
	}

	void restartLoader() {
		mLoaderManager.restartLoader(LOADER_ID_MATCH_LIST, null, this);
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
		String condition1 = "";
		String condition2 = "";
		CursorLoader loader = null;

		for (int i = 0; i < STOCK_PERIOD_ARRAY_SIZE; i++) {
			if (Utility.getSettingBoolean(this, mPeriodArray.get(i))) {
				if (!TextUtils.isEmpty(condition1)) {
					condition1 += " AND ";
				}

				condition1 += "value_" + mPeriodArray.get(i) + " > 1";

				if (!TextUtils.isEmpty(condition2)) {
					condition2 += " AND ";
				}

				condition2 += "value_" + mPeriodArray.get(i) + " < -1";
			}
		}

		selection = "(" + condition1 + ")" + " OR " + "(" + condition2 + ")";

		switch (id) {
		case LOADER_ID_MATCH_LIST:
			loader = new CursorLoader(this,
					DatabaseContract.StockMatch.CONTENT_URI,
					DatabaseContract.StockMatch.PROJECTION_ALL, selection,
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
		case LOADER_ID_MATCH_LIST:
			setStockMatchList(cursor);

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

	void setStockMatchList(Cursor cursor) {
		mStockMatchList.clear();

		try {
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockMatch stockMatch = new StockMatch();
					stockMatch.set(cursor);
					mStockMatchList.add(stockMatch);
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
			mMatch.setId(id);
			startLoadTask(EXECUTE_MATCH_LIST_ON_ITEM_CLICK);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {

		if (mCurrentActionMode != null) {
			return false;
		}

		mMatch.setId(id);
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

			if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.StockMatch.COLUMN_VALUE_5MIN)) {
				return setTextViewValue(Constants.PERIOD_5MIN, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.StockMatch.COLUMN_VALUE_15MIN)) {
				return setTextViewValue(Constants.PERIOD_15MIN, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.StockMatch.COLUMN_VALUE_30MIN)) {
				return setTextViewValue(Constants.PERIOD_30MIN, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.StockMatch.COLUMN_VALUE_60MIN)) {
				return setTextViewValue(Constants.PERIOD_60MIN, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.StockMatch.COLUMN_VALUE_DAY)) {
				return setTextViewValue(Constants.PERIOD_DAY, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.StockMatch.COLUMN_VALUE_WEEK)) {
				return setTextViewValue(Constants.PERIOD_WEEK, view);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.StockMatch.COLUMN_VALUE_MONTH)) {
				return setTextViewValue(Constants.PERIOD_MONTH, view);
			}

			return false;
		}
	}
}
