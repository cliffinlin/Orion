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
import android.os.Looper;
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
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.DefaultYAxisValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.Utils;

public class StockStatisticsChartListActivity extends OrionBaseActivity
		implements LoaderManager.LoaderCallbacks<Cursor>,
		OnChartGestureListener {
	static final String TAG = Constants.TAG + " "
			+ StockStatisticsChartListActivity.class.getSimpleName();

	static final int ITEM_VIEW_TYPE_MAIN = 0;
	static final int LOADER_ID_STOCK_LIST = 0;
	static final int FLING_DISTANCE = 50;
	static final int FLING_VELOCITY = 100;

	static final int MESSAGE_REFRESH = 0;

	public static final int REQUEST_CODE_STOCK_FILTER = 1;

	int mStockListIndex = 0;
	Menu mMenu = null;

	String mSortOrder = null;

	ListView mListView = null;
	StatisticsChartArrayAdapter mStatisticsChartArrayAdapter = null;
	ArrayList<StatisticsChartItem> mStatisticsChartItemList = null;
	ArrayList<StatisticsChartItemMain> mStatisticsChartItemMainList = null;
	ArrayList<StockStatisticsChart> mStatisticsChartList = null;

	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MESSAGE_REFRESH:
				startService(Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE,
						Constants.EXECUTE_IMMEDIATE, mStock.getSE(),
						mStock.getCode());
				restartLoader();
				break;

			default:
				break;
			}
		}
	};

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mResumed) {

				int serviceType = intent.getIntExtra(
						Constants.EXTRA_SERVICE_TYPE,
						Constants.SERVICE_TYPE_NONE);

				if (serviceType == Constants.SERVICE_DATABASE_UPDATE) {
					if (intent.getLongExtra(Constants.EXTRA_STOCK_ID, 0) == mStock
							.getId()) {
						if (System.currentTimeMillis() - mLastRestartLoader > Constants.DEFAULT_RESTART_LOADER_INTERAL) {
							mLastRestartLoader = System.currentTimeMillis();
							restartLoader();
						}
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

		setContentView(R.layout.activity_statistics_chart_list);

		mStockFilter.read();

		initListView();

		mStock.setId(getIntent().getLongExtra(Constants.EXTRA_STOCK_ID, 0));

		mSortOrder = getIntent().getStringExtra(
				Setting.KEY_SORT_ORDER_STOCK_LIST);

		LocalBroadcastManager.getInstance(this).registerReceiver(
				mBroadcastReceiver,
				new IntentFilter(Constants.ACTION_SERVICE_FINISHED));

		initLoader();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		getMenuInflater().inflate(R.menu.stock_statistics_chart, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		case R.id.action_refresh:
			mHandler.sendEmptyMessage(MESSAGE_REFRESH);
			return true;

		case R.id.action_settings:
			startActivityForResult(new Intent(this, StockFilterActivity.class),
					REQUEST_CODE_STOCK_FILTER);
			return true;

		case R.id.action_order_by_roi:
			mSortOrder = DatabaseContract.COLUMN_ROI
					+ DatabaseContract.ORDER_DIRECTION_DESC;
			restartLoader();
			return true;

		case R.id.action_order_by_roe:
			mSortOrder = DatabaseContract.COLUMN_ROE
					+ DatabaseContract.ORDER_DIRECTION_DESC;
			restartLoader();
			return true;

		case R.id.action_order_by_rate:
			mSortOrder = DatabaseContract.COLUMN_RATE
					+ DatabaseContract.ORDER_DIRECTION_DESC;
			restartLoader();
			return true;

		case R.id.action_order_by_pe:
			mSortOrder = DatabaseContract.COLUMN_PE
					+ DatabaseContract.ORDER_DIRECTION_DESC;
			restartLoader();
			return true;

		case R.id.action_order_by_yield:
			mSortOrder = DatabaseContract.COLUMN_YIELD
					+ DatabaseContract.ORDER_DIRECTION_DESC;
			restartLoader();
			return true;

		case R.id.action_order_by_delta:
			mSortOrder = DatabaseContract.COLUMN_DELTA
					+ DatabaseContract.ORDER_DIRECTION_DESC;
			restartLoader();
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
			case REQUEST_CODE_STOCK_FILTER:
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					mStockFilter.get(bundle);
				}
				break;

			default:
				break;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mStatisticsChartItemList.clear();

		mStatisticsChartItemList.add(mStatisticsChartItemMainList.get(0));

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
			swapStockCursor(mStatisticsChartList.get(0), cursor);
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
			swapStockCursor(mStatisticsChartList.get(0), null);
		}
	}

	void initListView() {
		mListView = (ListView) findViewById(R.id.listView);

		if (mStatisticsChartList == null) {
			mStatisticsChartList = new ArrayList<StockStatisticsChart>();
		}

		if (mStatisticsChartItemList == null) {
			mStatisticsChartItemList = new ArrayList<StatisticsChartItem>();
		}

		if (mStatisticsChartItemMainList == null) {
			mStatisticsChartItemMainList = new ArrayList<StatisticsChartItemMain>();
		}

		mStatisticsChartList.add(new StockStatisticsChart());
		mStatisticsChartItemMainList.add(new StatisticsChartItemMain(
				mStatisticsChartList.get(0)));
		mStatisticsChartItemList.add(mStatisticsChartItemMainList.get(0));

		mStatisticsChartArrayAdapter = new StatisticsChartArrayAdapter(this,
				mStatisticsChartItemList);
		mListView.setAdapter(mStatisticsChartArrayAdapter);
	}

	void initLoader() {
		mLoaderManager.initLoader(LOADER_ID_STOCK_LIST, null, this);
	}

	void restartLoader() {
		mLoaderManager.restartLoader(LOADER_ID_STOCK_LIST, null, this);
	}

	CursorLoader getStockCursorLoader() {
		String selection = "";
		CursorLoader loader = null;

		selection = DatabaseContract.Stock.COLUMN_MARK + " = '"
				+ Constants.STOCK_FLAG_MARK_FAVORITE + "'";

		selection += mStockFilter.getSelection();

		loader = new CursorLoader(this, DatabaseContract.Stock.CONTENT_URI,
				DatabaseContract.Stock.PROJECTION_ALL, selection, null,
				mSortOrder);

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

	public void swapStockCursor(StockStatisticsChart stockDataChart,
			Cursor cursor) {
		int index = 0;

		if (mStockList == null) {
			return;
		}

		stockDataChart.clear();

		mStockList.clear();

		try {
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					Stock stock = new Stock();
					stock.set(cursor);

					index = stockDataChart.mXValues.size();
					stockDataChart.mXValues.add(stock.getName());

					Entry roiEntry = new Entry((float) stock.getRoi(), index);
					stockDataChart.mRoiEntryList.add(roiEntry);

					Entry roeEntry = new Entry((float) stock.getRoe(), index);
					stockDataChart.mRoeEntryList.add(roeEntry);

					Entry rateEntry = new Entry((float) stock.getRate(), index);
					stockDataChart.mRateEntryList.add(rateEntry);

					BarEntry peEntry = new BarEntry((float) stock.getPE(),
							index);
					stockDataChart.mPeEntryList.add(peEntry);

					Entry yieldEntry = new Entry((float) stock.getYield(),
							index);
					stockDataChart.mYieldEntryList.add(yieldEntry);

					Entry deltaEntry = new Entry((float) stock.getDelta(),
							index);
					stockDataChart.mDeltaEntryList.add(deltaEntry);

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

		stockDataChart.setMainChartData();

		mStatisticsChartArrayAdapter.notifyDataSetChanged();

		if (mMainHandler != null) {
			mMainHandler.sendEmptyMessage(0);
		}
	}

	static class MainHandler extends Handler {
		private final WeakReference<StockStatisticsChartListActivity> mActivity;

		MainHandler(StockStatisticsChartListActivity activity) {
			mActivity = new WeakReference<StockStatisticsChartListActivity>(
					activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			StockStatisticsChartListActivity activity = mActivity.get();
			activity.updateMenuAction();
		}
	}

	class StatisticsChartItem {
		int mItemViewType;
		int mResource;
		StockStatisticsChart mStatisticsChart;

		public StatisticsChartItem() {
		}

		public StatisticsChartItem(int itemViewType, int resource,
				StockStatisticsChart stockDataChart) {
			mItemViewType = itemViewType;
			mResource = resource;
			mStatisticsChart = stockDataChart;
		}

		public int getItemViewType() {
			return mItemViewType;
		}

		public View getView(int position, View view, Context context) {
			ViewHolder viewHolder = null;
			XAxis xAxis = null;
			YAxis leftAxis = null;
			YAxis rightAxis = null;

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

			leftAxis = viewHolder.chart.getAxisLeft();
			if (leftAxis != null) {
				leftAxis.setPosition(YAxisLabelPosition.INSIDE_CHART);
				leftAxis.setStartAtZero(false);
				leftAxis.setValueFormatter(new DefaultYAxisValueFormatter(2));
				leftAxis.removeAllLimitLines();
			}

			rightAxis = viewHolder.chart.getAxisRight();
			if (rightAxis != null) {
				rightAxis.setEnabled(false);
			}

			if (mItemViewType == ITEM_VIEW_TYPE_MAIN) {
				viewHolder.chart.setData(mStatisticsChart.mCombinedDataMain);
			}

			return view;
		}

		class ViewHolder {
			CombinedChart chart;
		}
	}

	class StatisticsChartItemMain extends StatisticsChartItem {
		public StatisticsChartItemMain(StockStatisticsChart stockDataChart) {
			super(ITEM_VIEW_TYPE_MAIN,
					R.layout.activity_stock_data_chart_list_item_main,
					stockDataChart);
		}
	}

	class StatisticsChartArrayAdapter extends ArrayAdapter<StatisticsChartItem> {

		public StatisticsChartArrayAdapter(Context context,
				List<StatisticsChartItem> objects) {
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
			return mStatisticsChartItemList.size();
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
		}

		if (me2.getX() - me1.getX() > distance
				&& Math.abs(velocityX) > velocity) {
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
