package com.android.orion;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import android.app.LoaderManager;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.FinancialData;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.Search;
import com.android.orion.utility.Utility;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.DefaultYAxisValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.Utils;

public class StockDataChartListActivity extends BaseActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnChartGestureListener {
	static final String TAG = Constants.TAG + " "
			+ StockDataChartListActivity.class.getSimpleName();

	public static final int ITEM_VIEW_TYPE_MAIN = 0;
	public static final int ITEM_VIEW_TYPE_SUB = 1;
	public static final int LOADER_ID_STOCK_LIST = Settings.KEY_PERIODS.length + 1;
	public static final int FLING_DISTANCE = 50;
	public static final int FLING_VELOCITY = 100;
	public static final int REQUEST_CODE_SETTINGS = 0;

	public static final int MESSAGE_REFRESH = 0;
	public static final int MESSAGE_LOAD_STOCK_LIST = 1;

	boolean mKeyDisplayLatest = true;
	boolean mKeyDisplayCost = true;
	boolean mKeyDisplayCandle = false;
	boolean mKeyDisplayDeal = false;
	boolean mKeyDisplayBonus = false;
	boolean mKeyDisplayBPS = false;
	boolean mKeyDisplayNPS = false;
	boolean mKeyDisplayRoe = false;
	boolean mKeyDisplayRoi = false;

	int mStockListIndex = 0;

	String mSortOrder = null;

	ArrayList<String> mStockIDList = new ArrayList<String>();

	Menu mMenu = null;
	ListView mListView = null;
	StockDataChartArrayAdapter mStockDataChartArrayAdapter = null;
	ArrayList<StockDataChartItem> mStockDataChartItemList = null;
	ArrayList<StockDataChartItemMain> mStockDataChartItemMainList = null;
	ArrayList<StockDataChartItemSub> mStockDataChartItemSubList = null;
	ArrayList<StockDataChart> mStockDataChartList = null;

	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MESSAGE_REFRESH:
				if (mOrionService != null) {
					mStockDatabaseManager.deleteStockData(mStock.getId());
					mStockDatabaseManager.deleteFinancialData(mStock.getId());
					mStockDatabaseManager.deleteShareBonus(mStock.getId());

					mStock.setPinyin("");
					mStock.setTotalShare(0);
					mStock.setPrice(0);
					mStock.setCreated("");
					mStock.setModified("");
					mStockDatabaseManager.updateStock(mStock,
							mStock.getContentValues());

					mOrionService.download(mStock);

					restartLoader();
				}
				break;

			case MESSAGE_LOAD_STOCK_LIST:
				mStockList.clear();
				for (int i = 0; i < mStockIDList.size(); i++) {
					Stock stock = new Stock();
					stock.setId(Long.valueOf(mStockIDList.get(i)));
					mStockDatabaseManager.getStockById(stock);
					if (mStock.getId() == stock.getId()) {
						mStock.set(stock);
						mStockListIndex = mStockList.size();
					}
					mStockList.add(stock);
				}
				break;

