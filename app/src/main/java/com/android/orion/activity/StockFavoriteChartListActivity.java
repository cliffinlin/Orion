package com.android.orion.activity;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.android.orion.R;
import com.android.orion.chart.ChartSyncHelper;
import com.android.orion.chart.StockDataChart;
import com.android.orion.data.Period;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockDeal;
import com.android.orion.database.StockQuant;
import com.android.orion.database.StockTrend;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.DefaultYAxisValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.Utils;
import com.markupartist.android.widget.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

public class StockFavoriteChartListActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnChartGestureListener {

	public static final int ITEM_VIEW_TYPE_MAIN = 0;
	public static final int ITEM_VIEW_TYPE_SUB = 1;
	public static final int LOADER_ID_STOCK_LIST = Period.PERIODS.length + 1;
	public static final int REQUEST_CODE_SETTING = 0;

	boolean mKeyDisplayDeal = false;
	boolean mKeyDisplayQuant = false;
	int mStockListIndex = 0;
	Menu mMenu = null;
	String mSortOrder = null;
	StockData mStockData = new StockData();
	StockTrend mStockTrend = new StockTrend();
	PullToRefreshListView mListView = null;
	StockDataChartArrayAdapter mStockDataChartArrayAdapter = null;
	ArrayList<String> mStockIDList = new ArrayList<>();
	ArrayList<StockTrend> mStockTrendList = new ArrayList<>();
	ArrayList<StockDataChartItem> mStockDataChartItemList = null;
	ArrayList<StockDataChartItemMain> mStockDataChartItemMainList = null;
	ArrayList<StockDataChartItemSub> mStockDataChartItemSubList = null;
	ArrayList<StockDataChart> mStockDataChartList = null;
	ArrayList<StockDeal> mStockDealList = new ArrayList<>();
	ArrayList<StockQuant> mStockQuantList = new ArrayList<>();
	ArrayMap<Integer, CombinedChart> mCombinedChartMap = new ArrayMap<>();
	ChartSyncHelper mChartSyncHelper = new ChartSyncHelper();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// For chart init only
		Utils.init(this);
		// For chart init only

		setContentView(R.layout.activity_stock_data_chart_list);

		onNewIntent();
		initListView();
		updateTitle();
		updateMenuAction();
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

