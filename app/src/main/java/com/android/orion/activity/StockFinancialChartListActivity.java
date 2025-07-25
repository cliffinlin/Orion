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
import android.os.Message;
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
import com.android.orion.chart.StockFinancialChart;
import com.android.orion.data.Period;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockBonus;
import com.android.orion.database.StockFinancial;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Search;
import com.android.orion.utility.Utility;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.Utils;
import com.markupartist.android.widget.PullToRefreshListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public class StockFinancialChartListActivity extends BaseActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnChartGestureListener {

	static final int ITEM_VIEW_TYPE_MAIN = 0;
	static final int ITEM_VIEW_TYPE_SUB = 1;
	static final int LOADER_ID_STOCK_LIST = 0;
	static final int LOADER_ID_STOCK_FINANCIAL_LIST = 1;

	int mStockListIndex = 0;
	Menu mMenu = null;

	String mSortOrder = null;
	StockFinancial mStockFinancial = new StockFinancial();

	PullToRefreshListView mListView = null;
	ArrayList<StockFinancial> mStockFinancialList = new ArrayList<>();
	StockFinancialChartArrayAdapter mStockFinancialChartArrayAdapter = null;
	ArrayList<StockFinancialChartItem> mStockFinancialChartItemList = null;
	ArrayList<StockFinancialChartItemMain> mStockFinancialChartItemMainList = null;
	ArrayList<StockFinancialChartItemSub> mStockFinancialChartItemSubList = null;
	ArrayList<StockFinancialChart> mStockFinancialChartList = null;
	ArrayList<StockBonus> mStockBonusList = new ArrayList<>();
	ArrayMap<Integer, CombinedChart> mCombinedChartMap = new ArrayMap<>();
	ChartSyncHelper mChartSyncHelper = new ChartSyncHelper();

	MainHandler mMainHandler = new MainHandler(this);
	Comparator<StockFinancial> comparator = new Comparator<StockFinancial>() {

		@Override
		public int compare(StockFinancial arg0, StockFinancial arg1) {
			if (arg0 == null || arg1 == null) {
				return 0;
			}

			Calendar calendar0;
			Calendar calendar1;

			calendar0 = Utility.getCalendar(arg0.getDate(),
					Utility.CALENDAR_DATE_TIME_FORMAT);
			calendar1 = Utility.getCalendar(arg1.getDate(),
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// For chart init only
		Utils.init(this);
		// For chart init only

		setContentView(R.layout.activity_stock_financial_chart_list);

		mStock.setId(getIntent().getLongExtra(Constant.EXTRA_STOCK_ID, DatabaseContract.INVALID_ID));
		mDatabaseManager.getStockById(mStock);
		mSortOrder = getIntent().getStringExtra(Constant.EXTRA_STOCK_LIST_SORT_ORDER);

		initListView();
		initLoader();
		updateTitle();
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
				mDatabaseManager.deleteStockFinancial(mStock);
				mDatabaseManager.deleteStockBonus(mStock);
				mBackgroundHandler.downloadStockFinancial(mStock);
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
			default:
				super.handleOnOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mStockFinancialChartItemList.clear();

		int i = 0;
		mStockFinancialChartItemList.add(mStockFinancialChartItemMainList.get(i));
		mStockFinancialChartItemList.add(mStockFinancialChartItemSubList.get(i));

		restartLoader();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mChartSyncHelper.unregisterOnChartGestureListener(this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader loader = null;

		if (id == LOADER_ID_STOCK_LIST) {
			loader = getStockCursorLoader();
		} else if (id == LOADER_ID_STOCK_FINANCIAL_LIST) {
			loader = getStockFinancialCursorLoader();
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
		} else if (id == LOADER_ID_STOCK_FINANCIAL_LIST) {
			swapStockFinancialCursor(mStockFinancialChartList.get(0), cursor);
		} else {
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
		} else if (id == LOADER_ID_STOCK_FINANCIAL_LIST) {
			swapStockFinancialCursor(mStockFinancialChartList.get(0), null);
		} else {
		}
	}

	void initListView() {
		mListView = findViewById(R.id.listView);

		if (mStockFinancialChartList == null) {
			mStockFinancialChartList = new ArrayList<>();
		}

		if (mStockFinancialChartItemList == null) {
			mStockFinancialChartItemList = new ArrayList<>();
		}

		if (mStockFinancialChartItemMainList == null) {
			mStockFinancialChartItemMainList = new ArrayList<>();
		}

		if (mStockFinancialChartItemSubList == null) {
			mStockFinancialChartItemSubList = new ArrayList<>();
		}

		int i = 0;
		mStockFinancialChartList.add(new StockFinancialChart(
				Period.PERIODS[i]));
		mStockFinancialChartItemMainList.add(new StockFinancialChartItemMain(
				mStockFinancialChartList.get(i)));
		mStockFinancialChartItemSubList.add(new StockFinancialChartItemSub(
				mStockFinancialChartList.get(i)));
		mStockFinancialChartItemList.add(mStockFinancialChartItemMainList.get(i));
		mStockFinancialChartItemList.add(mStockFinancialChartItemSubList.get(i));

		mStockFinancialChartArrayAdapter = new StockFinancialChartArrayAdapter(
				this, mStockFinancialChartItemList);
		mListView.setAdapter(mStockFinancialChartArrayAdapter);
		mListView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
			@Override
			public void onRefresh() {
				mListView.onRefreshComplete();
				mBackgroundHandler.downloadStockFinancial(mStock);
			}
		});
		mChartSyncHelper.registerOnChartGestureListener(this);
	}

	void initLoader() {
		mDatabaseManager.getStockById(mStock);
		mLoaderManager.initLoader(LOADER_ID_STOCK_LIST, null, this);
		mLoaderManager.initLoader(LOADER_ID_STOCK_FINANCIAL_LIST, null, this);
	}

	void restartLoader() {
		mDatabaseManager.getStockById(mStock);
		mLoaderManager.restartLoader(LOADER_ID_STOCK_LIST, null, this);
		mLoaderManager.restartLoader(LOADER_ID_STOCK_FINANCIAL_LIST, null, this);
	}

	CursorLoader getStockCursorLoader() {
		String selection = mDatabaseManager.hasFlagSelection(Stock.FLAG_FAVORITE);
		CursorLoader loader = new CursorLoader(this, DatabaseContract.Stock.CONTENT_URI,
				DatabaseContract.Stock.PROJECTION_ALL, selection, null,
				mSortOrder);

		return loader;
	}

	CursorLoader getStockFinancialCursorLoader() {
		String selection = "";
		String sortOrder = "";
		CursorLoader loader = null;

		selection = mDatabaseManager.getStockSelection(mStock);

		sortOrder = mDatabaseManager.getStockFinancialOrder();

		loader = new CursorLoader(this,
				DatabaseContract.StockFinancial.CONTENT_URI,
				DatabaseContract.StockFinancial.PROJECTION_ALL, selection, null,
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

	public void swapStockFinancialCursor(StockFinancialChart stockFinancialChart,
	                                     Cursor cursor) {
		int index = 0;
		double unit = Constant.DOUBLE_CONSTANT_YI;

		if (mStockFinancial == null) {
			return;
		}

		String sortOrder = DatabaseContract.COLUMN_DATE + " ASC ";
		mDatabaseManager.getStockBonusList(mStock, mStockBonusList,
				sortOrder);

		stockFinancialChart.clear();

		try {
			if ((cursor != null) && (cursor.getCount() > 0)) {
				String dateString = "";

				while (cursor.moveToNext()) {
					index = stockFinancialChart.mXValues.size();
					mStockFinancial.set(cursor);

					dateString = mStockFinancial.getDate();
					stockFinancialChart.mXValues.add(dateString);

					Entry mainBusinessIncomeEntry = new Entry(
							(float) mStockFinancial.getMainBusinessIncome()
									/ (float) unit, index);
					stockFinancialChart.mMainBusinessIncomeEntryList
							.add(mainBusinessIncomeEntry);

					Entry mainBusinessIncomeInYearEntry = new Entry(
							(float) mStockFinancial.getMainBusinessIncomeInYear()
									/ (float) unit, index);
					stockFinancialChart.mMainBusinessIncomeInYearEntryList
							.add(mainBusinessIncomeInYearEntry);

					Entry netProfitEntry = new Entry(
							(float) mStockFinancial.getNetProfit()
									/ (float) unit, index);
					stockFinancialChart.mNetProfitEntryList.add(netProfitEntry);

					Entry netProfitInYearEntry = new Entry(
							(float) mStockFinancial.getNetProfitInYear()
									/ (float) unit, index);
					stockFinancialChart.mNetProfitInYearEntryList.add(netProfitInYearEntry);

					Entry stockShareEntry = new Entry(
							(float) mStockFinancial.getShare()
									/ (float) unit, index);
					stockFinancialChart.mStockShareEntryList
							.add(stockShareEntry);

					Entry priceEntry = new Entry(
							(float) mStockFinancial.getPrice(),
							index);
					stockFinancialChart.mPriceEntryList
							.add(priceEntry);

					Entry bookValuePerShareEntry = new Entry(
							(float) mStockFinancial.getBookValuePerShare(),
							index);
					stockFinancialChart.mBookValuePerShareEntryList
							.add(bookValuePerShareEntry);

					Entry cashFlowPerShareEntry = new Entry(
							(float) mStockFinancial.getCashFlowPerShare(), index);
					stockFinancialChart.mCashFlowPerShareEntryList
							.add(cashFlowPerShareEntry);

					Entry netProfitPerShareEntry = new Entry(
							(float) mStockFinancial.getNetProfitPerShare(),
							index);
					stockFinancialChart.mNetProfitPerShareEntryList
							.add(netProfitPerShareEntry);

					Entry roeEntry = new Entry((float) mStockFinancial.getRoe(),
							index);
					stockFinancialChart.mRoeEntryList.add(roeEntry);

					if (mStockBonusList.size() > 0) {
						float dividend = 0;
						StockBonus stockBonus = Search.getStockBonusByDate(dateString,
								mStockBonusList);
						if (stockBonus != null) {
							dividend = (float) (stockBonus.getDividend() / 10.0);
						}
						BarEntry stockBonusEntry = new BarEntry(dividend,
								index);
						stockFinancialChart.mDividendEntryList
								.add(stockBonusEntry);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mDatabaseManager.closeCursor(cursor);
		}

		updateTitle();

		stockFinancialChart.mDescription = mStock.getName();
		stockFinancialChart.setMainChartData();
		stockFinancialChart.setSubChartData();

		mStockFinancialChartArrayAdapter.notifyDataSetChanged();
	}

	int binarySearch(int[] arr, int l, int r, int x) {
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

			Calendar calendarMid = Utility.getCalendar(mStockFinancialList
					.get(mid).getDate(), Utility.CALENDAR_DATE_FORMAT);

			// If the element is present at the
			// middle itself
			if (calendarMid.equals(calendar))
				return mid;

			// If element is smaller than mid, then
			// it can only be present in left subarray
			if (calendar.before(calendarMid))
				return binarySearch(l, mid - 1, calendar);

			Calendar calendarMid1 = Utility.getCalendar(mStockFinancialList
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

	StockFinancial getStockFinancialByDate(String dateString) {
		int index = 0;
		StockFinancial stockFinancial = null;

		if (mStockFinancialList.size() < 1) {
			return stockFinancial;
		}

		if (TextUtils.isEmpty(dateString)) {
			return stockFinancial;
		}

		Calendar calendar = Utility.getCalendar(dateString,
				Utility.CALENDAR_DATE_FORMAT);
		Calendar calendarMin = Utility.getCalendar(
				mStockFinancialList.get(0).getDate(),
				Utility.CALENDAR_DATE_FORMAT);
		Calendar calendarMax = Utility
				.getCalendar(
						mStockFinancialList.get(mStockFinancialList.size() - 1)
								.getDate(), Utility.CALENDAR_DATE_FORMAT);

		if (calendar.before(calendarMin)) {
			return stockFinancial;
		} else if (calendar.after(calendarMax)) {
			return mStockFinancialList.get(mStockFinancialList.size() - 1);
		} else {
			index = binarySearch(0, mStockFinancialList.size() - 1, calendar);

			if ((index > 0) && (index < mStockFinancialList.size())) {
				stockFinancial = mStockFinancialList.get(index);
			}
		}

		return stockFinancial;
	}

	void navigateStock(int step) {
		if (mStockList == null || mStockList.size() == 0) {
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

		restartLoader();
	}

	void toggleFirstSwitch() {
		Setting.setDisplayMainIncome(!Setting.getDisplayMainIncome());
	}

	void toggleSecondSwitch() {
	}

	private List<Runnable> mSwitchActions = Arrays.asList(
			this::toggleFirstSwitch
	);

	private int mCurrentSwitchActionIndex = 0;

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
		mSwitchActions.get(mCurrentSwitchActionIndex).run();
		mCurrentSwitchActionIndex = (mCurrentSwitchActionIndex + 1) % mSwitchActions.size();
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

	class StockFinancialChartItem {
		int mItemViewType;
		int mResource;
		StockFinancialChart mStockFinancialChart;

		public StockFinancialChartItem() {
		}

		public StockFinancialChartItem(int itemViewType, int resource,
		                               StockFinancialChart stockFinancialChart) {
			mItemViewType = itemViewType;
			mResource = resource;
			mStockFinancialChart = stockFinancialChart;
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
//				leftYAxis.setValueFormatter(new DefaultYAxisValueFormatter(2));
				leftYAxis.removeAllLimitLines();
				if (mItemViewType == ITEM_VIEW_TYPE_MAIN) {
					for (int i = 0; i < mStockFinancialChart.mLimitLineList
							.size(); i++) {
						leftYAxis.addLimitLine(mStockFinancialChart.mLimitLineList
								.get(i));
					}
				}
			}

			rightYAxis = viewHolder.chart.getAxisRight();
			if (rightYAxis != null) {
				if (mItemViewType == ITEM_VIEW_TYPE_MAIN) {
					rightYAxis.setEnabled(true);
					rightYAxis.setDrawLabels(false);
					rightYAxis.setStartAtZero(false);
				} else {
					rightYAxis.setEnabled(true);
//					rightYAxis.setDrawLabels(false);
					rightYAxis.setStartAtZero(false);
				}
			}

			viewHolder.chart.setDescription(mStockFinancialChart.mDescription);
			if (mItemViewType == ITEM_VIEW_TYPE_MAIN) {
				viewHolder.chart.setData(mStockFinancialChart.mCombinedDataMain);
			} else {
				viewHolder.chart.setData(mStockFinancialChart.mCombinedDataSub);
			}
			mCombinedChartMap.put(position, viewHolder.chart);
			mChartSyncHelper.syncCharts(mCombinedChartMap);
			return view;
		}

		class ViewHolder {
			CombinedChart chart;
		}
	}

	class StockFinancialChartItemMain extends StockFinancialChartItem {
		public StockFinancialChartItemMain(StockFinancialChart stockFinancialChart) {
			super(ITEM_VIEW_TYPE_MAIN,
					R.layout.activity_stock_financial_chart_list_item_main,
					stockFinancialChart);
		}
	}

	class StockFinancialChartItemSub extends StockFinancialChartItem {
		public StockFinancialChartItemSub(StockFinancialChart stockFinancialChart) {
			super(ITEM_VIEW_TYPE_SUB,
					R.layout.activity_stock_financial_chart_list_item_sub,
					stockFinancialChart);
		}
	}

	class StockFinancialChartArrayAdapter extends
			ArrayAdapter<StockFinancialChartItem> {

		public StockFinancialChartArrayAdapter(Context context,
		                                       List<StockFinancialChartItem> objects) {
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
			return mStockFinancialChartItemList.size();
		}
	}
}
