package com.android.orion.activity;

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

import androidx.annotation.NonNull;

import com.android.orion.R;
import com.android.orion.chart.StockDataChart;
import com.android.orion.data.Period;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDeal;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.DefaultYAxisValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class StockFavoriteChartListActivity extends BaseActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnChartGestureListener {

	public static final int ITEM_VIEW_TYPE_MAIN = 0;
	public static final int ITEM_VIEW_TYPE_SUB = 1;
	public static final int LOADER_ID_STOCK_LIST = Period.PERIODS.length + 1;
	public static final int REQUEST_CODE_SETTING = 0;
	public static final int REQUEST_CODE_SETTING_DEBUG_LOOPBACK = 1;
	public static final int MESSAGE_REFRESH = 0;
	public static final int MESSAGE_LOAD_STOCK_LIST = 1;

	boolean mKeyDisplayThreshold = true;
	boolean mKeyDisplayLatest = true;
	boolean mKeyDisplayCost = true;
	boolean mKeyDisplayDeal = false;
	boolean mKeyDisplayBonus = false;

	int mStockListIndex = 0;

	String mSortOrder = null;

	ArrayList<String> mStockIDList = new ArrayList<>();

	StockData mStockData = new StockData();

	Menu mMenu = null;
	ListView mListView = null;
	StockDataChartArrayAdapter mStockDataChartArrayAdapter = null;
	ArrayList<StockDataChartItem> mStockDataChartItemList = null;
	ArrayList<StockDataChartItemMain> mStockDataChartItemMainList = null;
	ArrayList<StockDataChartItemSub> mStockDataChartItemSubList = null;
	ArrayList<StockDataChart> mStockDataChartList = null;
	ArrayList<StockDeal> mStockDealList = new ArrayList<>();

	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
				case MESSAGE_REFRESH:
					mDatabaseManager.deleteStockData(mStock.getId());
					Setting.setDownloadStockData(mStock.getSE(), mStock.getCode(), 0);
					mStockDataProvider.download(mStock);
					restartLoader();
					break;

				case MESSAGE_LOAD_STOCK_LIST:
					mStockList.clear();
					for (int i = 0; i < mStockIDList.size(); i++) {
						Stock stock = new Stock();
						stock.setId(Long.parseLong(mStockIDList.get(i)));
						mDatabaseManager.getStockById(stock);
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

		mStock.setId(getIntent().getLongExtra(Constant.EXTRA_STOCK_ID,
				Stock.INVALID_ID));
		mStockIDList = getIntent().getStringArrayListExtra(
				Constant.EXTRA_STOCK_ID_LIST);
		if ((mStockIDList != null) && (mStockIDList.size() > 0)) {
			mHandler.sendEmptyMessage(MESSAGE_LOAD_STOCK_LIST);
		}
		mSortOrder = getIntent().getStringExtra(
				Constant.EXTRA_STOCK_LIST_SORT_ORDER);

		mKeyDisplayDeal = getIntent().getBooleanExtra(Constant.EXTRA_STOCK_DEAL, false);
		mKeyDisplayBonus = getIntent().getBooleanExtra(Constant.EXTRA_STOCK_BONUS, false);

		initLoader();

		updateTitle();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		mKeyDisplayDeal = getIntent().getBooleanExtra(Constant.EXTRA_STOCK_DEAL, false);
		mKeyDisplayBonus = getIntent().getBooleanExtra(Constant.EXTRA_STOCK_BONUS, false);
		restartLoader();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		getMenuInflater().inflate(R.menu.stock_data_chart, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		MenuItem menuLoopback = menu.findItem(R.id.action_loopback);
		menuLoopback.setVisible(Setting.getDebugLoopback());
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

			case R.id.action_setting:
				startActivityForResult(new Intent(this,
						SettingActivity.class), REQUEST_CODE_SETTING);
				return true;

			case R.id.action_edit:
				mIntent = new Intent(this, StockActivity.class);
				mIntent.setAction(Constant.ACTION_STOCK_EDIT);
				mIntent.putExtra(Constant.EXTRA_STOCK_ID, mStock.getId());
				startActivity(mIntent);
				return true;

			case R.id.action_deal: {
				Bundle bundle = new Bundle();
				bundle.putString(Constant.EXTRA_STOCK_SE, mStock.getSE());
				bundle.putString(Constant.EXTRA_STOCK_CODE, mStock.getCode());
				Intent intent = new Intent(this, StockDealListActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
				return true;
			}

			case R.id.action_trend:
				mIntent = new Intent(this, StockTrendListActivity.class);
				mIntent.setAction(Constant.ACTION_STOCK_TREND_LIST);
				mIntent.putExtra(Constant.EXTRA_STOCK_ID, mStock.getId());
				startActivity(mIntent);
				return true;

			case R.id.action_loopback: {
				startActivityForResult(new Intent(this,
						SettingLoopbackActivity.class), REQUEST_CODE_SETTING_DEBUG_LOOPBACK);
				return true;
			}

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
				case REQUEST_CODE_SETTING:
					restartLoader();
					break;

				case REQUEST_CODE_SETTING_DEBUG_LOOPBACK:
					for (String period : Period.PERIODS) {
						if (Setting.getPeriod(period)) {
							mDatabaseManager.deleteStockData(mStock.getId(), period);
						}
					}
					Setting.setDownloadStockData(mStock.getSE(), mStock.getCode(), 0);
					mStockDataProvider.download(mStock);
					break;

				default:
					break;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		updateStockDataChartItemList();
		restartLoader();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader loader;

		if (id == LOADER_ID_STOCK_LIST) {
			loader = getStockCursorLoader();
		} else {
			loader = getStockDataCursorLoader(Period.PERIODS[id]);
		}

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader == null) {
			return;
		}

		int id = loader.getId();
		if (id == LOADER_ID_STOCK_LIST) {
			swapStockCursor(cursor);
		} else {
			swapStockDataCursor(mStockDataChartList.get(id), cursor);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader == null) {
			return;
		}

		int id = loader.getId();
		if (id == LOADER_ID_STOCK_LIST) {
			swapStockCursor(null);
		} else {
			swapStockDataCursor(mStockDataChartList.get(id), null);
		}
	}

	void initListView() {
		mListView = findViewById(R.id.listView);

		if (mStockDataChartList == null) {
			mStockDataChartList = new ArrayList<>();
		}

		if (mStockDataChartItemList == null) {
			mStockDataChartItemList = new ArrayList<>();
		}

		if (mStockDataChartItemMainList == null) {
			mStockDataChartItemMainList = new ArrayList<>();
		}

		if (mStockDataChartItemSubList == null) {
			mStockDataChartItemSubList = new ArrayList<>();
		}

		int selection = 0;
		for (int i = 0; i < Period.PERIODS.length; i++) {
			if (StockData.getPeriodIndex(Period.PERIODS[i]) < StockData.getPeriodIndex(DatabaseContract.COLUMN_DAY)) {
				if (Setting.getPeriod(Period.PERIODS[i])) {
					selection += 2;
				}
			}
			mStockDataChartList.add(new StockDataChart(mStock, Period.PERIODS[i]));
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
		mListView.setSelection(selection);
	}

	void initLoader() {
		if (mStockIDList == null) {
			mLoaderManager.initLoader(LOADER_ID_STOCK_LIST, null, this);
		}

		mDatabaseManager.getStockById(mStock);
		for (int i = 0; i < Period.PERIODS.length; i++) {
			if (Setting.getPeriod(Period.PERIODS[i])) {
				mLoaderManager.initLoader(i, null, this);
			}
		}
	}

	void restartLoader(Intent intent) {
		if (intent == null) {
			return;
		}

		if (intent.getLongExtra(Constant.EXTRA_STOCK_ID,
				Stock.INVALID_ID) == mStock.getId()) {
			restartLoader();
		}
	}

	void restartLoader() {
		if (mStockIDList == null) {
			mLoaderManager.restartLoader(LOADER_ID_STOCK_LIST, null, this);
		}

		mDatabaseManager.getStockById(mStock);
		for (int i = 0; i < Period.PERIODS.length; i++) {
			if (Setting.getPeriod(Period.PERIODS[i])) {
				mLoaderManager.restartLoader(i, null, this);
			}
		}
	}

	CursorLoader getStockCursorLoader() {
		String selection = "" + (DatabaseContract.COLUMN_FLAG + " >= "
				+ Stock.FLAG_FAVORITE);
		CursorLoader loader = new CursorLoader(this, DatabaseContract.Stock.CONTENT_URI,
				DatabaseContract.Stock.PROJECTION_ALL, selection, null,
				mSortOrder);
		return loader;
	}

	CursorLoader getStockDataCursorLoader(String period) {
		String selection = mDatabaseManager.getStockDataSelection(mStock.getId(),
				period, StockData.LEVEL_NONE);
		String sortOrder = mDatabaseManager.getStockDataOrder();
		CursorLoader loader = new CursorLoader(this, DatabaseContract.StockData.CONTENT_URI,
				DatabaseContract.StockData.PROJECTION_ALL, selection, null,
				sortOrder);

		return loader;
	}

	void updateMenuAction() {
		if (mMenu == null) {
			return;
		}

		MenuItem actionPrev = mMenu.findItem(R.id.action_prev);
		MenuItem actionNext = mMenu.findItem(R.id.action_next);

		int size = mStockList.size();
		if (actionPrev != null) {
			actionPrev.setEnabled(size > 1);
		}

		if (actionNext != null) {
			actionNext.setEnabled(size > 1);
		}
	}

	void updateTitle() {
		if (mStock != null) {
			setTitle(mStock.getName());
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
			mDatabaseManager.closeCursor(cursor);
		}

		if (mMainHandler != null) {
			mMainHandler.sendEmptyMessage(0);
		}
	}

	public void swapStockDataCursor(StockDataChart stockDataChart, Cursor cursor) {
		if (mStockData == null || stockDataChart == null) {
			return;
		}

		try {
			stockDataChart.clear();
			if ((cursor != null) && (cursor.getCount() > 0)) {
				String dateString = "";
				String timeString = "";

				while (cursor.moveToNext()) {
					int index = stockDataChart.mXValues.size();
					mStockData.set(cursor);

					dateString = mStockData.getDate();
					timeString = mStockData.getTime();

					if (mStockData.isMinutePeriod()) {
						stockDataChart.mXValues.add(dateString + " "
								+ timeString);
					} else {
						stockDataChart.mXValues.add(dateString);
					}

					if (mKeyDisplayThreshold) {
						if (mStockData.getNaturalRally() > 0) {
							BubbleEntry entry = new BubbleEntry(index, (float) mStockData.getNaturalRally(), 0);
							stockDataChart.mNaturalRallyList.add(entry);
						}

						if (mStockData.getUpwardTrend() > 0) {
							BubbleEntry entry = new BubbleEntry(index, (float) mStockData.getUpwardTrend(), 0);
							stockDataChart.mUpwardTrendList.add(entry);
						}

						if (mStockData.getDownwardTrend() > 0) {
							BubbleEntry entry = new BubbleEntry(index, (float) mStockData.getDownwardTrend(), 0);
							stockDataChart.mDownwardTrendList.add(entry);
						}

						if (mStockData.getNaturalReaction() > 0) {
							BubbleEntry entry = new BubbleEntry(index, (float) mStockData.getNaturalReaction(), 0);
							stockDataChart.mNaturalReactionList.add(entry);
						}
					}

					if (Setting.getDisplayCandle()) {
						CandleEntry candleEntry = new CandleEntry(index,
								(float) mStockData.getHigh(),
								(float) mStockData.getLow(),
								(float) mStockData.getOpen(),
								(float) mStockData.getClose(),
								mStockData.getAction());
						stockDataChart.mCandleEntryList.add(candleEntry);
					}

					if (mStockData.vertexOf(StockData.VERTEX_TOP_LEVEL_1)) {
						Entry drawEntry = new Entry(
								(float) mStockData.getVertexHigh(), index);
						stockDataChart.mLineList[0].add(drawEntry);
					} else if (mStockData
							.vertexOf(StockData.VERTEX_BOTTOM_LEVEL_1)) {
						Entry drawEntry = new Entry(
								(float) mStockData.getVertexLow(), index);
						stockDataChart.mLineList[0].add(drawEntry);
					}

					if (index == cursor.getCount() - 1) {
						float val = 0;
						if (mStockData
								.directionOf(StockData.DIRECTION_UP_LEVEL_1)) {
							val = (float) mStockData.getHigh();
						} else if (mStockData
								.directionOf(StockData.DIRECTION_DOWN_LEVEL_1)) {
							val = (float) mStockData.getLow();
						}
						Entry drawEntry = new Entry(val, index);
						stockDataChart.mLineList[0].add(drawEntry);
					}

					if (mStockData.vertexOf(StockData.VERTEX_TOP_LEVEL_2)) {
						Entry strokeEntry = new Entry(
								(float) mStockData.getVertexHigh(), index);
						stockDataChart.mLineList[1].add(strokeEntry);
					} else if (mStockData
							.vertexOf(StockData.VERTEX_BOTTOM_LEVEL_2)) {
						Entry strokeEntry = new Entry(
								(float) mStockData.getVertexLow(), index);
						stockDataChart.mLineList[1].add(strokeEntry);
					}

					if (mStockData.vertexOf(StockData.VERTEX_TOP_LEVEL_3)) {
						Entry segmentEntry = new Entry(
								(float) mStockData.getVertexHigh(), index);
						stockDataChart.mLineList[2].add(segmentEntry);
					} else if (mStockData
							.vertexOf(StockData.VERTEX_BOTTOM_LEVEL_3)) {
						Entry segmentEntry = new Entry(
								(float) mStockData.getVertexLow(), index);
						stockDataChart.mLineList[2].add(segmentEntry);
					}

					if (mStockData.vertexOf(StockData.VERTEX_TOP_LEVEL_4)) {
						Entry lineEntry = new Entry(
								(float) mStockData.getVertexHigh(), index);
						stockDataChart.mLineList[3].add(lineEntry);
					} else if (mStockData
							.vertexOf(StockData.VERTEX_BOTTOM_LEVEL_4)) {
						Entry lineEntry = new Entry(
								(float) mStockData.getVertexLow(), index);
						stockDataChart.mLineList[3].add(lineEntry);
					}

					if (mStockData.vertexOf(StockData.VERTEX_TOP_LEVEL_5)) {
						Entry outlineEntry = new Entry(
								(float) mStockData.getVertexHigh(), index);
						stockDataChart.mLineList[4].add(outlineEntry);
					} else if (mStockData
							.vertexOf(StockData.VERTEX_BOTTOM_LEVEL_5)) {
						Entry outlineEntry = new Entry(
								(float) mStockData.getVertexLow(), index);
						stockDataChart.mLineList[4].add(outlineEntry);
					}

					if (Setting.getDisplayCandle()) {
						Entry average5Entry = new Entry(
								(float) mStockData.getMacd().getAverage5(), index);
						stockDataChart.mAverage5EntryList.add(average5Entry);

						Entry average10Entry = new Entry(
								(float) mStockData.getMacd().getAverage10(), index);
						stockDataChart.mAverage10EntryList.add(average10Entry);
					}

					stockDataChart.setMainChartYMinMax(index, stockDataChart.mLineList[0], stockDataChart.mLineList[1], stockDataChart.mLineList[2]);

					Entry difEntry = new Entry((float) mStockData.getMacd().getDIF(),
							index);
					stockDataChart.mDIFEntryList.add(difEntry);

					Entry deaEntry = new Entry((float) mStockData.getMacd().getDEA(),
							index);
					stockDataChart.mDEAEntryList.add(deaEntry);

					BarEntry histogramBarEntry = new BarEntry(
							(float) mStockData.getMacd().getHistogram(), index);
					stockDataChart.mHistogramEntryList.add(histogramBarEntry);

					if (!TextUtils.isEmpty(mStock.getOperate()) && mStockData.isMinutePeriod()) {
						Entry velocityEntry = new Entry(
								(float) mStockData.getMacd().getVelocity(), index);
						stockDataChart.mVelocityEntryList.add(velocityEntry);
					}

					stockDataChart.setSubChartYMinMax(index, stockDataChart.mDIFEntryList, stockDataChart.mDEAEntryList);
				}
			}

			updateTitle();

			if (mKeyDisplayDeal) {
				loadStockDealList();
			}

			stockDataChart.updateDescription(mStock);
			stockDataChart.updateLimitLines(mStock, mStockDealList, mKeyDisplayLatest, mKeyDisplayCost, mKeyDisplayDeal);
			stockDataChart.setMainChartData(mContext);
			stockDataChart.setSubChartData(mContext);

			mStockDataChartArrayAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mDatabaseManager.closeCursor(cursor);
		}
	}

	void loadStockDealList() {
		String selection = DatabaseContract.COLUMN_SE + " = " + "'" + mStock.getSE()
				+ "'" + " AND " + DatabaseContract.COLUMN_CODE + " = " + "'"
				+ mStock.getCode() + "'";
		String sortOrder = DatabaseContract.COLUMN_BUY + " DESC ";

		mDatabaseManager.getStockDealList(mStockDealList, selection, sortOrder);
	}

	void updateStockDataChartItemList() {
		mStockDataChartItemList.clear();

		mDatabaseManager.getStockById(mStock);
		for (int i = 0; i < Period.PERIODS.length; i++) {
			if (Setting.getPeriod(Period.PERIODS[i])) {
				mStockDataChartItemMainList.get(i).mStockDataChart.setStock(mStock);
				mStockDataChartItemList.add(mStockDataChartItemMainList.get(i));
				mStockDataChartItemList.add(mStockDataChartItemSubList.get(i));
			}
		}
	}

	void navigateStock(int step) {
		if ((mStockList == null) || (mStockList.size() == 0)) {
			return;
		}

		mStockListIndex += step;

		boolean loop = true;
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

		updateStockDataChartItemList();
		restartLoader();
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
	public void onChartFling(@NonNull MotionEvent me1, @NonNull MotionEvent me2, float velocityX,
	                         float velocityY) {
		if (me2.getX() > me1.getX()) {
//			navigateStock(1);
		} else {
//			navigateStock(-1);
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

	static class MainHandler extends Handler {
		private final WeakReference<StockFavoriteChartListActivity> mActivity;

		MainHandler(StockFavoriteChartListActivity activity) {
			mActivity = new WeakReference<StockFavoriteChartListActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			StockFavoriteChartListActivity activity = mActivity.get();
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
			viewHolder.chart = view.findViewById(R.id.chart);
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

			viewHolder.chart.setDescription(mStockDataChart.mDescription.toString());

			if (mItemViewType == ITEM_VIEW_TYPE_MAIN) {
				viewHolder.chart.setData(mStockDataChart.mCombinedDataMain);
			} else {
				viewHolder.chart.setData(mStockDataChart.mCombinedDataSub);
			}

//			viewHolder.chart.setOnChartGestureListener(StockDataChartListActivity.this);

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
}
