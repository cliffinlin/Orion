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
import com.android.orion.database.Stock;
import com.android.orion.utility.Utility;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.DefaultYAxisValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.Utils;

public class StockFinancialChartListActivity extends BaseActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnChartGestureListener {
	static final String TAG = Constants.TAG + " "
			+ StockFinancialChartListActivity.class.getSimpleName();

	static final int ITEM_VIEW_TYPE_MAIN = 0;
	static final int ITEM_VIEW_TYPE_SUB = 1;
	static final int LOADER_ID_STOCK_LIST = 0;
	static final int LOADER_ID_FINANCIAL_DATA_LIST = 1;
	static final int FLING_DISTANCE = 50;
	static final int FLING_VELOCITY = 100;

	static final int MESSAGE_REFRESH = 0;

	int mStockListIndex = 0;
	Menu mMenu = null;

	String mSortOrder = null;

	ListView mListView = null;
	FinancialDataChartArrayAdapter mFinancialDataChartArrayAdapter = null;
	ArrayList<FinancialDataChartItem> mFinancialDataChartItemList = null;
	ArrayList<FinancialDataChartItemMain> mFinancialDataChartItemMainList = null;
	ArrayList<FinancialDataChartItemSub> mFinancialDataChartItemSubList = null;
	ArrayList<StockFinancialChart> mFinancialDataChartList = null;

	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MESSAGE_REFRESH:
				mOrionService.download(mStock);
				restartLoader();
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

		setContentView(R.layout.activity_financial_data_chart_list);

		initListView();

		mStock.setId(getIntent().getLongExtra(Constants.EXTRA_STOCK_ID,
				Constants.STOCK_ID_INVALID));

		mSortOrder = getIntent().getStringExtra(
				Constants.EXTRA_STOCK_LIST_SORT_ORDER);

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
		case android.R.id.home: {
			finish();
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
		case R.id.action_edit: {
			mIntent = new Intent(this, StockActivity.class);
			mIntent.setAction(StockActivity.ACTION_STOCK_EDIT);
			mIntent.putExtra(Constants.EXTRA_STOCK_ID, mStock.getId());
			startActivity(mIntent);
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
		case R.id.action_refresh: {
			mHandler.sendEmptyMessage(MESSAGE_REFRESH);
			return true;
		}

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mFinancialDataChartItemList.clear();

		int i = 0;
		mFinancialDataChartItemList.add(mFinancialDataChartItemMainList.get(i));
		mFinancialDataChartItemList.add(mFinancialDataChartItemSubList.get(i));

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
		} else if (id == LOADER_ID_FINANCIAL_DATA_LIST) {
			loader = getFinancialDataCursorLoader();
		} else {
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
		} else if (id == LOADER_ID_FINANCIAL_DATA_LIST) {
			swapFinancialDataCursor(mFinancialDataChartList.get(0), cursor);
		} else {
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
		} else if (id == LOADER_ID_FINANCIAL_DATA_LIST) {
			swapFinancialDataCursor(mFinancialDataChartList.get(0), null);
		} else {
		}
	}

	void initListView() {
		mListView = (ListView) findViewById(R.id.listView);

		if (mFinancialDataChartList == null) {
			mFinancialDataChartList = new ArrayList<StockFinancialChart>();
		}

		if (mFinancialDataChartItemList == null) {
			mFinancialDataChartItemList = new ArrayList<FinancialDataChartItem>();
		}

		if (mFinancialDataChartItemMainList == null) {
			mFinancialDataChartItemMainList = new ArrayList<FinancialDataChartItemMain>();
		}

		if (mFinancialDataChartItemSubList == null) {
			mFinancialDataChartItemSubList = new ArrayList<FinancialDataChartItemSub>();
		}

		int i = 0;
		mFinancialDataChartList.add(new StockFinancialChart(
				Constants.PERIODS[i]));
		mFinancialDataChartItemMainList.add(new FinancialDataChartItemMain(
				mFinancialDataChartList.get(i)));
		mFinancialDataChartItemSubList.add(new FinancialDataChartItemSub(
				mFinancialDataChartList.get(i)));
		mFinancialDataChartItemList.add(mFinancialDataChartItemMainList.get(i));
		mFinancialDataChartItemList.add(mFinancialDataChartItemSubList.get(i));

		mFinancialDataChartArrayAdapter = new FinancialDataChartArrayAdapter(
				this, mFinancialDataChartItemList);
		mListView.setAdapter(mFinancialDataChartArrayAdapter);
	}

	void initLoader() {
		mLoaderManager.initLoader(LOADER_ID_STOCK_LIST, null, this);
		mLoaderManager.initLoader(LOADER_ID_FINANCIAL_DATA_LIST, null, this);
	}

	void restartLoader(Intent intent) {
		if (intent.getLongExtra(Constants.EXTRA_STOCK_ID,
				Constants.STOCK_ID_INVALID) == mStock.getId()) {
			restartLoader();
		}
	}