	void onNewIntent() {
		Intent intent = getIntent();
		if (intent == null) {
			return;
		}

		long stockId = intent.getLongExtra(Constant.EXTRA_STOCK_ID, DatabaseContract.INVALID_ID);
		mStock.setId(stockId);
		mDatabaseManager.getStockById(mStock);
		mStockIDList = intent.getStringArrayListExtra(Constant.EXTRA_STOCK_ID_LIST);
		if (mStockIDList != null && !mStockIDList.isEmpty()) {
			onMessageLoadStockList();
		}
		long stockTrendId = intent.getLongExtra(Constant.EXTRA_STOCK_TREND_ID, DatabaseContract.INVALID_ID);
		mStockTrend.setId(stockTrendId);
		mDatabaseManager.getStockTrendById(mStockTrend);

		mSortOrder = intent.getStringExtra(Constant.EXTRA_STOCK_LIST_SORT_ORDER);
		mKeyDisplayDeal = intent.getBooleanExtra(Constant.EXTRA_STOCK_DEAL, false);
		mKeyDisplayQuant = getIntent().getBooleanExtra(Constant.EXTRA_STOCK_QUANT, false);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		onNewIntent();
		initListView();
		restartLoader();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		getMenuInflater().inflate(R.menu.stock_data_chart, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void handleOnOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_prev: {
				navigateStock(-1);
				break;
			}
			case R.id.action_next: {
				navigateStock(1);
				break;
			}
			case R.id.action_refresh: {
				mDatabaseManager.deleteTDXData(mStock);
				mDatabaseManager.deleteStockData(mStock);
				mDatabaseManager.deleteStockTrend(mStock);
				mDatabaseManager.deleteStockQuant(mStock);
				mBackgroundHandler.downloadStockData(mStock);
				break;
			}
			case R.id.action_setting: {
				startActivityForResult(new Intent(this,
						SettingActivity.class), REQUEST_CODE_SETTING);
				break;
			}
			case R.id.action_edit: {
				mIntent = new Intent(this, StockActivity.class);
				mIntent.setAction(Constant.ACTION_STOCK_EDIT);
				mIntent.putExtra(Constant.EXTRA_STOCK_ID, mStock.getId());
				startActivity(mIntent);
				break;
			}
			case R.id.action_deal: {
				Bundle bundle = new Bundle();
				bundle.putString(Constant.EXTRA_STOCK_SE, mStock.getSE());
				bundle.putString(Constant.EXTRA_STOCK_CODE, mStock.getCode());
				Intent intent = new Intent(this, StockDealListActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			}
			case R.id.action_trend: {
				Bundle bundle = new Bundle();
				bundle.putString(Constant.EXTRA_STOCK_SE, mStock.getSE());
				bundle.putString(Constant.EXTRA_STOCK_CODE, mStock.getCode());
				Intent intent = new Intent(this, StockTrendListActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			}
			case R.id.action_backup: {
				performSaveToFile(FILE_TYPE_TDX_DATA);
				break;
			}
			default:
				super.handleOnOptionsItemSelected(item);
		}
	}

	void onMessageLoadStockList() {
		mStockList.clear();
		if (mStockIDList != null) {
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
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	                                Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CODE_SETTING) {
				restartLoader();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		onNewIntent();
		initListView();
		updateStockDataChartItemList();
		restartLoader();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mChartSyncHelper.unregisterOnChartGestureListener(this);
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

		for (int i = 0; i < Period.PERIODS.length; i++) {
			mStockDataChartList.add(new StockDataChart(mStock, Period.PERIODS[i]));
			mStockDataChartItemMainList.add(new StockDataChartItemMain(
					mStockDataChartList.get(i)));
			mStockDataChartItemSubList.add(new StockDataChartItemSub(
					mStockDataChartList.get(i)));
			mStockDataChartItemList.add(mStockDataChartItemMainList.get(i));
			mStockDataChartItemList.add(mStockDataChartItemSubList.get(i));
		}

		if (mStockDataChartItemList != null && !mStockDataChartItemList.isEmpty()) {
			mStockDataChartArrayAdapter = new StockDataChartArrayAdapter(this,
					mStockDataChartItemList);
			mListView.setAdapter(mStockDataChartArrayAdapter);
		}

		mListView.setOnRefreshListener(() -> {
			mListView.onRefreshComplete();
			mBackgroundHandler.downloadStockData(mStock);
		});
		mChartSyncHelper.registerOnChartGestureListener(this);
	}

	void initLoader() {
		if (mStockIDList == null) {
			mLoaderManager.initLoader(LOADER_ID_STOCK_LIST, null, this);
		}

		for (int i = 0; i < Period.PERIODS.length; i++) {
			if (Setting.getPeriod(Period.PERIODS[i])) {
				mLoaderManager.initLoader(i, null, this);
			}
		}
	}

	void destroyLoader() {
		if (mStockIDList == null) {
			mLoaderManager.destroyLoader(LOADER_ID_STOCK_LIST);
		}

		for (int i = 0; i < Period.PERIODS.length; i++) {
			if (Setting.getPeriod(Period.PERIODS[i])) {
				mLoaderManager.destroyLoader(i);
			}
		}
	}

	void restartLoader() {
		Log.d("restartLoader");
		if (mStockIDList == null) {
			mLoaderManager.restartLoader(LOADER_ID_STOCK_LIST, null, this);
		}

		for (int i = 0; i < Period.PERIODS.length; i++) {
			if (Setting.getPeriod(Period.PERIODS[i])) {
				mLoaderManager.restartLoader(i, null, this);
			}
		}
	}

	CursorLoader getStockCursorLoader() {
		String selection = mDatabaseManager.hasFlagSelection(Stock.FLAG_FAVORITE);
		return new CursorLoader(this, DatabaseContract.Stock.CONTENT_URI,
				DatabaseContract.Stock.PROJECTION_ALL, selection, null,
				mSortOrder);
	}

	CursorLoader getStockDataCursorLoader(String period) {
		String selection = mDatabaseManager.getStockDataSelection(mStock.getSE(), mStock.getCode(), period);
		String sortOrder = mDatabaseManager.getStockDataOrder();
		return new CursorLoader(this, DatabaseContract.StockData.CONTENT_URI,
				DatabaseContract.StockData.PROJECTION_ALL, selection, null,
				sortOrder);
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

					if (mStock.getId() == stock.getId()) {
						mStock.set(cursor);
						mStockListIndex = mStockList.size();
					}
					mStockList.add(stock);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mDatabaseManager.closeCursor(cursor);
		}

		updateTitle();
		updateMenuAction();
	}

	public void swapStockDataCursor(StockDataChart stockDataChart, Cursor cursor) {
		if (mStockData == null || stockDataChart == null) {
			return;
		}

		try {
			stockDataChart.clear();
			if ((cursor != null) && (cursor.getCount() > 0)) {
				String dateString;
				String timeString;

				while (cursor.moveToNext()) {
					int index = stockDataChart.mXValues.size();
					mStockData.set(cursor);

					dateString = mStockData.getDate();
					timeString = mStockData.getTime();

					if (Period.isMinutePeriod(mStockData.getPeriod())) {
						stockDataChart.mXValues.add(dateString + " "
								+ timeString);
					} else {
						stockDataChart.mXValues.add(dateString);
					}

					CandleEntry candleEntry = new CandleEntry(index,
							(float) mStockData.getCandle().getHigh(),
							(float) mStockData.getCandle().getLow(),
							(float) mStockData.getCandle().getOpen(),
							(float) mStockData.getCandle().getClose(),
							"");
					stockDataChart.mCandleEntryList.add(candleEntry);

					Entry netProfitInYearEntry = new Entry(
							(float) mStockData.getNetProfitInYear(), index);
					stockDataChart.mNetProfitInYearEntryList.add(netProfitInYearEntry);

					Entry rzValueEntry = new Entry(
							(float) mStockData.getRZValue(), index);
					stockDataChart.mRZValueEntryList.add(rzValueEntry);

					Entry rqValueEntry = new Entry(
							(float) mStockData.getRQValue(), index);
					stockDataChart.mRQValueEntryList.add(rqValueEntry);

					Entry average5Entry = new Entry(
							(float) mStockData.getMacd().getAverage5(), index);
					stockDataChart.mAverage5EntryList.add(average5Entry);

					Entry average10Entry = new Entry(
							(float) mStockData.getMacd().getAverage10(), index);
					stockDataChart.mAverage10EntryList.add(average10Entry);

					if (mStockData.vertexOf(StockTrend.VERTEX_TOP)) {
						Entry drawEntry = new Entry(
								(float) mStockData.getCandle().getHigh(), index);
						stockDataChart.mTrendEntryList[StockTrend.LEVEL_DRAW].add(drawEntry);
					} else if (mStockData
							.vertexOf(StockTrend.VERTEX_BOTTOM)) {
						Entry drawEntry = new Entry(
								(float) mStockData.getCandle().getLow(), index);
						stockDataChart.mTrendEntryList[StockTrend.LEVEL_DRAW].add(drawEntry);
					}

					if (mStockData.vertexOf(StockTrend.VERTEX_TOP_STROKE)) {
						Entry strokeEntry = new Entry(
								(float) mStockData.getCandle().getHigh(), index);
						stockDataChart.mTrendEntryList[StockTrend.LEVEL_STROKE].add(strokeEntry);
					} else if (mStockData
							.vertexOf(StockTrend.VERTEX_BOTTOM_STROKE)) {
						Entry strokeEntry = new Entry(
								(float) mStockData.getCandle().getLow(), index);
						stockDataChart.mTrendEntryList[StockTrend.LEVEL_STROKE].add(strokeEntry);
					}

					if (mStockData.vertexOf(StockTrend.VERTEX_TOP_SEGMENT)) {
						Entry segmentEntry = new Entry(
								(float) mStockData.getCandle().getHigh(), index);
						stockDataChart.mTrendEntryList[StockTrend.LEVEL_SEGMENT].add(segmentEntry);
					} else if (mStockData
							.vertexOf(StockTrend.VERTEX_BOTTOM_SEGMENT)) {
						Entry segmentEntry = new Entry(
								(float) mStockData.getCandle().getLow(), index);
						stockDataChart.mTrendEntryList[StockTrend.LEVEL_SEGMENT].add(segmentEntry);
					}

					if (mStockData.vertexOf(StockTrend.VERTEX_TOP_LINE)) {
						Entry lineEntry = new Entry(
								(float) mStockData.getCandle().getHigh(), index);
						stockDataChart.mTrendEntryList[StockTrend.LEVEL_LINE].add(lineEntry);
					} else if (mStockData
							.vertexOf(StockTrend.VERTEX_BOTTOM_LINE)) {
						Entry lineEntry = new Entry(
								(float) mStockData.getCandle().getLow(), index);
						stockDataChart.mTrendEntryList[StockTrend.LEVEL_LINE].add(lineEntry);
					}

					if (mStockData.vertexOf(StockTrend.VERTEX_TOP_OUTLINE)) {
						Entry entry = new Entry(
								(float) mStockData.getCandle().getHigh(), index);
						stockDataChart.mTrendEntryList[StockTrend.LEVEL_OUT_LINE].add(entry);
					} else if (mStockData
							.vertexOf(StockTrend.VERTEX_BOTTOM_OUTLINE)) {
						Entry entry = new Entry(
								(float) mStockData.getCandle().getLow(), index);
						stockDataChart.mTrendEntryList[StockTrend.LEVEL_OUT_LINE].add(entry);
					}

					if (mStockData.vertexOf(StockTrend.VERTEX_TOP_SUPERLINE)) {
						Entry entry = new Entry(
								(float) mStockData.getCandle().getHigh(), index);
						stockDataChart.mTrendEntryList[StockTrend.LEVEL_SUPER_LINE].add(entry);
					} else if (mStockData
							.vertexOf(StockTrend.VERTEX_BOTTOM_SUPERLINE)) {
						Entry entry = new Entry(
								(float) mStockData.getCandle().getLow(), index);
						stockDataChart.mTrendEntryList[StockTrend.LEVEL_SUPER_LINE].add(entry);
					}

					if (mStockData.vertexOf(StockTrend.VERTEX_TOP_TREND_LINE)) {
						Entry entry = new Entry(
								(float) mStockData.getCandle().getHigh(), index);
						stockDataChart.mTrendEntryList[StockTrend.LEVEL_TREND_LINE].add(entry);
					} else if (mStockData
							.vertexOf(StockTrend.VERTEX_BOTTOM_TREND_LINE)) {
						Entry entry = new Entry(
								(float) mStockData.getCandle().getLow(), index);
						stockDataChart.mTrendEntryList[StockTrend.LEVEL_TREND_LINE].add(entry);
					}

					Entry difEntry = new Entry((float) mStockData.getMacd().getDIF(),
							index);
					stockDataChart.mDIFEntryList.add(difEntry);

					Entry deaEntry = new Entry((float) mStockData.getMacd().getDEA(),
							index);
					stockDataChart.mDEAEntryList.add(deaEntry);

					BarEntry histogramBarEntry = new BarEntry(
							(float) mStockData.getMacd().getHistogram(), index);
					stockDataChart.mHistogramEntryList.add(histogramBarEntry);
					Entry velocityEntry = new Entry(
							(float) mStockData.getMacd().getVelocity(), index);
					stockDataChart.mVelocityEntryList.add(velocityEntry);
				}
			}

			updateTitle();
			updateMenuAction();

			if (mKeyDisplayDeal) {
				loadStockDealList();
			} else {
				mStockDealList.clear();
			}


			if (mKeyDisplayQuant) {
				loadStockQuantList();
			} else {
				mStockQuantList.clear();
			}

			stockDataChart.updateDescription(mStock);
			stockDataChart.updateLimitLines(mStock, mStockDealList, mStockQuantList);
			stockDataChart.updateGroupEntry();
			stockDataChart.setMainChartData();
			stockDataChart.setSubChartData();

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

	void loadStockQuantList() {
		ArrayMap<String, StockQuant> stockquantMap = new ArrayMap<>();

		String selection = DatabaseContract.COLUMN_SE + " = " + "'" + mStock.getSE()
				+ "'" + " AND " + DatabaseContract.COLUMN_CODE + " = " + "'"
				+ mStock.getCode() + "'";
		String sortOrder = DatabaseContract.COLUMN_CREATED + DatabaseContract.ORDER_ASC;

		mDatabaseManager.getStockQuantList(mStock, mStockQuantList, selection, sortOrder);

		for (int i = 0; i < mStockQuantList.size(); i++) {
			StockQuant stockQuant = mStockQuantList.get(i);
			if (TextUtils.isEmpty(stockQuant.getCreated())) {
				continue;
			}

			if (TextUtils.isEmpty(stockQuant.getModified())) {
				stockquantMap.put(stockQuant.getCreated(), stockQuant);
			} else {
				stockquantMap.remove(stockQuant.getCreated());
			}
		}

		if (stockquantMap.size() == 0) {
			mStockQuantList.clear();
		} else {
			mStockQuantList = new ArrayList<StockQuant>(stockquantMap.values());
		}
	}
	void updateStockDataChartItemList() {
		mDatabaseManager.getStockTrendList(mStock, mStockTrendList);
		mStockDataChartItemList.clear();
		for (int i = 0; i < Period.PERIODS.length; i++) {
			if (Setting.getPeriod(Period.PERIODS[i])) {
				mStockDataChartItemMainList.get(i).mStockDataChart.setupStockTrendMap(mStock, Period.PERIODS[i], mStockTrendList);
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

		if (mStockListIndex > mStockList.size() - 1) {
			mStockListIndex = 0;
		}

		if (mStockListIndex < 0) {
			mStockListIndex = mStockList.size() - 1;
		}

		mStock = mStockList.get(mStockListIndex);

		updateStockDataChartItemList();
		restartLoader();
	}

	@Override
	public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

	}

	@Override
	public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

	}

	@Override
	public void onChartLongPressed(MotionEvent me) {

	}

	@Override
	public void onChartDoubleTapped(MotionEvent me) {
		Setting.setDisplayCandle(!Setting.getDisplayCandle());
		restartLoader();
	}

	@Override
	public void onChartSingleTapped(MotionEvent me) {

	}

	@Override
	public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

	}

	@Override
	public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

	}

	@Override
	public void onChartTranslate(MotionEvent me, float dX, float dY) {

	}

	class StockDataChartItem {
		int mItemViewType;
		int mResource;
		StockDataChart mStockDataChart;

		public StockDataChartItem(int itemViewType, int resource,
		                          StockDataChart stockDataChart) {
			mItemViewType = itemViewType;
			mResource = resource;
			mStockDataChart = stockDataChart;
		}

		public int getItemViewType() {
			return mItemViewType;
		}

		public View getView(int position, Context context) {
			XAxis xAxis;
			YAxis leftYAxis;
			YAxis rightYAxis;

			// For android 5 and above solution:
			// if (view == null) {
			View view = LayoutInflater.from(context).inflate(mResource, null);
			ViewHolder viewHolder = new ViewHolder();
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
					for (int i = 0; i < mStockDataChart.mLimitLineList.size(); i++) {
						leftYAxis.addLimitLine(mStockDataChart.mLimitLineList
								.get(i));
					}
				}
			}

			rightYAxis = viewHolder.chart.getAxisRight();
			if (rightYAxis != null) {
				rightYAxis.setEnabled(true);
				rightYAxis.setPosition(YAxisLabelPosition.INSIDE_CHART);
				rightYAxis.setStartAtZero(false);
			}

			viewHolder.chart.setDescription(mStockDataChart.mDescription.toString());

			if (mItemViewType == ITEM_VIEW_TYPE_MAIN) {
				viewHolder.chart.setData(mStockDataChart.mCombinedDataMain);
			} else {
				viewHolder.chart.setData(mStockDataChart.mCombinedDataSub);
			}
			mCombinedChartMap.put(position, viewHolder.chart);
			mChartSyncHelper.syncCharts(mCombinedChartMap);
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
			return getItem(position).getView(position,
					getContext());
		}

		@Override
		public int getItemViewType(int position) {
			return getItem(position).getItemViewType();
		}

		@Override
		public int getViewTypeCount() {
			return mStockDataChartItemList != null ? mStockDataChartItemList.size() : 0;
		}

		@Override
		public int getCount() {
			return mStockDataChartItemList != null ? mStockDataChartItemList.size() : 0;
		}

		@Override
		public StockDataChartItem getItem(int position) {
			if (mStockDataChartItemList == null || position < 0 || position >= mStockDataChartItemList.size()) {
				return null;
			}
			return mStockDataChartItemList.get(position);
		}
	}
}
