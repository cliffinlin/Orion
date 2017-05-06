package com.android.orion;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Setting;
import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
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

public class StockChartListActivity extends OrionBaseActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnChartGestureListener {

	static final int ITEM_VIEW_TYPE_MAIN = 0;
	static final int ITEM_VIEW_TYPE_SUB = 1;
	static final int LOADER_ID_STOCK_LIST = Constants.PERIODS.length + 1;
	static final int FLING_DISTANCE = 50;
	static final int FLING_VELOCITY = 100;

	int mStockListIndex = 0;
	Menu mMenu = null;

	String mSortOrder = null;

	ListView mListView = null;
	StockChartArrayAdapter mStockChartArrayAdapter = null;
	ArrayList<StockChartItem> mStockChartItemList = null;
	ArrayList<StockChartItemMain> mStockChartItemMainList = null;
	ArrayList<StockChartItemSub> mStockChartItemSubList = null;
	ArrayList<StockChartData> mStockChartDataList = null;

	ArrayList<StockDeal> mStockDealList = null;

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mResumed) {

				int serviceType = intent.getIntExtra(
						Constants.EXTRA_SERVICE_TYPE,
						Constants.SERVICE_TYPE_NONE);

				if ((serviceType == Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE_REALTIME)
						|| (serviceType == Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE_DATA_HISTORY)
						|| (serviceType == Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE_DATA_REALTIME)) {
					if (intent.getLongExtra(Constants.EXTRA_STOCK_ID, 0) == mStock
							.getId()) {
						restartLoader();
					}
				}
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

		setContentView(R.layout.activity_stock_chart_list);

		initListView();

		mStock.setId(getIntent().getLongExtra(Constants.EXTRA_STOCK_ID, 0));

		mSortOrder = getIntent().getStringExtra(
				Setting.KEY_SORT_ORDER_STOCK_LIST);

		LocalBroadcastManager.getInstance(this).registerReceiver(
				mBroadcastReceiver,
				new IntentFilter(Constants.ACTION_SERVICE_FINISHED));

		initLoader();

		updateTitle();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		getMenuInflater().inflate(R.menu.stock_chart, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			finish();
			return true;
		}
		case R.id.action_deal: {
			Bundle bundle = new Bundle();
			bundle.putString(Constants.EXTRA_STOCK_SE, mStock.getSE());
			bundle.putString(Constants.EXTRA_STOCK_CODE, mStock.getCode());
			Intent intent = new Intent(this, StockDealListActivity.class);
			intent.putExtras(bundle);
			startActivity(intent);
			return true;
		}
		case R.id.action_prev: {
			navigateStock(-1);
			return true;
		}
		case R.id.action_next: {
			navigateStock(1);
			return true;
		}
		case R.id.action_clean_data: {
			updateStockAction(mStock.getId(), "");
			deleteStockData(mStock.getId());
			startService(Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE,
					Constants.EXECUTE_IMMEDIATE, mStock.getSE(),
					mStock.getCode());
			restartLoader();
			return true;
		}
		case R.id.action_remove_favorite: {
			updateStockMark(mStock.getId(), Constants.STOCK_FLAG_NONE);
			if (mStockListIndex < mStockList.size()) {
				mStockList.remove(mStockListIndex);
			}
			startService(Constants.SERVICE_REMOVE_STOCK_FAVORITE,
					Constants.EXECUTE_IMMEDIATE);
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mStockChartItemList.clear();

		for (int i = 0; i < Constants.PERIODS.length; i++) {
			if (Utility.getSettingBoolean(this, Constants.PERIODS[i])) {
				mStockChartItemList.add(mStockChartItemMainList.get(i));
				mStockChartItemList.add(mStockChartItemSubList.get(i));
			}
		}

		restartLoader();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mBroadcastReceiver);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader loader = null;

		if (id == LOADER_ID_STOCK_LIST) {
			loader = getStockCursorLoader();
		} else {
			loader = getStockDataCursorLoader(Constants.PERIODS[id]);
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
			swapStockDataCursor(mStockChartDataList.get(id), cursor);
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
			swapStockDataCursor(mStockChartDataList.get(id), null);
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
		// int velocitx = FLING_VELOCITY;
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

	void initListView() {
		mListView = (ListView) findViewById(R.id.listView);

		if (mStockDealList == null) {
			mStockDealList = new ArrayList<StockDeal>();
		}

		if (mStockChartDataList == null) {
			mStockChartDataList = new ArrayList<StockChartData>();
		}

		if (mStockChartItemList == null) {
			mStockChartItemList = new ArrayList<StockChartItem>();
		}

		if (mStockChartItemMainList == null) {
			mStockChartItemMainList = new ArrayList<StockChartItemMain>();
		}

		if (mStockChartItemSubList == null) {
			mStockChartItemSubList = new ArrayList<StockChartItemSub>();
		}

		for (int i = 0; i < Constants.PERIODS.length; i++) {
			mStockChartDataList.add(new StockChartData(Constants.PERIODS[i]));
			mStockChartItemMainList.add(new StockChartItemMain(
					mStockChartDataList.get(i)));
			mStockChartItemSubList.add(new StockChartItemSub(
					mStockChartDataList.get(i)));
			mStockChartItemList.add(mStockChartItemMainList.get(i));
			mStockChartItemList.add(mStockChartItemSubList.get(i));
		}

		mStockChartArrayAdapter = new StockChartArrayAdapter(this,
				mStockChartItemList);
		mListView.setAdapter(mStockChartArrayAdapter);
	}

	void initLoader() {
		mLoaderManager.initLoader(LOADER_ID_STOCK_LIST, null, this);
		for (int i = 0; i < Constants.PERIODS.length; i++) {
			mLoaderManager.initLoader(i, null, this);
		}
	}

	void restartLoader() {
		mLoaderManager.restartLoader(LOADER_ID_STOCK_LIST, null, this);
		for (int i = 0; i < Constants.PERIODS.length; i++) {
			mLoaderManager.restartLoader(i, null, this);
		}
	}

	CursorLoader getStockCursorLoader() {
		String selection = "";
		CursorLoader loader = null;

		selection = DatabaseContract.Stock.COLUMN_MARK + " = '"
				+ Constants.STOCK_FLAG_MARK_FAVORITE + "'";
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
				period);

		sortOrder = mStockDatabaseManager.getStockDataOrder();

		loader = new CursorLoader(this, DatabaseContract.StockData.CONTENT_URI,
				DatabaseContract.StockData.PROJECTION_ALL, selection, null,
				sortOrder);

		return loader;
	}

	void loadStockDealList() {
		Cursor cursor = null;
		String selection = "";

		if ((mStock == null) || (mStockDealList == null)) {
			return;
		}

		mStockDealList.clear();

		selection = DatabaseContract.COLUMN_SE + " = " + "\'" + mStock.getSE()
				+ "\'" + " AND " + DatabaseContract.COLUMN_CODE + " = " + "\'"
				+ mStock.getCode() + "\'";

		try {
			cursor = mStockDatabaseManager
					.queryStockDeal(selection, null, null);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockDeal stockDeal = new StockDeal();
					stockDeal.set(cursor);
					mStockDealList.add(stockDeal);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}
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
					Stock stock = Stock.obtain();
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

	public void swapStockDataCursor(StockChartData stockChartData, Cursor cursor) {
		int index = 0;

		if (mStockData == null) {
			return;
		}

		stockChartData.clear();

		try {
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					index = stockChartData.mXValues.size();
					mStockData.set(cursor);

					stockChartData.mXValues.add(mStockData.getDate());

					CandleEntry candleEntry = new CandleEntry(index,
							(float) mStockData.getHigh(),
							(float) mStockData.getLow(),
							(float) mStockData.getOpen(),
							(float) mStockData.getClose(),
							mStockData.getAction());
					stockChartData.mCandleEntryList.add(candleEntry);

					if (mStockData.vertexOf(Constants.STOCK_VERTEX_TOP)) {
						Entry drawEntry = new Entry(
								(float) mStockData.getVertexHigh(), index);
						stockChartData.mDrawEntryList.add(drawEntry);
					} else if (mStockData
							.vertexOf(Constants.STOCK_VERTEX_BOTTOM)) {
						Entry drawEntry = new Entry(
								(float) mStockData.getVertexLow(), index);
						stockChartData.mDrawEntryList.add(drawEntry);
					}

					if (mStockData.vertexOf(Constants.STOCK_VERTEX_TOP_STROKE)) {
						Entry strokeEntry = new Entry(
								(float) mStockData.getVertexHigh(), index);
						stockChartData.mStrokeEntryList.add(strokeEntry);
					} else if (mStockData
							.vertexOf(Constants.STOCK_VERTEX_BOTTOM_STROKE)) {
						Entry strokeEntry = new Entry(
								(float) mStockData.getVertexLow(), index);
						stockChartData.mStrokeEntryList.add(strokeEntry);
					}

					if (mStockData.vertexOf(Constants.STOCK_VERTEX_TOP_SEGMENT)) {
						Entry segmentEntry = new Entry(
								(float) mStockData.getVertexHigh(), index);
						stockChartData.mSegmentEntryList.add(segmentEntry);
					} else if (mStockData
							.vertexOf(Constants.STOCK_VERTEX_BOTTOM_SEGMENT)) {
						Entry segmentEntry = new Entry(
								(float) mStockData.getVertexLow(), index);
						stockChartData.mSegmentEntryList.add(segmentEntry);
					}

					if ((mStockData.getOverlapHigh() > 0)
							&& (mStockData.getOverlapLow() > 0)) {
						Entry overlayHighEntry = new Entry(
								(float) mStockData.getOverlapHigh(), index);
						stockChartData.mOverlapHighEntryList
								.add(overlayHighEntry);

						Entry overlapLowEntry = new Entry(
								(float) mStockData.getOverlapLow(), index);
						stockChartData.mOverlapLowEntryList
								.add(overlapLowEntry);
					}

					Entry average5Entry = new Entry(
							(float) mStockData.getAverage5(), index);
					stockChartData.mAverage5EntryList.add(average5Entry);

					Entry average10Entry = new Entry(
							(float) mStockData.getAverage10(), index);
					stockChartData.mAverage10EntryList.add(average10Entry);

					Entry difEntry = new Entry((float) mStockData.getDIF(),
							index);
					stockChartData.mDIFEntryList.add(difEntry);

					Entry deaEntry = new Entry((float) mStockData.getDEA(),
							index);
					stockChartData.mDEAEntryList.add(deaEntry);

					BarEntry histogramBarEntry = new BarEntry(
							(float) mStockData.getHistogram(), index);
					stockChartData.mHistogramEntryList.add(histogramBarEntry);

					Entry averageEntry = new Entry(
							(float) mStockData.getAverage(), index);
					stockChartData.mAverageEntryList.add(averageEntry);

					Entry velocityEntry = new Entry(
							(float) mStockData.getVelocity(), index);
					stockChartData.mVelocityEntryList.add(velocityEntry);

					Entry acclerateEntry = new Entry(
							(float) mStockData.getAcceleration(), index);
					stockChartData.mAccelerateEntryList.add(acclerateEntry);

					Entry acclerateVelocityEntry = new Entry(
							(float) mStockData.getAccelerationVelocity(), index);
					stockChartData.mAccelerateVelocityEntryList
							.add(acclerateVelocityEntry);
				}

				updateTitle();

				mStockDatabaseManager.getStockDealList(mStock, mStockDealList);

				stockChartData.updateDescription(mStock);
				stockChartData.updateLimitLine(mStockDealList);
				stockChartData.setMainChartData();
				stockChartData.setSubChartData();

				mStockChartArrayAdapter = new StockChartArrayAdapter(this,
						mStockChartItemList);
				mListView.setAdapter(mStockChartArrayAdapter);
				// mStockChartArrayAdapter.notifyDataSetChanged();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}
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
		private final WeakReference<StockChartListActivity> mActivity;

		MainHandler(StockChartListActivity activity) {
			mActivity = new WeakReference<StockChartListActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			StockChartListActivity activity = mActivity.get();
			activity.updateTitle();
			activity.updateMenuAction();
		}
	}

	class StockChartItem {
		int mItemViewType;
		int mResource;
		StockChartData mStockChartData;

		public StockChartItem() {
		}

		public StockChartItem(int itemViewType, int resource,
				StockChartData stockChartData) {
			mItemViewType = itemViewType;
			mResource = resource;
			mStockChartData = stockChartData;
		}

		public int getItemViewType() {
			return mItemViewType;
		}

		public View getView(int position, View view, Context context) {
			ViewHolder viewHolder = null;
			XAxis xAxis = null;
			YAxis leftAxis = null;
			YAxis rightAxis = null;

			if (view == null) {
				view = LayoutInflater.from(context).inflate(mResource, null);
				viewHolder = new ViewHolder();
				viewHolder.chart = (CombinedChart) view
						.findViewById(R.id.chart);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			viewHolder.chart.setBackgroundColor(Color.LTGRAY);
			viewHolder.chart.setGridBackgroundColor(Color.LTGRAY);

			viewHolder.chart.setMaxVisibleValueCount(0);

			xAxis = viewHolder.chart.getXAxis();
			if (xAxis != null) {
				xAxis.setPosition(XAxisPosition.BOTTOM);
			}

			leftAxis = viewHolder.chart.getAxisLeft();
			if (leftAxis != null) {
				leftAxis.setPosition(YAxisLabelPosition.INSIDE_CHART);
				leftAxis.setStartAtZero(false);
				leftAxis.setValueFormatter(new DefaultYAxisValueFormatter(2));
				leftAxis.removeAllLimitLines();
				if (mItemViewType == ITEM_VIEW_TYPE_MAIN) {
					for (int i = 0; i < mStockChartData.mLimitLineList.size(); i++) {
						leftAxis.addLimitLine(mStockChartData.mLimitLineList
								.get(i));
					}
				}
			}

			rightAxis = viewHolder.chart.getAxisRight();
			if (rightAxis != null) {
				rightAxis.setEnabled(false);
			}

			viewHolder.chart.setDescription(mStockChartData.mDescription);

			if (mItemViewType == ITEM_VIEW_TYPE_MAIN) {
				viewHolder.chart.setData(mStockChartData.mCombinedDataMain);
			} else {
				viewHolder.chart.setData(mStockChartData.mCombinedDataSub);
			}

			return view;
		}

		class ViewHolder {
			CombinedChart chart;
		}
	}

	class StockChartItemMain extends StockChartItem {
		public StockChartItemMain(StockChartData stockChartData) {
			super(ITEM_VIEW_TYPE_MAIN,
					R.layout.activity_stock_chart_list_item_main,
					stockChartData);
		}
	}

	class StockChartItemSub extends StockChartItem {
		public StockChartItemSub(StockChartData stockChartData) {
			super(ITEM_VIEW_TYPE_SUB,
					R.layout.activity_stock_chart_list_item_sub, stockChartData);
		}
	}

	class StockChartArrayAdapter extends ArrayAdapter<StockChartItem> {

		public StockChartArrayAdapter(Context context,
				List<StockChartItem> objects) {
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
			return mStockChartItemList.size();
		}
	}
}
