package com.android.orion;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.regression.SimpleRegression;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.orion.database.Setting;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockMatch;
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

public class StockMatchChartListActivity extends StorageActivity implements
		OnChartGestureListener {

	public static final String EXTRA_STOCK_MATCH_ID = "stock_match_id";

	static final int ITEM_VIEW_TYPE_MAIN = 0;
	static final int ITEM_VIEW_TYPE_SUB = 1;
	static final int STOCK_PERIOD_ARRAY_SIZE = 7;
	static final int FLING_DISTANCE = 50;
	static final int FLING_VELOCITY = 100;

	static final int EXECUTE_LOAD_STOCK_MATCH_LIST = 0;
	static final int EXECUTE_LOAD_STOCK_DATA_LIST = 1;

	static final long RESULT_LOAD_STOCK_MATCH_LIST_SUCCESS = RESULT_SUCCESS + 1;

	int mStockMatchListIndex = 0;

	Stock mStock_X;
	Stock mStock_Y;
	StockMatch mStockMatch;

	ArrayList<StockData> mStockDataList_X = null;
	ArrayList<StockData> mStockDataList_Y = null;
	ArrayList<StockMatch> mStockMatchList = null;

	Menu mMenu = null;
	SparseArray<String> mPeriodArray = null;

	String mSortOrder = null;

	ListView mListView = null;
	StockMatchChartArrayAdapter mStockMatchChartArrayAdapter = null;
	ArrayList<StockMatchChartItem> mStockMatchChartItemList = null;
	ArrayList<StockMatchChartItemMain> mStockMatchChartItemMainList = null;
	ArrayList<StockMatchChartItemSub> mStockMatchChartItemSubList = null;
	ArrayList<StockMatchChartData> mStockMatchChartDataList = null;

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
					if (intent.getLongExtra(Constants.EXTRA_STOCK_ID, 0) == mStock_X
							.getId()
							|| intent.getLongExtra(Constants.EXTRA_STOCK_ID, 0) == mStock_Y
									.getId()) {
						startLoadTask(EXECUTE_LOAD_STOCK_MATCH_LIST);
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

		setContentView(R.layout.activity_stock_match_chart_list);

		fillPeriodArray();

		initListView();

		if (mStock_X == null) {
			mStock_X = Stock.obtain();
		}

		if (mStock_Y == null) {
			mStock_Y = Stock.obtain();
		}

		if (mStockMatch == null) {
			mStockMatch = StockMatch.obtain();
		}

		if (mStockDataList_X == null) {
			mStockDataList_X = new ArrayList<StockData>();
		}

		if (mStockDataList_Y == null) {
			mStockDataList_Y = new ArrayList<StockData>();
		}

		if (mStockMatchList == null) {
			mStockMatchList = new ArrayList<StockMatch>();
		}

		mStockMatch.setId(getIntent().getLongExtra(EXTRA_STOCK_MATCH_ID, 0));

		mSortOrder = getIntent().getStringExtra(
				Setting.KEY_SORT_ORDER_STOCK_LIST);

		LocalBroadcastManager.getInstance(this).registerReceiver(
				mBroadcastReceiver,
				new IntentFilter(Constants.ACTION_SERVICE_FINISHED));

		startLoadTask(EXECUTE_LOAD_STOCK_MATCH_LIST);

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
			navigateStockMatch(-1);
			return true;
		}
		case R.id.action_next: {
			navigateStockMatch(1);
			return true;
		}
		case R.id.action_clean_data: {
			deleteStockData(mStock.getId());
			startService(Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE,
					Constants.EXECUTE_IMMEDIATE, mStock.getSE(),
					mStock.getCode());
			startLoadTask(EXECUTE_LOAD_STOCK_MATCH_LIST);
			return true;
		}
		case R.id.action_remove_favorite: {
			updateStockMark(mStock.getId(), Constants.STOCK_FLAG_NONE);
			if (mStockMatchListIndex < mStockMatchList.size()) {
				mStockMatchList.remove(mStockMatchListIndex);
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

		mStockMatchChartItemList.clear();

		for (int i = 0; i < STOCK_PERIOD_ARRAY_SIZE; i++) {
			if (Utility.getSettingBoolean(this, mPeriodArray.get(i))) {
				mStockMatchChartItemList.add(mStockMatchChartItemMainList
						.get(i));
				mStockMatchChartItemList
						.add(mStockMatchChartItemSubList.get(i));
			}
		}

		startLoadTask(EXECUTE_LOAD_STOCK_MATCH_LIST);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mBroadcastReceiver);
	}

	@Override
	Long doInBackgroundLoad(Object... params) {
		super.doInBackgroundSave(params);

		int execute = (Integer) params[0];

		if (execute == EXECUTE_LOAD_STOCK_MATCH_LIST) {
			loadStockMatchList();

			loadStock_X();
			loadStock_Y();

			return RESULT_LOAD_STOCK_MATCH_LIST_SUCCESS;
		} else if (execute == EXECUTE_LOAD_STOCK_DATA_LIST) {
			for (int i = 0; i < STOCK_PERIOD_ARRAY_SIZE; i++) {
				loadStockDataList(mStock_X.getId(), mPeriodArray.get(i),
						mStockDataList_X);
				loadStockDataList(mStock_Y.getId(), mPeriodArray.get(i),
						mStockDataList_Y);

				if (mStockDataList_X.size() != mStockDataList_Y.size()) {
					continue;
				}

				if (mStockDataList_X.size() < Constants.STOCK_VERTEX_TYPING_SIZE) {
					continue;
				}

				setupStockMatchChat(mStockMatchChartDataList.get(i));
			}
		}

		return RESULT_SUCCESS;
	}

	@Override
	void onPostExecuteLoad(Long result) {
		super.onPostExecuteLoad(result);

		if (result == RESULT_LOAD_STOCK_MATCH_LIST_SUCCESS) {
			startLoadTask(EXECUTE_LOAD_STOCK_DATA_LIST);
		} else {
			updateTitle();
			mStockMatchChartArrayAdapter.notifyDataSetChanged();
		}
	}

	void loadStockMatchList() {
		Cursor cursor = null;

		mStockMatchList.clear();

		try {
			cursor = mStockDatabaseManager.queryStockMatch(null, null, null);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockMatch stockMatch = StockMatch.obtain();
					stockMatch.set(cursor);

					if (mStockMatch.getId() == stockMatch.getId()) {
						mStockMatch.set(cursor);
						mStockMatchListIndex = mStockMatchList.size();
					}

					mStockMatchList.add(stockMatch);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}
	}

	void loadStock_X() {
		mStock_X.setSE(mStockMatch.getSE_X());
		mStock_X.setCode(mStockMatch.getCode_X());
		mStockDatabaseManager.getStock(mStock_X);
	}

	void loadStock_Y() {
		mStock_Y.setSE(mStockMatch.getSE_Y());
		mStock_Y.setCode(mStockMatch.getCode_Y());
		mStockDatabaseManager.getStock(mStock_Y);
	}

	void loadStockDataList(long stockId, String period,
			ArrayList<StockData> stockDataList) {
		Cursor cursor = null;
		String selection = mStockDatabaseManager.getStockDataSelection(stockId,
				period);
		String sortOrder = mStockDatabaseManager.getStockDataOrder();

		if (stockDataList == null) {
			return;
		}

		stockDataList.clear();

		try {
			cursor = mStockDatabaseManager.queryStockData(selection, null,
					sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockData stockData = StockData.obtain();
					stockData.set(cursor);
					stockDataList.add(stockData);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}
	}

	void setupStockMatchChat(StockMatchChartData stockMatchChartData) {
		int index = 0;

		double x = 0;
		double y = 0;
		double minX = 0;
		double maxX = 0;
		double slope = 0;
		double intercept = 0;
		double XVal = 0;
		double fitValue = 0;
		double mean = 0;
		double std = 0;
		double delta = 0;

		SimpleRegression simpleRegression = new SimpleRegression();
		DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();

		simpleRegression.clear();
		descriptiveStatistics.clear();

		minX = mStockDataList_X.get(0).getClose();
		maxX = mStockDataList_X.get(0).getClose();
		for (int i = 0; i < mStockDataList_X.size(); i++) {
			x = mStockDataList_X.get(i).getClose();
			y = mStockDataList_Y.get(i).getClose();

			minX = Math.min(minX, x);
			maxX = Math.max(maxX, x);

			simpleRegression.addData(x, y);
		}

		if (minX == maxX) {
			return;
		}

		slope = simpleRegression.getSlope();
		intercept = simpleRegression.getIntercept();

		stockMatchChartData.clear();

		for (int i = 0; i < mStockDataList_X.size(); i++) {
			x = mStockDataList_X.get(i).getClose();
			y = mStockDataList_Y.get(i).getClose();

			index = stockMatchChartData.mXValuesMain.size();
			Entry scatterEntry = new Entry((float) y, index);
			scatterEntry.setXVal((float) Utility.Round((x - minX)
					* (mStockDataList_X.size() - 1) / (maxX - minX),
					Constants.DOUBLE_FIXED_DECIMAL - 1));
			stockMatchChartData.mScatterEntryList.add(scatterEntry);

			XVal = minX + i * (maxX - minX) / (mStockDataList_X.size() - 1);
			stockMatchChartData.mXValuesMain.add(Double.toString(Utility.Round(
					XVal, Constants.DOUBLE_FIXED_DECIMAL)));
			fitValue = XVal * slope + intercept;
			Entry fitEntry = new Entry((float) fitValue, index);
			stockMatchChartData.mFitEntryList.add(fitEntry);

			delta = y - slope * x;
			descriptiveStatistics.addValue(delta);
		}

		mean = descriptiveStatistics.getMean();
		std = descriptiveStatistics.getStandardDeviation();

		for (int i = 0; i < mStockDataList_X.size(); i++) {
			StockData stockData_X = mStockDataList_X.get(i);
			x = mStockDataList_X.get(i).getClose();
			y = mStockDataList_Y.get(i).getClose();

			index = stockMatchChartData.mXValuesSub.size();
			stockMatchChartData.mXValuesSub.add(stockData_X.getDate());

			delta = y - slope * x;

			if (std == 0) {
				delta = 0;
			} else {
				delta = (delta - mean) / std;
			}

			Entry deltaEntry = new Entry((float) delta, index);
			stockMatchChartData.mDeltaEntryList.add(deltaEntry);
		}

		stockMatchChartData.updateDescription(mStock_X);
		stockMatchChartData.updateLimitLine(mStock_X, mStock_Y);

		stockMatchChartData.setMainChartData();
		stockMatchChartData.setSubChartData();
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
			navigateStockMatch(-1);
		}

		if (me2.getX() - me1.getX() > distance
				&& Math.abs(velocityX) > velocity) {
			navigateStockMatch(1);
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

	void initListView() {
		mListView = (ListView) findViewById(R.id.listView);

		if (mStockMatchChartDataList == null) {
			mStockMatchChartDataList = new ArrayList<StockMatchChartData>();
		}

		if (mStockMatchChartItemList == null) {
			mStockMatchChartItemList = new ArrayList<StockMatchChartItem>();
		}

		if (mStockMatchChartItemMainList == null) {
			mStockMatchChartItemMainList = new ArrayList<StockMatchChartItemMain>();
		}

		if (mStockMatchChartItemSubList == null) {
			mStockMatchChartItemSubList = new ArrayList<StockMatchChartItemSub>();
		}

		for (int i = 0; i < STOCK_PERIOD_ARRAY_SIZE; i++) {
			mStockMatchChartDataList.add(new StockMatchChartData(mPeriodArray
					.get(i)));
			mStockMatchChartItemMainList.add(new StockMatchChartItemMain(
					mStockMatchChartDataList.get(i)));
			mStockMatchChartItemSubList.add(new StockMatchChartItemSub(
					mStockMatchChartDataList.get(i)));
			mStockMatchChartItemList.add(mStockMatchChartItemMainList.get(i));
			mStockMatchChartItemList.add(mStockMatchChartItemSubList.get(i));
		}

		mStockMatchChartArrayAdapter = new StockMatchChartArrayAdapter(this,
				mStockMatchChartItemList);
		mListView.setAdapter(mStockMatchChartArrayAdapter);
	}

	void updateMenuAction() {
		int size = 0;
		if (mMenu == null) {
			return;
		}

		MenuItem actionPrev = mMenu.findItem(R.id.action_prev);
		MenuItem actionNext = mMenu.findItem(R.id.action_next);

		size = mStockMatchList.size();

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
		if (mStockMatch != null) {
			setTitle(mStockMatch.getName_X() + " " + mStockMatch.getName_Y());
		}
	}

	void navigateStockMatch(int direction) {
		boolean loop = true;

		if ((mStockMatchList == null) || (mStockMatchList.size() == 0)) {
			return;
		}

		mStockMatchListIndex += direction;

		if (mStockMatchListIndex > mStockMatchList.size() - 1) {
			if (loop) {
				mStockMatchListIndex = 0;
			} else {
				mStockMatchListIndex = mStockMatchList.size() - 1;
			}
		}

		if (mStockMatchListIndex < 0) {
			if (loop) {
				mStockMatchListIndex = mStockMatchList.size() - 1;
			} else {
				mStockMatchListIndex = 0;
			}
		}

		mStockMatch = mStockMatchList.get(mStockMatchListIndex);

		startLoadTask(EXECUTE_LOAD_STOCK_MATCH_LIST);
	}

	static class MainHandler extends Handler {
		private final WeakReference<StockMatchChartListActivity> mActivity;

		MainHandler(StockMatchChartListActivity activity) {
			mActivity = new WeakReference<StockMatchChartListActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			StockMatchChartListActivity activity = mActivity.get();
			activity.updateTitle();
			activity.updateMenuAction();
		}
	}

	class StockMatchChartItem {
		int mItemViewType;
		int mResource;
		StockMatchChartData mStockMatchChartData;

		public StockMatchChartItem() {
		}

		public StockMatchChartItem(int itemViewType, int resource,
				StockMatchChartData stockMatchChartData) {
			mItemViewType = itemViewType;
			mResource = resource;
			mStockMatchChartData = stockMatchChartData;
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
				if (mItemViewType == ITEM_VIEW_TYPE_SUB) {
					for (int i = 0; i < mStockMatchChartData.mLimitLineList
							.size(); i++) {
						leftAxis.addLimitLine(mStockMatchChartData.mLimitLineList
								.get(i));
					}
				}
			}

			rightAxis = viewHolder.chart.getAxisRight();
			if (rightAxis != null) {
				rightAxis.setEnabled(false);
			}

			viewHolder.chart.setDescription(mStockMatchChartData.mDescription);

			if (mItemViewType == ITEM_VIEW_TYPE_MAIN) {
				viewHolder.chart
						.setData(mStockMatchChartData.mCombinedDataMain);
			} else {
				viewHolder.chart.setData(mStockMatchChartData.mCombinedDataSub);
			}

			return view;
		}

		class ViewHolder {
			CombinedChart chart;
		}
	}

	class StockMatchChartItemMain extends StockMatchChartItem {
		public StockMatchChartItemMain(StockMatchChartData stockMatchChartData) {
			super(ITEM_VIEW_TYPE_MAIN,
					R.layout.activity_stock_chart_list_item_main,
					stockMatchChartData);
		}
	}

	class StockMatchChartItemSub extends StockMatchChartItem {
		public StockMatchChartItemSub(StockMatchChartData stockMatchChartData) {
			super(ITEM_VIEW_TYPE_SUB,
					R.layout.activity_stock_chart_list_item_sub,
					stockMatchChartData);
		}
	}

	class StockMatchChartArrayAdapter extends ArrayAdapter<StockMatchChartItem> {

		public StockMatchChartArrayAdapter(Context context,
				List<StockMatchChartItem> objects) {
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
			return mStockMatchChartItemList.size();
		}
	}
}