			default:
				break;
			}
		}
	};

	MainHandler mMainHandler = new MainHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// For chart init only
		Utils.init(this);
		// For chart init only

		setContentView(R.layout.activity_stock_data_chart_list);

		initListView();

		mStock.setId(getIntent().getLongExtra(Constants.EXTRA_STOCK_ID,
				Stock.INVALID_ID));
		mStockIDList = getIntent().getStringArrayListExtra(
				Constants.EXTRA_STOCK_ID_LIST);
		if ((mStockIDList != null) && (mStockIDList.size() > 0)) {
			mHandler.sendEmptyMessage(MESSAGE_LOAD_STOCK_LIST);
		}
		mSortOrder = getIntent().getStringExtra(
				Constants.EXTRA_STOCK_LIST_SORT_ORDER);

		mKeyDisplayLatest = Preferences.getBoolean(mContext, Settings.KEY_DISPLAY_LATEST, true);
		mKeyDisplayCost = Preferences.getBoolean(mContext, Settings.KEY_DISPLAY_COST, true);
		mKeyDisplayCandle = Preferences.getBoolean(mContext, Settings.KEY_DISPLAY_CANDLE,
				false);

		if (getIntent().getBooleanExtra(Constants.EXTRA_STOCK_DEAL, false)) {
			mKeyDisplayDeal = true;
		} else {
			mKeyDisplayDeal = Preferences.getBoolean(mContext, Settings.KEY_DISPLAY_DEAL, false);
		}

		if (getIntent().getBooleanExtra(Constants.EXTRA_STOCK_FINANCIAL, false)) {
			mKeyDisplayBonus = Preferences
					.getBoolean(mContext, Settings.KEY_DISPLAY_BONUS, false);
			mKeyDisplayBPS = Preferences.getBoolean(mContext, Settings.KEY_DISPLAY_BPS, false);
			mKeyDisplayNPS = Preferences.getBoolean(mContext, Settings.KEY_DISPLAY_NPS, false);
			mKeyDisplayRoe = Preferences.getBoolean(mContext, Settings.KEY_DISPLAY_ROE, false);
			mKeyDisplayRoi = Preferences.getBoolean(mContext, Settings.KEY_DISPLAY_ROI, false);
		} else {
			mKeyDisplayBonus = false;
			mKeyDisplayBPS = false;
			mKeyDisplayNPS = false;
			mKeyDisplayRoe = false;
			mKeyDisplayRoi = false;
		}

		initLoader();

		updateTitle();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		getMenuInflater().inflate(R.menu.stock_data_chart, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		case R.id.action_prev:
			navigateStock(-1);
			return true;

		case R.id.action_next:
			navigateStock(1);
			return true;

		case R.id.action_refresh:
			mHandler.sendEmptyMessage(MESSAGE_REFRESH);
			return true;

		case R.id.action_settings:
			startActivityForResult(new Intent(this,
					ServiceSettingActivity.class), REQUEST_CODE_SETTINGS);
			return true;

		case R.id.action_edit:
			mIntent = new Intent(this, StockActivity.class);
			mIntent.setAction(StockActivity.ACTION_STOCK_EDIT);
			mIntent.putExtra(Constants.EXTRA_STOCK_ID, mStock.getId());
			startActivity(mIntent);
			return true;

		case R.id.action_deal:
			Bundle bundle = new Bundle();
			bundle.putString(Constants.EXTRA_STOCK_SE, mStock.getSE());
			bundle.putString(Constants.EXTRA_STOCK_CODE, mStock.getCode());
			Intent intent = new Intent(this, StockDealListActivity.class);
			intent.putExtras(bundle);
			startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_SETTINGS:
				restartLoader();
				break;

			default:
				break;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mStockDataChartItemList.clear();

		for (int i = 0; i < Settings.KEY_PERIODS.length; i++) {
			if (Preferences.getBoolean(this, Settings.KEY_PERIODS[i], false)) {
				mStockDataChartItemList.add(mStockDataChartItemMainList.get(i));
				mStockDataChartItemList.add(mStockDataChartItemSubList.get(i));
			}
		}

		restartLoader();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader loader = null;

		if (id == LOADER_ID_STOCK_LIST) {
			loader = getStockCursorLoader();
		} else {
			loader = getStockDataCursorLoader(Settings.KEY_PERIODS[id]);
		}

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		int id = 0;

		if (loader == null) {
			return;
		}

		id = loader.getId();

		if (id == LOADER_ID_STOCK_LIST) {
			swapStockCursor(cursor);
		} else {
			swapStockDataCursor(mStockDataChartList.get(id), cursor);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = 0;

		if (loader == null) {
			return;
		}

		id = loader.getId();

		if (id == LOADER_ID_STOCK_LIST) {
			swapStockCursor(null);
		} else {
			swapStockDataCursor(mStockDataChartList.get(id), null);
		}
	}

	void initListView() {
		mListView = (ListView) findViewById(R.id.listView);

		if (mStockDataChartList == null) {
			mStockDataChartList = new ArrayList<StockDataChart>();
		}

		if (mStockDataChartItemList == null) {
			mStockDataChartItemList = new ArrayList<StockDataChartItem>();
		}

		if (mStockDataChartItemMainList == null) {
			mStockDataChartItemMainList = new ArrayList<StockDataChartItemMain>();
		}

		if (mStockDataChartItemSubList == null) {
			mStockDataChartItemSubList = new ArrayList<StockDataChartItemSub>();
		}

		for (int i = 0; i < Settings.KEY_PERIODS.length; i++) {
			mStockDataChartList.add(new StockDataChart(Settings.KEY_PERIODS[i]));
			mStockDataChartItemMainList.add(new StockDataChartItemMain(
					mStockDataChartList.get(i)));
			mStockDataChartItemSubList.add(new StockDataChartItemSub(
					mStockDataChartList.get(i)));
			mStockDataChartItemList.add(mStockDataChartItemMainList.get(i));
			mStockDataChartItemList.add(mStockDataChartItemSubList.get(i));
		}

		mStockDataChartArrayAdapter = new StockDataChartArrayAdapter(this,
				mStockDataChartItemList);
		mListView.setAdapter(mStockDataChartArrayAdapter);
	}

	void initLoader() {
		if (mStockIDList == null) {
			mLoaderManager.initLoader(LOADER_ID_STOCK_LIST, null, this);
		}

		for (int i = 0; i < Settings.KEY_PERIODS.length; i++) {
			if (Preferences.getBoolean(this, Settings.KEY_PERIODS[i], false)) {
				mLoaderManager.initLoader(i, null, this);
			}
		}
	}

	void restartLoader(Intent intent) {
		if (intent.getLongExtra(Constants.EXTRA_STOCK_ID,
				Stock.INVALID_ID) == mStock.getId()) {
			restartLoader();
		}
	}

	void restartLoader() {
		if (mStockIDList == null) {
			mLoaderManager.restartLoader(LOADER_ID_STOCK_LIST, null, this);
		}

		for (int i = 0; i < Settings.KEY_PERIODS.length; i++) {
			if (Preferences.getBoolean(this, Settings.KEY_PERIODS[i], false)) {
				mLoaderManager.restartLoader(i, null, this);
			}
		}
	}

	CursorLoader getStockCursorLoader() {
		String selection = "";
		CursorLoader loader = null;

		mStockFilter.read();
		selection += mStockFilter.getSelection();

		loader = new CursorLoader(this, DatabaseContract.Stock.CONTENT_URI,
				DatabaseContract.Stock.PROJECTION_ALL, selection, null,
				mSortOrder);

		return loader;
	}

	CursorLoader getStockDataCursorLoader(String period) {
		String selection = "";
		String sortOrder = "";
		CursorLoader loader = null;

		selection = mStockDatabaseManager.getStockDataSelection(mStock.getId(),
				period, StockData.LEVEL_NONE);

		sortOrder = mStockDatabaseManager.getStockDataOrder();

		loader = new CursorLoader(this, DatabaseContract.StockData.CONTENT_URI,
				DatabaseContract.StockData.PROJECTION_ALL, selection, null,
				sortOrder);

		return loader;
	}

	void updateMenuAction() {
		int size = 0;
		if (mMenu == null) {
			return;
		}

		MenuItem actionPrev = mMenu.findItem(R.id.action_prev);
		MenuItem actionNext = mMenu.findItem(R.id.action_next);

		size = mStockList.size();

		if (actionPrev != null) {
			if (size > 1) {
				actionPrev.setEnabled(true);
			} else {
				actionPrev.setEnabled(false);
			}
		}

		if (actionNext != null) {
			if (size > 1) {
				actionNext.setEnabled(true);
			} else {
				actionNext.setEnabled(false);
			}
		}
	}

	void updateTitle() {
		if (mStock != null) {
			setTitle(mStock.getName() + " " + mStock.getCode());
		}
	}

	public void swapStockCursor(Cursor cursor) {
		if (mStockList == null) {
			return;
		}

		mStockList.clear();

		try {
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					Stock stock = new Stock();
					stock.set(cursor);

					if (stock != null) {
						if (mStock.getId() == stock.getId()) {
							mStock.set(cursor);

							mStockListIndex = mStockList.size();

							if (mMainHandler != null) {
								mMainHandler.sendEmptyMessage(0);
							}
						}
						mStockList.add(stock);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}

		if (mMainHandler != null) {
			mMainHandler.sendEmptyMessage(0);
		}
	}

	public void swapStockDataCursor(StockDataChart stockDataChart, Cursor cursor) {
		int index = 0;
		float bookValuePerShare = 0;
		float netProfitPerShare = 0;
		float roe = 0;
		float roi = 0;
		float dividend = 0;
		String sortOrder = DatabaseContract.COLUMN_DATE + " ASC ";
		FinancialData financialData = null;
		ShareBonus shareBonus = null;

		if (mStockData == null) {
			return;
		}

		stockDataChart.clear();

		try {
			if ((cursor != null) && (cursor.getCount() > 0)) {
				String dateString = "";
				String timeString = "";

				mStockDatabaseManager.getFinancialDataList(mStock, mFinancialDataList,
						sortOrder);
				mStockDatabaseManager.getShareBonusList(mStock, mShareBonusList,
						sortOrder);

				while (cursor.moveToNext()) {
					index = stockDataChart.mXValues.size();
					mStockData.set(cursor);

					dateString = mStockData.getDate();
					timeString = mStockData.getTime();

					if (mStockData.getPeriod().equals(Settings.KEY_PERIOD_YEAR)
							|| mStockData.getPeriod().equals(
									Settings.KEY_PERIOD_QUARTER)
							|| mStockData.getPeriod().equals(
									Settings.KEY_PERIOD_MONTH)
							|| mStockData.getPeriod().equals(
									Settings.KEY_PERIOD_WEEK)
							|| mStockData.getPeriod().equals(
									Settings.KEY_PERIOD_DAY)) {
						stockDataChart.mXValues.add(dateString);
					} else {
						stockDataChart.mXValues.add(dateString + " "
								+ timeString);
					}

					if (mKeyDisplayCandle) {
						CandleEntry candleEntry = new CandleEntry(index,
								(float) mStockData.getHigh(),
								(float) mStockData.getLow(),
								(float) mStockData.getOpen(),
								(float) mStockData.getClose(),
								mStockData.getAction());
						stockDataChart.mCandleEntryList.add(candleEntry);
					}

					if (mStockData.vertexOf(StockData.VERTEX_TOP)) {
						Entry drawEntry = new Entry(
								(float) mStockData.getVertexHigh(), index);
						stockDataChart.mDrawEntryList.add(drawEntry);
					} else if (mStockData
							.vertexOf(StockData.VERTEX_BOTTOM)) {
						Entry drawEntry = new Entry(
								(float) mStockData.getVertexLow(), index);
						stockDataChart.mDrawEntryList.add(drawEntry);
					}

					if (index == cursor.getCount() - 1) {
						float val = 0;
						if (mStockData
								.directionOf(StockData.DIRECTION_UP)) {
							val = (float) mStockData.getHigh();
						} else if (mStockData
								.directionOf(StockData.DIRECTION_DOWN)) {
							val = (float) mStockData.getLow();
						}
						Entry drawEntry = new Entry(val, index);
						stockDataChart.mDrawEntryList.add(drawEntry);
					}

					if (mStockData.vertexOf(StockData.VERTEX_TOP_STROKE)) {
						Entry strokeEntry = new Entry(
								(float) mStockData.getVertexHigh(), index);
						stockDataChart.mStrokeEntryList.add(strokeEntry);
					} else if (mStockData
							.vertexOf(StockData.VERTEX_BOTTOM_STROKE)) {
						Entry strokeEntry = new Entry(
								(float) mStockData.getVertexLow(), index);
						stockDataChart.mStrokeEntryList.add(strokeEntry);
					}

					if (mStockData.vertexOf(StockData.VERTEX_TOP_SEGMENT)) {
						Entry segmentEntry = new Entry(
								(float) mStockData.getVertexHigh(), index);
						stockDataChart.mSegmentEntryList.add(segmentEntry);
					} else if (mStockData
							.vertexOf(StockData.VERTEX_BOTTOM_SEGMENT)) {
						Entry segmentEntry = new Entry(
								(float) mStockData.getVertexLow(), index);
						stockDataChart.mSegmentEntryList.add(segmentEntry);
					}

					if ((mStockData.getOverlapHigh() > 0)
							&& (mStockData.getOverlapLow() > 0)) {
						Entry overlayHighEntry = new Entry(
								(float) mStockData.getOverlapHigh(), index);
						stockDataChart.mOverlapHighEntryList
								.add(overlayHighEntry);

						Entry overlapLowEntry = new Entry(
								(float) mStockData.getOverlapLow(), index);
						stockDataChart.mOverlapLowEntryList
								.add(overlapLowEntry);
					}

					if (mKeyDisplayRoi) {
						roi = (float) mStockData.getRoi();
						Entry roiEntry = new Entry(roi, index);
						stockDataChart.mRoiList.add(roiEntry);
					}

					if (mFinancialDataList.size() > 0) {
						bookValuePerShare = 0;
						netProfitPerShare = 0;
						roe = 0;

						financialData = Search.getFinancialDataByDate(dateString,
								mFinancialDataList);
						if (financialData != null) {
							bookValuePerShare = (float) financialData
									.getBookValuePerShare();
							netProfitPerShare = (float) financialData
									.getNetProfitPerShare() * 10;
							roe = (float) financialData.getRoe();
						}

						if (mKeyDisplayBPS) {
							Entry bookValuePerShareEntry = new Entry(
									bookValuePerShare, index);
							stockDataChart.mBookValuePerShareList
									.add(bookValuePerShareEntry);
						}

						if (mKeyDisplayNPS) {
							Entry netProfitPerShareEntry = new Entry(
									netProfitPerShare, index);
							stockDataChart.mNetProfitPerShareList
									.add(netProfitPerShareEntry);
						}

						if (mKeyDisplayRoe) {
							Entry roeEntry = new Entry(roe, index);
							stockDataChart.mRoeList.add(roeEntry);
						}
					}

					if (mShareBonusList.size() > 0) {
						dividend = 0;

						if (mKeyDisplayBonus) {
							shareBonus = Search.getShareBonusByDate(dateString,
									mShareBonusList);
							if (shareBonus != null) {
								dividend = (float) (shareBonus.getDividend());
							}

							BarEntry shareBonusEntry = new BarEntry(dividend,
									index);
							stockDataChart.mDividendEntryList
									.add(shareBonusEntry);
						}
					}

					if (mKeyDisplayCandle) {
						Entry average5Entry = new Entry(
								(float) mStockData.getAverage5(), index);
						stockDataChart.mAverage5EntryList.add(average5Entry);

						Entry average10Entry = new Entry(
								(float) mStockData.getAverage10(), index);
						stockDataChart.mAverage10EntryList.add(average10Entry);
					}

					Entry difEntry = new Entry((float) mStockData.getDIF(),
							index);
					stockDataChart.mDIFEntryList.add(difEntry);

					Entry deaEntry = new Entry((float) mStockData.getDEA(),
							index);
					stockDataChart.mDEAEntryList.add(deaEntry);

					BarEntry histogramBarEntry = new BarEntry(
							(float) mStockData.getHistogram(), index);
					stockDataChart.mHistogramEntryList.add(histogramBarEntry);
				}
			}

			updateTitle();

			loadStockDealList();

			stockDataChart.updateDescription(mStock);
			stockDataChart.updateXLimitLines(mStock, mStockDealList, mKeyDisplayLatest, mKeyDisplayCost, mKeyDisplayDeal);
			stockDataChart.setMainChartData();
			stockDataChart.setSubChartData();

			mStockDataChartArrayAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}
	}

	void loadStockDealList() {
		String selection = DatabaseContract.COLUMN_SE + " = " + "\'" + mStock.getSE()
				+ "\'" + " AND " + DatabaseContract.COLUMN_CODE + " = " + "\'"
				+ mStock.getCode() + "\'";
		String sortOrder = DatabaseContract.COLUMN_DEAL + " DESC ";

		mStockDatabaseManager.getStockDealList(mStock, mStockDealList, selection, sortOrder);
	}

	void navigateStock(int direction) {
		boolean loop = true;

		if ((mStockList == null) || (mStockList.size() == 0)) {
			return;
		}

		mStockListIndex += direction;

		if (mStockListIndex > mStockList.size() - 1) {
			if (loop) {
				mStockListIndex = 0;
			} else {
				mStockListIndex = mStockList.size() - 1;
			}
		}

		if (mStockListIndex < 0) {
			if (loop) {
				mStockListIndex = mStockList.size() - 1;
			} else {
				mStockListIndex = 0;
			}
		}

		mStock = mStockList.get(mStockListIndex);

		restartLoader();
	}

	static class MainHandler extends Handler {
		private final WeakReference<StockDataChartListActivity> mActivity;

		MainHandler(StockDataChartListActivity activity) {
			mActivity = new WeakReference<StockDataChartListActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			StockDataChartListActivity activity = mActivity.get();
			activity.updateTitle();
			activity.updateMenuAction();
		}
	}

	class StockDataChartItem {
		int mItemViewType;
		int mResource;
		StockDataChart mStockDataChart;

		public StockDataChartItem() {
		}

		public StockDataChartItem(int itemViewType, int resource,
				StockDataChart stockDataChart) {
			mItemViewType = itemViewType;
			mResource = resource;
			mStockDataChart = stockDataChart;
		}

		public int getItemViewType() {
			return mItemViewType;
		}

		public View getView(int position, View view, Context context) {
			ViewHolder viewHolder = null;
			XAxis xAxis = null;
			YAxis leftYAxis = null;
			YAxis rightYAxis = null;

			// For android 5 and above solution:
			// if (view == null) {
			view = LayoutInflater.from(context).inflate(mResource, null);
			viewHolder = new ViewHolder();
			viewHolder.chart = (CombinedChart) view.findViewById(R.id.chart);
			view.setTag(viewHolder);
			// } else {
			// viewHolder = (ViewHolder) view.getTag();
			// }

			viewHolder.chart.setBackgroundColor(Color.LTGRAY);
			viewHolder.chart.setGridBackgroundColor(Color.LTGRAY);

			viewHolder.chart.setMaxVisibleValueCount(0);

			xAxis = viewHolder.chart.getXAxis();
			if (xAxis != null) {
				xAxis.setPosition(XAxisPosition.BOTTOM);
			}

			leftYAxis = viewHolder.chart.getAxisLeft();
			if (leftYAxis != null) {
				leftYAxis.setPosition(YAxisLabelPosition.INSIDE_CHART);
				leftYAxis.setStartAtZero(false);
				leftYAxis.setValueFormatter(new DefaultYAxisValueFormatter(2));
				leftYAxis.removeAllLimitLines();
				if (mItemViewType == ITEM_VIEW_TYPE_MAIN) {
					for (int i = 0; i < mStockDataChart.mXLimitLineList.size(); i++) {
						leftYAxis.addLimitLine(mStockDataChart.mXLimitLineList
								.get(i));
					}
				}
			}

			rightYAxis = viewHolder.chart.getAxisRight();
			if (rightYAxis != null) {
				rightYAxis.setEnabled(false);
			}

			viewHolder.chart.setDescription(mStockDataChart.mDescription);

			if (mItemViewType == ITEM_VIEW_TYPE_MAIN) {
				viewHolder.chart.setData(mStockDataChart.mCombinedDataMain);
			} else {
				viewHolder.chart.setData(mStockDataChart.mCombinedDataSub);
			}

			return view;
		}

		class ViewHolder {
			CombinedChart chart;
		}
	}

	class StockDataChartItemMain extends StockDataChartItem {
		public StockDataChartItemMain(StockDataChart stockDataChart) {
			super(ITEM_VIEW_TYPE_MAIN,
					R.layout.activity_stock_data_chart_list_item_main,
					stockDataChart);
		}
	}

	class StockDataChartItemSub extends StockDataChartItem {
		public StockDataChartItemSub(StockDataChart stockDataChart) {
			super(ITEM_VIEW_TYPE_SUB,
					R.layout.activity_stock_data_chart_list_item_sub,
					stockDataChart);
		}
	}

	class StockDataChartArrayAdapter extends ArrayAdapter<StockDataChartItem> {

		public StockDataChartArrayAdapter(Context context,
				List<StockDataChartItem> objects) {
			super(context, 0, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return getItem(position).getView(position, convertView,
					getContext());
		}

		@Override
		public int getItemViewType(int position) {
			return getItem(position).getItemViewType();
		}

		@Override
		public int getViewTypeCount() {
			return mStockDataChartItemList.size();
		}
	}

	@Override
	public void onChartLongPressed(MotionEvent me) {
	}

	@Override
	public void onChartDoubleTapped(MotionEvent me) {
	}

	@Override
	public void onChartSingleTapped(MotionEvent me) {
	}

	@Override
	public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX,
			float velocityY) {
		int distance = FLING_DISTANCE;
		int velocity = FLING_VELOCITY;

		if (me1.getX() - me2.getX() > distance
				&& Math.abs(velocityX) > velocity) {
			navigateStock(-1);
		}

		if (me2.getX() - me1.getX() > distance
				&& Math.abs(velocityX) > velocity) {
			navigateStock(1);
		}
	}

	@Override
	public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
	}

	@Override
	public void onChartTranslate(MotionEvent me, float dX, float dY) {
	}

	@Override
	public void onChartGestureStart(MotionEvent me,
			ChartGesture lastPerformedGesture) {
	}

	@Override
	public void onChartGestureEnd(MotionEvent me,
			ChartGesture lastPerformedGesture) {
	}
}