	void restartLoader() {
		mLoaderManager.restartLoader(LOADER_ID_STOCK_LIST, null, this);
		mLoaderManager.restartLoader(LOADER_ID_FINANCIAL_DATA_LIST, null, this);
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

	CursorLoader getFinancialDataCursorLoader() {
		String selection = "";
		String sortOrder = "";
		CursorLoader loader = null;

		selection = mStockDatabaseManager.getFinancialDataSelection(mStock
				.getId());

		sortOrder = mStockDatabaseManager.getFinancialDataOrder();

		loader = new CursorLoader(this,
				DatabaseContract.FinancialData.CONTENT_URI,
				DatabaseContract.FinancialData.PROJECTION_ALL, selection, null,
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
			mStockDatabaseManager.closeCursor(cursor);
		}

		if (mMainHandler != null) {
			mMainHandler.sendEmptyMessage(0);
		}
	}

	public void swapFinancialDataCursor(StockFinancialChart financialDataChart,
			Cursor cursor) {
		int index = 0;
		double unit = 100000000.0;

		if (mFinancialData == null) {
			return;
		}

		// mStockDatabaseManager.getFinancialDataList(mStock,
		// mFinancialDataList);

		financialDataChart.clear();

		try {
			if ((cursor != null) && (cursor.getCount() > 0)) {
				String dateString = "";

				while (cursor.moveToNext()) {
					index = financialDataChart.mXValues.size();
					mFinancialData.set(cursor);

					dateString = mFinancialData.getDate();
					financialDataChart.mXValues.add(dateString);

					Entry totalCurrentAssetsEntry = new Entry(
							(float) mFinancialData.getTotalCurrentAssets()
									/ (float) unit, index);
					financialDataChart.mTotalCurrentAssetsEntryList
							.add(totalCurrentAssetsEntry);

					Entry totalAssetsEntry = new Entry(
							(float) mFinancialData.getTotalAssets()
									/ (float) unit, index);
					financialDataChart.mTotalAssetsEntryList
							.add(totalAssetsEntry);

					Entry totalLongTermLiabilitiesEntry = new Entry(
							(float) mFinancialData.getTotalLongTermLiabilities()
									/ (float) unit, index);
					financialDataChart.mTotalLongTermLiabilitiesEntryList
							.add(totalLongTermLiabilitiesEntry);

					Entry mainBusinessIncomeEntry = new Entry(
							(float) mFinancialData.getMainBusinessIncome()
									/ (float) unit, index);
					financialDataChart.mMainBusinessIncomeEntryList
							.add(mainBusinessIncomeEntry);

					Entry financialExpensesEntry = new Entry(
							(float) mFinancialData.getFinancialExpenses()
									/ (float) unit, index);
					financialDataChart.mFinancialExpensesEntryList
							.add(financialExpensesEntry);

					Entry netProfittEntry = new Entry(
							(float) mFinancialData.getNetProfit()
									/ (float) unit, index);
					financialDataChart.mNetProfitEntryList.add(netProfittEntry);

					Entry totalShareEntry = new Entry(
							(float) mFinancialData.getTotalShare()
									/ (float) unit, index);
					financialDataChart.mTotalShareEntryList
							.add(totalShareEntry);

					Entry bookValuePerShareEntry = new Entry(
							(float) mFinancialData.getBookValuePerShare(),
							index);
					financialDataChart.mBookValuePerShareEntryList
							.add(bookValuePerShareEntry);

					Entry cashFlowPerShareEntry = new Entry(
							(float) mFinancialData.getCashFlowPerShare(), index);
					financialDataChart.mCashFlowPerShareEntryList
							.add(cashFlowPerShareEntry);

					Entry netProfitPerShareEntry = new Entry(
							(float) mFinancialData.getNetProfitPerShare(),
							index);
					financialDataChart.mNetProfitPerShareEntryList
							.add(netProfitPerShareEntry);

					Entry roeEntry = new Entry((float) mFinancialData.getRoe(),
							index);
					financialDataChart.mRoeEntryList.add(roeEntry);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}

		updateTitle();

		financialDataChart.setMainChartData();
		financialDataChart.setSubChartData();

		mFinancialDataChartArrayAdapter.notifyDataSetChanged();
	}

	Comparator<FinancialData> comparator = new Comparator<FinancialData>() {

		@Override
		public int compare(FinancialData arg0, FinancialData arg1) {
			Calendar calendar0;
			Calendar calendar1;

			calendar0 = Utility.stringToCalendar(arg0.getDate(),
					Utility.CALENDAR_DATE_TIME_FORMAT);
			calendar1 = Utility.stringToCalendar(arg1.getDate(),
					Utility.CALENDAR_DATE_TIME_FORMAT);
			if (calendar1.before(calendar0)) {
				return -1;
			} else if (calendar1.after(calendar0)) {
				return 1;
			} else {
				return 0;
			}
		}
	};

	int binarySearch(int arr[], int l, int r, int x) {
		if (r >= l) {
			int mid = l + (r - l) / 2;

			// If the element is present at the
			// middle itself
			if (arr[mid] == x)
				return mid;

			// If element is smaller than mid, then
			// it can only be present in left subarray
			if (arr[mid] > x)
				return binarySearch(arr, l, mid - 1, x);

			// Else the element can only be present
			// in right subarray
			return binarySearch(arr, mid + 1, r, x);
		}

		// We reach here when element is not present
		// in array
		return -1;
	}

	int binarySearch(int l, int r, Calendar calendar) {
		if (r >= l) {
			int mid = l + (r - l) / 2;

			Calendar calendarMid = Utility.stringToCalendar(mFinancialDataList
					.get(mid).getDate(), Utility.CALENDAR_DATE_FORMAT);

			// If the element is present at the
			// middle itself
			if (calendarMid.equals(calendar))
				return mid;

			// If element is smaller than mid, then
			// it can only be present in left subarray
			if (calendar.before(calendarMid))
				return binarySearch(l, mid - 1, calendar);

			Calendar calendarMid1 = Utility.stringToCalendar(mFinancialDataList
					.get(mid + 1).getDate(), Utility.CALENDAR_DATE_FORMAT);
			if (calendar.after(calendarMid) && (calendar.before(calendarMid1)))
				return mid;

			// Else the element can only be present
			// in right subarray
			return binarySearch(mid + 1, r, calendar);
		}

		// We reach here when element is not present
		// in array
		return -1;
	}

	FinancialData getFinancialDataByDate(String dateString) {
		int index = 0;
		FinancialData financialData = null;

		if (mFinancialDataList.size() < 1) {
			return financialData;
		}

		if (TextUtils.isEmpty(dateString)) {
			return financialData;
		}

		Calendar calendar = Utility.stringToCalendar(dateString,
				Utility.CALENDAR_DATE_FORMAT);
		Calendar calendarMin = Utility.stringToCalendar(
				mFinancialDataList.get(0).getDate(),
				Utility.CALENDAR_DATE_FORMAT);
		Calendar calendarMax = Utility
				.stringToCalendar(
						mFinancialDataList.get(mFinancialDataList.size() - 1)
								.getDate(), Utility.CALENDAR_DATE_FORMAT);

		if (calendar.before(calendarMin)) {
			return financialData;
		} else if (calendar.after(calendarMax)) {
			return mFinancialDataList.get(mFinancialDataList.size() - 1);
		} else {
			index = binarySearch(0, mFinancialDataList.size() - 1, calendar);

			if ((index > 0) && (index < mFinancialDataList.size())) {
				financialData = mFinancialDataList.get(index);
			}
		}

		return financialData;
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
		private final WeakReference<StockFinancialChartListActivity> mActivity;

		MainHandler(StockFinancialChartListActivity activity) {
			mActivity = new WeakReference<StockFinancialChartListActivity>(
					activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			StockFinancialChartListActivity activity = mActivity.get();
			activity.updateTitle();
			activity.updateMenuAction();
		}
	}

	class FinancialDataChartItem {
		int mItemViewType;
		int mResource;
		StockFinancialChart mFinancialDataChart;

		public FinancialDataChartItem() {
		}

		public FinancialDataChartItem(int itemViewType, int resource,
				StockFinancialChart financialDataChart) {
			mItemViewType = itemViewType;
			mResource = resource;
			mFinancialDataChart = financialDataChart;
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
				if (mItemViewType == ITEM_VIEW_TYPE_MAIN) {
					for (int i = 0; i < mFinancialDataChart.mLimitLineList
							.size(); i++) {
						leftAxis.addLimitLine(mFinancialDataChart.mLimitLineList
								.get(i));
					}
				}
			}

			rightAxis = viewHolder.chart.getAxisRight();
			if (rightAxis != null) {
				rightAxis.setEnabled(false);
			}

			viewHolder.chart.setDescription(mFinancialDataChart.mDescription);

			if (mItemViewType == ITEM_VIEW_TYPE_MAIN) {
				viewHolder.chart.setData(mFinancialDataChart.mCombinedDataMain);
			} else {
				viewHolder.chart.setData(mFinancialDataChart.mCombinedDataSub);
			}

			return view;
		}

		class ViewHolder {
			CombinedChart chart;
		}
	}

	class FinancialDataChartItemMain extends FinancialDataChartItem {
		public FinancialDataChartItemMain(StockFinancialChart financialDataChart) {
			super(ITEM_VIEW_TYPE_MAIN,
					R.layout.activity_financial_data_chart_list_item_main,
					financialDataChart);
		}
	}

	class FinancialDataChartItemSub extends FinancialDataChartItem {
		public FinancialDataChartItemSub(StockFinancialChart financialDataChart) {
			super(ITEM_VIEW_TYPE_SUB,
					R.layout.activity_financial_data_chart_list_item_sub,
					financialDataChart);
		}
	}

	class FinancialDataChartArrayAdapter extends
			ArrayAdapter<FinancialDataChartItem> {

		public FinancialDataChartArrayAdapter(Context context,
				List<FinancialDataChartItem> objects) {
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
			return mFinancialDataChartItemList.size();
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
